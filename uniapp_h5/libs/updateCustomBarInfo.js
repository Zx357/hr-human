/**
 * 更新自定义顶部导航栏的高度
 *
 * 使用同步 API:异步回调版在 H5 冷启动偶发不落地(onLaunch 的 .then 永不执行,
 * vuex_custom_bar_height 停留 0,搜索栏等内容被固定导航栏遮挡),同步获取保证
 * 任何端、任何启动时序下都有确定值。
 */
function updateCustomBarInfo () {
  try {
    const e = uni.getSystemInfoSync()
    let statusBarHeight = 0
    let customBarHeight = 0
    // #ifndef MP
    statusBarHeight = e.statusBarHeight || 0
    if (e.platform == 'android') {
      customBarHeight = statusBarHeight + 50
    } else {
      customBarHeight = statusBarHeight + 45
    }
    // #endif

    // #ifdef MP-WEIXIN
    statusBarHeight = e.statusBarHeight || 0
    const custom = wx.getMenuButtonBoundingClientRect()
    customBarHeight = custom.bottom + ((custom.top - statusBarHeight) <= 4 ? (custom.top - statusBarHeight) + 4 : (custom.top - statusBarHeight))
    // #endif

    // #ifdef MP-ALIPAY
    statusBarHeight = e.statusBarHeight || 0
    customBarHeight = statusBarHeight + e.titleBarHeight
    // #endif

    return Promise.resolve({ statusBarHeight, customBarHeight })
  } catch (err) {
    // 兜底:H5 浏览器没有状态栏,导航栏按 45px 处理
    // #ifdef H5
    return Promise.resolve({ statusBarHeight: 0, customBarHeight: 45 })
    // #endif
    // #ifndef H5
    return Promise.reject(err)
    // #endif
  }
}

export default updateCustomBarInfo
