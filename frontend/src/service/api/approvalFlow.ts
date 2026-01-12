import { request } from '../request';

export interface ApprovalNode {
  id?: number;
  flowId?: number;
  nodeName: string;
  nodeType: number; // 1-审批, 2-抄送
  approverType?: number; // 1-指定角色
  roleId?: number;
  roleName?: string;
  sortOrder?: number;
}

export interface ApprovalFlow {
  id?: number;
  flowCode: string;
  flowName: string;
  flowType: string;
  description?: string;
  status?: number;
  autoPass?: number; // 1-免审批直接通过, 0-需要审批
  nodes?: ApprovalNode[];
}

export function fetchApprovalFlowList() {
  return request<ApprovalFlow[]>({ url: '/system/approval-flow/list', method: 'get' });
}

export function fetchApprovalFlowById(id: number) {
  return request<ApprovalFlow>({ url: `/system/approval-flow/${id}`, method: 'get' });
}

export function createApprovalFlow(data: ApprovalFlow) {
  return request<boolean>({ url: '/system/approval-flow', method: 'post', data });
}

export function updateApprovalFlow(data: ApprovalFlow) {
  return request<boolean>({ url: '/system/approval-flow', method: 'put', data });
}

export function deleteApprovalFlow(id: number) {
  return request<boolean>({ url: `/system/approval-flow/${id}`, method: 'delete' });
}

export function updateApprovalFlowStatus(id: number, status: number) {
  return request<boolean>({ url: `/system/approval-flow/status/${id}`, method: 'post', params: { status } });
}
