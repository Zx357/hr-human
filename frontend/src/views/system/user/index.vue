<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { request } from '@/service/request';

defineOptions({ name: 'SystemUser' });

// 查询参数
const queryParams = reactive({
  current: 1,
  size: 10,
  username: '',
  nickname: '',
  status: undefined as number | undefined
});

// 表格数据
const tableData = ref<any[]>([]);
const total = ref(0);
const loading = ref(false);

// 角色列表
const roleList = ref<any[]>([]);

// 员工列表
const employeeList = ref<any[]>([]);

// 对话框
const dialogVisible = ref(false);
const dialogTitle = ref('');
const formRef = ref();
const formData = reactive({
  id: undefined as number | undefined,
  username: '',
  password: '',
  nickname: '',
  email: '',
  phone: '',
  gender: 0,
  status: 1,
  employeeId: undefined as number | undefined,
  roleIds: [] as number[]
});

// 表单规则
const formRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  nickname: [{ required: true, message: '请输入昵称', trigger: 'blur' }]
};

// 重置密码对话框
const resetPwdDialogVisible = ref(false);
const resetPwdForm = reactive({
  userId: undefined as number | undefined,
  newPassword: ''
});

// 获取用户列表
async function fetchData() {
  loading.value = true;
  try {
    const { data, error } = await request<any>({
      url: '/system/user/page',
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

// 获取角色列表
async function fetchRoles() {
  const { data, error } = await request<any[]>({
    url: '/system/role/list',
    method: 'get'
  });
  if (!error && data) {
    roleList.value = data;
  }
}

// 搜索
function handleSearch() {
  queryParams.current = 1;
  fetchData();
}

// 重置
function handleReset() {
  queryParams.username = '';
  queryParams.nickname = '';
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
  dialogTitle.value = '新增用户';
  Object.assign(formData, {
    id: undefined,
    username: '',
    password: '',
    nickname: '',
    email: '',
    phone: '',
    gender: 0,
    status: 1,
    employeeId: undefined,
    roleIds: []
  });
  dialogVisible.value = true;
}

// 编辑
async function handleEdit(row: any) {
  dialogTitle.value = '编辑用户';
  const { data, error } = await request<any>({
    url: `/system/user/${row.id}`,
    method: 'get'
  });
  if (!error && data) {
    Object.assign(formData, {
      id: data.user.id,
      username: data.user.username,
      password: '',
      nickname: data.user.nickname,
      email: data.user.email,
      phone: data.user.phone,
      gender: data.user.gender,
      status: data.user.status,
      employeeId: data.user.employeeId,
      roleIds: data.roleIds || []
    });
    dialogVisible.value = true;
  }
}

// 提交表单
async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false);
  if (!valid) return;

  const isEdit = Boolean(formData.id);
  const url = '/system/user';
  const method = isEdit ? 'put' : 'post';

  // 编辑时如果密码为空，不传密码字段
  const submitData = { ...formData };
  if (isEdit && !submitData.password) {
    delete (submitData as any).password;
  }

  const { error } = await request({
    url,
    method,
    data: submitData
  });

  if (!error) {
    ElMessage.success(isEdit ? '更新成功' : '新增成功');
    dialogVisible.value = false;
    fetchData();
  }
}

// 删除
async function handleDelete(row: any) {
  try {
    await ElMessageBox.confirm('确认删除该用户吗？', '提示', {
      type: 'warning'
    });

    const { error } = await request({
      url: `/system/user/${row.id}`,
      method: 'delete'
    });

    if (!error) {
      ElMessage.success('删除成功');
      fetchData();
    } else {
      ElMessage.error('删除失败');
    }
  } catch {
    // 用户取消删除
  }
}

// 修改状态
async function handleStatusChange(row: any) {
  const { error } = await request({
    url: `/system/user/${row.id}/status`,
    method: 'put',
    data: { status: row.status }
  });

  if (!error) {
    ElMessage.success('状态修改成功');
  } else {
    row.status = row.status === 1 ? 0 : 1;
  }
}

// 打开重置密码对话框
function handleResetPwd(row: any) {
  resetPwdForm.userId = row.id;
  resetPwdForm.newPassword = '';
  resetPwdDialogVisible.value = true;
}

// 重置密码
async function submitResetPwd() {
  if (!resetPwdForm.newPassword) {
    ElMessage.warning('请输入新密码');
    return;
  }

  const { error } = await request({
    url: `/system/user/${resetPwdForm.userId}/resetPassword`,
    method: 'post',
    data: { newPassword: resetPwdForm.newPassword }
  });

  if (!error) {
    ElMessage.success('密码重置成功');
    resetPwdDialogVisible.value = false;
  }
}

// 获取员工列表
async function fetchEmployees() {
  const { data, error } = await request<any[]>({
    url: '/employee/list',
    method: 'get',
    params: { status: 1 }
  });
  if (!error && data) {
    employeeList.value = data;
  }
}

onMounted(() => {
  fetchData();
  fetchRoles();
  fetchEmployees();
});
</script>

<template>
  <div class="list-page">
    <!-- 搜索区域 -->
    <ElCard class="search-card">
      <ElForm :model="queryParams" inline>
        <ElFormItem label="用户名">
          <ElInput v-model="queryParams.username" placeholder="请输入用户名" clearable />
        </ElFormItem>
        <ElFormItem label="昵称">
          <ElInput v-model="queryParams.nickname" placeholder="请输入昵称" clearable />
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
    <ElCard class="table-card">
      <template #header>
        <div class="flex items-center justify-between">
          <span>用户列表</span>
          <ElButton v-permission="'system:user:add'" type="primary" @click="handleAdd">
            <template #icon><icon-ep-plus /></template>
            新增
          </ElButton>
        </div>
      </template>

      <div class="table-wrapper">
        <ElTable v-loading="loading" :data="tableData" border stripe height="100%">
          <ElTableColumn prop="id" label="ID" width="80" />
          <ElTableColumn prop="username" label="用户名" width="120" />
          <ElTableColumn prop="nickname" label="昵称" width="120" />
          <ElTableColumn prop="email" label="邮箱" width="180" />
          <ElTableColumn prop="phone" label="手机号" width="130" />
          <ElTableColumn prop="gender" label="性别" width="80">
            <template #default="{ row }">
              <ElTag v-if="row.gender === 1" type="primary">男</ElTag>
              <ElTag v-else-if="row.gender === 2" type="danger">女</ElTag>
              <ElTag v-else type="info">未知</ElTag>
            </template>
          </ElTableColumn>
          <ElTableColumn prop="status" label="状态" width="100">
            <template #default="{ row }">
              <ElSwitch v-model="row.status" :active-value="1" :inactive-value="0" @change="handleStatusChange(row)" />
            </template>
          </ElTableColumn>
          <ElTableColumn prop="createdTime" label="创建时间" width="180" />
          <ElTableColumn label="操作" width="200" fixed="right">
            <template #default="{ row }">
              <ElButton v-permission="'system:user:edit'" type="primary" link @click="handleEdit(row)">编辑</ElButton>
              <ElButton v-permission="'system:user:reset'" type="warning" link @click="handleResetPwd(row)">
                重置密码
              </ElButton>
              <ElButton
                v-if="row.username !== 'admin'"
                v-permission="'system:user:delete'"
                type="danger"
                link
                @click="handleDelete(row)"
              >
                删除
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
    <ElDialog v-model="dialogVisible" :title="dialogTitle" width="600px" destroy-on-close>
      <ElForm ref="formRef" :model="formData" :rules="formRules" label-width="100px">
        <ElFormItem label="用户名" prop="username">
          <ElInput v-model="formData.username" placeholder="请输入用户名" :disabled="!!formData.id" />
        </ElFormItem>
        <ElFormItem v-if="!formData.id" label="密码" prop="password">
          <ElInput v-model="formData.password" type="password" placeholder="请输入密码" show-password />
        </ElFormItem>
        <ElFormItem label="昵称" prop="nickname">
          <ElInput v-model="formData.nickname" placeholder="请输入昵称" />
        </ElFormItem>
        <ElFormItem label="邮箱">
          <ElInput v-model="formData.email" placeholder="请输入邮箱" />
        </ElFormItem>
        <ElFormItem label="手机号">
          <ElInput v-model="formData.phone" placeholder="请输入手机号" />
        </ElFormItem>
        <ElFormItem label="性别">
          <ElRadioGroup v-model="formData.gender">
            <ElRadio :value="0">未知</ElRadio>
            <ElRadio :value="1">男</ElRadio>
            <ElRadio :value="2">女</ElRadio>
          </ElRadioGroup>
        </ElFormItem>
        <ElFormItem label="状态">
          <ElRadioGroup v-model="formData.status">
            <ElRadio :value="1">启用</ElRadio>
            <ElRadio :value="0">禁用</ElRadio>
          </ElRadioGroup>
        </ElFormItem>
        <ElFormItem label="角色">
          <ElSelect v-model="formData.roleIds" multiple placeholder="请选择角色" style="width: 100%">
            <ElOption v-for="role in roleList" :key="role.id" :label="role.roleName" :value="role.id" />
          </ElSelect>
        </ElFormItem>
      </ElForm>
      <template #footer>
        <ElButton @click="dialogVisible = false">取消</ElButton>
        <ElButton type="primary" @click="handleSubmit">确定</ElButton>
      </template>
    </ElDialog>

    <!-- 重置密码对话框 -->
    <ElDialog v-model="resetPwdDialogVisible" title="重置密码" width="400px">
      <ElForm label-width="80px">
        <ElFormItem label="新密码">
          <ElInput v-model="resetPwdForm.newPassword" type="password" placeholder="请输入新密码" show-password />
        </ElFormItem>
      </ElForm>
      <template #footer>
        <ElButton @click="resetPwdDialogVisible = false">取消</ElButton>
        <ElButton type="primary" @click="submitResetPwd">确定</ElButton>
      </template>
    </ElDialog>
  </div>
</template>

<style scoped></style>
