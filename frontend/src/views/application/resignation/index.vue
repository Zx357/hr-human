<script setup lang="ts">
import { onMounted, ref, watch } from 'vue';
import { ElMessage } from 'element-plus';
import { fetchDictDataByCode } from '@/service/api/system';
import { fetchEmployeePage } from '@/service/api/hr';
import { fetchOrgTree } from '@/service/api/organization';
import { fetchApplicationPage, createApplication, cancelApplication, type Application } from '@/service/api/application';

defineOptions({ name: 'ResignationApplication' });

const loading = ref(false);
const data = ref<Application[]>([]);
const total = ref(0);
const currentPage = ref(1);
const pageSize = ref(10);

const resignTypeOptions = ref<Api.System.DictData[]>([]);
const genderOptions = ref<Api.System.DictData[]>([]);
const positionOptions = ref<Api.System.DictData[]>([]);
const orgTreeOptions = ref<Api.Organization.OrgUnit[]>([]);
const dialogVisible = ref(false);
const submitLoading = ref(false);
const formData = ref<Application & { employeeName?: string; employeeNo?: string; companyName?: string; deptName?: string; entryDate?: string }>({
  employeeId: undefined as any, appType: 'resignation'
});

// 员工选择弹窗相关
const employeeDialogVisible = ref(false);
const employeeDialogLoading = ref(false);
const employeeDialogData = ref<Api.Hr.Employee[]>([]);
const employeeDialogTotal = ref(0);
const employeeDialogPage = ref(1);
const employeeDialogPageSize = ref(10);
const employeeDialogSearch = ref({ name: '', employeeNo: '', orgIds: [] as number[] });
const cascadeSelect = ref(false);

// 交接人选择弹窗相关
const handoverDialogVisible = ref(false);
const handoverDialogLoading = ref(false);
const handoverDialogData = ref<Api.Hr.Employee[]>([]);
const handoverDialogTotal = ref(0);
const handoverDialogPage = ref(1);
const handoverDialogPageSize = ref(10);
const handoverDialogSearch = ref({ name: '', employeeNo: '', orgIds: [] as number[] });
const handoverCascadeSelect = ref(false);
const handoverToName = ref('');

const searchParams = ref({ employeeName: '', employeeNo: '', status: undefined as number | undefined, resignType: undefined as string | undefined });

async function loadDictData() {
  const [resignRes, genderRes, positionRes] = await Promise.all([
    fetchDictDataByCode('resign_type'),
    fetchDictDataByCode('gender'),
    fetchDictDataByCode('position')
  ]);
  resignTypeOptions.value = resignRes.data || [];
  genderOptions.value = genderRes.data || [];
  positionOptions.value = positionRes.data || [];
}

async function loadOrgTree() {
  try {
    const res = await fetchOrgTree();
    orgTreeOptions.value = res.data || [];
  } catch (error) { console.error('加载组织架构失败:', error); }
}

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
  } finally { loading.value = false; }
}

// 加载员工弹窗数据
async function loadEmployeeDialogData() {
  employeeDialogLoading.value = true;
  try {
    const res = await fetchEmployeePage({
      pageNum: employeeDialogPage.value,
      pageSize: employeeDialogPageSize.value,
      name: employeeDialogSearch.value.name || undefined,
      employeeNo: employeeDialogSearch.value.employeeNo || undefined,
      orgIds: employeeDialogSearch.value.orgIds.length > 0 ? employeeDialogSearch.value.orgIds.join(',') : undefined,
      status: 1
    });
    if (res.data) {
      employeeDialogData.value = res.data.records || [];
      employeeDialogTotal.value = res.data.total || 0;
    }
  } catch (error) {
    console.error('加载员工列表失败:', error);
  } finally {
    employeeDialogLoading.value = false;
  }
}

