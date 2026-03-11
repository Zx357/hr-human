<template>
  <view class="page-wrap">
    <view class="top-bg"></view>

    <view class="main-content">
      <view class="page-header">
        <text class="page-title">工作台</text>
        <text class="page-subtitle">让工作更高效</text>
      </view>

      <!-- 快捷入口区 -->
      <view class="quick-nav-card">
        <view class="nav-item" @click="goToClock">
          <view class="nav-icon" style="background: rgba(37, 99, 235, 0.1);">
            <u-icon name="map-fill" size="32" color="#2563EB"></u-icon>
          </view>
          <text class="nav-name">定位打卡</text>
        </view>
        <view class="nav-item" @click="goToMyApply">
          <view class="nav-icon" style="background: rgba(245, 158, 11, 0.1);">
            <u-icon name="order" size="32" color="#f59e0b"></u-icon>
          </view>
          <text class="nav-name">我的申请</text>
        </view>
        <view class="nav-item" @click="goToApprovalList">
          <view class="nav-icon" style="background: rgba(16, 185, 129, 0.1);">
            <u-icon name="list-dot" size="32" color="#10b981"></u-icon>
          </view>
          <text class="nav-name">待审批</text>
        </view>
        <view class="nav-item" @click="goToMyRecords">
          <view class="nav-icon" style="background: rgba(236, 72, 153, 0.1);">
            <u-icon name="calendar-fill" size="32" color="#ec4899"></u-icon>
          </view>
          <text class="nav-name">考勤记录</text>
        </view>
      </view>

      <!-- 业务应用区 -->
      <view class="section-card" v-if="applyMenus.length > 0">
        <view class="section-header">
          <text class="title">业务应用</text>
        </view>
        <view class="menu-grid">
          <view class="menu-item" v-for="(item, index) in applyMenus" :key="index" @click="handleMenuClick(item)">
            <view class="icon-cube" :style="getIconStyle(item)">
              <u-icon :name="getMenuIcon(item)" size="32" :color="getIconColor(item)"></u-icon>
            </view>
            <text class="item-text">{{ item.menuName }}</text>
          </view>
        </view>
      </view>

      <!-- 待我审批区 -->
      <view class="section-card" style="margin-bottom: 30rpx;">
        <view class="section-header">
          <text class="title">待我审批</text>
          <view class="more-btn" @click="goToApprovalList">
            <text>更多</text>
            <u-icon name="arrow-right" size="12" color="#9ca3af" customStyle="margin-left:4rpx"></u-icon>
          </view>
        </view>

        <view class="approval-list" v-if="pendingList.length > 0">
          <view class="approval-item" v-for="(item, index) in pendingList" :key="index"
            @click="goToApprovalDetail(item)">
            <view class="approval-left">
              <view class="avatar-box">{{ getFirstChar(item.applicantName) }}</view>
              <view class="info-content">
                <view class="type-name">{{ item.typeName }}</view>
                <view class="meta-info">
                  <text class="applicant">{{ item.applicantName }}</text>
                  <text class="dot">·</text>
                  <text class="time">{{ item.createTime }}</text>
                </view>
              </view>
            </view>
            <view class="approval-right">
              <view class="action-btn">审批</view>
            </view>
          </view>
        </view>

        <u-empty v-else mode="list" text="当前没有待办事项" iconSize="80" customStyle="padding: 60rpx 0;"></u-empty>
      </view>
    </view>
  </view>
</template>

<script>
import { getMobileMenus } from '@/api/menu'

