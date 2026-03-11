<script>
  import config from './config'
  import { getToken } from '@/utils/auth'

  export default {
    onLaunch: function() {
      this.initApp()
    },
    methods: {
      // 初始化应用
      initApp() {
        // 初始化应用配置
        this.initConfig()
      },
      initConfig() {
        this.globalData.config = config
      }
    }
  }
</script>

<style lang="scss">
  @import "uview-plus/index.scss";
  @import '@/static/scss/index.scss';

  /* 底部TabBar美化 - H5端 */
  uni-tabbar {
    .uni-tabbar {
      border-top: none !important;
      box-shadow: 0 -4rpx 24rpx rgba(0, 0, 0, 0.06) !important;
      backdrop-filter: blur(20px);
      height: 110rpx !important;
      padding-bottom: env(safe-area-inset-bottom) !important;
    }

    .uni-tabbar__item {
      .uni-tabbar__bd {
        padding: 8rpx 0 !important;
      }

      .uni-tabbar__label {
        font-size: 22rpx !important;
        font-weight: 600 !important;
        margin-top: 4rpx !important;
      }

      .uni-tabbar__icon {
        width: 48rpx !important;
        height: 48rpx !important;
      }
    }
  }
</style>
