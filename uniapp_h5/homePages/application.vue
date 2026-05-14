<template>
  <view class="oa-content">
    <!-- 顶部自定义导航 -->
    <tn-navbar fixed home-icon="" :bottom-shadow="false" bg-color="#FFFFFF" :placeholder="false">
      <view slot="back" class='tn-custom-nav-bar__back'
        @click="goBack">
        <tn-icon name="left-arrow" class='icon'></tn-icon>
      </view>
      <view class="tn-flex tn-flex-col-center tn-flex-row-center ">
        <text class="tn-text-bold tn-text-xl tn-color-black">应用消息</text>
      </view>
    </tn-navbar>
    
    
    <view class="" :style="{paddingTop: vuex_custom_bar_height+10+'px'}">
      
      <view class="" @click="tn('/homePages/xxxx')">
        <view class="image-pic" style="background-image:url('https://resource.tuniaokj.com/images/oa/enterprise.jpg');background-size: cover;background-position: center;">
          <view class="image-design">
            <view class="tn-text-center" style="width: 120rpx;height: 50rpx;position: absolute;top: 0;right:0;border-radius: 0 12rpx 0 12rpx;font-size: 22rpx;background-color: #00000050;">
              <view class="tn-margin-xs tn-color-white">未 读</view>
            </view>
            <view class="" style="width: 100%;min-height: 120rpx;background: linear-gradient(0deg, rgba(0,0,0,0.6), rgba(0,0,0,0.2), rgba(0,0,0,0));position: absolute;bottom: 0;">
              <view class="tn-text-lg tn-padding-top-xl tn-padding-left tn-padding-right tn-color-white tn-text-bold clamp-text-1">
                下班时间到了，勿浪费公司水电，请滚
              </view>
            </view>
          </view>
        </view>
        <view class="content-bg tn-padding">
          <!-- <view class="tn-text-justify clamp-text-1 tn-text-bold tn-margin-bottom-sm">
            标题标题标题标题标题标题标题标题标题标题标题标题标题标题
          </view>
          <view class="tn-text-justify clamp-text-2 tn-padding-bottom tn-color-gray--dark">
            内容内容内容内容内容内容内容内容内容内容内容内容内容内容
          </view> -->
          <view class="tn-flex tn-flex-direction-row tn-flex-col-center tn-flex-row-between">
            <view class="tn-flex">
              <view class="tn-flex user-pic">
                <view class="user-image" style="background-image:url('https://resource.tuniaokj.com/images/logo/logo.png');width: 35rpx;height: 35rpx;background-size: cover;">
                </view>
              </view>
              <view class="tn-flex tn-margin-left-xs" style="width: 300rpx;">
                <text class="clamp-text-1 tn-color-gray--dark">图鸟官方团队</text>
              </view>
            </view>
            <view class="tn-color-gray">
              2024-02-28 18:00
            </view>
          </view>
        </view>
      </view>
      
      <view class="" @click="tn('/homePages/xxxx')">
        <view class="image-pic" style="background-image:url('https://cdn.nlark.com/yuque/0/2023/jpeg/280373/1681955091346-assets/web-upload/e05d86a5-1cec-40b0-8198-734d8b8379da.jpeg');background-size: cover;background-position: center;">
          <view class="image-design">
            <!-- <view class="tn-text-center" style="width: 120rpx;height: 50rpx;position: absolute;top: 0;right:0;border-radius: 0 12rpx 0 12rpx;font-size: 22rpx;background-color: #00000050;">
              <view class="tn-margin-xs tn-color-white">未 读</view>
            </view> -->
            <view class="" style="width: 100%;min-height: 120rpx;background: linear-gradient(0deg, rgba(0,0,0,0.6), rgba(0,0,0,0.2), rgba(0,0,0,0));position: absolute;bottom: 0;">
              <view class="tn-text-lg tn-padding-top-xl tn-padding-left tn-padding-right tn-color-white tn-text-bold clamp-text-1">
                新年新气象，除夕快乐
              </view>
            </view>
          </view>
        </view>
        <view class="content-bg tn-padding">
          <!-- <view class="tn-text-justify clamp-text-1 tn-text-bold tn-margin-bottom-sm">
            标题标题标题标题标题标题标题标题标题标题标题标题标题标题
          </view>
          <view class="tn-text-justify clamp-text-2 tn-padding-bottom tn-color-gray--dark">
            内容内容内容内容内容内容内容内容内容内容内容内容内容内容
          </view> -->
          <view class="tn-flex tn-flex-direction-row tn-flex-col-center tn-flex-row-between">
            <view class="tn-flex">
              <view class="tn-flex user-pic">
                <view class="user-image" style="background-image:url('https://resource.tuniaokj.com/images/logo/logo.png');width: 35rpx;height: 35rpx;background-size: cover;">
                </view>
              </view>
              <view class="tn-flex tn-margin-left-xs" style="width: 300rpx;">
                <text class="clamp-text-1 tn-color-gray--dark">图鸟官方团队</text>
              </view>
            </view>
            <view class="tn-color-gray">
              2024-02-09 16:28
            </view>
          </view>
        </view>
      </view>
      
      <view class="" @click="tn('/homePages/certification')">
        <view class="image-pic" style="background-image:url('https://cdn.nlark.com/yuque/0/2021/jpeg/280373/1634788556599-assets/web-upload/92c9033d-8738-4ed6-a430-93192664437a.jpeg');background-size: cover;background-position: center;">
          <view class="image-design">
            <!-- <view class="tn-text-center" style="width: 120rpx;height: 50rpx;position: absolute;top: 0;right:0;border-radius: 0 12rpx 0 12rpx;font-size: 22rpx;background-color: #00000050;">
              <view class="tn-margin-xs tn-color-white">未 读</view>
            </view> -->
            <view class="" style="width: 100%;min-height: 120rpx;background: linear-gradient(0deg, rgba(0,0,0,0.6), rgba(0,0,0,0.2), rgba(0,0,0,0));position: absolute;bottom: 0;">
              <view class="tn-text-lg tn-padding-top-xl tn-padding-left tn-padding-right tn-color-white tn-text-bold clamp-text-1">
                企业认证，到期后企业使用将受限
              </view>
            </view>
          </view>
        </view>
        <view class="content-bg tn-padding">
          <!-- <view class="tn-text-justify clamp-text-1 tn-text-bold tn-margin-bottom-sm">
            标题标题标题标题标题标题标题标题标题标题标题标题标题标题
          </view> -->
          <view class="tn-text-justify clamp-text-2 tn-padding-bottom tn-color-gray--dark">
            请于2024年06月08日前认证，否则认证标识将变为未认证状态，部分功能也将受限，点击
            <text class="tn-color-blue tn-padding-left-xs">立即认证</text>
            <tn-icon name="safe-fill" class="tn-color-blue"></tn-icon>
          </view>
          <view class="tn-flex tn-flex-direction-row tn-flex-col-center tn-flex-row-between">
            <view class="tn-flex">
              <view class="tn-flex user-pic">
                <view class="user-image" style="background-image:url('https://resource.tuniaokj.com/images/logo/logo.png');width: 35rpx;height: 35rpx;background-size: cover;">
                </view>
              </view>
              <view class="tn-flex tn-margin-left-xs" style="width: 300rpx;">
                <text class="clamp-text-1 tn-color-gray--dark">图鸟官方团队</text>
              </view>
            </view>
            <view class="tn-color-gray">
              2024-01-22 16:28
            </view>
          </view>
        </view>
      </view>
        
    </view>
  </view>
</template>

<script setup>
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
    // box-shadow: 0rpx 0rpx 50rpx 0rpx rgba(0, 0, 0, 0.07);
  }
  .image-design{
    padding: 150rpx 0rpx;
    position: relative;
  }
  .image-pic{
    margin: 30rpx 30rpx 0 30rpx;
    // border: 1rpx solid rgba(0, 0, 0, 0.04);
    background-size: cover;
    background-repeat:no-repeat;
    // background-attachment:fixed;
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
    // background-attachment:fixed;
    background-position: top;
    border: 1rpx solid rgba(255,255,255,0.05);
    box-shadow: 0rpx 0rpx 80rpx 0rpx rgba(0, 0, 0, 0.15);
    border-radius: 50%;
    overflow: hidden;
    // background-color: #FFFFFF;
  }
</style>
