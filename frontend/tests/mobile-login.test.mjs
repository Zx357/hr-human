import assert from 'node:assert/strict';
import { readFileSync } from 'node:fs';
import test from 'node:test';
import { fileURLToPath } from 'node:url';
import { runInNewContext } from 'node:vm';
import * as vue from 'vue';
import ts from 'typescript';

const mobileRoot = new URL('../../uniapp_h5/', import.meta.url);

function loadModule(path, modules, globals) {
  const filename = fileURLToPath(new URL(path, mobileRoot));
  let source = readFileSync(filename, 'utf8');
  if (path.endsWith('.vue')) {
    source = source.match(/<script setup[^>]*>([\s\S]*?)<\/script>/)[1];
    source += '\nmodule.exports = { handleLogin, loginForm, loading, failHint };';
  }
  const output = { exports: {} };
  const code = ts.transpileModule(source, {
    compilerOptions: { module: ts.ModuleKind.CommonJS, target: ts.ScriptTarget.ES2022 }
  }).outputText;
  runInNewContext(
    code,
    {
      ...globals,
      module: output,
      exports: output.exports,
      require(id) {
        if (id in modules) return modules[id];
        throw new Error(`Missing dependency: ${id}`);
      }
    },
    { filename }
  );
  return output.exports;
}

// Use the installed uni-app adapter: no callbacks returns a Promise, while
// callback mode preserves the native SocketTask. Only the transport is fake.
function wrapSocketApi(platform, nativeConnect) {
  const path =
    platform === 'h5'
      ? 'node_modules/@dcloudio/uni-h5/dist/uni-h5.es.js'
      : 'node_modules/@dcloudio/uni-mp-weixin/dist/uni.api.esm.js';
  const source = readFileSync(new URL(path, mobileRoot), 'utf8');
  const ast = ts.createSourceFile(path, source, ts.ScriptTarget.Latest, true);
  const adapter = ast.statements.find(node => ts.isFunctionDeclaration(node) && node.name?.text === 'promisify');
  assert.ok(adapter, 'installed uni-app promisify adapter must exist');
  return runInNewContext(`${adapter.getText(ast)}\npromisify('connectSocket', nativeConnect)`, {
    nativeConnect,
    hasCallback: options => ['success', 'fail', 'complete'].some(key => typeof options[key] === 'function'),
    shouldPromise: () => true,
    isFunction: value => typeof value === 'function',
    extend: Object.assign,
    wrapperReturnValue: (_name, value) => value,
    handlePromise: value => value,
    invokeApi: (...args) => {
      const [, api, options, rest] = args;
      return api(options, ...rest);
    }
  });
}

function createLoginHarness(platform, { socketThrows = false, loginGate } = {}) {
  const stored = new Map();
  const navigations = [];
  const timers = [];
  const calls = { login: 0, employee: 0, socket: 0, close: 0 };
  const events = {};
  const task = {
    onOpen: callback => {
      events.open = callback;
    },
    onMessage: callback => {
      events.message = callback;
    },
    onError: callback => {
      events.error = callback;
    },
    onClose: callback => {
      events.close = callback;
    },
    close: () => {
      calls.close += 1;
    },
    send() {}
  };
  const uni = {
    getStorageSync: key => stored.get(key) || '',
    setStorageSync: (key, value) => stored.set(key, value),
    removeStorageSync: key => stored.delete(key),
    onNetworkStatusChange() {},
    showLoading() {},
    hideLoading() {},
    showToast() {},
    reLaunch: options => {
      navigations.push(options.url);
    },
    connectSocket: wrapSocketApi(platform, options => {
      calls.socket += 1;
      if (socketThrows) throw new Error('Socket creation failed');
      queueMicrotask(() => {
        options.success?.({});
        events.open?.();
      });
      return task;
    })
  };
  const globals = {
    uni,
    setTimeout: (callback, delay) => {
      timers.push({ callback, delay });
      return timers.length;
    },
    clearTimeout() {},
    setInterval: () => 1,
    clearInterval() {}
  };
  const auth = loadModule('utils/auth.js', {}, globals);
  const config = { default: { baseUrl: 'http://test.invalid/api' } };
  const socket = loadModule('utils/websocket.js', { '@/config': config, '@/utils/auth': auth }, globals);
  const constants = loadModule('utils/constant.js', {}, globals);
  const storage = loadModule('utils/storage.js', { './constant': constants }, globals);
  const user = loadModule(
    'store/modules/user.js',
    {
      '@/config': config,
      '@/utils/storage': storage,
      '@/utils/constant': constants,
      '@/utils/auth': auth,
      '@/utils/websocket': socket,
      '@/api/login': {
        async login() {
          calls.login += 1;
          await loginGate;
          return { code: 200, data: { token: 'isolated-test-token' } };
        },
        async getCurrentEmployee() {
          calls.employee += 1;
          return { code: 200, data: { id: 1, name: 'Test Employee' } };
        },
        async logout() {}
      }
    },
    globals
  ).default;
  const state = user.state();
  const commit = (name, value) => user.mutations[name]?.(state, value);
  const store = { dispatch: (name, payload) => user.actions[name]({ commit }, payload) };
  const page = loadModule(
    'pages/login.vue',
    {
      vue,
      vuex: { useStore: () => store },
      '@dcloudio/uni-app': { onLoad() {} },
      '@/libs/composables': { useGoBack: () => ({ goBack() {} }) },
      '@/utils/auth': auth,
      '@/utils/auth-guard': { WORKBENCH_PAGE: '/pages/index?index=2', resolveRedirectUrl: url => url }
    },
    globals
  );
  page.loginForm.value = { employeeNo: 'test-employee', password: 'test-password' };
  return { page, calls, navigations, socket, events, timers, state };
}

for (const platform of ['h5', 'mp-weixin']) {
  test(`${platform}: a single login click reaches the workbench and connects chat`, async () => {
    const harness = createLoginHarness(platform);
    await harness.page.handleLogin();
    assert.deepEqual(harness.navigations, ['/pages/index?index=2']);
    assert.equal(harness.calls.login, 1);
    assert.equal(harness.calls.employee, 1);
    assert.equal(harness.calls.socket, 1);
    assert.equal(harness.page.failHint.value, '');
    assert.equal(harness.page.loading.value, false);
    assert.equal(harness.socket.isOpen(), true);
    harness.socket.disconnect();
    assert.equal(harness.calls.close, 1);
  });
}

test('rapid repeated login clicks submit credentials only once', async () => {
  let finishLogin;
  const loginGate = new Promise(resolve => {
    finishLogin = resolve;
  });
  const harness = createLoginHarness('h5', { loginGate });
  const firstClick = harness.page.handleLogin();
  await harness.page.handleLogin();
  assert.equal(harness.calls.login, 1);
  assert.equal(harness.page.loading.value, true);
  finishLogin();
  await firstClick;
  assert.equal(harness.navigations.length, 1);
});

test('chat startup failure does not fail successful login and schedules a retry', async () => {
  const harness = createLoginHarness('h5', { socketThrows: true });
  await harness.page.handleLogin();
  assert.deepEqual(harness.navigations, ['/pages/index?index=2']);
  assert.equal(harness.page.failHint.value, '');
  assert.equal(harness.calls.employee, 1);
  assert.equal(harness.socket.isOpen(), false);
  assert.equal(harness.timers.length, 1);
  assert.equal(harness.timers[0].delay, 1000);
});
