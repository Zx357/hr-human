<script setup lang="tsx">
import { computed, onMounted, ref } from 'vue';
import { ElMessage, ElPopconfirm } from 'element-plus';
import type { TagType } from '@/constants/common';
import {
  type LeaveQuota,
  deleteLeaveQuota,
  fetchLeaveQuotaPage,
  saveLeaveQuota
} from '@/service/api/leave-quota';
import { useDictOptions } from '@/composables/use-dict-options';
import EmployeePickerDialog from '@/components/common/employee-picker-dialog.vue';
import { $t } from '@/locales';

defineOptions({ name: 'LeaveQuotaManage' });

const loading = ref(false);
const data = ref<LeaveQuota[]>([]);
const total = ref(0);
const currentPage = ref(1);
const pageSize = ref(10);

// 假期类型字典（leave_type：1-年假 2-事假 ...）
const { options: leaveTypeOptions, getDictLabel: getLeaveTypeLabel } = useDictOptions('leave_type');

const currentYear = new Date().getFullYear();
// 年度选项：近两年到下一年
const yearOptions = Array.from({ length: 4 }, (_, i) => currentYear - 2 + i);

const searchParams = ref({
  year: currentYear as number | undefined,
  leaveType: undefined as string | undefined,
  employeeName: '',
  employeeNo: ''
});

async function loadData() {
  loading.value = true;
  try {
    const res = await fetchLeaveQuotaPage({
      pageNum: currentPage.value,
      pageSize: pageSize.value,
      year: searchParams.value.year,
      leaveType: searchParams.value.leaveType,
      employeeName: searchParams.value.employeeName || undefined,
      employeeNo: searchParams.value.employeeNo || undefined
    });
    if (res.data) {
      data.value = res.data.records || [];
      total.value = res.data.total || 0;
    }
  } catch {
    // 请求层已统一弹错
  } finally {
    loading.value = false;
  }
}

onMounted(() => {
  loadData();
});

function handleSearch() {
  currentPage.value = 1;
  loadData();
}

function handleReset() {
  searchParams.value = {
    year: currentYear,
    leaveType: undefined,
    employeeName: '',
    employeeNo: ''
  };
  handleSearch();
}

function handlePageChange() {
  loadData();
}

function handleSizeChange() {
  currentPage.value = 1;
  loadData();
}

function remainHours(row: LeaveQuota) {
  const remain = Number(row.totalHours || 0) - Number(row.usedHours || 0);
  return Math.max(0, Math.round(remain * 100) / 100);
}

/** 剩余额度展示色：不足 10% 红色告警 */
function remainTagType(row: LeaveQuota): TagType {
  const total = Number(row.totalHours || 0);
  if (total <= 0 || remainHours(row) / total < 0.1) return 'danger';
  if (remainHours(row) / total < 0.3) return 'warning';
  return 'success';
}

// ===== 新增/编辑额度 =====
const dialogVisible = ref(false);
const submitLoading = ref(false);
const editingId = ref<number | undefined>(undefined);
const employeeDialogVisible = ref(false);
const selectedEmployee = ref<Api.Hr.Employee | null>(null);
const employeeDisplayName = ref('');

const formData = ref({
  employeeId: undefined as number | undefined,
  year: currentYear,
  leaveType: '',
  totalHours: undefined as number | undefined
});

const dialogTitle = computed(() =>
  editingId.value ? $t('hr.leaveQuota.editQuota') : $t('hr.leaveQuota.addQuota')
);

function handleAdd() {
  editingId.value = undefined;
  selectedEmployee.value = null;
  employeeDisplayName.value = '';
  formData.value = { employeeId: undefined, year: currentYear, leaveType: '', totalHours: undefined };
  dialogVisible.value = true;
}

function handleEdit(row: LeaveQuota) {
  editingId.value = row.id;
  selectedEmployee.value = null;
  employeeDisplayName.value = row.employeeName ? `${row.employeeName} (${row.employeeNo})` : '';
  formData.value = {
    employeeId: row.employeeId,
    year: row.year,
    leaveType: row.leaveType,
    totalHours: Number(row.totalHours)
  };
  dialogVisible.value = true;
}

