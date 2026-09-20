<script setup lang="ts">
import { onMounted } from 'vue';
import { fetchEmployeeReportSummary } from '@/service/api/report';
import { useEcharts } from '@/hooks/common/echarts';
import { $t } from '@/locales';

defineOptions({ name: 'PieChart' });

const pieColors = ['#6366f1', '#10b981', '#f59e0b', '#f43f5e', '#8b5cf6', '#06b6d4'];

const { domRef, updateOptions } = useEcharts(() => ({
  tooltip: {
    trigger: 'item',
    formatter: $t('home.educationChart.people')
  },
  legend: {
    bottom: '2%',
    left: 'center',
    itemWidth: 10,
    itemHeight: 10,
    icon: 'circle',
    itemStyle: { borderWidth: 0 }
  },
  series: [
    {
      color: pieColors,
      name: $t('common.ageDistribution'),
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

/** 服务端聚合返回的年龄段顺序 */
const ageOrder = ['18-25', '26-35', '36-45', '46-55', '55以上'];

async function loadData() {
  try {
    const res = await fetchEmployeeReportSummary();
    const buckets = res.data?.ageBuckets ?? [];
    const data = buckets
      .slice()
      .sort((a, b) => ageOrder.indexOf(a.name) - ageOrder.indexOf(b.name))
      .map(item => ({ name: item.name, value: item.value }));

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
          <span class="font-medium">{{ $t('home.pieChart.employeeAgeDistribution') }}</span>
        </div>
        <span class="text-12px text-#9ca3af">{{ $t('home.common.activeEmployees') }}</span>
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
