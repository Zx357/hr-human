<script setup lang="tsx">
import { onMounted, ref } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { type FileConfig, fetchFileConfigList, refreshFileConfig, updateFileConfig } from '@/service/api/file-config';
import { $t } from '@/locales';

defineOptions({ name: 'FileConfigManage' });

const loading = ref(false);
const configList = ref<FileConfig[]>([]);
const drawerVisible = ref(false);
const editingData = ref<Partial<FileConfig>>({});
const submitLoading = ref(false);

const configKeyMap: Record<string, { icon: string; color: string }> = {
  upload_base_path: { icon: 'ep-folder', color: '#409eff' },
  employee_avatar_path: { icon: 'ep-avatar', color: '#67c23a' },
  id_card_front_path: { icon: 'ep-postcard', color: '#e6a23c' },
  id_card_back_path: { icon: 'ep-postcard', color: '#f56c6c' }
};

async function loadData() {
  loading.value = true;
  try {
    const { data, error } = await fetchFileConfigList();
    if (!error && data) {
      configList.value = data;
    }
  } finally {
    loading.value = false;
  }
}

function handleEdit(row: FileConfig) {
  editingData.value = { ...row };
  drawerVisible.value = true;
}

async function handleSave() {
  if (!editingData.value.configValue?.trim()) {
    ElMessage.warning($t('sys.fileConfig.pathCannotBeEmpty'));
    return;
  }
  submitLoading.value = true;
  try {
    const { error } = await updateFileConfig(editingData.value);
    if (!error) {
      ElMessage.success($t('common.updateSuccess'));
      drawerVisible.value = false;
      loadData();
    }
  } finally {
    submitLoading.value = false;
  }
}

async function handleRefresh() {
  try {
    await ElMessageBox.confirm(
      $t('sys.fileConfig.afterRefreshAllPathConfigCachesWillBeReloadedAndUploadsWillUseTheNewPathImmediatelyStaticResourceMappingTakesEffectAfterRestartingTheBackendService'),
      $t('sys.fileConfig.refreshConfirmation'),
      { type: 'info', confirmButtonText: $t('sys.fileConfig.confirmRefresh'), cancelButtonText: $t('common.cancel') }
    );
    const { error } = await refreshFileConfig();
    if (!error) {
      ElMessage.success($t('sys.fileConfig.cacheRefreshedSuccessfully'));
      loadData();
    }
  } catch {
    // 取消
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
          <span>{{ $t('sys.fileConfig.filePathConfig') }}</span>
          <div class="flex gap-8px">
            <ElButton v-permission="'system:file-config:edit'" type="warning" size="small" @click="handleRefresh">
              <template #icon><icon-ep-refresh /></template>
              {{ $t('sys.fileConfig.refreshCache') }}
            </ElButton>
          </div>
        </div>
      </template>

      <ElAlert
        :title="$t('sys.fileConfig.afterChangingThePathConfigNewFilesAreSavedToTheNewPathIfStaticResourceMappingRelatedPathsChangedTheBackendServiceMustBeRestartedForExistingFilesToBeAccessibleUnderTheNewPath')"
        type="info"
        show-icon
        :closable="false"
        class="mb-16px"
      />

      <ElTable v-loading="loading" :data="configList" border stripe>
        <ElTableColumn type="index" :label="$t('common.index2')" width="60" align="center" />
        <ElTableColumn prop="configName" :label="$t('sys.fileConfig.configName')" width="160">
          <template #default="{ row }">
            <div class="flex items-center gap-6px">
              <ElIcon :color="configKeyMap[row.configKey]?.color || '#409eff'" :size="18">
                <component :is="'icon-' + (configKeyMap[row.configKey]?.icon || 'ep-folder')" />
              </ElIcon>
              <span class="font-bold">{{ row.configName }}</span>
            </div>
          </template>
        </ElTableColumn>
        <ElTableColumn prop="configKey" :label="$t('sys.fileConfig.configKey')" width="200">
          <template #default="{ row }">
            <ElTag size="small" type="info">{{ row.configKey }}</ElTag>
          </template>
        </ElTableColumn>
        <ElTableColumn prop="configValue" :label="$t('common.path')" min-width="300">
          <template #default="{ row }">
            <div class="flex items-center gap-4px">
              <ElIcon :size="14" class="text-[var(--el-text-color-placeholder)]"><icon-ep-folder-opened /></ElIcon>
              <code class="rounded bg-[var(--el-fill-color)] px-8px py-2px text-13px">{{ row.configValue }}</code>
            </div>
          </template>
        </ElTableColumn>
        <ElTableColumn prop="remark" :label="$t('common.remark')" min-width="200" show-overflow-tooltip />
        <ElTableColumn prop="updatedTime" :label="$t('common.updateTime')" width="170" />
        <ElTableColumn :label="$t('common.action')" width="80" align="center" fixed="right">
          <template #default="{ row }">
            <ElButton v-permission="'system:file-config:edit'" type="primary" link size="small" @click="handleEdit(row)">{{ $t('common.edit') }}</ElButton>
          </template>
        </ElTableColumn>
      </ElTable>
    </ElCard>

    <ElDrawer v-model="drawerVisible" :title="$t('sys.fileConfig.editPathConfig')" size="500px">
      <ElForm label-width="100px" :model="editingData">
        <ElFormItem :label="$t('sys.fileConfig.configName')">
          <ElInput v-model="editingData.configName" :placeholder="$t('sys.fileConfig.pleaseEnterConfigName')" />
        </ElFormItem>
        <ElFormItem :label="$t('sys.fileConfig.configKey')">
          <ElInput v-model="editingData.configKey" disabled />
        </ElFormItem>
        <ElFormItem :label="$t('common.path')" required>
          <ElInput v-model="editingData.configValue" :placeholder="$t('sys.fileConfig.pleaseEnterFileStoragePath')">
            <template #prepend>
              <ElIcon><icon-ep-folder-opened /></ElIcon>
            </template>
          </ElInput>
          <div class="mt-4px text-12px text-gray-400">
            {{ $t('sys.fileConfig.absolutePathsEGDRzphotoEmployeePhotoOrRelativePathsEGUploadsAreSupported') }}
          </div>
        </ElFormItem>
        <ElFormItem :label="$t('common.remark')">
          <ElInput v-model="editingData.remark" type="textarea" :rows="3" :placeholder="$t('hr.contract.pleaseEnterRemark')" />
        </ElFormItem>
      </ElForm>
      <template #footer>
        <ElButton @click="drawerVisible = false">{{ $t('common.cancel') }}</ElButton>
        <ElButton type="primary" :loading="submitLoading" @click="handleSave">{{ $t('common.save') }}</ElButton>
      </template>
    </ElDrawer>
  </div>
</template>
