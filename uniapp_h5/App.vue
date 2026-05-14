<script setup>
import { onLaunch, onShow, onHide } from '@dcloudio/uni-app'
import { useStore } from 'vuex'
import updateCustomBarInfo from './libs/updateCustomBarInfo.js'
import { getToken } from '@/utils/auth'
import { requireLoginFromLaunch } from '@/utils/auth-guard'

const store = useStore()
let userInfoLoaded = false

function checkLogin(options = {}) {
  setTimeout(() => {
    const loggedIn = !!getToken()
    requireLoginFromLaunch(options)

    if (loggedIn && !userInfoLoaded) {
      userInfoLoaded = true
      store.dispatch('GetInfo').catch(() => {
        userInfoLoaded = false
      })
    }
  }, 0)
}

onLaunch((options) => {
  checkLogin(options)

  uni.getSystemInfo({
    success: function (e) {
      // #ifndef H5
      // 获取手机系统版本
      const system = e.system.toLowerCase()
      const platform = e.platform.toLowerCase()
     
      // #endif
    }
  })

  // 获取设备的状态栏信息和自定义顶栏信息
  updateCustomBarInfo().then((res) => {
    console.log(res)
    store.commit('$tStore', {
      name: 'vuex_status_bar_height',
      value: res.statusBarHeight
    })
    store.commit('$tStore', {
      name: 'vuex_custom_bar_height',
      value: res.customBarHeight
    })
  })

  // #ifdef MP-WEIXIN
  // 更新检测
  if (wx.canIUse('getUpdateManager')) {
    const updateManager = wx.getUpdateManager()
    updateManager &&
      updateManager.onCheckForUpdate((res) => {
        if (res.hasUpdate) {
          updateManager.onUpdateReady(() => {
            uni.showModal({
              title: '更新提示',
              content: '新版本已经准备就绪，是否需要重新启动应用？',
              success: (res) => {
                if (res.confirm) {
                  uni.clearStorageSync() // 更新完成后刷新storage的数据
                  updateManager.applyUpdate()
                }
              }
            })
          })

          updateManager.onUpdateFailed(() => {
            uni.showModal({
              title: '已有新版本上线',
              content: '小程序自动更新失败，请删除该小程序后重新搜索打开哟~~~',
              showCancel: false
            })
          })
        }
      })
  } else {
    uni.showModal({
      title: '提示',
      content: '当前微信版本过低，无法使用该功能，请更新到最新的微信后再重试。',
      showCancel: false
    })
  }
  // #endif
})

onShow((options) => {
  checkLogin(options)
})

onHide(() => {
  // console.log('App Hide')
})
</script>

<script>  
import config from './config'

export default {  
  globalData: {  
    SystemPlatform: '',
    config
  },  
};  
</script> 

<style lang="scss">
@import "@/css/color.scss";
@import "@/css/main.scss";
@import '@/uni_modules/tuniaoui-vue3/index.css';


.message-dot{
  width: 30rpx;
  height: 30rpx;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: #ff444f;
  border-radius: 50%;
  font-size:18rpx;
  line-height: 1;
  color:#fff;
}

.navbar-fixed{
  position: fixed;top:0;left:0;right: 0;
}

</style>
