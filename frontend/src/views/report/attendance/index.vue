<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue';
import { ElMessage } from 'element-plus';
import type { AttDailyRecord, MonthlyAttendance } from '@/service/api/attendance';
import { fetchDailyRecordPage, fetchMonthlyAttendance } from '@/service/api/attendance';
import { fetchCompanyList, fetchDepartmentTree } from '@/service/api/organization';
import { downloadCsv, getMonthRange, resolveOrgIds } from '@/utils/report';

defineOptions({ name: 'AttendanceReport' });

type ReportTab = 'department' | 'employee' | 'abnormal';

interface DepartmentSummary {
  deptName: string;
  employees: number;
  workDays: number;
  actualDays: number;
  attendanceRate: number;
  lateTimes: number;
  earlyTimes: number;
  absentDays: number;
  leaveDays: number;
  totalWorkHours: number;
}

interface AbnormalRecord extends AttDailyRecord {
  abnormalType: string;
  occurrenceTime: string;
}

const loading = ref(false);
const activeTab = ref<ReportTab>('employee');
const currentMonth = ref(getCurrentMonth());
const companies = ref<Api.Organization.OrgUnit[]>([]);
const departments = ref<Api.Organization.OrgUnit[]>([]);
const monthlyData = ref<MonthlyAttendance[]>([]);
const dailyData = ref<AttDailyRecord[]>([]);

const employeePagination = ref({ current: 1, pageSize: 10 });
const abnormalPagination = ref({ current: 1, pageSize: 10 });

const searchParams = ref({
  companyId: undefined as number | undefined,
  deptId: undefined as number | undefined,
  employeeNo: '',
  employeeName: ''
});

watch(
  () => searchParams.value.companyId,
  async companyId => {
    searchParams.value.deptId = undefined;
    departments.value = [];

    if (companyId) {
      await loadDepartments(companyId);
    }
  }
);

const selectedOrgIds = computed<number[] | undefined>(() => {
  if (!searchParams.value.companyId && !searchParams.value.deptId) {
    return undefined;
  }

  return resolveOrgIds(departments.value, searchParams.value.deptId);
});

const overview = computed(() => {
  const employeeCount = monthlyData.value.length;
  const totalWorkDays = monthlyData.value.reduce((sum, item) => sum + Number(item.workDays || 0), 0);
  const totalActualDays = monthlyData.value.reduce((sum, item) => sum + Number(item.actualDays || 0), 0);
  const totalLate = monthlyData.value.reduce((sum, item) => sum + Number(item.lateTimes || 0), 0);
  const totalEarly = monthlyData.value.reduce((sum, item) => sum + Number(item.earlyTimes || 0), 0);
  const totalAbsent = monthlyData.value.reduce((sum, item) => sum + Number(item.absentDays || 0), 0);
  const totalLeave = monthlyData.value.reduce((sum, item) => sum + Number(item.leaveDays || 0), 0);
  const totalWorkHours = monthlyData.value.reduce((sum, item) => sum + Number(item.totalWorkHours || 0), 0);
  const abnormalCount = dailyData.value.filter(item => [2, 3, 4, 7].includes(Number(item.status))).length;
  const attendanceRate = totalWorkDays > 0 ? Number(((totalActualDays / totalWorkDays) * 100).toFixed(1)) : 0;

  return {
    employeeCount,
    totalWorkDays,
    totalActualDays,
    totalLate,
    totalEarly,
    totalAbsent,
    totalLeave,
    totalWorkHours,
    abnormalCount,
    attendanceRate
  };
});

const overviewCards = computed(() => [
  { label: '参统员工', value: overview.value.employeeCount, hint: '参与月度统计人数', tone: 'primary' },
  { label: '平均出勤率', value: `${overview.value.attendanceRate}%`, hint: '实出勤 / 应出勤', tone: 'success' },
  { label: '异常记录', value: overview.value.abnormalCount, hint: '迟到 / 早退 / 旷工', tone: 'danger' },
  { label: '总工时', value: `${overview.value.totalWorkHours.toFixed(1)} h`, hint: '所有员工工时汇总', tone: 'info' }
]);

const digestCards = computed(() => [
  { label: '迟到', value: overview.value.totalLate, tone: 'warning' },
  { label: '早退', value: overview.value.totalEarly, tone: 'info' },
  { label: '旷工', value: overview.value.totalAbsent, tone: 'danger' },
  { label: '请假', value: overview.value.totalLeave, tone: 'primary' }
]);

