<template>
  <view class="oa-content">
    <!-- 顶部自定义导航 -->
    <tn-navbar fixed  home-icon="" :bottom-shadow="false" bg-color="#F8F7F8" :placeholder="false">
      <view slot="back" class='tn-custom-nav-bar__back'
        @click="goBack">
        <tn-icon name="left-arrow" class='icon'></tn-icon>
      </view>
      <view class="tn-flex tn-flex-col-center tn-flex-row-center tn-padding-left" @click="tn('/partnerPages/user')">
        <text class="tn-text-bold tn-text-xl tn-color-black">付衣衣</text>
        <tn-icon name="rocket" class="tn-text-xl tn-padding-left-sm tn-color-gray"></tn-icon>
      </view>
    </tn-navbar>
    
    <view class="oa-backgroup" :style="{paddingTop: vuex_custom_bar_height + 20 + 'px'}">
      
      <view class="tn-text-justify">
        
        <view class="tn-margin">
          <view class="tn-flex tn-flex-row-between tn-flex-col-center">
            <view class="justify-content-item">
              <view class="tn-flex tn-flex-col-top tn-flex-row-left">
                <view class="logo-pic tn-margin-top-sm" style="background-image:url('https://cdn.nlark.com/yuque/0/2022/jpeg/280373/1664005699053-assets/web-upload/8645ea3a-e0a9-4422-8364-cc5ede305c9f.jpeg')">
                  <view class="logo-image" >
                  </view>
                </view>
                <view class="tn-padding-right tn-color-black">
                  
                  <!-- <view class="tn-padding-left-sm tn-text-bold tn-margin-top-sm" style="max-width: 62vw;">
                    付衣衣
                  </view> -->
                  <view class="tn-flex tn-flex-col-center">
                    <view class="tn-bg-white tn-margin-sm tn-padding-sm" style="max-width: 62vw;border-radius: 0 20rpx 20rpx 20rpx;">
                      <text class="">下午去抓猪</text>
                    </view>
                    <view class="">
                      <!-- 发送失败 -->
                      <!-- <tn-icon name="warning-fill" class="tn-color-purplered tn-text-xxl"></tn-icon> -->
                    </view>
                  </view>
                  
                </view>
                
              </view>
            </view>
            <view class="justify-content-item">
              <!-- 预留空位 -->
            </view>
          </view>
        </view>
        
        <view class="tn-margin">
          <view class="tn-flex tn-flex-row-between tn-flex-col-center">
            <view class="justify-content-item">
              <view class="tn-flex tn-flex-col-top tn-flex-row-left">
                <view class="logo-pic tn-margin-top-sm" style="background-image:url('https://cdn.nlark.com/yuque/0/2022/jpeg/280373/1664005699053-assets/web-upload/8645ea3a-e0a9-4422-8364-cc5ede305c9f.jpeg')">
                  <view class="logo-image" >
                  </view>
                </view>
                <view class="tn-padding-right tn-color-black">
                  
                  <!-- <view class="tn-padding-left-sm tn-text-bold tn-margin-top-sm" style="max-width: 62vw;">
                    付衣衣
                  </view> -->
                  <view class="tn-flex tn-flex-col-center">
                    <view class="tn-margin-sm" style="max-width: 62vw;">
                      
                      <view class="bg-img-cont tn-shadow-blur"
                        style="background-image:url('https://resource.tuniaokj.com/images/content/rodion.jpg');">
                      </view>
                      
                    </view>
                    <view class="">
                      <!-- 发送失败 -->
                      <!-- <tn-icon name="warning-fill" class="tn-color-purplered tn-text-xxl"></tn-icon> -->
                    </view>
                  </view>
                  
                </view>
                
              </view>
            </view>
            <view class="justify-content-item">
              <!-- 预留空位 -->
            </view>
          </view>
        </view>
        
        <view class="tn-margin">
          <view class="tn-flex tn-flex-row-between tn-flex-col-center">
            <view class="justify-content-item">
              <view class="tn-flex tn-flex-col-top tn-flex-row-left">
                <view class="logo-pic tn-margin-top-sm" style="background-image:url('https://cdn.nlark.com/yuque/0/2022/jpeg/280373/1664005699053-assets/web-upload/8645ea3a-e0a9-4422-8364-cc5ede305c9f.jpeg')">
                  <view class="logo-image" >
                  </view>
                </view>
                <view class="tn-padding-right tn-color-black">
                  
                  <!-- <view class="tn-padding-left-sm tn-text-bold tn-margin-top-sm" style="max-width: 62vw;">
                    付衣衣
                  </view> -->
                  <view class="tn-flex tn-flex-col-center">
                    <view class="tn-bg-white tn-margin-sm tn-padding-sm" style="max-width: 62vw;border-radius: 0 20rpx 20rpx 20rpx;">
                      <text class="">地址：广东省广州市番禺区祈福新村129号</text>
                    </view>
                    <view class="">
                      <tn-icon name="location-fill" class="tn-color-blue tn-text-xxl"></tn-icon>
                    </view>
                  </view>
                  
                </view>
                
              </view>
            </view>
            <view class="justify-content-item">
              <!-- 预留空位 -->
            </view>
          </view>
        </view>
        
        
        <view class="tn-margin">
          <view class="tn-flex tn-flex-row-between tn-flex-col-center">
            <view class="justify-content-item">
              <!-- 预留空位 -->
            </view>
            <view class="justify-content-item">
              <view class="tn-flex tn-flex-col-top tn-flex-row-left">
                
                <view class="tn-padding-left tn-color-black">
                  <!-- <view class="tn-padding-right tn-text-bold tn-margin-top-sm tn-text-right" style="max-width: 62vw;">
                    不许凶我
                  </view> -->
                  <view class="tn-flex tn-flex-col-center">
                    <view class="">
                      <!-- 发送失败 -->
                      <tn-icon name="warning-fill" class="tn-color-purplered tn-text-xxl"></tn-icon>
                    </view>
                    <view class="tn-color-white tn-margin-sm tn-padding-sm" style="max-width: 62vw;border-radius: 20rpx 0 20rpx 20rpx;background: #00C8B0">
                      <text class="">遵命衣衣大人</text>
                    </view>
                  </view>
                </view>
                <view class="logo-pic tn-margin-top-sm" style="background-image:url('https://cdn.nlark.com/yuque/0/2023/jpeg/280373/1692940242409-assets/web-upload/fcc4eab6-b2ce-44eb-9165-c49b51f5f830.jpeg')">
                  <view class="logo-image" >
                  </view>
                </view>
                
              </view>
            </view>
          </view>
        </view>
        
        
        <view class="tn-margin">
          <view class="tn-flex tn-flex-row-between tn-flex-col-center">
            <view class="justify-content-item">
              <!-- 预留空位 -->
            </view>
            <view class="justify-content-item">
              <view class="tn-flex tn-flex-col-top tn-flex-row-left">
                
                <view class="tn-padding-left tn-color-black">
                  <!-- <view class="tn-padding-right tn-text-bold tn-margin-top-sm tn-text-right" style="max-width: 62vw;">
                    不许凶我
                  </view> -->
                  <view class="tn-flex tn-flex-col-center">
                    <view class="tn-bg-white tn-margin-sm tn-padding-sm" style="max-width: 62vw;border-radius: 20rpx 0 20rpx 20rpx;">
                      <view class="tn-flex tn-flex-row-left tn-flex-col-center tn-strip-bottom-min" style="padding: 10rpx 0 20rpx 0;">
                        <view class="justify-content-item user-pic" style="background-image:url('https://cdn.nlark.com/yuque/0/2021/jpeg/280373/1631345816936-assets/web-upload/b89032b6-4d07-4983-90e5-cc6d86de8b3e.jpeg')">
                          <view class="user-image" >
                          </view>
                        </view>
                        <view class="justify-content-item tn-padding-left-sm tn-padding-right">
                          <view class="tn-text-bold">
                            东东
                          </view>
                          <view class="tn-color-gray tn-padding-top-xs">
                            图鸟科技・背锅侠
                          </view>
                        </view>
                      </view>
                      
                      <view class="tn-padding-top-sm tn-color-gray">个人名片</view>
                    </view>
                  </view>
                </view>
                <view class="logo-pic tn-margin-top-sm" style="background-image:url('https://cdn.nlark.com/yuque/0/2023/jpeg/280373/1692940242409-assets/web-upload/fcc4eab6-b2ce-44eb-9165-c49b51f5f830.jpeg')">
                  <view class="logo-image" >
                  </view>
                </view>
                
              </view>
            </view>
          </view>
        </view>
        
        
        <view class="tn-text-center">
          <text class="tn-bg-white tn-text-xs" style="border-radius: 6rpx;padding: 10rpx 14rpx;">晚上 7:02</text>
        </view>
        
        <view class="tn-margin">
          <view class="tn-flex tn-flex-row-between tn-flex-col-center">
            <view class="justify-content-item">
              <!-- 预留空位 -->
            </view>
            <view class="justify-content-item">
              <view class="tn-flex tn-flex-col-top tn-flex-row-left">
                
                <view class="tn-padding-left tn-color-black">
                  <!-- <view class="tn-padding-right tn-text-bold tn-margin-top-sm tn-text-right" style="max-width: 62vw;">
                    不许凶我
                  </view> -->
                  <view class="tn-flex tn-flex-col-center">
                    <view class="">
                      <!-- 发送失败 -->
                      <!-- <tn-icon name="warning-fill" class="tn-color-purplered tn-text-xxl"></tn-icon> -->
                    </view>
                    <view class="tn-color-white tn-margin-sm tn-padding-sm" style="max-width: 62vw;border-radius: 20rpx 0 20rpx 20rpx;background: #00C8B0">
                      <text class="">现在就去抓</text>
                    </view>
                  </view>
                </view>
                <view class="logo-pic tn-margin-top-sm" style="background-image:url('https://cdn.nlark.com/yuque/0/2023/jpeg/280373/1692940242409-assets/web-upload/fcc4eab6-b2ce-44eb-9165-c49b51f5f830.jpeg')">
                  <view class="logo-image" >
                  </view>
                </view>
                
              </view>
            </view>
          </view>
        </view>
        
        <view class="tn-margin">
          <view class="tn-flex tn-flex-row-between tn-flex-col-center">
            <view class="justify-content-item">
              <view class="tn-flex tn-flex-col-top tn-flex-row-left">
                <view class="logo-pic tn-margin-top-sm" style="background-image:url('https://cdn.nlark.com/yuque/0/2022/jpeg/280373/1664005699053-assets/web-upload/8645ea3a-e0a9-4422-8364-cc5ede305c9f.jpeg')">
                  <view class="logo-image" >
                  </view>
                </view>
                <view class="tn-padding-right tn-color-black">
                  
                  <!-- <view class="tn-padding-left-sm tn-text-bold tn-margin-top-sm" style="max-width: 62vw;">
                    付衣衣
                  </view> -->
                  <view class="tn-flex tn-flex-col-center">
                    <view class="tn-bg-white tn-margin-sm tn-padding-sm" style="max-width: 62vw;border-radius: 0 20rpx 20rpx 20rpx;">
                      <text class="">妹妹，现在很晚了</text>
                    </view>
                    <!-- <view class="">
                      <tn-icon name="location-fill" class="tn-color-blue tn-text-xxl"></tn-icon>
                    </view> -->
                  </view>
                  
                </view>
                
              </view>
            </view>
            <view class="justify-content-item">
              <!-- 预留空位 -->
            </view>
          </view>
        </view>
        
        
        <view class="tn-margin">
          <view class="tn-flex tn-flex-row-between tn-flex-col-center">
            <view class="justify-content-item">
              <!-- 预留空位 -->
            </view>
            <view class="justify-content-item">
              <view class="tn-flex tn-flex-col-top tn-flex-row-left">
                
                <view class="tn-padding-left tn-color-black">
                  <!-- <view class="tn-padding-right tn-text-bold tn-margin-top-sm tn-text-right" style="max-width: 62vw;">
                    不许凶我
                  </view> -->
                  <view class="tn-flex tn-flex-col-center">
                    <view class="">
                      <!-- 发送失败 -->
                      <!-- <tn-icon name="warning-fill" class="tn-color-purplered tn-text-xxl"></tn-icon> -->
                    </view>
                    <view class="tn-color-white tn-margin-sm tn-padding-sm" style="max-width: 62vw;border-radius: 20rpx 0 20rpx 20rpx;background: #00C8B0">
                      <text class="">可以的，我点蜡烛</text>
                    </view>
                  </view>
                </view>
                <view class="logo-pic tn-margin-top-sm" style="background-image:url('https://cdn.nlark.com/yuque/0/2023/jpeg/280373/1692940242409-assets/web-upload/fcc4eab6-b2ce-44eb-9165-c49b51f5f830.jpeg')">
                  <view class="logo-image" >
                  </view>
                </view>
                
              </view>
            </view>
          </view>
        </view>
        
        <view class="tn-margin">
          <view class="tn-flex tn-flex-row-between tn-flex-col-center">
            <view class="justify-content-item">
              <view class="tn-flex tn-flex-col-top tn-flex-row-left">
                <view class="logo-pic tn-margin-top-sm" style="background-image:url('https://cdn.nlark.com/yuque/0/2022/jpeg/280373/1664005699053-assets/web-upload/8645ea3a-e0a9-4422-8364-cc5ede305c9f.jpeg')">
                  <view class="logo-image" >
                  </view>
                </view>
                <view class="tn-padding-right tn-color-black">
                  
                  <!-- <view class="tn-padding-left-sm tn-text-bold tn-margin-top-sm" style="max-width: 62vw;">
                    付衣衣
                  </view> -->
                  <view class="tn-flex tn-flex-col-center">
                    <view class="tn-bg-white tn-margin-sm tn-padding-sm" style="max-width: 62vw;border-radius: 0 20rpx 20rpx 20rpx;">
                      <text class="">明天再去??</text>
                    </view>
                    <!-- <view class="">
                      <tn-icon name="location-fill" class="tn-color-blue tn-text-xxl"></tn-icon>
                    </view> -->
                  </view>
                  
                </view>
                
              </view>
            </view>
            <view class="justify-content-item">
              <!-- 预留空位 -->
            </view>
          </view>
        </view>
        
        <view class="tn-margin">
          <view class="tn-flex tn-flex-row-between tn-flex-col-center">
            <view class="justify-content-item">
              <view class="tn-flex tn-flex-col-top tn-flex-row-left">
                <view class="logo-pic tn-margin-top-sm" style="background-image:url('https://cdn.nlark.com/yuque/0/2022/jpeg/280373/1664005699053-assets/web-upload/8645ea3a-e0a9-4422-8364-cc5ede305c9f.jpeg')">
                  <view class="logo-image" >
                  </view>
                </view>
                <view class="tn-padding-right tn-color-black">
                  
                  <!-- <view class="tn-padding-left-sm tn-text-bold tn-margin-top-sm" style="max-width: 62vw;">
                    付衣衣
                  </view> -->
                  <view class="tn-flex tn-flex-col-center">
                    <view class="" style="max-width: 62vw;font-size: 180rpx;">
                      <text class="tn-worm-eat">️</text>
                    </view>
                    <!-- <view class="">
                      <tn-icon name="location-fill" class="tn-color-blue tn-text-xxl"></tn-icon>
                    </view> -->
                  </view>
                  
                </view>
                
              </view>
            </view>
            <view class="justify-content-item">
              <!-- 预留空位 -->
            </view>
          </view>
        </view>
        
        
        <view class="tn-margin">
          <view class="tn-flex tn-flex-row-between tn-flex-col-center">
            <view class="justify-content-item">
              <!-- 预留空位 -->
            </view>
            <view class="justify-content-item">
              <view class="tn-flex tn-flex-col-top tn-flex-row-left">
                
                <view class="tn-padding-left tn-color-black">
                  <!-- <view class="tn-padding-right tn-text-bold tn-margin-top-sm tn-text-right" style="max-width: 62vw;">
                    不许凶我
                  </view> -->
                  <view class="tn-flex tn-flex-col-center">
                    <view class="">
                      <!-- 发送失败 -->
                      <!-- <tn-icon name="warning-fill" class="tn-color-purplered tn-text-xxl"></tn-icon> -->
                    </view>
                    <view class="tn-color-white tn-margin-sm tn-padding-sm" style="max-width: 62vw;border-radius: 20rpx 0 20rpx 20rpx;background: #00C8B0">
                      <text class="">遵命大人，此处发送爱心️</text>
                    </view>
                  </view>
                </view>
                <view class="logo-pic tn-margin-top-sm" style="background-image:url('https://cdn.nlark.com/yuque/0/2023/jpeg/280373/1692940242409-assets/web-upload/fcc4eab6-b2ce-44eb-9165-c49b51f5f830.jpeg')">
                  <view class="logo-image" >
                  </view>
                </view>
                
              </view>
            </view>
          </view>
        </view>
        
        
        <view class="tn-margin">
          <view class="tn-flex tn-flex-row-between tn-flex-col-center">
            <view class="justify-content-item">
              <!-- 预留空位 -->
            </view>
            <view class="justify-content-item">
              <view class="tn-flex tn-flex-col-top tn-flex-row-left">
                
                <view class="tn-padding-left tn-color-black">
                  <!-- <view class="tn-padding-right tn-text-bold tn-margin-top-sm tn-text-right" style="max-width: 62vw;">
                    不许凶我
                  </view> -->
                  <view class="tn-flex tn-flex-col-center">
                    <view class="">
                      <!-- 发送失败 -->
                      <!-- <tn-icon name="warning-fill" class="tn-color-purplered tn-text-xxl"></tn-icon> -->
                    </view>
                    <view class="" style="max-width: 62vw;font-size: 180rpx;">
                      <text class="tn-cat-fish">️</text>
                    </view>
                  </view>
                </view>
                <view class="logo-pic tn-margin-top-sm" style="background-image:url('https://cdn.nlark.com/yuque/0/2023/jpeg/280373/1692940242409-assets/web-upload/fcc4eab6-b2ce-44eb-9165-c49b51f5f830.jpeg')">
                  <view class="logo-image" >
                  </view>
                </view>
                
              </view>
            </view>
          </view>
        </view>

        
      </view>
      
    </view>
    
    
    <view class="tabbar footerfixed tn-bg-white">
      <view class="tn-flex tn-flex-row-between tn-flex-col-center">
        <view class="justify-content-item tn-margin-top tn-flex-3" @tap="showLandscape">
          <view class="tn-margin-left" style="padding: 20rpx 40rpx;background-color: #F9F9F9;max-width: 36vw;border-radius: 1000rpx;color: #999999;">
            输入聊天内容
          </view>
        </view>
        <view class="justify-content-item tn-flex-row-center tn-flex-col-center tn-margin-top tn-padding-right-sm">
          <view class="tn-flex tn-flex-row-center tn-flex-col-center">
            <view class="icon27__item--icon tn-flex tn-flex-row-center tn-flex-col-center tn-padding-right-sm" @tap.stop="openEmotionModal">
              <tn-icon name="emoji-good" class="tn-color-gray"></tn-icon>
            </view>
            <view class="icon27__item--icon tn-flex tn-flex-row-center tn-flex-col-center" @tap.stop="openMoreModal">
              <tn-icon name="add-circle" class="tn-color-gray"></tn-icon>
            </view>
            <!-- <view class="avatar-all">
              <view class="tn-shadow-blur" style="background-image:url('https://cdn.nlark.com/yuque/0/2023/jpeg/280373/1692940242409-assets/web-upload/fcc4eab6-b2ce-44eb-9165-c49b51f5f830.jpeg');width: 60rpx;height: 60rpx;background-size: cover;">
              </view>
            </view> -->
          </view>
        </view>
      </view>
    </view>
    
    <view class="">
      <!-- 压屏窗-->
      <!-- <tn-landscape :show="show" @close="closeLandscape" :closeBtn="false">
        <view class="chat-input" style="">
          <view class="tn-bg-gray--light tn-padding tn-text-justify" style="border-radius: 20rpx;margin: 40rpx 30rpx 30rpx 30rpx;">
            <textarea class="tn-input" maxlength="500" confirm-type="done" placeholder="输入聊天内容" placeholder-style="color:#AAAAAA" :show-confirm-bar="false" :focus="focusTextarea" @blur="closeLandscape"></textarea>
          </view>
          <view class="tn-flex tn-flex-row-right" style="width: 100vw;padding-right: 30rpx;" @tap.stop="closeLandscape">
            <tn-button backgroundColor="#4B98FE" fontColor="#FFFFFF" :custom-style="{padding:'2rpx 30rpx 2rpx 30rpx'}" width="100%" fontSize="26">发 送</tn-button>
          </view>
          <view class="tn-tabbar-height-safe tn-color-white">
            预留安全区域
          </view>
        </view>
    
      </tn-landscape> -->
    </view>
    
    <!-- 功能弹框 -->
    <!-- popup会有延迟，采用这种方式来优化弹窗的优化体验-->
    <more-modal v-model="showMoreModal"></more-modal>
    
    <!-- 表情弹框 -->
    <!-- popup会有延迟，采用这种方式来优化弹窗的优化体验-->
    <emotion-modal v-model="showEmotionModal"></emotion-modal>
    
    <view class="tn-tabbar-height"></view>
    
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { useCustomBarHeight, useGoBack } from '@/libs/composables'
import MoreModal from '@/components/more/more.vue'
import EmotionModal from '@/components/emotion/emotion.vue'

