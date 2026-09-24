<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import dayjs from 'dayjs';
import {
  type PayrollBatch,
  type PayrollItem,
  type Payslip,
  computeBatch,
  confirmBatch,
  createPayrollBatch,
  deleteBatch,
  fetchBatchPage,
  fetchPayslipDetail,
  fetchPayslipPage,
  publishBatch,
  unlockBatch,
  updateManualItem
} from '@/service/api/salary';
import { fetchCompanyList } from '@/service/api/organization';
import { downloadFile } from '@/utils/download';
import { $t } from '@/locales';

defineOptions({ name: 'SalaryPayroll' });

/** 批次状态：0-核算中 1-已核算 2-已确认 3-已发放 */
const STATUS_COMPUTING = 0;
const STATUS_COMPUTED = 1;
const STATUS_CONFIRMED = 2;
const STATUS_PUBLISHED = 3;

const loading = ref(false);
const batches = ref<PayrollBatch[]>([]);
const total = ref(0);
const currentPage = ref(1);
const pageSize = ref(10);
const companies = ref<{ id: number; unitName?: string; companyName?: string }[]>([]);

const searchParams = ref({
  yearMonth: undefined as string | undefined,
  companyId: undefined as number | undefined
});

function statusType(status: number) {
  if (status === STATUS_PUBLISHED) return 'success';
  if (status === STATUS_CONFIRMED) return 'primary';
  if (status === STATUS_COMPUTED) return 'warning';
  return 'info';
}

function statusLabel(status: number) {
  const labels: Record<number, string> = {
    0: $t('salary.payroll.statusComputing'),
    1: $t('salary.payroll.statusComputed'),
    2: $t('salary.payroll.statusConfirmed'),
    3: $t('salary.payroll.statusPublished')
  };
  return labels[status] || status;
}

async function loadBatches() {
  loading.value = true;
  try {
    const res = await fetchBatchPage({
      pageNum: currentPage.value,
      pageSize: pageSize.value,
      yearMonth: searchParams.value.yearMonth,
      companyId: searchParams.value.companyId
    });
    if (res.data) {
      batches.value = res.data.records || [];
      total.value = res.data.total || 0;
    }
  } catch {
    // 请求层已统一弹错
  } finally {
    loading.value = false;
  }
}

async function loadCompanies() {
  const res = await fetchCompanyList();
  companies.value = res.data || [];
}

onMounted(() => {
  loadCompanies();
  loadBatches();
});

// ===== 新建批次 =====
const createDialogVisible = ref(false);
const createSubmitting = ref(false);
const createForm = ref<{ yearMonth: string; companyId?: number; remark: string }>({
  yearMonth: dayjs().subtract(1, 'month').format('YYYY-MM'),
  companyId: undefined,
  remark: ''
});

function openCreateDialog() {
  createForm.value = { yearMonth: dayjs().subtract(1, 'month').format('YYYY-MM'), companyId: undefined, remark: '' };
  createDialogVisible.value = true;
}

async function submitCreate() {
  if (!createForm.value.companyId) {
    ElMessage.warning($t('common.pleaseSelectCompany'));
    return;
  }
  createSubmitting.value = true;
  try {
    await createPayrollBatch({
      yearMonth: createForm.value.yearMonth,
      companyId: createForm.value.companyId,
      remark: createForm.value.remark
    });
    ElMessage.success($t('salary.payroll.createSuccess'));
    createDialogVisible.value = false;
    loadBatches();
  } catch {
    // 请求层已统一弹错
  } finally {
    createSubmitting.value = false;
  }
}

// ===== 批次操作 =====
const operating = ref(false);
const exporting = ref(false);

async function withOperating(action: () => Promise<unknown>, successMsg?: string) {
  operating.value = true;
  try {
    await action();
    if (successMsg) ElMessage.success(successMsg);
    loadBatches();
  } catch {
    // 请求层已统一弹错
  } finally {
    operating.value = false;
  }
}

