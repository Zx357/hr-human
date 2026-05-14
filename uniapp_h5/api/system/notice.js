import request from '@/utils/request'

export function getNoticeList(params) {
  return request({
    url: '/system/notice/list',
    method: 'get',
    params
  })
}

export function getNoticeDetail(id) {
  return request({
    url: `/system/notice/${id}`,
    method: 'get'
  })
}
