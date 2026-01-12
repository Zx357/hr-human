import { createApp } from 'vue';
import './plugins/assets';
import {
  setupAppVersionNotification,
  setupDayjs,
  setupIconifyOffline,
  setupLoading,
  setupNProgress,
  setupUI
} from './plugins';
import { setupStore } from './store';
import { setupRouter } from './router';
import { setupI18n } from './locales';
import { setupPermissionDirective } from './directives/permission';
import App from './App.vue';

async function setupApp() {
  setupLoading();

  setupNProgress();

  setupIconifyOffline();

  setupDayjs();

  const app = createApp(App);

  setupUI(app);

  setupStore(app);

  await setupRouter(app);

  setupI18n(app);

  // 注册权限指令
  setupPermissionDirective(app);

  setupAppVersionNotification();

  app.mount('#app');
}

setupApp();
