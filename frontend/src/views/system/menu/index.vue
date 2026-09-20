<script setup lang="tsx">
import { computed, onMounted, onUnmounted, reactive, ref } from 'vue';
import type { FormInstance, FormRules } from 'element-plus';
import { createMenu, deleteMenu, fetchMenuTree, updateMenu } from '@/service/api';
import { $t } from '@/locales';

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
  menuCode: [{ required: true, message: $t('sys.menu.pleaseEnterMenuCode'), trigger: 'blur' }],
  menuName: [{ required: true, message: $t('sys.common.pleaseEnterMenuName'), trigger: 'blur' }],
  sortOrder: [{ required: true, message: $t('sys.menu.pleaseEnterDisplayOrder'), trigger: 'blur' }]
};

// 菜单类型选项
const menuTypeOptions = [
  { label: $t('sys.common.directory'), value: 1 },
  { label: $t('sys.common.menu'), value: 2 },
  { label: $t('common.button'), value: 3 }
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
  dialogTitle.value = $t('sys.menu.addMenu');
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
  dialogTitle.value = $t('sys.menu.modifyMenu');
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
      window.$message?.success($t('common.deleteSuccess'));
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
            window.$message?.success($t('common.addSuccess'));
            dialogVisible.value = false;
            loadData();
          }
        } else {
          const { error } = await updateMenu(formData);
          if (!error) {
            window.$message?.success($t('common.modifySuccess'));
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
  1: { text: $t('sys.common.directory'), type: 'primary' },
  2: { text: $t('sys.common.menu'), type: 'success' },
  3: { text: $t('common.button'), type: 'warning' }
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
            <ElOption :label="$t('common.normal')" :value="1" />
            <ElOption :label="$t('common.deactivate')" :value="0" />
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
    <ElCard shadow="never" class="flex flex-col flex-1 overflow-hidden">
      <!-- 工具栏 -->
      <div class="mb-16px flex flex-shrink-0 items-center justify-between">
        <div>
          <ElButton v-permission="'system:menu:add'" type="primary" @click="handleAdd(0)">
            <template #icon><icon-ep-plus /></template>
            {{ $t('common.add') }}
          </ElButton>
          <ElButton type="info" plain @click="toggleExpandAll">
            <template #icon><icon-ep-sort /></template>
            {{ isExpandAll ? $t('common.collapse') : $t('common.expand') }}
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
        <ElTableColumn prop="menuName" :label="$t('sys.common.menuName')" min-width="180" show-overflow-tooltip />
        <ElTableColumn prop="icon" :label="$t('common.icon')" width="80" align="center">
          <template #default="{ row }">
            <SvgIcon v-if="row.icon" :icon="row.icon" class="text-18px" />
            <span v-else>-</span>
          </template>
        </ElTableColumn>
        <ElTableColumn prop="sortOrder" :label="$t('common.sort')" width="80" align="center" />
        <ElTableColumn prop="permission" :label="$t('sys.menu.permission')" min-width="150" show-overflow-tooltip>
          <template #default="{ row }">
            <span>{{ row.permission || '-' }}</span>
          </template>
        </ElTableColumn>
        <ElTableColumn prop="component" :label="$t('sys.menu.componentPath')" min-width="150" show-overflow-tooltip>
          <template #default="{ row }">
            <span>{{ row.component || '-' }}</span>
          </template>
        </ElTableColumn>
        <ElTableColumn prop="menuType" :label="$t('common.type')" width="80" align="center">
          <template #default="{ row }">
            <ElTag :type="menuTypeMap[row.menuType]?.type || 'info'" size="small">
              {{ menuTypeMap[row.menuType]?.text || $t('common.unknown') }}
            </ElTag>
          </template>
        </ElTableColumn>
        <ElTableColumn prop="visible" :label="$t('sys.menu.visible')" width="80" align="center">
          <template #default="{ row }">
            <ElTag :type="row.visible === 1 ? 'success' : 'info'" size="small">
              {{ row.visible === 1 ? $t('common.show') : $t('common.hide') }}
            </ElTag>
          </template>
        </ElTableColumn>
        <ElTableColumn prop="status" :label="$t('common.status')" width="80" align="center">
          <template #default="{ row }">
            <ElTag :type="row.status === 1 ? 'success' : 'danger'" size="small">
              {{ row.status === 1 ? $t('common.normal') : $t('common.deactivate') }}
            </ElTag>
          </template>
        </ElTableColumn>
        <ElTableColumn prop="createTime" :label="$t('common.createTime')" width="160" align="center" />
        <ElTableColumn :label="$t('common.action')" width="200" align="center" fixed="right">
          <template #default="{ row }">
            <ElButton v-permission="'system:menu:edit'" type="primary" link size="small" @click="handleEdit(row)">
              <template #icon><icon-ep-edit /></template>
              {{ $t('common.modify') }}
            </ElButton>
            <ElButton v-permission="'system:menu:add'" type="primary" link size="small" @click="handleAdd(row.id)">
              <template #icon><icon-ep-plus /></template>
              {{ $t('common.add') }}
            </ElButton>
            <ElPopconfirm
              :title="$t('sys.menu.areYouSureYouWantToDeleteThisMenu')"
              :confirm-button-text="$t('common.ok')"
              :cancel-button-text="$t('common.cancel')"
              @confirm="handleDelete(row.id)"
            >
              <template #reference>
                <ElButton v-permission="'system:menu:delete'" type="danger" link size="small">
                  <template #icon><icon-ep-delete /></template>
                  {{ $t('common.delete') }}
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
            <ElFormItem :label="$t('sys.menu.parentMenu')">
              <ElTreeSelect
                v-model="formData.parentId"
                :data="[{ id: 0, menuName: $t('sys.menu.rootCategory'), children: data }]"
                :props="{ value: 'id', label: 'menuName', children: 'children' }"
                value-key="id"
                :placeholder="$t('sys.menu.selectParentMenu')"
                check-strictly
                :render-after-expand="false"
                style="width: 100%"
              />
            </ElFormItem>
          </ElCol>
          <ElCol :span="24">
            <ElFormItem :label="$t('sys.common.menuType')" required>
              <ElRadioGroup v-model="formData.menuType">
                <ElRadio v-for="item in menuTypeOptions" :key="item.value" :value="item.value">
                  {{ item.label }}
                </ElRadio>
              </ElRadioGroup>
            </ElFormItem>
          </ElCol>
          <ElCol v-if="formData.menuType !== 3" :span="24">
            <ElFormItem :label="$t('sys.menu.menuIcon')">
              <ElPopover v-model:visible="showIconPicker" placement="bottom-start" :width="400" trigger="click">
                <template #reference>
                  <ElInput v-model="formData.icon" :placeholder="$t('sys.menu.clickToSelectAnIcon')" readonly>
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
                    <ElInput v-model="formData.icon" :placeholder="$t('sys.menu.enterIconName')" clearable />
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
            <ElFormItem :label="$t('sys.common.menuCode')" prop="menuCode">
              <ElInput v-model="formData.menuCode" :placeholder="$t('sys.menu.pleaseEnterMenuCode')" />
            </ElFormItem>
          </ElCol>
          <ElCol :span="12">
            <ElFormItem :label="$t('sys.menu.displayOrder')" prop="sortOrder">
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
            <ElFormItem :label="$t('sys.common.menuName')" prop="menuName">
              <ElInput v-model="formData.menuName" :placeholder="$t('sys.menu.pleaseEnterMenuNameChinese')" />
            </ElFormItem>
          </ElCol>
          <ElCol :span="12">
            <ElFormItem :label="$t('sys.dict.englishName')">
              <ElInput v-model="formData.menuNameEn" :placeholder="$t('sys.menu.pleaseEnterMenuNameEnglish')" />
            </ElFormItem>
          </ElCol>
          <ElCol v-if="formData.menuType !== 3" :span="12">
            <ElFormItem :label="$t('sys.menu.routeAddress')">
              <ElInput v-model="formData.path" :placeholder="$t('sys.menu.pleaseEnterRouteAddress')" />
            </ElFormItem>
          </ElCol>
          <ElCol v-if="formData.menuType === 2" :span="12">
            <ElFormItem :label="$t('sys.menu.componentPath')">
              <ElInput v-model="formData.component" :placeholder="$t('sys.menu.pleaseEnterComponentPath')">
                <template #prepend>view.</template>
              </ElInput>
            </ElFormItem>
          </ElCol>
          <ElCol v-if="formData.menuType === 3" :span="12">
            <ElFormItem :label="$t('sys.menu.permission')">
              <ElInput v-model="formData.permission" :placeholder="$t('sys.menu.pleaseEnterPermission')" />
            </ElFormItem>
          </ElCol>
          <ElCol v-if="formData.menuType !== 3" :span="12">
            <ElFormItem :label="$t('sys.menu.visibleStatus')">
              <ElRadioGroup v-model="formData.visible">
                <ElRadio :value="1">{{ $t('common.show') }}</ElRadio>
                <ElRadio :value="0">{{ $t('common.hide') }}</ElRadio>
              </ElRadioGroup>
            </ElFormItem>
          </ElCol>
          <ElCol :span="12">
            <ElFormItem :label="$t('sys.common.menuStatus')">
              <ElRadioGroup v-model="formData.status">
                <ElRadio :value="1">{{ $t('common.normal') }}</ElRadio>
                <ElRadio :value="0">{{ $t('common.deactivate') }}</ElRadio>
              </ElRadioGroup>
            </ElFormItem>
          </ElCol>
        </ElRow>
      </ElForm>
      <template #footer>
        <div class="dialog-footer">
          <ElButton @click="cancel">{{ $t('common.cancel') }}</ElButton>
          <ElButton type="primary" :loading="loading" @click="submitForm">{{ $t('common.ok') }}</ElButton>
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
