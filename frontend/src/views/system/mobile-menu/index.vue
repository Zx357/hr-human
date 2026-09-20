<script setup lang="tsx">
import { onMounted, reactive, ref } from 'vue';
import type { FormInstance, FormRules } from 'element-plus';
import { ElMessage } from 'element-plus';
import { deleteMobileMenu, fetchMobileMenuPage, saveMobileMenu, toggleMobileMenuStatus } from '@/service/api/system';
import { $t } from '@/locales';

defineOptions({ name: 'MobileMenuManage' });

// 类型定义
interface MobileMenu {
  id?: number;
  menuName: string;
  menuCode: string;
  icon: string;
  iconBgColor: string;
  path: string;
  menuGroup: string;
  sortOrder: number;
  status: number;
  createdTime?: string;
}

// 搜索表单
const queryForm = reactive({
  menuName: '',
  status: '' as '' | 0 | 1
});

// 数据
const loading = ref(false);
const tableData = ref<MobileMenu[]>([]);
const total = ref(0);
const pageNum = ref(1);
const pageSize = ref(10);

// 加载数据
async function loadData() {
  loading.value = true;
  try {
    const res = await fetchMobileMenuPage({
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      menuName: queryForm.menuName || undefined,
      status: queryForm.status === '' ? undefined : queryForm.status
    });
    const data = res.data;
    if (data) {
      tableData.value = (data.records as unknown as MobileMenu[]) || [];
      total.value = data.total;
    }
  } finally {
    loading.value = false;
  }
}

// 搜索
function handleQuery() {
  pageNum.value = 1;
  loadData();
}

// 重置
function resetQuery() {
  queryForm.menuName = '';
  queryForm.status = '';
  handleQuery();
}

// 分页
function handlePageChange(page: number) {
  pageNum.value = page;
  loadData();
}

function handleSizeChange(size: number) {
  pageSize.value = size;
  pageNum.value = 1;
  loadData();
}

onMounted(() => {
  loadData();
});

// 弹窗
const dialogVisible = ref(false);
const dialogTitle = ref('');
const operateType = ref<'add' | 'edit'>('add');
const formRef = ref<FormInstance>();

const formData = reactive<MobileMenu>({
  menuName: '',
  menuCode: '',
  icon: '',
  iconBgColor: '#2d8cf0',
  path: '',
  menuGroup: 'apply',
  sortOrder: 0,
  status: 1
});

const rules: FormRules = {
  menuName: [{ required: true, message: $t('sys.common.pleaseEnterMenuName'), trigger: 'blur' }],
  menuCode: [{ required: true, message: $t('sys.menu.pleaseEnterMenuCode'), trigger: 'blur' }],
  path: [{ required: true, message: $t('sys.mobileMenu.pleaseEnterRoutePath'), trigger: 'blur' }],
  menuGroup: [{ required: true, message: $t('sys.mobileMenu.pleaseSelectAMenuGroup'), trigger: 'change' }]
};

// 菜单分组选项
const menuGroupOptions = [
  { label: $t('sys.mobileMenu.quickFeatures'), value: 'quick' },
  { label: $t('sys.mobileMenu.applicationCenter'), value: 'apply' }
];

// 图标选项
const iconOptions = [
  'calendar',
  'clock',
  'checkbox',
  'location',
  'auth',
  'redo',
  'closeempty',
  'refreshempty',
  'person',
  'settings',
  'home',
  'chat',
  'email',
  'phone',
  'camera',
  'image',
  'folder',
  'star'
];

// 颜色选项
const colorOptions = [
  '#2d8cf0',
  '#5cadff',
  '#19be6b',
  '#ff9900',
  '#ed4014',
  '#9254de',
  '#f5222d',
  '#fa8c16',
  '#13c2c2',
  '#722ed1'
];

function resetForm() {
  formData.id = undefined;
  formData.menuName = '';
  formData.menuCode = '';
  formData.icon = '';
  formData.iconBgColor = '#2d8cf0';
  formData.path = '';
  formData.menuGroup = 'apply';
  formData.sortOrder = 0;
  formData.status = 1;
}

function handleAdd() {
  resetForm();
  operateType.value = 'add';
  dialogTitle.value = $t('sys.mobileMenu.newMobileMenu');
  dialogVisible.value = true;
}

function handleEdit(row: MobileMenu) {
  resetForm();
  operateType.value = 'edit';
  dialogTitle.value = $t('sys.mobileMenu.editMobileMenu');
  Object.assign(formData, row);
  dialogVisible.value = true;
}

async function handleDelete(id: number) {
  loading.value = true;
  try {
    const res = await deleteMobileMenu(id);
    if (res.data !== null && res.data !== undefined) {
      ElMessage.success($t('common.deleteSuccess'));
      loadData();
    }
  } finally {
    loading.value = false;
  }
}

async function handleToggleStatus(row: MobileMenu) {
  loading.value = true;
  try {
    const res = await toggleMobileMenuStatus(row.id!);
    if (res.data !== null && res.data !== undefined) {
      ElMessage.success(row.status === 1 ? $t('sys.common.disabled') : $t('approval.flow.enabled'));
      loadData();
    }
  } finally {
    loading.value = false;
  }
}

