<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { statusMap } from '@/constants/application';
import {
  type Application,
  cancelApplication,
  createApplication,
  fetchApplicationPage
} from '@/service/api/application';
import { useDictOptions } from '@/composables/use-dict-options';
import EmployeePickerDialog from '@/components/common/EmployeePickerDialog.vue';

defineOptions({ name: 'ResignationApplication' });

const loading = ref(false);
// 选择员工
const employeeDisplayName = ref('');
const data = ref<Application[]>([]);
const total = ref(0);
const currentPage = ref(1);
const pageSize = ref(10);

const { options: resignTypeOptions, getDictLabel: getResignTypeLabel } = useDictOptions('resign_type');
const dialogVisible = ref(false);
const submitLoading = ref(false);
const formData = ref<
  Application & {
    employeeName?: string;
    employeeNo?: string;
    companyName?: string;
    deptName?: string;
    entryDate?: string;
  }
>({
  employeeId: undefined as any,
  appType: 'resignation'
});

// 员工选择弹窗
const employeeDialogVisible = ref(false);

// 交接人选择弹窗相关
const handoverDialogVisible = ref(false);
const handoverToName = ref('');

const searchParams = ref({
  employeeName: '',
  employeeNo: '',
  status: undefined as number | undefined,
  resignType: undefined as string | undefined
});
async function loadData() {
  loading.value = true;
  try {
    const res = await fetchApplicationPage({
      pageNum: currentPage.value,
      pageSize: pageSize.value,
      appType: 'resignation',
      employeeName: searchParams.value.employeeName || undefined,
      employeeNo: searchParams.value.employeeNo || undefined,
      status: searchParams.value.status,
      resignType: searchParams.value.resignType
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
    appType: 'resignation',
    resignType: '',
    lastWorkDate: '',
    handoverTo: undefined,
    reason: '',
    employeeName: '',
    employeeNo: '',
    companyName: '',
    deptName: '',
    entryDate: ''
  };
  employeeDisplayName.value = '';
  handoverToName.value = '';
  dialogVisible.value = true;
}

function handleConfirmEmployee(selected: Api.Hr.Employee[]) {
  const row = selected[0];
  if (!row?.id) return;

  formData.value.employeeId = row.id!;
  formData.value.employeeName = row.name;
  formData.value.employeeNo = row.employeeNo;
  formData.value.companyName = row.companyName || '';
  formData.value.deptName = row.deptName || '';
  formData.value.entryDate = row.entryDate || '';
  employeeDisplayName.value = `${row.name} (${row.employeeNo})`;
  // 清空交接人
  formData.value.handoverTo = undefined;
  handoverToName.value = '';
} // 交接人弹窗重置// 交接人弹窗分页// 选择交接人
function handleConfirmHandover(selected: Api.Hr.Employee[]) {
  const row = selected[0];
  if (!row?.id) return;

  formData.value.handoverTo = row.id!;
  handoverToName.value = `${row.name} (${row.employeeNo})`;
}

async function handleSubmit() {
  if (!formData.value.employeeId || !formData.value.resignType || !formData.value.lastWorkDate) {
    ElMessage.warning('请填写必填项');
    return;
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
  searchParams.value = { employeeName: '', employeeNo: '', status: undefined, resignType: undefined };
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
        <ElFormItem label="离职类型">
          <ElSelect v-model="searchParams.resignType" placeholder="请选择类型" clearable style="width: 120px">
            <ElOption
              v-for="item in resignTypeOptions"
              :key="item.dictValue"
              :label="getResignTypeLabel(item.dictValue)"
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
          <span>离职申请列表</span>
          <ElButton type="primary" @click="handleAdd">
            <template #icon><icon-ep-plus /></template>
            发起离职申请
          </ElButton>
        </div>
      </template>

      <ElTable v-loading="loading" :data="data" border stripe>
        <ElTableColumn type="index" label="序号" width="60" align="center" />
        <ElTableColumn prop="employeeNo" label="工号" width="100" />
        <ElTableColumn prop="employeeName" label="员工姓名" width="100" />
        <ElTableColumn prop="companyName" label="所属公司" min-width="120" show-overflow-tooltip />
        <ElTableColumn prop="deptName" label="部门" width="100" />
        <ElTableColumn prop="entryDate" label="入职日期" width="110" />
        <ElTableColumn prop="resignType" label="离职类型" width="100">
          <template #default="{ row }">{{ getResignTypeLabel(row.resignType) }}</template>
        </ElTableColumn>
        <ElTableColumn prop="lastWorkDate" label="最后工作日" width="110" />
        <ElTableColumn prop="handoverToName" label="交接人" width="100" />
        <ElTableColumn prop="reason" label="离职原因" min-width="150" show-overflow-tooltip />
        <ElTableColumn prop="status" label="状态" width="90" align="center">
          <template #default="{ row }">
            <ElTag :type="statusMap[row.status]?.type as any">{{ statusMap[row.status]?.label }}</ElTag>
          </template>
        </ElTableColumn>
        <ElTableColumn prop="createdTime" label="申请时间" width="160" />
        <ElTableColumn label="操作" width="100" align="center" fixed="right">
          <template #default="{ row }">
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
    <ElDialog v-model="dialogVisible" title="发起离职申请" width="600px" destroy-on-close>
      <ElForm label-width="100px" :model="formData">
        <ElFormItem label="员工" required>
          <div class="w-full flex gap-8px">
            <ElInput v-model="employeeDisplayName" disabled placeholder="请选择员工" class="flex-1" />
            <ElButton type="primary" @click="employeeDialogVisible = true">选择员工</ElButton>
          </div>
        </ElFormItem>
        <ElFormItem label="离职类型" required>
          <ElSelect v-model="formData.resignType" placeholder="请选择离职类型" style="width: 100%">
            <ElOption
              v-for="item in resignTypeOptions"
              :key="item.dictValue"
              :label="getResignTypeLabel(item.dictValue)"
              :value="item.dictValue"
            />
          </ElSelect>
        </ElFormItem>
        <ElFormItem label="最后工作日" required>
          <ElDatePicker
            v-model="formData.lastWorkDate"
            type="date"
            placeholder="选择日期"
            style="width: 100%"
            value-format="YYYY-MM-DD"
          />
        </ElFormItem>
        <ElFormItem label="工作交接人">
          <div class="w-full flex gap-8px">
            <ElInput v-model="handoverToName" disabled placeholder="请选择交接人" class="flex-1" />
            <ElButton @click="handoverDialogVisible = true">选择交接人</ElButton>
          </div>
        </ElFormItem>
        <ElFormItem label="离职原因">
          <ElInput v-model="formData.reason" type="textarea" :rows="3" placeholder="请输入离职原因" />
        </ElFormItem>
      </ElForm>
      <template #footer>
        <ElButton @click="dialogVisible = false">取消</ElButton>
        <ElButton type="primary" :loading="submitLoading" @click="handleSubmit">提交申请</ElButton>
      </template>
    </ElDialog>

    <EmployeePickerDialog v-model="employeeDialogVisible" @confirm="handleConfirmEmployee" />

    <EmployeePickerDialog v-model="handoverDialogVisible" @confirm="handleConfirmHandover" />
  </div>
</template>
