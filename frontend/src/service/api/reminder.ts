import { request } from '../request';

export interface ReminderItem {
  type: 'contract' | 'probation' | 'certificate';
  typeName: string;
  employeeId: number;
  employeeName: string;
  /** 到期日期，如 2026-09-30 */
  date: string;
  /** 剩余天数（负数表示已过期） */
  remainDays: number;
  detail?: string;
}

export interface ReminderListData {
  contracts: ReminderItem[];
  probations: ReminderItem[];
  certificates: ReminderItem[];
  total: number;
}

/** 获取到期提醒（合同 / 试用期 / 证书） */
export function fetchReminders(days = 30) {
  return request<ReminderListData>({
    url: '/reminder/list',
    method: 'get',
    params: { days }
  });
}