const departmentSummary = computed<DepartmentSummary[]>(() => {
  const map = new Map<string, DepartmentSummary>();

  for (const item of monthlyData.value) {
    const key = item.deptName || '未分配部门';
    const current =
      map.get(key) || {
        deptName: key,
        employees: 0,
        workDays: 0,
        actualDays: 0,
        attendanceRate: 0,
        lateTimes: 0,
        earlyTimes: 0,
        absentDays: 0,
        leaveDays: 0,
        totalWorkHours: 0
      };

    current.employees += 1;
    current.workDays += Number(item.workDays || 0);
    current.actualDays += Number(item.actualDays || 0);
    current.lateTimes += Number(item.lateTimes || 0);
    current.earlyTimes += Number(item.earlyTimes || 0);
    current.absentDays += Number(item.absentDays || 0);
    current.leaveDays += Number(item.leaveDays || 0);
    current.totalWorkHours += Number(item.totalWorkHours || 0);
    map.set(key, current);
  }

  return Array.from(map.values())
    .map(item => ({
      ...item,
      attendanceRate: item.workDays > 0 ? Number(((item.actualDays / item.workDays) * 100).toFixed(1)) : 0
    }))
    .sort((a, b) => {
      if (b.attendanceRate === a.attendanceRate) {
        return b.employees - a.employees;
      }

      return b.attendanceRate - a.attendanceRate;
    });
});

const topDepartments = computed(() => departmentSummary.value.slice(0, 6));

const abnormalRecords = computed<AbnormalRecord[]>(() =>
  dailyData.value
    .filter(item => [2, 3, 4, 7].includes(Number(item.status)))
    .map(item => ({
      ...item,
      abnormalType: getStatusLabel(item.status),
      occurrenceTime: getOccurrenceTime(item)
    }))
    .sort((a, b) => `${b.attDate} ${b.employeeNo || ''}`.localeCompare(`${a.attDate} ${a.employeeNo || ''}`))
);

const latestAbnormalRecords = computed(() => abnormalRecords.value.slice(0, 6));

const pagedMonthlyData = computed(() => {
  const start = (employeePagination.value.current - 1) * employeePagination.value.pageSize;

  return monthlyData.value.slice(start, start + employeePagination.value.pageSize);
});

const pagedAbnormalRecords = computed(() => {
  const start = (abnormalPagination.value.current - 1) * abnormalPagination.value.pageSize;

  return abnormalRecords.value.slice(start, start + abnormalPagination.value.pageSize);
});

async function loadBaseData() {
  const res = await fetchCompanyList();
  companies.value = res.data || [];
}

async function loadDepartments(companyId: number) {
  const res = await fetchDepartmentTree(companyId);
  departments.value = res.data || [];
}

async function fetchAllDailyRecords() {
  const orgIds = selectedOrgIds.value;

  if ((searchParams.value.companyId || searchParams.value.deptId) && (!orgIds || orgIds.length === 0)) {
    return [];
  }

  const { startDate, endDate } = getMonthRange(currentMonth.value);
  const records: AttDailyRecord[] = [];
  const pageSize = 200;
  let page = 1;

  while (true) {
    // eslint-disable-next-line no-await-in-loop
    const res = await fetchDailyRecordPage({
      page,
      size: pageSize,
      startDate,
      endDate,
      orgIds,
      employeeNo: searchParams.value.employeeNo || undefined,
      employeeName: searchParams.value.employeeName || undefined
    });

    const pageData = res.data;
    const chunk = pageData?.records || [];

    records.push(...chunk);

    if (!pageData || records.length >= (pageData.total || 0) || chunk.length < pageSize) {
      break;
    }

    page += 1;
  }

  return records;
}

async function loadData() {
  const orgIds = selectedOrgIds.value;

  if ((searchParams.value.companyId || searchParams.value.deptId) && (!orgIds || orgIds.length === 0)) {
    monthlyData.value = [];
    dailyData.value = [];
    return;
  }

  loading.value = true;

  try {
    const [monthlyRes, dailyRes] = await Promise.all([
      fetchMonthlyAttendance({
        month: currentMonth.value,
        orgIds,
        employeeNo: searchParams.value.employeeNo || undefined,
        employeeName: searchParams.value.employeeName || undefined
      }),
      fetchAllDailyRecords()
    ]);

    monthlyData.value = (monthlyRes.data || []).sort((a, b) => {
      if ((a.deptName || '') === (b.deptName || '')) {
        return (a.employeeNo || '').localeCompare(b.employeeNo || '');
      }

      return (a.deptName || '').localeCompare(b.deptName || '');
    });
    dailyData.value = dailyRes;
    employeePagination.value.current = 1;
    abnormalPagination.value.current = 1;
  } finally {
    loading.value = false;
  }
}

