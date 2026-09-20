import dayjs from 'dayjs';

/** 默认日期时间格式 */
export const DEFAULT_DATETIME_FORMAT = 'YYYY-MM-DD HH:mm';

/**
 * 统一的时间格式化工具
 *
 * 后端返回的 createdTime/clockTime 等可能是 `2026-01-02T15:04:05` 带 T 的 ISO 字符串，
 * 各列表页统一使用本函数渲染，避免手写 replace/slice。
 *
 * @param value 时间字符串/Date/时间戳，空值返回 '-'
 * @param format dayjs 格式串，默认 YYYY-MM-DD HH:mm
 */
export function formatDateTime(
  value?: string | number | Date | null,
  format: string = DEFAULT_DATETIME_FORMAT
): string {
  if (value === undefined || value === null || value === '') {
    return '-';
  }
  const d = dayjs(value);
  return d.isValid() ? d.format(format) : String(value);
}
