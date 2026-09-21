<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
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
import { useOrgTree } from '@/composables/use-org-tree';
import { formatDateTime } from '@/utils/format';
import EmployeePickerDialog from '@/components/common/employee-picker-dialog.vue';
import ApplicationDetailDrawer from '@/components/business/application-detail-drawer.vue';
import { $t } from '@/locales';

defineOptions({ name: 'TransferApplication' });

const loading = ref(false);
// 选择员工
const employeeDisplayName = ref('');
const data = ref<Application[]>([]);
const total = ref(0);
const currentPage = ref(1);
const pageSize = ref(10);

const { options: transferTypeOptions, getDictLabel: getTransferTypeLabel } = useDictOptions('transfer_type');
const { options: positionOptions, getDictLabel: getPositionLabel } = useDictOptions('position');
const { orgTreeOptions } = useOrgTree();
const orgFlatMap = ref<Map<number, Api.Organization.OrgUnit>>(new Map());
const dialogVisible = ref(false);
const submitLoading = ref(false);
const formRef = ref<FormInstance>();
const formData = ref<
  Application & { employeeName?: string; employeeNo?: string; fromCompanyName?: string; fromDeptName?: string }
>({
  employeeId: undefined,
  appType: 'transfer'
});

// 弹窗表单校验规则（新部门/新职位按变更类型条件必填，提交前单独校验）
const formRules: FormRules = {
  employeeId: [{ required: true, message: $t('common.pleaseSelectEmployees'), trigger: 'change' }],
  transferType: [{ required: true, message: $t('application.transfer.pleaseSelectChangeType'), trigger: 'change' }],
  effectDate: [{ required: true, message: $t('application.reward.pleaseSelectEffectiveDate'), trigger: 'change' }]
};

// 变更类型：1-部门调动，2-职位变更，3-部门+职位变更
const showNewDept = computed(() => formData.value.transferType === '1' || formData.value.transferType === '3');
const showNewPosition = computed(() => formData.value.transferType === '2' || formData.value.transferType === '3');

// 员工选择弹窗
const employeeDialogVisible = ref(false);

const searchParams = ref({
  employeeName: '',
  employeeNo: '',
  dateRange: [] as string[],
  status: undefined as number | undefined,
  transferType: undefined as string | undefined
});

// 根据部门ID向上查找公司ID（unitType=2）
function findCompanyId(deptId: number | undefined): number | undefined {
  if (!deptId) return undefined;
  let current = orgFlatMap.value.get(deptId);
  while (current) {
    if (current.unitType === 2) return current.id;
    if (!current.parentId) break;
    current = orgFlatMap.value.get(current.parentId);
  }
  return undefined;
} // 变更类型改变时清空相关字段
function handleTransferTypeChange() {
  // 清空新部门和新职位
  formData.value.toDeptId = undefined;
  formData.value.toPosition = '';
}

