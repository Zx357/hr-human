import { request } from '../request';

export interface ScheduleRow {
  employeeId: number;
  employeeName: string;
  employeeNo: string;
  deptId?: number;
  schedule: Record<string, { shiftId: number; shiftName: string; shiftCode: string }>;
}

export function fetchWeekSchedule(params: { deptId?: number; employeeName?: string; startDate: string; endDate: string }) {
  return request<ScheduleRow[]>({ url: '/attendance/schedule/week', method: 'get', params });
}

export function saveSchedule(data: { employeeId: number; shiftId: number | null; scheduleDate: string }) {
  return request<boolean>({ url: '/attendance/schedule/save', method: 'post', data });
}

export function batchSchedule(data: { employeeIds: number[]; shiftId: number; startDate: string; endDate: string }) {
  return request<boolean>({ url: '/attendance/schedule/batch', method: 'post', data });
}
