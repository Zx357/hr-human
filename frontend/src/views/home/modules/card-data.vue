<script setup lang="ts">
import { computed, onMounted } from 'vue';
import { useHomeStats } from './use-home-stats';

defineOptions({ name: 'CardData' });

interface CardItem {
  key: string;
  title: string;
  value: number;
  icon: string;
  iconBg: string;
  iconColor: string;
  trend?: string;
}

const { loading, stats, load } = useHomeStats();

const cardData = computed<CardItem[]>(() => [
  {
    key: 'employeeTotal',
    title: '在职员工',
    value: stats.value.employeeTotal,
    icon: 'mdi:account-group',
    iconBg: 'rgba(99, 102, 241, 0.1)',
    iconColor: '#6366f1'
  },
  {
    key: 'pendingTotal',
    title: '待审批',
    value: stats.value.pendingTotal,
    icon: 'mdi:file-clock-outline',
    iconBg: 'rgba(245, 158, 11, 0.1)',
    iconColor: '#f59e0b'
  },
  {
    key: 'todayClockTotal',
    title: '今日打卡',
    value: stats.value.todayClockTotal,
    icon: 'mdi:fingerprint',
    iconBg: 'rgba(16, 185, 129, 0.1)',
    iconColor: '#10b981'
  },
  {
    key: 'todayAbnormal',
    title: '今日异常',
    value: stats.value.todayAbnormal,
    icon: 'mdi:alert-circle-outline',
    iconBg: 'rgba(239, 68, 68, 0.1)',
    iconColor: '#ef4444'
  }
]);

onMounted(() => {
  load();
});
</script>

<template>
  <ElRow v-loading="loading" :gutter="18" class="mb-18px">
    <ElCol v-for="item in cardData" :key="item.key" :xl="6" :lg="6" :md="12" :sm="12" :xs="24" class="mb-sm-0 mb-12px">
      <div class="stat-card">
        <div class="stat-card-body">
          <div class="stat-info">
            <span class="stat-title">{{ item.title }}</span>
            <div class="stat-value">
              <CountTo :start-value="0" :end-value="item.value" class="text-28px font-bold" />
            </div>
          </div>
          <div class="stat-icon" :style="{ background: item.iconBg }">
            <SvgIcon :icon="item.icon" class="text-24px" :style="{ color: item.iconColor }" />
          </div>
        </div>
      </div>
    </ElCol>
  </ElRow>
</template>

<style scoped lang="scss">
.stat-card {
  background: var(--el-bg-color);
  border-radius: 12px;
  padding: 20px 24px;
  border: 1px solid var(--el-border-color-lighter);
  transition: all 0.3s ease;

  &:hover {
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.06);
    transform: translateY(-2px);
  }
}

.stat-card-body {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.stat-info {
  min-width: 0;
}

.stat-title {
  font-size: 14px;
  color: var(--el-text-color-secondary);
}

.stat-value {
  margin-top: 8px;
  color: var(--el-text-color-primary);
}

.stat-icon {
  width: 52px;
  height: 52px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
</style>
