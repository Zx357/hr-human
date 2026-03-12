import request from '@/utils/request'

/**
 * 获取我的申请列表
 */
export function getMyApplications(params) {
  return request({
    url: '/hr/application/page',
    method: 'get',
    params
  })
}

/**
 * 获取申请详情
 */
export function getApplicationDetail(id) {
  return request({
    url: `/hr/application/${id}`,
    method: 'get'
  })
}

/**
 * 提交申请
 */
export function submitApplication(data) {
  return request({
    url: '/hr/application',
    method: 'post',
    data
  })
}

/**
 * 撤销申请
 */
export function cancelApplication(id) {
  return request({
    url: `/hr/application/cancel/${id}`,
    method: 'post'
  })
}

/**
 * 获取待审批列表（移动端）
 */
export function getPendingApprovals(params) {
  return request({
    url: '/hr/application/mobile-pending',
    method: 'get',
    params
  })
}

/**
 * 审批
 */
export function approveApplication(id, status, remark) {
  return request({
    url: `/hr/application/approve/${id}`,
    method: 'post',
    params: { status, remark }
  })
}

/**
 * 计算请假小时数
 */
export function calculateLeaveHours(employeeId, startTime, endTime) {
  return request({
    url: '/hr/application/calculate-leave-hours',
    method: 'get',
    params: { employeeId, startTime, endTime }
  })
}

/**
 * 计算加班小时数
 */
export function calculateOvertimeHours(employeeId, startTime, endTime) {
  return request({
    url: '/hr/application/calculate-overtime-hours',
    method: 'get',
    params: { employeeId, startTime, endTime }
  })
}
