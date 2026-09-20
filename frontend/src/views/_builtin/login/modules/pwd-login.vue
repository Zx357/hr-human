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
}

const model = ref<FormModel>({
  userName: '',
  password: ''
});

const rules = computed<Record<keyof FormModel, App.Global.FormRule[]>>(() => {
  // inside computed to make locale ref, if not apply i18n, you can define it without computed
  const { formRules } = useFormRules();

  return {
    userName: formRules.userName,
    password: formRules.pwd
  };
});

async function handleSubmit() {
  await validate();
  await authStore.login(model.value.userName, model.value.password);
}

function showForgotPasswordTip() {
  ElMessage.info('请联系系统管理员在后台重置密码');
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
        <ElCheckbox>{{ $t('page.login.pwdLogin.rememberMe') }}</ElCheckbox>
        <ElButton link type="primary" @click="showForgotPasswordTip">忘记密码？</ElButton>
      </div>
      <ElButton type="primary" size="large" round block :loading="authStore.loginLoading" @click="handleSubmit">
        {{ $t('common.confirm') }}
      </ElButton>
    </ElSpace>
  </ElForm>
</template>

<style scoped></style>
