<template>
  <view class="mine-page tn-safe-area-inset-bottom">
    <view class="page-inner" :style="{ paddingTop: vuex_custom_bar_height + 18 + 'px' }">
      <view class="profile-panel">
        <view class="avatar-wrap">
          <image v-if="avatarUrl" :src="avatarUrl" mode="aspectFill" class="avatar-image"></image>
          <view v-else class="avatar-fallback">{{ firstChar(displayName) }}</view>
        </view>
        <view class="profile-main">
          <view class="profile-name">{{ displayName }}</view>
          <view class="profile-sub clamp-1">{{ displaySubtitle }}</view>
          <view class="profile-tags">
            <text class="tag">工号 {{ employee.employeeNo || '--' }}</text>
            <text v-if="employee.entryDate" class="tag">入职 {{ formatDate(employee.entryDate) }}</text>
          </view>
        </view>

      </view>

      <view class="stats-card">
        <view class="stats-item" @click="go('/workPages/calendar')">
          <view class="stats-num">{{ monthStats.normalIn }}</view>
          <view class="stats-label">本月出勤(天)</view>
        </view>
        <view class="stats-item" @click="go('/homePages/approval')">
          <view class="stats-num orange">{{ applyStats.pending }}</view>
          <view class="stats-label">待审批(条)</view>
        </view>
        <view class="stats-item" @click="go('/homePages/approval')">
          <view class="stats-num green">{{ applyStats.approved }}</view>
          <view class="stats-label">已通过(条)</view>
        </view>
      </view>

      <view class="quick-grid">
        <view class="quick-item" @click="go('/homePages/pending')">
          <view class="quick-icon blue"><tn-icon name="flag-fill"></tn-icon></view>
          <text>待办事项</text>
        </view>
        <view class="quick-item" @click="go('/minePages/nav')">
          <view class="quick-icon cyan"><tn-icon name="task-fill"></tn-icon></view>
          <text>快捷导航</text>
        </view>
        <view class="quick-item" @click="go('/minePages/set')">
          <view class="quick-icon orange"><tn-icon name="identity-fill"></tn-icon></view>
          <text>个人信息</text>
        </view>
        <view class="quick-item" @click="go('/minePages/help')">
          <view class="quick-icon purple"><tn-icon name="help-fill"></tn-icon></view>
          <text>帮助中心</text>
        </view>
      </view>

      <view class="info-card">
        <view class="info-row" @click="copyEmployeeNo">
          <view class="row-icon blue"><tn-icon name="copy"></tn-icon></view>
          <view class="row-title">复制工号</view>
          <view class="row-value">{{ employee.employeeNo || '--' }}</view>
          <tn-icon name="right" class="row-arrow"></tn-icon>
        </view>
        <view class="info-row" @click="go('/minePages/feedback')">
          <view class="row-icon cyan"><tn-icon name="edit-write"></tn-icon></view>
          <view class="row-title">意见反馈</view>
          <view class="row-value">提交问题或建议</view>
          <tn-icon name="right" class="row-arrow"></tn-icon>
        </view>
      </view>

      <view class="notice-card" @click="go('/homePages/notice')">
        <view class="notice-head">
          <view class="row-icon orange"><tn-icon name="notice-fill"></tn-icon></view>
          <view class="notice-head-title">最新公告</view>
          <tn-icon name="right" class="row-arrow"></tn-icon>
        </view>
        <view v-if="latestNotices.length" class="notice-list">
          <view v-for="n in latestNotices" :key="n.id" class="notice-item">
            <text class="notice-dot"></text>
            <text class="notice-title clamp-1">{{ n.noticeTitle }}</text>
            <text class="notice-date">{{ n.date }}</text>
          </view>
        </view>
        <view v-else class="notice-empty">暂无公告</view>
      </view>

      <view class="logout-wrap">
        <tn-button
          bg-color="#FB6A67"
          :custom-style="{ padding: '22rpx' }"
          width="100%"
          :font-size="28"
          text-color="#FFFFFF"
          shape="round"
          @click="handleLogout"
        >
          <text>退出登录</text>
        </tn-button>
      </view>
    </view>

    <view class="tn-tabbar-height"></view>
  </view>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useStore } from 'vuex'
import { getMonthAttendance } from '@/api/attendance'
import { getMyApplications } from '@/api/application'
import { getNoticeList } from '@/api/system/notice'
import { toastRequestError } from '@/utils/common'

const store = useStore()

const monthStats = ref({ normalIn: 0 })
const applyStats = ref({ pending: 0, approved: 0 })
const latestNotices = ref([])

