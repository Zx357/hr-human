<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { type ReminderItem, fetchReminders } from '@/service/api/reminder';
import { $t } from '@/locales';

defineOptions({ name: 'ReminderCard' });

const loading = ref(false);
const dialogVisible = ref(false);
const activeTab = ref('contract');

const contracts = ref<ReminderItem[]>([]);
const probations = ref<ReminderItem[]>([]);
const certificates = ref<ReminderItem[]>([]);

const total = computed(() => contracts.value.length + probations.value.length + certificates.value.length);

const latest = computed<ReminderItem[]>(() =>
  [...contracts.value, ...probations.value, ...certificates.value]
    .slice()
    .sort((a, b) => Number(a.remainDays) - Number(b.remainDays))
    .slice(0, 3)
);

const tabs = computed(() => [
  { name: 'contract', label: $t('home.reminderCard.contractExpirations', { count: contracts.value.length }), items: contracts.value },
  { name: 'probation', label: $t('home.reminderCard.probationExpirations', { count: probations.value.length }), items: probations.value },
  { name: 'certificate', label: $t('home.reminderCard.certificateExpirations', { count: certificates.value.length }), items: certificates.value }
]);

/** 到期文案：负数已过期、0 今天到期、正数 N 天后到期 */
function getRemainText(item: ReminderItem) {
  const days = Number(item.remainDays);
  if (Number.isFinite(days) && days < 0) {
    return $t('home.reminderCard.expiredDaysAgo', { days: Math.abs(Math.trunc(days)) });
  }
  if (days === 0) {
    return $t('home.reminderCard.expiresToday');
  }
  return $t('home.reminderCard.expiresInDays', { days: Math.trunc(days) });
}

function getRemainClass(item: ReminderItem) {
  const days = Number(item.remainDays);
  if (Number.isFinite(days) && days < 0) return 'text-gray-400';
  if (days <= 3) return 'text-red-500';
  if (days <= 7) return 'text-orange-500';
  return 'text-gray-500';
}

async function loadReminders() {
  loading.value = true;
  try {
    const res = await fetchReminders(30);
    contracts.value = res.data?.contracts || [];
    probations.value = res.data?.probations || [];
    certificates.value = res.data?.certificates || [];
  } catch {
    // 请求层已统一弹错
  } finally {
    loading.value = false;
  }
}

onMounted(() => {
  loadReminders();
});
</script>

<template>
  <div v-loading="loading" class="stat-card reminder-card" @click="dialogVisible = true">
    <div class="stat-card-body">
      <div class="stat-info">
        <span class="stat-title">
          <SvgIcon icon="mdi:bell-ring-outline" class="mr-4px text-14px text-#f59e0b" />
          {{ $t('home.reminderCard.expirationReminders30Days') }}
        </span>
        <div class="stat-value">
          <CountTo :start-value="0" :end-value="total" class="text-28px font-bold" />
        </div>
      </div>
      <div class="stat-icon" style="background: rgba(245, 158, 11, 0.1)">
        <SvgIcon icon="mdi:alarm-note" class="text-24px" style="color: #f59e0b" />
      </div>
    </div>

    <div v-if="latest.length > 0" class="reminder-list">
      <div v-for="item in latest" :key="`${item.type}-${item.employeeId}-${item.date}`" class="reminder-item">
        <span class="truncate">{{ item.employeeName }}{{ $t('home.reminderCard.s') }}{{ item.typeName }}</span>
        <span class="shrink-0 font-500" :class="getRemainClass(item)">{{ getRemainText(item) }}</span>
      </div>
      <div v-if="total > latest.length" class="reminder-more">{{ $t('home.reminderCard.total') }} {{ total }} {{ $t('home.reminderCard.itemsClickToViewAll') }}</div>
    </div>
    <ElEmpty v-else :description="$t('home.reminderCard.noUpcomingExpirations')" :image-size="60" />
  </div>

  <ElDialog v-model="dialogVisible" :title="$t('home.reminderCard.expirationReminders')" width="640px">
    <ElTabs v-model="activeTab">
      <ElTabPane v-for="tab in tabs" :key="tab.name" :label="tab.label" :name="tab.name">
        <ElTable v-if="tab.items.length > 0" :data="tab.items" border stripe size="small" max-height="420">
          <ElTableColumn prop="employeeName" :label="$t('common.employee')" width="100" />
          <ElTableColumn prop="typeName" :label="$t('common.type')" width="110" />
          <ElTableColumn prop="date" :label="$t('home.reminderCard.expirationDate')" width="120" />
          <ElTableColumn :label="$t('home.reminderCard.timeRemaining')" width="110" align="center">
            <template #default="{ row }">
              <span :class="getRemainClass(row)">{{ getRemainText(row) }}</span>
            </template>
          </ElTableColumn>
          <ElTableColumn prop="detail" :label="$t('common.details')" min-width="140" show-overflow-tooltip>
            <template #default="{ row }">{{ row.detail || '-' }}</template>
          </ElTableColumn>
        </ElTable>
        <ElEmpty v-else :description="$t('common.noData')" :image-size="80" />
      </ElTabPane>
    </ElTabs>
    <template #footer>
      <ElButton @click="dialogVisible = false">{{ $t('common.close') }}</ElButton>
    </template>
  </ElDialog>
</template>

<style scoped lang="scss">
.stat-card {
  background: var(--el-bg-color);
  border-radius: 12px;
  padding: 20px 24px;
  border: 1px solid var(--el-border-color-lighter);
  transition: all 0.3s ease;
  cursor: pointer;

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

.reminder-list {
  margin-top: 12px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.reminder-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  font-size: 13px;
  padding: 6px 10px;
  border-radius: 8px;
  background: var(--el-fill-color-light);
}

.reminder-more {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  text-align: center;
}
</style>
