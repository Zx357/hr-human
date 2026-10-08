import { request } from '../request';

/** 系统参数配置 */
export interface SysConfig {
  id: number;
  /** 配置键，如 map.amap.key */
  configKey: string;
  /** 配置名称 */
  configName: string;
  /** 配置值 */
  configValue: string;
  /** 分组，如 map_config / wechat_config */
  configGroup?: string;
  /** 是否公开（1=公开，登录即可通过公开接口读取） */
  isPublic: number;
  /** 说明 */
  remark?: string;
  /** 状态（1=启用 0=停用） */
  status: number;
  /** 排序 */
  sortOrder?: number;
}

/** 获取参数配置列表 */
export function fetchConfigList() {
  return request<SysConfig[]>({ url: '/system/config/list' });
}

/** 新增参数配置 */
export function createConfig(data: Partial<SysConfig>) {
  return request<boolean>({
    url: '/system/config',
    method: 'post',
    data
  });
}

/** 更新参数配置 */
export function updateConfig(data: Partial<SysConfig>) {
  return request<boolean>({
    url: '/system/config',
    method: 'put',
    data
  });
}

/** 删除参数配置 */
export function deleteConfig(id: number) {
  return request<boolean>({ url: `/system/config/${id}`, method: 'delete' });
}

/** 获取公开配置（只返回有值的公开配置，登录即可访问） */
export function fetchPublicConfigs(keys: string[]) {
  return request<Record<string, string>>({
    url: '/config/public',
    method: 'get',
    params: { keys: keys.join(',') }
  });
}

/** 公开配置模块级缓存：同一组 key 只拉取一次（失败也按空结果缓存，避免反复请求） */
const publicConfigCache = new Map<string, Record<string, string>>();
const publicConfigPending = new Map<string, Promise<Record<string, string>>>();

/** 读取公开配置（带模块级缓存），失败时返回空对象 */
export function getPublicConfigs(keys: string[]): Promise<Record<string, string>> {
  const cacheKey = [...keys].sort().join(',');
  const cached = publicConfigCache.get(cacheKey);
  if (cached) {
    return Promise.resolve(cached);
  }

  let pending = publicConfigPending.get(cacheKey);
  if (!pending) {
    pending = fetchPublicConfigs(keys)
      .then(({ data, error }) => {
        const result = !error && data ? data : {};
        publicConfigCache.set(cacheKey, result);
        return result;
      })
      .catch(() => {
        publicConfigCache.set(cacheKey, {});
        return {};
      })
      .finally(() => {
        publicConfigPending.delete(cacheKey);
      });
    publicConfigPending.set(cacheKey, pending);
  }

  return pending;
}
