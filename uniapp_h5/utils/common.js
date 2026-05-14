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
