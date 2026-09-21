<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { ElMessage } from 'element-plus';
import type { FormInstance, FormRules } from 'element-plus';
import { statusLabel, statusTagType } from '@/constants/application';
import {
  type Application,
  cancelApplication,
  createApplication,
  fetchApplicationPage
} from '@/service/api/application';
import { canCancelApplication, confirmCancelApplication } from '@/composables/use-application-cancel';
import { formatDateTime } from '@/utils/format';
import EmployeePickerDialog from '@/components/common/employee-picker-dialog.vue';
import ApplicationDetailDrawer from '@/components/business/application-detail-drawer.vue';
import { $t } from '@/locales';

defineOptions({ name: 'MakeupApplication' });

const loading = ref(false);
// 选择员工
const employeeDisplayName = ref('');
const data = ref<Application[]>([]);
const total = ref(0);
const currentPage = ref(1);
const pageSize = ref(10);

const dialogVisible = ref(false);
const submitLoading = ref(false);
const formRef = ref<FormInstance>();
const formData = ref<
  Application & { employeeName?: string; employeeNo?: string; companyName?: string; deptName?: string }
>({
  employeeId: undefined,
  appType: 'makeup'
});

// 弹窗表单校验规则
const formRules: FormRules = {
  employeeId: [{ required: true, message: $t('common.pleaseSelectEmployees'), trigger: 'change' }],
  title: [{ required: true, message: $t('application.makeup.pleaseSelectMakeupClockType'), trigger: 'change' }],
  startTime: [{ required: true, message: $t('application.makeup.pleaseSelectMakeupClockTime'), trigger: 'change' }],
  reason: [{ required: true, message: $t('application.makeup.pleaseEnterMakeupClockReason'), trigger: 'blur' }]
};

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
      appType: 'makeup',
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
  formData.value = {
    employeeId: undefined,
    appType: 'makeup',
    title: '',
    startTime: '',
    endTime: '',
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

const makeupTypeMap: Record<string, string> = {
  checkin: $t('application.makeup.clockInMakeup'),
  checkout: $t('application.makeup.clockOutMakeup')
};

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false);
  if (!valid) return;
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

async function handleCancel(row: Application) {
  if (!(await confirmCancelApplication(row, makeupTypeMap[row.title || ''] || row.title))) return;
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
          <span>{{ $t('application.makeup.makeupClockApplications') }}</span>
          <ElButton v-permission="'application:makeup:add'" type="primary" @click="handleAdd">
            <template #icon><icon-ep-plus /></template>
            {{ $t('application.makeup.createMakeupClockApplication') }}
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
          <ElTableColumn prop="title" :label="$t('application.makeup.makeupClockType')" width="100">
            <template #default="{ row }">{{ makeupTypeMap[row.title] || row.title }}</template>
          </ElTableColumn>
          <ElTableColumn prop="startTime" :label="$t('application.makeup.makeupClockTime')" width="160" />
          <ElTableColumn
            prop="reason"
            :label="$t('application.makeup.makeupClockReason')"
            min-width="200"
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

    <!-- 新增申请弹窗 -->
    <ElDialog
      v-model="dialogVisible"
      :title="$t('application.makeup.createMakeupClockApplication')"
      width="600px"
      :close-on-click-modal="false"
      :close-on-press-escape="false"
      destroy-on-close
    >
      <ElForm ref="formRef" label-width="100px" :model="formData" :rules="formRules">
        <ElFormItem :label="$t('application.common.applicant')" prop="employeeId" required>
          <div class="w-full flex gap-8px">
            <ElInput
              v-model="employeeDisplayName"
              disabled
              :placeholder="$t('common.pleaseSelectEmployees')"
              class="flex-1"
            />
            <ElButton type="primary" @click="employeeDialogVisible = true">{{ $t('common.selectEmployees') }}</ElButton>
          </div>
        </ElFormItem>
        <ElFormItem :label="$t('application.makeup.makeupClockType')" prop="title" required>
          <ElSelect
            v-model="formData.title"
            :placeholder="$t('application.makeup.pleaseSelectMakeupClockType')"
            style="width: 100%"
          >
            <ElOption :label="$t('application.makeup.clockInMakeup')" value="checkin" />
            <ElOption :label="$t('application.makeup.clockOutMakeup')" value="checkout" />
          </ElSelect>
        </ElFormItem>
        <ElFormItem :label="$t('application.makeup.makeupClockTime')" prop="startTime" required>
          <ElDatePicker
            v-model="formData.startTime"
            type="datetime"
            :placeholder="$t('common.selectTime')"
            style="width: 100%"
            value-format="YYYY-MM-DD HH:mm:00"
            format="YYYY-MM-DD HH:mm"
          />
        </ElFormItem>
        <ElFormItem :label="$t('application.makeup.makeupClockReason')" prop="reason" required>
          <ElInput
            v-model="formData.reason"
            type="textarea"
            :rows="3"
            :placeholder="$t('application.makeup.pleaseEnterMakeupClockReason')"
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

    <EmployeePickerDialog v-model="employeeDialogVisible" @confirm="handleConfirmEmployee" />
    <ApplicationDetailDrawer v-model="detailVisible" :application="currentApplication" />
  </div>
</template>
