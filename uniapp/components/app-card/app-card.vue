<template>
  <view class="app-card" :style="cardStyle">
    <slot />
  </view>
</template>

<script>
export default {
  name: 'AppCard',
  props: {
    padding: {
      type: String,
      default: '24rpx'
    },
    radius: {
      type: String,
      default: '16rpx'
    },
    margin: {
      type: String,
      default: '0'
    }
  },
  computed: {
    cardStyle() {
      return {
        padding: this.padding,
        borderRadius: this.radius,
        margin: this.margin
      }
    }
  }
}
</script>

<style lang="scss" scoped>
</style>

