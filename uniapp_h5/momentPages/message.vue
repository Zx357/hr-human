<template>
  <view class="oa-content">
    <!-- 顶部自定义导航 -->
    <tn-navbar fixed :bottom-shadow="false" bg-color="#FFFFFF" :placeholder="false">
      <template #back><view class='tn-custom-nav-bar__back'
        @click="goBack">
        <tn-icon class='icon' name="left-arrow"></tn-icon>
      </view></template>
      <view class="tn-flex tn-flex-col-center tn-flex-row-center tn-padding-left">
        <text class="tn-text-bold tn-text-xl tn-color-black">消息互动</text>
      </view>
    </tn-navbar>

    <view class="" :style="{paddingTop: vuex_custom_bar_height + 'px'}">
      <!-- 互动统计 -->
      <view class="tn-margin-top tn-bg-white">
        <view
          v-for="(item, index) in summaryList"
          :key="index"
          class="summary-item tn-flex tn-flex-row-between tn-flex-col-center tn-padding tn-margin-left tn-margin-right"
          :class="{ 'tn-border-solid-bottom': index !== summaryList.length - 1 }"
        >
          <view class="tn-flex tn-flex-col-center">
            <view class="summary-icon tn-flex tn-flex-row-center tn-flex-col-center" :style="{ backgroundColor: item.bgColor }">
              <tn-icon :name="item.icon" class="tn-color-white tn-text-lg"></tn-icon>
            </view>
            <text class="tn-padding-left-sm tn-text-lg">{{ item.name }}</text>
          </view>
          <view class="tn-flex tn-flex-col-center">
            <text class="tn-text-lg tn-text-bold" :class="item.count > 0 ? 'tn-color-red' : 'tn-color-gray'">{{ item.count }}</text>
            <tn-icon class="tn-color-gray tn-padding-left-xs" name="right"></tn-icon>
          </view>
        </view>
      </view>

      <!-- 互动消息列表 -->
      <view class="tn-margin-top tn-bg-white" v-if="messages.length">
        <view
          v-for="(item, index) in messages"
          :key="item.id || index"
          class="msg-item tn-flex tn-flex-col-top tn-padding tn-margin-left tn-margin-right"
          :class="{ 'tn-border-solid-bottom': index !== messages.length - 1 }"
          @click="goPostDetail(item)"
        >
          <tn-avatar class="msg-item__avatar" shape="circle" :src="item.avatar" size="md"></tn-avatar>
          <view class="tn-flex-1 tn-padding-left-sm" style="min-width: 0;">
            <view class="tn-text-df msg-item__title tn-text-ellipsis">{{ item.title }}</view>
            <view v-if="item.postContent" class="tn-color-gray tn-text-sm msg-item__desc tn-text-ellipsis">{{ item.postContent }}</view>
          </view>
          <view class="msg-item__right tn-color-gray--disabled tn-text-xs tn-padding-left-sm">
            <text>{{ item.time }}</text>
            <tn-icon class="tn-color-gray tn-padding-top-xs" name="right"></tn-icon>
          </view>
        </view>
        <!-- 接口一次最多拉 50 条,返回不足时说明已全部加载 -->
        <view v-if="allLoaded" class="tn-color-gray--disabled tn-text-center tn-text-sm tn-padding">已加载全部</view>
      </view>

      <view v-if="!messages.length" class="tn-padding-xl">
        <view class="tn-text-center" style="font-size: 180rpx;padding-top: 60rpx;">
          <text class="tn-icon-clip tn-color-gray--light"></text>
        </view>
        <view class="tn-color-gray--disabled tn-text-center tn-text-lg">暂无互动消息</view>
      </view>
    </view>

    <view class='tn-tabbar-height'></view>

  </view>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { onPullDownRefresh } from '@dcloudio/uni-app'
import { useStore } from 'vuex'
import { useCustomBarHeight, useGoBack } from '@/libs/composables'
import config from '@/config'
import { getMomentMessages, getMomentMessageList, markMomentMessagesRead } from '@/api/moment'

// 使用 composable 获取自定义导航栏高度
const { vuex_custom_bar_height } = useCustomBarHeight()
const { goBack } = useGoBack()
const store = useStore()

defineOptions({
  name: 'TemplateMessage'
})

const summary = ref({
  unreadCount: 0,
  likeCount: 0,
  commentCount: 0,
  mentionCount: 0
})

const messages = ref([])
// 互动消息一次最多拉 50 条;返回条数不足时标记已全部加载,列表底部提示
const MESSAGE_LIMIT = 50
const allLoaded = ref(false)

