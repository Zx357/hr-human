<template>
  <view class="oa-content">
    <!-- 顶部自定义导航 -->
    <tn-navbar fixed bg-color="#ffffff00" :placeholder="false" customBack>
      <template #back><view class='tn-custom-nav-bar__back' @click="goBack">
        <tn-icon name="left" class='icon'></tn-icon>
        <tn-icon name="home-capsule-fill" class='icon'></tn-icon>
      </view></template>
    </tn-navbar>

    <swiper class="card-swiper" :circular="true" :autoplay="true" duration="500" interval="5000" @change="cardSwiper">
      <swiper-item v-for="(item,index) in swiperList" :key="index" :class="cardCur==index?'cur':''">
        <view class="swiper-item image-banner">
          <image :src="item.url" mode="aspectFill" v-if="item.type=='image'"></image>
        </view>
      </swiper-item>
    </swiper>
    <view class="indication">
      <block v-for="(item,index) in swiperList" :key="index">
        <view class="spot" :class="cardCur==index?'active':''"></view>
      </block>
    </view>
    
    <view class="top-wrap tn-padding-top-sm">
      
      <view class="tn-padding" style="background-color: #FFFFFF;border-radius: 24rpx;margin: 30rpx 30rpx 30rpx 30rpx;position: relative;">
        <view class="tn-flex tn-flex-row-between tn-flex-col-center">
          <view class="justify-content-item tn-text-bold tn-text-xl tn-flex-basic-xl">
            图鸟龙年盲盒
          </view>
          <view class="justify-content-item tn-text-sm tn-text-right tn-color-gray--dark">
            <text>剩余 129 份</text>
          </view>
        </view>
        
        <view class="tn-text-justify tn-padding-top-sm">
          龙衣颜色随机，兑换后，凭兑换码前往人事部领取
        </view>
        
        <view class="tn-flex tn-flex-row-between tn-flex-col-center tn-padding-top-lg">
          <view class="justify-content-item" style="font-size: 52rpx;color: #E83A37;">
            <text class="tn-text-bold">129</text>
            <text class="tn-margin-left-xs tn-text-xs tn-color-gray">图鸟币</text>
          </view>
          <view class="justify-content-item tn-color-gray--dark tn-text-sm">
            <text class="tn-padding-right-xs">已兑换</text>
            <text class="">126</text>
          </view>
        </view>
        
      </view>

      <view class="" style="margin: 30rpx 28rpx 0 28rpx;">
        <tn-tabs v-model="current" :bar="true" activeColor="#000000" bold fontSize="30" bgColor="#FFFFFF00" height="80">
          <tn-tabs-item
            v-for="(item, index) in fixedList"
            :key="index"
            :title="item.name"
          />
        </tn-tabs>
      </view>


      <view v-if="current==0" class="tn-padding tn-text-justify" style="background-color: #FFFFFF;border-radius: 0 0 24rpx 24rpx;margin: 0rpx 30rpx 0rpx 30rpx;position: relative;">
        <view class="">
          <image src='https://cdn.nlark.com/yuque/0/2023/jpeg/280373/1692940242362-assets/web-upload/39542345-83ab-456c-b309-e156174d5561.jpeg' mode='widthFix' style="border-radius: 12rpx;width: 100%;"></image>
        </view>
        <view class="tn-padding-top">
          <text class="">富文本内容演示区富文本内容演示区富文本内容演示区富文本内容演示区富文本内容演示区</text>
        </view>
      </view>
      
      <view v-if="current==1" class="tn-padding tn-text-justify" style="background-color: #FFFFFF;border-radius: 0 0 24rpx 24rpx;margin: 0rpx 30rpx 0rpx 30rpx;position: relative;">
        <view class="tn-color-gray tn-padding-top-sm">是否盒装<text class="tn-color-black tn-padding-left-sm">是</text></view>
        <view class="tn-color-gray tn-padding-top-sm">规格尺寸<text class="tn-color-black tn-padding-left-sm">8x8x12cm</text></view>
        <view class="tn-color-gray tn-padding-top-sm">生产产地<text class="tn-color-black tn-padding-left-sm">广东省广州市番禺区</text></view>
        <view class="tn-color-gray tn-padding-top-sm">生产日期<text class="tn-color-black tn-padding-left-sm">2023-11-18</text></view>
        <view class="tn-color-gray tn-padding-top-sm">保质期限<text class="tn-color-black tn-padding-left-sm">永久</text></view>
      </view>
      
      <view v-if="current==2" class="tn-padding tn-text-justify" style="background-color: #FFFFFF;border-radius: 0 0 24rpx 24rpx;margin: 0rpx 30rpx 0rpx 30rpx;position: relative;">
        <view class="tn-color-gray tn-padding-top-sm">服务<text class="tn-color-black tn-padding-left-sm">不可退换</text></view>
        <view class="tn-color-gray tn-padding-top-sm">须知<text class="tn-color-black tn-padding-left-sm">相关瑕疵品，可反馈联系人事部</text></view>
      </view>
      
      <view class="tn-footerfixed">
        <view class="tn-flex tn-flex-row-between tn-flex-col-center">
          <view class="tn-flex-1 justify-content-item tn-color-gray">
            <view class="tn-flex tn-flex-row-center">
              <view class="tn-flex-1 tn-padding-left tn-padding-right" @click="goBack">
                <tn-icon name="home-vertical" style="font-size: 42rpx;padding-bottom: 3rpx;"></tn-icon>
                <view class="tn-text-center tn-text-xs">首页</view>
              </view>
              <view class="tn-flex-1 tn-padding-right-lg">
                <button class="tn-button--clear-style" open-type="share" style="border-radius: 0;">
                  <tn-icon name="share-circle" style="font-size: 42rpx;padding-bottom: 3rpx;"></tn-icon>
                  <view class="tn-text-center tn-text-xs">分享</view>
                </button>
              </view>
            </view>
          </view>
          <view class="tn-flex-2 justify-content-item tn-padding-top-xs tn-padding-bottom-xs">
            <view class="tn-flex tn-flex-row-between">
              <view class="tn-flex-1 justify-content-item tn-margin-xs tn-text-center">
                <tn-button
                  bg-color="#FFAC00"
                  :custom-style="{padding:'35rpx 0'}"
                  width="90%"
                  :fontSize="30"
                  text-color="#FFFFFF"
                  shape="round"
                  @click="tn('')"
                >
                  <text class="">赚取积分</text>
                </tn-button>
              </view>
              <view class="tn-flex-1 justify-content-item tn-margin-xs tn-text-center">
                <tn-button
                  bg-color="#E83A37"
                  :custom-style="{padding:'35rpx 0'}"
                  width="90%"
                  :fontSize="30"
                  text-color="#FFFFFF"
                  shape="round"
                  @click="tn('/momentPages/convert')"
                >
                  <text class="">立即兑换</text>
                </tn-button>
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
import { ref, reactive } from 'vue'
import { useGoBack } from '@/libs/composables'
const { goBack } = useGoBack()
defineOptions({
  name: 'TemplateProduct'
})

