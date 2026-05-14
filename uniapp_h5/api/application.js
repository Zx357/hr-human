import request from '@/utils/request'

export function getMyApplications(params) {
  return request({
    url: '/hr/application/page',
    method: 'get',
    params
  })
}

export function getApplicationDetail(id) {
  return request({
    url: `/hr/application/${id}`,
    method: 'get'
  })
}

export function submitApplication(data) {
  return request({
    url: '/hr/application',
    method: 'post',
    data
  })
}

export function cancelApplication(id) {
  return request({
    url: `/hr/application/cancel/${id}`,
    method: 'post'
  })
}

export function getPendingApprovals(params) {
  return request({
    url: '/hr/application/mobile-pending',
    method: 'get',
    params
  })
}

export function approveApplication(id, status, remark) {
  return request({
    url: `/hr/application/approve/${id}`,
    method: 'post',
    params: { status, remark }
  })
}

export function calculateLeaveHours(employeeId, startTime, endTime) {
  return request({
    url: '/hr/application/calculate-leave-hours',
    method: 'get',
    params: { employeeId, startTime, endTime }
  })
}

export function calculateOvertimeHours(employeeId, startTime, endTime) {
  return request({
    url: '/hr/application/calculate-overtime-hours',
    method: 'get',
    params: { employeeId, startTime, endTime }
  })
}
