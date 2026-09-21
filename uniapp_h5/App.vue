<script setup>
import { onLaunch, onShow, onHide } from '@dcloudio/uni-app'
import { useStore } from 'vuex'
import updateCustomBarInfo from './libs/updateCustomBarInfo.js'
import { getToken, setToken } from '@/utils/auth'
import { requireLoginFromLaunch } from '@/utils/auth-guard'
import { connect as connectWs } from '@/utils/websocket'

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

  // 本地已有 token(上次登录未退出):启动即建立全局 WS 单例连接(幂等)
  if (getToken()) {
    connectWs()
  }

  // 获取设备的状态栏信息和自定义顶栏信息
  updateCustomBarInfo().then((res) => {
    store.commit('$tStore', {
      name: 'vuex_status_bar_height',
      value: res.statusBarHeight
    })
    store.commit('$tStore', {
      name: 'vuex_custom_bar_height',
      value: res.customBarHeight
    })
  }).catch(() => {})

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
                  // 更新前保留登录态与记住的账号:先暂存,清理后恢复,避免更新后强制重新登录/丢失预填工号
                  const token = getToken()
                  const rememberedEmployeeNo = uni.getStorageSync('rememberedEmployeeNo')
                  uni.clearStorageSync() // 更新完成后刷新storage的数据
                  if (token) setToken(token)
                  if (rememberedEmployeeNo) uni.setStorageSync('rememberedEmployeeNo', rememberedEmployeeNo)
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

/* ===== 全局统一风格:浅灰底 + 白卡片 + 品牌色 ===== */
page, uni-page-body, body {
  background-color: #F8F7F8;
}

.uni-card {
  background: #ffffff;
  border-radius: 20rpx;
  box-shadow: 0 10rpx 30rpx rgba(29, 37, 65, 0.06);
}

.uni-card-title {
  font-size: 30rpx;
  font-weight: 700;
  color: #1d2541;
}

.uni-text-primary { color: #3668FC; }
.uni-text-success { color: #00C8B0; }
.uni-text-warning { color: #FFAC00; }
.uni-text-danger { color: #FB6A67; }


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
