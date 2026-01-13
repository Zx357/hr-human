<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { ElMessage } from 'element-plus';
import { fetchDictDataByCode } from '@/service/api/system';
import { fetchPendingPage, approveApplication, type Application } from '@/service/api/application';

defineOptions({ name: 'ApprovalPending' });

const loading = ref(false);
const data = ref<Application[]>([]);
const total = ref(0);
const currentPage = ref(1);
const pageSize = ref(10);

const detailVisible = ref(false);
const currentRecord = ref<Application | null>(null);
const approvalComment = ref('');
const submitLoading = ref(false);

const searchParams = ref({ employeeName: '', appType: undefined as string | undefined });

// 字典数据
const leaveTypeOptions = ref<Api.System.DictData[]>([]);
const transferTypeOptions = ref<Api.System.DictData[]>([]);
const positionOptions = ref<Api.System.DictData[]>([]);
const resignTypeOptions = ref<Api.System.DictData[]>([]);
const rewardCategoryOptions = ref<Api.System.DictData[]>([]);
const punishCategoryOptions = ref<Api.System.DictData[]>([]);
const employeeTypeOptions = ref<Api.System.DictData[]>([]);

async function loadDictData() {
  const [leaveRes, transferRes, positionRes, resignRes, rewardRes, punishRes, empTypeRes] = await Promise.all([
    fetchDictDataByCode('leave_type'),
    fetchDictDataByCode('transfer_type'),
    fetchDictDataByCode('position'),
    fetchDictDataByCode('resign_type'),
    fetchDictDataByCode('reward_category'),
    fetchDictDataByCode('punish_category'),
    fetchDictDataByCode('employee_type')
  ]);
  leaveTypeOptions.value = leaveRes.data || [];
  transferTypeOptions.value = transferRes.data || [];
  positionOptions.value = positionRes.data || [];
  resignTypeOptions.value = resignRes.data || [];
  rewardCategoryOptions.value = rewardRes.data || [];
  punishCategoryOptions.value = punishRes.data || [];
  employeeTypeOptions.value = empTypeRes.data || [];
}

async function loadData() {
  loading.value = true;
  try {
    const res = await fetchPendingPage({ pageNum: currentPage.value, pageSize: pageSize.value, ...searchParams.value });
    data.value = res.data?.records || [];
    total.value = res.data?.total || 0;
  } finally { loading.value = false; }
}

onMounted(() => { loadDictData(); loadData(); });

function handleView(row: Application) {
  currentRecord.value = row;
  approvalComment.value = '';
  detailVisible.value = true;
}

async function handleApprove() {
  if (!currentRecord.value) return;
  submitLoading.value = true;
  try {
    await approveApplication(currentRecord.value.id!, 1, approvalComment.value);
    ElMessage.success('审批通过');
    detailVisible.value = false;
    loadData();
  } catch { ElMessage.error('操作失败'); }
  finally { submitLoading.value = false; }
}

async function handleReject() {
  if (!approvalComment.value) {
    ElMessage.warning('请填写拒绝原因');
    return;
  }
  if (!currentRecord.value) return;
  submitLoading.value = true;
  try {
    await approveApplication(currentRecord.value.id!, 2, approvalComment.value);
    ElMessage.success('已拒绝');
    detailVisible.value = false;
    loadData();
  } catch { ElMessage.error('操作失败'); }
  finally { submitLoading.value = false; }
}

function handleSearch() { currentPage.value = 1; loadData(); }
function handleReset() { searchParams.value = { employeeName: '', appType: undefined }; currentPage.value = 1; loadData(); }
function handlePageChange(page: number) { currentPage.value = page; loadData(); }
function handleSizeChange(size: number) { pageSize.value = size; currentPage.value = 1; loadData(); }

function getDictLabel(options: Api.System.DictData[], value?: string) {
  if (!value) return '';
  return options.find(o => o.dictValue === value)?.dictLabel || value;
}

const appTypeMap: Record<string, string> = {
  leave: '请假申请',
  overtime: '加班申请',
  business: '出差申请',
  makeup: '补卡申请',
  exchange: '换休申请',
  regularization: '转正申请',
  transfer: '调动申请',
  reward: '奖励申请',
  punish: '惩罚申请',
  resignation: '离职申请'
};
</script>

