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

// ===== 群聊管理(后端契约接口) =====

// 群成员列表 → [{employeeId,name,avatar,isOwner}]
export function getGroupMembers(groupId) {
  return request({
    url: `/mobile/chat/group/${groupId}/members`,
    method: 'get'
  })
}

// 添加群成员 {memberIds:[number]}
export function addGroupMembers(groupId, memberIds) {
  return request({
    url: `/mobile/chat/group/${groupId}/members`,
    method: 'post',
    data: { memberIds }
  })
}

// 移除群成员(群主)
export function removeGroupMember(groupId, employeeId) {
  return request({
    url: `/mobile/chat/group/${groupId}/members/${employeeId}`,
    method: 'delete'
  })
}

// 退出群聊(普通成员,群主不可退)
export function leaveGroup(groupId) {
  return request({
    url: `/mobile/chat/group/${groupId}/leave`,
    method: 'post'
  })
}

// 修改群名(群主) {name}
export function renameGroup(groupId, name) {
  return request({
    url: `/mobile/chat/group/${groupId}/name`,
    method: 'put',
    data: { name }
  })
}

// 解散群聊(群主)
export function dissolveGroup(groupId) {
  return request({
    url: `/mobile/chat/group/${groupId}`,
    method: 'delete'
  })
}

// ===== 会话管理(后端契约接口) =====

// 会话管理契约 targetType:1-单聊 2-群聊,与会话列表的 chatType(1-群聊 2-单聊)取值相反,调用前需换算
export function chatTypeToTargetType(chatType) {
  return Number(chatType) === 1 ? 2 : 1
}

// 会话设置:置顶/取消置顶 {targetType:1|2, targetId, sticky:0|1}
export function updateConversationSettings(data) {
  return request({
    url: '/mobile/chat/conversation/settings',
    method: 'put',
    data
  })
}

// 隐藏(删除)会话
export function hideConversation(targetType, targetId) {
  return request({
    url: '/mobile/chat/conversation',
    method: 'delete',
    params: { targetType, targetId }
  })
}

// 撤回消息(2分钟内,本人)
export function recallChatMessage(messageId) {
  return request({
    url: `/mobile/chat/message/${messageId}/recall`,
    method: 'put'
  })
}
