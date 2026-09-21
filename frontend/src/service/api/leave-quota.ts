import { request } from '../request';

/** 假期额度 */
export interface LeaveQuota {
  id?: number;
  employeeId: number;
  employeeName?: string;
  employeeNo?: string;
  year: number;
  leaveType: string;
  totalHours: number;
  usedHours?: number;
}

/** 分页查询额度列表 */
export function fetchLeaveQuotaPage(params: {
  pageNum?: number;
  pageSize?: number;
  year?: number;
  leaveType?: string;
  employeeName?: string;
  employeeNo?: string;
}) {
  return request<Api.Common.PageResult<LeaveQuota>>({
    url: '/hr/leave-quota/page',
    method: 'get',
    params
  });
}

/** 指定员工某年度的额度列表（请假时展示余额用） */
export function fetchLeaveQuotaList(params: { employeeId: number; year?: number }) {
  return request<LeaveQuota[]>({
    url: '/hr/leave-quota/list',
    method: 'get',
    params
  });
}

/** 新增/更新额度（按 员工+年度+类型 幂等） */
export function saveLeaveQuota(data: LeaveQuota) {
  return request<LeaveQuota>({
    url: '/hr/leave-quota/save',
    method: 'post',
    data
  });
}

/** 删除额度 */
export function deleteLeaveQuota(id: number) {
  return request<null>({
    url: `/hr/leave-quota/${id}`,
    method: 'delete'
  });
}
