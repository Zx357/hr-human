<script setup lang="ts">
import { onMounted, ref, watch } from 'vue';
import { ElMessage } from 'element-plus';
import type { FormInstance, FormRules } from 'element-plus';
import { statusLabel, statusTagType } from '@/constants/application';
import {
  type Application,
  calculateOvertimeHours,
  cancelApplication,
  createApplication,
  fetchApplicationPage
} from '@/service/api/application';
import { canCancelApplication, confirmCancelApplication } from '@/composables/use-application-cancel';
import { formatDateTime } from '@/utils/format';
import EmployeePickerDialog from '@/components/common/employee-picker-dialog.vue';
import ApplicationDetailDrawer from '@/components/business/application-detail-drawer.vue';
import { $t } from '@/locales';

defineOptions({ name: 'OvertimeApplication' });

const loading = ref(false);
const data = ref<Application[]>([]);
const total = ref(0);
const currentPage = ref(1);
const pageSize = ref(10);

const dialogVisible = ref(false);
const submitLoading = ref(false);
const formRef = ref<FormInstance>();
const formData = ref<Partial<Application>>({
  appType: 'overtime'
});

// 弹窗表单校验规则（申请人/加班时间为独立选择数据，提交前单独校验）
const formRules: FormRules = {
  reason: [{ required: true, message: $t('application.overtime.pleaseEnterOvertimeReason'), trigger: 'blur' }]
};

// 日期范围
const dateRange = ref<[string, string] | null>(null);

// 加班小时数（自动计算）
const overtimeHours = ref<number>(0);
const calculatingHours = ref(false);

// 多员工选择相关
const selectedEmployees = ref<
  Array<{ id: number; name: string; employeeNo: string; companyName?: string; deptName?: string }>
>([]);

// 监听员工和时间变化，自动计算加班小时（取第一个员工计算）
watch(
  [selectedEmployees, dateRange],
  async () => {
    if (selectedEmployees.value.length > 0 && dateRange.value && dateRange.value.length === 2) {
      calculatingHours.value = true;
      try {
        const startTime = `${dateRange.value[0]}:00`;
        const endTime = `${dateRange.value[1]}:00`;
        const res = await calculateOvertimeHours(selectedEmployees.value[0].id, startTime, endTime);
        overtimeHours.value = res.data || 0;
      } catch {
        overtimeHours.value = 0;
      } finally {
        calculatingHours.value = false;
      }
    } else {
      overtimeHours.value = 0;
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
      appType: 'overtime',
      employeeName: searchParams.value.employeeName || undefined,
      employeeNo: searchParams.value.employeeNo || undefined,
      status: searchParams.value.status,
      beginTime: searchParams.value.dateRange?.[0] || undefined,
      endTime: searchParams.value.dateRange?.[1] || undefined
    });
    data.value = res.data?.records || [];
    total.value = res.data?.total || 0;
  } catch {
    // 错误已由请求层统一提示
  } finally {
    loading.value = false;
  }
}
onMounted(() => {
  loadData();
});

function handleAdd() {
  formData.value = { appType: 'overtime', title: '', startTime: '', endTime: '', duration: 0, reason: '' };
  selectedEmployees.value = [];
  dateRange.value = null;
  overtimeHours.value = 0;
  dialogVisible.value = true;
}
function removeSelectedEmployee(index: number) {
  selectedEmployees.value.splice(index, 1);
}

function handleConfirmEmployees(selected: Api.Hr.Employee[]) {
  selectedEmployees.value = selected.map(row => ({
    id: row.id!,
    name: row.name!,
    employeeNo: row.employeeNo!,
    companyName: row.companyName || '',
    deptName: row.deptName || ''
  }));
}

