import { getLocale } from '@/locales';

/**
 * 根据当前语言获取字典标签
 * @param dictData 字典数据
 * @returns 当前语言对应的标签
 */
export function getDictLabel(dictData: Api.System.DictData): string {
  const locale = getLocale();

  // 如果是英文环境且有英文标签，使用英文标签
  if (locale === 'en-US' && dictData.dictLabelEn) {
    return dictData.dictLabelEn;
  }

  // 默认返回中文标签
  return dictData.dictLabel;
}

/**
 * 根据当前语言获取字典类型名称
 * @param dictType 字典类型
 * @returns 当前语言对应的名称
 */
export function getDictTypeName(dictType: Api.System.DictType): string {
  const locale = getLocale();

  // 如果是英文环境且有英文名称，使用英文名称
  if (locale === 'en-US' && dictType.dictNameEn) {
    return dictType.dictNameEn;
  }

  // 默认返回中文名称
  return dictType.dictName;
}

/**
 * 根据字典值获取对应的标签（支持多语言）
 * @param dictDataList 字典数据列表
 * @param value 字典值
 * @returns 当前语言对应的标签
 */
export function getDictLabelByValue(dictDataList: Api.System.DictData[], value: string | number): string {
  const dictData = dictDataList.find(item => item.dictValue === String(value));
  if (dictData) {
    return getDictLabel(dictData);
  }
  return String(value);
}

/**
 * 将字典数据转换为选项列表（支持多语言）
 * @param dictDataList 字典数据列表
 * @returns 选项列表
 */
export function dictToOptions(dictDataList: Api.System.DictData[]): { label: string; value: string }[] {
  return dictDataList.map(item => ({
    label: getDictLabel(item),
    value: item.dictValue
  }));
}
