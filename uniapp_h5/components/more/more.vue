<template>
  <view
    v-if="openModal"
    class="wx-modal"
  >
    <view
      class="wam__mask"
      @touchmove.prevent=""
      @tap.stop="closeModal"
    ></view>
    
    <!-- 内容区域 -->
    <view class="wam__wrapper">
      <!-- 关闭按钮 -->
      <!-- <view class="wam__close-btn" @tap.stop="closeModal">
        <text class="tn-icon-close"></text>
      </view> -->
      
      <view class="tn-padding-top-sm">
        <swiper class="card-swiper" :circular="false"
          :autoplay="false" duration="500" interval="5000" @change="cardSwiper"> 
          <swiper-item :class="cardCur==index?'cur':''">
            <!-- 方式5 start-->
            <view class="tn-flex tn-flex-wrap tn-padding-bottom">
             <block v-for="(item, index) in icons1" :key="index">
              <view class="tn-margin-bottom tn-margin-top-sm" style="width: 25%;" @click="tn(item.url)">
                <view class="tn-flex tn-flex-direction-column tn-flex-row-center tn-flex-col-center">
                  <!-- 当然，如果你有图片，可以换成图片模式 -->
                  <!-- <view class="icon13__item--icon tn-flex tn-flex-row-center tn-flex-col-center" :style="'background-image:url('+ item.img +');background-size:100% 100%;background-size: cover;'">
                  </view> -->
                  <view class="icon12__item--icon tn-flex tn-flex-row-center tn-flex-col-center tn-color-white" :style="'background-color:'+ item.color +';'" >
                    <view :class="[`tn-icon-${item.icon}`]" style="text-shadow: 5rpx 8rpx 10rpx rgba(0,0,0,0.16);transform: rotate(19deg);">
                      <!-- <tn-badge backgroundColor="#EA5E55" fontColor="#FFFFFF" :absolute="true" :fontSize="16" :translateCenter="false" top="-30rpx" right="-26rpx">
                        <text>16</text>
                      </tn-badge> -->
                    </view>
                  </view>
                  <view class="tn-color-gray--dark tn-text-center tn-text-df">
                    <text class="tn-text-ellipsis">{{ item.title }}</text>
                  </view>
                </view>
              </view>
             </block>
            </view>
            <!-- 方式5 end-->
          </swiper-item>
          <swiper-item :class="cardCur==index?'cur':''">
            <!-- 方式5 start-->
            <view class="tn-flex tn-flex-wrap tn-padding-bottom">
             <block v-for="(item, index) in icons2" :key="index">
              <view class="tn-margin-bottom tn-margin-top-sm" style="width: 25%;" @click="tn(item.url)">
                <view class="tn-flex tn-flex-direction-column tn-flex-row-center tn-flex-col-center">
                  <!-- 当然，如果你有图片，可以换成图片模式 -->
                  <!-- <view class="icon13__item--icon tn-flex tn-flex-row-center tn-flex-col-center" :style="'background-image:url('+ item.img +');background-size:100% 100%;background-size: cover;'">
                  </view> -->
                  <view class="icon12__item--icon tn-flex tn-flex-row-center tn-flex-col-center tn-color-white" :style="'background-color:'+ item.color +';'" >
                    <view :class="[`tn-icon-${item.icon}`]" style="text-shadow: 5rpx 8rpx 10rpx rgba(0,0,0,0.16);transform: rotate(19deg);">
                      <tn-badge backgroundColor="#EA5E55" fontColor="#FFFFFF" :absolute="true" :fontSize="16" :translateCenter="false" top="-30rpx" right="-26rpx">
                        <text>16</text>
                      </tn-badge>
                    </view>
                  </view>
                  <view class="tn-color-gray--dark tn-text-center tn-text-df">
                    <text class="tn-text-ellipsis">{{ item.title }}</text>
                  </view>
                </view>
              </view>
             </block>
            </view>
            <!-- 方式5 end-->
          </swiper-item>
        </swiper>
        <view class="indication">
            <block v-for="(item,index) in 2" :key="index">
                <view class="spot" :class="cardCur==index?'active':''"></view>
            </block>
        </view>
      </view>
      
      
      <!-- 悬浮按钮-->
      <!-- <view class="tn-flex tn-footerfixed" style="padding: 40rpx;margin-bottom: 100rpx;">
        <view class="tn-flex-1 justify-content-item tn-margin-left-xs tn-text-center">
          <tn-button backgroundColor="#07C160 " padding="40rpx 0" width="100%" :fontSize="28" fontColor="#FFFFFF" shape="round" @tap.stop="closeModal">
            <text class="">下一步</text>
          </tn-button>
        </view>
      </view> -->
      
      
    </view>
  </view>
