<template>
  <view class="oa-content">
    <!-- 顶部自定义导航 -->
    <tn-navbar fixed home-icon="" :bottom-shadow="false" bg-color="#FFFFFF00" :placeholder="false">
      <view slot="back" class='tn-custom-nav-bar__back'
        @click="goBack">
        <tn-icon name='home-in-fill' class='icon'></tn-icon>
      </view>
      <view class="tn-flex tn-flex-col-center tn-flex-row-center ">
        <text class="tn-text-bold tn-text-xl tn-color-black">商研学院</text>
      </view>
    </tn-navbar>
    
    <view class="tn-search-fixed" style="background: linear-gradient(180deg, #D8E5FF, #FFFFFF);">
      <view class="tn-flex tn-flex-row-between tn-flex-col-center tn-margin" :style="{paddingTop: vuex_custom_bar_height + 'px'}">
        <view class="justify-content-item align-content-item" style="width: 100vw;" @click="tn('/homePages/search')">
          <view class="tn-flex tn-flex-col-center tn-bg-white" style="border: 2rpx solid #3467C2;border-radius: 100rpx;padding: 10rpx 20rpx 10rpx 20rpx;width: 90%;">
            <tn-icon name="search" class="justify-content-item tn-padding-right-xs tn-text-lg" style="color: #3467C2;"></tn-icon>
            <input class="justify-content-item" placeholder="请输入搜索关键词" name="input" placeholder-style="color:#3467C280" disabled style="width: 90%;color: #0E2D6D;"></input>
          </view>
        </view>
        <!-- 切换-->
        <view class="align-content-item">
          <view class="justify-content-item tn-text-center"  style="font-size: 60rpx;color: #3467C2;">
            <tn-icon name="menu-flex" v-if="currentModeIndex === 0" @tap.stop="modeSwitch(1)"></tn-icon>
            <tn-icon name="carousel" v-if="currentModeIndex === 1" @tap.stop="modeSwitch(0)"></tn-icon>
          </view>
        </view>
      </view>
    </view>
    
    <view class="" :style="{paddingTop: vuex_custom_bar_height + 100 + 'px'}">
      
      <!-- 这里用css来判断显隐，不用v-if-->
      <view class="" :class="currentModeIndex==0?'content-show':'content-hide'">
        
        <swiper class="card-swiper" :circular="true" :autoplay="false" duration="500" interval="12000"
          previous-margin="80rpx" next-margin="80rpx" @change="cardSwiper">
          <swiper-item v-for="(item,index) in swiperList" :key="index" :class="cardCur==index?'cur':''"  @click="tn(item.pathurl)">
            <view class="swiper-item image-banner">
              <image :src="item.url" mode="aspectFill" v-if="item.type=='image'"></image>
            </view>
        
            <view class="swiper-item-text">
              <view class="tn-padding-sm">
                <view class="tn-color-white tn-padding-top-xs tn-text-bold" style="font-size: 48rpx;">{{item.name}}</view>
                <view class="tn-text-sm tn-text-bold tn-color-white tn-padding-top-xs" style="opacity: 0.8;">{{item.text}}
                </view>
              </view>
        
            </view>
        
          </swiper-item>
        </swiper>
        <view class="indication">
          <block v-for="(item,index) in swiperList" :key="index">
            <view class="spot" :class="cardCur==index?'active':''"></view>
          </block>
        </view>
        
      </view>
      
      <!-- 这里用css来判断显隐，不用v-if-->
      <view class="tn-margin" :class="currentModeIndex==1?'content-show':'content-hide'">
        
        <view class="" @click="tn('')">
          <view class="tn-flex">
            <view class="image-pic" style="background-image:url('https://cdn.nlark.com/yuque/0/2023/jpeg/280373/1692940242318-assets/web-upload/6d5b712d-5996-4a80-bb02-4ddc23e01acf.jpeg');">
              <view class="image-article">
              </view>
            </view>
            <view class="tn-margin-sm tn-padding-top" style="width: 100%;">
              <view class="tn-text-lg tn-text-bold clamp-text-1 tn-margin-left">
                企业共建
              </view>
              <view class="tn-padding-top-sm tn-margin-left">
                <text class="tn-text-sm tn-color-gray tn-padding-bottom-xs clamp-text-1">
                  开启你的发现之旅
                </text>
              </view>
            </view>
          </view>
        </view>
      
        <view class="tn-margin-top-xl" @click="tn('')">
          <view class="tn-flex">
            <view class="tn-margin-sm tn-padding-top" style="width: 100%;">
              <view class="tn-text-lg tn-text-bold clamp-text-1">
                学海无涯
              </view>
              <view class="tn-padding-top-sm">
                <text class="tn-text-sm tn-color-gray tn-padding-bottom-xs clamp-text-1">
                  开启你的学习之旅
                </text>
              </view>
            </view>
            <view class="image-pic" style="background-image:url('https://cdn.nlark.com/yuque/0/2023/jpeg/280373/1692940242318-assets/web-upload/6d5b712d-5996-4a80-bb02-4ddc23e01acf.jpeg');">
              <view class="image-article">
              </view>
            </view>
          </view>
        </view>
      
        <view class="tn-margin-top-xl" @click="tn('')">
          <view class="tn-flex">
            <view class="image-pic" style="background-image:url('https://cdn.nlark.com/yuque/0/2023/jpeg/280373/1692940242318-assets/web-upload/6d5b712d-5996-4a80-bb02-4ddc23e01acf.jpeg');">
              <view class="image-article">
              </view>
            </view>
            <view class="tn-margin-sm tn-padding-top" style="width: 100%;">
              <view class="tn-text-lg tn-text-bold clamp-text-1 tn-margin-left">
                职位晋升
              </view>
              <view class="tn-padding-top-sm tn-margin-left">
                <text class="tn-text-sm tn-color-gray tn-padding-bottom-xs clamp-text-1">
                  开启你的提升之旅
                </text>
              </view>
            </view>
          </view>
        </view>
      
        <view class="tn-margin-top-xl" @click="tn('')">
          <view class="tn-flex">
            <view class="tn-margin-sm tn-padding-top" style="width: 100%;">
              <view class="tn-text-lg tn-text-bold clamp-text-1">
                每日词条
              </view>
              <view class="tn-padding-top-sm">
                <text class="tn-text-sm tn-color-gray tn-padding-bottom-xs clamp-text-1">
                  开启你的创造之旅
                </text>
              </view>
            </view>
            <view class="image-pic" style="background-image:url('https://cdn.nlark.com/yuque/0/2023/jpeg/280373/1692940242318-assets/web-upload/6d5b712d-5996-4a80-bb02-4ddc23e01acf.jpeg');">
              <view class="image-article">
              </view>
            </view>
          </view>
        </view>
        
        <view class="tn-margin-top-xl" @click="tn('')">
          <view class="tn-flex">
            <view class="image-pic" style="background-image:url('https://cdn.nlark.com/yuque/0/2023/jpeg/280373/1692940242318-assets/web-upload/6d5b712d-5996-4a80-bb02-4ddc23e01acf.jpeg');">
              <view class="image-article">
              </view>
            </view>
            <view class="tn-margin-sm tn-padding-top" style="width: 100%;">
              <view class="tn-text-lg tn-text-bold clamp-text-1 tn-margin-left">
                新人融入
              </view>
              <view class="tn-padding-top-sm tn-margin-left">
                <text class="tn-text-sm tn-color-gray tn-padding-bottom-xs clamp-text-1">
                  开启你的探索之旅
                </text>
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
import { ref } from 'vue'
import { useCustomBarHeight, useGoBack } from '@/libs/composables'
// 使用 composable 获取自定义导航栏高度
const { vuex_custom_bar_height } = useCustomBarHeight()
const { goBack } = useGoBack()

