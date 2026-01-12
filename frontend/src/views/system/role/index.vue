<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { request } from '@/service/request';

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
      1: '[目录]',
      2: '[菜单]',
      3: '[按钮]'
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
  { label: '全部数据', value: 1 },
  { label: '本公司数据', value: 2 },
  { label: '本部门数据', value: 3 },
  { label: '本部门及以下数据', value: 4 },
  { label: '仅本人数据', value: 5 },
  { label: '自定义数据', value: 6 }
];

// 对话框
const dialogVisible = ref(false);
const dialogTitle = ref('');
const formRef = ref();
const menuTreeRef = ref();
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
  roleCode: [{ required: true, message: '请输入角色编码', trigger: 'blur' }],
  roleName: [{ required: true, message: '请输入角色名称', trigger: 'blur' }]
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
  dialogTitle.value = '新增角色';
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
  setTimeout(() => {
    menuTreeRef.value?.setCheckedKeys([]);
  }, 100);
}

// 编辑
async function handleEdit(row: any) {
  dialogTitle.value = '编辑角色';
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
    setTimeout(() => {
      const menuIds = data.menuIds || [];
      // 过滤出叶子节点（没有子节点的菜单）
      const leafMenuIds = filterLeafMenuIds(menuIds, menuTreeData.value);
      menuTreeRef.value?.setCheckedKeys(leafMenuIds);
    }, 100);
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

  console.log('提交角色数据:', {
    formData: { ...formData },
    checkedKeys,
    halfCheckedKeys,
    menuIds
  });

  const { error, data } = await request({
    url,
    method,
    data: { ...formData, menuIds }
  });

  console.log('提交结果:', { error, data });

  if (!error) {
    ElMessage.success(isEdit ? '更新成功' : '新增成功');
    dialogVisible.value = false;
    fetchData();
  }
}

// 删除
async function handleDelete(row: any) {
  try {
    await ElMessageBox.confirm('确认删除该角色吗？', '提示', {
      type: 'warning'
    });

    console.log('删除角色请求:', row.id);
    const { error, data } = await request({
      url: `/system/role/${row.id}`,
      method: 'delete'
    });

    console.log('删除角色响应:', { error, data });

    if (!error) {
      ElMessage.success('删除成功');
      fetchData();
    } else {
      ElMessage.error('删除失败');
    }
  } catch {
    // 用户取消删除
    console.log('用户取消删除');
  }
}

