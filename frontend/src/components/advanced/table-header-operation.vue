<script setup lang="ts">
import { $t } from '@/locales';

defineOptions({ name: 'TableHeaderOperation' });

type TableDensity = 'large' | 'default' | 'small';

interface Props {
  disabledDelete?: boolean;
  loading?: boolean;
}

defineProps<Props>();

interface Emits {
  (e: 'add'): void;
  (e: 'delete'): void;
  (e: 'refresh'): void;
}

const emit = defineEmits<Emits>();

const columns = defineModel<UI.TableColumnCheck[]>('columns', {
  default: () => []
});

/** 表格密度，配合 ElTable 的 size 属性使用 */
const density = defineModel<TableDensity>('density', {
  default: 'default'
});

const densityOptions: Array<{ value: TableDensity; labelKey: App.I18n.I18nKey }> = [
  { value: 'large', labelKey: 'common.sizeLarge' },
  { value: 'default', labelKey: 'common.sizeDefault' },
  { value: 'small', labelKey: 'common.sizeSmall' }
];

function add() {
  emit('add');
}

function batchDelete() {
  emit('delete');
}

function refresh() {
  emit('refresh');
}
</script>

<template>
  <ElSpace direction="horizontal" wrap justify="end" class="lt-sm:w-200px">
    <slot name="prefix"></slot>
    <slot name="default">
      <ElButton plain type="primary" @click="add">
        <template #icon>
          <icon-ic-round-plus class="text-icon" />
        </template>
        {{ $t('common.add') }}
      </ElButton>
      <ElPopconfirm :title="$t('common.confirmDelete')" @confirm="batchDelete">
        <template #reference>
          <ElButton type="danger" plain :disabled="disabledDelete">
            <template #icon>
              <icon-ic-round-delete class="text-icon" />
            </template>
            {{ $t('common.batchDelete') }}
          </ElButton>
        </template>
      </ElPopconfirm>
    </slot>
    <ElButton @click="refresh">
      <template #icon>
        <icon-mdi-refresh class="text-icon" :class="{ 'animate-spin': loading }" />
      </template>
      {{ $t('common.refresh') }}
    </ElButton>
    <ElDropdown trigger="click" @command="(cmd: TableDensity) => (density = cmd)">
      <ElButton :title="$t('common.density')">
        <template #icon>
          <icon-mdi-dots-horizontal class="text-icon" />
        </template>
      </ElButton>
      <template #dropdown>
        <ElDropdownMenu>
          <ElDropdownItem
            v-for="option in densityOptions"
            :key="option.value"
            :command="option.value"
            :class="{ 'text-primary': density === option.value }"
          >
            {{ $t(option.labelKey) }}
          </ElDropdownItem>
        </ElDropdownMenu>
      </template>
    </ElDropdown>
    <TableColumnSetting v-model:columns="columns" />
    <slot name="suffix"></slot>
  </ElSpace>
</template>

<style scoped></style>