defineOptions({
  name: 'TemplateInstitute'
})

// 当前选中的模式
const currentModeIndex = ref(0)

const cardCur = ref(0)
const swiperList = ref([
  {
    id: 0,
    type: 'image',
    name: '企业共建',
    text: '开启你的发现之旅',
    url: 'https://cdn.nlark.com/yuque/0/2023/jpeg/280373/1692940242318-assets/web-upload/6d5b712d-5996-4a80-bb02-4ddc23e01acf.jpeg',
    pathurl: "/momentPages/nav"
  }, {
    id: 1,
    type: 'image',
    name: '学海无涯',
    text: '开启你的学习之旅',
    url: 'https://cdn.nlark.com/yuque/0/2023/jpeg/280373/1692940242318-assets/web-upload/6d5b712d-5996-4a80-bb02-4ddc23e01acf.jpeg',
    pathurl: "/momentPages/ocean"
  }, {
    id: 2,
    type: 'image',
    name: '职位晋升',
    text: '开启你的提升之旅',
    url: 'https://cdn.nlark.com/yuque/0/2023/jpeg/280373/1692940242318-assets/web-upload/6d5b712d-5996-4a80-bb02-4ddc23e01acf.jpeg',
    pathurl: "/momentPages/nav"
  }, {
    id: 3,
    type: 'image',
    name: '每日词条',
    text: '开启你的创造之旅',
    url: 'https://cdn.nlark.com/yuque/0/2023/jpeg/280373/1692940242318-assets/web-upload/6d5b712d-5996-4a80-bb02-4ddc23e01acf.jpeg',
    pathurl: "/momentPages/dictionary"
  }, {
    id: 4,
    type: 'image',
    name: '新人融入',
    text: '开启你的探索之旅',
    url: 'https://cdn.nlark.com/yuque/0/2023/jpeg/280373/1692940242318-assets/web-upload/6d5b712d-5996-4a80-bb02-4ddc23e01acf.jpeg',
    pathurl: "/momentPages/blend"
  }
])

