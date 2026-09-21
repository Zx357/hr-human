<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import { appTypeColorMap, appTypeShortLabel } from '@/constants/application';
import { type Application, fetchPendingPage } from '@/service/api/application';
import { formatDateTime } from '@/utils/format';

defineOptions({ name: 'ProjectNews' });

const router = useRouter();
const loading = ref(false);
const list = ref<Application[]>([]);
const total = ref(0);

function getTypeInfo(type: string) {
  return {
    label: appTypeShortLabel(type),
    color: appTypeColorMap[type] ?? '#6b7280'
  };
}

async function loadData() {
  loading.value = true;
  try {
    const res = await fetchPendingPage({ pageNum: 1, pageSize: 6 });
    list.value = res?.data?.records ?? [];
    total.value = res?.data?.total ?? 0;
  } catch {
    list.value = [];
    total.value = 0;
  } finally {
    loading.value = false;
  }
}

function goPending() {
  router.push({ name: 'approval_pending' });
}

onMounted(() => {
  loadData();
});
</script>

<template>
  <ElCard v-loading="loading" class="news-card">
    <template #header>
      <div class="flex items-center justify-between">
        <div class="flex items-center gap-8px">
          <div class="header-dot" style="background: #f59e0b"></div>
          <span class="font-medium">{{ $t('common.pendingApproval') }}</span>
          <ElBadge v-if="total" :value="total" :max="99" class="ml-4px" />
        </div>
        <ElButton link type="primary" @click="goPending">
          {{ $t('home.common.viewAll') }}
          <SvgIcon icon="mdi:chevron-right" class="ml-2px text-16px" />
        </ElButton>
      </div>
    </template>

    <div v-if="!list.length" class="empty-state">
      <SvgIcon icon="mdi:check-circle-outline" class="mb-12px text-48px text-#d1d5db" />
      <p class="text-14px text-#9ca3af">{{ $t('home.projectNews.noPendingApprovalsAllGood') }}</p>
    </div>

    <div v-else class="approval-list">
      <div v-for="item in list" :key="item.id" class="approval-item">
        <div class="item-left">
          <div
            class="type-badge"
            :style="{ background: getTypeInfo(item.appType).color + '15', color: getTypeInfo(item.appType).color }"
          >
            {{ getTypeInfo(item.appType).label }}
          </div>
          <div class="item-info">
            <div class="item-title">
              <span class="font-medium">{{ item.employeeName || '-' }}</span>
              <span class="mx-6px text-#9ca3af">·</span>
              <span class="text-13px text-#9ca3af">{{ item.deptName || '-' }}</span>
            </div>
            <div class="mt-4px truncate text-13px text-#6b7280">{{ item.reason || item.title || '—' }}</div>
          </div>
        </div>
        <div class="item-time whitespace-nowrap text-12px text-#9ca3af">
          {{ formatDateTime(item.createdTime, 'MM-DD HH:mm') }}
        </div>
      </div>
    </div>
  </ElCard>
</template>

<style scoped lang="scss">
.news-card {
  border-radius: 12px;
  border: 1px solid var(--el-border-color-lighter);

  :deep(.el-card__header) {
    border-bottom: 1px solid var(--el-border-color-extra-light);
  }
}

.header-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 48px 0;
}

.approval-list {
  display: flex;
  flex-direction: column;
}

.approval-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 0;
  border-bottom: 1px solid var(--el-border-color-extra-light);
  gap: 12px;

  &:last-child {
    border-bottom: none;
  }

  &:first-child {
    padding-top: 0;
  }
}

.item-left {
  display: flex;
  align-items: center;
  gap: 12px;
  min-width: 0;
  flex: 1;
}

.type-badge {
  font-size: 12px;
  font-weight: 500;
  padding: 4px 10px;
  border-radius: 6px;
  white-space: nowrap;
  flex-shrink: 0;
}

.item-info {
  min-width: 0;
  flex: 1;
}

.item-title {
  display: flex;
  align-items: center;
  font-size: 14px;
}
</style>
