<script setup lang="ts">
import { computed } from 'vue';
import type { Component } from 'vue';
import { mixColor } from '@sa/color';
import { loginModuleRecord } from '@/constants/app';
import { useAppStore } from '@/store/modules/app';
import { useThemeStore } from '@/store/modules/theme';
import loginBg from '@/assets/imgs/login_bg.png';
import { $t } from '@/locales';
import PwdLogin from './modules/pwd-login.vue';

defineOptions({ name: 'LoginPage' });

const appStore = useAppStore();
const themeStore = useThemeStore();

interface LoginModule {
  label: string;
  component: Component;
}

// 仅保留账号密码登录，其它登录模块（code-login/register/reset-pwd/bind-wechat）入口已移除
const activeModule = computed<LoginModule>(() => ({ label: loginModuleRecord['pwd-login'], component: PwdLogin }));

const bgColor = computed(() => {
  const COLOR_WHITE = '#ffffff';
  const ratio = themeStore.darkMode ? 0.3 : 0.05;
  return mixColor(COLOR_WHITE, themeStore.themeColor, ratio);
});
</script>

<template>
  <div class="relative size-full flex overflow-hidden bg-white transition-colors duration-300 dark:bg-dark-900">
    <!-- Left panel with abstract background image -->
    <div
      class="relative hidden flex-1 items-center justify-center overflow-hidden bg-cover bg-center bg-no-repeat md:flex lg:flex-[1.5]"
      :style="{ backgroundImage: `url(${loginBg})` }"
    >
      <!-- Dark overlay to ensure contrast and premium feel -->
      <div class="absolute inset-0 bg-black/30 backdrop-blur-[2px] transition-all"></div>

      <!-- Content on top of background -->
      <div
        class="relative z-10 flex flex-col transform items-center justify-center border border-white/20 rounded-3xl bg-white/10 p-12 text-white shadow-2xl backdrop-blur-md transition-transform duration-700 hover:scale-105"
      >
        <SystemLogo class="mb-6 text-100px drop-shadow-[0_0_15px_rgba(255,255,255,0.4)] filter" />
        <h2 class="mb-4 text-4xl font-bold tracking-wider text-shadow-sm">{{ $t('system.title') }}</h2>
        <p class="mt-2 max-w-lg text-center text-lg font-light leading-relaxed tracking-wide opacity-85">
          人力资源数字化管理平台
          <br />
          组织 · 员工 · 考勤 · 审批一站式管理
        </p>
      </div>
    </div>

    <!-- Right panel with login form -->
    <div
      class="relative z-20 min-h-full w-full flex flex-col items-center justify-center p-6 shadow-[-10px_0_30px_rgba(0,0,0,0.05)] lg:w-550px md:w-450px"
      :style="{ backgroundColor: bgColor }"
    >
      <!-- Top Right Controls -->
      <div class="absolute right-6 top-6 z-10 flex gap-4">
        <ThemeSchemaSwitch
          :theme-schema="themeStore.themeScheme"
          :show-tooltip="false"
          class="cursor-pointer text-22px transition-colors hover:text-primary"
          @switch="themeStore.toggleThemeScheme"
        />
        <LangSwitch
          v-if="themeStore.header.multilingual.visible"
          :lang="appStore.locale"
          :lang-options="appStore.localeOptions"
          :show-tooltip="false"
          class="cursor-pointer text-22px transition-colors hover:text-primary"
          @change-lang="appStore.changeLocale"
        />
      </div>

      <div class="max-w-400px w-full px-4 sm:px-0">
        <!-- Mobile Header (hidden on large screens) -->
        <header class="mb-10 flex flex-col items-center md:hidden">
          <SystemLogo class="mb-3 text-72px text-primary" />
          <h3 class="text-28px text-primary font-600 tracking-wide">{{ $t('system.title') }}</h3>
        </header>

        <!-- Form Container -->
        <main class="w-full">
          <div class="mb-8">
            <h3 class="mb-2 text-28px text-primary font-bold">{{ $t(activeModule.label) }}</h3>
            <p class="text-sm text-gray-400 tracking-wide dark:text-gray-500">{{ $t('page.login.common.loginSubtitle') }}</p>
          </div>

          <div class="w-full">
            <Transition :name="themeStore.page.animateMode" mode="out-in" appear>
              <component :is="activeModule.component" />
            </Transition>
          </div>
        </main>
      </div>
    </div>
  </div>
</template>

<style scoped>
.text-shadow-sm {
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.3);
}
</style>
