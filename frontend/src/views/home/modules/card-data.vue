<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import dayjs from 'dayjs';
import { fetchEmployeePage } from '@/service/api/hr';
import { fetchPendingPage } from '@/service/api/application';
import { fetchClockRecordPage, fetchDailyRecordPage } from '@/service/api/attendance';

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

const loading = ref(false);
const employeeTotal = ref(0);
const pendingTotal = ref(0);
const todayClockTotal = ref(0);
const todayAbnormal = ref(0);

const cardData = computed<CardItem[]>(() => [
  {
    key: 'employeeTotal',
    title: '在职员工',
    value: employeeTotal.value,
    icon: 'mdi:account-group',
    iconBg: 'rgba(99, 102, 241, 0.1)',
    iconColor: '#6366f1'
  },
  {
    key: 'pendingTotal',
    title: '待审批',
    value: pendingTotal.value,
    icon: 'mdi:file-clock-outline',
    iconBg: 'rgba(245, 158, 11, 0.1)',
    iconColor: '#f59e0b'
  },
  {
    key: 'todayClockTotal',
    title: '今日打卡',
    value: todayClockTotal.value,
    icon: 'mdi:fingerprint',
    iconBg: 'rgba(16, 185, 129, 0.1)',
    iconColor: '#10b981'
  },
  {
    key: 'todayAbnormal',
    title: '今日异常',
    value: todayAbnormal.value,
    icon: 'mdi:alert-circle-outline',
    iconBg: 'rgba(239, 68, 68, 0.1)',
    iconColor: '#ef4444'
  }
]);

async function loadCardData() {
  loading.value = true;
  try {
    const today = dayjs().format('YYYY-MM-DD');
    const [empRes, pendingRes, clockRes, dailyRes] = await Promise.all([
      fetchEmployeePage({ pageNum: 1, pageSize: 1, status: 1 }),
      fetchPendingPage({ pageNum: 1, pageSize: 1 }),
      fetchClockRecordPage({ page: 1, size: 1, startDate: today, endDate: today }),
      fetchDailyRecordPage({ page: 1, size: 2000, startDate: today, endDate: today })
    ]);

    employeeTotal.value = empRes?.data?.total ?? 0;
    pendingTotal.value = pendingRes?.data?.total ?? 0;
    todayClockTotal.value = clockRes?.data?.total ?? 0;

    const records = dailyRes?.data?.records ?? [];
    todayAbnormal.value = records.filter(r => r.status !== 1).length;
  } catch {
    // ignore
  } finally {
    loading.value = false;
  }
}

onMounted(() => {
  loadCardData();
});
</script>

<template>
  <ElRow :gutter="18" class="mb-18px" v-loading="loading">
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
