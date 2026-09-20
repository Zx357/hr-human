<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue';
import { ElMessage } from 'element-plus';
import { fetchEmployeePage } from '@/service/api/hr';
import { fetchCompanyList, fetchDepartmentTree } from '@/service/api/organization';
import { fetchDictDataByCode } from '@/service/api/system';
import { type EmployeeReportSummary, fetchEmployeeReportSummary } from '@/service/api/report';
import { useEcharts } from '@/hooks/common/echarts';
import { getDictLabelByValue } from '@/utils/dict';
import { downloadCsv, resolveOrgIds } from '@/utils/report';
import { $t } from '@/locales';

defineOptions({ name: 'EmployeeReport' });

interface StatCard {
  label: string;
  value: string | number;
  hint: string;
  tone: 'primary' | 'success' | 'warning' | 'danger' | 'info';
}

interface DistributionRow {
  name: string;
  value: number;
  ratio: number;
}

interface TrendRow {
  month: string;
  entry: number;
  resign: number;
  net: number;
}

const loading = ref(false);
const companies = ref<Api.Organization.OrgUnit[]>([]);
const departments = ref<Api.Organization.OrgUnit[]>([]);
const employees = ref<Api.Hr.Employee[]>([]);
const educationOptions = ref<Api.System.DictData[]>([]);
const employeeTypeOptions = ref<Api.System.DictData[]>([]);
const genderOptions = ref<Api.System.DictData[]>([]);

const pagination = ref({
  current: 1,
  pageSize: 10
});

const searchParams = ref({
  companyId: undefined as number | undefined,
  deptId: undefined as number | undefined,
  employeeNo: '',
  employeeName: '',
  status: undefined as number | undefined
});

const statusOptions = [
  { label: $t('common.active'), value: 1 },
  { label: $t('common.resigned'), value: 2 }
];

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
const employeeSummary = ref<EmployeeReportSummary | null>(null);

const totalEmployees = computed(() => {
  if (employeeSummary.value?.totals) {
    return Number(employeeSummary.value.totals.total || 0);
  }
  return employees.value.length;
});

const summaryCards = computed<StatCard[]>(() => {
  const currentMonth = getCurrentMonthKey();
  const newThisMonthLocal = employees.value.filter(item => isInMonth(item.entryDate, currentMonth)).length;
  const resignedThisMonthLocal = employees.value.filter(item => isInMonth(item.leaveDate, currentMonth)).length;
  const averageAge = getAverageAge(employees.value);

  // 优先使用后端统计接口数据
  const api = employeeSummary.value;
  if (api?.totals) {
    const apiTrend = api.monthlyTrend?.find(item => item.month === currentMonth);
    return [
      { label: $t('common.totalEmployees'), value: Number(api.totals.total || 0), hint: $t('report.employee.employeesUnderCurrentFilters'), tone: 'primary' },
      { label: $t('report.employee.activeEmployees'), value: Number(api.totals.active || 0), hint: $t('report.employee.employeesWithActiveStatus'), tone: 'success' },
      { label: $t('report.employee.employeesOnProbation'), value: Number(api.totals.probation || 0), hint: $t('report.employee.employeesOnProbation2'), tone: 'info' },
      { label: $t('report.employee.resignedEmployees'), value: Number(api.totals.resigned || 0), hint: $t('report.employee.employeesWithResignedStatus'), tone: 'warning' },
      {
        label: $t('report.employee.newHiresThisMonth'),
        value: apiTrend ? Number(apiTrend.entry || 0) : newThisMonthLocal,
        hint: $t('report.employee.countedByEntryDate'),
        tone: 'primary'
      },
      {
        label: $t('report.employee.resignationsThisMonth'),
        value: apiTrend ? Number(apiTrend.exit || 0) : resignedThisMonthLocal,
        hint: $t('report.employee.countedByResignationDate'),
        tone: 'danger'
      },
      {
        label: $t('report.employee.averageAge'),
        value: averageAge > 0 ? $t('report.employee.yrs', { age: averageAge.toFixed(1) }) : '-',
        hint: $t('report.employee.basedOnEmployeesWithBirthdayFilled'),
        tone: 'primary'
      }
    ];
  }

  // 回退：前端全量计算
  const onJobCount = employees.value.filter(item => item.status === 1).length;
  const resignedCount = employees.value.filter(item => item.status === 2).length;

  return [
    { label: $t('common.totalEmployees'), value: totalEmployees.value, hint: $t('report.employee.employeesUnderCurrentFilters'), tone: 'primary' },
    { label: $t('report.employee.activeEmployees'), value: onJobCount, hint: $t('report.employee.employeesWithActiveStatus'), tone: 'success' },
    { label: $t('report.employee.resignedEmployees'), value: resignedCount, hint: $t('report.employee.employeesWithResignedStatus'), tone: 'warning' },
    { label: $t('report.employee.newHiresThisMonth'), value: newThisMonthLocal, hint: $t('report.employee.countedByEntryDate'), tone: 'info' },
    { label: $t('report.employee.resignationsThisMonth'), value: resignedThisMonthLocal, hint: $t('report.employee.countedByResignationDate'), tone: 'danger' },
    {
      label: $t('report.employee.averageAge'),
      value: averageAge > 0 ? $t('report.employee.yrs', { age: averageAge.toFixed(1) }) : '-',
      hint: $t('report.employee.basedOnEmployeesWithBirthdayFilled'),
      tone: 'primary'
    }
  ];
});

