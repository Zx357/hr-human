<script setup lang="tsx">
import { computed, onMounted, ref } from 'vue';
import { ElMessage } from 'element-plus';
import type { UploadProps } from 'element-plus';
import dayjs from 'dayjs';
import { Plus } from '@element-plus/icons-vue';
import type { TagType } from '@/constants/common';
import {
  type Contract,
  createContract,
  deleteContract,
  fetchContractPage,
  renewContract,
  updateContract
} from '@/service/api/contract';
import { uploadContractPhoto } from '@/service/api/file';
import { useDictOptions } from '@/composables/use-dict-options';
import { useTableColumnSetting } from '@/composables/use-table-column-setting';
import { downloadFile } from '@/utils/download';
import AuthImage from '@/components/business/auth-image.vue';
import EmployeePickerDialog from '@/components/common/employee-picker-dialog.vue';
import TableHeaderOperation from '@/components/advanced/table-header-operation.vue';
import { $t } from '@/locales';

defineOptions({ name: 'ContractManage' });

// 数据
const loading = ref(false);
const exporting = ref(false);
const data = ref<Contract[]>([]);
const total = ref(0);
const currentPage = ref(1);
const pageSize = ref(10);

// 表格列设置与密度（localStorage 持久化），配合 TableHeaderOperation 使用
const { columnChecks, density, isColumnVisible } = useTableColumnSetting('hr-contract', [
  { prop: 'contractNo', label: $t('hr.contract.contractNo'), checked: true, visible: true },
  { prop: 'companyName', label: $t('application.common.company'), checked: true, visible: true },
  { prop: 'employeeNo', label: $t('common.employeeNo'), checked: true, visible: true },
  { prop: 'employeeName', label: $t('common.employeeName'), checked: true, visible: true },
  { prop: 'contractType', label: $t('hr.contract.contractType'), checked: true, visible: true },
  { prop: 'startDate', label: $t('common.startDate'), checked: true, visible: true },
  { prop: 'endDate', label: $t('common.endDate'), checked: true, visible: true },
  { prop: 'signDate', label: $t('hr.contract.signDate'), checked: true, visible: true },
  { prop: 'status', label: $t('common.status'), checked: true, visible: true }
]);

// 员工选择弹窗相关
const employeeDialogVisible = ref(false);
const selectedEmployee = ref<Api.Hr.Employee | null>(null);

// 字典数据
const { options: contractTypeOptions, getDictLabel: getContractTypeLabel } = useDictOptions('contract_type');

const drawerVisible = ref(false);
const operateType = ref<'add' | 'edit' | 'view'>('add');
/** 查看模式：表单只读、隐藏保存/选择员工/上传操作 */
const isViewMode = computed(() => operateType.value === 'view');
const editingData = ref<
  Contract & { dateRange?: [string, string]; companyName?: string; employeeName?: string; employeeNo?: string }
