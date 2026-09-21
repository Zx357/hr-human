<script setup lang="ts">
import { computed, nextTick, onMounted, ref } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import {
  type AttLocation,
  createAttLocation,
  deleteAttLocation,
  fetchAttLocationDetail,
  fetchAttLocationPage,
  updateAttLocation
} from '@/service/api/attendance';
import { fetchEmployeePage } from '@/service/api/hr';
import { fetchOrgTree } from '@/service/api/organization';
import AttendanceLocationPicker from '@/views/organization/org-structure/components/attendance-location-picker.vue';
import { $t } from '@/locales';

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

const dialogTitle = computed(() =>
  form.value.id ? $t('attendance.location.editClockLocation') : $t('attendance.location.newClockLocation')
);
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
        : undefined) ||
      tableData.value[0] ||
      null;

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

function handleLocationPageChange() {
  loadLocations();
}

function handleLocationPageSizeChange() {
  pageNum.value = 1;
  loadLocations();
}

function handleAdd() {
  form.value = createEmptyForm();
  dialogVisible.value = true;
}

async function handleEdit(row: AttLocation) {
  let detail: AttLocation;
  try {
    const res = await fetchAttLocationDetail(row.id as number);
    detail = res.data || row;
  } catch {
    // 获取详情失败（请求层已统一弹错），不打开编辑弹窗
    return;
  }
  form.value = {
    ...createEmptyForm(),
    ...detail,
    employeeIds: detail.employeeIds || []
  };
  dialogVisible.value = true;
}

async function handleDelete(row: AttLocation) {
  try {
    await ElMessageBox.confirm(
      $t('attendance.location.areYouSureYouWantToDeleteClockLocationAssignedEmployeesWillBeUnassigned', {
        name: row.locationName
      }),
      $t('common.tip'),
      {
        type: 'warning'
      }
    );
  } catch {
    // 用户取消删除
    return;
  }
  try {
    await deleteAttLocation(row.id as number);
    ElMessage.success($t('common.deleteSuccess'));
    if (selectedLocation.value?.id === row.id) {
      selectedLocation.value = null;
      resetEmployeeSelection();
    }
    await loadLocations(false);
  } catch {
    // 请求层已统一弹错
  }
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
    ElMessage.warning($t('attendance.location.pleaseEnterALocationName'));
    return;
  }
  if (!form.value.address?.trim()) {
    ElMessage.warning($t('attendance.location.pleaseSelectOrEnterAClockAddress'));
    return;
  }
  if (form.value.latitude === undefined || form.value.longitude === undefined) {
    ElMessage.warning($t('attendance.location.pleaseSelectAPointOnTheMap'));
    return;
  }
  if (!form.value.clockRange || form.value.clockRange <= 0) {
    ElMessage.warning($t('attendance.location.pleaseEnterAValidClockRadius'));
    return;
  }

  submitLoading.value = true;
  try {
    if (form.value.id) {
      await updateAttLocation(form.value.id, form.value);
    } else {
      await createAttLocation(form.value);
    }
    ElMessage.success($t('common.saveSuccess'));
    dialogVisible.value = false;
    await loadLocations();
  } finally {
    submitLoading.value = false;
  }
}

