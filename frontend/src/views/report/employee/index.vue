<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue';
import { ElMessage } from 'element-plus';
import { fetchEmployeePage } from '@/service/api/hr';
import { fetchCompanyList, fetchDepartmentTree } from '@/service/api/organization';
import { fetchDictDataByCode } from '@/service/api/system';
import { getDictLabelByValue } from '@/utils/dict';
import { downloadCsv, resolveOrgIds } from '@/utils/report';

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
  { label: '在职', value: 1 },
  { label: '离职', value: 2 }
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

const totalEmployees = computed(() => employees.value.length);

const summaryCards = computed<StatCard[]>(() => {
  const onJobCount = employees.value.filter(item => item.status === 1).length;
  const resignedCount = employees.value.filter(item => item.status === 2).length;
  const currentMonth = getCurrentMonthKey();
  const newThisMonth = employees.value.filter(item => isInMonth(item.entryDate, currentMonth)).length;
  const resignedThisMonth = employees.value.filter(item => isInMonth(item.leaveDate, currentMonth)).length;
  const averageAge = getAverageAge(employees.value);

  return [
    { label: '员工总数', value: totalEmployees.value, hint: '当前筛选条件下的员工数量', tone: 'primary' },
    { label: '在职人数', value: onJobCount, hint: '状态为在职的员工', tone: 'success' },
    { label: '离职人数', value: resignedCount, hint: '状态为离职的员工', tone: 'warning' },
    { label: '本月入职', value: newThisMonth, hint: '按入职日期统计', tone: 'info' },
    { label: '本月离职', value: resignedThisMonth, hint: '按离职日期统计', tone: 'danger' },
    { label: '平均年龄', value: averageAge > 0 ? `${averageAge.toFixed(1)} 岁` : '-', hint: '基于已填写生日的员工', tone: 'primary' }
  ];
});

const departmentDistribution = computed(() =>
  buildDistribution(
    employees.value.reduce<Record<string, number>>((acc, item) => {
      const name = item.deptName || '未分配部门';
      acc[name] = (acc[name] || 0) + 1;

      return acc;
    }, {})
  )
);

const educationDistribution = computed(() =>
  buildDistribution(
    employees.value.reduce<Record<string, number>>((acc, item) => {
      const name = item.highestEducation ? getDictLabelByValue(educationOptions.value, item.highestEducation) : '未填写';
      acc[name] = (acc[name] || 0) + 1;

      return acc;
    }, {})
  )
);

const employeeTypeDistribution = computed(() =>
  buildDistribution(
    employees.value.reduce<Record<string, number>>((acc, item) => {
      const name = item.employeeType ? getDictLabelByValue(employeeTypeOptions.value, item.employeeType) : '未填写';
      acc[name] = (acc[name] || 0) + 1;

      return acc;
    }, {})
  )
);