// 使用 composable 获取自定义导航栏高度
const { vuex_custom_bar_height } = useCustomBarHeight()
const { goBack } = useGoBack()

const showMoreModal = ref(false)
const showEmotionModal = ref(false)

const show = ref(false)
const focusTextarea = ref(false)
const maskCloseable = ref(true)

// 弹出压屏窗
const showLandscape = () => {
  openLandscape()
}

// 打开压屏窗
const openLandscape = () => {
  show.value = true
  nextTick(() => {
    focusTextarea.value = true
  })
}

// 关闭压屏窗，@blur 点击失去聚焦时，则相当于完成，关闭压屏窗并发送消息
const closeLandscape = () => {
  show.value = false
  focusTextarea.value = false
}

// 点击失去聚焦时，则相当于完成，关闭压屏窗并发送消息
const finish = () => {
  closeLandscape()
}

// 跳转
const tn = (e) => {
  uni.navigateTo({
    url: e,
  });
}

// 打开信息弹框
const openMoreModal = () => {
  showMoreModal.value = true
}

// 打开信息弹框
const openEmotionModal = () => {
  showEmotionModal.value = true
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
  
  .tn-tabbar-height {
  	min-height: 100rpx;
  	height: calc(120rpx + env(safe-area-inset-bottom) / 2);
  }
  
  .oa-backgroup{
    background-color: #F8F7F8;
    min-height: 100vh;
    padding-bottom: 60rpx;
  }
  
  /* 用户头像 start */
  .logo-image {
    width: 80rpx;
    height: 80rpx;
    position: relative;
  }
  
  .logo-pic {
    background-size: cover;
    background-repeat: no-repeat;
    // background-attachment:fixed;
    background-position: top;
    // border: 1rpx solid rgba(255,255,255,0.05);
    box-shadow: 0rpx 0rpx 80rpx 0rpx rgba(0, 0, 0, 0.05);
    border-radius: 50%;
    // overflow: hidden;
    // background-color: #FFFFFF;
  }
  
  /* 名片头像 start */
  .user-image {
    width: 90rpx;
    height: 90rpx;
    position: relative;
  }
  
  .user-pic {
    background-size: cover;
    background-repeat: no-repeat;
    // background-attachment:fixed;
    background-position: top;
    // border: 1rpx solid rgba(255,255,255,0.05);
    box-shadow: 0rpx 0rpx 80rpx 0rpx rgba(0, 0, 0, 0.05);
    border-radius: 50%;
    // overflow: hidden;
    // background-color: #FFFFFF;
  }
  
  .bg-img-cont {
    background-size: cover;
    background-position: center;
    background-repeat: no-repeat;
    height: 260rpx;
    width: 55vw;
    margin: 10rpx 0 0 0;
    border-radius: 12rpx;
  }
  
  /* 图标容器27 start */
  .icon27 {
    &__item {
      width: 30%;
      background-color: #FFFFFF;
      // border-radius: 10rpx;
      padding: 30rpx 0rpx 30rpx 30rpx;
      margin: 20rpx 0rpx;
      transform: scale(1);
      transition: transform 0.3s linear;
      transform-origin: center center;
      
      &--icon {
        width: 70rpx;
        height: 70rpx;
        font-size: 60rpx;
        // border-radius: 50%;
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
  
  
  /* 底部 start*/
  .footerfixed{
    position: fixed;
    width: 100%;
    bottom: 0;
    z-index: 999;
    background-color: rgba(255,255,255,0.5);
    box-shadow: 0rpx 0rpx 30rpx 0rpx rgba(0, 0, 0, 0.07);
  }
  
  .tabbar {
    max-width: 640px;
    margin: 0 auto;
    align-items: center;
    min-height: 130rpx;
  	padding: 0;
  	height: calc(130rpx + env(safe-area-inset-bottom) / 2);
  	padding-bottom: calc(30rpx + env(safe-area-inset-bottom) / 2);
    padding-left: 10rpx;
    padding-right: 10rpx;
  }
  
  /* 间隔线 start*/
  .tn-strip-bottom {
   width: 100%;
   border-bottom: 20rpx solid #F8F7F8;
  }
  /* 间隔线 start*/
  .tn-strip-bottom-min {
   width: 100%;
   border-bottom: 1rpx solid #F8F7F8;
  }
  
  /* 头像*/
  .avatar-all {
    width: 60rpx;
    height: 60rpx;
    border: 4rpx solid rgba(255,255,255,0.05);
    border-radius: 50%;
    overflow: hidden;
    box-shadow: 0rpx 0rpx 80rpx 0rpx rgba(0, 0, 0, 0.15);
  }
  
  .tn-input{
      width: calc(100vw - 120rpx);
      height: 120rpx;
    }
    
    .chat-input{
      border-radius: 24rpx 24rpx 0 0;
      background: linear-gradient(180deg, #FFFFFF, #FFFFFF, #FFFFFF,  #FFFFFF,  #FFFFFF, #FFFFFF00);
      margin-bottom: -640rpx;
    }
    
    /* 底部安全边距 start*/
    .tn-tabbar-height-safe {
    	min-height: 580rpx;
    	height: calc(620rpx + env(safe-area-inset-bottom));
    	height: calc(620rpx + constant(safe-area-inset-bottom));
    }
  
  :deep(.input-placeholder) {
    font-size: 30rpx;
    color: #C6D1D8;
  }
  
</style>
