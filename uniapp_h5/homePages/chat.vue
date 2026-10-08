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
      <view v-if="conversations.length" class="conv-list tn-bg-white">
        <view
          v-for="(item, index) in conversations"
          :key="item.key"
          class="conv-item tn-flex tn-flex-col-center"
          :class="{ 'conv-item--border': index !== conversations.length - 1 }"
          @click="goChat(item)"
          @longpress="onConversationLongPress(item)"
        >
          <!-- 头像:单聊显示对方头像,群聊无头像时显示默认群图标 -->
          <view class="conv-item__avatar-box">
            <image v-if="item.avatar" class="conv-item__avatar" :src="item.avatar" mode="aspectFill"></image>
            <view v-else class="conv-item__avatar conv-item__avatar--group tn-flex tn-flex-row-center tn-flex-col-center">
              <tn-icon name="group-circle" class="tn-color-white" style="font-size: 48rpx;"></tn-icon>
            </view>
            <view v-if="item.unreadCount > 0" class="conv-item__badge">{{ formatBadge(item.unreadCount) }}</view>
          </view>

          <view class="tn-flex-1 tn-padding-left-sm" style="min-width: 0; flex-direction: column;">
            <view class="tn-flex tn-flex-row-between tn-flex-col-center">
              <view class="conv-item__name-wrap">
                <view v-if="item.sticky" class="conv-item__sticky">置顶</view>
                <text class="conv-item__name tn-text-ellipsis">{{ item.name }}</text>
              </view>
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
import { onShow, onHide, onUnload, onPullDownRefresh } from '@dcloudio/uni-app'
import { useStore } from 'vuex'
import { useCustomBarHeight, useGoBack } from '@/libs/composables'
import config from '@/config'
import { getConversations, updateConversationSettings, hideConversation, chatTypeToTargetType } from '@/api/chat'
import { connect as connectWs, on as onWsMessage, isOpen as wsIsOpen } from '@/utils/websocket'
import { toastRequestError } from '@/utils/common'

// 使用 composable 获取自定义导航栏高度
const { vuex_custom_bar_height } = useCustomBarHeight()
const { goBack } = useGoBack()
const store = useStore()

defineOptions({
  name: 'TemplateChat'
})

const conversations = ref([])
const loading = ref(true)

// WS 订阅注销器(onShow 注册,onHide/onUnload 注销;仅取消订阅,不断开全局单例)
let offWsChatMessage = null
let offWsUnreadTotal = null

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
    // 会话无名时:单聊兜底"同事",群聊兜底"群聊"
    name: item.name || (chatType === 2 ? '同事' : '群聊'),
    avatar: formatAvatar(item.avatar) || (chatType === 2 ? '/static/author.jpg' : ''),
    lastMessage: formatLastMessage(item.lastMessage),
    unreadCount: Number(item.unreadCount || 0),
    sticky: Number(item.sticky || 0),
    timeText: formatTime(item.lastMessageTime),
    typeText: chatType === 2 ? '' : '群聊'
  }
}

const loadConversations = async (silent = false) => {
  try {
    const res = await getConversations()
    const list = Array.isArray(res.data) ? res.data : []
    // 列表渲染按后端返回顺序(置顶优先),前端不重排
    conversations.value = list.map(normalizeConversation)
  } catch (error) {
    // 推送触发的静默刷新不打扰用户,仅用户手动进入/下拉时提示(request.js 已统一 toast,不重复提示)
    if (!silent) {
      toastRequestError(error, '加载失败')
    }
  } finally {
    loading.value = false
  }
}

// ===== 会话管理:长按操作(置顶/取消置顶、删除会话) =====

function onConversationLongPress(item) {
  if (!item?.targetId) return
  const sticky = Number(item.sticky) === 1
  uni.showActionSheet({
    itemList: [sticky ? '取消置顶' : '置顶', '删除该会话'],
    success: (res) => {
      if (res.tapIndex === 0) {
        toggleSticky(item)
      } else if (res.tapIndex === 1) {
        confirmRemoveConversation(item)
      }
    }
  })
}

