<template>
  <view class="oa-content">
    <!-- 顶部自定义导航 -->
    <tn-navbar fixed home-icon="" :placeholder="false" :bottom-shadow="false" bg-color="#05C160">
      <template #back><view class='tn-custom-nav-bar__back'
        @click="goBack">
        <tn-icon class='icon' name="left-arrow"></tn-icon>
      </view></template>
      <view class="tn-flex tn-flex-col-center tn-flex-row-center ">
        <text class="tn-text-bold tn-text-xl tn-color-white">指纹门锁</text>
      </view>
    </tn-navbar>
    
    <view class="">
      <view class="top-fixed">
        <view class="tn-text-center" style="position: absolute;top: 160rpx;right:30rpx;font-size: 200rpx;opacity: 0.1;">
          <tn-icon class="tn-color-white" name="fingerprint"></tn-icon>
        </view>
        <view class="tn-margin tn-padding-left-sm tn-padding-bottom-xl tn-padding-top tn-color-white">
          <view class="tn-text-sm">
            <text style="opacity: 0.6;">当前电量</text>
          </view>
          <view class="tn-text-bold tn-padding-top-xs" style="font-size:70rpx">
            98 
            <text class="tn-text-sm tn-padding-left-xs" style="opacity: 0.6;">%</text>
          </view>
        </view>
        <view class="tn-margin tn-padding-left-sm tn-color-white">
          <view class="tn-flex tn-flex-row-between tn-flex-col-center">
            <view class="justify-content-item tn-flex-1">
              <view class="tn-text-sm" style="opacity: 0.6;">使用时长</view>
              <view class="tn-text-lg tn-text-bold tn-padding-top-xs">129 天</view>
            </view>
            <view class="justify-content-item tn-flex-1">
              <view class="tn-text-sm" style="opacity: 0.6;">温度</view>
              <view class="tn-text-lg tn-text-bold tn-padding-top-xs">27 °C</view>
            </view>
            <view class="justify-content-item tn-flex-1">
              <view class="tn-text-sm" style="opacity: 0.6;">湿度</view>
              <view class="tn-text-lg tn-text-bold tn-padding-top-xs">32 %</view>
            </view>
          </view>
        </view>
      </view>
      
      <view class="tn-padding-bottom-xs tn-padding-top-xs" style="border-radius: 15rpx;background-color: #FFFFFF;margin: 50rpx 30rpx 30rpx 30rpx;">
        <view class="tn-padding tn-flex tn-flex-row-between tn-flex-col-center">
          <view class="tn-flex justify-content-item tn-flex-col-center">
            <view class="tn-text-bold tn-text-lg">
              临时密码
            </view>
            <view class="tn-text-sm tn-color-gray">
              （使用后4小时内自动失效）
            </view>
          </view>
          <!-- <view class="justify-content-item tn-text-xl tn-color-grey">
            <tn-icon name="delete"></tn-icon>
          </view> -->
        </view>
        <view class="tn-margin-sm" style="background-color: #00C8B008;color: #00C8B0;border-radius: 10rpx;">
          <view class="tn-flex tn-flex-row-between tn-flex-col-center tn-padding">
            <view class="justify-content-item tn-text-left" style="width: 80%;">
              <tn-icon name="safe-fill"></tn-icon>
              <text class="tn-padding-left-xs tn-padding-right-sm">临时密码</text>
              <text>208964</text>
              <tn-icon class="tn-padding-left-xs" name="copy"></tn-icon>
            </view>
            <view class="justify-content-item">
              <tn-icon name="refresh-simple"></tn-icon>
            </view>
          </view>
        </view>
      </view>
      
      <view class="tn-padding-bottom-xs tn-padding-top-xs" style="border-radius: 15rpx;background-color: #FFFFFF;margin: 30rpx 30rpx 30rpx 30rpx;">
        <view class="tn-padding tn-flex tn-flex-row-between tn-flex-col-center">
          <view class="tn-flex justify-content-item tn-flex-col-center">
            <view class="tn-text-bold tn-text-lg">
              设置密码
            </view>
            <view class="tn-text-sm tn-color-gray">
              （密码必须为6位数字）
            </view>
          </view>
          <!-- <view class="justify-content-item tn-text-xl tn-color-grey">
            <tn-icon name="add-circle"></tn-icon>
          </view> -->
        </view>
        <view class="tn-margin-sm" style="background-color: #00C8B008;color: #00C8B0;border-radius: 10rpx;" v-for="(item, index) in 3" :key="index" @click="tn('')">
          <view class="tn-flex tn-flex-row-between tn-flex-col-center tn-padding">
            <view class="justify-content-item tn-text-left" style="width: 80%;">
              <tn-icon name="safe-fill"></tn-icon>
              <text class="tn-padding-left-xs tn-padding-right-sm">安全密码</text>
              <text>612809</text>
              <tn-icon class="tn-padding-left-xs" name="copy"></tn-icon>
            </view>
            <view class="justify-content-item">
              <tn-icon name="edit"></tn-icon>
            </view>
          </view>
        </view>
      </view>
      
      
      <view class="tn-footerfixed tn-padding tn-safe-area-inset-bottom dd-glass">
        <!-- 方式12 start-->
        <view class="tn-flex tn-flex-wrap">
          <view v-for="(item, index) in linksData1" :key="index" style="width: 25%;" @click="tn(item.path)">
            <view class="tn-margin-bottom tn-padding-sm">
              <view class="tn-flex tn-flex-direction-column tn-flex-row-center tn-flex-col-center">
                <!-- 预留的图片形式 -->
                <!-- <view class="icon12__item--icon tn-flex tn-flex-row-center tn-flex-col-center" :style="'background-image:url('+ item.url +');background-size:100% 100%;background-size: cover;'">
                </view> -->
                <!-- 字体图标形式-->
                <view class="icon12__item--icon tn-flex tn-flex-row-center tn-flex-col-center" :class="[item.select ? 'icon-select' : 'icon-select-no']">
                  <view :class="['tn-icon-' + item.icon]"></view>
                </view> 
                <view class="tn-text-center" :class="[item.select ? 'color-select' : 'tn-color-gray']">
                  <text class="tn-text-ellipsis tn-text-sm">{{ item.title }}</text>
                </view>
              </view>
            </view>
         </view>
        </view>
        <!-- 方式12 end-->
      </view>
      
      <view class="tn-tabbar-height"></view>
      
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { useGoBack } from '@/libs/composables'
const { goBack } = useGoBack()

