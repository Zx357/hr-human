<template>
  <view class="textarea-row">
    <view class="textarea-head">
      <view class="field-label">
        {{ label }}<text v-if="required">*</text>
      </view>
      <text>{{ maxlength }}字内</text>
    </view>
    <textarea
      class="plain-textarea tn-apply-textarea"
      :value="modelValue"
      :maxlength="maxlength"
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
  maxlength: {
    type: Number,
    default: 200
  },
  required: {
    type: Boolean,
    default: true
  }
})

const emit = defineEmits(['update:modelValue'])

function handleInput(event) {
  emit('update:modelValue', event.detail.value)
}
</script>
