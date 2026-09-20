<script setup lang="ts">
import { computed, nextTick, onMounted, ref, watch } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import {
  OrgUnitType,
  createOrgUnit,
  deleteOrgUnit,
  fetchAllOrgStatistics,
  fetchOrgStatistics,
  fetchOrgTreeWithCount,
  updateOrgUnit
} from '@/service/api/organization';
import { fetchEmployeePage } from '@/service/api/hr';
import { useEcharts } from '@/hooks/common/echarts';
import { $t } from '@/locales';

defineOptions({ name: 'OrgStructure' });

type StatsDimension = 'age' | 'gender' | 'education' | 'status' | 'employeeType';
type ChartDatum = { name: string; value: number };
type TableRow = { index: number; label: string; count: number; percent: string };
type ContextMenuCommand = { action: 'add' | 'edit' | 'delete'; node: Api.Organization.OrgUnit };
type TypeOption = { label: string; value: number };

const loading = ref(false);
const statsLoading = ref(false);
const submitLoading = ref(false);
const dialogVisible = ref(false);
const operateType = ref<'add' | 'edit'>('add');
const editingId = ref<number | null>(null);

const treeData = ref<Api.Organization.OrgUnit[]>([]);
const selectedNode = ref<Api.Organization.OrgUnit | null>(null);
const employeeOptions = ref<Api.Hr.Employee[]>([]);
const searchKeyword = ref('');

const treeRef = ref<any>(null);
const contextMenuRef = ref<any>(null);
const contextMenuNode = ref<Api.Organization.OrgUnit | null>(null);
const contextMenuVirtualRef = ref<any>(null);

const typeOptions: TypeOption[] = [
  { label: $t('org.structure.group'), value: OrgUnitType.GROUP },
  { label: $t('common.company'), value: OrgUnitType.COMPANY },
  { label: $t('common.department'), value: OrgUnitType.DEPT }
];

const dimensionOptions: Array<{ label: string; value: StatsDimension }> = [
  { label: $t('org.structure.ageAnalysis'), value: 'age' },
  { label: $t('org.structure.genderAnalysis'), value: 'gender' },
  { label: $t('org.structure.educationAnalysis'), value: 'education' },
  { label: $t('org.structure.employmentStatus'), value: 'status' },
  { label: $t('org.structure.employeeType'), value: 'employeeType' }
];

const chartColors = ['#2563eb', '#10b981', '#f59e0b', '#ef4444', '#8b5cf6', '#06b6d4', '#f97316', '#64748b'];
const statsDimension = ref<StatsDimension>('age');
const parentNode = ref<Api.Organization.OrgUnit | null>(null);

const editingData = ref<Api.Organization.OrgUnitForm>(createEmptyForm(OrgUnitType.GROUP, 0));
const isCompanyUnit = computed(() => editingData.value.unitType === OrgUnitType.COMPANY);

const statistics = ref<Api.Organization.OrgStatistics>(createEmptyStatistics());

// 图表改用 useEcharts hook：自动跟随暗色主题、容器尺寸与生命周期
const { domRef: chartRef, updateOptions: updateChartOptions } = useEcharts(() => ({
  color: chartColors,
  tooltip: {
    trigger: 'item',
    formatter: $t('home.educationChart.people')
  },
  legend: {
    orient: 'vertical',
    right: 16,
    top: 'center',
    itemGap: 12
  },
  series: [
    {
      type: 'pie',
      radius: ['42%', '72%'],
      center: ['34%', '50%'],
      avoidLabelOverlap: false,
      itemStyle: {
        borderRadius: 8,
        borderColor: '#fff',
        borderWidth: 2
      },
      label: { show: false },
      emphasis: {
        label: {
          show: true,
          fontSize: 14,
          fontWeight: 'bold'
        }
      },
      labelLine: { show: false },
      data: [] as ChartDatum[]
    }
  ]
}));

function createEmptyStatistics(): Api.Organization.OrgStatistics {
  return {
    totalCount: 0,
    educationDistribution: [],
    genderDistribution: [],
    ageDistribution: [],
    statusDistribution: [],
    employeeTypeDistribution: []
  };
}

