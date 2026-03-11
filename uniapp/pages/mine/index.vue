<template>
  <view class="page-wrap">
    <view class="header-bg"></view>

    <view class="main-content">
      <view class="user-card">
        <view class="user-info">
          <view class="avatar-box">
            <u-avatar :src="avatar || ''" size="70" shape="circle" mode="aspectFill"></u-avatar>
          </view>
          <view class="info-content">
            <text class="name">{{ name || '未登录' }}</text>
            <text class="desc">{{ name ? 'Hi~ 欢迎回来' : '点击此处登录系统' }}</text>
          </view>

          <view class="action-btn" @click="name ? handleToInfo() : handleToLogin()">
            <text class="btn-text">{{ name ? '个人信息' : '去登录' }}</text>
            <view class="arrow-wrap">
              <u-icon name="arrow-right" size="12" color="#fff"></u-icon>
            </view>
          </view>
        </view>


      </view>

      <view class="menu-card">
        <u-cell-group :border="false">
          <u-cell title="个人信息" isLink @click="handleToInfo" :border="false" customStyle="padding: 20rpx 24rpx;">
            <template #icon>
              <view class="cell-icon bg-blue">
                <u-icon name="account-fill" size="22" color="#3b82f6"></u-icon>
              </view>
            </template>
            <template #right-icon>
              <u-icon name="arrow-right" size="14" color="#9ca3af"></u-icon>
            </template>
          </u-cell>

          <u-cell title="修改密码" isLink @click="handleToPwd" :border="false" customStyle="padding: 20rpx 24rpx;">
            <template #icon>
              <view class="cell-icon bg-indigo">
                <u-icon name="lock-fill" size="22" color="#6366f1"></u-icon>
              </view>
            </template>
            <template #right-icon>
              <u-icon name="arrow-right" size="14" color="#9ca3af"></u-icon>
            </template>
          </u-cell>

          <u-cell title="常见问题" isLink :border="false" customStyle="padding: 20rpx 24rpx;"
            @click="$modal.showToast('功能开发中~')">
            <template #icon>
              <view class="cell-icon bg-orange">
                <u-icon name="question-circle-fill" size="22" color="#f59e0b"></u-icon>
              </view>
            </template>
            <template #right-icon>
              <u-icon name="arrow-right" size="14" color="#9ca3af"></u-icon>
            </template>
          </u-cell>

          <u-cell title="系统设置" isLink :border="false" customStyle="padding: 20rpx 24rpx;"
            @click="$modal.showToast('功能开发中~')">
            <template #icon>
              <view class="cell-icon bg-gray">
                <u-icon name="setting-fill" size="22" color="#6b7280"></u-icon>
              </view>
            </template>
            <template #right-icon>
              <u-icon name="arrow-right" size="14" color="#9ca3af"></u-icon>
            </template>
          </u-cell>
        </u-cell-group>
      </view>

      <view class="logout-btn" v-if="name" @click="handleLogout">
        <text>退出登录</text>
      </view>
    </view>
  </view>
</template>

<script>
export default {
  data() {
    return {
    }
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
      this.$tab.navigateTo('/pages/mine/info/index')
    },
    handleToPwd() {
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
  background-color: #f3f4f6;
  position: relative;
}

.header-bg {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 480rpx;
  background: linear-gradient(135deg, #818cf8 0%, #6366f1 100%);
  border-bottom-left-radius: 60rpx;
  border-bottom-right-radius: 60rpx;
  z-index: 0;
}

.main-content {
  position: relative;
  z-index: 1;
  padding: 180rpx 30rpx 40rpx;
}

.user-card {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  border-radius: 32rpx;
  box-shadow: 0 16rpx 48rpx rgba(79, 70, 229, 0.15);
  padding: 40rpx;
  margin-bottom: 30rpx;
  border: 1rpx solid rgba(255, 255, 255, 0.8);

  .user-info {
    display: flex;
    align-items: center;
    margin-bottom: 40rpx;

    .avatar-box {
      border: 6rpx solid rgba(99, 102, 241, 0.15);
      border-radius: 50%;
      padding: 4rpx;
    }

    .info-content {
      flex: 1;
      margin-left: 24rpx;
      display: flex;
      flex-direction: column;

      .name {
        font-size: 38rpx;
        font-weight: 800;
        color: #1f2937;
      }

      .desc {
        font-size: 26rpx;
        color: #6b7280;
        margin-top: 8rpx;
      }
    }

    .action-btn {
      display: flex;
      align-items: center;
      background: linear-gradient(135deg, #818cf8 0%, #6366f1 100%);
      padding: 12rpx 12rpx 12rpx 24rpx;
      border-radius: 40rpx;

      .btn-text {
        font-size: 24rpx;
        font-weight: 600;
        color: #ffffff;
        margin-right: 8rpx;
      }

      .arrow-wrap {
        width: 36rpx;
        height: 36rpx;
        background: rgba(255, 255, 255, 0.2);
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;
      }
    }
  }
}

.menu-card {
  background: #ffffff;
  border-radius: 32rpx;
  overflow: hidden;
  box-shadow: 0 8rpx 30rpx rgba(0, 0, 0, 0.03);
  margin-bottom: 50rpx;

  ::v-deep .u-cell__body {
    padding: 20rpx 24rpx !important;
  }

  ::v-deep .u-cell__title-text {
    font-size: 30rpx;
    font-weight: 600;
    color: #374151;
    margin-left: 20rpx;
  }

  .cell-icon {
    width: 52rpx;
    height: 52rpx;
    border-radius: 16rpx;
    display: flex;
    align-items: center;
    justify-content: center;

    &.bg-blue {
      background: rgba(59, 130, 246, 0.1);
    }

    &.bg-indigo {
      background: rgba(99, 102, 241, 0.1);
    }

    &.bg-orange {
      background: rgba(245, 158, 11, 0.1);
    }

    &.bg-gray {
      background: rgba(107, 114, 128, 0.1);
    }
  }
}

.logout-btn {
  background: #ffffff;
  border-radius: 24rpx;
  padding: 32rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 8rpx 24rpx rgba(239, 68, 68, 0.1);

  text {
    font-size: 32rpx;
    font-weight: 600;
    color: #ef4444;
  }

  &:active {
    opacity: 0.8;
  }
}
</style>
