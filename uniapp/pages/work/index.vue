<template>
  <view class="page-wrap">
    <view class="top-bg"></view>

    <view class="main-content">
      <view class="page-header">
        <text class="page-title">工作台</text>
        <text class="page-subtitle">效率工具 · 一站直达</text>
      </view>

      <!-- 公告通知 -->
      <view class="section-card">
        <view class="section-header">
          <view class="title-with-dot">
            <view class="dot-indicator dot-amber"></view>
            <text class="title">公告通知</text>
          </view>
        </view>

        <view class="notice-list" v-if="noticeList.length > 0">
          <view class="notice-item" v-for="(item, index) in noticeList" :key="index" @click="viewNotice(item)">
            <view class="notice-left">
              <view class="notice-type-badge" :class="'badge-' + item.noticeType">
                {{ item.noticeType === '1' ? '公告' : '通知' }}
              </view>
              <view class="notice-info">
                <text class="notice-title">{{ item.noticeTitle }}</text>
                <text class="notice-time">{{ item.publishTime ? item.publishTime.substring(0, 10) : (item.createdTime ? item.createdTime.substring(0, 10) : '') }}</text>
              </view>
            </view>
            <u-icon name="arrow-right" size="14" color="#d1d5db"></u-icon>
          </view>
        </view>

        <view class="empty-mini" v-else>
          <u-icon name="bell" size="40" color="#d1d5db"></u-icon>
          <text class="empty-text">暂无公告通知</text>
        </view>
      </view>

      <!-- 本周考勤概览 -->
      <view class="section-card" style="margin-bottom: 30rpx;">
        <view class="section-header">
          <view class="title-with-dot">
            <view class="dot-indicator dot-green"></view>
            <text class="title">本周出勤</text>
          </view>
          <view class="more-btn" @click="goToMyAttendance">
            <text>详情</text>
            <u-icon name="arrow-right" size="12" color="#9ca3af" customStyle="margin-left:4rpx"></u-icon>
          </view>
        </view>

        <view class="week-grid">
          <view class="week-day" v-for="(day, index) in weekDays" :key="index">
            <text class="day-label">{{ day.label }}</text>
            <view class="day-dot" :class="getDayStatus(day)"></view>
            <text class="day-date">{{ day.date }}</text>
          </view>
        </view>
        <view class="week-legend">
          <view class="legend-item"><view class="legend-dot dot-ok"></view><text>正常</text></view>
          <view class="legend-item"><view class="legend-dot dot-late"></view><text>迟到</text></view>
          <view class="legend-item"><view class="legend-dot dot-absent"></view><text>缺勤</text></view>
          <view class="legend-item"><view class="legend-dot dot-rest"></view><text>休息</text></view>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import { getClockInfo } from '@/api/attendance'
import { getNoticeList } from '@/api/system/notice'

