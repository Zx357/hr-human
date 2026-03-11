<template>
  <view class="page-wrap">
    <!-- 地图区域 -->
    <view class="map-section">
      <map
        id="attendanceMap"
        :latitude="latitude"
        :longitude="longitude"
        :markers="markers"
        :scale="16"
        class="attendance-map"
        show-location
      />
      <!-- 浮动定位信息 -->
      <view class="location-float" v-if="distance !== null">
        <view class="loc-icon">
          <u-icon name="map-fill" size="18" color="#6366f1"></u-icon>
        </view>
        <text class="loc-text">距打卡地点 {{ formatDistance(distance) }}</text>
      </view>
    </view>

    <!-- 底部内容区 -->
    <view class="content-area">
      <!-- 时间与打卡核心区 -->
      <view class="clock-core">
        <view class="time-display">
          <text class="current-time">{{ currentTime }}</text>
          <text class="current-date">{{ currentDate }}</text>
        </view>
        
        <view class="clock-buttons">
          <view
            class="clock-btn"
            :class="{ done: hasClockedIn, ready: !hasClockedIn }"
            @click="handleClockIn"
          >
            <view class="btn-inner">
              <u-icon :name="hasClockedIn ? 'checkmark-circle-fill' : 'clock-fill'" size="36" :color="hasClockedIn ? '#10b981' : '#fff'"></u-icon>
              <text class="btn-label">{{ hasClockedIn ? '已签到' : '签到' }}</text>
            </view>
          </view>

          <view class="clock-divider">
            <view class="divider-line"></view>
            <view class="divider-dot"></view>
            <view class="divider-line"></view>
          </view>

          <view
            class="clock-btn"
            :class="{ done: hasClockedOut, ready: !hasClockedOut && hasClockedIn, locked: !hasClockedIn }"
            @click="handleClockOut"
          >
            <view class="btn-inner">
              <u-icon :name="hasClockedOut ? 'checkmark-circle-fill' : 'clock-fill'" size="36" :color="hasClockedOut ? '#10b981' : (!hasClockedIn ? '#d1d5db' : '#fff')"></u-icon>
              <text class="btn-label">{{ hasClockedOut ? '已签退' : '签退' }}</text>
            </view>
          </view>
        </view>
      </view>

      <!-- 快捷操作 -->
      <view class="quick-actions">
        <view class="action-item" @click="goToCardApply">
          <view class="action-icon" style="background: rgba(99, 102, 241, 0.1);">
            <u-icon name="edit-pen-fill" size="24" color="#6366f1"></u-icon>
          </view>
          <text class="action-name">补卡申请</text>
        </view>
        <view class="action-item" @click="goToMyAttendance">
          <view class="action-icon" style="background: rgba(14, 165, 233, 0.1);">
            <u-icon name="calendar-fill" size="24" color="#0ea5e9"></u-icon>
          </view>
          <text class="action-name">我的考勤</text>
        </view>
      </view>

      <!-- 今日打卡记录 -->
      <view class="record-section">
        <view class="section-header">
          <text class="section-title">今日打卡</text>
        </view>
        <view class="record-cards" v-if="todayRecords.length > 0">
          <view class="record-item">
            <view class="record-left">
              <view class="record-icon in">
                <u-icon name="arrow-downward" size="18" color="#6366f1"></u-icon>
              </view>
              <view class="record-info">
                <text class="record-type">上班签到</text>
                <text class="record-meta">{{ scheduledIn ? ('规定 ' + scheduledIn) : '' }}</text>
              </view>
            </view>
            <view class="record-right">
              <text class="record-time">{{ clockInTime || '--:--' }}</text>
              <view class="status-pill" :class="clockInStatus" v-if="clockInStatusText">
                <text>{{ clockInStatusText }}</text>
              </view>
            </view>
          </view>
          
          <view class="record-item">
            <view class="record-left">
              <view class="record-icon out">
                <u-icon name="arrow-upward" size="18" color="#0ea5e9"></u-icon>
              </view>
              <view class="record-info">
                <text class="record-type">下班签退</text>
                <text class="record-meta">{{ scheduledOut ? ('规定 ' + scheduledOut) : '' }}</text>
              </view>
            </view>
            <view class="record-right">
              <text class="record-time">{{ clockOutTime || '--:--' }}</text>
              <view class="status-pill" :class="clockOutStatus" v-if="clockOutStatusText">
                <text>{{ clockOutStatusText }}</text>
              </view>
            </view>
          </view>
        </view>
        <u-empty v-else mode="data" text="今日暂无打卡记录" iconSize="60" customStyle="padding: 40rpx 0;"></u-empty>
      </view>
    </view>
  </view>
