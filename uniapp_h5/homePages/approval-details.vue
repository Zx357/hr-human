<template>
  <view class="oa-page">
    <tn-navbar fixed home-icon="" :bottom-shadow="false" bg-color="#FFFFFF" :placeholder="false">
      <template #back>
        <view class="nav-back" @click="goBack">
          <tn-icon name="left-arrow"></tn-icon>
        </view>
      </template>
      <view class="tn-flex tn-flex-col-center tn-flex-row-center">
        <text class="tn-text-bold tn-text-xl tn-color-black">审批详情</text>
      </view>
    </tn-navbar>

    <scroll-view scroll-y class="page-scroll" :style="{ paddingTop: vuex_custom_bar_height + 18 + 'px' }">
      <view v-if="detail" class="content-wrap">
        <view class="hero-card">
          <view class="hero-top">
            <view class="type-badge">{{ typeName(detail.appType) }}</view>
            <view class="status-badge" :style="{ color: statusMeta(detail.status).color, backgroundColor: statusMeta(detail.status).bg }">
              {{ statusMeta(detail.status).name }}
            </view>
          </view>
          <view class="applicant-row">
            <view class="avatar" :style="{ backgroundColor: typeColor(detail.appType) }">
              {{ firstChar(detail.employeeName) }}
            </view>
            <view class="applicant-main">
              <text class="name">{{ detail.employeeName || '未知员工' }}</text>
              <text class="sub clamp-1">{{ detail.deptName || detail.companyName || '未设置部门' }} {{ detail.employeeNo || '' }}</text>
            </view>
          </view>
        </view>

        <view class="card">
          <view class="card-title">申请信息</view>
          <view v-for="row in infoRows" :key="row.label" class="info-row">
            <text class="info-label">{{ row.label }}</text>
            <text class="info-value">{{ row.value }}</text>
          </view>

          <view v-if="detail.reason" class="text-block">
            <text class="block-label">事由</text>
            <text class="block-value">{{ detail.reason }}</text>
          </view>
          <view v-if="detail.remark" class="text-block">
            <text class="block-label">备注</text>
            <text class="block-value">{{ detail.remark }}</text>
          </view>
        </view>

        <view class="card">
          <view class="card-title">审批流转</view>
          <view class="timeline">
            <view v-for="node in timeline" :key="node.name" class="timeline-item" :class="{ active: node.active }">
              <view class="timeline-dot" :style="{ backgroundColor: node.active ? node.color : '#D6DBE5' }">
                <tn-icon :name="node.icon"></tn-icon>
              </view>
              <view class="timeline-main">
                <text class="timeline-name">{{ node.name }}</text>
                <text class="timeline-desc">{{ node.desc }}</text>
              </view>
            </view>
          </view>
        </view>

        <view v-if="Number(detail.status) !== 0" class="card">
          <view class="card-title">审批结果</view>
          <view v-for="row in resultRows" :key="row.label" class="info-row">
            <text class="info-label">{{ row.label }}</text>
            <text class="info-value">{{ row.value }}</text>
          </view>
          <view v-if="detail.approveRemark" class="text-block">
            <text class="block-label">审批意见</text>
            <text class="block-value">{{ detail.approveRemark }}</text>
          </view>
        </view>

        <view v-if="canApprove" class="action-card">
          <textarea v-model="approveRemark" class="remark-input" maxlength="200" placeholder="审批意见，拒绝时必填" />
          <view class="action-row">
            <view class="reject-btn" @click="reject">拒绝</view>
            <view class="approve-btn" @click="approve">通过</view>
          </view>
        </view>
      </view>

      <view v-else class="loading-state">
        <view class="loading-icon">
          <tn-icon name="time"></tn-icon>
        </view>
        <text>加载中...</text>
      </view>
    </scroll-view>
  </view>
</template>

