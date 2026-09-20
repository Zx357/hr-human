<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { changeFeedbackStatus, deleteFeedback, fetchFeedbackPage, handleFeedbackReply } from '@/service/api/system';
import { formatDateTime } from '@/utils/format';
import { $t } from '@/locales';

defineOptions({ name: 'SystemFeedback' });

interface FeedbackRow {
  id: number;
  feedbackType: number;
  feedbackContent: string;
  contactName?: string;
  contactPhone?: string;
  employeeId?: number;
  employeeNo?: string;
  status: number;
  replyContent?: string;
  replyTime?: string;
  createdTime?: string;
}

const queryParams = reactive({
  current: 1,
  size: 10,
  keyword: '',
  employeeNo: '',
  feedbackType: undefined as number | undefined,
  status: undefined as number | undefined
});

const tableData = ref<FeedbackRow[]>([]);
const total = ref(0);
const loading = ref(false);
const drawerVisible = ref(false);
const activeRow = ref<FeedbackRow | null>(null);
const replyContent = ref('');
const replying = ref(false);

async function fetchData() {
  loading.value = true;
  try {
    const res = await fetchFeedbackPage({ ...queryParams });

    const data = res.data;
    if (data) {
      tableData.value = (data.records as unknown as FeedbackRow[]) || [];
      total.value = data.total || 0;
    }
  } finally {
    loading.value = false;
  }
}

function handleSearch() {
  queryParams.current = 1;
  fetchData();
}

function handleReset() {
  queryParams.keyword = '';
  queryParams.employeeNo = '';
  queryParams.feedbackType = undefined;
  queryParams.status = undefined;
  handleSearch();
}

function handlePageChange(page: number) {
  queryParams.current = page;
  fetchData();
}

function handleSizeChange(size: number) {
  queryParams.size = size;
  queryParams.current = 1;
  fetchData();
}

function handleView(row: FeedbackRow) {
  activeRow.value = row;
  replyContent.value = row.replyContent || '';
  drawerVisible.value = true;
}

async function handleReply() {
  if (!activeRow.value) return;

  replying.value = true;
  try {
    const res = await handleFeedbackReply(activeRow.value.id, { replyContent: replyContent.value });

    if (!res.error) {
      ElMessage.success($t('sys.feedback.processedSuccessfully'));
      drawerVisible.value = false;
      fetchData();
    }
  } finally {
    replying.value = false;
  }
}

async function handleStatus(row: FeedbackRow, status: number) {
  const res = await changeFeedbackStatus(row.id, status);

  if (!res.error) {
    ElMessage.success($t('sys.feedback.statusUpdated'));
    fetchData();
  }
}

async function handleDelete(row: FeedbackRow) {
  try {
    await ElMessageBox.confirm($t('sys.feedback.areYouSureYouWantToDeleteThisFeedback'), $t('common.tip'), { type: 'warning' });
    const res = await deleteFeedback(row.id);

    if (!res.error) {
      ElMessage.success($t('common.deleteSuccess'));
      fetchData();
    }
  } catch {
    // canceled
  }
}

function typeText(type: number) {
  const map: Record<number, string> = {
    1: $t('sys.feedback.featureSuggestion'),
    2: $t('sys.feedback.issueFeedback'),
    3: $t('common.other')
  };
  return map[type] || $t('common.other');
}

function statusText(status: number) {
  const map: Record<number, string> = {
    0: $t('sys.feedback.pending'),
    1: $t('sys.feedback.processing'),
    2: $t('sys.feedback.processed')
  };
  return map[status] || $t('sys.feedback.pending');
}

function statusTagType(status: number) {
  if (status === 2) return 'success';
  if (status === 1) return 'warning';
  return 'danger';
}

onMounted(() => {
  fetchData();
});
</script>