function createEmptyForm(unitType: number, parentId = 0): Api.Organization.OrgUnitForm {
  return {
    parentId,
    unitType,
    unitCode: '',
    unitName: '',
    shortName: '',
    leaderId: undefined,
    phone: '',
    email: '',
    address: '',
    attendanceAddress: '',
    attendanceLatitude: undefined,
    attendanceLongitude: undefined,
    attendanceRange: undefined,
    description: '',
    sortOrder: 0
  };
}

function buildOrgUnitForm(unit: Api.Organization.OrgUnit): Api.Organization.OrgUnitForm {
  return {
    parentId: unit.parentId ?? 0,
    unitType: unit.unitType,
    unitCode: unit.unitCode,
    unitName: unit.unitName,
    shortName: unit.shortName ?? '',
    leaderId: unit.leaderId,
    phone: unit.phone ?? '',
    email: unit.email ?? '',
    address: unit.address ?? '',
    attendanceAddress: unit.attendanceAddress ?? '',
    attendanceLatitude: unit.attendanceLatitude,
    attendanceLongitude: unit.attendanceLongitude,
    attendanceRange: unit.attendanceRange,
    description: unit.description ?? '',
    sortOrder: unit.sortOrder ?? 0
  };
}

function getAvailableTypes(parentType?: number): TypeOption[] {
  if (!parentType) {
    return [
      { label: $t('org.structure.group'), value: OrgUnitType.GROUP },
      { label: $t('common.company'), value: OrgUnitType.COMPANY }
    ];
  }
  if (parentType === OrgUnitType.GROUP) {
    return [{ label: $t('common.company'), value: OrgUnitType.COMPANY }];
  }
  return [{ label: $t('common.department'), value: OrgUnitType.DEPT }];
}

function filterNode(keyword: string, data: any) {
  if (!keyword) {
    return true;
  }
  return data.unitName?.includes(keyword) || data.unitCode?.includes(keyword);
}

watch(searchKeyword, value => {
  treeRef.value?.filter(value);
});

function getAllIds(nodes: Api.Organization.OrgUnit[]): number[] {
  return nodes.flatMap(node => [node.id, ...(node.children?.length ? getAllIds(node.children) : [])]);
}

function findDistributionValue(items: Array<{ name: string; value: number }>, names: string[]) {
  return items.find(item => names.includes(item.name))?.value ?? 0;
}

const maleCount = computed(() => findDistributionValue(statistics.value.genderDistribution, [$t('common.male'), 'Male']));
const femaleCount = computed(() => findDistributionValue(statistics.value.genderDistribution, [$t('common.female'), 'Female']));
const activeCount = computed(() => findDistributionValue(statistics.value.statusDistribution, [$t('common.active'), 'Active']));

const dialogTitle = computed(() => (operateType.value === 'add' ? $t('org.structure.addOrganization') : $t('org.structure.editOrganization')));
const pageTitle = computed(() =>
  selectedNode.value ? $t('org.structure.workforceAnalysis', { name: selectedNode.value.unitName }) : $t('org.structure.wholeCompanyWorkforceAnalysis')
);
const chartTitle = computed(() => {
  const dimensionLabel = dimensionOptions.find(item => item.value === statsDimension.value)?.label ?? '';
  const prefix = selectedNode.value?.unitName ?? $t('org.structure.wholeCompany');
  return `${prefix} - ${dimensionLabel}`;
});

function runAsyncTask(task: Promise<unknown>) {
  task.catch(() => undefined);
}

const tableData = computed<TableRow[]>(() => {
  const total = statistics.value.totalCount || 1;
  const mapDistribution = (item: { name: string; value: number }, index: number) => ({
    index: index + 1,
    label: item.name,
    count: item.value,
    percent: ((item.value / total) * 100).toFixed(2)
  });

  switch (statsDimension.value) {
    case 'age':
      return statistics.value.ageDistribution.map((item, index) => ({
        index: index + 1,
        label: item.range,
        count: item.count,
        percent: ((item.count / total) * 100).toFixed(2)
      }));
    case 'gender':
      return statistics.value.genderDistribution.map(mapDistribution);
    case 'education':
      return statistics.value.educationDistribution.map(mapDistribution);
    case 'status':
      return statistics.value.statusDistribution.map(mapDistribution);
    case 'employeeType':
      return statistics.value.employeeTypeDistribution.map(mapDistribution);
    default:
      return [];
  }
});

