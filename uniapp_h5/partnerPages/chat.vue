<template>
  <view class="chat-page">
    <!-- 顶部自定义导航 -->
    <tn-navbar fixed home-icon="" :placeholder="false" :bottom-shadow="false" bg-color="#FFFFFF">
      <template #back>
        <view class='tn-custom-nav-bar__back' @click="goBack">
          <tn-icon class='icon' name='left-arrow'></tn-icon>
        </view>
      </template>
      <view class="tn-flex tn-flex-col-center tn-flex-row-center ">
        <text class="tn-text-bold tn-text-xl tn-color-black">{{ peerName }}</text>
      </view>
    </tn-navbar>

    <!-- 消息列表 -->
    <scroll-view
      scroll-y
      class="chat-body"
      :style="{ paddingTop: vuex_custom_bar_height + 'px' }"
      :scroll-into-view="scrollInto"
      scroll-anchoring
      :refresher-enabled="!historyDone"
      refresher-default-style="black"
      :refresher-triggered="refreshing"
      @refresherrefresh="loadOlder"
    >
      <view v-if="historyDone && messages.length" class="history-end tn-text-center tn-color-gray tn-text-sm tn-padding-sm">
        没有更多了
      </view>
      <view v-if="loadingHistory" class="tn-text-center tn-color-gray tn-padding-sm">加载历史消息...</view>
      <view v-if="loading" class="tn-text-center tn-color-gray tn-padding">加载中...</view>
      <view v-else-if="!messages.length" class="tn-text-center tn-color-gray--disabled tn-padding-xl">
        暂无消息，发送第一条消息吧
      </view>

      <template v-for="(item, index) in messages" :key="item.id || 'tmp-' + index">
        <view
          v-if="shouldShowTime(index)"
          class="time-chip tn-text-center tn-color-gray tn-text-sm"
        >
          {{ timeChipText(item) }}
        </view>
        <view
          :id="'msg-' + (item.id || index)"
          class="chat-msg tn-padding-left tn-padding-right"
          :class="{ 'chat-msg--mine': isMine(item) }"
        >
          <image class="chat-msg__avatar" :src="formatAvatar(item.fromAvatar)" mode="aspectFill" />
          <view class="chat-msg__main">
            <view class="chat-msg__name tn-color-gray tn-text-xs">{{ item.fromName || '成员' }}</view>
            <!-- 图片消息:点击预览,不用文本气泡样式 -->
            <image
              v-if="Number(item.msgType) === 2"
              class="chat-msg__image"
              :src="formatImageUrl(item.content)"
              mode="widthFix"
              @click="previewImage(item)"
            />
            <view v-else class="chat-msg__bubble">{{ item.content }}</view>
          </view>
        </view>
      </template>
      <view class="chat-body__bottom"></view>
    </scroll-view>

    <!-- 输入区 -->
    <view class="chat-input tn-bg-white tn-flex tn-flex-col-center tn-padding-sm tn-padding-left tn-padding-right">
      <view class="chat-input__plus tn-flex tn-flex-row-center tn-flex-col-center" @click="chooseAndSendImage">
        <tn-icon name="camera-fill" class="tn-color-gray tn-text-lg"></tn-icon>
      </view>
      <input
        v-model="draft"
        class="chat-input__field"
        placeholder="说点什么…"
        placeholder-style="color:#AAAAAA"
        confirm-type="send"
        :adjust-position="true"
        @confirm="send"
      />
      <tn-button
        shape="round"
        bg-color="#3668FC"
        text-color="#FFFFFF"
        :custom-style="{ padding: '14rpx 40rpx' }"
        :disabled="sending"
        @click="send"
      >
        发送
      </tn-button>
    </view>
  </view>
</template>

<script setup>
import { computed, nextTick, onBeforeUnmount, ref } from 'vue'
import { onLoad, onShow, onHide, onUnload } from '@dcloudio/uni-app'
import { useStore } from 'vuex'
import { useCustomBarHeight, useGoBack } from '@/libs/composables'
import config from '@/config'
import { getChatMessages, sendChatMessage, markConversationRead } from '@/api/chat'
import { refreshChatUnreadBadge } from '@/utils/chat-badge'
import { uploadImageToServer } from '@/utils/upload'
import {
  connectChatSocket,
  closeChatSocket,
  isChatSocketOpen,
  onChatMessage
} from '@/utils/websocket'

