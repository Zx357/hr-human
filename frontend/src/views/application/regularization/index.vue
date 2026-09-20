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
import { formatDateTime } from '@/utils/format';
import EmployeePickerDialog from '@/components/common/employee-picker-dialog.vue';
import ApplicationDetailDrawer from '@/components/business/application-detail-drawer.vue';
import { $t } from '@/locales';

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

const searchParams = ref({
  employeeName: '',
  employeeNo: '',
  dateRange: [] as string[],
  status: undefined as number | undefined
});
async function loadData() {
  loading.value = true;
  try {
    const res = await fetchApplicationPage({
      pageNum: currentPage.value,
      pageSize: pageSize.value,
      appType: 'regularization',
      employeeName: searchParams.value.employeeName || undefined,
      employeeNo: searchParams.value.employeeNo || undefined,
      status: searchParams.value.status,
      beginTime: searchParams.value.dateRange?.[0] || undefined,
      endTime: searchParams.value.dateRange?.[1] || undefined
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
    ElMessage.warning($t('common.pleaseFillRequired'));
    return;
  }
  submitLoading.value = true;
  try {
    await createApplication(formData.value);
    ElMessage.success($t('application.common.applicationSubmittedSuccessfully'));
    dialogVisible.value = false;
    loadData();
  } catch {
    // 请求层已统一弹错
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
    await ElMessageBox.confirm($t('application.business.areYouSureYouWantToWithdrawThisApplicationThisCannotBeUndone'), $t('application.common.withdrawalConfirmation'), {
      type: 'warning',
      confirmButtonText: $t('application.common.confirmWithdrawal'),
      cancelButtonText: $t('common.cancel')
    });
  } catch {
    return;
  }
  try {
    await cancelApplication(id);
    ElMessage.success($t('common.withdrawn'));
    loadData();
  } catch {
    // 请求层已统一弹错
  }
}

function handleSearch() {
  currentPage.value = 1;
  loadData();
}
function handleReset() {
  searchParams.value = { dateRange: [], employeeName: '', employeeNo: '', status: undefined };
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
        <ElFormItem :label="$t('common.employeeName')">
          <ElInput
            v-model="searchParams.employeeName"
            :placeholder="$t('common.pleaseInputEmployeeName')"
            clearable
            @keyup.enter="handleSearch"
          />
        </ElFormItem>
        <ElFormItem :label="$t('common.employeeNo')">
          <ElInput
            v-model="searchParams.employeeNo"
            :placeholder="$t('common.pleaseInputEmployeeNo')"
            clearable
            @keyup.enter="handleSearch"
          />
        </ElFormItem>
        <ElFormItem :label="$t('application.common.applicationTime')">
          <ElDatePicker
            v-model="searchParams.dateRange"
            type="daterange"
            :range-separator="$t('common.to')"
            :start-placeholder="$t('common.startDate')"
            :end-placeholder="$t('common.endDate')"
            value-format="YYYY-MM-DD"
            style="width: 240px"
          />
        </ElFormItem>
        <ElFormItem :label="$t('common.status')">
          <ElSelect v-model="searchParams.status" :placeholder="$t('common.pleaseSelectStatus')" clearable style="width: 150px">
            <ElOption :label="$t('common.pendingApproval')" :value="0" />
            <ElOption :label="$t('common.approved')" :value="1" />
            <ElOption :label="$t('common.rejected')" :value="2" />
            <ElOption :label="$t('common.withdrawn')" :value="3" />
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

    <!-- 表格区域 -->
    <ElCard class="table-card">
      <template #header>
        <div class="flex items-center justify-between">
          <span>{{ $t('application.regularization.regularizationApplications') }}</span>
          <ElButton v-permission="'application:regularization:add'" type="primary" @click="handleAdd">
            <template #icon><icon-ep-plus /></template>
            {{ $t('application.common.newApplication') }}
          </ElButton>
        </div>
      </template>

      <div class="table-wrapper">
        <ElTable v-loading="loading" :data="data" border stripe height="100%">
          <ElTableColumn type="index" :label="$t('common.index2')" width="60" align="center" />
          <ElTableColumn prop="employeeNo" :label="$t('common.employeeNo')" min-width="100" />
          <ElTableColumn prop="employeeName" :label="$t('common.employeeName')" min-width="100" />
          <ElTableColumn prop="companyName" :label="$t('application.common.company')" min-width="120" />
          <ElTableColumn prop="deptName" :label="$t('common.department')" min-width="100" />
          <ElTableColumn prop="regularDate" :label="$t('application.regularization.regularizationDate')" min-width="110" />
          <ElTableColumn prop="newEmployeeType" :label="$t('application.regularization.typeAfterRegularization')" min-width="100" align="center">
            <template #default="{ row }">
              {{ getEmployeeTypeLabel(row.newEmployeeType) }}
            </template>
          </ElTableColumn>
          <ElTableColumn prop="status" :label="$t('common.status')" min-width="90" align="center">
            <template #default="{ row }">
              <ElTag :type="statusMap[row.status]?.type as any">
                {{ statusMap[row.status]?.label }}
              </ElTag>
            </template>
          </ElTableColumn>
          <ElTableColumn prop="createdTime" :label="$t('application.common.applicationTime')" min-width="160">
            <template #default="{ row }">{{ formatDateTime(row.createdTime) }}</template>
          </ElTableColumn>
          <ElTableColumn :label="$t('common.action')" width="160" align="center" fixed="right">
            <template #default="{ row }">
              <ElButton type="primary" link size="small" @click="handleViewDetail(row)">{{ $t('common.details') }}</ElButton>
              <ElButton v-if="row.status === 0" type="warning" link size="small" @click="handleCancel(row.id)">
                {{ $t('common.withdraw') }}
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
    <ElDialog v-model="dialogVisible" :title="$t('application.regularization.newRegularizationApplication')" width="600px" destroy-on-close>
      <ElForm label-width="100px" :model="formData">
        <ElFormItem :label="$t('common.employee')" required>
          <div class="w-full flex gap-8px">
            <ElInput v-model="employeeDisplayName" disabled :placeholder="$t('common.pleaseSelectEmployees')" class="flex-1" />
            <ElButton type="primary" @click="employeeDialogVisible = true">{{ $t('common.selectEmployees') }}</ElButton>
          </div>
        </ElFormItem>
        <ElFormItem :label="$t('application.regularization.regularizationDate')" required>
          <ElDatePicker
            v-model="formData.regularDate"
            type="date"
            :placeholder="$t('application.regularization.selectRegularizationDate')"
            style="width: 100%"
            value-format="YYYY-MM-DD"
          />
        </ElFormItem>
        <ElFormItem :label="$t('application.regularization.typeAfterRegularization')" required>
          <ElSelect v-model="formData.newEmployeeType" :placeholder="$t('application.regularization.pleaseSelectEmployeeType')" style="width: 100%">
            <ElOption
              v-for="item in employeeTypeOptions"
              :key="item.dictValue"
              :label="getEmployeeTypeLabel(item.dictValue)"
              :value="item.dictValue"
            />
          </ElSelect>
        </ElFormItem>
        <ElFormItem :label="$t('application.regularization.probationEvaluation')">
          <ElInput v-model="formData.evaluation" type="textarea" :rows="3" :placeholder="$t('application.regularization.pleaseEnterProbationEvaluation')" />
        </ElFormItem>
        <ElFormItem :label="$t('application.common.applicationReason')">
          <ElInput v-model="formData.reason" type="textarea" :rows="3" :placeholder="$t('application.common.pleaseEnterApplicationReason')" />
        </ElFormItem>
      </ElForm>
      <template #footer>
        <ElButton @click="dialogVisible = false">{{ $t('common.cancel') }}</ElButton>
        <ElButton type="primary" :loading="submitLoading" @click="handleSubmit">{{ $t('application.common.submitApplication') }}</ElButton>
      </template>
    </ElDialog>

    <EmployeePickerDialog v-model="employeeDialogVisible" @confirm="handleConfirmEmployee" />
    <ApplicationDetailDrawer v-model="detailVisible" :application="currentApplication" />
  </div>
</template>
