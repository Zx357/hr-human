<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { changeFeedbackStatus, deleteFeedback, fetchFeedbackPage, handleFeedbackReply } from '@/service/api/system';

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

async function fetchData() {
  loading.value = true;
  try {
    const res = await fetchFeedbackPage({ ...queryParams });

    const data = res.data;
    if (data) {
      tableData.value = data.records || [];
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

  const res = await handleFeedbackReply(activeRow.value.id, { replyContent: replyContent.value });

  if (res.data !== null && res.data !== undefined) {
    ElMessage.success('处理成功');
    drawerVisible.value = false;
    fetchData();
  }
}

async function handleStatus(row: FeedbackRow, status: number) {
  const res = await changeFeedbackStatus(row.id, status);

  if (res.data !== null && res.data !== undefined) {
    ElMessage.success('状态已更新');
    fetchData();
  }
}

async function handleDelete(row: FeedbackRow) {
  try {
    await ElMessageBox.confirm('确定删除这条反馈吗？', '提示', { type: 'warning' });
    const res = await deleteFeedback(row.id);

    if (res.data !== null && res.data !== undefined) {
      ElMessage.success('删除成功');
      fetchData();
    }
  } catch {
    // canceled
  }
}

function typeText(type: number) {
  const map: Record<number, string> = {
    1: '功能建议',
    2: '问题反馈',
    3: '其他'
  };
  return map[type] || '其他';
}

function statusText(status: number) {
  const map: Record<number, string> = {
    0: '待处理',
    1: '处理中',
    2: '已处理'
  };
  return map[status] || '待处理';
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
        <ElFormItem label="关键词">
          <ElInput v-model="queryParams.keyword" placeholder="反馈内容/联系人/电话" clearable />
        </ElFormItem>
        <ElFormItem label="工号">
          <ElInput v-model="queryParams.employeeNo" placeholder="请输入工号" clearable />
        </ElFormItem>
        <ElFormItem label="类型">
          <ElSelect v-model="queryParams.feedbackType" placeholder="请选择类型" clearable style="width: 130px">
            <ElOption label="功能建议" :value="1" />
            <ElOption label="问题反馈" :value="2" />
            <ElOption label="其他" :value="3" />
          </ElSelect>
        </ElFormItem>
        <ElFormItem label="状态">
          <ElSelect v-model="queryParams.status" placeholder="请选择状态" clearable style="width: 130px">
            <ElOption label="待处理" :value="0" />
            <ElOption label="处理中" :value="1" />
            <ElOption label="已处理" :value="2" />
          </ElSelect>
        </ElFormItem>
        <ElFormItem>
          <ElButton type="primary" @click="handleSearch">
            <template #icon><icon-ep-search /></template>
            搜索
          </ElButton>
          <ElButton @click="handleReset">
            <template #icon><icon-ep-refresh /></template>
            重置
          </ElButton>
        </ElFormItem>
      </ElForm>
    </ElCard>

    <ElCard class="table-card">
      <template #header>
        <div class="flex items-center justify-between">
          <span>意见反馈</span>
          <ElButton type="primary" plain @click="fetchData">
            <template #icon><icon-ep-refresh /></template>
            刷新
          </ElButton>
        </div>
      </template>

      <div class="table-wrapper">
        <ElTable v-loading="loading" :data="tableData" border stripe height="100%">
          <ElTableColumn prop="id" label="ID" width="80" />
          <ElTableColumn prop="feedbackType" label="类型" width="110" align="center">
            <template #default="{ row }">
              <ElTag type="primary">{{ typeText(row.feedbackType) }}</ElTag>
            </template>
          </ElTableColumn>
          <ElTableColumn prop="feedbackContent" label="反馈内容" min-width="320" show-overflow-tooltip />
          <ElTableColumn prop="contactName" label="联系人" width="120" show-overflow-tooltip />
          <ElTableColumn prop="employeeNo" label="工号" width="120" />
          <ElTableColumn prop="contactPhone" label="联系电话" width="150" />
          <ElTableColumn prop="status" label="状态" width="100" align="center">
            <template #default="{ row }">
              <ElTag :type="statusTagType(row.status)" size="small">{{ statusText(row.status) }}</ElTag>
            </template>
          </ElTableColumn>
          <ElTableColumn prop="createdTime" label="提交时间" width="180" />
          <ElTableColumn label="操作" width="230" fixed="right" align="center">
            <template #default="{ row }">
              <ElButton type="primary" link @click="handleView(row)">查看</ElButton>
              <ElButton v-if="row.status === 0" type="warning" link @click="handleStatus(row, 1)">处理中</ElButton>
              <ElButton v-if="row.status !== 2" type="success" link @click="handleStatus(row, 2)">已处理</ElButton>
              <ElButton type="danger" link @click="handleDelete(row)">删除</ElButton>
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

    <ElDrawer v-model="drawerVisible" title="反馈详情" size="520px">
      <template v-if="activeRow">
        <ElDescriptions :column="1" border>
          <ElDescriptionsItem label="类型">{{ typeText(activeRow.feedbackType) }}</ElDescriptionsItem>
          <ElDescriptionsItem label="状态">{{ statusText(activeRow.status) }}</ElDescriptionsItem>
          <ElDescriptionsItem label="联系人">{{ activeRow.contactName || '-' }}</ElDescriptionsItem>
          <ElDescriptionsItem label="工号">{{ activeRow.employeeNo || '-' }}</ElDescriptionsItem>
          <ElDescriptionsItem label="联系电话">{{ activeRow.contactPhone || '-' }}</ElDescriptionsItem>
          <ElDescriptionsItem label="提交时间">{{ activeRow.createdTime || '-' }}</ElDescriptionsItem>
        </ElDescriptions>

        <div class="mt-16px">
          <div class="mb-8px font-medium">反馈内容</div>
          <ElInput :model-value="activeRow.feedbackContent" type="textarea" :rows="6" readonly />
        </div>

        <div class="mt-16px">
          <div class="mb-8px font-medium">处理回复</div>
          <ElInput v-model="replyContent" type="textarea" :rows="5" placeholder="填写处理结果或回复内容" />
        </div>
      </template>

      <template #footer>
        <ElButton @click="drawerVisible = false">取消</ElButton>
        <ElButton type="primary" @click="handleReply">保存回复并标记已处理</ElButton>
      </template>
    </ElDrawer>
  </div>
</template>

<style scoped></style>
