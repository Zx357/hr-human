<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue';
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus';
import { type SysNoticeItem, deleteNotice, fetchNoticePage, saveNotice } from '@/service/api/system';
import { formatDateTime } from '@/utils/format';
import { $t } from '@/locales';

defineOptions({ name: 'SystemNotice' });

type NoticeRow = SysNoticeItem;

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

const formRef = ref<FormInstance>();
const saving = ref(false);
const formRules: FormRules = {
  noticeTitle: [{ required: true, message: $t('sys.notice.pleaseEnterNoticeTitle2'), trigger: 'blur' }],
  publishTime: [{ required: true, message: $t('sys.notice.pleaseSelectPublishDate'), trigger: 'change' }]
};

async function fetchData() {
  loading.value = true;
  try {
    const res = await fetchNoticePage({ ...queryParams });

    const data = res.data;
    if (data) {
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
  const valid = await formRef.value?.validate().catch(() => false);
  if (!valid) return;
  const isEdit = operateType.value === 'edit';
  saving.value = true;
  try {
    const res = await saveNotice({ ...formData });
    if (!res.error) {
      ElMessage.success(isEdit ? $t('common.updateSuccess') : $t('common.addSuccess'));
      drawerVisible.value = false;
      fetchData();
    }
  } finally {
    saving.value = false;
  }
}

async function handleDelete(id: number) {
  try {
    await ElMessageBox.confirm($t('sys.notice.areYouSureYouWantToDeleteThisNotice'), $t('common.tip'), { type: 'warning' });
    const res = await deleteNotice(id);
    if (!res.error) {
      ElMessage.success($t('common.deleteSuccess'));
      fetchData();
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
        <ElFormItem :label="$t('sys.notice.noticeTitle')">
          <ElInput
            v-model="queryParams.noticeTitle"
            :placeholder="$t('sys.notice.pleaseEnterNoticeTitle')"
            clearable
            @keyup.enter="handleSearch"
          />
        </ElFormItem>
        <ElFormItem :label="$t('sys.notice.noticeType')">
          <ElSelect v-model="queryParams.noticeType" :placeholder="$t('common.pleaseSelectType')" clearable style="width: 120px">
            <ElOption :label="$t('sys.notice.notice')" :value="1" />
            <ElOption :label="$t('sys.notice.notification')" :value="2" />
          </ElSelect>
        </ElFormItem>
        <ElFormItem :label="$t('common.status')">
          <ElSelect v-model="queryParams.status" :placeholder="$t('common.pleaseSelectStatus')" clearable style="width: 120px">
            <ElOption :label="$t('common.normal')" :value="1" />
            <ElOption :label="$t('common.close')" :value="0" />
          </ElSelect>
        </ElFormItem>
        <ElFormItem>
          <ElButton type="primary" @click="handleSearch">
            <template #icon><icon-ep-search /></template>
            {{ $t('common.search') }}
          </ElButton>
          <ElButton @click="handleReset">
            <template #icon><icon-ep-refresh /></template>
            {{ $t('common.reset') }}
          </ElButton>
        </ElFormItem>
      </ElForm>
    </ElCard>

    <ElCard class="table-card">
      <template #header>
        <div class="flex items-center justify-between">
          <span>{{ $t('sys.notice.noticeList') }}</span>
          <ElButton v-permission="'system:notice:add'" type="primary" @click="handleAdd">
            <template #icon><icon-ep-plus /></template>
            {{ $t('common.add') }}
          </ElButton>
        </div>
      </template>

      <div class="table-wrapper">
        <ElTable v-loading="loading" :data="tableData" border stripe height="100%">
          <ElTableColumn prop="id" label="ID" width="80" />
          <ElTableColumn prop="noticeTitle" :label="$t('sys.notice.noticeTitle')" min-width="220" show-overflow-tooltip />
          <ElTableColumn prop="noticeContent" :label="$t('sys.notice.noticeContent')" min-width="320" show-overflow-tooltip />
          <ElTableColumn prop="noticeType" :label="$t('sys.notice.noticeType')" width="100" align="center">
            <template #default="{ row }">
              <ElTag v-if="row.noticeType === 1" type="warning">{{ $t('sys.notice.notice') }}</ElTag>
              <ElTag v-else type="primary">{{ $t('sys.notice.notification') }}</ElTag>
            </template>
          </ElTableColumn>
          <ElTableColumn prop="status" :label="$t('common.status')" width="80" align="center">
            <template #default="{ row }">
              <ElTag :type="row.status === 1 ? 'success' : 'danger'" size="small">
                {{ row.status === 1 ? $t('common.normal') : $t('common.close') }}
              </ElTag>
            </template>
          </ElTableColumn>
          <ElTableColumn prop="publishTime" :label="$t('sys.notice.publishDate')" width="120" />
          <ElTableColumn prop="createdTime" :label="$t('common.createTime')" width="180">
            <template #default="{ row }">{{ formatDateTime(row.createdTime) }}</template>
          </ElTableColumn>
          <ElTableColumn :label="$t('common.action')" width="150" fixed="right" align="center">
            <template #default="{ row }">
              <ElButton v-permission="'system:notice:edit'" type="primary" link @click="handleEdit(row)">{{ $t('common.edit') }}</ElButton>
              <ElButton v-permission="'system:notice:delete'" type="danger" link @click="handleDelete(row.id)">
                {{ $t('common.delete') }}
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

    <ElDrawer v-model="drawerVisible" :title="operateType === 'add' ? $t('sys.notice.newNotice') : $t('sys.notice.editNotice')" size="500px">
      <ElForm ref="formRef" :model="formData" :rules="formRules" label-width="100px">
        <ElFormItem :label="$t('sys.notice.noticeTitle')" prop="noticeTitle">
          <ElInput v-model="formData.noticeTitle" :placeholder="$t('sys.notice.pleaseEnterNoticeTitle')" />
        </ElFormItem>
        <ElFormItem :label="$t('sys.notice.noticeType')">
          <ElRadioGroup v-model="formData.noticeType">
            <ElRadio :value="1">{{ $t('sys.notice.notice') }}</ElRadio>
            <ElRadio :value="2">{{ $t('sys.notice.notification') }}</ElRadio>
          </ElRadioGroup>
        </ElFormItem>
        <ElFormItem :label="$t('common.status')">
          <ElRadioGroup v-model="formData.status">
            <ElRadio :value="1">{{ $t('common.normal') }}</ElRadio>
            <ElRadio :value="0">{{ $t('common.close') }}</ElRadio>
          </ElRadioGroup>
        </ElFormItem>
        <ElFormItem :label="$t('sys.notice.publishDate')" prop="publishTime">
          <ElDatePicker
            v-model="formData.publishTime"
            type="date"
            value-format="YYYY-MM-DD"
            :placeholder="$t('sys.notice.pleaseSelectPublishDate')"
            style="width: 100%"
          />
        </ElFormItem>
        <ElFormItem :label="$t('common.content')">
          <ElInput v-model="formData.noticeContent" type="textarea" :rows="8" :placeholder="$t('sys.notice.pleaseEnterNoticeContent')" />
        </ElFormItem>
      </ElForm>
      <template #footer>
        <ElButton @click="drawerVisible = false">{{ $t('common.cancel') }}</ElButton>
        <ElButton type="primary" :loading="saving" @click="handleSave">{{ $t('common.save') }}</ElButton>
      </template>
    </ElDrawer>
  </div>
</template>

<style scoped></style>
