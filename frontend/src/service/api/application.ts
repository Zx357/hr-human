import { request } from '../request';

export interface Application {
  id?: number;
  employeeId: number;
  employeeName?: string;
  employeeNo?: string;
  deptName?: string;
  companyName?: string;
  entryDate?: string;
  appType: string; // leave-请假, overtime-加班, business-出差, makeup-补卡, exchange-换休, regularization-转正, transfer-调动, reward-奖励, punish-惩罚, resignation-离职
  title?: string;
  startTime?: string;
  endTime?: string;
  duration?: number;
  reason?: string;
  // 转正申请字段
  regularDate?: string;
  probationEndDate?: string;
  evaluation?: string;
  newEmployeeType?: string;
  // 调动申请字段
  transferType?: string;
  fromCompanyId?: number;
  toCompanyId?: number;
  fromDeptId?: number;
  toDeptId?: number;
  fromPosition?: string;
  toPosition?: string;
  effectDate?: string;
  fromCompanyName?: string;
  toCompanyName?: string;
  fromDeptName?: string;
  toDeptName?: string;
  // 奖惩申请字段
  rewardType?: number;
  category?: string;
  amount?: number;
  // 离职申请字段
  resignType?: string;
  lastWorkDate?: string;
  handoverTo?: number;
  handoverToName?: string;

  status?: number;
  approveBy?: number;
  approveTime?: string;
  approveRemark?: string;
  remark?: string;
  createdTime?: string;
}

export function fetchApplicationPage(params: {
  pageNum?: number;
  pageSize?: number;
  employeeName?: string;
  employeeNo?: string;
  /** 申请时间范围-开始（yyyy-MM-dd，按 createTime 过滤） */
  beginTime?: string;
  /** 申请时间范围-结束（yyyy-MM-dd，按 createTime 过滤） */
  endTime?: string;
  appType?: string;
  status?: number;
  employeeId?: number;
  transferType?: string;
  resignType?: string;
}) {
  return request<Api.Common.PageResult<Application>>({ url: '/hr/application/page', method: 'get', params });
}

export function fetchPendingPage(params: {
  pageNum?: number;
  pageSize?: number;
  employeeName?: string;
  employeeNo?: string;
  appType?: string;
}) {
  return request<Api.Common.PageResult<Application>>({ url: '/hr/application/pending', method: 'get', params });
}

export function createApplication(data: Application) {
  return request<boolean>({ url: '/hr/application', method: 'post', data });
}

export function updateApplication(data: Application) {
  return request<boolean>({ url: '/hr/application', method: 'put', data });
}

export function deleteApplication(id: number) {
  return request<boolean>({ url: `/hr/application/${id}`, method: 'delete' });
}

export function approveApplication(id: number, status: number, remark?: string) {
  return request<boolean>({ url: `/hr/application/approve/${id}`, method: 'post', params: { status, remark } });
}

export function cancelApplication(id: number) {
  return request<boolean>({ url: `/hr/application/cancel/${id}`, method: 'post' });
}

export interface ApprovalRecord {
  nodeName?: string;
  approverName?: string;
  status?: number; // 0-待审批 1-通过 2-拒绝
  comment?: string;
  createTime?: string;
}

/** 获取申请的审批进度记录 */
export function fetchApprovalRecords(id: number) {
  return request<ApprovalRecord[]>({ url: `/hr/application/approval-records/${id}`, method: 'get' });
}

export function calculateLeaveHours(employeeId: number, startTime: string, endTime: string) {
  return request<number>({
    url: '/hr/application/calculate-leave-hours',
    method: 'get',
    params: { employeeId, startTime, endTime }
  });
}

export function calculateOvertimeHours(employeeId: number, startTime: string, endTime: string) {
  return request<number>({
    url: '/hr/application/calculate-overtime-hours',
    method: 'get',
    params: { employeeId, startTime, endTime }
  });
}
