<template>
  <view class="template-home tn-safe-area-inset-bottom">
    <view class="page-inner" :style="{ paddingTop: vuex_custom_bar_height + 18 + 'px' }">
      <!-- 搜索入口 -->
      <view class="search-card" @click="tn('/homePages/search')">
        <tn-icon name="search" class="tn-color-gray"></tn-icon>
        <text class="search-text">搜索同事、公告、功能</text>
        <tn-icon name="right" class="tn-color-gray--disabled"></tn-icon>
      </view>

      <!-- 快捷入口 -->
      <view class="section-title">快捷入口</view>
      <view class="quick-grid">
        <view v-for="item in shortcutList" :key="item.title" class="quick-item" @click="tn(item.url)">
          <view class="quick-chip" :style="{ backgroundColor: hexToBg(item.chipColor) }">
            <tn-badge v-if="item.badge" :value="item.badge" type="danger">
              <tn-icon :name="item.icon" :style="{ color: item.chipColor, fontSize: '42rpx' }"></tn-icon>
            </tn-badge>
            <tn-icon v-else :name="item.icon" :style="{ color: item.chipColor, fontSize: '42rpx' }"></tn-icon>
          </view>
          <text class="quick-label">{{ item.title }}</text>
        </view>
      </view>

      <!-- 消息动态 -->
      <view class="section-title">消息动态</view>
      <!-- 首屏加载中 -->
      <view v-if="homeLoading && !messageList.length" class="msg-card">
        <view class="msg-empty">加载中...</view>
      </view>

      <!-- 全部接口加载失败:提示 + 重试 -->
      <view v-else-if="homeLoadFailed && !messageList.length" class="msg-card">
        <view class="msg-empty">首页数据加载失败</view>
        <view class="msg-retry" @click="loadHomeData">点击重试</view>
      </view>

      <view v-else class="msg-card">
        <view v-if="!messageList.length" class="msg-empty">暂无消息</view>
        <view
          v-for="item in messageList"
          :key="item.id"
          class="msg-row"
          @click="tn(item.url)"
        >
          <view class="msg-chip" :style="{ backgroundColor: hexToBg(item.color) }">
            <tn-icon :name="item.icon" :style="{ color: item.color, fontSize: '36rpx' }"></tn-icon>
          </view>
          <view class="msg-main">
            <view class="msg-head">
              <text class="msg-title clamp-1">{{ item.title }}</text>
              <text class="msg-time">{{ item.time }}</text>
            </view>
            <text class="msg-desc clamp-1">{{ item.desc }}</text>
          </view>
          <tn-icon name="right" class="msg-arrow tn-color-gray--disabled"></tn-icon>
        </view>
      </view>
    </view>

    <view class="tn-tabbar-height"></view>
  </view>
</template>

