<template>
  <view class="oa-content">
    <!-- 顶部自定义导航 -->
    <tn-navbar fixed :bottom-shadow="false" bg-color="#FFFFFF" :placeholder="false">
      <view slot="back" class='tn-custom-nav-bar__back'
        @click="goBack">
        <tn-icon class='icon' name="left-arrow"></tn-icon>
      </view>
      <view class="tn-flex tn-flex-col-center tn-flex-row-center tn-padding-left">
        <text class="tn-text-bold tn-text-xl tn-color-black">消息互动</text>
        <tn-icon class="tn-text-xl tn-padding-left-sm tn-color-gray" name="circle-arrow"></tn-icon>
      </view>
    </tn-navbar>
    
    
    
    <view class="" :style="{paddingTop: vuex_custom_bar_height + 'px'}">
      
      <view class="tn-text-justify">
        
        <view class="tn-margin">
          <view class="tn-flex tn-flex-row-between">
            <view class="justify-content-item">
              <view class="tn-flex tn-flex-col-top tn-flex-row-left">
                <view class="logo-pic tn-margin-top-sm" style="background-image:url('https://cdn.nlark.com/yuque/0/2023/jpeg/280373/1683551116291-assets/web-upload/c4c0d31c-88ee-42c7-99ba-139ef206aaed.jpeg')">
                  <view class="logo-image" >
                  </view>
                </view>
                <view class="tn-padding-right tn-color-black">
                  
                  <view class="tn-padding-left-sm tn-padding-top-sm" style="max-width: 50vw;">
                    <view class="tn-flex tn-padding-right">
                      <view class="justify-content-item tn-flex-col-center tn-flex-row-center tn-text-bold tn-text-lg">
                        <text class="" style="">不许凶我吖</text>
                      </view>
                      <view class="justify-content-item tn-margin-left-xs tn-text-xs tn-color-red">
                        未读
                      </view>
                    </view>
                  </view>
                  <view class="tn-flex tn-flex-col-center">
                    <view class="tn-margin-sm tn-padding-sm" style="max-width: 50vw;border-radius: 0 15rpx 15rpx 15rpx;background-color: #F9F9F9;">
                      <text class="">这个是评论记录</text>
                    </view>
                  </view>
                  <view class="tn-padding-left-sm tn-color-gray" style="max-width: 50vw;">
                    <text class="tn-text-sm">16分钟前</text>
                  </view>
                  
                </view>
                
              </view>
            </view>
            <view class="justify-content-item">
              <view class="logo-pic2 tn-margin-top-sm" style="background-image:url('https://cdn.nlark.com/yuque/0/2023/jpeg/280373/1692940242362-assets/web-upload/39542345-83ab-456c-b309-e156174d5561.jpeg')">
                <view class="logo-image2">
                </view>
              </view>
            </view>
          </view>
        </view>
        
        
        
        <view class="tn-margin">
          <view class="tn-flex tn-flex-row-between">
            <view class="justify-content-item">
              <view class="tn-flex tn-flex-col-top tn-flex-row-left">
                <view class="logo-pic tn-margin-top-sm" style="background-image:url('https://cdn.nlark.com/yuque/0/2023/jpeg/280373/1692940242480-assets/web-upload/f09e233b-953b-4b1a-8a77-2a75f2445655.jpeg')">
                  <view class="logo-image" >
                  </view>
                </view>
                <view class="tn-padding-right tn-color-black">
                  
                  <view class="tn-padding-left-sm tn-padding-top-sm" style="max-width: 50vw;">
                    <view class="tn-flex tn-padding-right">
                      <view class="justify-content-item tn-flex-col-center tn-flex-row-center tn-text-bold tn-text-lg">
                        <text class="" style="">抓住那只猪</text>
                      </view>
                      <view class="justify-content-item tn-margin-left-xs tn-text-xs tn-color-red">
                        未读
                      </view>
                    </view>
                  </view>
                  <view class="tn-flex tn-flex-col-center">
                    <view class="tn-margin-sm" style="max-width: 50vw;border-radius: 0 15rpx 15rpx 15rpx;">
                      <text class="">赞了你的帖子</text>
                    </view>
                    <view class="">
                      <tn-icon class="tn-color-red tn-text-xxl" name="like-fill"></tn-icon>
                    </view>
                  </view>
                  <view class="tn-padding-left-sm tn-color-gray" style="max-width: 50vw;">
                    <text class="tn-text-sm">8月22日 09:08</text>
                  </view>
                  
                </view>
                
              </view>
            </view>
            <view class="justify-content-item">
              <view class="logo-pic2 tn-margin-top-sm" style="background-image:url('https://cdn.nlark.com/yuque/0/2023/jpeg/280373/1692940242388-assets/web-upload/471a3e0d-246d-4daa-ab83-43e70011177c.jpeg')">
                <view class="logo-image2">
                </view>
              </view>
            </view>
          </view>
        </view>
        
        <view class="tn-margin">
          <view class="tn-flex tn-flex-row-between">
            <view class="justify-content-item">
              <view class="tn-flex tn-flex-col-top tn-flex-row-left">
                <view class="logo-pic tn-margin-top-sm" style="background-image:url('https://cdn.nlark.com/yuque/0/2023/jpeg/280373/1692940242423-assets/web-upload/6d82b929-67e4-4919-a826-c931940f5872.jpeg')">
                  <view class="logo-image" >
                  </view>
                </view>
                <view class="tn-padding-right tn-color-black">
                  
                  <view class="tn-padding-left-sm tn-padding-top-sm" style="max-width: 50vw;">
                    <view class="tn-flex tn-padding-right">
                      <view class="justify-content-item tn-flex-col-center tn-flex-row-center tn-text-bold tn-text-lg">
                        <text class="" style="">猪不见了</text>
                      </view>
                    </view>
                  </view>
                  <view class="tn-flex tn-flex-col-center">
                    <view class="tn-margin-sm tn-padding-sm" style="max-width: 50vw;border-radius: 0 15rpx 15rpx 15rpx;background-color: #F9F9F9;">
                      <text class="">这个是一个神奇的评论</text>
                    </view>
                  </view>
                  <view class="tn-padding-left-sm tn-color-gray" style="max-width: 50vw;">
                    <text class="tn-text-sm">8月21日 11:54</text>
                  </view>
                  
                </view>
                
              </view>
            </view>
            <view class="justify-content-item">
              <view class="logo-pic2 tn-margin-top-sm" style="background-image:url('https://cdn.nlark.com/yuque/0/2023/jpeg/280373/1692940242375-assets/web-upload/72155ffc-795c-4cc4-b4b5-171fbb44ab8d.jpeg')">
                <view class="logo-image2">
                </view>
              </view>
            </view>
          </view>
        </view>
        
      </view>
      
    </view>
    
    <view class='tn-tabbar-height'></view>
    
  </view>
</template>

<script setup>
import { useCustomBarHeight, useGoBack } from '@/libs/composables'

// 使用 composable 获取自定义导航栏高度
const { vuex_custom_bar_height } = useCustomBarHeight()
const { goBack } = useGoBack()

defineOptions({
  name: 'TemplateMessage'
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
  
  .tn-tabbar-height {
  	min-height: 100rpx;
  	height: calc(120rpx + env(safe-area-inset-bottom) / 2);
  }
  

  
  /* 用户头像 start */
  .logo-image {
    width: 70rpx;
    height: 70rpx;
    position: relative;
  }
  
  .logo-pic {
    background-size: cover;
    background-repeat: no-repeat;
    border: 1rpx solid #F8F7F8;
    background-position: center;
    border-radius: 100rpx;
    overflow: hidden;
  }
  
  /* 用户帖子 start */
  .logo-image2 {
    width: 120rpx;
    height: 120rpx;
    position: relative;
  }
  
  .logo-pic2 {
    background-size: cover;
    background-repeat: no-repeat;
    border: 1rpx solid #F8F7F8;
    border-radius: 10rpx;
    background-position: top;
  }

  
  ::v-deep(.input-placeholder) {
    font-size: 30rpx;
    color: #7C8191;
  }
  
</style>
