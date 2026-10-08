<template>
  <view class="group-page">
    <!-- 顶部自定义导航 -->
    <tn-navbar fixed home-icon="" :bottom-shadow="false" bg-color="#FFFFFF" :placeholder="false">
      <template #back>
        <view class="nav-back" @click="goBack">
          <tn-icon name="left-arrow"></tn-icon>
        </view>
      </template>
      <view class="nav-center">
        <text class="nav-title">我的群聊</text>
      </view>
      <template #right>
        <view class="create-btn" @click="goCreate">
          <tn-icon name="add" class="create-btn__icon"></tn-icon>
          <text>发起群聊</text>
        </view>
      </template>
    </tn-navbar>

    <scroll-view
      scroll-y
      class="page-scroll"
      :style="{ paddingTop: vuex_custom_bar_height + 'px' }"
      :refresher-enabled="true"
      :refresher-triggered="refreshing"
      @refresherrefresh="onRefresh"
    >
      <view class="page-wrap">
        <!-- 加载中 -->
        <view v-if="loading && !groups.length" class="state-tip">加载中...</view>

        <!-- 加载失败 -->
        <view v-else-if="loadFailed && !groups.length" class="state-tip">
          <view>群聊加载失败</view>
          <view class="retry-btn" @click="reload">点击重试</view>
        </view>

        <!-- 空态 -->
        <view v-else-if="!groups.length" class="empty-state">
          <view class="empty-icon">
            <tn-icon name="group-circle"></tn-icon>
          </view>
          <text class="empty-title">暂未加入任何群聊</text>
          <text class="empty-desc">点击右上角"发起群聊"创建一个吧</text>
        </view>

        <!-- 群聊卡片列表 -->
        <view
          v-for="item in groups"
          :key="item.id"
          class="group-card"
          @click="openChat(item)"
          @longpress="onGroupLongPress(item)"
        >
          <view class="group-avatar-box">
            <image v-if="item.avatar" class="group-avatar" :src="formatAvatar(item.avatar)" mode="aspectFill" />
            <view v-else class="group-avatar group-avatar--default">
              <tn-icon name="group-circle" style="font-size: 44rpx;"></tn-icon>
            </view>
          </view>
          <view class="group-main">
            <view class="group-head">
              <text class="group-name">{{ item.groupName || '群聊' }}</text>
              <view v-if="Number(item.sticky) === 1" class="sticky-tag">置顶</view>
            </view>
            <view class="group-sub">
              <text class="group-last">{{ item.lastMessage || '暂无消息' }}</text>
            </view>
            <view class="group-meta">
              <tn-icon name="team" class="group-meta__icon"></tn-icon>
              <text>{{ item.memberCount || 0 }}人</text>
              <text class="group-meta__dot">·</text>
              <text>{{ formatTime(item.lastMessageTime) || '暂无活跃' }}</text>
            </view>
          </view>
          <tn-icon name="right" class="group-arrow"></tn-icon>
        </view>
      </view>
    </scroll-view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { useCustomBarHeight, useGoBack } from '@/libs/composables'
import config from '@/config'
import { getContactGroups } from '@/api/contact'
import { getConversations, updateConversationSettings, hideConversation } from '@/api/chat'
import { toastRequestError } from '@/utils/common'

const { vuex_custom_bar_height } = useCustomBarHeight()
const { goBack } = useGoBack()

const groups = ref([])
const loading = ref(false)
const loadFailed = ref(false)
const refreshing = ref(false)

