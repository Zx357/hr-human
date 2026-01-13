<template>
  <view class="work-container">
    <!-- 待办统计 -->
    <view class="stat-section">
      <view class="stat-item" @click="goToList('pending')">
        <text class="stat-num">{{ stats.pending }}</text>
        <text class="stat-label">待审批</text>
      </view>
      <view class="stat-item" @click="goToList('approved')">
        <text class="stat-num">{{ stats.approved }}</text>
        <text class="stat-label">已通过</text>
      </view>
      <view class="stat-item" @click="goToList('rejected')">
        <text class="stat-num">{{ stats.rejected }}</text>
        <text class="stat-label">已驳回</text>
      </view>
    </view>

    <!-- 快捷入口 -->
    <view class="section">
      <view class="quick-entry">
        <view class="entry-item" @click="goToClock">
          <view class="entry-icon" style="background-color: #2d8cf0;">
            <uni-icons type="location" size="24" color="#fff"></uni-icons>
          </view>
          <text class="entry-name">打卡</text>
        </view>
        <view class="entry-item" @click="goToMyApply">
          <view class="entry-icon" style="background-color: #19be6b;">
            <uni-icons type="list" size="24" color="#fff"></uni-icons>
          </view>
          <text class="entry-name">我的申请</text>
        </view>
        <view class="entry-item" @click="goToApprovalList">
          <view class="entry-icon" style="background-color: #ff9900;">
            <uni-icons type="checkbox" size="24" color="#fff"></uni-icons>
          </view>
          <text class="entry-name">待审批</text>
        </view>
        <view class="entry-item" @click="goToMyRecords">
          <view class="entry-icon" style="background-color: #9254de;">
            <uni-icons type="calendar" size="24" color="#fff"></uni-icons>
          </view>
          <text class="entry-name">考勤记录</text>
        </view>
      </view>
    </view>

    <!-- 申请中心 -->
    <view class="section">
      <view class="section-header">
        <text class="section-title">申请中心</text>
      </view>
      <view class="apply-grid">
        <view class="apply-item" v-for="(item, index) in applyMenus" :key="index" @click="handleMenuClick(item)">
          <view class="icon-box" :style="{ backgroundColor: item.iconBgColor }">
            <uni-icons :type="item.icon" size="22" color="#fff"></uni-icons>
          </view>
          <text class="apply-name">{{ item.menuName }}</text>
        </view>
      </view>
    </view>

    <!-- 待我审批 -->
    <view class="section">
      <view class="section-header">
        <text class="section-title">待我审批</text>
        <text class="more" @click="goToApprovalList">更多 ></text>
      </view>
      <view class="approval-list" v-if="pendingList.length > 0">
        <view class="approval-item" v-for="(item, index) in pendingList" :key="index" @click="goToApprovalDetail(item)">
          <view class="approval-left">
            <view class="approval-type">{{ item.typeName }}</view>
            <view class="approval-info">
              <text class="applicant">{{ item.applicantName }}</text>
              <text class="time">{{ item.createTime }}</text>
            </view>
          </view>
          <view class="approval-right">
            <uni-icons type="right" size="16" color="#999"></uni-icons>
          </view>
        </view>
      </view>
      <view class="empty-tip" v-else>
        <text>暂无待审批事项</text>
      </view>
    </view>
  </view>
</template>

<script>
import { getMobileMenus } from '@/api/menu'

export default {
  data() {
    return {
      stats: {
        pending: 0,
        approved: 0,
        rejected: 0
      },
      applyMenus: [],
      pendingList: []
    }
  },
  onShow() {
    this.loadMenus()
  },
  methods: {
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
      this.$modal.showToast('功能开发中~')
    },
    goToMyRecords() {
      this.$modal.showToast('功能开发中~')
    },
    goToList(type) {
      this.$modal.showToast('功能开发中~')
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
.work-container {
  min-height: 100vh;
  background-color: #f5f6f7;
}

.stat-section {
  display: flex;
  background-color: #fff;
  padding: 30rpx 0;
  margin-bottom: 20rpx;
}

.stat-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  border-right: 1rpx solid #f0f0f0;
  
  &:last-child {
    border-right: none;
  }
  
  .stat-num {
    font-size: 48rpx;
    font-weight: bold;
    color: #2d8cf0;
  }
  
  .stat-label {
    font-size: 26rpx;
    color: #666;
    margin-top: 8rpx;
  }
}

.section {
  background-color: #fff;
  margin: 0 20rpx 20rpx;
  border-radius: 16rpx;
  padding: 24rpx;
}

.quick-entry {
  display: flex;
  justify-content: space-around;
}

.entry-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  
  .entry-icon {
    width: 90rpx;
    height: 90rpx;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
  }
  
  .entry-name {
    font-size: 26rpx;
    color: #333;
    margin-top: 12rpx;
  }
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24rpx;
}

.section-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
}

.more {
  font-size: 26rpx;
  color: #999;
}

.apply-grid {
  display: flex;
  flex-wrap: wrap;
}

.apply-item {
  width: 25%;
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 24rpx;
  
  .icon-box {
    width: 80rpx;
    height: 80rpx;
    border-radius: 16rpx;
    display: flex;
    align-items: center;
    justify-content: center;
  }
  
  .apply-name {
    font-size: 26rpx;
    color: #333;
    margin-top: 12rpx;
  }
}

.approval-list {
  .approval-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 20rpx 0;
    border-bottom: 1rpx solid #f0f0f0;
    
    &:last-child {
      border-bottom: none;
    }
  }
  
  .approval-left {
    flex: 1;
  }
  
  .approval-type {
    font-size: 30rpx;
    color: #333;
    font-weight: 500;
  }
  
  .approval-info {
    margin-top: 8rpx;
    
    .applicant {
      font-size: 26rpx;
      color: #666;
    }
    
    .time {
      font-size: 24rpx;
      color: #999;
      margin-left: 16rpx;
    }
  }
}

.empty-tip {
  text-align: center;
  padding: 40rpx;
  color: #999;
  font-size: 28rpx;
}
</style>
