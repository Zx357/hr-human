import request from '@/utils/request'

export function getClockInfo() {
  return request({
    url: '/mobile/attendance/clock/info',
    method: 'get'
  })
}

export function clock(data) {
  return request({
    url: '/mobile/attendance/clock',
    method: 'post',
    data
  })
}

export function getHomeStats() {
  return request({
    url: '/mobile/home/stats',
    method: 'get'
  })
}
