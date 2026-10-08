import { AMAP_KEY, AMAP_SECURITY_CODE } from '@/constants/map-sdk';
import { getPublicConfigs } from '@/service/api/config';

/** 后台「系统参数配置」中高德相关配置键 */
export const AMAP_CONFIG_KEY = 'map.amap.key';
export const AMAP_SECURITY_CODE_CONFIG_KEY = 'map.amap.security-code';

/** 高德 JS API 使用 GCJ-02 坐标系（与后端打卡点存储一致），无需坐标系转换 */
export interface AmapConfig {
  key: string;
  securityCode: string;
}

let amapConfigPromise: Promise<AmapConfig> | null = null;
let amapApiPromise: Promise<any> | null = null;

/**
 * 解析高德配置：后台公开配置 map.amap.key / map.amap.security-code > env VITE_AMAP_* > 空
 *
 * 结果模块级缓存，同一会话只请求一次；拉取失败按无 key 处理（回退 env）。
 */
export function resolveAmapConfig(): Promise<AmapConfig> {
  if (!amapConfigPromise) {
    amapConfigPromise = getPublicConfigs([AMAP_CONFIG_KEY, AMAP_SECURITY_CODE_CONFIG_KEY])
      .then(configs => ({
        key: configs[AMAP_CONFIG_KEY]?.trim() || AMAP_KEY,
        securityCode: configs[AMAP_SECURITY_CODE_CONFIG_KEY]?.trim() || AMAP_SECURITY_CODE
      }))
      .catch(() => ({ key: AMAP_KEY, securityCode: AMAP_SECURITY_CODE }));
  }

  return amapConfigPromise;
}

function buildAmapUrl(key: string, callbackName: string) {
  const params = [
    'v=2.0',
    `key=${encodeURIComponent(key)}`,
    `callback=${encodeURIComponent(callbackName)}`,
    'plugin=AMap.Geocoder,AMap.PlaceSearch'
  ];
  return `https://webapi.amap.com/maps?${params.join('&')}`;
}

/**
 * 动态加载高德 JS API 2.0（含地理编码/POI 搜索插件），单例防重复加载。
 *
 * 2021-12-02 之后申请的 Key 必须配套安全密钥：通过 window._AMapSecurityConfig 注入，
 * 且必须在 SDK 脚本加载前设置；旧 Key 无需安全密钥，可空。
 */
export function loadAmapApi(config: AmapConfig) {
  if (typeof window === 'undefined' || typeof document === 'undefined') {
    return Promise.reject(new Error('AMap can only be loaded in browser environments'));
  }

  if (!config.key) {
    return Promise.reject(new Error('AMap key is missing'));
  }

  const existing = (window as Window & { AMap?: any }).AMap;
  if (existing) {
    return Promise.resolve(existing);
  }

  if (amapApiPromise) {
    return amapApiPromise;
  }

  if (config.securityCode) {
    (window as Window & { _AMapSecurityConfig?: { securityJsCode: string } })._AMapSecurityConfig = {
      securityJsCode: config.securityCode
    };
  }

  const callbackName = `__amapReady_${Date.now()}`;

  amapApiPromise = new Promise<any>((resolve, reject) => {
    const timer = window.setTimeout(() => {
      reject(new Error('AMap script load timeout'));
    }, 15000);

    (window as any)[callbackName] = () => {
      window.clearTimeout(timer);
      const AMap = (window as Window & { AMap?: any }).AMap;
      if (AMap) {
        resolve(AMap);
      } else {
        reject(new Error('AMap global not found after load'));
      }
      delete (window as any)[callbackName];
    };

    const script = document.createElement('script');
    script.src = buildAmapUrl(config.key, callbackName);
    script.async = true;
    script.onerror = () => {
      window.clearTimeout(timer);
      amapApiPromise = null;
      delete (window as any)[callbackName];
      reject(new Error('AMap script load failed'));
    };
    document.head.appendChild(script);
  });

  return amapApiPromise;
}
