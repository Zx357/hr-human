import request from '@/utils/request'

/**
 * 获取移动端菜单
 */
export function getMobileMenus() {
  return request({
    url: '/system/mobile-menu/mobile/list',
    method: 'get'
  })
}
