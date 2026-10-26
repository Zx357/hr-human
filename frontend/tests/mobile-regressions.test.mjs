import assert from 'node:assert/strict';
import { readFileSync } from 'node:fs';
import { createRequire } from 'node:module';
import test from 'node:test';
import { fileURLToPath } from 'node:url';
import { runInNewContext } from 'node:vm';
import * as vue from 'vue';
import { compileTemplate, parse } from '@vue/compiler-sfc';
import { renderToString } from '@vue/server-renderer';
import ts from 'typescript';

const require = createRequire(import.meta.url);

function loadPage(relativePath, bindings, modules = {}) {
  const filename = fileURLToPath(new URL(`../../uniapp_h5/${relativePath}`, import.meta.url));
  const source = readFileSync(filename, 'utf8').match(/<script setup[^>]*>([\s\S]*?)<\/script>/)[1];
  const code = ts.transpileModule(`${source}\nmodule.exports = { ${bindings} };`, {
    compilerOptions: { module: ts.ModuleKind.CommonJS, target: ts.ScriptTarget.ES2022, esModuleInterop: true }
  }).outputText;
  const sandbox = {
    exports: {},
    module: { exports: {} },
    defineOptions() {},
    uni: { getStorageSync: () => ({}), showToast() {}, stopPullDownRefresh() {} },
    require(id) {
      if (id in modules) return modules[id];
      if (id === 'vue') return { ...vue, onMounted() {}, watch() {} };
      if (id === '@dcloudio/uni-app') return { onLoad() {}, onShow() {}, onPullDownRefresh() {}, onReachBottom() {} };
      if (id === '@/libs/composables') {
        return { useCustomBarHeight: () => ({ vuex_custom_bar_height: vue.ref(0) }), useGoBack: () => ({ goBack() {} }) };
      }
      if (id === 'vuex') return { useStore: () => ({ state: { user: { employeeInfo: { id: 1 } } } }) };
      if (id === '@/utils/common') return { toastRequestError() {} };
      if (id.startsWith('@/')) throw new Error(`Missing dependency: ${id}`);
      return require(id);
    }
  };
  runInNewContext(code, sandbox, { filename });
  return sandbox.module.exports;
}

function deferred() {
  let resolve;
  let reject;
  const promise = new Promise((res, rej) => { resolve = res; reject = rej; });
  return { promise, resolve, reject };
}

test('notice tab changes load the new filter and ignore a slower previous response', async () => {
  const requests = [];
  const page = loadPage('homePages/notice.vue', 'loadNotices, currentTab, list, loading, refreshing', {
    '@/api/system/notice': { getMobileNotices: params => {
      const pending = { ...deferred(), params };
      requests.push(pending);
      return pending.promise;
    } }
  });
  const oldRequest = page.loadNotices();
  page.currentTab.value = 2;
  const newRequest = page.loadNotices();
  assert.equal(requests.length, 2);
  assert.equal(requests[1].params.noticeType, 2);
  requests[1].resolve({ data: { records: [{ id: 2 }] } });
  await newRequest;
  requests[0].resolve({ data: { records: [{ id: 1 }] } });
  await oldRequest;
  assert.deepEqual([...page.list.value.map(item => item.id)], [2]);
  assert.equal(page.loading.value, false);
  assert.equal(page.refreshing.value, false);
});

test('notice pagination cannot append records from the previous tab', async () => {
  const requests = [];
  const page = loadPage('homePages/notice.vue', 'loadNotices, loadMore, currentTab, list, finished', {
    '@/api/system/notice': { getMobileNotices: () => {
      const pending = deferred();
      requests.push(pending);
      return pending.promise;
    } }
  });
  page.list.value = [{ id: 1 }];
  const oldPage = page.loadMore();
  page.currentTab.value = 1;
  const newTab = page.loadNotices();
  requests[1].resolve({ data: { records: [{ id: 3 }] } });
  await newTab;
  requests[0].resolve({ data: { records: [{ id: 2 }] } });
  await oldPage;
  assert.deepEqual([...page.list.value.map(item => item.id)], [3]);
});

test('payslip month changes ignore earlier responses', async () => {
  const requests = [];
  const page = loadPage('minePages/payslip.vue', 'loadData, filterMonth, payslips, loading', {
    '@/api/salary': { getMyPayslips: () => {
      const pending = deferred();
      requests.push(pending);
      return pending.promise;
    } }
  });
  page.filterMonth.value = '2026-08';
  const oldMonth = page.loadData(true);
  page.filterMonth.value = '2026-09';
  const newMonth = page.loadData(true);
  requests[1].resolve({ data: { records: [{ id: 9 }], total: 1 } });
  await newMonth;
  requests[0].resolve({ data: { records: [{ id: 8 }], total: 1 } });
  await oldMonth;
  assert.deepEqual([...page.payslips.value.map(item => item.id)], [9]);
  assert.equal(page.loading.value, false);
});

