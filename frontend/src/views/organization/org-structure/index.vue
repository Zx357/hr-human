<script setup lang="ts">
import { computed, nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import * as echarts from 'echarts';
import {
  OrgUnitType,
  createOrgUnit,
  deleteOrgUnit,
  fetchAllOrgStatistics,
  fetchOrgStatistics,
  fetchOrgTreeWithCount,
  fetchOrgUnitById,
  updateOrgUnit
} from '@/service/api/organization';
import { fetchEmployeePage } from '@/service/api/hr';
import AttendanceLocationPicker from './components/attendance-location-picker.vue';

defineOptions({ name: 'OrgStructure' });

type StatsDimension = 'age' | 'gender' | 'education' | 'status' | 'employeeType';
type ChartDatum = { name: string; value: number };
type TableRow = { index: number; label: string; count: number; percent: string };
type ContextMenuCommand = { action: 'add' | 'edit' | 'delete' | 'attendance'; node: Api.Organization.OrgUnit };
type TypeOption = { label: string; value: number };
type AttendanceLocationPayload = { address: string; latitude: number; longitude: number };

const loading = ref(false);
const statsLoading = ref(false);
const submitLoading = ref(false);
const dialogVisible = ref(false);
const attendanceDialogVisible = ref(false);
const attendanceDialogLoading = ref(false);
const attendanceSubmitLoading = ref(false);
const attendanceLocationPickerVisible = ref(false);
const operateType = ref<'add' | 'edit'>('add');
const editingId = ref<number | null>(null);
const attendanceEditingId = ref<number | null>(null);
const attendanceCompanyName = ref('');

const treeData = ref<Api.Organization.OrgUnit[]>([]);
const selectedNode = ref<Api.Organization.OrgUnit | null>(null);
const employeeOptions = ref<Api.Hr.Employee[]>([]);
const searchKeyword = ref('');

const treeRef = ref<any>(null);
const contextMenuRef = ref<any>(null);
const contextMenuNode = ref<Api.Organization.OrgUnit | null>(null);
const contextMenuVirtualRef = ref<any>(null);
const chartRef = ref<HTMLDivElement | null>(null);

const typeOptions: TypeOption[] = [
  { label: '集团', value: OrgUnitType.GROUP },
  { label: '公司', value: OrgUnitType.COMPANY },
  { label: '部门', value: OrgUnitType.DEPT }
];

const dimensionOptions: Array<{ label: string; value: StatsDimension }> = [
  { label: '年龄分析', value: 'age' },
  { label: '性别分析', value: 'gender' },
  { label: '学历分析', value: 'education' },
  { label: '在职状态', value: 'status' },
  { label: '员工类型', value: 'employeeType' }
];

const chartColors = ['#2563eb', '#10b981', '#f59e0b', '#ef4444', '#8b5cf6', '#06b6d4', '#f97316', '#64748b'];
const statsDimension = ref<StatsDimension>('age');
const parentNode = ref<Api.Organization.OrgUnit | null>(null);

const editingData = ref<Api.Organization.OrgUnitForm>(createEmptyForm(OrgUnitType.GROUP, 0));
const attendanceData = ref<Api.Organization.OrgUnitForm>(createEmptyForm(OrgUnitType.COMPANY, 0));
const isCompanyUnit = computed(() => editingData.value.unitType === OrgUnitType.COMPANY);
const canConfigureSelectedAttendance = computed(() => selectedNode.value?.unitType === OrgUnitType.COMPANY);

function hasCoordinateValue(value?: number) {
  return value !== undefined && value !== null;
}

function hasAttendanceLocationConfig(form: Api.Organization.OrgUnitForm) {
  return (
    hasCoordinateValue(form.attendanceLatitude) ||
    hasCoordinateValue(form.attendanceLongitude) ||
    Boolean(form.attendanceAddress?.trim())
  );
}

const hasAttendanceDialogLocationConfig = computed(() => hasAttendanceLocationConfig(attendanceData.value));

const statistics = ref<Api.Organization.OrgStatistics>(createEmptyStatistics());

let chartInstance: echarts.ECharts | null = null;
const resizeHandler = () => chartInstance?.resize();

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
      { label: '集团', value: OrgUnitType.GROUP },
      { label: '公司', value: OrgUnitType.COMPANY }
    ];
  }
  if (parentType === OrgUnitType.GROUP) {
    return [{ label: '公司', value: OrgUnitType.COMPANY }];
  }
  return [{ label: '部门', value: OrgUnitType.DEPT }];
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

