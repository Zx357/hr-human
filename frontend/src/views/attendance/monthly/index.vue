<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue';
import { ElMessage } from 'element-plus';
import type { TableColumnCtx } from 'element-plus';
import dayjs from 'dayjs';
import { type MonthlyAttendance, fetchMonthlyAttendance } from '@/service/api/attendance';
import { fetchCompanyList, fetchDepartmentTree } from '@/service/api/organization';
import { useTableColumnSetting } from '@/composables/use-table-column-setting';
import { resolveOrgIds } from '@/utils/report';
import { downloadFile } from '@/utils/download';
import TableHeaderOperation from '@/components/advanced/table-header-operation.vue';
import { $t } from '@/locales';

defineOptions({ name: 'MonthlyAttendance' });

const loading = ref(false);
const exporting = ref(false);
const data = ref<MonthlyAttendance[]>([]);
const companies = ref<any[]>([]);
const departments = ref<any[]>([]);

// 表格列设置与密度（localStorage 持久化），配合 TableHeaderOperation 使用；页面原为 small 密度，沿用为初始值
const { columnChecks, density, isColumnVisible } = useTableColumnSetting(
  'attendance-monthly',
  [
    { prop: 'companyName', label: $t('common.company'), checked: true, visible: true },
    { prop: 'employeeNo', label: $t('common.employeeNo'), checked: true, visible: true },
    { prop: 'employeeName', label: $t('common.name'), checked: true, visible: true },
    { prop: 'deptName', label: $t('common.department'), checked: true, visible: true },
    { prop: 'workDays', label: $t('attendance.monthly.requiredAttendance'), checked: true, visible: true },
    { prop: 'actualDays', label: $t('attendance.monthly.actualAttendance'), checked: true, visible: true },
    { prop: 'lateTimes', label: $t('attendance.monthly.lateCount'), checked: true, visible: true },
    { prop: 'totalLateMinutes', label: $t('attendance.monthly.lateMin'), checked: true, visible: true },
    { prop: 'earlyTimes', label: $t('attendance.monthly.earlyLeaveCount'), checked: true, visible: true },
    { prop: 'totalEarlyMinutes', label: $t('attendance.monthly.earlyLeaveMin'), checked: true, visible: true },
    { prop: 'absentDays', label: $t('common.absent'), checked: true, visible: true },
    { prop: 'leaveDays', label: $t('common.leave'), checked: true, visible: true },
    { prop: 'totalWorkHours', label: $t('attendance.monthly.totalWorkHours'), checked: true, visible: true }
  ],
  'small'
);

const currentMonth = ref(dayjs().format('YYYY-MM'));
const searchParams = ref({
  companyId: undefined as number | undefined,
  deptId: undefined as number | undefined,
  employeeNo: '',
  employeeName: ''
});

// 前端分页
const pagination = ref({ current: 1, pageSize: 20 });
const pagedData = computed(() => {
  const start = (pagination.value.current - 1) * pagination.value.pageSize;
  return data.value.slice(start, start + pagination.value.pageSize);
});

function handlePageChange(page: number) {
  pagination.value.current = page;
}

function handleSizeChange(size: number) {
  pagination.value.pageSize = size;
  pagination.value.current = 1;
}

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

async function loadData() {
  loading.value = true;
  try {
    const res = await fetchMonthlyAttendance({
      month: currentMonth.value,
      companyId: searchParams.value.companyId,
      deptId: searchParams.value.deptId,
      employeeNo: searchParams.value.employeeNo,
      employeeName: searchParams.value.employeeName
    });
    data.value = res.data || [];
    pagination.value.current = 1;
    // 月度汇总接口为一次性全量返回（无分页参数），数据量过大时提示缩小筛选范围
    if (data.value.length > 20000) {
      ElMessage.warning(
        $t('attendance.monthly.thisMonthHasTooMuchAttendanceDataAndThePageMayLagNarrowTheRangeByCompanyDepartment')
      );
    }
  } finally {
    loading.value = false;
  }
}

onMounted(() => {
  loadCompanies();
  loadData();
});

function handleSearch() {
  loadData();
}

function handleReset() {
  searchParams.value = { companyId: undefined, deptId: undefined, employeeNo: '', employeeName: '' };
  departments.value = [];
  loadData();
}

function handleMonthChange(val: string | null) {
  // 清空时回退当前月
  if (!val) {
    currentMonth.value = dayjs().format('YYYY-MM');
  }
  loadData();
}