async function submitForm() {
  if (!formRef.value) return;

  await formRef.value.validate(async valid => {
    if (valid) {
      loading.value = true;
      try {
        const res = await saveMobileMenu({ ...formData });
        if (res.data !== null && res.data !== undefined) {
          ElMessage.success(operateType.value === 'add' ? $t('common.addSuccess') : $t('common.modifySuccess'));
          dialogVisible.value = false;
          loadData();
        }
      } finally {
        loading.value = false;
      }
    }
  });
}

function cancel() {
  dialogVisible.value = false;
  resetForm();
}

// 判断是否是第一个
function isFirst(row: MobileMenu) {
  const index = tableData.value.findIndex(item => item.id === row.id);
  return index === 0;
}

// 判断是否是最后一个
function isLast(row: MobileMenu) {
  const index = tableData.value.findIndex(item => item.id === row.id);
  return index === tableData.value.length - 1;
}

// 上移
async function handleMoveUp(row: MobileMenu) {
  const index = tableData.value.findIndex(item => item.id === row.id);
  if (index <= 0) return;

  const prevRow = tableData.value[index - 1];
  await swapSort(row, prevRow);
}

// 下移
async function handleMoveDown(row: MobileMenu) {
  const index = tableData.value.findIndex(item => item.id === row.id);
  if (index >= tableData.value.length - 1) return;

  const nextRow = tableData.value[index + 1];
  await swapSort(row, nextRow);
}

// 交换排序
async function swapSort(row1: MobileMenu, row2: MobileMenu) {
  loading.value = true;
  try {
    // 交换排序号
    const tempSort = row1.sortOrder;

    // 更新第一个
    await saveMobileMenu({ ...row1, sortOrder: row2.sortOrder });

    // 更新第二个
    await saveMobileMenu({ ...row2, sortOrder: tempSort });

    loadData();
  } finally {
    loading.value = false;
  }
}
</script>

