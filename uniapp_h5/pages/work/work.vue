<template>
  <view class="template-work tn-safe-area-inset-bottom">

    <!-- 问候卡片 -->
    <view class="work-card work-greet-card">
      <view class="work-greet-left">
        <view class="work-avatar-wrap">
          <image :src="userAvatar" mode="aspectFill"></image>
          <view class="work-online"></view>
        </view>
        <view class="work-greet-info">
          <view class="work-greet-title">{{ greetingText }},<text>{{ userName }}</text></view>
          <view class="work-greet-sub">
            <text>{{ todayDateText }}</text>
            <text class="work-greet-entry">{{ entryDaysLabel }}</text>
          </view>
        </view>
      </view>
      <view class="work-bell" @click="tn('/homePages/application')">
        <tn-icon name="notice-fill"></tn-icon>
        <view v-if="pendingCount > 0" class="work-bell-dot"></view>
      </view>
    </view>

    <!-- 打卡卡片 -->
    <view class="work-card work-clock-card" @click="goToClock">
      <view>
        <view class="work-clock-time">
          <text>{{ currentTime }}</text>
          <text class="work-clock-seconds">{{ currentSeconds }}</text>
        </view>
        <view class="work-clock-weekday">{{ weekdayText }}</view>
      </view>
      <view class="work-clock-go">
        <tn-icon name="location-fill" class="tn-color-white" style="font-size: 32rpx;"></tn-icon>
        <text class="work-clock-go-text">打卡</text>
      </view>
    </view>

    <!-- 申请菜单 -->
    <view class="work-card work-menu-card">
      <view class="work-menu-grid">
        <view v-for="(item, index) in icons" :key="index" class="work-menu-item" @click="tn(item.url)">
          <view class="work-menu-icon" :style="{ backgroundColor: softColor(item.color) }">
            <tn-icon :name="item.icon" :style="{ color: item.color, fontSize: '40rpx' }"></tn-icon>
          </view>
          <text class="work-menu-label tn-color-gray--dark">{{ item.title }}</text>
        </view>
      </view>
    </view>

    <!-- 考勤统计 -->
    <view class="work-card">
      <view class="work-card-head">
        <text class="work-card-title">考勤统计</text>
      </view>
      <view class="work-stats-grid">
        <view v-for="(item, index) in attendance" :key="index" class="work-stats-item" @click="tn(item.url)">
          <text class="work-stats-num" :style="{ color: item.color }">{{ item.title }}</text>
        </view>
      </view>
    </view>

    <view class="tn-tabbar-height"></view>

  </view>
</template>

<script setup>
import { computed, onMounted, onUnmounted, ref } from 'vue'
import { useStore } from 'vuex'
import { getClockInfo, getHomeStats } from '@/api/attendance'
import { getMobileMenus } from '@/api/menu'

const store = useStore()
// 使用 computed 保持响应式
const vuex_custom_bar_height = computed(() => store.state.vuex_custom_bar_height)

const pendingCount = ref(0)
const weekAttendance = ref({})
// 申请菜单宫格与考勤统计(模板中使用,缺失会直接 ReferenceError)
// 考勤统计直接用构建函数初始化,后续接口返回后在 loadWeekAttendance 中更新,避免 setup 顶层与 onMounted 重复初始化
const icons = ref([])
const attendance = ref(buildAttendanceItems())
const currentTime = ref('--:--')
const currentSeconds = ref('--')
const colorList = ['#4B98FE', '#FFAC00', '#00D05E', '#FB6A67', '#957BFE', '#00B9FE', '#CC52E2']
const topMenuOrder = ['请假申请', '加班申请', '补卡申请', '离职申请', '出差申请', '换休申请']
const topMenuColorMap = {
  '请假申请': '#4B98FE',
  '加班申请': '#FFAC00',
  '补卡申请': '#00D05E',
  '离职申请': '#FB6A67',
  '出差申请': '#957BFE',
  '换休申请': '#00B9FE'
}
let clockTimer = null

