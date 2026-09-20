<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { ElMessage } from 'element-plus';
import {
  type Shift,
  type ShiftPeriod,
  createShift,
  deleteShift,
  fetchShiftList,
  updateShift
} from '@/service/api/shift';
import { $t } from '@/locales';

defineOptions({ name: 'ShiftManage' });

const loading = ref(false);
const data = ref<Shift[]>([]);

const dialogVisible = ref(false);
const operateType = ref<'add' | 'edit'>('add');
const submitLoading = ref(false);
const formData = ref<Shift>({ shiftCode: '', shiftName: '', periods: [] });

// 计算正班时间（小时）
function calcWorkHours(periods?: ShiftPeriod[]): number {
  if (!periods?.length) return 0;
  let totalMinutes = 0;
  for (const p of periods) {
    if (p.startTime && p.endTime) {
      const [sh, sm] = p.startTime.split(':').map(Number);
      const [eh, em] = p.endTime.split(':').map(Number);
      const startMin = sh * 60 + sm;
      let endMin = eh * 60 + em;
      if (p.crossDay === 1 || endMin < startMin) endMin += 24 * 60;
      totalMinutes += endMin - startMin;
    }
  }
  return Math.round((totalMinutes / 60) * 10) / 10;
}

// 计算列表中每行的正班时间
function getRowWorkHours(row: Shift): number {
  if (row.periods?.length) return calcWorkHours(row.periods);
  if (row.workHours) return Number(row.workHours);
  return 0;
}

// 表单中的正班时间（响应式计算）
const computedWorkHours = computed(() => calcWorkHours(formData.value.periods));

async function loadData() {
  loading.value = true;
  try {
    const res = await fetchShiftList();
    data.value = res.data || [];
  } finally {
    loading.value = false;
  }
}

onMounted(() => {
  loadData();
});

function handleAdd() {
  operateType.value = 'add';
  formData.value = {
    shiftCode: '',
    shiftName: '',
    status: 1,
    periods: [
      { periodName: $t('attendance.shift.am'), startTime: '09:00', endTime: '12:00', crossDay: 0, needClockIn: 1, needClockOut: 1 },
      { periodName: $t('attendance.shift.pm'), startTime: '13:00', endTime: '18:00', crossDay: 0, needClockIn: 1, needClockOut: 1 }
    ]
  };
  dialogVisible.value = true;
}

function handleEdit(row: Shift) {
  operateType.value = 'edit';
  const periods = row.periods?.length ? row.periods.map(p => ({ ...p })) : [];
  // 如果没有时段数据，从主表时间生成
  if (!periods.length && row.workStartTime && row.workEndTime) {
    periods.push({
      periodName: $t('attendance.shift.workPeriod'),
      startTime: row.workStartTime,
      endTime: row.workEndTime,
      crossDay: row.isNextDay || 0
    });
  }
  formData.value = { ...row, periods };
  dialogVisible.value = true;
}

async function handleDelete(id: number) {
  try {
    await deleteShift(id);
    ElMessage.success($t('common.deleteSuccess'));
    loadData();
  } catch {
    // 请求层已统一弹错
  }
}

function addPeriod() {
  formData.value.periods?.push({
    periodName: '',
    startTime: '',
    endTime: '',
    crossDay: 0,
    needClockIn: 1,
    needClockOut: 1
  });
}

function removePeriod(index: number) {
  formData.value.periods?.splice(index, 1);
}

async function handleSubmit() {
  if (!formData.value.shiftCode || !formData.value.shiftName) {
    ElMessage.warning($t('attendance.shift.pleaseEnterShiftCodeAndName'));
    return;
  }
  if (!formData.value.periods?.length) {
    ElMessage.warning($t('attendance.shift.pleaseAddAtLeastOnePeriod'));
    return;
  }
  for (const period of formData.value.periods) {
    if (!period.startTime || !period.endTime) {
      ElMessage.warning($t('attendance.shift.pleaseFillInCompletePeriodTimes'));
      return;
    }
  }
  // 用第一个时段的时间作为主表时间
  const firstPeriod = formData.value.periods[0];
  formData.value.workStartTime = firstPeriod.startTime;
  formData.value.workEndTime = formData.value.periods[formData.value.periods.length - 1].endTime;
  formData.value.isNextDay = formData.value.periods.some(p => p.crossDay === 1) ? 1 : 0;
  formData.value.workHours = computedWorkHours.value;

  submitLoading.value = true;
  try {
    if (operateType.value === 'add') {
      await createShift(formData.value);
      ElMessage.success($t('common.addSuccess'));
    } else {
      await updateShift(formData.value);
      ElMessage.success($t('common.updateSuccess'));
    }
    dialogVisible.value = false;
    loadData();
  } catch {
    // 请求层已统一弹错
  } finally {
    submitLoading.value = false;
  }
}

