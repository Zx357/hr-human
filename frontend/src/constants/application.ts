import type { TagType } from '@/constants/common';
import { $t } from '@/locales';

export type { TagType };

/** 申请类型文案（i18n key，展示请用 appTypeLabel()） */
export const appTypeMap: Record<string, App.I18n.I18nKey> = {
  leave: 'application.types.leave',
  overtime: 'application.types.overtime',
  business: 'application.types.business',
  makeup: 'application.types.makeup',
  exchange: 'application.types.exchange',
  regularization: 'application.types.regularization',
  transfer: 'application.types.transfer',
  reward: 'application.types.reward',
  punish: 'application.types.punish',
  resignation: 'application.types.resignation'
};

/** 申请单据状态：0-待审批 1-已通过 2-已拒绝 3-已撤销（labelKey 展示请用 statusLabel()） */
export const statusMap: Record<number, { labelKey: App.I18n.I18nKey; type: TagType }> = {
  0: { labelKey: 'application.status.pending', type: 'warning' },
  1: { labelKey: 'application.status.approved', type: 'success' },
  2: { labelKey: 'application.status.rejected', type: 'danger' },
  3: { labelKey: 'application.status.cancelled', type: 'info' }
};

/** 申请类型简称（用于首页待审批徽标等紧凑展示；i18n key） */
export const appTypeShortMap: Record<string, App.I18n.I18nKey> = {
  leave: 'application.typeShort.leave',
  overtime: 'application.typeShort.overtime',
  business: 'application.typeShort.business',
  makeup: 'application.typeShort.makeup',
  exchange: 'application.typeShort.exchange',
  regularization: 'application.typeShort.regularization',
  transfer: 'application.typeShort.transfer',
  reward: 'application.typeShort.reward',
  punish: 'application.typeShort.punish',
  resignation: 'application.typeShort.resignation'
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

/** 各申请类型的时长单位：请假/加班/换休/补卡按小时，出差按天（i18n key） */
export const durationUnitMap: Record<string, App.I18n.I18nKey> = {
  leave: 'application.durationUnit.hour',
  overtime: 'application.durationUnit.hour',
  exchange: 'application.durationUnit.hour',
  makeup: 'application.durationUnit.hour',
  business: 'application.durationUnit.day'
};

/** 根据申请类型获取时长单位 key，默认按天 */
export function getDurationUnitKey(appType?: string): App.I18n.I18nKey {
  return (appType && durationUnitMap[appType]) || 'application.durationUnit.day';
}

/** 申请类型显示文案（已翻译；未知类型回退原文） */
export function appTypeLabel(appType?: string): string {
  if (!appType) return '-';
  const key = appTypeMap[appType];
  return key ? $t(key) : appType;
}

/** 申请类型简称（已翻译；未知类型回退原文） */
export function appTypeShortLabel(appType?: string): string {
  if (!appType) return '';
  const key = appTypeShortMap[appType];
  return key ? $t(key) : appType;
}

/** 申请状态显示文案（已翻译；未知状态显示 -） */
export function statusLabel(status?: number): string {
  const item = status !== undefined ? statusMap[status] : undefined;
  return item ? $t(item.labelKey) : '-';
}

/** 申请状态对应的 ElTag type（未知状态回退 info，避免页面再写 as any） */
export function statusTagType(status?: number): TagType {
  return (status !== undefined ? statusMap[status]?.type : undefined) || 'info';
}

/** 时长单位显示文案（已翻译） */
export function getDurationUnit(appType?: string): string {
  return $t(getDurationUnitKey(appType));
}
