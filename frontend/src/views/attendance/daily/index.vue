<script setup lang="ts">
import { onMounted, ref, watch } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import dayjs from 'dayjs';
import {
  type AttDailyRecord,
  calculateDailyAttendance,
  fetchDailyRecordPage,
  lockDailyRecords
} from '@/service/api/attendance';
import { fetchCompanyList, fetchDepartmentTree } from '@/service/api/organization';
import { resolveOrgIds } from '@/utils/report';
import { downloadFile } from '@/utils/download';
import { $t } from '@/locales';

defineOptions({ name: 'DailyAttendance' });

const loading = ref(false);
const calculating = ref(false);
const exporting = ref(false);
const data = ref<AttDailyRecord[]>([]);
const companies = ref<any[]>([]);
const departments = ref<any[]>([]);
const selectedRows = ref<AttDailyRecord[]>([]);

// 默认日期范围为今天（本地时区）
const today = dayjs().format('YYYY-MM-DD');
const searchParams = ref({
  dateRange: [today, today] as string[],
  companyId: undefined as number | undefined,
  deptId: undefined as number | undefined,
  employeeNo: '',
  employeeName: '',
  status: undefined as number | undefined
});

const pagination = ref({ current: 1, pageSize: 20, total: 0 });

const statusMap: Record<number, { label: string; type: string }> = {
  0: { label: $t('attendance.daily.unprocessed'), type: 'info' },
  1: { label: $t('common.normal'), type: 'success' },
  2: { label: $t('common.late'), type: 'warning' },
  3: { label: $t('common.earlyLeave'), type: 'warning' },
  4: { label: $t('common.absent'), type: 'danger' },
  5: { label: $t('common.leave'), type: 'info' },
  6: { label: $t('common.businessTrip'), type: 'primary' },
  7: { label: $t('attendance.daily.lateEarlyLeave'), type: 'danger' }
};

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
    const res = await fetchDailyRecordPage({
      page: pagination.value.current,
      size: pagination.value.pageSize,
      startDate: searchParams.value.dateRange?.[0],
      endDate: searchParams.value.dateRange?.[1],
      companyId: searchParams.value.companyId,
      deptId: searchParams.value.deptId,
      employeeNo: searchParams.value.employeeNo,
      employeeName: searchParams.value.employeeName,
      status: searchParams.value.status
    });
    data.value = res.data?.records || [];
    pagination.value.total = res.data?.total || 0;
  } finally {
    loading.value = false;
  }
}

onMounted(() => {
  loadCompanies();
  loadData();
});

function handleSearch() {
  pagination.value.current = 1;
  loadData();
}

