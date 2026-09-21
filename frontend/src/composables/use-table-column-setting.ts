import { ref, watch } from 'vue';

export type TableDensity = 'large' | 'default' | 'small';

const DENSITY_STORAGE_KEY = 'kadmin:table:density';

/**
 * 表格列设置 + 密度状态（localStorage 持久化）。
 *
 * 配合 components/advanced/table-header-operation.vue 使用：
 * - columnChecks 驱动列设置弹窗（勾选/拖拽排序），页面用 isColumnVisible(prop) 控制 ElTableColumn 的 v-if
 * - density 绑定 ElTable 的 size 属性
 */
export function useTableColumnSetting(
  storageKey: string,
  columns: UI.TableColumnCheck[],
  initialDensity: TableDensity = 'default'
) {
  const storageFullKey = `kadmin:table:columns:${storageKey}`;

  function loadChecks(): UI.TableColumnCheck[] {
    try {
      const raw = localStorage.getItem(storageFullKey);
      if (!raw) return columns;
      const saved = JSON.parse(raw) as Array<Partial<UI.TableColumnCheck>>;
      // 以传入列定义为准合并，列变更后不会残留旧配置
      return columns.map(col => {
        const hit = saved.find(item => item.prop === col.prop);
        return hit ? { ...col, checked: Boolean(hit.checked) } : col;
      });
    } catch {
      return columns;
    }
  }

  const columnChecks = ref<UI.TableColumnCheck[]>(loadChecks());

  function loadDensity(): TableDensity {
    const value = localStorage.getItem(DENSITY_STORAGE_KEY);
    return value === 'large' || value === 'small' ? value : initialDensity;
  }

  const density = ref<TableDensity>(loadDensity());

  watch(
    columnChecks,
    value => {
      try {
        localStorage.setItem(storageFullKey, JSON.stringify(value));
      } catch {
        // localStorage 不可用时静默降级为不持久化
      }
    },
    { deep: true }
  );

  watch(density, value => {
    try {
      localStorage.setItem(DENSITY_STORAGE_KEY, value);
    } catch {
      // localStorage 不可用时静默降级为不持久化
    }
  });

  /** 列是否显示（用于 ElTableColumn 的 v-if） */
  function isColumnVisible(prop: string): boolean {
    return columnChecks.value.find(item => item.prop === prop)?.checked ?? true;
  }

  return {
    columnChecks,
    density,
    isColumnVisible
  };
}