async function handleSubmit() {
  if (selectedEmployees.value.length === 0) {
    ElMessage.warning($t('common.pleaseSelectEmployees'));
    return;
  }
  if (!dateRange.value || dateRange.value.length !== 2) {
    ElMessage.warning($t('common.pleaseSelectTimeRange'));
    return;
  }
  const valid = await formRef.value?.validate().catch(() => false);
  if (!valid) return;
  if (overtimeHours.value <= 0) {
    ElMessage.warning($t('application.overtime.invalidOvertimeTimePleaseCheckTheTimeRange'));
    return;
  }
  submitLoading.value = true;
  try {
    const startTime = `${dateRange.value[0]}:00`;
    const endTime = `${dateRange.value[1]}:00`;
    for (const emp of selectedEmployees.value) {
      // eslint-disable-next-line no-await-in-loop
      await createApplication({
        ...formData.value,
        employeeId: emp.id,
        employeeName: emp.name,
        employeeNo: emp.employeeNo,
        companyName: emp.companyName,
        deptName: emp.deptName,
        startTime,
        endTime,
        duration: overtimeHours.value
      } as Application);
    }
    ElMessage.success(
      $t('application.overtime.successfullySubmittedOvertimeApplications', { count: selectedEmployees.value.length })
    );
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

async function handleCancel(row: Application) {
  if (!(await confirmCancelApplication(row))) return;
  try {
    await cancelApplication(row.id!);
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
          <ElSelect
            v-model="searchParams.status"
            :placeholder="$t('common.pleaseSelectStatus')"
            clearable
            style="width: 120px"
          >
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
          <span>{{ $t('application.overtime.overtimeApplications') }}</span>
          <ElButton v-permission="'application:overtime:add'" type="primary" @click="handleAdd">
            <template #icon><icon-ep-plus /></template>
            {{ $t('application.overtime.createOvertimeApplication') }}
          </ElButton>
        </div>
      </template>
      <div class="table-wrapper">
        <ElTable v-loading="loading" :data="data" border stripe height="100%">
          <ElTableColumn type="index" :label="$t('common.index2')" width="60" align="center" />
          <ElTableColumn prop="employeeNo" :label="$t('common.employeeNo')" width="100" />
          <ElTableColumn prop="employeeName" :label="$t('application.common.applicant')" width="100" />
          <ElTableColumn
            prop="companyName"
            :label="$t('application.common.company')"
            min-width="120"
            show-overflow-tooltip
          />
          <ElTableColumn prop="deptName" :label="$t('common.department')" width="100" />
          <ElTableColumn prop="startTime" :label="$t('common.startTime')" width="160" />
          <ElTableColumn prop="endTime" :label="$t('common.endTime')" width="160" />
          <ElTableColumn prop="duration" :label="$t('application.overtime.durationHours')" width="100" align="center" />
          <ElTableColumn
            prop="reason"
            :label="$t('application.overtime.overtimeReason')"
            min-width="150"
            show-overflow-tooltip
          />
          <ElTableColumn prop="status" :label="$t('common.status')" width="90" align="center">
            <template #default="{ row }">
              <ElTag :type="statusTagType(row.status)">{{ statusLabel(row.status) }}</ElTag>
            </template>
          </ElTableColumn>
          <ElTableColumn prop="createdTime" :label="$t('application.common.applicationTime')" width="160">
            <template #default="{ row }">{{ formatDateTime(row.createdTime) }}</template>
          </ElTableColumn>
          <ElTableColumn :label="$t('common.action')" width="140" align="center" fixed="right">
            <template #default="{ row }">
              <ElButton type="primary" link size="small" @click="handleViewDetail(row)">
                {{ $t('common.details') }}
              </ElButton>
              <ElButton v-if="canCancelApplication(row)" type="warning" link size="small" @click="handleCancel(row)">
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

    <ElDialog
      v-model="dialogVisible"
      :title="$t('application.overtime.createOvertimeApplication')"
      width="600px"
      :close-on-click-modal="false"
      :close-on-press-escape="false"
      destroy-on-close
    >
      <ElForm ref="formRef" label-width="100px" :model="formData" :rules="formRules">
        <ElFormItem :label="$t('application.common.applicant')" required>
          <div class="w-full flex gap-8px">
            <div
              class="min-h-32px flex flex-1 flex-wrap cursor-pointer items-center gap-4px border border-gray-300 rounded-4px px-8px py-4px hover:border-blue-500"
              @click="employeeDialogVisible = true"
            >
              <template v-if="selectedEmployees.length > 0">
                <ElTag
                  v-for="(emp, index) in selectedEmployees"
                  :key="emp.id"
                  closable
                  size="small"
                  @close.stop="removeSelectedEmployee(index)"
                >
                  {{ emp.name }} ({{ emp.employeeNo }})
                </ElTag>
              </template>
              <span v-else class="text-gray-400">{{ $t('common.pleaseSelectEmployees') }}</span>
            </div>
            <ElButton type="primary" @click="employeeDialogVisible = true">{{ $t('common.selectEmployees') }}</ElButton>
          </div>
        </ElFormItem>
        <ElFormItem :label="$t('application.overtime.overtimeTime')" required>
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
        <ElFormItem :label="$t('application.overtime.overtimeHours')">
          <ElInput v-model="overtimeHours" disabled style="width: 100%">
            <template #suffix>
              <span v-if="calculatingHours" class="text-gray-400">{{ $t('common.calculatingDot') }}</span>
              <span v-else>{{ $t('common.hours') }}</span>
            </template>
          </ElInput>
          <div
            v-if="overtimeHours === 0 && selectedEmployees.length > 0 && dateRange"
            class="mt-4px text-12px text-orange-500"
          >
            {{ $t('application.overtime.noteOvertimeHoursAre0TheTimeMayFallWithinRegularShiftHours') }}
          </div>
        </ElFormItem>
        <ElFormItem :label="$t('application.overtime.overtimeReason')" prop="reason" required>
          <ElInput
            v-model="formData.reason"
            type="textarea"
            :rows="3"
            :placeholder="$t('application.overtime.pleaseEnterOvertimeReason')"
          />
        </ElFormItem>
      </ElForm>
      <template #footer>
        <ElButton @click="dialogVisible = false">{{ $t('common.cancel') }}</ElButton>
        <ElButton type="primary" :loading="submitLoading" @click="handleSubmit">
          {{ $t('application.common.submitApplication') }}
        </ElButton>
      </template>
    </ElDialog>

    <EmployeePickerDialog v-model="employeeDialogVisible" multiple @confirm="handleConfirmEmployees" />
    <ApplicationDetailDrawer v-model="detailVisible" :application="currentApplication" />
  </div>
</template>
