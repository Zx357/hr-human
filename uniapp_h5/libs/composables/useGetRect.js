import { getCurrentInstance } from 'vue'

/**
 * 获取当前页面实例（兼容微信小程序）
 * @returns {Object} 页面实例
 */
function getCurrentPageInstance() {
  const pages = getCurrentPages()
  return pages[pages.length - 1]
}

/**
 * 获取元素节点信息的 Composable
 * @returns {Function} _tGetRect 函数
 */
export function useGetRect() {
  /**
   * 获取元素节点信息
   * @param {string} selector - CSS 选择器
   * @param {boolean} all - 是否获取所有匹配的元素
   * @returns {Promise} 返回元素节点信息
   */
  const _tGetRect = (selector, all = false) => {
    return new Promise((resolve) => {
      const pageInstance = getCurrentPageInstance()
      const query = uni.createSelectorQuery()
      
      // 如果有页面实例，使用 .in()，否则默认查询当前页面
      if (pageInstance) {
        query.in(pageInstance)
      }
      
      query[all ? 'selectAll' : 'select'](selector)
        .boundingClientRect(rect => {
          if (all && Array.isArray(rect) && rect.length) {
            resolve(rect)
          } else if (!all && rect) {
            resolve(rect)
          } else {
            resolve(all ? [] : null)
          }
        })
        .exec()
    })
  }

  return {
    _tGetRect
  }
}

export default useGetRect
