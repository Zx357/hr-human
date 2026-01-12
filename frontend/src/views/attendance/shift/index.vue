<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { ElMessage } from 'element-plus';
import { fetchShiftList, createShift, updateShift, deleteShift, type Shift, type ShiftPeriod } from '@/service/api/shift';

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
    if (!p.startTime || !p.endTime) continue;
    const [sh, sm] = p.startTime.split(':').map(Number);
    const [eh, em] = p.endTime.split(':').map(Number);
    let startMin = sh * 60 + sm;
    let endMin = eh * 60 + em;
    if (p.crossDay === 1 || endMin < startMin) endMin += 24 * 60;
    totalMinutes += endMin - startMin;
  }
  return Math.round(totalMinutes / 60 * 10) / 10;
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
  } finally { loading.value = false; }
}

onMounted(() => { loadData(); });

function handleAdd() {
  operateType.value = 'add';
  formData.value = { 
    shiftCode: '', 
    shiftName: '', 
    status: 1, 
    periods: [{ periodName: '上午', startTime: '09:00', endTime: '12:00', crossDay: 0, needClockIn: 1, needClockOut: 1 }, { periodName: '下午', startTime: '13:00', endTime: '18:00', crossDay: 0, needClockIn: 1, needClockOut: 1 }] 
  };
  dialogVisible.value = true;
}

function handleEdit(row: Shift) {
  operateType.value = 'edit';
  const periods = row.periods?.length ? row.periods.map(p => ({ ...p })) : [];
  // 如果没有时段数据，从主表时间生成
  if (!periods.length && row.workStartTime && row.workEndTime) {
    periods.push({ periodName: '工作时段', startTime: row.workStartTime, endTime: row.workEndTime, crossDay: row.isNextDay || 0 });
  }
  formData.value = { ...row, periods };
  dialogVisible.value = true;
}

async function handleDelete(id: number) {
  try {
    await deleteShift(id);
    ElMessage.success('删除成功');
    loadData();
  } catch { ElMessage.error('删除失败'); }
}

function addPeriod() {
  formData.value.periods?.push({ periodName: '', startTime: '', endTime: '', crossDay: 0, needClockIn: 1, needClockOut: 1 });
}

function removePeriod(index: number) {
  formData.value.periods?.splice(index, 1);
}

async function handleSubmit() {
  if (!formData.value.shiftCode || !formData.value.shiftName) {
    ElMessage.warning('请填写班次编码和名称');
    return;
  }
  if (!formData.value.periods?.length) {
    ElMessage.warning('请至少添加一个时段');
    return;
  }
  for (const period of formData.value.periods) {
    if (!period.startTime || !period.endTime) {
      ElMessage.warning('请填写完整的时段时间');
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
      ElMessage.success('新增成功');
    } else {
      await updateShift(formData.value);
      ElMessage.success('更新成功');
    }
    dialogVisible.value = false;
    loadData();
  } catch { ElMessage.error('保存失败'); }
  finally { submitLoading.value = false; }
}

