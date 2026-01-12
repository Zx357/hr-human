<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue';
import { ElMessage } from 'element-plus';
import { fetchWeekSchedule, saveSchedule, batchSchedule, type ScheduleRow } from '@/service/api/schedule';
import { fetchShiftList, type Shift } from '@/service/api/shift';
import { fetchCompanyList, fetchDepartmentTree } from '@/service/api/organization';
import { fetchEmployeeList } from '@/service/api/hr';

defineOptions({ name: 'ScheduleManage' });

const loading = ref(false);
const currentMonth = ref(new Date());
const scheduleData = ref<ScheduleRow[]>([]);
const shifts = ref<Shift[]>([]);
const companies = ref<any[]>([]);
const departments = ref<any[]>([]);
const employees = ref<any[]>([]);

// 当前选中的画笔班次 (null=循环切换, -1=清除, 其他=指定班次ID)
const currentBrush = ref<number | null>(null);

function selectBrush(shiftId: number | null) {
  currentBrush.value = currentBrush.value === shiftId ? null : shiftId;
}

function isBrushActive(shiftId: number | null) {
  return currentBrush.value === shiftId;
}

const searchParams = ref({
  companyId: undefined as number | undefined,
  deptId: undefined as number | undefined,
  employeeName: ''
});

const batchDialogVisible = ref(false);
const batchForm = ref({
  employeeIds: [] as number[],
  shiftId: undefined as number | undefined,
  dateRange: [] as string[]
});

// 计算当前月的所有日期
const monthDates = computed(() => {
  const year = currentMonth.value.getFullYear();
  const month = currentMonth.value.getMonth();
  const firstDay = new Date(year, month, 1);
  const lastDay = new Date(year, month + 1, 0);

  const dates = [];
  const dayNames = ['日', '一', '二', '三', '四', '五', '六'];

  for (let d = 1; d <= lastDay.getDate(); d++) {
    const date = new Date(year, month, d);
    const dayOfWeek = date.getDay();
    dates.push({
      key: `${year}-${String(month + 1).padStart(2, '0')}-${String(d).padStart(2, '0')}`,
      day: d,
      weekDay: dayNames[dayOfWeek],
      isWeekend: dayOfWeek === 0 || dayOfWeek === 6
    });
  }
  return dates;
});

const startDate = computed(() => monthDates.value[0]?.key || '');
const endDate = computed(() => monthDates.value[monthDates.value.length - 1]?.key || '');

const monthLabel = computed(() => {
  const year = currentMonth.value.getFullYear();
  const month = currentMonth.value.getMonth() + 1;
  return `${year}年${month}月`;
});

async function loadShifts() {
  const res = await fetchShiftList();
  shifts.value = res.data || [];
}

async function loadCompanies() {
  const res = await fetchCompanyList();
  companies.value = res.data || [];
}

async function loadDepartments() {
  const res = await fetchDepartmentTree(searchParams.value.companyId);
  departments.value = res.data || [];
}

async function loadEmployees() {
  const res = await fetchEmployeeList({ deptId: searchParams.value.deptId });
  employees.value = res.data || [];
}

async function loadData() {
  if (!startDate.value || !endDate.value) return;
  loading.value = true;
  try {
    const res = await fetchWeekSchedule({
      deptId: searchParams.value.deptId,
      employeeName: searchParams.value.employeeName,
      startDate: startDate.value,
      endDate: endDate.value
    });
    scheduleData.value = res.data || [];
  } finally {
    loading.value = false;
  }
}

onMounted(() => {
  loadShifts();
  loadCompanies();
  loadDepartments();
  loadEmployees();
  loadData();
});

watch(currentMonth, () => loadData());
watch(() => searchParams.value.companyId, () => {
  searchParams.value.deptId = undefined;
  loadDepartments();
});

function getShiftColor(shiftId?: number) {
  if (!shiftId) return '#f5f5f5';
  const shift = shifts.value.find(s => s.id === shiftId);
  if (!shift) return '#f5f5f5';
  const name = shift.shiftName || '';
  if (name.includes('休') || name.includes('假')) return '#909399';
  if (name.includes('早') || name.includes('白') || name.includes('标准')) return '#67C23A';
  if (name.includes('中')) return '#E6A23C';
  if (name.includes('晚') || name.includes('夜')) return '#F56C6C';
  return '#409EFF';
}

