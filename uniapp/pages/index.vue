<template>
  <view class="page-wrap">
    <!-- 顶部背景 -->
    <view class="header-bg">
      <view class="bg-orb bg-orb-1"></view>
      <view class="bg-orb bg-orb-2"></view>
      <view class="bg-orb bg-orb-3"></view>
    </view>

    <view class="main-content">
      <!-- 顶部用户信息 -->
      <view class="user-info">
        <view class="user-left">
          <view class="avatar-wrap">
            <u-avatar :src="avatar || ''" size="56" shape="circle" mode="aspectFill"></u-avatar>
            <view class="online-dot"></view>
          </view>
          <view class="info">
            <view class="name-row">
              <text class="greeting">{{ greetingText }}，</text>
              <text class="name">{{ name || '未登录' }}</text>
            </view>
            <view class="sub-row">
              <text class="date-text">{{ todayDateText }}</text>
              <text class="entry-days" v-if="entryDays >= 0">已入职 {{ entryDays }} 天</text>
              <text class="entry-days" v-else>欢迎使用</text>
            </view>
          </view>
        </view>
        <view class="header-actions">
          <view class="action-btn-circle" @click="goToNotification">
            <u-icon name="bell-fill" size="22" color="#ffffff"></u-icon>
            <view class="badge-dot"></view>
          </view>
        </view>
      </view>

      <!-- 考勤状态卡片 -->
      <view class="attendance-hero" @click="goToClock">
        <view class="hero-left">
          <view class="hero-time-row">
            <text class="hero-time">{{ currentTime }}</text>
            <text class="hero-seconds">{{ currentSeconds }}</text>
          </view>
          <text class="hero-weekday">{{ weekdayText }}</text>
        </view>
        <view class="hero-right">
          <view class="clock-pulse-btn">
            <view class="pulse-ring"></view>
            <view class="pulse-ring pulse-ring-2"></view>
            <view class="clock-btn-inner">
              <u-icon name="map-fill" size="28" color="#fff"></u-icon>
            </view>
          </view>
          <text class="clock-hint">去打卡</text>
        </view>
      </view>

      <!-- 数据概览卡片 -->
      <view class="stats-row">
        <view class="stat-card" @click="goToMyAttendance">
          <view class="stat-icon-wrap" style="background: rgba(99,102,241,0.1);">
            <u-icon name="calendar-fill" size="24" color="#6366f1"></u-icon>
          </view>
          <view class="stat-info">
            <text class="stat-value">{{ monthAttendance }}</text>
            <text class="stat-label">本月出勤</text>
          </view>
        </view>
        <view class="stat-card" @click="goToMyApply">
          <view class="stat-icon-wrap" style="background: rgba(245,158,11,0.1);">
            <u-icon name="file-text" size="24" color="#f59e0b"></u-icon>
          </view>
          <view class="stat-info">
            <text class="stat-value">{{ pendingApply }}</text>
            <text class="stat-label">待处理</text>
          </view>
        </view>
        <view class="stat-card" @click="goToApprovalList">
          <view class="stat-icon-wrap" style="background: rgba(16,185,129,0.1);">
            <u-icon name="checkmark-circle-fill" size="24" color="#10b981"></u-icon>
          </view>
          <view class="stat-info">
            <text class="stat-value">{{ approvalCount }}</text>
            <text class="stat-label">待我审批</text>
          </view>
        </view>
      </view>

      <!-- 快捷功能区 -->
      <view class="section-card" v-if="applyMenus.length > 0">
        <view class="section-header">
          <view class="title-with-dot">
            <view class="dot-indicator"></view>
            <text class="section-title">快捷申请</text>
          </view>
        </view>
        <view class="quick-grid">
          <view class="quick-item" v-for="(item, index) in applyMenus" :key="index" @click="handleMenuClick(item)">
            <view class="quick-icon" :style="getIconStyle(item)">
              <u-icon :name="getMenuIcon(item)" size="28" :color="getIconColor(item)"></u-icon>
            </view>
            <text class="quick-text">{{ item.menuName }}</text>
          </view>
        </view>
      </view>

      <!-- 待我审批区 -->
      <view class="section-card">
        <view class="section-header">
          <view class="title-with-dot">
            <view class="dot-indicator dot-green"></view>
            <text class="section-title">待我审批</text>
          </view>
          <view class="more-link" @click="goToApprovalList">
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

        <view class="empty-state" v-else style="padding: 40rpx 0 20rpx;">
          <u-icon name="checkmark-circle" size="48" color="#d1d5db"></u-icon>
          <text class="empty-text" style="margin-top: 16rpx;">暂无待审批事项</text>
        </view>
      </view>

      <!-- 我的申请区 -->
      <view class="section-card" style="margin-bottom: 40rpx;">
        <view class="section-header">
          <view class="title-with-dot">
            <view class="dot-indicator dot-amber"></view>
            <text class="section-title">最近申请</text>
          </view>
          <view class="more-link" @click="goToMyApply">
            <text>全部</text>
            <u-icon name="arrow-right" size="12" color="#9ca3af" customStyle="margin-left:4rpx"></u-icon>
          </view>
        </view>
        <view class="apply-list" v-if="recentApplies && recentApplies.length > 0">
          <view class="apply-item" v-for="(item, index) in recentApplies" :key="index">
            <view class="apply-left">
              <view class="apply-icon-wrap" :style="getApplyIconStyle(item)">
                <u-icon :name="getApplyIcon(item)" size="22" :color="getApplyIconColor(item)"></u-icon>
              </view>
              <view class="apply-info">
                <text class="apply-title">{{ getAppTypeName(item.appType) }}</text>
                <text class="apply-time">{{ formatApplyTime(item.createdTime) }}</text>
              </view>
            </view>
            <view class="apply-right">
              <view class="status-pill" :class="'status-' + item.status">
                <view class="status-dot-inner" :class="'dot-' + item.status"></view>
                <text>{{ getAppStatusName(item.status) }}</text>
              </view>
            </view>
          </view>
        </view>
        <view class="empty-state" v-else>
          <view class="empty-icon">
            <u-icon name="file-text" size="48" color="#d1d5db"></u-icon>
          </view>
          <text class="empty-text">暂无近期申请记录</text>
          <text class="empty-hint">点击上方快捷入口发起新申请</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import { mapGetters } from 'vuex'