test('payslip refresh clears records that are no longer returned', async () => {
  const page = loadPage('minePages/payslip.vue', 'refreshLoaded, payslips, finished', {
    '@/api/salary': { getMyPayslips: async () => ({ data: { records: [], total: 0 } }) }
  });
  page.payslips.value = [{ id: 1 }];
  await page.refreshLoaded();
  assert.equal(page.payslips.value.length, 0);
  assert.equal(page.finished.value, true);
});

test('calendar month changes retain the current request loading state and data', async () => {
  const requests = [];
  const page = loadPage('workPages/calendar.vue', 'loadCalendar, currentMonth, recordMap, loading', {
    '@/api/attendance': { getMonthAttendance: () => {
      const pending = deferred();
      requests.push(pending);
      return pending.promise;
    } }
  });
  page.currentMonth.value = 8;
  const oldMonth = page.loadCalendar();
  page.currentMonth.value = 9;
  const newMonth = page.loadCalendar();
  requests[0].resolve({ data: { records: [{ attDate: '2026-08-01' }] } });
  await oldMonth;
  assert.equal(page.loading.value, true);
  assert.equal(page.recordMap.value['2026-08-01'], undefined);
  requests[1].resolve({ data: { records: [{ attDate: '2026-09-01' }] } });
  await newMonth;
  assert.ok(page.recordMap.value['2026-09-01']);
  assert.equal(page.loading.value, false);
});

test('approval pagination reads the top-level total of flattened rows responses', () => {
  for (const file of ['homePages/approval.vue', 'homePages/pending.vue']) {
    const page = loadPage(file, 'normalizeTotal', { '@/api/application': {} });
    const records = Array.from({ length: 10 }, (_, index) => ({ id: index }));
    assert.equal(page.normalizeTotal({ data: records, total: 35 }, records), 35);
  }
});

test('business trip details use days and leave details use hours', () => {
  const page = loadPage('homePages/approval-details.vue', 'detail, infoRows', { '@/api/application': {} });
  page.detail.value = { appType: 'business', duration: 2 };
  assert.equal(page.infoRows.value.find(row => row.label === '时长').value, '2天');
  page.detail.value = { appType: 'leave', duration: 2 };
  assert.equal(page.infoRows.value.find(row => row.label === '时长').value, '2小时');
});

test('approval details without an ID show a failure state instead of loading forever', async () => {
  const page = loadPage('homePages/approval-details.vue', 'loadDetail, loadFailed', { '@/api/application': {} });
  await page.loadDetail();
  assert.equal(page.loadFailed.value, true);
});

test('withdrawn applications are not presented as approved in the progress steps', () => {
  const listPage = loadPage('homePages/approval.vue', 'steps', { '@/api/application': {} });
  assert.equal(listPage.steps(3).some(step => step.name === '已审批'), false);
  const detailPage = loadPage('homePages/approval-details.vue', 'detail, timeline', { '@/api/application': {} });
  detailPage.detail.value = { status: 3 };
  assert.equal(detailPage.timeline.value.some(step => step.name === '已审批'), false);
  assert.equal(detailPage.timeline.value[2].name, '已撤销');
});

test('hidden payslip amounts also hide calculation notes containing salary amounts', async () => {
  const filename = fileURLToPath(new URL('../../uniapp_h5/minePages/payslip-detail.vue', import.meta.url));
  const { descriptor } = parse(readFileSync(filename, 'utf8'));
  const template = compileTemplate({ filename, id: 'payslip-mask', source: descriptor.template.content });
  const compiledExports = {};
  const code = ts.transpileModule(template.code, {
    compilerOptions: { module: ts.ModuleKind.CommonJS, target: ts.ScriptTarget.ES2022 }
  }).outputText;
  runInNewContext(code, { exports: compiledExports, require: () => vue });
  const context = {
    loading: false, loadFailed: false, vuex_custom_bar_height: 0, yearMonth: '2026-08',
    detail: { employeeName: 'Tester', netPay: 7300, grossPay: 7500, totalDeduction: 200 },
    incomeItems: [{ id: 1, itemName: 'Income', amount: 100, source: '5000 × 0.08' }],
    deductionItems: [{ id: 2, itemName: 'Deduction', amount: 100, source: '5000 × 0.12' }],
    confirming: false, amountVisible: false, formatAmount: value => String(value)
  };
  async function renderPayslip() {
    const app = vue.createSSRApp({ render: () => compiledExports.render(context, []) });
    app.config.warnHandler = () => {};
    return renderToString(app);
  }
  assert.equal((await renderPayslip()).includes('5000'), false);
  context.amountVisible = true;
  assert.equal((await renderPayslip()).includes('5000'), true);
});
