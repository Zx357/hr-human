<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue';
import { ElMessage } from 'element-plus';
import { type ScheduleRow, batchSchedule, fetchWeekSchedule, saveSchedule } from '@/service/api/schedule';
import { type Shift, fetchShiftList } from '@/service/api/shift';
import { useOrgTree } from '@/composables/use-org-tree';
import EmployeePickerDialog from '@/components/common/EmployeePickerDialog.vue';

defineOptions({ name: 'ScheduleManage' });

const loading = ref(false);
const currentMonth = ref(new Date());
const scheduleData = ref<ScheduleRow[]>([]);
const shifts = ref<Shift[]>([]);
const employeeDialogVisible = ref(false);
const { orgTreeOptions: orgTree, loadOrgTree } = useOrgTree();

// 字典数据

// 当前选中的画笔班次 (null=循环切换, -1=清除, 其他=指定班次ID)
const currentBrush = ref<number | null>(null);

function selectBrush(shiftId: number | null) {
  currentBrush.value = currentBrush.value === shiftId ? null : shiftId;
}

function isBrushActive(shiftId: number | null) {
  return currentBrush.value === shiftId;
}

const searchParams = ref({
  orgIds: [] as number[],
  employeeNo: '',
  employeeName: ''
});

const linkage = ref(false); // 默认不联动

const batchDialogVisible = ref(false);
const batchForm = ref({
  shiftId: undefined as number | undefined,
  dateRange: [] as string[]
});

// 批量排班-员工选择相关
const selectedEmployees = ref<
  Array<{ id: number; name: string; employeeNo: string; companyName?: string; deptName?: string }>
