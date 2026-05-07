<script setup lang="ts">
import { computed } from 'vue';
import type { Component } from 'vue';
import { getPaletteColorByNumber, mixColor } from '@sa/color';
import { loginModuleRecord } from '@/constants/app';
import { useAppStore } from '@/store/modules/app';
import { useThemeStore } from '@/store/modules/theme';
import { $t } from '@/locales';
import PwdLogin from './modules/pwd-login.vue';
import CodeLogin from './modules/code-login.vue';
import Register from './modules/register.vue';
import ResetPwd from './modules/reset-pwd.vue';
import BindWechat from './modules/bind-wechat.vue';
import loginBg from '@/assets/imgs/login_bg.png';

defineOptions({ name: 'LoginPage' });

interface Props {
  /** The login module */
  module?: UnionKey.LoginModule;
}

const props = defineProps<Props>();

const appStore = useAppStore();
const themeStore = useThemeStore();

interface LoginModule {
  label: string;
  component: Component;
}

const moduleMap: Record<UnionKey.LoginModule, LoginModule> = {
  'pwd-login': { label: loginModuleRecord['pwd-login'], component: PwdLogin },
  'code-login': { label: loginModuleRecord['code-login'], component: CodeLogin },
  register: { label: loginModuleRecord.register, component: Register },
  'reset-pwd': { label: loginModuleRecord['reset-pwd'], component: ResetPwd },
  'bind-wechat': { label: loginModuleRecord['bind-wechat'], component: BindWechat }
};

const activeModule = computed(() => moduleMap[props.module || 'pwd-login']);

const bgThemeColor = computed(() =>
  themeStore.darkMode ? getPaletteColorByNumber(themeStore.themeColor, 600) : themeStore.themeColor
);

const bgColor = computed(() => {
  const COLOR_WHITE = '#ffffff';
  const ratio = themeStore.darkMode ? 0.3 : 0.05;
  return mixColor(COLOR_WHITE, themeStore.themeColor, ratio);
});
</script>

<template>
  <div class="relative size-full flex overflow-hidden bg-white dark:bg-dark-900 transition-colors duration-300">
    
    <!-- Left panel with abstract background image -->
    <div 
      class="relative hidden flex-1 items-center justify-center bg-cover bg-center bg-no-repeat overflow-hidden md:flex lg:flex-[1.5]" 
      :style="{ backgroundImage: `url(${loginBg})` }"
    >
      <!-- Dark overlay to ensure contrast and premium feel -->
      <div class="absolute inset-0 bg-black/30 backdrop-blur-[2px] transition-all"></div>
      
      <!-- Content on top of background -->
      <div class="relative z-10 flex flex-col items-center justify-center p-12 text-white shadow-2xl backdrop-blur-md rounded-3xl bg-white/10 border border-white/20 transform transition-transform hover:scale-105 duration-700">
        <SystemLogo class="text-100px mb-6 filter drop-shadow-[0_0_15px_rgba(255,255,255,0.4)]" />
        <h2 class="text-4xl font-bold mb-4 tracking-wider text-shadow-sm">{{ $t('system.title') }}</h2>
        <p class="text-lg opacity-85 font-light mt-2 max-w-lg text-center tracking-wide leading-relaxed">
          极致纯粹的视觉体验<br/>构建卓越的现代企业级管理平台
        </p>
      </div>
    </div>

    <!-- Right panel with login form -->
    <div class="relative w-full flex-col min-h-full flex items-center justify-center p-6 md:w-450px lg:w-550px shadow-[-10px_0_30px_rgba(0,0,0,0.05)] z-20" :style="{ backgroundColor: bgColor }">
      
      <!-- Top Right Controls -->
      <div class="absolute top-6 right-6 z-10 flex gap-4">
        <ThemeSchemaSwitch
          :theme-schema="themeStore.themeScheme"
          :show-tooltip="false"
          class="text-22px cursor-pointer hover:text-primary transition-colors"
          @switch="themeStore.toggleThemeScheme"
        />
        <LangSwitch
          v-if="themeStore.header.multilingual.visible"
          :lang="appStore.locale"
          :lang-options="appStore.localeOptions"
          :show-tooltip="false"
          class="text-22px cursor-pointer hover:text-primary transition-colors"
          @change-lang="appStore.changeLocale"
        />
      </div>

      <div class="w-full max-w-400px px-4 sm:px-0">
        <!-- Mobile Header (hidden on large screens) -->
        <header class="flex flex-col items-center md:hidden mb-10">
          <SystemLogo class="text-72px text-primary mb-3" />
          <h3 class="text-28px text-primary font-600 tracking-wide">{{ $t('system.title') }}</h3>
        </header>
        
        <!-- Form Container -->
        <main class="w-full">
          <div class="mb-8">
            <h3 class="text-28px text-primary font-bold mb-2">{{ $t(activeModule.label) }}</h3>
            <p class="text-gray-400 dark:text-gray-500 text-sm tracking-wide">欢迎回来！请登录您的账户继续操作</p>
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
  text-shadow: 0 2px 4px rgba(0,0,0,0.3);
}
</style>
