<template>
  <view class="oa-content">
    <!-- 顶部自定义导航 -->
    <tn-navbar fixed :bottom-shadow="false" bg-color="#FFFFFF" :placeholder="false">
      <template #back><view class='tn-custom-nav-bar__back'
        @click="goBack">
        <tn-icon class='icon' name="left-arrow"></tn-icon>
      </view></template>
      <view class="tn-flex tn-flex-col-center tn-flex-row-center tn-padding-left">
        <text class="tn-text-bold tn-text-xl tn-color-black">沟通交流</text>
      </view>
    </tn-navbar>

    <view class="oa-backgroup" :style="{paddingTop: vuex_custom_bar_height + 'px'}">

      <!-- 会话列表 -->
      <view v-if="conversations.length" class="tn-bg-white">
        <view
          v-for="(item, index) in conversations"
          :key="item.key"
          class="conv-item tn-flex tn-flex-col-center"
          :class="{ 'conv-item--border': index !== conversations.length - 1 }"
          @click="goChat(item)"
        >
          <!-- 头像:单聊显示对方头像,群聊无头像时显示默认群图标 -->
          <view class="conv-item__avatar-box">
            <image v-if="item.avatar" class="conv-item__avatar" :src="item.avatar" mode="aspectFill"></image>
            <view v-else class="conv-item__avatar conv-item__avatar--group tn-flex tn-flex-row-center tn-flex-col-center">
              <tn-icon name="group-circle" class="tn-color-white" style="font-size: 48rpx;"></tn-icon>
            </view>
            <view v-if="item.unreadCount > 0" class="conv-item__badge">{{ formatBadge(item.unreadCount) }}</view>
          </view>

          <view class="tn-flex-1 tn-padding-left-sm" style="min-width: 0;">
            <view class="tn-flex tn-flex-row-between tn-flex-col-center">
              <text class="conv-item__name tn-text-ellipsis">{{ item.name }}</text>
              <text class="conv-item__time tn-color-gray--disabled tn-text-xs">{{ item.timeText }}</text>
            </view>
            <view class="tn-flex tn-flex-row-between tn-flex-col-center tn-padding-top-xs">
              <text class="conv-item__last tn-color-gray tn-text-sm tn-text-ellipsis">{{ item.lastMessage || ' ' }}</text>
              <text v-if="item.typeText" class="conv-item__type tn-color-gray--disabled tn-text-xs">{{ item.typeText }}</text>
            </view>
          </view>
        </view>
      </view>

      <!-- 空会话 -->
      <view v-else-if="!loading" class="tn-padding-xl">
        <view class="tn-text-center" style="font-size: 180rpx;padding-top: 60rpx;">
          <text class="tn-icon-clip tn-color-gray--light"></text>
        </view>
        <view class="tn-color-gray--disabled tn-text-center tn-text-lg">暂无会话，去通讯录发起聊天吧</view>
        <view class="tn-flex tn-flex-row-center tn-padding-top">
          <tn-button
            shape="round"
            bg-color="#3668FC"
            text-color="#FFFFFF"
            :custom-style="{ padding: '18rpx 60rpx' }"
            @click="goContacts"
          >
            去通讯录
          </tn-button>
        </view>
      </view>

      <!-- 加载中 -->
      <view v-if="loading" class="tn-text-center tn-color-gray tn-padding-xl">加载中...</view>
    </view>

    <view class='tn-tabbar-height'></view>

  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onShow, onPullDownRefresh } from '@dcloudio/uni-app'
import { useCustomBarHeight, useGoBack } from '@/libs/composables'
import config from '@/config'
import { getConversations } from '@/api/chat'

// 使用 composable 获取自定义导航栏高度
const { vuex_custom_bar_height } = useCustomBarHeight()
const { goBack } = useGoBack()

defineOptions({
  name: 'TemplateChat'
})

const conversations = ref([])
const loading = ref(true)