/** 自定义合计行：基于全量数据求和（而非当前页） */
function getSummary({
  columns
}: {
  columns: TableColumnCtx<MonthlyAttendance>[];
  data: MonthlyAttendance[];
}): string[] {
  const sumProps = new Set([
    'workDays',
    'actualDays',
    'lateTimes',
    'totalLateMinutes',
    'earlyTimes',
    'totalEarlyMinutes',
    'absentDays',
    'leaveDays',
    'totalWorkHours'
  ]);
  const sums: string[] = [];
  columns.forEach((column, index) => {
    if (index === 0) {
      sums[index] = $t('common.total');
      return;
    }
    const prop = column.property;
    if (!prop || !sumProps.has(prop)) {
      sums[index] = '';
      return;
    }
    const total = data.value.reduce(
      (sum, item) => sum + Number((item as unknown as Record<string, number>)[prop] || 0),
      0
    );
    sums[index] = prop === 'totalWorkHours' ? `${total.toFixed(1)}h` : String(Number(total.toFixed(2)));
  });
  return sums;
}

/** 导出月考勤汇总 */
async function handleExport() {
  exporting.value = true;
  try {
    await downloadFile(
      '/attendance/monthly/export',
      $t('attendance.monthly.monthlyAttendanceSummaryXlsx', { month: currentMonth.value }),
      buildExportParams()
    );
    ElMessage.success($t('attendance.common.exportSuccessful'));
  } catch {
    // downloadFile 内部已提示具体错误
  } finally {
    exporting.value = false;
  }
}

/** 导出月度对账（请假/加班/缺勤工时对账表，算薪用） */
async function handleExportReconciliation() {
  exporting.value = true;
  try {
    await downloadFile(
      '/attendance/monthly/reconciliation/export',
      $t('attendance.monthly.monthlyReconciliationXlsx', { month: currentMonth.value }),
      buildExportParams()
    );
    ElMessage.success($t('attendance.common.exportSuccessful'));
  } catch {
    // downloadFile 内部已提示具体错误
  } finally {
    exporting.value = false;
  }
}

/** 导出查询参数：公司/部门解析为 orgIds（含子部门），与报表页口径一致 */
function buildExportParams() {
  const { companyId, deptId } = searchParams.value;
  let orgIds: number[] | undefined;
  if (companyId || deptId) {
    const resolved = resolveOrgIds(departments.value, deptId);
    orgIds = resolved.length > 0 ? resolved : [deptId ?? companyId!];
  }
  return {
    month: currentMonth.value,
    orgIds: orgIds?.join(','),
    employeeNo: searchParams.value.employeeNo,
    employeeName: searchParams.value.employeeName
  };
}
</script>

