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

export function getMonthAttendance(month) {
  return request({
    url: '/mobile/attendance/month',
    method: 'get',
    params: { month }
  })
}

export function getClockRecords(params = {}) {
  return request({
    url: '/mobile/attendance/clock/records',
    method: 'get',
    params
  })
}

export function getHomeStats() {
  return request({
    url: '/mobile/home/stats',
    method: 'get'
  })
}
