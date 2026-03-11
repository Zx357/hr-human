import store from '@/store'
import config from '@/config'
import { getToken } from '@/utils/auth'
import errorCode from '@/utils/errorCode'
import { toast, showConfirm, tansParams } from '@/utils/common'

let timeout = 10000
const baseUrl = config.baseUrl

const request = config => {
  // 是否需要设置 token
  const isToken = (config.headers || {}).isToken === false
  config.header = config.header || {}
  if (getToken() && !isToken) {
    config.header['Authorization'] = 'Bearer ' + getToken()
  }
  // get请求映射params参数
  if (config.params) {
    let url = config.url + '?' + tansParams(config.params)
    url = url.slice(0, -1)
    config.url = url
  }
  return new Promise((resolve, reject) => {
    uni.request({
        method: config.method || 'get',
        timeout: config.timeout ||  timeout,
        url: config.baseUrl || baseUrl + config.url,
        data: config.data,
        header: config.header,
        dataType: 'json'
      }).then(response => {
        let [error, res] = response
        if (error) {
          console.log('请求错误', error)
          toast('后端接口连接异常')
          reject('后端接口连接异常')
          return
        }
        
        // 检查 HTTP 状态码
        if (res.statusCode === 401) {
          // 未认证，Token无效或过期
          showConfirm('登录状态已过期，您可以继续留在该页面，或者重新登录?').then(res => {
            if (res.confirm) {
              store.dispatch('LogOut').then(res => {
                uni.reLaunch({ url: '/pages/login' })
              })
            }
          })
          reject('无效的会话，或者会话已过期，请重新登录。')
          return
        }
        
        if (res.statusCode !== 200) {
          console.log('HTTP错误', res.statusCode, res)
          toast('请求失败: ' + res.statusCode)
          reject('请求失败: ' + res.statusCode)
          return
        }
        
        const resData = res.data
        console.log('接口返回数据', config.url, resData)
        
        const code = resData.code
        const msg = resData.msg || errorCode['default']
        
        // 适配后端返回格式：成功码为 "0000"
        if (code === '0000') {
          // 成功，转换为前端期望的格式
          resolve({ code: 200, data: resData.data, msg: msg })
        } else if (code === '8888' || code === '9999') {
          // Token无效或过期
          showConfirm('登录状态已过期，您可以继续留在该页面，或者重新登录?').then(res => {
            if (res.confirm) {
              store.dispatch('LogOut').then(res => {
                uni.reLaunch({ url: '/pages/login' })
              })
            }
          })
          reject('无效的会话，或者会话已过期，请重新登录。')
        } else {
          // 其他错误
          console.log('业务错误', code, msg)
          toast(msg)
          reject(msg)
        }
      })
      .catch(error => {
        console.log('请求异常', error)
        let message = error.message || error.errMsg || '请求失败'
        if (message === 'Network Error') {
          message = '后端接口连接异常'
        } else if (message.includes && message.includes('timeout')) {
          message = '系统接口请求超时'
        } else if (message.includes && message.includes('Request failed with status code')) {
          message = '系统接口' + message.substr(message.length - 3) + '异常'
        }
        toast(message)
        reject(error)
      })
  })
}

export default request
