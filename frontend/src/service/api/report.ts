import { request } from '../request';

// ==================== 员工报表统计（管理员接口） ====================

export interface EmployeeSummaryTotals {
  total: number;
  active: number;
  probation: number;
  resigned: number;
}

export interface NameValueItem {
  name: string;
  value: number;
}

export interface MonthlyTrendItem {
  /** YYYY-MM */
  month: string;
  entry: number;
  exit: number;
}

export interface EmployeeReportSummary {
  totals: EmployeeSummaryTotals;
  gender: NameValueItem[];
  education: NameValueItem[];
  ageBuckets: NameValueItem[];
  deptDistribution: NameValueItem[];
  monthlyTrend: MonthlyTrendItem[];
}

export function fetchEmployeeReportSummary(params?: { companyId?: number; deptId?: number }) {
  return request<EmployeeReportSummary>({
    url: '/report/employee/summary',
    method: 'get',
    params
  });
}

// ==================== 考勤报表统计（管理员接口） ====================

export interface AttendanceSummaryOverview {
  workDays: number;
  actualDays: number;
  lateTimes: number;
  earlyTimes: number;
  absentDays: number;
  leaveDays: number;
  /** 可能是小数(0.93)、百分比数值(93)或字符串("93%") */
  attendanceRate: number | string;
}

export interface AttendanceDeptRankingItem {
  deptName: string;
  attendanceRate: number | string;
  lateTimes: number;
  absentDays: number;
}

export interface AttendanceReportSummary {
  overview: AttendanceSummaryOverview;
  deptRanking: AttendanceDeptRankingItem[];
}

export function fetchAttendanceReportSummary(params: { month: string; companyId?: number; deptId?: number }) {
  return request<AttendanceReportSummary>({
    url: '/report/attendance/summary',
    method: 'get',
    params
  });
}

export interface AttendanceDailyTrendItem {
  /** YYYY-MM-DD */
  date: string;
  normal: number;
  abnormal: number;
}

/** 考勤按日趋势（服务端聚合，最多31天） */
export function fetchAttendanceDailyTrend(params: { startDate: string; endDate: string }) {
  return request<AttendanceDailyTrendItem[]>({
    url: '/report/attendance/daily-trend',
    method: 'get',
    params
  });
}
