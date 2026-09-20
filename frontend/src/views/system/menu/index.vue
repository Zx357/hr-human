<script setup lang="tsx">
import { computed, onMounted, onUnmounted, reactive, ref } from 'vue';
import type { FormInstance, FormRules } from 'element-plus';
import { createMenu, deleteMenu, fetchMenuTree, updateMenu } from '@/service/api';

defineOptions({ name: 'MenuManage' });

// 表格最大高度
const tableMaxHeight = ref(500);

// 计算表格高度
function calcTableHeight() {
  // 窗口高度 - 头部 - 搜索区域 - 工具栏 - 底部边距
  const windowHeight = window.innerHeight;
  tableMaxHeight.value = windowHeight - 280;
}

// 监听窗口大小变化
onMounted(() => {
  calcTableHeight();
  window.addEventListener('resize', calcTableHeight);
});

onUnmounted(() => {
  window.removeEventListener('resize', calcTableHeight);
});

// 搜索表单
const queryForm = reactive({
  menuName: '',
  status: '' as '' | 0 | 1
});

// 数据
const loading = ref(false);
const data = ref<Api.System.Menu[]>([]);
const filteredData = computed(() => {
  if (!queryForm.menuName && queryForm.status === '') {
    return data.value;
  }
  return filterMenuTree(data.value, queryForm.menuName, queryForm.status);
});

// 过滤菜单树
function filterMenuTree(menus: Api.System.Menu[], name: string, status: '' | 0 | 1): Api.System.Menu[] {
  const result: Api.System.Menu[] = [];
  for (const menu of menus) {
    const nameMatch = !name || menu.menuName.includes(name) || (menu.menuNameEn && menu.menuNameEn.includes(name));
    const statusMatch = status === '' || menu.status === status;

    if (menu.children && menu.children.length > 0) {
      const filteredChildren = filterMenuTree(menu.children, name, status);
      if (filteredChildren.length > 0 || (nameMatch && statusMatch)) {
        result.push({
          ...menu,
          children: filteredChildren
        });
      }
    } else if (nameMatch && statusMatch) {
      result.push({ ...menu });
    }
  }
  return result;
}

// 展开/折叠
const isExpandAll = ref(true);
const tableRef = ref();

function toggleExpandAll() {
  isExpandAll.value = !isExpandAll.value;
  toggleRowExpansion(filteredData.value, isExpandAll.value);
}

function toggleRowExpansion(rows: Api.System.Menu[], expanded: boolean) {
  rows.forEach(row => {
    tableRef.value?.toggleRowExpansion(row, expanded);
    if (row.children && row.children.length > 0) {
      toggleRowExpansion(row.children, expanded);
    }
  });
}

// 加载菜单数据
async function loadData() {
  loading.value = true;
  try {
    const { data: result, error } = await fetchMenuTree();
    if (!error && result) {
      data.value = result;
    }
  } finally {
    loading.value = false;
  }
}

// 搜索
function handleQuery() {
  // 搜索时自动展开所有
  isExpandAll.value = true;
}

// 重置搜索
function resetQuery() {
  queryForm.menuName = '';
  queryForm.status = '';
}

onMounted(() => {
  loadData();
});

// 弹窗相关
const dialogVisible = ref(false);
const dialogTitle = ref('');
const operateType = ref<'add' | 'edit'>('add');
const formRef = ref<FormInstance>();

// 表单数据
const formData = reactive<Api.System.MenuForm>({
  parentId: 0,
  menuType: 1,
  menuCode: '',
  menuName: '',
  menuNameEn: '',
  path: '',
  component: '',
  permission: '',
  icon: '',
  sortOrder: 0,
  visible: 1,
  status: 1
});

// 表单验证规则
const rules: FormRules = {
  menuCode: [{ required: true, message: '请输入菜单编码', trigger: 'blur' }],
  menuName: [{ required: true, message: '请输入菜单名称', trigger: 'blur' }],
  sortOrder: [{ required: true, message: '请输入显示排序', trigger: 'blur' }]
};

// 菜单类型选项
const menuTypeOptions = [
  { label: '目录', value: 1 },
  { label: '菜单', value: 2 },
  { label: '按钮', value: 3 }
];

// 重置表单
function resetForm() {
  formData.id = undefined;
  formData.parentId = 0;
  formData.menuType = 1;
  formData.menuCode = '';
  formData.menuName = '';
  formData.menuNameEn = '';
  formData.path = '';
  formData.component = '';
  formData.permission = '';
  formData.icon = '';
  formData.sortOrder = 0;
  formData.visible = 1;
  formData.status = 1;
}