>([]);
// 计算当前月的所有日期
const monthDates = computed(() => {
  const year = currentMonth.value.getFullYear();
  const month = currentMonth.value.getMonth();
  const lastDay = new Date(year, month + 1, 0);

  const dates = [];
  const dayNames = ['日', '一', '二', '三', '四', '五', '六'];

  for (let d = 1; d <= lastDay.getDate(); d += 1) {
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

function getOrgName(id: number): string {
  const find = (nodes: any[]): string => {
    for (const node of nodes) {
      if (node.id === id) return node.unitName;
      if (node.children?.length) {
        const found = find(node.children);
        if (found) return found;
      }
    }
    return '';
  };
  return find(orgTree.value);
}
async function loadData() {
  if (!startDate.value || !endDate.value) return;
  loading.value = true;
  try {
    const res = await fetchWeekSchedule({
      orgIds: searchParams.value.orgIds,
      employeeNo: searchParams.value.employeeNo,
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
  loadOrgTree();
  loadData();
});

watch(currentMonth, () => loadData());

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
      Reflect.deleteProperty(row.schedule, dateKey);
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
  batchForm.value = { shiftId: undefined, dateRange: [] };
  selectedEmployees.value = [];
  batchDialogVisible.value = true;
}
function removeSelectedEmployee(index: number) {
  selectedEmployees.value.splice(index, 1);
}

function handleConfirmEmployees(selected: Api.Hr.Employee[]) {
  selectedEmployees.value = selected.map(row => ({
    id: row.id!,
    name: row.name!,
    employeeNo: row.employeeNo!,
    companyName: row.companyName || '',
    deptName: row.deptName || ''
  }));
}

async function handleBatchSubmit() {
  if (!selectedEmployees.value.length) {
    ElMessage.warning('请选择员工');
    return;
  }
  if (!batchForm.value.shiftId) {
    ElMessage.warning('请选择班次');
    return;
  }
  if (!batchForm.value.dateRange?.length) {
    ElMessage.warning('请选择日期范围');
    return;
  }
  try {
    await batchSchedule({
      employeeIds: selectedEmployees.value.map(e => e.id),
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

function handleSearch() {
  loadData();
}
function handleReset() {
  searchParams.value = { orgIds: [], employeeNo: '', employeeName: '' };
  loadData();
}
</script>

<template>
  <div class="min-h-500px flex-col-stretch gap-16px overflow-hidden lt-sm:overflow-auto">
    <ElCard>
      <ElForm inline :model="searchParams">
        <ElFormItem label="组织">
          <ElTreeSelect
            v-model="searchParams.orgIds"
            :data="orgTree"
            :props="{ label: 'unitName', value: 'id', children: 'children' }"
            placeholder="请选择组织"
            clearable
            multiple
            show-checkbox
            :check-strictly="!linkage"
            collapse-tags
            collapse-tags-tooltip
            :render-after-expand="false"
            style="width: 280px"
          >
            <template #default="{ node, data }">
              <div class="tree-node-content">
                <span>{{ data.unitName }}</span>
                <ElCheckbox v-if="node.level === 1" v-model="linkage" @click.stop>联动</ElCheckbox>
              </div>
            </template>
            <template #label="{ value }">
              <span>{{ getOrgName(value) }}</span>
            </template>
          </ElTreeSelect>
        </ElFormItem>
        <ElFormItem label="工号">
          <ElInput v-model="searchParams.employeeNo" placeholder="工号" clearable style="width: 120px" />
        </ElFormItem>
        <ElFormItem label="姓名">
          <ElInput v-model="searchParams.employeeName" placeholder="姓名" clearable style="width: 120px" />
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

    <ElCard class="schedule-card flex-1">
      <template #header>
        <div class="flex flex-wrap items-center justify-between gap-12px">
          <div class="flex items-center gap-12px">
            <ElButton @click="handlePrevMonth"><icon-ep-arrow-left /></ElButton>
            <span class="text-lg font-bold">{{ monthLabel }}</span>
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
              <span class="brush-item brush-clear" :class="{ active: isBrushActive(-1) }" @click="selectBrush(-1)">
                清除
              </span>
            </div>
            <ElButton type="primary" @click="handleBatchSchedule">
              <icon-ep-calendar />
              批量排班
            </ElButton>
          </div>
        </div>
      </template>

      <div class="schedule-table-wrapper">
        <table v-loading="loading" class="schedule-table">
          <thead>
            <tr>
              <th class="fixed-col">工号</th>
              <th class="fixed-col">姓名</th>
              <th v-for="d in monthDates" :key="d.key" :class="{ weekend: d.isWeekend }">
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
      <div class="mt-8px text-xs text-gray-400">
        提示：{{
          currentBrush !== null
            ? currentBrush === -1
              ? '点击单元格清除排班'
              : '点击单元格应用选中班次'
            : '请先选择上方班次后才能点击单元格排班'
        }}
      </div>
    </ElCard>

    <ElDialog v-model="batchDialogVisible" title="批量排班" width="600px">
      <ElForm label-width="100px" :model="batchForm">
        <ElFormItem label="选择员工" required>
          <div class="w-full flex gap-8px">
            <div
              class="min-h-32px flex flex-1 flex-wrap cursor-pointer items-center gap-4px border border-gray-300 rounded-4px px-8px py-4px hover:border-blue-500"
              @click="employeeDialogVisible = true"
            >
              <template v-if="selectedEmployees.length > 0">
                <ElTag
                  v-for="(emp, index) in selectedEmployees"
                  :key="emp.id"
                  closable
                  size="small"
                  @close.stop="removeSelectedEmployee(index)"
                >
                  {{ emp.name }} ({{ emp.employeeNo }})
                </ElTag>
              </template>
              <span v-else class="text-gray-400">请选择员工</span>
            </div>
            <ElButton type="primary" @click="employeeDialogVisible = true">选择员工</ElButton>
          </div>
        </ElFormItem>
        <ElFormItem label="选择班次" required>
          <ElSelect v-model="batchForm.shiftId" placeholder="请选择班次" style="width: 100%">
            <ElOption v-for="shift in shifts" :key="shift.id" :label="shift.shiftName" :value="shift.id!" />
          </ElSelect>
        </ElFormItem>
        <ElFormItem label="日期范围" required>
          <ElDatePicker
            v-model="batchForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始"
            end-placeholder="结束"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </ElFormItem>
      </ElForm>
      <template #footer>
        <ElButton @click="batchDialogVisible = false">取消</ElButton>
        <ElButton type="primary" @click="handleBatchSubmit">确定</ElButton>
      </template>
    </ElDialog>

    <EmployeePickerDialog v-model="employeeDialogVisible" multiple @confirm="handleConfirmEmployees" />
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
  border-color: var(--brush-color, #409eff);
}

.brush-item.active {
  border-color: var(--brush-color, #409eff);
  background: var(--brush-color, #409eff);
  color: #fff;
}

.brush-item.brush-clear {
  --brush-color: #909399;
}

:deep(.el-tree-node__content) {
  width: 100%;
}

.tree-node-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex: 1;
  padding-right: 8px;
}
</style>
