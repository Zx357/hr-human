<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { useAppStore } from '@/store/modules/app';
import { useAuthStore } from '@/store/modules/auth';
import { $t } from '@/locales';
import dayjs from 'dayjs';
import { fetchEmployeePage } from '@/service/api/hr';
import { fetchPendingPage } from '@/service/api/application';
import { fetchDailyRecordPage } from '@/service/api/attendance';

defineOptions({ name: 'HeaderBanner' });

const appStore = useAppStore();
const authStore = useAuthStore();

const gap = computed(() => (appStore.isMobile ? 0 : 16));

const loading = ref(false);
const employeeTotal = ref<number>(0);
const pendingTotal = ref<number>(0);
const todayAbnormal = ref<number>(0);

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
  { id: 'employeeTotal', title: '在职员工', value: employeeTotal.value, icon: 'mdi:account-group', color: '#6366f1' },
  { id: 'pendingTotal', title: '待审批', value: pendingTotal.value, icon: 'mdi:file-clock-outline', color: '#f59e0b' },
  { id: 'todayAbnormal', title: '今日异常', value: todayAbnormal.value, icon: 'mdi:alert-decagram-outline', color: '#ef4444' }
]);

async function loadTopStats() {
  loading.value = true;
  try {
    const today = dayjs().format('YYYY-MM-DD');

    const [empRes, pendingRes, dailyRes] = await Promise.all([
      fetchEmployeePage({ pageNum: 1, pageSize: 1, status: 1 }),
      fetchPendingPage({ pageNum: 1, pageSize: 1 }),
      fetchDailyRecordPage({ page: 1, size: 2000, startDate: today, endDate: today })
    ]);

    employeeTotal.value = empRes?.data?.total ?? 0;
    pendingTotal.value = pendingRes?.data?.total ?? 0;

    const records = dailyRes?.data?.records ?? [];
    todayAbnormal.value = records.filter(r => r.status !== 1).length;
  } catch {
    // ignore: keep zeroes for dashboard
  } finally {
    loading.value = false;
  }
}

onMounted(() => {
  loadTopStats();
});
</script>

<template>
  <div class="header-banner mb-18px" v-loading="loading">
    <div class="banner-bg">
      <div class="banner-content">
        <ElRow :gutter="gap" class="w-full items-center">
          <ElCol :md="16" :sm="24">
            <div class="flex items-center">
              <div class="avatar-ring">
                <img src="@/assets/imgs/avatar.svg" class="size-full" />
              </div>
              <div class="pl-20px">
                <h2 class="text-22px font-bold mb-4px" style="color: var(--el-text-color-primary)">
                  {{ timeGreeting }}，{{ authStore.userInfo.userName }}
                </h2>
                <p class="text-14px" style="color: var(--el-text-color-secondary)">{{ todayStr }} · 欢迎回到人资管理工作台</p>
              </div>
            </div>
          </ElCol>
          <ElCol :md="8" :sm="24">
            <div class="stat-row">
              <div v-for="item in statisticData" :key="item.id" class="stat-item">
                <div class="stat-value">{{ item.value }}</div>
                <div class="stat-label">
                  <SvgIcon :icon="item.icon" class="text-14px mr-4px" />
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
