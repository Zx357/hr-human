<template>
  <view class="payslip-page">
    <tn-navbar fixed home-icon="" :placeholder="false" :bottom-shadow="false" bg-color="#FFFFFF">
      <template #back>
        <view class="nav-back" @click="goBack">
          <tn-icon name="left-arrow"></tn-icon>
        </view>
      </template>
      <view class="nav-title">
        <text>我的工资条</text>
      </view>
      <template #right>
        <!-- 金额显示/隐藏切换(页面级) -->
        <view class="eye-btn" @click="toggleAmountVisible">
          <tn-icon :name="amountVisible ? 'eye' : 'eye-hide'" />
        </view>
      </template>
    </tn-navbar>

    <view class="page-content" :style="{ paddingTop: vuex_custom_bar_height + 20 + 'px' }">
      <!-- 月份筛选 -->
      <view class="filter-bar">
        <picker
          mode="date"
          fields="month"
          :value="filterMonth || currentMonth"
          :end="currentMonth"
          @change="onMonthChange"
        >
          <view class="filter-chip" :class="{ 'filter-chip--active': filterMonth }">
            <tn-icon name="calendar" class="filter-icon" />
            <text>{{ filterMonth || '选择月份' }}</text>
            <tn-icon name="down" class="filter-icon" />
          </view>
        </picker>
        <view v-if="filterMonth" class="filter-clear" @click="clearMonth">
          清除筛选
        </view>
      </view>

      <!-- 状态:加载中/空/失败 -->
      <view v-if="loading && !payslips.length" class="state-tip">加载中...</view>
      <view v-else-if="loadFailed && !payslips.length" class="state-tip">
        <view>工资条加载失败</view>
        <view class="retry-btn" @click="reload">点击重试</view>
      </view>
      <view v-else-if="!payslips.length" class="state-tip">{{ filterMonth ? '该月份暂无工资条' : '暂无工资条，发放后可在这里查看' }}</view>

      <view
        v-for="item in payslips"
        :key="item.id"
        class="payslip-card"
        @click="goDetail(item)"
      >
        <view class="card-head">
          <view class="card-head-left">
            <text class="card-month">{{ item.yearMonth || '' }}</text>
            <view v-if="!item.readFlag" class="unread-dot"></view>
          </view>
          <view v-if="item.confirmFlag" class="confirmed-tag">已确认</view>
        </view>
        <view class="card-net">
          <text class="net-label">实发</text>
          <text class="net-value">{{ amountVisible ? '¥' + formatAmount(item.netPay) : '¥ •••••' }}</text>
        </view>
        <view class="card-sub">
          <text>应发 {{ amountVisible ? formatAmount(item.grossPay) : '•••••' }}</text>
          <text class="deduction">扣款 {{ amountVisible ? formatAmount(item.totalDeduction) : '•••••' }}</text>
        </view>
      </view>

      <view v-if="payslips.length && finished" class="state-tip state-tip--end">已加载全部</view>
      <view v-else-if="payslips.length && loading" class="state-tip state-tip--end">加载中...</view>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onPullDownRefresh, onReachBottom, onShow } from '@dcloudio/uni-app'
import { useCustomBarHeight, useGoBack } from '@/libs/composables'
import { getMyPayslips } from '@/api/salary'
import { toastRequestError } from '@/utils/common'

const { vuex_custom_bar_height } = useCustomBarHeight()
const { goBack } = useGoBack()

const loading = ref(false)
const loadFailed = ref(false)
const payslips = ref([])
const finished = ref(false)
const pageNum = ref(1)
const pageSize = 12

// 金额默认遮挡,右上角眼睛图标切换(页面级状态)
const amountVisible = ref(false)

// 月份筛选:空字符串为全量
const filterMonth = ref('')
const currentMonth = (() => {
  const now = new Date()
  return `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}`
})()

function formatAmount(value) {
  return Number(value || 0).toFixed(2)
}

function toggleAmountVisible() {
  amountVisible.value = !amountVisible.value
}

// 月份筛选变化:重置分页按月份拉取
function onMonthChange(event) {
  const value = event?.detail?.value
  if (!value || value === filterMonth.value) return
  filterMonth.value = value
  loadData(true)
}

// 清除筛选:恢复分页全量
function clearMonth() {
  if (!filterMonth.value) return
  filterMonth.value = ''
  loadData(true)
}

function reload() {
  loadData(true)
}

async function loadData(reset = false) {
  const page = reset ? 1 : pageNum.value + 1
  loading.value = true
  loadFailed.value = false
  try {
    const params = { pageNum: page, pageSize }
    // 选中月份时传 month=yyyy-MM,后端按月过滤
    if (filterMonth.value) params.month = filterMonth.value
    const res = await getMyPayslips(params)
    const records = res.data?.records || []
    const total = res.data?.total ?? null
    if (reset) {
      payslips.value = records
      pageNum.value = 1
    } else {
      payslips.value = payslips.value.concat(records)
      pageNum.value = page
    }
    finished.value = records.length < pageSize || (total !== null && total <= payslips.value.length)
  } catch (error) {
    toastRequestError(error, '加载工资条失败')
    // 首屏失败展示失败态;加载更多失败保留已有数据
    if (!payslips.value.length) loadFailed.value = true
  } finally {
    loading.value = false
  }
}