<script setup>
  import { computed, onMounted, ref } from 'vue'
  import { useStore } from 'vuex'
  import { getHomeStats, getHomeMessages, getNotificationTop, getNotificationUnreadCount, markAllNotificationsRead } from '@/api/home'
  import { getMomentMessages } from '@/api/moment'
  import { getUnreadTotal } from '@/api/chat'
  import { getNoticeList } from '@/api/system/notice'
  
  const store = useStore()
  // 使用 computed 保持响应式
  const vuex_custom_bar_height = computed(() => store.state.vuex_custom_bar_height)

  const shortcutColors = ['#4B98FE', '#00C8B0', '#FFAC00', '#957BFE']

  const stats = ref({
    pendingCount: 0,
    approvalCount: 0,
    approvedCount: 0
  })
  const notices = ref([])
  const backendMessages = ref([])
  const notifications = ref([])
  const notificationUnread = ref(0)
  const momentSummary = ref({
    unreadCount: 0,
    likeCount: 0,
    commentCount: 0,
    mentionCount: 0
  })

  const formatBadge = (value) => {
    const count = Number(value || 0)
    if (count <= 0) return ''
    return count > 99 ? '99+' : count
  }

  // 时光互动未读角标:后端 unreadCount 即点赞+评论合计,直接使用,避免重复累加导致角标翻倍
  const momentBadgeCount = computed(() => Number(momentSummary.value.unreadCount || 0))

  // 聊天未读总数(会话列表角标),进入会话/标记已读后由后端清零
  const chatUnreadCount = computed(() => Number(store.state.unreadBadge?.chatUnread || 0))

  const hexToBg = (hex, alpha = 0.12) => {
    const v = String(hex || '#4B98FE').replace('#', '')
    if (v.length < 6) return 'rgba(75, 152, 254, 0.12)'
    const r = parseInt(v.slice(0, 2), 16)
    const g = parseInt(v.slice(2, 4), 16)
    const b = parseInt(v.slice(4, 6), 16)
    return `rgba(${r}, ${g}, ${b}, ${alpha})`
  }

  const shortcutList = computed(() => [
    {
      title: '消息',
      icon: 'chat',
      chipColor: shortcutColors[0],
      badge: formatBadge(chatUnreadCount.value),
      url: '/homePages/chat'
    },
    {
      title: '互动',
      icon: 'topics-fill',
      chipColor: shortcutColors[1],
      badge: formatBadge(momentBadgeCount.value),
      url: '/momentPages/message'
    },
    {
      title: '待办',
      icon: 'flag-fill',
      chipColor: shortcutColors[2],
      badge: formatBadge(stats.value.pendingCount),
      url: '/homePages/pending'
    },
    {
      title: '审批',
      icon: 'seal',
      chipColor: shortcutColors[3],
      badge: formatBadge(stats.value.approvalCount),
      url: '/homePages/approval'
    },
    {
      title: '系统',
      icon: 'notice-fill',
      chipColor: shortcutColors[0],
      badge: formatBadge(stats.value.noticeCount ?? 0),
      url: '/homePages/notice'
    }
  ])

  const messageList = computed(() => {
    // 站内通知(审批结果/待审批/到期提醒)优先展示
    const notificationMessages = notifications.value.map((item) => ({
      id: `ntf-${item.id}`,
      title: item.title || '消息提醒',
      desc: item.content || '暂无内容',
      time: formatDate(item.createdTime),
      color: item.readFlag ? '#9AA4B2' : Number(item.type === 'approval_result') ? (item.title === '审批通过' ? '#00C8B0' : '#FB6A67') : '#4B98FE',
      icon: ['contract', 'probation', 'certificate'].includes(item.type) ? 'clock-fill' : item.type === 'approval_todo' ? 'flag-fill' : 'ticket-fill',
      badge: '',
      url: item.url || '/homePages/pending'
    }))
    const apiMessages = backendMessages.value.map((item, index) => ({
      id: item.id || `message-${index}`,
      title: item.title || '消息提醒',
      desc: item.desc || '暂无内容',
      time: item.time || '',
      color: item.color || '#4B98FE',
      icon: item.icon || 'notice-fill',
      badge: item.badge || '',
      url: item.url || '/homePages/notice',
      avatar: item.avatar
    }))
    const merged = notificationMessages.concat(apiMessages)
    if (merged.length) {
      return merged.slice(0, 12)
    }

    const noticeMessages = notices.value.slice(0, 3).map((item, index) => ({
      id: `notice-${item.id || index}`,
      title: item.noticeTitle || '系统通知',
      desc: stripHtml(item.noticeContent) || '暂无内容',
      time: formatDate(item.publishTime || item.createdTime),
      color: Number(item.noticeType) === 2 ? '#4B98FE' : '#00C8B0',
      icon: Number(item.noticeType) === 2 ? 'notice-fill' : 'image-text-fill',
      url: '/homePages/notice'
    }))

    return noticeMessages.slice(0, 12)
  })

  const stripHtml = (value) => {
    return String(value || '').replace(/<[^>]+>/g, '').replace(/&nbsp;/g, ' ').trim()
  }

  const formatDate = (value) => {
    if (!value) return ''
    const date = String(value).replace('T', ' ')
    return date.length > 10 ? date.slice(5, 16) : date
  }

  const normalizeList = (data) => {
    if (Array.isArray(data)) return data
    return data?.records || data?.rows || data?.list || []
  }

  // 首屏加载/失败状态(allSettled 不会 reject,失败需按各请求结果判断)
  const homeLoading = ref(false)
  const homeLoadFailed = ref(false)

  const loadHomeData = async () => {
    homeLoading.value = true
    homeLoadFailed.value = false
    const [statsResult, messageResult, noticeResult, momentResult, chatUnreadResult, ntfResult, ntfUnreadResult] = await Promise.allSettled([
      getHomeStats(),
      getHomeMessages(),
      getNoticeList({ status: 1 }),
      getMomentMessages(),
      getUnreadTotal(),
      getNotificationTop(10),
      getNotificationUnreadCount()
    ])
    const results = [statsResult, messageResult, noticeResult, momentResult, chatUnreadResult, ntfResult, ntfUnreadResult]
    if (statsResult.status === 'fulfilled') {
      stats.value = {
        ...stats.value,
        ...(statsResult.value.data || {})
      }
    }
    if (noticeResult.status === 'fulfilled') {
      notices.value = normalizeList(noticeResult.value.data)
    }
    if (messageResult.status === 'fulfilled') {
      backendMessages.value = normalizeList(messageResult.value.data)
    }
    if (momentResult.status === 'fulfilled') {
      momentSummary.value = { ...momentSummary.value, ...(momentResult.value.data || {}) }
    }
    if (chatUnreadResult.status === 'fulfilled') {
      // 聊天未读总数同步到 vuex,供首页快捷入口与 tabbar 角标使用
      store.commit('SET_UNREAD_BADGE', { chatUnread: Number(chatUnreadResult.value.data || 0) })
    }
    if (ntfResult.status === 'fulfilled') {
      notifications.value = normalizeList(ntfResult.value.data)
    }
    if (ntfUnreadResult.status === 'fulfilled') {
      notificationUnread.value = Number(ntfUnreadResult.value.data?.count || 0)
    }
    // 全部请求都失败时给出失败提示与重试入口(部分失败仍正常展示)
    homeLoadFailed.value = results.every((result) => result.status === 'rejected')
    if (homeLoadFailed.value) {
      uni.showToast({ icon: 'none', title: '首页数据加载失败' })
    }
    homeLoading.value = false
  }

  // 跳转方法
  const tn = (e) => {
    if (!e) return
    uni.navigateTo({
      url: e,
    })
  }

  // 首次挂载加载数据,后续由父页面切换/下拉时刷新
  onMounted(() => {
    loadHomeData()
  })

  // 供 pages/index.vue 调用:刷新
  defineExpose({
    refresh: loadHomeData
  })