<template>
  <div class="list-page">
    <ElCard class="search-card">
      <ElForm :model="queryParams" inline>
        <ElFormItem :label="$t('common.keyword')">
          <ElInput
            v-model="queryParams.keyword"
            :placeholder="$t('sys.feedback.feedbackContentContactPhone')"
            clearable
            @keyup.enter="handleSearch"
          />
        </ElFormItem>
        <ElFormItem :label="$t('common.employeeNo')">
          <ElInput v-model="queryParams.employeeNo" :placeholder="$t('common.pleaseInputEmployeeNo')" clearable @keyup.enter="handleSearch" />
        </ElFormItem>
        <ElFormItem :label="$t('common.type')">
          <ElSelect v-model="queryParams.feedbackType" :placeholder="$t('common.pleaseSelectType')" clearable style="width: 130px">
            <ElOption :label="$t('sys.feedback.featureSuggestion')" :value="1" />
            <ElOption :label="$t('sys.feedback.issueFeedback')" :value="2" />
            <ElOption :label="$t('common.other')" :value="3" />
          </ElSelect>
        </ElFormItem>
        <ElFormItem :label="$t('common.status')">
          <ElSelect v-model="queryParams.status" :placeholder="$t('common.pleaseSelectStatus')" clearable style="width: 130px">
            <ElOption :label="$t('sys.feedback.pending')" :value="0" />
            <ElOption :label="$t('sys.feedback.processing')" :value="1" />
            <ElOption :label="$t('sys.feedback.processed')" :value="2" />
          </ElSelect>
        </ElFormItem>
        <ElFormItem>
          <ElButton type="primary" @click="handleSearch">
            <template #icon><icon-ep-search /></template>
            {{ $t('common.search') }}
          </ElButton>
          <ElButton @click="handleReset">
            <template #icon><icon-ep-refresh /></template>
            {{ $t('common.reset') }}
          </ElButton>
        </ElFormItem>
      </ElForm>
    </ElCard>

    <ElCard class="table-card">
      <template #header>
        <div class="flex items-center justify-between">
          <span>{{ $t('sys.feedback.feedback') }}</span>
          <ElButton type="primary" plain @click="fetchData">
            <template #icon><icon-ep-refresh /></template>
            {{ $t('common.refresh2') }}
          </ElButton>
        </div>
      </template>

      <div class="table-wrapper">
        <ElTable v-loading="loading" :data="tableData" border stripe height="100%">
          <ElTableColumn prop="id" label="ID" width="80" />
          <ElTableColumn prop="feedbackType" :label="$t('common.type')" width="110" align="center">
            <template #default="{ row }">
              <ElTag type="primary">{{ typeText(row.feedbackType) }}</ElTag>
            </template>
          </ElTableColumn>
          <ElTableColumn prop="feedbackContent" :label="$t('sys.feedback.feedbackContent')" min-width="320" show-overflow-tooltip />
          <ElTableColumn prop="contactName" :label="$t('common.contact')" width="120" show-overflow-tooltip />
          <ElTableColumn prop="employeeNo" :label="$t('common.employeeNo')" width="120" />
          <ElTableColumn prop="contactPhone" :label="$t('common.contactPhone')" width="150" />
          <ElTableColumn prop="status" :label="$t('common.status')" width="100" align="center">
            <template #default="{ row }">
              <ElTag :type="statusTagType(row.status)" size="small">{{ statusText(row.status) }}</ElTag>
            </template>
          </ElTableColumn>
          <ElTableColumn prop="createdTime" :label="$t('sys.feedback.submitTime')" width="180">
            <template #default="{ row }">{{ formatDateTime(row.createdTime) }}</template>
          </ElTableColumn>
          <ElTableColumn :label="$t('common.action')" width="230" fixed="right" align="center">
            <template #default="{ row }">
              <ElButton type="primary" link @click="handleView(row)">{{ $t('common.view') }}</ElButton>
              <ElButton v-if="row.status === 0" v-permission="'system:feedback:reply'" type="warning" link @click="handleStatus(row, 1)">{{ $t('sys.feedback.processing') }}</ElButton>
              <ElButton v-if="row.status !== 2" v-permission="'system:feedback:reply'" type="success" link @click="handleStatus(row, 2)">{{ $t('sys.feedback.processed') }}</ElButton>
              <ElButton v-permission="'system:feedback:delete'" type="danger" link @click="handleDelete(row)">{{ $t('common.delete') }}</ElButton>
            </template>
          </ElTableColumn>
        </ElTable>
      </div>

      <div class="mt-16px flex justify-end">
        <ElPagination
          v-model:current-page="queryParams.current"
          v-model:page-size="queryParams.size"
          :total="total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @current-change="handlePageChange"
          @size-change="handleSizeChange"
        />
      </div>
    </ElCard>

    <ElDrawer v-model="drawerVisible" :title="$t('sys.feedback.feedbackDetails')" size="520px">
      <template v-if="activeRow">
        <ElDescriptions :column="1" border>
          <ElDescriptionsItem :label="$t('common.type')">{{ typeText(activeRow.feedbackType) }}</ElDescriptionsItem>
          <ElDescriptionsItem :label="$t('common.status')">{{ statusText(activeRow.status) }}</ElDescriptionsItem>
          <ElDescriptionsItem :label="$t('common.contact')">{{ activeRow.contactName || '-' }}</ElDescriptionsItem>
          <ElDescriptionsItem :label="$t('common.employeeNo')">{{ activeRow.employeeNo || '-' }}</ElDescriptionsItem>
          <ElDescriptionsItem :label="$t('common.contactPhone')">{{ activeRow.contactPhone || '-' }}</ElDescriptionsItem>
          <ElDescriptionsItem :label="$t('sys.feedback.submitTime')">{{ formatDateTime(activeRow.createdTime) }}</ElDescriptionsItem>
        </ElDescriptions>

        <div class="mt-16px">
          <div class="mb-8px font-medium">{{ $t('sys.feedback.feedbackContent') }}</div>
          <ElInput :model-value="activeRow.feedbackContent" type="textarea" :rows="6" readonly />
        </div>

        <div class="mt-16px">
          <div class="mb-8px font-medium">{{ $t('sys.feedback.handleReply') }}</div>
          <ElInput v-model="replyContent" type="textarea" :rows="5" :placeholder="$t('sys.feedback.enterHandlingResultOrReplyContent')" />
        </div>
      </template>

      <template #footer>
        <ElButton @click="drawerVisible = false">{{ $t('common.cancel') }}</ElButton>
        <ElButton v-permission="'system:feedback:reply'" type="primary" :loading="replying" @click="handleReply">{{ $t('sys.feedback.saveReplyAndMarkAsProcessed') }}</ElButton>
      </template>
    </ElDrawer>
  </div>
</template>

<style scoped></style>