async function handleOpenAssignDialog() {
  if (!selectedLocation.value?.id) {
    ElMessage.warning($t('attendance.location.pleaseSelectAClockLocationOnTheLeft'));
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
    ElMessage.success($t('attendance.location.employeeAssignmentsSaved'));
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
  try {
    await ElMessageBox.confirm(
      $t('attendance.location.areYouSureYouWantToRemoveEmployeesFromTheCurrentClockLocation', {
        count: employees.length,
        names: names ? `（${names}）` : ''
      }),
      $t('common.tip'),
      {
        type: 'warning'
      }
    );
  } catch {
    // 用户取消移除
    return;
  }

  const removeIds = new Set(employees.map(item => item.id));
  const employeeIds = currentEmployeeIds.value.filter(id => !removeIds.has(id));
  try {
    await updateAttLocation(selectedLocation.value.id, {
      ...selectedLocation.value,
      employeeIds
    });
    ElMessage.success($t('attendance.location.assignmentsRemoved'));
    await refreshSelectedLocation();
    await loadLocations();
  } catch {
    // 请求层已统一弹错
  }
}

function handleRemoveEmployee(employee: Api.Hr.Employee) {
  removeEmployees([employee]);
}

function handleBatchRemoveEmployees() {
  if (selectedEmployeeRows.value.length === 0) {
    ElMessage.warning($t('attendance.location.pleaseSelectEmployeesToRemove'));
    return;
  }
  removeEmployees(selectedEmployeeRows.value);
}
</script>

<template>
  <div class="location-page">
    <div class="page-header">
      <div>
        <div class="page-title">{{ $t('attendance.location.clockLocationSettings') }}</div>
        <div class="page-desc">
          {{
            $t('attendance.location.manageClockLocationsOnTheLeftAndAssignedEmployeesOfTheCurrentLocationOnTheRight')
          }}
        </div>
      </div>
      <ElButton v-permission="'attendance:location:add'" type="primary" @click="handleAdd">
        <template #icon>
          <icon-ep-plus />
        </template>
        {{ $t('attendance.location.newLocation') }}
      </ElButton>
    </div>

    <div class="split-layout">
      <ElCard shadow="never" class="location-panel">
        <template #header>
          <div class="panel-title">{{ $t('common.clockLocation') }}</div>
        </template>

        <div class="toolbar">
          <ElInput
            v-model="keyword"
            clearable
            :placeholder="$t('attendance.location.searchLocationNameOrAddress')"
            @keyup.enter="handleSearch"
          >
            <template #prefix>
              <icon-ep-search />
            </template>
          </ElInput>
          <ElButton type="primary" plain @click="handleSearch">{{ $t('common.search') }}</ElButton>
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
          <ElTableColumn
            prop="locationName"
            :label="$t('attendance.location.locationName')"
            width="110"
            show-overflow-tooltip
          />
          <ElTableColumn prop="address" :label="$t('common.clockAddress')" min-width="180" show-overflow-tooltip />
          <ElTableColumn prop="clockRange" :label="$t('attendance.location.radius')" width="72" align="center">
            <template #default="{ row }">{{ row.clockRange }}{{ $t('common.meter') }}</template>
          </ElTableColumn>
          <ElTableColumn prop="assignedCount" :label="$t('common.employee')" width="68" align="center">
            <template #default="{ row }">
              <ElTag type="success">{{ row.assignedCount || 0 }} {{ $t('org.structure.people') }}</ElTag>
            </template>
          </ElTableColumn>
          <ElTableColumn prop="status" :label="$t('common.status')" width="72" align="center">
            <template #default="{ row }">
              <ElTag :type="row.status === 1 ? 'success' : 'info'">
                {{ row.status === 1 ? $t('common.enable') : $t('common.deactivate') }}
              </ElTag>
            </template>
          </ElTableColumn>
          <ElTableColumn :label="$t('common.action')" width="116" align="center" fixed="right">
            <template #default="{ row }">
              <ElButton
                v-permission="'attendance:location:edit'"
                type="primary"
                link
                size="small"
                @click.stop="handleEdit(row)"
              >
                {{ $t('common.edit') }}
              </ElButton>
              <ElButton
                v-permission="'attendance:location:delete'"
                type="danger"
                link
                size="small"
                @click.stop="handleDelete(row)"
              >
                {{ $t('common.delete') }}
              </ElButton>
            </template>
          </ElTableColumn>
        </ElTable>

        <div class="pagination">
          <ElPagination
            v-model:current-page="pageNum"
            v-model:page-size="pageSize"
            :total="total"
            :page-sizes="[10, 20, 50, 100]"
            layout="total, sizes, prev, pager, next"
            @size-change="handleLocationPageSizeChange"
            @current-change="handleLocationPageChange"
          />
        </div>
      </ElCard>

      <ElCard shadow="never" class="employee-panel">
        <template #header>
          <div class="employee-header">
            <div>
              <div class="panel-title">{{ $t('common.assignEmployees') }}</div>
              <div class="panel-desc">
                {{
                  selectedLocation
                    ? $t('org.mapPicker.currentLocation', { name: selectedLocation.locationName })
                    : $t('attendance.location.pleaseSelectAClockLocationOnTheLeft')
                }}
              </div>
            </div>
            <div class="employee-actions">
              <ElButton
                type="danger"
                plain
                :disabled="selectedEmployeeRows.length === 0"
                @click="handleBatchRemoveEmployees"
              >
                {{ $t('attendance.location.batchRemove') }}
              </ElButton>
              <ElButton type="primary" plain :disabled="!selectedLocation" @click="handleOpenAssignDialog">
                {{ $t('common.assignEmployees') }}
              </ElButton>
            </div>
          </div>
        </template>

        <div v-if="selectedLocation" class="location-summary">
          <ElDescriptions :column="1" border size="small">
            <ElDescriptionsItem :label="$t('common.clockAddress')">
              {{ selectedLocation.address || '--' }}
            </ElDescriptionsItem>
            <ElDescriptionsItem :label="$t('common.coordinates')">
              {{ selectedLocation.latitude?.toFixed?.(6) || '--' }},
              {{ selectedLocation.longitude?.toFixed?.(6) || '--' }}
            </ElDescriptionsItem>
            <ElDescriptionsItem :label="$t('attendance.location.clockRadius')">
              {{ selectedLocation.clockRange || 0 }} {{ $t('common.meter') }}
            </ElDescriptionsItem>
          </ElDescriptions>
        </div>

        <ElTable
          ref="employeeTableRef"
          v-loading="detailLoading"
          :data="pagedAssignedEmployees"
          border
          stripe
          :empty-text="$t('attendance.location.noAssignedEmployees')"
          @selection-change="handleEmployeeSelectionChange"
        >
          <ElTableColumn type="selection" width="44" align="center" />
          <ElTableColumn prop="employeeNo" :label="$t('common.employeeNo')" width="86" />
          <ElTableColumn prop="name" :label="$t('common.name')" width="88" show-overflow-tooltip />
          <ElTableColumn prop="deptName" :label="$t('common.department')" min-width="100" show-overflow-tooltip />
          <ElTableColumn prop="phone" :label="$t('common.phone')" min-width="106" show-overflow-tooltip />
          <ElTableColumn prop="status" :label="$t('common.status')" width="66" align="center">
            <template #default="{ row }">
              <ElTag :type="row.status === 1 ? 'success' : 'info'">
                {{ row.status === 1 ? $t('common.active') : $t('common.resigned') }}
              </ElTag>
            </template>
          </ElTableColumn>
          <ElTableColumn :label="$t('common.action')" width="74" align="center" fixed="right">
            <template #default="{ row }">
              <ElButton type="danger" link size="small" @click="handleRemoveEmployee(row)">
                {{ $t('common.remove') }}
              </ElButton>
            </template>
          </ElTableColumn>
        </ElTable>

        <div class="pagination">
          <ElPagination
            v-model:current-page="employeePageNum"
            v-model:page-size="employeePageSize"
            :total="assignedEmployees.length"
            :page-sizes="[10, 20, 50, 100]"
            layout="total, sizes, prev, pager, next"
            @size-change="resetEmployeeSelection"
            @current-change="resetEmployeeSelection"
          />
        </div>
      </ElCard>
    </div>

    <ElDialog v-model="dialogVisible" :title="dialogTitle" width="760px" destroy-on-close>
      <ElForm :model="form" label-width="100px">
        <ElFormItem :label="$t('attendance.location.locationName')" required>
          <ElInput
            v-model="form.locationName"
            maxlength="100"
            show-word-limit
            :placeholder="$t('attendance.location.eGHuashuiIndustrialParkBuilding1Floor5')"
          />
        </ElFormItem>

        <ElFormItem :label="$t('attendance.location.mapPicker')" required>
          <div class="pick-row">
            <ElButton type="primary" plain @click="handlePickLocation">
              {{ $t('attendance.location.pickLocationOnMap') }}
            </ElButton>
            <span class="pick-tip">
              {{ $t('attendance.location.theAddressAndGcj02CoordinatesWillBeFilledAutomaticallyAfterSelection') }}
            </span>
          </div>
        </ElFormItem>

        <ElFormItem :label="$t('common.clockAddress')" required>
          <ElInput
            v-model="form.address"
            maxlength="255"
            show-word-limit
            :placeholder="$t('attendance.location.shownOnMobileBuildingAndFloorCanBeAddedManually')"
          />
        </ElFormItem>

        <ElRow :gutter="12">
          <ElCol :span="12">
            <ElFormItem :label="$t('common.latitude')" required>
              <ElInputNumber
                v-model="form.latitude"
                class="w-full"
                :precision="6"
                :step="0.000001"
                controls-position="right"
              />
            </ElFormItem>
          </ElCol>
          <ElCol :span="12">
            <ElFormItem :label="$t('common.longitude')" required>
              <ElInputNumber
                v-model="form.longitude"
                class="w-full"
                :precision="6"
                :step="0.000001"
                controls-position="right"
              />
            </ElFormItem>
          </ElCol>
        </ElRow>

        <ElRow :gutter="12">
          <ElCol :span="12">
            <ElFormItem :label="$t('attendance.location.clockRadius')" required>
              <ElInputNumber
                v-model="form.clockRange"
                class="w-full"
                :min="1"
                :max="5000"
                :step="10"
                controls-position="right"
              />
            </ElFormItem>
          </ElCol>
          <ElCol :span="12">
            <ElFormItem :label="$t('common.status')">
              <ElRadioGroup v-model="form.status">
                <ElRadioButton :value="1">{{ $t('common.enable') }}</ElRadioButton>
                <ElRadioButton :value="0">{{ $t('common.deactivate') }}</ElRadioButton>
              </ElRadioGroup>
            </ElFormItem>
          </ElCol>
        </ElRow>

        <ElFormItem :label="$t('common.remark')">
          <ElInput v-model="form.remark" type="textarea" :rows="3" maxlength="500" show-word-limit />
        </ElFormItem>
      </ElForm>

      <template #footer>
        <ElButton @click="dialogVisible = false">{{ $t('common.cancel') }}</ElButton>
        <ElButton type="primary" :loading="submitLoading" @click="handleSubmit">{{ $t('common.save') }}</ElButton>
      </template>
    </ElDialog>

    <ElDialog
      v-model="assignDialogVisible"
      :title="$t('common.selectEmployees')"
      width="900px"
      destroy-on-close
      append-to-body
    >
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
        <span v-else class="selected-placeholder">{{ $t('common.pleaseSelectEmployees') }}</span>
      </div>

      <div class="mb-16px">
        <ElForm inline :model="assignEmployeeSearch">
          <ElFormItem :label="$t('common.name')">
            <ElInput
              v-model="assignEmployeeSearch.name"
              :placeholder="$t('common.pleaseInputName')"
              clearable
              style="width: 120px"
            />
          </ElFormItem>
          <ElFormItem :label="$t('common.employeeNo')">
            <ElInput
              v-model="assignEmployeeSearch.employeeNo"
              :placeholder="$t('common.pleaseInputEmployeeNo')"
              clearable
              style="width: 120px"
            />
          </ElFormItem>
          <ElFormItem :label="$t('common.organization')">
            <ElTreeSelect
              v-model="assignEmployeeSearch.orgIds"
              :data="orgTreeOptions"
              :props="{ children: 'children', label: 'unitName', value: 'id' }"
              node-key="id"
              :placeholder="$t('common.pleaseSelectOrganization')"
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
                  <ElCheckbox v-model="cascadeSelect" size="small">{{ $t('common.cascadeSelect') }}</ElCheckbox>
                </div>
              </template>
            </ElTreeSelect>
          </ElFormItem>
          <ElFormItem>
            <ElButton type="primary" @click="handleAssignSearch">{{ $t('common.search') }}</ElButton>
            <ElButton @click="handleAssignReset">{{ $t('common.reset') }}</ElButton>
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
        <ElTableColumn prop="employeeNo" :label="$t('common.employeeNo')" width="110" />
        <ElTableColumn prop="name" :label="$t('common.name')" width="100" />
        <ElTableColumn prop="companyName" :label="$t('common.company')" min-width="140" show-overflow-tooltip />
        <ElTableColumn prop="deptName" :label="$t('common.department')" min-width="120" show-overflow-tooltip />
        <ElTableColumn prop="phone" :label="$t('common.phone')" min-width="130" show-overflow-tooltip />
      </ElTable>

      <div class="employee-dialog-footer">
        <span class="selected-count">
          {{ $t('attendance.location.selected') }} {{ tempSelectedEmployees.length }} {{ $t('org.structure.people') }}
        </span>
        <ElPagination
          v-model:current-page="assignEmployeePage"
          v-model:page-size="assignEmployeePageSize"
          :total="assignEmployeeTotal"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next"
          @current-change="handleAssignPageChange"
          @size-change="handleAssignSizeChange"
        />
      </div>

      <ElAlert
        class="assign-tip"
        :title="
          $t(
            'attendance.location.afterSavingSelectedEmployeesWillBeBoundToTheCurrentClockLocationAnEmployeeCanBindToMultipleLocations'
          )
        "
        type="info"
        :closable="false"
        show-icon
      />

      <template #footer>
        <ElButton @click="assignDialogVisible = false">{{ $t('common.cancel') }}</ElButton>
        <ElButton type="primary" :loading="assignSubmitLoading" @click="handleSaveAssignments">
          {{ $t('attendance.location.saveAssignments') }}
        </ElButton>
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
