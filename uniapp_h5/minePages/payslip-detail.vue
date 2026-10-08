<template>
  <view class="detail-page">
    <tn-navbar fixed home-icon="" :placeholder="false" :bottom-shadow="false" bg-color="#FFFFFF">
      <template #back>
        <view class="nav-back" @click="goBack">
          <tn-icon name="left-arrow"></tn-icon>
        </view>
      </template>
      <view class="nav-title">
        <text>工资条明细</text>
      </view>
      <template #right>
        <!-- 金额显示/隐藏切换(页面级,默认遮挡) -->
        <view class="eye-btn" @click="toggleAmountVisible">
          <tn-icon :name="amountVisible ? 'eye' : 'eye-hide'" />
        </view>
      </template>
    </tn-navbar>

    <view class="page-content" :style="{ paddingTop: vuex_custom_bar_height + 28 + 'px' }">
      <!-- 加载中 -->
      <view v-if="loading && !detail" class="state-tip">加载中...</view>

      <!-- 加载失败:提示 + 重试(替代原 detail 为 null 时的白屏) -->
      <view v-else-if="loadFailed && !detail" class="state-tip">
        <view class="fail-icon">
          <tn-icon name="notice-no"></tn-icon>
        </view>
        <view class="fail-text">工资条加载失败</view>
        <view class="retry-btn" @click="retryLoad">点击重试</view>
      </view>

      <!-- 参数缺失 -->
      <view v-else-if="!detail" class="state-tip">未找到工资条，请返回列表重新进入</view>

      <template v-else>
        <!-- 汇总卡片(白卡片,替代旧渐变 hero) -->
        <view class="hero-card">
          <view class="hero-month">{{ yearMonth }} · {{ detail.employeeName }}</view>
          <view class="hero-net">{{ amountVisible ? '¥' + formatAmount(detail.netPay) : '¥ •••••' }}</view>
          <view class="hero-sub">
            <text>应发 {{ amountVisible ? formatAmount(detail.grossPay) : '•••••' }}</text>
            <text>扣款 {{ amountVisible ? formatAmount(detail.totalDeduction) : '•••••' }}</text>
          </view>
          <view v-if="detail.confirmFlag" class="hero-confirmed">已确认</view>
        </view>

        <view class="section-card">
          <view class="section-title income">收入明细</view>
          <view v-if="!incomeItems.length" class="empty-tip">本月无收入项</view>
          <view v-for="item in incomeItems" :key="item.id" class="item-row">
            <view class="item-info">
              <text class="item-name">{{ item.itemName }}</text>
              <text class="item-source">{{ item.source }}</text>
            </view>
            <text class="item-amount income-amount">+{{ amountVisible ? formatAmount(item.amount) : '••••' }}</text>
          </view>
        </view>

        <view class="section-card">
          <view class="section-title deduction">扣款明细</view>
          <view v-if="!deductionItems.length" class="empty-tip">本月无扣款项</view>
          <view v-for="item in deductionItems" :key="item.id" class="item-row">
            <view class="item-info">
              <text class="item-name">{{ item.itemName }}</text>
              <text class="item-source">{{ item.source }}</text>
            </view>
            <text class="item-amount deduction-amount">-{{ amountVisible ? formatAmount(item.amount) : '••••' }}</text>
          </view>
        </view>

        <view class="footer-bar">
          <tn-button
            width="100%"
            height="88"
            shape="round"
            :bg-color="detail.confirmFlag ? '#F1F3F7' : '#3668FC'"
            :text-color="detail.confirmFlag ? '#9AA4B2' : '#FFFFFF'"
            :font-size="30"
            bold
            :disabled="detail.confirmFlag || confirming"
            :loading="confirming"
            @click="handleConfirm"
          >
            <text>{{ detail.confirmFlag ? '已确认' : confirming ? '确认中...' : '确认工资条' }}</text>
          </tn-button>
        </view>
      </template>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { useCustomBarHeight, useGoBack } from '@/libs/composables'
import { confirmMyPayslip, getMyPayslipDetail } from '@/api/salary'
import { toastRequestError } from '@/utils/common'

const { vuex_custom_bar_height } = useCustomBarHeight()
const { goBack } = useGoBack()

const loading = ref(true)
const loadFailed = ref(false)
const confirming = ref(false)
const detail = ref(null)
const yearMonth = ref('')
const incomeItems = ref([])
const deductionItems = ref([])
const payslipId = ref(null)

// 金额默认遮挡,点击右上角眼睛图标切换
const amountVisible = ref(false)

function formatAmount(value) {
  return Number(value || 0).toFixed(2)
}

