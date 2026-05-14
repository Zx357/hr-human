<template>
  <view class="notice-page">
    <tn-navbar fixed home-icon="" :bottom-shadow="false" bg-color="#FFFFFF" :placeholder="false">
      <template #back>
        <view class="nav-back" @click="goBack">
          <tn-icon name="left-arrow"></tn-icon>
        </view>
      </template>
      <view class="tn-flex tn-flex-col-center tn-flex-row-center">
        <text class="tn-text-bold tn-text-xl tn-color-black">系统公告</text>
      </view>
    </tn-navbar>

    <scroll-view scroll-y class="notice-scroll" :style="{ paddingTop: vuex_custom_bar_height + 16 + 'px' }">
      <view class="hero-card">
        <view>
          <view class="hero-title">系统公告</view>
          <view class="hero-desc">这里展示后台发布的公告，主要放系统上线、功能调整和使用提醒。</view>
        </view>
        <view class="hero-icon">
          <tn-icon name="notice-fill"></tn-icon>
        </view>
      </view>

      <view v-if="loading" class="state-card">
        <tn-icon name="loading"></tn-icon>
        <text>加载中...</text>
      </view>

      <view v-else-if="noticeList.length === 0" class="state-card">
        <tn-icon name="empty-list"></tn-icon>
        <text>暂无公告</text>
      </view>

      <view v-else class="notice-list">
        <view v-for="item in noticeList" :key="item.id" class="notice-card" @click="openNotice(item)">
          <view class="notice-head">
            <view class="notice-icon">
              <tn-icon name="bookmark-fill"></tn-icon>
            </view>
            <view class="notice-main">
              <view class="notice-title">{{ item.noticeTitle }}</view>
              <view class="notice-date">{{ formatDate(item.publishTime || item.createdTime) }}</view>
            </view>
            <tn-icon name="right" class="notice-arrow"></tn-icon>
          </view>
          <view class="notice-content clamp-2">{{ item.noticeContent }}</view>
        </view>
      </view>
    </scroll-view>

    <view v-if="detailVisible" class="detail-mask" @click="detailVisible = false">
      <view class="detail-panel" @click.stop>
        <view class="detail-title">{{ activeNotice.noticeTitle }}</view>
        <view class="detail-date">{{ formatDate(activeNotice.publishTime || activeNotice.createdTime) }}</view>
        <scroll-view scroll-y class="detail-content">
          <text>{{ activeNotice.noticeContent }}</text>
        </scroll-view>
        <tn-button
          width="100%"
          shape="round"
          bg-color="#5B8DFF"
          text-color="#FFFFFF"
          :custom-style="{ padding: '18rpx' }"
          @click="detailVisible = false"
        >
          我知道了
        </tn-button>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { useCustomBarHeight, useGoBack } from '@/libs/composables'
import { getNoticeList } from '@/api/system/notice'

const { vuex_custom_bar_height } = useCustomBarHeight()
const { goBack } = useGoBack()

const loading = ref(false)
const noticeList = ref([])
const detailVisible = ref(false)
const activeNotice = ref({})

onLoad(() => {
  loadNotices()
})

async function loadNotices() {
  loading.value = true
  try {
    const res = await getNoticeList({ noticeType: 1, status: 1 })
    noticeList.value = res.data || []
  } catch (error) {
    noticeList.value = []
  } finally {
    loading.value = false
  }
}

function openNotice(item) {
  activeNotice.value = item
  detailVisible.value = true
}

function formatDate(value) {
  if (!value) return ''
  return String(value).replace('T', ' ').slice(0, 10)
}
</script>

<style scoped>
.notice-page {
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

.notice-scroll {
  height: 100vh;
  box-sizing: border-box;
  padding: 0 28rpx 48rpx;
}

.hero-card,
.notice-card,
.state-card,
.detail-panel {
  background: #ffffff;
  border-radius: 24rpx;
  box-shadow: 0 16rpx 46rpx rgba(55, 74, 105, 0.06);
}

.hero-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 22rpx;
  padding: 32rpx;
}

.hero-title {
  color: #111a32;
  font-size: 36rpx;
  font-weight: 800;
}

.hero-desc {
  margin-top: 12rpx;
  color: #7c879b;
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
  border-radius: 50%;
  color: #ffac00;
  font-size: 44rpx;
  background: rgba(255, 172, 0, 0.13);
}

.notice-list {
  margin-top: 24rpx;
}

.notice-card {
  margin-bottom: 18rpx;
  padding: 24rpx;
}

.notice-head {
  display: flex;
  align-items: center;
}

.notice-icon {
  width: 70rpx;
  height: 70rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  flex: none;
  border-radius: 22rpx;
  color: #5b8dff;
  font-size: 34rpx;
  background: rgba(91, 141, 255, 0.1);
}

.notice-main {
  flex: 1;
  min-width: 0;
  padding: 0 18rpx;
}

.notice-title {
  color: #111a32;
  font-size: 29rpx;
  font-weight: 700;
}

.notice-date {
  margin-top: 8rpx;
  color: #9aa4b6;
  font-size: 23rpx;
}

.notice-arrow {
  color: #b5bdca;
  font-size: 28rpx;
}

.notice-content {
  margin-top: 18rpx;
  color: #5f6b7c;
  font-size: 25rpx;
  line-height: 1.55;
}

.state-card {
  margin-top: 24rpx;
  padding: 72rpx 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16rpx;
  color: #9aa4b6;
  font-size: 25rpx;
}

.state-card .tn-icon {
  font-size: 52rpx;
}

.detail-mask {
  position: fixed;
  inset: 0;
  z-index: 50;
  display: flex;
  align-items: flex-end;
  justify-content: center;
  background: rgba(13, 25, 45, 0.42);
}

.detail-panel {
  width: 100%;
  max-width: 640px;
  box-sizing: border-box;
  padding: 34rpx 30rpx calc(34rpx + env(safe-area-inset-bottom));
  border-radius: 28rpx 28rpx 0 0;
}

.detail-title {
  color: #111a32;
  font-size: 34rpx;
  font-weight: 800;
}

.detail-date {
  margin-top: 10rpx;
  color: #9aa4b6;
  font-size: 24rpx;
}

.detail-content {
  max-height: 520rpx;
  margin: 24rpx 0 30rpx;
  color: #4d596c;
  font-size: 27rpx;
  line-height: 1.7;
  white-space: pre-wrap;
}

.clamp-2 {
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
  overflow: hidden;
}
</style>
