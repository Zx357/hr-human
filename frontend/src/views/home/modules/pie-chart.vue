<script setup lang="ts">
import { onMounted } from 'vue';
import dayjs from 'dayjs';
import { fetchEmployeeList } from '@/service/api/hr';
import { useEcharts } from '@/hooks/common/echarts';

defineOptions({ name: 'PieChart' });

const pieColors = ['#6366f1', '#10b981', '#f59e0b', '#f43f5e', '#8b5cf6', '#06b6d4'];

const { domRef, updateOptions } = useEcharts(() => ({
  tooltip: {
    trigger: 'item',
    backgroundColor: 'rgba(255,255,255,0.96)',
    borderColor: '#e5e7eb',
    borderWidth: 1,
    textStyle: { color: '#374151' },
    formatter: '{b}: {c} 人 ({d}%)'
  },
  legend: {
    bottom: '2%',
    left: 'center',
    itemWidth: 10,
    itemHeight: 10,
    icon: 'circle',
    textStyle: { color: '#6b7280', fontSize: 12 },
    itemStyle: { borderWidth: 0 }
  },
  series: [
    {
      color: pieColors,
      name: '年龄分布',
      type: 'pie',
      radius: ['48%', '72%'],
      center: ['50%', '45%'],
      avoidLabelOverlap: false,
      itemStyle: {
        borderRadius: 8,
        borderColor: '#fff',
        borderWidth: 2
      },
      label: { show: false, position: 'center' },
      emphasis: {
        label: { show: true, fontSize: 14, fontWeight: 'bold' },
        scaleSize: 6
      },
      labelLine: { show: false },
      data: [] as { name: string; value: number }[]
    }
  ]
}));

function getAgeGroup(birthDate: string): string {
  const age = dayjs().diff(dayjs(birthDate), 'year');
  if (age < 18) return '18岁以下';
  if (age < 30) return '18-29岁';
  if (age < 45) return '30-44岁';
  if (age <= 55) return '45-55岁';
  return '55岁以上';
}

const ageOrder = ['18岁以下', '18-29岁', '30-44岁', '45-55岁', '55岁以上'];

async function loadData() {
  try {
    const res = await fetchEmployeeList({ status: 1 });
    const employees = res?.data ?? [];
    const map = new Map<string, number>();

    employees.forEach(emp => {
      if (!emp.birthDate) return;
      const group = getAgeGroup(emp.birthDate);
      map.set(group, (map.get(group) ?? 0) + 1);
    });

    const data = ageOrder.filter(g => map.has(g)).map(g => ({ name: g, value: map.get(g)! }));

    updateOptions(opts => {
      opts.series[0].data = data;
      return opts;
    });
  } catch {
    updateOptions(opts => {
      opts.series[0].data = [];
      return opts;
    });
  }
}

onMounted(() => {
  loadData();
});
</script>

<template>
  <ElCard class="chart-card">
    <template #header>
      <div class="flex items-center justify-between">
        <div class="flex items-center gap-8px">
          <div class="header-dot" style="background: #10b981"></div>
          <span class="font-medium">员工年龄分布</span>
        </div>
        <span class="text-12px text-#9ca3af">在职员工</span>
      </div>
    </template>
    <div ref="domRef" class="h-360px overflow-hidden"></div>
  </ElCard>
</template>

<style scoped lang="scss">
.chart-card {
  border-radius: 12px;
  border: 1px solid var(--el-border-color-lighter);

  :deep(.el-card__header) {
    border-bottom: 1px solid var(--el-border-color-extra-light);
  }
}

.header-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
}
</style>