function getChartData(): ChartDatum[] {
  switch (statsDimension.value) {
    case 'age':
      return statistics.value.ageDistribution.map(item => ({ name: item.range, value: item.count }));
    case 'gender':
      return statistics.value.genderDistribution;
    case 'education':
      return statistics.value.educationDistribution;
    case 'status':
      return statistics.value.statusDistribution;
    case 'employeeType':
      return statistics.value.employeeTypeDistribution;
    default:
      return [];
  }
}

function renderChart() {
  if (!chartRef.value) {
    return;
  }

  updateChartOptions(opts => {
    const series = opts.series as { data: ChartDatum[] }[];
    series[0].data = getChartData();
    return opts;
  });
}

watch(statsDimension, () => {
  runAsyncTask(nextTick(renderChart));
});

watch(
  () => statistics.value,
  () => {
    runAsyncTask(nextTick(renderChart));
  },
  { deep: true }
);

async function loadTreeData() {
  loading.value = true;
  try {
    const response = await fetchOrgTreeWithCount();
    treeData.value = response.data ?? [];
  } finally {
    loading.value = false;
  }
}

async function loadStatistics(orgId?: number) {
  statsLoading.value = true;
  try {
    const response = orgId ? await fetchOrgStatistics(orgId) : await fetchAllOrgStatistics();
    statistics.value = response.data ?? createEmptyStatistics();
  } finally {
    statsLoading.value = false;
  }
}

async function loadEmployees() {
  try {
    // 负责人下拉需要全量在职员工（仅使用 id/name/employeeNo 三个字段）；
    // 员工规模扩大后应由后端提供精简的负责人候选列表接口替代全量分页拉取
    const response = await fetchEmployeePage({ pageNum: 1, pageSize: 1000, status: 1 });
    employeeOptions.value = response.data?.records ?? [];
  } catch {
    employeeOptions.value = [];
  }
}

function handleNodeClick(node: Api.Organization.OrgUnit) {
  selectedNode.value = node;
  runAsyncTask(loadStatistics(node.id));
}

function handleViewAll() {
  selectedNode.value = null;
  runAsyncTask(loadStatistics());
}

function handleNodeContextMenu(event: MouseEvent, node: Api.Organization.OrgUnit) {
  event.preventDefault();
  contextMenuNode.value = node;
  contextMenuVirtualRef.value = {
    getBoundingClientRect: () =>
      ({
        width: 0,
        height: 0,
        top: event.clientY,
        right: event.clientX,
        bottom: event.clientY,
        left: event.clientX,
        x: event.clientX,
        y: event.clientY,
        toJSON: () => undefined
      }) as DOMRect
  };
  runAsyncTask(nextTick(() => contextMenuRef.value?.handleOpen()));
}

function handleContextMenuCommand(command: ContextMenuCommand) {
  if (command.action === 'add') {
    handleAdd(command.node);
    return;
  }
  if (command.action === 'edit') {
    handleEdit(command.node);
    return;
  }
  runAsyncTask(handleDelete(command.node));
}

function handleExpandAll() {
  getAllIds(treeData.value).forEach(id => {
    treeRef.value?.getNode(id)?.expand();
  });
}

function handleCollapseAll() {
  getAllIds(treeData.value).forEach(id => {
    treeRef.value?.getNode(id)?.collapse();
  });
}

function handleRefresh() {
  runAsyncTask(loadTreeData());
  runAsyncTask(loadStatistics(selectedNode.value?.id));
}

function handleAdd(parent?: Api.Organization.OrgUnit) {
  operateType.value = 'add';
  editingId.value = null;
  parentNode.value = parent ?? null;
  const availableTypes = getAvailableTypes(parent?.unitType);
  editingData.value = createEmptyForm(availableTypes[0]?.value ?? OrgUnitType.GROUP, parent?.id ?? 0);
  dialogVisible.value = true;
}

function handleEdit(row: Api.Organization.OrgUnit) {
  operateType.value = 'edit';
  editingId.value = row.id;
  parentNode.value = null;
  editingData.value = buildOrgUnitForm(row);
  dialogVisible.value = true;
}

