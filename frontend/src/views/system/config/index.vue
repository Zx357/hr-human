<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import type { FormInstance, FormRules } from 'element-plus';
import { type SysConfig, createConfig, deleteConfig, fetchConfigList, updateConfig } from '@/service/api/config';
import { $t } from '@/locales';

defineOptions({ name: 'SysConfigManage' });

const loading = ref(false);
const configList = ref<SysConfig[]>([]);

// 搜索条件
const searchKeyword = ref('');
const searchGroup = ref('');

// 分组字典（未知分组显示原文）
const knownGroups = ['map_config', 'wechat_config'];
const groupLabelMap = computed<Record<string, string>>(() => ({
  map_config: $t('sys.config.groupMap'),
  wechat_config: $t('sys.config.groupWechat')
}));
const groupTagTypeMap: Record<string, 'primary' | 'success' | 'info'> = {
  map_config: 'primary',
  wechat_config: 'success'
};

const groupOptions = computed(() => {
  const groups = new Set<string>(knownGroups);
  configList.value.forEach(item => {
    if (item.configGroup) {
      groups.add(item.configGroup);
    }
  });
  return Array.from(groups);
});

const filteredList = computed(() => {
  const keyword = searchKeyword.value.trim().toLowerCase();
  const group = searchGroup.value;
  return configList.value.filter(item => {
    const matchGroup = !group || item.configGroup === group;
    const matchKeyword =
      !keyword || item.configKey?.toLowerCase().includes(keyword) || item.configName?.toLowerCase().includes(keyword);
    return matchGroup && matchKeyword;
  });
});

// 明文展示的配置 id 集合
const revealedIds = ref<Set<number>>(new Set());

// 新增/编辑弹窗
const drawerVisible = ref(false);
const operateType = ref<'add' | 'edit'>('add');
const editingData = ref<Partial<SysConfig>>({});
const submitLoading = ref(false);
const formRef = ref<FormInstance>();

const configKeyPattern = /^[A-Za-z][A-Za-z0-9]*(?:\.[A-Za-z0-9-]+)+$/;

const formRules: FormRules = {
  configKey: [{ required: true, message: $t('sys.config.pleaseEnterConfigKey'), trigger: 'blur' }],
  configName: [{ required: true, message: $t('sys.config.pleaseEnterConfigName'), trigger: 'blur' }]
};

async function loadData() {
  loading.value = true;
  try {
    const { data, error } = await fetchConfigList();
    if (!error && data) {
      configList.value = data;
    }
  } finally {
    loading.value = false;
  }
}

function getGroupLabel(group?: string) {
  if (!group) {
    return $t('common.unknown');
  }
  return groupLabelMap.value[group] || group;
}

function getGroupTagType(group?: string): 'primary' | 'success' | 'info' {
  return (group && groupTagTypeMap[group]) || 'info';
}

function isFlagOn(value: unknown) {
  return value === 1 || value === true || value === '1';
}

/** 脱敏显示：前 4 位 + **** + 后 4 位，长度 ≤ 8 全遮 */
function maskValue(value?: string) {
  if (!value) {
    return '';
  }
  if (value.length <= 8) {
    return '********';
  }
  return `${value.slice(0, 4)}****${value.slice(-4)}`;
}

function isRevealed(row: SysConfig) {
  return revealedIds.value.has(row.id);
}

function toggleReveal(row: SysConfig) {
  const next = new Set(revealedIds.value);
  if (next.has(row.id)) {
    next.delete(row.id);
  } else {
    next.add(row.id);
  }
  revealedIds.value = next;
}

/** 关键字/分组为前端过滤（列表接口无查询参数），点击搜索时顺带刷新数据 */
function handleSearch() {
  loadData();
}

function handleReset() {
  searchKeyword.value = '';
  searchGroup.value = '';
}

function handleAdd() {
  operateType.value = 'add';
  editingData.value = {
    configKey: '',
    configName: '',
    configValue: '',
    configGroup: '',
    isPublic: 0,
    status: 1,
    sortOrder: 0,
    remark: ''
  };
  drawerVisible.value = true;
}

function handleEdit(row: SysConfig) {
  operateType.value = 'edit';
  editingData.value = {
    ...row,
    isPublic: isFlagOn(row.isPublic) ? 1 : 0,
    status: isFlagOn(row.status) ? 1 : 0
  };
  drawerVisible.value = true;
}

