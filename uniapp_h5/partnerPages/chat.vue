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
    >
      <view v-if="loading" class="tn-text-center tn-color-gray tn-padding">加载中...</view>
      <view v-else-if="!messages.length" class="tn-text-center tn-color-gray--disabled tn-padding-xl">
        暂无消息，发送第一条消息吧
      </view>

      <view
        v-for="(item, index) in messages"
        :key="item.id || 'tmp-' + index"
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
import { uploadImageToServer } from '@/utils/upload'

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
let pollTimer = null

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

const loadMessages = async (silent = false) => {
  if (!targetId.value) return
  if (!silent) loading.value = true
  try {
    // 首次进入拉全量;轮询时传当前最大消息 id,只拉增量
    const afterMessageId = silent && messages.value.length ? maxMessageId() : undefined
    const res = await getChatMessages({ chatType: chatType.value, targetId: targetId.value, afterMessageId })
    const list = Array.isArray(res.data) ? res.data : []
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
    console.log('加载聊天记录失败', error)
  } finally {
    loading.value = false
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
    console.log('发送消息失败', error)
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
        console.log('发送图片消息失败', error)
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
  pollTimer = setInterval(() => loadMessages(true), 4000)
}

onLoad((options) => {
  // 只初始化参数,数据拉取统一由 onShow 处理,避免首屏双重全量请求
  chatType.value = options?.type === 'single' ? 2 : 1
  targetId.value = options?.targetId || null
  if (options?.name) {
    peerName.value = decodeURIComponent(options.name)
  }
})

onShow(() => {
  // messages 为空走全量(带 loading),否则按最大消息 id 增量拉取
  loadMessages(messages.value.length > 0)
  // 标记会话已读,清掉会话列表未读数
  if (targetId.value) {
    markConversationRead({ chatType: chatType.value, targetId: targetId.value }).catch(() => {})
  }
  // 轮询刷新,保证能收到对方消息(先清理旧定时器,避免重复轮询)
  startPolling()
})

onHide(() => {
  stopPolling()
})

onUnload(() => {
  stopPolling()
})

// #ifdef H5
// H5 端页面切到后台时暂停轮询,回到前台恢复
const handleVisibilityChange = () => {
  if (document.hidden) {
    stopPolling()
  } else {
    loadMessages(true)
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