function toggleAmountVisible() {
  amountVisible.value = !amountVisible.value
}

async function loadData(id) {
  payslipId.value = id || payslipId.value
  if (!payslipId.value) {
    loading.value = false
    loadFailed.value = false
    return
  }
  loading.value = true
  loadFailed.value = false
  try {
    const res = await getMyPayslipDetail(payslipId.value)
    detail.value = res.data?.payslip || null
    yearMonth.value = res.data?.yearMonth || ''
    const items = res.data?.items || []
    incomeItems.value = items.filter(item => item.direction === 1)
    deductionItems.value = items.filter(item => item.direction === 2)
    // 详情加载成功但无数据时也算失败态(避免白屏)
    if (!detail.value) loadFailed.value = true
  } catch (error) {
    loadFailed.value = true
    toastRequestError(error, '加载工资条失败')
  } finally {
    loading.value = false
  }
}

function retryLoad() {
  loadData()
}

async function handleConfirm() {
  if (!detail.value?.id || detail.value.confirmFlag) return
  confirming.value = true
  try {
    await confirmMyPayslip(detail.value.id)
    detail.value.confirmFlag = 1
    uni.showToast({ icon: 'none', title: '已确认' })
  } catch (error) {
    toastRequestError(error, '确认失败')
  } finally {
    confirming.value = false
  }
}

onLoad((options) => {
  loadData(options?.id)
})
</script>

<style lang="scss" scoped>
.detail-page {
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
  padding: 0 24rpx 200rpx;
  box-sizing: border-box;
}

.state-tip {
  padding: 120rpx 0;
  color: #9aa4b2;
  font-size: 26rpx;
  text-align: center;
}

.fail-icon {
  margin: 0 auto;
  width: 110rpx;
  height: 110rpx;
  border-radius: 50%;
  background: rgba(251, 106, 103, 0.1);
  color: #fb6a67;
  font-size: 58rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.fail-text {
  margin-top: 22rpx;
  color: #1d2541;
  font-size: 30rpx;
  font-weight: 700;
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

/* 汇总卡片 */
.hero-card {
  position: relative;
  padding: 36rpx 32rpx;
  border-radius: 16rpx;
  background: #ffffff;
  border: 1rpx solid rgba(17, 31, 46, 0.06);
}

.hero-month {
  font-size: 26rpx;
  color: #657189;
}

.hero-net {
  margin-top: 12rpx;
  font-size: 56rpx;
  font-weight: 900;
  color: #1d2541;
}

.hero-sub {
  display: flex;
  gap: 32rpx;
  margin-top: 10rpx;
  font-size: 24rpx;
  color: #8b98aa;
}

.hero-confirmed {
  position: absolute;
  top: 28rpx;
  right: 28rpx;
  padding: 4rpx 16rpx;
  border-radius: 999rpx;
  background: rgba(0, 200, 176, 0.12);
  color: #00a08a;
  font-size: 22rpx;
}

.section-card {
  margin-top: 20rpx;
  padding: 28rpx 28rpx 10rpx;
  border-radius: 16rpx;
  background: #ffffff;
  border: 1rpx solid rgba(17, 31, 46, 0.06);
}

.section-title {
  margin-bottom: 10rpx;
  padding-left: 16rpx;
  border-left: 6rpx solid #3668fc;
  font-size: 30rpx;
  font-weight: 800;
}

.section-title.deduction {
  border-color: #c9656a;
}

.item-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 22rpx 0;
  border-bottom: 1rpx solid #f3f2f7;
}

.item-info {
  display: flex;
  flex-direction: column;
  gap: 4rpx;
}

.item-name {
  color: #1d2541;
  font-size: 28rpx;
}

.item-source {
  color: #9aa4b2;
  font-size: 22rpx;
}

.item-amount {
  font-size: 30rpx;
  font-weight: 800;
}

.income-amount {
  color: #1d2541;
}

.deduction-amount {
  color: #c9656a;
}

.empty-tip {
  padding: 24rpx 0;
  color: #9aa4b2;
  font-size: 24rpx;
  text-align: center;
}

.footer-bar {
  position: fixed;
  right: 0;
  bottom: 0;
  left: 0;
  z-index: 30;
  max-width: 640px;
  margin: 0 auto;
  padding: 20rpx 28rpx;
  padding-bottom: calc(20rpx + env(safe-area-inset-bottom));
  background: rgba(255, 255, 255, 0.96);
  box-shadow: 0 -12rpx 36rpx rgba(70, 84, 110, 0.08);
  box-sizing: border-box;
}
</style>
