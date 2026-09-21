import localConfig from './config.local.js'
import serverConfig from './config.server.js'

// Local run uses config.local.js; H5 production build uses config.server.js.
const env = import.meta.env || {}
const configEnv = env.UNI_APP_CONFIG || env.VITE_APP_CONFIG || ''

const normalizedEnv = String(configEnv).toLowerCase()
const isExplicitLocal = normalizedEnv === 'local' || normalizedEnv === 'dev'
const isExplicitServer =
  normalizedEnv === 'server' ||
  normalizedEnv === 'prod'
const isProductionBuild = env.PROD === true || env.MODE === 'production'

const activeConfig = isExplicitLocal ? localConfig : (isExplicitServer || isProductionBuild ? serverConfig : localConfig)

// 小程序端 uni.request / uni.connectSocket 只接受绝对地址(H5 可用同源相对路径):
// MP-WEIXIN 下把相对 baseUrl 替换为 mpBaseUrl,未配置时用 appInfo.siteUrl 推导
let isMpWeixin = false
// #ifdef MP-WEIXIN
isMpWeixin = true
// #endif

function resolveBaseUrl(cfg) {
  const base = String(cfg.baseUrl || '')
  if (!isMpWeixin || !base.startsWith('/')) return base
  const explicit = String(cfg.mpBaseUrl || '')
  if (explicit) return explicit
  const siteUrl = String((cfg.appInfo && cfg.appInfo.siteUrl) || '')
  if (siteUrl) return siteUrl.replace(/\/+$/, '') + base
  return base
}

export default {
  ...activeConfig,
  baseUrl: resolveBaseUrl(activeConfig)
}
