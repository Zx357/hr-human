<script setup lang="ts">
import { ref, watch } from 'vue';
import { fetchEmployeePage } from '@/service/api/hr';
import { useOrgTree } from '@/composables/use-org-tree';
import { useDictOptions } from '@/composables/use-dict-options';

defineOptions({ name: 'EmployeePickerDialog' });

interface Props {
  /** 是否允许多选，默认单选 */
  multiple?: boolean;
}

const props = withDefaults(defineProps<Props>(), { multiple: false });

const visible = defineModel<boolean>({ default: false });

const emit = defineEmits<{
  confirm: [selectedEmployees: Api.Hr.Employee[]];
}>();

const { orgTreeOptions, loadOrgTree } = useOrgTree();
const { getDictLabel: getGenderLabel } = useDictOptions('gender');
const { getDictLabel: getPositionLabel } = useDictOptions('position');

const loading = ref(false);
const tableData = ref<Api.Hr.Employee[]>([]);
const total = ref(0);
const page = ref(1);
const pageSize = ref(10);
const search = ref({ name: '', employeeNo: '', orgIds: [] as number[] });
const cascadeSelect = ref(false);
const selectedRows = ref<Api.Hr.Employee[]>([]);

async function loadData() {
  loading.value = true;
  try {
    const res = await fetchEmployeePage({
      pageNum: page.value,
      pageSize: pageSize.value,
      name: search.value.name || undefined,
      employeeNo: search.value.employeeNo || undefined,
      orgIds: search.value.orgIds.length > 0 ? search.value.orgIds.join(',') : undefined,
      status: 1
    });
    if (res.data) {
      tableData.value = res.data.records || [];
      total.value = res.data.total || 0;
    }
  } catch {
    // 请求层已统一弹错
  } finally {
    loading.value = false;
  }
}

function handleSearch() {
  page.value = 1;
  loadData();
}

function handleReset() {
  search.value = { name: '', employeeNo: '', orgIds: [] };
  page.value = 1;
  loadData();
}

function handlePageChange(pageNum: number) {
  page.value = pageNum;
  loadData();
}

function handleSizeChange(size: number) {
  pageSize.value = size;
  page.value = 1;
  loadData();
}

function handleSelectionChange(rows: Api.Hr.Employee[]) {
  selectedRows.value = rows;
}

/** 单选模式：点击行内“选择”立即确认 */
function handlePick(row: Api.Hr.Employee) {
  emit('confirm', [row]);
  visible.value = false;
}

/** 多选模式：底部确认按钮 */
function handleConfirm() {
  if (selectedRows.value.length === 0) {
    return;
  }
  emit('confirm', selectedRows.value);
  visible.value = false;
}

watch(visible, val => {
  if (val) {
    search.value = { name: '', employeeNo: '', orgIds: [] };
    page.value = 1;
    selectedRows.value = [];
    if (!orgTreeOptions.value.length) {
      loadOrgTree();
    }
    loadData();
  }
});
</script>

<template>
  <ElDialog
    :model-value="visible"
    title="选择员工"
    width="900px"
    destroy-on-close
    append-to-body
    @update:model-value="visible = $event"
  >
    <div class="mb-16px">
      <ElForm inline :model="search">
        <ElFormItem label="姓名">
          <ElInput v-model="search.name" placeholder="请输入姓名" clearable style="width: 120px" />
        </ElFormItem>
        <ElFormItem label="工号">
          <ElInput v-model="search.employeeNo" placeholder="请输入工号" clearable style="width: 120px" />
        </ElFormItem>
        <ElFormItem label="组织">
          <ElTreeSelect
            v-model="search.orgIds"
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
              <div class="border-b border-gray-200 px-12px py-8px">
                <ElCheckbox v-model="cascadeSelect" size="small">联动选择</ElCheckbox>
              </div>
            </template>
          </ElTreeSelect>
        </ElFormItem>
        <ElFormItem>
          <ElButton type="primary" @click="handleSearch">搜索</ElButton>
          <ElButton @click="handleReset">重置</ElButton>
        </ElFormItem>
      </ElForm>
    </div>

    <ElTable
      v-loading="loading"
      :data="tableData"
      border
      stripe
      max-height="400px"
      @selection-change="handleSelectionChange"
    >
      <ElTableColumn v-if="props.multiple" type="selection" width="42" />
      <ElTableColumn prop="employeeNo" label="工号" width="100" />
      <ElTableColumn prop="name" label="姓名" width="80" />
      <ElTableColumn prop="gender" label="性别" width="60" align="center">
        <template #default="{ row }">{{ getGenderLabel(row.gender) }}</template>
      </ElTableColumn>
      <ElTableColumn prop="companyName" label="公司" min-width="120" show-overflow-tooltip />
      <ElTableColumn prop="deptName" label="部门" min-width="100" />
      <ElTableColumn prop="position" label="职位" min-width="100">
        <template #default="{ row }">{{ getPositionLabel(row.position) }}</template>
      </ElTableColumn>
      <ElTableColumn v-if="!props.multiple" label="操作" width="80" align="center" fixed="right">
        <template #default="{ row }">
          <ElButton type="primary" link size="small" @click="handlePick(row)">选择</ElButton>
        </template>
      </ElTableColumn>
    </ElTable>

    <div class="mt-16px flex justify-end">
      <ElPagination
        v-model:current-page="page"
        v-model:page-size="pageSize"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        @current-change="handlePageChange"
        @size-change="handleSizeChange"
      />
    </div>

    <template v-if="props.multiple" #footer>
      <ElButton @click="visible = false">取消</ElButton>
      <ElButton type="primary" :disabled="selectedRows.length === 0" @click="handleConfirm">
        确认（已选 {{ selectedRows.length }} 人）
      </ElButton>
    </template>
  </ElDialog>
</template>
