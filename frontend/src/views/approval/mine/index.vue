<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { fetchApplicationPage, type Application } from '@/service/api/application';

defineOptions({ name: 'ApprovalMine' });

const loading = ref(false);
const data = ref<Application[]>([]);
const total = ref(0);
const currentPage = ref(1);
const pageSize = ref(10);

const searchParams = ref({ appType: undefined as string | undefined, status: undefined as number | undefined });

async function loadData() {
  loading.value = true;
  try {
    // 这里应该传入当前用户ID，暂时查询所有
    const res = await fetchApplicationPage({ pageNum: currentPage.value, pageSize: pageSize.value, ...searchParams.value });
    data.value = res.data?.records || [];
    total.value = res.data?.total || 0;
  } finally { loading.value = false; }
}

onMounted(() => { loadData(); });

function handleSearch() { currentPage.value = 1; loadData(); }
function handleReset() { searchParams.value = { appType: undefined, status: undefined }; currentPage.value = 1; loadData(); }
function handlePageChange(page: number) { currentPage.value = page; loadData(); }
function handleSizeChange(size: number) { pageSize.value = size; currentPage.value = 1; loadData(); }

const appTypeMap: Record<string, string> = {
  leave: '请假申请', overtime: '加班申请', business: '出差申请', makeup: '补卡申请', exchange: '换休申请',
  regularization: '转正申请', transfer: '调动申请', reward: '奖励申请', punish: '惩罚申请', resignation: '离职申请'
};

const statusMap: Record<number, { label: string; type: string }> = {
  0: { label: '待审批', type: 'warning' }, 1: { label: '已通过', type: 'success' },
  2: { label: '已拒绝', type: 'danger' }, 3: { label: '已撤销', type: 'info' }
};
</script>

<template>
  <div class="min-h-500px flex-col-stretch gap-16px overflow-hidden lt-sm:overflow-auto">
    <ElCard>
      <ElForm inline :model="searchParams">
        <ElFormItem label="申请类型">
          <ElSelect v-model="searchParams.appType" placeholder="请选择类型" clearable>
            <ElOption label="请假申请" value="leave" /><ElOption label="加班申请" value="overtime" />
            <ElOption label="出差申请" value="business" /><ElOption label="补卡申请" value="makeup" />
            <ElOption label="换休申请" value="exchange" /><ElOption label="转正申请" value="regularization" />
            <ElOption label="调动申请" value="transfer" /><ElOption label="奖励申请" value="reward" />
            <ElOption label="惩罚申请" value="punish" /><ElOption label="离职申请" value="resignation" />
          </ElSelect>
        </ElFormItem>
        <ElFormItem label="状态">
          <ElSelect v-model="searchParams.status" placeholder="请选择状态" clearable>
            <ElOption label="待审批" :value="0" /><ElOption label="已通过" :value="1" />
            <ElOption label="已拒绝" :value="2" /><ElOption label="已撤销" :value="3" />
          </ElSelect>
        </ElFormItem>
        <ElFormItem>
          <ElButton type="primary" @click="handleSearch"><template #icon><icon-ep-search /></template>搜索</ElButton>
          <ElButton @click="handleReset"><template #icon><icon-ep-refresh /></template>重置</ElButton>
        </ElFormItem>
      </ElForm>
    </ElCard>

    <ElCard>
      <template #header><span>我的申请</span></template>

      <ElTable v-loading="loading" :data="data" border stripe>
        <ElTableColumn type="index" label="序号" width="60" align="center" />
        <ElTableColumn prop="appType" label="申请类型" width="120">
          <template #default="{ row }"><ElTag>{{ appTypeMap[row.appType] || row.appType }}</ElTag></template>
        </ElTableColumn>
        <ElTableColumn prop="startTime" label="开始时间" width="160" />
        <ElTableColumn prop="endTime" label="结束时间" width="160" />
        <ElTableColumn prop="reason" label="申请原因" min-width="200" show-overflow-tooltip />
        <ElTableColumn prop="status" label="状态" width="100" align="center">
          <template #default="{ row }"><ElTag :type="statusMap[row.status]?.type as any">{{ statusMap[row.status]?.label }}</ElTag></template>
        </ElTableColumn>
        <ElTableColumn prop="approveRemark" label="审批意见" width="150" show-overflow-tooltip />
        <ElTableColumn prop="createdTime" label="申请时间" width="170" />
      </ElTable>

      <div class="mt-16px flex justify-end">
        <ElPagination v-model:current-page="currentPage" v-model:page-size="pageSize" :total="total" :page-sizes="[10, 20, 50, 100]" layout="total, sizes, prev, pager, next, jumper" @current-change="handlePageChange" @size-change="handleSizeChange" />
      </div>
    </ElCard>
  </div>
</template>
