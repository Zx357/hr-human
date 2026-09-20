import request from '@/utils/request'

// 获取聊天消息
// chatType: 1-群聊 2-单聊
// afterMessageId: 可选,增量拉取该 id 之后的新消息(轮询使用)
// beforeMessageId: 可选,翻历史时拉取该 id 之前的一页(配合 limit)
// limit: 可选,分页大小
export function getChatMessages({ chatType, targetId, afterMessageId, beforeMessageId, limit } = {}) {
  const params = { chatType, targetId }
  if (afterMessageId !== undefined && afterMessageId !== null && afterMessageId !== '') {
    params.afterMessageId = afterMessageId
  }
  if (beforeMessageId !== undefined && beforeMessageId !== null && beforeMessageId !== '') {
    params.beforeMessageId = beforeMessageId
  }
  if (limit !== undefined && limit !== null) {
    params.limit = limit
  }
  return request({
    url: '/mobile/chat/messages',
    method: 'get',
    params
  })
}

// 发送聊天消息
// chatType: 1-群聊 2-单聊
// msgType: 消息类型 1-文本(默认) 2-图片(content 为图片 URL,由 /file/upload/image 上传获得)
export function sendChatMessage(data) {
  return request({
    url: '/mobile/chat/send',
    method: 'post',
    data
  })
}

// 最近会话列表(群聊+单聊,含未读数,按最后消息时间倒序)
export function getConversations() {
  return request({
    url: '/mobile/chat/conversations',
    method: 'get'
  })
}

// 未读消息总数(角标)
export function getUnreadTotal() {
  return request({
    url: '/mobile/chat/unread-total',
    method: 'get'
  })
}

// 标记会话已读
export function markConversationRead(data) {
  return request({
    url: '/mobile/chat/read',
    method: 'post',
    data
  })
}