const statusMap: Record<number, { label: string; type: string }> = {
  0: { label: $t('common.disable'), type: 'danger' },
  1: { label: $t('common.enable'), type: 'success' }
};

function formatTime(time?: string) {
  if (!time) return '';
  if (time.length === 8) return time.substring(0, 5);
  return time;
}
</script>

<template>
  <div class="min-h-500px flex-col-stretch gap-16px overflow-hidden lt-sm:overflow-auto">
    <ElCard>
      <template #header>
        <div class="flex items-center justify-between">
          <span>{{ $t('attendance.shift.shiftManagement') }}</span>
          <ElButton v-permission="'attendance:shift:add'" type="primary" @click="handleAdd">
            <template #icon><icon-ep-plus /></template>
            {{ $t('attendance.shift.newShift') }}
          </ElButton>
        </div>
      </template>

      <ElTable v-loading="loading" :data="data" border stripe>
        <ElTableColumn type="index" :label="$t('common.index2')" width="60" align="center" />
        <ElTableColumn prop="shiftCode" :label="$t('attendance.shift.shiftCode')" width="120" />
        <ElTableColumn prop="shiftName" :label="$t('attendance.shift.shiftName')" width="120" />
        <ElTableColumn :label="$t('attendance.shift.workPeriod')" min-width="350">
          <template #default="{ row }">
            <div v-if="row.periods?.length" class="flex flex-wrap gap-8px">
              <ElTag v-for="(period, index) in row.periods" :key="index" size="small">
                {{ period.periodName || $t('attendance.shift.period', { index: index + 1 }) }}: {{ formatTime(period.startTime) }} -
                {{ formatTime(period.endTime) }}
                <span v-if="period.crossDay" class="text-orange-500">{{ $t('attendance.shift.crossDay') }}</span>
              </ElTag>
            </div>
            <div v-else>
              <ElTag size="small">
                {{ formatTime(row.workStartTime) }} - {{ formatTime(row.workEndTime) }}
                <span v-if="row.isNextDay" class="text-orange-500">{{ $t('attendance.shift.crossDay') }}</span>
              </ElTag>
            </div>
          </template>
        </ElTableColumn>
        <ElTableColumn :label="$t('attendance.shift.regularShiftTime')" width="100" align="center">
          <template #default="{ row }">{{ getRowWorkHours(row) }} {{ $t('common.hours') }}</template>
        </ElTableColumn>
        <ElTableColumn prop="status" :label="$t('common.status')" width="80" align="center">
          <template #default="{ row }">
            <ElTag :type="statusMap[row.status]?.type as any">{{ statusMap[row.status]?.label }}</ElTag>
          </template>
        </ElTableColumn>
        <ElTableColumn :label="$t('common.action')" width="150" align="center" fixed="right">
          <template #default="{ row }">
            <ElButton v-permission="'attendance:shift:edit'" type="primary" link size="small" @click="handleEdit(row)">
              {{ $t('common.edit') }}
            </ElButton>
            <ElPopconfirm :title="$t('attendance.shift.areYouSureYouWantToDeleteThisShift')" @confirm="handleDelete(row.id)">
              <template #reference>
                <ElButton v-permission="'attendance:shift:delete'" type="danger" link size="small">{{ $t('common.delete') }}</ElButton>
              </template>
            </ElPopconfirm>
          </template>
        </ElTableColumn>
      </ElTable>
    </ElCard>

    <ElDialog v-model="dialogVisible" :title="operateType === 'add' ? $t('attendance.shift.newShift') : $t('attendance.shift.editShift')" width="750px">
      <ElForm :model="formData" label-position="top">
        <ElRow :gutter="16">
          <ElCol :span="8">
            <ElFormItem :label="$t('attendance.shift.shiftCode')" required>
              <ElInput v-model="formData.shiftCode" :placeholder="$t('attendance.shift.eGDay01')" :disabled="operateType === 'edit'" />
            </ElFormItem>
          </ElCol>
          <ElCol :span="8">
            <ElFormItem :label="$t('attendance.shift.shiftName')" required>
              <ElInput v-model="formData.shiftName" :placeholder="$t('attendance.shift.eGDayShift')" />
            </ElFormItem>
          </ElCol>
          <ElCol :span="4">
            <ElFormItem :label="$t('attendance.shift.regularShiftHours')">
              <div class="work-hours">{{ computedWorkHours }}h</div>
            </ElFormItem>
          </ElCol>
          <ElCol :span="4">
            <ElFormItem :label="$t('common.status')">
              <ElSwitch v-model="formData.status" :active-value="1" :inactive-value="0" />
            </ElFormItem>
          </ElCol>
        </ElRow>

        <div class="period-section">
          <div class="period-header">
            <span>{{ $t('attendance.shift.workPeriod') }}</span>
            <ElButton type="primary" link size="small" @click="addPeriod">
              <icon-ep-plus />
              {{ $t('common.add') }}
            </ElButton>
          </div>

          <div class="period-list">
            <div v-for="(period, index) in formData.periods" :key="index" class="period-item">
              <div class="period-row">
                <span class="period-num">{{ index + 1 }}</span>
                <ElInput v-model="period.periodName" :placeholder="$t('common.name')" class="period-name" />
                <ElTimePicker
                  v-model="period.startTime"
                  format="HH:mm"
                  value-format="HH:mm"
                  :placeholder="$t('common.clockIn')"
                  class="period-time"
                />
                <span class="period-to">{{ $t('common.to') }}</span>
                <ElTimePicker
                  v-model="period.endTime"
                  format="HH:mm"
                  value-format="HH:mm"
                  :placeholder="$t('common.clockOut')"
                  class="period-time"
                />
                <ElCheckbox v-model="period.crossDay" :true-value="1" :false-value="0" class="period-check">
                  {{ $t('attendance.shift.crossDay2') }}
                </ElCheckbox>
                <ElCheckbox v-model="period.needClockIn" :true-value="1" :false-value="0" class="period-check">
                  {{ $t('attendance.shift.clockIn') }}
                </ElCheckbox>
                <ElCheckbox v-model="period.needClockOut" :true-value="1" :false-value="0" class="period-check">
                  {{ $t('attendance.shift.clockOut') }}
                </ElCheckbox>
                <ElButton
                  v-if="formData.periods && formData.periods.length > 1"
                  type="danger"
                  link
                  size="small"
                  class="period-del"
                  @click="removePeriod(index)"
                >
                  <icon-ep-delete />
                </ElButton>
              </div>
            </div>
          </div>
        </div>
      </ElForm>
      <template #footer>
        <ElButton @click="dialogVisible = false">{{ $t('common.cancel') }}</ElButton>
        <ElButton type="primary" :loading="submitLoading" @click="handleSubmit">{{ $t('common.ok') }}</ElButton>
      </template>
    </ElDialog>
  </div>
