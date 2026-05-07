<script setup lang="ts">
import { computed, nextTick, onMounted, ref } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import {
  createAttLocation,
  deleteAttLocation,
  fetchAttLocationDetail,
  fetchAttLocationPage,
  updateAttLocation,
  type AttLocation
} from '@/service/api/attendance';
import { fetchEmployeePage } from '@/service/api/hr';
import { fetchOrgTree } from '@/service/api/organization';
import AttendanceLocationPicker from '@/views/organization/org-structure/components/attendance-location-picker.vue';

defineOptions({ name: 'AttendanceLocation' });

type LocationForm = AttLocation & {
  employeeIds: number[];
};

const loading = ref(false);
const submitLoading = ref(false);
const assignSubmitLoading = ref(false);
const detailLoading = ref(false);
const dialogVisible = ref(false);
const assignDialogVisible = ref(false);
const pickerVisible = ref(false);
const keyword = ref('');
const pageNum = ref(1);
const pageSize = ref(10);
const total = ref(0);
const tableData = ref<AttLocation[]>([]);
const tableRef = ref<any>(null);
const employeeTableRef = ref<any>(null);
const assignEmployeeTableRef = ref<any>(null);
const selectedLocation = ref<AttLocation | null>(null);
const selectedEmployeeRows = ref<Api.Hr.Employee[]>([]);
const orgTreeOptions = ref<Api.Organization.OrgUnit[]>([]);
const cascadeSelect = ref(false);
const assignEmployeeLoading = ref(false);
const assignEmployeeData = ref<Api.Hr.Employee[]>([]);
const assignEmployeeTotal = ref(0);
const assignEmployeePage = ref(1);
const assignEmployeePageSize = ref(10);
const assignEmployeeSearch = ref({ name: '', employeeNo: '', orgIds: [] as number[] });
const tempSelectedEmployees = ref<Api.Hr.Employee[]>([]);
const employeePageNum = ref(1);
const employeePageSize = ref(10);
const form = ref<LocationForm>(createEmptyForm());

const dialogTitle = computed(() => (form.value.id ? '编辑打卡地点' : '新增打卡地点'));
const assignedEmployees = computed(() => selectedLocation.value?.employees || []);
const currentEmployeeIds = computed(() => selectedLocation.value?.employeeIds || []);
const pagedAssignedEmployees = computed(() => {
  const start = (employeePageNum.value - 1) * employeePageSize.value;
  return assignedEmployees.value.slice(start, start + employeePageSize.value);
});

function createEmptyForm(): LocationForm {
  return {
    locationName: '',
    address: '',
    latitude: undefined,
    longitude: undefined,
    clockRange: 300,
    status: 1,
    remark: '',
    employeeIds: []
  };
}

function getPageRecords(data: any) {
  return data?.records || data?.list || [];
}

function getPageTotal(data: any) {
  return Number(data?.total || 0);
}

function resetEmployeeSelection() {
  selectedEmployeeRows.value = [];
  employeeTableRef.value?.clearSelection?.();
}

function normalizeEmployeePage() {
  const maxPage = Math.max(1, Math.ceil(assignedEmployees.value.length / employeePageSize.value));
  if (employeePageNum.value > maxPage) {
    employeePageNum.value = maxPage;
  }
}

async function loadLocations(keepSelected = true) {
  loading.value = true;
  try {
    const res = await fetchAttLocationPage({
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      keyword: keyword.value
    });
    tableData.value = getPageRecords(res.data);
    total.value = getPageTotal(res.data);

    const nextSelected =
      (keepSelected && selectedLocation.value
        ? tableData.value.find(item => item.id === selectedLocation.value?.id)
        : undefined) || tableData.value[0] || null;

    if (nextSelected) {
      await selectLocation(nextSelected);
    } else {
      selectedLocation.value = null;
      resetEmployeeSelection();
    }
  } finally {
    loading.value = false;
  }
}

async function refreshSelectedLocation() {
  if (!selectedLocation.value?.id) {
    return;
  }

  detailLoading.value = true;
  try {
    const res = await fetchAttLocationDetail(selectedLocation.value.id);
    selectedLocation.value = res.data || selectedLocation.value;
    const index = tableData.value.findIndex(item => item.id === selectedLocation.value?.id);
    if (index >= 0 && selectedLocation.value) {
      tableData.value[index] = selectedLocation.value;
    }
    normalizeEmployeePage();
    resetEmployeeSelection();
  } finally {
    detailLoading.value = false;
  }
}

