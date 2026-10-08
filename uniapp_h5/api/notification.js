import request from '@/utils/request'

/**
 * 站内通知(审批结果/待审批/到期提醒等)接口
 * 复用后端 MobileNotificationController 现有端点:
 *   GET  /mobile/notifications            我的通知分页
 *   POST /mobile/notifications/{id}/read   标记单条已读
 *   POST /mobile/notifications/read-all    标记全部已读
 *   GET  /mobile/notifications/unread-count 未读数
 */

// 我的通知分页(含未读标记 readFlag)
export function getNotifications(params = {}) {
  return request({
    url: '/mobile/notifications',
    method: 'get',
    params
  })
}

// 标记单条通知已读(幂等)
export function markNotificationRead(id) {
  return request({
    url: `/mobile/notifications/${id}/read`,
    method: 'post'
  })
}

// 标记全部通知已读
export function markAllNotificationsRead() {
  return request({
    url: '/mobile/notifications/read-all',
    method: 'post'
  })
}

// 未读通知数:返回 { count }
export function getNotificationUnreadCount() {
  return request({
    url: '/mobile/notifications/unread-count',
    method: 'get'
  })
}
