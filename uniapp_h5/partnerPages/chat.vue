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
      <!-- 群聊头部右侧"群信息"入口 -->
      <template v-if="chatType === 1" #right>
        <view class="group-info-btn" @click="goGroupInfo">
          <tn-icon name="team" class="group-info-btn__icon"></tn-icon>
          <text>群信息</text>
        </view>
      </template>
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
      <view v-else-if="missingTarget" class="tn-text-center tn-color-gray--disabled tn-padding-xl">
        会话不存在或已失效，请返回后重新进入
      </view>
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
            <!-- 已撤回消息:灰色系统提示样式,不渲染原内容 -->
            <view v-if="isRecalled(item)" class="chat-msg__recalled">
              <tn-icon name="refresh" class="chat-msg__recalled-icon"></tn-icon>
              <text>{{ isMine(item) ? '你撤回了一条消息' : `${item.fromName || '对方'}撤回了一条消息` }}</text>
            </view>
            <!-- 图片消息:点击预览,不用文本气泡样式 -->
            <image
              v-else-if="Number(item.msgType) === 2"
              class="chat-msg__image"
              :src="formatImageUrl(item.content)"
              mode="widthFix"
              @click="previewImage(item)"
              @longpress="onMessageLongPress(item)"
            />
            <view v-else class="chat-msg__bubble" @longpress="onMessageLongPress(item)">{{ item.content }}</view>
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
import { getChatMessages, sendChatMessage, markConversationRead, recallChatMessage } from '@/api/chat'
import { refreshChatUnreadBadge } from '@/utils/chat-badge'
import { uploadImageToServer } from '@/utils/upload'
import { toastRequestError } from '@/utils/common'
import {
  connect,
  isOpen,
  on
} from '@/utils/websocket'

const { vuex_custom_bar_height } = useCustomBarHeight()
const { goBack } = useGoBack()
const store = useStore()

const chatType = ref(1)
const targetId = ref(null)
const peerName = ref('聊天')
const messages = ref([])
const loading = ref(true)
// 页面参数缺失(targetId 为空):展示空态提示而非永远"加载中"
const missingTarget = ref(false)
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
// 已读上报节流:800ms 窗口合并,窗口结束时仍有新消息则补发一次(尾触发)
let markReadTimer = null
let markReadPending = false

// ===== WebSocket 实时化 =====
// WS 消息处理函数注销器(onLoad 注册,onUnload/H5 卸载时注销)
let offWsChatMessage = null
let offWsUnreadTotal = null
let offWsRecall = null
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

// ===== 消息撤回 =====
// 撤回时间窗:2 分钟(与后端校验一致)
const RECALL_WINDOW_MS = 2 * 60 * 1000

// 后端历史消息会把撤回消息的 content 替换并带 recalled 标记,前端按标记渲染灰色提示
const isRecalled = (item) => {
  return Number(item.recalled) === 1 || item.recalled === true
}

// 消息时间戳(毫秒)
const messageTime = (item) => {
  const time = new Date(String(item.createdTime || '').replace(/-/g, '/').replace('T', ' '))
  return Number.isFinite(time.getTime()) ? time.getTime() : 0
}

// 自己发送且发送时间在 2 分钟内的消息才可撤回(超时旧消息不显示撤回选项)
const canRecall = (item) => {
  if (!item?.id || isRecalled(item) || !isMine(item)) return false
  const sentAt = messageTime(item)
  if (!sentAt) return false
  return Date.now() - sentAt <= RECALL_WINDOW_MS
}

// 长按消息:自己 2 分钟内发送的消息弹出撤回
const onMessageLongPress = (item) => {
  if (!canRecall(item)) return
  uni.showActionSheet({
    itemList: ['撤回'],
    success: (res) => {
      if (res.tapIndex === 0) recallMessage(item)
    }
  })
}

// 调撤回契约接口,成功后本地把消息替换为 recalled 状态(后端会向对方推送撤回)
const recallMessage = async (item) => {
  try {
    await recallChatMessage(item.id)
    item.recalled = 1
    item.content = ''
    uni.showToast({ icon: 'none', title: '已撤回' })
  } catch (error) {
    toastRequestError(error, '撤回失败')
  }
}

