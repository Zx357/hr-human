<script setup lang="ts">
defineOptions({
  name: 'GlobalFooter'
});

const currentYear = new Date().getFullYear();
</script>

<template>
  <DarkModeContainer class="h-full flex-center">
    <span class="text-gray-500">Copyright © {{ currentYear }} 人资管理系统</span>
  </DarkModeContainer>
</template>

<style scoped></style>
