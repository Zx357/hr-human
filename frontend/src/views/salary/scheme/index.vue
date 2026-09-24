<script setup lang="tsx">
import { onMounted, ref } from 'vue';
import { ElMessage, ElPopconfirm } from 'element-plus';
import { type SalaryItemDef, type SalaryScheme, type SalarySchemeItem, deleteScheme, fetchSchemeDetail, fetchSchemePage, fetchSalaryItems, saveScheme } from '@/service/api/salary';
import { $t } from '@/locales';

defineOptions({ name: 'SalaryScheme' });

const loading = ref(false);
const data = ref<SalaryScheme[]>([]);
const total = ref(0);
const currentPage = ref(1);
const pageSize = ref(10);
const keyword = ref('');

async function loadData() {
  loading.value = true;
  try {
    const res = await fetchSchemePage({ pageNum: currentPage.value, pageSize: pageSize.value, keyword: keyword.value || undefined });
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

function handleSearch() {
  currentPage.value = 1;
  loadData();
}

function handlePageChange() {
  loadData();
}

function handleSizeChange() {
  currentPage.value = 1;
  loadData();
}

// ===== 新增/编辑方案（含明细编排） =====
const dialogVisible = ref(false);
const submitLoading = ref(false);
const itemPool = ref<SalaryItemDef[]>([]);

const formData = ref<{
  id?: number;
  schemeName: string;
  schemeCode: string;
  enabled: number;
  sortOrder: number;
  remark: string;
  items: { itemId: number; defaultAmount?: number }[];
}>(emptyForm());

function emptyForm() {
  return {
    schemeName: '',
    schemeCode: '',
    enabled: 1,
    sortOrder: 0,
    remark: '',
    items: [] as { itemId: number; defaultAmount?: number }[]
  };
}

function itemDefOf(itemId: number): SalaryItemDef | undefined {
  return itemPool.value.find(item => item.id === itemId);
}

/** 未被方案使用的项（可添加） */
function availableItems() {
  return itemPool.value.filter(item => !formData.value.items.some(selected => selected.itemId === item.id));
}

async function handleAdd() {
  formData.value = emptyForm();
  await loadItemPool();
  dialogVisible.value = true;
}

async function handleEdit(row: SalaryScheme) {
  if (!row.id) return;
  const detail = await fetchSchemeDetail(row.id);
  const scheme = detail.data;
  formData.value = {
    id: scheme?.id,
    schemeName: scheme?.schemeName || '',
    schemeCode: scheme?.schemeCode || '',
    enabled: scheme?.enabled ?? 1,
    sortOrder: scheme?.sortOrder ?? 0,
    remark: scheme?.remark || '',
    items: (scheme?.items || []).map(item => ({
      itemId: item.itemId,
      defaultAmount: item.defaultAmount ?? undefined
    }))
  };
  await loadItemPool();
  dialogVisible.value = true;
}

async function loadItemPool() {
  const res = await fetchSalaryItems(1);
  itemPool.value = res.data || [];
}

function addItem(itemId: number) {
  const def = itemDefOf(itemId);
  if (!def) return;
  formData.value.items.push({
    itemId,
    defaultAmount: def.valueType === 1 ? 0 : undefined
  });
}

function removeItem(index: number) {
  formData.value.items.splice(index, 1);
}

function moveItem(index: number, offset: -1 | 1) {
  const target = index + offset;
  if (target < 0 || target >= formData.value.items.length) return;
  const items = formData.value.items;
  [items[index], items[target]] = [items[target], items[index]];
}

async function handleSubmit() {
  if (!formData.value.schemeName?.trim()) {
    ElMessage.warning($t('salary.scheme.pleaseInputName'));
    return;
  }
  submitLoading.value = true;
  try {
    await saveScheme({
      id: formData.value.id,
      schemeName: formData.value.schemeName,
      schemeCode: formData.value.schemeCode || undefined,
      enabled: formData.value.enabled,
      sortOrder: formData.value.sortOrder,
      remark: formData.value.remark,
      items: formData.value.items
    });
    ElMessage.success($t('common.updateSuccess'));
    dialogVisible.value = false;
    loadData();
  } catch {
    // 请求层已统一弹错（含比例项引用顺序校验错误）
  } finally {
    submitLoading.value = false;
  }
}

async function handleDelete(id?: number) {
  if (!id) return;
  try {
    await deleteScheme(id);
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
      <ElForm inline @submit.prevent>
        <ElFormItem :label="$t('salary.scheme.keyword')">
          <ElInput
            v-model="keyword"
            :placeholder="$t('salary.scheme.keywordTip')"
            clearable
            style="width: 200px"
            @keyup.enter="handleSearch"
          />
        </ElFormItem>
        <ElFormItem>
          <ElButton type="primary" @click="handleSearch">
            <template #icon><icon-ep-search /></template>
            {{ $t('common.search') }}
          </ElButton>
        </ElFormItem>
      </ElForm>
    </ElCard>

    <ElCard class="flex-1">
      <template #header>
        <div class="flex flex-wrap items-center justify-between gap-12px">
          <span>{{ $t('salary.scheme.title') }}</span>
          <ElButton v-permission="'sal:scheme:manage'" type="primary" @click="handleAdd">
            <template #icon><icon-ep-plus /></template>
            {{ $t('salary.scheme.addScheme') }}
          </ElButton>
        </div>
      </template>

      <ElTable v-loading="loading" :data="data" size="small" border>
        <ElTableColumn prop="sortOrder" :label="$t('salary.common.sortOrder')" width="70" align="center" />
        <ElTableColumn prop="schemeName" :label="$t('salary.scheme.schemeName')" min-width="140" />
        <ElTableColumn prop="schemeCode" :label="$t('salary.scheme.schemeCode')" min-width="120" />
        <ElTableColumn prop="itemCount" :label="$t('salary.scheme.itemCount')" width="100" align="center" />
        <ElTableColumn :label="$t('common.status')" width="90" align="center">
          <template #default="{ row }">
            <ElTag :type="row.enabled === 1 ? 'success' : 'info'">
              {{ row.enabled === 1 ? $t('salary.common.enabled') : $t('salary.common.disabled') }}
            </ElTag>
          </template>
        </ElTableColumn>
        <ElTableColumn prop="remark" :label="$t('common.remark')" min-width="140" show-overflow-tooltip />
        <ElTableColumn :label="$t('common.action')" width="140" align="center" fixed="right">
          <template #default="{ row }">
            <ElButton v-permission="'sal:scheme:manage'" type="primary" link size="small" @click="handleEdit(row)">
              {{ $t('salary.scheme.configItems') }}
            </ElButton>
            <ElPopconfirm :title="$t('salary.scheme.confirmDelete')" @confirm="handleDelete(row.id)">
              <template #reference>
                <ElButton v-permission="'sal:scheme:manage'" type="danger" link size="small">
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

    <!-- 新增/编辑方案（含明细编排） -->
    <ElDialog
      v-model="dialogVisible"
      :title="formData.id ? $t('salary.scheme.editScheme') : $t('salary.scheme.addScheme')"
      width="680px"
      top="24px"
      destroy-on-close
      :close-on-click-modal="false"
    >
      <ElForm label-width="100px">
        <ElFormItem :label="$t('salary.scheme.schemeName')" required>
          <ElInput v-model="formData.schemeName" :placeholder="$t('salary.scheme.pleaseInputName')" />
        </ElFormItem>
        <ElFormItem :label="$t('salary.scheme.schemeCode')">
          <ElInput v-model="formData.schemeCode" :placeholder="$t('salary.scheme.schemeCodeTip')" />
        </ElFormItem>
        <ElFormItem :label="$t('common.status')">
          <ElSwitch v-model="formData.enabled" :active-value="1" :inactive-value="0" />
        </ElFormItem>
        <ElFormItem :label="$t('common.remark')">
          <ElInput v-model="formData.remark" type="textarea" :rows="2" />
        </ElFormItem>

        <ElFormItem :label="$t('salary.scheme.items')">
          <div class="w-full">
            <div class="mb-8px">
              <ElSelect
                :model-value="undefined"
                :placeholder="$t('salary.scheme.selectItemToAdd')"
                style="width: 100%"
                @change="(value: number) => addItem(value)"
              >
                <ElOption
                  v-for="item in availableItems()"
                  :key="item.id"
                  :label="`${item.itemName} (${item.itemCode})`"
                  :value="item.id!"
                />
              </ElSelect>
            </div>
            <ElTable :data="formData.items" size="small" border max-height="320">
              <ElTableColumn type="index" :label="'#'" width="46" align="center" />
              <ElTableColumn :label="$t('salary.item.itemName')" min-width="110">
                <template #default="{ row }">{{ itemDefOf(row.itemId)?.itemName }}</template>
              </ElTableColumn>
              <ElTableColumn :label="$t('salary.common.valueType')" width="90" align="center">
                <template #default="{ row }">
                  {{ itemDefOf(row.itemId)?.valueType === 1
                    ? $t('salary.common.valueTypeFixed')
                    : itemDefOf(row.itemId)?.valueType === 2
                        ? $t('salary.common.valueTypeRatio')
                        : itemDefOf(row.itemId)?.valueType === 3
                            ? $t('salary.common.valueTypeAttendance')
                            : itemDefOf(row.itemId)?.valueType === 4 ? $t('salary.common.valueTypeManual') : '-' }}
                </template>
              </ElTableColumn>
              <ElTableColumn :label="$t('salary.scheme.defaultAmount')" width="150" align="center">
                <template #default="{ row }">
                  <ElInputNumber
                    v-if="itemDefOf(row.itemId)?.valueType === 1"
                    v-model="row.defaultAmount"
                    :min="0"
                    :precision="2"
                    :controls="false"
                    size="small"
                    style="width: 120px"
                  />
                  <span v-else class="text-gray-400">-</span>
                </template>
              </ElTableColumn>
              <ElTableColumn :label="$t('common.action')" width="140" align="center">
                <template #default="{ $index }">
                  <ElButton link size="small" :disabled="$index === 0" @click="moveItem($index, -1)">
                    {{ $t('salary.scheme.moveUp') }}
                  </ElButton>
                  <ElButton
                    link
                    size="small"
                    :disabled="$index === formData.items.length - 1"
                    @click="moveItem($index, 1)"
                  >
                    {{ $t('salary.scheme.moveDown') }}
                  </ElButton>
                  <ElButton type="danger" link size="small" @click="removeItem($index)">
                    {{ $t('common.delete') }}
                  </ElButton>
                </template>
              </ElTableColumn>
            </ElTable>
            <div class="mt-4px text-12px text-gray-400">{{ $t('salary.scheme.orderTip') }}</div>
          </div>
        </ElFormItem>
      </ElForm>
      <template #footer>
        <ElButton @click="dialogVisible = false">{{ $t('common.cancel') }}</ElButton>
        <ElButton type="primary" :loading="submitLoading" @click="handleSubmit">{{ $t('common.ok') }}</ElButton>
      </template>
    </ElDialog>
  </div>
</template>
