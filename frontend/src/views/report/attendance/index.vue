<script setup lang="ts">
import { computed, nextTick, onMounted, ref, watch } from 'vue';
import { ElMessage } from 'element-plus';
import type { AttDailyRecord, MonthlyAttendance } from '@/service/api/attendance';
import { fetchDailyRecordPage, fetchMonthlyAttendance } from '@/service/api/attendance';
import { fetchCompanyList, fetchDepartmentTree } from '@/service/api/organization';
import { type AttendanceReportSummary, fetchAttendanceReportSummary } from '@/service/api/report';
import { useEcharts } from '@/hooks/common/echarts';
import { downloadCsv, getMonthRange, normalizeRateValue, resolveOrgIds } from '@/utils/report';
import { $t } from '@/locales';

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

/** 部门排行行（后端接口仅提供部分字段） */
interface RankingRow {
  deptName: string;
  employees?: number;
  attendanceRate: number;
  lateTimes: number;
  absentDays: number;
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

/** 后端统计接口数据：优先使用，接口失败或返回空时回退到前端全量计算 */
const attendanceSummary = ref<AttendanceReportSummary | null>(null);

const overview = computed(() => {
  const employeeCount = monthlyData.value.length;
  const totalWorkHours = monthlyData.value.reduce((sum, item) => sum + Number(item.totalWorkHours || 0), 0);
  const abnormalCount = dailyData.value.filter(item => [2, 3, 4, 7].includes(Number(item.status))).length;

  // 优先使用后端统计接口数据（出勤率做空值/格式兼容）
  const apiOverview = attendanceSummary.value?.overview;
  if (apiOverview) {
    return {
      employeeCount,
      totalWorkDays: Number(apiOverview.workDays || 0),
      totalActualDays: Number(apiOverview.actualDays || 0),
      totalLate: Number(apiOverview.lateTimes || 0),
      totalEarly: Number(apiOverview.earlyTimes || 0),
      totalAbsent: Number(apiOverview.absentDays || 0),
      totalLeave: Number(apiOverview.leaveDays || 0),
      totalWorkHours,
      abnormalCount,
      attendanceRate: normalizeRateValue(apiOverview.attendanceRate)
    };
  }

  // 回退：前端全量计算
  const totalWorkDays = monthlyData.value.reduce((sum, item) => sum + Number(item.workDays || 0), 0);
  const totalActualDays = monthlyData.value.reduce((sum, item) => sum + Number(item.actualDays || 0), 0);
  const totalLate = monthlyData.value.reduce((sum, item) => sum + Number(item.lateTimes || 0), 0);
  const totalEarly = monthlyData.value.reduce((sum, item) => sum + Number(item.earlyTimes || 0), 0);
  const totalAbsent = monthlyData.value.reduce((sum, item) => sum + Number(item.absentDays || 0), 0);
  const totalLeave = monthlyData.value.reduce((sum, item) => sum + Number(item.leaveDays || 0), 0);
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
  { label: $t('report.attendance.employeesInStatistics'), value: overview.value.employeeCount, hint: $t('report.attendance.employeesInMonthlyStatistics'), tone: 'primary' },
  { label: $t('report.attendance.averageAttendanceRate'), value: `${overview.value.attendanceRate}%`, hint: $t('report.attendance.actualRequiredAttendance'), tone: 'success' },
  { label: $t('report.attendance.abnormalRecords'), value: overview.value.abnormalCount, hint: $t('report.attendance.lateEarlyLeaveAbsent'), tone: 'danger' },
  { label: $t('attendance.monthly.totalWorkHours'), value: `${overview.value.totalWorkHours.toFixed(1)} h`, hint: $t('report.attendance.workHoursSummaryOfAllEmployees'), tone: 'info' }
]);

const digestCards = computed(() => [
  { label: $t('common.late'), value: overview.value.totalLate, tone: 'warning' },
  { label: $t('common.earlyLeave'), value: overview.value.totalEarly, tone: 'info' },
  { label: $t('common.absent'), value: overview.value.totalAbsent, tone: 'danger' },
  { label: $t('common.leave'), value: overview.value.totalLeave, tone: 'primary' }
]);

const departmentSummary = computed<DepartmentSummary[]>(() => {
  const map = new Map<string, DepartmentSummary>();

  for (const item of monthlyData.value) {
    const key = item.deptName || $t('report.attendance.unassignedDepartment');
    const current = map.get(key) || {
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

/** 后端部门排行（接口字段：deptName/attendanceRate/lateTimes/absentDays），无数据时为 null */
const apiDeptRanking = computed<RankingRow[] | null>(() => {
  const rows = attendanceSummary.value?.deptRanking;
  if (!rows?.length) {
    return null;
  }
  return rows.map(item => ({
    deptName: item.deptName,
    attendanceRate: normalizeRateValue(item.attendanceRate),
    lateTimes: Number(item.lateTimes || 0),
    absentDays: Number(item.absentDays || 0)
  }));
});

const topDepartments = computed<RankingRow[]>(() => {
  // 优先使用后端统计接口数据
  if (apiDeptRanking.value) {
    return apiDeptRanking.value.slice(0, 6);
  }
  return departmentSummary.value.slice(0, 6);
});

const hasDeptChart = computed(() => departmentSummary.value.length > 0 || Boolean(apiDeptRanking.value));

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

// ==================== 部门表现 - 横向条形图 ====================

const { domRef: deptChartRef, updateOptions: updateDeptChartOptions } = useEcharts(() => ({
  tooltip: {
    trigger: 'axis',
    axisPointer: { type: 'shadow' },
    formatter: '{b}: {c}%'
  },
  grid: { left: '2%', right: '8%', top: '10px', bottom: '3%', containLabel: true },
  xAxis: {
    type: 'value',
    max: 100,
    axisLabel: { fontSize: 12, formatter: '{value}%' },
    splitLine: { lineStyle: { type: 'dashed' } }
  },
  yAxis: {
    type: 'category',
    data: [] as string[],
    axisTick: { show: false },
    axisLine: { show: false },
    axisLabel: { fontSize: 12 }
  },
  series: [
    {
      name: $t('report.attendance.attendanceRate'),
      type: 'bar',
      barWidth: '55%',
      itemStyle: {
        color: {
          type: 'linear',
          x: 0,
          y: 0,
          x2: 1,
          y2: 0,
          colorStops: [
            { offset: 0, color: '#6366f1' },
            { offset: 1, color: '#22d3ee' }
          ]
        },
        borderRadius: [0, 4, 4, 0]
      },
      label: { show: true, position: 'right', fontSize: 12, formatter: '{c}%' },
      data: [] as number[]
    }
  ]
}));

async function renderDeptChart() {
  // 等待 v-if 容器挂载完成后再渲染，避免首次加载图表容器不存在导致空白
  await nextTick();
  if (!deptChartRef.value) {
    return;
  }
  // 优先使用后端部门排行数据；横向条形图类目轴自下而上，倒序使出勤率最高的部门显示在顶部
  const source = apiDeptRanking.value ?? departmentSummary.value;
  const rows = source.slice(0, 10).reverse();
  await updateDeptChartOptions(opts => {
    const yAxis = opts.yAxis as { data: string[] };
    const series = opts.series as { data: number[] }[];
    yAxis.data = rows.map(item => item.deptName);
    series[0].data = rows.map(item => item.attendanceRate);
    return opts;
  });
}

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
  // 分页循环上限保护：最多 100 页 / 2 万行，防止大范围查询产生过多请求拖垮页面
  const MAX_PAGES = 100;
  const MAX_ROWS = 20000;
  let truncated = false;

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

    if (page > MAX_PAGES || records.length >= MAX_ROWS) {
      truncated = true;
      break;
    }
  }

  if (truncated) {
    ElMessage.warning($t('report.attendance.tooMuchAttendanceDataOnlyTheFirst20000DetailsAreCountedNarrowTheTimeRangeOrAddFilters'));
  }

  return records;
}

/** 加载后端统计接口（失败时置空，回退前端计算） */
async function loadSummary() {
  try {
    const res = await fetchAttendanceReportSummary({
      month: currentMonth.value,
      companyId: searchParams.value.companyId,
      deptId: searchParams.value.deptId
    });
    attendanceSummary.value = res.data && res.data.overview ? res.data : null;
  } catch {
    attendanceSummary.value = null;
  }
}

async function loadData() {
  const orgIds = selectedOrgIds.value;

  if ((searchParams.value.companyId || searchParams.value.deptId) && (!orgIds || orgIds.length === 0)) {
    monthlyData.value = [];
    dailyData.value = [];
    attendanceSummary.value = null;
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
      fetchAllDailyRecords(),
      loadSummary()
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
    await renderDeptChart();
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

function handleMonthChange(val: string | null) {
  // 清空时回退当前月
  if (!val) {
    currentMonth.value = getCurrentMonth();
  }
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
    ElMessage.warning($t('report.attendance.noAttendanceDataToExport'));
    return;
  }

  downloadCsv(
    $t('report.attendance.attendanceReportCsv', { month: currentMonth.value, stamp: getExportStamp() }),
    [
      { title: $t('common.month'), key: 'month' },
      { title: $t('common.company'), key: 'companyName' },
      { title: $t('common.department'), key: 'deptName' },
      { title: $t('common.employeeNo'), key: 'employeeNo' },
      { title: $t('common.name'), key: 'employeeName' },
      { title: $t('report.attendance.requiredAttendanceDays'), key: 'workDays' },
      { title: $t('report.attendance.actualAttendanceDays'), key: 'actualDays' },
      { title: $t('report.attendance.attendanceRate'), key: 'attendanceRate', formatter: row => formatRate(row.actualDays, row.workDays) },
      { title: $t('attendance.monthly.lateCount'), key: 'lateTimes' },
      { title: $t('report.attendance.lateMinutes'), key: 'totalLateMinutes' },
      { title: $t('attendance.monthly.earlyLeaveCount'), key: 'earlyTimes' },
      { title: $t('report.attendance.earlyLeaveMinutes'), key: 'totalEarlyMinutes' },
      { title: $t('report.attendance.absentDays'), key: 'absentDays' },
      { title: $t('report.attendance.leaveDays'), key: 'leaveDays' },
      { title: $t('attendance.monthly.totalWorkHours'), key: 'totalWorkHours' }
    ],
    monthlyData.value.map(item => ({
      ...item,
      attendanceRate: formatRate(item.actualDays, item.workDays)
    }))
  );

  ElMessage.success($t('report.attendance.attendanceReportExported'));
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
    1: $t('common.normal'),
    2: $t('common.late'),
    3: $t('common.earlyLeave'),
    4: $t('common.absent'),
    5: $t('common.leave'),
    6: $t('common.businessTrip'),
    7: $t('attendance.daily.lateEarlyLeave')
  };

  return status ? map[status] || $t('common.abnormal') : $t('common.abnormal');
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
        <ElFormItem :label="$t('common.month')">
          <ElDatePicker
            v-model="currentMonth"
            type="month"
            :placeholder="$t('report.attendance.pleaseSelectAMonth')"
            value-format="YYYY-MM"
            :clearable="false"
            style="width: 150px"
            @change="handleMonthChange"
          />
        </ElFormItem>
        <ElFormItem :label="$t('common.company')">
          <ElSelect v-model="searchParams.companyId" :placeholder="$t('common.pleaseSelectCompany')" clearable style="width: 170px">
            <ElOption v-for="company in companies" :key="company.id" :label="company.unitName" :value="company.id" />
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
            style="width: 200px"
          />
        </ElFormItem>
        <ElFormItem :label="$t('common.employeeNo')">
          <ElInput
            v-model="searchParams.employeeNo"
            :placeholder="$t('common.pleaseInputEmployeeNo')"
            clearable
            style="width: 140px"
            @keyup.enter="handleSearch"
          />
        </ElFormItem>
        <ElFormItem :label="$t('common.name')">
          <ElInput
            v-model="searchParams.employeeName"
            :placeholder="$t('common.pleaseInputName')"
            clearable
            style="width: 140px"
            @keyup.enter="handleSearch"
          />
        </ElFormItem>
        <ElFormItem>
          <ElButton type="primary" :loading="loading" @click="handleSearch">
            <template #icon><icon-ep-search /></template>
            {{ $t('report.attendance.refreshReport') }}
          </ElButton>
          <ElButton @click="handleReset">
            <template #icon><icon-ep-refresh /></template>
            {{ $t('common.reset') }}
          </ElButton>
          <ElButton v-permission="'report:attendance:export'" type="success" :disabled="loading || monthlyData.length === 0" @click="handleExport">
            <template #icon><icon-ep-download /></template>
            {{ $t('report.attendance.exportMonthlyReport') }}
          </ElButton>
        </ElFormItem>
      </ElForm>
    </ElCard>

    <div class="overview-grid grid grid-cols-4 gap-16px lt-lg:grid-cols-2 lt-sm:grid-cols-1">
      <ElCard
        v-for="card in overviewCards"
        :key="card.label"
        shadow="hover"
        class="stat-card"
        :class="`stat-card--${card.tone}`"
      >
        <div class="stat-card__label">{{ card.label }}</div>
        <div class="stat-card__value">{{ card.value }}</div>
        <div class="stat-card__hint">{{ card.hint }}</div>
      </ElCard>
    </div>

    <div class="insight-grid grid grid-cols-2 gap-16px lt-xl:grid-cols-1">
      <ElCard>
        <template #header>
          <div class="section-head">
            <span>{{ $t('report.attendance.departmentPerformance') }}</span>
            <span class="section-head__meta">{{ $t('report.attendance.top6AttendanceRate') }}</span>
          </div>
        </template>
        <div v-if="hasDeptChart" ref="deptChartRef" class="mb-16px h-320px overflow-hidden"></div>
        <div v-if="topDepartments.length" class="ranking-list">
          <div v-for="(item, index) in topDepartments" :key="item.deptName" class="ranking-item">
            <div class="ranking-item__top">
              <div class="min-w-0 flex items-center gap-10px">
                <span class="ranking-item__index">{{ index + 1 }}</span>
                <span class="truncate font-600">{{ item.deptName }}</span>
              </div>
              <span class="text-emerald-700 font-700">{{ item.attendanceRate }}%</span>
            </div>
            <div class="ranking-item__meta">
              <span v-if="item.employees !== undefined">{{ item.employees }} {{ $t('org.structure.people') }}</span>
              <span>{{ $t('common.late') }} {{ item.lateTimes }}</span>
              <span>{{ $t('common.absent') }} {{ item.absentDays }}</span>
            </div>
            <ElProgress
              :percentage="item.attendanceRate"
              :stroke-width="10"
              :status="item.attendanceRate >= 95 ? 'success' : item.attendanceRate >= 90 ? undefined : 'exception'"
            />
          </div>
        </div>
        <ElEmpty v-else :description="$t('report.attendance.noDepartmentData')" :image-size="88" />
      </ElCard>

      <ElCard>
        <template #header>
          <div class="section-head">
            <span>{{ $t('report.attendance.abnormalityInsights') }}</span>
            <span class="section-head__meta">{{ $t('report.attendance.latestAbnormalitiesDistribution') }}</span>
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
              <span>{{ item.deptName || $t('report.attendance.unassignedDepartment') }}</span>
              <span>{{ item.occurrenceTime }}</span>
            </div>
          </div>
        </div>
        <ElEmpty v-else :description="$t('report.attendance.noAbnormalRecords')" :image-size="88" />
      </ElCard>
    </div>

    <ElCard>
      <template #header>
        <div class="section-head">
          <span>{{ $t('report.attendance.detailView') }}</span>
          <span class="section-head__meta">{{ $t('report.attendance.switchAmongDepartmentEmployeeAndAbnormalityViews') }}</span>
        </div>
      </template>

      <ElTabs v-model="activeTab">
        <ElTabPane :label="$t('report.attendance.departmentSummary')" name="department">
          <div class="table-wrapper">
            <ElTable
              v-loading="loading"
              :data="departmentSummary"
              border
              stripe
              size="small"
              class="table-content table-content--department"
            >
              <ElTableColumn prop="deptName" :label="$t('common.department')" min-width="180" show-overflow-tooltip />
              <ElTableColumn prop="employees" :label="$t('report.attendance.employees')" width="90" align="center" />
              <ElTableColumn prop="workDays" :label="$t('attendance.monthly.requiredAttendance')" width="90" align="center" />
              <ElTableColumn prop="actualDays" :label="$t('attendance.monthly.actualAttendance')" width="90" align="center" />
              <ElTableColumn prop="attendanceRate" :label="$t('report.attendance.attendanceRate')" width="110" align="center">
                <template #default="{ row }">{{ row.attendanceRate }}%</template>
              </ElTableColumn>
              <ElTableColumn prop="lateTimes" :label="$t('common.late')" width="80" align="center" />
              <ElTableColumn prop="earlyTimes" :label="$t('common.earlyLeave')" width="80" align="center" />
              <ElTableColumn prop="absentDays" :label="$t('common.absent')" width="80" align="center" />
              <ElTableColumn prop="leaveDays" :label="$t('common.leave')" width="80" align="center" />
              <ElTableColumn prop="totalWorkHours" :label="$t('attendance.monthly.totalWorkHours')" width="100" align="center">
                <template #default="{ row }">{{ row.totalWorkHours.toFixed(1) }}h</template>
              </ElTableColumn>
            </ElTable>
          </div>
        </ElTabPane>

        <ElTabPane :label="$t('report.attendance.employeeMonthlyReport')" name="employee">
          <div class="table-wrapper">
            <ElTable
              v-loading="loading"
              :data="pagedMonthlyData"
              border
              stripe
              size="small"
              class="table-content table-content--monthly"
            >
              <ElTableColumn prop="companyName" :label="$t('common.company')" width="150" show-overflow-tooltip />
              <ElTableColumn prop="deptName" :label="$t('common.department')" width="160" show-overflow-tooltip />
              <ElTableColumn prop="employeeNo" :label="$t('common.employeeNo')" width="110" />
              <ElTableColumn prop="employeeName" :label="$t('common.name')" width="100" />
              <ElTableColumn prop="workDays" :label="$t('attendance.monthly.requiredAttendance')" width="88" align="center" />
              <ElTableColumn prop="actualDays" :label="$t('attendance.monthly.actualAttendance')" width="88" align="center" />
              <ElTableColumn :label="$t('report.attendance.attendanceRate')" width="100" align="center">
                <template #default="{ row }">{{ formatRate(row.actualDays, row.workDays) }}</template>
              </ElTableColumn>
              <ElTableColumn prop="lateTimes" :label="$t('common.late')" width="72" align="center" />
              <ElTableColumn prop="totalLateMinutes" :label="$t('report.attendance.lateMinutes2')" width="86" align="center" />
              <ElTableColumn prop="earlyTimes" :label="$t('common.earlyLeave')" width="72" align="center" />
              <ElTableColumn prop="totalEarlyMinutes" :label="$t('report.attendance.earlyLeaveMinutes2')" width="86" align="center" />
              <ElTableColumn prop="absentDays" :label="$t('common.absent')" width="72" align="center" />
              <ElTableColumn prop="leaveDays" :label="$t('common.leave')" width="72" align="center" />
              <ElTableColumn prop="totalWorkHours" :label="$t('attendance.monthly.totalWorkHours')" width="90" align="center">
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

        <ElTabPane :label="$t('report.attendance.abnormalityDetails')" name="abnormal">
          <div class="table-wrapper">
            <ElTable
              v-loading="loading"
              :data="pagedAbnormalRecords"
              border
              stripe
              size="small"
              class="table-content table-content--abnormal"
            >
              <ElTableColumn type="index" label="#" width="54" align="center" />
              <ElTableColumn prop="attDate" :label="$t('common.date')" width="120" />
              <ElTableColumn prop="companyName" :label="$t('common.company')" width="140" show-overflow-tooltip />
              <ElTableColumn prop="deptName" :label="$t('common.department')" width="150" show-overflow-tooltip />
              <ElTableColumn prop="employeeNo" :label="$t('common.employeeNo')" width="110" />
              <ElTableColumn prop="employeeName" :label="$t('common.name')" width="100" />
              <ElTableColumn prop="abnormalType" :label="$t('report.attendance.abnormalityType')" width="110" align="center">
                <template #default="{ row }">
                  <ElTag :type="getStatusTagType(row.status)" size="small">{{ row.abnormalType }}</ElTag>
                </template>
              </ElTableColumn>
              <ElTableColumn prop="occurrenceTime" :label="$t('report.attendance.relatedTime')" width="180" show-overflow-tooltip />
              <ElTableColumn prop="remark" :label="$t('common.remark')" min-width="180" show-overflow-tooltip>
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
