<script setup lang="ts">
import { onMounted } from 'vue';
import { useEcharts } from '@/hooks/common/echarts';
import { fetchEmployeeList } from '@/service/api/hr';

defineOptions({ name: 'EducationChart' });

const pieColors = ['#f59e0b', '#6366f1', '#10b981', '#f43f5e', '#06b6d4', '#8b5cf6', '#64748b'];

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

/** 字典值 -> 中文标签 */
const dictValueMap: Record<string, string> = {
  doctor: '博士',
  master: '硕士',
  bachelor: '本科',
  college: '大专',
  high_school: '高中',
  secondary_vocational: '中专',
  junior_high: '初中及以下'
};

const educationOrder = ['博士', '硕士', '本科', '大专', '高中', '中专', '初中及以下'];

function normalizeEducation(val: string | undefined): string {
  if (!val) return '未填写';
  const v = val.trim();
  // 先按字典值匹配
  if (dictValueMap[v]) return dictValueMap[v];
  // 再按中文关键字匹配
  for (const label of educationOrder) {
    if (v.includes(label)) return label;
  }
  return v;
}

async function loadData() {
  try {
    const res = await fetchEmployeeList({ status: 1 });
    const employees = res?.data ?? [];
    const map = new Map<string, number>();

    employees.forEach(emp => {
      const edu = normalizeEducation(emp.highestEducation);
      map.set(edu, (map.get(edu) ?? 0) + 1);
    });

    // Sort by predefined order, unknowns at the end
    const data = Array.from(map.entries())
      .sort((a, b) => {
        const ia = educationOrder.indexOf(a[0]);
        const ib = educationOrder.indexOf(b[0]);
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

onMounted(() => {
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
