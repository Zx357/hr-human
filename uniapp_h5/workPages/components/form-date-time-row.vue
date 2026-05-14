<template>
  <view class="date-time-row">
    <view class="date-time-label">{{ label }}<text>*</text></view>
    <view class="date-time-controls">
      <picker class="date-time-picker" mode="date" :value="date" @change="emitDate">
        <view class="date-time-value" :class="{ placeholder: !date }">
          {{ date || '日期' }}
        </view>
      </picker>
      <picker class="date-time-picker date-time-picker--time" mode="time" :value="time" @change="emitTime">
        <view class="date-time-value" :class="{ placeholder: !time }">
          {{ time || '时间' }}
        </view>
      </picker>
    </view>
  </view>
</template>

<script setup>
defineProps({
  label: {
    type: String,
    required: true
  },
  date: {
    type: String,
    default: ''
  },
  time: {
    type: String,
    default: ''
  }
})

const emit = defineEmits(['date', 'time'])

function emitDate(event) {
  emit('date', event.detail.value)
}

function emitTime(event) {
  emit('time', event.detail.value)
}
</script>
