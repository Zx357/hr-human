<template>
  <view class="oa-content">
    <!-- 顶部自定义导航 -->
    <tn-navbar fixed home-icon="" :bottom-shadow="false" bg-color="#FFFFFF" :placeholder="false">
      <template #back><view class='tn-custom-nav-bar__back'
        @click="goBack">
        <tn-icon name="left-arrow" class="icon"></tn-icon>
      </view></template>
      <view class="tn-flex tn-flex-col-center tn-flex-row-center ">
        <text class="tn-text-bold tn-text-xl tn-color-black">我的群聊</text>
      </view>
    </tn-navbar>
    
    
    <view class="" :style="{paddingTop: vuex_custom_bar_height + 10 + 'px'}">
      <view v-if="!groupList.length" class="tn-text-center tn-color-gray--disabled tn-padding-xl">暂未加入任何群聊,可在"发起群聊"中创建</view>
      <view class="tn-flex tn-flex-col-center" style="margin: 50rpx 0rpx 50rpx 30rpx;" v-for="item in groupList" :key="item.id" @click="openChat(item)">
        <!-- <view class="icon15__item--icon tn-flex tn-flex-row-center tn-flex-col-center tn-bg-blue--light tn-color-blue">
          <tn-icon name="organizatio-fill"></tn-icon>
        </view> -->
        <view class="logo-pic">
          <view class="logo-image" :style="{ backgroundImage: `url(${formatAvatar(item.avatar)})`, width: '90rpx', height: '90rpx', backgroundSize: 'cover' }">
          </view>
        </view>
        <view class="tn-padding-left-sm" style="width: 67vw">
          <view class="tn-flex tn-flex-row-between tn-flex-col-between">
            <view class="justify-content-item">
              <text class="oa-black tn-text-xl">{{ item.groupName }}</text>
            </view>
            
          </view>
          <view class="tn-padding-top-xs tn-text-ellipsis">
            <text class="tn-color-gray tn-text-sm">{{ item.lastMessage || '暂无消息' }} {{ formatTime(item.lastMessageTime) }}</text>
          </view>
        </view>
        <view class="" style="width: 15vw;">
          <text class="tn-color-gray tn-padding-right-xs">{{ item.memberCount || 0 }}人</text>
          <tn-icon name="right" class="tn-color-gray tn-text-lg"></tn-icon>
        </view>
      </view>
      
    </view>
    
    <view class='tn-tabbar-height'></view>
    
  </view>
</template>

<script setup>
  import { computed, ref } from 'vue'
  import { onShow } from '@dcloudio/uni-app'
  import { useCustomBarHeight, useGoBack } from '@/libs/composables'
  import config from '@/config'
  import { getContactGroups } from '@/api/contact'
  // 使用 composable 获取自定义导航栏高度
  const { vuex_custom_bar_height } = useCustomBarHeight()
  const { goBack } = useGoBack()

  const groups = ref([])
  const groupList = computed(() => groups.value)

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

  const loadGroups = async () => {
    try {
      const res = await getContactGroups()
      groups.value = Array.isArray(res.data) ? res.data : []
    } catch (error) {
      uni.showToast({ icon: 'none', title: '加载群聊失败，请重试' })
    }
  }
  
  // 跳转
  const tn = (e) => {
    uni.navigateTo({
      url: e,
    });
  }

  // 进入群聊会话
  const openChat = (item) => {
    if (!item?.id) return
    uni.navigateTo({
      url: `/partnerPages/chat?type=group&targetId=${item.id}&name=${encodeURIComponent(item.groupName || '群聊')}`,
    })
  }

  // 群列表统一由 onShow 加载(首次显示与从聊天页返回时都会触发),onMounted 不再重复请求
  onShow(() => {
    loadGroups()
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
    // background-color: #F8F7F8;
    min-height: 100vh;
    padding-bottom: 60rpx;
    padding-bottom: calc(80rpx + env(safe-area-inset-bottom) / 2);
    padding-bottom: calc(80rpx + constant(safe-area-inset-bottom));
  }
  
  /* 底部安全边距 start*/
  .tn-tabbar-height {
  	min-height: 60rpx;
  	height: calc(80rpx + env(safe-area-inset-bottom));
  	height: calc(80rpx + constant(safe-area-inset-bottom));
  }
  
  
  /* 新增OA色系，自行调用，或者拿色值去用，多种方式*/
  .oa-black{
    color: #1D2541;
    // background-color: #1D254150;
  }
  .oa-blue{
    color: #4B98FE;
    background-color: #4B98FE50;
  }
  .oa-orangeyellow{
    color: #FFAC00;
    background-color: #FFAC0050;
  }
  .oa-green{
    color: #00D05E;
    background-color: #00D05E50;
  }
  .oa-orange{
    color: #FE871B;
    background-color: #FE871B50;
  }
  .oa-cyan{
    color: #00C8B0;
    background-color: #00C8B050;
  }
  .oa-indigo{
    color: #00B9FE;
    background-color: #00B9FE50;
  }
  .oa-orangered{
    color: #FB6A67;
    background-color: #FB6A6750;
  }
  .oa-purple{
    color: #957BFE;
    background-color: #957BFE50;
  }
  
  /* 图标容器15 start */
  .icon15 {
    &__item {
      width: 30%;
      background-color: #FFFFFF;
      padding: 30rpx;
      margin: 20rpx 10rpx;
      transform: scale(1);
      transition: transform 0.3s linear;
      transform-origin: center center;
      
      &--icon {
        width: 90rpx;
        height: 90rpx;
        font-size: 60rpx;
        border-radius: 20rpx;
        position: relative;
        z-index: 1;
        
        &::after {
          content: " ";
          position: absolute;
          z-index: -1;
          width: 100%;
          height: 100%;
          left: 0;
          bottom: 0;
          border-radius: inherit;
          opacity: 1;
          transform: scale(1, 1);
          background-size: 100% 100%;
  
            
        }
      }
    }
  }

  /* 用户头像 start */
  .logo-image {
    width: 90rpx;
    height: 90rpx;
    position: relative;
  }
  
  .logo-pic {
    background-size: cover;
    background-repeat: no-repeat;
    // background-attachment:fixed;
    background-position: center;
    // border: 1rpx solid rgba(255,255,255,0.05);
    // box-shadow: 0rpx 0rpx 80rpx 0rpx rgba(0, 0, 0, 0.15);
    border-radius: 20rpx;
    overflow: hidden;
    // background-color: #FFFFFF;
  }
</style>
