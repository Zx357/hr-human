<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue';
import { ElMessage } from 'element-plus';
import { type ScheduleRow, batchSchedule, fetchWeekSchedule, saveSchedule } from '@/service/api/schedule';
import { type Shift, fetchShiftList } from '@/service/api/shift';
import { useOrgTree } from '@/composables/use-org-tree';
import EmployeePickerDialog from '@/components/common/employee-picker-dialog.vue';
import { $t } from '@/locales';

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
const batchSubmitting = ref(false);
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
  const dayNames = [
    $t('attendance.daily.sun'),
    $t('attendance.daily.mon'),
    $t('attendance.daily.tue'),
    $t('attendance.daily.wed'),
    $t('attendance.daily.thu'),
    $t('attendance.daily.fri'),
    $t('attendance.daily.sat')
  ];

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
  return $t('attendance.schedule.yearMonth', { year, month });
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
  // 未排班单元格背景用主题变量，跟随暗色模式
  if (!shiftId) return 'var(--el-fill-color-lighter)';
  const shift = shifts.value.find(s => s.id === shiftId);
  if (!shift) return 'var(--el-fill-color-lighter)';
  // 按班次的稳定时间字段判定配色（shiftCode/shiftName 为自由命名，不做文本匹配）：
  // 无时段或零工时 → 休息类班次（中性灰）；跨天 → 夜班（红）；午后开班 → 中班（橙）；其余 → 白班（绿）
  const { workStartTime: start, workEndTime: end, workHours, isNextDay } = shift;
  if (!start || !end || !workHours) return '#909399';
  if (isNextDay === 1 || end <= start) return '#F56C6C';
  if (Number(start.slice(0, 2)) >= 12) return '#E6A23C';
  return '#67C23A';
}

