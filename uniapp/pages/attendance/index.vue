<template>
  <view class="attendance-container">
    <!-- 月份导航 -->
    <view class="month-nav">
      <view class="nav-btn" @click="prevMonth">
        <text class="nav-arrow">◀</text>
        <text class="nav-text">上个月</text>
      </view>
      <view class="month-center">
        <text class="month-label">{{ currentYear }}年{{ currentMonth }}月</text>
        <view class="refresh-icon" @click="loadMonthData">
          <uni-icons type="refreshempty" size="18" color="#666"></uni-icons>
        </view>
      </view>
      <view class="nav-btn" @click="nextMonth">
        <text class="nav-text">下个月</text>
        <text class="nav-arrow">▶</text>
      </view>
    </view>

    <!-- 操作按钮 -->
    <view class="action-row">
      <view class="action-btn btn-refresh" @click="loadMonthData">
        <uni-icons type="refreshempty" size="18" color="#fff"></uni-icons>
        <text class="action-text">刷新</text>
      </view>
      <view class="action-btn btn-card" @click="goToCardApply">
        <uni-icons type="compose" size="18" color="#fff"></uni-icons>
        <text class="action-text">补卡申请</text>
      </view>
    </view>

    <!-- 考勤统计 -->
    <view class="stats-section">
      <view class="section-title">考勤统计</view>
      <view class="stats-grid">
        <view class="stats-row">
          <view class="stat-card">
            <text class="stat-num" :class="{ 'num-zero': stats.normalIn === 0 }">{{ stats.normalIn }}</text>
            <text class="stat-label">签到正常</text>
          </view>
          <view class="stat-card">
            <text class="stat-num" :class="{ 'num-zero': stats.normalOut === 0 }">{{ stats.normalOut }}</text>
            <text class="stat-label">签退正常</text>
          </view>
          <view class="stat-card">
            <text class="stat-num" :class="{ 'num-warn': stats.lateDays > 0 }">{{ stats.lateDays }}</text>
            <text class="stat-label">迟到天数</text>
          </view>
        </view>
        <view class="stats-row">
          <view class="stat-card">
            <text class="stat-num" :class="{ 'num-warn': stats.earlyDays > 0 }">{{ stats.earlyDays }}</text>
            <text class="stat-label">早退天数</text>
          </view>
          <view class="stat-card stat-card-wide">
            <text class="stat-num" :class="{ 'num-warn': stats.absentDays > 0 }">{{ stats.absentDays }}</text>
            <text class="stat-label">缺卡天数</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 考勤明细日历 -->
    <view class="calendar-section">
      <view class="section-title">考勤明细</view>
      <!-- 星期标题 -->
      <view class="week-header">
        <view class="week-cell" v-for="(w, i) in weekNames" :key="i">
          <text :class="{ 'weekend': i === 0 || i === 6 }">{{ w }}</text>
        </view>
      </view>
      <!-- 日期网格 -->
      <view class="calendar-grid">
        <view
          class="day-cell"
          v-for="(day, index) in calendarDays"
          :key="index"
          :class="{ 'has-record': day.hasRecord, 'today': day.isToday, 'other-month': !day.currentMonth }"
        >
          <template v-if="day.currentMonth">
            <text class="day-num">{{ day.day }}</text>
            <text class="day-week">{{ day.weekLabel }}</text>
            <text class="day-clock" :class="getClockClass(day, 'in')">{{ day.clockIn || '未签到' }}</text>
            <text class="day-clock" :class="getClockClass(day, 'out')">{{ day.clockOut || '未签退' }}</text>
          </template>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import { getMyMonthAttendance } from '@/api/attendance'

