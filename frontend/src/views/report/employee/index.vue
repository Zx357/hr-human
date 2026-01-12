<script setup lang="tsx">
import { ref, onMounted } from 'vue';

defineOptions({ name: 'EmployeeReport' });

const loading = ref(false);

// 统计数据
const statistics = ref({
  total: 156,
  onJob: 142,
  probation: 8,
  resigned: 6,
  newThisMonth: 5,
  resignedThisMonth: 2
});

// 部门人员分布
const deptDistribution = ref([
  { name: '技术部', value: 45 },
  { name: '产品部', value: 25 },
  { name: '人事部', value: 15 },
  { name: '财务部', value: 12 },
  { name: '市场部', value: 20 },
  { name: '运营部', value: 18 },
  { name: '其他', value: 21 }
]);

// 学历分布
const educationDistribution = ref([
  { name: '博士', value: 5 },
  { name: '硕士', value: 35 },
  { name: '本科', value: 85 },
  { name: '大专', value: 25 },
  { name: '其他', value: 6 }
]);

// 年龄分布
const ageDistribution = ref([
  { name: '25岁以下', value: 20 },
  { name: '25-30岁', value: 55 },
  { name: '30-35岁', value: 45 },
  { name: '35-40岁', value: 25 },
  { name: '40岁以上', value: 11 }
]);

// 入职趋势（近12个月）
const entryTrend = ref([
  { month: '2023-01', entry: 8, resign: 3 },
  { month: '2023-02', entry: 5, resign: 2 },
  { month: '2023-03', entry: 12, resign: 4 },
  { month: '2023-04', entry: 6, resign: 3 },
  { month: '2023-05', entry: 9, resign: 2 },
  { month: '2023-06', entry: 7, resign: 5 },
  { month: '2023-07', entry: 10, resign: 3 },
  { month: '2023-08', entry: 8, resign: 4 },
  { month: '2023-09', entry: 6, resign: 2 },
  { month: '2023-10', entry: 11, resign: 3 },
  { month: '2023-11', entry: 7, resign: 4 },
  { month: '2023-12', entry: 5, resign: 2 }
]);

function handleExport() {
  window.$message?.info('导出员工报表');
}

onMounted(() => {
  // 加载数据
});
</script>

<template>
  <div class="min-h-500px flex-col-stretch gap-16px overflow-hidden lt-sm:overflow-auto">
    <!-- 统计卡片 -->
    <div class="grid grid-cols-6 gap-16px lt-lg:grid-cols-3 lt-sm:grid-cols-2">
      <ElCard shadow="hover">
        <div class="text-center">
          <div class="text-3xl font-bold text-primary">{{ statistics.total }}</div>
          <div class="text-gray-500 mt-8px">员工总数</div>
        </div>
      </ElCard>
      <ElCard shadow="hover">
        <div class="text-center">
          <div class="text-3xl font-bold text-success">{{ statistics.onJob }}</div>
          <div class="text-gray-500 mt-8px">在职人数</div>
        </div>
      </ElCard>
      <ElCard shadow="hover">
        <div class="text-center">
          <div class="text-3xl font-bold text-warning">{{ statistics.probation }}</div>
          <div class="text-gray-500 mt-8px">试用期</div>
        </div>
      </ElCard>
      <ElCard shadow="hover">
        <div class="text-center">
          <div class="text-3xl font-bold text-info">{{ statistics.resigned }}</div>
          <div class="text-gray-500 mt-8px">已离职</div>
        </div>
      </ElCard>
      <ElCard shadow="hover">
        <div class="text-center">
          <div class="text-3xl font-bold text-success">+{{ statistics.newThisMonth }}</div>
          <div class="text-gray-500 mt-8px">本月入职</div>
        </div>
      </ElCard>
      <ElCard shadow="hover">
        <div class="text-center">
          <div class="text-3xl font-bold text-danger">-{{ statistics.resignedThisMonth }}</div>
          <div class="text-gray-500 mt-8px">本月离职</div>
        </div>
      </ElCard>
    </div>

    <!-- 图表区域 -->
    <div class="grid grid-cols-2 gap-16px lt-lg:grid-cols-1">
      <!-- 部门人员分布 -->
      <ElCard>
        <template #header>
          <div class="flex items-center justify-between">
            <span>部门人员分布</span>
            <ElButton type="primary" link size="small" @click="handleExport">导出</ElButton>
          </div>
        </template>
        <div class="h-300px">
          <ElTable :data="deptDistribution" border stripe height="280">
            <ElTableColumn type="index" label="序号" width="60" align="center" />
            <ElTableColumn prop="name" label="部门" />
            <ElTableColumn prop="value" label="人数" width="100" align="center" />
            <ElTableColumn label="占比" width="150">
              <template #default="{ row }">
                <ElProgress
                  :percentage="Math.round(row.value / statistics.total * 100)"
                  :stroke-width="10"
                />
              </template>
            </ElTableColumn>
          </ElTable>
        </div>
      </ElCard>

      <!-- 学历分布 -->
      <ElCard>
        <template #header>
          <span>学历分布</span>
        </template>
        <div class="h-300px">
          <ElTable :data="educationDistribution" border stripe height="280">
            <ElTableColumn type="index" label="序号" width="60" align="center" />
            <ElTableColumn prop="name" label="学历" />
            <ElTableColumn prop="value" label="人数" width="100" align="center" />
            <ElTableColumn label="占比" width="150">
              <template #default="{ row }">
                <ElProgress
                  :percentage="Math.round(row.value / statistics.total * 100)"
                  :stroke-width="10"
                  status="success"
                />
              </template>
            </ElTableColumn>
          </ElTable>
        </div>
      </ElCard>

      <!-- 年龄分布 -->
      <ElCard>
        <template #header>
          <span>年龄分布</span>
        </template>
        <div class="h-300px">
          <ElTable :data="ageDistribution" border stripe height="280">
            <ElTableColumn type="index" label="序号" width="60" align="center" />
            <ElTableColumn prop="name" label="年龄段" />
            <ElTableColumn prop="value" label="人数" width="100" align="center" />
            <ElTableColumn label="占比" width="150">
              <template #default="{ row }">
                <ElProgress
                  :percentage="Math.round(row.value / statistics.total * 100)"
                  :stroke-width="10"
                  status="warning"
                />
              </template>
            </ElTableColumn>
          </ElTable>
        </div>
      </ElCard>

      <!-- 入离职趋势 -->
      <ElCard>
        <template #header>
          <span>入离职趋势（近12个月）</span>
        </template>
        <div class="h-300px">
          <ElTable :data="entryTrend" border stripe height="280">
            <ElTableColumn prop="month" label="月份" width="100" />
            <ElTableColumn prop="entry" label="入职人数" align="center">
              <template #default="{ row }">
                <span class="text-success">+{{ row.entry }}</span>
              </template>
            </ElTableColumn>
            <ElTableColumn prop="resign" label="离职人数" align="center">
              <template #default="{ row }">
                <span class="text-danger">-{{ row.resign }}</span>
              </template>
            </ElTableColumn>
            <ElTableColumn label="净增长" align="center">
              <template #default="{ row }">
                <span :class="row.entry - row.resign >= 0 ? 'text-success' : 'text-danger'">
                  {{ row.entry - row.resign >= 0 ? '+' : '' }}{{ row.entry - row.resign }}
                </span>
              </template>
            </ElTableColumn>
          </ElTable>
        </div>
      </ElCard>
    </div>
  </div>
</template>
