<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import {
  fetchMobileApproverPage,
  createMobileApprover,
  updateMobileApprover,
  deleteMobileApprover,
  type MobileApprover
} from '@/service/api/mobileApprover';
import { fetchEmployeeList } from '@/service/api/hr';

defineOptions({ name: 'ApprovalMobileApprover' });

const APP_TYPE_OPTIONS = [
  { label: '请假', value: 'leave' },
  { label: '加班', value: 'overtime' },
  { label: '出差', value: 'business' },
  { label: '补卡', value: 'makeup' },
  { label: '换休', value: 'exchange' },
  { label: '转正', value: 'regularization' },
  { label: '调动', value: 'transfer' },
  { label: '奖惩', value: 'reward' },
  { label: '离职', value: 'resignation' }
];

// 列表相关
const loading = ref(false);
const tableData = ref<MobileApprover[]>([]);
const total = ref(0);
const pageNum = ref(1);
const pageSize = ref(10);
const searchName = ref('');
const searchNo = ref('');

// 对话框相关
const dialogVisible = ref(false);
const operateType = ref<'add' | 'edit'>('add');
const submitLoading = ref(false);
const formData = ref<MobileApprover>({ employeeId: undefined, appTypes: '' });
const employeeList = ref<any[]>([]);

// 选中的类型数组（用于多选组件）
const selectedTypes = ref<string[]>([]);

async function loadData() {
  loading.value = true;
  try {
    const { data, error } = await fetchMobileApproverPage({
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      employeeName: searchName.value || undefined,
      employeeNo: searchNo.value || undefined
    });
    if (!error && data) {
      tableData.value = data.records || [];
      total.value = data.total || 0;
    }
  } finally {
    loading.value = false;
  }
}

async function loadEmployees() {
  try {
    const { data, error } = await fetchEmployeeList({ status: 1 });
    if (!error && data) {
      employeeList.value = data;
    }
  } catch (e) {
    console.error(e);
  }
}

onMounted(() => {
  loadData();
  loadEmployees();
});

function handleSearch() {
  pageNum.value = 1;
  loadData();
}

function handleReset() {
  searchName.value = '';
  searchNo.value = '';
  pageNum.value = 1;
  loadData();
}

function handleAdd() {
  operateType.value = 'add';
  formData.value = { employeeId: undefined, appTypes: '' };
  selectedTypes.value = [];
  dialogVisible.value = true;
}

function handleEdit(row: MobileApprover) {
  operateType.value = 'edit';
  formData.value = { ...row };
  selectedTypes.value = row.appTypes ? row.appTypes.split(',').filter(Boolean) : [];
  dialogVisible.value = true;
}

async function handleDelete(row: MobileApprover) {
  try {
    await ElMessageBox.confirm('确定要删除该审批权限配置吗？', '提示', { type: 'warning' });
    const { error } = await deleteMobileApprover(row.id!);
    if (!error) {
      ElMessage.success('删除成功');
      loadData();
    }
  } catch {
    // cancelled
  }
}

async function handleSubmit() {
  if (!formData.value.employeeId) {
    ElMessage.warning('请选择员工');
    return;
  }
  formData.value.appTypes = selectedTypes.value.length > 0 ? selectedTypes.value.join(',') : '';

  submitLoading.value = true;
  try {
    const fn = operateType.value === 'add' ? createMobileApprover : updateMobileApprover;
    const { error, data: resData } = await fn(formData.value);
    if (!error) {
      ElMessage.success(operateType.value === 'add' ? '添加成功' : '修改成功');
      dialogVisible.value = false;
      loadData();
    }
  } finally {
    submitLoading.value = false;
  }
}

function formatAppTypes(types: string | undefined) {
  if (!types) return '全部类型';
  const map: Record<string, string> = {};
  APP_TYPE_OPTIONS.forEach(o => { map[o.value] = o.label; });
  return types.split(',').map(t => map[t] || t).join('、');
}

function handlePageChange(val: number) {
  pageNum.value = val;
  loadData();
}