function fromNameValue(items?: { name: string; value: number }[]): Record<string, number> {
  const record: Record<string, number> = {};
  for (const item of items || []) {
    if (item?.name !== undefined && item?.name !== null) {
      record[item.name] = Number(item.value || 0);
    }
  }
  return record;
}

const departmentDistribution = computed(() => {
  // 优先使用后端统计接口数据
  if (employeeSummary.value?.deptDistribution?.length) {
    return buildDistribution(fromNameValue(employeeSummary.value.deptDistribution));
  }

  // 回退：前端全量计算
  return buildDistribution(
    employees.value.reduce<Record<string, number>>((acc, item) => {
      const name = item.deptName || $t('report.attendance.unassignedDepartment');
      acc[name] = (acc[name] || 0) + 1;

      return acc;
    }, {})
  );
});

const educationDistribution = computed(() => {
  // 优先使用后端统计接口数据（名称已是文案）
  if (employeeSummary.value?.education?.length) {
    return buildDistribution(fromNameValue(employeeSummary.value.education));
  }

  // 回退：前端全量计算
  return buildDistribution(
    employees.value.reduce<Record<string, number>>((acc, item) => {
      const name = item.highestEducation
        ? getDictLabelByValue(educationOptions.value, item.highestEducation)
        : $t('home.educationChart.notFilled');
      acc[name] = (acc[name] || 0) + 1;

      return acc;
    }, {})
  );
});

const employeeTypeDistribution = computed(() =>
  buildDistribution(
    employees.value.reduce<Record<string, number>>((acc, item) => {
      const name = item.employeeType ? getDictLabelByValue(employeeTypeOptions.value, item.employeeType) : $t('home.educationChart.notFilled');
      acc[name] = (acc[name] || 0) + 1;

      return acc;
    }, {})
  )
);

const ageDistribution = computed(() => {
  // 优先使用后端统计接口数据
  if (employeeSummary.value?.ageBuckets?.length) {
    return buildDistribution(fromNameValue(employeeSummary.value.ageBuckets));
  }

  // 回退：前端全量计算
  const bucketMap: Record<string, number> = {
    '24岁及以下': 0,
    '25-29岁': 0,
    '30-34岁': 0,
    '35-39岁': 0,
    '40-44岁': 0,
    '45岁及以上': 0,
    未填写: 0
  };

  for (const item of employees.value) {
    const age = getAge(item.birthDate);

    if (age === null) {
      bucketMap[$t('home.educationChart.notFilled')] += 1;
    } else if (age <= 24) {
      bucketMap['24岁及以下'] += 1;
    } else if (age <= 29) {
      bucketMap['25-29岁'] += 1;
    } else if (age <= 34) {
      bucketMap['30-34岁'] += 1;
    } else if (age <= 39) {
      bucketMap['35-39岁'] += 1;
    } else if (age <= 44) {
      bucketMap['40-44岁'] += 1;
    } else {
      bucketMap['45岁及以上'] += 1;
    }
  }

  return buildDistribution(bucketMap);
});