// 从明细页返回:按已加载条数刷新(一次请求拉齐已加载数量),保留当前页码与滚动位置,
// 避免"确认后返回 onShow 全量 reload 导致从第 2 页跳回第 1 页"
async function refreshLoaded() {
  try {
    const params = { pageNum: 1, pageSize: payslips.value.length }
    if (filterMonth.value) params.month = filterMonth.value
    const res = await getMyPayslips(params)
    const records = res.data?.records || []
    if (records.length) {
      payslips.value = records
      pageNum.value = Math.max(1, Math.ceil(records.length / pageSize))
    }
  } catch (error) {
    // 静默刷新失败不打扰用户,保留当前列表
    toastRequestError(error, '刷新工资条失败')
  }
}

function goDetail(item) {
  uni.navigateTo({ url: `/minePages/payslip-detail?id=${item.id}` })
}

onShow(() => {
  // 首次进入全量加载;再次显示(从明细页返回)仅刷新已加载部分
  if (!payslips.value.length) {
    loadData(true)
  } else {
    refreshLoaded()
  }
})

onPullDownRefresh(() => {
  loadData(true).finally(() => uni.stopPullDownRefresh())
})

onReachBottom(() => {
  if (!finished.value && !loading.value) {
    loadData()
  }
})
</script>

<style lang="scss" scoped>
.payslip-page {
  max-width: 640px;
  min-height: 100vh;
  margin: 0 auto;
  background: #F7F8FA;
  color: #1d2541;
}

.nav-back {
  width: 72rpx;
  height: 52rpx;
  margin-left: 18rpx;
  border-radius: 999rpx;
  background: rgba(29, 37, 65, 0.08);
  color: #1d2541;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 34rpx;
}

.nav-title {
  width: 100%;
  text-align: center;
  color: #1d2541;
  font-size: 34rpx;
  font-weight: 800;
}

/* 右上角金额显示切换 */
.eye-btn {
  margin-right: 24rpx;
  width: 68rpx;
  height: 52rpx;
  border-radius: 999rpx;
  background: rgba(29, 37, 65, 0.08);
  color: #1d2541;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 34rpx;
}

.page-content {
  padding: 0 24rpx 40rpx;
  box-sizing: border-box;
}

/* 月份筛选 */
.filter-bar {
  display: flex;
  align-items: center;
  gap: 20rpx;
  margin-bottom: 24rpx;
}

.filter-chip {
  display: flex;
  align-items: center;
  gap: 8rpx;
  padding: 14rpx 26rpx;
  border-radius: 999rpx;
  background: #ffffff;
  border: 1rpx solid rgba(17, 31, 46, 0.06);
  color: #657189;
  font-size: 25rpx;
}

.filter-chip--active {
  color: #3668fc;
  border-color: rgba(54, 104, 252, 0.35);
  background: rgba(54, 104, 252, 0.06);
}

.filter-icon {
  font-size: 26rpx;
}

.filter-clear {
  padding: 10rpx 8rpx;
  color: #9aa4b2;
  font-size: 25rpx;
}

.state-tip {
  padding: 120rpx 0;
  color: #9aa4b2;
  font-size: 26rpx;
  text-align: center;
}

.state-tip--end {
  padding: 28rpx 0;
}

.retry-btn {
  margin: 24rpx auto 0;
  width: fit-content;
  padding: 12rpx 48rpx;
  border-radius: 999rpx;
  color: #3668fc;
  font-size: 25rpx;
  font-weight: 600;
  background: rgba(54, 104, 252, 0.1);
}

.payslip-card {
  margin-bottom: 20rpx;
  padding: 28rpx;
  border-radius: 16rpx;
  background: #ffffff;
  border: 1rpx solid rgba(17, 31, 46, 0.06);
}

.card-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.card-head-left {
  display: flex;
  align-items: center;
  gap: 12rpx;
}

.card-month {
  color: #1d2541;
  font-size: 30rpx;
  font-weight: 800;
}

.unread-dot {
  width: 14rpx;
  height: 14rpx;
  border-radius: 50%;
  background: #fb6a67;
}

.confirmed-tag {
  padding: 4rpx 16rpx;
  border-radius: 999rpx;
  background: rgba(0, 200, 176, 0.12);
  color: #00a08a;
  font-size: 22rpx;
}

.card-net {
  display: flex;
  align-items: baseline;
  gap: 16rpx;
  margin-top: 18rpx;
}

.net-label {
  color: #8b98aa;
  font-size: 24rpx;
}

.net-value {
  color: #1d2541;
  font-size: 44rpx;
  font-weight: 900;
}

.card-sub {
  display: flex;
  gap: 32rpx;
  margin-top: 10rpx;
  color: #8b98aa;
  font-size: 24rpx;
}

.card-sub .deduction {
  color: #c9656a;
}
</style>