const { vuex_custom_bar_height } = useCustomBarHeight()
const { goBack } = useGoBack()
const store = useStore()

const chatType = ref(1)
const targetId = ref(null)
const peerName = ref('聊天')
const messages = ref([])
const loading = ref(true)
const sending = ref(false)
const draft = ref('')
const scrollInto = ref('')
const loadingHistory = ref(false)
const historyDone = ref(false)
const refreshing = ref(false)
let pollTimer = null
// 轮询防重入:上一次静默请求未返回时跳过本轮
let silentLoading = false
// 连续加载失败计数(恢复成功后重置,连续失败 3 次提示一次网络异常)
let loadFailCount = 0
let loadFailToastShown = false

// ===== WebSocket 实时化 =====
// WS 消息处理函数注销器(onLoad 注册,onUnload/H5 卸载时注销)
let offWsChatMessage = null
let offWsUnreadTotal = null
// 拉取进行中收到推送时,延迟补一次增量拉取(防止推送消息被拉取结果覆盖)
let wsCatchUpTimer = null

// 历史分页大小
const HISTORY_PAGE_SIZE = 20

const myEmployeeId = computed(() => {
  const info = store.getters.employeeInfo || store.state.user?.employeeInfo || uni.getStorageSync('userInfo') || {}
  return store.getters.id || info.id || null
})

const isMine = (item) => {
  return String(item.fromEmployeeId) === String(myEmployeeId.value)
}

