<script setup lang="tsx">
import { onMounted, ref, computed, nextTick, watch } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { 
  fetchOrgTreeWithCount, 
  fetchOrgStatistics, 
  fetchAllOrgStatistics,
  createOrgUnit, 
  updateOrgUnit, 
  deleteOrgUnit, 
  OrgUnitType 
} from '@/service/api/organization';
import { fetchEmployeePage } from '@/service/api/hr';
import * as echarts from 'echarts';

defineOptions({ name: 'OrgStructure' });

// 基础数据
const loading = ref(false);
const treeData = ref<Api.Organization.OrgUnit[]>([]);
const selectedNode = ref<Api.Organization.OrgUnit | null>(null);
const expandedKeys = ref<number[]>([]);
const employeeOptions = ref<Api.Hr.Employee[]>([]);
const searchKeyword = ref('');
const treeRef = ref();

// 右键菜单
const contextMenuRef = ref();
const contextMenuVirtualRef = ref();
const contextMenuNode = ref<Api.Organization.OrgUnit | null>(null);

// 统计数据
const statsLoading = ref(false);
const statistics = ref<Api.Organization.OrgStatistics>({
  totalCount: 0,
  educationDistribution: [],
  genderDistribution: [],
  ageDistribution: [],
  statusDistribution: [],
  employeeTypeDistribution: []
});

// 统计维度选择
const statsDimension = ref('age');
const dimensionOptions = [
  { label: '年龄分析', value: 'age' },
  { label: '性别分析', value: 'gender' },
  { label: '学历分析', value: 'education' },
  { label: '在职状态', value: 'status' },
  { label: '员工类型', value: 'employeeType' }
];

// 图表颜色
const chartColors = ['#409eff', '#67c23a', '#e6a23c', '#f56c6c', '#909399', '#00d4ff', '#ff6b6b', '#9b59b6'];

// 表单相关
const dialogVisible = ref(false);
const operateType = ref<'add' | 'edit'>('add');
const submitLoading = ref(false);
const editingData = ref<Api.Organization.OrgUnitForm>({
  parentId: 0,
  unitType: OrgUnitType.GROUP,
  unitCode: '',
  unitName: '',
  sortOrder: 0
});
const parentNode = ref<Api.Organization.OrgUnit | null>(null);

const typeOptions = [
  { label: '集团', value: OrgUnitType.GROUP },
  { label: '公司', value: OrgUnitType.COMPANY },
  { label: '部门', value: OrgUnitType.DEPT }
];

// 过滤树节点
const filterNode = (value: string, data: Api.Organization.OrgUnit) => {
  if (!value) return true;
  return data.unitName.includes(value) || data.unitCode.includes(value);
};

watch(searchKeyword, (val) => treeRef.value?.filter(val));

// 当前维度的表格数据
const tableData = computed(() => {
  const total = statistics.value.totalCount || 1;
  switch (statsDimension.value) {
    case 'age':
      return statistics.value.ageDistribution.map((item, i) => ({
        index: i + 1, label: item.range, count: item.count,
        percent: ((item.count / total) * 100).toFixed(2)
      }));
    case 'gender':
      return statistics.value.genderDistribution.map((item, i) => ({
        index: i + 1, label: item.name, count: item.value,
        percent: ((item.value / total) * 100).toFixed(2)
      }));
    case 'education':
      return statistics.value.educationDistribution.map((item, i) => ({
        index: i + 1, label: item.name, count: item.value,
        percent: ((item.value / total) * 100).toFixed(2)
      }));
    case 'status':
      return statistics.value.statusDistribution.map((item, i) => ({
        index: i + 1, label: item.name, count: item.value,
        percent: ((item.value / total) * 100).toFixed(2)
      }));
    case 'employeeType':
      return statistics.value.employeeTypeDistribution.map((item, i) => ({
        index: i + 1, label: item.name, count: item.value,
        percent: ((item.value / total) * 100).toFixed(2)
      }));
    default:
      return [];
  }
});

