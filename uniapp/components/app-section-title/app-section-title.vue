<template>
  <view class="app-section-title">
    <view class="left">
      <text class="title">{{ title }}</text>
      <text v-if="subtitle" class="subtitle">{{ subtitle }}</text>
    </view>
    <view v-if="$slots.right" class="right">
      <slot name="right" />
    </view>
  </view>
</template>

<script>
export default {
  name: 'AppSectionTitle',
  props: {
    title: { type: String, required: true },
    subtitle: { type: String, default: '' }
  }
}
</script>

<style lang="scss" scoped>
@import "@/static/scss/tokens.scss";

.app-section-title {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16rpx;
  padding: 4rpx 0;
}

.left {
  min-width: 0;
}

.title {
  font-size: $app-font-lg;
  font-weight: 600;
  color: $app-text;
}

.subtitle {
  margin-left: 12rpx;
  font-size: $app-font-sm;
  color: $app-text-2;
}
</style>

