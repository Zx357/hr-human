<script setup lang="tsx">
import { onMounted, reactive, ref } from 'vue';
import type { FormInstance, FormRules } from 'element-plus';
import { ElMessage } from 'element-plus';
import { deleteMobileMenu, fetchMobileMenuPage, saveMobileMenu, toggleMobileMenuStatus } from '@/service/api/system';

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
  menuName: [{ required: true, message: '请输入菜单名称', trigger: 'blur' }],
  menuCode: [{ required: true, message: '请输入菜单编码', trigger: 'blur' }],
  path: [{ required: true, message: '请输入路由路径', trigger: 'blur' }],
  menuGroup: [{ required: true, message: '请选择菜单分组', trigger: 'change' }]
};

// 菜单分组选项
const menuGroupOptions = [
  { label: '快捷功能', value: 'quick' },
  { label: '申请中心', value: 'apply' }
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
  dialogTitle.value = '新增移动端菜单';
  dialogVisible.value = true;
}

function handleEdit(row: MobileMenu) {
  resetForm();
  operateType.value = 'edit';
  dialogTitle.value = '编辑移动端菜单';
  Object.assign(formData, row);
  dialogVisible.value = true;
}

async function handleDelete(id: number) {
  loading.value = true;
  try {
    const res = await deleteMobileMenu(id);
    if (res.data !== null && res.data !== undefined) {
      ElMessage.success('删除成功');
      loadData();
    }
  } finally {
    loading.value = false;
  }
}

async function handleToggleStatus(row: MobileMenu) {
  loading.value = true;
  try {
    const res = await toggleMobileMenuStatus(row.id);
    if (res.data !== null && res.data !== undefined) {
      ElMessage.success(row.status === 1 ? '已禁用' : '已启用');
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
          ElMessage.success(operateType.value === 'add' ? '新增成功' : '修改成功');
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
        <ElFormItem label="菜单名称">
          <ElInput
            v-model="queryForm.menuName"
            placeholder="请输入菜单名称"
            clearable
            style="width: 200px"
            @keyup.enter="handleQuery"
          />
        </ElFormItem>
        <ElFormItem label="状态">
          <ElSelect v-model="queryForm.status" placeholder="菜单状态" clearable style="width: 200px">
            <ElOption label="启用" :value="1" />
            <ElOption label="禁用" :value="0" />
          </ElSelect>
        </ElFormItem>
        <ElFormItem>
          <ElButton type="primary" @click="handleQuery">
            <template #icon><icon-ep-search /></template>
            搜索
          </ElButton>
          <ElButton @click="resetQuery">
            <template #icon><icon-ep-refresh /></template>
            重置
          </ElButton>
        </ElFormItem>
      </ElForm>
    </ElCard>

    <!-- 表格区域 -->
    <ElCard shadow="never" class="table-card">
      <div class="mb-16px">
        <ElButton type="primary" @click="handleAdd">
          <template #icon><icon-ep-plus /></template>
          新增
        </ElButton>
      </div>

      <div class="table-wrapper">
        <ElTable v-loading="loading" :data="tableData" border height="100%">
          <ElTableColumn prop="menuName" label="菜单名称" min-width="120" />
          <ElTableColumn prop="menuCode" label="菜单编码" min-width="100" />
          <ElTableColumn prop="icon" label="图标" width="80" align="center">
            <template #default="{ row }">
              <view class="icon-preview" :style="{ backgroundColor: row.iconBgColor }">
                <UniIcons :type="row.icon" size="16" color="#fff" />
              </view>
            </template>
          </ElTableColumn>
          <ElTableColumn prop="path" label="路由路径" min-width="200" show-overflow-tooltip />
          <ElTableColumn prop="menuGroup" label="分组" width="100" align="center">
            <template #default="{ row }">
              <ElTag :type="row.menuGroup === 'quick' ? 'success' : 'primary'" size="small">
                {{ row.menuGroup === 'quick' ? '快捷功能' : '申请中心' }}
              </ElTag>
            </template>
          </ElTableColumn>
          <ElTableColumn prop="sortOrder" label="排序" width="120" align="center">
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
          <ElTableColumn prop="status" label="状态" width="100" align="center">
            <template #default="{ row }">
              <ElSwitch
                :model-value="row.status === 1"
                inline-prompt
                active-text="启用"
                inactive-text="禁用"
                @change="handleToggleStatus(row)"
              />
            </template>
          </ElTableColumn>
          <ElTableColumn prop="createdTime" label="创建时间" width="160" align="center" />
          <ElTableColumn label="操作" width="150" align="center" fixed="right">
            <template #default="{ row }">
              <ElButton type="primary" link size="small" @click="handleEdit(row)">编辑</ElButton>
              <ElPopconfirm title="确认删除该菜单吗？" @confirm="handleDelete(row.id)">
                <template #reference>
                  <ElButton type="danger" link size="small">删除</ElButton>
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
        <ElFormItem label="菜单名称" prop="menuName">
          <ElInput v-model="formData.menuName" placeholder="请输入菜单名称" />
        </ElFormItem>
        <ElFormItem label="菜单编码" prop="menuCode">
          <ElInput v-model="formData.menuCode" placeholder="请输入菜单编码" />
        </ElFormItem>
        <ElFormItem label="图标" prop="icon">
          <ElSelect v-model="formData.icon" placeholder="请选择图标" style="width: 100%">
            <ElOption v-for="icon in iconOptions" :key="icon" :label="icon" :value="icon">
              <span>{{ icon }}</span>
            </ElOption>
          </ElSelect>
        </ElFormItem>
        <ElFormItem label="图标背景色">
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
        <ElFormItem label="路由路径" prop="path">
          <ElInput v-model="formData.path" placeholder="如：/pages/apply/leave/index" />
        </ElFormItem>
        <ElFormItem label="菜单分组" prop="menuGroup">
          <ElRadioGroup v-model="formData.menuGroup">
            <ElRadio v-for="item in menuGroupOptions" :key="item.value" :value="item.value">
              {{ item.label }}
            </ElRadio>
          </ElRadioGroup>
        </ElFormItem>
        <ElFormItem label="排序">
          <ElInputNumber v-model="formData.sortOrder" :min="0" :max="999" />
        </ElFormItem>
        <ElFormItem label="状态">
          <ElRadioGroup v-model="formData.status">
            <ElRadio :value="1">启用</ElRadio>
            <ElRadio :value="0">禁用</ElRadio>
          </ElRadioGroup>
        </ElFormItem>
      </ElForm>
      <template #footer>
        <ElButton @click="cancel">取消</ElButton>
        <ElButton type="primary" :loading="loading" @click="submitForm">确定</ElButton>
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
