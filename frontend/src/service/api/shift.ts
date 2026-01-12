import { request } from '../request';

export interface ShiftPeriod {
  id?: number;
  shiftId?: number;
  periodName?: string;
  startTime: string;
  endTime: string;
  crossDay?: number;
  needClockIn?: number;
  needClockOut?: number;
  sortOrder?: number;
}

export interface Shift {
  id?: number;
  companyId?: number;
  shiftCode: string;
  shiftName: string;
  workStartTime?: string;
  workEndTime?: string;
  lateMinutes?: number;
  earlyMinutes?: number;
  workHours?: number;
  isNextDay?: number;
  restStartTime?: string;
  restEndTime?: string;
  status?: number;
  remark?: string;
  periods?: ShiftPeriod[];
}

export function fetchShiftList() {
  return request<Shift[]>({ url: '/attendance/shift/list', method: 'get' });
}

export function fetchShiftById(id: number) {
  return request<Shift>({ url: `/attendance/shift/${id}`, method: 'get' });
}

export function createShift(data: Shift) {
  return request<boolean>({ url: '/attendance/shift', method: 'post', data });
}

export function updateShift(data: Shift) {
  return request<boolean>({ url: '/attendance/shift', method: 'put', data });
}

export function deleteShift(id: number) {
  return request<boolean>({ url: `/attendance/shift/${id}`, method: 'delete' });
}