function handleCompute(row: PayrollBatch) {
  ElMessageBox.confirm($t('salary.payroll.computeConfirm'), $t('common.tip')).then(() =>
    withOperating(() => computeBatch(row.id!), $t('salary.payroll.computeSuccess'))
  );
}

function handleConfirm(row: PayrollBatch) {
  ElMessageBox.confirm($t('salary.payroll.confirmTip'), $t('common.tip')).then(() =>
    withOperating(() => confirmBatch(row.id!), $t('common.updateSuccess'))
  );
}

function handlePublish(row: PayrollBatch) {
  ElMessageBox.confirm($t('salary.payroll.publishTip'), $t('common.tip')).then(() =>
    withOperating(() => publishBatch(row.id!), $t('salary.payroll.publishSuccess'))
  );
}

function handleUnlock(row: PayrollBatch) {
  ElMessageBox.confirm($t('salary.payroll.unlockTip'), $t('common.tip')).then(() =>
    withOperating(() => unlockBatch(row.id!), $t('common.updateSuccess'))
  );
}

async function handleExport(row: PayrollBatch) {
  exporting.value = true;
  try {
    await downloadFile(
      `/salary/payroll/${row.id}/export`,
      `工资表-${row.yearMonth}.xlsx`
    );
    ElMessage.success($t('attendance.common.exportSuccessful'));
  } catch {
    // downloadFile 内部已提示具体错误
  } finally {
    exporting.value = false;
  }
}

function handleDelete(row: PayrollBatch) {
  ElMessageBox.confirm($t('salary.payroll.deleteTip'), $t('common.tip')).then(() =>
    withOperating(() => {
      return deleteBatch(row.id!);
    }, $t('common.deleteSuccess'))
  );
}

// ===== 工资条抽屉 =====
const payslipDrawerVisible = ref(false);
const payslipLoading = ref(false);
const currentBatch = ref<PayrollBatch | null>(null);
const payslips = ref<Payslip[]>([]);
const payslipTotal = ref(0);
const payslipPage = ref(1);
const payslipPageSize = ref(20);
const payslipSearch = ref({ employeeName: '', employeeNo: '' });

async function openPayslips(row: PayrollBatch) {
  currentBatch.value = row;
  payslipPage.value = 1;
  payslipSearch.value = { employeeName: '', employeeNo: '' };
  payslipDrawerVisible.value = true;
  await loadPayslips();
}

async function loadPayslips() {
  if (!currentBatch.value?.id) return;
  payslipLoading.value = true;
  try {
    const res = await fetchPayslipPage(currentBatch.value.id, {
      pageNum: payslipPage.value,
      pageSize: payslipPageSize.value,
      employeeName: payslipSearch.value.employeeName || undefined,
      employeeNo: payslipSearch.value.employeeNo || undefined
    });
    if (res.data) {
      payslips.value = res.data.records || [];
      payslipTotal.value = res.data.total || 0;
    }
  } catch {
    // 请求层已统一弹错
  } finally {
    payslipLoading.value = false;
  }
}

// ===== 工资条明细抽屉 =====
const detailDrawerVisible = ref(false);
const detailYearMonth = ref('');
const detailPayslip = ref<Payslip | null>(null);
const incomeItems = ref<PayrollItem[]>([]);
const deductionItems = ref<PayrollItem[]>([]);

async function openDetail(row: Payslip) {
  const res = await fetchPayslipDetail(row.id!);
  detailPayslip.value = res.data?.payslip || null;
  detailYearMonth.value = res.data?.yearMonth || '';
  const items = res.data?.items || [];
  incomeItems.value = items.filter(item => item.direction === 1);
  deductionItems.value = items.filter(item => item.direction === 2);
  detailDrawerVisible.value = true;
}

// ===== 手工项金额编辑 =====
const editingItemId = ref<number | null>(null);
const editingAmount = ref<number | null>(null);

function canEditItem(item: PayrollItem) {
  return item.valueType === 4 && (currentBatch.value?.status ?? 99) < STATUS_CONFIRMED;
}