export default {
  data() {
    return {
      noticeList: [],
      weekAttendance: {}
    }
  },
  computed: {
    weekDays() {
      const days = ['一', '二', '三', '四', '五', '六', '日']
      const now = new Date()
      const dayOfWeek = now.getDay() || 7 // 1=周一...7=周日
      const result = []
      for (let i = 1; i <= 7; i++) {
        const d = new Date(now)
        d.setDate(now.getDate() - dayOfWeek + i)
        result.push({
          label: '周' + days[i - 1],
          date: (d.getMonth() + 1) + '/' + d.getDate(),
          dateStr: d.toISOString().substring(0, 10),
          isToday: i === dayOfWeek,
          isFuture: i > dayOfWeek,
          isWeekend: i >= 6
        })
      }
      return result
    }
  },
  onShow() {
    this.loadNotices()
    this.loadWeekAttendance()
  },
  methods: {
    async loadNotices() {
      try {
        const res = await getNoticeList({ status: 1 })
        if (res.code === 200 && res.data) {
          this.noticeList = Array.isArray(res.data) ? res.data.slice(0, 5) : (res.data.records || [])
        }
      } catch (e) {
        this.noticeList = []
      }
    },
    async loadWeekAttendance() {
      try {
        const res = await getClockInfo()
        if (res.code === 200 && res.data) {
          this.weekAttendance = res.data.weekStatus || {}
        }
      } catch (e) {
        this.weekAttendance = {}
      }
    },
    getDayStatus(day) {
      if (day.isFuture) return 'status-future'
      if (day.isWeekend) return 'status-rest'
      const status = this.weekAttendance[day.dateStr]
      if (status === 'normal') return 'status-ok'
      if (status === 'late') return 'status-late'
      if (status === 'absent') return 'status-absent'
      if (day.isToday) return 'status-today'
      return 'status-rest'
    },
    viewNotice(item) {
      if (item.noticeContent) {
        uni.navigateTo({
          url: '/pages/common/textview/index?title=' + encodeURIComponent(item.noticeTitle) + '&content=' + encodeURIComponent(item.noticeContent)
        })
      }
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
  background-color: #f0f1f5;
  position: relative;
}

.top-bg {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 340rpx;
  background: #f0f1f5;
  z-index: 0;
}

.main-content {
  position: relative;
  z-index: 1;
  padding: 0 28rpx;
}

.page-header {
  padding: 100rpx 0 36rpx;

  .page-title {
    display: block;
    font-size: 48rpx;
    font-weight: 800;
    color: #111827;
  }

  .page-subtitle {
    display: block;
    font-size: 26rpx;
    color: #9ca3af;
    margin-top: 10rpx;
  }
}

// === 卡片通用 ===
.section-card {
  background: #ffffff;
  border-radius: 28rpx;
  padding: 32rpx 28rpx;
  margin-bottom: 24rpx;
  box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.04);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 32rpx;

  .title-with-dot {
    display: flex;
    align-items: center;

    .dot-indicator {
      width: 10rpx;
      height: 10rpx;
      border-radius: 50%;
      margin-right: 14rpx;

      &.dot-amber {
        background: #f59e0b;
        box-shadow: 0 0 0 4rpx rgba(245, 158, 11, 0.15);
      }

      &.dot-green {
        background: #10b981;
        box-shadow: 0 0 0 4rpx rgba(16, 185, 129, 0.15);
      }
    }
  }

  .title {
    font-size: 32rpx;
    font-weight: 800;
    color: #111827;
  }

  .more-btn {
    display: flex;
    align-items: center;
    font-size: 24rpx;
    color: #9ca3af;
    padding: 8rpx 16rpx;
    border-radius: 20rpx;
    background: #f9fafb;
  }
}

// === 公告通知 ===
.notice-list {
  .notice-item {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 24rpx 20rpx;
    margin-bottom: 12rpx;
    background: #fafafa;
    border-radius: 18rpx;

    &:last-child {
      margin-bottom: 0;
    }

    &:active {
      background: #f3f4f6;
    }

    .notice-left {
      display: flex;
      align-items: center;
      flex: 1;
      min-width: 0;

      .notice-type-badge {
        font-size: 20rpx;
        padding: 6rpx 16rpx;
        border-radius: 10rpx;
        font-weight: 700;
        flex-shrink: 0;

        &.badge-1 {
          background: #eef2ff;
          color: #6366f1;
        }

        &.badge-2 {
          background: #fef3c7;
          color: #d97706;
        }
      }

      .notice-info {
        margin-left: 20rpx;
        min-width: 0;

        .notice-title {
          display: block;
          font-size: 28rpx;
          color: #1f2937;
          font-weight: 600;
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
        }

        .notice-time {
          display: block;
          font-size: 22rpx;
          color: #9ca3af;
          margin-top: 6rpx;
        }
      }
    }
  }
}

// === 本周考勤 ===
.week-grid {
  display: flex;
  justify-content: space-between;
  margin-bottom: 24rpx;
}

.week-day {
  display: flex;
  flex-direction: column;
  align-items: center;
  flex: 1;

  .day-label {
    font-size: 22rpx;
    color: #9ca3af;
    font-weight: 600;
    margin-bottom: 16rpx;
  }

  .day-dot {
    width: 28rpx;
    height: 28rpx;
    border-radius: 50%;
    margin-bottom: 12rpx;

    &.status-ok {
      background: #10b981;
      box-shadow: 0 0 0 4rpx rgba(16, 185, 129, 0.2);
    }

    &.status-late {
      background: #f59e0b;
      box-shadow: 0 0 0 4rpx rgba(245, 158, 11, 0.2);
    }

    &.status-absent {
      background: #ef4444;
      box-shadow: 0 0 0 4rpx rgba(239, 68, 68, 0.2);
    }

    &.status-rest {
      background: #e5e7eb;
    }

    &.status-today {
      background: #6366f1;
      box-shadow: 0 0 0 4rpx rgba(99, 102, 241, 0.2);
    }

    &.status-future {
      background: #f3f4f6;
      border: 2rpx dashed #d1d5db;
    }
  }

  .day-date {
    font-size: 20rpx;
    color: #9ca3af;
  }
}

.week-legend {
  display: flex;
  justify-content: center;
  gap: 32rpx;
  padding-top: 8rpx;

  .legend-item {
    display: flex;
    align-items: center;
    font-size: 20rpx;
    color: #9ca3af;

    .legend-dot {
      width: 14rpx;
      height: 14rpx;
      border-radius: 50%;
      margin-right: 8rpx;

      &.dot-ok {
        background: #10b981;
      }

      &.dot-late {
        background: #f59e0b;
      }

      &.dot-absent {
        background: #ef4444;
      }

      &.dot-rest {
        background: #e5e7eb;
      }
    }
  }
}

// === 空状态 ===
.empty-mini {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 40rpx 0 20rpx;

  .empty-text {
    font-size: 24rpx;
    color: #d1d5db;
    margin-top: 16rpx;
  }
}
</style>

