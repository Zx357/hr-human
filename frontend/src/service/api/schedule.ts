import { request } from '../request';

export interface ScheduleRow {
  employeeId: number;
  employeeName: string;
  employeeNo: string;
  deptId?: number;
  schedule: Record<string, { shiftId: number; shiftName: string; shiftCode: string }>;
}

export function fetchWeekSchedule(params: {
  orgIds?: number[];
  employeeNo?: string;
  employeeName?: string;
  startDate: string;
  endDate: string;
}) {
  return request<ScheduleRow[]>({
    url: '/attendance/schedule/week',
    method: 'get',
    params: { ...params, orgIds: params.orgIds?.length ? params.orgIds.join(',') : undefined }
  });
}

export function saveSchedule(data: { employeeId: number; shiftId: number | null; scheduleDate: string }) {
  return request<boolean>({ url: '/attendance/schedule/save', method: 'post', data });
}

export function batchSchedule(data: { employeeIds: number[]; shiftId: number; startDate: string; endDate: string }) {
  return request<boolean>({ url: '/attendance/schedule/batch', method: 'post', data });
}