const entryTrend = computed<TrendRow[]>(() => {
  // 优先使用后端统计接口数据
  if (employeeSummary.value?.monthlyTrend?.length) {
    return employeeSummary.value.monthlyTrend.map(item => {
      const entry = Number(item.entry || 0);
      const resign = Number(item.exit || 0);

      return { month: item.month, entry, resign, net: entry - resign };
    });
  }

  // 回退：前端全量计算
  return getRecentMonths(12).map(month => {
    const entry = employees.value.filter(item => isInMonth(item.entryDate, month)).length;
    const resign = employees.value.filter(item => isInMonth(item.leaveDate, month)).length;

    return {
      month,
      entry,
      resign,
      net: entry - resign
    };
  });
});

const pagedEmployees = computed(() => {
  const start = (pagination.value.current - 1) * pagination.value.pageSize;

  return employees.value.slice(start, start + pagination.value.pageSize);
});

// ==================== 图表 ====================

/** 近 12 个月入离职趋势 - 折线图 */
const { domRef: trendChartRef, updateOptions: updateTrendOptions } = useEcharts(() => ({
  tooltip: { trigger: 'axis' },
  legend: { data: [$t('report.employee.newHires'), $t('report.employee.resignedEmployees'), $t('report.employee.netGrowth')], top: 0 },
  grid: { left: '2%', right: '2%', bottom: '3%', top: '40px', containLabel: true },
  xAxis: {
    type: 'category',
    boundaryGap: false,
    data: [] as string[],
    axisLabel: { fontSize: 12 }
  },
  yAxis: {
    type: 'value',
    minInterval: 1,
    splitLine: { lineStyle: { type: 'dashed' } },
    axisLabel: { fontSize: 12 }
  },
  series: [
    {
      name: $t('report.employee.newHires'),
      type: 'line',
      smooth: true,
      symbol: 'circle',
      symbolSize: 6,
      itemStyle: { color: '#10b981' },
      data: [] as number[]
    },
    {
      name: $t('report.employee.resignedEmployees'),
      type: 'line',
      smooth: true,
      symbol: 'circle',
      symbolSize: 6,
      itemStyle: { color: '#ef4444' },
      data: [] as number[]
    },
    {
      name: $t('report.employee.netGrowth'),
      type: 'line',
      smooth: true,
      symbol: 'circle',
      symbolSize: 6,
      itemStyle: { color: '#6366f1' },
      lineStyle: { type: 'dashed' },
      data: [] as number[]
    }
  ]
}));

/** 年龄分布 - 环形图 */
const { domRef: ageChartRef, updateOptions: updateAgeOptions } = useEcharts(() => ({
  tooltip: { trigger: 'item', formatter: $t('report.employee.people') },
  legend: { orient: 'vertical', right: 0, top: 'middle' },
  series: [
    {
      name: $t('common.ageDistribution'),
      type: 'pie',
      radius: ['42%', '68%'],
      center: ['38%', '50%'],
      avoidLabelOverlap: true,
      itemStyle: { borderRadius: 6, borderColor: '#fff', borderWidth: 2 },
      label: { show: false },
      emphasis: {
        label: { show: true, fontSize: 14, fontWeight: 'bold' }
      },
      data: [] as { name: string; value: number }[]
    }
  ]
}));

