<script setup lang="ts">
import { onMounted, ref, watch } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { fetchClockRecordPage, saveClockRecord, deleteClockRecord, type AttClockRecord } from '@/service/api/attendance';
import { fetchEmployeeList } from '@/service/api/hr';
import { fetchCompanyList } from '@/service/api/organization';
import { fetchDepartmentTree } from '@/service/api/organization';

defineOptions({ name: 'ClockRecord' });

const loading = ref(false);
const data = ref<AttClockRecord[]>([]);
const employees = ref<any[]>([]);
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
const dialogForm = ref<AttClockRecord>({ employeeId: undefined as any, clockTime: '', clockType: 1, clockMethod: 3 });

const clockTypeOptions = [
  { value: 1, label: '上班打卡', type: 'success' },
  { value: 2, label: '下班打卡', type: 'warning' }
];

const clockMethodOptions = [
  { value: 1, label: 'APP' },
  { value: 2, label: '考勤机' },
  { value: 3, label: '手动补卡' }
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

watch(() => searchParams.value.companyId, (val) => {
  searchParams.value.deptId = undefined;
  loadDepartments(val);
});

async function loadEmployees() {
  const res = await fetchEmployeeList({});
  employees.value = res.data || [];
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
  loadEmployees();
  // 默认查询今天
  const today = new Date().toISOString().slice(0, 10);
  searchParams.value.dateRange = [today, today];
  loadData();
});

function handleSearch() {
  pagination.value.current = 1;
  loadData();
}

function handleReset() {
  const today = new Date().toISOString().slice(0, 10);
  searchParams.value = { companyId: undefined, deptId: undefined, employeeName: '', dateRange: [today, today] };
  departments.value = [];
  handleSearch();
}

function handleAdd() {
  const now = new Date();
  dialogForm.value = {
    employeeId: undefined as any,
    clockTime: now.toISOString().slice(0, 16).replace('T', ' '),
    clockType: 1,
    clockMethod: 3
  };
  dialogVisible.value = true;
}

async function handleSubmit() {
  if (!dialogForm.value.employeeId) {
    ElMessage.warning('请选择员工');
    return;
  }
  if (!dialogForm.value.clockTime) {
    ElMessage.warning('请选择打卡时间');
    return;
  }
  await saveClockRecord(dialogForm.value);
  ElMessage.success('保存成功');
  dialogVisible.value = false;
  loadData();
}

async function handleDelete(row: AttClockRecord) {
  await ElMessageBox.confirm('确定删除该打卡记录吗？', '提示');
  await deleteClockRecord(row.id!);
  ElMessage.success('删除成功');
  loadData();
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

function formatDateTime(dt: string) {
  if (!dt) return '-';
  return dt.replace('T', ' ').slice(0, 16);
}
</script>

<template>
  <div class="list-page">
    <ElCard class="search-card">
      <ElForm inline :model="searchParams">
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
        <ElFormItem label="员工">
          <ElInput v-model="searchParams.employeeName" placeholder="员工姓名" clearable style="width: 120px" />
        </ElFormItem>
        <ElFormItem>
          <ElButton type="primary" @click="handleSearch"><icon-ep-search />搜索</ElButton>
          <ElButton @click="handleReset"><icon-ep-refresh />重置</ElButton>
        </ElFormItem>
      </ElForm>
    </ElCard>

    <ElCard class="table-card">
      <template #header>
        <div class="flex items-center justify-between">
          <span>打卡记录</span>
          <ElButton v-permission="'attendance:clock:add'" type="primary" @click="handleAdd">
            <template #icon><icon-ep-plus /></template>
            补卡
          </ElButton>
        </div>
      </template>

      <div class="table-wrapper">
        <ElTable v-loading="loading" :data="data" border stripe size="small" height="100%">
        <ElTableColumn type="index" label="#" width="50" align="center" />
        <ElTableColumn prop="companyName" label="公司" width="120" show-overflow-tooltip />
        <ElTableColumn prop="employeeNo" label="工号" width="100" />
        <ElTableColumn prop="employeeName" label="姓名" width="80" />
        <ElTableColumn prop="deptName" label="部门" width="120" />
        <ElTableColumn prop="clockTime" label="打卡时间" width="160">
          <template #default="{ row }">{{ formatDateTime(row.clockTime) }}</template>
        </ElTableColumn>
        <ElTableColumn prop="clockType" label="类型" width="100" align="center">
          <template #default="{ row }">
            <ElTag :type="row.clockType === 1 ? 'success' : 'warning'" size="small">
              {{ row.clockType === 1 ? '上班' : '下班' }}
            </ElTag>
          </template>
        </ElTableColumn>
        <ElTableColumn prop="clockMethod" label="方式" width="80" align="center">
          <template #default="{ row }">
            {{ clockMethodOptions.find(o => o.value === row.clockMethod)?.label || '-' }}
          </template>
        </ElTableColumn>
        <ElTableColumn prop="location" label="地点" min-width="150" show-overflow-tooltip />
        <ElTableColumn prop="remark" label="备注" min-width="120" show-overflow-tooltip />
        <ElTableColumn label="操作" width="80" align="center">
          <template #default="{ row }">
            <ElButton v-permission="'attendance:clock:delete'" type="danger" link size="small" @click="handleDelete(row)">删除</ElButton>
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
    <ElDialog v-model="dialogVisible" title="补卡" width="450px">
      <ElForm label-width="80px" :model="dialogForm">
        <ElFormItem label="员工" required>
          <ElSelect v-model="dialogForm.employeeId" placeholder="请选择员工" filterable style="width: 100%">
            <ElOption v-for="emp in employees" :key="emp.id" :label="`${emp.name} (${emp.employeeNo})`" :value="emp.id" />
          </ElSelect>
        </ElFormItem>
        <ElFormItem label="打卡时间" required>
          <ElDatePicker v-model="dialogForm.clockTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" format="YYYY-MM-DD HH:mm" style="width: 100%" />
        </ElFormItem>
        <ElFormItem label="类型" required>
          <ElRadioGroup v-model="dialogForm.clockType">
            <ElRadio :value="1">上班打卡</ElRadio>
            <ElRadio :value="2">下班打卡</ElRadio>
          </ElRadioGroup>
        </ElFormItem>
        <ElFormItem label="备注">
          <ElInput v-model="dialogForm.remark" placeholder="补卡原因" />
        </ElFormItem>
      </ElForm>
      <template #footer>
        <ElButton @click="dialogVisible = false">取消</ElButton>
        <ElButton type="primary" @click="handleSubmit">确定</ElButton>
      </template>
    </ElDialog>
  </div>
</template>
