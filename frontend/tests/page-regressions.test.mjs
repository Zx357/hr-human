import assert from 'node:assert/strict';
import { readFileSync } from 'node:fs';
import { createRequire } from 'node:module';
import test from 'node:test';
import { fileURLToPath } from 'node:url';
import { runInNewContext } from 'node:vm';
import * as vue from 'vue';
import { compileScript, parse } from '@vue/compiler-sfc';
import { renderToString } from '@vue/server-renderer';
import ts from 'typescript';

const require = createRequire(import.meta.url);

// Exercise the actual page handlers with controlled API responses, without a database.
function loadSource(relativePath, bindings, mocks = {}) {
  const filename = fileURLToPath(new URL(`../${relativePath}`, import.meta.url));
  const source = readFileSync(filename, 'utf8');
  const script = source.match(/<script setup[^>]*>([\s\S]*?)<\/script>/)?.[1] || source;
  const code = ts.transpileModule(`${script}\nmodule.exports = { ${bindings} };`, {
    compilerOptions: { module: ts.ModuleKind.CommonJS, target: ts.ScriptTarget.ES2022, esModuleInterop: true }
  }).outputText;
  const sandbox = {
    module: { exports: {} },
    exports: {},
    defineOptions() {},
    require(id) {
      if (id in mocks) return mocks[id];
      if (id === 'vue') return { ...vue, onMounted() {}, watch() {} };
      if (id === '@/locales') return { $t: (key, params) => ({ key, ...params }) };
      if (id.endsWith('.vue')) return {};
      if (id.startsWith('@/')) throw new Error(`Missing test dependency: ${id}`);
      return require(id);
    }
  };
  runInNewContext(code, sandbox, { filename });
  return sandbox.module.exports;
}

const orgTreeMock = { useOrgTree: () => ({ orgTreeOptions: vue.ref([]), loadOrgTree() {} }) };

function schedulePage(api = {}) {
  return loadSource('src/views/attendance/schedule/index.vue',
    'currentMonth, monthDates, handlePrevMonth, handleNextMonth, handleCellClick, currentBrush, shifts', {
      '@/composables/use-org-tree': orgTreeMock,
      '@/service/api/schedule': api,
      '@/service/api/shift': {},
      'element-plus': { ElMessage: { warning() {} } }
    });
}

test('month navigation visits February from January 31 and March 31', () => {
  for (const year of [2024, 2025, 2026]) {
    const page = schedulePage();
    page.currentMonth.value = new Date(year, 0, 31);
    page.handleNextMonth();
    assert.equal(page.currentMonth.value.getMonth(), 1);
    assert.equal(page.monthDates.value.length, year === 2024 ? 29 : 28);
    page.currentMonth.value = new Date(year, 2, 31);
    page.handlePrevMonth();
    assert.equal(page.currentMonth.value.getMonth(), 1);
    assert.equal(page.monthDates.value[0].key, `${year}-02-01`);
  }
});

test('failed schedule saves leave the displayed shift unchanged', async () => {
  const page = schedulePage({ saveSchedule: async () => ({ error: new Error('denied') }) });
  const row = { employeeId: 1, schedule: { '2026-10-26': { shiftId: 1, shiftName: 'Day' } } };
  page.shifts.value = [{ id: 2, shiftName: 'Night' }];
  for (const brush of [2, -1]) {
    page.currentBrush.value = brush;
    // eslint-disable-next-line no-await-in-loop -- Verify consecutive edits on the same row.
    await page.handleCellClick(row, '2026-10-26');
    assert.equal(row.schedule['2026-10-26'].shiftId, 1);
  }
});

test('successful schedule saves update and clear the displayed shift', async () => {
  const page = schedulePage({ saveSchedule: async () => ({ error: null, data: true }) });
  const row = { employeeId: 1, schedule: {} };
  page.shifts.value = [{ id: 2, shiftName: 'Night' }];
  page.currentBrush.value = 2;
  await page.handleCellClick(row, '2026-10-26');
  assert.equal(row.schedule['2026-10-26'].shiftId, 2);
  page.currentBrush.value = -1;
  await page.handleCellClick(row, '2026-10-26');
  assert.equal(row.schedule['2026-10-26'], undefined);
});

