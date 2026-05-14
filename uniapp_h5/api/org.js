import request from '@/utils/request'

export function getOrgTreeWithCount() {
  return request({
    url: '/org-unit/tree-with-count',
    method: 'get'
  })
}
