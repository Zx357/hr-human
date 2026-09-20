<script setup lang="ts">
import { onMounted, ref, watch } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import dayjs from 'dayjs';
import {
  type AttClockRecord,
  deleteClockRecord,
  fetchClockRecordPage,
  saveClockRecord
} from '@/service/api/attendance';
import { fetchEmployeePage } from '@/service/api/hr';
import { fetchCompanyList, fetchDepartmentTree } from '@/service/api/organization';
import { formatDateTime } from '@/utils/format';
import { $t } from '@/locales';

defineOptions({ name: 'ClockRecord' });

const loading = ref(false);
const data = ref<AttClockRecord[]>([]);
// 补卡弹窗员工下拉：远程搜索结果（按姓名/工号），避免一次性拉取全量员工
const employees = ref<any[]>([]);
const employeeSearching = ref(false);
const companies = ref<any[]>([]);
const departments = ref<any[]>([]);

const searchParams = ref({
  companyId: undefined as number | undefined,
  deptId: undefined as number | undefined,
  employeeName: '',
  dateRange: [] as string[]
});

const pagination = ref({ current: 1, pageSize: 20, total: 0 });

const dialogVisible = ref(false);
const submitLoading = ref(false);
const dialogForm = ref<AttClockRecord>({ employeeId: undefined as any, clockTime: '', clockType: 1, clockMethod: 3 });

const clockMethodOptions = [
  { value: 1, label: 'APP' },
  { value: 2, label: $t('attendance.clock.attendanceMachine') },
  { value: 3, label: $t('attendance.clock.manualMakeupClock') }
];

async function loadCompanies() {
  const res = await fetchCompanyList();
  companies.value = res.data || [];
}

async function loadDepartments(companyId?: number) {
  if (!companyId) {
    departments.value = [];
    return;
  }
  const res = await fetchDepartmentTree(companyId);
  departments.value = res.data || [];
}

watch(
  () => searchParams.value.companyId,
  val => {
    searchParams.value.deptId = undefined;
    loadDepartments(val);
  }
);

/** 补卡员工下拉远程搜索：纯数字按工号查，否则按姓名查 */
async function searchEmployees(query: string) {
  const keyword = query.trim();
  if (!keyword) {
    employees.value = [];
    return;
  }
  employeeSearching.value = true;
  try {
    const byNo = /^\d+$/.test(keyword);
    const res = await fetchEmployeePage({
      pageNum: 1,
      pageSize: 50,
      employeeNo: byNo ? keyword : undefined,
      name: byNo ? undefined : keyword,
      status: 1
    });
    employees.value = res.data?.records || [];
  } catch {
    employees.value = [];
  } finally {
    employeeSearching.value = false;
  }
}

async function loadData() {
  loading.value = true;
  try {
    const res = await fetchClockRecordPage({
      page: pagination.value.current,
      size: pagination.value.pageSize,
      companyId: searchParams.value.companyId,
      deptId: searchParams.value.deptId,
      employeeName: searchParams.value.employeeName,
      startDate: searchParams.value.dateRange?.[0],
      endDate: searchParams.value.dateRange?.[1]
    });
    data.value = res.data?.records || [];
    pagination.value.total = res.data?.total || 0;
  } finally {
    loading.value = false;
  }
}

onMounted(() => {
  loadCompanies();
  // 默认查询今天（本地时区）
  const today = dayjs().format('YYYY-MM-DD');
  searchParams.value.dateRange = [today, today];
  loadData();
});

function handleSearch() {
  pagination.value.current = 1;
  loadData();
}

function handleReset() {
  const today = dayjs().format('YYYY-MM-DD');
  searchParams.value = { companyId: undefined, deptId: undefined, employeeName: '', dateRange: [today, today] };
  departments.value = [];
  handleSearch();
}

function handleAdd() {
  dialogForm.value = {
    employeeId: undefined as any,
    clockTime: dayjs().format('YYYY-MM-DD HH:mm:ss'),
    clockType: 1,
    clockMethod: 3
  };
  dialogVisible.value = true;
}

async function handleSubmit() {
  if (!dialogForm.value.employeeId) {
    ElMessage.warning($t('common.pleaseSelectEmployees'));
    return;
  }
  if (!dialogForm.value.clockTime) {
    ElMessage.warning($t('attendance.clock.pleaseSelectClockTime'));
    return;
  }
  submitLoading.value = true;
  try {
    await saveClockRecord(dialogForm.value);
    ElMessage.success($t('common.saveSuccess'));
    dialogVisible.value = false;
    loadData();
  } catch {
    // 请求层已统一弹错
  } finally {
    submitLoading.value = false;
  }
}

async function handleDelete(row: AttClockRecord) {
  try {
    await ElMessageBox.confirm($t('attendance.clock.areYouSureYouWantToDeleteThisClockRecord'), $t('common.tip'));
  } catch {
    // 用户取消删除
    return;
  }
  try {
    await deleteClockRecord(row.id!);
    ElMessage.success($t('common.deleteSuccess'));
    loadData();
  } catch {
    // 请求层已统一弹错
  }
}

function handlePageChange(page: number) {
  pagination.value.current = page;
  loadData();
}

function handleSizeChange(size: number) {
  pagination.value.pageSize = size;
  pagination.value.current = 1;
  loadData();
}
</script>

