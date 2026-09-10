<template>
  <view class="oa-page">
    <tn-navbar fixed home-icon="" :bottom-shadow="false" bg-color="#FFFFFF" :placeholder="false">
      <template #back>
        <view class="nav-back" @click="goBack">
          <tn-icon name="left-arrow"></tn-icon>
        </view>
      </template>
      <view class="tn-flex tn-flex-col-center tn-flex-row-center">
        <text class="tn-text-bold tn-text-xl tn-color-black">待办事项</text>
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
        <view v-for="item in list" :key="item.id" class="apply-card" @click="goDetail(item)">
          <view class="card-top">
            <view class="type-pill" :style="{ color: typeMeta(item).color, backgroundColor: typeMeta(item).bg }">
              {{ typeMeta(item).name }}
            </view>
            <text class="create-time">{{ formatTime(item.createdTime) }}</text>
          </view>

          <view class="person-row">
            <view class="avatar" :style="{ backgroundColor: typeMeta(item).color }">
              {{ firstChar(item.employeeName) }}
            </view>
            <view class="person-main">
              <view class="tn-flex tn-flex-col-center">
                <text class="person-name">{{ item.employeeName || '未知员工' }}</text>
                <text class="person-no">{{ item.employeeNo || '' }}</text>
              </view>
              <text class="dept clamp-1">{{ item.deptName || item.companyName || '未设置部门' }}</text>
            </view>
          </view>

          <view class="info-grid">
            <view v-if="item.startTime" class="info-item">
              <text class="info-label">开始</text>
              <text class="info-value">{{ formatTime(item.startTime) }}</text>
            </view>
            <view v-if="item.endTime" class="info-item">
              <text class="info-label">结束</text>
              <text class="info-value">{{ formatTime(item.endTime) }}</text>
            </view>
            <view v-if="item.duration" class="info-item">
              <text class="info-label">时长</text>
              <text class="info-value">{{ item.duration }}小时</text>
            </view>
          </view>

          <view v-if="item.reason || item.remark" class="reason clamp-2">
            {{ item.reason || item.remark }}
          </view>

          <view class="card-actions">
            <view class="ghost-btn reject" @click.stop="openReject(item)">拒绝</view>
            <view class="solid-btn" @click.stop="approve(item)">通过</view>
          </view>
        </view>

        <view v-if="!loading && !list.length" class="empty-state">
          <view class="empty-icon">
            <tn-icon name="success-circle"></tn-icon>
          </view>
          <text class="empty-title">暂无待办</text>
          <text class="empty-desc">需要你审批的事项会出现在这里</text>
        </view>

        <view v-if="list.length" class="load-more">
          <text>{{ loading ? '加载中...' : finished ? '没有更多了' : '上拉加载更多' }}</text>
        </view>
      </view>
    </scroll-view>

    <view v-if="showReject" class="popup-mask" @click="closeReject">
      <view class="reject-panel" @click.stop>
        <view class="panel-title">拒绝原因</view>
        <textarea v-model="rejectRemark" class="remark-input" maxlength="200" placeholder="请输入拒绝原因" />
        <view class="panel-actions">
          <view class="panel-cancel" @click="closeReject">取消</view>
          <view class="panel-confirm" @click="confirmReject">确认拒绝</view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onLoad, onShow } from '@dcloudio/uni-app'
import { useCustomBarHeight, useGoBack } from '@/libs/composables'
import { getPendingApprovals, approveApplication } from '@/api/application'

const { vuex_custom_bar_height } = useCustomBarHeight()
const { goBack } = useGoBack()

const tabs = [
  { name: '全部', value: 'all' },
  { name: '请假', value: 'leave' },
  { name: '加班', value: 'overtime' },
  { name: '补卡', value: 'makeup' },
  { name: '出差', value: 'business' },
  { name: '其他', value: 'other' }
]