function openEmployeePicker() {
  employeeDialogVisible.value = true;
}

function handleConfirmEmployee(selected: Api.Hr.Employee[]) {
  const row = selected[0];
  if (!row?.id) return;
  selectedEmployee.value = row;
  formData.value.employeeId = row.id!;
  employeeDisplayName.value = `${row.name} (${row.employeeNo})`;
}

async function handleSubmit() {
  if (!formData.value.employeeId) {
    ElMessage.warning($t('hr.leaveQuota.pleaseSelectEmployee'));
    return;
  }
  if (!formData.value.year) {
    ElMessage.warning($t('hr.leaveQuota.pleaseSelectYear'));
    return;
  }
  if (!formData.value.leaveType) {
    ElMessage.warning($t('application.leave.pleaseSelectLeaveType'));
    return;
  }
  if (formData.value.totalHours === undefined || formData.value.totalHours === null || formData.value.totalHours < 0) {
    ElMessage.warning($t('hr.leaveQuota.pleaseInputTotalHours'));
    return;
  }
  submitLoading.value = true;
  try {
    await saveLeaveQuota({
      id: editingId.value,
      employeeId: formData.value.employeeId,
      year: formData.value.year,
      leaveType: formData.value.leaveType,
      totalHours: formData.value.totalHours
    });
    ElMessage.success($t('common.updateSuccess'));
    dialogVisible.value = false;
    loadData();
  } catch {
    // 请求层已统一弹错
  } finally {
    submitLoading.value = false;
  }
}

async function handleDelete(id?: number) {
  if (!id) return;
  try {
    await deleteLeaveQuota(id);
    ElMessage.success($t('common.deleteSuccess'));
    loadData();
  } catch {
    // 请求层已统一弹错
  }
}
</script>

