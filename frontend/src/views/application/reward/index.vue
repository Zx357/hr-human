<script setup lang="ts">
import { onMounted, ref, watch } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { statusMap } from '@/constants/application';
import {
  type Application,
  cancelApplication,
  createApplication,
  fetchApplicationPage
} from '@/service/api/application';
import { useDictOptions } from '@/composables/use-dict-options';
import EmployeePickerDialog from '@/components/common/EmployeePickerDialog.vue';

defineOptions({ name: 'RewardApplication' });

const loading = ref(false);
// 选择员工
const employeeDisplayName = ref('');
const data = ref<Application[]>([]);
const total = ref(0);
const currentPage = ref(1);
const pageSize = ref(10);

const { options: rewardCategoryOptions, getDictLabel: getRewardCategoryLabel } = useDictOptions('reward_category');
const { options: punishCategoryOptions, getDictLabel: getPunishCategoryLabel } = useDictOptions('punish_category');
const dialogVisible = ref(false);

function getCategoryLabel(appType?: string, category?: string) {
  return appType === 'reward' ? getRewardCategoryLabel(category) : getPunishCategoryLabel(category);
}
const submitLoading = ref(false);
const formData = ref<
  Application & { employeeName?: string; employeeNo?: string; companyName?: string; deptName?: string }
>({
  employeeId: undefined as any,
  appType: 'reward',
  rewardType: 1
});

// 员工选择弹窗
const employeeDialogVisible = ref(false);

const searchParams = ref({
  employeeName: '',
  employeeNo: '',
  status: undefined as number | undefined,
  appType: undefined as string | undefined
});
async function loadData() {
  loading.value = true;
  try {
    const appType = searchParams.value.appType;
    let records: Application[] = [];
    let totalCount = 0;

    if (!appType || appType === 'reward') {
      const res1 = await fetchApplicationPage({
        pageNum: currentPage.value,
        pageSize: pageSize.value,
        appType: 'reward',
        employeeName: searchParams.value.employeeName || undefined,
        employeeNo: searchParams.value.employeeNo || undefined,
        status: searchParams.value.status
      });
      records = [...(res1.data?.records || [])];
      totalCount += res1.data?.total || 0;
    }
    if (!appType || appType === 'punish') {
      const res2 = await fetchApplicationPage({
        pageNum: currentPage.value,
        pageSize: pageSize.value,
        appType: 'punish',
        employeeName: searchParams.value.employeeName || undefined,
        employeeNo: searchParams.value.employeeNo || undefined,
        status: searchParams.value.status
      });
      records = [...records, ...(res2.data?.records || [])];
      totalCount += res2.data?.total || 0;
    }

    records.sort((a, b) => new Date(b.createdTime || '').getTime() - new Date(a.createdTime || '').getTime());
    data.value = records;
    total.value = totalCount;
  } finally {
    loading.value = false;
  }
}
onMounted(() => {
  loadData();
});

watch(
  () => formData.value.rewardType,
  () => {
    formData.value.category = '';
  }
);

function handleAdd() {
  formData.value = {
    employeeId: undefined as any,
    appType: 'reward',
    rewardType: 1,
    category: '',
    amount: 0,
    effectDate: '',
    reason: '',
    employeeName: '',
    employeeNo: '',
    companyName: '',
    deptName: ''
  };
  employeeDisplayName.value = '';
  dialogVisible.value = true;
}
function handleConfirmEmployee(selected: Api.Hr.Employee[]) {
  const row = selected[0];
  if (!row?.id) return;

  formData.value.employeeId = row.id!;
  formData.value.employeeName = row.name;
  formData.value.employeeNo = row.employeeNo;
  formData.value.companyName = row.companyName || '';
  formData.value.deptName = row.deptName || '';
  employeeDisplayName.value = `${row.name} (${row.employeeNo})`;
}

async function handleSubmit() {
  if (!formData.value.employeeId || !formData.value.category || !formData.value.effectDate) {
    ElMessage.warning('请填写必填项');
    return;
  }
  formData.value.appType = formData.value.rewardType === 1 ? 'reward' : 'punish';
  submitLoading.value = true;
  try {
    await createApplication(formData.value);
    ElMessage.success('申请提交成功');
    dialogVisible.value = false;
    loadData();
  } catch {
    ElMessage.error('提交失败');
  } finally {
    submitLoading.value = false;
  }
}

async function handleCancel(id: number) {
  try {
    await ElMessageBox.confirm('确定撤销该申请吗？撤销后不可恢复', '撤销确认', {
      type: 'warning',
      confirmButtonText: '确认撤销',
      cancelButtonText: '取消'
    });
  } catch {
    return;
  }
  try {
    await cancelApplication(id);
    ElMessage.success('已撤销');
    loadData();
  } catch {
    ElMessage.error('撤销失败');
  }
}

function handleSearch() {
  currentPage.value = 1;
  loadData();
}
function handleReset() {
  searchParams.value = { employeeName: '', employeeNo: '', status: undefined, appType: undefined };
  currentPage.value = 1;
  loadData();
}
function handlePageChange(page: number) {
  currentPage.value = page;
  loadData();
}
function handleSizeChange(size: number) {
  pageSize.value = size;
  currentPage.value = 1;
  loadData();
}
</script>

