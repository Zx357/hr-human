<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { request } from '@/service/request';

defineOptions({ name: 'SystemNotice' });

interface NoticeRow {
  id: number;
  noticeTitle: string;
  noticeType: number;
  noticeContent: string;
  status: number;
  publishTime?: string;
  createdTime?: string;
}

const queryParams = reactive({
  current: 1,
  size: 10,
  noticeTitle: '',
  noticeType: undefined as number | undefined,
  status: undefined as number | undefined
});

const tableData = ref<NoticeRow[]>([]);
const total = ref(0);
const loading = ref(false);

const drawerVisible = ref(false);
const operateType = ref<'add' | 'edit'>('add');
const formData = reactive({
  id: undefined as number | undefined,
  noticeTitle: '',
  noticeType: 1,
  noticeContent: '',
  status: 1,
  publishTime: ''
});

async function fetchData() {
  loading.value = true;
  try {
    const { data, error } = await request<any>({
      url: '/system/notice/page',
      method: 'get',
      params: queryParams
    });

    if (!error && data) {
      tableData.value = data.records || [];
      total.value = data.total || 0;
    }
  } finally {
    loading.value = false;
  }
}

function handleSearch() {
  queryParams.current = 1;
  fetchData();
}

function handleReset() {
  queryParams.noticeTitle = '';
  queryParams.noticeType = undefined;
  queryParams.status = undefined;
  handleSearch();
}

function handlePageChange(page: number) {
  queryParams.current = page;
  fetchData();
}

function handleSizeChange(size: number) {
  queryParams.size = size;
  queryParams.current = 1;
  fetchData();
}

function handleAdd() {
  operateType.value = 'add';
  Object.assign(formData, {
    id: undefined,
    noticeTitle: '',
    noticeType: 1,
    noticeContent: '',
    status: 1,
    publishTime: ''
  });
  drawerVisible.value = true;
}

function handleEdit(row: NoticeRow) {
  operateType.value = 'edit';
  Object.assign(formData, {
    id: row.id,
    noticeTitle: row.noticeTitle,
    noticeType: row.noticeType,
    noticeContent: row.noticeContent,
    status: row.status,
    publishTime: row.publishTime || ''
  });
  drawerVisible.value = true;
}

async function handleSave() {
  if (!formData.noticeTitle) {
    ElMessage.warning('请填写公告标题');
    return;
  }

  const isEdit = operateType.value === 'edit';
  const { error } = await request({
    url: '/system/notice',
    method: isEdit ? 'put' : 'post',
    data: formData
  });

  if (!error) {
    ElMessage.success(isEdit ? '更新成功' : '新增成功');
    drawerVisible.value = false;
    fetchData();
  }
}

async function handleDelete(id: number) {
  try {
    await ElMessageBox.confirm('确定删除该公告吗？', '提示', { type: 'warning' });
    const { error } = await request({
      url: `/system/notice/${id}`,
      method: 'delete'
    });

    if (!error) {
      ElMessage.success('删除成功');
      fetchData();
    } else {
      ElMessage.error('删除失败');
    }
  } catch {
    // canceled
  }
}

onMounted(() => {
  fetchData();
});
</script>

