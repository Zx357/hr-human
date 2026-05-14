<template>
  <view class="oa-page">
    <tn-navbar fixed home-icon="" :bottom-shadow="false" bg-color="#FFFFFF" :placeholder="false">
      <template #back>
        <view class="nav-back" @click="goBack">
          <tn-icon name="left-arrow"></tn-icon>
        </view>
      </template>
      <view class="tn-flex tn-flex-col-center tn-flex-row-center">
        <text class="tn-text-bold tn-text-xl tn-color-black">通知</text>
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
      @refresherrefresh="loadNotices"
    >
      <view class="list-wrap">
        <view v-for="item in filteredList" :key="item.id" class="notice-card" @click="openDetail(item)">
          <view class="notice-icon" :style="{ color: noticeMeta(item).color, backgroundColor: noticeMeta(item).bg }">
            <tn-icon :name="noticeMeta(item).icon"></tn-icon>
          </view>
          <view class="notice-main">
            <view class="notice-head">
              <text class="notice-title clamp-1">{{ item.noticeTitle || '未命名通知' }}</text>
              <text class="notice-date">{{ formatDate(item.publishTime || item.createdTime) }}</text>
            </view>
            <text class="notice-content clamp-2">{{ stripHtml(item.noticeContent) || '暂无内容' }}</text>
            <view class="notice-footer">
              <text class="notice-tag" :style="{ color: noticeMeta(item).color, backgroundColor: noticeMeta(item).bg }">
                {{ noticeMeta(item).name }}
              </text>
              <text class="read-more">查看详情</text>
            </view>
          </view>
        </view>

        <view v-if="!loading && !filteredList.length" class="empty-state">
          <view class="empty-icon">
            <tn-icon name="notice"></tn-icon>
          </view>
          <text class="empty-title">暂无通知</text>
          <text class="empty-desc">系统通知会显示在这里</text>
        </view>
      </view>
    </scroll-view>

    <view v-if="activeNotice" class="detail-mask" @click="closeDetail">
      <view class="detail-panel" @click.stop>
        <view class="detail-head">
          <view>
            <text class="detail-title">{{ activeNotice.noticeTitle || '通知详情' }}</text>
            <text class="detail-date">{{ formatDate(activeNotice.publishTime || activeNotice.createdTime) }}</text>
          </view>
          <view class="close-btn" @click="closeDetail">
            <tn-icon name="close"></tn-icon>
          </view>
        </view>
        <scroll-view scroll-y class="detail-body">
          <rich-text v-if="hasHtml(activeNotice.noticeContent)" :nodes="activeNotice.noticeContent"></rich-text>
          <text v-else class="plain-content">{{ activeNotice.noticeContent || '暂无内容' }}</text>
        </scroll-view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { computed, ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { useCustomBarHeight, useGoBack } from '@/libs/composables'
import { getNoticeList } from '@/api/system/notice'

const { vuex_custom_bar_height } = useCustomBarHeight()
const { goBack } = useGoBack()

const tabs = [
  { name: '全部', value: undefined },
  { name: '公告', value: 1 },
  { name: '通知', value: 2 }
]

const currentTab = ref(0)
const list = ref([])
const loading = ref(false)
const refreshing = ref(false)
const activeNotice = ref(null)

const filteredList = computed(() => {
  const type = tabs[currentTab.value]?.value
  if (!type) return list.value
  return list.value.filter((item) => Number(item.noticeType) === type)
})

onLoad(() => {
  loadNotices()
})

async function loadNotices() {
  if (loading.value) return
  loading.value = true
  refreshing.value = true
  try {
    const res = await getNoticeList({ status: 1 })
    list.value = Array.isArray(res.data) ? res.data : (res.data?.records || res.data?.list || [])
  } catch (e) {
    console.log('加载通知失败', e)
  } finally {
    loading.value = false
    refreshing.value = false
  }
}

function onTabChange(index) {
  currentTab.value = index
}

function noticeMeta(item) {
  const isNotice = Number(item.noticeType) === 2
  return isNotice
    ? { name: '通知', icon: 'notice-fill', color: '#3D7EFF', bg: 'rgba(61, 126, 255, 0.1)' }
    : { name: '公告', icon: 'image-text-fill', color: '#00C8B0', bg: 'rgba(0, 200, 176, 0.12)' }
}

function formatDate(value) {
  if (!value) return ''
  return String(value).replace('T', ' ').slice(0, 10)
}

function stripHtml(value) {
  if (!value) return ''
  return String(value).replace(/<[^>]+>/g, '').replace(/&nbsp;/g, ' ').trim()
}

function hasHtml(value) {
  return /<[^>]+>/.test(String(value || ''))
}

function openDetail(item) {
  activeNotice.value = item
}

function closeDetail() {
  activeNotice.value = null
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

.notice-card {
  display: flex;
  margin-bottom: 22rpx;
  padding: 26rpx;
  background: #fff;
  border-radius: 18rpx;
  box-shadow: 0 10rpx 30rpx rgba(29, 37, 65, 0.06);
}

.notice-icon {
  flex-shrink: 0;
  width: 86rpx;
  height: 86rpx;
  border-radius: 50%;
  font-size: 42rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.notice-main {
  flex: 1;
  min-width: 0;
  margin-left: 20rpx;
}

.notice-head,
.notice-footer,
.detail-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.notice-title {
  flex: 1;
  color: #1d2541;
  font-size: 31rpx;
  font-weight: 800;
}

.notice-date {
  flex-shrink: 0;
  margin-left: 16rpx;
  color: #9aa4b2;
  font-size: 24rpx;
}

.notice-content {
  margin-top: 12rpx;
  color: #657189;
  font-size: 26rpx;
  line-height: 1.55;
}

.notice-footer {
  margin-top: 16rpx;
}

.notice-tag {
  padding: 6rpx 16rpx;
  border-radius: 999rpx;
  font-size: 22rpx;
  font-weight: 700;
}

.read-more {
  color: #9aa4b2;
  font-size: 24rpx;
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
  color: #3d7eff;
  background: rgba(61, 126, 255, 0.1);
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

.empty-desc {
  margin-top: 8rpx;
  color: #9aa4b2;
  font-size: 24rpx;
}

.detail-mask {
  position: fixed;
  inset: 0;
  z-index: 30;
  background: rgba(0, 0, 0, 0.35);
  display: flex;
  align-items: flex-end;
}

.detail-panel {
  width: 100%;
  max-width: 640px;
  max-height: 78vh;
  margin: 0 auto;
  padding: 30rpx 30rpx calc(40rpx + env(safe-area-inset-bottom));
  border-radius: 28rpx 28rpx 0 0;
  background: #fff;
}

.detail-title {
  display: block;
  max-width: 520rpx;
  color: #1d2541;
  font-size: 34rpx;
  font-weight: 800;
  line-height: 1.35;
}

.detail-date {
  display: block;
  margin-top: 10rpx;
  color: #9aa4b2;
  font-size: 24rpx;
}

.close-btn {
  width: 62rpx;
  height: 62rpx;
  border-radius: 50%;
  color: #657189;
  background: #f1f3f7;
  display: flex;
  align-items: center;
  justify-content: center;
}

.detail-body {
  max-height: 58vh;
  margin-top: 28rpx;
  color: #1d2541;
  font-size: 28rpx;
  line-height: 1.8;
}

.plain-content {
  color: #1d2541;
  font-size: 28rpx;
  line-height: 1.8;
  white-space: pre-wrap;
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
