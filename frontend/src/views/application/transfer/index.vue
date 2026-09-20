<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { statusMap } from '@/constants/application';
import {
  type Application,
  cancelApplication,
  createApplication,
  fetchApplicationPage
} from '@/service/api/application';
import { useDictOptions } from '@/composables/use-dict-options';
import { useOrgTree } from '@/composables/use-org-tree';
import EmployeePickerDialog from '@/components/common/EmployeePickerDialog.vue';
import ApplicationDetailDrawer from '@/components/business/application-detail-drawer.vue';

defineOptions({ name: 'TransferApplication' });

const loading = ref(false);
// 选择员工
const employeeDisplayName = ref('');
const data = ref<Application[]>([]);
const total = ref(0);
const currentPage = ref(1);
const pageSize = ref(10);

const { options: transferTypeOptions, getDictLabel: getTransferTypeLabel } = useDictOptions('transfer_type');
const { options: positionOptions, getDictLabel: getPositionLabel } = useDictOptions('position');
const { orgTreeOptions } = useOrgTree();
const orgFlatMap = ref<Map<number, Api.Organization.OrgUnit>>(new Map());
const dialogVisible = ref(false);
const submitLoading = ref(false);
const formData = ref<
  Application & { employeeName?: string; employeeNo?: string; fromCompanyName?: string; fromDeptName?: string }
>({
  employeeId: undefined as any,
  appType: 'transfer'
});

// 变更类型：1-部门调动，2-职位变更，3-部门+职位变更
const showNewDept = computed(() => formData.value.transferType === '1' || formData.value.transferType === '3');
const showNewPosition = computed(() => formData.value.transferType === '2' || formData.value.transferType === '3');

// 员工选择弹窗
const employeeDialogVisible = ref(false);

const searchParams = ref({
  employeeName: '',
  employeeNo: '',
  status: undefined as number | undefined,
  transferType: undefined as string | undefined
});

// 根据部门ID向上查找公司ID（unitType=2）
function findCompanyId(deptId: number | undefined): number | undefined {
  if (!deptId) return undefined;
  let current = orgFlatMap.value.get(deptId);
  while (current) {
    if (current.unitType === 2) return current.id;
    if (!current.parentId) break;
    current = orgFlatMap.value.get(current.parentId);
  }
  return undefined;
} // 变更类型改变时清空相关字段
function handleTransferTypeChange() {
  // 清空新部门和新职位
  formData.value.toDeptId = undefined;
  formData.value.toPosition = '';
}

async function loadData() {
  loading.value = true;
  try {
    const res = await fetchApplicationPage({
      pageNum: currentPage.value,
      pageSize: pageSize.value,
      appType: 'transfer',
      employeeName: searchParams.value.employeeName || undefined,
      employeeNo: searchParams.value.employeeNo || undefined,
      status: searchParams.value.status
    });
    data.value = res.data?.records || [];
    total.value = res.data?.total || 0;
  } finally {
    loading.value = false;
  }
}
onMounted(() => {
  loadData();
});

function handleAdd() {
  formData.value = {
    employeeId: undefined as any,
    appType: 'transfer',
    transferType: '',
    fromCompanyId: undefined,
    toCompanyId: undefined,
    fromDeptId: undefined,
    toDeptId: undefined,
    fromPosition: '',
    toPosition: '',
    effectDate: '',
    reason: '',
    employeeName: '',
    employeeNo: '',
    fromCompanyName: '',
    fromDeptName: ''
  };
  employeeDisplayName.value = '';
  dialogVisible.value = true;
}
function handleConfirmEmployee(selected: Api.Hr.Employee[]) {
  const row = selected[0];
  if (!row?.id) return;

  formData.value.employeeId = row.id!;
  formData.value.employeeName = row.name;
  formData.value.employeeNo = row.employeeNo;
  formData.value.fromCompanyName = row.companyName || '';
  formData.value.fromDeptName = row.deptName || '';
  formData.value.fromCompanyId = row.companyId;
  formData.value.fromDeptId = row.deptId;
  formData.value.fromPosition = row.position || '';
  employeeDisplayName.value = `${row.name} (${row.employeeNo})`;
}