const maleCount = computed(() => findDistributionValue(statistics.value.genderDistribution, ['男', 'Male']));
const femaleCount = computed(() => findDistributionValue(statistics.value.genderDistribution, ['女', 'Female']));
const activeCount = computed(() => findDistributionValue(statistics.value.statusDistribution, ['在职', 'Active']));

const dialogTitle = computed(() => (operateType.value === 'add' ? '新增组织' : '编辑组织'));
const pageTitle = computed(() =>
  selectedNode.value ? `${selectedNode.value.unitName} - 人员结构分析` : '全公司 - 人员结构分析'
);
const chartTitle = computed(() => {
  const dimensionLabel = dimensionOptions.find(item => item.value === statsDimension.value)?.label ?? '';
  const prefix = selectedNode.value?.unitName ?? '全公司';
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

  const data = getChartData();
  if (!data.length) {
    chartInstance?.dispose();
    chartInstance = null;
    return;
  }

  if (!chartInstance) {
    chartInstance = echarts.init(chartRef.value);
  } else {
    chartInstance.clear();
  }

  chartInstance.setOption({
    color: chartColors,
    tooltip: {
      trigger: 'item',
      formatter: '{b}: {c} 人 ({d}%)'
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
        data
      }
    ]
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
  if (command.action === 'attendance') {
    runAsyncTask(handleConfigureAttendance(command.node));
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

async function handleConfigureAttendance(row: Api.Organization.OrgUnit) {
  if (row.unitType !== OrgUnitType.COMPANY) {
    ElMessage.warning('只有公司节点支持配置打卡点');
    return;
  }

  attendanceDialogLoading.value = true;
  try {
    const response = await fetchOrgUnitById(row.id);
    const company = response.data ?? row;
    attendanceEditingId.value = company.id;
    attendanceCompanyName.value = company.unitName;
    attendanceData.value = buildOrgUnitForm(company);
    attendanceDialogVisible.value = true;
  } catch (error: any) {
    ElMessage.error(error?.message || '加载打卡配置失败');
  } finally {
    attendanceDialogLoading.value = false;
  }
}

async function handleDelete(row: Api.Organization.OrgUnit) {
  if (row.children?.length) {
    ElMessage.warning('当前节点下还有子节点，请先删除子节点');
    return;
  }

  try {
    await ElMessageBox.confirm(`确认删除“${row.unitName}”吗？`, '删除确认', { type: 'warning' });
    await deleteOrgUnit(row.id);
    ElMessage.success('删除成功');
    await loadTreeData();
    if (selectedNode.value?.id === row.id) {
      selectedNode.value = null;
      await loadStatistics();
    }
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error?.message || '删除失败');
    }
  }
}

function validateAttendanceForm(form: Api.Organization.OrgUnitForm, isCompany: boolean) {
  if (!isCompany) {
    form.attendanceAddress = '';
    form.attendanceLatitude = undefined;
    form.attendanceLongitude = undefined;
    form.attendanceRange = undefined;
    return true;
  }

  const { attendanceLatitude, attendanceLongitude, attendanceRange } = form;
  const hasAddress = Boolean(form.attendanceAddress?.trim());
  const hasLatitude = attendanceLatitude !== undefined && attendanceLatitude !== null;
  const hasLongitude = attendanceLongitude !== undefined && attendanceLongitude !== null;
  const hasRange = attendanceRange !== undefined && attendanceRange !== null;

  if (hasLatitude !== hasLongitude) {
    ElMessage.warning('请同时填写打卡纬度和经度');
    return false;
  }

  if (hasLatitude) {
    if ((attendanceLatitude as number) < -90 || (attendanceLatitude as number) > 90) {
      ElMessage.warning('打卡纬度必须在 -90 到 90 之间');
      return false;
    }
    if ((attendanceLongitude as number) < -180 || (attendanceLongitude as number) > 180) {
      ElMessage.warning('打卡经度必须在 -180 到 180 之间');
      return false;
    }

    if (!hasRange) {
      form.attendanceRange = 300;
    } else if ((attendanceRange as number) <= 0) {
      ElMessage.warning('打卡范围必须大于 0');
      return false;
    }
  } else if (hasAddress || hasRange) {
    ElMessage.warning('配置打卡地址或范围时，请先填写打卡经纬度');
    return false;
  }

  return true;
}

function handleOpenAttendanceLocationPicker() {
  attendanceLocationPickerVisible.value = true;
}

function handleAttendanceLocationConfirm(location: AttendanceLocationPayload) {
  attendanceData.value.attendanceAddress = location.address;
  attendanceData.value.attendanceLatitude = location.latitude;
  attendanceData.value.attendanceLongitude = location.longitude;

  if (!attendanceData.value.attendanceRange || attendanceData.value.attendanceRange <= 0) {
    attendanceData.value.attendanceRange = 300;
  }
}

function handleClearAttendanceLocation() {
  attendanceData.value.attendanceAddress = '';
  attendanceData.value.attendanceLatitude = undefined;
  attendanceData.value.attendanceLongitude = undefined;
  attendanceData.value.attendanceRange = undefined;
}

async function handleSubmit() {
  if (!editingData.value.unitCode?.trim()) {
    ElMessage.warning('请输入组织编码');
    return;
  }

  if (!editingData.value.unitName?.trim()) {
    ElMessage.warning('请输入组织名称');
    return;
  }

  if (!validateAttendanceForm(editingData.value, isCompanyUnit.value)) {
    return;
  }

  submitLoading.value = true;
  try {
    if (operateType.value === 'add') {
      await createOrgUnit(editingData.value);
      ElMessage.success('新增成功');
    } else if (editingId.value !== null) {
      await updateOrgUnit(editingId.value, editingData.value);
      ElMessage.success('保存成功');
    }

    dialogVisible.value = false;
    await loadTreeData();
    await loadStatistics(selectedNode.value?.id);
  } catch (error: any) {
    ElMessage.error(error?.message || '保存失败');
  } finally {
    submitLoading.value = false;
  }
}

async function handleAttendanceSubmit() {
  if (attendanceEditingId.value === null) {
    ElMessage.warning('当前公司信息缺失，无法保存打卡配置');
    return;
  }

  if (!validateAttendanceForm(attendanceData.value, true)) {
    return;
  }

  attendanceSubmitLoading.value = true;
  try {
    await updateOrgUnit(attendanceEditingId.value, attendanceData.value);
    ElMessage.success('打卡配置已保存');
    attendanceDialogVisible.value = false;
    await loadTreeData();
    if (selectedNode.value?.id === attendanceEditingId.value) {
      selectedNode.value = {
        ...selectedNode.value,
        attendanceAddress: attendanceData.value.attendanceAddress,
        attendanceLatitude: attendanceData.value.attendanceLatitude,
        attendanceLongitude: attendanceData.value.attendanceLongitude,
        attendanceRange: attendanceData.value.attendanceRange
      };
    }
  } catch (error: any) {
    ElMessage.error(error?.message || '保存打卡配置失败');
  } finally {
    attendanceSubmitLoading.value = false;
  }
}

function getTypeTag(type: number) {
  const typeMap: Record<number, { label: string; type: 'danger' | 'warning' | 'primary' | 'info' }> = {
    [OrgUnitType.GROUP]: { label: '集团', type: 'danger' },
    [OrgUnitType.COMPANY]: { label: '公司', type: 'warning' },
    [OrgUnitType.DEPT]: { label: '部门', type: 'primary' }
  };
  return typeMap[type] ?? { label: '未知', type: 'info' };
}

function getAddButtonText(type?: number) {
  if (!type) {
    return '新增';
  }
  if (type === OrgUnitType.GROUP) {
    return '添加公司';
  }
  if (type === OrgUnitType.COMPANY) {
    return '添加部门';
  }
  return '添加子节点';
}

function getParentName() {
  return parentNode.value?.unitName ?? '顶级节点';
}

onMounted(async () => {
  window.addEventListener('resize', resizeHandler);
  await Promise.all([loadTreeData(), loadEmployees(), loadStatistics()]);
  await nextTick();
  renderChart();
});

onBeforeUnmount(() => {
  window.removeEventListener('resize', resizeHandler);
  chartInstance?.dispose();
  chartInstance = null;
});
</script>

<template>
  <div class="org-page">
    <div class="org-left">
      <ElCard shadow="hover" class="full-height">
        <template #header>
          <div class="tree-header">
            <span class="tree-title">组织架构</span>
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

        <ElInput v-model="searchKeyword" placeholder="搜索组织名称或编码" clearable class="mb-12px">
          <template #prefix>
            <icon-ep-search />
          </template>
        </ElInput>

        <ElButton type="primary" size="small" class="mb-12px" @click="handleAdd()">
          <icon-ep-plus class="mr-4px" />
          新增顶级组织
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
                <div class="node-actions">
                  <ElButton type="success" link size="small" @click.stop="handleAdd(data)">
                    <icon-ep-plus />
                  </ElButton>
                  <ElButton
                    v-if="data.unitType === OrgUnitType.COMPANY"
                    type="warning"
                    link
                    size="small"
                    @click.stop="handleConfigureAttendance(data)"
                  >
                    打卡
                  </ElButton>
                  <ElButton type="primary" link size="small" @click.stop="handleEdit(data)">
                    <icon-ep-edit />
                  </ElButton>
                  <ElButton type="danger" link size="small" @click.stop="handleDelete(data)">
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
                <ElDropdownItem v-if="contextMenuNode" :command="{ action: 'add', node: contextMenuNode }">
                  <icon-ep-plus class="mr-8px" />
                  {{ getAddButtonText(contextMenuNode.unitType) }}
                </ElDropdownItem>
                <ElDropdownItem
                  v-if="contextMenuNode && contextMenuNode.unitType === OrgUnitType.COMPANY"
                  :command="{ action: 'attendance', node: contextMenuNode }"
                >
                  <icon-ep-check class="mr-8px" />
                  打卡设置
                </ElDropdownItem>
                <ElDropdownItem v-if="contextMenuNode" :command="{ action: 'edit', node: contextMenuNode }">
                  <icon-ep-edit class="mr-8px" />
                  编辑
                </ElDropdownItem>
                <ElDropdownItem v-if="contextMenuNode" :command="{ action: 'delete', node: contextMenuNode }" divided>
                  <span class="text-red-500">
                    <icon-ep-delete class="mr-8px" />
                    删除
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
              <ElButton
                v-if="canConfigureSelectedAttendance && selectedNode"
                type="warning"
                plain
                size="small"
                @click="handleConfigureAttendance(selectedNode)"
              >
                打卡设置
              </ElButton>
              <ElButton v-if="selectedNode" type="primary" link @click="handleViewAll">
                <icon-ep-back class="mr-4px" />
                查看全公司
              </ElButton>
            </div>
          </div>
        </template>

        <ElRow :gutter="16" class="mb-16px">
          <ElCol :span="6">
            <ElCard shadow="never" body-style="padding: 18px">
              <ElStatistic title="员工总数" :value="statistics.totalCount">
                <template #prefix>
                  <icon-ep-user class="text-primary" />
                </template>
              </ElStatistic>
            </ElCard>
          </ElCol>
          <ElCol :span="6">
            <ElCard shadow="never" body-style="padding: 18px">
              <ElStatistic title="男性员工" :value="maleCount">
                <template #prefix>
                  <icon-ep-male class="text-blue-500" />
                </template>
              </ElStatistic>
            </ElCard>
          </ElCol>
          <ElCol :span="6">
            <ElCard shadow="never" body-style="padding: 18px">
              <ElStatistic title="女性员工" :value="femaleCount">
                <template #prefix>
                  <icon-ep-female class="text-pink-500" />
                </template>
              </ElStatistic>
            </ElCard>
          </ElCol>
          <ElCol :span="6">
            <ElCard shadow="never" body-style="padding: 18px">
              <ElStatistic title="在职员工" :value="activeCount">
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
                  <ElTableColumn prop="index" label="序号" width="70" align="center" />
                  <ElTableColumn :label="statsDimension === 'age' ? '年龄段' : '类别'" prop="label" min-width="120" />
                  <ElTableColumn prop="count" label="人数" width="90" align="center">
                    <template #default="{ row }">
                      <span class="text-primary font-bold">{{ row.count }}</span>
                    </template>
                  </ElTableColumn>
                  <ElTableColumn label="占比" width="150" align="center">
                    <template #default="{ row }">
                      <div class="percent-cell">
                        <ElProgress :percentage="Number(row.percent)" :stroke-width="8" :show-text="false" />
                        <span class="percent-text">{{ row.percent }}%</span>
                      </div>
                    </template>
                  </ElTableColumn>
                </ElTable>
                <div class="stats-total">
                  合计
                  <span class="text-primary font-bold">{{ statistics.totalCount }}</span>
                  人
                </div>
              </div>
              <ElEmpty v-else description="暂无统计数据" :image-size="80" />
            </ElCard>
          </ElCol>

          <ElCol :span="12">
            <ElCard shadow="never" class="stats-panel">
              <template #header>
                <span class="font-bold">{{ chartTitle }}图表</span>
              </template>
              <div v-if="tableData.length" ref="chartRef" class="chart-container" />
              <ElEmpty v-else description="暂无图表数据" :image-size="80" />
            </ElCard>
          </ElCol>
        </ElRow>
      </ElCard>
    </div>

    <ElDialog v-model="dialogVisible" :title="dialogTitle" width="680px" destroy-on-close>
      <ElForm :model="editingData" label-width="110px">
        <ElFormItem v-if="operateType === 'add'" label="上级组织">
          <ElInput :model-value="getParentName()" disabled />
        </ElFormItem>

        <ElFormItem label="组织类型" required>
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

        <ElFormItem label="组织编码" required>
          <ElInput
            v-model="editingData.unitCode"
            placeholder="例如：GRP001 / COM001 / DEPT001"
            maxlength="50"
            show-word-limit
          />
        </ElFormItem>

        <ElFormItem label="组织名称" required>
          <ElInput v-model="editingData.unitName" placeholder="请输入组织名称" maxlength="100" show-word-limit />
        </ElFormItem>

        <ElFormItem label="简称">
          <ElInput v-model="editingData.shortName" placeholder="请输入简称" maxlength="50" />
        </ElFormItem>

        <ElFormItem label="负责人">
          <ElSelect v-model="editingData.leaderId" placeholder="请选择负责人" class="w-full" filterable clearable>
            <ElOption
              v-for="employee in employeeOptions"
              :key="employee.id"
              :label="`${employee.name} (${employee.employeeNo})`"
              :value="employee.id"
            />
          </ElSelect>
        </ElFormItem>

        <ElFormItem label="联系电话">
          <ElInput v-model="editingData.phone" placeholder="请输入联系电话" maxlength="20" />
        </ElFormItem>

        <ElFormItem label="邮箱">
          <ElInput v-model="editingData.email" placeholder="请输入邮箱" maxlength="100" />
        </ElFormItem>

        <ElFormItem v-if="editingData.unitType !== OrgUnitType.DEPT" label="办公地址">
          <ElInput v-model="editingData.address" placeholder="请输入办公地址" maxlength="255" show-word-limit />
        </ElFormItem>

        <template v-if="isCompanyUnit">
          <ElFormItem label="打卡配置">
            <ElAlert
              :title="
                operateType === 'add'
                  ? '公司创建完成后，可在左侧公司节点或右侧顶部点击“打卡设置”单独维护打卡位置。'
                  : '打卡点已拆分为独立入口，请使用“打卡设置”按钮维护地图位置、经纬度和打卡范围。'
              "
              type="info"
              :closable="false"
              show-icon
            />
          </ElFormItem>
        </template>

        <ElFormItem label="描述">
          <ElInput
            v-model="editingData.description"
            type="textarea"
            :rows="3"
            placeholder="请输入描述"
            maxlength="500"
            show-word-limit
          />
        </ElFormItem>

        <ElFormItem label="排序">
          <ElInputNumber v-model="editingData.sortOrder" :min="0" :max="999" class="w-full" />
        </ElFormItem>
      </ElForm>

      <template #footer>
        <ElButton @click="dialogVisible = false">取消</ElButton>
        <ElButton type="primary" :loading="submitLoading" @click="handleSubmit">
          {{ operateType === 'add' ? '确认新增' : '确认保存' }}
        </ElButton>
      </template>
    </ElDialog>

    <ElDialog
      v-model="attendanceDialogVisible"
      :title="attendanceCompanyName ? `${attendanceCompanyName} - 打卡设置` : '打卡设置'"
      width="720px"
      destroy-on-close
    >
      <div v-loading="attendanceDialogLoading">
        <ElForm :model="attendanceData" label-width="110px">
          <ElFormItem label="打卡说明">
            <ElAlert
              title="这里配置的小程序考勤点会用于地图打卡校验，请填写 GCJ-02 坐标。"
              type="info"
              :closable="false"
              show-icon
            />
          </ElFormItem>

          <ElFormItem label="地图选点">
            <div class="attendance-picker-actions">
              <ElButton type="primary" plain @click="handleOpenAttendanceLocationPicker">地图选择打卡点</ElButton>
              <ElButton
                link
                type="danger"
                :disabled="!hasAttendanceDialogLocationConfig"
                @click="handleClearAttendanceLocation"
              >
                清空打卡配置
              </ElButton>
              <span class="attendance-picker-tip">地图点击后会自动回填地址和经纬度</span>
            </div>
          </ElFormItem>

          <ElFormItem label="打卡地址">
            <ElInput
              v-model="attendanceData.attendanceAddress"
              placeholder="用于小程序展示，可与办公地址不同"
              maxlength="255"
              show-word-limit
            />
          </ElFormItem>

          <ElRow :gutter="12">
            <ElCol :span="12">
              <ElFormItem label="打卡纬度">
                <ElInputNumber
                  v-model="attendanceData.attendanceLatitude"
                  class="w-full"
                  :precision="6"
                  :step="0.000001"
                  :min="-90"
                  :max="90"
                  controls-position="right"
                />
              </ElFormItem>
            </ElCol>
            <ElCol :span="12">
              <ElFormItem label="打卡经度">
                <ElInputNumber
                  v-model="attendanceData.attendanceLongitude"
                  class="w-full"
                  :precision="6"
                  :step="0.000001"
                  :min="-180"
                  :max="180"
                  controls-position="right"
                />
              </ElFormItem>
            </ElCol>
          </ElRow>

          <ElFormItem label="打卡范围">
            <ElInputNumber
              v-model="attendanceData.attendanceRange"
              class="w-full"
              :min="1"
              :max="5000"
              :step="10"
              controls-position="right"
              placeholder="单位：米，默认 300"
            />
          </ElFormItem>
        </ElForm>
      </div>

      <template #footer>
        <ElButton @click="attendanceDialogVisible = false">取消</ElButton>
        <ElButton type="primary" :loading="attendanceSubmitLoading" @click="handleAttendanceSubmit">
          保存打卡配置
        </ElButton>
      </template>
    </ElDialog>

    <AttendanceLocationPicker
      v-model="attendanceLocationPickerVisible"
      :latitude="attendanceData.attendanceLatitude"
      :longitude="attendanceData.attendanceLongitude"
      :address="attendanceData.attendanceAddress"
      :reference-address="attendanceData.address"
      @confirm="handleAttendanceLocationConfirm"
    />
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

.attendance-picker-actions {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 12px;
}

.attendance-picker-tip {
  font-size: 12px;
  color: var(--el-text-color-secondary);
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