<template>
  <div class="list-page">
    <ElCard class="search-card">
      <ElForm inline :model="searchParams">
        <ElFormItem label="申请人"><ElInput v-model="searchParams.employeeName" placeholder="请输入申请人" clearable /></ElFormItem>
        <ElFormItem label="申请类型">
          <ElSelect v-model="searchParams.appType" placeholder="请选择类型" clearable style="width: 150px">
            <ElOption label="请假申请" value="leave" /><ElOption label="加班申请" value="overtime" />
            <ElOption label="出差申请" value="business" /><ElOption label="补卡申请" value="makeup" />
            <ElOption label="换休申请" value="exchange" /><ElOption label="转正申请" value="regularization" />
            <ElOption label="调动申请" value="transfer" /><ElOption label="奖励申请" value="reward" />
            <ElOption label="惩罚申请" value="punish" /><ElOption label="离职申请" value="resignation" />
          </ElSelect>
        </ElFormItem>
        <ElFormItem>
          <ElButton type="primary" @click="handleSearch"><template #icon><icon-ep-search /></template>搜索</ElButton>
          <ElButton @click="handleReset"><template #icon><icon-ep-refresh /></template>重置</ElButton>
        </ElFormItem>
      </ElForm>
    </ElCard>

    <ElCard class="table-card">
      <template #header>
        <div class="flex items-center justify-between">
          <span>待审批列表</span>
          <ElTag type="danger">{{ total }} 条待处理</ElTag>
        </div>
      </template>

      <div class="table-wrapper">
      <ElTable v-loading="loading" :data="data" border stripe height="100%">
        <ElTableColumn type="index" label="序号" width="60" align="center" />
        <ElTableColumn prop="appType" label="申请类型" width="120">
          <template #default="{ row }"><ElTag>{{ appTypeMap[row.appType] || row.appType }}</ElTag></template>
        </ElTableColumn>
        <ElTableColumn prop="employeeName" label="申请人" width="100" />
        <ElTableColumn prop="companyName" label="公司" width="120" show-overflow-tooltip />
        <ElTableColumn prop="deptName" label="部门" width="120" />
        <ElTableColumn prop="reason" label="申请原因/说明" min-width="200" show-overflow-tooltip>
          <template #default="{ row }">
            <template v-if="row.appType === 'leave'">{{ getDictLabel(leaveTypeOptions, row.title) }} {{ row.duration }}天</template>
            <template v-else-if="row.appType === 'regularization'">转正日期: {{ row.regularDate }}</template>
            <template v-else-if="row.appType === 'transfer'">{{ getDictLabel(transferTypeOptions, row.transferType) }}: {{ row.fromDeptName }} → {{ row.toDeptName }}</template>
            <template v-else-if="row.appType === 'reward' || row.appType === 'punish'">{{ getDictLabel(row.appType === 'reward' ? rewardCategoryOptions : punishCategoryOptions, row.category) }} {{ row.amount ? `¥${row.amount}` : '' }}</template>
            <template v-else-if="row.appType === 'resignation'">{{ getDictLabel(resignTypeOptions, row.resignType) }} 最后工作日: {{ row.lastWorkDate }}</template>
            <template v-else>{{ row.reason }}</template>
          </template>
        </ElTableColumn>
        <ElTableColumn prop="createdTime" label="申请时间" width="170" />
        <ElTableColumn label="操作" width="100" align="center" fixed="right">
          <template #default="{ row }">
            <ElButton type="success" size="small" @click="handleView(row)">审批</ElButton>
          </template>
        </ElTableColumn>
      </ElTable>
      </div>

      <div class="mt-16px flex justify-end">
        <ElPagination v-model:current-page="currentPage" v-model:page-size="pageSize" :total="total" :page-sizes="[10, 20, 50, 100]" layout="total, sizes, prev, pager, next, jumper" @current-change="handlePageChange" @size-change="handleSizeChange" />
      </div>
    </ElCard>

    <ElDialog v-model="detailVisible" title="审批详情" width="700px">
      <template v-if="currentRecord">
        <ElDescriptions :column="2" border>
          <ElDescriptionsItem label="申请类型">{{ appTypeMap[currentRecord.appType] || currentRecord.appType }}</ElDescriptionsItem>
          <ElDescriptionsItem label="申请人">{{ currentRecord.employeeName }}</ElDescriptionsItem>
          <ElDescriptionsItem label="公司">{{ currentRecord.companyName }}</ElDescriptionsItem>
          <ElDescriptionsItem label="部门">{{ currentRecord.deptName }}</ElDescriptionsItem>
          <ElDescriptionsItem label="申请时间" :span="2">{{ currentRecord.createdTime }}</ElDescriptionsItem>
          
          <!-- 请假/加班/出差/补卡/换休 -->
          <template v-if="['leave', 'overtime', 'business', 'makeup', 'exchange'].includes(currentRecord.appType)">
            <ElDescriptionsItem label="开始时间">{{ currentRecord.startTime }}</ElDescriptionsItem>
            <ElDescriptionsItem label="结束时间">{{ currentRecord.endTime }}</ElDescriptionsItem>
            <ElDescriptionsItem v-if="currentRecord.appType === 'leave'" label="请假类型">{{ getDictLabel(leaveTypeOptions, currentRecord.title) }}</ElDescriptionsItem>
            <ElDescriptionsItem v-if="currentRecord.duration" label="时长">{{ currentRecord.duration }} 天</ElDescriptionsItem>
          </template>
          
          <!-- 转正申请 -->
          <template v-if="currentRecord.appType === 'regularization'">
            <ElDescriptionsItem label="入职日期">{{ currentRecord.entryDate }}</ElDescriptionsItem>
            <ElDescriptionsItem label="试用期结束">{{ currentRecord.probationEndDate }}</ElDescriptionsItem>
            <ElDescriptionsItem label="转正日期">{{ currentRecord.regularDate }}</ElDescriptionsItem>
            <ElDescriptionsItem label="转正后类别">{{ getDictLabel(employeeTypeOptions, currentRecord.newEmployeeType) }}</ElDescriptionsItem>
            <ElDescriptionsItem label="试用期评价" :span="2">{{ currentRecord.evaluation }}</ElDescriptionsItem>
          </template>
          
          <!-- 调动申请 -->
          <template v-if="currentRecord.appType === 'transfer'">
            <ElDescriptionsItem label="变更类型" :span="2">{{ getDictLabel(transferTypeOptions, currentRecord.transferType) }}</ElDescriptionsItem>
            <ElDescriptionsItem label="原公司">{{ currentRecord.fromCompanyName }}</ElDescriptionsItem>
            <ElDescriptionsItem label="新公司">{{ currentRecord.toCompanyName }}</ElDescriptionsItem>
            <ElDescriptionsItem label="原部门">{{ currentRecord.fromDeptName }}</ElDescriptionsItem>
            <ElDescriptionsItem label="新部门">{{ currentRecord.toDeptName }}</ElDescriptionsItem>
            <ElDescriptionsItem label="原职位">{{ getDictLabel(positionOptions, currentRecord.fromPosition) }}</ElDescriptionsItem>
            <ElDescriptionsItem label="新职位">{{ getDictLabel(positionOptions, currentRecord.toPosition) }}</ElDescriptionsItem>
            <ElDescriptionsItem label="生效日期" :span="2">{{ currentRecord.effectDate }}</ElDescriptionsItem>
          </template>
          
          <!-- 奖惩申请 -->
          <template v-if="currentRecord.appType === 'reward' || currentRecord.appType === 'punish'">
            <ElDescriptionsItem label="类型">{{ currentRecord.appType === 'reward' ? '奖励' : '惩罚' }}</ElDescriptionsItem>
            <ElDescriptionsItem label="类别">{{ getDictLabel(currentRecord.appType === 'reward' ? rewardCategoryOptions : punishCategoryOptions, currentRecord.category) }}</ElDescriptionsItem>
            <ElDescriptionsItem label="金额">{{ currentRecord.amount ? `¥${currentRecord.amount}` : '-' }}</ElDescriptionsItem>
            <ElDescriptionsItem label="生效日期">{{ currentRecord.effectDate }}</ElDescriptionsItem>
          </template>
          
          <!-- 离职申请 -->
          <template v-if="currentRecord.appType === 'resignation'">
            <ElDescriptionsItem label="入职日期">{{ currentRecord.entryDate }}</ElDescriptionsItem>
            <ElDescriptionsItem label="离职类型">{{ getDictLabel(resignTypeOptions, currentRecord.resignType) }}</ElDescriptionsItem>
            <ElDescriptionsItem label="最后工作日">{{ currentRecord.lastWorkDate }}</ElDescriptionsItem>
            <ElDescriptionsItem label="工作交接人">{{ currentRecord.handoverToName }}</ElDescriptionsItem>
          </template>
          
          <ElDescriptionsItem label="申请原因/备注" :span="2">{{ currentRecord.reason }}</ElDescriptionsItem>
        </ElDescriptions>
        <div class="mt-20px">
          <div class="mb-10px font-bold">审批意见</div>
          <ElInput v-model="approvalComment" type="textarea" :rows="3" placeholder="请输入审批意见（拒绝时必填）" />
        </div>
      </template>
      <template #footer>
        <ElButton @click="detailVisible = false">取消</ElButton>
        <ElButton type="danger" :loading="submitLoading" @click="handleReject">拒绝</ElButton>
        <ElButton type="success" :loading="submitLoading" @click="handleApprove">通过</ElButton>
      </template>
    </ElDialog>
  </div>
</template>