const dimensionTitle = computed(() => {
  const prefix = selectedNode.value ? selectedNode.value.unitName : '全公司';
  const dim = dimensionOptions.find(d => d.value === statsDimension.value);
  return `${prefix}${dim?.label || ''}`;
});

// 统计卡片数据
const maleCount = computed(() => statistics.value.genderDistribution.find(g => g.name === '男')?.value || 0);
const femaleCount = computed(() => statistics.value.genderDistribution.find(g => g.name === '女')?.value || 0);
const activeCount = computed(() => statistics.value.statusDistribution.find(s => s.name === '在职')?.value || 0);

function getAllIds(nodes: Api.Organization.OrgUnit[]): number[] {
  const ids: number[] = [];
  for (const node of nodes) {
    ids.push(node.id);
    if (node.children?.length) ids.push(...getAllIds(node.children));
  }
  return ids;
}

async function loadTreeData() {
  loading.value = true;
  try {
    const res = await fetchOrgTreeWithCount();
    treeData.value = res.data || [];
    expandedKeys.value = getAllIds(treeData.value);
  } catch (error) {
    ElMessage.error('加载组织架构失败');
  } finally {
    loading.value = false;
  }
}

async function loadStatistics(orgId?: number) {
  statsLoading.value = true;
  try {
    const res = orgId ? await fetchOrgStatistics(orgId) : await fetchAllOrgStatistics();
    statistics.value = res.data || {
      totalCount: 0, educationDistribution: [], genderDistribution: [],
      ageDistribution: [], statusDistribution: [], employeeTypeDistribution: []
    };
    await nextTick();
    renderCurrentChart();
  } catch (error) {
    ElMessage.error('加载统计数据失败');
  } finally {
    statsLoading.value = false;
  }
}

async function loadEmployees() {
  try {
    const res = await fetchEmployeePage({ pageNum: 1, pageSize: 1000, status: 1 });
    employeeOptions.value = res.data?.records || [];
  } catch (error) {
    console.error('加载员工列表失败:', error);
  }
}

function renderCurrentChart() {
  const chartDom = document.getElementById('statsChart');
  if (!chartDom) return;
  
  const existingChart = echarts.getInstanceByDom(chartDom);
  if (existingChart) existingChart.dispose();
  
  const myChart = echarts.init(chartDom);
  let data: Array<{name: string; value: number}> = [];
  
  switch (statsDimension.value) {
    case 'age':
      data = statistics.value.ageDistribution.map(item => ({ name: item.range, value: item.count }));
      break;
    case 'gender':
      data = statistics.value.genderDistribution;
      break;
    case 'education':
      data = statistics.value.educationDistribution;
      break;
    case 'status':
      data = statistics.value.statusDistribution;
      break;
    case 'employeeType':
      data = statistics.value.employeeTypeDistribution;
      break;
  }
  
  const option = {
    color: chartColors,
    tooltip: { trigger: 'item', formatter: '{b}: {c}人 ({d}%)' },
    legend: { orient: 'vertical', right: 20, top: 'center', itemGap: 12 },
    series: [{
      type: 'pie',
      radius: ['40%', '70%'],
      center: ['35%', '50%'],
      avoidLabelOverlap: false,
      itemStyle: { borderRadius: 6, borderColor: '#fff', borderWidth: 2 },
      label: { show: false },
      emphasis: { label: { show: true, fontSize: 14, fontWeight: 'bold' } },
      labelLine: { show: false },
      data: data
    }]
  };
  
  myChart.setOption(option);
  window.addEventListener('resize', () => myChart.resize());
}

watch(statsDimension, () => nextTick(() => renderCurrentChart()));

function handleNodeClick(data: Api.Organization.OrgUnit) {
  selectedNode.value = data;
  loadStatistics(data.id);
}

function handleViewAll() {
  selectedNode.value = null;
  loadStatistics();
}

function handleNodeContextMenu(event: MouseEvent, data: any) {
  event.preventDefault();
  contextMenuNode.value = data;
  contextMenuVirtualRef.value = {
    getBoundingClientRect() {
      return { width: 0, height: 0, top: event.clientY, left: event.clientX };
    },
  };
  nextTick(() => contextMenuRef.value?.handleOpen());
}