const statusMap: Record<number, { label: string; type: string }> = {
  0: { label: '禁用', type: 'danger' },
  1: { label: '启用', type: 'success' }
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
          <span>班次管理</span>
          <ElButton v-permission="'attendance:shift:add'" type="primary" @click="handleAdd">
            <template #icon><icon-ep-plus /></template>新增班次
          </ElButton>
        </div>
      </template>

      <ElTable v-loading="loading" :data="data" border stripe>
        <ElTableColumn type="index" label="序号" width="60" align="center" />
        <ElTableColumn prop="shiftCode" label="班次编码" width="120" />
        <ElTableColumn prop="shiftName" label="班次名称" width="120" />
        <ElTableColumn label="工作时段" min-width="350">
          <template #default="{ row }">
            <div v-if="row.periods?.length" class="flex flex-wrap gap-8px">
              <ElTag v-for="(period, index) in row.periods" :key="index" size="small">
                {{ period.periodName || `时段${index + 1}` }}: {{ formatTime(period.startTime) }} - {{ formatTime(period.endTime) }}
                <span v-if="period.crossDay" class="text-orange-500">(跨天)</span>
              </ElTag>
            </div>
            <div v-else>
              <ElTag size="small">
                {{ formatTime(row.workStartTime) }} - {{ formatTime(row.workEndTime) }}
                <span v-if="row.isNextDay" class="text-orange-500">(跨天)</span>
              </ElTag>
            </div>
          </template>
        </ElTableColumn>
        <ElTableColumn label="正班时间" width="100" align="center">
          <template #default="{ row }">{{ getRowWorkHours(row) }} 小时</template>
        </ElTableColumn>
        <ElTableColumn prop="status" label="状态" width="80" align="center">
          <template #default="{ row }">
            <ElTag :type="statusMap[row.status]?.type as any">{{ statusMap[row.status]?.label }}</ElTag>
          </template>
        </ElTableColumn>
        <ElTableColumn label="操作" width="150" align="center" fixed="right">
          <template #default="{ row }">
            <ElButton v-permission="'attendance:shift:edit'" type="primary" link size="small" @click="handleEdit(row)">编辑</ElButton>
            <ElPopconfirm title="确定删除该班次吗？" @confirm="handleDelete(row.id)">
              <template #reference>
                <ElButton v-permission="'attendance:shift:delete'" type="danger" link size="small">删除</ElButton>
              </template>
            </ElPopconfirm>
          </template>
        </ElTableColumn>
      </ElTable>
    </ElCard>

    <ElDialog v-model="dialogVisible" :title="operateType === 'add' ? '新增班次' : '编辑班次'" width="750px">
      <ElForm :model="formData" label-position="top">
        <ElRow :gutter="16">
          <ElCol :span="8">
            <ElFormItem label="班次编码" required>
              <ElInput v-model="formData.shiftCode" placeholder="如：DAY01" :disabled="operateType === 'edit'" />
            </ElFormItem>
          </ElCol>
          <ElCol :span="8">
            <ElFormItem label="班次名称" required>
              <ElInput v-model="formData.shiftName" placeholder="如：白班" />
            </ElFormItem>
          </ElCol>
          <ElCol :span="4">
            <ElFormItem label="正班时长">
              <div class="work-hours">{{ computedWorkHours }}h</div>
            </ElFormItem>
          </ElCol>
          <ElCol :span="4">
            <ElFormItem label="状态">
              <ElSwitch v-model="formData.status" :active-value="1" :inactive-value="0" />
            </ElFormItem>
          </ElCol>
        </ElRow>

        <div class="period-section">
          <div class="period-header">
            <span>工作时段</span>
            <ElButton type="primary" link size="small" @click="addPeriod"><icon-ep-plus /> 添加</ElButton>
          </div>
          
          <div class="period-list">
            <div v-for="(period, index) in formData.periods" :key="index" class="period-item">
              <div class="period-row">
                <span class="period-num">{{ index + 1 }}</span>
                <ElInput v-model="period.periodName" placeholder="名称" class="period-name" />
                <ElTimePicker v-model="period.startTime" format="HH:mm" value-format="HH:mm" placeholder="上班" class="period-time" />
                <span class="period-to">至</span>
                <ElTimePicker v-model="period.endTime" format="HH:mm" value-format="HH:mm" placeholder="下班" class="period-time" />
                <ElCheckbox v-model="period.crossDay" :true-value="1" :false-value="0" class="period-check">跨天</ElCheckbox>
                <ElCheckbox v-model="period.needClockIn" :true-value="1" :false-value="0" class="period-check">上班卡</ElCheckbox>
                <ElCheckbox v-model="period.needClockOut" :true-value="1" :false-value="0" class="period-check">下班卡</ElCheckbox>
                <ElButton v-if="formData.periods && formData.periods.length > 1" type="danger" link size="small" @click="removePeriod(index)" class="period-del">
                  <icon-ep-delete />
                </ElButton>
              </div>
            </div>
          </div>
        </div>
      </ElForm>
      <template #footer>
        <ElButton @click="dialogVisible = false">取消</ElButton>
        <ElButton type="primary" :loading="submitLoading" @click="handleSubmit">确定</ElButton>
      </template>
    </ElDialog>
  </div>
</template>


<style scoped>
.work-hours {
  font-size: 18px;
  font-weight: 600;
  color: #409eff;
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
  color: #606266;
}

.period-list {
  background: #fafafa;
  border-radius: 6px;
  padding: 12px;
}

.period-item {
  background: #fff;
  border-radius: 4px;
  padding: 10px 12px;
  margin-bottom: 8px;
  border: 1px solid #ebeef5;
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
  background: #409eff;
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
  color: #909399;
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
