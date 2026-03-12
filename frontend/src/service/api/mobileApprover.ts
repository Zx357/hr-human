import { request } from '../request';

export interface MobileApprover {
  id?: number;
  employeeId?: number;
  appTypes?: string;
  employeeName?: string;
  employeeNo?: string;
  deptName?: string;
  createdTime?: string;
}

export function fetchMobileApproverPage(params: { pageNum: number; pageSize: number; employeeName?: string; employeeNo?: string }) {
  return request<Api.Common.PaginatingQueryRecord<MobileApprover>>({
    url: '/hr/mobile-approver/page',
    method: 'get',
    params
  });
}

export function createMobileApprover(data: MobileApprover) {
  return request<boolean>({ url: '/hr/mobile-approver', method: 'post', data });
}

export function updateMobileApprover(data: MobileApprover) {
  return request<boolean>({ url: '/hr/mobile-approver', method: 'put', data });
}

export function deleteMobileApprover(id: number) {
  return request<boolean>({ url: `/hr/mobile-approver/${id}`, method: 'delete' });
}