async function handleSave() {
  const valid = await formRef.value?.validate().catch(() => false);
  if (!valid) {
    return;
  }

  // 配置键建议 xx.xx.xx 格式：提示但不强制
  if (editingData.value.configKey && !configKeyPattern.test(editingData.value.configKey)) {
    try {
      await ElMessageBox.confirm($t('sys.config.keyFormatConfirm'), $t('common.tip'), {
        type: 'warning',
        confirmButtonText: $t('sys.config.saveAnyway'),
        cancelButtonText: $t('common.cancel')
      });
    } catch {
      return;
    }
  }

  submitLoading.value = true;
  try {
    const isEdit = operateType.value === 'edit';
    const { error } = await (isEdit ? updateConfig(editingData.value) : createConfig(editingData.value));
    if (!error) {
      ElMessage.success(isEdit ? $t('common.updateSuccess') : $t('common.addSuccess'));
      drawerVisible.value = false;
      loadData();
    }
  } finally {
    submitLoading.value = false;
  }
}

async function handleDelete(row: SysConfig) {
  try {
    await ElMessageBox.confirm($t('sys.config.deleteConfirm'), $t('common.deleteConfirmTitle'), {
      type: 'warning',
      confirmButtonText: $t('common.confirm'),
      cancelButtonText: $t('common.cancel')
    });

    const { error } = await deleteConfig(row.id);
    if (!error) {
      ElMessage.success($t('common.deleteSuccess'));
      loadData();
    }
  } catch {
    // 用户取消删除，不做任何处理
  }
}

onMounted(() => {
  loadData();
});
</script>

