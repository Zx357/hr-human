<template>
  <view class="oa-content">
    <!-- 顶部自定义导航 -->
    <tn-navbar fixed home-icon="" :bottom-shadow="false" bg-color="#FFFFFF00" :placeholder="false">
      <template #back><view class='tn-custom-nav-bar__back'
        @click="goBack">
        <tn-icon name="left-arrow" class="icon"></tn-icon>
      </view></template>
    </tn-navbar>
    
    
    
    <view class="oa-topbg" :style="{paddingTop: vuex_custom_bar_height + 30 + 'px'}">
      <view class="tn-text-center" style="position: absolute;top: -20rpx;right:-50rpx;font-size: 400rpx;line-height: 400rpx;opacity: 0.1;transform: rotate(39deg);">
        <tn-icon name="prize" class="tn-color-white"></tn-icon>
      </view>
      <view class="tn-flex tn-flex-row-center tn-color-white" style="position: relative;">
        <view class="justify-content-item">
          <tn-icon name="success-circle-fill" style="font-size: 100rpx;line-height: 100rpx;"></tn-icon>
          <tn-icon name="starry" style="position: absolute;"></tn-icon>
        </view>
        <view class="justify-content-item tn-padding-left">
          <view class="tn-text-bold tn-text-xl">兑换成功！</view>
          <view class="tn-text-sm tn-padding-top-xs" style="opacity: 0.8;">感谢鼎力支持吖</view>
        </view>
      </view>
      
    </view>
    
    <view class="tn-padding tn-text-justify" style="background-color: #FFFFFF;border-radius:24rpx;margin: -120rpx 30rpx 30rpx 30rpx;position: relative;z-index: 1;">
      <view class="tn-text-center tn-strip-bottom-dashed tn-padding-bottom tn-padding-top-sm tn-margin-bottom">
        <view class="" style="font-size: 62rpx;">
          <text class="">129</text>
          <text class="tn-margin-left-xs tn-text-xs tn-color-gray">图鸟币</text>
        </view>
      </view>
      <view class="tn-color-gray tn-padding-top" @click="copySource">
        订单编号
        <text class="tn-color-black tn-padding-left-sm">TN202412265029</text>
        <tn-icon name="copy" class="tn-padding-left-xs tn-color-blue--light"></tn-icon>
      </view>
      <view class="tn-color-gray tn-padding-top">兑换时间<text class="tn-color-black tn-padding-left-sm">2024-12-26 10:26:29</text></view>
      <view class="tn-color-gray tn-padding-top tn-padding-bottom">支付方式<text class="tn-color-black tn-padding-left-sm">图鸟币</text></view>
    </view>
    
    <view class="tn-flex tn-padding tn-margin-top-lg">
      <view class="tn-flex-1 justify-content-item tn-margin-left-xs tn-text-center">
        <tn-button
          bg-color="#05C160"
          :custom-style="{padding:'40rpx 0'}"
          width="48%"
          :fontSize="28"
          text-color="#FFFFFF"
          shape="round"
          @click="tn('')"
        >
          <text class="">订单详情</text>
        </tn-button>
      </view>
    </view>
    <view class="tn-text-center">
      <view class="tn-color-gray tn-text-sm">点击详情，向人事出示订单页完成扫码核销 ~</view>
    </view>
    
    
  </view>
</template>

<script setup>
defineOptions({
  name: 'TemplatePrompt'
})

import { useCustomBarHeight, useGoBack } from '@/libs/composables'
// 使用 composable 获取自定义导航栏高度
const { vuex_custom_bar_height } = useCustomBarHeight()
const { goBack } = useGoBack()

// 跳转
const tn = (e) => {
  uni.navigateTo({
    url: e,
  });
}

// 返回首页
const tnindex = (e) => {
  uni.reLaunch({
    url: e,
  });
}

// 复制开源地址
const copySource = () => {
  // #ifdef MP-WEIXIN
  wx.vibrateShort();
  // #endif
  uni.setClipboardData({
    data: "https://ext.dcloud.net.cn/publisher?id=356088",
  })
}
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
  
  
  .oa-topbg{
    position: relative;
    background: linear-gradient(120deg, #04ca64, #05C160);
    top: 0;
    width: 100%;
    height: 540rpx;
    z-index: 0;
    overflow: hidden;
  }
  
  /* 间隔线 start*/
  .tn-strip-bottom-dashed {
    width: 100%;
    border-bottom: 2rpx dashed #E6E6E6;
  }
  
  .tn-strip-bottom {
   width: 100%;
   border-bottom: 20rpx solid rgba(241, 241, 241, 0.8);
  }
   /* 间隔线 end*/
  
  /* 底部悬浮按钮 start*/
  .tn-tabbar-height {
  	min-height: 160rpx;
  	height: calc(180rpx + env(safe-area-inset-bottom) / 2);
    height: calc(180rpx + constant(safe-area-inset-bottom));
  }
  .tn-footerfixed {
    position: fixed;
    width: 100%;
    bottom: calc(40rpx + env(safe-area-inset-bottom));
    z-index: 1024;
    box-shadow: 0 1rpx 6rpx rgba(0, 0, 0, 0);
  }
  /* 底部悬浮按钮 end*/
  
</style>
