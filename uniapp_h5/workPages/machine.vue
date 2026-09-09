<template>
  <view class="oa-content">
    <!-- 顶部自定义导航 -->
    <tn-navbar fixed home-icon="" :placeholder="false" :bottom-shadow="false" bg-color="#FFFFFF">
      <template #back><view class='tn-custom-nav-bar__back'
        @click="goBack">
        <tn-icon name="left-arrow" class="icon"></tn-icon>
      </view></template>
      <view class="tn-flex tn-flex-col-center tn-flex-row-center ">
        <text class="tn-text-bold tn-text-xl tn-color-black">仪器维护</text>
      </view>
    </tn-navbar>
    
    
    <view class="" :style="{paddingTop: vuex_custom_bar_height + 10 + 'px'}">
      
      <view class="content-bg tn-margin tn-padding" v-for="(item, index) in machineList" :key="index" @click="tn('')">
        <!-- <view class="tn-text-center" style="width: 70rpx;height: 30rpx;position: absolute;top: 0;right:0;border-radius: 0 12rpx 0 12rpx;font-size: 14rpx;background-color: #4B98FE50;">
          <view class="tn-color-white" style="margin-top: 6rpx;">运行中</view>
        </view> -->
        <view class="tn-flex tn-flex-row-between tn-flex-col-center">
          <view class="justify-content-item tn-text-justify clamp-text-1 tn-text-lg tn-text-bold">
            {{ item.title }}
          </view>
          <tn-icon name="deploy" class="justify-content-item tn-color-gray tn-text-lg"></tn-icon>
        </view>
        <view class="tn-text-justify clamp-text-2 tn-padding-top-sm tn-color-gray--dark">
          设备位置：{{ item.map }}
        </view>
          
      </view>
      
      <view class="">
        <view class="icon15__item--icon tn-flex tn-flex-row-center tn-flex-col-center tn-shadow-blur button-1" @click="showModal">
          <tn-icon name="add" class="tn-color-white"></tn-icon>
        </view>
      </view>  
      
      <tn-modal v-model="show" :custom="true" :showCloseBtn="true" :maskCloseable="false">
        <view class="custom-modal-content">
          <!-- <view class="tn-text-lg tn-text-bold tn-text-center tn-padding">添加设备</view> -->
          <view class="tn-margin-top-xl">
            <view class="tn-flex tn-flex-col-center tn-margin-top"
              style="border: 1rpx solid #4B98FE;border-radius: 10rpx;padding: 10rpx 20rpx 10rpx 20rpx;width: 100%;">
              <tn-icon name="link" class="justify-content-item tn-padding-right-xs tn-color-gray tn-text-lg"></tn-icon>
              <input class="justify-content-item" placeholder="请输入IP地址" name="input" placeholder-style="color:#AAAAAA"
                style="width: 100%;"></input>
            </view>
            <view class="tn-flex tn-flex-col-center tn-margin-top"
              style="border: 1rpx solid #4B98FE;border-radius: 10rpx;padding: 10rpx 20rpx 10rpx 20rpx;width: 100%;">
              <tn-icon name="focus" class="justify-content-item tn-padding-right-xs tn-color-gray tn-text-lg"></tn-icon>
              <input class="justify-content-item" placeholder="请输入端口号" name="input" placeholder-style="color:#AAAAAA"
                style="width: 100%;"></input>
            </view>
            <view class="tn-flex tn-flex-col-center tn-margin-top"
              style="border: 1rpx solid #4B98FE;border-radius: 10rpx;padding: 10rpx 20rpx 10rpx 20rpx;width: 100%;">
              <tn-icon name="safe" class="justify-content-item tn-padding-right-xs tn-color-gray tn-text-lg"></tn-icon>
              <input class="justify-content-item" placeholder="请输入激活码" name="input" placeholder-style="color:#AAAAAA"
                style="width: 100%;"></input>
            </view>
            <view class="tn-flex tn-flex-col-center tn-margin-top"
              style="border: 1rpx solid #4B98FE;border-radius: 10rpx;padding: 10rpx 20rpx 10rpx 20rpx;width: 100%;">
              <tn-icon name="qr-code" class="justify-content-item tn-padding-right-xs tn-color-gray tn-text-lg"></tn-icon>
              <input class="justify-content-item" placeholder="请输入仪器码" name="input" placeholder-style="color:#AAAAAA"
                style="width: 100%;"></input>
            </view>
            <view class="tn-flex tn-flex-col-center tn-margin-top"
              style="border: 1rpx solid #4B98FE;border-radius: 10rpx;padding: 10rpx 20rpx 10rpx 20rpx;width: 100%;">
              <tn-icon name="level" class="justify-content-item tn-padding-right-xs tn-color-gray tn-text-lg"></tn-icon>
              <input class="justify-content-item" placeholder="请输入仪器名" name="input" placeholder-style="color:#AAAAAA"
                style="width: 100%;"></input>
            </view>
            <view class="tn-flex tn-flex-col-center tn-margin-top"
              style="border: 1rpx solid #4B98FE;border-radius: 10rpx;padding: 10rpx 20rpx 10rpx 20rpx;width: 100%;">
              <tn-icon name="map" class="justify-content-item tn-padding-right-xs tn-color-gray tn-text-lg"></tn-icon>
              <input class="justify-content-item" placeholder="请输入安装位置" name="input" placeholder-style="color:#AAAAAA"
                style="width: 100%;"></input>
            </view>
          </view>
          <view class="tn-flex-1 justify-content-item tn-text-center tn-margin-top">
            <tn-button
              bg-color="#4B98FE"
              :custom-style="{padding:'40rpx 0'}"
              width="100%"
              :fontSize="28"
              text-color="#FFFFFF"
              shape="round"
              @click="tn('')"
            >
              <text class="">立即添加</text>
            </tn-button>
          </view>
        </view>
      </tn-modal>
      
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { useCustomBarHeight, useGoBack } from '@/libs/composables'
// 使用 composable 获取自定义导航栏高度
const { vuex_custom_bar_height } = useCustomBarHeight()
const { goBack } = useGoBack()