// 新增菜单
function handleAdd(parentId = 0) {
  resetForm();
  operateType.value = 'add';
  dialogTitle.value = '添加菜单';
  formData.parentId = parentId;
  // 如果有父级，默认为菜单类型
  if (parentId !== 0) {
    const parent = findMenuById(data.value, parentId);
    if (parent) {
      // 如果父级是目录，子级默认为菜单
      // 如果父级是菜单，子级默认为按钮
      formData.menuType = parent.menuType === 1 ? 2 : 3;
    }
  }
  dialogVisible.value = true;
}

// 查找菜单
function findMenuById(menus: Api.System.Menu[], id: number): Api.System.Menu | null {
  for (const menu of menus) {
    if (menu.id === id) return menu;
    if (menu.children && menu.children.length > 0) {
      const found = findMenuById(menu.children, id);
      if (found) return found;
    }
  }
  return null;
}

// 编辑菜单
function handleEdit(row: Api.System.Menu) {
  resetForm();
  operateType.value = 'edit';
  dialogTitle.value = '修改菜单';
  Object.assign(formData, {
    id: row.id,
    parentId: row.parentId,
    menuType: row.menuType,
    menuCode: row.menuCode,
    menuName: row.menuName,
    menuNameEn: row.menuNameEn || '',
    path: row.path || '',
    component: row.component || '',
    permission: row.permission || '',
    icon: row.icon || '',
    sortOrder: row.sortOrder,
    visible: row.visible,
    status: row.status
  });
  dialogVisible.value = true;
}

// 删除菜单
async function handleDelete(id: number) {
  loading.value = true;
  try {
    const { error } = await deleteMenu(id);
    if (!error) {
      window.$message?.success('删除成功');
      loadData();
    }
  } finally {
    loading.value = false;
  }
}

// 提交表单
async function submitForm() {
  if (!formRef.value) return;

  await formRef.value.validate(async valid => {
    if (valid) {
      loading.value = true;
      try {
        if (operateType.value === 'add') {
          const { error } = await createMenu(formData);
          if (!error) {
            window.$message?.success('新增成功');
            dialogVisible.value = false;
            loadData();
          }
        } else {
          const { error } = await updateMenu(formData);
          if (!error) {
            window.$message?.success('修改成功');
            dialogVisible.value = false;
            loadData();
          }
        }
      } finally {
        loading.value = false;
      }
    }
  });
}

// 取消
function cancel() {
  dialogVisible.value = false;
  resetForm();
}

// 菜单类型标签
const menuTypeMap: Record<number, { text: string; type: 'primary' | 'success' | 'warning' }> = {
  1: { text: '目录', type: 'primary' },
  2: { text: '菜单', type: 'success' },
  3: { text: '按钮', type: 'warning' }
};

// 常用图标列表
const iconList = [
  'mdi:home',
  'mdi:account',
  'mdi:cog',
  'mdi:file-document',
  'mdi:folder',
  'mdi:chart-bar',
  'mdi:calendar',
  'mdi:clock',
  'mdi:bell',
  'mdi:email',
  'mdi:lock',
  'mdi:shield',
  'mdi:database',
  'mdi:server',
  'mdi:cloud',
  'mdi:download',
  'mdi:upload',
  'mdi:refresh',
  'mdi:magnify',
  'mdi:plus',
  'mdi:minus',
  'mdi:pencil',
  'mdi:delete',
  'mdi:check',
  'mdi:close',
  'mdi:menu',
  'mdi:view-list',
  'mdi:view-grid',
  'mdi:office-building',
  'mdi:domain',
  'mdi:sitemap',
  'mdi:account-group',
  'mdi:badge-account',
  'mdi:briefcase',
  'mdi:clipboard-text',
  'mdi:file-sign',
  'mdi:swap-horizontal',
  'mdi:trophy',
  'mdi:exit-run',
  'mdi:calendar-clock',
  'mdi:calendar-check',
  'mdi:beach',
  'mdi:airplane',
  'mdi:car',
  'mdi:hand-wave',
  'mdi:clipboard-check',
  'mdi:format-list-checks'
];

const showIconPicker = ref(false);

function selectIcon(icon: string) {
  formData.icon = icon;
  showIconPicker.value = false;
}
</script>

