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
    </tn-navbar>

    <view class="page-content" :style="{ paddingTop: vuex_custom_bar_height + 28 + 'px' }">
      <view v-if="loading && !payslips.length" class="state-tip">加载中...</view>
      <view v-else-if="!payslips.length" class="state-tip">暂无工资条，发放后可在这里查看</view>

      <view
        v-for="item in payslips"
        :key="item.id"
        class="payslip-card"
        @click="goDetail(item)"
      >
        <view class="card-head">
          <text class="card-month">{{ item.yearMonth || '' }}</text>
          <view v-if="!item.readFlag" class="unread-dot"></view>
          <view v-if="item.confirmFlag" class="confirmed-tag">已确认</view>
        </view>
        <view class="card-net">
          <text class="net-label">实发</text>
          <text class="net-value">¥{{ formatAmount(item.netPay) }}</text>
        </view>
        <view class="card-sub">
          <text>应发 {{ formatAmount(item.grossPay) }}</text>
          <text class="deduction">扣款 {{ formatAmount(item.totalDeduction) }}</text>
        </view>
      </view>

      <view v-if="payslips.length && finished" class="state-tip">已加载全部</view>
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
const payslips = ref([])
const finished = ref(false)
const pageNum = ref(1)
const pageSize = 12

function formatAmount(value) {
  return Number(value || 0).toFixed(2)
}

async function loadData(reset = false) {
  const page = reset ? 1 : pageNum.value + 1
  loading.value = true
  try {
    const res = await getMyPayslips({ pageNum: page, pageSize })
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
  } finally {
    loading.value = false
  }
}

function goDetail(item) {
  uni.navigateTo({ url: `/minePages/payslip-detail?id=${item.id}` })
}

onShow(() => {
  // 详情页确认后返回时刷新确认状态
  loadData(true)
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

<style lang="scss">
.payslip-page {
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
  padding: 0 24rpx 40rpx;
  box-sizing: border-box;
}

.state-tip {
  padding: 120rpx 0;
  color: #9aa6b6;
  font-size: 26rpx;
  text-align: center;
}

.payslip-card {
  margin-top: 24rpx;
  padding: 28rpx;
  border-radius: 24rpx;
  background: #ffffff;
  box-shadow: 0 18rpx 50rpx rgba(69, 87, 116, 0.08);
}

.card-head {
  display: flex;
  align-items: center;
  gap: 12rpx;
}

.card-month {
  color: #1b2740;
  font-size: 30rpx;
  font-weight: 800;
}

.unread-dot {
  width: 14rpx;
  height: 14rpx;
  border-radius: 50%;
  background: #e85b65;
}

.confirmed-tag {
  margin-left: auto;
  padding: 4rpx 16rpx;
  border-radius: 999rpx;
  background: #e6f7f0;
  color: #22a873;
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
  color: #16233a;
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