import { getMobileMenus } from '@/api/menu'
import { getClockInfo, getHomeStats } from '@/api/attendance'
import { getMyApplications, getPendingApprovals } from '@/api/application'

export default {
  data() {
    return {
      currentTime: '',
      currentSeconds: '',
      applyMenus: [],
      recentApplies: [],
      pendingList: [],
      timer: null,
      monthAttendance: '--',
      pendingApply: '--',
      approvedCount: '--',
      approvalCount: '--'
    }
  },
  computed: {
    ...mapGetters(['avatar', 'name']),
    entryDays() {
      const info = this.$store.state.user.employeeInfo
      if (!info) return -1
      // 兼容驼峰和下划线两种字段名
      const dateStr = info.entryDate || info.entry_date
      if (!dateStr) return -1
      const entry = new Date(dateStr.replace(/-/g, '/'))
      const now = new Date()
      const diff = Math.floor((now - entry) / (1000 * 60 * 60 * 24))
      return diff >= 0 ? diff : -1
    },
    greetingText() {
      const hour = new Date().getHours()
      if (hour < 6) return '夜深了'
      if (hour < 9) return '早上好'
      if (hour < 12) return '上午好'
      if (hour < 14) return '中午好'
      if (hour < 18) return '下午好'
      return '晚上好'
    },
    todayDateText() {
      const now = new Date()
      const month = now.getMonth() + 1
      const day = now.getDate()
      return `${now.getFullYear()}年${month}月${day}日`
    },
    weekdayText() {
      const weekDays = ['星期日', '星期一', '星期二', '星期三', '星期四', '星期五', '星期六']
      return weekDays[new Date().getDay()]
    }
  },
  onShow() {
    this.loadMenus()
    this.loadClockInfo()
    this.loadHomeStats()
    this.loadRecentApplies()
    this.loadPendingList()
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
      const seconds = String(now.getSeconds()).padStart(2, '0')
      this.currentTime = `${hours}:${minutes}`
      this.currentSeconds = seconds
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
        'calendar-fill': { bg: 'rgba(56, 189, 248, 0.12)' },
        'account-fill': { bg: 'rgba(52, 211, 153, 0.12)' },
        'clock-fill': { bg: 'rgba(251, 146, 60, 0.12)' },
        'edit-pen-fill': { bg: 'rgba(99, 102, 241, 0.12)' },
        'car-fill': { bg: 'rgba(244, 114, 182, 0.12)' },
        'minus-circle-fill': { bg: 'rgba(248, 113, 113, 0.12)' },
        'list-dot': { bg: 'rgba(167, 139, 250, 0.12)' },
        'reload': { bg: 'rgba(52, 211, 153, 0.12)' },
        'star-fill': { bg: 'rgba(251, 191, 36, 0.12)' }
      }
      let iconName = this.getMenuIcon(item)
      return { backgroundColor: (colors[iconName] || colors['star-fill']).bg }
    },
    getIconColor(item) {
      const colors = {
        'calendar-fill': '#0284c7',
        'account-fill': '#059669',
        'clock-fill': '#ea580c',
        'edit-pen-fill': '#4f46e5',
        'car-fill': '#db2777',
        'minus-circle-fill': '#dc2626',
        'list-dot': '#7c3aed',
        'reload': '#059669',
        'star-fill': '#d97706'
      }
      let iconName = this.getMenuIcon(item)
      return colors[iconName] || colors['star-fill']
    },
    getApplyIcon(item) {
      const name = this.getAppTypeName(item.appType)
      if (!name) return 'file-text'
      if (name.includes('假')) return 'calendar-fill'
      if (name.includes('加班')) return 'clock-fill'
      if (name.includes('补卡')) return 'edit-pen-fill'
      if (name.includes('出差')) return 'car-fill'
      if (name.includes('转正')) return 'account-fill'
      if (name.includes('离职')) return 'minus-circle-fill'
      if (name.includes('调动')) return 'list-dot'
      if (name.includes('换休')) return 'reload'
      return 'file-text'
    },
    getApplyIconStyle(item) {
      const iconName = this.getApplyIcon(item)
      const map = {
        'calendar-fill': 'rgba(56,189,248,0.1)',
        'clock-fill': 'rgba(251,146,60,0.1)',
        'edit-pen-fill': 'rgba(99,102,241,0.1)',
        'car-fill': 'rgba(244,114,182,0.1)',
        'account-fill': 'rgba(52,211,153,0.1)',
        'minus-circle-fill': 'rgba(248,113,113,0.1)',
        'list-dot': 'rgba(167,139,250,0.1)',
        'reload': 'rgba(52,211,153,0.1)',
        'file-text': 'rgba(245,158,11,0.1)'
      }
      return { backgroundColor: map[iconName] || map['file-text'] }
    },
    getApplyIconColor(item) {
      const iconName = this.getApplyIcon(item)
      const map = {
        'calendar-fill': '#0284c7',
        'clock-fill': '#ea580c',
        'edit-pen-fill': '#4f46e5',
        'car-fill': '#db2777',
        'account-fill': '#059669',
        'minus-circle-fill': '#dc2626',
        'list-dot': '#7c3aed',
        'reload': '#059669',
        'file-text': '#f59e0b'
      }
      return map[iconName] || map['file-text']
    },
    getAppTypeName(appType) {
      const map = {
        leave: '请假', overtime: '加班', business: '出差',
        makeup: '补卡', exchange: '换休', regularization: '转正',
        transfer: '调动', reward: '奖励', punish: '惩罚', resignation: '离职'
      }
      return map[appType] || appType || '申请'
    },
    getAppStatusName(status) {
      const map = { 0: '待审批', 1: '已通过', 2: '已拒绝', 3: '已撤销' }
      return map[status] ?? '未知'
    },
    formatApplyTime(timeStr) {
      if (!timeStr) return ''
      // 简化时间显示
      const now = new Date()
      const target = new Date(timeStr.replace(/-/g, '/'))
      const diff = now.getTime() - target.getTime()
      const minutes = Math.floor(diff / 60000)
      if (minutes < 1) return '刚刚'
      if (minutes < 60) return `${minutes}分钟前`
      const hours = Math.floor(minutes / 60)
      if (hours < 24) return `${hours}小时前`
      const days = Math.floor(hours / 24)
      if (days < 7) return `${days}天前`
      // 超过7天显示日期
      return timeStr.substring(0, 10)
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
    async loadClockInfo() {
      try {
        const res = await getClockInfo()
        if (res.code === 200 && res.data) {
          // 填充考勤相关的统计数据
          if (res.data.todayDaily) {
            // 使用已有数据
          }
        }
      } catch (e) {
        console.log('加载考勤信息失败', e)
      }
    },
    async loadHomeStats() {
      try {
        const res = await getHomeStats()
        if (res.code === 200 && res.data) {
          this.monthAttendance = res.data.monthAttendDays ?? 0
          this.pendingApply = res.data.pendingCount ?? 0
          this.approvedCount = res.data.approvedCount ?? 0
          this.approvalCount = res.data.approvalCount ?? 0
        }
      } catch (e) {
        console.log('加载首页统计失败', e)
      }
    },
    async loadRecentApplies() {
      try {
        const info = this.$store.state.user.employeeInfo || uni.getStorageSync('userInfo')
        if (!info || !info.id) return
        const res = await getMyApplications({ pageNum: 1, pageSize: 5, employeeId: info.id })
        if (res.code === 200 && res.data && res.data.records) {
          this.recentApplies = res.data.records
        }
      } catch (e) {
        console.log('加载最近申请失败', e)
      }
    },
    goToClock() {
      uni.navigateTo({ url: '/pages/clock/index' })
    },
    goToMyAttendance() {
      uni.navigateTo({ url: '/pages/attendance/index' })
    },
    handleMenuClick(item) {
      if (item.path) {
        uni.navigateTo({ url: item.path })
      } else {
        this.$modal.showToast('功能开发中~')
      }
    },
    goToMyApply() {
      uni.navigateTo({ url: '/pages/apply/list/index?status=0' })
    },
    goToLeave() {
      uni.navigateTo({ url: '/pages/apply/list/index?status=1' })
    },
    goToApprovalList() {
      uni.navigateTo({ url: '/pages/approval/list/index' })
    },
    getFirstChar(name) {
      if (!name) return '?'
      return name.charAt(0)
    },
    goToApprovalDetail(item) {
      uni.navigateTo({ url: '/pages/approval/detail/index?id=' + item.id })
    },
    async loadPendingList() {
      try {
        const res = await getPendingApprovals({ pageNum: 1, pageSize: 5 })
        if (res.code === 200 && res.data) {
          const typeMap = {
            leave: '请假', overtime: '加班', business: '出差',
            makeup: '补卡', exchange: '换休', regularization: '转正',
            transfer: '调动', reward: '奖励', punish: '惩罚', resignation: '离职'
          }
          this.pendingList = (res.data.records || []).map(item => ({
            id: item.id,
            applicantName: item.employeeName || '未知',
            typeName: typeMap[item.appType] || item.appType || '申请',
            createTime: item.createdTime ? item.createdTime.substring(0, 16) : ''
          }))
        }
      } catch (e) {
        console.log('加载待审批列表失败', e)
      }
    },
    goToNotification() {
      this.$modal.showToast('暂无新消息')
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

// === 顶部背景 ===
.header-bg {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 520rpx;
  background: linear-gradient(145deg, #4f46e5 0%, #6366f1 40%, #818cf8 100%);
  border-bottom-left-radius: 48rpx;
  border-bottom-right-radius: 48rpx;
  z-index: 0;
  overflow: hidden;
}

.bg-orb {
  position: absolute;
  border-radius: 50%;

  &.bg-orb-1 {
    top: -60rpx;
    right: -40rpx;
    width: 280rpx;
    height: 280rpx;
    background: radial-gradient(circle, rgba(255, 255, 255, 0.15) 0%, rgba(255, 255, 255, 0) 70%);
  }

  &.bg-orb-2 {
    bottom: 40rpx;
    left: -60rpx;
    width: 200rpx;
    height: 200rpx;
    background: radial-gradient(circle, rgba(255, 255, 255, 0.1) 0%, rgba(255, 255, 255, 0) 70%);
  }

  &.bg-orb-3 {
    top: 120rpx;
    left: 40%;
    width: 160rpx;
    height: 160rpx;
    background: radial-gradient(circle, rgba(255, 255, 255, 0.06) 0%, rgba(255, 255, 255, 0) 70%);
  }
}

// === 主内容区 ===
.main-content {
  position: relative;
  z-index: 1;
  padding: 0 28rpx;
}

// === 用户信息 ===
.user-info {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 100rpx 6rpx 36rpx;

  .user-left {
    display: flex;
    align-items: center;
  }

  .avatar-wrap {
    position: relative;
    border: 3rpx solid rgba(255, 255, 255, 0.35);
    border-radius: 50%;
    padding: 3rpx;

    .online-dot {
      position: absolute;
      bottom: 4rpx;
      right: 4rpx;
      width: 18rpx;
      height: 18rpx;
      background: #34d399;
      border-radius: 50%;
      border: 3rpx solid #6366f1;
      z-index: 2;
    }
  }

  .info {
    margin-left: 22rpx;

    .name-row {
      display: flex;
      align-items: baseline;

      .greeting {
        font-size: 28rpx;
        color: rgba(255, 255, 255, 0.85);
      }

      .name {
        font-size: 36rpx;
        font-weight: 800;
        color: #ffffff;
      }
    }

    .sub-row {
      display: flex;
      align-items: center;
      gap: 12rpx;
      margin-top: 6rpx;
    }

    .date-text {
      font-size: 24rpx;
      color: rgba(255, 255, 255, 0.6);
    }

    .entry-days {
      font-size: 22rpx;
      color: rgba(255, 255, 255, 0.85);
      background: rgba(255, 255, 255, 0.15);
      padding: 2rpx 14rpx;
      border-radius: 20rpx;
    }
  }
}

.header-actions {
  display: flex;
  gap: 16rpx;
}

.action-btn-circle {
  position: relative;
  width: 72rpx;
  height: 72rpx;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.15);
  backdrop-filter: blur(10px);
  display: flex;
  align-items: center;
  justify-content: center;

  &:active {
    transform: scale(0.92);
    background: rgba(255, 255, 255, 0.25);
  }

  .badge-dot {
    position: absolute;
    top: 16rpx;
    right: 16rpx;
    width: 14rpx;
    height: 14rpx;
    background: #f43f5e;
    border-radius: 50%;
    border: 3rpx solid rgba(99, 102, 241, 0.8);
  }
}

// === 考勤主卡片 ===
.attendance-hero {
  background: rgba(255, 255, 255, 0.97);
  border-radius: 28rpx;
  padding: 40rpx 36rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
  box-shadow: 0 20rpx 60rpx rgba(79, 70, 229, 0.18), 0 4rpx 16rpx rgba(0, 0, 0, 0.04);
  margin-bottom: 28rpx;

  &:active {
    transform: scale(0.985);
  }

  .hero-left {
    .hero-time-row {
      display: flex;
      align-items: baseline;

      .hero-time {
        font-size: 72rpx;
        font-weight: 800;
        color: #1e1b4b;
        letter-spacing: 2rpx;
        font-variant-numeric: tabular-nums;
      }

      .hero-seconds {
        font-size: 32rpx;
        font-weight: 600;
        color: #a5b4fc;
        margin-left: 8rpx;
        font-variant-numeric: tabular-nums;
      }
    }

    .hero-weekday {
      display: block;
      font-size: 26rpx;
      color: #6b7280;
      margin-top: 4rpx;
      font-weight: 500;
    }
  }

  .hero-right {
    display: flex;
    flex-direction: column;
    align-items: center;

    .clock-hint {
      font-size: 22rpx;
      color: #6366f1;
      font-weight: 600;
      margin-top: 12rpx;
    }
  }
}

.clock-pulse-btn {
  position: relative;
  width: 108rpx;
  height: 108rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.pulse-ring {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  border-radius: 50%;
  border: 3rpx solid rgba(99, 102, 241, 0.25);
  animation: pulseRing 2.5s ease-out infinite;

  &.pulse-ring-2 {
    animation-delay: 1.25s;
  }
}

@keyframes pulseRing {
  0% {
    transform: scale(0.8);
    opacity: 1;
  }

  100% {
    transform: scale(1.4);
    opacity: 0;
  }
}

.clock-btn-inner {
  width: 80rpx;
  height: 80rpx;
  border-radius: 50%;
  background: linear-gradient(135deg, #6366f1 0%, #4f46e5 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 8rpx 24rpx rgba(99, 102, 241, 0.4);
  position: relative;
  z-index: 1;
}

// === 数据概览 ===
.stats-row {
  display: flex;
  gap: 16rpx;
  margin-bottom: 28rpx;
}

.stat-card {
  flex: 1;
  background: #ffffff;
  border-radius: 24rpx;
  padding: 28rpx 20rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.04);
  transition: all 0.2s;

  &:active {
    transform: scale(0.96);
  }

  .stat-icon-wrap {
    width: 72rpx;
    height: 72rpx;
    border-radius: 22rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    margin-bottom: 16rpx;
  }

  .stat-info {
    text-align: center;

    .stat-value {
      display: block;
      font-size: 40rpx;
      font-weight: 800;
      color: #1f2937;
      font-variant-numeric: tabular-nums;
    }

    .stat-label {
      display: block;
      font-size: 22rpx;
      color: #9ca3af;
      margin-top: 4rpx;
      font-weight: 500;
    }
  }
}

// === 通用 Section 卡片 ===
.section-card {
  background: #ffffff;
  border-radius: 28rpx;
  padding: 32rpx 28rpx;
  margin-bottom: 24rpx;
  box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.04);
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 32rpx;

  .title-with-dot {
    display: flex;
    align-items: center;

    .dot-indicator {
      width: 10rpx;
      height: 10rpx;
      border-radius: 50%;
      background: #6366f1;
      margin-right: 14rpx;
      box-shadow: 0 0 0 4rpx rgba(99, 102, 241, 0.15);

      &.dot-amber {
        background: #f59e0b;
        box-shadow: 0 0 0 4rpx rgba(245, 158, 11, 0.15);
      }
    }

    .section-title {
      font-size: 32rpx;
      font-weight: 800;
      color: #111827;
    }
  }

  .more-link {
    display: flex;
    align-items: center;
    font-size: 24rpx;
    color: #9ca3af;
    padding: 8rpx 16rpx;
    border-radius: 20rpx;
    background: #f9fafb;

    &:active {
      background: #f3f4f6;
    }
  }
}

// === 快捷功能网格 ===
.quick-grid {
  display: flex;
  flex-wrap: wrap;
}

.quick-item {
  width: 25%;
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 28rpx;

  .quick-icon {
    width: 92rpx;
    height: 92rpx;
    border-radius: 26rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    margin-bottom: 14rpx;
    transition: all 0.15s ease;

    &:active {
      transform: scale(0.9);
    }
  }

  .quick-text {
    font-size: 24rpx;
    color: #4b5563;
    font-weight: 600;
  }
}

// === 申请列表 ===
.apply-list {
  .apply-item {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 22rpx 20rpx;
    margin-bottom: 16rpx;
    background: #fafafa;
    border-radius: 20rpx;
    transition: all 0.15s;

    &:last-child {
      margin-bottom: 0;
    }

    &:active {
      background: #f3f4f6;
    }

    .apply-left {
      display: flex;
      align-items: center;
      flex: 1;
      min-width: 0;

      .apply-icon-wrap {
        width: 72rpx;
        height: 72rpx;
        border-radius: 20rpx;
        display: flex;
        align-items: center;
        justify-content: center;
        flex-shrink: 0;
      }

      .apply-info {
        margin-left: 20rpx;
        min-width: 0;

        .apply-title {
          display: block;
          font-size: 28rpx;
          color: #1f2937;
          font-weight: 700;
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
        }

        .apply-time {
          display: block;
          font-size: 22rpx;
          color: #9ca3af;
          margin-top: 6rpx;
        }
      }
    }

    .apply-right {
      flex-shrink: 0;
      margin-left: 16rpx;
    }
  }
}

// === 状态标签 ===
.status-pill {
  display: flex;
  align-items: center;
  gap: 8rpx;
  font-size: 22rpx;
  padding: 8rpx 18rpx;
  border-radius: 20rpx;
  font-weight: 600;

  .status-dot-inner {
    width: 10rpx;
    height: 10rpx;
    border-radius: 50%;
  }

  &.status-0 {
    background: #eef2ff;
    color: #6366f1;

    .dot-0 {
      background: #6366f1;
    }
  }

  &.status-1 {
    background: #ecfdf5;
    color: #059669;

    .dot-1 {
      background: #059669;
    }
  }

  &.status-2 {
    background: #fef2f2;
    color: #dc2626;

    .dot-2 {
      background: #dc2626;
    }
  }
}

// === 待审批列表 ===
.approval-list {
  .approval-item {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 24rpx 20rpx;
    margin-bottom: 16rpx;
    background: #fafafa;
    border-radius: 20rpx;
    transition: all 0.15s;

    &:last-child {
      margin-bottom: 0;
    }

    &:active {
      background: #f3f4f6;
    }

    .approval-left {
      display: flex;
      align-items: center;
      flex: 1;
      min-width: 0;

      .avatar-box {
        width: 72rpx;
        height: 72rpx;
        border-radius: 22rpx;
        background: linear-gradient(135deg, #60a5fa 0%, #3b82f6 100%);
        color: #fff;
        font-size: 30rpx;
        font-weight: bold;
        display: flex;
        align-items: center;
        justify-content: center;
        flex-shrink: 0;
      }

      .info-content {
        margin-left: 20rpx;
        min-width: 0;

        .type-name {
          font-size: 28rpx;
          color: #1f2937;
          font-weight: 700;
        }

        .meta-info {
          font-size: 22rpx;
          color: #9ca3af;
          margin-top: 6rpx;
          display: flex;
          align-items: center;

          .dot {
            margin: 0 8rpx;
            color: #d1d5db;
          }
        }
      }
    }

    .approval-right {
      flex-shrink: 0;
      margin-left: 16rpx;

      .action-btn {
        padding: 10rpx 28rpx;
        border-radius: 32rpx;
        background: #eef2ff;
        color: #4f46e5;
        font-size: 24rpx;
        font-weight: 600;

        &:active {
          background: #e0e7ff;
        }
      }
    }
  }
}

.dot-green {
  background: #10b981 !important;
  box-shadow: 0 0 0 4rpx rgba(16, 185, 129, 0.15) !important;
}

// === 空状态 ===
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 60rpx 0 40rpx;

  .empty-icon {
    width: 120rpx;
    height: 120rpx;
    border-radius: 50%;
    background: #f9fafb;
    display: flex;
    align-items: center;
    justify-content: center;
    margin-bottom: 24rpx;
  }

  .empty-text {
    font-size: 28rpx;
    color: #9ca3af;
    font-weight: 600;
  }

  .empty-hint {
    font-size: 22rpx;
    color: #d1d5db;
    margin-top: 10rpx;
  }
}
</style>
