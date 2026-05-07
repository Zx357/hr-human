import { request } from '../request';

type OrgIdsParam = string | number[];

function normalizeOrgIds(orgIds?: OrgIdsParam) {
  if (Array.isArray(orgIds)) {
    return orgIds.join(',');
  }

  return orgIds;
}

export interface AttClockRecord {
  id?: number;
  employeeId: number;
  clockTime: string;
  clockType: number;
  clockMethod?: number;
  location?: string;
  deviceInfo?: string;
  remark?: string;
  employeeName?: string;
  employeeNo?: string;
  companyName?: string;
  deptName?: string;
}

export interface AttDailyRecord {
  id?: number;
  employeeId: number;
  attDate: string;
  shiftId?: number;
  periodId?: number;
  periodName?: string;
  scheduledIn?: string;
  scheduledOut?: string;
  actualIn?: string;
  actualOut?: string;
  status?: number;
  lateMinutes?: number;
  earlyMinutes?: number;
  workHours?: number;
  overtimeHours?: number;
  remark?: string;
  locked?: number;
  employeeName?: string;
  employeeNo?: string;
  companyName?: string;
  deptName?: string;
  shiftName?: string;
  periods?: AttDailyRecord[];
  overtimeDuration?: number;
  businessDuration?: number;
  annualLeaveDuration?: number;
  personalLeaveDuration?: number;
  sickLeaveDuration?: number;
  marriageLeaveDuration?: number;
  maternityLeaveDuration?: number;
  paternityLeaveDuration?: number;
  bereavementLeaveDuration?: number;
}

export interface AttLocation {
  id?: number;
  locationName: string;
  address?: string;
  latitude?: number;
  longitude?: number;
  clockRange?: number;
  status?: number;
  remark?: string;
  assignedCount?: number;
  employeeIds?: number[];
  employees?: Api.Hr.Employee[];
}

export function fetchClockRecordPage(params: {
  page: number;
  size: number;
  companyId?: number;
  deptId?: number;
  orgIds?: OrgIdsParam;
  employeeName?: string;
  startDate?: string;
  endDate?: string;
}) {
  const { orgIds, ...rest } = params;

  return request<{ records: AttClockRecord[]; total: number }>({
    url: '/attendance/clock/page',
    method: 'get',
    params: { ...rest, orgIds: normalizeOrgIds(orgIds) }
  });
}

export function saveClockRecord(data: AttClockRecord) {
  return request({ url: '/attendance/clock', method: 'post', data });
}

export function deleteClockRecord(id: number) {
  return request({ url: `/attendance/clock/${id}`, method: 'delete' });
}

export function fetchDailyRecordPage(params: {
  page: number;
  size: number;
  startDate?: string;
  endDate?: string;
  companyId?: number;
  deptId?: number;
  orgIds?: OrgIdsParam;
  employeeNo?: string;
  employeeName?: string;
  status?: number;
}) {
  const { orgIds, ...rest } = params;

  return request<{ records: AttDailyRecord[]; total: number }>({
    url: '/attendance/daily/page',
    method: 'get',
    params: { ...rest, orgIds: normalizeOrgIds(orgIds) }
  });
}

export function saveDailyRecord(data: AttDailyRecord) {
  return request({ url: '/attendance/daily', method: 'post', data });
}

export function calculateDailyAttendance(data: {
  startDate: string;
  endDate: string;
  companyId?: number;
  deptId?: number;
  orgIds?: number[];
  employeeNo?: string;
  employeeName?: string;
  employeeIds?: number[];
}) {
  return request({ url: '/attendance/daily/calculate', method: 'post', data });
}

export function lockDailyRecords(data: { ids: number[]; lock: boolean }) {
  return request({ url: '/attendance/daily/lock', method: 'post', data });
}

export interface MonthlyAttendance {
  employeeId: number;
  employeeName: string;
  employeeNo: string;
  companyId: number;
  companyName: string;
  deptId: number;
  deptName: string;
  month: string;
  workDays: number;
  actualDays: number;
  lateTimes: number;
  earlyTimes: number;
  absentDays: number;
  leaveDays: number;
  totalWorkHours: number;
  totalLateMinutes: number;
  totalEarlyMinutes: number;
}

export function fetchMonthlyAttendance(params: {
  month: string;
  companyId?: number;
  deptId?: number;
  orgIds?: OrgIdsParam;
  employeeNo?: string;
  employeeName?: string;
}) {
  const { orgIds, ...rest } = params;

  return request<MonthlyAttendance[]>({
    url: '/attendance/monthly',
    method: 'get',
    params: { ...rest, orgIds: normalizeOrgIds(orgIds) }
  });
}

export function fetchAttLocationPage(params: { pageNum: number; pageSize: number; keyword?: string }) {
  return request<Api.Common.PageResult<AttLocation>>({
    url: '/attendance/location/page',
    method: 'get',
    params
  });
}

export function fetchAttLocationDetail(id: number) {
  return request<AttLocation>({
    url: `/attendance/location/${id}`,
    method: 'get'
  });
}

export function createAttLocation(data: AttLocation) {
  return request<boolean>({
    url: '/attendance/location',
    method: 'post',
    data
  });
}

export function updateAttLocation(id: number, data: AttLocation) {
  return request<boolean>({
    url: `/attendance/location/${id}`,
    method: 'put',
    data
  });
}

export function deleteAttLocation(id: number) {
  return request<boolean>({
    url: `/attendance/location/${id}`,
    method: 'delete'
  });
}
