import store from '@/store'

// 请求(request.js)与上传(upload.js)共用的"登录过期处理中"标志：
// 防止过期后请求/上传多处同时触发重复 toast 与重复跳转
let authExpiredRedirecting = false

/**
 * 会话过期统一处理：清本地会话并回到登录页（带防重复跳转）
 * @param {Function} reject 调用方的 Promise reject
 * @param {string} message 提示文案
 */
export function handleAuthExpired(reject, message = '登录状态已过期，请重新登录。') {
  if (!authExpiredRedirecting) {
    authExpiredRedirecting = true
    uni.showToast({ icon: 'none', title: message })
    store.dispatch('ClearSession').finally(() => {
      uni.reLaunch({ url: '/pages/login' })
      setTimeout(() => {
        authExpiredRedirecting = false
      }, 500)
    })
  }
  // 与 request.js 的错误契约对齐:reject 带 message 的 Error 并标记已 toast,页面侧免二次提示
  const error = new Error(message)
  error._toastShown = true
  reject(error)
}
