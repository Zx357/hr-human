<template>
  <view class="oa-page">
    <tn-navbar fixed home-icon="" :bottom-shadow="false" bg-color="#FFFFFF" :placeholder="false">
      <template #back>
        <view class="nav-back" @click="goBack">
          <tn-icon name="left-arrow"></tn-icon>
        </view>
      </template>
      <view class="tn-flex tn-flex-col-center tn-flex-row-center">
        <text class="tn-text-bold tn-text-xl tn-color-black">审批进程</text>
      </view>
    </tn-navbar>

    <view class="tabs-wrap" :style="{ paddingTop: vuex_custom_bar_height + 10 + 'px' }">
      <tn-tabs v-model="currentTab" active-color="#3D7EFF" color="#9AA4B2" bold bg-color="#FFFFFF" font-size="30" @change="onTabChange">
        <tn-tabs-item v-for="item in tabs" :key="item.value" :title="item.name" />
      </tn-tabs>
    </view>

    <scroll-view
      scroll-y
      class="list-scroll"
      :style="{ paddingTop: vuex_custom_bar_height + 70 + 'px' }"
      :refresher-enabled="true"
      :refresher-triggered="refreshing"
      @refresherrefresh="refresh"
      @scrolltolower="loadMore"
    >
      <view class="list-wrap">
        <view v-for="item in list" :key="item.id" class="process-card" @click="goDetail(item)">
          <view class="card-main">
            <view class="card-head">
              <view class="title-wrap">
                <view class="type-chip" :style="{ backgroundColor: statusMeta(item.status).bg, color: statusMeta(item.status).color }">
                  <tn-icon :name="typeIcon(item.appType)"></tn-icon>
                </view>
                <text class="type-name">{{ typeName(item.appType) }}</text>
                <view class="status-pill" :style="{ color: statusMeta(item.status).color, backgroundColor: statusMeta(item.status).bg }">
                  {{ statusMeta(item.status).name }}
                </view>
              </view>
              <text class="time">{{ formatTime(item.createdTime) }}</text>
            </view>

            <view class="summary clamp-1">
              {{ summaryText(item) }}
            </view>

            <view class="step-box">
              <view v-for="step in steps(item.status)" :key="step.name" class="step-item" :class="{ active: step.active }">
                <view class="step-dot" :style="stepDotStyle(step, item.status)">
                  <tn-icon :name="step.icon"></tn-icon>
                </view>
                <text class="step-name">{{ step.name }}</text>
              </view>
            </view>

            <view v-if="canCancel(item)" class="card-actions">
              <tn-button
                size="sm"
                shape="round"
                bg-color="rgba(251, 106, 103, 0.1)"
                text-color="#FB6A67"
                :custom-style="{ padding: '10rpx 36rpx' }"
                @click.stop="cancelItem(item)"
              >
                撤销申请
              </tn-button>
            </view>
          </view>
        </view>

        <view v-if="!loading && !list.length" class="empty-state">
          <view class="empty-icon">
            <tn-icon name="empty-list"></tn-icon>
          </view>
          <text class="empty-title">暂无审批记录</text>
          <text class="empty-desc">你提交的申请会出现在这里</text>
        </view>

        <view v-if="list.length" class="load-more">
          <text>{{ loading ? '加载中...' : finished ? '没有更多了' : '上拉加载更多' }}</text>
        </view>
      </view>
    </scroll-view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'
import { onLoad, onShow } from '@dcloudio/uni-app'
import { useStore } from 'vuex'
import { useCustomBarHeight, useGoBack } from '@/libs/composables'
import { getMyApplications, cancelApplication } from '@/api/application'

const store = useStore()
const { vuex_custom_bar_height } = useCustomBarHeight()
const { goBack } = useGoBack()

const employeeInfo = computed(() => store.state.user.employeeInfo || uni.getStorageSync('userInfo') || {})

const tabs = [
  { name: '全部', value: undefined },
  { name: '待审批', value: 0 },
  { name: '已通过', value: 1 },
  { name: '已拒绝', value: 2 },
  { name: '已撤销', value: 3 }
]

