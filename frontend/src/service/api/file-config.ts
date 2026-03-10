import { request } from '../request';

export interface FileConfig {
  id: number;
  configKey: string;
  configName: string;
  configValue: string;
  remark?: string;
  createdTime?: string;
  updatedTime?: string;
}

/** 获取所有路径配置 */
export function fetchFileConfigList() {
  return request<FileConfig[]>({ url: '/system/file-config/list' });
}

/** 获取配置详情 */
export function fetchFileConfigById(id: number) {
  return request<FileConfig>({ url: `/system/file-config/${id}` });
}

/** 更新路径配置 */
export function updateFileConfig(data: Partial<FileConfig>) {
  return request<boolean>({
    url: '/system/file-config',
    method: 'put',
    data
  });
}

/** 刷新路径配置缓存 */
export function refreshFileConfig() {
  return request<boolean>({
    url: '/system/file-config/refresh',
    method: 'post'
  });
}
