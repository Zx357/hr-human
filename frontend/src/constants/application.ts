/** 申请类型文案 */
export const appTypeMap: Record<string, string> = {
  leave: '请假申请',
  overtime: '加班申请',
  business: '出差申请',
  makeup: '补卡申请',
  exchange: '换休申请',
  regularization: '转正申请',
  transfer: '调动申请',
  reward: '奖励申请',
  punish: '惩罚申请',
  resignation: '离职申请'
};

/** 申请单据状态：0-待审批 1-已通过 2-已拒绝 3-已撤销 */
export const statusMap: Record<number, { label: string; type: string }> = {
  0: { label: '待审批', type: 'warning' },
  1: { label: '已通过', type: 'success' },
  2: { label: '已拒绝', type: 'danger' },
  3: { label: '已撤销', type: 'info' }
};

/** 申请类型简称（用于首页待审批徽标等紧凑展示） */
export const appTypeShortMap: Record<string, string> = {
  leave: '请假',
  overtime: '加班',
  business: '出差',
  makeup: '补卡',
  exchange: '换休',
  regularization: '转正',
  transfer: '调动',
  reward: '奖励',
  punish: '惩罚',
  resignation: '离职'
};

/** 申请类型徽标配色（与简称配套使用） */
export const appTypeColorMap: Record<string, string> = {
  leave: '#6366f1',
  overtime: '#f59e0b',
  business: '#06b6d4',
  makeup: '#8b5cf6',
  exchange: '#10b981',
  regularization: '#3b82f6',
  transfer: '#ec4899',
  reward: '#22c55e',
  punish: '#ef4444',
  resignation: '#64748b'
};

/** 各申请类型的时长单位：请假/加班/换休/补卡按小时，出差按天 */
export const durationUnitMap: Record<string, string> = {
  leave: '小时',
  overtime: '小时',
  exchange: '小时',
  makeup: '小时',
  business: '天'
};

/** 根据申请类型获取时长单位，默认按天 */
export function getDurationUnit(appType?: string): string {
  return (appType && durationUnitMap[appType]) || '天';
}