function handleContextMenuCommand(command: { action: string; node: Api.Organization.OrgUnit }) {
  const { action, node } = command;
  switch (action) {
    case 'add': handleAdd(node); break;
    case 'edit': handleEdit(node); break;
    case 'delete': handleDelete(node); break;
  }
}

function getAvailableTypes(parentType?: number): typeof typeOptions {
  if (!parentType) return [{ label: '集团', value: OrgUnitType.GROUP }, { label: '公司', value: OrgUnitType.COMPANY }];
  if (parentType === OrgUnitType.GROUP) return [{ label: '公司', value: OrgUnitType.COMPANY }];
  return [{ label: '部门', value: OrgUnitType.DEPT }];
}

function handleAdd(parent?: Api.Organization.OrgUnit) {
  operateType.value = 'add';
  parentNode.value = parent || null;
  const availableTypes = getAvailableTypes(parent?.unitType);
  editingData.value = {
    parentId: parent?.id || 0, unitType: availableTypes[0].value,
    unitCode: '', unitName: '', shortName: '', leaderId: undefined,
    phone: '', email: '', address: '', description: '', sortOrder: 0
  };
  dialogVisible.value = true;
}

function handleEdit(row: Api.Organization.OrgUnit) {
  operateType.value = 'edit';
  parentNode.value = null;
  editingData.value = {
    parentId: row.parentId || 0, unitType: row.unitType, unitCode: row.unitCode,
    unitName: row.unitName, shortName: row.shortName, leaderId: row.leaderId,
    phone: row.phone, email: row.email, address: row.address,
    description: row.description, sortOrder: row.sortOrder || 0
  };
  (editingData.value as any).id = row.id;
  dialogVisible.value = true;
}

async function handleDelete(row: Api.Organization.OrgUnit) {
  if (row.children?.length) {
    ElMessage.warning('该节点下存在子节点，请先删除子节点');
    return;
  }
  try {
    await ElMessageBox.confirm(`确定要删除 "${row.unitName}" 吗？`, '删除确认', { type: 'warning' });
    await deleteOrgUnit(row.id);
    ElMessage.success('删除成功');
    loadTreeData();
    if (selectedNode.value?.id === row.id) {
      selectedNode.value = null;
      loadStatistics();
    }
  } catch (error: any) {
    if (error !== 'cancel') ElMessage.error(error.message || '删除失败');
  }
}

async function handleSubmit() {
  if (!editingData.value.unitCode?.trim()) { ElMessage.warning('请填写编码'); return; }
  if (!editingData.value.unitName?.trim()) { ElMessage.warning('请填写名称'); return; }

  submitLoading.value = true;
  try {
    if (operateType.value === 'add') {
      await createOrgUnit(editingData.value);
      ElMessage.success('新增成功');
    } else {
      await updateOrgUnit((editingData.value as any).id, editingData.value);
      ElMessage.success('更新成功');
    }
    dialogVisible.value = false;
    loadTreeData();
  } catch (error: any) {
    ElMessage.error(error.message || '保存失败');
  } finally {
    submitLoading.value = false;
  }
}

function getTypeTag(type: number) {
  const map: Record<number, { label: string; type: string }> = {
    1: { label: '集团', type: 'danger' },
    2: { label: '公司', type: 'warning' },
    3: { label: '部门', type: 'primary' }
  };
  return map[type] || { label: '未知', type: 'info' };
}

function getAddButtonText(type?: number) {
  if (!type) return '新增';
  if (type === OrgUnitType.GROUP) return '添加公司';
  if (type === OrgUnitType.COMPANY) return '添加部门';
  return '添加子部门';
}

function getParentName() {
  return parentNode.value ? parentNode.value.unitName : '顶级（无上级）';
}

function handleExpandAll() {
  const allIds = getAllIds(treeData.value);
  allIds.forEach(id => treeRef.value?.getNode(id)?.expand());
}

