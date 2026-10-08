<template>
  <view class="feedback-page">
    <tn-navbar fixed home-icon="" :bottom-shadow="false" bg-color="#FFFFFF" :placeholder="false">
      <template #back>
        <view class="nav-back" @click="goBack">
          <tn-icon name="left-arrow"></tn-icon>
        </view>
      </template>
      <view class="tn-flex tn-flex-col-center tn-flex-row-center">
        <text class="tn-text-bold tn-text-xl tn-color-black">意见反馈</text>
      </view>
    </tn-navbar>

    <scroll-view
      scroll-y
      class="feedback-scroll"
      :style="{ paddingTop: vuex_custom_bar_height + 16 + 'px' }"
      @scrolltolower="loadMoreHistory"
    >
      <view class="hero-card">
        <view class="hero-main">
          <view class="hero-title">把问题说清楚</view>
          <view class="hero-desc">登录、打卡、审批、资料显示这些问题都可以在这里提交，后台管理员会统一查看处理。</view>
        </view>
        <view class="hero-icon">
          <tn-icon name="edit-write"></tn-icon>
        </view>
      </view>

      <view class="form-card">
        <view class="field-block">
          <view class="field-label">反馈类型</view>
          <view class="type-list">
            <view
              v-for="item in feedbackTypes"
              :key="item.value"
              class="type-chip"
              :class="{ active: form.feedbackType === item.value }"
              @click="form.feedbackType = item.value"
            >
              {{ item.label }}
            </view>
          </view>
        </view>

        <view class="field-block">
          <view class="field-label">反馈内容</view>
          <textarea
            v-model="form.feedbackContent"
            class="feedback-textarea"
            maxlength="500"
            placeholder="请描述遇到的问题或建议，例如：在哪个页面、点了什么、出现了什么结果。"
            placeholder-class="placeholder"
          />
          <view class="counter">{{ form.feedbackContent.length }}/500</view>
        </view>

        <view class="field-block">
          <view class="field-label">联系方式</view>
          <input
            v-model="form.contactPhone"
            class="feedback-input"
            maxlength="30"
            type="text"
            placeholder="可选，方便管理员联系你"
            placeholder-class="placeholder"
          />
        </view>

        <tn-button
          width="100%"
          shape="round"
          size="xl"
          bg-color="#5B8DFF"
          text-color="#FFFFFF"
          :loading="submitting"
          :custom-style="{ padding: '22rpx' }"
          @click="handleSubmit"
        >
          提交反馈
        </tn-button>
      </view>

      <!-- 我的反馈:分页历史(上拉加载更多) -->
      <view v-if="records.length || historyFailed" class="history-section">
        <view class="section-title">我的反馈</view>

        <!-- 历史加载失败态(与空态区分) -->
        <view v-if="historyFailed && !records.length" class="history-card history-state">
          <view class="history-state-text">反馈记录加载失败</view>
          <view class="history-retry" @click="reloadHistory">点击重试</view>
        </view>

        <view v-for="item in records" :key="item.id" class="history-card">
          <view class="history-head">
            <view class="history-type">{{ typeText(item.feedbackType) }}</view>
            <view class="status-tag" :class="'status-' + item.status">{{ statusText(item.status) }}</view>
          </view>
          <view class="history-content">{{ item.feedbackContent }}</view>
          <!-- 管理员回复:内容 + 回复时间,样式区分 -->
          <view v-if="replyOf(item)" class="reply-box">
            <view class="reply-title">
              <tn-icon name="comment-fill" class="reply-title__icon"></tn-icon>
              <text>管理员回复</text>
            </view>
            <view class="reply-content">{{ replyOf(item) }}</view>
            <view v-if="item.replyTime" class="reply-time">{{ formatTime(item.replyTime) }}</view>
          </view>
          <view class="history-time">{{ item.createdTime || '' }}</view>
        </view>

        <!-- 空态 -->
        <view v-if="!historyFailed && !records.length" class="history-card history-state">
          <view class="history-state-text">暂无反馈记录</view>
          <view class="history-state-desc">提交的反馈和处理进度会显示在这里</view>
        </view>

        <!-- 加载更多状态 -->
        <view v-if="records.length" class="load-more" @click="loadMoreHistory">
          <text v-if="loadingMore" class="load-more-text">加载中...</text>
          <text v-else-if="historyFinished" class="load-more-text load-more-text--end">没有更多了</text>
          <text v-else class="load-more-text">上拉加载更多</text>
        </view>
      </view>
    </scroll-view>
  </view>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { useCustomBarHeight, useGoBack } from '@/libs/composables'