// ===== 群信息入口 =====
const goGroupInfo = () => {
  if (!targetId.value) return
  uni.navigateTo({
    url: `/partnerPages/group-info?groupId=${targetId.value}&name=${encodeURIComponent(peerName.value)}`
  })
}

// 群名修改/解散事件:group-info 页操作后同步聊天页标题
const handleGroupRenamed = (data) => {
  if (data && Number(data.groupId) === Number(targetId.value) && data.name) {
    peerName.value = data.name
  }
}

const handleGroupDissolved = (data) => {
  if (data && Number(data.groupId) === Number(targetId.value)) {
    uni.showToast({ icon: 'none', title: '群聊已解散' })
    setTimeout(() => uni.navigateBack(), 600)
  }
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
  // 页面参数缺失(targetId 为空):早退并清理 loading,给出空态提示,避免永远"加载中"
  if (!targetId.value) {
    missingTarget.value = true
    loading.value = false
    silentLoading = false
    return
  }
  missingTarget.value = false
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
        // 停留在聊天页期间拉到新消息,同步标记已读(节流),避免未读角标不清零
        markReadThrottled()
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
    // 发送失败恢复输入:仅当输入框已被清空/为空时回填,避免覆盖请求期间新输入的内容
    if (msgType === 1 && !draft.value) {
      draft.value = content
    }
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
    if (isOpen()) return
    loadMessages(true)
  }, 4000)
}

// 已读上报节流(800ms 尾触发):首条消息立即上报合并窗口内后续消息,
// 窗口结束时若仍有未上报的新消息再补发一次,避免每条消息打一次接口
const markReadThrottled = () => {
  if (!targetId.value) return
  if (markReadTimer) {
    markReadPending = true
    return
  }
  markConversationRead({ chatType: chatType.value, targetId: targetId.value }).catch(() => {})
  markReadTimer = setTimeout(() => {
    markReadTimer = null
    if (markReadPending) {
      markReadPending = false
      markReadThrottled()
    }
  }, 800)
}

const stopMarkReadThrottle = () => {
  if (markReadTimer) {
    clearTimeout(markReadTimer)
    markReadTimer = null
  }
  markReadPending = false
}

// ===== WebSocket 实时消息处理 =====

// 当前会话在后端推送帧里的 conversationId(单聊 S+对方员工ID,群聊 G+群ID,与后端 ChatMessagePushService 约定一致)
const conversationIdOf = () => {
  return (Number(chatType.value) === 1 ? 'G' : 'S') + targetId.value
}

// 推送消息是否属于当前会话:优先按后端 conversationId 匹配,
// 字段级判断兜底(含自己换设备发送的消息:from 是自己且 peer 指向当前会话)
const belongsToCurrentConversation = (data) => {
  const msg = data && data.message
  if (msg == null || targetId.value == null) return false
  if (data.conversationId) {
    if (String(data.conversationId) === String(conversationIdOf())) return true
    // conversationId 不匹配时仍走字段兜底(换设备自己发的消息 conversationId 指向自己)
  }
  if (Number(msg.chatType) !== Number(chatType.value)) return false
  if (Number(chatType.value) === 1) {
    return Number(msg.groupId) === Number(targetId.value)
  }
  // 单聊:对方发来的消息,或自己(其他设备)发的且对端是当前会话
  return Number(msg.fromEmployeeId) === Number(targetId.value) ||
    Number(msg.peerEmployeeId) === Number(targetId.value) ||
    (Number(msg.fromEmployeeId) === Number(myEmployeeId.value) &&
      Number(msg.peerEmployeeId) === Number(targetId.value))
}