// 切换模式
const modeSwitch = (index) => {
  currentModeIndex.value = index
}

// cardSwiper
const cardSwiper = (e) => {
  cardCur.value = e.detail.current
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
    width: 40%;
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
    z-index: 1;
  }
  
  .content-show{
    display: block;
  }
  
  .content-hide{
    display: none;
  }
  
  /* 轮播图片入口 start*/
  .card-swiper {
    height: 60vh !important;
  }
  
  .card-swiper swiper-item {
    width: 590rpx !important;
    box-sizing: border-box;
    padding: 0rpx 0rpx 90rpx 0rpx;
    overflow: initial;
  }
  
  .card-swiper swiper-item .swiper-item {
    width: 100%;
    display: block;
    height: 100%;
    border-radius: 20rpx;
    transform: scale(0.87);
    transition: all 0.2s ease-in 0s;
    overflow: hidden;
  }
  
  .card-swiper swiper-item.cur .swiper-item {
    transform: none;
    transition: all 0.2s ease-in 0s;
  }
  
  .card-swiper swiper-item .swiper-item-text {
    position: absolute;
    bottom: 0;
    width: 100%;
    display: block;
    height: 140rpx;
    border-radius: 20rpx;
    transform: translate(0rpx, -120rpx) scale(0.8, 0.8);
    transition: all 0.6s ease 0s;
    overflow: hidden;
    opacity: 0;
  }
  
  .card-swiper swiper-item.cur .swiper-item-text {
    width: 100%;
    transform: translate(0rpx, -100rpx) scale(0.92, 0.92);
    transition: all 0.6s ease 0s;
    opacity: 1;
  }
  
  .image-banner {
    display: flex;
    align-items: center;
    justify-content: center;
    box-shadow: 0rpx 30rpx 40rpx 0rpx rgba(116, 10, 250, 0.08);
  }
  
  .image-banner image {
    width: 100%;
    height: 100%;
  }
  
  /* 轮播指示点 start*/
  .indication {
    max-width: 640px;
    margin: 0 auto;
    z-index: 99;
    width: 100%;
    height: 36rpx;
    position: absolute;
    display: flex;
    margin-top: -50rpx;
    flex-direction: row;
    align-items: center;
    justify-content: center;
  }
  
  .spot {
    background-color: #00000040;
    opacity: 0.4;
    width: 10rpx;
    height: 10rpx;
    border-radius: 20rpx;
    margin: 0 8rpx !important;
    position: relative;
  }
  
  .spot.active {
    opacity: 1;
    width: 30rpx;
    background-color: #00000040;
  }
  
  /* 底部安全边距 start*/
  .tn-tabbar-height {
  	min-height: 60rpx;
  	height: calc(80rpx + env(safe-area-inset-bottom));
  	height: calc(80rpx + constant(safe-area-inset-bottom));
  }
  
  /* 资讯主图 start*/
  .image-article {
    border-radius: 20rpx;
    width: 330rpx;
    height: 230rpx;
    position: relative;
  }
  
  .image-pic {
    background-size: cover;
    background-repeat: no-repeat;
    background-position: top;
    border-radius: 20rpx;
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
  
  /* 新增OA色系，自行调用，或者拿色值去用，多种方式*/
  .oa-black{
    color: #1D2541;
    background-color: #1D254150;
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
  
</style>