export default {
  data() {
    return {
      currentYear: new Date().getFullYear(),
      currentMonth: new Date().getMonth() + 1,
      weekNames: ['日', '一', '二', '三', '四', '五', '六'],
      monthData: [],
      stats: {
        normalIn: 0,
        normalOut: 0,
        lateDays: 0,
        earlyDays: 0,
        absentDays: 0
      }
    }
  },
  computed: {
    calendarDays() {
      const year = this.currentYear
      const month = this.currentMonth
      const firstDay = new Date(year, month - 1, 1)
      const lastDay = new Date(year, month, 0)
      const daysInMonth = lastDay.getDate()
      const startWeekDay = firstDay.getDay()

      const today = new Date()
      const todayStr = `${today.getFullYear()}-${String(today.getMonth() + 1).padStart(2, '0')}-${String(today.getDate()).padStart(2, '0')}`

      const weekLabels = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
      const days = []

      for (let i = 0; i < startWeekDay; i++) {
        days.push({ currentMonth: false })
      }

      for (let d = 1; d <= daysInMonth; d++) {
        const dateStr = `${year}-${String(month).padStart(2, '0')}-${String(d).padStart(2, '0')}`
        const dayOfWeek = new Date(year, month - 1, d).getDay()
        const record = this.monthData.find(r => r.attDate === dateStr)

        days.push({
          day: d,
          dateStr,
          currentMonth: true,
          weekLabel: weekLabels[dayOfWeek],
          isToday: dateStr === todayStr,
          hasRecord: !!record,
          clockIn: record ? record.clockIn : null,
          clockOut: record ? record.clockOut : null,
          lateIn: record ? record.lateIn : false,
          earlyOut: record ? record.earlyOut : false,
          normalIn: record ? record.normalIn : false,
          normalOut: record ? record.normalOut : false
        })
      }

      const remaining = 7 - (days.length % 7)
      if (remaining < 7) {
        for (let i = 0; i < remaining; i++) {
          days.push({ currentMonth: false })
        }
      }

      return days
    }
  },
  onLoad() {
    this.loadMonthData()
  },
  methods: {
    prevMonth() {
      if (this.currentMonth === 1) {
        this.currentMonth = 12
        this.currentYear--
      } else {
        this.currentMonth--
      }
      this.loadMonthData()
    },
    nextMonth() {
      if (this.currentMonth === 12) {
        this.currentMonth = 1
        this.currentYear++
      } else {
        this.currentMonth++
      }
      this.loadMonthData()
    },
    async loadMonthData() {
      try {
        const monthStr = `${this.currentYear}-${String(this.currentMonth).padStart(2, '0')}`
        const res = await getMyMonthAttendance({ month: monthStr })
        if (res.code === 200 && res.data) {
          this.monthData = res.data.records || []
          this.stats = res.data.stats || {
            normalIn: 0,
            normalOut: 0,
            lateDays: 0,
            earlyDays: 0,
            absentDays: 0
          }
        }
      } catch (e) {
        console.log('加载考勤数据失败', e)
      }
    },
    getClockClass(day, type) {
      if (type === 'in') {
        if (!day.clockIn) return 'clock-absent'
        if (day.lateIn) return 'clock-late'
        if (day.normalIn) return 'clock-normal'
        return 'clock-has'
      } else {
        if (!day.clockOut) return 'clock-absent'
        if (day.earlyOut) return 'clock-early'
        if (day.normalOut) return 'clock-normal'
        return 'clock-has'
      }
    },
    goToCardApply() {
      uni.navigateTo({ url: '/pages/apply/card/index' })
    }
  }
}
</script>

<style lang="scss" scoped>
.attendance-container {
  min-height: 100vh;
  background-color: #f5f6f7;
  padding-bottom: 40rpx;
}

.month-nav {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background-color: #fff;
  padding: 24rpx 30rpx;
  border-bottom: 1rpx solid #eee;
}

.nav-btn {
  display: flex;
  align-items: center;
  gap: 8rpx;
  padding: 10rpx 16rpx;
}

.nav-arrow {
  font-size: 24rpx;
  color: #666;
}

.nav-text {
  font-size: 28rpx;
  color: #666;
}

.month-center {
  display: flex;
  align-items: center;
  gap: 16rpx;
}

.month-label {
  font-size: 34rpx;
  font-weight: bold;
  color: #333;
}

.refresh-icon {
  padding: 8rpx;
}

.action-row {
  display: flex;
  gap: 24rpx;
  padding: 24rpx 30rpx;
  background-color: #fff;
}

.action-btn {
  flex: 1;
  height: 80rpx;
  border-radius: 12rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12rpx;
}

.btn-refresh {
  background: linear-gradient(135deg, #43cea2 0%, #185a9d 100%);
  .action-text { color: #fff; font-size: 28rpx; }
}

.btn-card {
  background: linear-gradient(135deg, #f5af19 0%, #f12711 100%);
  .action-text { color: #fff; font-size: 28rpx; }
}

.stats-section {
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
}

.stats-grid {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.stats-row {
  display: flex;
  gap: 20rpx;
}

.stat-card {
  flex: 1;
  background-color: #fafafa;
  border-radius: 12rpx;
  padding: 24rpx 16rpx;
  text-align: center;
  border: 1rpx solid #f0f0f0;
}

.stat-card-wide {
  flex: 2;
}

.stat-num {
  display: block;
  font-size: 48rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 8rpx;

  &.num-zero {
    color: #999;
  }

  &.num-warn {
    color: #9254de;
  }
}

.stat-label {
  display: block;
  font-size: 24rpx;
  color: #999;
}

.calendar-section {
  background-color: #fff;
  margin: 20rpx;
  border-radius: 16rpx;
  padding: 24rpx;
}

.week-header {
  display: flex;
  margin-bottom: 16rpx;
}

.week-cell {
  flex: 1;
  text-align: center;
  font-size: 28rpx;
  color: #333;
  font-weight: bold;
  padding: 12rpx 0;

  .weekend {
    color: #999;
  }
}

.calendar-grid {
  display: flex;
  flex-wrap: wrap;
}

.day-cell {
  width: calc(100% / 7);
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 12rpx 0;
  min-height: 160rpx;
  border-top: 1rpx solid #f5f5f5;

  &.other-month {
    visibility: hidden;
  }

  &.today {
    background-color: #f0f5ff;
    border-radius: 8rpx;
  }

  &.has-record {
    background-color: #faf5ff;
    border-radius: 8rpx;
  }
}

.day-num {
  font-size: 30rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 4rpx;
}

.day-week {
  font-size: 20rpx;
  color: #999;
  margin-bottom: 6rpx;
}

.day-clock {
  font-size: 18rpx;
  line-height: 1.6;

  &.clock-absent {
    color: #ccc;
  }

  &.clock-normal {
    color: #4caf50;
  }

  &.clock-late {
    color: #f44336;
  }

  &.clock-early {
    color: #f44336;
  }

  &.clock-has {
    color: #333;
  }
}
</style>
