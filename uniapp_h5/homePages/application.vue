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

      <view class="" v-for="item in messageList" :key="item.id" @click="openMessage(item)">
        <view class="image-pic" :style="item.avatar ? 'background-image:url(' + item.avatar + ')' : ''">
          <view class="image-design">
            <view v-if="item.badge" class="tn-text-center" style="width: 120rpx;height: 50rpx;position: absolute;top: 0;right:0;border-radius: 0 12rpx 0 12rpx;font-size: 22rpx;background-color: #00000050;">
              <view class="tn-margin-xs tn-color-white">{{ item.badge }}条未读</view>
            </view>
            <view class="" style="width: 100%;min-height: 120rpx;background: linear-gradient(0deg, rgba(0,0,0,0.6), rgba(0,0,0,0.2), rgba(0,0,0,0));position: absolute;bottom: 0;">
              <view class="tn-text-lg tn-padding-top-xl tn-padding-left tn-padding-right tn-color-white tn-text-bold clamp-text-1">
                {{ item.title }}
              </view>
            </view>
          </view>
        </view>
        <view class="content-bg tn-padding">
          <view class="tn-text-justify clamp-text-2 tn-padding-bottom tn-color-gray--dark" v-if="item.desc">
            {{ item.desc }}
          </view>
          <view class="tn-flex tn-flex-direction-row tn-flex-col-center tn-flex-row-between">
            <view class="tn-flex">
              <view class="tn-flex user-pic">
                <view class="tn-flex tn-flex-row-center tn-flex-col-center tn-color-white" :style="{ backgroundColor: item.color || '#4B98FE', width: '35rpx', height: '35rpx', borderRadius: '50%' }">
                  <tn-icon :name="item.icon || 'notice-fill'" style="font-size: 20rpx;"></tn-icon>
                </view>
              </view>
              <view class="tn-flex tn-margin-left-xs" style="width: 300rpx;">
                <text class="clamp-text-1 tn-color-gray--dark">{{ item.title || '系统消息' }}</text>
              </view>
            </view>
            <view class="tn-color-gray">
              {{ item.time }}
            </view>
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
