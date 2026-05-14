import request from '@/utils/request'

export function getMobileMenus() {
  return request({
    url: '/system/mobile-menu/mobile/list',
    method: 'get'
  })
}
