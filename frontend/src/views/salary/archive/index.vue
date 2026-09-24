<script setup lang="tsx">
import { onMounted, ref, watch } from 'vue';
import { ElMessage, ElPopconfirm } from 'element-plus';
import dayjs from 'dayjs';
import {
  type SalaryArchiveItem,
  type SalaryArchiveRow,
  type SalaryScheme,
  bindArchive,
  fetchArchiveEmployeePage,
  fetchArchiveItems,
  fetchSchemeEnabled,
  unbindArchive,
  updateArchiveItems
} from '@/service/api/salary';
import { fetchCompanyList, fetchDepartmentTree } from '@/service/api/organization';
import { $t } from '@/locales';

defineOptions({ name: 'SalaryArchive' });

const loading = ref(false);
const data = ref<SalaryArchiveRow[]>([]);
const total = ref(0);
const currentPage = ref(1);
const pageSize = ref(10);
const companies = ref<{ id: number; unitName?: string; companyName?: string }[]>([]);
const departments = ref<{ id: number; unitName: string; children?: unknown[] }[]>([]);

const searchParams = ref({
  companyId: undefined as number | undefined,
  deptId: undefined as number | undefined,
  employeeName: '',
  employeeNo: '',
  archivedOnly: false
});

async function loadData() {
  loading.value = true;
  try {
    const res = await fetchArchiveEmployeePage({
      pageNum: currentPage.value,
      pageSize: pageSize.value,
      companyId: searchParams.value.companyId,
      deptId: searchParams.value.deptId,
      employeeName: searchParams.value.employeeName || undefined,
      employeeNo: searchParams.value.employeeNo || undefined,
      archivedOnly: searchParams.value.archivedOnly || undefined
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

async function loadCompanies() {
  const res = await fetchCompanyList();
  companies.value = res.data || [];
}

async function loadDepartments(companyId?: number) {
  if (!companyId) {
    departments.value = [];
    return;
  }
  const res = await fetchDepartmentTree(companyId);
  departments.value = (res.data || []) as typeof departments.value;
}

watch(
  () => searchParams.value.companyId,
  val => {
    searchParams.value.deptId = undefined;
    loadDepartments(val);
  }
);

onMounted(() => {
  loadCompanies();
  loadData();
});

function handleSearch() {
  currentPage.value = 1;
  loadData();
}

function handleReset() {
  searchParams.value = {
    companyId: undefined,
    deptId: undefined,
    employeeName: '',
    employeeNo: '',
    archivedOnly: false
  };
  departments.value = [];
  handleSearch();
}

// ===== 绑定/换绑方案 =====
const bindDialogVisible = ref(false);
const bindSubmitting = ref(false);
const schemes = ref<SalaryScheme[]>([]);
const bindForm = ref<{
  employeeId?: number;
  employeeName?: string;
  schemeId?: number;
  effectiveDate?: string;
  remark?: string;
}>({});

async function openBindDialog(row: SalaryArchiveRow) {
  bindForm.value = {
    employeeId: row.employeeId,
    employeeName: `${row.employeeName} (${row.employeeNo})`,
    schemeId: row.schemeId,
    effectiveDate: row.effectiveDate || dayjs().format('YYYY-MM-DD'),
    remark: row.remark
  };
  const res = await fetchSchemeEnabled();
  schemes.value = res.data || [];
  bindDialogVisible.value = true;
}

async function submitBind() {
  if (!bindForm.value.schemeId) {
    ElMessage.warning($t('salary.archive.pleaseSelectScheme'));
    return;
  }
  bindSubmitting.value = true;
  try {
    await bindArchive({
      employeeId: bindForm.value.employeeId!,
      schemeId: bindForm.value.schemeId,
      effectiveDate: bindForm.value.effectiveDate,
      remark: bindForm.value.remark
    });
    ElMessage.success($t('common.updateSuccess'));
    bindDialogVisible.value = false;
    loadData();
  } catch {
    // 请求层已统一弹错
  } finally {
    bindSubmitting.value = false;
  }
}

// ===== 调整明细 =====
const itemsDialogVisible = ref(false);
const itemsSubmitting = ref(false);
const editingRow = ref<SalaryArchiveRow | null>(null);
const editingItems = ref<SalaryArchiveItem[]>([]);

async function openItemsDialog(row: SalaryArchiveRow) {
  if (!row.archiveId) return;
  editingRow.value = row;
  const res = await fetchArchiveItems(row.archiveId);
  editingItems.value = (res.data || []).map(item => ({ ...item }));
  itemsDialogVisible.value = true;
}

async function submitItems() {
  if (!editingRow.value?.archiveId) return;
  itemsSubmitting.value = true;
  try {
    await updateArchiveItems(
      editingRow.value.archiveId,
      editingItems.value.map(item => ({ itemId: item.itemId, amount: item.amount }))
    );
    ElMessage.success($t('common.updateSuccess'));
    itemsDialogVisible.value = false;
  } catch {
    // 请求层已统一弹错
  } finally {
    itemsSubmitting.value = false;
  }
}

async function handleUnbind(row: SalaryArchiveRow) {
  if (!row.archiveId) return;
  try {
    await unbindArchive(row.archiveId);
    ElMessage.success($t('common.deleteSuccess'));
    loadData();
  } catch {
    // 请求层已统一弹错
  }
}
</script>

<template>
  <div class="min-h-500px flex-col-stretch gap-16px overflow-hidden lt-sm:overflow-auto">
    <ElCard>
      <ElForm inline :model="searchParams">
        <ElFormItem :label="$t('common.company')">
          <ElSelect
            v-model="searchParams.companyId"
            :placeholder="$t('common.pleaseSelectCompany')"
            clearable
            style="width: 160px"
          >
            <ElOption v-for="c in companies" :key="c.id" :label="c.unitName || c.companyName" :value="c.id" />
          </ElSelect>
        </ElFormItem>
        <ElFormItem :label="$t('common.department')">
          <ElTreeSelect
            v-model="searchParams.deptId"
            :data="departments"
            :props="{ label: 'unitName', value: 'id', children: 'children' }"
            :placeholder="$t('common.pleaseSelectDepartment')"
            clearable
            check-strictly
            style="width: 160px"
          />
        </ElFormItem>
        <ElFormItem :label="$t('common.employeeNo')">
          <ElInput
            v-model="searchParams.employeeNo"
            :placeholder="$t('common.employeeNo')"
            clearable
            style="width: 110px"
            @keyup.enter="handleSearch"
          />
        </ElFormItem>
        <ElFormItem :label="$t('common.name')">
          <ElInput
            v-model="searchParams.employeeName"
            :placeholder="$t('common.name')"
            clearable
            style="width: 110px"
            @keyup.enter="handleSearch"
          />
        </ElFormItem>
        <ElFormItem :label="$t('salary.archive.archivedOnly')">
          <ElSwitch v-model="searchParams.archivedOnly" />
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

    <ElCard class="flex-1">
      <template #header>
        <span>{{ $t('salary.archive.title') }}</span>
      </template>

      <ElTable v-loading="loading" :data="data" size="small" border>
        <ElTableColumn prop="employeeNo" :label="$t('common.employeeNo')" width="110" />
        <ElTableColumn prop="employeeName" :label="$t('common.employeeName')" width="100" />
        <ElTableColumn prop="deptName" :label="$t('common.department')" min-width="130" show-overflow-tooltip />
        <ElTableColumn :label="$t('salary.archive.scheme')" min-width="140">
          <template #default="{ row }">
            <ElTag v-if="row.schemeName" type="success">{{ row.schemeName }}</ElTag>
            <ElTag v-else type="info">{{ $t('salary.archive.notBound') }}</ElTag>
          </template>
        </ElTableColumn>
        <ElTableColumn prop="effectiveDate" :label="$t('salary.archive.effectiveDate')" width="110" align="center" />
        <ElTableColumn prop="remark" :label="$t('common.remark')" min-width="120" show-overflow-tooltip />
        <ElTableColumn :label="$t('common.action')" width="220" align="center" fixed="right">
          <template #default="{ row }">
            <ElButton
              v-permission="'sal:archive:manage'"
              :type="row.archiveId ? 'primary' : 'warning'"
              link
              size="small"
              @click="openBindDialog(row)"
            >
              {{ row.archiveId ? $t('salary.archive.rebind') : $t('salary.archive.bind') }}
            </ElButton>
            <ElButton
              v-if="row.archiveId"
              v-permission="'sal:archive:manage'"
              type="primary"
              link
              size="small"
              @click="openItemsDialog(row)"
            >
              {{ $t('salary.archive.adjustItems') }}
            </ElButton>
            <ElPopconfirm v-if="row.archiveId" :title="$t('salary.archive.confirmUnbind')" @confirm="handleUnbind(row)">
              <template #reference>
                <ElButton v-permission="'sal:archive:manage'" type="danger" link size="small">
                  {{ $t('salary.archive.unbind') }}
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
          @current-change="loadData"
          @size-change="
            () => {
              currentPage = 1;
              loadData();
            }
          "
        />
      </div>
    </ElCard>

    <!-- 绑定/换绑方案 -->
    <ElDialog
      v-model="bindDialogVisible"
      :title="$t('salary.archive.bindDialogTitle')"
      width="460px"
      destroy-on-close
      :close-on-click-modal="false"
    >
      <ElForm label-width="100px">
        <ElFormItem :label="$t('common.employee')">
          <span>{{ bindForm.employeeName }}</span>
        </ElFormItem>
        <ElFormItem :label="$t('salary.archive.scheme')" required>
          <ElSelect
            v-model="bindForm.schemeId"
            :placeholder="$t('salary.archive.pleaseSelectScheme')"
            style="width: 100%"
          >
            <ElOption v-for="s in schemes" :key="s.id" :label="s.schemeName" :value="s.id!" />
          </ElSelect>
          <div class="w-full text-12px text-gray-400">{{ $t('salary.archive.bindTip') }}</div>
        </ElFormItem>
        <ElFormItem :label="$t('salary.archive.effectiveDate')">
          <ElDatePicker v-model="bindForm.effectiveDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
        </ElFormItem>
        <ElFormItem :label="$t('common.remark')">
          <ElInput v-model="bindForm.remark" type="textarea" :rows="2" />
        </ElFormItem>
      </ElForm>
      <template #footer>
        <ElButton @click="bindDialogVisible = false">{{ $t('common.cancel') }}</ElButton>
        <ElButton type="primary" :loading="bindSubmitting" @click="submitBind">{{ $t('common.ok') }}</ElButton>
      </template>
    </ElDialog>

    <!-- 调整固定项明细 -->
    <ElDialog
      v-model="itemsDialogVisible"
      :title="$t('salary.archive.itemsDialogTitle')"
      width="480px"
      destroy-on-close
      :close-on-click-modal="false"
    >
      <ElTable :data="editingItems" size="small" border max-height="420">
        <ElTableColumn prop="itemName" :label="$t('salary.item.itemName')" min-width="120" />
        <ElTableColumn prop="itemCode" :label="$t('salary.item.itemCode')" min-width="130" />
        <ElTableColumn :label="$t('salary.archive.amount')" width="170" align="center">
          <template #default="{ row }">
            <ElInputNumber v-model="row.amount" :min="0" :precision="2" :controls="false" style="width: 140px" />
          </template>
        </ElTableColumn>
      </ElTable>
      <template #footer>
        <ElButton @click="itemsDialogVisible = false">{{ $t('common.cancel') }}</ElButton>
        <ElButton type="primary" :loading="itemsSubmitting" @click="submitItems">{{ $t('common.ok') }}</ElButton>
      </template>
    </ElDialog>
  </div>
</template>
