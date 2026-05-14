/**
 * 返回上一页 composable
 * 通过判断当前页面的页面栈信息，是否有上一页进行返回，如果没有则跳转到首页
 */
export function useGoBack() {
  const goBack = () => {
    const pages = getCurrentPages()
    if (pages && pages.length > 0) {
      const firstPage = pages[0]
      if (pages.length == 1 && (!firstPage.route || firstPage.route != 'pages/index')) {
        uni.reLaunch({
            url: '/pages/index?index=2'
        })
      } else {
        uni.navigateBack({
          delta: 1
        })
      }
    } else {
      uni.reLaunch({
        url: '/pages/index?index=2'
      })
    }
  }

  return {
    goBack
  }
}
