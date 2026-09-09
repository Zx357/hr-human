<template>
  <view class="oa-content">
    <!-- 顶部自定义导航 -->
    <tn-navbar fixed home-icon="" :bottom-shadow="false" bg-color="#FFFFFF" :placeholder="false">
      <template #back><view class='tn-custom-nav-bar__back'
        @click="goBack">
        <tn-icon name="left-arrow" class="icon"></tn-icon>
      </view></template>
      <view class="tn-flex tn-flex-col-center tn-flex-row-center ">
        <text class="tn-text-bold tn-text-xl tn-color-black">添加同事</text>
      </view>
    </tn-navbar>

    <view class="tn-search-fixed tn-bg-white">
      <view class="tn-flex tn-flex-row-between tn-flex-col-center tn-margin" :style="{paddingTop: vuex_custom_bar_height + 'px'}">
        
        <view class="justify-content-item align-content-item" style="width: 100vw;">
          <view class="tn-flex tn-flex-col-center" style="border: 1rpx solid #00D05E;border-radius: 100rpx;padding: 10rpx 20rpx 10rpx 20rpx;width: 90%;">
            <tn-icon name="search" class="justify-content-item tn-padding-right-xs tn-color-gray tn-text-lg"></tn-icon>
            <input v-model="keyword" class="justify-content-item" placeholder="请输入姓名/工号/手机号" name="input" placeholder-style="color:#AAAAAA" style="width: 90%;"></input>
          </view>
        </view>
        
        <view class="align-content-item">
          <view class="justify-content-item tn-text-center">
            <tn-button
              bg-color="#00D05E"
              :custom-style="{padding:'20rpx 20rpx'}"
              width="150rpx"
              :fontSize="28"
              text-color="#FFFFFF"
              shape="round"
              @tap="searchUsers"
            >
              <text class="">搜 索</text>
            </tn-button>
    
          </view>
        </view>
      </view>
      
    </view>
    
    <!-- 页面内容 -->
    <view class="" :style="{paddingTop: vuex_custom_bar_height + 80 +'px'}">
      
      
      <view class="tn-flex tn-flex-row-between tn-flex-col-center" @click="tn('')" style="background-color: #FFFFFF;border-radius: 24rpx;margin: 0 30rpx 30rpx 30rpx;padding: 10rpx 0 10rpx 30rpx;position: relative;z-index: 9999;">
        <view class="justify-content-item tn-flex tn-flex-col-center">
          <view class="justify-content-item icon15__item--icon tn-flex tn-flex-row-center tn-flex-col-center tn-color-white" style="background-color: #00B9FE;">
            <tn-icon name="scan"></tn-icon>
          </view>
          <view class="justify-content-item tn-margin-sm">
            <view class="tn-text-bold tn-text-lg">扫一扫</view>
            <view class="tn-text-sm tn-color-gray tn-padding-top-xs">前往扫描</view>
          </view>
        </view>
        
        <view class="justify-content-item tn-margin-sm tn-text-lg">
          <tn-icon name="right" class="tn-color-gray"></tn-icon>
        </view>
      </view>
      
      <view class="tn-flex tn-flex-row-between tn-flex-col-center" @click="tn('')" style="background-color: #FFFFFF;border-radius: 24rpx;margin: 0 30rpx 30rpx 30rpx;padding: 10rpx 0 10rpx 30rpx;position: relative;z-index: 9999;">
        <view class="justify-content-item tn-flex tn-flex-col-center">
          <view class="justify-content-item icon15__item--icon tn-flex tn-flex-row-center tn-flex-col-center tn-color-white" style="background-color: #00C8B0;">
            <tn-icon name="circle-more"></tn-icon>
          </view>
          <view class="justify-content-item tn-margin-sm">
            <view class="tn-text-bold tn-text-lg">面对面添加</view>
            <view class="tn-text-sm tn-color-gray tn-padding-top-xs">多人互加</view>
          </view>
        </view>
        
        <view class="justify-content-item tn-margin-sm tn-text-lg">
          <tn-icon name="right" class="tn-color-gray"></tn-icon>
        </view>
      </view>
      
      <view class="tn-flex tn-flex-row-between tn-flex-col-center" @click="tn('')" style="background-color: #FFFFFF;border-radius: 24rpx;margin: 0 30rpx 30rpx 30rpx;padding: 10rpx 0 10rpx 30rpx;position: relative;z-index: 9999;">
        <view class="justify-content-item tn-flex tn-flex-col-center">
          <view class="justify-content-item icon15__item--icon tn-flex tn-flex-row-center tn-flex-col-center tn-color-white" style="background-color: #1485EE;">
            <tn-icon name="qr-code"></tn-icon>
          </view>
          <view class="justify-content-item tn-margin-sm">
            <view class="tn-text-bold tn-text-lg">我的二维码</view>
            <view class="tn-text-sm tn-color-gray tn-padding-top-xs">前往查看</view>
          </view>
        </view>
        
        <view class="justify-content-item tn-margin-sm tn-text-lg">
          <tn-icon name="right" class="tn-color-gray"></tn-icon>
        </view>
      </view>
      
      
      
      <view
        v-for="item in searchResults"
        :key="item.id"
        class="tn-flex tn-flex-row-between tn-flex-col-center"
        style="background-color: #FFFFFF;border-radius: 24rpx;margin: 0 30rpx 30rpx 30rpx;padding: 20rpx 20rpx 20rpx 30rpx;position: relative;z-index: 9999;"
      >
        <view class="justify-content-item tn-flex tn-flex-col-center">
          <image :src="formatAvatar(item.avatar)" mode="aspectFill" style="width: 80rpx;height: 80rpx;border-radius: 16rpx;"></image>
          <view class="justify-content-item tn-margin-sm">
            <view class="tn-text-bold tn-text-lg">{{ item.name || item.employeeNo }}</view>
            <view class="tn-text-sm tn-color-gray tn-padding-top-xs">{{ item.deptName || item.position || item.post || '未设置部门' }}</view>
          </view>
        </view>
        <tn-button
          bg-color="#00D05E"
          :custom-style="{padding:'18rpx 24rpx'}"
          :fontSize="24"
          text-color="#FFFFFF"
          shape="round"
          @tap="sendRequest(item)"
        >
          <text>添加</text>
        </tn-button>
      </view>

      <view v-if="searched && !searchResults.length" style="padding: 10vh 20rpx;">
        <view class="tn-text-center" style="font-size: 160rpx;">
          <tn-icon name="wea-wind" class="tn-color-gray--disabled"></tn-icon>
        </view>
        <view class="tn-color-gray tn-text-center tn-text-lg tn-padding-top-sm">未找到同事</view>
      </view>

      <!-- 搜索好友为空-->
      <!-- <view class="" style="padding: 10vh 20rpx;">
        <view class="tn-text-center" style="font-size: 160rpx;">
          <tn-icon name="wea-wind" class="tn-color-gray--disabled"></tn-icon>
        </view>
        <view class="tn-color-gray tn-text-center tn-text-lg tn-padding-top-sm">该用户不存在</view>
      </view>   -->
      
      
      
    </view>

  </view>

