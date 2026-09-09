import request from '@/utils/request'

export function getChatMessages(params) {
  return request({
    url: '/mobile/chat/messages',
    method: 'get',
    params
  })
}

export function sendChatMessage(data) {
  return request({
    url: '/mobile/chat/send',
    method: 'post',
    data
  })
}
