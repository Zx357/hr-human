<script setup lang="ts">
import { onMounted, ref, watch } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { statusMap } from '@/constants/application';
import {
  type Application,
  calculateLeaveHours,
  cancelApplication,
  createApplication,
  fetchApplicationPage
} from '@/service/api/application';
import { useDictOptions } from '@/composables/use-dict-options';
import { formatDateTime } from '@/utils/format';
import EmployeePickerDialog from '@/components/common/employee-picker-dialog.vue';
import ApplicationDetailDrawer from '@/components/business/application-detail-drawer.vue';
import { $t } from '@/locales';

defineOptions({ name: 'LeaveApplication' });

const loading = ref(false);
// 选择员工
const employeeDisplayName = ref('');
const data = ref<Application[]>([]);
const total = ref(0);
const currentPage = ref(1);
const pageSize = ref(10);

const { options: leaveTypeOptions, getDictLabel: getLeaveTypeLabel } = useDictOptions('leave_type');

const dialogVisible = ref(false);
const submitLoading = ref(false);
const formData = ref<
  Application & { employeeName?: string; employeeNo?: string; companyName?: string; deptName?: string }
>({
  employeeId: undefined as any,
  appType: 'leave'
});

// 日期范围
const dateRange = ref<[string, string] | null>(null);

// 请假小时数（自动计算）
const leaveHours = ref<number>(0);
const calculatingHours = ref(false);

// 监听员工和时间变化，自动计算请假小时
watch(
  [() => formData.value.employeeId, dateRange],
  async () => {
    if (formData.value.employeeId && dateRange.value && dateRange.value.length === 2) {
      calculatingHours.value = true;
      try {
        const startTime = `${dateRange.value[0]}:00`;
        const endTime = `${dateRange.value[1]}:00`;
        const res = await calculateLeaveHours(formData.value.employeeId, startTime, endTime);
        leaveHours.value = res.data || 0;
      } catch {
        leaveHours.value = 0;
      } finally {
        calculatingHours.value = false;
      }
    } else {
      leaveHours.value = 0;
    }
  },
  { deep: true }
);

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
      appType: 'leave',
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
    appType: 'leave',
    title: '',
    startTime: '',
    endTime: '',
    duration: 0,
    reason: '',
    employeeName: '',
    employeeNo: '',
    companyName: '',
    deptName: ''
  };
  dateRange.value = null;
  leaveHours.value = 0;
  employeeDisplayName.value = '';
  dialogVisible.value = true;
}

function handleConfirmEmployee(selected: Api.Hr.Employee[]) {
  const row = selected[0];
  if (!row?.id) return;
  formData.value.employeeId = row.id;
  formData.value.employeeName = row.name;
  formData.value.employeeNo = row.employeeNo;
  formData.value.companyName = row.companyName || '';
  formData.value.deptName = row.deptName || '';
  employeeDisplayName.value = `${row.name} (${row.employeeNo})`;
}