</template>

<style scoped>
.work-hours {
  font-size: 18px;
  font-weight: 600;
  color: var(--el-color-primary);
  line-height: 32px;
}

.period-section {
  margin-top: 8px;
}

.period-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
  font-weight: 500;
  color: var(--el-text-color-regular);
}

.period-list {
  background: var(--el-fill-color-light);
  border-radius: 6px;
  padding: 12px;
}

.period-item {
  background: var(--el-bg-color);
  border-radius: 4px;
  padding: 10px 12px;
  margin-bottom: 8px;
  border: 1px solid var(--el-border-color-lighter);
}

.period-item:last-child {
  margin-bottom: 0;
}

.period-row {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: nowrap;
}

.period-num {
  width: 20px;
  height: 20px;
  background: var(--el-color-primary);
  color: #fff;
  border-radius: 50%;
  font-size: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.period-name {
  width: 70px;
  flex-shrink: 0;
}

.period-time {
  width: 95px;
  flex-shrink: 0;
}

.period-to {
  color: var(--el-text-color-secondary);
  font-size: 12px;
}

.period-check {
  margin-right: 0;
  flex-shrink: 0;
}

.period-check :deep(.el-checkbox__label) {
  font-size: 12px;
  padding-left: 4px;
}

.period-del {
  margin-left: auto;
}
</style>
