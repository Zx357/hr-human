import { request } from '../request';

export interface AttClockRecord {
  id?: number;
  employeeId: number;
  clockTime: string;
  clockType: number; // 1-上班 2-下班
  clockMethod?: number; // 1-APP 2-考勤机 3-手动补卡
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
  status?: number; // 0-未处理 1-正常 2-迟到 3-早退 4-旷工 5-请假 6-出差 7-迟到+早退
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
  periods?: AttDailyRecord[]; // 各时段明细
}

// 打卡记录分页
export function fetchClockRecordPage(params: { page: number; size: number; companyId?: number; deptId?: number; employeeName?: string; startDate?: string; endDate?: string }) {
  return request<{ records: AttClockRecord[]; total: number }>({ url: '/attendance/clock/page', method: 'get', params });
}

// 保存打卡记录
export function saveClockRecord(data: AttClockRecord) {
  return request({ url: '/attendance/clock', method: 'post', data });
}

// 删除打卡记录
export function deleteClockRecord(id: number) {
  return request({ url: `/attendance/clock/${id}`, method: 'delete' });
}

// 日考勤分页
export function fetchDailyRecordPage(params: { page: number; size: number; startDate?: string; endDate?: string; companyId?: number; deptId?: number; employeeNo?: string; employeeName?: string; status?: number }) {
  return request<{ records: AttDailyRecord[]; total: number }>({ url: '/attendance/daily/page', method: 'get', params });
}

// 保存日考勤记录
export function saveDailyRecord(data: AttDailyRecord) {
  return request({ url: '/attendance/daily', method: 'post', data });
}

// 计算日考勤
export function calculateDailyAttendance(data: { startDate: string; endDate: string; companyId?: number; deptId?: number; employeeNo?: string; employeeName?: string; employeeIds?: number[] }) {
  return request({ url: '/attendance/daily/calculate', method: 'post', data });
}

// 锁定/解锁日考勤
export function lockDailyRecords(data: { ids: number[]; lock: boolean }) {
  return request({ url: '/attendance/daily/lock', method: 'post', data });
}

// 月考勤汇总
export interface MonthlyAttendance {
  employeeId: number;
  employeeName: string;
  employeeNo: string;
  companyId: number;
  companyName: string;
  deptId: number;
  deptName: string;
  month: string;
  workDays: number;      // 应出勤天数
  actualDays: number;    // 实出勤天数
  lateTimes: number;     // 迟到次数
  earlyTimes: number;    // 早退次数
  absentDays: number;    // 旷工天数
  leaveDays: number;     // 请假天数
  totalWorkHours: number; // 总工时
  totalLateMinutes: number; // 总迟到分钟
  totalEarlyMinutes: number; // 总早退分钟
}

export function fetchMonthlyAttendance(params: { month: string; companyId?: number; deptId?: number; employeeNo?: string; employeeName?: string }) {
  return request<MonthlyAttendance[]>({ url: '/attendance/monthly', method: 'get', params });
}