async function handleSubmit() {
  if (!formData.value.employeeId || !formData.value.title || !dateRange.value || dateRange.value.length !== 2) {
    ElMessage.warning($t('common.pleaseFillRequired'));
    return;
  }
  if (leaveHours.value <= 0) {
    ElMessage.warning($t('application.leave.invalidLeaveTimePleaseCheckTheEmployeeSchedule'));
    return;
  }
  // 补上秒
  formData.value.startTime = `${dateRange.value[0]}:00`;
  formData.value.endTime = `${dateRange.value[1]}:00`;
  formData.value.duration = leaveHours.value;
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
          <ElSelect v-model="searchParams.status" :placeholder="$t('common.pleaseSelectStatus')" clearable style="width: 120px">
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

    <ElCard class="table-card">
      <template #header>
        <div class="flex items-center justify-between">
          <span>{{ $t('application.leave.leaveApplications') }}</span>
          <ElButton v-permission="'application:leave:add'" type="primary" @click="handleAdd">
            <template #icon><icon-ep-plus /></template>
            {{ $t('application.leave.createLeaveApplication') }}
          </ElButton>
        </div>
      </template>

      <div class="table-wrapper">
        <ElTable v-loading="loading" :data="data" border stripe height="100%">
          <ElTableColumn type="index" :label="$t('common.index2')" width="60" align="center" />
          <ElTableColumn prop="employeeNo" :label="$t('common.employeeNo')" width="100" />
          <ElTableColumn prop="employeeName" :label="$t('application.common.applicant')" width="100" />
          <ElTableColumn prop="companyName" :label="$t('application.common.company')" min-width="120" show-overflow-tooltip />
          <ElTableColumn prop="deptName" :label="$t('common.department')" width="100" />
          <ElTableColumn prop="title" :label="$t('common.leaveType')" width="100">
            <template #default="{ row }">{{ getLeaveTypeLabel(row.title) }}</template>
          </ElTableColumn>
          <ElTableColumn prop="startTime" :label="$t('common.startTime')" width="160" />
          <ElTableColumn prop="endTime" :label="$t('common.endTime')" width="160" />
          <ElTableColumn prop="duration" :label="$t('common.hours')" width="80" align="center" />
          <ElTableColumn prop="reason" :label="$t('application.leave.leaveReason')" min-width="150" show-overflow-tooltip />
          <ElTableColumn prop="status" :label="$t('common.status')" width="90" align="center">
            <template #default="{ row }">
              <ElTag :type="statusMap[row.status]?.type as any">{{ statusMap[row.status]?.label }}</ElTag>
            </template>
          </ElTableColumn>
          <ElTableColumn prop="createdTime" :label="$t('application.common.applicationTime')" width="160">
            <template #default="{ row }">{{ formatDateTime(row.createdTime) }}</template>
          </ElTableColumn>
          <ElTableColumn :label="$t('common.action')" width="140" align="center" fixed="right">
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
    <ElDialog v-model="dialogVisible" :title="$t('application.leave.createLeaveApplication')" width="600px" destroy-on-close>
      <ElForm label-width="100px" :model="formData">
        <ElFormItem :label="$t('application.common.applicant')" required>
          <div class="w-full flex gap-8px">
            <ElInput v-model="employeeDisplayName" disabled :placeholder="$t('common.pleaseSelectEmployees')" class="flex-1" />
            <ElButton type="primary" @click="employeeDialogVisible = true">{{ $t('common.selectEmployees') }}</ElButton>
          </div>
        </ElFormItem>
        <ElFormItem :label="$t('common.leaveType')" required>
          <ElSelect v-model="formData.title" :placeholder="$t('application.leave.pleaseSelectLeaveType')" style="width: 100%">
            <ElOption
              v-for="item in leaveTypeOptions"
              :key="item.dictValue"
              :label="getLeaveTypeLabel(item.dictValue)"
              :value="item.dictValue"
            />
          </ElSelect>
        </ElFormItem>
        <ElFormItem :label="$t('application.leave.leaveTime')" required>
          <ElDatePicker
            v-model="dateRange"
            type="datetimerange"
            :range-separator="$t('common.to')"
            :start-placeholder="$t('common.startTime')"
            :end-placeholder="$t('common.endTime')"
            style="width: 100%"
            value-format="YYYY-MM-DD HH:mm"
            format="YYYY-MM-DD HH:mm"
          />
        </ElFormItem>
        <ElFormItem :label="$t('application.leave.leaveHours')">
          <ElInput v-model="leaveHours" disabled style="width: 100%">
            <template #suffix>
              <span v-if="calculatingHours" class="text-gray-400">{{ $t('common.calculatingDot') }}</span>
              <span v-else>{{ $t('common.hours') }}</span>
            </template>
          </ElInput>
          <div v-if="leaveHours === 0 && formData.employeeId && dateRange" class="mt-4px text-12px text-orange-500">
            {{ $t('application.leave.noteLeaveHoursAre0TheEmployeeMayHaveNoScheduleInThisPeriod') }}
          </div>
        </ElFormItem>
        <ElFormItem :label="$t('application.leave.leaveReason')" required>
          <ElInput v-model="formData.reason" type="textarea" :rows="3" :placeholder="$t('application.leave.pleaseEnterLeaveReason')" />
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
