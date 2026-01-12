import { request } from '../request';

export interface AttCalendarRule {
  id?: number;
  companyId?: number;
  ruleType: number; // 1-单休(周日) 2-双休 3-单休(周六) 4-法定假日 5-调休上班
  startDate?: string;
  endDate?: string;
  ruleName?: string;
  remark?: string;
  companyName?: string;
}

// 获取所有规则
export function fetchCalendarRules() {
  return request<AttCalendarRule[]>({ url: '/calendar/rules', method: 'get' });
}

// 保存规则
export function saveCalendarRule(data: AttCalendarRule) {
  return request({ url: '/calendar/rule', method: 'post', data });
}

// 删除规则
export function deleteCalendarRule(id: number) {
  return request({ url: `/calendar/rule/${id}`, method: 'delete' });
}