function handleCollapseAll() {
  const allIds = getAllIds(treeData.value);
  allIds.forEach(id => treeRef.value?.getNode(id)?.collapse());
}
function handleRefresh() {
  loadTreeData();
  selectedNode.value ? loadStatistics(selectedNode.value.id) : loadStatistics();
}

onMounted(() => {
  loadTreeData();
  loadEmployees();
  loadStatistics();
});
</script>

<template>
  <ElRow :gutter="16" class="h-full">
    <!-- 左侧组织树面板 -->
    <ElCol :span="7">
      <ElCard shadow="hover" class="h-full">
        <template #header>
          <div class="flex items-center justify-between">
            <span class="font-bold">组织架构</span>
            <ElButtonGroup size="small">
              <ElButton @click="handleExpandAll"><icon-ep-arrow-down /></ElButton>
              <ElButton @click="handleCollapseAll"><icon-ep-arrow-up /></ElButton>
              <ElButton @click="handleRefresh" :loading="loading"><icon-ep-refresh /></ElButton>
            </ElButtonGroup>
          </div>
        </template>
        
        <ElInput v-model="searchKeyword" placeholder="搜索组织名称或编码" clearable class="mb-12px">
          <template #prefix><icon-ep-search /></template>
        </ElInput>
        
        <ElButton type="primary" size="small" class="mb-12px" @click="handleAdd()">
          <icon-ep-plus class="mr-4px" />新增顶级组织
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
                <div class="flex items-center gap-8px flex-1 min-w-0">
                  <ElTag :type="getTypeTag(data.unitType).type as any" size="small">
                    {{ getTypeTag(data.unitType).label }}
                  </ElTag>
                  <span class="truncate">{{ data.unitName }}</span>
                  <ElBadge v-if="data.employeeCount > 0" :value="data.employeeCount" :max="999" type="info" class="ml-auto employee-badge" />
                </div>
                <div class="node-actions">
                  <ElButton type="success" link size="small" @click.stop="handleAdd(data)"><icon-ep-plus /></ElButton>
                  <ElButton type="primary" link size="small" @click.stop="handleEdit(data)"><icon-ep-edit /></ElButton>
                  <ElButton type="danger" link size="small" @click.stop="handleDelete(data)"><icon-ep-delete /></ElButton>
                </div>
              </div>
            </template>
          </ElTree>
          
          <!-- 右键菜单 -->
          <ElDropdown ref="contextMenuRef" :virtual-ref="contextMenuVirtualRef" virtual-triggering @command="handleContextMenuCommand">
            <template #dropdown>
              <ElDropdownMenu>
                <ElDropdownItem v-if="contextMenuNode" :command="{ action: 'add', node: contextMenuNode }">
                  <icon-ep-plus class="mr-8px" />{{ getAddButtonText(contextMenuNode.unitType) }}
                </ElDropdownItem>
                <ElDropdownItem v-if="contextMenuNode" :command="{ action: 'edit', node: contextMenuNode }">
                  <icon-ep-edit class="mr-8px" />编辑
                </ElDropdownItem>
                <ElDropdownItem v-if="contextMenuNode" :command="{ action: 'delete', node: contextMenuNode }" divided>
                  <span class="text-red-500"><icon-ep-delete class="mr-8px" />删除</span>
                </ElDropdownItem>
              </ElDropdownMenu>
            </template>
          </ElDropdown>
        </ElScrollbar>
      </ElCard>
    </ElCol>

    <!-- 右侧统计面板 -->
    <ElCol :span="17">
      <ElCard shadow="hover" class="h-full" v-loading="statsLoading">
        <template #header>
          <div class="stats-header">
            <div class="stats-title">
              <span class="font-bold text-lg">{{ selectedNode ? selectedNode.unitName : '全公司' }} - 人员结构分析</span>
              <ElTag v-if="selectedNode" :type="getTypeTag(selectedNode.unitType).type as any" size="small">
                {{ getTypeTag(selectedNode.unitType).label }}
              </ElTag>
              <ElButton v-if="selectedNode" type="primary" link size="small" @click="handleViewAll">
                <icon-ep-back class="mr-4px" />查看全公司
              </ElButton>
            </div>
            <div class="stats-dimension">
              <span class="text-gray-500 text-sm whitespace-nowrap">统计维度：</span>
              <ElSelect v-model="statsDimension" style="width: 130px" size="default" :teleported="true">
                <ElOption v-for="opt in dimensionOptions" :key="opt.value" :label="opt.label" :value="opt.value" />
              </ElSelect>
            </div>
          </div>
        </template>

        <!-- 统计卡片 -->
        <ElRow :gutter="16" class="mb-16px">
          <ElCol :span="6">
            <ElCard shadow="hover" body-style="padding: 16px">
              <ElStatistic title="员工总数" :value="statistics.totalCount">
                <template #prefix><icon-ep-user class="text-primary" /></template>
              </ElStatistic>
            </ElCard>
          </ElCol>
          <ElCol :span="6">
            <ElCard shadow="hover" body-style="padding: 16px">
              <ElStatistic title="男性员工" :value="maleCount">
                <template #prefix><icon-ep-male class="text-blue-500" /></template>
              </ElStatistic>
            </ElCard>
          </ElCol>
          <ElCol :span="6">
            <ElCard shadow="hover" body-style="padding: 16px">
              <ElStatistic title="女性员工" :value="femaleCount">
                <template #prefix><icon-ep-female class="text-pink-500" /></template>
              </ElStatistic>
            </ElCard>
          </ElCol>
          <ElCol :span="6">
            <ElCard shadow="hover" body-style="padding: 16px">
              <ElStatistic title="在职员工" :value="activeCount">
                <template #prefix><icon-ep-check class="text-green-500" /></template>
              </ElStatistic>
            </ElCard>
          </ElCol>
        </ElRow>

        <!-- 表格和图表 -->
        <ElRow :gutter="16">
          <ElCol :span="12">
            <ElCard shadow="never">
              <template #header>
                <span class="font-bold">{{ dimensionTitle }}</span>
              </template>
              <ElTable :data="tableData" border stripe size="small" max-height="320">
                <ElTableColumn prop="index" label="序号" width="60" align="center" />
                <ElTableColumn :label="statsDimension === 'age' ? '年龄段' : '类别'" prop="label" min-width="100" />
                <ElTableColumn prop="count" label="人数" width="80" align="center">
                  <template #default="{ row }">
                    <span class="text-primary font-bold">{{ row.count }}</span>
                  </template>
                </ElTableColumn>
                <ElTableColumn label="占比" width="120" align="center">
                  <template #default="{ row }">
                    <ElProgress :percentage="Number(row.percent)" :stroke-width="8" :show-text="false" />
                    <span class="text-xs text-gray-500">{{ row.percent }}%</span>
                  </template>
                </ElTableColumn>
              </ElTable>
              <ElDescriptions :column="4" border class="mt-8px">
                <ElDescriptionsItem label="合计">{{ statistics.totalCount }}人</ElDescriptionsItem>
              </ElDescriptions>
              <ElEmpty v-if="tableData.length === 0" description="暂无数据" :image-size="60" />
            </ElCard>
          </ElCol>
          <ElCol :span="12">
            <ElCard shadow="never">
              <template #header>
                <span class="font-bold">{{ dimensionTitle }}图表</span>
              </template>
              <div id="statsChart" class="h-320px"></div>
              <ElEmpty v-if="tableData.length === 0" description="暂无图表数据" :image-size="60" />
            </ElCard>
          </ElCol>
        </ElRow>
      </ElCard>
    </ElCol>

    <!-- 新增/编辑弹出框 -->
    <ElDialog v-model="dialogVisible" :title="operateType === 'add' ? '新增组织' : '编辑组织'" width="550px" destroy-on-close>
      <ElForm label-width="100px" :model="editingData">
        <ElFormItem v-if="operateType === 'add'" label="上级组织">
          <ElInput :model-value="getParentName()" disabled />
        </ElFormItem>
        <ElFormItem label="类型" required>
          <ElRadioGroup v-model="editingData.unitType" :disabled="operateType === 'edit'">
            <ElRadioButton v-for="item in (operateType === 'add' ? getAvailableTypes(parentNode?.unitType) : typeOptions)" :key="item.value" :value="item.value">
              {{ item.label }}
            </ElRadioButton>
          </ElRadioGroup>
        </ElFormItem>
        <ElFormItem label="编码" required>
          <ElInput v-model="editingData.unitCode" placeholder="如：GRP001、COM001、DEPT001" :maxlength="50" show-word-limit />
        </ElFormItem>
        <ElFormItem label="名称" required>
          <ElInput v-model="editingData.unitName" placeholder="请输入组织名称" :maxlength="100" show-word-limit />
        </ElFormItem>
        <ElFormItem label="简称">
          <ElInput v-model="editingData.shortName" placeholder="请输入简称（可选）" :maxlength="50" />
        </ElFormItem>
        <ElFormItem label="负责人">
          <ElSelect v-model="editingData.leaderId" placeholder="请选择负责人" class="w-full" filterable clearable>
            <ElOption v-for="emp in employeeOptions" :key="emp.id" :label="`${emp.name} (${emp.employeeNo})`" :value="emp.id" />
          </ElSelect>
        </ElFormItem>
        <ElFormItem label="联系电话">
          <ElInput v-model="editingData.phone" placeholder="请输入联系电话" :maxlength="20" />
        </ElFormItem>
        <ElFormItem label="邮箱">
          <ElInput v-model="editingData.email" placeholder="请输入邮箱" :maxlength="100" />
        </ElFormItem>
        <ElFormItem v-if="editingData.unitType !== OrgUnitType.DEPT" label="地址">
          <ElInput v-model="editingData.address" placeholder="请输入地址" :maxlength="255" />
        </ElFormItem>
        <ElFormItem label="描述">
          <ElInput v-model="editingData.description" type="textarea" :rows="3" placeholder="请输入描述（可选）" :maxlength="500" show-word-limit />
        </ElFormItem>
        <ElFormItem label="排序">
          <ElInputNumber v-model="editingData.sortOrder" :min="0" :max="999" class="w-full" />
        </ElFormItem>
      </ElForm>
      <template #footer>
        <ElButton @click="dialogVisible = false">取消</ElButton>
        <ElButton type="primary" :loading="submitLoading" @click="handleSubmit">
          {{ operateType === 'add' ? '确定新增' : '确定保存' }}
        </ElButton>
      </template>
    </ElDialog>
  </ElRow>