async function handleCellClick(row: ScheduleRow, dateKey: string) {
  // 未选择画笔时不允许点击
  if (currentBrush.value === null) {
    ElMessage.warning($t('attendance.schedule.pleaseSelectAShiftFirst'));
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
    // 请求层已统一弹错
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
    ElMessage.warning($t('common.pleaseSelectEmployees'));
    return;
  }
  if (!batchForm.value.shiftId) {
    ElMessage.warning($t('attendance.schedule.pleaseSelectAShift'));
    return;
  }
  if (!batchForm.value.dateRange?.length) {
    ElMessage.warning($t('attendance.common.pleaseSelectDateRange'));
    return;
  }
  batchSubmitting.value = true;
  try {
    await batchSchedule({
      employeeIds: selectedEmployees.value.map(e => e.id),
      shiftId: batchForm.value.shiftId,
      startDate: batchForm.value.dateRange[0],
      endDate: batchForm.value.dateRange[1]
    });
    ElMessage.success($t('attendance.schedule.batchSchedulingSuccessful'));
    batchDialogVisible.value = false;
    loadData();
  } catch {
    // 请求层已统一弹错
  } finally {
    batchSubmitting.value = false;
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
        <ElFormItem :label="$t('common.organization')">
          <ElTreeSelect
            v-model="searchParams.orgIds"
            :data="orgTree"
            :props="{ label: 'unitName', value: 'id', children: 'children' }"
            :placeholder="$t('common.pleaseSelectOrganization')"
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
                <ElCheckbox v-if="node.level === 1" v-model="linkage" @click.stop>
                  {{ $t('common.cascade') }}
                </ElCheckbox>
              </div>
            </template>
            <template #label="{ value }">
              <span>{{ getOrgName(value) }}</span>
            </template>
          </ElTreeSelect>
        </ElFormItem>
        <ElFormItem :label="$t('common.employeeNo')">
          <ElInput
            v-model="searchParams.employeeNo"
            :placeholder="$t('common.employeeNo')"
            clearable
            style="width: 120px"
            @keyup.enter="handleSearch"
          />
        </ElFormItem>
        <ElFormItem :label="$t('common.name')">
          <ElInput
            v-model="searchParams.employeeName"
            :placeholder="$t('common.name')"
            clearable
            style="width: 120px"
            @keyup.enter="handleSearch"
          />
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
            <div v-permission="['attendance:schedule:add', 'attendance:schedule:edit']" class="brush-toolbar">
              <span class="brush-label">{{ $t('attendance.schedule.shift') }}</span>
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
                {{ $t('common.clear') }}
              </span>
            </div>
            <ElButton v-permission="'attendance:schedule:add'" type="primary" @click="handleBatchSchedule">
              <icon-ep-calendar />
              {{ $t('attendance.schedule.batchSchedule') }}
            </ElButton>
          </div>
        </div>
      </template>

      <div class="schedule-table-wrapper">
        <table v-loading="loading" class="schedule-table">
          <thead>
            <tr>
              <th class="fixed-col">{{ $t('common.employeeNo') }}</th>
              <th class="fixed-col">{{ $t('common.name') }}</th>
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
              <td :colspan="monthDates.length + 2" class="empty-row">
                <ElEmpty :description="$t('attendance.schedule.noScheduleData')" :image-size="80" />
              </td>
            </tr>
          </tbody>
        </table>
      </div>
      <div class="mt-8px text-xs text-gray-400">
        {{ $t('attendance.schedule.note')
        }}{{
          currentBrush !== null
            ? currentBrush === -1
              ? $t('attendance.schedule.clickACellToClearItsSchedule')
              : $t('attendance.schedule.clickACellToApplyTheSelectedShift')
            : $t('attendance.schedule.selectAShiftAboveBeforeSchedulingCells')
        }}
      </div>
    </ElCard>

    <ElDialog v-model="batchDialogVisible" :title="$t('attendance.schedule.batchSchedule')" width="600px">
      <ElForm label-width="100px" :model="batchForm">
        <ElFormItem :label="$t('common.selectEmployees')" required>
          <div class="w-full flex gap-8px">
            <div
              class="min-h-32px flex flex-1 flex-wrap cursor-pointer items-center gap-4px border border-[var(--el-border-color)] rounded-4px px-8px py-4px hover:border-primary"
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
              <span v-else class="text-gray-400">{{ $t('common.pleaseSelectEmployees') }}</span>
            </div>
            <ElButton type="primary" @click="employeeDialogVisible = true">{{ $t('common.selectEmployees') }}</ElButton>
          </div>
        </ElFormItem>
        <ElFormItem :label="$t('attendance.schedule.selectShift')" required>
          <ElSelect
            v-model="batchForm.shiftId"
            :placeholder="$t('attendance.schedule.pleaseSelectAShift')"
            style="width: 100%"
          >
            <ElOption v-for="shift in shifts" :key="shift.id" :label="shift.shiftName" :value="shift.id!" />
          </ElSelect>
        </ElFormItem>
        <ElFormItem :label="$t('common.dateRange')" required>
          <ElDatePicker
            v-model="batchForm.dateRange"
            type="daterange"
            :range-separator="$t('common.to')"
            :start-placeholder="$t('attendance.clock.start')"
            :end-placeholder="$t('attendance.clock.end')"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </ElFormItem>
      </ElForm>
      <template #footer>
        <ElButton @click="batchDialogVisible = false">{{ $t('common.cancel') }}</ElButton>
        <ElButton type="primary" :loading="batchSubmitting" @click="handleBatchSubmit">{{ $t('common.ok') }}</ElButton>
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
  color: var(--el-text-color-regular);
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
  border: 1px solid var(--el-border-color-lighter);
  padding: 4px;
  text-align: center;
  white-space: nowrap;
}

.schedule-table th {
  background: var(--el-fill-color-light);
  font-weight: 500;
  position: sticky;
  top: 0;
  z-index: 2;
}

.schedule-table .fixed-col {
  position: sticky;
  left: 0;
  background: var(--el-bg-color);
  z-index: 1;
  min-width: 60px;
}

.schedule-table th.fixed-col {
  z-index: 3;
  background: var(--el-fill-color-light);
}

.schedule-table th.fixed-col:nth-child(2),
.schedule-table td.fixed-col:nth-child(2) {
  left: 60px;
}

.schedule-table .weekend {
  background: var(--el-fill-color-lighter);
}

.schedule-table th.weekend {
  background: var(--el-fill-color-dark);
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
  color: var(--el-text-color-secondary);
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
  padding: 8px;
}

.brush-toolbar {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 12px;
  background: var(--el-fill-color-light);
  border-radius: 6px;
}

.brush-label {
  font-size: 13px;
  color: var(--el-text-color-regular);
  font-weight: 500;
}

.brush-item {
  padding: 4px 10px;
  font-size: 12px;
  border-radius: 4px;
  cursor: pointer;
  background: var(--el-bg-color);
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
