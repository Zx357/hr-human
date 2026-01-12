<script setup lang="ts">
import { onMounted, ref, watch } from 'vue';
import { fetchMonthlyAttendance, type MonthlyAttendance } from '@/service/api/attendance';
import { fetchCompanyList, fetchDepartmentTree } from '@/service/api/organization';

defineOptions({ name: 'MonthlyAttendance' });

const loading = ref(false);
const data = ref<MonthlyAttendance[]>([]);
const companies = ref<any[]>([]);
const departments = ref<any[]>([]);

const currentMonth = ref(new Date().toISOString().slice(0, 7));
const searchParams = ref({
  companyId: undefined as number | undefined,
  deptId: undefined as number | undefined,
  employeeNo: '',
  employeeName: ''
});

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

watch(() => searchParams.value.companyId, (val) => {
  searchParams.value.deptId = undefined;
  loadDepartments(val);
});

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

function handleMonthChange() {
  loadData();
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
            style="width: 140px"
            @change="handleMonthChange"
          />
        </ElFormItem>
        <ElFormItem label="公司">
          <ElSelect v-model="searchParams.companyId" placeholder="请选择公司" clearable style="width: 150px">
            <ElOption v-for="c in companies" :key="c.id" :label="c.companyName" :value="c.id" />
          </ElSelect>
        </ElFormItem>
        <ElFormItem label="部门">
          <ElTreeSelect
            v-model="searchParams.deptId"
            :data="departments"
            :props="{ label: 'deptName', value: 'id', children: 'children' }"
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
          <ElButton type="primary" @click="handleSearch"><icon-ep-search />搜索</ElButton>
          <ElButton @click="handleReset"><icon-ep-refresh />重置</ElButton>
        </ElFormItem>
      </ElForm>
    </ElCard>

    <ElCard class="flex-1">
      <template #header>
        <span>月考勤汇总 - {{ currentMonth }}</span>
      </template>

      <ElTable v-loading="loading" :data="data" border stripe size="small" show-summary>
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
    </ElCard>
  </div>
</template>
