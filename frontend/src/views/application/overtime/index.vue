<script setup lang="ts">
import { onMounted, ref, watch } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { statusMap } from '@/constants/application';
import {
  type Application,
  calculateOvertimeHours,
  cancelApplication,
  createApplication,
  fetchApplicationPage
} from '@/service/api/application';
import EmployeePickerDialog from '@/components/common/EmployeePickerDialog.vue';

defineOptions({ name: 'OvertimeApplication' });

const loading = ref(false);
const data = ref<Application[]>([]);
const total = ref(0);
const currentPage = ref(1);
const pageSize = ref(10);

const dialogVisible = ref(false);
const submitLoading = ref(false);
const formData = ref<Partial<Application>>({
  appType: 'overtime'
});

// 日期范围
const dateRange = ref<[string, string] | null>(null);

// 加班小时数（自动计算）
const overtimeHours = ref<number>(0);
const calculatingHours = ref(false);

// 多员工选择相关
const selectedEmployees = ref<
  Array<{ id: number; name: string; employeeNo: string; companyName?: string; deptName?: string }>
>([]);

// 监听员工和时间变化，自动计算加班小时（取第一个员工计算）
watch(
  [selectedEmployees, dateRange],
  async () => {
    if (selectedEmployees.value.length > 0 && dateRange.value && dateRange.value.length === 2) {
      calculatingHours.value = true;
      try {
        const startTime = `${dateRange.value[0]}:00`;
        const endTime = `${dateRange.value[1]}:00`;
        const res = await calculateOvertimeHours(selectedEmployees.value[0].id, startTime, endTime);
        overtimeHours.value = res.data || 0;
      } catch {
        overtimeHours.value = 0;
      } finally {
        calculatingHours.value = false;
      }
    } else {
      overtimeHours.value = 0;
    }
  },
  { deep: true }
);

// 员工选择弹窗
const employeeDialogVisible = ref(false);
const searchParams = ref({ employeeName: '', employeeNo: '', status: undefined as number | undefined });
async function loadData() {
  loading.value = true;
  try {
    const res = await fetchApplicationPage({
      pageNum: currentPage.value,
      pageSize: pageSize.value,
      appType: 'overtime',
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
  formData.value = { appType: 'overtime', title: '', startTime: '', endTime: '', duration: 0, reason: '' };
  selectedEmployees.value = [];
  dateRange.value = null;
  overtimeHours.value = 0;
  dialogVisible.value = true;
}
function removeSelectedEmployee(index: number) {
  selectedEmployees.value.splice(index, 1);
}

function handleConfirmEmployees(selected: Api.Hr.Employee[]) {
  selectedEmployees.value = selected.map(row => ({
    id: row.id!,
    name: row.name!,
    employeeNo: row.employeeNo!,
    companyName: row.companyName || '',
    deptName: row.deptName || ''
  }));
}

async function handleSubmit() {
  if (selectedEmployees.value.length === 0 || !dateRange.value || dateRange.value.length !== 2) {
    ElMessage.warning('请填写必填项');
    return;
  }
  if (overtimeHours.value <= 0) {
    ElMessage.warning('加班时间无效，请检查时间范围');
    return;
  }
  submitLoading.value = true;
  try {
    const startTime = `${dateRange.value[0]}:00`;
    const endTime = `${dateRange.value[1]}:00`;
    for (const emp of selectedEmployees.value) {
      // eslint-disable-next-line no-await-in-loop
      await createApplication({
        ...formData.value,
        employeeId: emp.id,
        employeeName: emp.name,
        employeeNo: emp.employeeNo,
        companyName: emp.companyName,
        deptName: emp.deptName,
        startTime,
        endTime,
        duration: overtimeHours.value
      } as Application);
    }
    ElMessage.success(`成功提交 ${selectedEmployees.value.length} 条加班申请`);
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
          <span>加班申请列表</span>
          <ElButton type="primary" @click="handleAdd">
            <template #icon><icon-ep-plus /></template>
            发起加班申请
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
          <ElTableColumn prop="startTime" label="开始时间" width="160" />
          <ElTableColumn prop="endTime" label="结束时间" width="160" />
          <ElTableColumn prop="duration" label="时长(小时)" width="100" align="center" />
          <ElTableColumn prop="reason" label="加班原因" min-width="150" show-overflow-tooltip />
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

    <ElDialog v-model="dialogVisible" title="发起加班申请" width="600px" destroy-on-close>
      <ElForm label-width="100px" :model="formData">
        <ElFormItem label="申请人" required>
          <div class="w-full flex gap-8px">
            <div
              class="min-h-32px flex flex-1 flex-wrap cursor-pointer items-center gap-4px border border-gray-300 rounded-4px px-8px py-4px hover:border-blue-500"
              @click="employeeDialogVisible = true"
            >
              <template v-if="selectedEmployees.length > 0">
                <ElTag
                  v-for="(emp, index) in selectedEmployees"
                  :key="emp.id"
                  closable
                  size="small"
                  @close.stop="removeSelectedEmployee(index)"
                >
                  {{ emp.name }} ({{ emp.employeeNo }})
                </ElTag>
              </template>
              <span v-else class="text-gray-400">请选择员工</span>
            </div>
            <ElButton type="primary" @click="employeeDialogVisible = true">选择员工</ElButton>
          </div>
        </ElFormItem>
        <ElFormItem label="加班时间" required>
          <ElDatePicker
            v-model="dateRange"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            style="width: 100%"
            value-format="YYYY-MM-DD HH:mm"
            format="YYYY-MM-DD HH:mm"
          />
        </ElFormItem>
        <ElFormItem label="加班小时">
          <ElInput v-model="overtimeHours" disabled style="width: 100%">
            <template #suffix>
              <span v-if="calculatingHours" class="text-gray-400">计算中...</span>
              <span v-else>小时</span>
            </template>
          </ElInput>
          <div
            v-if="overtimeHours === 0 && selectedEmployees.length > 0 && dateRange"
            class="mt-4px text-12px text-orange-500"
          >
            提示：加班小时为0，可能是加班时间在正常班次时段内
          </div>
        </ElFormItem>
        <ElFormItem label="加班原因" required>
          <ElInput v-model="formData.reason" type="textarea" :rows="3" placeholder="请输入加班原因" />
        </ElFormItem>
      </ElForm>
      <template #footer>
        <ElButton @click="dialogVisible = false">取消</ElButton>
        <ElButton type="primary" :loading="submitLoading" @click="handleSubmit">提交申请</ElButton>
      </template>
    </ElDialog>

    <EmployeePickerDialog v-model="employeeDialogVisible" multiple @confirm="handleConfirmEmployees" />
  </div>
</template>