</template>

<script>
import { getClockInfo, clock } from '@/api/attendance'

export default {
  data() {
    return {
      latitude: 30.5,
      longitude: 114.4,
      markers: [],
      hasClockedIn: false,
      hasClockedOut: false,
      todayRecords: [],
      loading: false,
      distance: null,
      companyLat: null,
      companyLng: null,
      clockInRecord: null,
      clockOutRecord: null,
      todayDaily: null,
      scheduledIn: null,
      scheduledOut: null,
      currentTime: '',
      currentDate: '',
      timer: null
    }
  },
  computed: {
    clockInTime() {
      if (!this.clockInRecord) return ''
      return this.formatTime(this.clockInRecord.clockTime)
    },
    clockOutTime() {
      if (!this.clockOutRecord) return ''
      return this.formatTime(this.clockOutRecord.clockTime)
    },
    clockInStatusText() {
      if (!this.clockInRecord) return ''
      if (this.todayDaily && this.todayDaily.lateMinutes > 0) return '迟到'
      return '正常'
    },
    clockInStatus() {
      if (this.clockInStatusText === '迟到') return 'status-late'
      if (this.clockInStatusText === '正常') return 'status-normal'
      return ''
    },
    clockOutStatusText() {
      if (!this.clockOutRecord) return ''
      if (this.todayDaily && this.todayDaily.earlyMinutes > 0) return '早退'
      return '正常'
    },
    clockOutStatus() {
      if (this.clockOutStatusText === '早退') return 'status-early'
      if (this.clockOutStatusText === '正常') return 'status-normal'
      return ''
    }
  },
  onLoad() {
    this.getLocation()
    this.loadClockInfo()
    this.updateTime()
    this.timer = setInterval(() => { this.updateTime() }, 1000)
  },
  onShow() {
    this.loadClockInfo()
  },
  onUnload() {
    if (this.timer) clearInterval(this.timer)
  },
  methods: {
    updateTime() {
      const now = new Date()
      const h = String(now.getHours()).padStart(2, '0')
      const m = String(now.getMinutes()).padStart(2, '0')
      const s = String(now.getSeconds()).padStart(2, '0')
      this.currentTime = `${h}:${m}:${s}`
      const weekDays = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
      const month = now.getMonth() + 1
      const day = now.getDate()
      this.currentDate = `${month}月${day}日 ${weekDays[now.getDay()]}`
    },
    getLocation() {
      uni.getLocation({
        type: 'gcj02',
        success: (res) => {
          this.latitude = res.latitude
          this.longitude = res.longitude
          this.markers = [{
            id: 1,
            latitude: res.latitude,
            longitude: res.longitude,
            width: 30,
            height: 30
          }]
          if (this.companyLat && this.companyLng) {
            this.calcDistance()
          }
        },
        fail: () => {
          console.log('获取位置失败')
        }
      })
    },
    calcDistance() {
      if (!this.companyLat || !this.companyLng) return
      const radLat1 = (this.latitude * Math.PI) / 180
      const radLat2 = (this.companyLat * Math.PI) / 180
      const a = radLat1 - radLat2
      const b = ((this.longitude - this.companyLng) * Math.PI) / 180
      let s = 2 * Math.asin(
        Math.sqrt(
          Math.pow(Math.sin(a / 2), 2) +
          Math.cos(radLat1) * Math.cos(radLat2) * Math.pow(Math.sin(b / 2), 2)
        )
      )
      s = s * 6378137
      this.distance = Math.round(s * 100) / 100
    },
    formatDistance(d) {
      if (d >= 1000) {
        return (d / 1000).toFixed(2) + '千米'
      }
      return d.toFixed(0) + '米'
    },
    async loadClockInfo() {
      try {
        const res = await getClockInfo()
        if (res.code === 200 && res.data) {
          this.todayRecords = res.data.todayRecords || []
          this.hasClockedIn = res.data.hasClockedIn || false
          this.hasClockedOut = res.data.hasClockedOut || false
          this.scheduledIn = res.data.scheduledIn || null
          this.scheduledOut = res.data.scheduledOut || null
          this.todayDaily = res.data.todayDaily || null

          if (res.data.companyLat && res.data.companyLng) {
            this.companyLat = res.data.companyLat
            this.companyLng = res.data.companyLng
            this.markers.push({
              id: 2,
              latitude: res.data.companyLat,
              longitude: res.data.companyLng,
              width: 30,
              height: 30,
              callout: { content: '打卡地点', display: 'ALWAYS', fontSize: 12 }
            })
            this.calcDistance()
          }

          this.clockInRecord = this.todayRecords.find(r => r.clockType === 1) || null
          this.clockOutRecord = this.todayRecords.find(r => r.clockType === 2) || null
        }
      } catch (e) {
        console.log('加载打卡信息失败', e)
      }
    },
    async handleClockIn() {
      if (this.hasClockedIn || this.loading) return
      await this.doClock(1)
    },
    async handleClockOut() {
      if (this.hasClockedOut || this.loading) return
      if (!this.hasClockedIn) {
        this.$modal.msgError('请先签到')
        return
      }
      await this.doClock(2)
    },
    async doClock(clockType) {
      this.loading = true
      this.$modal.loading('打卡中...')
      try {
        const data = { clockType }
        if (this.latitude && this.longitude) {
          data.location = `${this.latitude},${this.longitude}`
        }
        const res = await clock(data)
        this.$modal.closeLoading()
        if (res.code === 200) {
          this.$modal.showToast(clockType === 1 ? '签到成功' : '签退成功')
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
      const parts = timeStr.split(' ')
      const timePart = parts.length > 1 ? parts[1] : timeStr
      return timePart.substring(0, 5)
    },
    goToCardApply() {
      uni.navigateTo({ url: '/pages/apply/card/index' })
    },
    goToMyAttendance() {
      uni.navigateTo({ url: '/pages/attendance/index' })
    }
  }
}
</script>

<style lang="scss" scoped>
.page-wrap {
  min-height: 100vh;
  background: #f3f4f6;
}

.map-section {
  width: 100%;
  height: 400rpx;
  position: relative;
}

.attendance-map {
  width: 100%;
  height: 100%;
}

.location-float {
  position: absolute;
  bottom: 24rpx;
  left: 30rpx;
  right: 30rpx;
  background: rgba(255,255,255,0.95);
  backdrop-filter: blur(10px);
  border-radius: 20rpx;
  padding: 16rpx 24rpx;
  display: flex;
  align-items: center;
  box-shadow: 0 4rpx 16rpx rgba(0,0,0,0.08);
  
  .loc-icon {
    width: 44rpx;
    height: 44rpx;
    border-radius: 12rpx;
    background: rgba(99, 102, 241, 0.1);
    display: flex;
    align-items: center;
    justify-content: center;
    margin-right: 16rpx;
  }
  
  .loc-text {
    font-size: 26rpx;
    color: #4b5563;
    font-weight: 500;
  }
}

.content-area {
  margin-top: -24rpx;
  position: relative;
  z-index: 1;
  background: #f3f4f6;
  border-top-left-radius: 32rpx;
  border-top-right-radius: 32rpx;
  padding: 0 30rpx 40rpx;
}

.clock-core {
  background: #ffffff;
  border-radius: 32rpx;
  padding: 40rpx 30rpx;
  margin-top: 24rpx;
  box-shadow: 0 8rpx 30rpx rgba(0,0,0,0.03);
}

.time-display {
  text-align: center;
  margin-bottom: 40rpx;
  
  .current-time {
    display: block;
    font-size: 64rpx;
    font-weight: 800;
    color: #111827;
    letter-spacing: 4rpx;
    font-variant-numeric: tabular-nums;
  }
  
  .current-date {
    display: block;
    font-size: 26rpx;
    color: #9ca3af;
    margin-top: 8rpx;
    font-weight: 500;
  }
}

.clock-buttons {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0;
}

.clock-divider {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin: 0  30rpx;
  
  .divider-line {
    width: 2rpx;
    height: 36rpx;
    background: #e5e7eb;
  }
  .divider-dot {
    width: 12rpx;
    height: 12rpx;
    border-radius: 50%;
    background: #d1d5db;
    margin: 8rpx 0;
  }
}

.clock-btn {
  width: 200rpx;
  height: 200rpx;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  
  &.ready {
    background: linear-gradient(135deg, #818cf8 0%, #6366f1 100%);
    box-shadow: 0 12rpx 36rpx rgba(99, 102, 241, 0.35);
    
    .btn-label { color: #fff; }
  }
  
  &.done {
    background: #f0fdf4;
    border: 3rpx solid #bbf7d0;
    
    .btn-label { color: #10b981; }
  }
  
  &.locked {
    background: #f9fafb;
    border: 3rpx solid #e5e7eb;
    
    .btn-label { color: #d1d5db; }
  }

  .btn-inner {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 12rpx;
  }
  
  .btn-label {
    font-size: 28rpx;
    font-weight: 700;
  }
}

.quick-actions {
  display: flex;
  justify-content: space-between;
  margin-top: 30rpx;
  background: #ffffff;
  border-radius: 32rpx;
  padding: 36rpx 20rpx;
  box-shadow: 0 8rpx 30rpx rgba(0,0,0,0.03);
  
  .action-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    flex: 1;
    
    .action-icon {
      width: 80rpx;
      height: 80rpx;
      border-radius: 24rpx;
      display: flex;
      align-items: center;
      justify-content: center;
      margin-bottom: 12rpx;
      
      &:active { transform: scale(0.95); }
    }
    
    .action-name {
      font-size: 24rpx;
      color: #4b5563;
      font-weight: 600;
    }
  }
}

.record-section {
  margin-top: 30rpx;
  background: #ffffff;
  border-radius: 32rpx;
  padding: 36rpx 30rpx;
  box-shadow: 0 8rpx 30rpx rgba(0,0,0,0.03);
}

.section-header {
  margin-bottom: 30rpx;
  
  .section-title {
    font-size: 34rpx;
    font-weight: 800;
    color: #111827;
  }
}

.record-cards {
  .record-item {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 24rpx 20rpx;
    background: #f9fafb;
    border-radius: 24rpx;
    margin-bottom: 20rpx;
    
    &:last-child { margin-bottom: 0; }
    
    .record-left {
      display: flex;
      align-items: center;
      
      .record-icon {
        width: 64rpx;
        height: 64rpx;
        border-radius: 20rpx;
        display: flex;
        align-items: center;
        justify-content: center;
        
        &.in { background: rgba(99, 102, 241, 0.1); }
        &.out { background: rgba(14, 165, 233, 0.1); }
      }
      
      .record-info {
        margin-left: 20rpx;
        
        .record-type {
          display: block;
          font-size: 28rpx;
          color: #1f2937;
          font-weight: 600;
        }
        
        .record-meta {
          display: block;
          font-size: 22rpx;
          color: #9ca3af;
          margin-top: 4rpx;
        }
      }
    }
    
    .record-right {
      display: flex;
      align-items: center;
      gap: 16rpx;
      
      .record-time {
        font-size: 36rpx;
        font-weight: 800;
        color: #1f2937;
        font-variant-numeric: tabular-nums;
      }
    }
  }
}

.status-pill {
  padding: 6rpx 16rpx;
  border-radius: 16rpx;
  font-size: 22rpx;
  font-weight: 600;
  
  &.status-normal {
    background: #dcfce7;
    color: #16a34a;
  }
  
  &.status-late {
    background: #fef2f2;
    color: #ef4444;
  }
  
  &.status-early {
    background: #fffbeb;
    color: #f59e0b;
  }
}
</style>