<script setup>
import { computed, ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { useCustomBarHeight, useGoBack } from '@/libs/composables'
import { getApplicationDetail, approveApplication } from '@/api/application'

const { vuex_custom_bar_height } = useCustomBarHeight()
const { goBack } = useGoBack()

const id = ref('')
const mode = ref('')
const detail = ref(null)
const approveRemark = ref('')

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

const typeColors = {
  leave: '#4B98FE',
  overtime: '#FFAC00',
  makeup: '#00C8B0',
  business: '#957BFE',
  exchange: '#00D05E',
  resignation: '#FB6A67',
  regularization: '#00B9FE',
  transfer: '#FE871B',
  reward: '#FFAC00',
  punish: '#FB6A67'
}

const statusMap = {
  0: { name: '待审批', color: '#FFAC00', bg: 'rgba(255, 172, 0, 0.14)' },
  1: { name: '已通过', color: '#00C8B0', bg: 'rgba(0, 200, 176, 0.12)' },
  2: { name: '已拒绝', color: '#FB6A67', bg: 'rgba(251, 106, 103, 0.12)' },
  3: { name: '已撤销', color: '#9AA4B2', bg: 'rgba(154, 164, 178, 0.14)' }
}

const canApprove = computed(() => mode.value === 'approve' && Number(detail.value?.status) === 0)

const infoRows = computed(() => {
  const item = detail.value || {}
  return [
    { label: '申请类型', value: typeName(item.appType) },
    { label: '开始时间', value: formatTime(item.startTime) },
    { label: '结束时间', value: formatTime(item.endTime) },
    { label: '时长', value: item.duration ? `${item.duration}小时` : '' },
    { label: '转正日期', value: item.regularDate },
    { label: '试用期结束', value: item.probationEndDate },
    { label: '调动类型', value: item.transferType },
    { label: '原部门', value: item.fromDeptName },
    { label: '新部门', value: item.toDeptName },
    { label: '原职位', value: item.fromPosition },
    { label: '新职位', value: item.toPosition },
    { label: '生效日期', value: item.effectDate },
    { label: '离职类型', value: item.resignType },
    { label: '最后工作日', value: item.lastWorkDate },
    { label: '交接人', value: item.handoverToName },
    { label: '类别', value: item.category },
    { label: '金额', value: item.amount ? `¥${item.amount}` : '' },
    { label: '提交时间', value: formatTime(item.createdTime) }
  ].filter((row) => row.value !== undefined && row.value !== null && row.value !== '')
})

const resultRows = computed(() => {
  const item = detail.value || {}
  return [
    { label: '审批状态', value: statusMeta(item.status).name },
    { label: '审批时间', value: formatTime(item.approveTime) }
  ].filter((row) => row.value !== undefined && row.value !== null && row.value !== '')
})

const timeline = computed(() => {
  const status = Number(detail.value?.status)
  const meta = statusMeta(status)
  if (status === 0) {
    return [
      { name: '已提交', desc: formatTime(detail.value?.createdTime), icon: 'success-circle-fill', color: '#00C8B0', active: true },
      { name: '待审批', desc: '等待审批人处理', icon: 'time-fill', color: '#FFAC00', active: true },
      { name: '完成', desc: '审批完成后更新状态', icon: 'check', color: '#D6DBE5', active: false }
    ]
  }
  return [
    { name: '已提交', desc: formatTime(detail.value?.createdTime), icon: 'success-circle-fill', color: '#00C8B0', active: true },
    { name: '已审批', desc: formatTime(detail.value?.approveTime) || '已处理', icon: status === 2 ? 'close' : 'safe-fill', color: meta.color, active: true },
    { name: meta.name, desc: detail.value?.approveRemark || '流程已结束', icon: status === 2 ? 'close' : 'success-circle-fill', color: meta.color, active: true }
  ]
})

onLoad((options) => {
  id.value = options.id || ''
  mode.value = options.mode || ''
  loadDetail()
})

async function loadDetail() {
  if (!id.value) return
  try {
    const res = await getApplicationDetail(id.value)
    detail.value = res.data
  } catch (e) {
    uni.showToast({ title: '加载失败', icon: 'none' })
  }
}

function typeName(appType) {
  return typeMap[appType] || appType || '申请'
}

function typeColor(appType) {
  return typeColors[appType] || '#4B98FE'
}

function statusMeta(status) {
  return statusMap[Number(status)] || { name: '未知', color: '#9AA4B2', bg: 'rgba(154, 164, 178, 0.14)' }
}

function firstChar(name) {
  return (name || '?').slice(0, 1)
}

function formatTime(value) {
  if (!value) return ''
  return String(value).replace('T', ' ').slice(0, 16)
}

function approve() {
  uni.showModal({
    title: '确认审批',
    content: `确定通过${detail.value?.employeeName || ''}的${typeName(detail.value?.appType)}吗？`,
    success: async (res) => {
      if (!res.confirm) return
      await submitApprove(1)
    }
  })
}

function reject() {
  if (!approveRemark.value.trim()) {
    uni.showToast({ title: '拒绝时请填写审批意见', icon: 'none' })
    return
  }
  uni.showModal({
    title: '确认拒绝',
    content: `确定拒绝${detail.value?.employeeName || ''}的${typeName(detail.value?.appType)}吗？`,
    success: async (res) => {
      if (!res.confirm) return
      await submitApprove(2)
    }
  })
}

async function submitApprove(status) {
  try {
    await approveApplication(id.value, status, approveRemark.value.trim())
    uni.showToast({ title: status === 1 ? '已通过' : '已拒绝', icon: 'success' })
    setTimeout(loadDetail, 500)
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
  background: #f7f8fb;
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

.page-scroll {
  box-sizing: border-box;
  height: 100vh;
}

.content-wrap {
  padding: 24rpx 24rpx 90rpx;
}

.hero-card,
.card,
.action-card {
  margin-bottom: 22rpx;
  padding: 28rpx;
  background: #fff;
  border-radius: 20rpx;
  box-shadow: 0 10rpx 30rpx rgba(29, 37, 65, 0.06);
}

.hero-card {
  background: linear-gradient(135deg, #eaf3ff, #fff);
}

.hero-top,
.applicant-row,
.action-row,
.info-row,
.timeline-item {
  display: flex;
  align-items: center;
}

.hero-top,
.action-row {
  justify-content: space-between;
}

.type-badge,
.status-badge {
  padding: 8rpx 18rpx;
  border-radius: 999rpx;
  font-size: 24rpx;
  font-weight: 800;
}

.type-badge {
  color: #3d7eff;
  background: rgba(61, 126, 255, 0.1);
}

.applicant-row {
  margin-top: 28rpx;
}

.avatar {
  width: 88rpx;
  height: 88rpx;
  border-radius: 50%;
  color: #fff;
  font-size: 36rpx;
  font-weight: 800;
  display: flex;
  align-items: center;
  justify-content: center;
}

.applicant-main {
  flex: 1;
  min-width: 0;
  margin-left: 20rpx;
}

.name {
  display: block;
  color: #1d2541;
  font-size: 34rpx;
  font-weight: 800;
}

.sub {
  margin-top: 8rpx;
  color: #657189;
  font-size: 25rpx;
}

.card-title {
  margin-bottom: 20rpx;
  color: #1d2541;
  font-size: 32rpx;
  font-weight: 800;
}

.info-row {
  padding: 18rpx 0;
  border-bottom: 1rpx solid #f0f2f6;
}

.info-row:last-child {
  border-bottom: 0;
}

.info-label {
  width: 170rpx;
  color: #9aa4b2;
  font-size: 26rpx;
}

.info-value {
  flex: 1;
  color: #1d2541;
  font-size: 27rpx;
  font-weight: 600;
  word-break: break-all;
}

.text-block {
  margin-top: 18rpx;
  padding: 20rpx;
  border-radius: 16rpx;
  background: #f7f8fb;
}

.block-label {
  display: block;
  color: #9aa4b2;
  font-size: 25rpx;
}

.block-value {
  display: block;
  margin-top: 10rpx;
  color: #1d2541;
  font-size: 27rpx;
  line-height: 1.7;
}

.timeline {
  padding-left: 8rpx;
}

.timeline-item {
  position: relative;
  padding: 0 0 28rpx;
}

.timeline-item::after {
  content: '';
  position: absolute;
  left: 24rpx;
  top: 54rpx;
  bottom: 0;
  width: 2rpx;
  background: #e8ebf1;
}

.timeline-item:last-child {
  padding-bottom: 0;
}

.timeline-item:last-child::after {
  display: none;
}

.timeline-dot {
  position: relative;
  z-index: 1;
  width: 50rpx;
  height: 50rpx;
  border-radius: 50%;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
}

.timeline-main {
  flex: 1;
  min-width: 0;
  margin-left: 18rpx;
}

.timeline-name {
  display: block;
  color: #1d2541;
  font-size: 28rpx;
  font-weight: 800;
}

.timeline-desc {
  display: block;
  margin-top: 6rpx;
  color: #9aa4b2;
  font-size: 24rpx;
}

.remark-input {
  box-sizing: border-box;
  width: 100%;
  height: 170rpx;
  padding: 22rpx;
  color: #1d2541;
  font-size: 28rpx;
  background: #f7f8fb;
  border-radius: 16rpx;
}

.action-row {
  margin-top: 24rpx;
}

.reject-btn,
.approve-btn {
  width: 47%;
  height: 76rpx;
  border-radius: 999rpx;
  color: #fff;
  font-size: 29rpx;
  font-weight: 800;
  display: flex;
  align-items: center;
  justify-content: center;
}

.reject-btn {
  background: #fb6a67;
}

.approve-btn {
  background: linear-gradient(135deg, #4b98fe, #3d7eff);
}

.loading-state {
  padding-top: 260rpx;
  color: #9aa4b2;
  font-size: 28rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.loading-icon {
  width: 110rpx;
  height: 110rpx;
  margin-bottom: 22rpx;
  border-radius: 50%;
  color: #4b98fe;
  background: rgba(75, 152, 254, 0.1);
  font-size: 58rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.clamp-1 {
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 1;
  overflow: hidden;
}
</style>