/** 学历分布 - 环形图 */
const { domRef: educationChartRef, updateOptions: updateEducationOptions } = useEcharts(() => ({
  tooltip: { trigger: 'item', formatter: $t('report.employee.people') },
  legend: { orient: 'vertical', right: 0, top: 'middle' },
  series: [
    {
      name: $t('home.educationChart.educationDistribution'),
      type: 'pie',
      radius: ['42%', '68%'],
      center: ['38%', '50%'],
      avoidLabelOverlap: true,
      itemStyle: { borderRadius: 6, borderColor: '#fff', borderWidth: 2 },
      label: { show: false },
      emphasis: {
        label: { show: true, fontSize: 14, fontWeight: 'bold' }
      },
      data: [] as { name: string; value: number }[]
    }
  ]
}));

function renderCharts() {
  updateTrendOptions(opts => {
    opts.xAxis.data = entryTrend.value.map(item => item.month);
    opts.series[0].data = entryTrend.value.map(item => item.entry);
    opts.series[1].data = entryTrend.value.map(item => item.resign);
    opts.series[2].data = entryTrend.value.map(item => item.net);
    return opts;
  });

  updateAgeOptions(opts => {
    opts.series[0].data = ageDistribution.value.map(item => ({ name: item.name, value: item.value }));
    return opts;
  });

  updateEducationOptions(opts => {
    opts.series[0].data = educationDistribution.value.map(item => ({ name: item.name, value: item.value }));
    return opts;
  });
}

async function loadBaseData() {
  const [companyRes, educationRes, employeeTypeRes, genderRes] = await Promise.all([
    fetchCompanyList(),
    fetchDictDataByCode('education'),
    fetchDictDataByCode('employee_type'),
    fetchDictDataByCode('gender')
  ]);

  companies.value = companyRes.data || [];
  educationOptions.value = educationRes.data || [];
  employeeTypeOptions.value = employeeTypeRes.data || [];
  genderOptions.value = genderRes.data || [];
}

async function loadDepartments(companyId: number) {
  const res = await fetchDepartmentTree(companyId);
  departments.value = res.data || [];
}

async function fetchAllEmployees() {
  const orgIds = selectedOrgIds.value;

  if ((searchParams.value.companyId || searchParams.value.deptId) && (!orgIds || orgIds.length === 0)) {
    return [];
  }

  const records: Api.Hr.Employee[] = [];
  const pageSize = 200;
  let pageNum = 1;
  // 分页循环上限保护：最多 100 页 / 2 万行，防止全量拉取产生过多请求
  const MAX_PAGES = 100;
  const MAX_ROWS = 20000;
  let truncated = false;

  while (true) {
    // eslint-disable-next-line no-await-in-loop
    const res = await fetchEmployeePage({
      pageNum,
      pageSize,
      name: searchParams.value.employeeName || undefined,
      employeeNo: searchParams.value.employeeNo || undefined,
      orgIds: orgIds?.join(','),
      status: searchParams.value.status
    });

    const pageData = res.data;
    const chunk = pageData?.records || [];

    records.push(...chunk);

    if (!pageData || records.length >= (pageData.total || 0) || chunk.length < pageSize) {
      break;
    }

    pageNum += 1;

    if (pageNum > MAX_PAGES || records.length >= MAX_ROWS) {
      truncated = true;
      break;
    }
  }

  if (truncated) {
    ElMessage.warning($t('report.employee.tooManyEmployeesOnlyTheFirst20000AreCountedAddFiltersToNarrowTheRange'));
  }

  return records;
}

/** 加载后端统计接口（失败时置空，回退前端计算） */
async function loadSummary() {
  try {
    const res = await fetchEmployeeReportSummary({
      companyId: searchParams.value.companyId,
      deptId: searchParams.value.deptId
    });
    employeeSummary.value = res.data && res.data.totals ? res.data : null;
  } catch {
    employeeSummary.value = null;
  }
}

async function loadData() {
  loading.value = true;

  try {
    await Promise.all([
      loadSummary(),
      fetchAllEmployees().then(records => {
        employees.value = records;
      })
    ]);
    pagination.value.current = 1;
    renderCharts();
  } finally {
    loading.value = false;
  }
}

function handleSearch() {
  loadData();
}

function handleReset() {
  searchParams.value = {
    companyId: undefined,
    deptId: undefined,
    employeeNo: '',
    employeeName: '',
    status: undefined
  };
  departments.value = [];
  loadData();
}