function handleSearch() {
  loadData();
}

function handleReset() {
  currentMonth.value = getCurrentMonth();
  searchParams.value = {
    companyId: undefined,
    deptId: undefined,
    employeeNo: '',
    employeeName: ''
  };
  departments.value = [];
  loadData();
}

function handleMonthChange() {
  loadData();
}

function handleEmployeePageChange(page: number) {
  employeePagination.value.current = page;
}

function handleEmployeeSizeChange(size: number) {
  employeePagination.value.pageSize = size;
  employeePagination.value.current = 1;
}

function handleAbnormalPageChange(page: number) {
  abnormalPagination.value.current = page;
}

function handleAbnormalSizeChange(size: number) {
  abnormalPagination.value.pageSize = size;
  abnormalPagination.value.current = 1;
}

function handleExport() {
  if (!monthlyData.value.length) {
    ElMessage.warning('暂无可导出的考勤数据');
    return;
  }

  downloadCsv(
    `考勤报表_${currentMonth.value}_${getExportStamp()}.csv`,
    [
      { title: '月份', key: 'month' },
      { title: '公司', key: 'companyName' },
      { title: '部门', key: 'deptName' },
      { title: '工号', key: 'employeeNo' },
      { title: '姓名', key: 'employeeName' },
      { title: '应出勤天数', key: 'workDays' },
      { title: '实出勤天数', key: 'actualDays' },
      { title: '出勤率', key: 'attendanceRate', formatter: row => formatRate(row.actualDays, row.workDays) },
      { title: '迟到次数', key: 'lateTimes' },
      { title: '迟到分钟', key: 'totalLateMinutes' },
      { title: '早退次数', key: 'earlyTimes' },
      { title: '早退分钟', key: 'totalEarlyMinutes' },
      { title: '旷工天数', key: 'absentDays' },
      { title: '请假天数', key: 'leaveDays' },
      { title: '总工时', key: 'totalWorkHours' }
    ],
    monthlyData.value.map(item => ({
      ...item,
      attendanceRate: formatRate(item.actualDays, item.workDays)
    }))
  );

  ElMessage.success('考勤报表已导出');
}

function getCurrentMonth() {
  const now = new Date();

  return `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}`;
}

function formatRate(actual: number, planned: number) {
  if (!planned) {
    return '0%';
  }

  return `${((actual / planned) * 100).toFixed(1)}%`;
}

function getStatusLabel(status?: number) {
  const map: Record<number, string> = {
    1: '正常',
    2: '迟到',
    3: '早退',
    4: '旷工',
    5: '请假',
    6: '出差',
    7: '迟到+早退'
  };

  return status ? map[status] || '异常' : '异常';
}

function getStatusTagType(status?: number) {
  if (status === 4 || status === 7) return 'danger';
  if (status === 2 || status === 3) return 'warning';
  return 'info';
}

function getOccurrenceTime(record: AttDailyRecord) {
  if (record.status === 2) return record.actualIn || '-';
  if (record.status === 3) return record.actualOut || '-';
  if (record.status === 7) {
    const parts = [record.actualIn, record.actualOut].filter(Boolean);

    return parts.length > 0 ? parts.join(' / ') : '-';
  }

  return '-';
}

function getExportStamp() {
  const now = new Date();

  return `${now.getFullYear()}${String(now.getMonth() + 1).padStart(2, '0')}${String(now.getDate()).padStart(2, '0')}_${String(now.getHours()).padStart(2, '0')}${String(now.getMinutes()).padStart(2, '0')}`;
}

onMounted(async () => {
  await loadBaseData();
  await loadData();
});
</script>