async function selectLocation(row: AttLocation) {
  if (!row?.id) {
    selectedLocation.value = row;
    employeePageNum.value = 1;
    resetEmployeeSelection();
    return;
  }

  detailLoading.value = true;
  try {
    const res = await fetchAttLocationDetail(row.id);
    selectedLocation.value = res.data || row;
    employeePageNum.value = 1;
    resetEmployeeSelection();
    await nextTick();
    tableRef.value?.setCurrentRow?.(row);
  } finally {
    detailLoading.value = false;
  }
}

async function loadOrgTree() {
  const res = await fetchOrgTree();
  orgTreeOptions.value = res.data || [];
}

async function loadAssignEmployeeData() {
  assignEmployeeLoading.value = true;
  try {
    const res = await fetchEmployeePage({
      pageNum: assignEmployeePage.value,
      pageSize: assignEmployeePageSize.value,
      name: assignEmployeeSearch.value.name || undefined,
      employeeNo: assignEmployeeSearch.value.employeeNo || undefined,
      orgIds: assignEmployeeSearch.value.orgIds.length > 0 ? assignEmployeeSearch.value.orgIds.join(',') : undefined,
      status: 1
    });
    assignEmployeeData.value = getPageRecords(res.data);
    assignEmployeeTotal.value = getPageTotal(res.data);
    await nextTick();
    restoreAssignSelection();
  } finally {
    assignEmployeeLoading.value = false;
  }
}

onMounted(() => {
  loadLocations(false);
  loadOrgTree();
});

function handleSearch() {
  pageNum.value = 1;
  loadLocations(false);
}

function handleAdd() {
  form.value = createEmptyForm();
  dialogVisible.value = true;
}

async function handleEdit(row: AttLocation) {
  const res = await fetchAttLocationDetail(row.id as number);
  const detail = res.data || row;
  form.value = {
    ...createEmptyForm(),
    ...detail,
    employeeIds: detail.employeeIds || []
  };
  dialogVisible.value = true;
}

async function handleDelete(row: AttLocation) {
  await ElMessageBox.confirm(`确定删除打卡地点“${row.locationName}”吗？关联员工也会同步解除。`, '提示', {
    type: 'warning'
  });
  await deleteAttLocation(row.id as number);
  ElMessage.success('删除成功');
  if (selectedLocation.value?.id === row.id) {
    selectedLocation.value = null;
    resetEmployeeSelection();
  }
  await loadLocations(false);
}

function handlePickLocation() {
  pickerVisible.value = true;
}

function handleLocationConfirm(location: { address: string; latitude: number; longitude: number }) {
  form.value.address = location.address;
  form.value.latitude = location.latitude;
  form.value.longitude = location.longitude;
  if (!form.value.locationName) {
    form.value.locationName = location.address;
  }
  if (!form.value.clockRange) {
    form.value.clockRange = 300;
  }
}

async function handleSubmit() {
  if (!form.value.locationName?.trim()) {
    ElMessage.warning('请输入地点名称');
    return;
  }
  if (!form.value.address?.trim()) {
    ElMessage.warning('请先选择或填写打卡地址');
    return;
  }
  if (form.value.latitude === undefined || form.value.longitude === undefined) {
    ElMessage.warning('请先选择地图点位');
    return;
  }
  if (!form.value.clockRange || form.value.clockRange <= 0) {
    ElMessage.warning('请输入有效打卡半径');
    return;
  }

  submitLoading.value = true;
  try {
    if (form.value.id) {
      await updateAttLocation(form.value.id, form.value);
    } else {
      await createAttLocation(form.value);
    }
    ElMessage.success('保存成功');
    dialogVisible.value = false;
    await loadLocations();
  } finally {
    submitLoading.value = false;
  }
}

async function handleOpenAssignDialog() {
  if (!selectedLocation.value?.id) {
    ElMessage.warning('请先选择左侧打卡地点');
    return;
  }

  assignEmployeePage.value = 1;
  assignEmployeeSearch.value = { name: '', employeeNo: '', orgIds: [] };
  tempSelectedEmployees.value = [...assignedEmployees.value];
  assignDialogVisible.value = true;
  await loadAssignEmployeeData();
}