// tabs样式
const barStyle = reactive({
  borderRadius: '100rpx',
  backgroundColor: '#FFC745'
})

const activeItemStyle = reactive({
  borderRadius: '24rpx 24rpx 0 0',
  borderLeft: "2rpx solid #F9F9F9",
  borderRight: "2rpx solid #F9F9F9",
  backgroundColor: '#FFFFFF',
})

const inactiveItemStyle = reactive({
  borderRadius: '24rpx 24rpx 0 0',
  borderLeft: "2rpx solid #F9F9F9",
  borderRight: "2rpx solid #F9F9F9",
  backgroundColor: '#F2F2F2',
})

const current = ref(0)
const fixedList = ref([
  {name: '产品介绍'},
  {name: '基础信息'},
  {name: '关于售后'}
])

const cardCur = ref(0)
const swiperList = ref([
  {
    id: 0,
    type: 'image',
    url: 'https://cdn.nlark.com/yuque/0/2023/jpeg/280373/1681955091346-assets/web-upload/e05d86a5-1cec-40b0-8198-734d8b8379da.jpeg',
  }, {
    id: 1,
    type: 'image',
    url: 'https://cdn.nlark.com/yuque/0/2023/jpeg/280373/1681955091346-assets/web-upload/e05d86a5-1cec-40b0-8198-734d8b8379da.jpeg',
  }, {
    id: 2,
    type: 'image',
    url: 'https://cdn.nlark.com/yuque/0/2023/jpeg/280373/1681955091346-assets/web-upload/e05d86a5-1cec-40b0-8198-734d8b8379da.jpeg',
  }, {
    id: 3,
    type: 'image',
    url: 'https://cdn.nlark.com/yuque/0/2023/jpeg/280373/1681955091346-assets/web-upload/e05d86a5-1cec-40b0-8198-734d8b8379da.jpeg',
  }
])

