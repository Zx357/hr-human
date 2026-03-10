import request from '@/utils/request'

/**
 * 获取打卡页面信息
 */
export function getClockInfo() {
  return request({
    url: '/mobile/attendance/clock/info',
    method: 'get'
  })
}

/**
 * 打卡
 */
export function clock(data) {
  return request({
    url: '/mobile/attendance/clock',
    method: 'post',
    data
  })
}

/**
 * 获取我的打卡记录
 */
export function getMyClockRecords(params) {
  return request({
    url: '/mobile/attendance/clock/records',
    method: 'get',
    params
  })
}

/**
 * 获取我的月度考勤数据（日历视图 + 统计）
 */
export function getMyMonthAttendance(params) {
  return request({
    url: '/mobile/attendance/month',
    method: 'get',
    params
  })
}
