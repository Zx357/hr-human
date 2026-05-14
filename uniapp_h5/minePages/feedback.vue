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

    <scroll-view scroll-y class="feedback-scroll" :style="{ paddingTop: vuex_custom_bar_height + 16 + 'px' }">
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

      <view v-if="records.length" class="history-section">
        <view class="section-title">我的反馈</view>
        <view v-for="item in records" :key="item.id" class="history-card">
          <view class="history-head">
            <view class="history-type">{{ typeText(item.feedbackType) }}</view>
            <view class="status-tag" :class="'status-' + item.status">{{ statusText(item.status) }}</view>
          </view>
          <view class="history-content">{{ item.feedbackContent }}</view>
          <view v-if="item.replyContent" class="reply-box">
            <view class="reply-title">处理回复</view>
            <view class="reply-content">{{ item.replyContent }}</view>
          </view>
          <view class="history-time">{{ item.createdTime || '' }}</view>
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
const records = ref([])

onShow(() => {
  loadRecords()
})

async function loadRecords() {
  try {
    const res = await getMyFeedback({ current: 1, size: 5 })
    records.value = res.data?.records || []
  } catch (error) {
    records.value = []
  }
}

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
    await loadRecords()
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
  background: #f4f7fb;
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
  gap: 24rpx;
  padding: 32rpx;
  color: #ffffff;
  background: linear-gradient(135deg, #5a7ec8, #8eb0d8);
}

.hero-title {
  font-size: 36rpx;
  font-weight: 800;
}

.hero-desc {
  margin-top: 12rpx;
  color: rgba(255, 255, 255, 0.84);
  font-size: 24rpx;
  line-height: 1.55;
}

.hero-icon {
  width: 92rpx;
  height: 92rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  flex: none;
  border-radius: 28rpx;
  font-size: 46rpx;
  background: rgba(255, 255, 255, 0.18);
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
}

.reply-content {
  margin-top: 8rpx;
  color: #5f6b7c;
  font-size: 24rpx;
  line-height: 1.5;
}

.history-time {
  margin-top: 14rpx;
  color: #a4adbb;
  font-size: 22rpx;
}
</style>