const userName = computed(() => store.state.user?.name || '同事')
const userAvatar = computed(() => store.state.user?.avatar || '/static/author.jpg')
const greetingText = computed(() => {
  const hour = new Date().getHours()
  if (hour < 6) return '夜深了'
  if (hour < 9) return '早上好'
  if (hour < 12) return '上午好'
  if (hour < 14) return '中午好'
  if (hour < 18) return '下午好'
  return '晚上好'
})
const todayDateText = computed(() => {
  const now = new Date()
  return `${now.getFullYear()}年${now.getMonth() + 1}月${now.getDate()}日`
})
const weekdayText = computed(() => {
  const weekDays = ['星期日', '星期一', '星期二', '星期三', '星期四', '星期五', '星期六']
  return weekDays[new Date().getDay()]
})
const entryDaysLabel = computed(() => {
  const info = store.state.user?.employeeInfo || {}
  const dateStr = info.entryDate || info.entry_date
  if (!dateStr) return '欢迎使用'
  const entry = new Date(String(dateStr).replace(/-/g, '/'))
  const diff = Math.floor((Date.now() - entry.getTime()) / (1000 * 60 * 60 * 24))
  return diff >= 0 ? `已入职 ${diff} 天` : '欢迎使用'
})

onMounted(() => {
  updateClock()
  clockTimer = setInterval(updateClock, 1000)
  // 首次挂载加载数据,后续由父页面切换/下拉时刷新
  loadWorkbench()
})

onUnmounted(() => {
  if (clockTimer) {
    clearInterval(clockTimer)
    clockTimer = null
  }
})

// 供 pages/index.vue 调用:刷新
defineExpose({
  refresh: async () => {
    loadWorkbench()
  }
})

function loadWorkbench() {
  loadMenus()
  loadWeekAttendance()
  loadPendingCount()
}

async function loadPendingCount() {
  try {
    const res = await getHomeStats()
    if (res.code === 200 && res.data) {
      pendingCount.value = Number(res.data.approvalCount || 0)
    }
    // 同步工作台待办数到 tabbar 角标
    store.commit('SET_UNREAD_BADGE', { workTodo: pendingCount.value })
  } catch (error) {
  }
}

async function loadMenus() {
  try {
    const res = await getMobileMenus()
    if (res.code === 200) {
      const menus = normalizeMenus(res.data)
      icons.value = menus.length ? mergeTopMenus(menus) : getFallbackMenus()
    }
  } catch (error) {
    icons.value = getFallbackMenus()
  }
}

async function loadWeekAttendance() {
  try {
    const res = await getClockInfo()
    if (res.code === 200 && res.data) {
      weekAttendance.value = res.data.weekStatus || {}
      attendance.value = buildAttendanceItems()
    }
  } catch (error) {
    weekAttendance.value = {}
    attendance.value = buildAttendanceItems()
  }
}

function updateClock() {
  const now = new Date()
  currentTime.value = `${String(now.getHours()).padStart(2, '0')}:${String(now.getMinutes()).padStart(2, '0')}`
  currentSeconds.value = String(now.getSeconds()).padStart(2, '0')
}

function normalizeMenus(payload) {
  const rawMenus = []
  if (Array.isArray(payload)) {
    rawMenus.push(...payload)
  } else if (payload && typeof payload === 'object') {
    Object.keys(payload).forEach((key) => {
      if (Array.isArray(payload[key])) rawMenus.push(...payload[key])
    })
  }

  const seen = new Set()
  return rawMenus.filter(Boolean).map((item, index) => {
    const title = item.menuName || item.title || item.name || '功能'
    const supportedTitle = matchTopMenuTitle(title)
    if (!supportedTitle || seen.has(supportedTitle)) return null
    seen.add(supportedTitle)
    return {
      title: supportedTitle,
      icon: iconForName(supportedTitle),
      color: topMenuColorMap[supportedTitle] || colorList[index % colorList.length],
      url: normalizeMenuUrl(item.path || item.url, supportedTitle)
    }
  }).filter(Boolean).sort((a, b) => topMenuOrder.indexOf(a.title) - topMenuOrder.indexOf(b.title))
}

function getFallbackMenus() {
  return [
    { title: '请假申请', icon: 'calendar-fill', color: topMenuColorMap['请假申请'], url: '/workPages/leave' },
    { title: '加班申请', icon: 'time-fill', color: topMenuColorMap['加班申请'], url: '/workPages/overtime' },
    { title: '补卡申请', icon: 'edit-form', color: topMenuColorMap['补卡申请'], url: '/workPages/replace' },
    { title: '离职申请', icon: 'reduce-circle-fill', color: topMenuColorMap['离职申请'], url: '/workPages/resign' },
    { title: '出差申请', icon: 'suitcase-fill', color: topMenuColorMap['出差申请'], url: '/workPages/travel' },
    { title: '换休申请', icon: 'menu-grille-fill', color: topMenuColorMap['换休申请'], url: '/workPages/exchange' }
  ]
}

