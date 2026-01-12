import { request } from '../request';

/**
 * 上传文件
 * @param file 文件
 */
export function uploadFile(file: File) {
  const formData = new FormData();
  formData.append('file', file);
  return request<string>({
    url: '/file/upload',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  });
}

/**
 * 上传图片
 * @param file 图片文件
 */
export function uploadImage(file: File) {
  const formData = new FormData();
  formData.append('file', file);
  return request<string>({
    url: '/file/upload/image',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  });
}

/**
 * 删除文件
 * @param path 文件路径
 */
export function deleteFile(path: string) {
  return request<boolean>({
    url: '/file/delete',
    method: 'delete',
    params: { path }
  });
}

/**
 * 获取文件完整URL
 * @param path 相对路径
 */
export function getFileUrl(path: string | undefined | null): string {
  if (!path) return '';
  if (path.startsWith('http://') || path.startsWith('https://')) {
    return path;
  }
  // 使用后端API地址
  const baseUrl = import.meta.env.VITE_SERVICE_BASE_URL || '';
  return `${baseUrl}${path}`;
}
