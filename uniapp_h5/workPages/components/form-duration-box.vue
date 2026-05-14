<template>
  <view class="duration-box">
    <view class="duration-label">预计时长</view>
    <view class="duration-value" :class="{ placeholder: !value && !loading }">
      {{ displayText }}
    </view>
  </view>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  loading: Boolean,
  value: {
    type: Number,
    default: 0
  },
  unit: {
    type: String,
    default: '小时'
  }
})

const displayText = computed(() => {
  if (props.loading) return '计算中...'
  if (props.value > 0) return `${props.value} ${props.unit}`
  return '选择时间后自动计算'
})
</script>