async function handleCellClick(row: ScheduleRow, dateKey: string) {
  // 未选择画笔时不允许点击
  if (currentBrush.value === null) {
    ElMessage.warning('请先选择一个班次');
    return;
  }

  let nextShiftId: number | null | undefined;

  if (currentBrush.value === -1) {
    // 清除模式
    nextShiftId = null;
  } else {
    nextShiftId = currentBrush.value;
  }

  try {
    await saveSchedule({ employeeId: row.employeeId, shiftId: nextShiftId, scheduleDate: dateKey });
    if (nextShiftId) {
      const shift = shifts.value.find(s => s.id === nextShiftId);
      if (shift) {
        row.schedule[dateKey] = { shiftId: shift.id!, shiftName: shift.shiftName, shiftCode: shift.shiftCode };
      }
    } else {
      delete row.schedule[dateKey];
    }
  } catch {
    ElMessage.error('保存失败');
  }
}

function handlePrevMonth() {
  const date = new Date(currentMonth.value);
  date.setMonth(date.getMonth() - 1);
  currentMonth.value = date;
}

function handleNextMonth() {
  const date = new Date(currentMonth.value);
  date.setMonth(date.getMonth() + 1);
  currentMonth.value = date;
}

function handleBatchSchedule() {
  batchForm.value = { employeeIds: [], shiftId: undefined, dateRange: [] };
  batchDialogVisible.value = true;
}

async function handleBatchSubmit() {
  if (!batchForm.value.employeeIds.length) return ElMessage.warning('请选择员工');
  if (!batchForm.value.shiftId) return ElMessage.warning('请选择班次');
  if (!batchForm.value.dateRange?.length) return ElMessage.warning('请选择日期范围');
  try {
    await batchSchedule({
      employeeIds: batchForm.value.employeeIds,
      shiftId: batchForm.value.shiftId,
      startDate: batchForm.value.dateRange[0],
      endDate: batchForm.value.dateRange[1]
    });
    ElMessage.success('批量排班成功');
    batchDialogVisible.value = false;
    loadData();
  } catch {
    ElMessage.error('批量排班失败');
  }
}

function handleSearch() { loadData(); }
function handleReset() {
  searchParams.value = { companyId: undefined, deptId: undefined, employeeName: '' };
  loadDepartments();
  loadData();
}
</script>

<template>
  <div class="min-h-500px flex-col-stretch gap-16px overflow-hidden lt-sm:overflow-auto">
    <ElCard>
      <ElForm inline :model="searchParams">
        <ElFormItem label="公司">
          <ElSelect v-model="searchParams.companyId" placeholder="请选择公司" clearable style="width: 180px">
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
            style="width: 180px"
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

    <ElCard class="flex-1 schedule-card">
      <template #header>
        <div class="flex items-center justify-between flex-wrap gap-12px">
          <div class="flex items-center gap-12px">
            <ElButton @click="handlePrevMonth"><icon-ep-arrow-left /></ElButton>
            <span class="font-bold text-lg">{{ monthLabel }}</span>
            <ElButton @click="handleNextMonth"><icon-ep-arrow-right /></ElButton>
          </div>
          <div class="flex items-center gap-16px">
            <!-- 画笔工具栏 -->
            <div class="brush-toolbar">
              <span class="brush-label">班次:</span>
              <span
                v-for="shift in shifts"
                :key="shift.id"
                class="brush-item"
                :class="{ active: isBrushActive(shift.id!) }"
                :style="{ '--brush-color': getShiftColor(shift.id) }"
                @click="selectBrush(shift.id!)"
              >
                {{ shift.shiftName }}
              </span>
              <span
                class="brush-item brush-clear"
                :class="{ active: isBrushActive(-1) }"
                @click="selectBrush(-1)"
              >
                清除
              </span>
            </div>
            <ElButton type="primary" @click="handleBatchSchedule"><icon-ep-calendar />批量排班</ElButton>
          </div>
        </div>
      </template>

      <div class="schedule-table-wrapper">
        <table class="schedule-table" v-loading="loading">
          <thead>
            <tr>
              <th class="fixed-col">工号</th>
              <th class="fixed-col">姓名</th>
              <th
                v-for="d in monthDates"
                :key="d.key"
                :class="{ weekend: d.isWeekend }"
              >
                <div class="date-header">
                  <span class="date-day">{{ d.day }}</span>
                  <span class="date-week">{{ d.weekDay }}</span>
                </div>
              </th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="row in scheduleData" :key="row.employeeId">
              <td class="fixed-col">{{ row.employeeNo }}</td>
              <td class="fixed-col">{{ row.employeeName }}</td>
              <td
                v-for="d in monthDates"
                :key="d.key"
                :class="{ weekend: d.isWeekend, disabled: currentBrush === null }"
                @click="handleCellClick(row, d.key)"
              >
                <div
                  class="schedule-cell"
                  :class="{ disabled: currentBrush === null }"
                  :style="{ backgroundColor: getShiftColor(row.schedule[d.key]?.shiftId) }"
                >
                  {{ row.schedule[d.key]?.shiftName?.charAt(0) || '' }}
                </div>
              </td>
            </tr>
            <tr v-if="!scheduleData.length && !loading">
              <td :colspan="monthDates.length + 2" class="empty-row">暂无数据</td>
            </tr>
          </tbody>
        </table>
      </div>
      <div class="mt-8px text-gray-400 text-xs">
        提示：{{ currentBrush !== null ? (currentBrush === -1 ? '点击单元格清除排班' : '点击单元格应用选中班次') : '请先选择上方班次后才能点击单元格排班' }}
      </div>
    </ElCard>

    <ElDialog v-model="batchDialogVisible" title="批量排班" width="500px">
      <ElForm label-width="100px" :model="batchForm">
        <ElFormItem label="选择员工" required>
          <ElSelect v-model="batchForm.employeeIds" placeholder="请选择员工" multiple filterable style="width: 100%">
            <ElOption v-for="emp in employees" :key="emp.id" :label="`${emp.name} (${emp.employeeNo})`" :value="emp.id" />
          </ElSelect>
        </ElFormItem>
        <ElFormItem label="选择班次" required>
          <ElSelect v-model="batchForm.shiftId" placeholder="请选择班次" style="width: 100%">
            <ElOption v-for="shift in shifts" :key="shift.id" :label="shift.shiftName" :value="shift.id" />
          </ElSelect>
        </ElFormItem>
        <ElFormItem label="日期范围" required>
          <ElDatePicker v-model="batchForm.dateRange" type="daterange" range-separator="至" start-placeholder="开始" end-placeholder="结束" value-format="YYYY-MM-DD" style="width: 100%" />
        </ElFormItem>
      </ElForm>
      <template #footer>
        <ElButton @click="batchDialogVisible = false">取消</ElButton>
        <ElButton type="primary" @click="handleBatchSubmit">确定</ElButton>
      </template>
    </ElDialog>
  </div>