const formatAvatar = (avatar) => {
  if (!avatar) return ''
  if (/^https?:\/\//.test(avatar) || avatar.startsWith('/static')) return avatar
  return config.baseUrl + avatar
}

// 时间:今天显示 HH:mm,更早显示 MM-DD HH:mm
const formatTime = (value) => {
  if (!value) return ''
  const date = String(value).replace('T', ' ')
  const now = new Date()
  const ymd = date.slice(0, 10)
  const today = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}-${String(now.getDate()).padStart(2, '0')}`
  if (ymd === today) return date.length > 15 ? date.slice(11, 16) : '今天'
  return date.length >= 10 ? date.slice(5, 16) : date
}

// 图片消息占位
const IMAGE_EXT_RE = /\.(png|jpe?g|gif|webp|bmp)(\?.*)?$/i
const formatLastMessage = (value) => {
  const text = String(value || '').trim()
  if (text && /^(\/file\/upload\/image|https?:\/\/)/i.test(text) && IMAGE_EXT_RE.test(text)) {
    return '[图片]'
  }
  return text
}

// 加载群列表,并合并会话数据(置顶标记/最后消息),置顶优先
const loadGroups = async () => {
  loading.value = true
  loadFailed.value = false
  try {
    const [groupResult, convResult] = await Promise.allSettled([getContactGroups(), getConversations()])
    let list = []
    if (groupResult.status === 'fulfilled') {
      list = Array.isArray(groupResult.value.data) ? groupResult.value.data : []
    }
    // 会话信息按群 id 建立索引(群聊 chatType=1),拿 sticky 置顶标记
    const convMap = new Map()
    if (convResult.status === 'fulfilled') {
      const convs = Array.isArray(convResult.value.data) ? convResult.value.data : []
      convs.forEach((conv) => {
        if (Number(conv.chatType) === 1 && conv.targetId != null) {
          convMap.set(String(conv.targetId), conv)
        }
      })
    }
    groups.value = list.map((item) => {
      const conv = convMap.get(String(item.id))
      return {
        ...item,
        sticky: conv ? Number(conv.sticky || 0) : 0,
        lastMessage: formatLastMessage(item.lastMessage || conv?.lastMessage)
      }
    })
    // 置顶优先,其余保持后端返回顺序
    groups.value.sort((a, b) => Number(b.sticky || 0) - Number(a.sticky || 0))
  } catch (error) {
    // request.js 已统一错误提示;此处仅置失败态,避免双重 toast
    loadFailed.value = true
  } finally {
    loading.value = false
  }
}

function reload() {
  loadGroups()
}

function onRefresh() {
  if (refreshing.value) return
  refreshing.value = true
  loadGroups().finally(() => {
    refreshing.value = false
  })
}

// 长按群聊:置顶/取消置顶、删除会话(仅入口,删除走会话管理契约接口)
function onGroupLongPress(item) {
  if (!item?.id) return
  const sticky = Number(item.sticky || 0) === 1
  uni.showActionSheet({
    itemList: [sticky ? '取消置顶' : '置顶该群', '删除该群会话'],
    success: (res) => {
      if (res.tapIndex === 0) {
        toggleSticky(item)
      } else if (res.tapIndex === 1) {
        confirmHideConversation(item)
      }
    }
  })
}

// 置顶/取消置顶(会话设置契约接口,targetType=1 群聊)
async function toggleSticky(item) {
  const nextSticky = Number(item.sticky || 0) === 1 ? 0 : 1
  try {
    await updateConversationSettings({ targetType: 1, targetId: item.id, sticky: nextSticky })
    item.sticky = nextSticky
    // 置顶优先重新排序
    groups.value.sort((a, b) => Number(b.sticky || 0) - Number(a.sticky || 0))
    uni.showToast({ icon: 'none', title: nextSticky === 1 ? '已置顶' : '已取消置顶' })
  } catch (error) {
    toastRequestError(error, '操作失败')
  }
}

// 删除(隐藏)会话:本地移除群卡片外的会话入口;群本身仍在,可重新发起聊天
function confirmHideConversation(item) {
  uni.showModal({
    title: '删除会话',
    content: `删除「${item.groupName || '群聊'}」的会话记录吗？群聊本身不受影响。`,
    confirmColor: '#FB6A67',
    success: async (res) => {
      if (!res.confirm) return
      try {
        await hideConversation(1, item.id)
        uni.showToast({ icon: 'none', title: '会话已删除' })
      } catch (error) {
        toastRequestError(error, '删除失败')
      }
    }
  })
}

// 进入群聊会话
function openChat(item) {
  if (!item?.id) return
  uni.navigateTo({
    url: `/partnerPages/chat?type=group&targetId=${item.id}&name=${encodeURIComponent(item.groupName || '群聊')}`
  })
}

// 发起群聊
function goCreate() {
  uni.navigateTo({ url: '/partnerPages/create' })
}

// 群列表统一由 onShow 加载(首次显示与从聊天/群信息页返回时都会触发)
onShow(() => {
  loadGroups()
})
</script>

<style lang="scss" scoped>
.group-page {
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

/* 右上角发起群聊入口 */
.create-btn {
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
    font-size: 26rpx;
  }
}

.page-scroll {
  box-sizing: border-box;
  height: 100vh;
}

.page-wrap {
  padding: 24rpx 24rpx 60rpx;
}

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
  color: #3668fc;
  background: rgba(54, 104, 252, 0.1);
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

/* 群聊卡片 */
.group-card {
  display: flex;
  align-items: center;
  margin-bottom: 20rpx;
  padding: 26rpx;
  background: #ffffff;
  border-radius: 16rpx;
  border: 1rpx solid rgba(17, 31, 46, 0.06);
}

.group-avatar-box {
  flex-shrink: 0;
}

.group-avatar {
  width: 92rpx;
  height: 92rpx;
  border-radius: 20rpx;
  background: #f4f5f9;
}

.group-avatar--default {
  display: flex;
  align-items: center;
  justify-content: center;
  background: #3668fc;
  color: #ffffff;
}

.group-main {
  flex: 1;
  min-width: 0;
  margin-left: 22rpx;
}

.group-head {
  display: flex;
  align-items: center;
  gap: 12rpx;
}

.group-name {
  flex: 1;
  min-width: 0;
  color: #1d2541;
  font-size: 30rpx;
  font-weight: 700;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.sticky-tag {
  flex-shrink: 0;
  padding: 2rpx 14rpx;
  border-radius: 999rpx;
  background: rgba(255, 172, 0, 0.14);
  color: #ff9f2e;
  font-size: 20rpx;
  font-weight: 600;
}

.group-sub {
  margin-top: 8rpx;
}

.group-last {
  color: #657189;
  font-size: 25rpx;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.group-meta {
  display: flex;
  align-items: center;
  gap: 6rpx;
  margin-top: 10rpx;
  color: #9aa4b2;
  font-size: 22rpx;

  &__icon {
    font-size: 24rpx;
  }

  &__dot {
    margin: 0 6rpx;
  }
}

.group-arrow {
  flex-shrink: 0;
  margin-left: 12rpx;
  color: #c4cbd4;
  font-size: 26rpx;
}
</style>