function mergeTopMenus(menus) {
  const menuMap = new Map(getFallbackMenus().map((item) => [item.title, item]))
  menus.forEach((item) => {
    const fallback = menuMap.get(item.title) || {}
    menuMap.set(item.title, { ...fallback, ...item })
  })
  return topMenuOrder.map((title) => menuMap.get(title)).filter(Boolean)
}

function matchTopMenuTitle(name) {
  if (name.includes('请假')) return '请假申请'
  if (name.includes('加班')) return '加班申请'
  if (name.includes('补卡') || name.includes('缺卡')) return '补卡申请'
  if (name.includes('离职')) return '离职申请'
  if (name.includes('出差')) return '出差申请'
  if (name.includes('换休') || name.includes('调休')) return '换休申请'
  return ''
}

function iconForName(name) {
  if (name.includes('假')) return 'calendar-fill'
  if (name.includes('加班')) return 'time-fill'
  if (name.includes('补卡')) return 'edit-form'
  if (name.includes('出差')) return 'suitcase-fill'
  if (name.includes('转正')) return 'my-job-fill'
  if (name.includes('离职')) return 'reduce-circle-fill'
  if (name.includes('换休') || name.includes('调休')) return 'menu-grille-fill'
  if (name.includes('调动')) return 'transfer-fill'
  if (name.includes('审批')) return 'seal'
  if (name.includes('报销')) return 'money-fill'
  return 'menu-fill'
}

function routeForName(name) {
  if (name.includes('请假')) return '/workPages/leave'
  if (name.includes('加班')) return '/workPages/overtime'
  if (name.includes('补卡')) return '/workPages/replace'
  if (name.includes('离职')) return '/workPages/resign'
  if (name.includes('出差')) return '/workPages/travel'
  if (name.includes('换休') || name.includes('调休')) return '/workPages/exchange'
  return ''
}

function normalizeMenuUrl(url, title) {
  const route = routeForName(title)
  if (!url) return route
  if (url.includes('/pages/apply/') || url.includes('pages/apply/')) return route
  return url.startsWith('/') ? url : `/${url}`
}

function buildAttendanceItems() {
  const summary = getWeekSummary()
  return [
    { title: `正常${summary.normal}天`, icon: 'reload-planet-fill', color: '#4B98FE', url: '/workPages/calendar' },
    { title: `迟到${summary.late}次`, icon: 'rocket-fill', color: '#FB6A67', url: '/workPages/calendar' },
    { title: `缺勤${summary.absent}次`, icon: 'warning-fill', color: '#FFAC00', url: '/workPages/leave-record' },
    { title: `待确认${summary.unknown}天`, icon: 'notebook-fill', color: '#00B9FE', url: '/workPages/calendar' }
  ]
}

function getWeekSummary() {
  return getWeekDays().reduce(
    (summary, day) => {
      const status = weekAttendance.value[day]
      if (status === 'normal') summary.normal += 1
      else if (status === 'late') summary.late += 1
      else if (status === 'absent') summary.absent += 1
      else summary.unknown += 1
      return summary
    },
    { normal: 0, late: 0, absent: 0, unknown: 0 }
  )
}

function getWeekDays() {
  const now = new Date()
  const dayOfWeek = now.getDay() || 7
  const days = []
  for (let i = 1; i <= 7; i++) {
    const date = new Date(now)
    date.setDate(now.getDate() - dayOfWeek + i)
    days.push(formatDateKey(date))
  }
  return days
}