async function loadMyStats() {
  const info = employee.value || {}
  const myId = store.getters.id || info.id
  if (!myId) return
  const now = new Date()
  const month = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}`
  const [att, pending, approved, notices] = await Promise.allSettled([
    getMonthAttendance(month),
    getMyApplications({ pageNum: 1, pageSize: 1, status: 0, employeeId: myId }),
    getMyApplications({ pageNum: 1, pageSize: 1, status: 1, employeeId: myId }),
    getNoticeList({ status: 1 })
  ])
  if (att.status === 'fulfilled' && att.value.data?.stats) {
    monthStats.value = { normalIn: Number(att.value.data.stats.normalIn || 0) }
  }
  const total = (r) => {
    if (r.status !== 'fulfilled') return 0
    // rows 风格响应的 total 在扁平化信封上(request.js 已透传)
    const envelope = r.value
    if (envelope && envelope.total !== undefined && !Array.isArray(envelope.data)) return Number(envelope.total)
    const d = envelope?.data
    if (Array.isArray(d)) return d.length
    return Number(d?.total || 0)
  }
  applyStats.value = { pending: total(pending), approved: total(approved) }
  if (notices.status === 'fulfilled') {
    const d = notices.value.data
    const list = Array.isArray(d) ? d : (d?.records || d?.list || [])
    latestNotices.value = list.slice(0, 2).map((n) => ({
      id: n.id,
      noticeTitle: n.noticeTitle || '系统通知',
      date: String(n.publishTime || n.createdTime || '').replace('T', ' ').slice(0, 10)
    }))
  }
}
const vuex_custom_bar_height = computed(() => store.state.vuex_custom_bar_height)

const employee = computed(() => store.state.user.employeeInfo || uni.getStorageSync('userInfo') || {})
const displayName = computed(() => pick(employee.value.name, store.state.user.name, employee.value.employeeNo, '未填写'))
const avatarUrl = computed(() => pick(store.state.user.avatar, employee.value.avatar, ''))
const displaySubtitle = computed(() => {
  const job = pick(employee.value.position, employee.value.duty, employee.value.occupation, employee.value.post, employee.value.jobTitle)
  const dept = pick(employee.value.deptName, employee.value.companyName)
  return [job, dept].filter(Boolean).join(' · ') || '员工档案'
})

// 首次挂载加载数据,后续由父页面切换/下拉时刷新
onMounted(() => {
  store.dispatch('GetInfo')
  loadMyStats()
})

// 供 pages/index.vue 调用:刷新
defineExpose({
  refresh: async () => {
    store.dispatch('GetInfo')
    loadMyStats()
  }
})

function pick(...values) {
  return values.find((value) => value !== undefined && value !== null && String(value).trim() !== '') || ''
}

function firstChar(value) {
  return String(value || '?').slice(0, 1)
}

function formatDate(value) {
  if (!value) return ''
  return String(value).replace('T', ' ').slice(0, 10)
}

function go(url) {
  if (!url) return
  uni.navigateTo({ url })
}

function copyEmployeeNo() {
  if (!employee.value.employeeNo) {
    uni.showToast({ title: '暂无工号', icon: 'none' })
    return
  }
  uni.setClipboardData({ data: employee.value.employeeNo })
}

function handleLogout() {
  uni.showModal({
    title: '提示',
    content: '确定退出当前账号吗？',
    success: async (res) => {
      if (!res.confirm) return
      uni.showLoading({ title: '退出中...' })
      try {
        await store.dispatch('LogOut')
        uni.hideLoading()
        uni.reLaunch({ url: '/pages/login' })
      } catch (error) {
        uni.hideLoading()
        // request.js 已 toast 过的错误不重复提示,其余取 error.message 可读文案
        toastRequestError(error, '退出失败')
      }
    }
  })
}
</script>

<style lang="scss" scoped>
.mine-page {
  max-width: 640px;
  min-height: 100vh;
  margin: 0 auto;
  background: #f7f8fb;
}

.page-inner {
  padding: 0 24rpx;
}

.profile-panel,
.quick-grid,
.info-card {
  background: #fff;
  border-radius: 16rpx;
  border: 1rpx solid #EEF0F4;
}

.profile-panel {
  padding: 30rpx;
  display: flex;
  align-items: center;
}

.avatar-wrap,
.avatar-image,
.avatar-fallback {
  width: 112rpx;
  height: 112rpx;
  border-radius: 50%;
}

.avatar-wrap {
  flex-shrink: 0;
  overflow: hidden;
  background: #f3f6fb;
}

.avatar-image {
  display: block;
}

.avatar-fallback {
  color: #fff;
  background: linear-gradient(135deg, #8eb0d8, #b7c7d9);
  font-size: 42rpx;
  font-weight: 800;
  display: flex;
  align-items: center;
  justify-content: center;
}

.profile-main {
  flex: 1;
  min-width: 0;
  margin-left: 22rpx;
}

.profile-name {
  color: #1d2541;
  font-size: 38rpx;
  font-weight: 900;
}

.profile-sub {
  margin-top: 10rpx;
  color: #657189;
  font-size: 26rpx;
}

.profile-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 12rpx;
  margin-top: 16rpx;
}

.tag {
  padding: 6rpx 16rpx;
  color: #3d7eff;
  background: rgba(61, 126, 255, 0.1);
  border-radius: 999rpx;
  font-size: 22rpx;
}

.profile-arrow,
.row-arrow {
  color: #8ca0b3;
  font-size: 34rpx;
}

.stats-card {
  margin-top: 24rpx;
  padding: 30rpx 12rpx;
  background: #fff;
  border-radius: 20rpx;
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  box-shadow: 0 10rpx 30rpx rgba(29, 37, 65, 0.06);
}

.stats-item {
  text-align: center;
}

.stats-num {
  color: #1d2541;
  font-size: 36rpx;
  font-weight: 600;
}

.stats-num.orange {
  color: #FFAC00;
}

.stats-num.green {
  color: #00C8B0;
}

.stats-label {
  margin-top: 8rpx;
  color: #9aa4b2;
  font-size: 22rpx;
}

.quick-grid {
  margin-top: 24rpx;
  padding: 26rpx 12rpx;
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
}

.quick-item {
  min-width: 0;
  color: #1d2541;
  font-size: 24rpx;
  text-align: center;
}

.quick-icon,
.row-icon {
  display: flex;
  align-items: center;
  justify-content: center;
}

.quick-icon {
  width: 74rpx;
  height: 74rpx;
  margin: 0 auto 14rpx;
  border-radius: 50%;
  font-size: 38rpx;
}

.blue {
  color: #3d7eff;
  background: rgba(61, 126, 255, 0.1);
}

.cyan {
  color: #00c8b0;
  background: rgba(0, 200, 176, 0.12);
}

.orange {
  color: #ffac00;
  background: rgba(255, 172, 0, 0.14);
}

.purple {
  color: #957bfe;
  background: rgba(149, 123, 254, 0.12);
}

.info-card {
  margin-top: 24rpx;
}

.info-row {
  min-height: 104rpx;
  padding: 0 28rpx;
  border-bottom: 1rpx solid #eef1f6;
  display: flex;
  align-items: center;
}

.info-row:last-child {
  border-bottom: 0;
}

.row-icon {
  width: 58rpx;
  height: 58rpx;
  border-radius: 50%;
  font-size: 30rpx;
}

.row-title {
  flex: 1;
  margin-left: 18rpx;
  color: #1d2541;
  font-size: 29rpx;
  font-weight: 700;
}

.row-value {
  max-width: 260rpx;
  color: #8a93a3;
  font-size: 25rpx;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.notice-card {
  margin-top: 24rpx;
  padding: 26rpx 28rpx;
  background: #fff;
  border-radius: 16rpx;
  border: 1rpx solid #EEF0F4;
}

.notice-head {
  display: flex;
  align-items: center;
}

.notice-head-title {
  flex: 1;
  margin-left: 18rpx;
  font-size: 30rpx;
  font-weight: 700;
  color: #1d2541;
}

.notice-list {
  margin-top: 18rpx;
}

.notice-item {
  display: flex;
  align-items: center;
  min-height: 64rpx;
}

.notice-dot {
  flex: 0 0 10rpx;
  width: 10rpx;
  height: 10rpx;
  border-radius: 50%;
  background: #4B98FE;
  margin-right: 14rpx;
}

.notice-title {
  flex: 1;
  min-width: 0;
  font-size: 26rpx;
  color: #657189;
}

.notice-date {
  flex-shrink: 0;
  margin-left: 16rpx;
  font-size: 22rpx;
  color: #9aa4b2;
}

.notice-empty {
  margin-top: 12rpx;
  text-align: center;
  color: #9aa4b2;
  font-size: 24rpx;
}

.logout-wrap {
  margin-top: 34rpx;
}

.tn-tabbar-height {
  min-height: 120rpx;
  height: calc(140rpx + env(safe-area-inset-bottom) / 2);
}

.clamp-1 {
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 1;
  overflow: hidden;
}
</style>