<template>
  <div class="min-h-500px flex-col-stretch gap-16px overflow-hidden lt-sm:overflow-auto">
    <ElCard>
      <ElForm inline :model="searchParams">
        <ElFormItem :label="$t('common.month')">
          <ElDatePicker
            v-model="currentMonth"
            type="month"
            :placeholder="$t('attendance.monthly.selectMonth')"
            value-format="YYYY-MM"
            :clearable="false"
            style="width: 140px"
            @change="handleMonthChange"
          />
        </ElFormItem>
        <ElFormItem :label="$t('common.company')">
          <ElSelect
            v-model="searchParams.companyId"
            :placeholder="$t('common.pleaseSelectCompany')"
            clearable
            style="width: 150px"
          >
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
        <ElFormItem :label="$t('common.employeeNo')">
          <ElInput
            v-model="searchParams.employeeNo"
            :placeholder="$t('common.employeeNo')"
            clearable
            style="width: 100px"
            @keyup.enter="handleSearch"
          />
        </ElFormItem>
        <ElFormItem :label="$t('common.name')">
          <ElInput
            v-model="searchParams.employeeName"
            :placeholder="$t('common.name')"
            clearable
            style="width: 100px"
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
          <span>{{ $t('attendance.monthly.monthlyAttendanceSummary') }} {{ currentMonth }}</span>
          <div class="flex flex-wrap items-center gap-8px">
            <ElButton
              v-permission="'attendance:monthly:export'"
              type="primary"
              :loading="exporting"
              @click="handleExport"
            >
              <template #icon><icon-ep-download /></template>
              {{ $t('common.export') }}
            </ElButton>
            <ElButton
              v-permission="'attendance:monthly:export'"
              :loading="exporting"
              @click="handleExportReconciliation"
            >
              <template #icon><icon-ep-download /></template>
              {{ $t('attendance.monthly.exportReconciliation') }}
            </ElButton>
            <TableHeaderOperation
              v-model:columns="columnChecks"
              v-model:density="density"
              :loading="loading"
              @refresh="loadData"
            >
              <template #default />
            </TableHeaderOperation>
          </div>
        </div>
      </template>

      <ElTable
        v-loading="loading"
        :data="pagedData"
        border
        stripe
        :size="density"
        show-summary
        :summary-method="getSummary"
      >
        <ElTableColumn
          v-if="isColumnVisible('companyName')"
          prop="companyName"
          :label="$t('common.company')"
          width="100"
          show-overflow-tooltip
          fixed="left"
        />
        <ElTableColumn
          v-if="isColumnVisible('employeeNo')"
          prop="employeeNo"
          :label="$t('common.employeeNo')"
          width="80"
          fixed="left"
        />
        <ElTableColumn
          v-if="isColumnVisible('employeeName')"
          prop="employeeName"
          :label="$t('common.name')"
          width="70"
          fixed="left"
        />
        <ElTableColumn
          v-if="isColumnVisible('deptName')"
          prop="deptName"
          :label="$t('common.department')"
          width="100"
          show-overflow-tooltip
        />
        <ElTableColumn
          v-if="isColumnVisible('workDays')"
          prop="workDays"
          :label="$t('attendance.monthly.requiredAttendance')"
          width="70"
          align="center"
        />
        <ElTableColumn
          v-if="isColumnVisible('actualDays')"
          prop="actualDays"
          :label="$t('attendance.monthly.actualAttendance')"
          width="70"
          align="center"
        />
        <ElTableColumn
          v-if="isColumnVisible('lateTimes')"
          prop="lateTimes"
          :label="$t('attendance.monthly.lateCount')"
          width="80"
          align="center"
        >
          <template #default="{ row }">
            <span :class="row.lateTimes > 0 ? 'text-red-500' : ''">{{ row.lateTimes }}</span>
          </template>
        </ElTableColumn>
        <ElTableColumn
          v-if="isColumnVisible('totalLateMinutes')"
          prop="totalLateMinutes"
          :label="$t('attendance.monthly.lateMin')"
          width="80"
          align="center"
        >
          <template #default="{ row }">
            <span :class="row.totalLateMinutes > 0 ? 'text-red-500' : ''">{{ row.totalLateMinutes }}</span>
          </template>
        </ElTableColumn>
        <ElTableColumn
          v-if="isColumnVisible('earlyTimes')"
          prop="earlyTimes"
          :label="$t('attendance.monthly.earlyLeaveCount')"
          width="80"
          align="center"
        >
          <template #default="{ row }">
            <span :class="row.earlyTimes > 0 ? 'text-red-500' : ''">{{ row.earlyTimes }}</span>
          </template>
        </ElTableColumn>
        <ElTableColumn
          v-if="isColumnVisible('totalEarlyMinutes')"
          prop="totalEarlyMinutes"
          :label="$t('attendance.monthly.earlyLeaveMin')"
          width="80"
          align="center"
        >
          <template #default="{ row }">
            <span :class="row.totalEarlyMinutes > 0 ? 'text-red-500' : ''">{{ row.totalEarlyMinutes }}</span>
          </template>
        </ElTableColumn>
        <ElTableColumn
          v-if="isColumnVisible('absentDays')"
          prop="absentDays"
          :label="$t('common.absent')"
          width="60"
          align="center"
        >
          <template #default="{ row }">
            <span :class="row.absentDays > 0 ? 'text-red-500 font-bold' : ''">{{ row.absentDays }}</span>
          </template>
        </ElTableColumn>
        <ElTableColumn
          v-if="isColumnVisible('leaveDays')"
          prop="leaveDays"
          :label="$t('common.leave')"
          width="60"
          align="center"
        />
        <ElTableColumn
          v-if="isColumnVisible('totalWorkHours')"
          prop="totalWorkHours"
          :label="$t('attendance.monthly.totalWorkHours')"
          min-width="80"
          align="center"
        >
          <template #default="{ row }">{{ row.totalWorkHours }}h</template>
        </ElTableColumn>
      </ElTable>

      <div class="mt-12px flex justify-end">
        <ElPagination
          v-model:current-page="pagination.current"
          v-model:page-size="pagination.pageSize"
          :total="data.length"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next"
          @current-change="handlePageChange"
          @size-change="handleSizeChange"
        />
      </div>
    </ElCard>
  </div>
</template>
