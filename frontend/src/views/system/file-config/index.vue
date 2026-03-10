<script setup lang="tsx">
import { onMounted, ref } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { fetchFileConfigList, updateFileConfig, refreshFileConfig, type FileConfig } from '@/service/api/file-config';

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
    ElMessage.warning('路径不能为空');
    return;
  }
  submitLoading.value = true;
  try {
    const { error } = await updateFileConfig(editingData.value);
    if (!error) {
      ElMessage.success('更新成功');
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
      '刷新后将重新加载所有路径配置的缓存，上传将立即使用新路径。静态资源映射需要重启后端服务才能生效。',
      '刷新确认',
      { type: 'info', confirmButtonText: '确认刷新', cancelButtonText: '取消' }
    );
    const { error } = await refreshFileConfig();
    if (!error) {
      ElMessage.success('缓存刷新成功');
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
          <span>文件路径配置</span>
          <div class="flex gap-8px">
            <ElButton type="warning" size="small" @click="handleRefresh">
              <template #icon><icon-ep-refresh /></template>
              刷新缓存
            </ElButton>
          </div>
        </div>
      </template>

      <ElAlert
        title="修改路径配置后，新上传的文件将保存到新路径。如果修改了静态资源映射相关路径，需要重启后端服务才能使已有文件在新路径下正常访问。"
        type="info"
        show-icon
        :closable="false"
        class="mb-16px"
      />

      <ElTable v-loading="loading" :data="configList" border stripe>
        <ElTableColumn type="index" label="序号" width="60" align="center" />
        <ElTableColumn prop="configName" label="配置名称" width="160">
          <template #default="{ row }">
            <div class="flex items-center gap-6px">
              <ElIcon :color="configKeyMap[row.configKey]?.color || '#409eff'" :size="18">
                <component :is="'icon-' + (configKeyMap[row.configKey]?.icon || 'ep-folder')" />
              </ElIcon>
              <span class="font-bold">{{ row.configName }}</span>
            </div>
          </template>
        </ElTableColumn>
        <ElTableColumn prop="configKey" label="配置键" width="200">
          <template #default="{ row }">
            <ElTag size="small" type="info">{{ row.configKey }}</ElTag>
          </template>
        </ElTableColumn>
        <ElTableColumn prop="configValue" label="路径" min-width="300">
          <template #default="{ row }">
            <div class="flex items-center gap-4px">
              <ElIcon :size="14" class="text-gray-400"><icon-ep-folder-opened /></ElIcon>
              <code class="text-13px bg-gray-50 px-8px py-2px rounded">{{ row.configValue }}</code>
            </div>
          </template>
        </ElTableColumn>
        <ElTableColumn prop="remark" label="备注" min-width="200" show-overflow-tooltip />
        <ElTableColumn prop="updatedTime" label="更新时间" width="170" />
        <ElTableColumn label="操作" width="80" align="center" fixed="right">
          <template #default="{ row }">
            <ElButton type="primary" link size="small" @click="handleEdit(row)">编辑</ElButton>
          </template>
        </ElTableColumn>
      </ElTable>
    </ElCard>

    <ElDrawer v-model="drawerVisible" title="编辑路径配置" size="500px">
      <ElForm label-width="100px" :model="editingData">
        <ElFormItem label="配置名称">
          <ElInput v-model="editingData.configName" placeholder="请输入配置名称" />
        </ElFormItem>
        <ElFormItem label="配置键">
          <ElInput v-model="editingData.configKey" disabled />
        </ElFormItem>
        <ElFormItem label="路径" required>
          <ElInput v-model="editingData.configValue" placeholder="请输入文件存储路径">
            <template #prepend>
              <ElIcon><icon-ep-folder-opened /></ElIcon>
            </template>
          </ElInput>
          <div class="text-12px text-gray-400 mt-4px">
            支持绝对路径（如 D:/rzphoto/employee_photo）或相对路径（如 ./uploads）
          </div>
        </ElFormItem>
        <ElFormItem label="备注">
          <ElInput v-model="editingData.remark" type="textarea" :rows="3" placeholder="请输入备注" />
        </ElFormItem>
      </ElForm>
      <template #footer>
        <ElButton @click="drawerVisible = false">取消</ElButton>
        <ElButton type="primary" :loading="submitLoading" @click="handleSave">保存</ElButton>
      </template>
    </ElDrawer>
  </div>
</template>