async function handleSaveAssignments() {
  if (!selectedLocation.value?.id) {
    return;
  }

  assignSubmitLoading.value = true;
  try {
    await updateAttLocation(selectedLocation.value.id, {
      ...selectedLocation.value,
      employeeIds: tempSelectedEmployees.value.map(item => item.id!).filter(Boolean)
    });
    ElMessage.success('员工关联已保存');
    assignDialogVisible.value = false;
    await refreshSelectedLocation();
    await loadLocations();
  } finally {
    assignSubmitLoading.value = false;
  }
}

function handleEmployeeSelectionChange(rows: Api.Hr.Employee[]) {
  selectedEmployeeRows.value = rows;
}

function restoreAssignSelection() {
  const selectedIds = new Set(tempSelectedEmployees.value.map(item => item.id));
  assignEmployeeTableRef.value?.clearSelection?.();
  assignEmployeeData.value.forEach(row => {
    if (selectedIds.has(row.id)) {
      assignEmployeeTableRef.value?.toggleRowSelection?.(row, true);
    }
  });
}

function handleAssignSelectionChange(rows: Api.Hr.Employee[]) {
  const currentPageIds = new Set(assignEmployeeData.value.map(item => item.id));
  const otherPageSelected = tempSelectedEmployees.value.filter(item => !currentPageIds.has(item.id));
  const currentSelected = rows.map(row => ({
    ...row,
    id: row.id!,
    name: row.name!,
    employeeNo: row.employeeNo!
  }));
  tempSelectedEmployees.value = [...otherPageSelected, ...currentSelected];
}

function handleAssignSearch() {
  assignEmployeePage.value = 1;
  loadAssignEmployeeData();
}

function handleAssignReset() {
  assignEmployeeSearch.value = { name: '', employeeNo: '', orgIds: [] };
  assignEmployeePage.value = 1;
  loadAssignEmployeeData();
}

function handleAssignPageChange(page: number) {
  assignEmployeePage.value = page;
  loadAssignEmployeeData();
}

function handleAssignSizeChange(size: number) {
  assignEmployeePageSize.value = size;
  assignEmployeePage.value = 1;
  loadAssignEmployeeData();
}

function removeTempSelectedEmployee(index: number) {
  tempSelectedEmployees.value.splice(index, 1);
  restoreAssignSelection();
}

async function removeEmployees(employees: Api.Hr.Employee[]) {
  if (!selectedLocation.value?.id || employees.length === 0) {
    return;
  }

  const names = employees.map(item => item.name || item.employeeNo).join('、');
  await ElMessageBox.confirm(`确定从当前打卡地点移除 ${employees.length} 名员工吗？${names ? `（${names}）` : ''}`, '提示', {
    type: 'warning'
  });

  const removeIds = new Set(employees.map(item => item.id));
  const employeeIds = currentEmployeeIds.value.filter(id => !removeIds.has(id));
  await updateAttLocation(selectedLocation.value.id, {
    ...selectedLocation.value,
    employeeIds
  });
  ElMessage.success('已移除关联');
  await refreshSelectedLocation();
  await loadLocations();
}

function handleRemoveEmployee(employee: Api.Hr.Employee) {
  removeEmployees([employee]);
}

function handleBatchRemoveEmployees() {
  if (selectedEmployeeRows.value.length === 0) {
    ElMessage.warning('请先勾选要移除的员工');
    return;
  }
  removeEmployees(selectedEmployeeRows.value);
}
</script>

