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
  0: { label: '未处理', type: 'info' },
  1: { label: '正常', type: 'success' },
  2: { label: '迟到', type: 'warning' },
  3: { label: '早退', type: 'warning' },
  4: { label: '旷工', type: 'danger' },
  5: { label: '请假', type: 'info' },
  6: { label: '出差', type: 'primary' },
  7: { label: '迟到+早退', type: 'danger' }
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

const weekDays = ['日', '一', '二', '三', '四', '五', '六'];
function getWeekDay(dateStr: string) {
  if (!dateStr) return '';
  return `周${weekDays[dayjs(dateStr).day()]}`;
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
    return `计算选中 ${selectedRows.value.length} 人`;
  }
  const filters: string[] = [];
  if (searchParams.value.companyId) {
    const company = companies.value.find(c => c.id === searchParams.value.companyId);
    if (company) filters.push(company.unitName || company.companyName);
  }
  if (searchParams.value.deptId) {
    filters.push('指定部门');
  }
  if (searchParams.value.employeeNo) {
    filters.push(`工号:${searchParams.value.employeeNo}`);
  }
  if (searchParams.value.employeeName) {
    filters.push(`姓名:${searchParams.value.employeeName}`);
  }
  if (filters.length > 0) {
    return `按条件计算`;
  }
  return '计算全部员工';
}

// 计算提示信息
function getCalculateTooltip() {
  if (selectedRows.value.length > 0) {
    return `将计算选中的 ${selectedRows.value.length} 名员工的考勤`;
  }
  const filters: string[] = [];
  if (searchParams.value.companyId) {
    const company = companies.value.find(c => c.id === searchParams.value.companyId);
    if (company) filters.push(`公司: ${company.unitName || company.companyName}`);
  }
  if (searchParams.value.deptId) {
    filters.push('指定部门');
  }
  if (searchParams.value.employeeNo) {
    filters.push(`工号包含: ${searchParams.value.employeeNo}`);
  }
  if (searchParams.value.employeeName) {
    filters.push(`姓名包含: ${searchParams.value.employeeName}`);
  }
  if (filters.length > 0) {
    return `将按以下条件筛选员工计算:\n${filters.join('\n')}`;
  }
  return '将计算所有在职员工的考勤';
}