const tagList = ref([
  { title: "盲盒" },
  { title: "手办" },
  { title: "本地" },
  { title: "质保" },
  { title: "龙年" }
])

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
    width: 100%;
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
  
    &:before {
      content: " ";
      width: 1rpx;
      height: 110%;
      position: absolute;
      top: 22.5%;
      left: 0;
      right: 0;
      margin: auto;
      transform: scale(0.5);
      transform-origin: 0 0;
      pointer-events: none;
      box-sizing: border-box;
      opacity: 0.7;
      background-color: #FFFFFF;
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
  
  /* 标签 start*/
  .tag-product {
    display: inline-block;
    padding: 10rpx 20rpx;
    margin: 0 15rpx 10rpx 0;
    border-radius: 10rpx;
    background-color: #000000;
    color: #ECA534;
    border: 1rpx solid #FFD994;
  }
  
  /* 标签 end*/
  
  /* 图片 start */
  .nav-image {
    width: 40rpx;
    height: 40rpx;
    position: relative;
    overflow: hidden;
  }
  
  .nav-pic {
    background-size: cover;
    background-repeat: no-repeat;
    background-position: center;
    overflow: hidden;
  }
  
  /* 内容 */
  .top-wrap {
    position: relative;
    z-index: 1;
    margin-top: -140rpx;  
    border-radius: 60rpx 60rpx 0 0;
  }
  


  /* 底部安全边距 start*/
  .tn-tabbar-height {
    background-color: #F9F9F9;
  	min-height: 140rpx;
  	height: calc(160rpx + env(safe-area-inset-bottom) / 2);
    height: calc(160rpx + constant(safe-area-inset-bottom));
  }


  /* 轮播视觉差 start */
  .card-swiper {
    height: 780rpx !important;
  }

  .card-swiper swiper-item {
    width: 750rpx !important;
    left: 0rpx;
    box-sizing: border-box;
    overflow: initial;
  }

  .card-swiper swiper-item .swiper-item {
    width: 100%;
    display: block;
    height: 100%;
    transform: scale(1);
    transition: all 0.2s ease-in 0s;
    overflow: hidden;
  }

  .card-swiper swiper-item.cur .swiper-item {
    transform: none;
    transition: all 0.2s ease-in 0s;
  }

  .image-banner {
    display: flex;
    align-items: center;
    justify-content: center;
  }

  .image-banner image {
    width: 100%;
    height: 100%;
  }

  /* 轮播指示点 start*/
  .indication {
    max-width: 640px;
    margin: 0 auto;
    z-index: 9999;
    width: 100%;
    height: 36rpx;
    position: absolute;
    display: flex;
    flex-direction: row;
    align-items: center;
    justify-content: center;
  }

  .spot {
    background-color: #FFFFFF;
    opacity: 0.6;
    width: 10rpx;
    height: 10rpx;
    border-radius: 20rpx;
    top: -180rpx;
    margin: 0 8rpx !important;
    position: relative;
  }

  .spot.active {
    opacity: 1;
    width: 30rpx;
    background-color: #FFFFFF;
  }


  /* 底部*/
  .tn-footerfixed {
    max-width: 640px;
    margin: 0 auto;
    position: fixed;
    background-color: #FFFFFF;
    box-shadow: 0rpx 0rpx 30rpx 0rpx rgba(0, 0, 0, 0.07);
    bottom: calc(env(safe-area-inset-bottom) / 2);
    width: 100%;
    transition: all 0.25s ease-out;
    will-change: transform;
    z-index: 100;
  }
  
</style>
