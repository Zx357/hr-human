<template>
  <view class="clock-container">
    <!-- 地图区域 -->
    <view class="map-section">
      <map
        id="attendanceMap"
        :latitude="latitude"
        :longitude="longitude"
        :markers="markers"
        :scale="10"
        class="attendance-map"
        show-location
      />
    </view>

    <!-- 打卡按钮 -->
    <view class="clock-buttons">
      <view
        class="clock-btn"
        :class="{ disabled: hasClockedIn, active: !hasClockedIn }"
        @click="handleClockIn"
      >
        <text class="btn-label">签到</text>
      </view>
      <view
        class="clock-btn"
        :class="{ disabled: hasClockedOut, active: !hasClockedOut && hasClockedIn }"
        @click="handleClockOut"
      >
        <text class="btn-label">签退</text>
      </view>
    </view>

    <!-- 距离提示 -->
    <view class="distance-info" v-if="distance !== null">
      <text>距打卡地点: {{ formatDistance(distance) }}</text>
    </view>

    <!-- 功能按钮 -->
    <view class="action-buttons">
      <view class="action-btn btn-card" @click="goToCardApply">
        <uni-icons type="compose" size="20" color="#c44d1a"></uni-icons>
        <text class="action-text">补卡申请</text>
      </view>
      <view class="action-btn btn-attendance" @click="goToMyAttendance">
        <uni-icons type="calendar" size="20" color="#c44d1a"></uni-icons>
        <text class="action-text">我的考勤</text>
      </view>
    </view>

    <!-- 今日打卡记录 -->
    <view class="record-section">
      <view class="section-title">今日打卡记录</view>
      <view class="record-card" v-if="todayRecords.length > 0">
        <view class="record-row">
          <view class="record-col">
            <text class="record-label">签到</text>
            <text class="record-time">{{ clockInTime || '--:--' }}</text>
            <view class="status-tag" :class="clockInStatus" v-if="clockInStatusText">
              <text>{{ clockInStatusText }}</text>
            </view>
          </view>
          <view class="record-divider"></view>
          <view class="record-col">
            <text class="record-label">签退</text>
            <text class="record-time">{{ clockOutTime || '--:--' }}</text>
            <view class="status-tag" :class="clockOutStatus" v-if="clockOutStatusText">
              <text>{{ clockOutStatusText }}</text>
            </view>
          </view>
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
      scheduledOut: null
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
  },
  onShow() {
    this.loadClockInfo()
  },
  methods: {
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
      return d.toFixed(2) + '米'
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
.clock-container {
  min-height: 100vh;
  background-color: #f5f6f7;
}

.map-section {
  width: 100%;
  height: 560rpx;
}

.attendance-map {
  width: 100%;
  height: 100%;
}

.clock-buttons {
  display: flex;
  justify-content: center;
  gap: 80rpx;
  padding: 50rpx 0 30rpx;
}

.clock-btn {
  width: 200rpx;
  height: 200rpx;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 8rpx 30rpx rgba(0, 0, 0, 0.12);

  &.active {
    background: linear-gradient(135deg, #f5deb3 0%, #e8c892 100%);
  }

  &.disabled {
    background: linear-gradient(135deg, #e8e8e8 0%, #d0d0d0 100%);
  }

  .btn-label {
    font-size: 36rpx;
    font-weight: bold;
    color: #666;
  }

  &.active .btn-label {
    color: #8B6914;
  }
}

.distance-info {
  text-align: center;
  padding: 16rpx 0 20rpx;
  font-size: 26rpx;
  color: #666;
  border-bottom: 1rpx solid #eee;
  margin: 0 30rpx;
}

.action-buttons {
  display: flex;
  gap: 30rpx;
  padding: 30rpx;
}

.action-btn {
  flex: 1;
  height: 88rpx;
  border-radius: 44rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12rpx;
  border: 2rpx solid transparent;

  .action-text {
    font-size: 30rpx;
    font-weight: 500;
  }
}

.btn-card {
  background: linear-gradient(135deg, #ffecd2 0%, #fcb69f 100%);
  .action-text { color: #c44d1a; }
}

.btn-attendance {
  background: linear-gradient(135deg, #ffecd2 0%, #fcb69f 100%);
  .action-text { color: #c44d1a; }
}

.record-section {
  background-color: #fff;
  margin: 0 20rpx 40rpx;
  border-radius: 16rpx;
  padding: 30rpx;
}

.section-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 30rpx;
}

.record-card {
  padding: 10rpx 0;
}

.record-row {
  display: flex;
  align-items: flex-start;
}

.record-col {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.record-divider {
  width: 1rpx;
  height: 120rpx;
  background-color: #eee;
  margin: 0 10rpx;
  align-self: center;
}

.record-label {
  font-size: 28rpx;
  color: #999;
  margin-bottom: 12rpx;
}

.record-time {
  font-size: 52rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 12rpx;
}

.status-tag {
  padding: 4rpx 20rpx;
  border-radius: 20rpx;
  font-size: 24rpx;

  &.status-normal {
    background-color: #e8f5e9;
    color: #4caf50;
  }

  &.status-late {
    background-color: #fff3e0;
    color: #f44336;
  }

  &.status-early {
    background-color: #fff3e0;
    color: #f44336;
  }
}

.empty-tip {
  text-align: center;
  padding: 40rpx;
  color: #999;
  font-size: 28rpx;
}
</style>
