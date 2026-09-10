<template>
  <view class="oa-content">
    <!-- 顶部自定义导航 -->
    <tn-navbar fixed home-icon="" :placeholder="false" :bottom-shadow="false" bg-color="#FFFFFF00">
      <template #back><view class='tn-custom-nav-bar__back'
        @click="goBack">
        <tn-icon class='icon' name='left-arrow'></tn-icon>
      </view></template>
      <view class="tn-flex tn-flex-col-center tn-flex-row-center ">
        <text class="tn-text-bold tn-text-xl tn-color-black">提交结果</text>
      </view>
    </tn-navbar>
    
    
    <view class="tn-text-center" :style="{paddingTop: vuex_custom_bar_height + 60 + 'px'}">
      <view class="tn-page-success" style="font-size: 420rpx;line-height: 420rpx"></view>
      <!-- <view class="tn-page-error" style="font-size: 420rpx;line-height: 420rpx"></view> -->
      <view class="tn-margin-top">
        <view class="tn-text-lg tn-margin-bottom tn-text-bold" style="color: #00C8B0;">提交成功</view>
        <!-- <view class="tn-text-lg tn-margin-bottom tn-text-bold" style="color: #FB6A67;">提交失败</view> -->
        <view class="tn-color-gray">你已完成提交</view>
        <view class="tn-color-gray">可在"我的申请"中查看审批进度</view>
      </view>
    </view>
    
    <!-- 悬浮按钮-->
    <!-- <view class="tn-footerfixed">
      <view class="tn-margin">
        <tn-button backgroundColor="#00C8B0 " :custom-style="{padding:'40rpx 0'}" width="100%" :fontSize="28" fontColor="#FFFFFF" @click="tn('/workPages/details')">
          <text class="">查看详情</text>
        </tn-button>
      </view>
      <view class="tn-margin">
        <tn-button backgroundColor="#00C8B0 " :custom-style="{padding:'39rpx 0'}" width="100%" :fontSize="28" :plain="true" fontColor="#00C8B0" @click="tnindex('/pages/index?index=2')">
          <text class="">返回首页</text>
        </tn-button>
      </view>
    </view> -->
    
    <!-- 悬浮按钮-->
    <view class="tn-flex tn-padding tn-margin-top-lg">
      <view class="tn-flex-1 justify-content-item tn-margin-right-xs tn-text-center tn-bg-white" style="border-radius: 100rpx;">
        <tn-button
          bg-color="#00C8B0"
          :custom-style="{padding:'39rpx 0'}"
          width="48%"
          :fontSize="28"
          text-color="#00C8B0"
          shape="round"
          :plain="true"
          @click="tnindex('/pages/index?index=2')"
        >
          <text class="">返回首页</text>
        </tn-button>
      </view>
      <!-- <view class="tn-flex-1 justify-content-item tn-margin-right-xs tn-text-center tn-bg-white" style="border-radius: 100rpx;">
        <tn-button backgroundColor="#00C8B0 " :custom-style="{padding:'39rpx 0'}" width="48%" :fontSize="28" :plain="true" fontColor="#00C8B0" shape="round"  @click="tn('')">
          <text class="">重新提交</text>
        </tn-button>
      </view> -->
    </view>
    
    
  </view>
</template>

<script setup>
import { useCustomBarHeight, useGoBack } from '@/libs/composables'
// 使用 composable 获取自定义导航栏高度
const { vuex_custom_bar_height } = useCustomBarHeight()
const { goBack } = useGoBack()

// 跳转
function tn(e) {
  uni.navigateTo({
    url: e,
  });
}

// 返回首页
function tnindex(e) {
  uni.reLaunch({
    url: e,
  });
}
</script>

<script>
export default {
  name: 'TemplatePrompt',
  
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
    // background-color: #F8F7F8;
    min-height: 100vh;
    padding-bottom: 60rpx;
    padding-bottom: calc(80rpx + env(safe-area-inset-bottom) / 2);
    padding-bottom: calc(80rpx + constant(safe-area-inset-bottom));
  }
  
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