function handlePageChange(page: number) {
  pagination.value.current = page;
}

function handleSizeChange(size: number) {
  pagination.value.pageSize = size;
  pagination.value.current = 1;
}

function handleExport() {
  if (!employees.value.length) {
    ElMessage.warning($t('report.employee.noEmployeeDataToExport'));
    return;
  }

  downloadCsv(
    $t('report.employee.employeeReportCsv', { stamp: getExportStamp() }),
    [
      { title: $t('common.company'), key: 'companyName' },
      { title: $t('common.department'), key: 'deptName' },
      { title: $t('common.employeeNo'), key: 'employeeNo' },
      { title: $t('common.name'), key: 'name' },
      { title: $t('common.gender'), key: 'gender', formatter: row => getGenderLabel(row.gender) },
      { title: $t('common.education'), key: 'highestEducation', formatter: row => getEducationLabel(row.highestEducation) },
      { title: $t('hr.employee.employeeCategory'), key: 'employeeType', formatter: row => getEmployeeTypeLabel(row.employeeType) },
      { title: $t('common.entryDate'), key: 'entryDate' },
      { title: $t('common.resignationDate'), key: 'leaveDate' },
      { title: $t('common.phone'), key: 'phone' },
      { title: $t('common.status'), key: 'status', formatter: row => getStatusLabel(row.status) }
    ],
    employees.value
  );

  ElMessage.success($t('report.employee.employeeReportExported'));
}

function buildDistribution(source: Record<string, number>) {
  const total = Object.values(source).reduce((sum, value) => sum + value, 0);

  return Object.entries(source)
    .map(([name, value]) => ({
      name,
      value,
      ratio: total > 0 ? Number(((value / total) * 100).toFixed(1)) : 0
    }))
    .filter(item => item.value > 0)
    .sort((a, b) => b.value - a.value) as DistributionRow[];
}

function getCurrentMonthKey() {
  const now = new Date();

  return `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}`;
}

