<template>
  <view class="tn-landscape-class tn-landscape">
    <view v-if="showValue" class="tn-landscape__container" :style="[containerStyle]">
      <slot></slot>
      <view
        v-if="closeBtn"
        class="tn-landscape__icon tn-icon-close-fill"
        :class="[{
          'tn-landscape__icon--left-top': closePosition === 'leftTop',
          'tn-landscape__icon--right-top': closePosition === 'rightTop',
          'tn-landscape__icon--bottom': closePosition === 'bottom'
        }]"
        :style="[closeBtnStyle]"
        @tap="close"
      ></view>
    </view>
    <view
      v-if="mask"
      class="tn-landscape__mask"
      :class="[{'tn-landscape__mask--show': showValue}]"
      :style="[maskStyle]"
      @tap="closeMask"
    ></view>
  </view>
</template>

<script setup>
  import { ref, computed, watch } from 'vue'
  
  const props = defineProps({
    // 显示
    show: {
      type: Boolean,
      default: false
    },
    // 显示关闭图标
    closeBtn: {
      type: Boolean,
      default: true
    },
    // 关闭图标颜色
    closeColor: {
      type: String,
      default: ''
    },
    // 关闭图标大小，单位rpx
    closeSize: {
      type: Number,
      default: 0
    },
    // 关闭图标位置
    // leftTop -> 左上角 rightTop -> 右上角 bottom -> 底部
    closePosition: {
      type: String,
      default: 'rightTop'
    },
    // 关闭图标top值，单位rpx
    // 当关闭图标在leftTop或者rightTop时生效
    closeTop: {
      type: Number,
      default: 0
    },
    // 关闭图标right值，单位rpx
    // 当关闭图标在RightTop时生效
    closeRight: {
      type: Number,
      default: 0
    },
    // 关闭图标bottom值，单位rpx
    // 当关闭图标在bottom时生效
    closeBottom: {
      type: Number,
      default: 0
    },
    // 关闭图标left值，单位rpx
    // 当关闭图标在leftTop时生效
    closeLeft: {
      type: Number,
      default: 0
    },
    // 显示遮罩
    mask: {
      type: Boolean,
      default: true
    },
    // 点击遮罩可以关闭
    maskCloseable: {
      type: Boolean,
      default: true
    },
    // zIndex
    zIndex: {
      type: Number,
      default: 0
    }
  })
  
  const emit = defineEmits(['close'])
  
  // 显示状态
  const showValue = ref(false)
  
  // 容器样式
  const containerStyle = computed(() => {
    let style = {}
    style.zIndex = props.zIndex ? props.zIndex : 20070
    return style
  })
  
  // 关闭按钮样式
  const closeBtnStyle = computed(() => {
    let style = {}
    if (props.closePosition === 'leftTop') {
      if (props.closeTop) {
        style.top = props.closeTop + 'rpx'
      }
      if (props.closeLeft) {
        style.left = props.closeLeft + 'rpx'
      }
    } else if (props.closePosition === 'rightTop') {
      if (props.closeTop) {
        style.top = props.closeTop + 'rpx'
      }
      if (props.closeRight) {
        style.right = props.closeRight + 'rpx'
      }
    } else if (props.closePosition === 'bottom') {
      if (props.closeBottom) {
        style.bottom = props.closeBottom + 'rpx'
      }
    }
    if (props.closeSize) {
      style.fontSize = props.closeSize + 'rpx'
    }
    if (props.closeColor) {
      style.color = props.closeColor
    }
    return style
  })
  
  // 遮罩样式
  const maskStyle = computed(() => {
    let style = {}
    style.zIndex = props.zIndex ? props.zIndex - 1 : 20069
    return style
  })
  
  // 监听show变化
  watch(() => props.show, (val) => {
    showValue.value = val
  }, { immediate: true })
  
  // 关闭压屏窗
  const close = () => {
    showValue.value = false
    emit('close')
  }
  
  // 点击遮罩关闭
  const closeMask = () => {
    if (!props.maskCloseable) return
    close()
  }
</script>

<style lang="scss" scoped>
  .tn-landscape {
    width: 100%;
    overflow: hidden;
    
    &__container {
      max-width: 100%;
      position: fixed;
      display: inline-flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      left: 50%;
      top: 50%;
      transform: translate(-50%, -50%);
    }
    
    &__icon {
      position: absolute;
      text-align: center;
      font-size: 50rpx;
      color: #FFFFFF;
      
      &--left-top {
        top: -40rpx;
        left: 20rpx;
      }
      
      &--right-top {
        top: -40rpx;
        right: 40rpx;
      }
      
      &--bottom {
        left: 50%;
        bottom: -40rpx;
        transform: translateX(-50%);
      }
    }
    
    &__mask {
      position: fixed;
      width: 100%;
      height: 100%;
      background-color: $tn-mask-bg-color;
      top: 0;
      right: 0;
      bottom: 0;
      left: 0;
      opacity: 0;
      transform: scale3d(1, 1, 0);
      transition: all 0.25s ease-in;
      
      &--show {
        opacity: 1 !important;
        transform: scale3d(1, 1, 1);
      }
    }
  }
</style>
