<script setup lang="tsx">
import { onMounted, ref } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { request } from '@/service/request';

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
    await ElMessageBox.confirm('确定删除该字典类型吗？删除后相关字典数据也会被删除', '提示', {
      type: 'warning'
    });

    const res = await request({
      url: `/system/dict/type/${id}`,
      method: 'delete'
    });

    console.log('删除字典类型响应:', res);

    if (!res.error) {
      ElMessage.success('删除成功');
      if (selectedDictType.value?.id === id) {
        selectedDictType.value = null;
        dictDataList.value = [];
      }
      await loadDictTypes();
    } else {
      ElMessage.error('删除失败');
    }
  } catch (e) {
    // 用户取消删除，不做任何处理
    console.log('删除取消或出错:', e);
  }
}

function handleAddData() {
  if (!selectedDictType.value) {
    ElMessage.warning('请先选择字典类型');
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
    await ElMessageBox.confirm('确定删除该字典数据吗？', '提示', {
      type: 'warning'
    });

    const res = await request({
      url: `/system/dict/data/${id}`,
      method: 'delete'
    });

    console.log('删除字典数据响应:', res);

    if (!res.error) {
      ElMessage.success('删除成功');
      await loadDictData(selectedDictType.value.id);
    } else {
      ElMessage.error('删除失败');
    }
  } catch (e) {
    // 用户取消删除，不做任何处理
    console.log('删除取消或出错:', e);
  }
}

async function handleSaveType() {
  if (!editingType.value.dictName || !editingType.value.dictCode) {
    ElMessage.warning('请填写必填项');
    return;
  }

  const isEdit = operateType.value === 'edit';
  const { error } = await request({
    url: '/system/dict/type',
    method: isEdit ? 'put' : 'post',
    data: editingType.value
  });

  if (!error) {
    ElMessage.success(isEdit ? '更新成功' : '新增成功');
    typeDrawerVisible.value = false;
    loadDictTypes();
  }
}

async function handleSaveData() {
  if (!editingData.value.dictLabel || !editingData.value.dictValue) {
    ElMessage.warning('请填写必填项');
    return;
  }

  const isEdit = operateType.value === 'edit';
  const { error } = await request({
    url: '/system/dict/data',
    method: isEdit ? 'put' : 'post',
    data: editingData.value
  });

  if (!error) {
    ElMessage.success(isEdit ? '更新成功' : '新增成功');
    dataDrawerVisible.value = false;
    loadDictData(selectedDictType.value.id);
  }
}

onMounted(() => {
  loadDictTypes();
});
</script>