<template>
  <div class="list-page">
    <ElCard>
      <template #header>
        <div class="flex items-center justify-between">
          <span>{{ $t('sys.config.paramConfig') }}</span>
          <div class="flex gap-8px">
            <ElButton v-permission="'system:config:add'" type="primary" size="small" @click="handleAdd">
              <template #icon><icon-ep-plus /></template>
              {{ $t('common.add') }}
            </ElButton>
          </div>
        </div>
      </template>

      <ElAlert :title="$t('sys.config.topTip')" type="info" show-icon :closable="false" class="mb-16px" />

      <div class="mb-16px flex items-center gap-12px">
        <ElInput
          v-model="searchKeyword"
          :placeholder="$t('sys.config.searchPlaceholder')"
          clearable
          class="w-260px"
          @keyup.enter="handleSearch"
        />
        <ElSelect v-model="searchGroup" :placeholder="$t('common.pleaseSelect')" clearable class="w-180px">
          <ElOption v-for="group in groupOptions" :key="group" :label="getGroupLabel(group)" :value="group" />
        </ElSelect>
        <ElButton type="primary" @click="handleSearch">
          <template #icon><icon-ep-search /></template>
          {{ $t('common.search') }}
        </ElButton>
        <ElButton @click="handleReset">
          <template #icon><icon-ep-refresh /></template>
          {{ $t('common.reset') }}
        </ElButton>
      </div>

      <ElTable v-loading="loading" :data="filteredList" border stripe>
        <ElTableColumn type="index" :label="$t('common.index2')" width="60" align="center" />
        <ElTableColumn prop="configKey" :label="$t('sys.config.configKey')" min-width="180">
          <template #default="{ row }">
            <ElTag size="small" type="info">{{ row.configKey }}</ElTag>
          </template>
        </ElTableColumn>
        <ElTableColumn prop="configName" :label="$t('sys.config.configName')" min-width="140">
          <template #default="{ row }">
            <span class="font-bold">{{ row.configName }}</span>
          </template>
        </ElTableColumn>
        <ElTableColumn prop="configGroup" :label="$t('sys.config.group')" width="120" align="center">
          <template #default="{ row }">
            <ElTag v-if="row.configGroup" size="small" :type="getGroupTagType(row.configGroup)">
              {{ getGroupLabel(row.configGroup) }}
            </ElTag>
            <span v-else>{{ $t('common.unknown') }}</span>
          </template>
        </ElTableColumn>
        <ElTableColumn prop="configValue" :label="$t('sys.config.configValue')" min-width="220">
          <template #default="{ row }">
            <div v-if="row.configValue" class="flex items-center gap-6px">
              <code class="rounded bg-[var(--el-fill-color)] px-8px py-2px text-13px">
                {{ isRevealed(row) ? row.configValue : maskValue(row.configValue) }}
              </code>
              <ElButton link size="small" @click="toggleReveal(row)">
                <ElIcon :size="16">
                  <icon-ep-hide v-if="isRevealed(row)" />
                  <icon-ep-view v-else />
                </ElIcon>
              </ElButton>
            </div>
            <span v-else class="text-[var(--el-text-color-placeholder)]">--</span>
          </template>
        </ElTableColumn>
        <ElTableColumn prop="isPublic" :label="$t('sys.config.isPublic')" width="90" align="center">
          <template #default="{ row }">
            <ElTag size="small" :type="isFlagOn(row.isPublic) ? 'warning' : 'info'">
              {{ isFlagOn(row.isPublic) ? $t('common.yesOrNo.yes') : $t('common.yesOrNo.no') }}
            </ElTag>
          </template>
        </ElTableColumn>
        <ElTableColumn prop="status" :label="$t('common.status')" width="80" align="center">
          <template #default="{ row }">
            <ElTag size="small" :type="isFlagOn(row.status) ? 'success' : 'danger'">
              {{ isFlagOn(row.status) ? $t('common.enable') : $t('common.disable') }}
            </ElTag>
          </template>
        </ElTableColumn>
        <ElTableColumn prop="remark" :label="$t('common.remark')" min-width="160" show-overflow-tooltip />
        <ElTableColumn :label="$t('common.action')" width="120" align="center" fixed="right">
          <template #default="{ row }">
            <ElButton v-permission="'system:config:edit'" type="primary" link size="small" @click="handleEdit(row)">
              {{ $t('common.edit') }}
            </ElButton>
            <ElButton v-permission="'system:config:delete'" type="danger" link size="small" @click="handleDelete(row)">
              {{ $t('common.delete') }}
            </ElButton>
          </template>
        </ElTableColumn>
      </ElTable>
    </ElCard>

    <ElDrawer
      v-model="drawerVisible"
      :title="operateType === 'add' ? $t('sys.config.addConfig') : $t('sys.config.editConfig')"
      size="500px"
    >
      <ElForm ref="formRef" :model="editingData" :rules="formRules" label-width="100px">
        <ElFormItem :label="$t('sys.config.configKey')" prop="configKey">
          <ElInput
            v-model="editingData.configKey"
            :placeholder="$t('sys.config.pleaseEnterConfigKey')"
            :disabled="operateType === 'edit'"
          />
          <div class="mt-4px text-12px text-gray-400">{{ $t('sys.config.keyFormatTip') }}</div>
        </ElFormItem>
        <ElFormItem :label="$t('sys.config.configName')" prop="configName">
          <ElInput v-model="editingData.configName" :placeholder="$t('sys.config.pleaseEnterConfigName')" />
        </ElFormItem>
        <ElFormItem :label="$t('sys.config.configValue')" required>
          <ElInput
            v-model="editingData.configValue"
            type="textarea"
            :rows="2"
            :placeholder="$t('sys.config.pleaseEnterConfigValue')"
          />
        </ElFormItem>
        <ElFormItem :label="$t('sys.config.group')">
          <ElSelect
            v-model="editingData.configGroup"
            :placeholder="$t('common.pleaseSelect')"
            clearable
            filterable
            allow-create
          >
            <ElOption v-for="group in groupOptions" :key="group" :label="getGroupLabel(group)" :value="group" />
          </ElSelect>
        </ElFormItem>
        <ElFormItem :label="$t('sys.config.isPublic')">
          <ElSwitch v-model="editingData.isPublic" :active-value="1" :inactive-value="0" />
          <div class="mt-4px text-12px text-gray-400">{{ $t('sys.config.publicTip') }}</div>
        </ElFormItem>
        <ElFormItem :label="$t('common.status')">
          <ElRadioGroup v-model="editingData.status">
            <ElRadio :value="1">{{ $t('common.enable') }}</ElRadio>
            <ElRadio :value="0">{{ $t('common.disable') }}</ElRadio>
          </ElRadioGroup>
        </ElFormItem>
        <ElFormItem :label="$t('common.sort')">
          <ElInputNumber v-model="editingData.sortOrder" :min="0" :max="9999" />
        </ElFormItem>
        <ElFormItem :label="$t('common.remark')">
          <ElInput
            v-model="editingData.remark"
            type="textarea"
            :rows="3"
            :placeholder="$t('hr.contract.pleaseEnterRemark')"
          />
        </ElFormItem>
      </ElForm>
      <template #footer>
        <ElButton @click="drawerVisible = false">{{ $t('common.cancel') }}</ElButton>
        <ElButton type="primary" :loading="submitLoading" @click="handleSave">{{ $t('common.save') }}</ElButton>
      </template>
    </ElDrawer>
  </div>
</template>