const summaryList = computed(() => [
  { name: '收到的赞', icon: 'like-fill', bgColor: '#FB6A67', count: Number(summary.value.likeCount || 0) },
  { name: '收到的评论', icon: 'comment-fill', bgColor: '#4B98FE', count: Number(summary.value.commentCount || 0) },
  { name: '提到的我', icon: 'at-sign', bgColor: '#FFAC00', count: Number(summary.value.mentionCount || 0) },
  { name: '未读消息', icon: 'email-fill', bgColor: '#00D05E', count: Number(summary.value.unreadCount || 0) }
])

const formatAvatar = (avatar) => {
  if (!avatar) return '/static/author.jpg'
  if (/^https?:\/\//.test(avatar) || avatar.startsWith('/static')) return avatar
  return config.baseUrl + avatar
}

const formatTime = (value) => {
  if (!value) return ''
  const date = String(value).replace('T', ' ')
  return date.length > 10 ? date.slice(5, 16) : date
}

const normalizeMessage = (item, index) => {
  const type = item.type === 'comment' ? 'comment' : 'like'
  const operatorName = item.operatorName || '同事'
  return {
    id: item.id || `msg-${index}`,
    type,
    postId: item.postId,
    avatar: formatAvatar(item.operatorAvatar),
    title: type === 'like'
      ? `${operatorName} 赞了你的动态`
      : `${operatorName} 评论了你的动态：${item.content || ''}`,
    postContent: item.postContent || '',
    time: formatTime(item.time)
  }
}

const loadSummary = async () => {
  try {
    const res = await getMomentMessages()
    if (res.data) {
      summary.value = { ...summary.value, ...res.data }
    }
  } catch (error) {
    uni.showToast({ title: '加载失败', icon: 'none' })
  }
}

const loadMessages = async () => {
  try {
    const res = await getMomentMessageList(MESSAGE_LIMIT)
    const list = Array.isArray(res.data) ? res.data : []
    messages.value = list.map(normalizeMessage)
    allLoaded.value = list.length < MESSAGE_LIMIT
  } catch (error) {
    uni.showToast({ title: '加载失败', icon: 'none' })
  }
}

// 下拉刷新
onPullDownRefresh(async () => {
  try {
    await Promise.all([loadSummary(), loadMessages()])
  } finally {
    uni.stopPullDownRefresh()
  }
})

// 进入页面即标记已读,并同步清零 tabbar 时光角标与本地摘要未读数
const markRead = async () => {
  try {
    await markMomentMessagesRead()
    // 本地同步清零,避免显示旧的未读数
    summary.value = { ...summary.value, unreadCount: 0 }
    store.commit('SET_UNREAD_BADGE', { momentUnread: 0 })
  } catch (error) {
  }
}

// 点击消息跳转对应动态详情
const goPostDetail = (item) => {
  if (!item || !item.postId) return
  uni.navigateTo({
    url: `/momentPages/details?id=${item.postId}`
  })
}

onMounted(async () => {
  // 先加载摘要,再标记已读;markRead 成功后本地清零,避免并发请求把旧未读数显示回来
  await loadSummary()
  markRead()
  loadMessages()
})
</script>

<style lang="scss" scoped>
  /* 胶囊*/
  .tn-custom-nav-bar__back {
    width: 60%;
    height: 100%;
    position: relative;
    display: flex;
    justify-content: space-evenly;
    align-items: center;
    box-sizing: border-box;
    background-color: rgba(0, 0, 0, 0.15);
    border-radius: 1000rpx;
    border: 1rpx solid rgba(255, 255, 255, 0.5);
    color: #FFFFFF;
    font-size: 18px;

    .icon {
      display: block;
      flex: 1;
      margin: auto;
      text-align: center;
    }

  }

  .oa-content{
    max-width: 640px;
    margin: 0 auto;
    background-color: #F8F7F8;
    min-height: 100vh;
    padding-bottom: 60rpx;
    padding-bottom: calc(80rpx + env(safe-area-inset-bottom) / 2);
    padding-bottom: calc(80rpx + constant(safe-area-inset-bottom));
  }

  .tn-tabbar-height {
  	min-height: 100rpx;
  	height: calc(120rpx + env(safe-area-inset-bottom) / 2);
  }

  .summary-item {
    background-color: #FFFFFF;
  }

  .summary-icon {
    width: 64rpx;
    height: 64rpx;
    border-radius: 16rpx;
  }

  .tn-border-solid-bottom {
    border-bottom: 1rpx solid #F3F2F7;
  }

  /* 互动消息列表 start */
  .msg-item {
    background-color: #FFFFFF;

    &__avatar {
      flex-shrink: 0;
    }

    &__title {
      font-weight: 600;
      color: #1D2541;
    }

    &__desc {
      padding-top: 8rpx;
    }

    &__right {
      flex-shrink: 0;
      display: flex;
      flex-direction: column;
      align-items: flex-end;
    }
  }
</style>