import { getMyFeedback, submitFeedback } from '@/api/feedback'
import { toastRequestError } from '@/utils/common'

const { vuex_custom_bar_height } = useCustomBarHeight()
const { goBack } = useGoBack()

const feedbackTypes = [
  { label: '功能建议', value: 1 },
  { label: '问题反馈', value: 2 },
  { label: '其他', value: 3 }
]

const form = reactive({
  feedbackType: 2,
  feedbackContent: '',
  contactPhone: ''
})

const submitting = ref(false)

// ===== 反馈历史:分页加载(上拉加载更多) =====
const records = ref([])
const pageNum = ref(1)
const pageSize = 10
const loadingMore = ref(false)
const historyLoading = ref(false)
const historyFinished = ref(false)
const historyFailed = ref(false)

async function loadHistory(reset = false) {
  if (reset) {
    if (historyLoading.value) return
    historyLoading.value = true
    historyFailed.value = false
    historyFinished.value = false
    pageNum.value = 1
  } else if (loadingMore.value || historyFinished.value || historyLoading.value) {
    return
  } else {
    loadingMore.value = true
  }
  const page = reset ? 1 : pageNum.value + 1
  try {
    const res = await getMyFeedback({ pageNum: page, pageSize })
    const list = res.data?.records || []
    if (reset) {
      records.value = list
      pageNum.value = 1
    } else {
      const seen = new Set(records.value.map((item) => String(item.id)))
      records.value = records.value.concat(list.filter((item) => !seen.has(String(item.id))))
      pageNum.value = page
    }
    if (list.length < pageSize) historyFinished.value = true
  } catch (error) {
    // request.js 已统一 toast;失败态仅在无数据时展示
    if (!records.value.length) historyFailed.value = true
  } finally {
    historyLoading.value = false
    loadingMore.value = false
  }
}

function reloadHistory() {
  loadHistory(true)
}

function loadMoreHistory() {
  loadHistory(false)
}

// 管理员回复内容:契约字段 reply,兼容旧字段 replyContent
function replyOf(item) {
  return item.reply || item.replyContent || ''
}

function formatTime(value) {
  if (!value) return ''
  return String(value).replace('T', ' ').slice(0, 16)
}

onShow(() => {
  loadHistory(true)
})

async function handleSubmit() {
  const content = form.feedbackContent.trim()
  if (!content) {
    uni.showToast({ title: '请填写反馈内容', icon: 'none' })
    return
  }

  submitting.value = true
  try {
    await submitFeedback({
      feedbackType: form.feedbackType,
      feedbackContent: content,
      contactPhone: form.contactPhone.trim()
    })
    uni.showToast({ title: '提交成功', icon: 'success' })
    form.feedbackContent = ''
    // 提交成功后刷新历史(回到第一页,保留上拉加载更多能力)
    await loadHistory(true)
  } catch (error) {
    // request.js 会统一提示错误
  } finally {
    submitting.value = false
  }
}

function typeText(type) {
  return feedbackTypes.find((item) => item.value === type)?.label || '其他'
}

function statusText(status) {
  const map = {
    0: '待处理',
    1: '处理中',
    2: '已处理'
  }
  return map[status] || '待处理'
}
</script>

<style scoped>
.feedback-page {
  max-width: 640px;
  min-height: 100vh;
  margin: 0 auto;
  background: #F8F7F8;
}

.nav-back {
  width: 72rpx;
  height: 48rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-left: 18rpx;
  border-radius: 999rpx;
  color: #3b4b68;
  background: rgba(20, 30, 50, 0.08);
}

.feedback-scroll {
  height: 100vh;
  box-sizing: border-box;
  padding: 0 28rpx 48rpx;
}

.hero-card,
.form-card,
.history-card {
  background: #ffffff;
  border-radius: 24rpx;
  box-shadow: 0 16rpx 46rpx rgba(55, 74, 105, 0.06);
}

.hero-card {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 24rpx;
  padding: 32rpx;
  color: #1d2541;
}

.hero-title {
  font-size: 36rpx;
  font-weight: 800;
  color: #1d2541;
}

