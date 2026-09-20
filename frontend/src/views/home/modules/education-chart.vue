<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { fetchEmployeeReportSummary } from '@/service/api/report';
import { fetchDictDataByCode } from '@/service/api/system';
import { useEcharts } from '@/hooks/common/echarts';

defineOptions({ name: 'EducationChart' });

const pieColors = ['#f59e0b', '#6366f1', '#10b981', '#f43f5e', '#06b6d4', '#8b5cf6', '#64748b'];

// 学历字典（dictValue -> dictLabel），由接口获取
const dictValueMap = ref<Record<string, string>>({});
/** 图例排序：按字典 sortOrder，未匹配的排最后 */
const educationOrder = ref<string[]>([]);

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
      name: '学历分布',
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

/** 加载学历字典，构建 值->标签 映射与展示顺序 */
async function loadEducationDict() {
  try {
    const res = await fetchDictDataByCode('education');
    const list = (res?.data ?? [])
      .filter(item => item.status === 1)
      .slice()
      .sort((a, b) => a.sortOrder - b.sortOrder);
    const map: Record<string, string> = {};
    list.forEach(item => {
      map[item.dictValue] = item.dictLabel;
    });
    dictValueMap.value = map;
    educationOrder.value = list.map(item => item.dictLabel);
  } catch {
    dictValueMap.value = {};
    educationOrder.value = [];
  }
}

function normalizeEducation(val: string | undefined): string {
  if (!val) return '未填写';
  const v = val.trim();
  // 先按字典值匹配
  if (dictValueMap.value[v]) return dictValueMap.value[v];
  // 再按标签关键字匹配（兼容历史数据直接存中文的情况）
  for (const label of educationOrder.value) {
    if (v.includes(label)) return label;
  }
  return v;
}

async function loadData() {
  try {
    const res = await fetchEmployeeReportSummary();
    const items = res.data?.education ?? [];
    const map = new Map<string, number>();

    items.forEach(item => {
      const edu = normalizeEducation(item.name);
      map.set(edu, (map.get(edu) ?? 0) + (item.value ?? 0));
    });

    // Sort by dict order, unknowns at the end
    const order = educationOrder.value;
    const data = Array.from(map.entries())
      .sort((a, b) => {
        const ia = order.indexOf(a[0]);
        const ib = order.indexOf(b[0]);
        return (ia === -1 ? 999 : ia) - (ib === -1 ? 999 : ib);
      })
      .map(([name, value]) => ({ name, value }));

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

onMounted(async () => {
  await loadEducationDict();
  loadData();
});
</script>

<template>
  <ElCard class="chart-card">
    <template #header>
      <div class="flex items-center justify-between">
        <div class="flex items-center gap-8px">
          <div class="header-dot" style="background: #f59e0b"></div>
          <span class="font-medium">员工学历分布</span>
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
