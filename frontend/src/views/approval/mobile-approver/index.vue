<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import {
  type MobileApprover,
  createMobileApprover,
  deleteMobileApprover,
  fetchMobileApproverPage,
  updateMobileApprover
} from '@/service/api/mobileApprover';
import { fetchEmployeeList } from '@/service/api/hr';
import { $t } from '@/locales';

defineOptions({ name: 'ApprovalMobileApprover' });

const APP_TYPE_OPTIONS = [
  { label: $t('common.leave'), value: 'leave' },
  { label: $t('common.overtime'), value: 'overtime' },
  { label: $t('common.businessTrip'), value: 'business' },
  { label: $t('common.makeupClock'), value: 'makeup' },
  { label: $t('common.exchangeLeave'), value: 'exchange' },
  { label: $t('common.regularization'), value: 'regularization' },
  { label: $t('common.transfer'), value: 'transfer' },
  { label: $t('common.rewardPunishment'), value: 'reward' },
  { label: $t('common.resigned'), value: 'resignation' }
];

// 列表相关
const loading = ref(false);
const tableData = ref<MobileApprover[]>([]);
const total = ref(0);
const pageNum = ref(1);
const pageSize = ref(10);
const searchName = ref('');
const searchNo = ref('');

// 对话框相关
const dialogVisible = ref(false);
const operateType = ref<'add' | 'edit'>('add');
const submitLoading = ref(false);
const formData = ref<MobileApprover>({ employeeId: undefined, appTypes: '' });
const employeeList = ref<any[]>([]);

// 选中的类型数组（用于多选组件）
const selectedTypes = ref<string[]>([]);

async function loadData() {
  loading.value = true;
  try {
    const { data, error } = await fetchMobileApproverPage({
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      employeeName: searchName.value || undefined,
      employeeNo: searchNo.value || undefined
    });
    if (!error && data) {
      tableData.value = data.records || [];
      total.value = data.total || 0;
    }
  } finally {
    loading.value = false;
  }
}

async function loadEmployees() {
  try {
    const { data, error } = await fetchEmployeeList({ status: 1 });
    if (!error && data) {
      employeeList.value = data;
    }
  } catch {
    // 请求层已统一弹错
  }
}

onMounted(() => {
  loadData();
  loadEmployees();
});

function handleSearch() {
  pageNum.value = 1;
  loadData();
}

function handleReset() {
  searchName.value = '';
  searchNo.value = '';
  pageNum.value = 1;
  loadData();
}

function handleAdd() {
  operateType.value = 'add';
  formData.value = { employeeId: undefined, appTypes: '' };
  selectedTypes.value = [];
  dialogVisible.value = true;
}

function handleEdit(row: MobileApprover) {
  operateType.value = 'edit';
  formData.value = { ...row };
  selectedTypes.value = row.appTypes ? row.appTypes.split(',').filter(Boolean) : [];
  dialogVisible.value = true;
}

async function handleDelete(row: MobileApprover) {
  try {
    await ElMessageBox.confirm($t('approval.mobileApprover.areYouSureYouWantToDeleteThisApprovalPermissionConfig'), $t('common.tip'), { type: 'warning' });
    const { error } = await deleteMobileApprover(row.id!);
    if (!error) {
      ElMessage.success($t('common.deleteSuccess'));
      loadData();
    }
  } catch {
    // cancelled
  }
}

async function handleSubmit() {
  if (!formData.value.employeeId) {
    ElMessage.warning($t('common.pleaseSelectEmployees'));
    return;
  }
  formData.value.appTypes = selectedTypes.value.length > 0 ? selectedTypes.value.join(',') : '';

  submitLoading.value = true;
  try {
    const fn = operateType.value === 'add' ? createMobileApprover : updateMobileApprover;
    const { error } = await fn(formData.value);
    if (!error) {
      ElMessage.success(operateType.value === 'add' ? $t('approval.mobileApprover.addSuccess') : $t('common.modifySuccess'));
      dialogVisible.value = false;
      loadData();
    }
  } finally {
    submitLoading.value = false;
  }
}

function formatAppTypes(types: string | undefined) {
  if (!types) return $t('approval.mobileApprover.allTypes');
  const map: Record<string, string> = {};
  APP_TYPE_OPTIONS.forEach(o => {
    map[o.value] = o.label;
  });
  return types
    .split(',')
    .map(t => map[t] || t)
    .join('、');
}

function handlePageChange(val: number) {
  pageNum.value = val;
  loadData();
}

