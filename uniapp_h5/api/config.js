import request from '@/utils/request'

// 高德地图 Key 对应的后台参数键(后台「系统管理-参数配置」中维护)
export const AMAP_CONFIG_KEY = 'map.amap.key'

// 高德安全密钥对应的后台参数键(2021-12-02 之后申请的 Key 必须配套 jscode)
export const AMAP_SECURITY_CODE_CONFIG_KEY = 'map.amap.security-code'

// 高德 Key 的 storage 缓存键与有效期(TTL 内直接用缓存,过期后重新拉取)
const AMAP_KEY_CACHE_KEY = 'amap_key'
const AMAP_KEY_CACHE_EXPIRE_KEY = 'amap_key_expire_at'
const AMAP_KEY_CACHE_TTL = 10 * 60 * 1000

// 会话内内存缓存:同一会话重复调用不再请求后端(含后端未配置的空值结果)
let amapRuntimeConfig = null

/**
 * 判断是否为有效的高德 Key(过滤空值与脚手架占位符 YOUR_AMAP_KEY)
 */
export function isValidAmapKey(key) {
  const value = String(key || '').trim()
  return !!value && !/^your_amap_key$/i.test(value)
}

/**
 * 获取后端公开配置(登录即可访问;后端只返回有值的配置项)
 * @param {string[]|string} keys 配置键列表,如 ['map.amap.key'],多键逗号拼接
 * @returns {Promise<{code:number,data:Object}>} data 形如 {"map.amap.key":"xxx"}
 */
export function getPublicConfig(keys) {
  const keyList = Array.isArray(keys) ? keys : [keys]
  return request({
    url: '/config/public',
    method: 'get',
    params: { keys: keyList.filter(Boolean).join(',') },
    // 非关键请求:失败不弹全局错误提示,调用方静默走本地兜底
    silent: true
  })
}

// 读取 storage 缓存的高德 Key(值为空/占位符/已过期一律视为无缓存)
function readCachedAmapKey() {
  const value = String(uni.getStorageSync(AMAP_KEY_CACHE_KEY) || '').trim()
  if (!isValidAmapKey(value)) return ''
  const expireAt = Number(uni.getStorageSync(AMAP_KEY_CACHE_EXPIRE_KEY))
  if (Number.isFinite(expireAt) && Date.now() > expireAt) return ''
  return value
}

// 写入 storage 缓存(带过期时间)
function saveCachedAmapKey(key) {
  uni.setStorageSync(AMAP_KEY_CACHE_KEY, key)
  uni.setStorageSync(AMAP_KEY_CACHE_EXPIRE_KEY, Date.now() + AMAP_KEY_CACHE_TTL)
}

/**
 * 一次性拉取高德运行时配置(Key + 安全密钥):
 * 后端「系统管理-参数配置」map.amap.key / map.amap.security-code,一次请求取回。
 * 成功后会话级缓存(Key 顺带写入 storage,默认 10 分钟 TTL);失败不缓存,用 storage 内
 * 未过期的 Key 兜底,安全密钥置空,下次调用可重试。任何情况都不抛错。
 *
 * @returns {Promise<{key:string, securityCode:string}>}
 */
export async function ensureAmapRuntimeConfig() {
  if (amapRuntimeConfig) return amapRuntimeConfig

  try {
    const res = await getPublicConfig([AMAP_CONFIG_KEY, AMAP_SECURITY_CODE_CONFIG_KEY])
    const data = res && res.code === 200 && res.data ? res.data : {}
    const remoteKey = isValidAmapKey(data[AMAP_CONFIG_KEY]) ? String(data[AMAP_CONFIG_KEY]).trim() : ''
    const securityCode = String(data[AMAP_SECURITY_CODE_CONFIG_KEY] || '').trim()
    // 仅将后端实际下发的有效 Key 写入 storage;未配置(空值)只做会话内缓存
    amapRuntimeConfig = { key: remoteKey, securityCode }
    if (remoteKey) saveCachedAmapKey(remoteKey)
    return amapRuntimeConfig
  } catch (error) {
    // 请求失败:storage 内未过期 Key 兜底(安全密钥拿不到,旧 Key 不需要),不缓存结果保证下次重试
    return { key: readCachedAmapKey(), securityCode: '' }
  }
}

/**
 * 获取高德地图 Key(仅取后端「系统管理-参数配置」map.amap.key,本地兜底由调用方处理)。
 * 后端返回空/请求失败一律返回 ''(不抛错),由调用方自行降级到本地配置。
 */
export async function ensureAmapKey() {
  return (await ensureAmapRuntimeConfig()).key
}
