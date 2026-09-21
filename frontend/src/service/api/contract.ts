import { request } from '../request';

/** 合同信息 */
export interface Contract {
  id?: number;
  contractNo: string;
  employeeId: number;
  employeeName?: string;
  employeeNo?: string;
  contractType: string | number;
  startDate: string;
  endDate: string;
  signDate?: string;
  probationMonths?: number;
  salary?: number;
  status?: number;
  remark?: string;
  contractImages?: string; // 合同图片（多张，逗号分隔）
  contractCount?: number; // 合同次数
}

/** 分页查询合同列表 */
export function fetchContractPage(params: {
  pageNum?: number;
  pageSize?: number;
  contractNo?: string;
  employeeName?: string;
  employeeNo?: string;
  contractType?: string;
  status?: number;
  employeeId?: number;
}) {
  return request<Api.Common.PageResult<Contract>>({
    url: '/hr/contract/page',
    method: 'get',
    params
  });
}

/** 获取合同详情 */
export function fetchContractById(id: number) {
  return request<Contract>({
    url: `/hr/contract/${id}`,
    method: 'get'
  });
}

/** 创建合同 */
export function createContract(data: Contract) {
  return request<boolean>({
    url: '/hr/contract',
    method: 'post',
    data
  });
}

/** 更新合同 */
export function updateContract(data: Contract) {
  return request<boolean>({
    url: '/hr/contract',
    method: 'put',
    data
  });
}

/** 删除合同 */
export function deleteContract(id: number) {
  return request<boolean>({
    url: `/hr/contract/${id}`,
    method: 'delete'
  });
}

/** 合同续签参数：id 为旧合同，其余字段缺省时沿用旧合同 */
export interface ContractRenewParams {
  id: number;
  contractNo?: string;
  contractType?: string | number;
  startDate?: string;
  endDate?: string;
  signDate?: string;
  probationMonths?: number;
  salary?: number;
  remark?: string;
}

/** 合同续签：旧合同置为已续签并生成新合同（次数+1） */
export function renewContract(data: ContractRenewParams) {
  return request<Contract>({
    url: '/hr/contract/renew',
    method: 'post',
    data
  });
}
