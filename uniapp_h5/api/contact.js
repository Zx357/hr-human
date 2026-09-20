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

