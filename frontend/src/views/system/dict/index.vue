<script setup lang="tsx">
import { onMounted, ref } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { request } from '@/service/request';
import { clearDictCache } from '@/composables/use-dict-options';
import { $t } from '@/locales';

defineOptions({ name: 'DictManage' });

// 字典类型数据
const loading = ref(false);
const dictTypes = ref<any[]>([]);

// 当前选中的字典类型
const selectedDictType = ref<any>(null);

// 字典数据
const dictDataList = ref<any[]>([]);

const typeDrawerVisible = ref(false);
const dataDrawerVisible = ref(false);
const operateType = ref<'add' | 'edit'>('add');
const saveTypeLoading = ref(false);
const saveDataLoading = ref(false);
const editingType = ref<any>({});
const editingData = ref<any>({});

// 加载字典类型列表
async function loadDictTypes() {
  loading.value = true;
  try {
    const { data, error } = await request<any[]>({
      url: '/system/dict/type/list',
      method: 'get'
    });
    if (!error && data) {
      dictTypes.value = data;
    }
  } finally {
    loading.value = false;
  }
}

// 加载字典数据列表
async function loadDictData(dictTypeId: number) {
  loading.value = true;
  try {
    const { data, error } = await request<any[]>({
      url: '/system/dict/data/list',
      method: 'get',
      params: { dictTypeId }
    });
    if (!error && data) {
      dictDataList.value = data;
    }
  } finally {
    loading.value = false;
  }
}

function handleSelectDictType(row: any) {
  selectedDictType.value = row;
  loadDictData(row.id);
}

function handleAddType() {
  operateType.value = 'add';
  editingType.value = {
    dictName: '',
    dictNameEn: '',
    dictCode: '',
    status: 1,
    description: ''
  };
  typeDrawerVisible.value = true;
}

function handleEditType(row: any) {
  operateType.value = 'edit';
  editingType.value = { ...row };
  typeDrawerVisible.value = true;
}

async function handleDeleteType(id: number) {
  try {
    await ElMessageBox.confirm($t('sys.dict.areYouSureYouWantToDeleteThisDictionaryTypeRelatedDictionaryDataWillAlsoBeDeleted'), $t('common.tip'), {
      type: 'warning'
    });

    const res = await request({
      url: `/system/dict/type/${id}`,
      method: 'delete'
    });

    if (!res.error) {
      ElMessage.success($t('common.deleteSuccess'));
      if (selectedDictType.value?.id === id) {
        selectedDictType.value = null;
        dictDataList.value = [];
      }
      // 清空字典缓存，全站标签按需重新拉取
      clearDictCache();
      await loadDictTypes();
    }
  } catch {
    // 用户取消删除，不做任何处理
  }
}

function handleAddData() {
  if (!selectedDictType.value) {
    ElMessage.warning($t('sys.dict.pleaseSelectADictionaryTypeFirst'));
    return;
  }
  operateType.value = 'add';
  editingData.value = {
    dictTypeId: selectedDictType.value.id,
    dictLabel: '',
    dictLabelEn: '',
    dictValue: '',
    sortOrder: 0,
    status: 1
  };
  dataDrawerVisible.value = true;
}

function handleEditData(row: any) {
  operateType.value = 'edit';
  editingData.value = { ...row };
  dataDrawerVisible.value = true;
}

async function handleDeleteData(id: number) {
  try {
    await ElMessageBox.confirm($t('sys.dict.areYouSureYouWantToDeleteThisDictionaryData'), $t('common.tip'), {
      type: 'warning'
    });

    const res = await request({
      url: `/system/dict/data/${id}`,
      method: 'delete'
    });

    if (!res.error) {
      ElMessage.success($t('common.deleteSuccess'));
      // 清除该类型缓存，使全站字典标签立即刷新
      clearDictCache(selectedDictType.value?.dictCode);
      await loadDictData(selectedDictType.value.id);
    }
  } catch {
    // 用户取消删除，不做任何处理
  }
}

async function handleSaveType() {
  if (!editingType.value.dictName || !editingType.value.dictCode) {
    ElMessage.warning($t('common.pleaseFillRequired'));
    return;
  }

  saveTypeLoading.value = true;
  try {
    const isEdit = operateType.value === 'edit';
    const { error } = await request({
      url: '/system/dict/type',
      method: isEdit ? 'put' : 'post',
      data: editingType.value
    });

    if (!error) {
      ElMessage.success(isEdit ? $t('common.updateSuccess') : $t('common.addSuccess'));
      typeDrawerVisible.value = false;
      // 字典类型变化影响全站缓存，清空后各页面按需重新拉取
      clearDictCache();
      loadDictTypes();
    }
  } finally {
    saveTypeLoading.value = false;
  }
}

