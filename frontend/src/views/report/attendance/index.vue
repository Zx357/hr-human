<script setup lang="tsx">
import { ref } from 'vue';

defineOptions({ name: 'AttendanceReport' });

// 当前月份
const currentMonth = ref(new Date().toISOString().slice(0, 7));

// 统计数据
const statistics = ref({
  totalWorkDays: 22,
  avgAttendanceRate: 96.5,
  totalLate: 15,
  totalEarly: 8,
  totalAbsent: 3,
  totalOvertime: 256
});

// 部门考勤统计
const deptAttendance = ref([
  { deptName: '技术部', employees: 45, attendanceRate: 97.2, lateTimes: 5, earlyTimes: 2, absentDays: 1 },
  { deptName: '产品部', employees: 25, attendanceRate: 96.8, lateTimes: 3, earlyTimes: 1, absentDays: 0 },
  { deptName: '人事部', employees: 15, attendanceRate: 98.5, lateTimes: 1, earlyTimes: 0, absentDays: 0 },
  { deptName: '财务部', employees: 12, attendanceRate: 99.0, lateTimes: 0, earlyTimes: 1, absentDays: 0 },
  { deptName: '市场部', employees: 20, attendanceRate: 94.5, lateTimes: 4, earlyTimes: 2, absentDays: 1 },
  { deptName: '运营部', employees: 18, attendanceRate: 95.0, lateTimes: 2, earlyTimes: 2, absentDays: 1 }
]);

// 异常考勤明细
const abnormalRecords = ref([
  { employeeName: '张三', employeeNo: 'EMP001', deptName: '技术部', date: '2023-12-15', type: '迟到', time: '09:15', remark: '地铁故障' },
  { employeeName: '李四', employeeNo: 'EMP002', deptName: '产品部', date: '2023-12-14', type: '早退', time: '17:30', remark: '身体不适' },
  { employeeName: '王五', employeeNo: 'EMP003', deptName: '市场部', date: '2023-12-13', type: '旷工', time: '-', remark: '未请假' }
]);

const searchParams = ref({
  deptId: undefined as number | undefined
});

function handleMonthChange(month: string) {
  currentMonth.value = month;
  console.log('Load attendance for month:', month);
}

function handleExport() {
  window.$message?.info('导出考勤报表');
}

function handleSearch() {
  console.log('Search:', searchParams.value);
}

function handleReset() {
  searchParams.value = {
    deptId: undefined
  };
}
</script>

