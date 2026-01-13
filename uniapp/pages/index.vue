<template>
  <view class="home-container">
    <!-- 顶部用户信息 -->
    <view class="header">
      <view class="user-info">
        <image class="avatar" :src="avatar" mode="aspectFill"></image>
        <view class="info">
          <text class="name">{{ name || '未登录' }}</text>
          <text class="dept">欢迎使用HR人事管理系统</text>
        </view>
      </view>
    </view>

    <!-- 打卡入口 -->
    <view class="clock-entry" @click="goToClock">
      <view class="clock-left">
        <view class="clock-icon">
          <uni-icons type="location" size="28" color="#fff"></uni-icons>
        </view>
        <view class="clock-info">
          <text class="clock-title">考勤打卡</text>
          <text class="clock-time">{{ currentTime }}</text>
        </view>
      </view>
      <view class="clock-right">
        <text class="clock-btn-text">去打卡</text>
        <uni-icons type="right" size="16" color="#2d8cf0"></uni-icons>
      </view>
    </view>

    <!-- 申请功能 -->
    <view class="section" v-if="applyMenus.length > 0">
      <view class="section-title">申请中心</view>
      <view class="grid-menu">
        <view class="menu-item" v-for="(item, index) in applyMenus" :key="index" @click="handleMenuClick(item)">
          <view class="icon-wrapper" :style="{ backgroundColor: item.iconBgColor }">
            <uni-icons :type="item.icon" size="24" color="#fff"></uni-icons>
          </view>
          <text class="menu-text">{{ item.menuName }}</text>
        </view>
      </view>
    </view>

    <!-- 我的申请 -->
    <view class="section">
      <view class="section-title">
        <text>我的申请</text>
        <text class="more" @click="goToMyApply">查看全部 ></text>
      </view>
      <view class="apply-list" v-if="recentApplies.length > 0">
        <view class="apply-item" v-for="(item, index) in recentApplies" :key="index">
          <view class="apply-info">
            <text class="apply-type">{{ item.typeName }}</text>
            <text class="apply-time">{{ item.createTime }}</text>
          </view>
          <view class="apply-status" :class="'status-' + item.status">
            {{ item.statusName }}
          </view>
        </view>
      </view>
      <view class="empty-tip" v-else>
        <text>暂无申请记录</text>
      </view>
    </view>
  </view>
</template>

<script>
import { mapGetters } from 'vuex'
import { getMobileMenus } from '@/api/menu'

export default {
  data() {
    return {
      currentTime: '',
      applyMenus: [],
      recentApplies: [],
      timer: null
    }
  },
  computed: {
    ...mapGetters(['avatar', 'name'])
  },
  onShow() {
    this.loadMenus()
    this.updateTime()
    this.timer = setInterval(() => {
      this.updateTime()
    }, 1000)
  },
  onHide() {
    if (this.timer) {
      clearInterval(this.timer)
    }
  },
  methods: {
    updateTime() {
      const now = new Date()
      const hours = String(now.getHours()).padStart(2, '0')
      const minutes = String(now.getMinutes()).padStart(2, '0')
      this.currentTime = `${hours}:${minutes}`
    },
    async loadMenus() {
      try {
        const res = await getMobileMenus()
        if (res.code === 200 && res.data) {
          // 只取apply分组的菜单
          this.applyMenus = res.data.apply || []
        }
      } catch (e) {
        console.log('加载菜单失败', e)
      }
    },
    goToClock() {
      uni.navigateTo({ url: '/pages/clock/index' })
    },
    handleMenuClick(item) {
      if (item.path) {
        uni.navigateTo({ url: item.path })
      } else {
        this.$modal.showToast('功能开发中~')
      }
    },
    goToMyApply() {
      uni.navigateTo({ url: '/pages/apply/list/index' })
    }
  }
}
</script>

<style lang="scss" scoped>
.home-container {
  min-height: 100vh;
  background-color: #f5f6f7;
}

.header {
  background: linear-gradient(135deg, #2d8cf0 0%, #5cadff 100%);
  padding: 40rpx 30rpx;
  padding-top: 80rpx;
}

.user-info {
  display: flex;
  align-items: center;
  
  .avatar {
    width: 100rpx;
    height: 100rpx;
    border-radius: 50%;
    border: 4rpx solid rgba(255, 255, 255, 0.5);
  }
  
  .info {
    margin-left: 24rpx;
    
    .name {
      font-size: 36rpx;
      color: #fff;
      font-weight: bold;
    }
    
    .dept {
      font-size: 26rpx;
      color: rgba(255, 255, 255, 0.8);
      margin-top: 8rpx;
    }
  }
}

.clock-entry {
  background-color: #fff;
  margin: 20rpx;
  border-radius: 16rpx;
  padding: 30rpx;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.05);
}

.clock-left {
  display: flex;
  align-items: center;
}

.clock-icon {
  width: 80rpx;
  height: 80rpx;
  border-radius: 50%;
  background: linear-gradient(135deg, #2d8cf0 0%, #5cadff 100%);
  display: flex;
  align-items: center;
  justify-content: center;
}

.clock-info {
  margin-left: 20rpx;
  
  .clock-title {
    display: block;
    font-size: 32rpx;
    font-weight: bold;
    color: #333;
  }
  
  .clock-time {
    display: block;
    font-size: 26rpx;
    color: #999;
    margin-top: 6rpx;
  }
}

.clock-right {
  display: flex;
  align-items: center;
  
  .clock-btn-text {
    font-size: 28rpx;
    color: #2d8cf0;
    margin-right: 8rpx;
  }
}

.section {
  background-color: #fff;
  margin: 20rpx;
  border-radius: 16rpx;
  padding: 24rpx;
}

.section-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 24rpx;
  display: flex;
  justify-content: space-between;
  align-items: center;
  
  .more {
    font-size: 26rpx;
    color: #999;
    font-weight: normal;
  }
}

.grid-menu {
  display: flex;
  flex-wrap: wrap;
}

.menu-item {
  width: 25%;
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 24rpx;
  
  .icon-wrapper {
    width: 90rpx;
    height: 90rpx;
    border-radius: 20rpx;
    display: flex;
    align-items: center;
    justify-content: center;
  }
  
  .menu-text {
    font-size: 26rpx;
    color: #333;
    margin-top: 12rpx;
  }
}

.apply-list {
  .apply-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 20rpx 0;
    border-bottom: 1rpx solid #f0f0f0;
    
    &:last-child {
      border-bottom: none;
    }
  }
  
  .apply-info {
    .apply-type {
      font-size: 28rpx;
      color: #333;
    }
    
    .apply-time {
      font-size: 24rpx;
      color: #999;
      margin-top: 8rpx;
      display: block;
    }
  }
  
  .apply-status {
    font-size: 26rpx;
    padding: 8rpx 16rpx;
    border-radius: 8rpx;
    
    &.status-0 {
      background-color: #e6f7ff;
      color: #1890ff;
    }
    
    &.status-1 {
      background-color: #f6ffed;
      color: #52c41a;
    }
    
    &.status-2 {
      background-color: #fff2f0;
      color: #ff4d4f;
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
