<script setup lang="ts">
import { nextTick, onMounted, reactive, ref } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { request } from '@/service/request';
import { $t } from '@/locales';

defineOptions({ name: 'SystemRole' });

// 查询参数
const queryParams = reactive({
  current: 1,
  size: 10,
  roleName: '',
  roleCode: '',
  status: undefined as number | undefined
});

// 表格数据
const tableData = ref<any[]>([]);
const total = ref(0);
const loading = ref(false);

// 菜单树数据
const menuTreeData = ref<any[]>([]);
const menuTreeProps = {
  children: 'children',
  label: (data: any) => {
    // 根据菜单类型显示不同的标签
    const typeLabels: Record<number, string> = {
      1: $t('sys.role.directory'),
      2: $t('sys.role.menu'),
      3: $t('sys.role.button')
    };
    const typeLabel = typeLabels[data.menuType] || '';
    return `${data.menuName} ${typeLabel}`;
  }
};

// 菜单树控制
const menuExpandAll = ref(true);
const menuCheckAll = ref(false);
const menuCheckStrictly = ref(true);

// 数据权限选项
const dataScopeOptions = [
  { label: $t('sys.role.allData'), value: 1 },
  { label: $t('sys.role.currentCompanyData'), value: 2 },
  { label: $t('sys.role.currentDepartmentData'), value: 3 },
  { label: $t('sys.role.currentDepartmentAndBelow'), value: 4 },
  { label: $t('sys.role.selfOnly'), value: 5 },
  { label: $t('sys.role.customData'), value: 6 }
];

// 对话框
const dialogVisible = ref(false);
const dialogTitle = ref('');
const formRef = ref();
const menuTreeRef = ref();
const submitLoading = ref(false);
const formData = reactive({
  id: undefined as number | undefined,
  roleCode: '',
  roleName: '',
  description: '',
  status: 1,
  sortOrder: 0,
  dataScope: 1,
  menuIds: [] as number[]
});

// 表单规则
const formRules = {
  roleCode: [{ required: true, message: $t('sys.role.pleaseEnterRoleCode'), trigger: 'blur' }],
  roleName: [{ required: true, message: $t('sys.role.pleaseEnterRoleName'), trigger: 'blur' }]
};

// 获取角色列表
async function fetchData() {
  loading.value = true;
  try {
    const { data, error } = await request<any>({
      url: '/system/role/page',
      method: 'get',
      params: queryParams
    });
    if (!error && data) {
      tableData.value = data.records || [];
      total.value = data.total || 0;
    }
  } finally {
    loading.value = false;
  }
}

// 获取菜单树
async function fetchMenuTree() {
  const { data, error } = await request<any[]>({
    url: '/system/menu/tree',
    method: 'get'
  });
  if (!error && data) {
    menuTreeData.value = data;
  }
}

// 搜索
function handleSearch() {
  queryParams.current = 1;
  fetchData();
}

// 重置
function handleReset() {
  queryParams.roleName = '';
  queryParams.roleCode = '';
  queryParams.status = undefined;
  handleSearch();
}

// 分页变化
function handlePageChange(page: number) {
  queryParams.current = page;
  fetchData();
}

function handleSizeChange(size: number) {
  queryParams.size = size;
  queryParams.current = 1;
  fetchData();
}

// 新增
function handleAdd() {
  dialogTitle.value = $t('sys.role.addRole');
  Object.assign(formData, {
    id: undefined,
    roleCode: '',
    roleName: '',
    description: '',
    status: 1,
    sortOrder: 0,
    dataScope: 1,
    menuIds: []
  });
  dialogVisible.value = true;
  // 清空菜单树选中
  nextTick(() => {
    menuTreeRef.value?.setCheckedKeys([]);
  });
}

// 编辑
async function handleEdit(row: any) {
  dialogTitle.value = $t('sys.role.editRole');
  const { data, error } = await request<any>({
    url: `/system/role/${row.id}`,
    method: 'get'
  });
  if (!error && data) {
    Object.assign(formData, {
      id: data.role.id,
      roleCode: data.role.roleCode,
      roleName: data.role.roleName,
      description: data.role.description,
      status: data.role.status,
      sortOrder: data.role.sortOrder,
      dataScope: data.role.dataScope || 1,
      menuIds: data.menuIds || []
    });
    dialogVisible.value = true;
    // 设置菜单树选中 - 只设置叶子节点，避免父子联动问题
    nextTick(() => {
      const menuIds = data.menuIds || [];
      // 过滤出叶子节点（没有子节点的菜单）
      const leafMenuIds = filterLeafMenuIds(menuIds, menuTreeData.value);
      menuTreeRef.value?.setCheckedKeys(leafMenuIds);
    });
  }
}

// 过滤出叶子节点ID
function filterLeafMenuIds(menuIds: number[], menus: any[]): number[] {
  const allMenuMap = new Map<number, any>();
  buildMenuMap(menus, allMenuMap);

  return menuIds.filter(id => {
    const menu = allMenuMap.get(id);
    return menu && (!menu.children || menu.children.length === 0);
  });
}