const typeMap = {
  leave: { name: '请假申请', color: '#4B98FE', bg: 'rgba(75, 152, 254, 0.12)' },
  overtime: { name: '加班申请', color: '#FFAC00', bg: 'rgba(255, 172, 0, 0.14)' },
  makeup: { name: '补卡申请', color: '#00C8B0', bg: 'rgba(0, 200, 176, 0.12)' },
  business: { name: '出差申请', color: '#957BFE', bg: 'rgba(149, 123, 254, 0.12)' },
  exchange: { name: '换休申请', color: '#00D05E', bg: 'rgba(0, 208, 94, 0.12)' },
  resignation: { name: '离职申请', color: '#FB6A67', bg: 'rgba(251, 106, 103, 0.12)' },
  regularization: { name: '转正申请', color: '#00B9FE', bg: 'rgba(0, 185, 254, 0.12)' },
  transfer: { name: '调动申请', color: '#FE871B', bg: 'rgba(254, 135, 27, 0.12)' },
  reward: { name: '奖惩申请', color: '#FFAC00', bg: 'rgba(255, 172, 0, 0.14)' },
  punish: { name: '奖惩申请', color: '#FB6A67', bg: 'rgba(251, 106, 103, 0.12)' },
  expense: { name: '费用报销', color: '#00D05E', bg: 'rgba(0, 208, 94, 0.12)' },
  device: { name: '设备申请', color: '#00B9FE', bg: 'rgba(0, 185, 254, 0.12)' }
}

const currentTab = ref(0)
const list = ref([])
const pageNum = ref(1)
const pageSize = 10
const loading = ref(false)
const finished = ref(false)
const refreshing = ref(false)
const showReject = ref(false)
const rejectRemark = ref('')
const rejectItem = ref(null)

onLoad(() => {
  refresh()
})

onShow(() => {
  if (list.value.length) refresh()
})

