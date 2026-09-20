import request from '@/utils/request'

export function getHomeStats() {
  return request({
    url: '/mobile/home/stats',
    method: 'get'
  })
}

export function getHomeMessages() {
  return request({
    url: '/mobile/home/messages',
    method: 'get'
  })
}

// 最近站内通知(消息动态顶部展示)
export function getNotificationTop(limit = 10) {
  return request({
    url: '/mobile/notifications/top',
    method: 'get',
    params: { limit }
  })
}

// 站内通知未读数:返回 { count }
export function getNotificationUnreadCount() {
  return request({
    url: '/mobile/notifications/unread-count',
    method: 'get'
  })
}

// 标记全部通知已读
export function markAllNotificationsRead() {
  return request({
    url: '/mobile/notifications/read-all',
    method: 'post'
  })
}