function handleReset() {
  const todayStr = dayjs().format('YYYY-MM-DD');
  searchParams.value = {
    dateRange: [todayStr, todayStr],
    companyId: undefined,
    deptId: undefined,
    employeeNo: '',
    employeeName: '',
    status: undefined
  };
  departments.value = [];
  handleSearch();
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

function handleSelectionChange(rows: AttDailyRecord[]) {
  selectedRows.value = rows;
}

const weekDays = [$t('attendance.daily.sun'), $t('attendance.daily.mon'), $t('attendance.daily.tue'), $t('attendance.daily.wed'), $t('attendance.daily.thu'), $t('attendance.daily.fri'), $t('attendance.daily.sat')];
function getWeekDay(dateStr: string) {
  if (!dateStr) return '';
  return $t('attendance.daily.wk', { day: weekDays[dayjs(dateStr).day()] });
}

// 计算请假总时长
function getTotalLeaveHours(row: AttDailyRecord): number {
  return (
    (row.annualLeaveDuration || 0) +
    (row.personalLeaveDuration || 0) +
    (row.sickLeaveDuration || 0) +
    (row.marriageLeaveDuration || 0) +
    (row.maternityLeaveDuration || 0) +
    (row.paternityLeaveDuration || 0) +
    (row.bereavementLeaveDuration || 0)
  );
}

// 计算按钮显示文案
function getCalculateButtonText() {
  if (selectedRows.value.length > 0) {
    return $t('attendance.daily.selected', { count: selectedRows.value.length });
  }
  const filters: string[] = [];
  if (searchParams.value.companyId) {
    const company = companies.value.find(c => c.id === searchParams.value.companyId);
    if (company) filters.push(company.unitName || company.companyName);
  }
  if (searchParams.value.deptId) {
    filters.push($t('attendance.daily.specifiedDepartment'));
  }
  if (searchParams.value.employeeNo) {
    filters.push($t('attendance.daily.no', { no: searchParams.value.employeeNo }));
  }
  if (searchParams.value.employeeName) {
    filters.push($t('attendance.daily.name', { name: searchParams.value.employeeName }));
  }
  if (filters.length > 0) {
    return $t('attendance.daily.calculateByFilters');
  }
  return $t('attendance.daily.calculateAllEmployees');
}

// 计算提示信息
function getCalculateTooltip() {
  if (selectedRows.value.length > 0) {
    return $t('attendance.daily.attendanceWillBeCalculatedForSelectedEmployees', { count: selectedRows.value.length });
  }
  const filters: string[] = [];
  if (searchParams.value.companyId) {
    const company = companies.value.find(c => c.id === searchParams.value.companyId);
    if (company) filters.push($t('attendance.daily.company', { name: company.unitName || company.companyName }));
  }
  if (searchParams.value.deptId) {
    filters.push($t('attendance.daily.specifiedDepartment'));
  }
  if (searchParams.value.employeeNo) {
    filters.push($t('attendance.daily.employeeNoContains', { no: searchParams.value.employeeNo }));
  }
  if (searchParams.value.employeeName) {
    filters.push($t('attendance.daily.nameContains', { name: searchParams.value.employeeName }));
  }
  if (filters.length > 0) {
    return $t('attendance.daily.employeesWillBeFilteredBy', { filters: filters.join('\n') });
  }
  return $t('attendance.daily.attendanceWillBeCalculatedForAllActiveEmployees');
}

async function handleCalculate() {
  if (!searchParams.value.dateRange?.[0] || !searchParams.value.dateRange?.[1]) {
    ElMessage.warning($t('attendance.common.pleaseSelectDateRange'));
    return;
  }
  try {
    await ElMessageBox.confirm($t('attendance.daily.confirmToRunAttendanceCalculation', { tip: getCalculateTooltip().replace(/\n/g, ' ') }), $t('attendance.daily.attendanceCalculationConfirmation'), {
      type: 'warning',
      confirmButtonText: $t('attendance.daily.confirmCalculation'),
      cancelButtonText: $t('common.cancel')
    });
  } catch {
    return;
  }
  calculating.value = true;
  try {
    // 获取选中的员工ID列表
    const employeeIds = selectedRows.value.length > 0 ? selectedRows.value.map(r => r.employeeId) : undefined;

    await calculateDailyAttendance({
      startDate: searchParams.value.dateRange[0],
      endDate: searchParams.value.dateRange[1],
      companyId: searchParams.value.companyId,
      deptId: searchParams.value.deptId,
      employeeNo: searchParams.value.employeeNo || undefined,
      employeeName: searchParams.value.employeeName || undefined,
      employeeIds
    });
    ElMessage.success($t('attendance.daily.attendanceCalculationCompleted'));
    loadData();
  } catch {
    // 请求层已统一弹错
  } finally {
    calculating.value = false;
  }
}

async function handleLock(lock: boolean) {
  if (selectedRows.value.length === 0) {
    ElMessage.warning($t('attendance.daily.pleaseSelectRecordsToOperateFirst'));
    return;
  }
  // 收集所有时段的ID
  const ids: number[] = [];
  for (const row of selectedRows.value) {
    if (row.periods && row.periods.length > 0) {
      for (const p of row.periods) {
        if (p.id) ids.push(p.id);
      }
    } else if (row.id) {
      ids.push(row.id);
    }
  }
  if (ids.length === 0) {
    ElMessage.warning($t('attendance.daily.noRecordsAvailable'));
    return;
  }
  try {
    await ElMessageBox.confirm(
      $t('attendance.daily.areYouSureYouWantToTheSelectedAttendanceRecords', { action: lock ? $t('attendance.daily.lock') : $t('attendance.daily.unlock'), count: selectedRows.value.length }),
      lock ? $t('attendance.daily.lockConfirmation') : $t('attendance.daily.unlockConfirmation'),
      {
        type: 'warning',
        confirmButtonText: $t('common.confirm'),
        cancelButtonText: $t('common.cancel')
      }
    );
  } catch {
    return;
  }
  try {
    await lockDailyRecords({ ids, lock });
    ElMessage.success(lock ? $t('attendance.daily.lockedSuccessfully') : $t('attendance.daily.unlockedSuccessfully'));
    loadData();
  } catch {
    // 请求层已统一弹错
  }
}

/** 导出日考勤记录 */
async function handleExport() {
  if (!searchParams.value.dateRange?.[0] || !searchParams.value.dateRange?.[1]) {
    ElMessage.warning($t('attendance.common.pleaseSelectDateRange'));
    return;
  }
  exporting.value = true;
  try {
    const startDate = searchParams.value.dateRange[0];
    const endDate = searchParams.value.dateRange[1];
    // 与列表查询保持一致：公司/部门解析为 orgIds（含子部门）
    const { companyId, deptId } = searchParams.value;
    let orgIds: number[] | undefined;
    if (companyId || deptId) {
      const resolved = resolveOrgIds(departments.value, deptId);
      orgIds = resolved.length > 0 ? resolved : [deptId ?? companyId!];
    }
    await downloadFile('/attendance/daily/export', $t('attendance.daily.dailyAttendanceXlsx', { start: startDate, end: endDate }), {
      startDate,
      endDate,
      orgIds: orgIds?.join(','),
      employeeNo: searchParams.value.employeeNo,
      employeeName: searchParams.value.employeeName,
      status: searchParams.value.status
    });
    ElMessage.success($t('attendance.common.exportSuccessful'));
  } catch {
    // downloadFile 内部已提示具体错误
  } finally {
    exporting.value = false;
  }
}
</script>

<template>
  <div class="min-h-500px flex-col-stretch gap-16px overflow-hidden lt-sm:overflow-auto">
    <ElCard>
      <ElForm inline :model="searchParams">
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
        <ElFormItem :label="$t('common.status')">
          <ElSelect v-model="searchParams.status" :placeholder="$t('common.all')" clearable style="width: 100px">
            <ElOption v-for="(item, key) in statusMap" :key="key" :label="item.label" :value="Number(key)" />
          </ElSelect>
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
        <div class="flex items-center justify-between">
          <div class="flex items-center gap-12px">
            <span>{{ $t('attendance.daily.dailyAttendanceRecords') }}</span>
            <ElTooltip :content="getCalculateTooltip()" placement="top">
              <ElButton v-permission="'attendance:daily:calculate'" type="success" :loading="calculating" @click="handleCalculate">
                <template #icon><icon-ep-refresh /></template>
                {{ getCalculateButtonText() }}
              </ElButton>
            </ElTooltip>
            <ElButton v-permission="'attendance:daily:lock'" type="warning" :disabled="selectedRows.length === 0" @click="handleLock(true)">
              <template #icon><icon-ep-lock /></template>
              {{ $t('attendance.daily.lock') }}
            </ElButton>
            <ElButton v-permission="'attendance:daily:lock'" :disabled="selectedRows.length === 0" @click="handleLock(false)">
              <template #icon><icon-ep-unlock /></template>
              {{ $t('attendance.daily.unlock') }}
            </ElButton>
            <ElButton v-permission="'attendance:daily:export'" type="primary" :loading="exporting" @click="handleExport">
              <template #icon><icon-ep-download /></template>
              {{ $t('common.export') }}
            </ElButton>
            <span class="text-xs text-gray-400">{{ $t('attendance.daily.tipCheckEmployeesToCalculateTheSelectionOrCalculateBySearchFilters') }}</span>
          </div>
          <div class="flex items-center gap-8px text-sm">
            <ElTag v-for="(item, key) in statusMap" :key="key" :type="item.type as any" size="small">
              {{ item.label }}
            </ElTag>
          </div>
        </div>
      </template>

      <ElTable v-loading="loading" :data="data" border stripe size="small" @selection-change="handleSelectionChange">
        <ElTableColumn type="selection" width="40" fixed="left" />
        <ElTableColumn prop="attDate" :label="$t('common.date')" width="100" fixed="left" />
        <ElTableColumn :label="$t('attendance.daily.weekday')" width="60" align="center" fixed="left">
          <template #default="{ row }">{{ getWeekDay(row.attDate) }}</template>
        </ElTableColumn>
        <ElTableColumn prop="companyName" :label="$t('common.company')" width="100" show-overflow-tooltip fixed="left" />
        <ElTableColumn prop="employeeNo" :label="$t('common.employeeNo')" width="80" fixed="left" />
        <ElTableColumn prop="employeeName" :label="$t('common.name')" width="70" fixed="left" />
        <ElTableColumn prop="deptName" :label="$t('common.department')" width="100" show-overflow-tooltip />
        <ElTableColumn prop="shiftName" :label="$t('common.shift')" width="70" />
        <ElTableColumn :label="$t('attendance.daily.clockInsByPeriod')" min-width="400">
          <template #default="{ row }">
            <div class="flex flex-wrap gap-8px">
              <div
                v-for="p in row.periods"
                :key="p.periodId"
                class="flex items-center gap-4px border rounded px-6px py-2px text-xs"
                :class="
                  p.status === 1
                    ? 'border-green-300 bg-green-50 dark:border-green-700 dark:bg-green-900/30'
                    : p.status === 4
                      ? 'border-red-300 bg-red-50 dark:border-red-700 dark:bg-red-900/30'
                      : 'border-orange-300 bg-orange-50 dark:border-orange-700 dark:bg-orange-900/30'
                "
              >
                <span class="font-medium">{{ p.periodName }}</span>
                <span class="text-gray-400">{{ p.scheduledIn?.slice(0, 5) }}-{{ p.scheduledOut?.slice(0, 5) }}</span>
                <span class="mx-2px">|</span>
                <span :class="p.lateMinutes > 0 ? 'text-red-500' : 'text-green-600'">
                  {{ p.actualIn?.slice(0, 5) || $t('common.missingClock') }}
                </span>
                <span>-</span>
                <span :class="p.earlyMinutes > 0 ? 'text-red-500' : 'text-green-600'">
                  {{ p.actualOut?.slice(0, 5) || $t('common.missingClock') }}
                </span>
              </div>
            </div>
          </template>
        </ElTableColumn>
        <ElTableColumn prop="lateMinutes" :label="$t('common.late')" width="60" align="center">
          <template #default="{ row }">
            <span v-if="row.lateMinutes > 0" class="text-red-500">{{ row.lateMinutes }}{{ $t('attendance.daily.min') }}</span>
            <span v-else>-</span>
          </template>
        </ElTableColumn>
        <ElTableColumn prop="earlyMinutes" :label="$t('common.earlyLeave')" width="60" align="center">
          <template #default="{ row }">
            <span v-if="row.earlyMinutes > 0" class="text-red-500">{{ row.earlyMinutes }}{{ $t('attendance.daily.min') }}</span>
            <span v-else>-</span>
          </template>
        </ElTableColumn>
        <ElTableColumn prop="workHours" :label="$t('common.workHours')" width="60" align="center">
          <template #default="{ row }">{{ row.workHours || 0 }}h</template>
        </ElTableColumn>
        <ElTableColumn prop="overtimeDuration" :label="$t('common.overtime')" width="70" align="center">
          <template #default="{ row }">
            <ElTooltip v-if="row.overtimeDuration > 0" placement="top">
              <template #content>
                <div>{{ $t('common.overtime') }} {{ row.overtimeDuration }}h</div>
              </template>
              <span class="cursor-pointer text-blue-500">{{ row.overtimeDuration }}h</span>
            </ElTooltip>
            <span v-else>-</span>
          </template>
        </ElTableColumn>
        <ElTableColumn :label="$t('common.leave')" width="70" align="center">
          <template #default="{ row }">
            <ElTooltip v-if="getTotalLeaveHours(row) > 0" placement="top">
              <template #content>
                <div class="text-xs">
                  <div v-if="row.annualLeaveDuration > 0">{{ $t('attendance.daily.annualLeave') }} {{ row.annualLeaveDuration }}h</div>
                  <div v-if="row.personalLeaveDuration > 0">{{ $t('attendance.daily.personalLeave') }} {{ row.personalLeaveDuration }}h</div>
                  <div v-if="row.sickLeaveDuration > 0">{{ $t('attendance.daily.sickLeave') }} {{ row.sickLeaveDuration }}h</div>
                  <div v-if="row.marriageLeaveDuration > 0">{{ $t('attendance.daily.marriageLeave') }} {{ row.marriageLeaveDuration }}h</div>
                  <div v-if="row.maternityLeaveDuration > 0">{{ $t('attendance.daily.maternityLeave') }} {{ row.maternityLeaveDuration }}h</div>
                  <div v-if="row.paternityLeaveDuration > 0">{{ $t('attendance.daily.paternityLeave') }} {{ row.paternityLeaveDuration }}h</div>
                  <div v-if="row.bereavementLeaveDuration > 0">{{ $t('attendance.daily.bereavementLeave') }} {{ row.bereavementLeaveDuration }}h</div>
                </div>
              </template>
              <span class="cursor-pointer text-orange-500">{{ getTotalLeaveHours(row) }}h</span>
            </ElTooltip>
            <span v-else>-</span>
          </template>
        </ElTableColumn>
        <ElTableColumn prop="businessDuration" :label="$t('common.businessTrip')" width="70" align="center">
          <template #default="{ row }">
            <ElTooltip v-if="row.businessDuration > 0" placement="top">
              <template #content>
                <div>{{ $t('common.businessTrip') }} {{ row.businessDuration }}h</div>
              </template>
              <span class="cursor-pointer text-purple-500">{{ row.businessDuration }}h</span>
            </ElTooltip>
            <span v-else>-</span>
          </template>
        </ElTableColumn>
        <ElTableColumn prop="status" :label="$t('common.status')" width="85" align="center">
          <template #default="{ row }">
            <ElTag :type="statusMap[row.status]?.type as any" size="small">{{ statusMap[row.status]?.label }}</ElTag>
          </template>
        </ElTableColumn>
        <ElTableColumn :label="$t('attendance.daily.lock')" width="60" align="center">
          <template #default="{ row }">
            <icon-ep-lock v-if="row.locked === 1" class="text-orange-500" />
            <span v-else class="text-[var(--el-text-color-placeholder)]">-</span>
          </template>
        </ElTableColumn>
      </ElTable>

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
  </div>
</template>