async function handleSubmit() {
  if (!formData.value.employeeId || !formData.value.transferType || !formData.value.effectDate) {
    ElMessage.warning('请填写必填项');
    return;
  }
  // 根据变更类型验证必填项
  if (showNewDept.value && !formData.value.toDeptId) {
    ElMessage.warning('请选择新部门');
    return;
  }
  if (showNewPosition.value && !formData.value.toPosition) {
    ElMessage.warning('请选择新职位');
    return;
  }
  // 自动计算新公司ID
  if (formData.value.toDeptId) {
    formData.value.toCompanyId = findCompanyId(formData.value.toDeptId);
  }
  submitLoading.value = true;
  try {
    await createApplication(formData.value);
    ElMessage.success('申请提交成功');
    dialogVisible.value = false;
    loadData();
  } catch {
    ElMessage.error('提交失败');
  } finally {
    submitLoading.value = false;
  }
}

// 详情抽屉
const detailVisible = ref(false);
const currentApplication = ref<Application | null>(null);

function handleViewDetail(row: Application) {
  currentApplication.value = row;
  detailVisible.value = true;
}

async function handleCancel(id: number) {
  try {
    await ElMessageBox.confirm('确定撤销该申请吗？撤销后不可恢复', '撤销确认', {
      type: 'warning',
      confirmButtonText: '确认撤销',
      cancelButtonText: '取消'
    });
  } catch {
    return;
  }
  try {
    await cancelApplication(id);
    ElMessage.success('已撤销');
    loadData();
  } catch {
    ElMessage.error('撤销失败');
  }
}

function handleSearch() {
  currentPage.value = 1;
  loadData();
}
function handleReset() {
  searchParams.value = { employeeName: '', employeeNo: '', status: undefined, transferType: undefined };
  currentPage.value = 1;
  loadData();
}
function handlePageChange(page: number) {
  currentPage.value = page;
  loadData();
}
function handleSizeChange(size: number) {
  pageSize.value = size;
  currentPage.value = 1;
  loadData();
}
</script>