const ageDistribution = computed(() => {
  const bucketMap: Record<string, number> = {
    '24岁及以下': 0,
    '25-29岁': 0,
    '30-34岁': 0,
    '35-39岁': 0,
    '40-44岁': 0,
    '45岁及以上': 0,
    '未填写': 0
  };

  for (const item of employees.value) {
    const age = getAge(item.birthDate);

    if (age === null) {
      bucketMap['未填写'] += 1;
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
  }

  return records;
}

async function loadData() {
  loading.value = true;

  try {
    employees.value = await fetchAllEmployees();
    pagination.value.current = 1;
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
    ElMessage.warning('暂无可导出的员工数据');
    return;
  }

  downloadCsv(
    `员工报表_${getExportStamp()}.csv`,
    [
      { title: '公司', key: 'companyName' },
      { title: '部门', key: 'deptName' },
      { title: '工号', key: 'employeeNo' },
      { title: '姓名', key: 'name' },
      { title: '性别', key: 'gender', formatter: row => getGenderLabel(row.gender) },
      { title: '学历', key: 'highestEducation', formatter: row => getEducationLabel(row.highestEducation) },
      { title: '员工类别', key: 'employeeType', formatter: row => getEmployeeTypeLabel(row.employeeType) },
      { title: '入职日期', key: 'entryDate' },
      { title: '离职日期', key: 'leaveDate' },
      { title: '手机号', key: 'phone' },
      { title: '状态', key: 'status', formatter: row => getStatusLabel(row.status) }
    ],
    employees.value
  );

  ElMessage.success('员工报表已导出');
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
  return value ? getDictLabelByValue(educationOptions.value, value) : '未填写';
}

function getEmployeeTypeLabel(value?: string) {
  return value ? getDictLabelByValue(employeeTypeOptions.value, value) : '未填写';
}

function getGenderLabel(value?: string) {
  return value ? getDictLabelByValue(genderOptions.value, value) : '未填写';
}

function getStatusLabel(status?: number) {
  if (status === 1) {
    return '在职';
  }

  if (status === 2) {
    return '离职';
  }

  return '未知';
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
        <ElFormItem label="公司">
          <ElSelect v-model="searchParams.companyId" placeholder="请选择公司" clearable style="width: 180px">
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
        <ElFormItem label="状态">
          <ElSelect v-model="searchParams.status" placeholder="全部状态" clearable style="width: 140px">
            <ElOption v-for="item in statusOptions" :key="String(item.value)" :label="item.label" :value="item.value" />
          </ElSelect>
        </ElFormItem>
        <ElFormItem>
          <ElButton type="primary" :loading="loading" @click="handleSearch">
            <template #icon><icon-ep-search /></template>
            查询报表
          </ElButton>
          <ElButton @click="handleReset">
            <template #icon><icon-ep-refresh /></template>
            重置
          </ElButton>
          <ElButton type="success" :disabled="loading || totalEmployees === 0" @click="handleExport">
            <template #icon><icon-ep-download /></template>
            导出明细
          </ElButton>
        </ElFormItem>
      </ElForm>
    </ElCard>

    <div class="summary-grid grid grid-cols-6 gap-16px lt-lg:grid-cols-3 lt-sm:grid-cols-2">
      <ElCard v-for="card in summaryCards" :key="card.label" shadow="hover" class="stat-card" :class="`stat-card--${card.tone}`">
        <div class="stat-card__label">{{ card.label }}</div>
        <div class="stat-card__value">{{ card.value }}</div>
        <div class="stat-card__hint">{{ card.hint }}</div>
      </ElCard>
    </div>

    <div class="distribution-grid grid grid-cols-2 gap-16px lt-xl:grid-cols-1">
      <ElCard class="data-card">
        <template #header>
          <div class="card-header">
            <span>部门分布</span>
            <span class="card-header__meta">共 {{ totalEmployees }} 人</span>
          </div>
        </template>
        <div class="table-wrapper">
          <ElTable :data="departmentDistribution" border stripe size="small" max-height="320" class="table-content table-content--distribution">
            <ElTableColumn type="index" label="#" width="54" align="center" />
            <ElTableColumn prop="name" label="部门" min-width="180" show-overflow-tooltip />
            <ElTableColumn prop="value" label="人数" width="90" align="center" />
            <ElTableColumn prop="ratio" label="占比" width="180">
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
            <span>学历分布</span>
            <span class="card-header__meta">共 {{ totalEmployees }} 人</span>
          </div>
        </template>
        <div class="table-wrapper">
          <ElTable :data="educationDistribution" border stripe size="small" max-height="320" class="table-content table-content--distribution">
            <ElTableColumn type="index" label="#" width="54" align="center" />
            <ElTableColumn prop="name" label="学历" min-width="180" />
            <ElTableColumn prop="value" label="人数" width="90" align="center" />
            <ElTableColumn prop="ratio" label="占比" width="180">
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
            <span>员工类别分布</span>
            <span class="card-header__meta">共 {{ totalEmployees }} 人</span>
          </div>
        </template>
        <div class="table-wrapper">
          <ElTable :data="employeeTypeDistribution" border stripe size="small" max-height="320" class="table-content table-content--distribution">
            <ElTableColumn type="index" label="#" width="54" align="center" />
            <ElTableColumn prop="name" label="员工类别" min-width="180" />
            <ElTableColumn prop="value" label="人数" width="90" align="center" />
            <ElTableColumn prop="ratio" label="占比" width="180">
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
            <span>年龄分布</span>
            <span class="card-header__meta">共 {{ totalEmployees }} 人</span>
          </div>
        </template>
        <div class="table-wrapper">
          <ElTable :data="ageDistribution" border stripe size="small" max-height="320" class="table-content table-content--distribution">
            <ElTableColumn type="index" label="#" width="54" align="center" />
            <ElTableColumn prop="name" label="年龄段" min-width="180" />
            <ElTableColumn prop="value" label="人数" width="90" align="center" />
            <ElTableColumn prop="ratio" label="占比" width="180">
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
          <span>近 12 个月入离职趋势</span>
          <span class="card-header__meta">按当前筛选范围统计</span>
        </div>
      </template>
      <div class="table-wrapper">
        <ElTable :data="entryTrend" border stripe size="small" class="table-content table-content--trend">
          <ElTableColumn prop="month" label="月份" width="120" />
          <ElTableColumn prop="entry" label="入职人数" min-width="120" align="center">
            <template #default="{ row }">
              <span class="text-emerald-600 font-600">+{{ row.entry }}</span>
            </template>
          </ElTableColumn>
          <ElTableColumn prop="resign" label="离职人数" min-width="120" align="center">
            <template #default="{ row }">
              <span class="text-rose-500 font-600">-{{ row.resign }}</span>
            </template>
          </ElTableColumn>
          <ElTableColumn prop="net" label="净增长" min-width="120" align="center">
            <template #default="{ row }">
              <span :class="row.net >= 0 ? 'text-emerald-600 font-600' : 'text-rose-500 font-600'">
                {{ row.net >= 0 ? '+' : '' }}{{ row.net }}
              </span>
            </template>
          </ElTableColumn>
        </ElTable>
      </div>
    </ElCard>

    <ElCard v-if="false" class="data-card">
      <template #header>
        <div class="card-header">
          <span>员工明细</span>
          <span class="card-header__meta">当前共 {{ totalEmployees }} 条</span>
        </div>
      </template>

      <div class="table-wrapper">
        <ElTable v-loading="loading" :data="pagedEmployees" border stripe size="small" class="table-content table-content--detail">
          <ElTableColumn prop="companyName" label="公司" width="150" show-overflow-tooltip />
          <ElTableColumn prop="deptName" label="部门" width="160" show-overflow-tooltip />
          <ElTableColumn prop="employeeNo" label="工号" width="110" />
          <ElTableColumn prop="name" label="姓名" width="100" />
          <ElTableColumn prop="gender" label="性别" width="80" align="center">
            <template #default="{ row }">{{ getGenderLabel(row.gender) }}</template>
          </ElTableColumn>
          <ElTableColumn prop="highestEducation" label="学历" width="110">
            <template #default="{ row }">{{ getEducationLabel(row.highestEducation) }}</template>
          </ElTableColumn>
          <ElTableColumn prop="employeeType" label="员工类别" width="120">
            <template #default="{ row }">{{ getEmployeeTypeLabel(row.employeeType) }}</template>
          </ElTableColumn>
          <ElTableColumn prop="entryDate" label="入职日期" width="120" />
          <ElTableColumn prop="leaveDate" label="离职日期" width="120">
            <template #default="{ row }">{{ row.leaveDate || '-' }}</template>
          </ElTableColumn>
          <ElTableColumn prop="phone" label="手机号" width="140" />
          <ElTableColumn prop="status" label="状态" width="90" align="center">
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