async function handleDelete(row: Api.Organization.OrgUnit) {
  if (row.children?.length) {
    ElMessage.warning($t('org.structure.thisNodeHasChildNodesDeleteTheChildNodesFirst'));
    return;
  }

  try {
    await ElMessageBox.confirm($t('org.structure.areYouSureYouWantToDelete', { name: row.unitName }), $t('common.deleteConfirmTitle'), { type: 'warning' });
    await deleteOrgUnit(row.id);
    ElMessage.success($t('common.deleteSuccess'));
    await loadTreeData();
    if (selectedNode.value?.id === row.id) {
      selectedNode.value = null;
      await loadStatistics();
    }
  } catch {
    // 用户取消删除；接口失败由请求层统一弹错
  }
}

async function handleSubmit() {
  if (!editingData.value.unitCode?.trim()) {
    ElMessage.warning($t('org.structure.pleaseEnterOrganizationCode'));
    return;
  }

  if (!editingData.value.unitName?.trim()) {
    ElMessage.warning($t('org.structure.pleaseEnterOrganizationName'));
    return;
  }

  submitLoading.value = true;
  try {
    if (operateType.value === 'add') {
      await createOrgUnit(editingData.value);
      ElMessage.success($t('common.addSuccess'));
    } else if (editingId.value !== null) {
      await updateOrgUnit(editingId.value, editingData.value);
      ElMessage.success($t('common.saveSuccess'));
    }

    dialogVisible.value = false;
    await loadTreeData();
    await loadStatistics(selectedNode.value?.id);
  } catch {
    // 请求层已统一弹错
  } finally {
    submitLoading.value = false;
  }
}

function getTypeTag(type: number) {
  const typeMap: Record<number, { label: string; type: 'danger' | 'warning' | 'primary' | 'info' }> = {
    [OrgUnitType.GROUP]: { label: $t('org.structure.group'), type: 'danger' },
    [OrgUnitType.COMPANY]: { label: $t('common.company'), type: 'warning' },
    [OrgUnitType.DEPT]: { label: $t('common.department'), type: 'primary' }
  };
  return typeMap[type] ?? { label: $t('common.unknown'), type: 'info' };
}

function getAddButtonText(type?: number) {
  if (!type) {
    return $t('common.add');
  }
  if (type === OrgUnitType.GROUP) {
    return $t('org.structure.addCompany');
  }
  if (type === OrgUnitType.COMPANY) {
    return $t('org.structure.addDepartment');
  }
  return $t('org.structure.addChildNode');
}

function getParentName() {
  return parentNode.value?.unitName ?? $t('org.structure.topLevelNode');
}

onMounted(async () => {
  await Promise.all([loadTreeData(), loadEmployees(), loadStatistics()]);
  await nextTick();
  renderChart();
});
</script>

