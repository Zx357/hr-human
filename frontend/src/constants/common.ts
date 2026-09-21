import { transformRecordToOption } from '@/utils/common';
import { $t } from '@/locales';

export const yesOrNoRecord: Record<CommonType.YesOrNo, App.I18n.I18nKey> = {
  Y: 'common.yesOrNo.yes',
  N: 'common.yesOrNo.no'
};

export const yesOrNoOptions = transformRecordToOption(yesOrNoRecord);

/** ElTag type 的窄类型（替代各页面 statusMap type 字段上的 as any） */
export type TagType = 'primary' | 'success' | 'warning' | 'danger' | 'info';

/**
 * 通用「启用/停用」状态（0-停用 1-启用）。
 * approval/flow（原 0-停用/info）与 attendance/shift（原 0-禁用/danger）语义相同，合并共用：
 * 停用统一展示「禁用」+ danger 色
 */
export const enableStatusMap: Record<number, { labelKey: App.I18n.I18nKey; type: TagType }> = {
  0: { labelKey: 'common.disable', type: 'danger' },
  1: { labelKey: 'common.enable', type: 'success' }
};

/** 通用启用/停用状态展示文案（未知状态显示 -） */
export function enableStatusLabel(status?: number): string {
  const item = status !== undefined ? enableStatusMap[status] : undefined;
  return item ? $t(item.labelKey) : '-';
}
