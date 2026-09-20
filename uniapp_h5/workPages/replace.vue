<template>
  <ApplyForm type="replace" :default-card-date="defaultCardDate" />
</template>

<script setup>
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import ApplyForm from './components/apply-form.vue'

defineOptions({ name: 'WorkReplace' })

const defaultCardDate = ref('')

onLoad((options) => {
  if (options && options.date) {
    defaultCardDate.value = options.date
  }
})
</script>