<template>
  <div class="min-h-500px flex-col-stretch gap-16px overflow-hidden lt-sm:overflow-auto">
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
            <ElOption label="正常" :value="1" />
            <ElOption label="停用" :value="0" />
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
    <ElCard shadow="never" class="flex flex-col flex-1 overflow-hidden">
      <!-- 工具栏 -->
      <div class="mb-16px flex flex-shrink-0 items-center justify-between">
        <div>
          <ElButton v-permission="'system:menu:add'" type="primary" @click="handleAdd(0)">
            <template #icon><icon-ep-plus /></template>
            新增
          </ElButton>
          <ElButton type="info" plain @click="toggleExpandAll">
            <template #icon><icon-ep-sort /></template>
            {{ isExpandAll ? '折叠' : '展开' }}
          </ElButton>
        </div>
        <div>
          <ElButton circle @click="loadData">
            <template #icon><icon-ep-refresh /></template>
          </ElButton>
        </div>
      </div>

      <!-- 表格 -->
      <ElTable
        ref="tableRef"
        v-loading="loading"
        :data="filteredData"
        border
        row-key="id"
        :default-expand-all="isExpandAll"
        :tree-props="{ children: 'children', hasChildren: 'hasChildren' }"
        :max-height="tableMaxHeight"
        class="flex-1"
      >
        <ElTableColumn prop="menuName" label="菜单名称" min-width="180" show-overflow-tooltip />
        <ElTableColumn prop="icon" label="图标" width="80" align="center">
          <template #default="{ row }">
            <SvgIcon v-if="row.icon" :icon="row.icon" class="text-18px" />
            <span v-else>-</span>
          </template>
        </ElTableColumn>
        <ElTableColumn prop="sortOrder" label="排序" width="80" align="center" />
        <ElTableColumn prop="permission" label="权限标识" min-width="150" show-overflow-tooltip>
          <template #default="{ row }">
            <span>{{ row.permission || '-' }}</span>
          </template>
        </ElTableColumn>
        <ElTableColumn prop="component" label="组件路径" min-width="150" show-overflow-tooltip>
          <template #default="{ row }">
            <span>{{ row.component || '-' }}</span>
          </template>
        </ElTableColumn>
        <ElTableColumn prop="menuType" label="类型" width="80" align="center">
          <template #default="{ row }">
            <ElTag :type="menuTypeMap[row.menuType]?.type || 'info'" size="small">
              {{ menuTypeMap[row.menuType]?.text || '未知' }}
            </ElTag>
          </template>
        </ElTableColumn>
        <ElTableColumn prop="visible" label="可见" width="80" align="center">
          <template #default="{ row }">
            <ElTag :type="row.visible === 1 ? 'success' : 'info'" size="small">
              {{ row.visible === 1 ? '显示' : '隐藏' }}
            </ElTag>
          </template>
        </ElTableColumn>
        <ElTableColumn prop="status" label="状态" width="80" align="center">
          <template #default="{ row }">
            <ElTag :type="row.status === 1 ? 'success' : 'danger'" size="small">
              {{ row.status === 1 ? '正常' : '停用' }}
            </ElTag>
          </template>
        </ElTableColumn>
        <ElTableColumn prop="createTime" label="创建时间" width="160" align="center" />
        <ElTableColumn label="操作" width="200" align="center" fixed="right">
          <template #default="{ row }">
            <ElButton v-permission="'system:menu:edit'" type="primary" link size="small" @click="handleEdit(row)">
              <template #icon><icon-ep-edit /></template>
              修改
            </ElButton>
            <ElButton v-permission="'system:menu:add'" type="primary" link size="small" @click="handleAdd(row.id)">
              <template #icon><icon-ep-plus /></template>
              新增
            </ElButton>
            <ElPopconfirm
              title="确认要删除该菜单吗？"
              confirm-button-text="确定"
              cancel-button-text="取消"
              @confirm="handleDelete(row.id)"
            >
              <template #reference>
                <ElButton v-permission="'system:menu:delete'" type="danger" link size="small">
                  <template #icon><icon-ep-delete /></template>
                  删除
                </ElButton>
              </template>
            </ElPopconfirm>
          </template>
        </ElTableColumn>
      </ElTable>
    </ElCard>

    <!-- 添加/修改菜单对话框 -->
    <ElDialog v-model="dialogVisible" :title="dialogTitle" width="680px" append-to-body>
      <ElForm ref="formRef" :model="formData" :rules="rules" label-width="100px">
        <ElRow :gutter="20">
          <ElCol :span="24">
            <ElFormItem label="上级菜单">
              <ElTreeSelect
                v-model="formData.parentId"
                :data="[{ id: 0, menuName: '主类目', children: data }]"
                :props="{ value: 'id', label: 'menuName', children: 'children' }"
                value-key="id"
                placeholder="选择上级菜单"
                check-strictly
                :render-after-expand="false"
                style="width: 100%"
              />
            </ElFormItem>
          </ElCol>
          <ElCol :span="24">
            <ElFormItem label="菜单类型" required>
              <ElRadioGroup v-model="formData.menuType">
                <ElRadio v-for="item in menuTypeOptions" :key="item.value" :value="item.value">
                  {{ item.label }}
                </ElRadio>
              </ElRadioGroup>
            </ElFormItem>
          </ElCol>
          <ElCol v-if="formData.menuType !== 3" :span="24">
            <ElFormItem label="菜单图标">
              <ElPopover v-model:visible="showIconPicker" placement="bottom-start" :width="400" trigger="click">
                <template #reference>
                  <ElInput v-model="formData.icon" placeholder="点击选择图标" readonly>
                    <template #prefix>
                      <SvgIcon v-if="formData.icon" :icon="formData.icon" class="text-16px" />
                    </template>
                    <template #suffix>
                      <icon-ep-arrow-down />
                    </template>
                  </ElInput>
                </template>
                <div class="icon-picker">
                  <div class="icon-picker-search mb-8px">
                    <ElInput v-model="formData.icon" placeholder="输入图标名称" clearable />
                  </div>
                  <div class="icon-picker-list">
                    <div
                      v-for="icon in iconList"
                      :key="icon"
                      class="icon-picker-item"
                      :class="{ active: formData.icon === icon }"
                      @click="selectIcon(icon)"
                    >
                      <SvgIcon :icon="icon" class="text-20px" />
                    </div>
                  </div>
                </div>
              </ElPopover>
            </ElFormItem>
          </ElCol>
          <ElCol :span="12">
            <ElFormItem label="菜单编码" prop="menuCode">
              <ElInput v-model="formData.menuCode" placeholder="请输入菜单编码" />
            </ElFormItem>
          </ElCol>
          <ElCol :span="12">
            <ElFormItem label="显示排序" prop="sortOrder">
              <ElInputNumber
                v-model="formData.sortOrder"
                :min="0"
                :max="9999"
                controls-position="right"
                style="width: 100%"
              />
            </ElFormItem>
          </ElCol>
          <ElCol :span="12">
            <ElFormItem label="菜单名称" prop="menuName">
              <ElInput v-model="formData.menuName" placeholder="请输入菜单名称（中文）" />
            </ElFormItem>
          </ElCol>
          <ElCol :span="12">
            <ElFormItem label="英文名称">
              <ElInput v-model="formData.menuNameEn" placeholder="请输入菜单名称（英文）" />
            </ElFormItem>
          </ElCol>
          <ElCol v-if="formData.menuType !== 3" :span="12">
            <ElFormItem label="路由地址">
              <ElInput v-model="formData.path" placeholder="请输入路由地址" />
            </ElFormItem>
          </ElCol>
          <ElCol v-if="formData.menuType === 2" :span="12">
            <ElFormItem label="组件路径">
              <ElInput v-model="formData.component" placeholder="请输入组件路径">
                <template #prepend>view.</template>
              </ElInput>
            </ElFormItem>
          </ElCol>
          <ElCol v-if="formData.menuType === 3" :span="12">
            <ElFormItem label="权限标识">
              <ElInput v-model="formData.permission" placeholder="请输入权限标识" />
            </ElFormItem>
          </ElCol>
          <ElCol v-if="formData.menuType !== 3" :span="12">
            <ElFormItem label="显示状态">
              <ElRadioGroup v-model="formData.visible">
                <ElRadio :value="1">显示</ElRadio>
                <ElRadio :value="0">隐藏</ElRadio>
              </ElRadioGroup>
            </ElFormItem>
          </ElCol>
          <ElCol :span="12">
            <ElFormItem label="菜单状态">
              <ElRadioGroup v-model="formData.status">
                <ElRadio :value="1">正常</ElRadio>
                <ElRadio :value="0">停用</ElRadio>
              </ElRadioGroup>
            </ElFormItem>
          </ElCol>
        </ElRow>
      </ElForm>
      <template #footer>
        <div class="dialog-footer">
          <ElButton @click="cancel">取 消</ElButton>
          <ElButton type="primary" :loading="loading" @click="submitForm">确 定</ElButton>
        </div>
      </template>
    </ElDialog>
  </div>
</template>

<style scoped>
.icon-picker-list {
  display: grid;
  grid-template-columns: repeat(8, 1fr);
  gap: 8px;
  max-height: 200px;
  overflow-y: auto;
}

.icon-picker-item {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  border: 1px solid var(--el-border-color);
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.2s;
}

.icon-picker-item:hover {
  border-color: var(--el-color-primary);
  color: var(--el-color-primary);
}

.icon-picker-item.active {
  border-color: var(--el-color-primary);
  background-color: var(--el-color-primary-light-9);
  color: var(--el-color-primary);
}
</style>
