<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue';
import type { FormInstance, FormRules } from 'element-plus';
import { ElMessage } from 'element-plus';
import { request } from '@/service/request';
import { useAuthStore } from '@/store/modules/auth';
import { $t } from '@/locales';

defineOptions({ name: 'UserCenter' });

type Role = {
  id: number;
  roleCode: string;
  roleName: string;
  status?: number;
};

type UserDetail = {
  id: number;
  username: string;
  nickname?: string;
  email?: string;
  phone?: string;
  gender?: number;
  status?: number;
  employeeId?: number;
  createdTime?: string;
  updatedTime?: string;
};

const authStore = useAuthStore();

const loading = ref(false);
const saving = ref(false);
const changingPassword = ref(false);
const detailLoaded = ref(false);
const roleList = ref<Role[]>([]);
const profileFormRef = ref<FormInstance>();
const passwordFormRef = ref<FormInstance>();

const profileForm = reactive({
  id: undefined as number | undefined,
  username: '',
  nickname: '',
  email: '',
  phone: '',
  gender: 0,
  status: 1,
  employeeId: undefined as number | undefined,
  roleIds: [] as number[],
  createdTime: '',
  updatedTime: ''
});

const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
});

const displayName = computed(() => profileForm.nickname || profileForm.username || authStore.userInfo.userName || '-');
const avatarText = computed(() => displayName.value.slice(0, 1).toUpperCase());
const statusType = computed(() => (profileForm.status === 1 ? 'success' : 'info'));
const statusLabel = computed(() => (profileForm.status === 1 ? $t('common.normal') : $t('common.disable')));
const genderLabel = computed(() => {
  const map: Record<number, string> = { 0: $t('common.unknown'), 1: $t('common.male'), 2: $t('common.female') };

  return map[profileForm.gender] || $t('common.unknown');
});

const roleNames = computed(() => {
  const names = profileForm.roleIds
    .map(id => roleList.value.find(role => role.id === id)?.roleName)
    .filter(Boolean) as string[];

  return names.length ? names : authStore.userInfo.roles;
});

const profileRules: FormRules = {
  nickname: [{ required: true, message: $t('common.pleaseEnterNickName'), trigger: 'blur' }],
  email: [{ type: 'email', message: $t('hr.employee.pleaseEnterAValidEmailAddress'), trigger: 'blur' }],
  phone: [
    {
      pattern: /^1[3-9]\d{9}$/,
      message: $t('hr.employee.pleaseEnterAValidPhoneNumber'),
      trigger: 'blur'
    }
  ]
};

const passwordRules: FormRules = {
  oldPassword: [{ required: true, message: $t('userCenter.pleaseEnterCurrentPassword'), trigger: 'blur' }],
  newPassword: [
    { required: true, message: $t('common.pleaseEnterNewPassword'), trigger: 'blur' },
    { min: 6, max: 18, message: $t('userCenter.passwordLengthMustBe618Characters'), trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: $t('userCenter.pleaseEnterNewPasswordAgain'), trigger: 'blur' },
    {
      validator(_rule, value, callback) {
        if (value !== passwordForm.newPassword) {
          callback(new Error($t('userCenter.theTwoNewPasswordsDoNotMatch')));
          return;
        }

        callback();
      },
      trigger: 'blur'
    }
  ]
};

function fillProfile(user: UserDetail, roleIds: number[] = []) {
  profileForm.id = user.id;
  profileForm.username = user.username || authStore.userInfo.userName;
  profileForm.nickname = user.nickname || '';
  profileForm.email = user.email || '';
  profileForm.phone = user.phone || '';
  profileForm.gender = user.gender ?? 0;
  profileForm.status = user.status ?? 1;
  profileForm.employeeId = user.employeeId;
  profileForm.roleIds = roleIds;
  profileForm.createdTime = user.createdTime || '';
  profileForm.updatedTime = user.updatedTime || '';
  detailLoaded.value = true;
}

function fillProfileFallback() {
  const userId = Number(authStore.userInfo.userId);
  const userName = authStore.userInfo.userName || '';

  profileForm.id = Number.isFinite(userId) && userId > 0 ? userId : undefined;
  profileForm.username = userName;
  profileForm.nickname = userName;
  profileForm.email = '';
  profileForm.phone = '';
  profileForm.gender = 0;
  profileForm.status = 1;
  profileForm.employeeId = undefined;
  profileForm.roleIds = [];
  profileForm.createdTime = '';
  profileForm.updatedTime = '';
}

async function loadRoles() {
  const { data, error } = await request<Role[]>({
    url: '/system/role/list',
    method: 'get'
  });

  if (!error && data) {
    roleList.value = data;
  }
}

async function loadProfile() {
  loading.value = true;

  try {
    if (!authStore.userInfo.userId) {
      await authStore.initUserInfo();
    }

    detailLoaded.value = false;
    fillProfileFallback();

    const userId = Number(authStore.userInfo.userId);
    if (!userId) {
      return;
    }

    await loadRoles();

    const { data, error } = await request<{ user: UserDetail; roleIds: number[] }>({
      url: `/system/user/${userId}`,
      method: 'get'
    });

    if (!error && data?.user) {
      fillProfile(data.user, data.roleIds || []);
    }
  } finally {
    loading.value = false;
  }
}

