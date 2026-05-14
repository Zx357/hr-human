import request from '@/utils/request'

export function submitFeedback(data) {
  return request({
    url: '/system/feedback',
    method: 'post',
    data
  })
}

export function getMyFeedback(params) {
  return request({
    url: '/system/feedback/my',
    method: 'get',
    params
  })
}