<template>
  <div class="location-page">
    <div class="page-header">
      <div>
        <div class="page-title">打卡地点设置</div>
        <div class="page-desc">左侧维护打卡地点，右侧维护当前地点关联的员工。</div>
      </div>
      <ElButton type="primary" @click="handleAdd">
        <template #icon>
          <icon-ep-plus />
        </template>
        新增地点
      </ElButton>
    </div>

    <div class="split-layout">
      <ElCard shadow="never" class="location-panel">
        <template #header>
          <div class="panel-title">打卡地点</div>
        </template>

        <div class="toolbar">
          <ElInput v-model="keyword" clearable placeholder="搜索地点名称或地址" @keyup.enter="handleSearch">
            <template #prefix>
              <icon-ep-search />
            </template>
          </ElInput>
          <ElButton type="primary" plain @click="handleSearch">查询</ElButton>
        </div>

        <ElTable
          ref="tableRef"
          v-loading="loading"
          :data="tableData"
          border
          stripe
          highlight-current-row
          row-key="id"
          @row-click="selectLocation"
        >
          <ElTableColumn prop="locationName" label="地点名称" min-width="120" />
          <ElTableColumn prop="address" label="打卡地址" min-width="210" show-overflow-tooltip />
          <ElTableColumn prop="clockRange" label="半径" width="82" align="center">
            <template #default="{ row }">{{ row.clockRange }}米</template>
          </ElTableColumn>
          <ElTableColumn prop="assignedCount" label="员工" width="76" align="center">
            <template #default="{ row }">
              <ElTag type="success">{{ row.assignedCount || 0 }} 人</ElTag>
            </template>
          </ElTableColumn>
          <ElTableColumn prop="status" label="状态" width="78" align="center">
            <template #default="{ row }">
              <ElTag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '启用' : '停用' }}</ElTag>
            </template>
          </ElTableColumn>
          <ElTableColumn label="操作" width="112" align="center">
            <template #default="{ row }">
              <ElButton type="primary" link size="small" @click.stop="handleEdit(row)">编辑</ElButton>
              <ElButton type="danger" link size="small" @click.stop="handleDelete(row)">删除</ElButton>
            </template>
          </ElTableColumn>
        </ElTable>

        <div class="pagination">
          <ElPagination
            v-model:current-page="pageNum"
            v-model:page-size="pageSize"
            :total="total"
            :page-sizes="[10, 20, 50]"
            layout="total, sizes, prev, pager, next"
            @size-change="loadLocations"
            @current-change="loadLocations"
          />
        </div>
      </ElCard>

      <ElCard shadow="never" class="employee-panel">
        <template #header>
          <div class="employee-header">
            <div>
              <div class="panel-title">关联员工</div>
              <div class="panel-desc">
                {{ selectedLocation ? `当前地点：${selectedLocation.locationName}` : '请先选择左侧打卡地点' }}
              </div>
            </div>
            <div class="employee-actions">
              <ElButton
                type="danger"
                plain
                :disabled="selectedEmployeeRows.length === 0"
                @click="handleBatchRemoveEmployees"
              >
                批量移除
              </ElButton>
              <ElButton type="primary" plain :disabled="!selectedLocation" @click="handleOpenAssignDialog">
                关联员工
              </ElButton>
            </div>
          </div>
        </template>

        <div v-if="selectedLocation" class="location-summary">
          <ElDescriptions :column="1" border size="small">
            <ElDescriptionsItem label="打卡地址">{{ selectedLocation.address || '--' }}</ElDescriptionsItem>
            <ElDescriptionsItem label="坐标">
              {{ selectedLocation.latitude?.toFixed?.(6) || '--' }},
              {{ selectedLocation.longitude?.toFixed?.(6) || '--' }}
            </ElDescriptionsItem>
            <ElDescriptionsItem label="打卡半径">{{ selectedLocation.clockRange || 0 }} 米</ElDescriptionsItem>
          </ElDescriptions>
        </div>

        <ElTable
          ref="employeeTableRef"
          v-loading="detailLoading"
          :data="pagedAssignedEmployees"
          border
          stripe
          empty-text="暂无关联员工"
          @selection-change="handleEmployeeSelectionChange"
        >
          <ElTableColumn type="selection" width="46" align="center" />
          <ElTableColumn prop="employeeNo" label="工号" width="96" />
          <ElTableColumn prop="name" label="姓名" width="100" />
          <ElTableColumn prop="deptName" label="部门" min-width="120" show-overflow-tooltip />
          <ElTableColumn prop="phone" label="手机号" min-width="124" show-overflow-tooltip />
          <ElTableColumn prop="status" label="状态" width="76" align="center">
            <template #default="{ row }">
              <ElTag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '在职' : '离职' }}</ElTag>
            </template>
          </ElTableColumn>
          <ElTableColumn label="操作" width="76" align="center">
            <template #default="{ row }">
              <ElButton type="danger" link size="small" @click="handleRemoveEmployee(row)">移除</ElButton>
            </template>
          </ElTableColumn>
        </ElTable>

        <div class="pagination">
          <ElPagination
            v-model:current-page="employeePageNum"
            v-model:page-size="employeePageSize"
            :total="assignedEmployees.length"
            :page-sizes="[10, 20, 50]"
            layout="total, sizes, prev, pager, next"
            @size-change="resetEmployeeSelection"
            @current-change="resetEmployeeSelection"
          />
        </div>
      </ElCard>
    </div>

    <ElDialog v-model="dialogVisible" :title="dialogTitle" width="760px" destroy-on-close>
      <ElForm :model="form" label-width="100px">
        <ElFormItem label="地点名称" required>
          <ElInput v-model="form.locationName" maxlength="100" show-word-limit placeholder="例如：华水工业园 1栋5楼" />
        </ElFormItem>

        <ElFormItem label="地图选点" required>
          <div class="pick-row">
            <ElButton type="primary" plain @click="handlePickLocation">地图选择位置</ElButton>
            <span class="pick-tip">选择后会自动回填地址和 GCJ-02 坐标</span>
          </div>
        </ElFormItem>

        <ElFormItem label="打卡地址" required>
          <ElInput v-model="form.address" maxlength="255" show-word-limit placeholder="用于移动端展示，可手动补充楼栋楼层" />
        </ElFormItem>

        <ElRow :gutter="12">
          <ElCol :span="12">
            <ElFormItem label="纬度" required>
              <ElInputNumber v-model="form.latitude" class="w-full" :precision="6" :step="0.000001" controls-position="right" />
            </ElFormItem>
          </ElCol>
          <ElCol :span="12">
            <ElFormItem label="经度" required>
              <ElInputNumber v-model="form.longitude" class="w-full" :precision="6" :step="0.000001" controls-position="right" />
            </ElFormItem>
          </ElCol>
        </ElRow>

        <ElRow :gutter="12">
          <ElCol :span="12">
            <ElFormItem label="打卡半径" required>
              <ElInputNumber v-model="form.clockRange" class="w-full" :min="1" :max="5000" :step="10" controls-position="right" />
            </ElFormItem>
          </ElCol>
          <ElCol :span="12">
            <ElFormItem label="状态">
              <ElRadioGroup v-model="form.status">
                <ElRadioButton :value="1">启用</ElRadioButton>
                <ElRadioButton :value="0">停用</ElRadioButton>
              </ElRadioGroup>
            </ElFormItem>
          </ElCol>
        </ElRow>

        <ElFormItem label="备注">
          <ElInput v-model="form.remark" type="textarea" :rows="3" maxlength="500" show-word-limit />
        </ElFormItem>
      </ElForm>

      <template #footer>
        <ElButton @click="dialogVisible = false">取消</ElButton>
        <ElButton type="primary" :loading="submitLoading" @click="handleSubmit">保存</ElButton>
      </template>
    </ElDialog>

    <ElDialog v-model="assignDialogVisible" title="选择员工" width="900px" destroy-on-close append-to-body>
      <div class="selected-employee-box">
        <template v-if="tempSelectedEmployees.length > 0">
          <ElTag
            v-for="(employee, index) in tempSelectedEmployees"
            :key="employee.id"
            closable
            size="small"
            @close.stop="removeTempSelectedEmployee(index)"
          >
            {{ employee.name }} ({{ employee.employeeNo }})
          </ElTag>
        </template>
        <span v-else class="selected-placeholder">请选择员工</span>
      </div>

      <div class="mb-16px">
        <ElForm inline :model="assignEmployeeSearch">
          <ElFormItem label="姓名">
            <ElInput v-model="assignEmployeeSearch.name" placeholder="请输入姓名" clearable style="width: 120px" />
          </ElFormItem>
          <ElFormItem label="工号">
            <ElInput v-model="assignEmployeeSearch.employeeNo" placeholder="请输入工号" clearable style="width: 120px" />
          </ElFormItem>
          <ElFormItem label="组织">
            <ElTreeSelect
              v-model="assignEmployeeSearch.orgIds"
              :data="orgTreeOptions"
              :props="{ children: 'children', label: 'unitName', value: 'id' }"
              node-key="id"
              placeholder="请选择组织"
              clearable
              multiple
              :check-strictly="!cascadeSelect"
              show-checkbox
              collapse-tags
              :max-collapse-tags="1"
              style="width: 220px"
              :render-after-expand="false"
              filterable
            >
              <template #header>
                <div class="tree-select-header">
                  <ElCheckbox v-model="cascadeSelect" size="small">联动选择</ElCheckbox>
                </div>
              </template>
            </ElTreeSelect>
          </ElFormItem>
          <ElFormItem>
            <ElButton type="primary" @click="handleAssignSearch">搜索</ElButton>
            <ElButton @click="handleAssignReset">重置</ElButton>
          </ElFormItem>
        </ElForm>
      </div>

      <ElTable
        ref="assignEmployeeTableRef"
        v-loading="assignEmployeeLoading"
        :data="assignEmployeeData"
        border
        stripe
        max-height="400px"
        row-key="id"
        @selection-change="handleAssignSelectionChange"
      >
        <ElTableColumn type="selection" width="50" />
        <ElTableColumn prop="employeeNo" label="工号" width="110" />
        <ElTableColumn prop="name" label="姓名" width="100" />
        <ElTableColumn prop="companyName" label="公司" min-width="140" show-overflow-tooltip />
        <ElTableColumn prop="deptName" label="部门" min-width="120" show-overflow-tooltip />
        <ElTableColumn prop="phone" label="手机号" min-width="130" show-overflow-tooltip />
      </ElTable>

      <div class="employee-dialog-footer">
        <span class="selected-count">已选择 {{ tempSelectedEmployees.length }} 人</span>
        <ElPagination
          v-model:current-page="assignEmployeePage"
          v-model:page-size="assignEmployeePageSize"
          :total="assignEmployeeTotal"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          @current-change="handleAssignPageChange"
          @size-change="handleAssignSizeChange"
        />
      </div>

      <ElAlert
        class="assign-tip"
        title="保存后，所选员工会绑定到当前打卡地点；员工可以同时绑定多个打卡地点。"
        type="info"
        :closable="false"
        show-icon
      />

      <template #footer>
        <ElButton @click="assignDialogVisible = false">取消</ElButton>
        <ElButton type="primary" :loading="assignSubmitLoading" @click="handleSaveAssignments">保存关联</ElButton>
      </template>
    </ElDialog>

    <AttendanceLocationPicker
      v-model="pickerVisible"
      :latitude="form.latitude"
      :longitude="form.longitude"
      :address="form.address"
      :reference-address="form.locationName"
      @confirm="handleLocationConfirm"
    />
  </div>
