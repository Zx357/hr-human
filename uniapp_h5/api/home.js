import request from '@/utils/request'

export function getHomeStats() {
  return request({
    url: '/mobile/home/stats',
    method: 'get'
  })
}

export function getHomeMessages() {
  return request({
    url: '/mobile/home/messages',
    method: 'get'
  })
}