<template>
  <div class="org-page">
    <div class="org-left">
      <ElCard shadow="hover" class="full-height">
        <template #header>
          <div class="tree-header">
            <span class="tree-title">{{ $t('common.organizationStructure') }}</span>
            <ElButtonGroup size="small">
              <ElButton @click="handleExpandAll">
                <icon-ep-arrow-down />
              </ElButton>
              <ElButton @click="handleCollapseAll">
                <icon-ep-arrow-up />
              </ElButton>
              <ElButton :loading="loading" @click="handleRefresh">
                <icon-ep-refresh />
              </ElButton>
            </ElButtonGroup>
          </div>
        </template>

        <ElInput v-model="searchKeyword" :placeholder="$t('org.structure.searchOrganizationNameOrCode')" clearable class="mb-12px">
          <template #prefix>
            <icon-ep-search />
          </template>
        </ElInput>

        <ElButton v-permission="'org:unit:add'" type="primary" size="small" class="mb-12px" @click="handleAdd()">
          <icon-ep-plus class="mr-4px" />
          {{ $t('org.structure.addTopOrganization') }}
        </ElButton>

        <ElScrollbar height="calc(100vh - 320px)">
          <ElTree
            ref="treeRef"
            v-loading="loading"
            :data="treeData"
            :props="{ children: 'children', label: 'unitName' }"
            :expand-on-click-node="false"
            :default-expand-all="true"
            :filter-node-method="filterNode"
            node-key="id"
            highlight-current
            @node-click="handleNodeClick"
            @node-contextmenu="handleNodeContextMenu"
          >
            <template #default="{ data }">
              <div class="tree-node">
                <div class="tree-node-label">
                  <ElTag :type="getTypeTag(data.unitType).type" size="small">
                    {{ getTypeTag(data.unitType).label }}
                  </ElTag>
                  <span class="truncate">{{ data.unitName }}</span>
                  <ElBadge
                    v-if="data.employeeCount > 0"
                    :value="data.employeeCount"
                    :max="999"
                    type="info"
                    class="tree-node-badge"
                  />
                </div>
                <div
                  v-permission="['org:unit:add', 'org:unit:edit', 'org:unit:delete']"
                  class="node-actions"
                >
                  <ElButton v-permission="'org:unit:add'" type="success" link size="small" @click.stop="handleAdd(data)">
                    <icon-ep-plus />
                  </ElButton>
                  <ElButton v-permission="'org:unit:edit'" type="primary" link size="small" @click.stop="handleEdit(data)">
                    <icon-ep-edit />
                  </ElButton>
                  <ElButton v-permission="'org:unit:delete'" type="danger" link size="small" @click.stop="handleDelete(data)">
                    <icon-ep-delete />
                  </ElButton>
                </div>
              </div>
            </template>
          </ElTree>

          <ElDropdown
            ref="contextMenuRef"
            :virtual-ref="contextMenuVirtualRef"
            virtual-triggering
            @command="handleContextMenuCommand"
          >
            <template #dropdown>
              <ElDropdownMenu>
                <ElDropdownItem v-if="contextMenuNode" v-permission="'org:unit:add'" :command="{ action: 'add', node: contextMenuNode }">
                  <icon-ep-plus class="mr-8px" />
                  {{ getAddButtonText(contextMenuNode.unitType) }}
                </ElDropdownItem>
                <ElDropdownItem v-if="contextMenuNode" v-permission="'org:unit:edit'" :command="{ action: 'edit', node: contextMenuNode }">
                  <icon-ep-edit class="mr-8px" />
                  {{ $t('common.edit') }}
                </ElDropdownItem>
                <ElDropdownItem v-if="contextMenuNode" v-permission="'org:unit:delete'" :command="{ action: 'delete', node: contextMenuNode }" divided>
                  <span class="text-red-500">
                    <icon-ep-delete class="mr-8px" />
                    {{ $t('common.delete') }}
                  </span>
                </ElDropdownItem>
              </ElDropdownMenu>
            </template>
          </ElDropdown>
        </ElScrollbar>
      </ElCard>
    </div>

    <div class="org-right">
      <ElCard v-loading="statsLoading" shadow="hover" class="full-height">
        <template #header>
          <div class="stats-header">
            <div class="stats-title">
              <span class="stats-title-text">{{ pageTitle }}</span>
              <ElTag v-if="selectedNode" :type="getTypeTag(selectedNode.unitType).type" size="small">
                {{ getTypeTag(selectedNode.unitType).label }}
              </ElTag>
            </div>
            <div class="stats-actions">
              <ElButton v-if="selectedNode" type="primary" link @click="handleViewAll">
                <icon-ep-back class="mr-4px" />
                {{ $t('org.structure.viewWholeCompany') }}
              </ElButton>
            </div>
          </div>
        </template>

        <ElRow :gutter="16" class="mb-16px">
          <ElCol :span="6">
            <ElCard shadow="never" body-style="padding: 18px">
              <ElStatistic :title="$t('common.totalEmployees')" :value="statistics.totalCount">
                <template #prefix>
                  <icon-ep-user class="text-primary" />
                </template>
              </ElStatistic>
            </ElCard>
          </ElCol>
          <ElCol :span="6">
            <ElCard shadow="never" body-style="padding: 18px">
              <ElStatistic :title="$t('org.structure.maleEmployees')" :value="maleCount">
                <template #prefix>
                  <icon-ep-male class="text-blue-500" />
                </template>
              </ElStatistic>
            </ElCard>
          </ElCol>
          <ElCol :span="6">
            <ElCard shadow="never" body-style="padding: 18px">
              <ElStatistic :title="$t('org.structure.femaleEmployees')" :value="femaleCount">
                <template #prefix>
                  <icon-ep-female class="text-pink-500" />
                </template>
              </ElStatistic>
            </ElCard>
          </ElCol>
          <ElCol :span="6">
            <ElCard shadow="never" body-style="padding: 18px">
              <ElStatistic :title="$t('home.common.activeEmployees')" :value="activeCount">
                <template #prefix>
                  <icon-ep-check class="text-green-500" />
                </template>
              </ElStatistic>
            </ElCard>
          </ElCol>
        </ElRow>

        <div class="dimension-bar">
          <ElRadioGroup v-model="statsDimension" size="small">
            <ElRadioButton v-for="item in dimensionOptions" :key="item.value" :value="item.value">
              {{ item.label }}
            </ElRadioButton>
          </ElRadioGroup>
        </div>

        <ElRow :gutter="16">
          <ElCol :span="12">
            <ElCard shadow="never" class="stats-panel">
              <template #header>
                <span class="font-bold">{{ chartTitle }}</span>
              </template>

              <div v-if="tableData.length" class="stats-panel-body">
                <ElTable :data="tableData" border stripe size="small">
                  <ElTableColumn prop="index" :label="$t('common.index2')" width="70" align="center" />
                  <ElTableColumn :label="statsDimension === 'age' ? $t('org.structure.ageRange') : $t('common.category')" prop="label" min-width="120" />
                  <ElTableColumn prop="count" :label="$t('common.employees')" width="90" align="center">
                    <template #default="{ row }">
                      <span class="text-primary font-bold">{{ row.count }}</span>
                    </template>
                  </ElTableColumn>
                  <ElTableColumn :label="$t('org.structure.percentage')" width="150" align="center">
                    <template #default="{ row }">
                      <div class="percent-cell">
                        <ElProgress :percentage="Number(row.percent)" :stroke-width="8" :show-text="false" />
                        <span class="percent-text">{{ row.percent }}%</span>
                      </div>
                    </template>
                  </ElTableColumn>
                </ElTable>
                <div class="stats-total">
                  {{ $t('common.total') }}
                  <span class="text-primary font-bold">{{ statistics.totalCount }}</span>
                  {{ $t('org.structure.people') }}
                </div>
              </div>
              <ElEmpty v-else :description="$t('org.structure.noStatisticsData')" :image-size="80" />
            </ElCard>
          </ElCol>

          <ElCol :span="12">
            <ElCard shadow="never" class="stats-panel">
              <template #header>
                <span class="font-bold">{{ chartTitle }}{{ $t('org.structure.chart') }}</span>
              </template>
              <div v-if="tableData.length" ref="chartRef" class="chart-container" />
              <ElEmpty v-else :description="$t('org.structure.noChartData')" :image-size="80" />
            </ElCard>
          </ElCol>
        </ElRow>
      </ElCard>
    </div>

    <ElDialog v-model="dialogVisible" :title="dialogTitle" width="680px" destroy-on-close>
      <ElForm :model="editingData" label-width="110px">
        <ElFormItem v-if="operateType === 'add'" :label="$t('org.structure.parentOrganization')">
          <ElInput :model-value="getParentName()" disabled />
        </ElFormItem>

        <ElFormItem :label="$t('org.structure.organizationType')" required>
          <ElRadioGroup v-model="editingData.unitType" :disabled="operateType === 'edit'">
            <ElRadioButton
              v-for="item in operateType === 'add' ? getAvailableTypes(parentNode?.unitType) : typeOptions"
              :key="item.value"
              :value="item.value"
            >
              {{ item.label }}
            </ElRadioButton>
          </ElRadioGroup>
        </ElFormItem>

        <ElFormItem :label="$t('org.structure.organizationCode')" required>
          <ElInput
            v-model="editingData.unitCode"
            :placeholder="$t('org.structure.eGGrp001Com001Dept001')"
            maxlength="50"
            show-word-limit
          />
        </ElFormItem>

        <ElFormItem :label="$t('org.structure.organizationName')" required>
          <ElInput v-model="editingData.unitName" :placeholder="$t('org.structure.pleaseEnterOrganizationName')" maxlength="100" show-word-limit />
        </ElFormItem>

        <ElFormItem :label="$t('org.structure.abbreviation')">
          <ElInput v-model="editingData.shortName" :placeholder="$t('org.structure.pleaseEnterAbbreviation')" maxlength="50" />
        </ElFormItem>

        <ElFormItem :label="$t('common.manager')">
          <ElSelect v-model="editingData.leaderId" :placeholder="$t('org.structure.pleaseSelectAManager')" class="w-full" filterable clearable>
            <ElOption
              v-for="employee in employeeOptions"
              :key="employee.id"
              :label="`${employee.name} (${employee.employeeNo})`"
              :value="employee.id"
            />
          </ElSelect>
        </ElFormItem>

        <ElFormItem :label="$t('common.contactPhone')">
          <ElInput v-model="editingData.phone" :placeholder="$t('hr.employee.pleaseEnterContactPhone')" maxlength="20" />
        </ElFormItem>

        <ElFormItem :label="$t('common.email')">
          <ElInput v-model="editingData.email" :placeholder="$t('common.pleaseEnterEmail')" maxlength="100" />
        </ElFormItem>

        <ElFormItem v-if="editingData.unitType !== OrgUnitType.DEPT" :label="$t('org.structure.officeAddress')">
          <ElInput v-model="editingData.address" :placeholder="$t('org.structure.pleaseEnterOfficeAddress')" maxlength="255" show-word-limit />
        </ElFormItem>

        <template v-if="isCompanyUnit">
          <ElFormItem :label="$t('org.structure.clockConfiguration')">
            <ElAlert
              :title="
                operateType === 'add'
                  ? $t('org.structure.afterTheCompanyIsCreatedGoToAttendanceClockLocationsToMaintainClockLocationsAndAssignEmployees')
                  : $t('org.structure.clockLocationsHaveMovedToAttendanceClockLocationsOnlyCompanyAndDepartmentBasicInfoIsMaintainedHere')
              "
              type="info"
              :closable="false"
              show-icon
            />
          </ElFormItem>
        </template>

        <ElFormItem :label="$t('common.description')">
          <ElInput
            v-model="editingData.description"
            type="textarea"
            :rows="3"
            :placeholder="$t('approval.flow.pleaseEnterDescription')"
            maxlength="500"
            show-word-limit
          />
        </ElFormItem>

        <ElFormItem :label="$t('common.sort')">
          <ElInputNumber v-model="editingData.sortOrder" :min="0" :max="999" class="w-full" />
        </ElFormItem>
      </ElForm>

      <template #footer>
        <ElButton @click="dialogVisible = false">{{ $t('common.cancel') }}</ElButton>
        <ElButton type="primary" :loading="submitLoading" @click="handleSubmit">
          {{ operateType === 'add' ? $t('org.structure.confirmAdd') : $t('org.structure.confirmSave') }}
        </ElButton>
      </template>
    </ElDialog>
  </div>