<template>
  <div class="list-page">
    <ElCard class="search-card">
      <ElForm inline :model="searchParams">
        <ElFormItem :label="$t('common.company')">
          <ElSelect v-model="searchParams.companyId" :placeholder="$t('common.pleaseSelectCompany')" clearable style="width: 150px">
            <ElOption v-for="c in companies" :key="c.id" :label="c.unitName || c.companyName" :value="c.id" />
          </ElSelect>
        </ElFormItem>
        <ElFormItem :label="$t('common.department')">
          <ElTreeSelect
            v-model="searchParams.deptId"
            :data="departments"
            :props="{ label: 'unitName', value: 'id', children: 'children' }"
            :placeholder="$t('common.pleaseSelectDepartment')"
            clearable
            check-strictly
            style="width: 150px"
          />
        </ElFormItem>
        <ElFormItem :label="$t('common.dateRange')">
          <ElDatePicker
            v-model="searchParams.dateRange"
            type="daterange"
            :range-separator="$t('common.to')"
            :start-placeholder="$t('attendance.clock.start')"
            :end-placeholder="$t('attendance.clock.end')"
            value-format="YYYY-MM-DD"
            style="width: 240px"
          />
        </ElFormItem>
        <ElFormItem :label="$t('common.employee')">
          <ElInput
            v-model="searchParams.employeeName"
            :placeholder="$t('common.employeeName')"
            clearable
            style="width: 120px"
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

    <ElCard class="table-card">
      <template #header>
        <div class="flex items-center justify-between">
          <span>{{ $t('common.clockRecords') }}</span>
          <ElButton v-permission="'attendance:clock:add'" type="primary" @click="handleAdd">
            <template #icon><icon-ep-plus /></template>
            {{ $t('common.makeupClock') }}
          </ElButton>
        </div>
      </template>

      <div class="table-wrapper">
        <ElTable v-loading="loading" :data="data" border stripe size="small" height="100%">
          <ElTableColumn type="index" label="#" width="50" align="center" />
          <ElTableColumn prop="companyName" :label="$t('common.company')" width="120" show-overflow-tooltip />
          <ElTableColumn prop="employeeNo" :label="$t('common.employeeNo')" width="100" />
          <ElTableColumn prop="employeeName" :label="$t('common.name')" width="80" />
          <ElTableColumn prop="deptName" :label="$t('common.department')" width="120" show-overflow-tooltip />
          <ElTableColumn prop="clockTime" :label="$t('attendance.clock.clockTime')" width="160">
            <template #default="{ row }">{{ formatDateTime(row.clockTime) }}</template>
          </ElTableColumn>
          <ElTableColumn prop="clockType" :label="$t('common.type')" width="100" align="center">
            <template #default="{ row }">
              <ElTag :type="row.clockType === 1 ? 'success' : 'warning'" size="small">
                {{ row.clockType === 1 ? $t('common.clockIn') : $t('common.clockOut') }}
              </ElTag>
            </template>
          </ElTableColumn>
          <ElTableColumn prop="clockMethod" :label="$t('attendance.clock.method')" width="80" align="center">
            <template #default="{ row }">
              {{ clockMethodOptions.find(o => o.value === row.clockMethod)?.label || '-' }}
            </template>
          </ElTableColumn>
          <ElTableColumn prop="location" :label="$t('attendance.clock.location')" min-width="150" show-overflow-tooltip />
          <ElTableColumn prop="remark" :label="$t('common.remark')" min-width="120" show-overflow-tooltip />
          <ElTableColumn :label="$t('common.action')" width="80" align="center">
            <template #default="{ row }">
              <ElButton
                v-permission="'attendance:clock:delete'"
                type="danger"
                link
                size="small"
                @click="handleDelete(row)"
              >
                {{ $t('common.delete') }}
              </ElButton>
            </template>
          </ElTableColumn>
        </ElTable>
      </div>

      <div class="mt-12px flex justify-end">
        <ElPagination
          v-model:current-page="pagination.current"
          v-model:page-size="pagination.pageSize"
          :total="pagination.total"
          :page-sizes="[20, 50, 100]"
          layout="total, sizes, prev, pager, next"
          @current-change="handlePageChange"
          @size-change="handleSizeChange"
        />
      </div>
    </ElCard>

    <!-- 补卡弹窗 -->
    <ElDialog v-model="dialogVisible" :title="$t('common.makeupClock')" width="450px">
      <ElForm label-width="80px" :model="dialogForm">
        <ElFormItem :label="$t('common.employee')" required>
          <ElSelect
            v-model="dialogForm.employeeId"
            :placeholder="$t('attendance.clock.searchEmployeesByNameOrEmployeeNo')"
            filterable
            remote
            :remote-method="searchEmployees"
            :loading="employeeSearching"
            style="width: 100%"
          >
            <ElOption
              v-for="emp in employees"
              :key="emp.id"
              :label="`${emp.name} (${emp.employeeNo})`"
              :value="emp.id"
            />
          </ElSelect>
        </ElFormItem>
        <ElFormItem :label="$t('attendance.clock.clockTime')" required>
          <ElDatePicker
            v-model="dialogForm.clockTime"
            type="datetime"
            value-format="YYYY-MM-DD HH:mm:ss"
            format="YYYY-MM-DD HH:mm"
            style="width: 100%"
          />
        </ElFormItem>
        <ElFormItem :label="$t('common.type')" required>
          <ElRadioGroup v-model="dialogForm.clockType">
            <ElRadio :value="1">{{ $t('attendance.clock.clockIn') }}</ElRadio>
            <ElRadio :value="2">{{ $t('attendance.clock.clockOut') }}</ElRadio>
          </ElRadioGroup>
        </ElFormItem>
        <ElFormItem :label="$t('common.remark')">
          <ElInput v-model="dialogForm.remark" :placeholder="$t('application.makeup.makeupClockReason')" />
        </ElFormItem>
      </ElForm>
      <template #footer>
        <ElButton @click="dialogVisible = false">{{ $t('common.cancel') }}</ElButton>
        <ElButton type="primary" :loading="submitLoading" @click="handleSubmit">{{ $t('common.ok') }}</ElButton>
      </template>
    </ElDialog>
  </div>
</template>
