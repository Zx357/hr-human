import { request } from '../request';

/** 薪资项定义 */
export interface SalaryItemDef {
  id?: number;
  itemCode: string;
  itemName: string;
  /** 1-收入 2-扣款 */
  direction: number;
  /** 1-固定 2-比例 3-考勤联动 4-手工 5-公式(预留) */
  valueType: number;
  ratioBaseCode?: string;
  ratioValue?: number;
  attRule?: string;
  unitPrice?: number;
  tolerance?: number;
  formula?: string;
  enabled?: number;
  sortOrder?: number;
  remark?: string;
}

/** 薪资方案 */
export interface SalaryScheme {
  id?: number;
  schemeCode?: string;
  schemeName: string;
  enabled?: number;
  sortOrder?: number;
  remark?: string;
  itemCount?: number;
  items?: SalarySchemeItem[];
}

/** 方案明细 */
export interface SalarySchemeItem {
  id?: number;
  schemeId?: number;
  itemId: number;
  /** FIXED 项默认金额 */
  defaultAmount?: number;
  sortOrder?: number;
  itemCode?: string;
  itemName?: string;
}

/** 员工薪资档案行（员工左联档案） */
export interface SalaryArchiveRow {
  employeeId: number;
  employeeNo: string;
  employeeName: string;
  deptName?: string;
  archiveId?: number;
  schemeId?: number;
  schemeName?: string;
  effectiveDate?: string;
  remark?: string;
}

/** 档案固定项明细 */
export interface SalaryArchiveItem {
  id?: number;
  archiveId?: number;
  itemId: number;
  amount: number;
  itemCode?: string;
  itemName?: string;
}

/** 工资批次 */
export interface PayrollBatch {
  id?: number;
  yearMonth: string;
  companyId: number;
  companyName?: string;
  /** 0-核算中 1-已核算 2-已确认 3-已发放 */
  status: number;
  employeeCount?: number;
  totalGross?: number;
  totalNet?: number;
  remark?: string;
}

/** 工资条 */
export interface Payslip {
  id?: number;
  batchId: number;
  employeeId: number;
  employeeNo?: string;
  employeeName?: string;
  grossPay: number;
  totalDeduction: number;
  netPay: number;
  readFlag?: number;
  confirmFlag?: number;
  yearMonth?: string;
}

/** 工资条明细行 */
export interface PayrollItem {
  id?: number;
  payslipId: number;
  itemId?: number;
  itemCode?: string;
  itemName?: string;
  /** 1-收入 2-扣款 */
  direction: number;
  valueType: number;
  amount: number;
  source?: string;
  sortOrder?: number;
}

// ==================== 薪资项 ====================

export function fetchSalaryItems(enabled?: number) {
  return request<SalaryItemDef[]>({
    url: '/salary/item/list',
    method: 'get',
    params: { enabled }
  });
}

export function saveSalaryItem(data: SalaryItemDef) {
  return request<null>({
    url: '/salary/item/save',
    method: 'post',
    data
  });
}

export function deleteSalaryItem(id: number) {
  return request<null>({
    url: `/salary/item/${id}`,
    method: 'delete'
  });
}

// ==================== 薪资方案 ====================

export function fetchSchemePage(params: { pageNum?: number; pageSize?: number; keyword?: string }) {
  return request<Api.Common.PageResult<SalaryScheme>>({
    url: '/salary/scheme/page',
    method: 'get',
    params
  });
}

export function fetchSchemeEnabled() {
  return request<SalaryScheme[]>({
    url: '/salary/scheme/list-enabled',
    method: 'get'
  });
}

export function fetchSchemeDetail(id: number) {
  return request<SalaryScheme>({
    url: `/salary/scheme/${id}`,
    method: 'get'
  });
}

export function saveScheme(
  data: Pick<SalaryScheme, 'id' | 'schemeName' | 'schemeCode' | 'enabled' | 'sortOrder' | 'remark'> & {
    items: { itemId: number; defaultAmount?: number }[];
  }
) {
  return request<SalaryScheme>({
    url: '/salary/scheme/save',
    method: 'post',
    data
  });
}

export function deleteScheme(id: number) {
  return request<null>({
    url: `/salary/scheme/${id}`,
    method: 'delete'
  });
}

// ==================== 薪资档案 ====================

export function fetchArchiveEmployeePage(params: {
  pageNum?: number;
  pageSize?: number;
  companyId?: number;
  deptId?: number;
  employeeName?: string;
  employeeNo?: string;
  archivedOnly?: boolean;
}) {
  return request<Api.Common.PageResult<SalaryArchiveRow>>({
    url: '/salary/archive/employee-page',
    method: 'get',
    params
  });
}

export function fetchArchiveItems(archiveId: number) {
  return request<SalaryArchiveItem[]>({
    url: `/salary/archive/${archiveId}/items`,
    method: 'get'
  });
}

export function bindArchive(data: { employeeId: number; schemeId: number; effectiveDate?: string; remark?: string }) {
  return request<SalaryArchiveRow>({
    url: '/salary/archive/bind',
    method: 'post',
    data
  });
}

export function updateArchiveItems(archiveId: number, items: { itemId: number; amount: number }[]) {
  return request<null>({
    url: `/salary/archive/${archiveId}/items`,
    method: 'post',
    data: items
  });
}

export function unbindArchive(archiveId: number) {
  return request<null>({
    url: `/salary/archive/${archiveId}`,
    method: 'delete'
  });
}

// ==================== 工资核算 ====================

export function fetchBatchPage(params: {
  pageNum?: number;
  pageSize?: number;
  yearMonth?: string;
  companyId?: number;
}) {
  return request<Api.Common.PageResult<PayrollBatch>>({
    url: '/salary/payroll/batch-page',
    method: 'get',
    params
  });
}

export function createPayrollBatch(data: { yearMonth: string; companyId: number; remark?: string }) {
  return request<PayrollBatch>({
    url: '/salary/payroll/create',
    method: 'post',
    data
  });
}

export function deleteBatch(id: number) {
  return request<null>({
    url: `/salary/payroll/${id}`,
    method: 'delete'
  });
}

export function computeBatch(id: number) {
  return request<PayrollBatch>({
    url: `/salary/payroll/${id}/compute`,
    method: 'post'
  });
}

export function confirmBatch(id: number) {
  return request<null>({
    url: `/salary/payroll/${id}/confirm`,
    method: 'post'
  });
}

export function publishBatch(id: number) {
  return request<null>({
    url: `/salary/payroll/${id}/publish`,
    method: 'post'
  });
}

export function unlockBatch(id: number) {
  return request<null>({
    url: `/salary/payroll/${id}/unlock`,
    method: 'post'
  });
}

export function fetchPayslipPage(
  batchId: number,
  params: { pageNum?: number; pageSize?: number; employeeName?: string; employeeNo?: string }
) {
  return request<Api.Common.PageResult<Payslip>>({
    url: `/salary/payroll/${batchId}/payslip-page`,
    method: 'get',
    params
  });
}

export function fetchPayslipDetail(payslipId: number) {
  return request<{ payslip: Payslip; yearMonth: string; batchStatus: number; items: PayrollItem[] }>({
    url: `/salary/payroll/payslip/${payslipId}`,
    method: 'get'
  });
}

export function updateManualItem(payrollItemId: number, amount: number) {
  return request<null>({
    url: `/salary/payroll/item/${payrollItemId}/amount`,
    method: 'post',
    data: { amount }
  });
}