<template>
  <div class="attendance-report-page flex-col-stretch gap-16px">
    <ElCard class="search-card">
      <ElForm inline :model="searchParams">
        <ElFormItem label="月份">
          <ElDatePicker
            v-model="currentMonth"
            type="month"
            placeholder="请选择月份"
            value-format="YYYY-MM"
            style="width: 150px"
            @change="handleMonthChange"
          />
        </ElFormItem>
        <ElFormItem label="公司">
          <ElSelect v-model="searchParams.companyId" placeholder="请选择公司" clearable style="width: 170px">
            <ElOption v-for="company in companies" :key="company.id" :label="company.unitName" :value="company.id" />
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
            style="width: 200px"
          />
        </ElFormItem>
        <ElFormItem label="工号">
          <ElInput v-model="searchParams.employeeNo" placeholder="请输入工号" clearable style="width: 140px" />
        </ElFormItem>
        <ElFormItem label="姓名">
          <ElInput v-model="searchParams.employeeName" placeholder="请输入姓名" clearable style="width: 140px" />
        </ElFormItem>
        <ElFormItem>
          <ElButton type="primary" :loading="loading" @click="handleSearch">
            <template #icon><icon-ep-search /></template>
            更新报表
          </ElButton>
          <ElButton @click="handleReset">
            <template #icon><icon-ep-refresh /></template>
            重置
          </ElButton>
          <ElButton type="success" :disabled="loading || monthlyData.length === 0" @click="handleExport">
            <template #icon><icon-ep-download /></template>
            导出月报
          </ElButton>
        </ElFormItem>
      </ElForm>
    </ElCard>

    <div class="overview-grid grid grid-cols-4 gap-16px lt-lg:grid-cols-2 lt-sm:grid-cols-1">
      <ElCard v-for="card in overviewCards" :key="card.label" shadow="hover" class="stat-card" :class="`stat-card--${card.tone}`">
        <div class="stat-card__label">{{ card.label }}</div>
        <div class="stat-card__value">{{ card.value }}</div>
        <div class="stat-card__hint">{{ card.hint }}</div>
      </ElCard>
    </div>

    <div class="insight-grid grid grid-cols-2 gap-16px lt-xl:grid-cols-1">
      <ElCard>
        <template #header>
          <div class="section-head">
            <span>部门表现</span>
            <span class="section-head__meta">出勤率前 6</span>
          </div>
        </template>
        <div v-if="topDepartments.length" class="ranking-list">
          <div v-for="(item, index) in topDepartments" :key="item.deptName" class="ranking-item">
            <div class="ranking-item__top">
              <div class="flex items-center gap-10px min-w-0">
                <span class="ranking-item__index">{{ index + 1 }}</span>
                <span class="font-600 truncate">{{ item.deptName }}</span>
              </div>
              <span class="text-emerald-700 font-700">{{ item.attendanceRate }}%</span>
            </div>
            <div class="ranking-item__meta">
              <span>{{ item.employees }} 人</span>
              <span>迟到 {{ item.lateTimes }}</span>
              <span>旷工 {{ item.absentDays }}</span>
            </div>
            <ElProgress
              :percentage="item.attendanceRate"
              :stroke-width="10"
              :status="item.attendanceRate >= 95 ? 'success' : item.attendanceRate >= 90 ? '' : 'exception'"
            />
          </div>
        </div>
        <ElEmpty v-else description="暂无部门数据" :image-size="88" />
      </ElCard>

      <ElCard>
        <template #header>
          <div class="section-head">
            <span>异常洞察</span>
            <span class="section-head__meta">最新异常与分布</span>
          </div>
        </template>

        <div class="digest-grid">
          <div v-for="item in digestCards" :key="item.label" class="digest-card" :class="`digest-card--${item.tone}`">
            <span class="text-12px text-[var(--el-text-color-secondary)]">{{ item.label }}</span>
            <strong class="text-24px leading-none">{{ item.value }}</strong>
          </div>
        </div>

        <div v-if="latestAbnormalRecords.length" class="latest-list">
          <div v-for="item in latestAbnormalRecords" :key="`${item.id}-${item.attDate}`" class="latest-item">
            <div class="flex items-center gap-10px">
              <ElTag :type="getStatusTagType(item.status)" size="small">{{ item.abnormalType }}</ElTag>
              <span class="font-600">{{ item.employeeName || '-' }}</span>
              <span class="text-12px text-[var(--el-text-color-secondary)]">{{ item.employeeNo || '-' }}</span>
            </div>
            <div class="latest-item__meta">
              <span>{{ item.attDate }}</span>
              <span>{{ item.deptName || '未分配部门' }}</span>
              <span>{{ item.occurrenceTime }}</span>
            </div>
          </div>
        </div>
        <ElEmpty v-else description="暂无异常记录" :image-size="88" />
      </ElCard>
    </div>

    <ElCard>
      <template #header>
        <div class="section-head">
          <span>明细视图</span>
          <span class="section-head__meta">切换部门、员工和异常三个视角</span>
        </div>
      </template>

      <ElTabs v-model="activeTab">
        <ElTabPane label="部门汇总" name="department">
          <div class="table-wrapper">
            <ElTable v-loading="loading" :data="departmentSummary" border stripe size="small" class="table-content table-content--department">
              <ElTableColumn prop="deptName" label="部门" min-width="180" show-overflow-tooltip />
              <ElTableColumn prop="employees" label="员工数" width="90" align="center" />
              <ElTableColumn prop="workDays" label="应出勤" width="90" align="center" />
              <ElTableColumn prop="actualDays" label="实出勤" width="90" align="center" />
              <ElTableColumn prop="attendanceRate" label="出勤率" width="110" align="center">
                <template #default="{ row }">{{ row.attendanceRate }}%</template>
              </ElTableColumn>
              <ElTableColumn prop="lateTimes" label="迟到" width="80" align="center" />
              <ElTableColumn prop="earlyTimes" label="早退" width="80" align="center" />
              <ElTableColumn prop="absentDays" label="旷工" width="80" align="center" />
              <ElTableColumn prop="leaveDays" label="请假" width="80" align="center" />
              <ElTableColumn prop="totalWorkHours" label="总工时" width="100" align="center">
                <template #default="{ row }">{{ row.totalWorkHours.toFixed(1) }}h</template>
              </ElTableColumn>
            </ElTable>
          </div>
        </ElTabPane>

        <ElTabPane label="员工月报" name="employee">
          <div class="table-wrapper">
            <ElTable v-loading="loading" :data="pagedMonthlyData" border stripe size="small" class="table-content table-content--monthly">
              <ElTableColumn prop="companyName" label="公司" width="150" show-overflow-tooltip />
              <ElTableColumn prop="deptName" label="部门" width="160" show-overflow-tooltip />
              <ElTableColumn prop="employeeNo" label="工号" width="110" />
              <ElTableColumn prop="employeeName" label="姓名" width="100" />
              <ElTableColumn prop="workDays" label="应出勤" width="88" align="center" />
              <ElTableColumn prop="actualDays" label="实出勤" width="88" align="center" />
              <ElTableColumn label="出勤率" width="100" align="center">
                <template #default="{ row }">{{ formatRate(row.actualDays, row.workDays) }}</template>
              </ElTableColumn>
              <ElTableColumn prop="lateTimes" label="迟到" width="72" align="center" />
              <ElTableColumn prop="totalLateMinutes" label="迟到分" width="86" align="center" />
              <ElTableColumn prop="earlyTimes" label="早退" width="72" align="center" />
              <ElTableColumn prop="totalEarlyMinutes" label="早退分" width="86" align="center" />
              <ElTableColumn prop="absentDays" label="旷工" width="72" align="center" />
              <ElTableColumn prop="leaveDays" label="请假" width="72" align="center" />
              <ElTableColumn prop="totalWorkHours" label="总工时" width="90" align="center">
                <template #default="{ row }">{{ Number(row.totalWorkHours || 0).toFixed(1) }}h</template>
              </ElTableColumn>
            </ElTable>
          </div>

          <div class="mt-16px flex justify-end">
            <ElPagination
              v-model:current-page="employeePagination.current"
              v-model:page-size="employeePagination.pageSize"
              :page-sizes="[10, 20, 50, 100]"
              :total="monthlyData.length"
              layout="total, sizes, prev, pager, next"
              @current-change="handleEmployeePageChange"
              @size-change="handleEmployeeSizeChange"
            />
          </div>
        </ElTabPane>

        <ElTabPane label="异常明细" name="abnormal">
          <div class="table-wrapper">
            <ElTable v-loading="loading" :data="pagedAbnormalRecords" border stripe size="small" class="table-content table-content--abnormal">
              <ElTableColumn type="index" label="#" width="54" align="center" />
              <ElTableColumn prop="attDate" label="日期" width="120" />
              <ElTableColumn prop="companyName" label="公司" width="140" show-overflow-tooltip />
              <ElTableColumn prop="deptName" label="部门" width="150" show-overflow-tooltip />
              <ElTableColumn prop="employeeNo" label="工号" width="110" />
              <ElTableColumn prop="employeeName" label="姓名" width="100" />
              <ElTableColumn prop="abnormalType" label="异常类型" width="110" align="center">
                <template #default="{ row }">
                  <ElTag :type="getStatusTagType(row.status)" size="small">{{ row.abnormalType }}</ElTag>
                </template>
              </ElTableColumn>
              <ElTableColumn prop="occurrenceTime" label="相关时间" width="180" show-overflow-tooltip />
              <ElTableColumn prop="remark" label="备注" min-width="180" show-overflow-tooltip>
                <template #default="{ row }">{{ row.remark || '-' }}</template>
              </ElTableColumn>
            </ElTable>
          </div>

          <div class="mt-16px flex justify-end">
            <ElPagination
              v-model:current-page="abnormalPagination.current"
              v-model:page-size="abnormalPagination.pageSize"
              :page-sizes="[10, 20, 50, 100]"
              :total="abnormalRecords.length"
              layout="total, sizes, prev, pager, next"
              @current-change="handleAbnormalPageChange"
              @size-change="handleAbnormalSizeChange"
            />
          </div>
        </ElTabPane>
      </ElTabs>
    </ElCard>
  </div>
