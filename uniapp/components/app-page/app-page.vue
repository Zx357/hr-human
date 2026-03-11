<template>
  <view class="app-page" :style="pageStyle">
    <slot />
  </view>
</template>

<script>
export default {
  name: 'AppPage',
  props: {
    padding: {
      type: String,
      default: '24rpx'
    }
  },
  computed: {
    pageStyle() {
      return {
        padding: this.padding
      }
    }
  }
}
</script>

<style lang="scss" scoped>
</style>