</script>

<style lang="scss" scoped>
  .template-home {
    min-height: 100vh;
    background-color: #F8F7F8;
  }

  .page-inner {
    padding: 0 24rpx;
  }

  /* 搜索入口 */
  .search-card {
    margin-top: 20rpx;
    min-height: 84rpx;
    padding: 0 28rpx;
    background: #ffffff;
    border-radius: 16rpx;
    border: 1rpx solid #EEF0F4;
    display: flex;
    align-items: center;
  }

  .search-text {
    flex: 1;
    margin-left: 14rpx;
    font-size: 26rpx;
    color: #9aa4b2;
  }

  /* 分组标题 */
  .section-title {
    margin: 28rpx 4rpx 16rpx;
    font-size: 28rpx;
    font-weight: 600;
    color: #1d2541;
    display: flex;
    align-items: center;
  }

  .section-title::before {
    content: "";
    width: 6rpx;
    height: 24rpx;
    border-radius: 3rpx;
    background: #3668FC;
    margin-right: 12rpx;
  }

  /* 快捷入口 */
  .quick-grid {
    background: #ffffff;
    border-radius: 16rpx;
    border: 1rpx solid #EEF0F4;
    display: grid;
    grid-template-columns: repeat(5, minmax(0, 1fr));
    padding: 28rpx 10rpx;
  }

  .quick-item {
    display: flex;
    flex-direction: column;
    align-items: center;
  }

  .quick-chip {
    width: 84rpx;
    height: 84rpx;
    border-radius: 20rpx;
    display: flex;
    align-items: center;
    justify-content: center;
  }

  .quick-label {
    margin-top: 12rpx;
    font-size: 24rpx;
    color: #425066;
  }

  /* 消息动态 */
  .msg-card {
    background: #ffffff;
    border-radius: 16rpx;
    border: 1rpx solid #EEF0F4;
    padding: 8rpx 0;
  }

  .msg-row {
    display: flex;
    align-items: center;
    padding: 24rpx 28rpx;
    border-bottom: 1rpx solid #F3F2F7;
  }

  .msg-row:last-child {
    border-bottom: none;
  }

  .msg-chip {
    flex-shrink: 0;
    width: 76rpx;
    height: 76rpx;
    border-radius: 20rpx;
    display: flex;
    align-items: center;
    justify-content: center;
  }

  .msg-main {
    flex: 1;
    min-width: 0;
    margin-left: 20rpx;
  }

  .msg-head {
    display: flex;
    align-items: center;
    justify-content: space-between;
  }

  .msg-title {
    flex: 1;
    min-width: 0;
    font-size: 30rpx;
    font-weight: 700;
    color: #1d2541;
  }

  .msg-time {
    flex-shrink: 0;
    margin-left: 16rpx;
    font-size: 22rpx;
    color: #9aa4b2;
  }

  .msg-desc {
    display: block;
    margin-top: 8rpx;
    font-size: 24rpx;
    color: #657189;
  }

  .msg-arrow {
    flex-shrink: 0;
    margin-left: 12rpx;
    font-size: 24rpx;
  }

  .msg-empty {
    padding: 40rpx 0;
    text-align: center;
    color: #9aa4b2;
    font-size: 26rpx;
  }

  .msg-retry {
    margin: 0 auto 40rpx;
    padding: 12rpx 48rpx;
    border-radius: 999rpx;
    color: #3668fc;
    font-size: 25rpx;
    font-weight: 600;
    background: rgba(54, 104, 252, 0.1);
  }

  .clamp-1 {
    display: -webkit-box;
    -webkit-box-orient: vertical;
    -webkit-line-clamp: 1;
    overflow: hidden;
  }

  .tn-tabbar-height {
    min-height: 120rpx;
    height: calc(140rpx + env(safe-area-inset-bottom));
    height: calc(140rpx + constant(safe-area-inset-bottom));
  }
</style>
