<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import dayjs from 'dayjs';
import { statusMap } from '@/constants/application';
import {
  type Application,
  cancelApplication,
  createApplication,
  fetchApplicationPage
} from '@/service/api/application';
import { useDictOptions } from '@/composables/use-dict-options';
import EmployeePickerDialog from '@/components/common/EmployeePickerDialog.vue';
import ApplicationDetailDrawer from '@/components/business/application-detail-drawer.vue';

defineOptions({ name: 'RegularizationApplication' });

const loading = ref(false);
// 选择员工
const employeeDisplayName = ref('');
const data = ref<Application[]>([]);
const total = ref(0);
const currentPage = ref(1);
const pageSize = ref(10);

const { options: employeeTypeOptions, getDictLabel: getEmployeeTypeLabel } = useDictOptions('employee_type');
const dialogVisible = ref(false);
const submitLoading = ref(false);
const formData = ref<
  Application & {
    employeeName?: string;
    employeeNo?: string;
    companyName?: string;
    deptName?: string;
    entryDateDisplay?: string;
  }
>({
  employeeId: undefined as any,
  appType: 'regularization'
});

// 员工选择弹窗
const employeeDialogVisible = ref(false);

const searchParams = ref({ employeeName: '', employeeNo: '', status: undefined as number | undefined });
async function loadData() {
  loading.value = true;
  try {
    const res = await fetchApplicationPage({
      pageNum: currentPage.value,
      pageSize: pageSize.value,
      appType: 'regularization',
      employeeName: searchParams.value.employeeName || undefined,
      employeeNo: searchParams.value.employeeNo || undefined,
      status: searchParams.value.status
    });
    data.value = res.data?.records || [];
    total.value = res.data?.total || 0;
  } finally {
    loading.value = false;
  }
}
onMounted(() => {
  loadData();
});

function handleAdd() {
  formData.value = {
    employeeId: undefined as any,
    appType: 'regularization',
    regularDate: '',
    probationEndDate: '',
    evaluation: '',
    newEmployeeType: '',
    reason: '',
    employeeName: '',
    employeeNo: '',
    companyName: '',
    deptName: '',
    entryDateDisplay: ''
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
  formData.value.entryDateDisplay = row.entryDate || '';
  employeeDisplayName.value = `${row.name} (${row.employeeNo})`;
  // 自动计算试用期结束日期（入职日期+3个月，本地时区）
  if (row.entryDate) {
    formData.value.probationEndDate = dayjs(row.entryDate).add(3, 'month').format('YYYY-MM-DD');
  }
}

async function handleSubmit() {
  if (!formData.value.employeeId || !formData.value.regularDate || !formData.value.newEmployeeType) {
    ElMessage.warning('请填写必填项');
    return;
  }
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

// 详情抽屉
const detailVisible = ref(false);
const currentApplication = ref<Application | null>(null);

function handleViewDetail(row: Application) {
  currentApplication.value = row;
  detailVisible.value = true;
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
  searchParams.value = { employeeName: '', employeeNo: '', status: undefined };
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
  <div class="list-page">
    <!-- 搜索区域 -->
    <ElCard class="search-card">
      <ElForm inline :model="searchParams">
        <ElFormItem label="员工姓名">
          <ElInput v-model="searchParams.employeeName" placeholder="请输入员工姓名" clearable />
        </ElFormItem>
        <ElFormItem label="员工编号">
          <ElInput v-model="searchParams.employeeNo" placeholder="请输入员工编号" clearable />
        </ElFormItem>
        <ElFormItem label="状态">
          <ElSelect v-model="searchParams.status" placeholder="请选择状态" clearable style="width: 150px">
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

    <!-- 表格区域 -->
    <ElCard class="table-card">
      <template #header>
        <div class="flex items-center justify-between">
          <span>转正申请列表</span>
          <ElButton type="primary" @click="handleAdd">
            <template #icon><icon-ep-plus /></template>
            新增申请
          </ElButton>
        </div>
      </template>

      <div class="table-wrapper">
        <ElTable v-loading="loading" :data="data" border stripe height="100%">
          <ElTableColumn type="index" label="序号" width="60" align="center" />
          <ElTableColumn prop="employeeNo" label="工号" min-width="100" />
          <ElTableColumn prop="employeeName" label="员工姓名" min-width="100" />
          <ElTableColumn prop="companyName" label="所属公司" min-width="120" />
          <ElTableColumn prop="deptName" label="部门" min-width="100" />
          <ElTableColumn prop="regularDate" label="转正日期" min-width="110" />
          <ElTableColumn prop="newEmployeeType" label="转正后类型" min-width="100" align="center">
            <template #default="{ row }">
              {{ getEmployeeTypeLabel(row.newEmployeeType) }}
            </template>
          </ElTableColumn>
          <ElTableColumn prop="status" label="状态" min-width="90" align="center">
            <template #default="{ row }">
              <ElTag :type="statusMap[row.status]?.type as any">
                {{ statusMap[row.status]?.label }}
              </ElTag>
            </template>
          </ElTableColumn>
          <ElTableColumn prop="createdTime" label="申请时间" min-width="160" />
          <ElTableColumn label="操作" width="160" align="center" fixed="right">
            <template #default="{ row }">
              <ElButton type="primary" link size="small" @click="handleViewDetail(row)">
                详情
              </ElButton>
              <ElButton v-if="row.status === 0" type="warning" link size="small" @click="handleCancel(row.id)">
                撤销
              </ElButton>
            </template>
          </ElTableColumn>
        </ElTable>
      </div>

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
    <ElDialog v-model="dialogVisible" title="新增转正申请" width="600px" destroy-on-close>
      <ElForm label-width="100px" :model="formData">
        <ElFormItem label="员工" required>
          <div class="w-full flex gap-8px">
            <ElInput v-model="employeeDisplayName" disabled placeholder="请选择员工" class="flex-1" />
            <ElButton type="primary" @click="employeeDialogVisible = true">选择员工</ElButton>
          </div>
        </ElFormItem>
        <ElFormItem label="转正日期" required>
          <ElDatePicker
            v-model="formData.regularDate"
            type="date"
            placeholder="选择转正日期"
            style="width: 100%"
            value-format="YYYY-MM-DD"
          />
        </ElFormItem>
        <ElFormItem label="转正后类型" required>
          <ElSelect v-model="formData.newEmployeeType" placeholder="请选择员工类型" style="width: 100%">
            <ElOption
              v-for="item in employeeTypeOptions"
              :key="item.dictValue"
              :label="getEmployeeTypeLabel(item.dictValue)"
              :value="item.dictValue"
            />
          </ElSelect>
        </ElFormItem>
        <ElFormItem label="试用期评价">
          <ElInput v-model="formData.evaluation" type="textarea" :rows="3" placeholder="请输入试用期评价" />
        </ElFormItem>
        <ElFormItem label="申请理由">
          <ElInput v-model="formData.reason" type="textarea" :rows="3" placeholder="请输入申请理由" />
        </ElFormItem>
      </ElForm>
      <template #footer>
        <ElButton @click="dialogVisible = false">取消</ElButton>
        <ElButton type="primary" :loading="submitLoading" @click="handleSubmit">提交申请</ElButton>
      </template>
    </ElDialog>

    <EmployeePickerDialog v-model="employeeDialogVisible" @confirm="handleConfirmEmployee" />
    <ApplicationDetailDrawer v-model="detailVisible" :application="currentApplication" />
  </div>
</template>