const show = ref(false)

const machineList = ref([{
    title: '仪器名称-设备一',
    map: '广东省广州市番禺区祈福新村126号',
    url: ''
  },
  {
    title: '仪器名称-设备二',
    map: '广东省广州市番禺区祈福新村127号',
    url: ''
  },
  {
    title: '仪器名称-设备三',
    map: '广东省广州市番禺区祈福新村128号',
    url: ''
  },
  {
    title: '仪器名称-设备四',
    map: '广东省广州市番禺区祈福新村129号',
    url: ''
  },
  {
    title: '仪器名称-设备五',
    map: '广东省广州市番禺区祈福新村129号',
    url: ''
  },
  {
    title: '仪器名称-设备六',
    map: '广东省广州市番禺区祈福新村130号',
    url: ''
  },
  {
    title: '仪器名称-设备七',
    map: '广东省广州市番禺区祈福新村131号',
    url: ''
  }
])

// 跳转
function tn(e) {
  if (!e) {
    uni.showToast({ icon: 'none', title: '详情功能暂未开放' })
    return
  }
  uni.navigateTo({
    url: e,
  });
}

// 弹出模态框-提示
function showModal(event) {
  openModal()
}

// 打开模态框-提示
function openModal() {
  show.value = true
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
  
  /* 背景阴影 start*/
  .content-bg {
    position: relative;
    border-radius: 15rpx;
    background-color: #FFFFFF;
    // box-shadow: 0rpx 0rpx 50rpx 0rpx rgba(0, 0, 0, 0.07);
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
  
  /* 按钮 */
  .button-1 {
    // background-color: rgba(0, 0, 0, 0.15);
    background-color: #3668FC80;
    position: fixed;
    /* bottom:200rpx;
      right: 20rpx; */
    bottom: 15%;
    right: 30rpx;
    z-index: 1001;
    border-radius: 100px;
  }
  
  /* 图标容器15 start */
  .icon15 {
    &__item {
      width: 30%;
      
      border-radius: 10rpx;
      padding: 30rpx;
      margin: 20rpx 10rpx;
      transform: scale(1);
      transition: transform 0.3s linear;
      transform-origin: center center;
      
      &--icon {
        width: 90rpx;
        height: 90rpx;
        font-size: 50rpx;
        border-radius: 50%;
        margin-bottom: 18rpx;
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
