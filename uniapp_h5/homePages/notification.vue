<template>
  <view class="notification-page">
    <tn-navbar fixed home-icon="" :bottom-shadow="false" bg-color="#FFFFFF" :placeholder="false">
      <template #back>
        <view class="nav-back" @click="goBack">
          <tn-icon name="left-arrow"></tn-icon>
        </view>
      </template>
      <view class="nav-center">
        <text class="nav-title">消息中心</text>
      </view>
      <template #right>
        <view class="read-all" :class="{ 'read-all--disabled': !hasUnread }" @click="markAllRead">
          全部已读
        </view>
      </template>
    </tn-navbar>

    <scroll-view
      scroll-y
      class="list-scroll"
      :style="{ paddingTop: vuex_custom_bar_height + 'px' }"
      :refresher-enabled="true"
      :refresher-triggered="refreshing"
      @refresherrefresh="onRefresh"
      @scrolltolower="loadMore"
    >
      <view class="list-wrap">
        <!-- 首屏加载中 -->
        <view v-if="loading && !list.length" class="state-tip">加载中...</view>

        <!-- 加载失败(首屏) -->
        <view v-else-if="loadFailed && !list.length" class="state-tip">
          <view>通知加载失败</view>
          <view class="retry-btn" @click="reload">点击重试</view>
        </view>

        <!-- 空态 -->
        <view v-else-if="!list.length" class="empty-state">
          <view class="empty-icon">
            <tn-icon name="notice"></tn-icon>
          </view>
          <text class="empty-title">暂无消息</text>
          <text class="empty-desc">审批结果、待办提醒会显示在这里</text>
        </view>

        <!-- 通知列表 -->
        <template v-else>
          <view
            v-for="item in list"
            :key="item.id"
            class="notice-card"
            @click="openDetail(item)"
            @longpress="openDetail(item)"
          >
            <view class="card-icon-wrap">
              <view class="card-icon" :style="{ color: typeMeta(item.type).color, backgroundColor: typeMeta(item.type).bg }">
                <tn-icon :name="typeMeta(item.type).icon"></tn-icon>
              </view>
              <view v-if="!item.readFlag" class="unread-dot"></view>
            </view>
            <view class="card-main">
              <view class="card-head">
                <text class="card-title clamp-1">{{ item.title || '消息提醒' }}</text>
                <text class="card-time">{{ formatTime(item.createdTime) }}</text>
              </view>
              <text class="card-content clamp-2">{{ item.content || '暂无内容' }}</text>
              <view class="card-foot">
                <text class="card-tag" :style="{ color: typeMeta(item.type).color, backgroundColor: typeMeta(item.type).bg }">
                  {{ typeMeta(item.type).name }}
                </text>
                <text v-if="item.url" class="card-link">去查看</text>
              </view>
            </view>
          </view>

          <view class="load-more-state" @click="loadMore">
            <text v-if="loadingMore" class="load-more-text">加载中...</text>
            <text v-else-if="finished" class="load-more-text load-more-text--end">没有更多了</text>
            <text v-else class="load-more-text">上拉加载更多</text>
          </view>
        </template>
      </view>
    </scroll-view>

    <!-- 通知详情弹层 -->
    <view v-if="activeItem" class="detail-mask" @click="closeDetail">
      <view class="detail-panel" @click.stop>
        <view class="detail-head">
          <view class="detail-head-main">
            <text class="detail-title">{{ activeItem.title || '消息提醒' }}</text>
            <text class="detail-date">{{ formatTime(activeItem.createdTime) }}</text>
          </view>
          <view class="close-btn" @click="closeDetail">
            <tn-icon name="close"></tn-icon>
          </view>
        </view>
        <scroll-view scroll-y class="detail-body">
          <text class="plain-content">{{ activeItem.content || '暂无内容' }}</text>
        </scroll-view>
        <view v-if="activeItem.url" class="detail-foot">
          <tn-button
            width="100%"
            shape="round"
            bg-color="#3668FC"
            text-color="#FFFFFF"
            :font-size="28"
            :custom-style="{ padding: '20rpx 0' }"
            @click="goUrl(activeItem.url)"
          >
            去处理
          </tn-button>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { computed, ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { useCustomBarHeight, useGoBack } from '@/libs/composables'
import {
  getNotifications,
  markNotificationRead,
  markAllNotificationsRead
} from '@/api/notification'

const { vuex_custom_bar_height } = useCustomBarHeight()
const { goBack } = useGoBack()

// 分页状态
const list = ref([])
const pageNum = ref(1)
const pageSize = 15
const loading = ref(false)
const loadingMore = ref(false)
const refreshing = ref(false)
const finished = ref(false)
const loadFailed = ref(false)
// 详情弹层
const activeItem = ref(null)

const hasUnread = computed(() => list.value.some((item) => !item.readFlag))

// 通知类型 -> 图标/颜色(未知类型给默认图标)
const NOTIFICATION_TYPE_META = {
  approval_result: { name: '审批结果', icon: 'ticket-fill', color: '#00C8B0', bg: 'rgba(0, 200, 176, 0.12)' },
  approval_todo: { name: '待审批', icon: 'flag-fill', color: '#4B98FE', bg: 'rgba(75, 152, 254, 0.12)' },
  reminder: { name: '到期提醒', icon: 'clock-fill', color: '#FFAC00', bg: 'rgba(255, 172, 0, 0.12)' },
  contract: { name: '合同提醒', icon: 'clock-fill', color: '#FFAC00', bg: 'rgba(255, 172, 0, 0.12)' },
  probation: { name: '试用期提醒', icon: 'clock-fill', color: '#FFAC00', bg: 'rgba(255, 172, 0, 0.12)' },
  certificate: { name: '证照提醒', icon: 'clock-fill', color: '#FFAC00', bg: 'rgba(255, 172, 0, 0.12)' }
}
const DEFAULT_META = { name: '消息提醒', icon: 'notice-fill', color: '#4B98FE', bg: 'rgba(75, 152, 254, 0.12)' }

const typeMeta = (type) => NOTIFICATION_TYPE_META[type] || DEFAULT_META

const formatTime = (value) => {
  if (!value) return ''
  return String(value).replace('T', ' ').slice(0, 16)
}

// 首屏/下拉刷新:重置分页拉第一页
async function loadFirst() {
  if (loading.value) return
  loading.value = true
  loadFailed.value = false
  finished.value = false
  pageNum.value = 1
  try {
    const res = await getNotifications({ pageNum: 1, pageSize })
    const records = res.data?.records || []
    list.value = records
    if (records.length < pageSize) finished.value = true
  } catch (error) {
    loadFailed.value = true
    // request.js 已统一 toast,这里不再重复提示
  } finally {
    loading.value = false
  }
}

// 上拉加载下一页
async function loadMore() {
  if (loadingMore.value || finished.value || loading.value || loadFailed.value) return
  loadingMore.value = true
  try {
    const nextPage = pageNum.value + 1
    const res = await getNotifications({ pageNum: nextPage, pageSize })
    const records = res.data?.records || []
    const seen = new Set(list.value.map((item) => String(item.id)))
    list.value = list.value.concat(records.filter((item) => !seen.has(String(item.id))))
    pageNum.value = nextPage
    if (records.length < pageSize) finished.value = true
  } catch (error) {
    // request.js 已统一 toast
  } finally {
    loadingMore.value = false
  }
}

function onRefresh() {
  if (refreshing.value) return
  refreshing.value = true
  loadFirst().finally(() => {
    refreshing.value = false
  })
}

function reload() {
  loadFirst()
}

// 打开详情:本地置为已读并上报(幂等,失败不影响浏览)
function openDetail(item) {
  if (!item?.id) return
  activeItem.value = item
  if (!item.readFlag) {
    item.readFlag = 1
    markNotificationRead(item.id).catch(() => {})
  }
}

function closeDetail() {
  activeItem.value = null
}

// 跳转通知关联页面
function goUrl(url) {
  if (!url) return
  closeDetail()
  uni.navigateTo({
    url,
    fail: () => {
      uni.showToast({ icon: 'none', title: '页面不存在' })
    }
  })
}

// 全部已读:本地全部置已读 + 上报
function markAllRead() {
  if (!hasUnread.value) {
    uni.showToast({ icon: 'none', title: '没有未读消息' })
    return
  }
  uni.showModal({
    title: '提示',
    content: '确定将所有消息标记为已读吗？',
    success: async (res) => {
      if (!res.confirm) return
      try {
        await markAllNotificationsRead()
        list.value = list.value.map((item) => ({ ...item, readFlag: 1 }))
        uni.showToast({ icon: 'none', title: '已全部标记已读' })
      } catch (error) {
        // request.js 已统一 toast
      }
    }
  })
}

onShow(() => {
  loadFirst()
})
</script>

<style lang="scss" scoped>
.notification-page {
  max-width: 640px;
  min-height: 100vh;
  margin: 0 auto;
  background: #F7F8FA;
}

.nav-back {
  width: 72rpx;
  height: 54rpx;
  margin-left: 18rpx;
  border-radius: 999rpx;
  background: rgba(29, 37, 65, 0.08);
  color: #1d2541;
  display: flex;
  align-items: center;
  justify-content: center;
}

.nav-center {
  flex: 1;
  text-align: center;
}

.nav-title {
  color: #1d2541;
  font-size: 34rpx;
  font-weight: 700;
}

/* 顶部"全部已读" */
.read-all {
  margin-right: 24rpx;
  padding: 10rpx 26rpx;
  border-radius: 999rpx;
  background: rgba(54, 104, 252, 0.1);
  color: #3668FC;
  font-size: 24rpx;
  font-weight: 600;
}

.read-all--disabled {
  color: #9aa4b2;
  background: #f1f3f7;
}

.list-scroll {
  box-sizing: border-box;
  height: 100vh;
}

.list-wrap {
  padding: 24rpx 24rpx 60rpx;
}

/* 状态:加载中/失败/空 */
.state-tip {
  padding: 160rpx 0;
  color: #9aa4b2;
  font-size: 26rpx;
  text-align: center;
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

.empty-state {
  padding-top: 160rpx;
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

/* 通知卡片 */
.notice-card {
  display: flex;
  margin-bottom: 20rpx;
  padding: 26rpx;
  background: #ffffff;
  border-radius: 16rpx;
  border: 1rpx solid rgba(17, 31, 46, 0.06);
}

.card-icon-wrap {
  position: relative;
  flex-shrink: 0;
}

.card-icon {
  width: 84rpx;
  height: 84rpx;
  border-radius: 20rpx;
  font-size: 40rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.unread-dot {
  position: absolute;
  top: -4rpx;
  right: -4rpx;
  width: 16rpx;
  height: 16rpx;
  border-radius: 50%;
  background: #fb6a67;
}

.card-main {
  flex: 1;
  min-width: 0;
  margin-left: 20rpx;
}

.card-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.card-title {
  flex: 1;
  min-width: 0;
  color: #1d2541;
  font-size: 30rpx;
  font-weight: 700;
}

.card-time {
  flex-shrink: 0;
  margin-left: 16rpx;
  color: #9aa4b2;
  font-size: 22rpx;
}

.card-content {
  display: block;
  margin-top: 10rpx;
  color: #657189;
  font-size: 25rpx;
  line-height: 1.5;
}

.card-foot {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 16rpx;
}

.card-tag {
  padding: 4rpx 16rpx;
  border-radius: 999rpx;
  font-size: 22rpx;
  font-weight: 600;
}

.card-link {
  color: #3668fc;
  font-size: 24rpx;
}

.load-more-state {
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

/* 详情弹层 */
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
  padding: 30rpx 30rpx calc(36rpx + env(safe-area-inset-bottom));
  border-radius: 28rpx 28rpx 0 0;
  background: #fff;
}

.detail-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
}

.detail-head-main {
  flex: 1;
  min-width: 0;
}

.detail-title {
  display: block;
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
  flex-shrink: 0;
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
  max-height: 50vh;
  margin-top: 24rpx;
}

.plain-content {
  color: #1d2541;
  font-size: 28rpx;
  line-height: 1.8;
  white-space: pre-wrap;
}

.detail-foot {
  margin-top: 28rpx;
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