const formatAvatar = (avatar) => {
  if (!avatar) return '/static/author.jpg'
  if (/^https?:\/\//.test(avatar) || avatar.startsWith('/static')) return avatar
  return config.baseUrl + avatar
}

// 图片消息地址(相对路径补全域名)
const formatImageUrl = (url) => {
  if (!url) return ''
  if (/^(https?:\/\/|data:|blob:)/.test(url) || url.startsWith('/static')) return url
  return config.baseUrl + url
}

// 点击图片消息预览
const previewImage = (item) => {
  const url = formatImageUrl(item?.content)
  if (!url) return
  uni.previewImage({ current: url, urls: [url] })
}

const scrollToBottom = () => {
  nextTick(() => {
    const last = messages.value[messages.value.length - 1]
    scrollInto.value = last ? 'msg-' + (last.id || (messages.value.length - 1)) : ''
  })
}

// 当前最大消息 id
const maxMessageId = () => {
  return messages.value.reduce((max, item) => {
    const id = Number(item.id)
    return Number.isFinite(id) && id > max ? id : max
  }, 0)
}

// 按 id 去重后追加新消息
const appendMessages = (list) => {
  const seen = new Set(messages.value.map((item) => String(item.id)))
  const fresh = list.filter((item) => !seen.has(String(item.id)))
  if (fresh.length) {
    messages.value = messages.value.concat(fresh)
  }
  return fresh.length
}

// 当前最小消息 id
const minMessageId = () => {
  return messages.value.reduce((min, item) => {
    const id = Number(item.id)
    return Number.isFinite(id) && id < min ? id : min
  }, Number.MAX_SAFE_INTEGER)
}

// 顶部下拉加载更早的历史消息
const loadOlder = async () => {
  if (loadingHistory.value || historyDone.value || !messages.value.length || !targetId.value) {
    refreshing.value = false
    return
  }
  loadingHistory.value = true
  try {
    const res = await getChatMessages({
      chatType: chatType.value,
      targetId: targetId.value,
      beforeMessageId: minMessageId(),
      limit: HISTORY_PAGE_SIZE
    })
    const list = Array.isArray(res.data) ? res.data : []
    const seen = new Set(messages.value.map((item) => String(item.id)))
    const older = list.filter((item) => !seen.has(String(item.id)))
    if (list.length < HISTORY_PAGE_SIZE) historyDone.value = true
    if (older.length) {
      const firstId = messages.value[0] ? 'msg-' + messages.value[0].id : ''
      // 插入更早消息后滚回首条,保持可视位置
      messages.value = older.concat(messages.value)
      nextTick(() => {
        scrollInto.value = firstId
      })
    }
  } catch (error) {
    uni.showToast({ title: '加载历史消息失败', icon: 'none' })
  } finally {
    loadingHistory.value = false
    refreshing.value = false
  }
}

// 相邻消息间隔超过5分钟时显示时间条
const shouldShowTime = (index) => {
  if (index === 0) return true
  const prev = messages.value[index - 1]
  const cur = messages.value[index]
  const prevTime = new Date(String(prev.createdTime || '').replace(/-/g, '/')).getTime()
  const curTime = new Date(String(cur.createdTime || '').replace(/-/g, '/')).getTime()
  if (!Number.isFinite(prevTime) || !Number.isFinite(curTime)) return false
  return curTime - prevTime > 5 * 60 * 1000
}

// 时间条文案:当天只显示 HH:mm,更早显示 MM-DD HH:mm
const timeChipText = (item) => {
  const time = String(item.createdTime || '').replace('T', ' ')
  if (!time) return ''
  const date = new Date(time.replace(/-/g, '/'))
  if (!Number.isFinite(date.getTime())) return time
  const now = new Date()
  const sameDay = date.getFullYear() === now.getFullYear() && date.getMonth() === now.getMonth() && date.getDate() === now.getDate()
  const pad = (n) => String(n).padStart(2, '0')
  const hhmm = pad(date.getHours()) + ':' + pad(date.getMinutes())
  if (sameDay) return hhmm
  return pad(date.getMonth() + 1) + '-' + pad(date.getDate()) + ' ' + hhmm
}

const loadMessages = async (silent = false) => {
  if (!targetId.value) return
  // 轮询加固:上一次静默拉取未返回则跳过本轮,避免请求堆积
  if (silent && silentLoading) return
  if (!silent) {
    loading.value = true
    historyDone.value = false
  }
  if (silent) silentLoading = true
  try {
    // 首次进入拉最近一页;轮询时传当前最大消息 id,只拉增量
    const afterMessageId = silent && messages.value.length ? maxMessageId() : undefined
    const res = await getChatMessages({
      chatType: chatType.value,
      targetId: targetId.value,
      afterMessageId,
      limit: silent ? undefined : HISTORY_PAGE_SIZE
    })
    const list = Array.isArray(res.data) ? res.data : []
    // 拉取成功,重置连续失败计数
    loadFailCount = 0
    loadFailToastShown = false
    if (silent) {
      const added = appendMessages(list)
      if (added) {
        scrollToBottom()
        // 停留在聊天页期间拉到新消息,同步标记已读,避免未读角标不清零
        markConversationRead({ chatType: chatType.value, targetId: targetId.value }).catch(() => {})
      }
    } else {
      messages.value = list
      scrollToBottom()
    }
  } catch (error) {
    // 连续失败 3 次时提示一次网络异常(非阻断),恢复成功后重置
    loadFailCount += 1
    if (loadFailCount >= 3 && !loadFailToastShown) {
      loadFailToastShown = true
      uni.showToast({ icon: 'none', title: '网络异常，消息刷新失败' })
    }
  } finally {
    loading.value = false
    silentLoading = false
  }
}

// 发送消息(content 为文本内容或图片 URL,msgType 1-文本 2-图片)
const sendMessage = async ({ content, msgType = 1 }) => {
  if (!content || sending.value) return
  sending.value = true
  try {
    const res = await sendChatMessage({ chatType: chatType.value, targetId: targetId.value, content, msgType })
    if (res.data) {
      appendMessages([res.data])
      if (msgType === 1) draft.value = ''
      scrollToBottom()
    } else {
      // 发送成功但响应无消息体时,本地无法回显,提示用户重试,避免消息静默丢失
      uni.showToast({ title: '发送失败，请重试', icon: 'none' })
    }
  } catch (error) {
    uni.showToast({ icon: 'none', title: '发送失败，请重试' })
  } finally {
    sending.value = false
  }
}

const send = () => {
  const content = draft.value.trim()
  if (!content) return
  sendMessage({ content, msgType: 1 })
}

// 选择图片(拍照/相册)并作为图片消息发送
const chooseAndSendImage = () => {
  if (sending.value) return
  uni.chooseImage({
    count: 1,
    // 压缩后再上传,减小消息图片体积
    sizeType: ['compressed'],
    success: async (res) => {
      const path = res.tempFilePaths && res.tempFilePaths[0]
      if (!path) return
      try {
        uni.showLoading({ title: '发送中', mask: true })
        const url = await uploadImageToServer(path)
        uni.hideLoading()
        await sendMessage({ content: url, msgType: 2 })
      } catch (error) {
        uni.hideLoading()
        uni.showToast({ icon: 'none', title: '发送失败，请重试' })
      }
    }
  })
}

const stopPolling = () => {
  if (pollTimer) {
    clearInterval(pollTimer)
    pollTimer = null
  }
}

const startPolling = () => {
  stopPolling()
  pollTimer = setInterval(() => {
    // WS 在线时暂停轮询;断开/重连中/连接失败时自动回退 4 秒轮询兜底(不劣于纯轮询现状)
    if (isChatSocketOpen()) return
    loadMessages(true)
  }, 4000)
}

// ===== WebSocket 实时消息处理 =====

// 推送消息是否属于当前会话
const belongsToCurrentConversation = (msg) => {
  if (msg == null || targetId.value == null) return false
  if (Number(msg.chatType) !== Number(chatType.value)) return false
  if (Number(chatType.value) === 1) {
    return Number(msg.groupId) === Number(targetId.value)
  }
  // 单聊:对方发来的消息(常规)或对端ID指向当前会话(兜底)
  return Number(msg.fromEmployeeId) === Number(targetId.value) ||
    Number(msg.peerEmployeeId) === Number(targetId.value)
}

// 拉取进行中收到推送:延迟补一次增量拉取,防止推送消息被进行中的全量/增量结果覆盖
const scheduleWsCatchUp = () => {
  if (wsCatchUpTimer) return
  wsCatchUpTimer = setTimeout(() => {
    wsCatchUpTimer = null
    if (isChatSocketOpen() && !loading.value && !silentLoading) {
      loadMessages(true)
    }
  }, 800)
}

// 新消息推送:属于当前会话则复用现有去重/滚动/已读逻辑
const handleWsChatMessage = (data) => {
  const msg = data && data.message
  if (!msg) return
  if (loading.value || silentLoading) {
    scheduleWsCatchUp()
    return
  }
  if (!belongsToCurrentConversation(msg)) return
  const added = appendMessages([msg])
  if (added) {
    scrollToBottom()
    // 停留在聊天页收到新消息,同步标记已读,避免未读角标不清零
    markConversationRead({ chatType: chatType.value, targetId: targetId.value }).catch(() => {})
  }
}

// 未读总数推送:直接更新 tabbar 角标(免一次 REST 查询)
const handleWsUnreadTotal = (data) => {
  if (!data) return
  store.commit('SET_UNREAD_BADGE', { chatUnread: Number(data.total || 0) })
}

const registerWsHandlers = () => {
  if (!offWsChatMessage) {
    offWsChatMessage = onChatMessage('chat_message', handleWsChatMessage)
  }
  if (!offWsUnreadTotal) {
    offWsUnreadTotal = onChatMessage('unread_total', handleWsUnreadTotal)
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
  if (wsCatchUpTimer) {
    clearTimeout(wsCatchUpTimer)
    wsCatchUpTimer = null
  }
}

onLoad((options) => {
  // 只初始化参数,数据拉取统一由 onShow 处理,避免首屏双重全量请求
  chatType.value = options?.type === 'single' ? 2 : 1
  targetId.value = options?.targetId || null
  if (options?.name) {
    peerName.value = decodeURIComponent(options.name)
  }
  // 注册 WS 消息分发(token 为空时 connectChatSocket 不连接,自动走轮询)
  registerWsHandlers()
})

onShow(() => {
  // messages 为空走全量(带 loading),否则按最大消息 id 增量拉取
  loadMessages(messages.value.length > 0)
  // 标记会话已读,清掉会话列表未读数
  if (targetId.value) {
    markConversationRead({ chatType: chatType.value, targetId: targetId.value }).catch(() => {})
  }
  // 建立/恢复 WS 实时连接(幂等,token 为空自动跳过);在线时轮询空转,断开时轮询兜底
  connectChatSocket()
  // 轮询兜底刷新(先清理旧定时器,避免重复轮询)
  startPolling()
})

onHide(() => {
  stopPolling()
  closeChatSocket()
  // 离开聊天页(或切后台)时刷新全局未读数,让 tabbar 首页角标及时清零/更新
  refreshChatUnreadBadge()
})

onUnload(() => {
  stopPolling()
  closeChatSocket()
  unregisterWsHandlers()
  refreshChatUnreadBadge()
})

// #ifdef H5
// H5 端页面切到后台时暂停轮询(WS 保持连接,推送仍会送达),回到前台恢复
const handleVisibilityChange = () => {
  if (document.hidden) {
    stopPolling()
  } else {
    loadMessages(true)
    connectChatSocket()
    startPolling()
  }
}
if (typeof document !== 'undefined' && document.addEventListener) {
  document.addEventListener('visibilitychange', handleVisibilityChange)
}
onBeforeUnmount(() => {
  if (typeof document !== 'undefined' && document.removeEventListener) {
    document.removeEventListener('visibilitychange', handleVisibilityChange)
  }
  stopPolling()
  closeChatSocket()
  unregisterWsHandlers()
})
// #endif
</script>

<style lang="scss" scoped>
.chat-page {
  display: flex;
  flex-direction: column;
  height: 100vh;
  max-width: 640px;
  margin: 0 auto;
  background-color: #f8f7f8;
}

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
  color: #ffffff;
  font-size: 18px;

  .icon {
    display: block;
    flex: 1;
    margin: auto;
    text-align: center;
  }
}

.chat-body {
  flex: 1;
  box-sizing: border-box;
  height: 0;
}

.chat-msg {
  display: flex;
  align-items: flex-start;
  margin-top: 24rpx;

  &__avatar {
    width: 72rpx;
    height: 72rpx;
    border-radius: 12rpx;
    background-color: #f4f5f9;
    flex-shrink: 0;
  }

  &__main {
    margin-left: 16rpx;
    max-width: 70%;
  }

  &__name {
    max-width: 100%;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    padding: 0 4rpx 6rpx;
  }

  &__bubble {
    display: inline-block;
    padding: 16rpx 22rpx;
    background-color: #ffffff;
    border-radius: 4rpx 20rpx 20rpx 20rpx;
    font-size: 28rpx;
    color: #1d2541;
    line-height: 1.5;
    word-break: break-all;
  }

  &__image {
    display: block;
    width: 300rpx;
    max-width: 300rpx;
    border-radius: 12rpx;
    background-color: #f4f5f9;
  }
}

.chat-msg--mine {
  flex-direction: row-reverse;

  .chat-msg__main {
    margin-left: 0;
    margin-right: 16rpx;
    text-align: right;
  }

  .chat-msg__bubble {
    background-color: #d8e5ff;
    border-radius: 20rpx 4rpx 20rpx 20rpx;
    text-align: left;
  }
}

.history-end {
  padding: 12rpx 0;
}

.time-chip {
  padding: 16rpx 0 4rpx;
}

.chat-body__bottom {
  height: 30rpx;
}

.chat-input {
  padding-top: 16rpx;
  padding-bottom: calc(16rpx + env(safe-area-inset-bottom) / 2);
  box-shadow: 0rpx -6rpx 20rpx 0rpx rgba(0, 0, 0, 0.04);

  &__plus {
    flex-shrink: 0;
    width: 68rpx;
    height: 68rpx;
    margin-right: 16rpx;
    background-color: #f4f5f9;
    border-radius: 100rpx;
  }

  &__field {
    flex: 1;
    height: 68rpx;
    padding: 0 24rpx;
    margin-right: 20rpx;
    background-color: #f4f5f9;
    border-radius: 100rpx;
    font-size: 28rpx;
  }
}
</style>