function handleSizeChange(val: number) {
  pageSize.value = val;
  pageNum.value = 1;
  loadData();
}
</script>

<template>
  <div class="list-page">
    <!-- 搜索栏 -->
    <ElCard shadow="never" class="mb-4">
      <ElForm inline>
        <ElFormItem label="员工姓名">
          <ElInput v-model="searchName" placeholder="请输入" clearable @keyup.enter="handleSearch" />
        </ElFormItem>
        <ElFormItem label="工号">
          <ElInput v-model="searchNo" placeholder="请输入" clearable @keyup.enter="handleSearch" />
        </ElFormItem>
        <ElFormItem>
          <ElButton type="primary" @click="handleSearch">搜索</ElButton>
          <ElButton @click="handleReset">重置</ElButton>
        </ElFormItem>
      </ElForm>
    </ElCard>

    <!-- 表格 -->
    <ElCard shadow="never" class="table-card">
      <template #header>
        <div class="card-header">
          <span>移动端审批权限</span>
          <ElButton type="primary" @click="handleAdd">新增</ElButton>
        </div>
      </template>
      <div class="table-wrapper">
        <ElTable v-loading="loading" :data="tableData" border stripe style="width: 100%">
          <ElTableColumn prop="employeeNo" label="工号" width="100" />
          <ElTableColumn prop="employeeName" label="员工姓名" width="120" />
          <ElTableColumn prop="deptName" label="部门" width="150" />
          <ElTableColumn label="可审批类型" min-width="250">
            <template #default="{ row }">
              <span>{{ formatAppTypes(row.appTypes) }}</span>
            </template>
          </ElTableColumn>
          <ElTableColumn prop="createdTime" label="配置时间" width="180" />
          <ElTableColumn label="操作" width="150" fixed="right">
            <template #default="{ row }">
              <ElButton link type="primary" @click="handleEdit(row)">编辑</ElButton>
              <ElButton link type="danger" @click="handleDelete(row)">删除</ElButton>
            </template>
          </ElTableColumn>
        </ElTable>
      </div>
      <div class="mt-4 flex justify-end">
        <ElPagination v-model:current-page="pageNum" v-model:page-size="pageSize" :total="total"
          :page-sizes="[10, 20, 50, 100]" layout="total, sizes, prev, pager, next" @current-change="handlePageChange"
          @size-change="handleSizeChange" />
      </div>
    </ElCard>

    <!-- 新增/编辑对话框 -->
    <ElDialog v-model="dialogVisible" :title="operateType === 'add' ? '新增审批权限' : '编辑审批权限'" width="550px">
      <ElForm label-width="100px">
        <ElFormItem label="选择员工" required>
          <ElSelect v-model="formData.employeeId" placeholder="请选择员工" filterable :disabled="operateType === 'edit'"
            style="width: 100%">
            <ElOption v-for="emp in employeeList" :key="emp.id" :label="emp.employeeNo + ' - ' + emp.name"
              :value="emp.id" />
          </ElSelect>
        </ElFormItem>
        <ElFormItem label="审批类型">
          <ElCheckboxGroup v-model="selectedTypes">
            <ElCheckbox v-for="opt in APP_TYPE_OPTIONS" :key="opt.value" :label="opt.value">
              {{ opt.label }}
            </ElCheckbox>
          </ElCheckboxGroup>
          <div class="text-xs text-gray-400 mt-1">不勾选则表示可审批全部类型</div>
        </ElFormItem>
      </ElForm>
      <template #footer>
        <ElButton @click="dialogVisible = false">取消</ElButton>
        <ElButton type="primary" :loading="submitLoading" @click="handleSubmit">确定</ElButton>
      </template>
    </ElDialog>
  </div>
</template>

<style scoped>
.list-page {
  display: flex;
  flex-direction: column;
  height: 100%;
}

.table-card {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.table-card :deep(.el-card__body) {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.table-wrapper {
  flex: 1;
  overflow: auto;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
