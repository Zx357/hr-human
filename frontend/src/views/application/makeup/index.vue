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
import EmployeePickerDialog from '@/components/common/EmployeePickerDialog.vue';
import ApplicationDetailDrawer from '@/components/business/application-detail-drawer.vue';

defineOptions({ name: 'MakeupApplication' });

const loading = ref(false);
// 选择员工
const employeeDisplayName = ref('');
const data = ref<Application[]>([]);
const total = ref(0);
const currentPage = ref(1);
const pageSize = ref(10);

const dialogVisible = ref(false);
const submitLoading = ref(false);
const formData = ref<
  Application & { employeeName?: string; employeeNo?: string; companyName?: string; deptName?: string }
>({
  employeeId: undefined as any,
  appType: 'makeup'
});

// 员工选择弹窗
const employeeDialogVisible = ref(false);

const searchParams = ref({ employeeName: '', employeeNo: '', status: undefined as number | undefined });
async function loadData() {
  loading.value = true;
  try {
    const res = await fetchApplicationPage({
      pageNum: currentPage.value,
      pageSize: pageSize.value,
      appType: 'makeup',
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
    appType: 'makeup',
    title: '',
    startTime: '',
    endTime: '',
    reason: '',
    employeeName: '',
    employeeNo: '',
    companyName: '',
    deptName: ''
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
  formData.value.companyName = row.companyName || '';
  formData.value.deptName = row.deptName || '';
  employeeDisplayName.value = `${row.name} (${row.employeeNo})`;
}

async function handleSubmit() {
  if (!formData.value.employeeId || !formData.value.title || !formData.value.startTime) {
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
  searchParams.value = { employeeName: '', employeeNo: '', status: undefined };
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

const makeupTypeMap: Record<string, string> = { checkin: '上班补卡', checkout: '下班补卡' };
</script>

<template>
  <div class="list-page">
    <ElCard class="search-card">
      <ElForm inline :model="searchParams">
        <ElFormItem label="员工姓名">
          <ElInput v-model="searchParams.employeeName" placeholder="请输入员工姓名" clearable />
        </ElFormItem>
        <ElFormItem label="员工编号">
          <ElInput v-model="searchParams.employeeNo" placeholder="请输入员工编号" clearable />
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

    <ElCard class="table-card">
      <template #header>
        <div class="flex items-center justify-between">
          <span>补卡申请列表</span>
          <ElButton type="primary" @click="handleAdd">
            <template #icon><icon-ep-plus /></template>
            发起补卡申请
          </ElButton>
        </div>
      </template>

      <div class="table-wrapper">
        <ElTable v-loading="loading" :data="data" border stripe height="100%">
          <ElTableColumn type="index" label="序号" width="60" align="center" />
          <ElTableColumn prop="employeeNo" label="工号" width="100" />
          <ElTableColumn prop="employeeName" label="申请人" width="100" />
          <ElTableColumn prop="companyName" label="所属公司" min-width="120" show-overflow-tooltip />
          <ElTableColumn prop="deptName" label="部门" width="100" />
          <ElTableColumn prop="title" label="补卡类型" width="100">
            <template #default="{ row }">{{ makeupTypeMap[row.title] || row.title }}</template>
          </ElTableColumn>
          <ElTableColumn prop="startTime" label="补卡时间" width="160" />
          <ElTableColumn prop="reason" label="补卡原因" min-width="200" show-overflow-tooltip />
          <ElTableColumn prop="status" label="状态" width="90" align="center">
            <template #default="{ row }">
              <ElTag :type="statusMap[row.status]?.type as any">{{ statusMap[row.status]?.label }}</ElTag>
            </template>
          </ElTableColumn>
          <ElTableColumn prop="createdTime" label="申请时间" width="160" />
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
      </div>

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
    <ElDialog v-model="dialogVisible" title="发起补卡申请" width="600px" destroy-on-close>
      <ElForm label-width="100px" :model="formData">
        <ElFormItem label="申请人" required>
          <div class="w-full flex gap-8px">
            <ElInput v-model="employeeDisplayName" disabled placeholder="请选择员工" class="flex-1" />
            <ElButton type="primary" @click="employeeDialogVisible = true">选择员工</ElButton>
          </div>
        </ElFormItem>
        <ElFormItem label="补卡类型" required>
          <ElSelect v-model="formData.title" placeholder="请选择补卡类型" style="width: 100%">
            <ElOption label="上班补卡" value="checkin" />
            <ElOption label="下班补卡" value="checkout" />
          </ElSelect>
        </ElFormItem>
        <ElFormItem label="补卡时间" required>
          <ElDatePicker
            v-model="formData.startTime"
            type="datetime"
            placeholder="选择时间"
            style="width: 100%"
            value-format="YYYY-MM-DD HH:mm:00"
            format="YYYY-MM-DD HH:mm"
          />
        </ElFormItem>
        <ElFormItem label="补卡原因" required>
          <ElInput v-model="formData.reason" type="textarea" :rows="3" placeholder="请输入补卡原因" />
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
