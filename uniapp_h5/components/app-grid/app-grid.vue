<template>
  <div class="tu-grid">
    <div class="tu-grid-item">
      <div class="tu-grid-item-content">
        <slot></slot>
      </div>
    </div>
  </div>
</template>

<style scoped lang="scss">
.tu-grid {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}
</style>