// 加载交接人弹窗数据
async function loadHandoverDialogData() {
  handoverDialogLoading.value = true;
  try {
    const res = await fetchEmployeePage({
      pageNum: handoverDialogPage.value,
      pageSize: handoverDialogPageSize.value,
      name: handoverDialogSearch.value.name || undefined,
      employeeNo: handoverDialogSearch.value.employeeNo || undefined,
      orgIds: handoverDialogSearch.value.orgIds.length > 0 ? handoverDialogSearch.value.orgIds.join(',') : undefined,
      status: 1
    });
    if (res.data) {
      // 排除当前选择的员工
      handoverDialogData.value = (res.data.records || []).filter(e => e.id !== formData.value.employeeId);
      handoverDialogTotal.value = res.data.total || 0;
    }
  } catch (error) {
    console.error('加载员工列表失败:', error);
  } finally {
    handoverDialogLoading.value = false;
  }
}

onMounted(() => { loadDictData(); loadOrgTree(); loadData(); });

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

// 打开员工选择弹窗
function openEmployeeDialog() {
  employeeDialogVisible.value = true;
  employeeDialogPage.value = 1;
  employeeDialogSearch.value = { name: '', employeeNo: '', orgIds: [] };
  loadEmployeeDialogData();
}

// 员工弹窗搜索
function handleEmployeeDialogSearch() {
  employeeDialogPage.value = 1;
  loadEmployeeDialogData();
}

// 员工弹窗重置
function handleEmployeeDialogReset() {
  employeeDialogSearch.value = { name: '', employeeNo: '', orgIds: [] };
  employeeDialogPage.value = 1;
  loadEmployeeDialogData();
}

// 员工弹窗分页
function handleEmployeeDialogPageChange(page: number) {
  employeeDialogPage.value = page;
  loadEmployeeDialogData();
}

function handleEmployeeDialogSizeChange(size: number) {
  employeeDialogPageSize.value = size;
  employeeDialogPage.value = 1;
  loadEmployeeDialogData();
}

// 选择员工
const employeeDisplayName = ref('');
function handleSelectEmployee(row: Api.Hr.Employee) {
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
  employeeDialogVisible.value = false;
}

// 打开交接人选择弹窗
function openHandoverDialog() {
  if (!formData.value.employeeId) {
    ElMessage.warning('请先选择员工');
    return;
  }
  handoverDialogVisible.value = true;
  handoverDialogPage.value = 1;
  handoverDialogSearch.value = { name: '', employeeNo: '', orgIds: [] };
  loadHandoverDialogData();
}

// 交接人弹窗搜索
function handleHandoverDialogSearch() {
  handoverDialogPage.value = 1;
  loadHandoverDialogData();
}

// 交接人弹窗重置
function handleHandoverDialogReset() {
  handoverDialogSearch.value = { name: '', employeeNo: '', orgIds: [] };
  handoverDialogPage.value = 1;
  loadHandoverDialogData();
}

// 交接人弹窗分页
function handleHandoverDialogPageChange(page: number) {
  handoverDialogPage.value = page;
  loadHandoverDialogData();
}

function handleHandoverDialogSizeChange(size: number) {
  handoverDialogPageSize.value = size;
  handoverDialogPage.value = 1;
  loadHandoverDialogData();
}

// 选择交接人
function handleSelectHandover(row: Api.Hr.Employee) {
  formData.value.handoverTo = row.id!;
  handoverToName.value = `${row.name} (${row.employeeNo})`;
  handoverDialogVisible.value = false;
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
  } catch { ElMessage.error('提交失败'); }
  finally { submitLoading.value = false; }
}

async function handleCancel(id: number) {
  try {
    await cancelApplication(id);
    ElMessage.success('已撤销');
    loadData();
  } catch { ElMessage.error('撤销失败'); }
}

function handleSearch() { currentPage.value = 1; loadData(); }
function handleReset() { searchParams.value = { employeeName: '', employeeNo: '', status: undefined, resignType: undefined }; currentPage.value = 1; loadData(); }
function handlePageChange(page: number) { currentPage.value = page; loadData(); }
function handleSizeChange(size: number) { pageSize.value = size; currentPage.value = 1; loadData(); }

