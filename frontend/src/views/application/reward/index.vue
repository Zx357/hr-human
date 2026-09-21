<script setup lang="ts">
import { onMounted, ref, watch } from 'vue';
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
import { useDictOptions } from '@/composables/use-dict-options';
import { formatDateTime } from '@/utils/format';
import EmployeePickerDialog from '@/components/common/employee-picker-dialog.vue';
import ApplicationDetailDrawer from '@/components/business/application-detail-drawer.vue';
import { $t } from '@/locales';

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
const submitLoading = ref(false);
const formRef = ref<FormInstance>();
const formData = ref<
  Application & { employeeName?: string; employeeNo?: string; companyName?: string; deptName?: string }
>({
  employeeId: undefined,
  appType: 'reward',
  rewardType: 1
});

// 弹窗表单校验规则
const formRules: FormRules = {
  employeeId: [{ required: true, message: $t('common.pleaseSelectEmployees'), trigger: 'change' }],
  category: [{ required: true, message: $t('application.reward.pleaseSelectACategory'), trigger: 'change' }],
  effectDate: [{ required: true, message: $t('application.reward.pleaseSelectEffectiveDate'), trigger: 'change' }]
};

function getCategoryLabel(appType?: string, category?: string) {
  return appType === 'reward' ? getRewardCategoryLabel(category) : getPunishCategoryLabel(category);
}

// 员工选择弹窗
const employeeDialogVisible = ref(false);

const searchParams = ref({
  employeeName: '',
  employeeNo: '',
  dateRange: [] as string[],
  status: undefined as number | undefined,
  appType: undefined as string | undefined
});
/** 构建列表查询参数（奖惩页需分别请求 reward/punish 两次） */
function buildSearchParams(appType: string) {
  return {
    pageNum: currentPage.value,
    pageSize: pageSize.value,
    appType,
    employeeName: searchParams.value.employeeName || undefined,
    employeeNo: searchParams.value.employeeNo || undefined,
    status: searchParams.value.status,
    beginTime: searchParams.value.dateRange?.[0] || undefined,
    endTime: searchParams.value.dateRange?.[1] || undefined
  };
}