</template>

<style scoped>
.org-page {
  display: flex;
  gap: 16px;
  height: calc(100vh - 100px);
  min-height: 720px;
}

.org-left {
  width: 320px;
  flex-shrink: 0;
}

.org-right {
  flex: 1;
  min-width: 0;
}

.full-height {
  height: 100%;
}

.tree-header,
.stats-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.tree-title,
.stats-title-text {
  font-size: 16px;
  font-weight: 700;
}

.stats-title {
  display: flex;
  align-items: center;
  gap: 8px;
}

.stats-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.dimension-bar {
  margin-bottom: 16px;
}

.tree-node {
  display: flex;
  width: 100%;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  padding-right: 8px;
}

.tree-node-label {
  display: flex;
  min-width: 0;
  flex: 1;
  align-items: center;
  gap: 8px;
}

.tree-node-badge :deep(.el-badge__content) {
  font-size: 11px;
}

.node-actions {
  display: flex;
  gap: 2px;
  opacity: 0;
  transition: opacity 0.2s ease;
}

.tree-node:hover .node-actions {
  opacity: 1;
}

.stats-panel {
  min-height: 380px;
}

.stats-panel-body {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.stats-total {
  padding: 8px 12px;
  border-radius: 6px;
  background: var(--el-fill-color-lighter);
  font-size: 14px;
}

.percent-cell {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}

.percent-text {
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.chart-container {
  height: 340px;
}

:deep(.el-card__header) {
  padding: 14px 20px;
}

:deep(.el-tree-node__content) {
  height: 40px;
  margin: 2px 0;
  border-radius: 6px;
}

:deep(.el-tree-node__content:hover) {
  background-color: var(--el-fill-color-light);
}

:deep(.el-tree-node.is-current > .el-tree-node__content) {
  background-color: var(--el-color-primary-light-9);
}

:deep(.el-statistic__head) {
  color: var(--el-text-color-secondary);
  font-size: 13px;
}

:deep(.el-statistic__content) {
  font-size: 24px;
  font-weight: 700;
}
</style>