async function handleCalculate() {
  if (!searchParams.value.dateRange?.[0] || !searchParams.value.dateRange?.[1]) {
    ElMessage.warning('请选择日期范围');
    return;
  }
  try {
    await ElMessageBox.confirm(`${getCalculateTooltip().replace(/\n/g, ' ')}，确认执行考勤计算吗？`, '考勤计算确认', {
      type: 'warning',
      confirmButtonText: '确认计算',
      cancelButtonText: '取消'
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
    ElMessage.success('考勤计算完成');
    loadData();
  } catch {
    ElMessage.error('考勤计算失败');
  } finally {
    calculating.value = false;
  }
}

async function handleLock(lock: boolean) {
  if (selectedRows.value.length === 0) {
    ElMessage.warning('请先选择要操作的记录');
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
    ElMessage.warning('没有可操作的记录');
    return;
  }
  try {
    await ElMessageBox.confirm(
      `确认${lock ? '锁定' : '解锁'}选中的 ${selectedRows.value.length} 条考勤记录吗？`,
      lock ? '锁定确认' : '解锁确认',
      {
        type: 'warning',
        confirmButtonText: '确认',
        cancelButtonText: '取消'
      }
    );
  } catch {
    return;
  }
  try {
    await lockDailyRecords({ ids, lock });
    ElMessage.success(lock ? '锁定成功' : '解锁成功');
    loadData();
  } catch {
    ElMessage.error(lock ? '锁定失败' : '解锁失败');
  }
}

/** 导出日考勤记录 */
async function handleExport() {
  if (!searchParams.value.dateRange?.[0] || !searchParams.value.dateRange?.[1]) {
    ElMessage.warning('请选择日期范围');
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
    await downloadFile('/attendance/daily/export', `日考勤记录_${startDate}_${endDate}.xlsx`, {
      startDate,
      endDate,
      orgIds: orgIds?.join(','),
      employeeNo: searchParams.value.employeeNo,
      employeeName: searchParams.value.employeeName,
      status: searchParams.value.status
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
        <ElFormItem label="日期范围">
          <ElDatePicker
            v-model="searchParams.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始"
            end-placeholder="结束"
            value-format="YYYY-MM-DD"
            style="width: 240px"
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
        <ElFormItem label="状态">
          <ElSelect v-model="searchParams.status" placeholder="全部" clearable style="width: 100px">
            <ElOption v-for="(item, key) in statusMap" :key="key" :label="item.label" :value="Number(key)" />
          </ElSelect>
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
          <div class="flex items-center gap-12px">
            <span>日考勤记录</span>
            <ElTooltip :content="getCalculateTooltip()" placement="top">
              <ElButton type="success" :loading="calculating" @click="handleCalculate">
                <template #icon><icon-ep-refresh /></template>
                {{ getCalculateButtonText() }}
              </ElButton>
            </ElTooltip>
            <ElButton type="warning" :disabled="selectedRows.length === 0" @click="handleLock(true)">
              <template #icon><icon-ep-lock /></template>
              锁定
            </ElButton>
            <ElButton :disabled="selectedRows.length === 0" @click="handleLock(false)">
              <template #icon><icon-ep-unlock /></template>
              解锁
            </ElButton>
            <ElButton type="primary" :loading="exporting" @click="handleExport">
              <template #icon><icon-ep-download /></template>
              导出
            </ElButton>
            <span class="text-xs text-gray-400">提示: 勾选员工计算选中，或按搜索条件筛选计算</span>
          </div>
          <div class="flex items-center gap-8px text-sm">
            <ElTag v-for="(item, key) in statusMap" :key="key" :type="item.type as any" size="small">
              {{ item.label }}
            </ElTag>
          </div>
        </div>
      </template>

      <ElTable v-loading="loading" :data="data" border stripe size="small" @selection-change="handleSelectionChange">
        <ElTableColumn type="selection" width="40" />
        <ElTableColumn prop="attDate" label="日期" width="100" />
        <ElTableColumn label="星期" width="60" align="center">
          <template #default="{ row }">{{ getWeekDay(row.attDate) }}</template>
        </ElTableColumn>
        <ElTableColumn prop="companyName" label="公司" width="100" show-overflow-tooltip />
        <ElTableColumn prop="employeeNo" label="工号" width="80" />
        <ElTableColumn prop="employeeName" label="姓名" width="70" />
        <ElTableColumn prop="deptName" label="部门" width="100" show-overflow-tooltip />
        <ElTableColumn prop="shiftName" label="班次" width="70" />
        <ElTableColumn label="各时段打卡" min-width="400">
          <template #default="{ row }">
            <div class="flex flex-wrap gap-8px">
              <div
                v-for="p in row.periods"
                :key="p.periodId"
                class="flex items-center gap-4px border rounded px-6px py-2px text-xs"
                :class="
                  p.status === 1
                    ? 'border-green-300 bg-green-50'
                    : p.status === 4
                      ? 'border-red-300 bg-red-50'
                      : 'border-orange-300 bg-orange-50'
                "
              >
                <span class="font-medium">{{ p.periodName }}</span>
                <span class="text-gray-400">{{ p.scheduledIn?.slice(0, 5) }}-{{ p.scheduledOut?.slice(0, 5) }}</span>
                <span class="mx-2px">|</span>
                <span :class="p.lateMinutes > 0 ? 'text-red-500' : 'text-green-600'">
                  {{ p.actualIn?.slice(0, 5) || '缺卡' }}
                </span>
                <span>-</span>
                <span :class="p.earlyMinutes > 0 ? 'text-red-500' : 'text-green-600'">
                  {{ p.actualOut?.slice(0, 5) || '缺卡' }}
                </span>
              </div>
            </div>
          </template>
        </ElTableColumn>
        <ElTableColumn prop="lateMinutes" label="迟到" width="60" align="center">
          <template #default="{ row }">
            <span v-if="row.lateMinutes > 0" class="text-red-500">{{ row.lateMinutes }}分</span>
            <span v-else>-</span>
          </template>
        </ElTableColumn>
        <ElTableColumn prop="earlyMinutes" label="早退" width="60" align="center">
          <template #default="{ row }">
            <span v-if="row.earlyMinutes > 0" class="text-red-500">{{ row.earlyMinutes }}分</span>
            <span v-else>-</span>
          </template>
        </ElTableColumn>
        <ElTableColumn prop="workHours" label="工时" width="60" align="center">
          <template #default="{ row }">{{ row.workHours || 0 }}h</template>
        </ElTableColumn>
        <ElTableColumn prop="overtimeDuration" label="加班" width="70" align="center">
          <template #default="{ row }">
            <ElTooltip v-if="row.overtimeDuration > 0" placement="top">
              <template #content>
                <div>加班 {{ row.overtimeDuration }}h</div>
              </template>
              <span class="cursor-pointer text-blue-500">{{ row.overtimeDuration }}h</span>
            </ElTooltip>
            <span v-else>-</span>
          </template>
        </ElTableColumn>
        <ElTableColumn label="请假" width="70" align="center">
          <template #default="{ row }">
            <ElTooltip v-if="getTotalLeaveHours(row) > 0" placement="top">
              <template #content>
                <div class="text-xs">
                  <div v-if="row.annualLeaveDuration > 0">年假: {{ row.annualLeaveDuration }}h</div>
                  <div v-if="row.personalLeaveDuration > 0">事假: {{ row.personalLeaveDuration }}h</div>
                  <div v-if="row.sickLeaveDuration > 0">病假: {{ row.sickLeaveDuration }}h</div>
                  <div v-if="row.marriageLeaveDuration > 0">婚假: {{ row.marriageLeaveDuration }}h</div>
                  <div v-if="row.maternityLeaveDuration > 0">产假: {{ row.maternityLeaveDuration }}h</div>
                  <div v-if="row.paternityLeaveDuration > 0">陪产假: {{ row.paternityLeaveDuration }}h</div>
                  <div v-if="row.bereavementLeaveDuration > 0">丧假: {{ row.bereavementLeaveDuration }}h</div>
                </div>
              </template>
              <span class="cursor-pointer text-orange-500">{{ getTotalLeaveHours(row) }}h</span>
            </ElTooltip>
            <span v-else>-</span>
          </template>
        </ElTableColumn>
        <ElTableColumn prop="businessDuration" label="出差" width="70" align="center">
          <template #default="{ row }">
            <ElTooltip v-if="row.businessDuration > 0" placement="top">
              <template #content>
                <div>出差 {{ row.businessDuration }}h</div>
              </template>
              <span class="cursor-pointer text-purple-500">{{ row.businessDuration }}h</span>
            </ElTooltip>
            <span v-else>-</span>
          </template>
        </ElTableColumn>
        <ElTableColumn prop="status" label="状态" width="85" align="center">
          <template #default="{ row }">
            <ElTag :type="statusMap[row.status]?.type as any" size="small">{{ statusMap[row.status]?.label }}</ElTag>
          </template>
        </ElTableColumn>
        <ElTableColumn label="锁定" width="60" align="center">
          <template #default="{ row }">
            <icon-ep-lock v-if="row.locked === 1" class="text-orange-500" />
            <span v-else class="text-gray-300">-</span>
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
