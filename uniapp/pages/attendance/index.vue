<template>
  <view class="page-wrap">
    <!-- 月份导航 -->
    <view class="month-nav">
      <view class="nav-btn" @click="prevMonth">
        <u-icon name="arrow-left" size="16" color="#6b7280"></u-icon>
      </view>
      <view class="month-center" @click="loadMonthData">
        <text class="month-label">{{ currentYear }}年{{ currentMonth }}月</text>
        <u-icon name="reload" size="16" color="#9ca3af" customStyle="margin-left:12rpx"></u-icon>
      </view>
      <view class="nav-btn" @click="nextMonth">
        <u-icon name="arrow-right" size="16" color="#6b7280"></u-icon>
      </view>
    </view>

    <!-- 统计概览 -->
    <view class="stats-row">
      <view class="stat-card stat-green">
        <view class="stat-icon-bg">
          <u-icon name="checkmark-circle-fill" size="20" color="#10b981"></u-icon>
        </view>
        <text class="stat-num">{{ stats.normalIn }}</text>
        <text class="stat-label">正常签到</text>
      </view>
      <view class="stat-card stat-blue">
        <view class="stat-icon-bg">
          <u-icon name="checkmark-circle-fill" size="20" color="#6366f1"></u-icon>
        </view>
        <text class="stat-num">{{ stats.normalOut }}</text>
        <text class="stat-label">正常签退</text>
      </view>
      <view class="stat-card stat-orange">
        <view class="stat-icon-bg">
          <u-icon name="error-circle-fill" size="20" color="#f59e0b"></u-icon>
        </view>
        <text class="stat-num">{{ stats.lateDays }}</text>
        <text class="stat-label">迟到</text>
      </view>
      <view class="stat-card stat-red">
        <view class="stat-icon-bg">
          <u-icon name="close-circle-fill" size="20" color="#ef4444"></u-icon>
        </view>
        <text class="stat-num">{{ stats.absentDays }}</text>
        <text class="stat-label">缺卡</text>
      </view>
    </view>


    <!-- 考勤日历 -->
    <view class="calendar-card">
      <view class="card-header">
        <text class="card-title">考勤明细</text>
      </view>
      <!-- 星期标题 -->
      <view class="week-header">
        <view class="week-cell" v-for="(w, i) in weekNames" :key="i">
          <text :class="{ 'is-weekend': i === 0 || i === 6 }">{{ w }}</text>
        </view>
      </view>
      <!-- 日期网格 -->
      <view class="calendar-grid">
        <view
          class="day-cell"
          v-for="(day, index) in calendarDays"
          :key="index"
          :class="getDayCellClass(day)"
        >
          <template v-if="day.currentMonth">
            <view class="day-num-wrap" :class="{ 'is-today': day.isToday }">
              <text class="day-num">{{ day.day }}</text>
            </view>
            <view class="day-dots" v-if="day.hasRecord">
              <view class="dot" :class="getDotClass(day, 'in')"></view>
              <view class="dot" :class="getDotClass(day, 'out')"></view>
            </view>
            <view class="day-dots" v-else-if="day.isPast && day.scheduled">
              <view class="dot dot-absent"></view>
            </view>
          </template>
        </view>
      </view>
    </view>

    <!-- 当日明细列表 -->
    <view class="detail-card">
      <view class="card-header">
        <text class="card-title">本月记录</text>
      </view>
      <view class="detail-list" v-if="monthData.length > 0">
        <view class="detail-item" v-for="(item, index) in monthData" :key="index">
          <view class="detail-date">
            <text class="date-day">{{ extractDay(item.attDate) }}</text>
            <text class="date-week">{{ getWeekLabel(item.attDate) }}</text>
          </view>
          <view class="detail-times">
            <view class="time-row">
              <view class="time-dot" :class="item.normalIn ? 'dot-normal' : (item.lateIn ? 'dot-late' : 'dot-absent')"></view>
              <text class="time-label">签到</text>
              <text class="time-value">{{ item.clockIn || '--:--' }}</text>
            </view>
            <view class="time-row">
              <view class="time-dot" :class="item.normalOut ? 'dot-normal' : (item.earlyOut ? 'dot-early' : 'dot-absent')"></view>
              <text class="time-label">签退</text>
              <text class="time-value">{{ item.clockOut || '--:--' }}</text>
            </view>
          </view>
        </view>
      </view>
      <u-empty v-else mode="data" text="暂无考勤记录" iconSize="60" customStyle="padding: 40rpx 0;"></u-empty>
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

      const days = []

      for (let i = 0; i < startWeekDay; i++) {
        days.push({ currentMonth: false })
      }

      for (let d = 1; d <= daysInMonth; d++) {
        const dateStr = `${year}-${String(month).padStart(2, '0')}-${String(d).padStart(2, '0')}`
        const record = this.monthData.find(r => r.attDate === dateStr)
        const dateObj = new Date(year, month - 1, d)
        const isPast = dateObj < new Date(today.getFullYear(), today.getMonth(), today.getDate())

        days.push({
          day: d,
          dateStr,
          currentMonth: true,
          isToday: dateStr === todayStr,
          isPast,
          scheduled: record ? !!record.scheduled : false,
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
    getDayCellClass(day) {
      if (!day.currentMonth) return 'other-month'
      let cls = ''
      if (day.isToday) cls += ' today'
      return cls
    },
    getDotClass(day, type) {
      if (type === 'in') {
        if (!day.clockIn) return 'dot-absent'
        if (day.lateIn) return 'dot-late'
        if (day.normalIn) return 'dot-normal'
        return 'dot-has'
      } else {
        if (!day.clockOut) return 'dot-absent'
        if (day.earlyOut) return 'dot-early'
        if (day.normalOut) return 'dot-normal'
        return 'dot-has'
      }
    },
    extractDay(dateStr) {
      if (!dateStr) return ''
      return parseInt(dateStr.split('-')[2])
    },
    getWeekLabel(dateStr) {
      if (!dateStr) return ''
      const labels = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
      const d = new Date(dateStr)
      return labels[d.getDay()]
    },
    goToCardApply() {
      uni.navigateTo({ url: '/pages/apply/card/index' })
    }
  }
}
</script>

<style lang="scss" scoped>
.page-wrap {
  min-height: 100vh;
  background: #f3f4f6;
  padding-bottom: 40rpx;
}

.month-nav {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #ffffff;
  padding: 20rpx 30rpx;
  margin: 0 0 20rpx;
  
  .nav-btn {
    padding: 16rpx 20rpx;
    border-radius: 16rpx;
    background: #f3f4f6;
    
    &:active { opacity: 0.7; }
  }
  
  .month-center {
    display: flex;
    align-items: center;
  }
  
  .month-label {
    font-size: 34rpx;
    font-weight: 800;
    color: #111827;
  }
}

.stats-row {
  display: flex;
  gap: 16rpx;
  padding: 0 30rpx;
  margin-bottom: 20rpx;
  
  .stat-card {
    flex: 1;
    background: #ffffff;
    border-radius: 24rpx;
    padding: 24rpx 12rpx;
    display: flex;
    flex-direction: column;
    align-items: center;
    box-shadow: 0 4rpx 16rpx rgba(0,0,0,0.02);
    
    .stat-icon-bg {
      width: 48rpx;
      height: 48rpx;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      margin-bottom: 12rpx;
    }
    
    &.stat-green .stat-icon-bg { background: rgba(16, 185, 129, 0.1); }
    &.stat-blue .stat-icon-bg { background: rgba(99, 102, 241, 0.1); }
    &.stat-orange .stat-icon-bg { background: rgba(245, 158, 11, 0.1); }
    &.stat-red .stat-icon-bg { background: rgba(239, 68, 68, 0.1); }
    
    .stat-num {
      font-size: 40rpx;
      font-weight: 800;
      color: #1f2937;
    }
    
    .stat-label {
      font-size: 22rpx;
      color: #9ca3af;
      font-weight: 500;
      margin-top: 6rpx;
    }
  }
}

.action-bar {
  display: flex;
  gap: 20rpx;
  padding: 0 30rpx;
  margin-bottom: 20rpx;
  
  .action-btn {
    flex: 1;
    height: 80rpx;
    background: #ffffff;
    border-radius: 20rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 12rpx;
    font-size: 28rpx;
    font-weight: 600;
    box-shadow: 0 4rpx 16rpx rgba(0,0,0,0.02);
    
    &:active { opacity: 0.7; }
  }
}

.calendar-card {
  background: #ffffff;
  margin: 0 30rpx 20rpx;
  border-radius: 32rpx;
  padding: 30rpx;
  box-shadow: 0 8rpx 30rpx rgba(0,0,0,0.03);
}

.card-header {
  margin-bottom: 24rpx;
  
  .card-title {
    font-size: 34rpx;
    font-weight: 800;
    color: #111827;
  }
}

.week-header {
  display: flex;
  margin-bottom: 12rpx;
  
  .week-cell {
    flex: 1;
    text-align: center;
    font-size: 24rpx;
    color: #6b7280;
    font-weight: 600;
    padding: 12rpx 0;
    
    .is-weekend { color: #d1d5db; }
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
  min-height: 100rpx;
  
  &.other-month { visibility: hidden; }
  
  &.today .day-num-wrap.is-today {
    background: #6366f1;
    .day-num { color: #ffffff; }
  }
}

.day-num-wrap {
  width: 56rpx;
  height: 56rpx;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 8rpx;
  
  .day-num {
    font-size: 28rpx;
    font-weight: 600;
    color: #374151;
  }
}

.day-dots {
  display: flex;
  gap: 6rpx;
  
  .dot {
    width: 10rpx;
    height: 10rpx;
    border-radius: 50%;
    
    &.dot-normal { background: #10b981; }
    &.dot-late { background: #f59e0b; }
    &.dot-early { background: #f59e0b; }
    &.dot-absent { background: #e5e7eb; }
    &.dot-has { background: #6366f1; }
  }
}

.detail-card {
  background: #ffffff;
  margin: 0 30rpx;
  border-radius: 32rpx;
  padding: 30rpx;
  box-shadow: 0 8rpx 30rpx rgba(0,0,0,0.03);
}

.detail-list {
  .detail-item {
    display: flex;
    align-items: center;
    padding: 24rpx 16rpx;
    border-bottom: 1rpx solid #f3f4f6;
    
    &:last-child { border-bottom: none; }
    
    .detail-date {
      width: 90rpx;
      display: flex;
      flex-direction: column;
      align-items: center;
      margin-right: 24rpx;
      
      .date-day {
        font-size: 36rpx;
        font-weight: 800;
        color: #1f2937;
      }
      
      .date-week {
        font-size: 22rpx;
        color: #9ca3af;
        margin-top: 4rpx;
      }
    }
    
    .detail-times {
      flex: 1;
      display: flex;
      flex-direction: column;
      gap: 12rpx;
      
      .time-row {
        display: flex;
        align-items: center;
        gap: 12rpx;
        
        .time-dot {
          width: 12rpx;
          height: 12rpx;
          border-radius: 50%;
          
          &.dot-normal { background: #10b981; }
          &.dot-late { background: #ef4444; }
          &.dot-early { background: #f59e0b; }
          &.dot-absent { background: #e5e7eb; }
        }
        
        .time-label {
          font-size: 26rpx;
          color: #6b7280;
          width: 56rpx;
        }
        
        .time-value {
          font-size: 28rpx;
          font-weight: 700;
          color: #1f2937;
          font-variant-numeric: tabular-nums;
        }
      }
    }
  }
}
</style>
