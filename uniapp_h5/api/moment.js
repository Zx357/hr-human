import request from '@/utils/request'

export function getMomentPosts(params = {}) {
  return request({
    url: '/mobile/moments/posts',
    method: 'get',
    params
  })
}

export function getMomentMessages() {
  return request({
    url: '/mobile/moments/messages',
    method: 'get'
  })
}

export function getMomentPostDetail(id) {
  return request({
    url: `/mobile/moments/posts/${id}`,
    method: 'get'
  })
}

export function createMomentPost(data) {
  return request({
    url: '/mobile/moments/posts',
    method: 'post',
    data
  })
}

export function toggleMomentLike(id) {
  return request({
    url: `/mobile/moments/posts/${id}/like`,
    method: 'post'
  })
}

// 动态评论列表
export function getMomentComments(id) {
  return request({
    url: `/mobile/moments/posts/${id}/comments`,
    method: 'get'
  })
}

// 发表动态评论
export function addMomentComment(id, content) {
  return request({
    url: `/mobile/moments/posts/${id}/comments`,
    method: 'post',
    data: { content }
  })
}

// 删除自己的评论
export function deleteMomentComment(commentId) {
  return request({
    url: `/mobile/moments/comments/${commentId}`,
    method: 'delete'
  })
}

// 互动消息列表(点赞/评论)
export function getMomentMessageList(limit = 50) {
  return request({
    url: '/mobile/moments/messages/list',
    method: 'get',
    params: { limit }
  })
}

// 标记互动消息已读(unreadCount清零)
export function markMomentMessagesRead() {
  return request({
    url: '/mobile/moments/messages/read',
    method: 'post'
  })
}

// 删除自己的动态
export function deleteMoment(id) {
  return request({
    url: `/mobile/moments/posts/${id}`,
    method: 'delete'
  })
}