const formatAvatar = (avatar) => {
  if (!avatar) return ''
  if (/^https?:\/\//.test(avatar) || avatar.startsWith('/static')) return avatar
  return config.baseUrl + avatar
}

const formatBadge = (value) => {
  const count = Number(value || 0)
  if (count <= 0) return ''
  return count > 99 ? '99+' : String(count)
}

// 时间:今天显示 HH:mm,更早显示 MM-DD
const formatTime = (value) => {
  if (!value) return ''
  const date = String(value).replace('T', ' ')
  const now = new Date()
  const ymd = date.slice(0, 10)
  const today = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}-${String(now.getDate()).padStart(2, '0')}`
  if (ymd === today) return date.length > 15 ? date.slice(11, 16) : ''
  return date.length >= 10 ? date.slice(5, 10) : date
}

// 最后一条是图片消息时显示占位文案(后端 lastMessage 存原文 URL,前端按 URL 特征判断)
const IMAGE_EXT_RE = /\.(png|jpe?g|gif|webp|bmp)(\?.*)?$/i
const formatLastMessage = (value) => {
  const text = String(value || '').trim()
  if (text && /^(\/file\/upload\/image|https?:\/\/)/i.test(text) && IMAGE_EXT_RE.test(text)) {
    return '[图片]'
  }
  return text
}

const normalizeConversation = (item) => {
  const chatType = Number(item.chatType) === 2 ? 2 : 1
  return {
    key: `${chatType}-${item.targetId}`,
    chatType,
    targetId: item.targetId,
    name: item.name || (chatType === 2 ? '单聊' : '群聊'),
    avatar: formatAvatar(item.avatar) || (chatType === 2 ? '/static/author.jpg' : ''),
    lastMessage: formatLastMessage(item.lastMessage),
    unreadCount: Number(item.unreadCount || 0),
    timeText: formatTime(item.lastMessageTime),
    typeText: chatType === 2 ? '' : '群聊'
  }
}

const loadConversations = async () => {
  try {
    const res = await getConversations()
    const list = Array.isArray(res.data) ? res.data : []
    conversations.value = list.map(normalizeConversation)
  } catch (error) {
    uni.showToast({ title: '加载失败', icon: 'none' })
  } finally {
    loading.value = false
  }
}

// 点击会话进入聊天页(partnerPages/chat 接收 type=single|group / targetId / name)
const goChat = (item) => {
  uni.navigateTo({
    url: `/partnerPages/chat?type=${item.chatType === 2 ? 'single' : 'group'}&targetId=${item.targetId}&name=${encodeURIComponent(item.name)}`
  })
}

// 去通讯录(首页第4个tab):reLaunch 关闭所有已开页面后切到主页通讯录 tab,避免页面栈叠加
const goContacts = () => {
  uni.reLaunch({
    url: '/pages/index?index=3'
  })
}

// 页面显示时刷新会话(从聊天页返回后未读数/最后消息保持最新)
onShow(() => {
  loadConversations()
})

onPullDownRefresh(async () => {
  await loadConversations()
  uni.stopPullDownRefresh()
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

  .oa-backgroup{
    background-color: #F8F7F8;
    min-height: 100vh;
    padding-bottom: 60rpx;
  }

  .tn-tabbar-height {
  	min-height: 100rpx;
  	height: calc(120rpx + env(safe-area-inset-bottom) / 2);
  }

  /* 会话列表 start */
  .conv-item {
    padding: 24rpx 30rpx;
    background-color: #FFFFFF;

    &--border {
      border-bottom: 1rpx solid #F3F2F7;
    }

    &__avatar-box {
      position: relative;
      flex-shrink: 0;
      width: 92rpx;
      height: 92rpx;
    }

    &__avatar {
      width: 92rpx;
      height: 92rpx;
      border-radius: 16rpx;
      background-color: #F4F5F9;

      &--group {
        background-color: #3668FC;
      }
    }

    &__badge {
      position: absolute;
      top: -10rpx;
      right: -14rpx;
      min-width: 32rpx;
      height: 32rpx;
      padding: 0 8rpx;
      box-sizing: border-box;
      background-color: #E34D59;
      border: 2rpx solid #FFFFFF;
      border-radius: 100rpx;
      color: #FFFFFF;
      font-size: 20rpx;
      line-height: 28rpx;
      text-align: center;
    }

    &__name {
      flex: 1;
      min-width: 0;
      font-size: 30rpx;
      font-weight: 600;
      color: #1D2541;
    }

    &__time {
      flex-shrink: 0;
      padding-left: 16rpx;
    }

    &__last {
      flex: 1;
      min-width: 0;
    }

    &__type {
      flex-shrink: 0;
      padding-left: 16rpx;
    }
  }
</style>
