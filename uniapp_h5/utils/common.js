export function toast(content) {
  uni.showToast({
    icon: 'none',
    title: content
  })
}

export function showConfirm(content) {
  return new Promise((resolve) => {
    uni.showModal({
      title: '提示',
      content,
      cancelText: '取消',
      confirmText: '确定',
      success(res) {
        resolve(res)
      }
    })
  })
}

export function tansParams(params) {
  let result = ''
  for (const propName of Object.keys(params || {})) {
    const value = params[propName]
    const part = encodeURIComponent(propName) + '='
    if (value !== null && value !== '' && typeof value !== 'undefined') {
      if (typeof value === 'object') {
        for (const key of Object.keys(value)) {
          if (value[key] !== null && value[key] !== '' && typeof value[key] !== 'undefined') {
            const paramsKey = propName + '[' + key + ']'
            const subPart = encodeURIComponent(paramsKey) + '='
            result += subPart + encodeURIComponent(value[key]) + '&'
          }
        }
      } else {
        result += part + encodeURIComponent(value) + '&'
      }
    }
  }
  return result
}

/**
 * 页面侧统一的请求错误提示(配合 utils/request.js 的错误契约):
 * - request.js 内部已 toast 过的错误(带 _toastShown 标志)不再重复提示;
 * - 其余错误取 error.message(或字符串本身)提示,无信息时用兜底文案。
 * @param {*} error catch 到的错误(Error 对象/字符串/其他)
 * @param {string} fallbackTitle 无可读信息时的兜底文案
 */
export function toastRequestError(error, fallbackTitle = '操作失败') {
  if (error && error._toastShown) return
  const message = (typeof error === 'string' && error) ||
    (error && error.message) ||
    fallbackTitle
  uni.showToast({ icon: 'none', title: message })
}

/**
 * 请假类型字典值 -> 展示名(与后端 sys_dict leave_type 及 PC 端提交口径一致)。
 * 历史数据 title 存中文标签时原样返回,保证新旧数据都能正确显示。
 */
const LEAVE_TYPE_LABELS = { '1': '年假', '2': '事假', '3': '病假', '4': '婚假', '5': '产假', '6': '陪产假', '7': '丧假' }
export function leaveTypeLabel(value) {
  const key = value == null ? '' : String(value)
  return LEAVE_TYPE_LABELS[key] || key
}