<template>
  <div class="list-page">
    <ElCard class="search-card">
      <ElForm :model="queryParams" inline>
        <ElFormItem label="公告标题">
          <ElInput v-model="queryParams.noticeTitle" placeholder="请输入公告标题" clearable />
        </ElFormItem>
        <ElFormItem label="公告类型">
          <ElSelect v-model="queryParams.noticeType" placeholder="请选择类型" clearable style="width: 120px">
            <ElOption label="公告" :value="1" />
            <ElOption label="通知" :value="2" />
          </ElSelect>
        </ElFormItem>
        <ElFormItem label="状态">
          <ElSelect v-model="queryParams.status" placeholder="请选择状态" clearable style="width: 120px">
            <ElOption label="正常" :value="1" />
            <ElOption label="关闭" :value="0" />
          </ElSelect>
        </ElFormItem>
        <ElFormItem>
          <ElButton type="primary" @click="handleSearch">
            <template #icon><icon-ep-search /></template>
            搜索
          </ElButton>
          <ElButton @click="handleReset">
            <template #icon><icon-ep-refresh /></template>
            重置
          </ElButton>
        </ElFormItem>
      </ElForm>
    </ElCard>

    <ElCard class="table-card">
      <template #header>
        <div class="flex items-center justify-between">
          <span>公告列表</span>
          <ElButton v-permission="'system:notice:add'" type="primary" @click="handleAdd">
            <template #icon><icon-ep-plus /></template>
            新增
          </ElButton>
        </div>
      </template>

      <div class="table-wrapper">
        <ElTable v-loading="loading" :data="tableData" border stripe height="100%">
          <ElTableColumn prop="id" label="ID" width="80" />
          <ElTableColumn prop="noticeTitle" label="公告标题" min-width="220" show-overflow-tooltip />
          <ElTableColumn prop="noticeContent" label="公告内容" min-width="320" show-overflow-tooltip />
          <ElTableColumn prop="noticeType" label="公告类型" width="100" align="center">
            <template #default="{ row }">
              <ElTag v-if="row.noticeType === 1" type="warning">公告</ElTag>
              <ElTag v-else type="primary">通知</ElTag>
            </template>
          </ElTableColumn>
          <ElTableColumn prop="status" label="状态" width="80" align="center">
            <template #default="{ row }">
              <ElTag :type="row.status === 1 ? 'success' : 'danger'" size="small">
                {{ row.status === 1 ? '正常' : '关闭' }}
              </ElTag>
            </template>
          </ElTableColumn>
          <ElTableColumn prop="publishTime" label="发布日期" width="120" />
          <ElTableColumn prop="createdTime" label="创建时间" width="180" />
          <ElTableColumn label="操作" width="150" fixed="right" align="center">
            <template #default="{ row }">
              <ElButton v-permission="'system:notice:edit'" type="primary" link @click="handleEdit(row)">编辑</ElButton>
              <ElButton v-permission="'system:notice:delete'" type="danger" link @click="handleDelete(row.id)">
                删除
              </ElButton>
            </template>
          </ElTableColumn>
        </ElTable>
      </div>

      <div class="mt-16px flex justify-end">
        <ElPagination
          v-model:current-page="queryParams.current"
          v-model:page-size="queryParams.size"
          :total="total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @current-change="handlePageChange"
          @size-change="handleSizeChange"
        />
      </div>
    </ElCard>

    <ElDrawer v-model="drawerVisible" :title="operateType === 'add' ? '新增公告' : '编辑公告'" size="500px">
      <ElForm :model="formData" label-width="100px">
        <ElFormItem label="公告标题" required>
          <ElInput v-model="formData.noticeTitle" placeholder="请输入公告标题" />
        </ElFormItem>
        <ElFormItem label="公告类型">
          <ElRadioGroup v-model="formData.noticeType">
            <ElRadio :value="1">公告</ElRadio>
            <ElRadio :value="2">通知</ElRadio>
          </ElRadioGroup>
        </ElFormItem>
        <ElFormItem label="状态">
          <ElRadioGroup v-model="formData.status">
            <ElRadio :value="1">正常</ElRadio>
            <ElRadio :value="0">关闭</ElRadio>
          </ElRadioGroup>
        </ElFormItem>
        <ElFormItem label="发布日期">
          <ElDatePicker
            v-model="formData.publishTime"
            type="date"
            value-format="YYYY-MM-DD"
            placeholder="请选择发布日期"
            style="width: 100%"
          />
        </ElFormItem>
        <ElFormItem label="内容">
          <ElInput v-model="formData.noticeContent" type="textarea" :rows="8" placeholder="请输入公告内容" />
        </ElFormItem>
      </ElForm>
      <template #footer>
        <ElButton @click="drawerVisible = false">取消</ElButton>
        <ElButton type="primary" @click="handleSave">保存</ElButton>
      </template>
    </ElDrawer>
  </div>
</template>

<style scoped></style>