</template>

<script setup>
  import { ref } from 'vue'
  import { useCustomBarHeight, useGoBack } from '@/libs/composables'
  import config from '@/config'
  import { searchContacts, sendContactRequest } from '@/api/contact'
  // 使用 composable 获取自定义导航栏高度
  const { vuex_custom_bar_height } = useCustomBarHeight()
  const { goBack } = useGoBack()
  const keyword = ref('')
  const searched = ref(false)
  const searchResults = ref([])

  const formatAvatar = (avatar) => {
    if (!avatar) return '/static/author.jpg'
    if (/^https?:\/\//.test(avatar) || avatar.startsWith('/static')) return avatar
    return config.baseUrl + avatar
  }

  const searchUsers = async () => {
    if (!keyword.value.trim()) {
      uni.showToast({
        title: '请输入搜索内容',
        icon: 'none'
      })
      return
    }
    try {
      const res = await searchContacts({
        pageNum: 1,
        pageSize: 20,
        keyword: keyword.value.trim()
      })
      searched.value = true
      searchResults.value = res.data?.records || []
    } catch (error) {
      console.log('搜索同事失败', error)
    }
  }

  const sendRequest = async (item) => {
    try {
      await sendContactRequest({
        targetEmployeeId: item.id,
        remark: '申请添加为好友'
      })
      uni.showToast({
        title: '申请已发送',
        icon: 'success'
      })
    } catch (error) {
      console.log('发送好友申请失败', error)
    }
  }
  
  // 跳转
  const tn = (e) => {
    uni.navigateTo({
      url: e,
    });
  }
</script>

<style lang="scss" scoped>
  /* 胶囊*/
  .tn-custom-nav-bar__back {
    z-index: 9999;
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
    font-size: 32rpx;
    
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
  
  .tn-search-fixed{
    max-width: 640px;
    margin: 0 auto;
    position: fixed;
    top: 0rpx;
    width: 100%;
    transition: all 0.25s ease-out;
    z-index: 999;
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
    border-radius: 100rpx;
    overflow: hidden;
    // background-color: #FFFFFF;
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
        width: 80rpx;
        height: 80rpx;
        font-size: 50rpx;
        border-radius: 50%;
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
</style>
