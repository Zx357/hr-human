import config from '@/config'
import { getToken } from '@/utils/auth'
import errorCode from '@/utils/errorCode'
import { tansParams, toast } from '@/utils/common'
import { handleAuthExpired as sharedHandleAuthExpired } from '@/utils/auth-expired'

const timeout = 10000
const baseUrl = config.baseUrl

function normalizeResponse(resData) {
  const code = resData?.code
  const msg = resData?.msg || errorCode.default
  const successCode = code === 200 || code === '200' || code === '0000'

  if (successCode) {
    // rows 风格(TableDataList: code/msg/rows/total)扁平化时会丢掉 total,这里透传保留
    const result = {
      code: 200,
      data: resData.data ?? resData.rows ?? resData,
      msg
    }
    if (resData.data === undefined && resData.total !== undefined) {
      result.total = resData.total
    }
    return result
  }

  return {
    code,
    data: resData?.data,
    msg
  }
}

function handleAuthExpired(reject) {
  sharedHandleAuthExpired(reject)
}

/**
 * 统一错误契约:request.js 内部 toast 提示后,reject 带 message 的 Error 对象,
 * 并标记 _toastShown = true,页面侧据此免二次 toast(error.message 可复用文案)。
 * 页面请使用 utils/common.js 的 toastRequestError 处理 catch 到的错误。
 */
function rejectAfterToast(reject, message) {
  toast(message)
  const error = new Error(message)
  error._toastShown = true
  reject(error)
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
            rejectAfterToast(reject, res.data?.msg || '登录失败，请检查账号或密码')
            return
          }

          handleAuthExpired(reject)
          return
        }

        if (res.statusCode !== 200) {
          rejectAfterToast(reject, res.data?.msg || ('请求失败: ' + res.statusCode))
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

        rejectAfterToast(reject, normalized.msg)
      },
      fail(error) {
        let message = error.message || error.errMsg || '请求失败'
        if (message === 'Network Error') {
          message = '后端接口连接异常'
        } else if (message.includes && message.includes('timeout')) {
          message = '系统接口请求超时'
        }
        // 网络层失败统一转为可读文案的 Error(原先直接 reject uni 的 error 对象,
        // 页面当 toast 标题会显示 [object Object])
        rejectAfterToast(reject, message)
      }
    })
  })
}

export default request
