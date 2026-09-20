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

// ==================== 移动端公告(分页+已读标记) ====================

// 公告分页:返回 { records:[{...notice, readFlag}], total }
export function getMobileNotices({ pageNum = 1, pageSize = 10, keyword, noticeType } = {}) {
  const params = { pageNum, pageSize }
  if (keyword) params.keyword = keyword
  if (noticeType) params.noticeType = noticeType
  return request({
    url: '/mobile/notices',
    method: 'get',
    params
  })
}

// 公告未读数:返回 { count }
export function getMobileNoticeUnreadCount() {
  return request({
    url: '/mobile/notices/unread-count',
    method: 'get'
  })
}

// 标记公告已读(幂等)
export function markNoticeRead(id) {
  return request({
    url: `/mobile/notices/${id}/read`,
    method: 'post'
  })
}