async function loadData() {
  loading.value = true;
  try {
    const res = await fetchApplicationPage({
      pageNum: currentPage.value,
      pageSize: pageSize.value,
      appType: 'transfer',
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
    appType: 'transfer',
    transferType: '',
    fromCompanyId: undefined,
    toCompanyId: undefined,
    fromDeptId: undefined,
    toDeptId: undefined,
    fromPosition: '',
    toPosition: '',
    effectDate: '',
    reason: '',
    employeeName: '',
    employeeNo: '',
    fromCompanyName: '',
    fromDeptName: ''
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
  formData.value.fromCompanyName = row.companyName || '';
  formData.value.fromDeptName = row.deptName || '';
  formData.value.fromCompanyId = row.companyId;
  formData.value.fromDeptId = row.deptId;
  formData.value.fromPosition = row.position || '';
  employeeDisplayName.value = `${row.name} (${row.employeeNo})`;
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false);
  if (!valid) return;
  // 根据变更类型验证必填项
  if (showNewDept.value && !formData.value.toDeptId) {
    ElMessage.warning($t('application.transfer.pleaseSelectNewDepartment'));
    return;
  }
  if (showNewPosition.value && !formData.value.toPosition) {
    ElMessage.warning($t('application.transfer.pleaseSelectNewPosition'));
    return;
  }
  // 自动计算新公司ID
  if (formData.value.toDeptId) {
    formData.value.toCompanyId = findCompanyId(formData.value.toDeptId);
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

async function handleCancel(row: Application) {
  if (!(await confirmCancelApplication(row, getTransferTypeLabel(row.transferType)))) return;
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
  searchParams.value = { dateRange: [], employeeName: '', employeeNo: '', status: undefined, transferType: undefined };
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
        <ElFormItem :label="$t('application.transfer.changeType')">
          <ElSelect
            v-model="searchParams.transferType"
            :placeholder="$t('common.pleaseSelectType')"
            clearable
            style="width: 150px"
          >
            <ElOption
              v-for="item in transferTypeOptions"
              :key="item.dictValue"
              :label="getTransferTypeLabel(item.dictValue)"
              :value="item.dictValue"
            />
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
          <span>{{ $t('application.transfer.transferApplications') }}</span>
          <ElButton v-permission="'application:transfer:add'" type="primary" @click="handleAdd">
            <template #icon><icon-ep-plus /></template>
            {{ $t('application.common.newApplication') }}
          </ElButton>
        </div>
      </template>

      <ElTable v-loading="loading" :data="data" border stripe>
        <ElTableColumn type="index" :label="$t('common.index2')" width="60" align="center" />
        <ElTableColumn prop="employeeNo" :label="$t('common.employeeNo')" min-width="100" />
        <ElTableColumn prop="employeeName" :label="$t('common.employeeName')" min-width="100" />
        <ElTableColumn
          prop="transferType"
          :label="$t('application.transfer.changeType')"
          min-width="100"
          align="center"
        >
          <template #default="{ row }">{{ getTransferTypeLabel(row.transferType) }}</template>
        </ElTableColumn>
        <ElTableColumn
          prop="fromCompanyName"
          :label="$t('application.transfer.originalCompany')"
          min-width="120"
          show-overflow-tooltip
        />
        <ElTableColumn prop="fromDeptName" :label="$t('application.transfer.originalDepartment')" min-width="100" />
        <ElTableColumn prop="fromPosition" :label="$t('application.transfer.originalPosition')" min-width="100">
          <template #default="{ row }">{{ getPositionLabel(row.fromPosition) }}</template>
        </ElTableColumn>
        <ElTableColumn
          prop="toCompanyName"
          :label="$t('application.transfer.newCompany')"
          min-width="120"
          show-overflow-tooltip
        />
        <ElTableColumn prop="toDeptName" :label="$t('application.transfer.newDepartment')" min-width="100" />
        <ElTableColumn prop="toPosition" :label="$t('application.transfer.newPosition')" min-width="100">
          <template #default="{ row }">{{ getPositionLabel(row.toPosition) }}</template>
        </ElTableColumn>
        <ElTableColumn prop="effectDate" :label="$t('application.reward.effectiveDate')" min-width="110" />
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
      :title="$t('application.transfer.newTransferApplication')"
      width="650px"
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
        <ElRow :gutter="20">
          <ElCol :span="12">
            <ElFormItem :label="$t('application.transfer.originalCompany')">
              <ElInput
                v-model="formData.fromCompanyName"
                disabled
                :placeholder="$t('application.transfer.autoDisplayedAfterSelectingAnEmployee')"
              />
            </ElFormItem>
          </ElCol>
          <ElCol :span="12">
            <ElFormItem :label="$t('application.transfer.originalDepartment')">
              <ElInput
                v-model="formData.fromDeptName"
                disabled
                :placeholder="$t('application.transfer.autoDisplayedAfterSelectingAnEmployee')"
              />
            </ElFormItem>
          </ElCol>
        </ElRow>
        <ElFormItem :label="$t('application.transfer.originalPosition')">
          <ElInput
            :model-value="getPositionLabel(formData.fromPosition)"
            disabled
            :placeholder="$t('application.transfer.autoDisplayedAfterSelectingAnEmployee')"
          />
        </ElFormItem>
        <ElFormItem :label="$t('application.transfer.changeType')" prop="transferType" required>
          <ElSelect
            v-model="formData.transferType"
            :placeholder="$t('application.transfer.pleaseSelectChangeType')"
            style="width: 100%"
            @change="handleTransferTypeChange"
          >
            <ElOption
              v-for="item in transferTypeOptions"
              :key="item.dictValue"
              :label="getTransferTypeLabel(item.dictValue)"
              :value="item.dictValue"
            />
          </ElSelect>
        </ElFormItem>
        <ElFormItem v-if="showNewDept" :label="$t('application.transfer.newDepartment')" required>
          <ElTreeSelect
            v-model="formData.toDeptId"
            :data="orgTreeOptions"
            :props="{ children: 'children', label: 'unitName', value: 'id' }"
            node-key="id"
            :placeholder="$t('application.transfer.pleaseSelectNewDepartment')"
            clearable
            style="width: 100%"
            :render-after-expand="false"
            filterable
            check-strictly
          />
        </ElFormItem>
        <ElFormItem v-if="showNewPosition" :label="$t('application.transfer.newPosition')" required>
          <ElSelect
            v-model="formData.toPosition"
            :placeholder="$t('application.transfer.pleaseSelectNewPosition')"
            style="width: 100%"
            clearable
          >
            <ElOption
              v-for="item in positionOptions"
              :key="item.dictValue"
              :label="getPositionLabel(item.dictValue)"
              :value="item.dictValue"
            />
          </ElSelect>
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
        <ElFormItem :label="$t('application.transfer.transferReason')">
          <ElInput
            v-model="formData.reason"
            type="textarea"
            :rows="3"
            :placeholder="$t('application.transfer.pleaseEnterTransferReason')"
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
