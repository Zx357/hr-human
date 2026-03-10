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
 * 上传员工头像
 * @param file 图片文件
 * @param employeeNo 员工工号
 */
export function uploadEmployeeAvatar(file: File, employeeNo: string) {
  const formData = new FormData();
  formData.append('file', file);
  formData.append('employeeNo', employeeNo);
  return request<string>({
    url: '/file/upload/employee/avatar',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  });
}

/**
 * 上传员工身份证照片
 * @param file 图片文件
 * @param employeeNo 员工工号
 * @param type 类型：front-正面，back-反面
 */
export function uploadEmployeeIdCard(file: File, employeeNo: string, type: 'front' | 'back') {
  const formData = new FormData();
  formData.append('file', file);
  formData.append('employeeNo', employeeNo);
  formData.append('type', type);
  return request<string>({
    url: '/file/upload/employee/idcard',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  });
}

/**
 * 上传毕业证照片
 * @param file 图片文件
 * @param employeeNo 员工工号
 */
export function uploadDiplomaPhoto(file: File, employeeNo: string) {
  const formData = new FormData();
  formData.append('file', file);
  formData.append('employeeNo', employeeNo);
  return request<string>({
    url: '/file/upload/diploma',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  });
}

/**
 * 上传证书照片
 * @param file 图片文件
 * @param employeeNo 员工工号
 */
export function uploadCertPhoto(file: File, employeeNo: string) {
  const formData = new FormData();
  formData.append('file', file);
  formData.append('employeeNo', employeeNo);
  return request<string>({
    url: '/file/upload/certificate',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  });
}

/**
 * 上传合同照片
 * @param file 图片文件
 * @param employeeNo 员工工号
 */
export function uploadContractPhoto(file: File, employeeNo: string) {
  const formData = new FormData();
  formData.append('file', file);
  formData.append('employeeNo', employeeNo);
  return request<string>({
    url: '/file/upload/contract',
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
