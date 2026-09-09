<template>
  <view
    v-if="openModal"
    class="wx-modal"
  >
    <view
      class="wam__mask"
      @touchmove.prevent="closeModal"
      @tap.stop="closeModal"
    ></view>
    
    <!-- 内容区域 -->
    <view class="wam__wrapper" :style="{ top: vuex_custom_bar_height - 30 + 'px' }">
      <!-- 关闭按钮 -->
      <!-- <view class="wam__close-btn" @tap.stop="closeModal">
        <text class="tn-icon-close"></text>
      </view> -->
      
      <view class="">
        
        <view class="bubble">
        </view>
        <view class="bubble-content" @tap.stop="closeModal">
          <view class="tn-flex tn-flex-col-center tn-color-white" style="padding: 20rpx 30rpx;" @click="tn('/partnerPages/create')">
            <view class="tn-icon-chat" style="font-size: 50rpx;">
            </view>
            <view class="tn-text-lg tn-padding-left">发起群聊</view>
          </view>
        </view>

      </view>
      
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
      modelValue: {
        type: Boolean,
        default: false
      },
      value: {
        type: Boolean,
        default: false
      }
    },
    emits: ['update:modelValue', 'input'],
    computed: {
      openModal() {
        return this.modelValue || this.value
      },
      vuex_custom_bar_height() {
        return this.$store?.state?.vuex_custom_bar_height || 0
      }
    },
    methods: {
      //App的扫码引擎，使用业内开源的通用扫码库，识别效率比不过微信、支付宝等商业扫码库。如需更强的扫码效果，去开发一个叭
      scanCode() {
        this.closeModal()
        // 允许从相机和相册扫码
        uni.scanCode({
        	success: function (res) {
        		console.log('条码类型：' + res.scanType);
        		console.log('条码内容：' + res.result);
        	}
        });
      },
      
      // 关闭弹框
      closeModal() {
        this.$emit('update:modelValue', false)
        this.$emit('input', false)
      },
      
      // 跳转
      tn(e) {
        this.closeModal()
        uni.navigateTo({
          url: e,
        });
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
        // background-color: rgba(0, 0, 0, 0.5);
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
        // position: absolute;
        // left: 0;
        // background-color: #FFFFFF80;
        max-width: 640px;
        margin: 0 auto;
        border-radius: 20rpx 20rpx 0rpx 0rpx;
        margin-top: 60rpx;
        transform-origin: center top;
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
  
  .bubble {
    margin-left: 34rpx;
    height: 0;
    width: 0;
    border-top: 20rpx solid transparent;
    border-right: 20rpx solid transparent;
    border-bottom: 20rpx solid #000000CC;
    border-left: 20rpx solid transparent;
  }
  
  .bubble-content{
    background-color: #000000CC;
    width: 300rpx;
    margin: 0 0 0 10rpx;
    padding: 10rpx 0;
    border-radius: 24rpx;
  }
  
  /* 间隔线 start*/
  .tn-strip-bottom-min {
    width: 100%;
    border-bottom: 1rpx solid #F8F7F810;
  } 
   
  .tn-strip-bottom {
   width: 100%;
   border-bottom: 20rpx solid #F8F7F8;
  }
   /* 间隔线 end*/
  
  /* 底部悬浮按钮 start*/
  .tn-tabbar-height {
  	min-height: 160rpx;
  	height: calc(180rpx + env(safe-area-inset-bottom) / 2);
    height: calc(180rpx + constant(safe-area-inset-bottom));
  }
  /* 底部悬浮按钮 end*/
</style>