</template>

<style scoped>
.attendance-report-page {
  min-height: 500px;
  overflow-x: hidden;
  padding-bottom: 16px;
}

.attendance-report-page > * {
  flex-shrink: 0;
}

.search-card {
  flex-shrink: 0;
}

.attendance-report-page :deep(.el-form) {
  display: flex;
  flex-wrap: wrap;
  align-items: flex-start;
}

.attendance-report-page :deep(.el-form-item) {
  margin-right: 12px;
  margin-bottom: 12px;
}

.attendance-report-page :deep(.el-card__body) {
  height: auto;
  min-height: 0;
}

.overview-grid > *,
.insight-grid > * {
  min-width: 0;
}

.stat-card {
  position: relative;
  overflow: hidden;
}

.stat-card::after {
  position: absolute;
  right: -24px;
  bottom: -28px;
  width: 84px;
  height: 84px;
  border-radius: 999px;
  content: '';
  opacity: 0.1;
}

.stat-card__label {
  color: var(--el-text-color-secondary);
  font-size: 13px;
}

.stat-card__value {
  margin-top: 14px;
  font-size: 30px;
  font-weight: 700;
  line-height: 1;
}

.stat-card__hint {
  margin-top: 12px;
  color: var(--el-text-color-secondary);
  font-size: 12px;
}

