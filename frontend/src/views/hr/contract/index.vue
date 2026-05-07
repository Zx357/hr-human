<script setup lang="tsx">
import { computed, onMounted, ref } from 'vue';
import { ElMessage } from 'element-plus';
import type { UploadProps } from 'element-plus';
import { Plus } from '@element-plus/icons-vue';
import { type Contract, createContract, deleteContract, fetchContractPage, updateContract } from '@/service/api/contract';
import { getFileUrl, uploadContractPhoto } from '@/service/api/file';
import { fetchEmployeePage } from '@/service/api/hr';
import { fetchOrgTree } from '@/service/api/organization';
import { fetchDictDataByCode } from '@/service/api/system';

defineOptions({ name: 'ContractManage' });

// 数据
const loading = ref(false);
const data = ref<Contract[]>([]);
const total = ref(0);
const currentPage = ref(1);
const pageSize = ref(10);

// 员工选择弹窗相关
const employeeDialogVisible = ref(false);
const employeeDialogLoading = ref(false);
const employeeDialogData = ref<Api.Hr.Employee[]>([]);
const employeeDialogTotal = ref(0);
const employeeDialogPage = ref(1);
const employeeDialogPageSize = ref(10);
const employeeDialogSearch = ref({ name: '', employeeNo: '', orgIds: [] as number[] });
const orgTreeOptions = ref<Api.Organization.OrgUnit[]>([]);
const cascadeSelect = ref(false);
const selectedEmployee = ref<Api.Hr.Employee | null>(null);

// 字典数据
const contractTypeOptions = ref<Api.System.DictData[]>([]);
const genderOptions = ref<Api.System.DictData[]>([]);

const drawerVisible = ref(false);
const operateType = ref<'add' | 'edit'>('add');
const editingData = ref<Contract & { dateRange?: [string, string]; companyName?: string; employeeName?: string; employeeNo?: string }>({
  contractNo: '',
  employeeId: undefined as any,
  contractType: '',
  startDate: '',
  endDate: '',
  dateRange: undefined,
  companyName: '',
  employeeName: '',
  employeeNo: '',
  contractImages: '',
  contractCount: undefined
});
const submitLoading = ref(false);

// 合同图片列表
const contractImageList = ref<string[]>([]);
const employeeDisplayName = ref('');

// 判断是否为无固定期限合同 (dict_value: '2' = 无固定期限)
const isNoFixedTerm = computed(() => editingData.value.contractType === '2');

const searchParams = ref({
  employeeName: '',
  employeeNo: '',
  contractNo: '',
  contractType: undefined as string | undefined,
  status: undefined as number | undefined
});

// 加载字典数据
async function loadDictData() {
  try {
    const [contractRes, genderRes] = await Promise.all([
      fetchDictDataByCode('contract_type'),
      fetchDictDataByCode('gender')
    ]);
    contractTypeOptions.value = contractRes.data || [];
    genderOptions.value = genderRes.data || [];
  } catch (error) {
    console.error('加载字典数据失败:', error);
  }
}

// 加载组织架构树
async function loadOrgTree() {
  try {
    const res = await fetchOrgTree();
    orgTreeOptions.value = res.data || [];
  } catch (error) {
    console.error('加载组织架构失败:', error);
  }
}

