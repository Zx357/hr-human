import request from '@/utils/request'

/**
 * 我的工资条分页（仅已发放批次，后端按当前登录员工过滤）
 */
export function getMyPayslips(params = {}) {
  return request({
    url: '/mobile/salary/payslips',
    method: 'get',
    params
  })
}

/**
 * 工资条详情（本人+已发放），首次查看自动标记已读
 */
export function getMyPayslipDetail(id) {
  return request({
    url: `/mobile/salary/payslips/${id}`,
    method: 'get'
  })
}

/**
 * 确认工资条
 */
export function confirmMyPayslip(id) {
  return request({
    url: `/mobile/salary/payslips/${id}/confirm`,
    method: 'post'
  })
}