// 修改状态
async function handleStatusChange(row: any) {
  const { error } = await request({
    url: `/system/role/${row.id}/status`,
    method: 'put',
    data: { status: row.status }
  });

  if (!error) {
    ElMessage.success('状态修改成功');
  } else {
    row.status = row.status === 1 ? 0 : 1;
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
  <div class="p-16px">
    <!-- 搜索区域 -->
    <ElCard class="mb-16px">
      <ElForm :model="queryParams" inline>
        <ElFormItem label="角色名称">
          <ElInput v-model="queryParams.roleName" placeholder="请输入角色名称" clearable />
        </ElFormItem>
        <ElFormItem label="角色编码">
          <ElInput v-model="queryParams.roleCode" placeholder="请输入角色编码" clearable />
        </ElFormItem>
        <ElFormItem label="状态">
          <ElSelect v-model="queryParams.status" placeholder="请选择状态" clearable style="width: 120px">
            <ElOption label="启用" :value="1" />
            <ElOption label="禁用" :value="0" />
          </ElSelect>
        </ElFormItem>
        <ElFormItem>
          <ElButton type="primary" @click="handleSearch">
            <template #icon><icon-ep-search /></template>
            搜索
          </ElButton>
          <ElButton @click="handleReset">
            <template #icon><icon-ep-refresh /></template>
            重置
          </ElButton>
        </ElFormItem>
      </ElForm>
    </ElCard>

    <!-- 表格区域 -->
    <ElCard>
      <template #header>
        <div class="flex justify-between items-center">
          <span>角色列表</span>
          <ElButton v-permission="'system:role:add'" type="primary" @click="handleAdd">
            <template #icon><icon-ep-plus /></template>
            新增
          </ElButton>
        </div>
      </template>

      <ElTable v-loading="loading" :data="tableData" border stripe>
        <ElTableColumn prop="id" label="ID" width="80" />
        <ElTableColumn prop="roleCode" label="角色编码" width="120" />
        <ElTableColumn prop="roleName" label="角色名称" width="120" />
        <ElTableColumn prop="dataScope" label="数据权限" width="140">
          <template #default="{ row }">
            <ElTag :type="row.dataScope === 1 ? 'success' : row.dataScope === 5 ? 'warning' : 'primary'">
              {{ dataScopeOptions.find(o => o.value === row.dataScope)?.label || '全部数据' }}
            </ElTag>
          </template>
        </ElTableColumn>
        <ElTableColumn prop="description" label="描述" min-width="150" />
        <ElTableColumn prop="sortOrder" label="排序" width="80" />
        <ElTableColumn prop="status" label="状态" width="100">
          <template #default="{ row }">
            <ElSwitch
              v-model="row.status"
              :active-value="1"
              :inactive-value="0"
              @change="handleStatusChange(row)"
            />
          </template>
        </ElTableColumn>
        <ElTableColumn prop="createdTime" label="创建时间" width="180" />
        <ElTableColumn label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <ElButton v-permission="'system:role:edit'" type="primary" link @click="handleEdit(row)">编辑</ElButton>
            <ElButton
              v-if="row.roleCode !== 'ROLE_ADMIN'"
              v-permission="'system:role:delete'"
              type="danger"
              link
              @click="handleDelete(row)"
            >删除</ElButton>
          </template>
        </ElTableColumn>
      </ElTable>

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
            <ElFormItem label="角色编码" prop="roleCode">
              <ElInput v-model="formData.roleCode" placeholder="请输入角色编码" />
            </ElFormItem>
          </ElCol>
          <ElCol :span="12">
            <ElFormItem label="角色名称" prop="roleName">
              <ElInput v-model="formData.roleName" placeholder="请输入角色名称" />
            </ElFormItem>
          </ElCol>
        </ElRow>
        <ElRow :gutter="20">
          <ElCol :span="12">
            <ElFormItem label="排序">
              <ElInputNumber v-model="formData.sortOrder" :min="0" style="width: 100%" />
            </ElFormItem>
          </ElCol>
          <ElCol :span="12">
            <ElFormItem label="状态">
              <ElRadioGroup v-model="formData.status">
                <ElRadio :value="1">启用</ElRadio>
                <ElRadio :value="0">禁用</ElRadio>
              </ElRadioGroup>
            </ElFormItem>
          </ElCol>
        </ElRow>
        <ElFormItem label="数据权限">
          <ElSelect v-model="formData.dataScope" placeholder="请选择数据权限" style="width: 100%">
            <ElOption
              v-for="item in dataScopeOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </ElSelect>
        </ElFormItem>
        <ElFormItem label="描述">
          <ElInput v-model="formData.description" type="textarea" :rows="2" placeholder="请输入描述" />
        </ElFormItem>
        <ElFormItem label="菜单权限">
          <div class="mb-8px">
            <ElCheckbox v-model="menuExpandAll" @change="handleExpandAll">展开/折叠</ElCheckbox>
            <ElCheckbox v-model="menuCheckAll" @change="handleCheckAll">全选/全不选</ElCheckbox>
            <ElCheckbox v-model="menuCheckStrictly">父子联动</ElCheckbox>
          </div>
          <div class="w-full border border-gray-200 rounded p-8px max-h-300px overflow-auto">
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
        <ElButton @click="dialogVisible = false">取消</ElButton>
        <ElButton type="primary" @click="handleSubmit">确定</ElButton>
      </template>
    </ElDialog>
  </div>
</template>

<style scoped></style>