// 构建菜单ID到菜单对象的映射
function buildMenuMap(menus: any[], map: Map<number, any>) {
  for (const menu of menus) {
    map.set(menu.id, menu);
    if (menu.children && menu.children.length > 0) {
      buildMenuMap(menu.children, map);
    }
  }
}

// 提交表单
async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false);
  if (!valid) return;

  const isEdit = Boolean(formData.id);
  const url = '/system/role';
  const method = isEdit ? 'put' : 'post';

  // 获取选中的菜单ID（包括半选中的父节点）
  const checkedKeys = menuTreeRef.value?.getCheckedKeys() || [];
  const halfCheckedKeys = menuTreeRef.value?.getHalfCheckedKeys() || [];
  const menuIds = [...checkedKeys, ...halfCheckedKeys];

  submitLoading.value = true;
  try {
    const { error } = await request({
      url,
      method,
      data: { ...formData, menuIds }
    });

    if (!error) {
      ElMessage.success(isEdit ? $t('common.updateSuccess') : $t('common.addSuccess'));
      dialogVisible.value = false;
      fetchData();
    }
  } finally {
    submitLoading.value = false;
  }
}

// 删除
async function handleDelete(row: any) {
  try {
    await ElMessageBox.confirm($t('sys.role.areYouSureYouWantToDeleteThisRole'), $t('common.tip'), {
      type: 'warning'
    });

    const { error } = await request({
      url: `/system/role/${row.id}`,
      method: 'delete'
    });

    if (!error) {
      ElMessage.success($t('common.deleteSuccess'));
      fetchData();
    }
  } catch {
    // 用户取消删除
  }
}

// 修改状态：先确认再调接口，取消或失败时回滚开关状态
async function handleStatusChange(row: any) {
  const targetStatus = row.status;
  const action = targetStatus === 1 ? $t('common.enable') : $t('common.disable');
  try {
    await ElMessageBox.confirm($t('sys.role.areYouSureYouWantToThisRole', { action }), $t('common.tip'), {
      type: 'warning'
    });
  } catch {
    row.status = targetStatus === 1 ? 0 : 1;
    return;
  }

  const { error } = await request({
    url: `/system/role/${row.id}/status`,
    method: 'put',
    data: { status: row.status }
  });

  if (!error) {
    ElMessage.success($t('sys.role.statusUpdatedSuccessfully'));
  } else {
    row.status = targetStatus === 1 ? 0 : 1;
  }
}

// 展开/折叠所有节点
function handleExpandAll(val: boolean | string | number) {
  const nodes = menuTreeRef.value?.store?.nodesMap || {};
  Object.keys(nodes).forEach(key => {
    nodes[key].expanded = Boolean(val);
  });
}

// 全选/全不选
function handleCheckAll(val: boolean | string | number) {
  if (val) {
    // 获取所有节点的ID
    const allIds = getAllMenuIds(menuTreeData.value);
    menuTreeRef.value?.setCheckedKeys(allIds);
  } else {
    menuTreeRef.value?.setCheckedKeys([]);
  }
}

// 递归获取所有菜单ID
function getAllMenuIds(menus: any[]): number[] {
  const ids: number[] = [];
  for (const menu of menus) {
    ids.push(menu.id);
    if (menu.children && menu.children.length > 0) {
      ids.push(...getAllMenuIds(menu.children));
    }
  }
  return ids;
}

onMounted(() => {
  fetchData();
  fetchMenuTree();
});
</script>

