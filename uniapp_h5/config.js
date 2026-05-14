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

export default isExplicitLocal ? localConfig : (isExplicitServer || isProductionBuild ? serverConfig : localConfig)
