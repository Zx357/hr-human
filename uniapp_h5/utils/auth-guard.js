import { getToken } from '@/utils/auth'

export const LOGIN_PAGE = '/pages/login'
export const WORKBENCH_PAGE = '/pages/index?index=2'

const loginPages = [LOGIN_PAGE, '/minePages/login']
const whiteList = [LOGIN_PAGE]

export function normalizeUrl(url = '') {
  const raw = String(url || '').trim()
  if (!raw) return ''

  const normalizedHash = raw.startsWith('#')
    ? raw.replace(/^#\/?/, '/')
    : raw

  const path = normalizedHash.split('?')[0]
  if (!path || path === '/') return ''

  return path.startsWith('/') ? path : `/${path}`
}

export function normalizeFullUrl(url = '') {
  const raw = String(url || '').trim()
  if (!raw) return ''

  const [path, query = ''] = raw.split('?')
  const normalizedPath = normalizeUrl(path)
  return normalizedPath ? `${normalizedPath}${query ? `?${query}` : ''}` : ''
}

export function isLoginPage(url) {
  return loginPages.includes(normalizeUrl(url))
}

export function isWhitePage(url) {
  return whiteList.includes(normalizeUrl(url))
}

export function hasLogin() {
  return !!getToken()
}

export function stringifyQuery(query = {}) {
  return Object.keys(query)
    .filter((key) => query[key] !== undefined && query[key] !== null && query[key] !== '')
    .map((key) => `${encodeURIComponent(key)}=${encodeURIComponent(query[key])}`)
    .join('&')
}

export function getCurrentPageUrl() {
  const pages = getCurrentPages()
  const currentPage = pages && pages[pages.length - 1]
  if (!currentPage?.route) return ''

  const query = stringifyQuery(currentPage.options || {})
  return `/${currentPage.route}${query ? `?${query}` : ''}`
}

export function getLaunchUrl(options = {}) {
  const path = options.path ? normalizeUrl(options.path) : ''
  if (!path) return getCurrentPageUrl()

  const query = stringifyQuery(options.query || {})
  return `${path}${query ? `?${query}` : ''}`
}

export function getLoginUrl(redirectUrl = '') {
  const normalizedRedirect = normalizeFullUrl(redirectUrl)

  if (!normalizedRedirect || isLoginPage(normalizedRedirect)) {
    return LOGIN_PAGE
  }
  return `${LOGIN_PAGE}?redirect=${encodeURIComponent(normalizedRedirect)}`
}

export function resolveRedirectUrl(redirect = '') {
  if (!redirect) return WORKBENCH_PAGE

  try {
    const url = normalizeFullUrl(decodeURIComponent(redirect))
    return isLoginPage(url) ? WORKBENCH_PAGE : url
  } catch (error) {
    return WORKBENCH_PAGE
  }
}

export function goLogin(redirectUrl = '') {
  uni.reLaunch({ url: getLoginUrl(redirectUrl) })
  return false
}

export function goWorkbench() {
  uni.reLaunch({ url: WORKBENCH_PAGE })
  return false
}

export function requireLogin(url = '') {
  const targetUrl = url || getCurrentPageUrl()

  if (hasLogin()) {
    return isLoginPage(targetUrl) ? goWorkbench() : true
  }

  if (isWhitePage(targetUrl)) {
    return true
  }

  return goLogin(targetUrl)
}

export function requireLoginFromLaunch(options = {}) {
  const launchUrl = getLaunchUrl(options)

  if (launchUrl) {
    return requireLogin(launchUrl)
  }

  setTimeout(() => {
    requireLogin(getCurrentPageUrl())
  }, 0)

  return true
}
