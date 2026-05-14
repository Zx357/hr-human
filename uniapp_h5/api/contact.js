import request from '@/utils/request'

export function getContactSummary() {
  return request({
    url: '/mobile/contacts/summary',
    method: 'get'
  })
}

export function getContactGroups() {
  return request({
    url: '/mobile/contacts/groups',
    method: 'get'
  })
}

export function searchContacts(params = {}) {
  return request({
    url: '/mobile/contacts/search',
    method: 'get',
    params
  })
}

export function createContactGroup(data) {
  return request({
    url: '/mobile/contacts/groups',
    method: 'post',
    data
  })
}

export function getContactRequests(params = {}) {
  return request({
    url: '/mobile/contacts/requests',
    method: 'get',
    params
  })
}

export function sendContactRequest(data) {
  return request({
    url: '/mobile/contacts/requests',
    method: 'post',
    data
  })
}

export function handleContactRequest(id, data) {
  return request({
    url: `/mobile/contacts/requests/${id}/handle`,
    method: 'put',
    data
  })
}