// 加载合同数据
async function loadData() {
  loading.value = true;
  try {
    const res = await fetchContractPage({
      pageNum: currentPage.value,
      pageSize: pageSize.value,
      contractNo: searchParams.value.contractNo || undefined,
      employeeName: searchParams.value.employeeName || undefined,
      employeeNo: searchParams.value.employeeNo || undefined,
      contractType: searchParams.value.contractType,
      status: searchParams.value.status
    });
    if (res.data) {
      data.value = res.data.records || [];
      total.value = res.data.total || 0;
    }
  } catch (error) {
    console.error('加载合同列表失败:', error);
  } finally {
    loading.value = false;
  }
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

onMounted(() => {
  loadDictData();
  loadOrgTree();
  loadData();
});

function handleContractDialogOpened() {
  const dialogBody = document.querySelector<HTMLElement>('.contract-dialog .el-dialog__body');
  dialogBody?.scrollTo({ top: 0, behavior: 'auto' });
}

function handleAdd() {
  operateType.value = 'add';
  editingData.value = {
    contractNo: '',
    employeeId: undefined as any,
    contractType: '',
    startDate: '',
    endDate: '',
    signDate: '',
    dateRange: undefined,
    remark: '',
    companyName: '',
    employeeName: '',
    employeeNo: '',
    contractImages: '',
    contractCount: undefined
  };
  selectedEmployee.value = null;
  employeeDisplayName.value = '';
  contractImageList.value = [];
  drawerVisible.value = true;
}

function handleEdit(row: Contract) {
  operateType.value = 'edit';
  editingData.value = {
    ...row,
    dateRange: row.startDate && row.endDate ? [row.startDate, row.endDate] : undefined,
    companyName: (row as any).companyName || '',
    employeeName: (row as any).employeeName || '',
    employeeNo: (row as any).employeeNo || ''
  };
  employeeDisplayName.value = editingData.value.employeeName ? `${editingData.value.employeeName} (${editingData.value.employeeNo})` : '';
  // 解析合同图片
  contractImageList.value = row.contractImages ? row.contractImages.split(',').filter(img => img) : [];
  drawerVisible.value = true;
}

async function handleDelete(id: number) {
  try {
    await deleteContract(id);
    ElMessage.success('删除成功');
    loadData();
  } catch (error) {
    console.error('删除失败:', error);
    ElMessage.error('删除失败');
  }
}

function handleView(row: Contract) {
  operateType.value = 'edit';
  editingData.value = {
    ...row,
    dateRange: row.startDate && row.endDate ? [row.startDate, row.endDate] : undefined,
    companyName: (row as any).companyName || '',
    employeeName: (row as any).employeeName || '',
    employeeNo: (row as any).employeeNo || ''
  };
  // 解析合同图片
  contractImageList.value = row.contractImages ? row.contractImages.split(',').filter(img => img) : [];
  drawerVisible.value = true;
}

function handleSearch() {
  currentPage.value = 1;
  loadData();
}

function handleReset() {
  searchParams.value = {
    employeeName: '',
    employeeNo: '',
    contractNo: '',
    contractType: undefined,
    status: undefined
  };
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
async function handleSelectEmployee(row: Api.Hr.Employee) {
  selectedEmployee.value = row;
  editingData.value.employeeId = row.id!;
  editingData.value.employeeName = row.name;
  editingData.value.employeeNo = row.employeeNo;
  editingData.value.companyName = row.companyName || '';
  employeeDisplayName.value = `${row.name} (${row.employeeNo})`;

  // 自动计算合同次数（查询该员工的合同数量+1）
  if (operateType.value === 'add') {
    await calculateContractCount(row.id!);
  }

  employeeDialogVisible.value = false;
}

// 自动计算合同次数
async function calculateContractCount(employeeId: number) {
  try {
    const res = await fetchContractPage({
      pageNum: 1,
      pageSize: 1000,
      employeeId
    });
    if (res.data) {
      // 合同次数 = 该员工现有合同数 + 1
      editingData.value.contractCount = (res.data.total || 0) + 1;
    }
  } catch (error) {
    console.error('计算合同次数失败:', error);
    editingData.value.contractCount = 1;
  }
}

async function handleSubmit() {
  if (!editingData.value.contractNo || !editingData.value.employeeId) {
    ElMessage.warning('请填写必填项');
    return;
  }
  // 无固定期限只需要开始日期，固定期限需要日期范围
  if (isNoFixedTerm.value) {
    if (!editingData.value.startDate) {
      ElMessage.warning('请选择开始日期');
      return;
    }
  } else if (!editingData.value.dateRange || editingData.value.dateRange.length !== 2) {
    ElMessage.warning('请选择合同期限');
    return;
  }
  submitLoading.value = true;
  try {
    const submitData: Contract = {
      ...editingData.value,
      startDate: isNoFixedTerm.value ? editingData.value.startDate : editingData.value.dateRange![0],
      endDate: isNoFixedTerm.value ? '' : editingData.value.dateRange![1],
      contractImages: contractImageList.value.join(',')
    };
    if (operateType.value === 'add') {
      await createContract(submitData);
      ElMessage.success('新增成功');
    } else {
      await updateContract(submitData);
      ElMessage.success('更新成功');
    }
    drawerVisible.value = false;
    loadData();
  } catch (error) {
    console.error('保存失败:', error);
    ElMessage.error('保存失败');
  } finally {
    submitLoading.value = false;
  }
}

// 图片上传相关
const beforeImageUpload: UploadProps['beforeUpload'] = (rawFile) => {
  const isImage = rawFile.type.startsWith('image/');
  const isLt5M = rawFile.size / 1024 / 1024 < 5;
  if (!isImage) {
    ElMessage.error('只能上传图片文件!');
    return false;
  }
  if (!isLt5M) {
    ElMessage.error('图片大小不能超过 5MB!');
    return false;
  }
  return true;
};

async function handleContractImageUpload(file: File): Promise<boolean> {
  try {
    if (!editingData.value.employeeNo) {
      ElMessage.warning('请先选择员工');
      return false;
    }
    const res = await uploadContractPhoto(file, editingData.value.employeeNo);
    if (res.data) {
      contractImageList.value.push(res.data);
      ElMessage.success('上传成功');
      return true;
    }
    return false;
  } catch (error) {
    console.error('上传失败:', error);
    ElMessage.error('上传失败');
    return false;
  }
}

function handleRemoveContractImage(index: number) {
  contractImageList.value.splice(index, 1);
}

function getDictLabel(options: Api.System.DictData[], value?: string | number): string {
  if (value === undefined || value === null) return '';
  const strValue = String(value);
  const item = options.find(o => o.dictValue === strValue);
  return item?.dictLabel || strValue;
}

const statusMap: Record<number, { label: string; type: string }> = {
  1: { label: '生效中', type: 'success' },
  2: { label: '即将到期', type: 'warning' },
  3: { label: '已到期', type: 'info' },
  4: { label: '已终止', type: 'danger' }
};
</script>

<template>
  <div class="min-h-500px flex-col-stretch gap-16px overflow-hidden lt-sm:overflow-auto">
    <!-- 搜索区域 -->
    <ElCard>
      <ElForm inline :model="searchParams">
        <ElFormItem label="员工姓名">
          <ElInput v-model="searchParams.employeeName" placeholder="请输入员工姓名" clearable />
        </ElFormItem>
        <ElFormItem label="员工编号">
          <ElInput v-model="searchParams.employeeNo" placeholder="请输入员工编号" clearable />
        </ElFormItem>
        <ElFormItem label="合同编号">
          <ElInput v-model="searchParams.contractNo" placeholder="请输入合同编号" clearable />
        </ElFormItem>
        <ElFormItem label="合同类型">
          <ElSelect v-model="searchParams.contractType" placeholder="请选择合同类型" clearable style="width: 180px">
            <ElOption
              v-for="item in contractTypeOptions"
              :key="item.dictValue"
              :label="item.dictLabel"
              :value="item.dictValue"
            />
          </ElSelect>
        </ElFormItem>
        <ElFormItem label="状态">
          <ElSelect v-model="searchParams.status" placeholder="请选择状态" clearable style="width: 150px">
            <ElOption label="生效中" :value="1" />
            <ElOption label="即将到期" :value="2" />
            <ElOption label="已到期" :value="3" />
            <ElOption label="已终止" :value="4" />
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

    <!-- 表格区域 -->
    <ElCard class="flex-1">
      <template #header>
        <div class="flex items-center justify-between">
          <span>合同列表</span>
          <ElButton v-permission="'hr:contract:add'" type="primary" @click="handleAdd">
            <template #icon><icon-ep-plus /></template>
            新增合同
          </ElButton>
        </div>
      </template>

      <ElTable v-loading="loading" :data="data" border stripe>
        <ElTableColumn type="index" label="序号" width="60" align="center" />
        <ElTableColumn prop="contractNo" label="合同编号" min-width="130" />
        <ElTableColumn prop="companyName" label="所属公司" min-width="120" />
        <ElTableColumn prop="employeeNo" label="工号" min-width="100" />
        <ElTableColumn prop="employeeName" label="员工姓名" min-width="100" />
        <ElTableColumn prop="contractType" label="合同类型" min-width="100" align="center">
          <template #default="{ row }">
            {{ getDictLabel(contractTypeOptions, row.contractType) }}
          </template>
        </ElTableColumn>
        <ElTableColumn prop="startDate" label="开始日期" min-width="110" />
        <ElTableColumn prop="endDate" label="结束日期" min-width="110" />
        <ElTableColumn prop="signDate" label="签订日期" min-width="110" />
        <ElTableColumn prop="status" label="状态" min-width="90" align="center">
          <template #default="{ row }">
            <ElTag :type="statusMap[row.status]?.type as any">
              {{ statusMap[row.status]?.label }}
            </ElTag>
          </template>
        </ElTableColumn>
        <ElTableColumn label="操作" width="180" align="center" fixed="right">
          <template #default="{ row }">
            <ElButton type="primary" link size="small" @click="handleView(row)">查看</ElButton>
            <ElButton v-permission="'hr:contract:edit'" type="primary" link size="small" @click="handleEdit(row)">编辑</ElButton>
            <ElPopconfirm title="确定删除该合同吗？" @confirm="handleDelete(row.id)">
              <template #reference>
                <ElButton v-permission="'hr:contract:delete'" type="danger" link size="small">删除</ElButton>
              </template>
            </ElPopconfirm>
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

    <!-- 新增/编辑弹窗 -->
    <ElDialog
      v-model="drawerVisible"
      :title="operateType === 'add' ? '新增合同' : '编辑合同'"
      width="600px"
      top="24px"
      destroy-on-close
      append-to-body
      class="contract-dialog"
      @opened="handleContractDialogOpened"
    >
      <ElForm label-width="100px" :model="editingData">
        <ElFormItem label="合同编号" required>
          <ElInput v-model="editingData.contractNo" placeholder="请输入合同编号" />
        </ElFormItem>
        <ElFormItem label="员工" required>
          <div class="flex gap-8px w-full">
            <ElInput v-model="employeeDisplayName" disabled placeholder="请选择员工" class="flex-1" />
            <ElButton type="primary" @click="openEmployeeDialog">选择员工</ElButton>
          </div>
        </ElFormItem>
        <ElFormItem v-if="editingData.contractCount" label="合同次数">
          <ElInput :value="`第${editingData.contractCount}次`" disabled style="width: 100%" />
        </ElFormItem>
        <ElFormItem label="合同类型" required>
          <ElSelect v-model="editingData.contractType" placeholder="请选择合同类型" style="width: 100%">
            <ElOption
              v-for="item in contractTypeOptions"
              :key="item.dictValue"
              :label="item.dictLabel"
              :value="item.dictValue"
            />
          </ElSelect>
        </ElFormItem>
        <ElFormItem label="合同期限" required>
          <template v-if="isNoFixedTerm">
            <ElDatePicker
              v-model="editingData.startDate"
              type="date"
              placeholder="选择开始日期"
              style="width: 100%"
              value-format="YYYY-MM-DD"
            />
          </template>
          <template v-else>
            <ElDatePicker
              v-model="editingData.dateRange"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              style="width: 100%"
              value-format="YYYY-MM-DD"
            />
          </template>
        </ElFormItem>
        <ElFormItem label="签订日期">
          <ElDatePicker v-model="editingData.signDate" type="date" placeholder="选择日期" style="width: 100%" value-format="YYYY-MM-DD" />
        </ElFormItem>
        <ElFormItem label="合同图片">
          <div class="w-full">
            <div class="flex flex-wrap gap-8px mb-8px">
              <div v-for="(img, index) in contractImageList" :key="index" class="relative">
                <ElImage :src="getFileUrl(img)" fit="cover" class="w-100px h-100px rounded border" />
                <ElIcon class="absolute top-2px right-2px cursor-pointer bg-red-500 text-white rounded-full p-2px" @click="handleRemoveContractImage(index)">
                  <icon-ep-close />
                </ElIcon>
              </div>
              <ElUpload
                v-if="contractImageList.length < 9"
                class="contract-image-uploader"
                :show-file-list="false"
                :before-upload="beforeImageUpload"
                :http-request="({ file }) => handleContractImageUpload(file as File)"
              >
                <div class="w-100px h-100px rounded bg-gray-100 flex items-center justify-center border border-dashed border-gray-300 cursor-pointer hover:border-primary">
                  <ElIcon :size="24" class="text-gray-400">
                    <Plus />
                  </ElIcon>
                </div>
              </ElUpload>
            </div>
            <div class="text-xs text-gray-400">支持上传最多9张图片，每张不超过5MB</div>
          </div>
        </ElFormItem>
        <ElFormItem label="备注">
          <ElInput v-model="editingData.remark" type="textarea" :rows="3" placeholder="请输入备注" />
        </ElFormItem>
      </ElForm>
      <template #footer>
        <ElButton @click="drawerVisible = false">取消</ElButton>
        <ElButton type="primary" :loading="submitLoading" @click="handleSubmit">确定</ElButton>
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
        <ElTableColumn prop="phone" label="电话" width="120" />
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
  </div>
</template>

<style scoped>
.contract-dialog :deep(.el-dialog) {
  margin-bottom: 24px;
}

.contract-dialog :deep(.el-dialog__body) {
  max-height: calc(100vh - 220px);
  overflow-y: auto;
}
</style>