async function saveProfile() {
  if (!profileForm.id || !detailLoaded.value) {
    ElMessage.warning($t('userCenter.currentAccountProfileIsNotLoadedYet'));
    return;
  }

  const valid = await profileFormRef.value?.validate().catch(() => false);
  if (!valid) return;

  saving.value = true;

  try {
    const { error } = await request({
      url: '/system/user',
      method: 'put',
      data: {
        id: profileForm.id,
        username: profileForm.username,
        nickname: profileForm.nickname,
        email: profileForm.email,
        phone: profileForm.phone,
        gender: profileForm.gender,
        status: profileForm.status,
        employeeId: profileForm.employeeId,
        roleIds: profileForm.roleIds
      }
    });

    if (!error) {
      ElMessage.success($t('userCenter.profileSavedSuccessfully'));
      await authStore.initUserInfo();
      await loadProfile();
    }
  } finally {
    saving.value = false;
  }
}

function resetPasswordForm() {
  passwordForm.oldPassword = '';
  passwordForm.newPassword = '';
  passwordForm.confirmPassword = '';
  passwordFormRef.value?.clearValidate();
}

async function changePassword() {
  const valid = await passwordFormRef.value?.validate().catch(() => false);
  if (!valid) return;

  changingPassword.value = true;

  try {
    const { error } = await request({
      url: '/auth/change-password',
      method: 'post',
      data: {
        oldPassword: passwordForm.oldPassword,
        newPassword: passwordForm.newPassword
      }
    });

    if (!error) {
      ElMessage.success($t('userCenter.passwordChangedSuccessfully'));
      resetPasswordForm();
    }
  } finally {
    changingPassword.value = false;
  }
}

onMounted(() => {
  loadProfile();
});
</script>

