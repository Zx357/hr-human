<template>
  <picker mode="date" :value="modelValue" @change="handleChange">
    <view class="field-row">
      <view>
        <view class="field-label">{{ label }} <text>*</text></view>
        <view class="field-value" :class="{ placeholder: !modelValue }">
          {{ modelValue || '请选择' }}
        </view>
      </view>
      <tn-icon name="right"></tn-icon>
    </view>
  </picker>
</template>

<script setup>
defineProps({
  modelValue: {
    type: String,
    default: ''
  },
  label: {
    type: String,
    required: true
  }
})

const emit = defineEmits(['update:modelValue'])

function handleChange(event) {
  emit('update:modelValue', event.detail.value)
}
</script>
