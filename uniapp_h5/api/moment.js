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