<template>
  <div class="user-center-page">
    <ElCard class="profile-card" shadow="never">
      <div class="profile-hero">
        <div class="avatar">{{ avatarText }}</div>
        <div class="profile-main">
          <div class="profile-name">{{ displayName }}</div>
          <div class="profile-subtitle">
            <SvgIcon icon="ph:user-circle" />
            <span>{{ profileForm.username || '-' }}</span>
          </div>
        </div>
        <div class="profile-tags">
          <ElTag :type="statusType" effect="light">{{ statusLabel }}</ElTag>
          <ElTag v-if="profileForm.employeeId" type="info" effect="plain">{{ $t('userCenter.employeeId') }} {{ profileForm.employeeId }}</ElTag>
        </div>
      </div>
    </ElCard>

    <ElRow :gutter="16" class="mt-16px">
      <ElCol :lg="8" :md="24" :sm="24">
        <ElCard v-loading="loading" shadow="never" class="info-card">
          <template #header>
            <div class="card-title">
              <SvgIcon icon="ph:identification-card" />
              <span>{{ $t('userCenter.accountInfo') }}</span>
            </div>
          </template>

          <ElDescriptions :column="1" border>
            <ElDescriptionsItem :label="$t('userCenter.loginAccount')">{{ profileForm.username || '-' }}</ElDescriptionsItem>
            <ElDescriptionsItem :label="$t('common.nickname')">{{ profileForm.nickname || '-' }}</ElDescriptionsItem>
            <ElDescriptionsItem :label="$t('common.gender')">{{ genderLabel }}</ElDescriptionsItem>
            <ElDescriptionsItem :label="$t('common.phone')">{{ profileForm.phone || '-' }}</ElDescriptionsItem>
            <ElDescriptionsItem :label="$t('common.email')">{{ profileForm.email || '-' }}</ElDescriptionsItem>
            <ElDescriptionsItem :label="$t('common.createTime')">{{ profileForm.createdTime || '-' }}</ElDescriptionsItem>
          </ElDescriptions>

          <div class="role-list">
            <div class="section-label">{{ $t('userCenter.roles') }}</div>
            <div class="role-tags">
              <ElTag v-for="role in roleNames" :key="role" effect="plain">{{ role }}</ElTag>
              <ElTag v-if="!roleNames.length" type="info" effect="plain">{{ $t('userCenter.noRolesAssigned') }}</ElTag>
            </div>
          </div>
        </ElCard>
      </ElCol>

      <ElCol :lg="16" :md="24" :sm="24">
        <ElCard v-loading="loading" shadow="never" class="form-card">
          <template #header>
            <div class="card-title">
              <SvgIcon icon="ph:user-gear" />
              <span>{{ $t('userCenter.profile') }}</span>
            </div>
          </template>

          <ElForm ref="profileFormRef" :model="profileForm" :rules="profileRules" label-width="88px">
            <ElRow :gutter="16">
              <ElCol :md="12" :sm="24">
                <ElFormItem :label="$t('userCenter.loginAccount')">
                  <ElInput v-model="profileForm.username" disabled />
                </ElFormItem>
              </ElCol>
              <ElCol :md="12" :sm="24">
                <ElFormItem :label="$t('common.nickname')" prop="nickname">
                  <ElInput v-model="profileForm.nickname" maxlength="30" :placeholder="$t('common.pleaseEnterNickName')" />
                </ElFormItem>
              </ElCol>
              <ElCol :md="12" :sm="24">
                <ElFormItem :label="$t('common.gender')">
                  <ElRadioGroup v-model="profileForm.gender">
                    <ElRadioButton :value="0">{{ $t('common.unknown') }}</ElRadioButton>
                    <ElRadioButton :value="1">{{ $t('common.male') }}</ElRadioButton>
                    <ElRadioButton :value="2">{{ $t('common.female') }}</ElRadioButton>
                  </ElRadioGroup>
                </ElFormItem>
              </ElCol>
              <ElCol :md="12" :sm="24">
                <ElFormItem :label="$t('common.phone')" prop="phone">
                  <ElInput v-model="profileForm.phone" maxlength="11" :placeholder="$t('common.pleaseEnterPhoneNumber')" />
                </ElFormItem>
              </ElCol>
              <ElCol :md="12" :sm="24">
                <ElFormItem :label="$t('common.email')" prop="email">
                  <ElInput v-model="profileForm.email" maxlength="80" :placeholder="$t('common.pleaseEnterEmail')" />
                </ElFormItem>
              </ElCol>
            </ElRow>

            <div class="form-actions">
              <ElButton type="primary" :disabled="!detailLoaded" :loading="saving" @click="saveProfile">
                <template #icon><icon-ep-check /></template>
                {{ $t('userCenter.saveProfile') }}
              </ElButton>
            </div>
          </ElForm>
        </ElCard>

        <ElCard shadow="never" class="form-card mt-16px">
          <template #header>
            <div class="card-title">
              <SvgIcon icon="ph:lock-key" />
              <span>{{ $t('userCenter.securitySettings') }}</span>
            </div>
          </template>

          <ElForm ref="passwordFormRef" :model="passwordForm" :rules="passwordRules" label-width="100px">
            <ElRow :gutter="16">
              <ElCol :md="8" :sm="24">
                <ElFormItem :label="$t('userCenter.currentPassword')" prop="oldPassword">
                  <ElInput
                    v-model="passwordForm.oldPassword"
                    type="password"
                    show-password
                    :placeholder="$t('userCenter.pleaseEnterCurrentPassword')"
                  />
                </ElFormItem>
              </ElCol>
              <ElCol :md="8" :sm="24">
                <ElFormItem :label="$t('common.newPassword')" prop="newPassword">
                  <ElInput
                    v-model="passwordForm.newPassword"
                    type="password"
                    show-password
                    :placeholder="$t('common.pleaseEnterNewPassword')"
                  />
                </ElFormItem>
              </ElCol>
              <ElCol :md="8" :sm="24">
                <ElFormItem :label="$t('userCenter.confirmNewPassword')" prop="confirmPassword">
                  <ElInput
                    v-model="passwordForm.confirmPassword"
                    type="password"
                    show-password
                    :placeholder="$t('userCenter.pleaseEnterNewPasswordAgain')"
                  />
                </ElFormItem>
              </ElCol>
            </ElRow>

            <div class="form-actions">
              <ElButton :disabled="changingPassword" @click="resetPasswordForm">{{ $t('common.reset') }}</ElButton>
              <ElButton type="primary" :loading="changingPassword" @click="changePassword">{{ $t('userCenter.changePassword') }}</ElButton>
            </div>
          </ElForm>
        </ElCard>
      </ElCol>
    </ElRow>
  </div>
</template>

<style scoped>
.user-center-page {
  min-height: 100%;
}

.profile-card,
.info-card,
.form-card {
  border-radius: 8px;
}

.profile-hero {
  display: flex;
  align-items: center;
  gap: 18px;
}

.avatar {
  display: flex;
  width: 72px;
  height: 72px;
  flex: 0 0 auto;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background: linear-gradient(135deg, #5b6cff 0%, #16b8a6 100%);
  color: #fff;
  font-size: 32px;
  font-weight: 700;
}

.profile-main {
  min-width: 0;
  flex: 1;
}

.profile-name {
  color: var(--el-text-color-primary);
  font-size: 24px;
  font-weight: 700;
  line-height: 32px;
}

.profile-subtitle,
.card-title {
  display: flex;
  align-items: center;
}

.profile-subtitle {
  gap: 6px;
  margin-top: 8px;
  color: var(--el-text-color-secondary);
  font-size: 14px;
}

.profile-tags {
  display: flex;
  flex-wrap: wrap;
  justify-content: flex-end;
  gap: 8px;
}

.card-title {
  gap: 8px;
  color: var(--el-text-color-primary);
  font-size: 16px;
  font-weight: 600;
}

.role-list {
  margin-top: 18px;
}

.section-label {
  margin-bottom: 10px;
  color: var(--el-text-color-secondary);
  font-size: 13px;
}

.role-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

@media (max-width: 768px) {
  .profile-hero {
    align-items: flex-start;
    flex-direction: column;
  }

  .profile-tags,
  .form-actions {
    justify-content: flex-start;
  }
}
</style>