</template>

<script>
  
  export default {
    options: {
      // 在微信小程序中将组件节点渲染为虚拟节点，更加接近Vue组件的表现(不会出现shadow节点下再去创建元素)
      virtualHost: true
    },
    props: {
      value: {
        type: Boolean,
        default: false
      }
    },
    data() {
      return {
        openModal: false,
        
        cardCur: 0,
        /* 功能区*/
        icons1: [
          {
            img: "https://cdn.nlark.com/yuque/0/2022/png/280373/1666764808285-assets/web-upload/b83d1b36-7355-4f36-bc02-9f06b8c0867c.png",
            title: "相册",
            icon: 'image-fill',
            color: '#4B98FE',
            url: "/workPages/time"
          },
          {
            img: "https://cdn.nlark.com/yuque/0/2022/png/280373/1666765211148-assets/web-upload/bc9ff0e7-36a5-4d99-8698-cd589b00dc99.png",
            title: "拍摄",
            icon: 'camera-fill',
            color: '#FFAC00',
            url: "/workPages/replace"
          },
          {
            img: "https://cdn.nlark.com/yuque/0/2022/png/280373/1666765049011-assets/web-upload/e49243fa-5182-4fbb-a850-33e927316a90.png",
            title: "语音",
            icon: 'voice-fill',
            color: '#00D05E',
            url: "/workPages/cost"
          },
          {
            img: "https://cdn.nlark.com/yuque/0/2022/png/280373/1666764788499-assets/web-upload/4cf1bbab-efb8-401c-9a2d-21689d024491.png",
            title: "电话",
            icon: 'tel-circle-fill',
            color: '#FE871B',
            url: "/workPages/report"
          },
          {
            img: "https://cdn.nlark.com/yuque/0/2022/png/280373/1666764932311-assets/web-upload/c9380d70-1291-467e-89dd-40d2b799b986.png",
            title: "红包",
            icon: 'lucky-money-fill',
            color: '#00C8B0',
            url: "/workPages/overtime"
          },
          {
            img: "https://cdn.nlark.com/yuque/0/2022/png/280373/1666764788528-assets/web-upload/955b13dd-7715-4627-b8cc-04ae3d85051a.png",
            title: "文件",
            icon: 'folder-fill',
            color: '#00B9FE',
            url: "/workPages/device"
          },
          {
            img: "https://cdn.nlark.com/yuque/0/2022/png/280373/1666765211145-assets/web-upload/98a55f9a-6188-4c95-a947-4d2db29bc08a.png",
            title: "名片",
            icon: 'identity-fill',
            color: '#FB6A67',
            url: "/workPages/travel"
          },
          {
            img: "https://cdn.nlark.com/yuque/0/2022/png/280373/1666764932305-assets/web-upload/8d5ff7dd-c2b0-4455-acf9-df6ba3a064b1.png",
            title: "收藏",
            icon: 'star-fill',
            color: '#957BFE',
            url: "/workPages/leave"
          },
        ],
        
        /* 功能区2*/
        icons2: [
          {
            img: "https://cdn.nlark.com/yuque/0/2022/png/280373/1666764808285-assets/web-upload/b83d1b36-7355-4f36-bc02-9f06b8c0867c.png",
            title: "位置",
            icon: 'map-fill',
            color: '#4B98FE',
            url: "/workPages/time"
          },
          {
            img: "https://cdn.nlark.com/yuque/0/2022/png/280373/1666765211148-assets/web-upload/bc9ff0e7-36a5-4d99-8698-cd589b00dc99.png",
            title: "邮箱",
            icon: 'email-fill',
            color: '#FFAC00',
            url: "/workPages/replace"
          },
          {
            img: "https://cdn.nlark.com/yuque/0/2022/png/280373/1666765049011-assets/web-upload/e49243fa-5182-4fbb-a850-33e927316a90.png",
            title: "日程",
            icon: 'pushpin-fill',
            color: '#00D05E',
            url: "/workPages/cost"
          },
          {
            img: "https://cdn.nlark.com/yuque/0/2022/png/280373/1666764788499-assets/web-upload/4cf1bbab-efb8-401c-9a2d-21689d024491.png",
            title: "待办",
            icon: 'flag-fill',
            color: '#FE871B',
            url: "/workPages/report"
          },
          {
            img: "https://cdn.nlark.com/yuque/0/2022/png/280373/1666764932311-assets/web-upload/c9380d70-1291-467e-89dd-40d2b799b986.png",
            title: "审批",
            icon: 'seal',
            color: '#00C8B0',
            url: "/workPages/overtime"
          },
          {
            img: "https://cdn.nlark.com/yuque/0/2022/png/280373/1666764788528-assets/web-upload/955b13dd-7715-4627-b8cc-04ae3d85051a.png",
            title: "应用",
            icon: 'menu-classify-fill',
            color: '#00B9FE',
            url: "/workPages/device"
          }
        ],
        
      }
    },
    watch: {
      value: {
        handler(val) {
          this.openModal = val
        },
        immediate: true
      }
    },
    methods: {
      
      // 关闭弹框
      closeModal() {
        this.$emit('input', false)
      },
      
      // cardSwiper
      cardSwiper(e) {
        this.cardCur = e.detail.current
      },
      
    }
  }
