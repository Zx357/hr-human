<template>
  <view class="page-wrap">
    <!-- 极简高质感顶部背景 -->
    <view class="header-bg"></view>

    <view class="main-content">
      <!-- 极简身份卡片 -->
      <view class="user-card" @click="handleToInfo">
        <view class="user-info">
          <view class="avatar-wrap">
            <u-avatar :src="avatar || ''" size="56" shape="circle" mode="aspectFill"></u-avatar>
          </view>
          <view class="info-content">
            <template v-if="name">
              <view class="name-line">
                <text class="name">{{ name }}</text>
                <view class="tag">正式员工</view>
              </view>
              <text class="desc">保持热爱，奔赴山海</text>
            </template>
            <template v-else>
              <view class="name-line" @click.stop="handleToLogin">
                <text class="name">点击登录</text>
              </view>
              <text class="desc">登录以体验完整功能</text>
            </template>
          </view>
          <view class="arrow-icon">
            <u-icon name="arrow-right" color="#c0c4cc" size="28"></u-icon>
          </view>
        </view>
      </view>

      <!-- 服务列表工具卡 -->
      <view class="menu-section">
        <view class="section-title">我的服务</view>
        <view class="menu-card">
          <u-cell-group :border="false">
            <u-cell title="个人资料" isLink @click="handleToInfo" :border="true" customStyle="padding: 20rpx 30rpx;">
              <template #icon>
                <view class="menu-icon-box" style="background-color: #f0f7ff;">
                  <u-icon name="account-fill" size="32" color="#3b82f6"></u-icon>
                </view>
              </template>
            </u-cell>

            <u-cell title="密码安全" isLink @click="handleToPwd" :border="true" customStyle="padding: 20rpx 30rpx;">
              <template #icon>
                <view class="menu-icon-box" style="background-color: #f5f3ff;">
                  <u-icon name="lock-fill" size="32" color="#8b5cf6"></u-icon>
                </view>
              </template>
            </u-cell>

            <u-cell title="常见问题" isLink :border="true" customStyle="padding: 20rpx 30rpx;"
              @click="$modal.showToast('功能开发中~')">
              <template #icon>
                <view class="menu-icon-box" style="background-color: #fffaf0;">
                  <u-icon name="question-circle-fill" size="32" color="#f59e0b"></u-icon>
                </view>
              </template>
            </u-cell>

            <u-cell title="系统偏好" isLink :border="false" customStyle="padding: 20rpx 30rpx;"
              @click="$modal.showToast('功能开发中~')">
              <template #icon>
                <view class="menu-icon-box" style="background-color: #f3f4f6;">
                  <u-icon name="setting-fill" size="32" color="#6b7280"></u-icon>
                </view>
              </template>
            </u-cell>
          </u-cell-group>
        </view>
      </view>

      <!-- 退出操作 -->
      <view class="logout-wrap" v-if="name">
        <view class="logout-btn" @click="handleLogout" hover-class="logout-hover" :hover-stay-time="100">
          <text>退出当前账号</text>
        </view>
      </view>

    </view>
  </view>
</template>

<script>
export default {
  data() {
    return {}
  },
  computed: {
    name() {
      return this.$store.state.user.name
    },
    avatar() {
      return this.$store.state.user.avatar
    },
  },
  methods: {
    handleToInfo() {
      if (!this.name) {
        this.handleToLogin()
        return
      }
      this.$tab.navigateTo('/pages/mine/info/index')
    },
    handleToPwd() {
      if (!this.name) return
      this.$tab.navigateTo('/pages/mine/pwd/index')
    },
    handleToLogin() {
      this.$tab.reLaunch('/pages/login')
    },
    handleLogout() {
      uni.showModal({
        title: '提示',
        content: '确定注销并退出系统吗？',
        success: (res) => {
          if (res.confirm) {
            this.$store.dispatch('LogOut').then(() => {
              this.$tab.reLaunch('/pages/index')
            })
          }
        }
      })
    }
  }
}
</script>

<style lang="scss" scoped>
.page-wrap {
  min-height: 100vh;
  background-color: #f4f6f9;
  position: relative;
  padding-bottom: 40rpx;
}

/* 顶部极简沉浸区，避免突兀的色块拼接 */
.header-bg {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 400rpx;
  background: linear-gradient(180deg, #dde7fa 0%, #f4f6f9 100%);
  z-index: 0;
}

.main-content {
  position: relative;
  z-index: 1;
  padding: 180rpx 32rpx 40rpx;
}

/* 结构扎实的用户卡片 */
.user-card {
  background: #ffffff;
  border-radius: 32rpx;
  padding: 40rpx;
  box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.02);
  margin-bottom: 48rpx;
  transition: transform 0.1s;
  
  &:active {
    transform: scale(0.98);
  }

  .user-info {
    display: flex;
    align-items: center;

    .avatar-wrap {
      width: 120rpx;
      height: 120rpx;
      border-radius: 50%;
      background-color: #f3f4f6;
      border: 2rpx solid #f3f4f6;
      overflow: hidden;
      flex-shrink: 0;
    }

    .info-content {
      flex: 1;
      margin-left: 32rpx;
      display: flex;
      flex-direction: column;
      justify-content: center;

      .name-line {
        display: flex;
        align-items: center;
        
        .name {
          font-size: 40rpx;
          font-weight: 600;
          color: #1a1a1a;
          line-height: 1.2;
        }

        .tag {
          margin-left: 16rpx;
          font-size: 20rpx;
          color: #3b82f6;
          background: #eff6ff;
          padding: 6rpx 12rpx;
          border-radius: 8rpx;
          font-weight: 500;
        }
      }

      .desc {
        font-size: 26rpx;
        color: #94a3b8;
        margin-top: 12rpx;
      }
    }

    .arrow-icon {
      margin-left: 20rpx;
    }
  }
}

.menu-section {
  margin-bottom: 60rpx;

  .section-title {
    font-size: 30rpx;
    font-weight: 600;
    color: #4b5563;
    margin-bottom: 24rpx;
    padding-left: 8rpx;
  }
}

.menu-card {
  background: #ffffff;
  border-radius: 24rpx;
  overflow: hidden;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.01);

  /* uview 的线条样式重写，更浅的颜色 */
  ::v-deep .u-line {
    border-color: #f8f9fa !important;
  }

  ::v-deep .u-cell__title-text {
    font-size: 30rpx;
    font-weight: 500;
    color: #333333;
    margin-left: 28rpx;
  }

  .menu-icon-box {
    width: 56rpx;
    height: 56rpx;
    border-radius: 16rpx;
    display: flex;
    align-items: center;
    justify-content: center;
  }
}

.logout-wrap {
  padding: 0 20rpx;
}

.logout-btn {
  height: 100rpx;
  background: #ffffff;
  border-radius: 50rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.02);
  transition: all 0.2s;

  text {
    font-size: 32rpx;
    font-weight: 500;
    color: #ef4444;
  }
}

.logout-hover {
  background: #fef2f2;
}
</style>
