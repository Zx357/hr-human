import request from '@/utils/request'

export function getEmployeeList(params = {}) {
  return request({
    url: '/employee/list',
    method: 'get',
    params
  })
}

export function getEmployeeDetail(id) {
  return request({
    url: `/employee/${id}`,
    method: 'get'
  })
}