async function handleSaveData() {
  if (!editingData.value.dictLabel || !editingData.value.dictValue) {
    ElMessage.warning($t('common.pleaseFillRequired'));
    return;
  }

  saveDataLoading.value = true;
  try {
    const isEdit = operateType.value === 'edit';
    const { error } = await request({
      url: '/system/dict/data',
      method: isEdit ? 'put' : 'post',
      data: editingData.value
    });

    if (!error) {
      ElMessage.success(isEdit ? $t('common.updateSuccess') : $t('common.addSuccess'));
      dataDrawerVisible.value = false;
      // 清除该类型缓存，使全站字典标签立即刷新
      clearDictCache(selectedDictType.value?.dictCode);
      loadDictData(selectedDictType.value.id);
    }
  } finally {
    saveDataLoading.value = false;
  }
}

onMounted(() => {
  loadDictTypes();
});
</script>

<template>
  <div class="h-full flex gap-16px overflow-hidden lt-sm:flex-col lt-sm:overflow-auto">
    <!-- 左侧：字典类型列表 -->
    <ElCard class="w-400px flex flex-col lt-sm:w-full">
      <template #header>
        <div class="flex items-center justify-between">
          <span>{{ $t('sys.dict.dictionaryType') }}</span>
          <ElButton v-permission="'system:dict:add'" type="primary" size="small" @click="handleAddType">
            <template #icon><icon-ep-plus /></template>
            {{ $t('common.add') }}
          </ElButton>
        </div>
      </template>

      <ElTable
        v-loading="loading"
        :data="dictTypes"
        border
        stripe
        highlight-current-row
        @row-click="handleSelectDictType"
      >
        <ElTableColumn prop="dictName" :label="$t('sys.dict.dictionaryName')" min-width="100" />
        <ElTableColumn prop="dictCode" :label="$t('sys.dict.dictionaryCode')" width="130" />
        <ElTableColumn prop="status" :label="$t('common.status')" width="70" align="center">
          <template #default="{ row }">
            <ElTag :type="row.status === 1 ? 'success' : 'danger'" size="small">
              {{ row.status === 1 ? $t('common.enable') : $t('common.disable') }}
            </ElTag>
          </template>
        </ElTableColumn>
        <ElTableColumn :label="$t('common.action')" width="100" align="center">
          <template #default="{ row }">
            <ElButton
              v-permission="'system:dict:edit'"
              type="primary"
              link
              size="small"
              @click.stop="handleEditType(row)"
            >
              {{ $t('common.edit') }}
            </ElButton>
            <ElButton
              v-permission="'system:dict:delete'"
              type="danger"
              link
              size="small"
              @click.stop="handleDeleteType(row.id)"
            >
              {{ $t('common.delete') }}
            </ElButton>
          </template>
        </ElTableColumn>
      </ElTable>
    </ElCard>

    <!-- 右侧：字典数据列表 -->
    <ElCard class="dict-data-card flex-1">
      <template #header>
        <div class="flex items-center justify-between">
          <span>
            {{ $t('common.dictionaryData') }}
            <template v-if="selectedDictType">
              - {{ selectedDictType.dictName }}（{{ selectedDictType.dictCode }}）
            </template>
          </span>
          <ElButton v-permission="'system:dict:add'" type="primary" size="small" @click="handleAddData">
            <template #icon><icon-ep-plus /></template>
            {{ $t('common.add') }}
          </ElButton>
        </div>
      </template>

      <div class="dict-data-content">
        <ElEmpty v-if="!selectedDictType" :description="$t('sys.dict.pleaseSelectADictionaryTypeOnTheLeft')" />

        <ElTable v-else v-loading="loading" :data="dictDataList" border stripe class="dict-data-table">
          <ElTableColumn type="index" :label="$t('common.index2')" width="60" align="center" />
          <ElTableColumn prop="dictLabel" :label="$t('sys.dict.label')" min-width="120" />
          <ElTableColumn prop="dictValue" :label="$t('sys.dict.value')" width="100" />
          <ElTableColumn prop="sortOrder" :label="$t('common.sort')" width="70" align="center" />
          <ElTableColumn prop="status" :label="$t('common.status')" width="80" align="center">
            <template #default="{ row }">
              <ElTag :type="row.status === 1 ? 'success' : 'danger'" size="small">
                {{ row.status === 1 ? $t('common.enable') : $t('common.disable') }}
              </ElTag>
            </template>
          </ElTableColumn>
          <ElTableColumn :label="$t('common.action')" width="120" align="center">
            <template #default="{ row }">
              <ElButton v-permission="'system:dict:edit'" type="primary" link size="small" @click="handleEditData(row)">
                {{ $t('common.edit') }}
              </ElButton>
              <ElButton
                v-permission="'system:dict:delete'"
                type="danger"
                link
                size="small"
                @click="handleDeleteData(row.id)"
              >
                {{ $t('common.delete') }}
              </ElButton>
            </template>
          </ElTableColumn>
        </ElTable>
      </div>
    </ElCard>

    <!-- 字典类型编辑抽屉 -->
    <ElDrawer v-model="typeDrawerVisible" :title="operateType === 'add' ? $t('sys.dict.newDictionaryType') : $t('sys.dict.editDictionaryType')" size="450px">
      <ElForm label-width="100px" :model="editingType">
        <ElFormItem :label="$t('sys.dict.dictionaryName')" required>
          <ElInput v-model="editingType.dictName" :placeholder="$t('sys.dict.pleaseEnterDictionaryNameChinese')" />
        </ElFormItem>
        <ElFormItem :label="$t('sys.dict.englishName')">
          <ElInput v-model="editingType.dictNameEn" :placeholder="$t('sys.dict.pleaseEnterDictionaryNameEnglish')" />
        </ElFormItem>
        <ElFormItem :label="$t('sys.dict.dictionaryCode')" required>
          <ElInput v-model="editingType.dictCode" :placeholder="$t('sys.dict.pleaseEnterDictionaryCode')" :disabled="operateType === 'edit'" />
        </ElFormItem>
        <ElFormItem :label="$t('common.status')">
          <ElRadioGroup v-model="editingType.status">
            <ElRadio :value="1">{{ $t('common.enable') }}</ElRadio>
            <ElRadio :value="0">{{ $t('common.disable') }}</ElRadio>
          </ElRadioGroup>
        </ElFormItem>
        <ElFormItem :label="$t('common.description')">
          <ElInput v-model="editingType.description" type="textarea" :rows="2" :placeholder="$t('approval.flow.pleaseEnterDescription')" />
        </ElFormItem>
      </ElForm>
      <template #footer>
        <ElButton @click="typeDrawerVisible = false">{{ $t('common.cancel') }}</ElButton>
        <ElButton type="primary" :loading="saveTypeLoading" @click="handleSaveType">{{ $t('common.save') }}</ElButton>
      </template>
    </ElDrawer>

    <!-- 字典数据编辑抽屉 -->
    <ElDrawer v-model="dataDrawerVisible" :title="operateType === 'add' ? $t('sys.dict.newDictionaryData') : $t('sys.dict.editDictionaryData')" size="450px">
      <ElForm label-width="100px" :model="editingData">
        <ElFormItem :label="$t('sys.dict.labelChinese')" required>
          <ElInput v-model="editingData.dictLabel" :placeholder="$t('sys.dict.pleaseEnterLabelChinese')" />
        </ElFormItem>
        <ElFormItem :label="$t('sys.dict.labelEnglish')">
          <ElInput v-model="editingData.dictLabelEn" :placeholder="$t('sys.dict.pleaseEnterLabelEnglish')" />
        </ElFormItem>
        <ElFormItem :label="$t('sys.dict.value')" required>
          <ElInput v-model="editingData.dictValue" :placeholder="$t('sys.dict.pleaseEnterValue')" />
        </ElFormItem>
        <ElFormItem :label="$t('common.sort')">
          <ElInputNumber v-model="editingData.sortOrder" :min="0" :max="999" />
        </ElFormItem>
        <ElFormItem :label="$t('common.status')">
          <ElRadioGroup v-model="editingData.status">
            <ElRadio :value="1">{{ $t('common.enable') }}</ElRadio>
            <ElRadio :value="0">{{ $t('common.disable') }}</ElRadio>
          </ElRadioGroup>
        </ElFormItem>
      </ElForm>
      <template #footer>
        <ElButton @click="dataDrawerVisible = false">{{ $t('common.cancel') }}</ElButton>
        <ElButton type="primary" :loading="saveDataLoading" @click="handleSaveData">{{ $t('common.save') }}</ElButton>
      </template>
    </ElDrawer>
  </div>
</template>

<style scoped>
.el-card {
  display: flex;
  flex-direction: column;
}

.el-card :deep(.el-card__body) {
  flex: 1;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

/* 右侧字典数据卡片 */
.dict-data-card {
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.dict-data-card :deep(.el-card__body) {
  flex: 1;
  overflow: hidden;
  padding: 0;
}

.dict-data-content {
  height: 100%;
  overflow-y: auto;
  padding: 20px;
}

.dict-data-table {
  width: 100%;
}

/* 自定义滚动条样式 */
.dict-data-content::-webkit-scrollbar {
  width: 8px;
  height: 8px;
}

.dict-data-content::-webkit-scrollbar-thumb {
  background-color: var(--el-border-color-darker);
  border-radius: 4px;
}

.dict-data-content::-webkit-scrollbar-thumb:hover {
  background-color: var(--el-text-color-placeholder);
}

.dict-data-content::-webkit-scrollbar-track {
  background-color: var(--el-fill-color-lighter);
  border-radius: 4px;
}
</style>