function getDictLabel(options: Api.System.DictData[], value?: string) {
  if (!value) return '';
  return options.find(o => o.dictValue === value)?.dictLabel || value;
}

const statusMap: Record<number, { label: string; type: string }> = {
  0: { label: '待审批', type: 'warning' },
  1: { label: '已通过', type: 'success' },
  2: { label: '已拒绝', type: 'danger' },
  3: { label: '已撤销', type: 'info' }
};
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
            <ElOption v-for="item in resignTypeOptions" :key="item.dictValue" :label="item.dictLabel" :value="item.dictValue" />
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
          <template #default="{ row }">{{ getDictLabel(resignTypeOptions, row.resignType) }}</template>
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
            <ElButton v-if="row.status === 0" type="warning" link size="small" @click="handleCancel(row.id)">撤销</ElButton>
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
          <div class="flex gap-8px w-full">
            <ElInput v-model="employeeDisplayName" disabled placeholder="请选择员工" class="flex-1" />
            <ElButton type="primary" @click="openEmployeeDialog">选择员工</ElButton>
          </div>
        </ElFormItem>
        <ElFormItem label="离职类型" required>
          <ElSelect v-model="formData.resignType" placeholder="请选择离职类型" style="width: 100%">
            <ElOption v-for="item in resignTypeOptions" :key="item.dictValue" :label="item.dictLabel" :value="item.dictValue" />
          </ElSelect>
        </ElFormItem>
        <ElFormItem label="最后工作日" required>
          <ElDatePicker v-model="formData.lastWorkDate" type="date" placeholder="选择日期" style="width: 100%" value-format="YYYY-MM-DD" />
        </ElFormItem>
        <ElFormItem label="工作交接人">
          <div class="flex gap-8px w-full">
            <ElInput v-model="handoverToName" disabled placeholder="请选择交接人" class="flex-1" />
            <ElButton @click="openHandoverDialog">选择交接人</ElButton>
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

    <!-- 员工选择弹窗 -->
    <ElDialog v-model="employeeDialogVisible" title="选择员工" width="900px" destroy-on-close append-to-body>
      <div class="mb-16px">
        <ElForm inline :model="employeeDialogSearch">
          <ElFormItem label="姓名">
            <ElInput v-model="employeeDialogSearch.name" placeholder="请输入姓名" clearable style="width: 120px" />
          </ElFormItem>
          <ElFormItem label="工号">
            <ElInput v-model="employeeDialogSearch.employeeNo" placeholder="请输入工号" clearable style="width: 120px" />
          </ElFormItem>
          <ElFormItem label="组织">
            <ElTreeSelect
              v-model="employeeDialogSearch.orgIds"
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
              style="width: 180px"
              :render-after-expand="false"
              filterable
            >
              <template #header>
                <div class="px-12px py-8px border-b border-gray-200">
                  <ElCheckbox v-model="cascadeSelect" size="small">联动选择</ElCheckbox>
                </div>
              </template>
            </ElTreeSelect>
          </ElFormItem>
          <ElFormItem>
            <ElButton type="primary" @click="handleEmployeeDialogSearch">搜索</ElButton>
            <ElButton @click="handleEmployeeDialogReset">重置</ElButton>
          </ElFormItem>
        </ElForm>
      </div>
      <ElTable v-loading="employeeDialogLoading" :data="employeeDialogData" border stripe max-height="400px">
        <ElTableColumn prop="employeeNo" label="工号" width="100" />
        <ElTableColumn prop="name" label="姓名" width="80" />
        <ElTableColumn prop="gender" label="性别" width="60" align="center">
          <template #default="{ row }">{{ getDictLabel(genderOptions, row.gender) }}</template>
        </ElTableColumn>
        <ElTableColumn prop="companyName" label="公司" min-width="120" show-overflow-tooltip />
        <ElTableColumn prop="deptName" label="部门" min-width="100" />
        <ElTableColumn prop="position" label="职位" min-width="100">
          <template #default="{ row }">{{ getDictLabel(positionOptions, row.position) }}</template>
        </ElTableColumn>
        <ElTableColumn label="操作" width="80" align="center" fixed="right">
          <template #default="{ row }">
            <ElButton type="primary" link size="small" @click="handleSelectEmployee(row)">选择</ElButton>
          </template>
        </ElTableColumn>
      </ElTable>
      <div class="mt-16px flex justify-end">
        <ElPagination
          v-model:current-page="employeeDialogPage"
          v-model:page-size="employeeDialogPageSize"
          :total="employeeDialogTotal"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          @current-change="handleEmployeeDialogPageChange"
          @size-change="handleEmployeeDialogSizeChange"
        />
      </div>
    </ElDialog>

    <!-- 交接人选择弹窗 -->
    <ElDialog v-model="handoverDialogVisible" title="选择交接人" width="900px" destroy-on-close append-to-body>
      <div class="mb-16px">
        <ElForm inline :model="handoverDialogSearch">
          <ElFormItem label="姓名">
            <ElInput v-model="handoverDialogSearch.name" placeholder="请输入姓名" clearable style="width: 120px" />
          </ElFormItem>
          <ElFormItem label="工号">
            <ElInput v-model="handoverDialogSearch.employeeNo" placeholder="请输入工号" clearable style="width: 120px" />
          </ElFormItem>
          <ElFormItem label="组织">
            <ElTreeSelect
              v-model="handoverDialogSearch.orgIds"
              :data="orgTreeOptions"
              :props="{ children: 'children', label: 'unitName', value: 'id' }"
              node-key="id"
              placeholder="请选择组织"
              clearable
              multiple
              :check-strictly="!handoverCascadeSelect"
              show-checkbox
              collapse-tags
              :max-collapse-tags="1"
              style="width: 180px"
              :render-after-expand="false"
              filterable
            >
              <template #header>
                <div class="px-12px py-8px border-b border-gray-200">
                  <ElCheckbox v-model="handoverCascadeSelect" size="small">联动选择</ElCheckbox>
                </div>
              </template>
            </ElTreeSelect>
          </ElFormItem>
          <ElFormItem>
            <ElButton type="primary" @click="handleHandoverDialogSearch">搜索</ElButton>
            <ElButton @click="handleHandoverDialogReset">重置</ElButton>
          </ElFormItem>
        </ElForm>
      </div>
      <ElTable v-loading="handoverDialogLoading" :data="handoverDialogData" border stripe max-height="400px">
        <ElTableColumn prop="employeeNo" label="工号" width="100" />
        <ElTableColumn prop="name" label="姓名" width="80" />
        <ElTableColumn prop="gender" label="性别" width="60" align="center">
          <template #default="{ row }">{{ getDictLabel(genderOptions, row.gender) }}</template>
        </ElTableColumn>
        <ElTableColumn prop="companyName" label="公司" min-width="120" show-overflow-tooltip />
        <ElTableColumn prop="deptName" label="部门" min-width="100" />
        <ElTableColumn prop="position" label="职位" min-width="100">
          <template #default="{ row }">{{ getDictLabel(positionOptions, row.position) }}</template>
        </ElTableColumn>
        <ElTableColumn label="操作" width="80" align="center" fixed="right">
          <template #default="{ row }">
            <ElButton type="primary" link size="small" @click="handleSelectHandover(row)">选择</ElButton>
          </template>
        </ElTableColumn>
      </ElTable>
      <div class="mt-16px flex justify-end">
        <ElPagination
          v-model:current-page="handoverDialogPage"
          v-model:page-size="handoverDialogPageSize"
          :total="handoverDialogTotal"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          @current-change="handleHandoverDialogPageChange"
          @size-change="handleHandoverDialogSizeChange"
        />
      </div>
    </ElDialog>
  </div>
</template>