const linksData1 = ref([{
    url: 'https://resource.tuniaokj.com/images/flower/guye1.jpg',
    title: '开 关',
    icon:'power',
    select: false
  },
  {
    url: 'https://resource.tuniaokj.com/images/flower/guye1.jpg',
    title: '智 能',
    icon:'trusty',
    select: true
  },
  {
    url: 'https://resource.tuniaokj.com/images/flower/guye1.jpg',
    title: '睡 眠',
    icon:'moon',
    select: false
  },
  {
    url: 'https://resource.tuniaokj.com/images/flower/guye1.jpg',
    title: '离 家',
    icon:'notice',
    select: false
  }
])

// 跳转
function tn(e) {
  uni.navigateTo({
    url: e,
  });
}
</script>

<script>
export default {
  name: 'TemplateContent',
  
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
  
  /* 毛玻璃*/
  .dd-glass {
     width: 100%;
     backdrop-filter: blur(20rpx);
    -webkit-backdrop-filter: blur(20rpx);
  }
  
  /* 图标容器12 start */
  .icon12 {
    &__item {
      width: 30%;
      background-color: #FFFFFF;
      border-radius: 10rpx;
      padding: 30rpx;
      margin: 20rpx 10rpx;
      transform: scale(1);
      transition: transform 0.3s linear;
      transform-origin: center center;

      &--icon {
        width: 100rpx;
        height: 100rpx;
        font-size: 50rpx;
        border-radius: 50%;
        margin-bottom: 18rpx;
        position: relative;
        z-index: 1;
      }
    }
  }
  
  .top-fixed{
    background: linear-gradient(90deg, #05C160, #05C160);
    padding-top: 120rpx;
    width: 100%;
    height: 520rpx;
    transition: all 0.25s ease-out;
  }
  
  .icon-select{
    border: 1rpx solid #00C8B0;
    color: #00C8B0;
  }
  
  .icon-select-no{
    border: 1rpx solid #E6E6E6;
    color: #AAAAAA;
  }
  
  .color-select{
    color: #00C8B0;
  }
  
  /* 底部安全边距 start*/
  .tn-tabbar-height {
  	min-height: 220rpx;
  	height: calc(240rpx + env(safe-area-inset-bottom) / 2);
    height: calc(240rpx + constant(safe-area-inset-bottom));
  }
  .tn-footerfixed {
    max-width: 640px;
    margin: 0 auto;
    position: fixed;
    background-color: rgba(255,255,255,0.5);
    box-shadow: 0rpx 0rpx 30rpx 0rpx rgba(0, 0, 0, 0.07);
    bottom: 0;
    width: 100%;
    transition: all 0.25s ease-out;
    will-change: transform;
    z-index: 100;
  }
</style>