<template>
  <div class="h-full flex gap-16px overflow-hidden lt-sm:overflow-auto lt-sm:flex-col">
    <!-- 左侧：字典类型列表 -->
    <ElCard class="w-400px lt-sm:w-full flex flex-col">
      <template #header>
        <div class="flex items-center justify-between">
          <span>字典类型</span>
          <ElButton v-permission="'system:dict:add'" type="primary" size="small" @click="handleAddType">
            <template #icon><icon-ep-plus /></template>
            新增
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
        <ElTableColumn prop="dictName" label="字典名称" min-width="100" />
        <ElTableColumn prop="dictCode" label="字典编码" width="130" />
        <ElTableColumn prop="status" label="状态" width="70" align="center">
          <template #default="{ row }">
            <ElTag :type="row.status === 1 ? 'success' : 'danger'" size="small">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </ElTag>
          </template>
        </ElTableColumn>
        <ElTableColumn label="操作" width="100" align="center">
          <template #default="{ row }">
            <ElButton v-permission="'system:dict:edit'" type="primary" link size="small" @click.stop="handleEditType(row)">编辑</ElButton>
            <ElButton v-permission="'system:dict:delete'" type="danger" link size="small" @click.stop="handleDeleteType(row.id)">删除</ElButton>
          </template>
        </ElTableColumn>
      </ElTable>
    </ElCard>

    <!-- 右侧：字典数据列表 -->
    <ElCard class="flex-1 dict-data-card">
      <template #header>
        <div class="flex items-center justify-between">
          <span>
            字典数据
            <template v-if="selectedDictType">
              - {{ selectedDictType.dictName }}（{{ selectedDictType.dictCode }}）
            </template>
          </span>
          <ElButton v-permission="'system:dict:add'" type="primary" size="small" @click="handleAddData">
            <template #icon><icon-ep-plus /></template>
            新增
          </ElButton>
        </div>
      </template>

      <div class="dict-data-content">
        <ElEmpty v-if="!selectedDictType" description="请选择左侧字典类型" />

        <ElTable v-else v-loading="loading" :data="dictDataList" border stripe class="dict-data-table">
          <ElTableColumn type="index" label="序号" width="60" align="center" />
          <ElTableColumn prop="dictLabel" label="标签" min-width="120" />
          <ElTableColumn prop="dictValue" label="值" width="100" />
          <ElTableColumn prop="sortOrder" label="排序" width="70" align="center" />
          <ElTableColumn prop="status" label="状态" width="80" align="center">
            <template #default="{ row }">
              <ElTag :type="row.status === 1 ? 'success' : 'danger'" size="small">
                {{ row.status === 1 ? '启用' : '禁用' }}
              </ElTag>
            </template>
          </ElTableColumn>
          <ElTableColumn label="操作" width="120" align="center">
            <template #default="{ row }">
              <ElButton v-permission="'system:dict:edit'" type="primary" link size="small" @click="handleEditData(row)">编辑</ElButton>
              <ElButton v-permission="'system:dict:delete'" type="danger" link size="small" @click="handleDeleteData(row.id)">删除</ElButton>
            </template>
          </ElTableColumn>
        </ElTable>
      </div>
    </ElCard>

    <!-- 字典类型编辑抽屉 -->
    <ElDrawer v-model="typeDrawerVisible" :title="operateType === 'add' ? '新增字典类型' : '编辑字典类型'" size="450px">
      <ElForm label-width="100px" :model="editingType">
        <ElFormItem label="字典名称" required>
          <ElInput v-model="editingType.dictName" placeholder="请输入字典名称（中文）" />
        </ElFormItem>
        <ElFormItem label="英文名称">
          <ElInput v-model="editingType.dictNameEn" placeholder="请输入字典名称（英文）" />
        </ElFormItem>
        <ElFormItem label="字典编码" required>
          <ElInput v-model="editingType.dictCode" placeholder="请输入字典编码" :disabled="operateType === 'edit'" />
        </ElFormItem>
        <ElFormItem label="状态">
          <ElRadioGroup v-model="editingType.status">
            <ElRadio :value="1">启用</ElRadio>
            <ElRadio :value="0">禁用</ElRadio>
          </ElRadioGroup>
        </ElFormItem>
        <ElFormItem label="描述">
          <ElInput v-model="editingType.description" type="textarea" :rows="2" placeholder="请输入描述" />
        </ElFormItem>
      </ElForm>
      <template #footer>
        <ElButton @click="typeDrawerVisible = false">取消</ElButton>
        <ElButton type="primary" @click="handleSaveType">保存</ElButton>
      </template>
    </ElDrawer>

    <!-- 字典数据编辑抽屉 -->
    <ElDrawer v-model="dataDrawerVisible" :title="operateType === 'add' ? '新增字典数据' : '编辑字典数据'" size="450px">
      <ElForm label-width="100px" :model="editingData">
        <ElFormItem label="标签（中文）" required>
          <ElInput v-model="editingData.dictLabel" placeholder="请输入标签（中文）" />
        </ElFormItem>
        <ElFormItem label="标签（英文）">
          <ElInput v-model="editingData.dictLabelEn" placeholder="请输入标签（英文）" />
        </ElFormItem>
        <ElFormItem label="值" required>
          <ElInput v-model="editingData.dictValue" placeholder="请输入值" />
        </ElFormItem>
        <ElFormItem label="排序">
          <ElInputNumber v-model="editingData.sortOrder" :min="0" :max="999" />
        </ElFormItem>
        <ElFormItem label="状态">
          <ElRadioGroup v-model="editingData.status">
            <ElRadio :value="1">启用</ElRadio>
            <ElRadio :value="0">禁用</ElRadio>
          </ElRadioGroup>
        </ElFormItem>
      </ElForm>
      <template #footer>
        <ElButton @click="dataDrawerVisible = false">取消</ElButton>
        <ElButton type="primary" @click="handleSaveData">保存</ElButton>
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
  background-color: #c0c4cc;
  border-radius: 4px;
}

.dict-data-content::-webkit-scrollbar-thumb:hover {
  background-color: #909399;
}

.dict-data-content::-webkit-scrollbar-track {
  background-color: #f5f7fa;
  border-radius: 4px;
}
</style>