<template>
  <div class="min-h-500px flex-col-stretch gap-16px overflow-hidden lt-sm:overflow-auto">
    <ElCard>
      <ElForm inline :model="searchParams">
        <ElFormItem :label="$t('hr.leaveQuota.year')">
          <ElSelect v-model="searchParams.year" :placeholder="$t('hr.leaveQuota.pleaseSelectYear')" clearable style="width: 110px">
            <ElOption v-for="y in yearOptions" :key="y" :label="y" :value="y" />
          </ElSelect>
        </ElFormItem>
        <ElFormItem :label="$t('common.leaveType')">
          <ElSelect
            v-model="searchParams.leaveType"
            :placeholder="$t('application.leave.pleaseSelectLeaveType')"
            clearable
            style="width: 130px"
          >
            <ElOption
              v-for="item in leaveTypeOptions"
              :key="item.dictValue"
              :label="getLeaveTypeLabel(item.dictValue)"
              :value="item.dictValue"
            />
          </ElSelect>
        </ElFormItem>
        <ElFormItem :label="$t('common.employeeNo')">
          <ElInput
            v-model="searchParams.employeeNo"
            :placeholder="$t('common.employeeNo')"
            clearable
            style="width: 110px"
            @keyup.enter="handleSearch"
          />
        </ElFormItem>
        <ElFormItem :label="$t('common.name')">
          <ElInput
            v-model="searchParams.employeeName"
            :placeholder="$t('common.name')"
            clearable
            style="width: 110px"
            @keyup.enter="handleSearch"
          />
        </ElFormItem>
        <ElFormItem>
          <ElButton type="primary" @click="handleSearch">
            <icon-ep-search />
            {{ $t('common.search') }}
          </ElButton>
          <ElButton @click="handleReset">
            <icon-ep-refresh />
            {{ $t('common.reset') }}
          </ElButton>
        </ElFormItem>
      </ElForm>
    </ElCard>

    <ElCard class="flex-1">
      <template #header>
        <div class="flex flex-wrap items-center justify-between gap-12px">
          <span>{{ $t('hr.leaveQuota.title') }}</span>
          <ElButton v-permission="'hr:leavequota:manage'" type="primary" @click="handleAdd">
            <template #icon><icon-ep-plus /></template>
            {{ $t('hr.leaveQuota.addQuota') }}
          </ElButton>
        </div>
      </template>

      <ElTable v-loading="loading" :data="data" size="small" border>
        <ElTableColumn prop="employeeNo" :label="$t('common.employeeNo')" width="110" />
        <ElTableColumn prop="employeeName" :label="$t('common.employeeName')" width="100" />
        <ElTableColumn prop="year" :label="$t('hr.leaveQuota.year')" width="90" align="center" />
        <ElTableColumn prop="leaveType" :label="$t('common.leaveType')" width="110" align="center">
          <template #default="{ row }">{{ getLeaveTypeLabel(row.leaveType) }}</template>
        </ElTableColumn>
        <ElTableColumn prop="totalHours" :label="$t('hr.leaveQuota.totalHours')" width="120" align="center">
          <template #default="{ row }">{{ row.totalHours }}h</template>
        </ElTableColumn>
        <ElTableColumn prop="usedHours" :label="$t('hr.leaveQuota.usedHours')" width="120" align="center">
          <template #default="{ row }">{{ row.usedHours || 0 }}h</template>
        </ElTableColumn>
        <ElTableColumn prop="remain" :label="$t('hr.leaveQuota.remainHours')" min-width="120" align="center">
          <template #default="{ row }">
            <ElTag :type="remainTagType(row)">{{ remainHours(row) }}h</ElTag>
          </template>
        </ElTableColumn>
        <ElTableColumn :label="$t('common.action')" width="130" align="center" fixed="right">
          <template #default="{ row }">
            <ElButton v-permission="'hr:leavequota:manage'" type="primary" link size="small" @click="handleEdit(row)">
              {{ $t('common.edit') }}
            </ElButton>
            <ElPopconfirm :title="$t('hr.leaveQuota.confirmDelete')" @confirm="handleDelete(row.id)">
              <template #reference>
                <ElButton v-permission="'hr:leavequota:manage'" type="danger" link size="small">
                  {{ $t('common.delete') }}
                </ElButton>
              </template>
            </ElPopconfirm>
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

    <!-- 新增/编辑额度弹窗 -->
    <ElDialog v-model="dialogVisible" :title="dialogTitle" width="480px" destroy-on-close :close-on-click-modal="false">
      <ElForm label-width="100px">
        <ElFormItem :label="$t('common.employee')" required>
          <div class="w-full flex gap-8px">
            <ElInput :model-value="employeeDisplayName" readonly :placeholder="$t('hr.leaveQuota.pleaseSelectEmployee')">
              <template #append>
                <ElButton @click="openEmployeePicker">
                  <icon-ep-search />
                </ElButton>
              </template>
            </ElInput>
          </div>
        </ElFormItem>
        <ElFormItem :label="$t('hr.leaveQuota.year')" required>
          <ElSelect v-model="formData.year" style="width: 100%">
            <ElOption v-for="y in yearOptions" :key="y" :label="y" :value="y" />
          </ElSelect>
        </ElFormItem>
        <ElFormItem :label="$t('common.leaveType')" required>
          <ElSelect
            v-model="formData.leaveType"
            :placeholder="$t('application.leave.pleaseSelectLeaveType')"
            :disabled="!!editingId"
            style="width: 100%"
          >
            <ElOption
              v-for="item in leaveTypeOptions"
              :key="item.dictValue"
              :label="getLeaveTypeLabel(item.dictValue)"
              :value="item.dictValue"
            />
          </ElSelect>
        </ElFormItem>
        <ElFormItem :label="$t('hr.leaveQuota.totalHours')" required>
          <ElInputNumber v-model="formData.totalHours" :min="0" :precision="2" :controls="false" style="width: 100%">
            <template #append>h</template>
          </ElInputNumber>
        </ElFormItem>
      </ElForm>
      <template #footer>
        <ElButton @click="dialogVisible = false">{{ $t('common.cancel') }}</ElButton>
        <ElButton type="primary" :loading="submitLoading" @click="handleSubmit">{{ $t('common.ok') }}</ElButton>
      </template>
    </ElDialog>

    <EmployeePickerDialog v-model="employeeDialogVisible" @confirm="handleConfirmEmployee" />
  </div>
</template>
