import store from '@/store'
import config from '@/config'
import { getToken } from '@/utils/auth'
import errorCode from '@/utils/errorCode'
import { tansParams, toast } from '@/utils/common'

const timeout = 10000
const baseUrl = config.baseUrl
let authExpiredRedirecting = false

function normalizeResponse(resData) {
  const code = resData?.code
  const msg = resData?.msg || errorCode.default
  const successCode = code === 200 || code === '200' || code === '0000'

  if (successCode) {
    return {
      code: 200,
      data: resData.data ?? resData.rows ?? resData,
      msg
    }
  }

  return {
    code,
    data: resData?.data,
    msg
  }
}

function handleAuthExpired(reject) {
  const message = '登录状态已过期，请重新登录。'

  if (!authExpiredRedirecting) {
    authExpiredRedirecting = true
    toast(message)
    store.dispatch('ClearSession').finally(() => {
      uni.reLaunch({ url: '/pages/login' })
      setTimeout(() => {
        authExpiredRedirecting = false
      }, 500)
    })
  }

  reject(message)
}

const request = (options) => {
  const header = { ...(options.header || {}) }
  const skipToken = (options.headers || {}).isToken === false || header.isToken === false
  delete header.isToken

  if (getToken() && !skipToken) {
    header.Authorization = 'Bearer ' + getToken()
  }

  let url = options.url
  if (options.params) {
    const query = tansParams(options.params)
    if (query) url += '?' + query.slice(0, -1)
  }

  return new Promise((resolve, reject) => {
    uni.request({
      method: options.method || 'get',
      timeout: options.timeout || timeout,
      url: options.baseUrl || baseUrl + url,
      data: options.data,
      header,
      dataType: 'json',
      success(res) {
        if (res.statusCode === 401) {
          if (skipToken) {
            const message = res.data?.msg || '登录失败，请检查账号或密码'
            toast(message)
            reject(message)
            return
          }

          handleAuthExpired(reject)
          return
        }

        if (res.statusCode !== 200) {
          const message = res.data?.msg || ('请求失败: ' + res.statusCode)
          toast(message)
          reject(message)
          return
        }

        const normalized = normalizeResponse(res.data)
        if (normalized.code === 200) {
          resolve(normalized)
          return
        }

        if (normalized.code === '8888' || normalized.code === '9999') {
          handleAuthExpired(reject)
          return
        }

        toast(normalized.msg)
        reject(normalized.msg)
      },
      fail(error) {
        let message = error.message || error.errMsg || '请求失败'
        if (message === 'Network Error') {
          message = '后端接口连接异常'
        } else if (message.includes && message.includes('timeout')) {
          message = '系统接口请求超时'
        }
        toast(message)
        reject(error)
      }
    })
  })
}

export default request
