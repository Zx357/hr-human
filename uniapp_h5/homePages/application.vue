<template>
  <view class="oa-content">
    <!-- 顶部自定义导航 -->
    <tn-navbar fixed home-icon="" :bottom-shadow="false" bg-color="#FFFFFF" :placeholder="false">
      <template #back><view class='tn-custom-nav-bar__back'
        @click="goBack">
        <tn-icon name="left-arrow" class='icon'></tn-icon>
      </view></template>
      <view class="tn-flex tn-flex-col-center tn-flex-row-center ">
        <text class="tn-text-bold tn-text-xl tn-color-black">应用消息</text>
      </view>
    </tn-navbar>


    <view class="" :style="{paddingTop: vuex_custom_bar_height+10+'px'}">
      <view v-if="loading && !messageList.length" class="tn-text-center tn-color-gray tn-padding-xl">加载中...</view>

      <view v-else-if="!messageList.length" class="tn-text-center tn-padding-xl">
        <view style="font-size: 160rpx;padding-top: 60rpx;">
          <text class="tn-icon-clip tn-color-gray--light"></text>
        </view>
        <view class="tn-color-gray--disabled tn-text-lg">暂无消息</view>
      </view>

      <view class="msg-card" v-for="item in messageList" :key="item.id" @click="openMessage(item)">
        <view class="msg-icon" :style="{ color: item.color || '#4B98FE', backgroundColor: hexToBg(item.color || '#4B98FE') }">
          <tn-icon :name="item.icon || 'notice-fill'"></tn-icon>
        </view>
        <view class="msg-main">
          <view class="msg-head">
            <text class="msg-title clamp-1">{{ item.title }}</text>
            <text class="msg-date">{{ item.time }}</text>
          </view>
          <text class="msg-desc clamp-2">{{ item.desc || '暂无内容' }}</text>
          <view class="msg-footer">
            <text class="msg-tag" :style="{ color: item.color || '#4B98FE', backgroundColor: hexToBg(item.color || '#4B98FE') }">应用消息</text>
            <text class="msg-more">查看详情</text>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { useCustomBarHeight, useGoBack } from '@/libs/composables'
import { getHomeMessages } from '@/api/home'

// 使用 composable 获取自定义导航栏高度
const { vuex_custom_bar_height } = useCustomBarHeight()
const { goBack } = useGoBack()

const loading = ref(false)
const messageList = ref([])

const normalizeList = (data) => {
  if (Array.isArray(data)) return data
  return data?.records || data?.rows || data?.list || []
}

const loadMessages = async () => {
  loading.value = true
  try {
    const res = await getHomeMessages()
    const list = normalizeList(res.data)
    messageList.value = list.map((item, index) => ({
      id: item.id || `message-${index}`,
      title: item.title || '消息提醒',
      desc: item.desc || '',
      time: item.time || '',
      color: item.color || '#4B98FE',
      icon: item.icon || 'notice-fill',
      badge: item.badge || '',
      url: item.url || '/homePages/notice',
      avatar: item.avatar
    }))
  } catch (error) {
    console.log('加载应用消息失败', error)
  } finally {
    loading.value = false
  }
}

const hexToBg = (hex) => {
  const v = String(hex || '#4B98FE').replace('#', '')
  if (v.length < 6) return 'rgba(75, 152, 254, 0.1)'
  const r = parseInt(v.slice(0, 2), 16)
  const g = parseInt(v.slice(2, 4), 16)
  const b = parseInt(v.slice(4, 6), 16)
  return `rgba(${r}, ${g}, ${b}, 0.1)`
}

const openMessage = (item) => {
  if (!item?.url) return
  uni.navigateTo({ url: item.url })
}

onShow(() => {
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
  /* 文字截取*/
  .clamp-text-1 {
    -webkit-line-clamp: 1;
    display: -webkit-box;
    -webkit-box-orient: vertical;
    text-overflow: ellipsis;
    overflow: hidden;
  }

  .clamp-text-2 {
    -webkit-line-clamp: 2;
    display: -webkit-box;
    -webkit-box-orient: vertical;
    text-overflow: ellipsis;
    overflow: hidden;
  }
  /* 背景阴影 start*/
  .content-bg {
    margin: 0 30rpx 0 30rpx;
    border-radius:  0 0 15rpx 15rpx;
    background-color: #FFFFFF;
  }
  .msg-card {
    display: flex;
    margin: 0 30rpx 22rpx;
    padding: 26rpx;
    background: #ffffff;
    border-radius: 18rpx;
    box-shadow: 0 10rpx 30rpx rgba(29, 37, 65, 0.06);
  }

  .msg-icon {
    flex-shrink: 0;
    width: 86rpx;
    height: 86rpx;
    border-radius: 50%;
    font-size: 42rpx;
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
    color: #1d2541;
    font-size: 31rpx;
    font-weight: 800;
  }

  .msg-date {
    flex-shrink: 0;
    margin-left: 16rpx;
    color: #9aa4b2;
    font-size: 24rpx;
  }

  .msg-desc {
    display: block;
    margin-top: 12rpx;
    color: #657189;
    font-size: 26rpx;
    line-height: 1.55;
  }

  .msg-footer {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-top: 16rpx;
  }

  .msg-tag {
    padding: 6rpx 16rpx;
    border-radius: 999rpx;
    font-size: 22rpx;
    font-weight: 700;
  }

  .msg-more {
    color: #9aa4b2;
    font-size: 24rpx;
  }

  .image-design{
    padding: 150rpx 0rpx;
    position: relative;
  }
  .image-pic{
    margin: 30rpx 30rpx 0 30rpx;
    background-color: #4B98FE;
    background-size: cover;
    background-repeat:no-repeat;
    background-position:top;
    border-radius: 15rpx 15rpx 0 0;
  }
  /* 用户头像 start */
  .user-image {
    width: 35rpx;
    height: 35rpx;
    position: relative;
    overflow: hidden;
    border-radius: 50%;
  }

  .user-pic {
    background-size: cover;
    background-repeat: no-repeat;
    background-position: top;
    border: 1rpx solid rgba(255,255,255,0.05);
    box-shadow: 0rpx 0rpx 80rpx 0rpx rgba(0, 0, 0, 0.15);
    border-radius: 50%;
    overflow: hidden;
  }
</style>