function handleSizeChange(val: number) {
  pageSize.value = val;
  pageNum.value = 1;
  loadData();
}
</script>

<template>
  <div class="list-page">
    <!-- 搜索栏 -->
    <ElCard shadow="never" class="mb-4">
      <ElForm inline>
        <ElFormItem :label="$t('common.employeeName')">
          <ElInput v-model="searchName" :placeholder="$t('common.pleaseInput')" clearable @keyup.enter="handleSearch" />
        </ElFormItem>
        <ElFormItem :label="$t('common.employeeNo')">
          <ElInput v-model="searchNo" :placeholder="$t('common.pleaseInput')" clearable @keyup.enter="handleSearch" />
        </ElFormItem>
        <ElFormItem>
          <ElButton type="primary" @click="handleSearch">{{ $t('common.search') }}</ElButton>
          <ElButton @click="handleReset">{{ $t('common.reset') }}</ElButton>
        </ElFormItem>
      </ElForm>
    </ElCard>

    <!-- 表格 -->
    <ElCard shadow="never" class="table-card">
      <template #header>
        <div class="card-header">
          <span>{{ $t('approval.mobileApprover.mobileApprovalPermission') }}</span>
          <ElButton type="primary" @click="handleAdd">{{ $t('common.add') }}</ElButton>
        </div>
      </template>
      <div class="table-wrapper">
        <ElTable v-loading="loading" :data="tableData" border stripe style="width: 100%">
          <ElTableColumn prop="employeeNo" :label="$t('common.employeeNo')" width="100" />
          <ElTableColumn prop="employeeName" :label="$t('common.employeeName')" width="120" />
          <ElTableColumn prop="deptName" :label="$t('common.department')" width="150" />
          <ElTableColumn :label="$t('approval.mobileApprover.approvableTypes')" min-width="250">
            <template #default="{ row }">
              <span>{{ formatAppTypes(row.appTypes) }}</span>
            </template>
          </ElTableColumn>
          <ElTableColumn prop="createdTime" :label="$t('approval.mobileApprover.configTime')" width="180" />
          <ElTableColumn :label="$t('common.action')" width="150" fixed="right">
            <template #default="{ row }">
              <ElButton link type="primary" @click="handleEdit(row)">{{ $t('common.edit') }}</ElButton>
              <ElButton link type="danger" @click="handleDelete(row)">{{ $t('common.delete') }}</ElButton>
            </template>
          </ElTableColumn>
        </ElTable>
      </div>
      <div class="mt-4 flex justify-end">
        <ElPagination
          v-model:current-page="pageNum"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next"
          @current-change="handlePageChange"
          @size-change="handleSizeChange"
        />
      </div>
    </ElCard>

    <!-- 新增/编辑对话框 -->
    <ElDialog v-model="dialogVisible" :title="operateType === 'add' ? $t('approval.mobileApprover.newApprovalPermission') : $t('approval.mobileApprover.editApprovalPermission')" width="550px">
      <ElForm label-width="100px">
        <ElFormItem :label="$t('common.selectEmployees')" required>
          <ElSelect
            v-model="formData.employeeId"
            :placeholder="$t('common.pleaseSelectEmployees')"
            filterable
            :disabled="operateType === 'edit'"
            style="width: 100%"
          >
            <ElOption
              v-for="emp in employeeList"
              :key="emp.id"
              :label="emp.employeeNo + ' - ' + emp.name"
              :value="emp.id"
            />
          </ElSelect>
        </ElFormItem>
        <ElFormItem :label="$t('approval.mobileApprover.approvalType')">
          <ElCheckboxGroup v-model="selectedTypes">
            <ElCheckbox v-for="opt in APP_TYPE_OPTIONS" :key="opt.value" :label="opt.value">
              {{ opt.label }}
            </ElCheckbox>
          </ElCheckboxGroup>
          <div class="mt-1 text-xs text-gray-400">{{ $t('approval.mobileApprover.leaveUncheckedToApproveAllTypes') }}</div>
        </ElFormItem>
      </ElForm>
      <template #footer>
        <ElButton @click="dialogVisible = false">{{ $t('common.cancel') }}</ElButton>
        <ElButton type="primary" :loading="submitLoading" @click="handleSubmit">{{ $t('common.ok') }}</ElButton>
      </template>
    </ElDialog>
  </div>
</template>

<style scoped>
.list-page {
  display: flex;
  flex-direction: column;
  height: 100%;
}

.table-card {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.table-card :deep(.el-card__body) {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.table-wrapper {
  flex: 1;
  overflow: auto;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