function formatDateKey(date) {
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

// hex 转浅色底(rgba)
function softColor(hex) {
  const v = String(hex || '').replace('#', '')
  if (v.length < 6) return 'rgba(75, 152, 254, 0.12)'
  const r = parseInt(v.slice(0, 2), 16)
  const g = parseInt(v.slice(2, 4), 16)
  const b = parseInt(v.slice(4, 6), 16)
  return `rgba(${r}, ${g}, ${b}, 0.12)`
}

function goToClock() {
  uni.navigateTo({
    url: '/workPages/time',
    fail() {
      uni.showToast({ icon: 'none', title: '打卡页打开失败，请稍后再试' })
    }
  })
}

// 跳转
const tn = (e) => {
  if (!e) {
    uni.showToast({ icon: 'none', title: '该功能即将上线，敬请期待' })
    return
  }
  uni.navigateTo({
    url: e,
    fail() {
      uni.showToast({ icon: 'none', title: '页面打开失败，请稍后再试' })
    }
  })
}
</script>

<style lang="scss" scoped>
  .template-work {
    background-color: #F7F8FA;
    max-width: 640px;
    margin: 0 auto;
  }

  .work-card {
    background: #ffffff;
    border-radius: 16rpx;
    margin: 20rpx 24rpx 0;
    padding: 28rpx;
    border: 1rpx solid rgba(17, 31, 46, 0.06);
  }

  /* 问候卡片 */
  .work-greet-card {
    margin-top: 20rpx;
    display: flex;
    align-items: center;
    justify-content: space-between;
  }

  .work-greet-left {
    display: flex;
    align-items: center;
    min-width: 0;
  }

  .work-avatar-wrap {
    position: relative;
    flex-shrink: 0;

    image {
      width: 88rpx;
      height: 88rpx;
      border-radius: 16rpx;
      background-color: #eef0f4;
    }
  }

  .work-online {
    position: absolute;
    right: -2rpx;
    bottom: -2rpx;
    width: 18rpx;
    height: 18rpx;
    border-radius: 50%;
    background: #00B578;
    border: 3rpx solid #ffffff;
  }

  .work-greet-info {
    margin-left: 20rpx;
    min-width: 0;
  }

  .work-greet-title {
    font-size: 30rpx;
    font-weight: 600;
    color: #1d2541;
  }

  .work-greet-sub {
    margin-top: 6rpx;
    font-size: 22rpx;
    color: #8a94a6;

    .work-greet-entry {
      margin-left: 16rpx;
      color: #3668FC;
    }
  }

  .work-bell {
    position: relative;
    flex-shrink: 0;
    width: 64rpx;
    height: 64rpx;
    border-radius: 16rpx;
    background: #f4f5f9;
    color: #425066;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 32rpx;
  }

  .work-bell-dot {
    position: absolute;
    top: 10rpx;
    right: 12rpx;
    width: 14rpx;
    height: 14rpx;
    border-radius: 50%;
    background: #FF4D4F;
  }

  /* 打卡卡片 */
  .work-clock-card {
    display: flex;
    align-items: center;
    justify-content: space-between;
  }

  .work-clock-time {
    color: #1d2541;
    font-size: 60rpx;
    font-weight: 700;
    line-height: 1.1;
    font-variant-numeric: tabular-nums;

    .work-clock-seconds {
      font-size: 26rpx;
      font-weight: 500;
      color: #8a94a6;
      margin-left: 8rpx;
    }
  }

  .work-clock-weekday {
    margin-top: 4rpx;
    color: #8a94a6;
    font-size: 24rpx;
  }

  .work-clock-go {
    display: flex;
    align-items: center;
    padding: 20rpx 36rpx;
    border-radius: 16rpx;
    background: #3668FC;
    color: #ffffff;
  }

  .work-clock-go-text {
    margin-left: 10rpx;
    font-size: 28rpx;
    font-weight: 500;
  }

  /* 申请菜单宫格 */
  .work-menu-grid {
    display: grid;
    grid-template-columns: repeat(4, minmax(0, 1fr));
    row-gap: 32rpx;
  }

  .work-menu-item {
    display: flex;
    flex-direction: column;
    align-items: center;
  }

  .work-menu-icon {
    width: 84rpx;
    height: 84rpx;
    border-radius: 20rpx;
    display: flex;
    align-items: center;
    justify-content: center;
  }

  .work-menu-label {
    margin-top: 12rpx;
    font-size: 24rpx;
    color: #425066;
  }

  /* 考勤统计 */
  .work-card-head {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 20rpx;
  }

  .work-card-title {
    font-size: 28rpx;
    font-weight: 600;
    color: #1d2541;
  }

  .work-card-more {
    color: #8a94a6;
    font-size: 24rpx;
  }

  .work-stats-grid {
    display: grid;
    grid-template-columns: repeat(4, minmax(0, 1fr));
  }

  .work-stats-item {
    text-align: center;
    padding: 8rpx 0;
  }

  .work-stats-num {
    font-size: 26rpx;
    font-weight: 600;
  }

  .tn-tabbar-height {
    min-height: 100rpx;
    height: calc(120rpx + env(safe-area-inset-bottom) / 2);
  }
</style>
