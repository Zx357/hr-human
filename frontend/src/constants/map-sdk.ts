const rawAmapKey = import.meta.env.VITE_AMAP_KEY || '';
const rawAmapSecurityCode = import.meta.env.VITE_AMAP_SECURITY_CODE || '';

// env 中的占位符视为未配置（key/安全密钥优先从后台「系统参数配置」map.amap.* 读取，env 仅兜底）
function sanitizeEnvValue(value: string) {
  const trimmed = value.trim();
  return !trimmed || trimmed.startsWith('YOUR_') ? '' : trimmed;
}

/** 高德地图 Key（env 兜底值，后台参数配置优先） */
export const AMAP_KEY = sanitizeEnvValue(rawAmapKey);

/** 高德安全密钥 jscode（env 兜底值，2021-12 后申请的 Key 必填） */
export const AMAP_SECURITY_CODE = sanitizeEnvValue(rawAmapSecurityCode);
