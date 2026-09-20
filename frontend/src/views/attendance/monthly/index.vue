<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue';
import { ElMessage } from 'element-plus';
import type { TableColumnCtx } from 'element-plus';
import dayjs from 'dayjs';
import { type MonthlyAttendance, fetchMonthlyAttendance } from '@/service/api/attendance';
import { fetchCompanyList, fetchDepartmentTree } from '@/service/api/organization';
import { resolveOrgIds } from '@/utils/report';
import { downloadFile } from '@/utils/download';

defineOptions({ name: 'MonthlyAttendance' });

const loading = ref(false);
const exporting = ref(false);
const data = ref<MonthlyAttendance[]>([]);
const companies = ref<any[]>([]);
const departments = ref<any[]>([]);

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
      sums[index] = '合计';
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
    // 与报表页保持一致：公司/部门解析为 orgIds（含子部门）
    const { companyId, deptId } = searchParams.value;
    let orgIds: number[] | undefined;
    if (companyId || deptId) {
      const resolved = resolveOrgIds(departments.value, deptId);
      orgIds = resolved.length > 0 ? resolved : [deptId ?? companyId!];
    }
    await downloadFile('/attendance/monthly/export', `月考勤汇总_${currentMonth.value}.xlsx`, {
      month: currentMonth.value,
      orgIds: orgIds?.join(','),
      employeeNo: searchParams.value.employeeNo,
      employeeName: searchParams.value.employeeName
    });
    ElMessage.success('导出成功');
  } catch {
    ElMessage.error('导出失败');
  } finally {
    exporting.value = false;
  }
}
</script>

<template>
  <div class="min-h-500px flex-col-stretch gap-16px overflow-hidden lt-sm:overflow-auto">
    <ElCard>
      <ElForm inline :model="searchParams">
        <ElFormItem label="月份">
          <ElDatePicker
            v-model="currentMonth"
            type="month"
            placeholder="选择月份"
            value-format="YYYY-MM"
            :clearable="false"
            style="width: 140px"
            @change="handleMonthChange"
          />
        </ElFormItem>
        <ElFormItem label="公司">
          <ElSelect v-model="searchParams.companyId" placeholder="请选择公司" clearable style="width: 150px">
            <ElOption v-for="c in companies" :key="c.id" :label="c.unitName || c.companyName" :value="c.id" />
          </ElSelect>
        </ElFormItem>
        <ElFormItem label="部门">
          <ElTreeSelect
            v-model="searchParams.deptId"
            :data="departments"
            :props="{ label: 'unitName', value: 'id', children: 'children' }"
            placeholder="请选择部门"
            clearable
            check-strictly
            style="width: 150px"
          />
        </ElFormItem>
        <ElFormItem label="工号">
          <ElInput v-model="searchParams.employeeNo" placeholder="工号" clearable style="width: 100px" />
        </ElFormItem>
        <ElFormItem label="姓名">
          <ElInput v-model="searchParams.employeeName" placeholder="姓名" clearable style="width: 100px" />
        </ElFormItem>
        <ElFormItem>
          <ElButton type="primary" @click="handleSearch">
            <icon-ep-search />
            搜索
          </ElButton>
          <ElButton @click="handleReset">
            <icon-ep-refresh />
            重置
          </ElButton>
        </ElFormItem>
      </ElForm>
    </ElCard>

    <ElCard class="flex-1">
      <template #header>
        <div class="flex items-center justify-between">
          <span>月考勤汇总 - {{ currentMonth }}</span>
          <ElButton type="primary" :loading="exporting" @click="handleExport">
            <template #icon><icon-ep-download /></template>
            导出
          </ElButton>
        </div>
      </template>

      <ElTable
        v-loading="loading"
        :data="pagedData"
        border
        stripe
        size="small"
        show-summary
        :summary-method="getSummary"
      >
        <ElTableColumn prop="companyName" label="公司" width="100" show-overflow-tooltip />
        <ElTableColumn prop="employeeNo" label="工号" width="80" />
        <ElTableColumn prop="employeeName" label="姓名" width="70" />
        <ElTableColumn prop="deptName" label="部门" width="100" show-overflow-tooltip />
        <ElTableColumn prop="workDays" label="应出勤" width="70" align="center" />
        <ElTableColumn prop="actualDays" label="实出勤" width="70" align="center" />
        <ElTableColumn prop="lateTimes" label="迟到次数" width="80" align="center">
          <template #default="{ row }">
            <span :class="row.lateTimes > 0 ? 'text-red-500' : ''">{{ row.lateTimes }}</span>
          </template>
        </ElTableColumn>
        <ElTableColumn prop="totalLateMinutes" label="迟到(分)" width="80" align="center">
          <template #default="{ row }">
            <span :class="row.totalLateMinutes > 0 ? 'text-red-500' : ''">{{ row.totalLateMinutes }}</span>
          </template>
        </ElTableColumn>
        <ElTableColumn prop="earlyTimes" label="早退次数" width="80" align="center">
          <template #default="{ row }">
            <span :class="row.earlyTimes > 0 ? 'text-red-500' : ''">{{ row.earlyTimes }}</span>
          </template>
        </ElTableColumn>
        <ElTableColumn prop="totalEarlyMinutes" label="早退(分)" width="80" align="center">
          <template #default="{ row }">
            <span :class="row.totalEarlyMinutes > 0 ? 'text-red-500' : ''">{{ row.totalEarlyMinutes }}</span>
          </template>
        </ElTableColumn>
        <ElTableColumn prop="absentDays" label="旷工" width="60" align="center">
          <template #default="{ row }">
            <span :class="row.absentDays > 0 ? 'text-red-500 font-bold' : ''">{{ row.absentDays }}</span>
          </template>
        </ElTableColumn>
        <ElTableColumn prop="leaveDays" label="请假" width="60" align="center" />
        <ElTableColumn prop="totalWorkHours" label="总工时" min-width="80" align="center">
          <template #default="{ row }">{{ row.totalWorkHours }}h</template>
        </ElTableColumn>
      </ElTable>

      <div class="mt-12px flex justify-end">
        <ElPagination
          v-model:current-page="pagination.current"
          v-model:page-size="pagination.pageSize"
          :total="data.length"
          :page-sizes="[20, 50, 100]"
          layout="total, sizes, prev, pager, next"
          @current-change="handlePageChange"
          @size-change="handleSizeChange"
        />
      </div>
    </ElCard>
  </div>
</template>