// 置顶/取消置顶:调会话设置契约接口后本地更新并重排(置顶优先)
async function toggleSticky(item) {
  const nextSticky = Number(item.sticky) === 1 ? 0 : 1
  try {
    await updateConversationSettings({
      targetType: chatTypeToTargetType(item.chatType),
      targetId: item.targetId,
      sticky: nextSticky
    })
    item.sticky = nextSticky
    conversations.value = [...conversations.value].sort((a, b) => Number(b.sticky || 0) - Number(a.sticky || 0))
    uni.showToast({ icon: 'none', title: nextSticky === 1 ? '已置顶' : '已取消置顶' })
  } catch (error) {
    toastRequestError(error, '操作失败')
  }
}

// 删除(隐藏)会话:调契约接口后本地移除
function confirmRemoveConversation(item) {
  uni.showModal({
    title: '删除会话',
    content: `删除与「${item.name}」的会话吗？删除后将不再显示。`,
    confirmColor: '#FB6A67',
    success: async (res) => {
      if (!res.confirm) return
      try {
        await hideConversation(chatTypeToTargetType(item.chatType), item.targetId)
        conversations.value = conversations.value.filter((conv) => conv.key !== item.key)
        uni.showToast({ icon: 'none', title: '会话已删除' })
      } catch (error) {
        toastRequestError(error, '删除失败')
      }
    }
  })
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

// ===== WebSocket 实时刷新 =====

// 新消息推送:静默刷新会话列表(最后消息/未读数/排序)
const handleWsChatMessage = () => {
  loadConversations(true)
}

// 未读总数推送:直接更新 tabbar 角标(免一次 REST 查询)
const handleWsUnreadTotal = (data) => {
  if (!data) return
  store.commit('SET_UNREAD_BADGE', { chatUnread: Number(data.total || 0) })
}

const registerWsHandlers = () => {
  if (!offWsChatMessage) {
    offWsChatMessage = onWsMessage('chat_message', handleWsChatMessage)
  }
  if (!offWsUnreadTotal) {
    offWsUnreadTotal = onWsMessage('unread_total', handleWsUnreadTotal)
  }
}

const unregisterWsHandlers = () => {
  if (offWsChatMessage) {
    offWsChatMessage()
    offWsChatMessage = null
  }
  if (offWsUnreadTotal) {
    offWsUnreadTotal()
    offWsUnreadTotal = null
  }
}

// ===== 轮询兜底:WS 未连接时每 10 秒拉一次会话列表,连上 WS 自动停止 =====

let pollTimer = null

const stopPolling = () => {
  if (pollTimer) {
    clearInterval(pollTimer)
    pollTimer = null
  }
}

const startPolling = () => {
  stopPolling()
  pollTimer = setInterval(() => {
    // WS 在线时停止轮询(连接推送已覆盖),断开/重连中自动回退轮询兜底
    if (wsIsOpen()) return
    loadConversations(true)
  }, 10000)
}

// 页面显示时刷新会话(从聊天页返回后未读数/最后消息保持最新),并接入全局 WS 推送
onShow(() => {
  loadConversations()
  registerWsHandlers()
  // 建立/恢复全局 WS 单例连接(幂等;连接由登录/启动时建立,此处兜底重试)
  connectWs()
  // WS 断开期间的轮询兜底(连上 WS 后空转,不再请求)
  startPolling()
})

onHide(() => {
  // 仅注销订阅与轮询,不断开全局 WS 单例(推送仍会更新角标)
  unregisterWsHandlers()
  stopPolling()
})

onUnload(() => {
  unregisterWsHandlers()
  stopPolling()
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

    &__name-wrap {
      flex: 1;
      min-width: 0;
      display: flex;
      align-items: center;
      gap: 10rpx;
    }

    &__sticky {
      flex-shrink: 0;
      padding: 2rpx 14rpx;
      border-radius: 999rpx;
      background: rgba(255, 172, 0, 0.14);
      color: #FF9F2E;
      font-size: 20rpx;
      font-weight: 600;
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