</template>

<style scoped>
.schedule-card :deep(.el-card__body) {
  padding: 12px;
}

.shift-legend {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #606266;
}

.shift-dot {
  width: 12px;
  height: 12px;
  border-radius: 2px;
}

.schedule-table-wrapper {
  overflow-x: auto;
  max-height: calc(100vh - 320px);
}

.schedule-table {
  border-collapse: collapse;
  font-size: 12px;
  min-width: 100%;
}

.schedule-table th,
.schedule-table td {
  border: 1px solid #ebeef5;
  padding: 4px;
  text-align: center;
  white-space: nowrap;
}

.schedule-table th {
  background: #f5f7fa;
  font-weight: 500;
  position: sticky;
  top: 0;
  z-index: 2;
}

.schedule-table .fixed-col {
  position: sticky;
  left: 0;
  background: #fff;
  z-index: 1;
  min-width: 60px;
}

.schedule-table th.fixed-col {
  z-index: 3;
  background: #f5f7fa;
}

.schedule-table th.fixed-col:nth-child(2),
.schedule-table td.fixed-col:nth-child(2) {
  left: 60px;
}

.schedule-table .weekend {
  background: #fafafa;
}

.schedule-table th.weekend {
  background: #f0f0f0;
}

.date-header {
  display: flex;
  flex-direction: column;
  line-height: 1.2;
}

.date-day {
  font-size: 13px;
  font-weight: 600;
}

.date-week {
  font-size: 10px;
  color: #909399;
}

.schedule-cell {
  width: 24px;
  height: 24px;
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto;
  cursor: pointer;
  color: #fff;
  font-size: 11px;
  font-weight: 500;
  transition: transform 0.15s;
}

.schedule-cell:hover {
  transform: scale(1.1);
}

.schedule-cell.disabled {
  cursor: not-allowed;
  opacity: 0.6;
}

.schedule-cell.disabled:hover {
  transform: none;
}

.schedule-table td.disabled {
  cursor: not-allowed;
}

.empty-row {
  padding: 40px !important;
  color: #909399;
}

.brush-toolbar {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 12px;
  background: #f5f7fa;
  border-radius: 6px;
}

.brush-label {
  font-size: 13px;
  color: #606266;
  font-weight: 500;
}

.brush-item {
  padding: 4px 10px;
  font-size: 12px;
  border-radius: 4px;
  cursor: pointer;
  background: #fff;
  border: 2px solid transparent;
  transition: all 0.2s;
  position: relative;
}

.brush-item:hover {
  border-color: var(--brush-color, #409EFF);
}

.brush-item.active {
  border-color: var(--brush-color, #409EFF);
  background: var(--brush-color, #409EFF);
  color: #fff;
}

.brush-item.brush-clear {
  --brush-color: #909399;
}
</style>
