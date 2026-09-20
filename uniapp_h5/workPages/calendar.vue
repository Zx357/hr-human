<template>
  <view class="attendance-calendar">
    <!-- 顶部自定义导航 -->
    <tn-navbar fixed home-icon="" :placeholder="false" :bottom-shadow="false" bg-color="#FFFFFF">
      <template #back><view class='tn-custom-nav-bar__back'
        @click="goBack">
        <tn-icon class='icon' name='left-arrow'></tn-icon>
      </view></template>
      <view class="tn-flex tn-flex-col-center tn-flex-row-center ">
        <text class="tn-text-bold tn-text-xl tn-color-black">考勤日历</text>
      </view>
    </tn-navbar>

    <view :style="{paddingTop: vuex_custom_bar_height + 'px'}">
    <!-- 月份切换 -->
    <view class="month-switch tn-bg-white tn-flex tn-flex-row-between tn-flex-col-center tn-padding">
      <view class="tn-flex tn-flex-col-center">
        <view class="month-arrow tn-flex tn-flex-row-center tn-flex-col-center" @click="changeMonth(-1)">
          <tn-icon name="left" class="tn-color-gray"></tn-icon>
        </view>
        <view v-if="!isCurrentMonth" class="today-btn tn-text-sm tn-color-blue" @click="backToToday">回到今天</view>
      </view>
      <text class="tn-text-xl tn-text-bold">{{ currentMonthLabel }}</text>
      <view class="month-arrow tn-flex tn-flex-row-center tn-flex-col-center" :style="isCurrentMonth ? 'opacity:0.35' : ''" @click="changeMonth(1)">
        <tn-icon name="right" class="tn-color-gray"></tn-icon>
      </view>
    </view>

    <!-- 统计卡片 -->
    <view class="stats-card tn-margin tn-padding tn-radius">
      <view class="stats-row tn-flex tn-flex-row-between">
        <view class="stats-item tn-text-center">
          <view class="stats-value tn-text-xl tn-text-bold">{{ stats.normalIn }}</view>
          <view class="tn-color-gray tn-text-sm tn-padding-top-xs">正常上班</view>
        </view>
        <view class="stats-item tn-text-center">
          <view class="stats-value tn-text-xl tn-text-bold">{{ stats.normalOut }}</view>
          <view class="tn-color-gray tn-text-sm tn-padding-top-xs">正常下班</view>
        </view>
        <view class="stats-item tn-text-center">
          <view class="stats-value tn-text-xl tn-text-bold" style="color: #FFAC00;">{{ stats.lateDays }}</view>
          <view class="tn-color-gray tn-text-sm tn-padding-top-xs">迟到</view>
        </view>
        <view class="stats-item tn-text-center">
          <view class="stats-value tn-text-xl tn-text-bold" style="color: #4B98FE;">{{ stats.earlyDays }}</view>
          <view class="tn-color-gray tn-text-sm tn-padding-top-xs">早退</view>
        </view>
        <view class="stats-item tn-text-center">
          <view class="stats-value tn-text-xl tn-text-bold" style="color: #FB6A67;">{{ stats.absentDays }}</view>
          <view class="tn-color-gray tn-text-sm tn-padding-top-xs">缺卡</view>
        </view>
      </view>
    </view>

    <!-- 日历 -->
    <view class="calendar-card tn-margin tn-padding tn-radius tn-bg-white">
      <view class="calendar-week tn-flex">
        <view v-for="week in weekLabels" :key="week" class="calendar-week__item tn-text-center tn-color-gray tn-text-sm">{{ week }}</view>
      </view>
      <view class="calendar-body tn-flex tn-flex-wrap">
        <view v-for="(cell, index) in calendarCells" :key="index" class="calendar-day tn-flex tn-flex-direction-column tn-flex-row-center tn-flex-col-center" @click="selectDate(cell)">
          <view v-if="cell.day" class="calendar-day__inner tn-flex tn-flex-direction-column tn-flex-row-center tn-flex-col-center" :class="cellClass(cell)">
            <text class="calendar-day__num">{{ cell.day }}</text>
            <view v-if="cell.record" class="calendar-day__dots tn-flex">
              <view class="dot" :class="{ 'dot--normal': cell.record.normalIn, 'dot--warn': cell.record.lateIn, 'dot--bad': !cell.record.normalIn && cell.record.attended }"></view>
              <view class="dot" :class="{ 'dot--normal': cell.record.normalOut, 'dot--warn': cell.record.earlyOut, 'dot--bad': !cell.record.normalOut && cell.record.attended }"></view>
            </view>
          </view>
        </view>
      </view>

      <!-- 图例 -->
      <view class="legend tn-flex tn-flex-row-center tn-padding-top-sm tn-flex-wrap">
        <view class="legend-item tn-flex tn-flex-col-center tn-margin-right"><view class="dot dot--normal tn-margin-right-xs"></view>正常</view>
        <view class="legend-item tn-flex tn-flex-col-center tn-margin-right"><view class="dot dot--warn tn-margin-right-xs"></view>迟到/早退</view>
        <view class="legend-item tn-flex tn-flex-col-center tn-margin-right"><view class="dot dot--bad tn-margin-right-xs"></view>缺卡</view>
      </view>
    </view>

    <!-- 选中日详情 -->
    <view v-if="selectedRecord" class="detail-card tn-margin tn-padding tn-radius tn-bg-white">
      <view class="tn-text-bold tn-text-lg tn-padding-bottom-sm">{{ selectedDateLabel }} 考勤详情</view>
      <view class="tn-flex tn-flex-row-between tn-padding-top-xs">
        <text class="tn-color-gray">上班打卡</text>
        <text :class="selectedRecord.clockIn ? (selectedRecord.lateIn ? 'text-warn' : 'text-normal') : 'tn-color-gray--disabled'">
          {{ selectedRecord.clockIn || '未打卡' }}{{ selectedRecord.clockIn && selectedRecord.lateIn ? ' (迟到)' : '' }}
        </text>
      </view>
      <view class="tn-flex tn-flex-row-between tn-padding-top-xs">
        <text class="tn-color-gray">下班打卡</text>
        <text :class="selectedRecord.clockOut ? (selectedRecord.earlyOut ? 'text-warn' : 'text-normal') : 'tn-color-gray--disabled'">
          {{ selectedRecord.clockOut || '未打卡' }}{{ selectedRecord.clockOut && selectedRecord.earlyOut ? ' (早退)' : '' }}
        </text>
      </view>
      <view v-if="selectedAbnormal" class="tn-flex tn-flex-row-right tn-padding-top">
        <tn-button bg-color="#3668FC" size="sm" :fontSize="24" text-color="#FFFFFF" shape="round" @click="goMakeup">
          <text>申请补卡</text>
        </tn-button>
      </view>
    </view>

    <view v-if="loading" class="tn-text-center tn-color-gray tn-padding-bottom">加载中...</view>
    <view class='tn-tabbar-height'></view>
    </view>
  </view>
