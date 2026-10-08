<template>
  <view class="input-row">
    <view class="field-label">{{ label }} <text>*</text></view>
    <input
      class="plain-input tn-apply-input"
      :type="type"
      :value="modelValue"
      :placeholder="placeholder"
      placeholder-style="color:#AEB8C8"
      @input="handleInput"
    />
  </view>
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
  },
  placeholder: {
    type: String,
    default: '请输入'
  },
  // 输入框类型:text / digit / number 等(金额、数量场景用数字键盘)
  type: {
    type: String,
    default: 'text'
  }
})

const emit = defineEmits(['update:modelValue'])

function handleInput(event) {
  emit('update:modelValue', event.detail.value)
}
</script>