<template>
  <div class="mobile-menu-page">
    <!-- 搜索区域 -->
    <ElCard shadow="never">
      <ElForm :model="queryForm" inline>
        <ElFormItem :label="$t('sys.common.menuName')">
          <ElInput
            v-model="queryForm.menuName"
            :placeholder="$t('sys.common.pleaseEnterMenuName')"
            clearable
            style="width: 200px"
            @keyup.enter="handleQuery"
          />
        </ElFormItem>
        <ElFormItem :label="$t('common.status')">
          <ElSelect v-model="queryForm.status" :placeholder="$t('sys.common.menuStatus')" clearable style="width: 200px">
            <ElOption :label="$t('common.enable')" :value="1" />
            <ElOption :label="$t('common.disable')" :value="0" />
          </ElSelect>
        </ElFormItem>
        <ElFormItem>
          <ElButton type="primary" @click="handleQuery">
            <template #icon><icon-ep-search /></template>
            {{ $t('common.search') }}
          </ElButton>
          <ElButton @click="resetQuery">
            <template #icon><icon-ep-refresh /></template>
            {{ $t('common.reset') }}
          </ElButton>
        </ElFormItem>
      </ElForm>
    </ElCard>

    <!-- 表格区域 -->
    <ElCard shadow="never" class="table-card">
      <div class="mb-16px">
        <ElButton v-permission="'system:mobile-menu:add'" type="primary" @click="handleAdd">
          <template #icon><icon-ep-plus /></template>
          {{ $t('common.add') }}
        </ElButton>
      </div>

      <div class="table-wrapper">
        <ElTable v-loading="loading" :data="tableData" border height="100%">
          <ElTableColumn prop="menuName" :label="$t('sys.common.menuName')" min-width="120" />
          <ElTableColumn prop="menuCode" :label="$t('sys.common.menuCode')" min-width="100" />
          <ElTableColumn prop="icon" :label="$t('common.icon')" width="80" align="center">
            <template #default="{ row }">
              <view class="icon-preview" :style="{ backgroundColor: row.iconBgColor }">
                <UniIcons :type="row.icon" size="16" color="#fff" />
              </view>
            </template>
          </ElTableColumn>
          <ElTableColumn prop="path" :label="$t('sys.mobileMenu.routePath')" min-width="200" show-overflow-tooltip />
          <ElTableColumn prop="menuGroup" :label="$t('common.group')" width="100" align="center">
            <template #default="{ row }">
              <ElTag :type="row.menuGroup === 'quick' ? 'success' : 'primary'" size="small">
                {{ row.menuGroup === 'quick' ? $t('sys.mobileMenu.quickFeatures') : $t('sys.mobileMenu.applicationCenter') }}
              </ElTag>
            </template>
          </ElTableColumn>
          <ElTableColumn prop="sortOrder" :label="$t('common.sort')" width="120" align="center">
            <template #default="{ row }">
              <div class="sort-actions">
                <ElButton type="primary" link size="small" :disabled="isFirst(row)" @click="handleMoveUp(row)">
                  <icon-ep-top />
                </ElButton>
                <span class="sort-num">{{ row.sortOrder }}</span>
                <ElButton type="primary" link size="small" :disabled="isLast(row)" @click="handleMoveDown(row)">
                  <icon-ep-bottom />
                </ElButton>
              </div>
            </template>
          </ElTableColumn>
          <ElTableColumn prop="status" :label="$t('common.status')" width="100" align="center">
            <template #default="{ row }">
              <ElSwitch
                v-permission="'system:mobile-menu:edit'"
                :model-value="row.status === 1"
                inline-prompt
                :active-text="$t('common.enable')"
                :inactive-text="$t('common.disable')"
                @change="handleToggleStatus(row)"
              />
            </template>
          </ElTableColumn>
          <ElTableColumn prop="createdTime" :label="$t('common.createTime')" width="160" align="center" />
          <ElTableColumn :label="$t('common.action')" width="150" align="center" fixed="right">
            <template #default="{ row }">
              <ElButton v-permission="'system:mobile-menu:edit'" type="primary" link size="small" @click="handleEdit(row)">{{ $t('common.edit') }}</ElButton>
              <ElPopconfirm :title="$t('sys.mobileMenu.areYouSureYouWantToDeleteThisMenu')" @confirm="handleDelete(row.id)">
                <template #reference>
                  <ElButton v-permission="'system:mobile-menu:delete'" type="danger" link size="small">{{ $t('common.delete') }}</ElButton>
                </template>
              </ElPopconfirm>
            </template>
          </ElTableColumn>
        </ElTable>
      </div>

      <div class="mt-16px flex justify-end">
        <ElPagination
          v-model:current-page="pageNum"
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
    <ElDialog v-model="dialogVisible" :title="dialogTitle" width="550px" append-to-body>
      <ElForm ref="formRef" :model="formData" :rules="rules" label-width="100px">
        <ElFormItem :label="$t('sys.common.menuName')" prop="menuName">
          <ElInput v-model="formData.menuName" :placeholder="$t('sys.common.pleaseEnterMenuName')" />
        </ElFormItem>
        <ElFormItem :label="$t('sys.common.menuCode')" prop="menuCode">
          <ElInput v-model="formData.menuCode" :placeholder="$t('sys.menu.pleaseEnterMenuCode')" />
        </ElFormItem>
        <ElFormItem :label="$t('common.icon')" prop="icon">
          <ElSelect v-model="formData.icon" :placeholder="$t('sys.mobileMenu.pleaseSelectAnIcon')" style="width: 100%">
            <ElOption v-for="icon in iconOptions" :key="icon" :label="icon" :value="icon">
              <span>{{ icon }}</span>
            </ElOption>
          </ElSelect>
        </ElFormItem>
        <ElFormItem :label="$t('sys.mobileMenu.iconBackgroundColor')">
          <div class="color-picker">
            <div
              v-for="color in colorOptions"
              :key="color"
              class="color-item"
              :class="{ active: formData.iconBgColor === color }"
              :style="{ backgroundColor: color }"
              @click="formData.iconBgColor = color"
            />
          </div>
        </ElFormItem>
        <ElFormItem :label="$t('sys.mobileMenu.routePath')" prop="path">
          <ElInput v-model="formData.path" :placeholder="$t('sys.mobileMenu.eGPagesApplyLeaveIndex')" />
        </ElFormItem>
        <ElFormItem :label="$t('sys.mobileMenu.menuGroup')" prop="menuGroup">
          <ElRadioGroup v-model="formData.menuGroup">
            <ElRadio v-for="item in menuGroupOptions" :key="item.value" :value="item.value">
              {{ item.label }}
            </ElRadio>
          </ElRadioGroup>
        </ElFormItem>
        <ElFormItem :label="$t('common.sort')">
          <ElInputNumber v-model="formData.sortOrder" :min="0" :max="999" />
        </ElFormItem>
        <ElFormItem :label="$t('common.status')">
          <ElRadioGroup v-model="formData.status">
            <ElRadio :value="1">{{ $t('common.enable') }}</ElRadio>
            <ElRadio :value="0">{{ $t('common.disable') }}</ElRadio>
          </ElRadioGroup>
        </ElFormItem>
      </ElForm>
      <template #footer>
        <ElButton @click="cancel">{{ $t('common.cancel') }}</ElButton>
        <ElButton type="primary" :loading="loading" @click="submitForm">{{ $t('common.ok') }}</ElButton>
      </template>
    </ElDialog>
  </div>
</template>

<style scoped>
.mobile-menu-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
  height: calc(100vh - 120px);
  overflow: hidden;
}

.table-card {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.table-card :deep(.el-card__body) {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.table-wrapper {
  flex: 1;
  min-height: 0;
  overflow: auto;
}

.icon-preview {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  border-radius: 6px;
}

.color-picker {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.color-item {
  width: 28px;
  height: 28px;
  border-radius: 4px;
  cursor: pointer;
  border: 2px solid transparent;
  transition: all 0.2s;
}

.color-item:hover {
  transform: scale(1.1);
}

.color-item.active {
  border-color: #333;
}

.sort-actions {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
}

.sort-num {
  min-width: 24px;
  text-align: center;
  font-weight: 500;
}
</style>
