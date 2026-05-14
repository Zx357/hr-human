<template>
  <div class="tu-grid-item">
    <div class="tu-grid-item-content">
      <slot></slot>
    </div>
  </div>
</template>

<style scoped lang="scss">
.tu-grid-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 12px;
  border-radius: 8px;
  background-color: #f5f5f5;
}

.tu-grid-item-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}
</style>