const typeMap = {
  leave: '请假申请',
  overtime: '加班申请',
  makeup: '补卡申请',
  business: '出差申请',
  exchange: '换休申请',
  resignation: '离职申请',
  regularization: '转正申请',
  transfer: '调动申请',
  reward: '奖励申请',
  punish: '惩罚申请',
  expense: '费用报销',
  device: '设备申请'
}

const statusMap = {
  0: { name: '待审批', color: '#FFAC00', bg: 'rgba(255, 172, 0, 0.14)' },
  1: { name: '已通过', color: '#00C8B0', bg: 'rgba(0, 200, 176, 0.12)' },
  2: { name: '已拒绝', color: '#FB6A67', bg: 'rgba(251, 106, 103, 0.12)' },
  3: { name: '已撤销', color: '#9AA4B2', bg: 'rgba(154, 164, 178, 0.14)' }
}

const currentTab = ref(0)
const list = ref([])
const pageNum = ref(1)
const pageSize = 10
const loading = ref(false)
const finished = ref(false)
const refreshing = ref(false)

onLoad(() => {
  refresh()
})

onShow(() => {
  if (list.value.length) refresh()
})

function normalizeRecords(data) {
  if (Array.isArray(data)) return data
  return data?.records || data?.rows || data?.list || []
}

function normalizeTotal(data, records) {
  if (Array.isArray(data)) return data.length
  return Number(data?.total ?? records.length)
}

async function loadList(reset = false) {
  if (loading.value || finished.value) return
  if (!employeeInfo.value.id) {
    list.value = []
    finished.value = true
    refreshing.value = false
    return
  }
  loading.value = true
  try {
    const params = {
      pageNum: pageNum.value,
      pageSize,
      employeeId: employeeInfo.value.id
    }
    const status = tabs[currentTab.value]?.value
    if (status !== undefined) params.status = status

    const res = await getMyApplications(params)
    const records = normalizeRecords(res.data)
    list.value = reset ? records : list.value.concat(records)
    const total = normalizeTotal(res.data, records)
    finished.value = list.value.length >= total || records.length < pageSize
    pageNum.value += 1
  } catch (e) {
    console.log('加载审批进程失败', e)
  } finally {
    loading.value = false
    refreshing.value = false
  }
}

function refresh() {
  refreshing.value = true
  pageNum.value = 1
  finished.value = false
  list.value = []
  loadList(true)
}

function loadMore() {
  loadList()
}

function onTabChange(index) {
  currentTab.value = index
  refresh()
}

function typeName(appType) {
  return typeMap[appType] || appType || '申请'
}

const typeIconMap = {
  leave: 'calendar-fill',
  overtime: 'time-fill',
  makeup: 'edit-form',
  business: 'suitcase-fill',
  exchange: 'menu-grille-fill',
  resignation: 'reduce-circle-fill',
  regularization: 'my-job-fill',
  transfer: 'transfer-fill',
  reward: 'medal-fill',
  punish: 'warning-fill',
  expense: 'money-fill',
  device: 'mouse-fill'
}

function typeIcon(appType) {
  return typeIconMap[appType] || 'menu-fill'
}

// 步骤圆点:激活态用状态色浅底 + 彩色图标,未激活灰底灰图标
function stepDotStyle(step, status) {
  const color = statusMeta(status).color
  if (step.active) {
    return { backgroundColor: statusMeta(status).bg, color }
  }
  return { backgroundColor: '#EFF1F5', color: '#C3CAD6' }
}

function statusMeta(status) {
  return statusMap[Number(status)] || { name: '未知', color: '#9AA4B2', bg: 'rgba(154, 164, 178, 0.14)' }
}

function formatTime(value) {
  if (!value) return ''
  return String(value).replace('T', ' ').slice(0, 16)
}

function summaryText(item) {
  if (item.reason) return item.reason
  if (item.remark) return item.remark
  if (item.startTime || item.endTime) return `${formatTime(item.startTime)} 至 ${formatTime(item.endTime)}`
  return '查看申请详情'
}

