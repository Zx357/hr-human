<script setup lang="ts">
import { computed, onMounted, watch } from 'vue';
import dayjs from 'dayjs';
import { fetchAttendanceDailyTrend } from '@/service/api/report';
import { useAppStore } from '@/store/modules/app';
import { useEcharts } from '@/hooks/common/echarts';

defineOptions({ name: 'LineChart' });

const appStore = useAppStore();

const rangeDays = 7;
const range = computed(() => {
  const end = dayjs().startOf('day');
  const start = end.subtract(rangeDays - 1, 'day');
  return { start: start.format('YYYY-MM-DD'), end: end.format('YYYY-MM-DD') };
});

const { domRef, updateOptions } = useEcharts(() => ({
  tooltip: {
    trigger: 'axis',
    backgroundColor: 'rgba(255,255,255,0.96)',
    borderColor: '#e5e7eb',
    borderWidth: 1,
    textStyle: { color: '#374151' },
    axisPointer: { type: 'shadow' }
  },
  legend: {
    data: ['正常出勤', '考勤异常'],
    right: 0,
    top: 0,
    itemWidth: 12,
    itemHeight: 12,
    textStyle: { color: '#6b7280' }
  },
  grid: {
    left: '2%',
    right: '2%',
    bottom: '3%',
    top: '40px',
    containLabel: true
  },
  xAxis: {
    type: 'category',
    data: [] as string[],
    axisLine: { show: false },
    axisTick: { show: false },
    axisLabel: { color: '#9ca3af', fontSize: 12 }
  },
  yAxis: {
    type: 'value',
    minInterval: 1,
    splitLine: { lineStyle: { color: '#f3f4f6', type: 'dashed' } },
    axisLabel: { color: '#9ca3af', fontSize: 12 }
  },
  series: [
    {
      name: '正常出勤',
      type: 'bar',
      barWidth: '36%',
      itemStyle: {
        color: {
          type: 'linear',
          x: 0,
          y: 0,
          x2: 0,
          y2: 1,
          colorStops: [
            { offset: 0, color: '#818cf8' },
            { offset: 1, color: '#6366f1' }
          ]
        },
        borderRadius: [4, 4, 0, 0]
      },
      emphasis: { focus: 'series' },
      data: [] as number[]
    },
    {
      name: '考勤异常',
      type: 'line',
      smooth: true,
      symbol: 'circle',
      symbolSize: 7,
      showSymbol: true,
      itemStyle: { color: '#f43f5e', borderColor: '#fff', borderWidth: 2 },
      lineStyle: { width: 2.5 },
      emphasis: { focus: 'series' },
      data: [] as number[]
    }
  ]
}));

function buildXAxis() {
  const labels: string[] = [];
  for (let i = rangeDays - 1; i >= 0; i -= 1) {
    labels.push(dayjs().subtract(i, 'day').format('MM-DD'));
  }
  return labels;
}

function updateLocale() {
  updateOptions((opts, factory) => {
    const originOpts = factory();
    opts.legend.data = originOpts.legend.data;
    opts.series[0].name = originOpts.series[0].name;
    opts.series[1].name = originOpts.series[1].name;
    return opts;
  });
}

async function loadData() {
  try {
    const { start, end } = range.value;
    const res = await fetchAttendanceDailyTrend({ startDate: start, endDate: end });
    const items = res.data ?? [];

    const dayMap = new Map<string, { normal: number; abnormal: number }>();
    for (let i = 0; i < rangeDays; i += 1) {
      const k = dayjs(range.value.start).add(i, 'day').format('YYYY-MM-DD');
      dayMap.set(k, { normal: 0, abnormal: 0 });
    }

    items.forEach(r => {
      if (!r.date) return;
      const k = dayjs(r.date).format('YYYY-MM-DD');
      const bucket = dayMap.get(k);
      if (!bucket) return;
      bucket.normal = Number(r.normal) || 0;
      bucket.abnormal = Number(r.abnormal) || 0;
    });

    const labels = buildXAxis();
    const normal: number[] = [];
    const abnormal: number[] = [];

    Array.from(dayMap.entries())
      .sort((a, b) => (a[0] > b[0] ? 1 : -1))
      .forEach(([, v]) => {
        normal.push(v.normal);
        abnormal.push(v.abnormal);
      });

    updateOptions(opts => {
      opts.xAxis.data = labels;
      opts.series[0].data = normal;
      opts.series[1].data = abnormal;
      return opts;
    });
  } catch {
    updateOptions(opts => {
      opts.xAxis.data = buildXAxis();
      opts.series[0].data = Array.from({ length: rangeDays }, () => 0);
      opts.series[1].data = Array.from({ length: rangeDays }, () => 0);
      return opts;
    });
  }
}

watch(
  () => appStore.locale,
  () => {
    updateLocale();
  }
);

onMounted(() => {
  loadData();
});
</script>

<template>
  <ElCard class="chart-card">
    <template #header>
      <div class="flex items-center justify-between">
        <div class="flex items-center gap-8px">
          <div class="header-dot" style="background: #6366f1"></div>
          <span class="font-medium">近 {{ rangeDays }} 天考勤概览</span>
        </div>
        <span class="text-12px text-#9ca3af">柱状：正常出勤 · 折线：考勤异常</span>
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