.hero-desc {
  margin-top: 12rpx;
  color: #657189;
  color: rgba(255, 255, 255, 0.84);
  font-size: 24rpx;
  line-height: 1.55;
}

.hero-icon {
  flex-shrink: 0;
  width: 88rpx;
  height: 88rpx;
  border-radius: 28rpx;
  background: rgba(54, 104, 252, 0.12);
  color: #3668FC;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 44rpx;
}

.form-card {
  margin-top: 24rpx;
  padding: 28rpx;
}

.field-block {
  margin-bottom: 28rpx;
}

.field-label {
  margin-bottom: 16rpx;
  color: #111a32;
  font-size: 28rpx;
  font-weight: 700;
}

.type-list {
  display: flex;
  gap: 16rpx;
}

.type-chip {
  flex: 1;
  height: 64rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 999rpx;
  color: #7c879b;
  font-size: 25rpx;
  background: #f4f7fb;
}

.type-chip.active {
  color: #ffffff;
  background: #5b8dff;
  box-shadow: 0 12rpx 26rpx rgba(61, 126, 255, 0.2);
}

.feedback-textarea,
.feedback-input {
  width: 100%;
  box-sizing: border-box;
  color: #111a32;
  background: #f7f9fc;
}

.feedback-textarea {
  height: 240rpx;
  padding: 22rpx;
  border-radius: 18rpx;
  font-size: 26rpx;
  line-height: 1.55;
}

.feedback-input {
  height: 80rpx;
  padding: 0 22rpx;
  border-radius: 18rpx;
  font-size: 26rpx;
}

.placeholder {
  color: #a4adbb;
}

.counter {
  margin-top: 10rpx;
  text-align: right;
  color: #a4adbb;
  font-size: 22rpx;
}

.history-section {
  margin-top: 30rpx;
}

.section-title {
  margin-bottom: 16rpx;
  color: #111a32;
  font-size: 31rpx;
  font-weight: 800;
}

.history-card {
  margin-bottom: 18rpx;
  padding: 24rpx;
}

.history-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.history-type {
  color: #111a32;
  font-size: 27rpx;
  font-weight: 700;
}

.status-tag {
  padding: 6rpx 16rpx;
  border-radius: 999rpx;
  font-size: 22rpx;
}

.status-0 {
  color: #fe871b;
  background: rgba(254, 135, 27, 0.12);
}

.status-1 {
  color: #3d7eff;
  background: rgba(61, 126, 255, 0.1);
}

.status-2 {
  color: #00c8b0;
  background: rgba(0, 200, 176, 0.12);
}

.history-content {
  margin-top: 16rpx;
  color: #5f6b7c;
  font-size: 25rpx;
  line-height: 1.55;
}

.reply-box {
  margin-top: 16rpx;
  padding: 18rpx;
  border-radius: 18rpx;
  background: #f4f8ff;
}

.reply-title {
  color: #3d7eff;
  font-size: 24rpx;
  font-weight: 700;
  display: flex;
  align-items: center;
}

.reply-title__icon {
  margin-right: 8rpx;
  font-size: 24rpx;
}

.reply-content {
  margin-top: 8rpx;
  color: #5f6b7c;
  font-size: 24rpx;
  line-height: 1.5;
}

.reply-time {
  margin-top: 8rpx;
  color: #a4adbb;
  font-size: 22rpx;
}

.history-time {
  margin-top: 14rpx;
  color: #a4adbb;
  font-size: 22rpx;
}

/* 历史区状态:空态/失败态 */
.history-state {
  padding: 40rpx 24rpx;
  text-align: center;
}

.history-state-text {
  color: #657189;
  font-size: 26rpx;
}

.history-state-desc {
  margin-top: 10rpx;
  color: #a4adbb;
  font-size: 23rpx;
}

.history-retry {
  margin: 20rpx auto 0;
  width: fit-content;
  padding: 12rpx 48rpx;
  border-radius: 999rpx;
  color: #3668fc;
  font-size: 25rpx;
  font-weight: 600;
  background: rgba(54, 104, 252, 0.1);
}

.load-more {
  padding: 24rpx 0;
  text-align: center;
}

.load-more-text {
  color: #9aa4b2;
  font-size: 24rpx;
}

.load-more-text--end {
  color: #c4cbd4;
}
</style>
