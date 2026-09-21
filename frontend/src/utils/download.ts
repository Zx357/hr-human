import { request } from '@/service/request';
import { useAuthStore } from '@/store/modules/auth';
import { $t } from '@/locales';

/** blob 响应下后端返回业务错误（HTTP 200 + JSON）时，响应体已被转成普通对象 */
interface BackendFailPayload {
  code?: number | string;
  msg?: string;
  message?: string;
}

function isBackendFailPayload(data: unknown): data is BackendFailPayload {
  return data !== null && data !== undefined && typeof data === 'object' && !(data instanceof Blob) && 'code' in data;
}

/**
 * 触发浏览器保存 blob 文件
 */
function saveBlob(blob: Blob, fileName: string) {
  const objectUrl = URL.createObjectURL(blob);
  const link = document.createElement('a');

  link.href = objectUrl;
  link.download = fileName;
  document.body.appendChild(link);
  link.click();
  document.body.removeChild(link);
  URL.revokeObjectURL(objectUrl);
}

/**
 * 下载文件（走统一 request 实例：自动携带鉴权、token 过期刷新/登出、错误统一提示）
 *
 * @param url 接口路径（不含 baseURL，如 /employee/export）
 * @param fileName 下载保存的文件名
 * @param params 查询参数
 */
export async function downloadFile(url: string, fileName: string, params?: Record<string, unknown>) {
  const { data, error, response } = await request<Blob, 'blob'>({ url, method: 'get', params, responseType: 'blob' });

  if (error) {
    // HTTP 401：登录已过期/未登录，走统一过期处理（清空登录态回登录页），与 request 层 logoutCodes 行为一致
    if (response?.status === 401) {
      useAuthStore().resetStore();
    }
    // 网络错误/HTTP 错误信息已由请求层统一 toast，这里仅抛出让调用方感知失败
    throw new Error(error.message || 'download failed');
  }

  if (isBackendFailPayload(data)) {
    // 后端业务失败（HTTP 200 + JSON），blob 模式不会经过统一错误处理，这里补一条提示
    const msg = data.msg || data.message || $t('common.downloadFailed');
    window.$message?.error(msg);
    throw new Error(msg);
  }

  if (!(data instanceof Blob)) {
    window.$message?.error($t('common.downloadFailed'));
    throw new Error($t('common.downloadFailed'));
  }

  saveBlob(data, fileName);
}