</template>

<style scoped>
.h-full {
  height: calc(100vh - 100px);
}

.h-320px {
  height: 320px;
}

.stats-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
  flex-wrap: nowrap;
}

.stats-title {
  display: flex;
  align-items: center;
  gap: 8px;
  flex: 1;
  min-width: 0;
  overflow: hidden;
}

.stats-title > span:first-child {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.stats-dimension {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-shrink: 0;
  margin-left: 16px;
}

:deep(.el-card__header) {
  padding: 12px 20px;
}

.tree-node {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
  padding-right: 8px;
}

.node-actions {
  display: flex;
  gap: 2px;
  opacity: 0;
  transition: opacity 0.2s;
}

.tree-node:hover .node-actions {
  opacity: 1;
}

:deep(.el-tree-node__content) {
  height: 40px;
  border-radius: 4px;
  margin: 2px 0;
}

:deep(.el-tree-node__content:hover) {
  background-color: var(--el-fill-color-light);
}

:deep(.el-tree-node.is-current > .el-tree-node__content) {
  background-color: var(--el-color-primary-light-9);
}

.employee-badge :deep(.el-badge__content) {
  font-size: 11px;
}

:deep(.el-statistic__head) {
  font-size: 13px;
  color: var(--el-text-color-secondary);
}

:deep(.el-statistic__content) {
  font-size: 24px;
  font-weight: 700;
}
</style>
