<script setup lang="ts">
import { onBeforeUnmount, onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import { ElTabs, ElTabPane, ElBadge, ElButton } from 'element-plus';
import { type Application, fetchPendingPage } from '@/service/api/application';
import { fetchNoticePage, type SysNoticeItem } from '@/service/api/system';
import { appTypeLabel } from '@/constants/application';
import { formatDateTime } from '@/utils/format';
import { $t } from '@/locales';

defineOptions({ name: 'HeaderNotification' });

const router = useRouter();

const popoverVisible = ref(false);
const activeTab = ref('pending');

const pendingTotal = ref(0);
const pendingList = ref<Application[]>([]);
const noticeList = ref<SysNoticeItem[]>([]);
const loading = ref(false);

let refreshTimer: ReturnType<typeof setInterval> | null = null;

async function loadData() {
  loading.value = true;
  try {
    const [pendingRes, noticeRes] = await Promise.allSettled([
      fetchPendingPage({ pageNum: 1, pageSize: 5 }),
      fetchNoticePage({ current: 1, size: 5, status: 1 })
    ]);
    if (pendingRes.status === 'fulfilled') {
      pendingList.value = pendingRes.value.data?.records || [];
      pendingTotal.value = pendingRes.value.data?.total || 0;
    }
    if (noticeRes.status === 'fulfilled') {
      noticeList.value = noticeRes.value.data?.records || [];
    }
  } finally {
    loading.value = false;
  }
}

function handleChange() {
  popoverVisible.value = !popoverVisible.value;
}

function goPending() {
  popoverVisible.value = false;
  router.push({ name: 'approval_pending' });
}

function goNotice() {
  popoverVisible.value = false;
  router.push({ name: 'system_notice' });
}

onMounted(() => {
  loadData();
  // 60 秒轮询待审批数，保证角标及时性
  refreshTimer = setInterval(loadData, 60_000);
});

onBeforeUnmount(() => {
  if (refreshTimer) {
    clearInterval(refreshTimer);
    refreshTimer = null;
  }
});
</script>

<template>
  <ElPopover v-model:visible="popoverVisible" trigger="click" :width="360" @show="loadData">
    <template #reference>
      <div class="flex-center h-full px-8px cursor-pointer" @click="handleChange">
        <ElBadge :value="pendingTotal" :hidden="!pendingTotal" :max="99">
          <ElIcon :size="18"><icon-ep-bell /></ElIcon>
        </ElBadge>
      </div>
    </template>

    <ElTabs v-model="activeTab">
      <ElTabPane :label="`${$t('headerNotice.pending')}(${pendingTotal})`" name="pending">
        <div v-loading="loading" class="max-h-320px overflow-auto">
          <template v-if="pendingList.length">
            <div
              v-for="item in pendingList"
              :key="item.id"
              class="notice-item"
              @click="goPending"
            >
              <ElTag size="small">{{ appTypeLabel(item.appType) }}</ElTag>
              <span class="notice-item-title">{{ item.employeeName }}</span>
              <span class="notice-item-time">{{ formatDateTime(item.startTime || '') }}</span>
            </div>
            <ElButton link type="primary" class="w-full mt-4px" @click="goPending">
              {{ $t('headerNotice.viewAll') }}
            </ElButton>
          </template>
          <ElEmpty v-else :description="$t('headerNotice.noPending')" :image-size="60" />
        </div>
      </ElTabPane>

      <ElTabPane :label="$t('headerNotice.notices')" name="notices">
        <div v-loading="loading" class="max-h-320px overflow-auto">
          <template v-if="noticeList.length">
            <div
              v-for="item in noticeList"
              :key="item.id"
              class="notice-item"
              @click="goNotice"
            >
              <span class="notice-item-title">{{ item.noticeTitle }}</span>
              <span class="notice-item-time">{{ formatDateTime(item.publishTime || '') }}</span>
            </div>
            <ElButton link type="primary" class="w-full mt-4px" @click="goNotice">
              {{ $t('headerNotice.viewAll') }}
            </ElButton>
          </template>
          <ElEmpty v-else :description="$t('headerNotice.noNotice')" :image-size="60" />
        </div>
      </ElTabPane>
    </ElTabs>
  </ElPopover>
</template>

<style scoped>
.notice-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 4px;
  cursor: pointer;
  border-bottom: 1px solid var(--el-border-color-lighter);
}

.notice-item:hover {
  background: var(--el-fill-color-light);
}

.notice-item-title {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-size: 13px;
}

.notice-item-time {
  font-size: 12px;
  color: var(--el-text-color-secondary);
}
</style>