async function loadData() {
  loading.value = true;
  try {
    const appType = searchParams.value.appType;
    let records: Application[] = [];
    let totalCount = 0;

    if (!appType || appType === 'reward') {
      const res1 = await fetchApplicationPage(buildSearchParams('reward'));
      records = [...(res1.data?.records || [])];
      totalCount += res1.data?.total || 0;
    }
    if (!appType || appType === 'punish') {
      const res2 = await fetchApplicationPage(buildSearchParams('punish'));
      records = [...records, ...(res2.data?.records || [])];
      totalCount += res2.data?.total || 0;
    }

    records.sort((a, b) => new Date(b.createdTime || '').getTime() - new Date(a.createdTime || '').getTime());
    data.value = records;
    total.value = totalCount;
  } catch {
    // 错误已由请求层统一提示
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
    employeeId: undefined,
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
  const valid = await formRef.value?.validate().catch(() => false);
  if (!valid) return;
  formData.value.appType = formData.value.rewardType === 1 ? 'reward' : 'punish';
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
  if (!(await confirmCancelApplication(row, getCategoryLabel(row.appType, row.category)))) return;
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
  searchParams.value = { dateRange: [], employeeName: '', employeeNo: '', status: undefined, appType: undefined };
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
        <ElFormItem :label="$t('common.type')">
          <ElSelect
            v-model="searchParams.appType"
            :placeholder="$t('common.pleaseSelectType')"
            clearable
            style="width: 120px"
          >
            <ElOption :label="$t('common.reward')" value="reward" />
            <ElOption :label="$t('common.punishment')" value="punish" />
          </ElSelect>
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

    <ElCard class="flex-1">
      <template #header>
        <div class="flex items-center justify-between">
          <span>{{ $t('application.reward.rewardPunishmentApplications') }}</span>
          <ElButton v-permission="'application:reward:add'" type="primary" @click="handleAdd">
            <template #icon><icon-ep-plus /></template>
            {{ $t('application.reward.createRewardPunishmentApplication') }}
          </ElButton>
        </div>
      </template>

      <ElTable v-loading="loading" :data="data" border stripe>
        <ElTableColumn type="index" :label="$t('common.index2')" width="60" align="center" />
        <ElTableColumn prop="employeeNo" :label="$t('common.employeeNo')" min-width="100" />
        <ElTableColumn prop="employeeName" :label="$t('common.employeeName')" min-width="100" />
        <ElTableColumn
          prop="companyName"
          :label="$t('application.common.company')"
          min-width="120"
          show-overflow-tooltip
        />
        <ElTableColumn prop="deptName" :label="$t('common.department')" min-width="100" />
        <ElTableColumn prop="appType" :label="$t('common.type')" min-width="80" align="center">
          <template #default="{ row }">
            <ElTag :type="row.appType === 'reward' ? 'success' : 'danger'">
              {{ row.appType === 'reward' ? $t('common.reward') : $t('common.punishment') }}
            </ElTag>
          </template>
        </ElTableColumn>
        <ElTableColumn prop="category" :label="$t('common.category')" min-width="100">
          <template #default="{ row }">
            {{ getCategoryLabel(row.appType, row.category) }}
          </template>
        </ElTableColumn>
        <ElTableColumn prop="amount" :label="$t('common.amount')" min-width="100" align="right">
          <template #default="{ row }">{{ row.amount ? `¥${row.amount}` : '-' }}</template>
        </ElTableColumn>
        <ElTableColumn prop="effectDate" :label="$t('application.reward.effectiveDate')" min-width="110" />
        <ElTableColumn prop="reason" :label="$t('common.reason')" min-width="150" show-overflow-tooltip />
        <ElTableColumn prop="status" :label="$t('common.status')" min-width="90" align="center">
          <template #default="{ row }">
            <ElTag :type="statusTagType(row.status)">{{ statusLabel(row.status) }}</ElTag>
          </template>
        </ElTableColumn>
        <ElTableColumn prop="createdTime" :label="$t('application.common.applicationTime')" min-width="160">
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
      :title="$t('application.reward.createRewardPunishmentApplication')"
      width="600px"
      :close-on-click-modal="false"
      :close-on-press-escape="false"
      destroy-on-close
    >
      <ElForm ref="formRef" label-width="100px" :model="formData" :rules="formRules">
        <ElFormItem :label="$t('common.employee')" prop="employeeId" required>
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
        <ElFormItem :label="$t('common.type')" required>
          <ElRadioGroup v-model="formData.rewardType">
            <ElRadio :value="1">{{ $t('common.reward') }}</ElRadio>
            <ElRadio :value="2">{{ $t('common.punishment') }}</ElRadio>
          </ElRadioGroup>
        </ElFormItem>
        <ElFormItem :label="$t('common.category')" prop="category" required>
          <ElSelect
            v-model="formData.category"
            :placeholder="$t('application.reward.pleaseSelectACategory')"
            style="width: 100%"
          >
            <ElOption
              v-for="item in formData.rewardType === 1 ? rewardCategoryOptions : punishCategoryOptions"
              :key="item.dictValue"
              :label="getPunishCategoryLabel(item.dictValue)"
              :value="item.dictValue"
            />
          </ElSelect>
        </ElFormItem>
        <ElFormItem :label="$t('common.amount')">
          <ElInputNumber v-model="formData.amount" :min="0" :precision="2" style="width: 100%" />
        </ElFormItem>
        <ElFormItem :label="$t('application.reward.effectiveDate')" prop="effectDate" required>
          <ElDatePicker
            v-model="formData.effectDate"
            type="date"
            :placeholder="$t('common.selectDate')"
            style="width: 100%"
            value-format="YYYY-MM-DD"
          />
        </ElFormItem>
        <ElFormItem :label="$t('common.reason')">
          <ElInput
            v-model="formData.reason"
            type="textarea"
            :rows="3"
            :placeholder="$t('application.reward.pleaseEnterReason')"
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
