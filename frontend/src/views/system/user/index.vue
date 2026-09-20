<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { request } from '@/service/request';
import { $t } from '@/locales';

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
const submitLoading = ref(false);
const resetPwdLoading = ref(false);
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
  username: [{ required: true, message: $t('sys.user.pleaseEnterUserName'), trigger: 'blur' }],
  password: [{ required: true, message: $t('sys.user.pleaseEnterPassword'), trigger: 'blur' }],
  nickname: [{ required: true, message: $t('common.pleaseEnterNickName'), trigger: 'blur' }]
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
  dialogTitle.value = $t('sys.user.addUser');
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
  dialogTitle.value = $t('sys.user.editUser');
  const { data, error } = await request<any>({
    url: `/system/user/${row.id}`,
    method: 'get'
  });
  // 对 data.user 判空，接口返回异常结构时直接提示不打开弹窗
  if (!error && data?.user) {
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

  submitLoading.value = true;
  try {
    const { error } = await request({
      url,
      method,
      data: submitData
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
    await ElMessageBox.confirm($t('sys.user.areYouSureYouWantToDeleteThisUser'), $t('common.tip'), {
      type: 'warning'
    });

    const { error } = await request({
      url: `/system/user/${row.id}`,
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
    await ElMessageBox.confirm($t('sys.user.areYouSureYouWantToThisUser', { action }), $t('common.tip'), {
      type: 'warning'
    });
  } catch {
    row.status = targetStatus === 1 ? 0 : 1;
    return;
  }

  const { error } = await request({
    url: `/system/user/${row.id}/status`,
    method: 'put',
    data: { status: row.status }
  });

  if (!error) {
    ElMessage.success($t('sys.role.statusUpdatedSuccessfully'));
  } else {
    row.status = targetStatus === 1 ? 0 : 1;
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
    ElMessage.warning($t('common.pleaseEnterNewPassword'));
    return;
  }
  // 与后端规则一致：6-20 位且同时包含字母和数字
  if (!/^(?=.*[A-Za-z])(?=.*\d).{6,20}$/.test(resetPwdForm.newPassword)) {
    ElMessage.warning($t('sys.user.passwordMustBe620CharactersAndContainBothLettersAndDigits'));
    return;
  }

  resetPwdLoading.value = true;
  try {
    const { error } = await request({
      url: `/system/user/${resetPwdForm.userId}/resetPassword`,
      method: 'post',
      data: { newPassword: resetPwdForm.newPassword }
    });

    if (!error) {
      ElMessage.success($t('sys.user.passwordResetSuccessfully'));
      resetPwdDialogVisible.value = false;
    }
  } finally {
    resetPwdLoading.value = false;
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
        <ElFormItem :label="$t('common.username')">
          <ElInput v-model="queryParams.username" :placeholder="$t('sys.user.pleaseEnterUserName')" clearable @keyup.enter="handleSearch" />
        </ElFormItem>
        <ElFormItem :label="$t('common.nickname')">
          <ElInput v-model="queryParams.nickname" :placeholder="$t('common.pleaseEnterNickName')" clearable @keyup.enter="handleSearch" />
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
          <span>{{ $t('sys.user.userList') }}</span>
          <ElButton v-permission="'system:user:add'" type="primary" @click="handleAdd">
            <template #icon><icon-ep-plus /></template>
            {{ $t('common.add') }}
          </ElButton>
        </div>
      </template>

      <div class="table-wrapper">
        <ElTable v-loading="loading" :data="tableData" border stripe height="100%">
          <ElTableColumn prop="id" label="ID" width="80" />
          <ElTableColumn prop="username" :label="$t('common.username')" width="120" />
          <ElTableColumn prop="nickname" :label="$t('common.nickname')" width="120" />
          <ElTableColumn prop="email" :label="$t('common.email')" width="180" />
          <ElTableColumn prop="phone" :label="$t('common.phone')" width="130" />
          <ElTableColumn prop="gender" :label="$t('common.gender')" width="80">
            <template #default="{ row }">
              <ElTag v-if="row.gender === 1" type="primary">{{ $t('common.male') }}</ElTag>
              <ElTag v-else-if="row.gender === 2" type="danger">{{ $t('common.female') }}</ElTag>
              <ElTag v-else type="info">{{ $t('common.unknown') }}</ElTag>
            </template>
          </ElTableColumn>
          <ElTableColumn prop="status" :label="$t('common.status')" width="100">
            <template #default="{ row }">
              <ElSwitch v-permission="'system:user:edit'" v-model="row.status" :active-value="1" :inactive-value="0" @change="handleStatusChange(row)" />
            </template>
          </ElTableColumn>
          <ElTableColumn prop="createdTime" :label="$t('common.createTime')" width="180" />
          <ElTableColumn :label="$t('common.action')" width="200" fixed="right">
            <template #default="{ row }">
              <ElButton v-permission="'system:user:edit'" type="primary" link @click="handleEdit(row)">{{ $t('common.edit') }}</ElButton>
              <ElButton v-permission="'system:user:reset'" type="warning" link @click="handleResetPwd(row)">
                {{ $t('sys.user.resetPassword') }}
              </ElButton>
              <ElButton
                v-if="row.username !== 'admin'"
                v-permission="'system:user:delete'"
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
    <ElDialog v-model="dialogVisible" :title="dialogTitle" width="600px" destroy-on-close>
      <ElForm ref="formRef" :model="formData" :rules="formRules" label-width="100px">
        <ElFormItem :label="$t('common.username')" prop="username">
          <ElInput v-model="formData.username" :placeholder="$t('sys.user.pleaseEnterUserName')" :disabled="!!formData.id" />
        </ElFormItem>
        <ElFormItem v-if="!formData.id" :label="$t('common.password')" prop="password">
          <ElInput v-model="formData.password" type="password" :placeholder="$t('sys.user.pleaseEnterPassword')" show-password />
        </ElFormItem>
        <ElFormItem :label="$t('common.nickname')" prop="nickname">
          <ElInput v-model="formData.nickname" :placeholder="$t('common.pleaseEnterNickName')" />
        </ElFormItem>
        <ElFormItem :label="$t('common.email')">
          <ElInput v-model="formData.email" :placeholder="$t('common.pleaseEnterEmail')" />
        </ElFormItem>
        <ElFormItem :label="$t('common.phone')">
          <ElInput v-model="formData.phone" :placeholder="$t('common.pleaseEnterPhoneNumber')" />
        </ElFormItem>
        <ElFormItem :label="$t('common.gender')">
          <ElRadioGroup v-model="formData.gender">
            <ElRadio :value="0">{{ $t('common.unknown') }}</ElRadio>
            <ElRadio :value="1">{{ $t('common.male') }}</ElRadio>
            <ElRadio :value="2">{{ $t('common.female') }}</ElRadio>
          </ElRadioGroup>
        </ElFormItem>
        <ElFormItem :label="$t('common.status')">
          <ElRadioGroup v-model="formData.status">
            <ElRadio :value="1">{{ $t('common.enable') }}</ElRadio>
            <ElRadio :value="0">{{ $t('common.disable') }}</ElRadio>
          </ElRadioGroup>
        </ElFormItem>
        <ElFormItem :label="$t('common.role')">
          <ElSelect v-model="formData.roleIds" multiple :placeholder="$t('approval.flow.pleaseSelectARole')" style="width: 100%">
            <ElOption v-for="role in roleList" :key="role.id" :label="role.roleName" :value="role.id" />
          </ElSelect>
        </ElFormItem>
      </ElForm>
      <template #footer>
        <ElButton @click="dialogVisible = false">{{ $t('common.cancel') }}</ElButton>
        <ElButton type="primary" :loading="submitLoading" @click="handleSubmit">{{ $t('common.ok') }}</ElButton>
      </template>
    </ElDialog>

    <!-- 重置密码对话框 -->
    <ElDialog v-model="resetPwdDialogVisible" :title="$t('sys.user.resetPassword')" width="400px">
      <ElForm label-width="80px">
        <ElFormItem :label="$t('common.newPassword')">
          <ElInput v-model="resetPwdForm.newPassword" type="password" :placeholder="$t('common.pleaseEnterNewPassword')" show-password />
        </ElFormItem>
      </ElForm>
      <template #footer>
        <ElButton @click="resetPwdDialogVisible = false">{{ $t('common.cancel') }}</ElButton>
        <ElButton type="primary" :loading="resetPwdLoading" @click="submitResetPwd">{{ $t('common.ok') }}</ElButton>
      </template>
    </ElDialog>
  </div>
</template>

<style scoped></style>
