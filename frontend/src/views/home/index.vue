<script setup lang="ts">
import { computed } from 'vue';
import { useAppStore } from '@/store/modules/app';
import HeaderBanner from './modules/header-banner.vue';
import CardData from './modules/card-data.vue';
import LineChart from './modules/line-chart.vue';
import PieChart from './modules/pie-chart.vue';
import EducationChart from './modules/education-chart.vue';
import ProjectNews from './modules/project-news.vue';
import CreativityBanner from './modules/creativity-banner.vue';

const appStore = useAppStore();

const gap = computed(() => (appStore.isMobile ? 0 : 18));
</script>

<template>
  <div class="home-page">
    <HeaderBanner />
    <CardData />
    <div class="mb-18px">
      <LineChart />
    </div>
    <ElRow :gutter="gap" class="w-full">
      <ElCol :lg="12" :sm="24" class="mb-18px">
        <PieChart />
      </ElCol>
      <ElCol :lg="12" :sm="24" class="mb-18px">
        <EducationChart />
      </ElCol>
    </ElRow>
    <ElRow :gutter="gap">
      <ElCol :lg="15" :sm="24" class="mb-18px">
        <ProjectNews />
      </ElCol>
      <ElCol :lg="9" :sm="24" class="mb-18px">
        <CreativityBanner />
      </ElCol>
    </ElRow>
  </div>
</template>

<style scoped lang="scss">
.home-page {
  display: flex;
  flex-direction: column;
  gap: 0;
  padding-bottom: 0;
}
</style>