// 拉取进行中收到推送:延迟补一次增量拉取,防止推送消息被进行中的全量/增量结果覆盖
const scheduleWsCatchUp = () => {
  if (wsCatchUpTimer) return
  wsCatchUpTimer = setTimeout(() => {
    wsCatchUpTimer = null
    if (isOpen() && !loading.value && !silentLoading) {
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
  if (!belongsToCurrentConversation(data)) return
  const added = appendMessages([msg])
  if (added) {
    scrollToBottom()
    // 停留在聊天页收到新消息,同步标记已读(节流),避免未读角标不清零
    markReadThrottled()
  }
}

// 未读总数推送:直接更新 tabbar 角标(免一次 REST 查询)
const handleWsUnreadTotal = (data) => {
  if (!data) return
  store.commit('SET_UNREAD_BADGE', { chatUnread: Number(data.total || 0) })
}

// 撤回推送:属于当前会话则把本地对应消息置为已撤回(轮询兜底之外实时生效)
const handleWsRecall = (data) => {
  const msg = data && data.message
  if (!msg || msg.id == null) return
  if (data.conversationId && String(data.conversationId) !== String(conversationIdOf())) return
  const local = messages.value.find((item) => Number(item.id) === Number(msg.id))
  if (local) local.recalled = 1
}

const registerWsHandlers = () => {
  if (!offWsChatMessage) {
    offWsChatMessage = on('chat_message', handleWsChatMessage)
  }
  if (!offWsUnreadTotal) {
    offWsUnreadTotal = on('unread_total', handleWsUnreadTotal)
  }
  if (!offWsRecall) {
    offWsRecall = on('chat_message_recall', handleWsRecall)
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
  if (offWsRecall) {
    offWsRecall()
    offWsRecall = null
  }
  if (wsCatchUpTimer) {
    clearTimeout(wsCatchUpTimer)
    wsCatchUpTimer = null
  }
  stopMarkReadThrottle()
}

onLoad((options) => {
  // 只初始化参数,数据拉取统一由 onShow 处理,避免首屏双重全量请求
  chatType.value = options?.type === 'single' ? 2 : 1
  targetId.value = options?.targetId || null
  if (options?.name) {
    peerName.value = decodeURIComponent(options.name)
  }
  // 注册 WS 消息分发(全局单例连接由登录/启动/onShow 建立/token 为空时自动走轮询)
  registerWsHandlers()
  // 群管理事件:group-info 页改群名/解散后同步本页标题与退出
  uni.$on('group-renamed', handleGroupRenamed)
  uni.$on('group-dissolved', handleGroupDissolved)
})

onShow(() => {
  // messages 为空走全量(带 loading),否则按最大消息 id 增量拉取
  loadMessages(messages.value.length > 0)
  // 标记会话已读,清掉会话列表未读数
  if (targetId.value) {
    markConversationRead({ chatType: chatType.value, targetId: targetId.value }).catch(() => {})
  }
  // 建立/恢复全局 WS 单例连接(幂等,token 为空自动跳过);在线时轮询空转,断开时轮询兜底
  connect()
  // 轮询兜底刷新(先清理旧定时器,避免重复轮询)
  startPolling()
})

onHide(() => {
  stopPolling()
  // 全局 WS 单例保持连接(其他页面仍实时收推送),此处仅停止本页轮询
  // 离开聊天页(或切后台)时刷新全局未读数,让 tabbar 首页角标及时清零/更新
  refreshChatUnreadBadge()
})

onUnload(() => {
  stopPolling()
  // 仅注销本页订阅,不断开全局 WS 单例
  unregisterWsHandlers()
  uni.$off('group-renamed', handleGroupRenamed)
  uni.$off('group-dissolved', handleGroupDissolved)
  refreshChatUnreadBadge()
})

// #ifdef H5
// H5 端页面切到后台时暂停轮询(WS 单例保持连接,推送仍会送达),回到前台恢复
const handleVisibilityChange = () => {
  if (document.hidden) {
    stopPolling()
  } else {
    loadMessages(true)
    connect()
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
  // 仅注销本页订阅,不断开全局 WS 单例
  unregisterWsHandlers()
  uni.$off('group-renamed', handleGroupRenamed)
  uni.$off('group-dissolved', handleGroupDissolved)
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

  /* 已撤回消息:灰色系统提示 */
  &__recalled {
    display: inline-flex;
    align-items: center;
    padding: 8rpx 16rpx;
    border-radius: 12rpx;
    background-color: rgba(29, 37, 65, 0.05);
    color: #9aa4b2;
    font-size: 23rpx;
    line-height: 1.5;
  }

  &__recalled-icon {
    margin-right: 8rpx;
    font-size: 24rpx;
  }
}

/* 群信息入口 */
.group-info-btn {
  display: flex;
  align-items: center;
  margin-right: 24rpx;
  padding: 10rpx 22rpx;
  border-radius: 999rpx;
  background: rgba(54, 104, 252, 0.1);
  color: #3668fc;
  font-size: 24rpx;
  font-weight: 600;

  &__icon {
    margin-right: 6rpx;
    font-size: 28rpx;
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