<template>
  <div class="min-h-500px flex-col-stretch gap-16px overflow-hidden lt-sm:overflow-auto">
    <!-- 筛选区域 -->
    <ElCard>
      <ElForm inline :model="searchParams">
        <ElFormItem label="月份">
          <ElDatePicker
            v-model="currentMonth"
            type="month"
            placeholder="选择月份"
            value-format="YYYY-MM"
            @change="handleMonthChange"
          />
        </ElFormItem>
        <ElFormItem label="部门">
          <ElSelect v-model="searchParams.deptId" placeholder="请选择部门" clearable>
            <ElOption label="技术部" :value="1" />
            <ElOption label="产品部" :value="2" />
            <ElOption label="人事部" :value="3" />
            <ElOption label="财务部" :value="4" />
            <ElOption label="市场部" :value="5" />
            <ElOption label="运营部" :value="6" />
          </ElSelect>
        </ElFormItem>
        <ElFormItem>
          <ElButton type="primary" @click="handleSearch">
            <template #icon><icon-ep-search /></template>
            搜索
          </ElButton>
          <ElButton @click="handleReset">
            <template #icon><icon-ep-refresh /></template>
            重置
          </ElButton>
          <ElButton type="success" @click="handleExport">
            <template #icon><icon-ep-download /></template>
            导出报表
          </ElButton>
        </ElFormItem>
      </ElForm>
    </ElCard>

    <!-- 统计卡片 -->
    <div class="grid grid-cols-6 gap-16px lt-lg:grid-cols-3 lt-sm:grid-cols-2">
      <ElCard shadow="hover">
        <div class="text-center">
          <div class="text-3xl font-bold text-primary">{{ statistics.totalWorkDays }}</div>
          <div class="text-gray-500 mt-8px">应出勤天数</div>
        </div>
      </ElCard>
      <ElCard shadow="hover">
        <div class="text-center">
          <div class="text-3xl font-bold text-success">{{ statistics.avgAttendanceRate }}%</div>
          <div class="text-gray-500 mt-8px">平均出勤率</div>
        </div>
      </ElCard>
      <ElCard shadow="hover">
        <div class="text-center">
          <div class="text-3xl font-bold text-warning">{{ statistics.totalLate }}</div>
          <div class="text-gray-500 mt-8px">迟到次数</div>
        </div>
      </ElCard>
      <ElCard shadow="hover">
        <div class="text-center">
          <div class="text-3xl font-bold text-warning">{{ statistics.totalEarly }}</div>
          <div class="text-gray-500 mt-8px">早退次数</div>
        </div>
      </ElCard>
      <ElCard shadow="hover">
        <div class="text-center">
          <div class="text-3xl font-bold text-danger">{{ statistics.totalAbsent }}</div>
          <div class="text-gray-500 mt-8px">旷工天数</div>
        </div>
      </ElCard>
      <ElCard shadow="hover">
        <div class="text-center">
          <div class="text-3xl font-bold text-info">{{ statistics.totalOvertime }}h</div>
          <div class="text-gray-500 mt-8px">加班时长</div>
        </div>
      </ElCard>
    </div>

    <!-- 部门考勤统计 -->
    <ElCard>
      <template #header>
        <span>部门考勤统计 - {{ currentMonth }}</span>
      </template>
      <ElTable :data="deptAttendance" border stripe>
        <ElTableColumn prop="deptName" label="部门" width="120" />
        <ElTableColumn prop="employees" label="员工数" width="100" align="center" />
        <ElTableColumn prop="attendanceRate" label="出勤率" width="120" align="center">
          <template #default="{ row }">
            <span :class="row.attendanceRate >= 95 ? 'text-success' : row.attendanceRate >= 90 ? 'text-warning' : 'text-danger'">
              {{ row.attendanceRate }}%
            </span>
          </template>
        </ElTableColumn>
        <ElTableColumn prop="lateTimes" label="迟到次数" width="100" align="center">
          <template #default="{ row }">
            <span :class="row.lateTimes > 0 ? 'text-warning' : ''">{{ row.lateTimes }}</span>
          </template>
        </ElTableColumn>
        <ElTableColumn prop="earlyTimes" label="早退次数" width="100" align="center">
          <template #default="{ row }">
            <span :class="row.earlyTimes > 0 ? 'text-warning' : ''">{{ row.earlyTimes }}</span>
          </template>
        </ElTableColumn>
        <ElTableColumn prop="absentDays" label="旷工天数" width="100" align="center">
          <template #default="{ row }">
            <span :class="row.absentDays > 0 ? 'text-danger font-bold' : ''">{{ row.absentDays }}</span>
          </template>
        </ElTableColumn>
        <ElTableColumn label="出勤率图示" min-width="200">
          <template #default="{ row }">
            <ElProgress
              :percentage="row.attendanceRate"
              :stroke-width="15"
              :status="row.attendanceRate >= 95 ? 'success' : row.attendanceRate >= 90 ? '' : 'exception'"
            />
          </template>
        </ElTableColumn>
      </ElTable>
    </ElCard>

    <!-- 异常考勤明细 -->
    <ElCard>
      <template #header>
        <span>异常考勤明细</span>
      </template>
      <ElTable :data="abnormalRecords" border stripe>
        <ElTableColumn type="index" label="序号" width="60" align="center" />
        <ElTableColumn prop="employeeNo" label="工号" width="100" />
        <ElTableColumn prop="employeeName" label="姓名" width="100" />
        <ElTableColumn prop="deptName" label="部门" width="100" />
        <ElTableColumn prop="date" label="日期" width="120" />
        <ElTableColumn prop="type" label="异常类型" width="100" align="center">
          <template #default="{ row }">
            <ElTag :type="row.type === '旷工' ? 'danger' : 'warning'">{{ row.type }}</ElTag>
          </template>
        </ElTableColumn>
        <ElTableColumn prop="time" label="打卡时间" width="100" />
        <ElTableColumn prop="remark" label="备注" min-width="150" />
      </ElTable>
    </ElCard>
  </div>
</template>