<template>
  <div class="list-page">
    <!-- 搜索区域 -->
    <ElCard class="search-card">
      <ElForm :model="queryParams" inline>
        <ElFormItem :label="$t('sys.role.roleName')">
          <ElInput v-model="queryParams.roleName" :placeholder="$t('sys.role.pleaseEnterRoleName')" clearable @keyup.enter="handleSearch" />
        </ElFormItem>
        <ElFormItem :label="$t('sys.role.roleCode')">
          <ElInput v-model="queryParams.roleCode" :placeholder="$t('sys.role.pleaseEnterRoleCode')" clearable @keyup.enter="handleSearch" />
        </ElFormItem>
        <ElFormItem :label="$t('common.status')">
          <ElSelect v-model="queryParams.status" :placeholder="$t('common.pleaseSelectStatus')" clearable style="width: 120px">
            <ElOption :label="$t('common.enable')" :value="1" />
            <ElOption :label="$t('common.disable')" :value="0" />
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
    <ElCard class="table-card">
      <template #header>
        <div class="flex items-center justify-between">
          <span>{{ $t('sys.common.roleList') }}</span>
          <ElButton v-permission="'system:role:add'" type="primary" @click="handleAdd">
            <template #icon><icon-ep-plus /></template>
            {{ $t('common.add') }}
          </ElButton>
        </div>
      </template>

      <div class="table-wrapper">
        <ElTable v-loading="loading" :data="tableData" border stripe height="100%">
          <ElTableColumn prop="id" label="ID" width="80" />
          <ElTableColumn prop="roleCode" :label="$t('sys.role.roleCode')" width="120" />
          <ElTableColumn prop="roleName" :label="$t('sys.role.roleName')" width="120" />
          <ElTableColumn prop="dataScope" :label="$t('sys.role.dataScope')" width="140">
            <template #default="{ row }">
              <ElTag :type="row.dataScope === 1 ? 'success' : row.dataScope === 5 ? 'warning' : 'primary'">
                {{ dataScopeOptions.find(o => o.value === row.dataScope)?.label || $t('sys.role.allData') }}
              </ElTag>
            </template>
          </ElTableColumn>
          <ElTableColumn prop="description" :label="$t('common.description')" min-width="150" show-overflow-tooltip />
          <ElTableColumn prop="sortOrder" :label="$t('common.sort')" width="80" />
          <ElTableColumn prop="status" :label="$t('common.status')" width="100">
            <template #default="{ row }">
              <ElSwitch v-permission="'system:role:edit'" v-model="row.status" :active-value="1" :inactive-value="0" @change="handleStatusChange(row)" />
            </template>
          </ElTableColumn>
          <ElTableColumn prop="createdTime" :label="$t('common.createTime')" width="180" />
          <ElTableColumn :label="$t('common.action')" width="150" fixed="right">
            <template #default="{ row }">
              <ElButton v-permission="'system:role:edit'" type="primary" link @click="handleEdit(row)">{{ $t('common.edit') }}</ElButton>
              <ElButton
                v-if="row.roleCode !== 'ROLE_ADMIN'"
                v-permission="'system:role:delete'"
                type="danger"
                link
                @click="handleDelete(row)"
              >
                {{ $t('common.delete') }}
              </ElButton>
            </template>
          </ElTableColumn>
        </ElTable>
      </div>

      <div class="mt-16px flex justify-end">
        <ElPagination
          v-model:current-page="queryParams.current"
          v-model:page-size="queryParams.size"
          :total="total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @current-change="handlePageChange"
          @size-change="handleSizeChange"
        />
      </div>
    </ElCard>

    <!-- 新增/编辑对话框 -->
    <ElDialog v-model="dialogVisible" :title="dialogTitle" width="700px" destroy-on-close>
      <ElForm ref="formRef" :model="formData" :rules="formRules" label-width="100px">
        <ElRow :gutter="20">
          <ElCol :span="12">
            <ElFormItem :label="$t('sys.role.roleCode')" prop="roleCode">
              <ElInput v-model="formData.roleCode" :placeholder="$t('sys.role.pleaseEnterRoleCode')" />
            </ElFormItem>
          </ElCol>
          <ElCol :span="12">
            <ElFormItem :label="$t('sys.role.roleName')" prop="roleName">
              <ElInput v-model="formData.roleName" :placeholder="$t('sys.role.pleaseEnterRoleName')" />
            </ElFormItem>
          </ElCol>
        </ElRow>
        <ElRow :gutter="20">
          <ElCol :span="12">
            <ElFormItem :label="$t('common.sort')">
              <ElInputNumber v-model="formData.sortOrder" :min="0" style="width: 100%" />
            </ElFormItem>
          </ElCol>
          <ElCol :span="12">
            <ElFormItem :label="$t('common.status')">
              <ElRadioGroup v-model="formData.status">
                <ElRadio :value="1">{{ $t('common.enable') }}</ElRadio>
                <ElRadio :value="0">{{ $t('common.disable') }}</ElRadio>
              </ElRadioGroup>
            </ElFormItem>
          </ElCol>
        </ElRow>
        <ElFormItem :label="$t('sys.role.dataScope')">
          <ElSelect v-model="formData.dataScope" :placeholder="$t('sys.role.pleaseSelectDataScope')" style="width: 100%">
            <ElOption v-for="item in dataScopeOptions" :key="item.value" :label="item.label" :value="item.value" />
          </ElSelect>
        </ElFormItem>
        <ElFormItem :label="$t('common.description')">
          <ElInput v-model="formData.description" type="textarea" :rows="2" :placeholder="$t('approval.flow.pleaseEnterDescription')" />
        </ElFormItem>
        <ElFormItem :label="$t('sys.role.menuPermission')">
          <div class="mb-8px">
            <ElCheckbox v-model="menuExpandAll" @change="handleExpandAll">{{ $t('common.expandCollapse') }}</ElCheckbox>
            <ElCheckbox v-model="menuCheckAll" @change="handleCheckAll">{{ $t('common.selectAllOrNone') }}</ElCheckbox>
            <ElCheckbox v-model="menuCheckStrictly">{{ $t('sys.role.cascadeParentChild') }}</ElCheckbox>
          </div>
          <div class="max-h-300px w-full overflow-auto border border-gray-200 rounded p-8px">
            <ElTree
              ref="menuTreeRef"
              :data="menuTreeData"
              :props="menuTreeProps"
              show-checkbox
              node-key="id"
              :default-expand-all="menuExpandAll"
              :check-strictly="!menuCheckStrictly"
            />
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

<style scoped></style>