test('batch approval reports resolved API failures and continues the batch', async () => {
  const messages = [];
  const seen = [];
  const page = loadSource('src/views/approval/pending/index.vue', 'batchApprove, selectedRows, batchLoading', {
    '@/constants/business': {},
    '@/constants/application': {},
    '@/utils/format': {},
    '@/composables/use-dict-options': { useDictOptions: () => ({ options: vue.ref([]) }) },
    '@/service/api/application': {
      approveApplication: async id => {
        seen.push(id);
        return { error: id === 2 ? new Error('denied') : null };
      },
      fetchPendingPage: async () => ({ data: { records: [], total: 0 } })
    },
    'element-plus': { ElMessage: { success: message => messages.push(message) } }
  });
  page.selectedRows.value = [{ id: 1 }, { id: 2 }, { id: 3 }];
  await page.batchApprove(1, '', 'batch-summary');
  assert.deepEqual(seen, [1, 2, 3]);
  assert.equal(messages[0].success, 2);
  assert.equal(messages[0].fail, 1);
  assert.equal(page.batchLoading.value, false);
});

test('failed payroll actions do not report success or reload the list', async () => {
  let successes = 0;
  let reloads = 0;
  const page = loadSource('src/views/salary/payroll/index.vue', 'withOperating, operating', {
    '@/service/api/salary': { fetchBatchPage: async () => { reloads += 1; return { data: {} }; } },
    '@/service/api/organization': {},
    '@/utils/download': {},
    'element-plus': { ElMessage: { success() { successes += 1; } } }
  });
  await page.withOperating(async () => ({ error: new Error('denied') }), 'done');
  assert.equal(successes, 0);
  assert.equal(reloads, 0);
  assert.equal(page.operating.value, false);
  await page.withOperating(async () => ({ error: null, data: null }), 'done');
  assert.equal(successes, 1);
  assert.equal(reloads, 1);
});

test('retrying a partially failed overtime batch does not duplicate successful applications', async () => {
  const submitted = [];
  let shouldFail = true;
  let successes = 0;
  const page = loadSource('src/views/application/overtime/index.vue',
    'handleSubmit, selectedEmployees, dateRange, overtimeHours, formRef, dialogVisible, submitLoading', {
      '@/constants/application': {},
      '@/composables/use-application-cancel': {},
      '@/utils/format': {},
      '@/service/api/application': {
        createApplication: async data => {
          submitted.push(data.employeeId);
          return { error: shouldFail && data.employeeId === 2 ? new Error('denied') : null };
        },
        fetchApplicationPage: async () => ({ data: { records: [], total: 0 } })
      },
      'element-plus': { ElMessage: { success() { successes += 1; } } }
    });
  page.selectedEmployees.value = [{ id: 1 }, { id: 2 }, { id: 3 }];
  page.formRef.value = { validate: async () => true };
  page.dateRange.value = ['2026-10-26 18:00', '2026-10-26 20:00'];
  page.overtimeHours.value = 2;
  page.dialogVisible.value = true;
  await page.handleSubmit();
  assert.deepEqual(submitted, [1, 2]);
  assert.deepEqual([...page.selectedEmployees.value.map(employee => employee.id)], [2, 3]);
  assert.equal(successes, 0);
  assert.equal(page.dialogVisible.value, true);
  assert.equal(page.submitLoading.value, false);
  shouldFail = false;
  await page.handleSubmit();
  assert.deepEqual(submitted, [1, 2, 2, 3]);
  assert.equal(successes, 1);
  assert.equal(page.dialogVisible.value, false);
});

test('menu saves accept a successful null payload and preserve the dialog on failure', async () => {
  let error = null;
  let successes = 0;
  const page = loadSource('src/views/system/mobile-menu/index.vue', 'submitForm, formRef, dialogVisible, loading', {
    '@/service/api/system': {
      saveMobileMenu: async () => ({ error, data: null }),
      fetchMobileMenuPage: async () => ({ data: { records: [], total: 0 } })
    },
    'element-plus': { ElMessage: { success() { successes += 1; } } }
  });
  page.formRef.value = { validate: async callback => callback(true) };
  page.dialogVisible.value = true;
  await page.submitForm();
  assert.equal(page.dialogVisible.value, false);
  assert.equal(successes, 1);
  error = new Error('denied');
  page.dialogVisible.value = true;
  await page.submitForm();
  assert.equal(page.dialogVisible.value, true);
  assert.equal(successes, 1);
  assert.equal(page.loading.value, false);
});