>({
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
  } catch {
    // 请求层已统一弹错
  } finally {
    loading.value = false;
  }
}
onMounted(() => {
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
  employeeDisplayName.value = editingData.value.employeeName
    ? `${editingData.value.employeeName} (${editingData.value.employeeNo})`
    : '';
  // 解析合同图片
  contractImageList.value = row.contractImages ? row.contractImages.split(',').filter(img => img) : [];
  drawerVisible.value = true;
}

async function handleDelete(id: number) {
  try {
    await deleteContract(id);
    ElMessage.success($t('common.deleteSuccess'));
    loadData();
  } catch {
    // 请求层已统一弹错
  }
}

// ===== 合同续签 =====
const renewVisible = ref(false);
const renewSubmitting = ref(false);
const renewTarget = ref<Contract | null>(null);
const renewForm = ref<{
  range?: [string, string];
  signDate?: string;
  salary?: number;
}>({});

function handleRenew(row: Contract) {
  renewTarget.value = row;
  // 默认期限：旧合同结束次日（已过期则从今天）起 3 年
  const oldEnd = row.endDate ? dayjs(row.endDate) : dayjs();
  const start = oldEnd.add(1, 'day').isAfter(dayjs()) ? oldEnd.add(1, 'day') : dayjs();
  renewForm.value = {
    range: [start.format('YYYY-MM-DD'), start.add(3, 'year').format('YYYY-MM-DD')],
    signDate: dayjs().format('YYYY-MM-DD'),
    salary: row.salary
  };
  renewVisible.value = true;
}

async function submitRenew() {
  const target = renewTarget.value;
  if (!target?.id) return;
  const range = renewForm.value.range;
  if (!range || !range[0] || !range[1]) {
    ElMessage.warning($t('hr.contract.pleaseSelectStartDate'));
    return;
  }
  renewSubmitting.value = true;
  try {
    await renewContract({
      id: target.id,
      startDate: range[0],
      endDate: range[1],
      signDate: renewForm.value.signDate,
      salary: renewForm.value.salary
    });
    ElMessage.success($t('common.updateSuccess'));
    renewVisible.value = false;
    loadData();
  } catch {
    // 请求层已统一弹错
  } finally {
    renewSubmitting.value = false;
  }
}

function handleView(row: Contract) {
  operateType.value = 'view';
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

/** 导出合同列表 */
async function handleExport() {
  exporting.value = true;
  try {
    // clearable 的 ElSelect 清空后值为 ''，跳过空串
    const params = searchParams.value;
    await downloadFile(
      '/hr/contract/export',
      $t('hr.contract.contractListXlsx', { date: dayjs().format('YYYYMMDD') }),
      {
        employeeName: params.employeeName || undefined,
        employeeNo: params.employeeNo || undefined,
        contractNo: params.contractNo || undefined,
        contractType: params.contractType || undefined,
        status: params.status === undefined || (params.status as unknown) === '' ? undefined : params.status
      }
    );
    ElMessage.success($t('attendance.common.exportSuccessful'));
  } catch {
    // downloadFile 内部已提示具体错误
  } finally {
    exporting.value = false;
  }
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
} // 员工弹窗重置// 员工弹窗分页// 选择员工
async function handleConfirmEmployee(selected: Api.Hr.Employee[]) {
  const row = selected[0];
  if (!row?.id) return;

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
}

// 自动计算合同次数
async function calculateContractCount(employeeId: number) {
  try {
    // 仅需要 total 计数，pageSize 用 1 拉取，避免为统计一次合同数全量拉取该员工合同列表
    const res = await fetchContractPage({
      pageNum: 1,
      pageSize: 1,
      employeeId
    });
    if (res.data) {
      // 合同次数 = 该员工现有合同数 + 1
      editingData.value.contractCount = (res.data.total || 0) + 1;
    }
  } catch {
    editingData.value.contractCount = 1;
  }
}

async function handleSubmit() {
  if (!editingData.value.contractNo || !editingData.value.employeeId) {
    ElMessage.warning($t('common.pleaseFillRequired'));
    return;
  }
  // 无固定期限只需要开始日期，固定期限需要日期范围
  if (isNoFixedTerm.value) {
    if (!editingData.value.startDate) {
      ElMessage.warning($t('hr.contract.pleaseSelectStartDate'));
      return;
    }
  } else if (!editingData.value.dateRange || editingData.value.dateRange.length !== 2) {
    ElMessage.warning($t('hr.contract.pleaseSelectContractTerm'));
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
      ElMessage.success($t('common.addSuccess'));
    } else {
      await updateContract(submitData);
      ElMessage.success($t('common.updateSuccess'));
    }
    drawerVisible.value = false;
    loadData();
  } catch {
    // 请求层已统一弹错
  } finally {
    submitLoading.value = false;
  }
}

// 图片上传相关
const beforeImageUpload: UploadProps['beforeUpload'] = rawFile => {
  const isImage = rawFile.type.startsWith('image/');
  const isLt5M = rawFile.size / 1024 / 1024 < 5;
  if (!isImage) {
    ElMessage.error($t('hr.contract.onlyImageFilesCanBeUploaded'));
    return false;
  }
  if (!isLt5M) {
    ElMessage.error($t('hr.contract.imageSizeCannotExceed5mb'));
    return false;
  }
  return true;
};

async function handleContractImageUpload(file: File): Promise<boolean> {
  try {
    if (!editingData.value.employeeNo) {
      ElMessage.warning($t('hr.contract.pleaseSelectAnEmployeeFirst'));
      return false;
    }
    const res = await uploadContractPhoto(file, editingData.value.employeeNo);
    if (res.data) {
      contractImageList.value.push(res.data);
      ElMessage.success($t('hr.contract.uploadedSuccessfully'));
      return true;
    }
    return false;
  } catch {
    // 请求层已统一弹错
    return false;
  }
}

function handleRemoveContractImage(index: number) {
  contractImageList.value.splice(index, 1);
}

// 合同状态（1-履行中 2-即将到期 3-已到期 4-已解除），语义独立，不并入 constants/common 的 enableStatusMap
const statusMap: Record<number, { label: string; type: TagType }> = {
  1: { label: $t('hr.contract.inEffect'), type: 'success' },
  2: { label: $t('hr.contract.expiringSoon'), type: 'warning' },
  3: { label: $t('hr.contract.expired'), type: 'info' },
  4: { label: $t('hr.contract.terminated'), type: 'danger' }
};
</script>

<template>
  <div class="min-h-500px flex-col-stretch gap-16px overflow-hidden lt-sm:overflow-auto">
    <!-- 搜索区域 -->
    <ElCard>
      <ElForm inline :model="searchParams">
        <ElFormItem :label="$t('common.employeeName')">
          <ElInput
            v-model="searchParams.employeeName"
            :placeholder="$t('common.pleaseInputEmployeeName')"
            clearable
            @keyup.enter="handleSearch"
          />
        </ElFormItem>
        <ElFormItem :label="$t('common.employeeNo')">
          <ElInput
            v-model="searchParams.employeeNo"
            :placeholder="$t('common.pleaseInputEmployeeNo')"
            clearable
            @keyup.enter="handleSearch"
          />
        </ElFormItem>
        <ElFormItem :label="$t('hr.contract.contractNo')">
          <ElInput
            v-model="searchParams.contractNo"
            :placeholder="$t('hr.contract.pleaseEnterContractNo')"
            clearable
            @keyup.enter="handleSearch"
          />
        </ElFormItem>
        <ElFormItem :label="$t('hr.contract.contractType')">
          <ElSelect
            v-model="searchParams.contractType"
            :placeholder="$t('hr.contract.pleaseSelectContractType')"
            clearable
            style="width: 180px"
          >
            <ElOption
              v-for="item in contractTypeOptions"
              :key="item.dictValue"
              :label="getContractTypeLabel(item.dictValue)"
              :value="item.dictValue"
            />
          </ElSelect>
        </ElFormItem>
        <ElFormItem :label="$t('common.status')">
          <ElSelect
            v-model="searchParams.status"
            :placeholder="$t('common.pleaseSelectStatus')"
            clearable
            style="width: 150px"
          >
            <ElOption :label="$t('hr.contract.inEffect')" :value="1" />
            <ElOption :label="$t('hr.contract.expiringSoon')" :value="2" />
            <ElOption :label="$t('hr.contract.expired')" :value="3" />
            <ElOption :label="$t('hr.contract.terminated')" :value="4" />
          </ElSelect>
        </ElFormItem>
        <ElFormItem>
          <ElButton type="primary" @click="handleSearch">
            <template #icon><icon-ep-search /></template>
            {{ $t('common.search') }}
          </ElButton>
          <ElButton @click="handleReset">
            <template #icon><icon-ep-refresh /></template>
            {{ $t('common.reset') }}
          </ElButton>
        </ElFormItem>
      </ElForm>
    </ElCard>

    <!-- 表格区域 -->
    <ElCard class="flex-1">
      <template #header>
        <div class="flex flex-wrap items-center justify-between gap-12px">
          <span>{{ $t('hr.contract.contractList') }}</span>
          <div class="flex flex-wrap items-center gap-8px">
            <ElButton v-permission="'hr:contract:export'" :loading="exporting" @click="handleExport">
              <template #icon><icon-ep-download /></template>
              {{ $t('common.export') }}
            </ElButton>
            <ElButton v-permission="'hr:contract:add'" type="primary" @click="handleAdd">
              <template #icon><icon-ep-plus /></template>
              {{ $t('hr.contract.newContract') }}
            </ElButton>
            <TableHeaderOperation
              v-model:columns="columnChecks"
              v-model:density="density"
              :loading="loading"
              @refresh="loadData"
            >
              <template #default />
            </TableHeaderOperation>
          </div>
        </div>
      </template>

      <ElTable v-loading="loading" :data="data" border stripe :size="density">
        <ElTableColumn type="index" :label="$t('common.index2')" width="60" align="center" />
        <ElTableColumn
          v-if="isColumnVisible('contractNo')"
          prop="contractNo"
          :label="$t('hr.contract.contractNo')"
          min-width="130"
        />
        <ElTableColumn
          v-if="isColumnVisible('companyName')"
          prop="companyName"
          :label="$t('application.common.company')"
          min-width="120"
        />
        <ElTableColumn
          v-if="isColumnVisible('employeeNo')"
          prop="employeeNo"
          :label="$t('common.employeeNo')"
          min-width="100"
        />
        <ElTableColumn
          v-if="isColumnVisible('employeeName')"
          prop="employeeName"
          :label="$t('common.employeeName')"
          min-width="100"
        />
        <ElTableColumn
          v-if="isColumnVisible('contractType')"
          prop="contractType"
          :label="$t('hr.contract.contractType')"
          min-width="100"
          align="center"
        >
          <template #default="{ row }">
            {{ getContractTypeLabel(row.contractType) }}
          </template>
        </ElTableColumn>
        <ElTableColumn
          v-if="isColumnVisible('startDate')"
          prop="startDate"
          :label="$t('common.startDate')"
          min-width="110"
        />
        <ElTableColumn v-if="isColumnVisible('endDate')" prop="endDate" :label="$t('common.endDate')" min-width="110" />
        <ElTableColumn
          v-if="isColumnVisible('signDate')"
          prop="signDate"
          :label="$t('hr.contract.signDate')"
          min-width="110"
        />
        <ElTableColumn
          v-if="isColumnVisible('status')"
          prop="status"
          :label="$t('common.status')"
          min-width="90"
          align="center"
        >
          <template #default="{ row }">
            <ElTag :type="statusMap[row.status]?.type">
              {{ statusMap[row.status]?.label }}
            </ElTag>
          </template>
        </ElTableColumn>
        <ElTableColumn :label="$t('common.action')" width="230" align="center" fixed="right">
          <template #default="{ row }">
            <ElButton type="primary" link size="small" @click="handleView(row)">{{ $t('common.view') }}</ElButton>
            <ElButton v-permission="'hr:contract:edit'" type="primary" link size="small" @click="handleEdit(row)">
              {{ $t('common.edit') }}
            </ElButton>
            <ElButton
              v-if="row.status !== 4"
              v-permission="'hr:contract:add'"
              type="warning"
              link
              size="small"
              @click="handleRenew(row)"
            >
              {{ $t('hr.contract.renew') }}
            </ElButton>
            <ElPopconfirm
              :title="$t('hr.contract.areYouSureYouWantToDeleteThisContract')"
              @confirm="handleDelete(row.id)"
            >
              <template #reference>
                <ElButton v-permission="'hr:contract:delete'" type="danger" link size="small">
                  {{ $t('common.delete') }}
                </ElButton>
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

    <!-- 新增/编辑/查看弹窗 -->
    <ElDialog
      v-model="drawerVisible"
      :title="
        operateType === 'add'
          ? $t('hr.contract.newContract')
          : isViewMode
            ? $t('hr.contract.viewContract')
            : $t('hr.contract.editContract')
      "
      width="600px"
      top="24px"
      destroy-on-close
      append-to-body
      :close-on-click-modal="false"
      :close-on-press-escape="false"
      class="contract-dialog"
      @opened="handleContractDialogOpened"
    >
      <ElForm label-width="100px" :model="editingData" :disabled="isViewMode">
        <ElFormItem :label="$t('hr.contract.contractNo')" required>
          <ElInput v-model="editingData.contractNo" :placeholder="$t('hr.contract.pleaseEnterContractNo')" />
        </ElFormItem>
        <ElFormItem :label="$t('common.employee')" required>
          <div class="w-full flex gap-8px">
            <ElInput
              v-model="employeeDisplayName"
              disabled
              :placeholder="$t('common.pleaseSelectEmployees')"
              class="flex-1"
            />
            <ElButton v-if="!isViewMode" type="primary" @click="employeeDialogVisible = true">
              {{ $t('common.selectEmployees') }}
            </ElButton>
          </div>
        </ElFormItem>
        <ElFormItem v-if="editingData.contractCount" :label="$t('hr.contract.contractCount')">
          <ElInput :value="$t('hr.contract.no', { count: editingData.contractCount })" disabled style="width: 100%" />
        </ElFormItem>
        <ElFormItem :label="$t('hr.contract.contractType')" required>
          <ElSelect
            v-model="editingData.contractType"
            :placeholder="$t('hr.contract.pleaseSelectContractType')"
            style="width: 100%"
          >
            <ElOption
              v-for="item in contractTypeOptions"
              :key="item.dictValue"
              :label="getContractTypeLabel(item.dictValue)"
              :value="item.dictValue"
            />
          </ElSelect>
        </ElFormItem>
        <ElFormItem :label="$t('hr.contract.contractTerm')" required>
          <template v-if="isNoFixedTerm">
            <ElDatePicker
              v-model="editingData.startDate"
              type="date"
              :placeholder="$t('hr.contract.selectStartDate')"
              style="width: 100%"
              value-format="YYYY-MM-DD"
            />
          </template>
          <template v-else>
            <ElDatePicker
              v-model="editingData.dateRange"
              type="daterange"
              :range-separator="$t('common.to')"
              :start-placeholder="$t('common.startDate')"
              :end-placeholder="$t('common.endDate')"
              style="width: 100%"
              value-format="YYYY-MM-DD"
            />
          </template>
        </ElFormItem>
        <ElFormItem :label="$t('hr.contract.signDate')">
          <ElDatePicker
            v-model="editingData.signDate"
            type="date"
            :placeholder="$t('common.selectDate')"
            style="width: 100%"
            value-format="YYYY-MM-DD"
          />
        </ElFormItem>
        <ElFormItem :label="$t('hr.contract.contractImage')">
          <div class="w-full">
            <div class="mb-8px flex flex-wrap gap-8px">
              <div v-for="(img, index) in contractImageList" :key="index" class="relative">
                <AuthImage :url="img" fit="cover" preview class="h-100px w-100px border rounded" />
                <ElIcon
                  v-if="!isViewMode"
                  class="absolute right-2px top-2px cursor-pointer rounded-full bg-red-500 p-2px text-white"
                  @click="handleRemoveContractImage(index)"
                >
                  <icon-ep-close />
                </ElIcon>
              </div>
              <ElUpload
                v-if="!isViewMode && contractImageList.length < 9"
                class="contract-image-uploader"
                :show-file-list="false"
                :before-upload="beforeImageUpload"
                :http-request="({ file }) => handleContractImageUpload(file as File)"
              >
                <div
                  class="h-100px w-100px flex cursor-pointer items-center justify-center border border-[var(--el-border-color)] rounded border-dashed bg-[var(--el-fill-color-light)] hover:border-primary"
                >
                  <ElIcon :size="24" class="text-[var(--el-text-color-placeholder)]">
                    <Plus />
                  </ElIcon>
                </div>
              </ElUpload>
            </div>
            <div class="text-xs text-gray-400">{{ $t('hr.contract.upTo9ImagesCanBeUploadedEachWithin5mb') }}</div>
          </div>
        </ElFormItem>
        <ElFormItem :label="$t('common.remark')">
          <ElInput
            v-model="editingData.remark"
            type="textarea"
            :rows="3"
            :placeholder="$t('hr.contract.pleaseEnterRemark')"
          />
        </ElFormItem>
      </ElForm>
      <template #footer>
        <ElButton @click="drawerVisible = false">{{ isViewMode ? $t('common.close') : $t('common.cancel') }}</ElButton>
        <ElButton v-if="!isViewMode" type="primary" :loading="submitLoading" @click="handleSubmit">
          {{ $t('common.ok') }}
        </ElButton>
      </template>
    </ElDialog>

    <!-- 合同续签弹窗：旧合同置为已续签，生成新合同 -->
    <ElDialog
      v-model="renewVisible"
      :title="$t('hr.contract.renewTitle')"
      width="480px"
      destroy-on-close
      append-to-body
      :close-on-click-modal="false"
    >
      <ElForm label-width="100px">
        <ElFormItem :label="$t('hr.contract.renewOriginal')">
          <span>{{ renewTarget?.contractNo }}（{{ renewTarget?.employeeName }}）</span>
        </ElFormItem>
        <ElFormItem :label="$t('hr.contract.renewTerm')" required>
          <ElDatePicker
            v-model="renewForm.range"
            type="daterange"
            value-format="YYYY-MM-DD"
            :start-placeholder="$t('common.startDate')"
            :end-placeholder="$t('common.endDate')"
            style="width: 100%"
          />
        </ElFormItem>
        <ElFormItem :label="$t('hr.contract.signDate')">
          <ElDatePicker
            v-model="renewForm.signDate"
            type="date"
            value-format="YYYY-MM-DD"
            :placeholder="$t('hr.contract.signDate')"
            style="width: 100%"
          />
        </ElFormItem>
        <ElFormItem :label="$t('hr.contract.renewSalary')">
          <ElInputNumber v-model="renewForm.salary" :min="0" :controls="false" style="width: 100%" />
        </ElFormItem>
      </ElForm>
      <template #footer>
        <ElButton @click="renewVisible = false">{{ $t('common.cancel') }}</ElButton>
        <ElButton type="primary" :loading="renewSubmitting" @click="submitRenew">
          {{ $t('hr.contract.renew') }}
        </ElButton>
      </template>
    </ElDialog>

    <EmployeePickerDialog v-model="employeeDialogVisible" @confirm="handleConfirmEmployee" />
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
