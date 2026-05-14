import App from './App'
import store from './store'
import './permission'

import { createSSRApp } from 'vue'
// 不能修改导出的 createApp 方法名，不能修改从 Vue 中导入的 createSSRApp。
export function createApp() {
  const app = createSSRApp(App)
  
  // 使用 Vuex store
  app.use(store)
  
  return {
    app
  }
}
