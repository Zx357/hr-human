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
    </tn-navbar>

    <view class="page-content" :style="{ paddingTop: vuex_custom_bar_height + 28 + 'px' }">
      <view v-if="loading && !detail" class="state-tip">加载中...</view>

      <template v-else-if="detail">
        <view class="hero-card">
          <view class="hero-month">{{ yearMonth }} · {{ detail.employeeName }}</view>
          <view class="hero-net">¥{{ formatAmount(detail.netPay) }}</view>
          <view class="hero-sub">
            <text>应发 {{ formatAmount(detail.grossPay) }}</text>
            <text>扣款 {{ formatAmount(detail.totalDeduction) }}</text>
          </view>
        </view>

        <view class="section-card">
          <view class="section-title income">收入明细</view>
          <view v-for="item in incomeItems" :key="item.id" class="item-row">
            <view class="item-info">
              <text class="item-name">{{ item.itemName }}</text>
              <text class="item-source">{{ item.source }}</text>
            </view>
            <text class="item-amount income-amount">+{{ formatAmount(item.amount) }}</text>
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
            <text class="item-amount deduction-amount">-{{ formatAmount(item.amount) }}</text>
          </view>
        </view>

        <view class="footer-bar">
          <tn-button
            width="100%"
            height="88"
            shape="round"
            :bg-color="detail.confirmFlag ? '#EEF3F8' : '#22A873'"
            :text-color="detail.confirmFlag ? '#66758D' : '#FFFFFF'"
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
const confirming = ref(false)
const detail = ref(null)
const yearMonth = ref('')
const incomeItems = ref([])
const deductionItems = ref([])

function formatAmount(value) {
  return Number(value || 0).toFixed(2)
}

async function loadData(id) {
  if (!id) {
    loading.value = false
    return
  }
  loading.value = true
  try {
    const res = await getMyPayslipDetail(id)
    detail.value = res.data?.payslip || null
    yearMonth.value = res.data?.yearMonth || ''
    const items = res.data?.items || []
    incomeItems.value = items.filter(item => item.direction === 1)
    deductionItems.value = items.filter(item => item.direction === 2)
  } catch (error) {
    toastRequestError(error, '加载工资条失败')
  } finally {
    loading.value = false
  }
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

<style lang="scss">
.detail-page {
  max-width: 640px;
  min-height: 100vh;
  margin: 0 auto;
  background: #f3f6fa;
  color: #16233a;
}

.nav-back {
  width: 72rpx;
  height: 52rpx;
  margin-left: 18rpx;
  border-radius: 999rpx;
  background: #eef3f8;
  color: #5d6f89;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 34rpx;
}

.nav-title {
  width: 100%;
  text-align: center;
  color: #111827;
  font-size: 34rpx;
  font-weight: 800;
}

.page-content {
  padding: 0 24rpx 200rpx;
  box-sizing: border-box;
}

.state-tip {
  padding: 120rpx 0;
  color: #9aa6b6;
  font-size: 26rpx;
  text-align: center;
}

.hero-card {
  margin-top: 24rpx;
  padding: 36rpx 32rpx;
  border-radius: 24rpx;
  color: #ffffff;
  background: linear-gradient(135deg, #4a9ab7 0%, #3c89a8 50%, #2f7390 100%);
  box-shadow: 0 24rpx 54rpx rgba(78, 103, 142, 0.16);
}

.hero-month {
  font-size: 26rpx;
  opacity: 0.86;
}

.hero-net {
  margin-top: 12rpx;
  font-size: 56rpx;
  font-weight: 900;
}

.hero-sub {
  display: flex;
  gap: 32rpx;
  margin-top: 10rpx;
  font-size: 24rpx;
  opacity: 0.86;
}

.section-card {
  margin-top: 24rpx;
  padding: 28rpx 28rpx 10rpx;
  border-radius: 24rpx;
  background: #ffffff;
  box-shadow: 0 18rpx 50rpx rgba(69, 87, 116, 0.08);
}

.section-title {
  margin-bottom: 10rpx;
  padding-left: 16rpx;
  border-left: 6rpx solid #22a873;
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
  border-bottom: 1rpx solid #eef2f7;
}

.item-info {
  display: flex;
  flex-direction: column;
  gap: 4rpx;
}

.item-name {
  color: #1b2740;
  font-size: 28rpx;
}

.item-source {
  color: #9aa6b6;
  font-size: 22rpx;
}

.item-amount {
  font-size: 30rpx;
  font-weight: 800;
}

.income-amount {
  color: #16233a;
}

.deduction-amount {
  color: #c9656a;
}

.empty-tip {
  padding: 24rpx 0;
  color: #9aa6b6;
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