</template>

<style scoped>
.location-page {
  min-height: 100%;
}

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 16px;
  padding: 18px 20px;
  background: var(--el-bg-color);
  border: 1px solid var(--el-border-color-light);
  border-radius: 8px;
}

.page-title {
  font-size: 20px;
  font-weight: 700;
  color: var(--el-text-color-primary);
}

.page-desc,
.panel-desc {
  margin-top: 4px;
  color: var(--el-text-color-secondary);
  font-size: 13px;
}

.split-layout {
  display: grid;
  grid-template-columns: minmax(600px, 1.05fr) minmax(460px, 0.95fr);
  gap: 20px;
  min-height: calc(100vh - 230px);
  isolation: isolate;
}

.location-panel,
.employee-panel {
  min-width: 0;
  overflow: hidden;
}

.location-panel {
  position: relative;
  z-index: 1;
}

.employee-panel {
  position: relative;
  z-index: 0;
}

.panel-title {
  font-size: 16px;
  font-weight: 700;
  color: var(--el-text-color-primary);
}

.employee-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.employee-actions {
  display: flex;
  flex-shrink: 0;
  gap: 8px;
}

.toolbar,
.assign-toolbar {
  display: grid;
  grid-template-columns: minmax(220px, 1fr) auto;
  gap: 12px;
  margin-bottom: 16px;
}

.pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}

.location-summary {
  margin-bottom: 16px;
}

.pick-row {
  display: flex;
  align-items: center;
  gap: 12px;
}

.pick-tip {
  color: var(--el-text-color-secondary);
  font-size: 13px;
}

.assign-tip {
  margin-top: 12px;
}

.selected-employee-box {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 6px;
  min-height: 36px;
  margin-bottom: 16px;
  padding: 6px 10px;
  border: 1px solid var(--el-border-color);
  border-radius: 4px;
  background: var(--el-fill-color-blank);
}

.selected-placeholder,
.selected-count {
  color: var(--el-text-color-secondary);
  font-size: 13px;
}

.tree-select-header {
  padding: 8px 12px;
  border-bottom: 1px solid var(--el-border-color-light);
}

.employee-dialog-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-top: 16px;
}

:deep(.el-table__fixed),
:deep(.el-table__fixed-right) {
  z-index: 2;
}

@media (max-width: 1280px) {
  .split-layout {
    grid-template-columns: 1fr;
  }
}
</style>
