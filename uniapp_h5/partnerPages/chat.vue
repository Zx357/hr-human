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
          <view class="chat-msg__bubble">{{ item.content }}</view>
        </view>
      </view>
      <view class="chat-body__bottom"></view>
    </scroll-view>

    <!-- 输入区 -->
    <view class="chat-input tn-bg-white tn-flex tn-flex-col-center tn-padding-sm tn-padding-left tn-padding-right">
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
import { computed, nextTick, ref } from 'vue'
import { onLoad, onShow, onHide, onUnload } from '@dcloudio/uni-app'
import { useStore } from 'vuex'
import { useCustomBarHeight, useGoBack } from '@/libs/composables'
import config from '@/config'
import { getChatMessages, sendChatMessage } from '@/api/chat'

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

const scrollToBottom = () => {
  nextTick(() => {
    const last = messages.value[messages.value.length - 1]
    scrollInto.value = last ? 'msg-' + (last.id || (messages.value.length - 1)) : ''
  })
}

const loadMessages = async (silent = false) => {
  if (!targetId.value) return
  if (!silent) loading.value = true
  try {
    const res = await getChatMessages({ chatType: chatType.value, targetId: targetId.value })
    const list = Array.isArray(res.data) ? res.data : []
    const changed = list.length !== messages.value.length ||
      (list.length && messages.value.length && String(list[list.length - 1].id) !== String(messages.value[messages.value.length - 1].id))
    messages.value = list
    if (changed || !silent) scrollToBottom()
  } catch (error) {
    console.log('加载聊天记录失败', error)
  } finally {
    loading.value = false
  }
}

const send = async () => {
  const content = draft.value.trim()
  if (!content) return
  if (sending.value) return
  sending.value = true
  try {
    const res = await sendChatMessage({ chatType: chatType.value, targetId: targetId.value, content })
    if (res.data) {
      messages.value.push(res.data)
      draft.value = ''
      scrollToBottom()
    }
  } catch (error) {
    console.log('发送消息失败', error)
  } finally {
    sending.value = false
  }
}

onLoad((options) => {
  chatType.value = options?.type === 'single' ? 2 : 1
  targetId.value = options?.targetId || null
  if (options?.name) {
    peerName.value = decodeURIComponent(options.name)
  }
  loadMessages()
})

onShow(() => {
  loadMessages(true)
  // 轮询刷新,保证能收到对方消息
  pollTimer = setInterval(() => loadMessages(true), 4000)
})

onHide(() => {
  if (pollTimer) { clearInterval(pollTimer); pollTimer = null }
})

onUnload(() => {
  if (pollTimer) { clearInterval(pollTimer); pollTimer = null }
})
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
