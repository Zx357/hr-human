import { request } from '../request';

// ==================== 转正管理 ====================
export interface Regularization {
  id?: number;
  employeeId: number;
  employeeName?: string;
  employeeNo?: string;
  deptName?: string;
  positionName?: string;
  entryDate?: string;
  applyDate: string;
  regularDate?: string;
  probationEndDate?: string;
  evaluation?: string;
  newEmployeeType?: string;
  status?: number;
  approveRemark?: string;
  remark?: string;
}

export function fetchRegularizationPage(params: {
  pageNum?: number;
  pageSize?: number;
  employeeName?: string;
  status?: number;
}) {
  return request<Api.Common.PageResult<Regularization>>({ url: '/hr/regularization/page', method: 'get', params });
}
export function createRegularization(data: Regularization) {
  return request<boolean>({ url: '/hr/regularization', method: 'post', data });
}
export function updateRegularization(data: Regularization) {
  return request<boolean>({ url: '/hr/regularization', method: 'put', data });
}
export function deleteRegularization(id: number) {
  return request<boolean>({ url: `/hr/regularization/${id}`, method: 'delete' });
}
export function approveRegularization(id: number, status: number, remark?: string) {
  return request<boolean>({ url: `/hr/regularization/approve/${id}`, method: 'post', params: { status, remark } });
}

// ==================== 调动管理 ====================
export interface Transfer {
  id?: number;
  employeeId: number;
  employeeName?: string;
  employeeNo?: string;
  transferType?: string;
  fromCompanyId?: number;
  toCompanyId?: number;
  fromDeptId?: number;
  toDeptId?: number;
  fromPosition?: string;
  toPosition?: string;
  fromCompanyName?: string;
  toCompanyName?: string;
  fromDeptName?: string;
  toDeptName?: string;
  effectDate?: string;
  reason?: string;
  status?: number;
  approveRemark?: string;
  remark?: string;
}

export function fetchTransferPage(params: {
  pageNum?: number;
  pageSize?: number;
  employeeName?: string;
  transferType?: string;
  status?: number;
}) {
  return request<Api.Common.PageResult<Transfer>>({ url: '/hr/transfer/page', method: 'get', params });
}
export function createTransfer(data: Transfer) {
  return request<boolean>({ url: '/hr/transfer', method: 'post', data });
}
export function updateTransfer(data: Transfer) {
  return request<boolean>({ url: '/hr/transfer', method: 'put', data });
}
export function deleteTransfer(id: number) {
  return request<boolean>({ url: `/hr/transfer/${id}`, method: 'delete' });
}
export function approveTransfer(id: number, status: number, remark?: string) {
  return request<boolean>({ url: `/hr/transfer/approve/${id}`, method: 'post', params: { status, remark } });
}

// ==================== 奖惩管理 ====================
export interface RewardPunish {
  id?: number;
  employeeId: number;
  employeeName?: string;
  employeeNo?: string;
  deptName?: string;
  type: number;
  category?: string;
  amount?: number;
  effectDate?: string;
  reason?: string;
  status?: number;
  approveRemark?: string;
  remark?: string;
}

export function fetchRewardPunishPage(params: {
  pageNum?: number;
  pageSize?: number;
  employeeName?: string;
  type?: number;
  status?: number;
}) {
  return request<Api.Common.PageResult<RewardPunish>>({ url: '/hr/reward/page', method: 'get', params });
}
export function createRewardPunish(data: RewardPunish) {
  return request<boolean>({ url: '/hr/reward', method: 'post', data });
}
export function updateRewardPunish(data: RewardPunish) {
  return request<boolean>({ url: '/hr/reward', method: 'put', data });
}
export function deleteRewardPunish(id: number) {
  return request<boolean>({ url: `/hr/reward/${id}`, method: 'delete' });
}
export function approveRewardPunish(id: number, status: number, remark?: string) {
  return request<boolean>({ url: `/hr/reward/approve/${id}`, method: 'post', params: { status, remark } });
}

// ==================== 离职管理 ====================
export interface Resignation {
  id?: number;
  employeeId: number;
  employeeName?: string;
  employeeNo?: string;
  deptName?: string;
  positionName?: string;
  entryDate?: string;
  resignType?: string;
  applyDate: string;
  lastWorkDate?: string;
  handoverTo?: number;
  handoverToName?: string;
  reason?: string;
  status?: number;
  approveRemark?: string;
  remark?: string;
}

export function fetchResignationPage(params: {
  pageNum?: number;
  pageSize?: number;
  employeeName?: string;
  resignType?: string;
  status?: number;
}) {
  return request<Api.Common.PageResult<Resignation>>({ url: '/hr/resignation/page', method: 'get', params });
}
export function createResignation(data: Resignation) {
  return request<boolean>({ url: '/hr/resignation', method: 'post', data });
}
export function updateResignation(data: Resignation) {
  return request<boolean>({ url: '/hr/resignation', method: 'put', data });
}
export function deleteResignation(id: number) {
  return request<boolean>({ url: `/hr/resignation/${id}`, method: 'delete' });
}
export function approveResignation(id: number, status: number, remark?: string) {
  return request<boolean>({ url: `/hr/resignation/approve/${id}`, method: 'post', params: { status, remark } });
}