</template>

<script setup>
import { computed, ref } from 'vue'
import { onPullDownRefresh, onShow } from '@dcloudio/uni-app'
import { useCustomBarHeight, useGoBack } from '@/libs/composables'
import { getMonthAttendance } from '@/api/attendance'

// 使用 composable 获取自定义导航栏高度与返回方法
const { vuex_custom_bar_height } = useCustomBarHeight()
const { goBack } = useGoBack()

defineOptions({
  name: 'AttendanceCalendar'
})

const weekLabels = ['日', '一', '二', '三', '四', '五', '六']

const now = new Date()
const currentYear = ref(now.getFullYear())
const currentMonth = ref(now.getMonth() + 1)
const loading = ref(false)
const records = ref([])
const recordMap = ref({})
const stats = ref({
  normalIn: 0,
  normalOut: 0,
  lateDays: 0,
  earlyDays: 0,
  absentDays: 0
})
const selectedDate = ref('')

const monthKey = computed(() => `${currentYear.value}-${String(currentMonth.value).padStart(2, '0')}`)
const currentMonthLabel = computed(() => `${currentYear.value}年${currentMonth.value}月`)
// 是否当前月（禁止翻看未来月份）
const isCurrentMonth = computed(() => currentYear.value === now.getFullYear() && currentMonth.value === now.getMonth() + 1)

// 选中日是否异常（迟到/早退/缺卡）
const selectedAbnormal = computed(() => {
  const record = selectedRecord.value
  if (!record) return false
  return !!(record.lateIn || record.earlyOut || (record.attended && (!record.clockIn || !record.clockOut)))
})

const goMakeup = () => {
  uni.navigateTo({ url: `/workPages/replace?date=${selectedDate.value}` })
}

const backToToday = () => {
  currentYear.value = now.getFullYear()
  currentMonth.value = now.getMonth() + 1
  selectedDate.value = ''
  loadCalendar()
}

const calendarCells = computed(() => {
  const year = currentYear.value
  const month = currentMonth.value
  const firstDay = new Date(year, month - 1, 1).getDay()
  const daysInMonth = new Date(year, month, 0).getDate()

  const cells = []
  for (let i = 0; i < firstDay; i++) {
    cells.push({ day: 0 })
  }
  for (let day = 1; day <= daysInMonth; day++) {
    const dateKey = `${monthKey.value}-${String(day).padStart(2, '0')}`
    cells.push({
      day,
      dateKey,
      record: recordMap.value[dateKey] || null,
      attended: !!(recordMap.value[dateKey] && (recordMap.value[dateKey].clockIn || recordMap.value[dateKey].clockOut))
    })
  }
  return cells
})