test('assignment page loading and checkbox restoration preserve selected employees', async () => {
  const employees = [{ id: 1, name: 'A' }, { id: 2, name: 'B' }, { id: 3, name: 'C' }];
  const page = loadSource('src/views/attendance/location/index.vue',
    'loadAssignEmployeeData, restoreAssignSelection, handleAssignSelectionChange, tempSelectedEmployees, assignEmployeeTableRef, assignEmployeeData', {
      vue: { ...vue, onMounted() {}, nextTick: async () => page.handleAssignSelectionChange([]) },
      '@/service/api/attendance': {},
      '@/service/api/hr': { fetchEmployeePage: async () => ({ error: null, data: { records: employees.slice(0, 2), total: 3 } }) },
      '@/service/api/organization': {},
      'element-plus': {}
    });
  const selected = [];
  page.tempSelectedEmployees.value = employees;
  page.assignEmployeeTableRef.value = {
    clearSelection() {
      selected.length = 0;
      page.handleAssignSelectionChange([]);
    },
    toggleRowSelection(row) {
      selected.push(row);
      page.handleAssignSelectionChange([...selected]);
    }
  };
  await page.loadAssignEmployeeData();
  assert.deepEqual([...page.tempSelectedEmployees.value.map(item => item.id)], [1, 2, 3]);
  assert.deepEqual(selected.map(item => item.id), [1, 2]);
  page.handleAssignSelectionChange([employees[1]]);
  assert.deepEqual([...page.tempSelectedEmployees.value.map(item => item.id)].sort(), [2, 3]);
});

test('HTTP 401 clears the admin session and ordinary API errors keep it', () => {
  let options;
  let resets = 0;
  let messages = 0;
  const filename = fileURLToPath(new URL('../src/service/request/index.ts', import.meta.url));
  const source = readFileSync(filename, 'utf8').replaceAll('import.meta.env', 'testEnv');
  const code = ts.transpileModule(source, {
    compilerOptions: { module: ts.ModuleKind.CommonJS, target: ts.ScriptTarget.ES2022 }
  }).outputText;
  const mocks = {
    '@sa/axios': { createFlatRequest: (_config, opts) => { options = opts; return { state: {} }; } },
    '@/store/modules/auth': { useAuthStore: () => ({ resetStore() { resets += 1; } }) },
    '@/utils/service': { getServiceBaseURL: () => ({ baseURL: '' }) },
    '@/locales': { $t: key => key },
    './shared': { showErrorMsg() { messages += 1; } }
  };
  runInNewContext(code, { exports: {}, testEnv: {}, require: id => mocks[id] || {} });
  options.onError({ config: {}, response: { status: 401, data: { code: '8888', msg: 'expired' } } });
  assert.equal(resets, 1);
  assert.equal(messages, 0);
  options.onError({ config: {}, response: { status: 400, data: { msg: 'invalid input' } } });
  assert.equal(resets, 1);
  assert.equal(messages, 1);
});

test('an explicitly empty toolbar slot hides default actions, while an omitted slot keeps them', async () => {
  const filename = fileURLToPath(new URL('../src/components/advanced/table-header-operation.vue', import.meta.url));
  const { descriptor } = parse(readFileSync(filename, 'utf8'));
  const script = compileScript(descriptor, { id: 'toolbar-regression', inlineTemplate: true });
  const compiledExports = {};
  const code = ts.transpileModule(script.content, {
    compilerOptions: { module: ts.ModuleKind.CommonJS, target: ts.ScriptTarget.ES2022 }
  }).outputText;
  runInNewContext(code, {
    exports: compiledExports,
    require: id => id === 'vue' ? vue : { $t: key => key }
  });
  async function renderToolbar(slots) {
    const app = vue.createSSRApp({ render: () => vue.h(compiledExports.default, {}, slots) });
    const passthrough = { setup: (_props, context) => () => context.slots.default?.() };
    app.component('ElSpace', passthrough);
    app.component('ElButton', passthrough);
    app.component('ElDropdown', passthrough);
    app.component('ElPopconfirm', { setup: (_props, context) => () => context.slots.reference?.() });
    app.config.warnHandler = () => {};
    return renderToString(app);
  }
  const emptySlot = await renderToolbar({ default: () => [] });
  assert.equal(emptySlot.includes('common.add'), false);
  assert.equal(emptySlot.includes('common.batchDelete'), false);
  const omittedSlot = await renderToolbar(undefined);
  assert.equal(omittedSlot.includes('common.add'), true);
  assert.equal(omittedSlot.includes('common.batchDelete'), true);
});
