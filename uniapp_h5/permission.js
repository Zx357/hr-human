import { getToken } from '@/utils/auth'
import { getLoginUrl, isLoginPage, isWhitePage, WORKBENCH_PAGE } from '@/utils/auth-guard'

;['navigateTo', 'redirectTo', 'reLaunch', 'switchTab'].forEach((method) => {
  uni.addInterceptor(method, {
    invoke(to) {
      const url = to.url || ''

      if (getToken()) {
        if (isLoginPage(url)) {
          uni.reLaunch({ url: WORKBENCH_PAGE })
          return false
        }
        return true
      }

      if (isWhitePage(url)) {
        return true
      }

      uni.reLaunch({ url: getLoginUrl(url) })
      return false
    },
    fail(err) {
      console.log(err)
    }
  })
})