function getRecentMonths(count: number) {
  return Array.from({ length: count }, (_, index) => {
    const date = new Date();
    date.setDate(1);
    date.setMonth(date.getMonth() - (count - index - 1));

    return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}`;
  });
}

function isInMonth(dateText: string | undefined, month: string) {
  return Boolean(dateText && dateText.slice(0, 7) === month);
}

function getAge(dateText?: string) {
  if (!dateText) {
    return null;
  }

  const birthday = new Date(dateText);

  if (Number.isNaN(birthday.getTime())) {
    return null;
  }

  const today = new Date();
  let age = today.getFullYear() - birthday.getFullYear();
  const monthDiff = today.getMonth() - birthday.getMonth();

  if (monthDiff < 0 || (monthDiff === 0 && today.getDate() < birthday.getDate())) {
    age -= 1;
  }

  return age;
}

function getAverageAge(list: Api.Hr.Employee[]) {
  const ages = list.map(item => getAge(item.birthDate)).filter((value): value is number => value !== null);

  if (!ages.length) {
    return 0;
  }

  return ages.reduce((sum, value) => sum + value, 0) / ages.length;
}

function getEducationLabel(value?: string) {
  return value ? getDictLabelByValue(educationOptions.value, value) : $t('home.educationChart.notFilled');
}

function getEmployeeTypeLabel(value?: string) {
  return value ? getDictLabelByValue(employeeTypeOptions.value, value) : $t('home.educationChart.notFilled');
}

function getGenderLabel(value?: string) {
  return value ? getDictLabelByValue(genderOptions.value, value) : $t('home.educationChart.notFilled');
}

function getStatusLabel(status?: number) {
  if (status === 1) {
    return $t('common.active');
  }

  if (status === 2) {
    return $t('common.resigned');
  }

  return $t('common.unknown');
}

function getStatusTagType(status?: number) {
  if (status === 1) {
    return 'success';
  }

  if (status === 2) {
    return 'info';
  }

  return 'warning';
}

function getExportStamp() {
  const now = new Date();
  const year = now.getFullYear();
  const month = String(now.getMonth() + 1).padStart(2, '0');
  const day = String(now.getDate()).padStart(2, '0');
  const hour = String(now.getHours()).padStart(2, '0');
  const minute = String(now.getMinutes()).padStart(2, '0');

  return `${year}${month}${day}_${hour}${minute}`;
}

onMounted(async () => {
  await loadBaseData();
  await loadData();
});
</script>

<template>
  <div class="report-page flex-col-stretch gap-16px">
    <ElCard class="search-card">
      <ElForm inline :model="searchParams">
        <ElFormItem :label="$t('common.company')">
          <ElSelect v-model="searchParams.companyId" :placeholder="$t('common.pleaseSelectCompany')" clearable style="width: 180px">
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
        <ElFormItem :label="$t('common.status')">
          <ElSelect v-model="searchParams.status" :placeholder="$t('common.allStatus')" clearable style="width: 140px">
            <ElOption v-for="item in statusOptions" :key="String(item.value)" :label="item.label" :value="item.value" />
          </ElSelect>
        </ElFormItem>
        <ElFormItem>
          <ElButton type="primary" :loading="loading" @click="handleSearch">
            <template #icon><icon-ep-search /></template>
            {{ $t('report.employee.queryReport') }}
          </ElButton>
          <ElButton @click="handleReset">
            <template #icon><icon-ep-refresh /></template>
            {{ $t('common.reset') }}
          </ElButton>
          <ElButton v-permission="'report:employee:export'" type="success" :disabled="loading || totalEmployees === 0" @click="handleExport">
            <template #icon><icon-ep-download /></template>
            {{ $t('report.employee.exportDetails') }}
          </ElButton>
        </ElFormItem>
      </ElForm>
    </ElCard>

    <div class="summary-grid grid grid-cols-6 gap-16px lt-lg:grid-cols-3 lt-sm:grid-cols-2">
      <ElCard
        v-for="card in summaryCards"
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

    <div class="distribution-grid grid grid-cols-2 gap-16px lt-xl:grid-cols-1">
      <ElCard class="data-card">
        <template #header>
          <div class="card-header">
            <span>{{ $t('report.employee.departmentDistribution') }}</span>
            <span class="card-header__meta">{{ $t('home.reminderCard.total') }} {{ totalEmployees }} {{ $t('org.structure.people') }}</span>
          </div>
        </template>
        <div class="table-wrapper">
          <ElTable
            :data="departmentDistribution"
            border
            stripe
            size="small"
            max-height="320"
            class="table-content table-content--distribution"
          >
            <ElTableColumn type="index" label="#" width="54" align="center" />
            <ElTableColumn prop="name" :label="$t('common.department')" min-width="180" show-overflow-tooltip />
            <ElTableColumn prop="value" :label="$t('common.employees')" width="90" align="center" />
            <ElTableColumn prop="ratio" :label="$t('org.structure.percentage')" width="180">
              <template #default="{ row }">
                <ElProgress :percentage="row.ratio" :stroke-width="10" />
              </template>
            </ElTableColumn>
          </ElTable>
        </div>
      </ElCard>

      <ElCard class="data-card">
        <template #header>
          <div class="card-header">
            <span>{{ $t('home.educationChart.educationDistribution') }}</span>
            <span class="card-header__meta">{{ $t('home.reminderCard.total') }} {{ totalEmployees }} {{ $t('org.structure.people') }}</span>
          </div>
        </template>
        <div ref="educationChartRef" class="h-300px overflow-hidden"></div>
        <div class="table-wrapper">
          <ElTable
            :data="educationDistribution"
            border
            stripe
            size="small"
            max-height="320"
            class="table-content table-content--distribution"
          >
            <ElTableColumn type="index" label="#" width="54" align="center" />
            <ElTableColumn prop="name" :label="$t('common.education')" min-width="180" />
            <ElTableColumn prop="value" :label="$t('common.employees')" width="90" align="center" />
            <ElTableColumn prop="ratio" :label="$t('org.structure.percentage')" width="180">
              <template #default="{ row }">
                <ElProgress :percentage="row.ratio" :stroke-width="10" status="success" />
              </template>
            </ElTableColumn>
          </ElTable>
        </div>
      </ElCard>

      <ElCard class="data-card">
        <template #header>
          <div class="card-header">
            <span>{{ $t('report.employee.employeeCategoryDistribution') }}</span>
            <span class="card-header__meta">{{ $t('home.reminderCard.total') }} {{ totalEmployees }} {{ $t('org.structure.people') }}</span>
          </div>
        </template>
        <div class="table-wrapper">
          <ElTable
            :data="employeeTypeDistribution"
            border
            stripe
            size="small"
            max-height="320"
            class="table-content table-content--distribution"
          >
            <ElTableColumn type="index" label="#" width="54" align="center" />
            <ElTableColumn prop="name" :label="$t('hr.employee.employeeCategory')" min-width="180" />
            <ElTableColumn prop="value" :label="$t('common.employees')" width="90" align="center" />
            <ElTableColumn prop="ratio" :label="$t('org.structure.percentage')" width="180">
              <template #default="{ row }">
                <ElProgress :percentage="row.ratio" :stroke-width="10" status="warning" />
              </template>
            </ElTableColumn>
          </ElTable>
        </div>
      </ElCard>

      <ElCard class="data-card">
        <template #header>
          <div class="card-header">
            <span>{{ $t('common.ageDistribution') }}</span>
            <span class="card-header__meta">{{ $t('home.reminderCard.total') }} {{ totalEmployees }} {{ $t('org.structure.people') }}</span>
          </div>
        </template>
        <div ref="ageChartRef" class="h-300px overflow-hidden"></div>
        <div class="table-wrapper">
          <ElTable
            :data="ageDistribution"
            border
            stripe
            size="small"
            max-height="320"
            class="table-content table-content--distribution"
          >
            <ElTableColumn type="index" label="#" width="54" align="center" />
            <ElTableColumn prop="name" :label="$t('org.structure.ageRange')" min-width="180" />
            <ElTableColumn prop="value" :label="$t('common.employees')" width="90" align="center" />
            <ElTableColumn prop="ratio" :label="$t('org.structure.percentage')" width="180">
              <template #default="{ row }">
                <ElProgress :percentage="row.ratio" :stroke-width="10" status="exception" />
              </template>
            </ElTableColumn>
          </ElTable>
        </div>
      </ElCard>
    </div>

    <ElCard class="data-card">
      <template #header>
        <div class="card-header">
          <span>{{ $t('report.employee.hireResignTrendLast12Months') }}</span>
          <span class="card-header__meta">{{ $t('report.employee.countedByCurrentFilterRange') }}</span>
        </div>
      </template>
      <div ref="trendChartRef" class="h-320px overflow-hidden"></div>
      <div class="table-wrapper">
        <ElTable :data="entryTrend" border stripe size="small" class="table-content table-content--trend">
          <ElTableColumn prop="month" :label="$t('common.month')" width="120" />
          <ElTableColumn prop="entry" :label="$t('report.employee.newHires')" min-width="120" align="center">
            <template #default="{ row }">
              <span class="text-emerald-600 font-600">+{{ row.entry }}</span>
            </template>
          </ElTableColumn>
          <ElTableColumn prop="resign" :label="$t('report.employee.resignedEmployees')" min-width="120" align="center">
            <template #default="{ row }">
              <span class="text-rose-500 font-600">-{{ row.resign }}</span>
            </template>
          </ElTableColumn>
          <ElTableColumn prop="net" :label="$t('report.employee.netGrowth')" min-width="120" align="center">
            <template #default="{ row }">
              <span :class="row.net >= 0 ? 'text-emerald-600 font-600' : 'text-rose-500 font-600'">
                {{ row.net >= 0 ? '+' : '' }}{{ row.net }}
              </span>
            </template>
          </ElTableColumn>
        </ElTable>
      </div>
    </ElCard>

    <ElCard class="data-card">
      <template #header>
        <div class="card-header">
          <span>{{ $t('report.employee.employeeDetails') }}</span>
          <span class="card-header__meta">{{ $t('report.employee.total') }} {{ totalEmployees }} {{ $t('hr.employee.items') }}</span>
        </div>
      </template>

      <div class="table-wrapper">
        <ElTable
          v-loading="loading"
          :data="pagedEmployees"
          border
          stripe
          size="small"
          class="table-content table-content--detail"
        >
          <ElTableColumn prop="companyName" :label="$t('common.company')" width="150" show-overflow-tooltip />
          <ElTableColumn prop="deptName" :label="$t('common.department')" width="160" show-overflow-tooltip />
          <ElTableColumn prop="employeeNo" :label="$t('common.employeeNo')" width="110" />
          <ElTableColumn prop="name" :label="$t('common.name')" width="100" />
          <ElTableColumn prop="gender" :label="$t('common.gender')" width="80" align="center">
            <template #default="{ row }">{{ getGenderLabel(row.gender) }}</template>
          </ElTableColumn>
          <ElTableColumn prop="highestEducation" :label="$t('common.education')" width="110">
            <template #default="{ row }">{{ getEducationLabel(row.highestEducation) }}</template>
          </ElTableColumn>
          <ElTableColumn prop="employeeType" :label="$t('hr.employee.employeeCategory')" width="120">
            <template #default="{ row }">{{ getEmployeeTypeLabel(row.employeeType) }}</template>
          </ElTableColumn>
          <ElTableColumn prop="entryDate" :label="$t('common.entryDate')" width="120" />
          <ElTableColumn prop="leaveDate" :label="$t('common.resignationDate')" width="120">
            <template #default="{ row }">{{ row.leaveDate || '-' }}</template>
          </ElTableColumn>
          <ElTableColumn prop="phone" :label="$t('common.phone')" width="140" />
          <ElTableColumn prop="status" :label="$t('common.status')" width="90" align="center">
            <template #default="{ row }">
              <ElTag :type="getStatusTagType(row.status)" size="small">{{ getStatusLabel(row.status) }}</ElTag>
            </template>
          </ElTableColumn>
        </ElTable>
      </div>

      <div class="mt-16px flex justify-end">
        <ElPagination
          v-model:current-page="pagination.current"
          v-model:page-size="pagination.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="totalEmployees"
          layout="total, sizes, prev, pager, next"
          @current-change="handlePageChange"
          @size-change="handleSizeChange"
        />
      </div>
    </ElCard>
  </div>
</template>

<style scoped>
.report-page {
  min-height: 500px;
  overflow-x: hidden;
  padding-bottom: 16px;
}

.report-page > * {
  flex-shrink: 0;
}

.search-card {
  flex-shrink: 0;
}

.search-card :deep(.el-form) {
  display: flex;
  flex-wrap: wrap;
  align-items: flex-start;
}

.search-card :deep(.el-form-item) {
  margin-right: 12px;
  margin-bottom: 12px;
}

.report-page :deep(.el-card__body) {
  height: auto;
  min-height: 0;
}

.summary-grid > *,
.distribution-grid > * {
  min-width: 0;
}

.data-card {
  min-width: 0;
}

.stat-card {
  position: relative;
  overflow: hidden;
  border: 1px solid var(--el-border-color-light);
}

.stat-card::after {
  position: absolute;
  inset: auto -28px -28px auto;
  width: 88px;
  height: 88px;
  border-radius: 999px;
  content: '';
  opacity: 0.12;
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

.stat-card--warning::after {
  background: #f59e0b;
}

.stat-card--danger::after {
  background: #ef4444;
}

.stat-card--info::after {
  background: #06b6d4;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  flex-wrap: wrap;
}

.card-header__meta {
  color: var(--el-text-color-secondary);
  font-size: 12px;
  white-space: nowrap;
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

.table-content--distribution {
  min-width: 520px;
}

.table-content--trend {
  min-width: 520px;
}

.table-content--detail {
  min-width: 1320px;
}

@media (max-width: 768px) {
  .stat-card__value {
    font-size: 26px;
  }
}
</style>
