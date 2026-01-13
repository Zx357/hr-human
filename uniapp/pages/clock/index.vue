<template>
  <view class="clock-container">
    <!-- 日期时间显示 -->
    <view class="datetime-section">
      <text class="date">{{ currentDate }}</text>
      <text class="time">{{ currentTime }}</text>
      <text class="week">{{ currentWeek }}</text>
    </view>

    <!-- 打卡按钮 -->
    <view class="clock-section">
      <view class="clock-btn" :class="{ 'clocked': hasClockedIn && !hasClockedOut }" @click="handleClock">
        <text class="clock-text">{{ clockBtnText }}</text>
        <text class="clock-tip">{{ clockTip }}</text>
      </view>
    </view>

    <!-- 今日打卡记录 -->
    <view class="record-section">
      <view class="section-title">今日打卡记录</view>
      <view class="record-list" v-if="todayRecords.length > 0">
        <view class="record-item" v-for="(item, index) in todayRecords" :key="index">
          <view class="record-icon" :class="item.clockType === 1 ? 'in' : 'out'">
            <uni-icons :type="item.clockType === 1 ? 'arrow-down' : 'arrow-up'" size="16" color="#fff"></uni-icons>
          </view>
          <view class="record-info">
            <text class="record-type">{{ item.clockType === 1 ? '上班打卡' : '下班打卡' }}</text>
            <text class="record-time">{{ formatTime(item.clockTime) }}</text>
          </view>
          <view class="record-status status-1">正常</view>
        </view>
      </view>
      <view class="empty-tip" v-else>
        <text>今日暂无打卡记录</text>
      </view>
    </view>
  </view>
</template>

<script>
import { getClockInfo, clock } from '@/api/attendance'

export default {
  data() {
    return {
      currentDate: '',
      currentTime: '',
      currentWeek: '',
      hasClockedIn: false,
      hasClockedOut: false,
      todayRecords: [],
      timer: null,
      loading: false
    }
  },
  computed: {
    clockBtnText() {
      if (!this.hasClockedIn) {
        return '上班打卡'
      } else if (!this.hasClockedOut) {
        return '下班打卡'
      } else {
        return '下班打卡'
      }
    },
    clockTip() {
      if (!this.hasClockedIn) {
        return '点击上班打卡'
      } else if (!this.hasClockedOut) {
        return '点击下班打卡'
      } else {
        return '今日已完成打卡'
      }
    }
  },
  onLoad() {
    this.updateDateTime()
    this.timer = setInterval(() => {
      this.updateDateTime()
    }, 1000)
    this.loadClockInfo()
  },
  onShow() {
    this.loadClockInfo()
  },
  onUnload() {
    if (this.timer) {
      clearInterval(this.timer)
    }
  },
  methods: {
    updateDateTime() {
      const now = new Date()
      const year = now.getFullYear()
      const month = String(now.getMonth() + 1).padStart(2, '0')
      const day = String(now.getDate()).padStart(2, '0')
      const hours = String(now.getHours()).padStart(2, '0')
      const minutes = String(now.getMinutes()).padStart(2, '0')
      const seconds = String(now.getSeconds()).padStart(2, '0')
      const weeks = ['星期日', '星期一', '星期二', '星期三', '星期四', '星期五', '星期六']
      
      this.currentDate = `${year}年${month}月${day}日`
      this.currentTime = `${hours}:${minutes}:${seconds}`
      this.currentWeek = weeks[now.getDay()]
    },
    async loadClockInfo() {
      try {
        const res = await getClockInfo()
        if (res.code === 200 && res.data) {
          this.todayRecords = res.data.todayRecords || []
          this.hasClockedIn = res.data.hasClockedIn || false
          this.hasClockedOut = res.data.hasClockedOut || false
        }
      } catch (e) {
        console.log('加载打卡信息失败', e)
      }
    },
    async handleClock() {
      if (this.loading) return
      
      this.loading = true
      this.$modal.loading('打卡中...')
      
      try {
        const clockType = this.hasClockedIn ? 2 : 1
        const res = await clock({ clockType })
        
        this.$modal.closeLoading()
        
        if (res.code === 200) {
          this.$modal.showToast(clockType === 1 ? '上班打卡成功' : '下班打卡成功')
          // 重新加载打卡信息
          await this.loadClockInfo()
        } else {
          this.$modal.msgError(res.msg || '打卡失败')
        }
      } catch (e) {
        this.$modal.closeLoading()
        this.$modal.msgError(e || '打卡失败')
      } finally {
        this.loading = false
      }
    },
    formatTime(timeStr) {
      if (!timeStr) return ''
      // 格式: 2026-01-12 20:30:00 -> 20:30:00
      const parts = timeStr.split(' ')
      return parts.length > 1 ? parts[1] : timeStr
    }
  }
}
</script>

<style lang="scss" scoped>
.clock-container {
  min-height: 100vh;
  background: linear-gradient(180deg, #2d8cf0 0%, #5cadff 50%, #f5f6f7 50%);
  padding-bottom: 40rpx;
}

.datetime-section {
  padding: 60rpx 0 40rpx;
  text-align: center;
  color: #fff;
  
  .date {
    display: block;
    font-size: 32rpx;
    margin-bottom: 16rpx;
  }
  
  .time {
    display: block;
    font-size: 80rpx;
    font-weight: bold;
    letter-spacing: 4rpx;
  }
  
  .week {
    display: block;
    font-size: 28rpx;
    margin-top: 16rpx;
    opacity: 0.9;
  }
}

.clock-section {
  display: flex;
  justify-content: center;
  padding: 40rpx 0;
}

.clock-btn {
  width: 300rpx;
  height: 300rpx;
  border-radius: 50%;
  background: linear-gradient(135deg, #fff 0%, #f0f0f0 100%);
  box-shadow: 0 10rpx 40rpx rgba(0, 0, 0, 0.2);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  
  &.clocked {
    background: linear-gradient(135deg, #ff9500 0%, #ff6b00 100%);
    
    .clock-text, .clock-tip {
      color: #fff;
    }
  }
  
  .clock-text {
    font-size: 36rpx;
    font-weight: bold;
    color: #2d8cf0;
  }
  
  .clock-tip {
    font-size: 24rpx;
    color: #999;
    margin-top: 12rpx;
  }
}

.record-section {
  background-color: #fff;
  margin: 0 20rpx;
  border-radius: 16rpx;
  padding: 24rpx;
}

.section-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 24rpx;
}

.record-list {
  .record-item {
    display: flex;
    align-items: center;
    padding: 20rpx 0;
    border-bottom: 1rpx solid #f0f0f0;
    
    &:last-child {
      border-bottom: none;
    }
  }
  
  .record-icon {
    width: 60rpx;
    height: 60rpx;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    
    &.in {
      background-color: #19be6b;
    }
    
    &.out {
      background-color: #ff9900;
    }
  }
  
  .record-info {
    flex: 1;
    margin-left: 20rpx;
    
    .record-type {
      display: block;
      font-size: 28rpx;
      color: #333;
    }
    
    .record-time {
      display: block;
      font-size: 24rpx;
      color: #999;
      margin-top: 4rpx;
    }
  }
  
  .record-status {
    font-size: 26rpx;
    padding: 6rpx 16rpx;
    border-radius: 20rpx;
    
    &.status-1 {
      background-color: #e8f5e9;
      color: #19be6b;
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
