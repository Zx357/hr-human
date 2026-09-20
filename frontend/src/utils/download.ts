import { getAuthorization } from '@/service/request/shared';
import { getServiceBaseURL } from '@/utils/service';

const isHttpProxy = import.meta.env.DEV && import.meta.env.VITE_HTTP_PROXY === 'Y';
const { baseURL } = getServiceBaseURL(import.meta.env, isHttpProxy);

/**
 * 构建查询字符串（跳过 undefined / null / 空字符串）
 */
function buildQuery(params?: Record<string, unknown>): string {
  if (!params) return '';

  const query = new URLSearchParams();
  Object.entries(params).forEach(([key, value]) => {
    if (value !== undefined && value !== null && value !== '') {
      query.append(key, String(value));
    }
  });

  const qs = query.toString();
  return qs ? `?${qs}` : '';
}

/**
 * 下载文件（携带 Authorization 请求头，以 blob 方式触发浏览器下载）
 *
 * @param url 接口路径（不含 baseURL，如 /employee/export）
 * @param fileName 下载保存的文件名
 * @param params 查询参数
 */
export async function downloadFile(url: string, fileName: string, params?: Record<string, unknown>) {
  const response = await fetch(`${baseURL}${url}${buildQuery(params)}`, {
    headers: { Authorization: getAuthorization() || '' }
  });

  if (!response.ok) {
    throw new Error(`下载失败（${response.status}）`);
  }

  // 后端业务错误也是 HTTP 200 + JSON（{code, msg}），只有非 JSON（Excel 二进制）才走 blob 下载
  const contentType = response.headers.get('content-type') || '';
  if (contentType.includes('application/json')) {
    const json = (await response.json()) as { code?: number | string; msg?: string; message?: string };
    const msg = json.msg || json.message || '下载失败';
    window.$message?.error(msg);
    throw new Error(msg);
  }

  const blob = await response.blob();
  const objectUrl = URL.createObjectURL(blob);

  const link = document.createElement('a');
  link.href = objectUrl;
  link.download = fileName;
  document.body.appendChild(link);
  link.click();
  document.body.removeChild(link);
  URL.revokeObjectURL(objectUrl);
}