export default {
  data() {
    return {
      applyMenus: [],
      pendingList: []
    }
  },
  onShow() {
    this.loadMenus()
  },
  methods: {
    getFirstChar(name) {
      if (!name) return '?'
      return name.charAt(0)
    },
    getMenuIcon(item) {
      if (!item.menuName) return 'star-fill'
      const name = item.menuName
      if (name.includes('假')) return 'calendar-fill'
      if (name.includes('转正')) return 'account-fill'
      if (name.includes('加班')) return 'clock-fill'
      if (name.includes('补卡')) return 'edit-pen-fill'
      if (name.includes('出差')) return 'car-fill'
      if (name.includes('离职')) return 'minus-circle-fill'
      if (name.includes('调动')) return 'list-dot'
      if (name.includes('换休')) return 'reload'
      return 'star-fill'
    },
    getIconStyle(item) {
      const colors = {
        'calendar-fill': { bg: 'rgba(56, 189, 248, 0.15)' },
        'account-fill': { bg: 'rgba(52, 211, 153, 0.15)' },
        'clock-fill': { bg: 'rgba(251, 146, 60, 0.15)' },
        'edit-pen-fill': { bg: 'rgba(99, 102, 241, 0.15)' },
        'car-fill': { bg: 'rgba(244, 114, 182, 0.15)' },
        'minus-circle-fill': { bg: 'rgba(248, 113, 113, 0.15)' },
        'list-dot': { bg: 'rgba(167, 139, 250, 0.15)' },
        'reload': { bg: 'rgba(52, 211, 153, 0.15)' },
        'star-fill': { bg: 'rgba(251, 191, 36, 0.15)' }
      }
      let iconName = this.getMenuIcon(item)
      return { backgroundColor: (colors[iconName] || colors['star-fill']).bg }
    },
    getIconColor(item) {
      const colors = {
        'calendar-fill': '#0ea5e9',
        'account-fill': '#10b981',
        'clock-fill': '#f97316',
        'edit-pen-fill': '#6366f1',
        'car-fill': '#ec4899',
        'minus-circle-fill': '#ef4444',
        'list-dot': '#8b5cf6',
        'reload': '#10b981',
        'star-fill': '#f59e0b'
      }
      let iconName = this.getMenuIcon(item)
      return colors[iconName] || colors['star-fill']
    },
    async loadMenus() {
      try {
        const res = await getMobileMenus()
        if (res.code === 200 && res.data) {
          this.applyMenus = res.data.apply || []
        }
      } catch (e) {
        console.log('加载菜单失败', e)
      }
    },
    handleMenuClick(item) {
      if (item.path) {
        uni.navigateTo({ url: item.path })
      }
    },
    goToClock() {
      uni.navigateTo({ url: '/pages/clock/index' })
    },
    goToMyApply() {
      uni.navigateTo({ url: '/pages/apply/list/index' })
    },
    goToMyRecords() {
      uni.navigateTo({ url: '/pages/attendance/index' })
    },
    goToList(type) {
      const statusMap = { pending: 0, approved: 1, rejected: 2 }
      const status = statusMap[type]
      uni.navigateTo({ url: '/pages/apply/list/index' + (status !== undefined ? '?status=' + status : '') })
    },
    goToApprovalList() {
      this.$modal.showToast('功能开发中~')
    },
    goToApprovalDetail(item) {
      this.$modal.showToast('功能开发中~')
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

.top-bg {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 340rpx;
  background: #f3f4f6;
  z-index: 0;
}

.main-content {
  position: relative;
  z-index: 1;
  padding: 0 30rpx;
}

.page-header {
  padding: 100rpx 0 40rpx;

  .page-title {
    display: block;
    font-size: 48rpx;
    font-weight: 800;
    color: #111827;
  }

  .page-subtitle {
    display: block;
    font-size: 28rpx;
    color: #6b7280;
    margin-top: 12rpx;
  }
}

.quick-nav-card {
  background: #ffffff;
  border-radius: 32rpx;
  padding: 40rpx 20rpx;
  display: flex;
  justify-content: space-around;
  margin-bottom: 30rpx;
  box-shadow: 0 8rpx 30rpx rgba(0, 0, 0, 0.03);

  .nav-item {
    display: flex;
    flex-direction: column;
    align-items: center;

    .nav-icon {
      width: 100rpx;
      height: 100rpx;
      border-radius: 30rpx;
      display: flex;
      align-items: center;
      justify-content: center;
      margin-bottom: 16rpx;
      transition: all 0.2s;

      &:active {
        transform: scale(0.95);
      }
    }

    .nav-name {
      font-size: 26rpx;
      color: #4b5563;
      font-weight: 600;
    }
  }
}

.section-card {
  background: #ffffff;
  border-radius: 32rpx;
  padding: 40rpx 30rpx;
  margin-bottom: 30rpx;
  box-shadow: 0 8rpx 30rpx rgba(0, 0, 0, 0.03);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 40rpx;

  .title {
    font-size: 34rpx;
    font-weight: 800;
    color: #111827;
  }

  .more-btn {
    display: flex;
    align-items: center;
    font-size: 26rpx;
    color: #6b7280;
  }
}

.menu-grid {
  display: flex;
  flex-wrap: wrap;
}

.menu-item {
  width: 25%;
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 30rpx;

  .icon-cube {
    width: 96rpx;
    height: 96rpx;
    border-radius: 28rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    margin-bottom: 16rpx;
    transition: all 0.2s ease;

    &:active {
      transform: scale(0.95);
    }
  }

  .item-text {
    font-size: 26rpx;
    color: #4b5563;
    font-weight: 600;
  }
}

.approval-list {
  .approval-item {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 30rpx 20rpx;
    margin-bottom: 20rpx;
    background: #f9fafb;
    border-radius: 24rpx;

    &:last-child {
      margin-bottom: 0;
    }

    .approval-left {
      display: flex;
      align-items: center;

      .avatar-box {
        width: 80rpx;
        height: 80rpx;
        border-radius: 24rpx;
        background: linear-gradient(135deg, #60a5fa 0%, #3b82f6 100%);
        color: #fff;
        font-size: 32rpx;
        font-weight: bold;
        display: flex;
        align-items: center;
        justify-content: center;
      }

      .info-content {
        margin-left: 24rpx;

        .type-name {
          font-size: 30rpx;
          color: #1f2937;
          font-weight: 700;
        }

        .meta-info {
          font-size: 24rpx;
          color: #6b7280;
          margin-top: 6rpx;
          display: flex;
          align-items: center;

          .dot {
            margin: 0 10rpx;
            color: #d1d5db;
          }
        }
      }
    }

    .approval-right {
      .action-btn {
        padding: 12rpx 32rpx;
        border-radius: 40rpx;
        background: #eff6ff;
        color: #2563eb;
        font-size: 26rpx;
        font-weight: 600;
      }
    }
  }
}
</style>
