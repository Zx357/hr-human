import { ref } from 'vue';
import { fetchDictDataByCode } from '@/service/api/system';
import { getDictLabelByValue } from '@/utils/dict';

/** 模块级缓存：按 dictType 共享字典数据，避免每个页面重复请求 */
const dictCache = new Map<string, Api.System.DictData[]>();
const inflightRequests = new Map<string, Promise<Api.System.DictData[]>>();

async function loadDictType(dictType: string): Promise<Api.System.DictData[]> {
  const cached = dictCache.get(dictType);
  if (cached) {
    return cached;
  }

  let inflight = inflightRequests.get(dictType);
  if (!inflight) {
    inflight = fetchDictDataByCode(dictType)
      .then(res => {
        const list = res.data || [];
        dictCache.set(dictType, list);
        return list;
      })
      .finally(() => {
        inflightRequests.delete(dictType);
      });
    inflightRequests.set(dictType, inflight);
  }

  return inflight;
}

/**
 * 字典选项 composable
 *
 * 用法：`const { options, getDictLabel } = useDictOptions('leave_type')`
 * - options：字典数据列表（模块级按 dictType 缓存，多页面共享）
 * - getDictLabel(value)：按当前 locale 取 dictLabelEn/dictLabel
 */
export function useDictOptions(dictType: string) {
  const options = ref<Api.System.DictData[]>([]);

  loadDictType(dictType)
    .then(list => {
      options.value = list;
    })
    .catch(() => {
      options.value = [];
    });

  /** 根据字典值获取当前语言的标签 */
  function getDictLabel(value?: string | number): string {
    if (value === undefined || value === null || value === '') {
      return '';
    }
    return getDictLabelByValue(options.value, value);
  }

  return { options, getDictLabel };
}
