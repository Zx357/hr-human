<script setup lang="ts">
import { computed, onMounted } from 'vue';
import dayjs from 'dayjs';
import { useAppStore } from '@/store/modules/app';
import { useAuthStore } from '@/store/modules/auth';
import { useHomeStats } from './use-home-stats';

defineOptions({ name: 'HeaderBanner' });

const appStore = useAppStore();
const authStore = useAuthStore();

const gap = computed(() => (appStore.isMobile ? 0 : 16));

const { loading, stats, load } = useHomeStats();

const timeGreeting = computed(() => {
  const hour = dayjs().hour();
  if (hour < 9) return '早安';
  if (hour < 12) return '上午好';
  if (hour < 14) return '中午好';
  if (hour < 18) return '下午好';
  return '晚上好';
});

const todayStr = computed(() => dayjs().format('YYYY年M月D日 dddd'));

const statisticData = computed(() => [
  {
    id: 'employeeTotal',
    title: '在职员工',
    value: stats.value.employeeTotal,
    icon: 'mdi:account-group',
    color: '#6366f1'
  },
  {
    id: 'pendingTotal',
    title: '待审批',
    value: stats.value.pendingTotal,
    icon: 'mdi:file-clock-outline',
    color: '#f59e0b'
  },
  {
    id: 'todayAbnormal',
    title: '今日异常',
    value: stats.value.todayAbnormal,
    icon: 'mdi:alert-decagram-outline',
    color: '#ef4444'
  }
]);

onMounted(() => {
  load();
});
</script>

<template>
  <div v-loading="loading" class="header-banner mb-18px">
    <div class="banner-bg">
      <div class="banner-content">
        <ElRow :gutter="gap" class="w-full items-center">
          <ElCol :md="16" :sm="24">
            <div class="flex items-center">
              <div class="avatar-ring">
                <img src="@/assets/imgs/avatar.svg" class="size-full" />
              </div>
              <div class="pl-20px">
                <h2 class="mb-4px text-22px font-bold" style="color: var(--el-text-color-primary)">
                  {{ timeGreeting }}，{{ authStore.userInfo.userName }}
                </h2>
                <p class="text-14px" style="color: var(--el-text-color-secondary)">
                  {{ todayStr }} · 欢迎回到人资管理工作台
                </p>
              </div>
            </div>
          </ElCol>
          <ElCol :md="8" :sm="24">
            <div class="stat-row">
              <div v-for="item in statisticData" :key="item.id" class="stat-item">
                <div class="stat-value">{{ item.value }}</div>
                <div class="stat-label">
                  <SvgIcon :icon="item.icon" class="mr-4px text-14px" />
                  {{ item.title }}
                </div>
              </div>
            </div>
          </ElCol>
        </ElRow>
      </div>
    </div>
  </div>
</template>

<style scoped lang="scss">
.header-banner {
  border-radius: 12px;
  overflow: hidden;
}

.banner-bg {
  background: var(--el-bg-color);
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 12px;
  position: relative;
  overflow: hidden;
}

.banner-content {
  position: relative;
  z-index: 1;
  padding: 28px 32px;
}

.avatar-ring {
  width: 64px;
  height: 64px;
  border-radius: 50%;
  overflow: hidden;
  border: 3px solid var(--el-border-color-lighter);
  flex-shrink: 0;
  background: var(--el-fill-color-light);
}

.stat-row {
  display: flex;
  justify-content: flex-end;
  gap: 28px;
}

.stat-item {
  text-align: center;
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: var(--el-text-color-primary);
  line-height: 1.2;
}

.stat-label {
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
  color: var(--el-text-color-secondary);
  margin-top: 4px;
}

@media (max-width: 768px) {
  .banner-content {
    padding: 20px 16px;
  }

  .stat-row {
    justify-content: flex-start;
    margin-top: 16px;
  }
}
</style>
