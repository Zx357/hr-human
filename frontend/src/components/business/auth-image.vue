<script setup lang="ts">
import { onBeforeUnmount, ref, watch } from 'vue';
import { getAuthorization } from '@/service/request/shared';
import { getServiceBaseURL } from '@/utils/service';
import { $t } from '@/locales';

defineOptions({ name: 'AuthImage' });

const props = withDefaults(
  defineProps<{
    /** 图片相对路径（如 /id_card_front/xxx.png）或完整 URL */
    url?: string | null;
    /** el-image 的 fit 模式 */
    fit?: 'cover' | 'contain' | 'fill' | 'none' | 'scale-down';
    /** 加载失败提示文字 */
    fallbackText?: string;
    /** 是否支持点击放大预览 */
    preview?: boolean;
  }>(),
  {
    url: '',
    fit: 'cover',
    fallbackText: $t('component.authImage.failedToLoadImage'),
    preview: false
  }
);

const SENSITIVE_PREFIXES = ['/id_card_front/', '/id_card_back/', '/contract_photo/', '/diploma_photo/', '/cert_photo/'];

const objectUrl = ref('');
const failed = ref(false);
let currentUrl = '';

const isSensitive = (path: string) => SENSITIVE_PREFIXES.some(prefix => path.startsWith(prefix));

async function load(path: string) {
  failed.value = false;
  release();

  if (!path) return;

  if (!isSensitive(path)) {
    // 头像/公开图片：直接走原地址（getFileUrl 拼接的完整 URL）
    currentUrl = path;
    objectUrl.value = path;
    return;
  }

  // 敏感证件图片：带 Authorization 以 blob 方式加载
  try {
    const response = await fetch(path, {
      headers: { Authorization: getAuthorization() || '' }
    });
    if (!response.ok) throw new Error($t('component.authImage.loadFailed', { status: response.status }));
    const contentType = response.headers.get('content-type') || '';
    if (contentType.includes('application/json')) {
      throw new Error($t('component.authImage.failedToLoadImage'));
    }
    const blob = await response.blob();
    currentUrl = URL.createObjectURL(blob);
    objectUrl.value = currentUrl;
  } catch {
    failed.value = true;
  }
}

function release() {
  if (currentUrl.startsWith('blob:')) {
    URL.revokeObjectURL(currentUrl);
  }
  currentUrl = '';
  objectUrl.value = '';
}

watch(
  () => props.url,
  path => {
    // 传入的可能是相对路径，补上 baseURL
    if (path && isSensitive(path)) {
      const isHttpProxy = import.meta.env.DEV && import.meta.env.VITE_HTTP_PROXY === 'Y';
      const { baseURL } = getServiceBaseURL(import.meta.env, isHttpProxy);
      load(path.startsWith('http') ? path : `${baseURL}${path}`);
    } else {
      load(path || '');
    }
  },
  { immediate: true }
);

onBeforeUnmount(release);
</script>

<template>
  <ElImage
    v-if="objectUrl && !failed"
    :src="objectUrl"
    :fit="fit"
    class="auth-image"
    :preview-src-list="preview ? [objectUrl] : undefined"
    preview-teleported
    teleported
    hide-on-click-modal
  />
  <div v-else-if="failed" class="auth-image auth-image__fallback">
    <span>{{ fallbackText }}</span>
  </div>
  <ElImage v-else :fit="fit" class="auth-image">
    <template #placeholder>
      <div class="auth-image__fallback"><span>{{ $t('common.loadingDot') }}</span></div>
    </template>
    <template #error>
      <div class="auth-image__fallback"><span>{{ $t('component.authImage.noImage') }}</span></div>
    </template>
  </ElImage>
</template>

<style scoped>
.auth-image {
  width: 100%;
  height: 100%;
  display: block;
}

.auth-image:deep(img) {
  cursor: pointer;
}

.auth-image__fallback {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--el-fill-color-light);
  color: var(--el-text-color-secondary);
  font-size: 12px;
  border-radius: 4px;
}
</style>