<template>
  <div class="min-h-500px flex-col-stretch gap-16px overflow-hidden lt-sm:overflow-auto">
    <ElCard>
      <ElForm inline :model="searchParams">
        <ElFormItem label="员工姓名">
          <ElInput v-model="searchParams.employeeName" placeholder="请输入员工姓名" clearable />
        </ElFormItem>
        <ElFormItem label="员工编号">
          <ElInput v-model="searchParams.employeeNo" placeholder="请输入员工编号" clearable />
        </ElFormItem>
        <ElFormItem label="类型">
          <ElSelect v-model="searchParams.appType" placeholder="请选择类型" clearable style="width: 120px">
            <ElOption label="奖励" value="reward" />
            <ElOption label="惩罚" value="punish" />
          </ElSelect>
        </ElFormItem>
        <ElFormItem label="状态">
          <ElSelect v-model="searchParams.status" placeholder="请选择状态" clearable style="width: 120px">
            <ElOption label="待审批" :value="0" />
            <ElOption label="已通过" :value="1" />
            <ElOption label="已拒绝" :value="2" />
            <ElOption label="已撤销" :value="3" />
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

    <ElCard class="flex-1">
      <template #header>
        <div class="flex items-center justify-between">
          <span>奖惩申请列表</span>
          <ElButton type="primary" @click="handleAdd">
            <template #icon><icon-ep-plus /></template>
            发起奖惩申请
          </ElButton>
        </div>
      </template>

      <ElTable v-loading="loading" :data="data" border stripe>
        <ElTableColumn type="index" label="序号" width="60" align="center" />
        <ElTableColumn prop="employeeNo" label="工号" min-width="100" />
        <ElTableColumn prop="employeeName" label="员工姓名" min-width="100" />
        <ElTableColumn prop="companyName" label="所属公司" min-width="120" show-overflow-tooltip />
        <ElTableColumn prop="deptName" label="部门" min-width="100" />
        <ElTableColumn prop="appType" label="类型" min-width="80" align="center">
          <template #default="{ row }">
            <ElTag :type="row.appType === 'reward' ? 'success' : 'danger'">
              {{ row.appType === 'reward' ? '奖励' : '惩罚' }}
            </ElTag>
          </template>
        </ElTableColumn>
        <ElTableColumn prop="category" label="类别" min-width="100">
          <template #default="{ row }">
            {{ getCategoryLabel(row.appType, row.category) }}
          </template>
        </ElTableColumn>
        <ElTableColumn prop="amount" label="金额" min-width="100" align="right">
          <template #default="{ row }">{{ row.amount ? `¥${row.amount}` : '-' }}</template>
        </ElTableColumn>
        <ElTableColumn prop="effectDate" label="生效日期" min-width="110" />
        <ElTableColumn prop="reason" label="原因" min-width="150" show-overflow-tooltip />
        <ElTableColumn prop="status" label="状态" min-width="90" align="center">
          <template #default="{ row }">
            <ElTag :type="statusMap[row.status]?.type as any">{{ statusMap[row.status]?.label }}</ElTag>
          </template>
        </ElTableColumn>
        <ElTableColumn prop="createdTime" label="申请时间" min-width="160" />
        <ElTableColumn label="操作" width="100" align="center" fixed="right">
          <template #default="{ row }">
            <ElButton v-if="row.status === 0" type="warning" link size="small" @click="handleCancel(row.id)">
              撤销
            </ElButton>
          </template>
        </ElTableColumn>
      </ElTable>

      <div class="mt-16px flex justify-end">
        <ElPagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @current-change="handlePageChange"
          @size-change="handleSizeChange"
        />
      </div>
    </ElCard>

    <!-- 新增申请弹窗 -->
    <ElDialog v-model="dialogVisible" title="发起奖惩申请" width="600px" destroy-on-close>
      <ElForm label-width="100px" :model="formData">
        <ElFormItem label="员工" required>
          <div class="w-full flex gap-8px">
            <ElInput v-model="employeeDisplayName" disabled placeholder="请选择员工" class="flex-1" />
            <ElButton type="primary" @click="employeeDialogVisible = true">选择员工</ElButton>
          </div>
        </ElFormItem>
        <ElFormItem label="类型" required>
          <ElRadioGroup v-model="formData.rewardType">
            <ElRadio :value="1">奖励</ElRadio>
            <ElRadio :value="2">惩罚</ElRadio>
          </ElRadioGroup>
        </ElFormItem>
        <ElFormItem label="类别" required>
          <ElSelect v-model="formData.category" placeholder="请选择类别" style="width: 100%">
            <ElOption
              v-for="item in formData.rewardType === 1 ? rewardCategoryOptions : punishCategoryOptions"
              :key="item.dictValue"
              :label="getPunishCategoryLabel(item.dictValue)"
              :value="item.dictValue"
            />
          </ElSelect>
        </ElFormItem>
        <ElFormItem label="金额">
          <ElInputNumber v-model="formData.amount" :min="0" :precision="2" style="width: 100%" />
        </ElFormItem>
        <ElFormItem label="生效日期" required>
          <ElDatePicker
            v-model="formData.effectDate"
            type="date"
            placeholder="选择日期"
            style="width: 100%"
            value-format="YYYY-MM-DD"
          />
        </ElFormItem>
        <ElFormItem label="原因">
          <ElInput v-model="formData.reason" type="textarea" :rows="3" placeholder="请输入原因" />
        </ElFormItem>
      </ElForm>
      <template #footer>
        <ElButton @click="dialogVisible = false">取消</ElButton>
        <ElButton type="primary" :loading="submitLoading" @click="handleSubmit">提交申请</ElButton>
      </template>
    </ElDialog>

    <EmployeePickerDialog v-model="employeeDialogVisible" @confirm="handleConfirmEmployee" />
  </div>
</template>
