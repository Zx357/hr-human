import request from '@/utils/request'

// 提交意见反馈
export function submitFeedback(data) {
  return request({
    url: '/system/feedback',
    method: 'post',
    data
  })
}

/**
 * 我的反馈历史(后端契约接口,分页,含管理员回复 reply/replyTime)
 * 返回分页结构: { records, total }
 */
export function getMyFeedback(params = {}) {
  return request({
    url: '/mobile/feedback/my',
    method: 'get',
    params
  })
}