.stat-card--primary::after {
  background: #3b82f6;
}

.stat-card--success::after {
  background: #10b981;
}

.stat-card--danger::after {
  background: #ef4444;
}

.stat-card--info::after {
  background: #06b6d4;
}

.section-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  flex-wrap: wrap;
}

.section-head__meta {
  color: var(--el-text-color-secondary);
  font-size: 12px;
  white-space: nowrap;
}

.ranking-list,
.latest-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.ranking-item,
.latest-item {
  min-width: 0;
  padding: 14px 16px;
  background: #f8fafc;
  border: 1px solid rgba(148, 163, 184, 0.18);
  border-radius: 18px;
}

.ranking-item__top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.ranking-item__index {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 26px;
  height: 26px;
  color: #fff;
  background: #0f766e;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 700;
}

.ranking-item__meta,
.latest-item__meta {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-top: 10px;
  color: var(--el-text-color-secondary);
  font-size: 12px;
}

.digest-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px;
  margin-bottom: 16px;
}

.digest-card {
  min-width: 0;
  padding: 16px;
  border-radius: 18px;
  border: 1px solid rgba(148, 163, 184, 0.18);
}

.digest-card--warning {
  background: rgba(245, 158, 11, 0.08);
}

.digest-card--info {
  background: rgba(6, 182, 212, 0.08);
}

.digest-card--danger {
  background: rgba(239, 68, 68, 0.08);
}

.digest-card--primary {
  background: rgba(59, 130, 246, 0.08);
}

.attendance-report-page :deep(.el-tabs__content) {
  min-width: 0;
  overflow: visible;
}

.attendance-report-page :deep(.el-tab-pane) {
  overflow: visible;
}

.table-wrapper {
  width: 100%;
  min-width: 0;
  overflow-x: auto;
  overflow-y: hidden;
}

.table-content {
  width: 100%;
}

.table-content--department {
  min-width: 1080px;
}

.table-content--monthly {
  min-width: 1280px;
}

.table-content--abnormal {
  min-width: 1180px;
}

@media (max-width: 1200px) {
  .digest-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 768px) {
  .digest-grid {
    grid-template-columns: 1fr;
  }
}
</style>