function steps(status) {
  const value = Number(status)
  if (value === 0) {
    return [
      { name: '已提交', icon: 'success-circle-fill', active: true },
      { name: '待审批', icon: 'time-fill', active: true },
      { name: '完成', icon: 'check', active: false }
    ]
  }
  return [
    { name: '已提交', icon: 'success-circle-fill', active: true },
    { name: '已审批', icon: value === 2 ? 'close' : 'safe-fill', active: true },
    { name: statusMeta(status).name, icon: value === 2 ? 'close' : 'success-circle-fill', active: true }
  ]
}

function goDetail(item) {
  uni.navigateTo({ url: `/homePages/approval-details?id=${item.id}` })
}

function canCancel(item) {
  return Number(item.status) === 0
}

function cancelItem(item) {
  uni.showModal({
    title: '撤销申请',
    content: '确定要撤销这条申请吗？撤销后不可恢复',
    confirmColor: '#FB6A67',
    success: async (res) => {
      if (!res.confirm) return
      try {
        await cancelApplication(item.id)
        uni.showToast({ title: '已撤销', icon: 'success' })
        refresh()
      } catch (e) {
        console.log('撤销申请失败', e)
      }
    }
  })
}
</script>

<style lang="scss" scoped>
.oa-page {
  max-width: 640px;
  min-height: 100vh;
  margin: 0 auto;
  background: #F8F7F8;
}

.nav-back {
  width: 72rpx;
  height: 54rpx;
  margin-left: 18rpx;
  border-radius: 999rpx;
  background: rgba(29, 37, 65, 0.12);
  color: #1d2541;
  display: flex;
  align-items: center;
  justify-content: center;
}

.tabs-wrap {
  position: fixed;
  top: 0;
  left: 50%;
  z-index: 8;
  width: 100%;
  max-width: 640px;
  transform: translateX(-50%);
  background: #fff;
  box-shadow: 0 8rpx 24rpx rgba(29, 37, 65, 0.04);
}

.list-scroll {
  box-sizing: border-box;
  height: 100vh;
}

.list-wrap {
  padding: 24rpx 24rpx 80rpx;
}

.process-card {
  position: relative;
  margin-bottom: 22rpx;
  overflow: hidden;
  background: #fff;
  border-radius: 20rpx;
  box-shadow: 0 10rpx 30rpx rgba(29, 37, 65, 0.06);
}

.card-main {
  flex: 1;
  min-width: 0;
  padding: 26rpx 24rpx;
}

.type-chip {
  width: 72rpx;
  height: 72rpx;
  border-radius: 24rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 38rpx;
  flex-shrink: 0;
}

.card-head,
.title-wrap,
.step-box {
  display: flex;
  align-items: center;
}

.card-head {
  justify-content: space-between;
}

.type-name {
  color: #1d2541;
  font-size: 31rpx;
  font-weight: 800;
}

.status-pill {
  margin-left: 14rpx;
  padding: 6rpx 16rpx;
  border-radius: 999rpx;
  font-size: 22rpx;
  font-weight: 700;
}

.time,
.load-more,
.empty-desc {
  color: #9aa4b2;
  font-size: 24rpx;
}

.summary {
  margin-top: 16rpx;
  color: #657189;
  font-size: 26rpx;
}

.step-box {
  margin-top: 24rpx;
  padding: 18rpx;
  border-radius: 16rpx;
  background: #f7f8fb;
}

.step-item {
  flex: 1;
  min-width: 0;
  text-align: center;
  color: #9aa4b2;
}

.step-item.active {
  color: #1d2541;
  font-weight: 700;
}

.step-dot {
  width: 56rpx;
  height: 56rpx;
  margin: 0 auto 8rpx;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.step-name {
  font-size: 23rpx;
}

.empty-state {
  padding-top: 180rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.empty-icon {
  width: 110rpx;
  height: 110rpx;
  border-radius: 50%;
  color: #4b98fe;
  background: rgba(75, 152, 254, 0.1);
  font-size: 58rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.empty-title {
  margin-top: 22rpx;
  color: #1d2541;
  font-size: 30rpx;
  font-weight: 700;
}

.load-more {
  padding: 22rpx 0;
  text-align: center;
}

.card-actions {
  margin-top: 20rpx;
  display: flex;
  justify-content: flex-end;
}

.clamp-1 {
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 1;
  overflow: hidden;
}
</style>