</script>

<style lang="scss" scoped>
  
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
  
  
  .wx-modal {
    position: fixed;
    left: 0;
    top: 0;
    width: 100vw;
    height: 100vh;
    z-index: 99998 !important;
    
    view {
      box-sizing: border-box;
    }
    
    .image {
      width: 100%;
      height: 100%;
      border-radius: inherit;
    }
    
    .wam {
      z-index: 9999 !important;
      /* mask */
      &__mask {
        position: absolute;
        left: 0;
        top: 0;
        width: 100%;
        height: 100%;
        background-color: rgba(0, 0, 0, 0.5);
        opacity: 0;
        animation: showMask 0.25s ease 0.1s forwards;
      }
      
      /* close-btn */
      &__close-btn {
        position: absolute;
        top: 30rpx;
        right: 30rpx;
        z-index: 99999;
        font-size: 40rpx;
      }
      
      /* wrapper */
      &__wrapper {
        position: absolute;
        left: 0;
        bottom: 0;
        width: 100%;
        background-color: #FFFFFF;
        border-radius: 40rpx 40rpx 0rpx 0rpx;
        padding-top: 60rpx;
        padding-bottom: 40rpx;
        padding-bottom: calc(constant(safe-area-inset-bottom) + 40rpx);
        padding-bottom: calc(env(safe-area-inset-bottom) + 40rpx);
        transform-origin: center bottom;
        transform: scaleY(0);
        animation: showWrapper 0.25s ease 0.1s forwards;
        z-index: 99999;
      }
      
    }
  }
  
  @keyframes showMask {
    0% {
      opacity: 0;
    }
    100% {
      opacity: 1;
    }
  }
  @keyframes showWrapper {
    0% {
      transform: scaleY(0);
      opacity: 0;
    }
    100% {
      transform: scaleY(1);
      opacity: 1;
    }
  }
  
  /* 底部悬浮按钮 start*/
  .tn-tabbar-height {
  	min-height: 160rpx;
  	height: calc(180rpx + env(safe-area-inset-bottom) / 2);
    height: calc(180rpx + constant(safe-area-inset-bottom));
  }
  .tn-footerfixed {
    width: 100%;
    margin-top: 60rpx;
    margin-bottom: calc(env(safe-area-inset-bottom) - 20rpx );
    z-index: 1024;
    box-shadow: 0 1rpx 6rpx rgba(0, 0, 0, 0);
  
  }
  /* 底部悬浮按钮 end*/
  
  
  /* 金刚区轮播 start */
  .card-swiper {
    height: 470rpx !important;
  }
    
  .card-swiper swiper-item {
    width: 750rpx !important;
    left: 0rpx;
    box-sizing: border-box;
    // padding: 0rpx 30rpx 90rpx 30rpx;
    overflow: initial;
  }
    
  .card-swiper swiper-item .swiper-item {
    width: 100%;
    display: block;
    height: 100%;
    transform: scale(1);
    transition: all 0.2s ease-in 0s;
    will-change: transform;
    overflow: hidden;
  }
    
  .card-swiper swiper-item.cur .swiper-item {
    transform: none;
    transition: all 0.2s ease-in 0s;
    will-change: transform;
  }
    
  .card-swiper swiper-item .swiper-item-text {
    margin-top: -300rpx;
    text-align: center;
    width: 100%;
    display: block;
    height: 50%;
    border-radius: 10rpx;
    transform: translate(100rpx, 0rpx) scale(0.9, 0.9);
    transition: all 0.6s ease 0s;
    will-change: transform;
    overflow: hidden;
  }
    
  .card-swiper swiper-item.cur .swiper-item-text {
    margin-top: -300rpx;
    width: 100%;
    transform: translate(0rpx, 0rpx) scale(0.9, 0.9);
    transition: all 0.6s ease 0s;
    will-change: transform;
  }
  
  .image-banner{
    display: flex;
    align-items: center;
    justify-content: center;
  }
  .image-banner image{
    width: 100%;
    height: 100%;
  }
  
  /* 轮播指示点 start*/
  .indication{
    z-index: 9999;
    width: 100%;
    height: 36rpx;
    position: absolute;
    display:flex;
    flex-direction:row;
    align-items:center;
    justify-content:center;
  }
  
  .spot{
    background-color: #000000;
    opacity: 0.1;
    width: 10rpx;
    height: 10rpx;
    border-radius: 20rpx;
    top: -70rpx;
    margin: 0 8rpx !important;
    position: relative;
  }
  
  .spot.active{
    opacity: 0.15;
    width: 30rpx;
    background-color: #000000;
  }
  
  .icon12 {
    &__item {
      transform: scale(1);
      transition: transform 0.3s linear;
      transform-origin: center center;
      
      &--icon {
        width: 100rpx;
        height: 100rpx;
        font-size: 56rpx;
        transform: rotate(-19deg);
        border-radius: 43% 57% 43% 57% / 57% 43% 57% 43%;
        margin-bottom: 18rpx;
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
          transform: scale(1, 1) rotate(19deg);
          background-size: 100% 100%;
          background-image: url(https://resource.tuniaokj.com/images/cool_bg_image/icon_bg4.png);
        }
      }
      
    }
  }
  
  .icon13 {
    &__item {
      transform: scale(1);
      transition: transform 0.3s linear;
      transform-origin: center center;
      
      &--icon {
        width: 100rpx;
        height: 100rpx;
        font-size: 56rpx;
        margin-bottom: 18rpx;
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
          background-image: url(https://resource.tuniaokj.com/images/cool_bg_image/icon_bg4.png);
            
        }
       
      }
      
    }
  }
  
</style>