const selectedRecord = computed(() => (selectedDate.value ? recordMap.value[selectedDate.value] || null : null))
const selectedDateLabel = computed(() => (selectedDate.value ? selectedDate.value.slice(5).replace('-', '月') + '日' : ''))

const cellClass = (cell) => {
  if (cell.dateKey === selectedDate.value) return 'calendar-day__inner--selected'
  return ''
}

const loadCalendar = async () => {
  loading.value = true
  try {
    const res = await getMonthAttendance(monthKey.value)
    const data = res.data || {}
    const list = Array.isArray(data.records) ? data.records : []
    records.value = list

    const map = {}
    list.forEach((item) => {
      if (item.attDate) map[item.attDate] = item
    })
    recordMap.value = map
    stats.value = {
      normalIn: Number(data.stats?.normalIn || 0),
      normalOut: Number(data.stats?.normalOut || 0),
      lateDays: Number(data.stats?.lateDays || 0),
      earlyDays: Number(data.stats?.earlyDays || 0),
      absentDays: Number(data.stats?.absentDays || 0)
    }
  } catch (error) {
    uni.showToast({ title: '加载考勤日历失败，请下拉重试', icon: 'none' })
  } finally {
    loading.value = false
    uni.stopPullDownRefresh()
  }
}

const changeMonth = (offset) => {
  let month = currentMonth.value + offset
  let year = currentYear.value
  if (month > 12) {
    month = 1
    year += 1
  } else if (month < 1) {
    month = 12
    year -= 1
  }
  // 不允许翻到未来月份
  const target = new Date(year, month - 1, 1)
  const current = new Date(now.getFullYear(), now.getMonth(), 1)
  if (target > current) {
    uni.showToast({ title: '不能查看未来月份', icon: 'none' })
    return
  }
  currentYear.value = year
  currentMonth.value = month
  selectedDate.value = ''
  loadCalendar()
}

// 选中某天查看详情
const selectDate = (cell) => {
  if (!cell?.dateKey) return
  selectedDate.value = cell.dateKey
}

onShow(() => {
  loadCalendar()
})

onPullDownRefresh(() => {
  loadCalendar()
})
</script>

<style lang="scss" scoped>
.attendance-calendar {
  min-height: 100vh;
  background-color: #f8f7f8;
}

/* 胶囊*/
.tn-custom-nav-bar__back {
  width: 60%;
  height: 100%;
  position: relative;
  display: flex;
  justify-content: space-evenly;
  align-items: center;
  box-sizing: border-box;
  background-color: rgba(0, 0, 0, 0.15);
  border-radius: 1000rpx;
  border: 1rpx solid rgba(255, 255, 255, 0.5);
  color: #ffffff;
  font-size: 18px;

  .icon {
    display: block;
    flex: 1;
    margin: auto;
    text-align: center;
  }
}

.month-switch {
  border-radius: 0 0 20rpx 20rpx;
}

.today-btn {
  padding: 4rpx 18rpx;
  color: #3668fc;
}

.month-arrow {
  width: 64rpx;
  height: 64rpx;
  border-radius: 50%;
  background-color: #f4f5f9;
}

.stats-card {
  background: linear-gradient(135deg, #4b98fe 0%, #3668fc 100%);
  color: #ffffff;

  .stats-item .tn-color-gray {
    color: rgba(255, 255, 255, 0.8) !important;
  }
}

.stats-row {
  padding: 10rpx 0;
}

.calendar-day {
  width: 14.28%;
  padding: 8rpx 0;

  &__inner {
    width: 72rpx;
    height: 96rpx;
    border-radius: 16rpx;
    background-color: #f8f7f8;
  }

  &__inner--selected {
    background-color: rgba(75, 152, 254, 0.12);
  }

  &__num {
    font-size: 26rpx;
  }
}

.calendar-day__dots {
  padding-bottom: 6rpx;

  .dot {
    width: 10rpx;
    height: 10rpx;
    border-radius: 50%;
    background-color: #e6e6e6;
    margin: 0 3rpx;
  }
}

.dot--normal {
  background-color: #00d05e !important;
}

.dot--warn {
  background-color: #ffac00 !important;
}

.dot--bad {
  background-color: #fb6a67 !important;
}

.legend {
  font-size: 24rpx;
  color: #9aa4b2;

  .dot {
    width: 12rpx;
    height: 12rpx;
    border-radius: 50%;
  }
}

.legend-item {
  padding: 4rpx 0;
}

.detail-card {
  .text-normal {
    color: #00d05e;
  }

  .text-warn {
    color: #ffac00;
  }
}

.tn-tabbar-height {
  min-height: 60rpx;
  height: calc(80rpx + env(safe-area-inset-bottom) / 2);
}
</style>