<template>
  <div class="min-h-500px flex-col-stretch gap-16px overflow-hidden lt-sm:overflow-auto">
    <ElCard>
      <ElForm inline :model="searchParams">
        <ElFormItem label="员工姓名">
          <ElInput v-model="searchParams.employeeName" placeholder="请输入员工姓名" clearable />
        </ElFormItem>
        <ElFormItem label="员工编号">
          <ElInput v-model="searchParams.employeeNo" placeholder="请输入员工编号" clearable />
        </ElFormItem>
        <ElFormItem label="变更类型">
          <ElSelect v-model="searchParams.transferType" placeholder="请选择类型" clearable style="width: 150px">
            <ElOption
              v-for="item in transferTypeOptions"
              :key="item.dictValue"
              :label="getTransferTypeLabel(item.dictValue)"
              :value="item.dictValue"
            />
          </ElSelect>
        </ElFormItem>
        <ElFormItem label="状态">
          <ElSelect v-model="searchParams.status" placeholder="请选择状态" clearable style="width: 120px">
            <ElOption label="待审批" :value="0" />
            <ElOption label="已通过" :value="1" />
            <ElOption label="已拒绝" :value="2" />
            <ElOption label="已撤销" :value="3" />
          </ElSelect>
        </ElFormItem>
        <ElFormItem>
          <ElButton type="primary" @click="handleSearch">
            <template #icon><icon-ep-search /></template>
            搜索
          </ElButton>
          <ElButton @click="handleReset">
            <template #icon><icon-ep-refresh /></template>
            重置
          </ElButton>
        </ElFormItem>
      </ElForm>
    </ElCard>

    <ElCard class="flex-1">
      <template #header>
        <div class="flex items-center justify-between">
          <span>调动申请列表</span>
          <ElButton type="primary" @click="handleAdd">
            <template #icon><icon-ep-plus /></template>
            新增申请
          </ElButton>
        </div>
      </template>

      <ElTable v-loading="loading" :data="data" border stripe>
        <ElTableColumn type="index" label="序号" width="60" align="center" />
        <ElTableColumn prop="employeeNo" label="工号" min-width="100" />
        <ElTableColumn prop="employeeName" label="员工姓名" min-width="100" />
        <ElTableColumn prop="transferType" label="变更类型" min-width="100" align="center">
          <template #default="{ row }">{{ getTransferTypeLabel(row.transferType) }}</template>
        </ElTableColumn>
        <ElTableColumn prop="fromCompanyName" label="原公司" min-width="120" show-overflow-tooltip />
        <ElTableColumn prop="fromDeptName" label="原部门" min-width="100" />
        <ElTableColumn prop="fromPosition" label="原职位" min-width="100">
          <template #default="{ row }">{{ getPositionLabel(row.fromPosition) }}</template>
        </ElTableColumn>
        <ElTableColumn prop="toCompanyName" label="新公司" min-width="120" show-overflow-tooltip />
        <ElTableColumn prop="toDeptName" label="新部门" min-width="100" />
        <ElTableColumn prop="toPosition" label="新职位" min-width="100">
          <template #default="{ row }">{{ getPositionLabel(row.toPosition) }}</template>
        </ElTableColumn>
        <ElTableColumn prop="effectDate" label="生效日期" min-width="110" />
        <ElTableColumn prop="status" label="状态" min-width="90" align="center">
          <template #default="{ row }">
            <ElTag :type="statusMap[row.status]?.type as any">{{ statusMap[row.status]?.label }}</ElTag>
          </template>
        </ElTableColumn>
        <ElTableColumn prop="createdTime" label="申请时间" min-width="160" />
        <ElTableColumn label="操作" width="140" align="center" fixed="right">
          <template #default="{ row }">
            <ElButton type="primary" link size="small" @click="handleViewDetail(row)">
              详情
            </ElButton>
            <ElButton v-if="row.status === 0" type="warning" link size="small" @click="handleCancel(row.id)">
              撤销
            </ElButton>
          </template>
        </ElTableColumn>
      </ElTable>

      <div class="mt-16px flex justify-end">
        <ElPagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @current-change="handlePageChange"
          @size-change="handleSizeChange"
        />
      </div>
    </ElCard>

    <!-- 新增申请弹窗 -->
    <ElDialog v-model="dialogVisible" title="新增调动申请" width="650px" destroy-on-close>
      <ElForm label-width="100px" :model="formData">
        <ElFormItem label="员工" required>
          <div class="w-full flex gap-8px">
            <ElInput v-model="employeeDisplayName" disabled placeholder="请选择员工" class="flex-1" />
            <ElButton type="primary" @click="employeeDialogVisible = true">选择员工</ElButton>
          </div>
        </ElFormItem>
        <ElRow :gutter="20">
          <ElCol :span="12">
            <ElFormItem label="原公司">
              <ElInput v-model="formData.fromCompanyName" disabled placeholder="选择员工后自动显示" />
            </ElFormItem>
          </ElCol>
          <ElCol :span="12">
            <ElFormItem label="原部门">
              <ElInput v-model="formData.fromDeptName" disabled placeholder="选择员工后自动显示" />
            </ElFormItem>
          </ElCol>
        </ElRow>
        <ElFormItem label="原职位">
          <ElInput :model-value="getPositionLabel(formData.fromPosition)" disabled placeholder="选择员工后自动显示" />
        </ElFormItem>
        <ElFormItem label="变更类型" required>
          <ElSelect
            v-model="formData.transferType"
            placeholder="请选择变更类型"
            style="width: 100%"
            @change="handleTransferTypeChange"
          >
            <ElOption
              v-for="item in transferTypeOptions"
              :key="item.dictValue"
              :label="getTransferTypeLabel(item.dictValue)"
              :value="item.dictValue"
            />
          </ElSelect>
        </ElFormItem>
        <ElFormItem v-if="showNewDept" label="新部门" required>
          <ElTreeSelect
            v-model="formData.toDeptId"
            :data="orgTreeOptions"
            :props="{ children: 'children', label: 'unitName', value: 'id' }"
            node-key="id"
            placeholder="请选择新部门"
            clearable
            style="width: 100%"
            :render-after-expand="false"
            filterable
            check-strictly
          />
        </ElFormItem>
        <ElFormItem v-if="showNewPosition" label="新职位" required>
          <ElSelect v-model="formData.toPosition" placeholder="请选择新职位" style="width: 100%" clearable>
            <ElOption
              v-for="item in positionOptions"
              :key="item.dictValue"
              :label="getPositionLabel(item.dictValue)"
              :value="item.dictValue"
            />
          </ElSelect>
        </ElFormItem>
        <ElFormItem label="生效日期" required>
          <ElDatePicker
            v-model="formData.effectDate"
            type="date"
            placeholder="选择日期"
            style="width: 100%"
            value-format="YYYY-MM-DD"
          />
        </ElFormItem>
        <ElFormItem label="调动原因">
          <ElInput v-model="formData.reason" type="textarea" :rows="3" placeholder="请输入调动原因" />
        </ElFormItem>
      </ElForm>
      <template #footer>
        <ElButton @click="dialogVisible = false">取消</ElButton>
        <ElButton type="primary" :loading="submitLoading" @click="handleSubmit">提交申请</ElButton>
      </template>
    </ElDialog>

    <EmployeePickerDialog v-model="employeeDialogVisible" @confirm="handleConfirmEmployee" />
    <ApplicationDetailDrawer v-model="detailVisible" :application="currentApplication" />
  </div>
</template>