function currentValue() {
  return tabs[currentTab.value]?.value || 'all'
}

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
  loading.value = true
  try {
    const tab = currentValue()
    const params = { pageNum: pageNum.value, pageSize }
    if (!['all', 'other'].includes(tab)) params.appType = tab

    const res = await getPendingApprovals(params)
    const records = normalizeRecords(res.data)
    let next = records
    if (tab === 'other') {
      const mainTypes = ['leave', 'overtime', 'makeup', 'business']
      next = records.filter((item) => !mainTypes.includes(item.appType))
    }

    list.value = reset ? next : list.value.concat(next)
    const total = normalizeTotal(res.data, records)
    finished.value = list.value.length >= total || records.length < pageSize
    pageNum.value += 1
  } catch (e) {
    console.log('加载待办失败', e)
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

function typeMeta(item) {
  return typeMap[item.appType] || { name: item.appType || '申请', color: '#1D2541', bg: 'rgba(29, 37, 65, 0.08)' }
}

function firstChar(name) {
  return (name || '?').slice(0, 1)
}

function formatTime(value) {
  if (!value) return ''
  return String(value).replace('T', ' ').slice(0, 16)
}

function goDetail(item) {
  uni.navigateTo({ url: `/homePages/approval-details?id=${item.id}&mode=approve` })
}

function approve(item) {
  uni.showModal({
    title: '确认审批',
    content: `确定通过${item.employeeName || ''}的${typeMeta(item).name}吗？`,
    success: async (res) => {
      if (!res.confirm) return
      try {
        await approveApplication(item.id, 1, '')
        uni.showToast({ title: '已通过', icon: 'success' })
        refresh()
      } catch (e) {
        uni.showToast({ title: '操作失败', icon: 'none' })
      }
    }
  })
}

function openReject(item) {
  rejectItem.value = item
  rejectRemark.value = ''
  showReject.value = true
}

function closeReject() {
  showReject.value = false
  rejectItem.value = null
}

async function confirmReject() {
  if (!rejectRemark.value.trim()) {
    uni.showToast({ title: '请填写拒绝原因', icon: 'none' })
    return
  }
  try {
    await approveApplication(rejectItem.value.id, 2, rejectRemark.value.trim())
    uni.showToast({ title: '已拒绝', icon: 'success' })
    closeReject()
    refresh()
  } catch (e) {
    uni.showToast({ title: '操作失败', icon: 'none' })
  }
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

.apply-card {
  padding: 26rpx;
  margin-bottom: 22rpx;
  background: #fff;
  border-radius: 18rpx;
  box-shadow: 0 10rpx 30rpx rgba(29, 37, 65, 0.06);
}

.card-top,
.person-row,
.card-actions,
.panel-actions {
  display: flex;
  align-items: center;
}

.card-top,
.card-actions,
.panel-actions {
  justify-content: space-between;
}

.type-pill {
  padding: 8rpx 18rpx;
  border-radius: 999rpx;
  font-size: 24rpx;
  font-weight: 700;
}

.create-time,
.dept,
.info-label,
.load-more,
.empty-desc {
  color: #9aa4b2;
  font-size: 24rpx;
}

.person-row {
  margin-top: 24rpx;
}

.avatar {
  width: 76rpx;
  height: 76rpx;
  border-radius: 50%;
  color: #fff;
  font-size: 32rpx;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
}

.person-main {
  flex: 1;
  min-width: 0;
  margin-left: 18rpx;
}

.person-name {
  color: #1d2541;
  font-size: 32rpx;
  font-weight: 800;
}

.person-no {
  margin-left: 12rpx;
  color: #9aa4b2;
  font-size: 24rpx;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14rpx;
  margin-top: 22rpx;
}

.info-item {
  min-width: 0;
  padding: 18rpx;
  border-radius: 14rpx;
  background: #f7f8fb;
}

.info-value {
  display: block;
  margin-top: 6rpx;
  color: #1d2541;
  font-size: 26rpx;
  font-weight: 700;
}

.reason {
  margin-top: 20rpx;
  padding: 18rpx;
  color: #657189;
  font-size: 26rpx;
  line-height: 1.6;
  background: rgba(75, 152, 254, 0.06);
  border-radius: 14rpx;
}

.card-actions {
  margin-top: 22rpx;
}

.ghost-btn,
.solid-btn {
  width: 45%;
  height: 70rpx;
  border-radius: 999rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28rpx;
  font-weight: 700;
}

.ghost-btn {
  color: #fb6a67;
  background: rgba(251, 106, 103, 0.1);
}

.solid-btn {
  color: #fff;
  background: linear-gradient(135deg, #4b98fe, #3d7eff);
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
  color: #00c8b0;
  background: rgba(0, 200, 176, 0.1);
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

.popup-mask {
  position: fixed;
  inset: 0;
  z-index: 30;
  background: rgba(0, 0, 0, 0.35);
  display: flex;
  align-items: center;
  justify-content: center;
}

.reject-panel {
  width: 610rpx;
  padding: 34rpx;
  border-radius: 24rpx;
  background: #fff;
}

.panel-title {
  color: #1d2541;
  font-size: 34rpx;
  font-weight: 800;
}

.remark-input {
  box-sizing: border-box;
  width: 100%;
  height: 190rpx;
  margin-top: 24rpx;
  padding: 22rpx;
  color: #1d2541;
  font-size: 28rpx;
  background: #f7f8fb;
  border-radius: 16rpx;
}

.panel-actions {
  margin-top: 28rpx;
}

.panel-cancel,
.panel-confirm {
  width: 47%;
  height: 72rpx;
  border-radius: 999rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28rpx;
  font-weight: 700;
}

.panel-cancel {
  color: #657189;
  background: #f1f3f7;
}

.panel-confirm {
  color: #fff;
  background: #fb6a67;
}

.clamp-1,
.clamp-2 {
  display: -webkit-box;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.clamp-1 {
  -webkit-line-clamp: 1;
}

.clamp-2 {
  -webkit-line-clamp: 2;
}
</style>