function startEditItem(item: PayrollItem) {
  editingItemId.value = item.id!;
  editingAmount.value = item.amount;
}

async function saveItemAmount() {
  if (editingItemId.value == null || editingAmount.value == null) return;
  try {
    await updateManualItem(editingItemId.value, editingAmount.value);
    ElMessage.success($t('common.updateSuccess'));
    editingItemId.value = null;
    // 重拉明细与工资条（汇总已变）
    if (detailPayslip.value?.id) await openDetail(detailPayslip.value);
    await loadPayslips();
  } catch {
    // 请求层已统一弹错
  }
}

function formatAmount(value?: number) {
  return Number(value || 0).toFixed(2);
}
</script>

<template>
  <div class="min-h-500px flex-col-stretch gap-16px overflow-hidden lt-sm:overflow-auto">
    <ElCard>
      <ElForm inline>
        <ElFormItem :label="$t('common.month')">
          <ElDatePicker
            v-model="searchParams.yearMonth"
            type="month"
            :placeholder="$t('attendance.monthly.selectMonth')"
            value-format="YYYY-MM"
            clearable
            style="width: 140px"
          />
        </ElFormItem>
        <ElFormItem :label="$t('common.company')">
          <ElSelect
            v-model="searchParams.companyId"
            :placeholder="$t('common.pleaseSelectCompany')"
            clearable
            style="width: 170px"
          >
            <ElOption v-for="c in companies" :key="c.id" :label="c.unitName || c.companyName" :value="c.id" />
          </ElSelect>
        </ElFormItem>
        <ElFormItem>
          <ElButton type="primary" @click="loadBatches">
            <template #icon><icon-ep-search /></template>
            {{ $t('common.search') }}
          </ElButton>
        </ElFormItem>
      </ElForm>
    </ElCard>

    <ElCard class="flex-1">
      <template #header>
        <div class="flex flex-wrap items-center justify-between gap-12px">
          <span>{{ $t('salary.payroll.title') }}</span>
          <ElButton v-permission="'sal:payroll:manage'" type="primary" @click="openCreateDialog">
            <template #icon><icon-ep-plus /></template>
            {{ $t('salary.payroll.createBatch') }}
          </ElButton>
        </div>
      </template>

      <ElTable v-loading="loading" :data="batches" size="small" border>
        <ElTableColumn prop="yearMonth" :label="$t('common.month')" width="100" align="center" />
        <ElTableColumn prop="companyName" :label="$t('common.company')" min-width="140" show-overflow-tooltip />
        <ElTableColumn :label="$t('common.status')" width="100" align="center">
          <template #default="{ row }">
            <ElTag :type="statusType(row.status)">{{ statusLabel(row.status) }}</ElTag>
          </template>
        </ElTableColumn>
        <ElTableColumn prop="employeeCount" :label="$t('salary.payroll.employeeCount')" width="100" align="center" />
        <ElTableColumn prop="totalGross" :label="$t('salary.payroll.totalGross')" width="130" align="right">
          <template #default="{ row }">{{ formatAmount(row.totalGross) }}</template>
        </ElTableColumn>
        <ElTableColumn prop="totalNet" :label="$t('salary.payroll.totalNet')" width="130" align="right">
          <template #default="{ row }">{{ formatAmount(row.totalNet) }}</template>
        </ElTableColumn>
        <ElTableColumn prop="remark" :label="$t('common.remark')" min-width="120" show-overflow-tooltip />
        <ElTableColumn :label="$t('common.action')" width="330" align="center" fixed="right">
          <template #default="{ row }">
            <ElButton type="primary" link size="small" @click="openPayslips(row)">
              {{ $t('salary.payroll.viewPayslips') }}
            </ElButton>
            <ElButton
              v-if="row.status < 2"
              v-permission="'sal:payroll:manage'"
              type="warning"
              link
              size="small"
              @click="handleCompute(row)"
            >
              {{ $t('salary.payroll.compute') }}
            </ElButton>
            <ElButton
              v-if="row.status === 1"
              v-permission="'sal:payroll:manage'"
              type="primary"
              link
              size="small"
              @click="handleConfirm(row)"
            >
              {{ $t('salary.payroll.confirm') }}
            </ElButton>
            <ElButton
              v-if="row.status === 2"
              v-permission="'sal:payroll:manage'"
              type="success"
              link
              size="small"
              @click="handlePublish(row)"
            >
              {{ $t('salary.payroll.publish') }}
            </ElButton>
            <ElButton
              v-if="row.status === 2"
              v-permission="'sal:payroll:manage'"
              type="danger"
              link
              size="small"
              @click="handleUnlock(row)"
            >
              {{ $t('salary.payroll.unlock') }}
            </ElButton>
            <ElButton
              v-if="row.status < 2"
              v-permission="'sal:payroll:manage'"
              type="danger"
              link
              size="small"
              @click="handleDelete(row)"
            >
              {{ $t('common.delete') }}
            </ElButton>
            <ElButton
              v-permission="'sal:payroll:export'"
              link
              size="small"
              :loading="exporting"
              @click="handleExport(row)"
            >
              {{ $t('common.export') }}
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
          @current-change="loadBatches"
          @size-change="
            () => {
              currentPage = 1;
              loadBatches();
            }
          "
        />
      </div>
    </ElCard>

    <!-- 新建批次 -->
    <ElDialog
      v-model="createDialogVisible"
      :title="$t('salary.payroll.createBatch')"
      width="460px"
      destroy-on-close
      :close-on-click-modal="false"
    >
      <ElForm label-width="100px">
        <ElFormItem :label="$t('common.month')" required>
          <ElDatePicker
            v-model="createForm.yearMonth"
            type="month"
            value-format="YYYY-MM"
            :clearable="false"
            style="width: 100%"
          />
        </ElFormItem>
        <ElFormItem :label="$t('common.company')" required>
          <ElSelect v-model="createForm.companyId" :placeholder="$t('common.pleaseSelectCompany')" style="width: 100%">
            <ElOption v-for="c in companies" :key="c.id" :label="c.unitName || c.companyName" :value="c.id" />
          </ElSelect>
        </ElFormItem>
        <ElFormItem :label="$t('common.remark')">
          <ElInput v-model="createForm.remark" type="textarea" :rows="2" />
        </ElFormItem>
      </ElForm>
      <template #footer>
        <ElButton @click="createDialogVisible = false">{{ $t('common.cancel') }}</ElButton>
        <ElButton type="primary" :loading="createSubmitting" @click="submitCreate">
          {{ $t('salary.payroll.createAndCompute') }}
        </ElButton>
      </template>
    </ElDialog>

    <!-- 工资条列表抽屉 -->
    <ElDrawer
      v-model="payslipDrawerVisible"
      :title="`${currentBatch?.yearMonth} ${currentBatch?.companyName || ''} ${$t('salary.payroll.payslipList')}`"
      size="72%"
    >
      <ElForm inline class="mb-8px" @submit.prevent>
        <ElFormItem :label="$t('common.employeeNo')">
          <ElInput
            v-model="payslipSearch.employeeNo"
            :placeholder="$t('common.employeeNo')"
            clearable
            style="width: 110px"
            @keyup.enter="loadPayslips"
          />
        </ElFormItem>
        <ElFormItem :label="$t('common.name')">
          <ElInput
            v-model="payslipSearch.employeeName"
            :placeholder="$t('common.name')"
            clearable
            style="width: 110px"
            @keyup.enter="loadPayslips"
          />
        </ElFormItem>
        <ElFormItem>
          <ElButton type="primary" @click="loadPayslips">
            <template #icon><icon-ep-search /></template>
            {{ $t('common.search') }}
          </ElButton>
        </ElFormItem>
      </ElForm>
      <ElTable v-loading="payslipLoading" :data="payslips" size="small" border>
        <ElTableColumn prop="employeeNo" :label="$t('common.employeeNo')" width="110" />
        <ElTableColumn prop="employeeName" :label="$t('common.employeeName')" width="100" />
        <ElTableColumn prop="grossPay" :label="$t('salary.payroll.grossPay')" width="120" align="right">
          <template #default="{ row }">{{ formatAmount(row.grossPay) }}</template>
        </ElTableColumn>
        <ElTableColumn prop="totalDeduction" :label="$t('salary.payroll.totalDeduction')" width="120" align="right">
          <template #default="{ row }">{{ formatAmount(row.totalDeduction) }}</template>
        </ElTableColumn>
        <ElTableColumn prop="netPay" :label="$t('salary.payroll.netPay')" width="120" align="right">
          <template #default="{ row }">
            <span class="font-bold">{{ formatAmount(row.netPay) }}</span>
          </template>
        </ElTableColumn>
        <ElTableColumn :label="$t('common.action')" width="100" align="center">
          <template #default="{ row }">
            <ElButton type="primary" link size="small" @click="openDetail(row)">
              {{ $t('salary.payroll.viewDetail') }}
            </ElButton>
          </template>
        </ElTableColumn>
      </ElTable>
      <div class="mt-16px flex justify-end">
        <ElPagination
          v-model:current-page="payslipPage"
          v-model:page-size="payslipPageSize"
          :total="payslipTotal"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next"
          @current-change="loadPayslips"
          @size-change="
            () => {
              payslipPage = 1;
              loadPayslips();
            }
          "
        />
      </div>
    </ElDrawer>

    <!-- 工资条明细抽屉 -->
    <ElDrawer v-model="detailDrawerVisible" :title="$t('salary.payroll.payslipDetail')" size="440px" append-to-body>
      <template v-if="detailPayslip">
        <div class="mb-16px rounded-8px bg-gray-50 p-16px dark:bg-gray-800">
          <div class="text-14px text-gray-500">{{ detailYearMonth }} · {{ detailPayslip.employeeName }}</div>
          <div class="mt-8px text-28px font-bold" style="color: #22a873">
            ¥{{ formatAmount(detailPayslip.netPay) }}
          </div>
          <div class="mt-4px text-12px text-gray-400">
            {{ $t('salary.payroll.grossPay') }} {{ formatAmount(detailPayslip.grossPay) }} ·
            {{ $t('salary.payroll.totalDeduction') }} {{ formatAmount(detailPayslip.totalDeduction) }}
          </div>
        </div>

        <div class="mb-8px text-14px font-bold">{{ $t('salary.common.income') }}</div>
        <div v-for="item in incomeItems" :key="item.id" class="flex items-center justify-between border-b border-gray-100 py-8px dark:border-gray-700">
          <div>
            <div class="text-14px">{{ item.itemName }}</div>
            <div class="text-12px text-gray-400">{{ item.source }}</div>
          </div>
          <div class="flex items-center gap-4px">
            <template v-if="canEditItem(item) && editingItemId === item.id">
              <ElInputNumber v-model="editingAmount" :min="0" :precision="2" :controls="false" size="small" style="width: 110px" />
              <ElButton type="primary" link size="small" @click="saveItemAmount">{{ $t('common.save') }}</ElButton>
            </template>
            <template v-else>
              <span class="font-bold">{{ formatAmount(item.amount) }}</span>
              <ElButton
                v-if="canEditItem(item)"
                link
                size="small"
                @click="startEditItem(item)"
              >
                {{ $t('common.edit') }}
              </ElButton>
            </template>
          </div>
        </div>

        <div class="mb-8px mt-16px text-14px font-bold">{{ $t('salary.common.deduction') }}</div>
        <div v-for="item in deductionItems" :key="item.id" class="flex items-center justify-between border-b border-gray-100 py-8px dark:border-gray-700">
          <div>
            <div class="text-14px">{{ item.itemName }}</div>
            <div class="text-12px text-gray-400">{{ item.source }}</div>
          </div>
          <span class="font-bold" style="color: #e85b65">{{ formatAmount(item.amount) }}</span>
        </div>
      </template>
    </ElDrawer>
  </div>
</template>
