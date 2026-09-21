<script setup lang="ts">
import { computed, ref } from 'vue';
import { ElMessage } from 'element-plus';
import { useAuthStore } from '@/store/modules/auth';
import { useForm, useFormRules } from '@/hooks/common/form';
import { $t } from '@/locales';

defineOptions({ name: 'PwdLogin' });

const authStore = useAuthStore();
const { formRef, validate } = useForm();

interface FormModel {
  userName: string;
  password: string;
  rememberMe: boolean;
}

// 记住我：仅记住用户名，凭据不落盘
const REMEMBER_KEY = 'pwd-login-remember-username';

const model = ref<FormModel>({
  userName: localStorage.getItem(REMEMBER_KEY) || '',
  password: '',
  rememberMe: !!localStorage.getItem(REMEMBER_KEY)
});

const rules = computed<Record<keyof FormModel, App.Global.FormRule[]>>(() => {
  // inside computed to make locale ref, if not apply i18n, you can define it without computed
  const { formRules } = useFormRules();

  return {
    userName: formRules.userName,
    password: formRules.pwd
  } as Record<keyof FormModel, App.Global.FormRule[]>;
});

async function handleSubmit() {
  await validate();
  if (model.value.rememberMe) {
    localStorage.setItem(REMEMBER_KEY, model.value.userName);
  } else {
    localStorage.removeItem(REMEMBER_KEY);
  }
  await authStore.login(model.value.userName, model.value.password);
}

function showForgotPasswordTip() {
  ElMessage.info($t('page.login.pwdLogin.contactAdminReset'));
}
</script>

<template>
  <ElForm ref="formRef" :model="model" :rules="rules" size="large" :show-label="false" @keyup.enter="handleSubmit">
    <ElFormItem prop="userName">
      <ElInput v-model="model.userName" :placeholder="$t('page.login.common.userNamePlaceholder')" />
    </ElFormItem>
    <ElFormItem prop="password">
      <ElInput
        v-model="model.password"
        type="password"
        show-password-on="click"
        :placeholder="$t('page.login.common.passwordPlaceholder')"
      />
    </ElFormItem>
    <ElSpace direction="vertical" :size="24" class="w-full" fill>
      <div class="flex-y-center justify-between">
        <ElCheckbox v-model="model.rememberMe">{{ $t('page.login.pwdLogin.rememberMe') }}</ElCheckbox>
        <ElButton link type="primary" @click="showForgotPasswordTip">{{ $t('page.login.pwdLogin.forgetPassword') }}</ElButton>
      </div>
      <ElButton type="primary" size="large" round block :loading="authStore.loginLoading" @click="handleSubmit">
        {{ $t('common.confirm') }}
      </ElButton>
    </ElSpace>
  </ElForm>
</template>

<style scoped></style>
