<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { cleanOperLogs, fetchOperLogPage } from '@/service/api';
import { $t } from '@/locales';

defineOptions({ name: 'SystemOperLog' });

const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  module: '',
  username: '',
  status: undefined as number | undefined,
  beginTime: undefined as string | undefined,
  endTime: undefined as string | undefined
});

/** 操作时间范围（[beginTime, endTime]，yyyy-MM-dd） */
const dateRange = ref<[string, string] | null>(null);

const tableData = ref<Api.System.OperLogRecord[]>([]);
const total = ref(0);
const loading = ref(false);

const drawerVisible = ref(false);
const currentRow = ref<Api.System.OperLogRecord | null>(null);

/** 请求参数美化展示：JSON 串格式化，其余原样展示 */
function formatRequestParams(params?: string) {
  if (!params) return '';
  try {
    return JSON.stringify(JSON.parse(params), null, 2);
  } catch {
    return params;
  }
}

async function fetchData() {
  loading.value = true;
  try {
    // 时间范围同步到查询参数（后端按操作时间过滤，参数名 beginTime/endTime）
    queryParams.beginTime = dateRange.value?.[0];
    queryParams.endTime = dateRange.value?.[1];
    const { data, error } = await fetchOperLogPage(queryParams);

    if (!error && data) {
      tableData.value = data.records || [];
      total.value = data.total || 0;
    }
  } finally {
    loading.value = false;
  }
}

function handleSearch() {
  queryParams.pageNum = 1;
  fetchData();
}

function handleReset() {
  queryParams.module = '';
  queryParams.username = '';
  queryParams.status = undefined;
  dateRange.value = null;
  handleSearch();
}

function handlePageChange(page: number) {
  queryParams.pageNum = page;
  fetchData();
}

function handleSizeChange(size: number) {
  queryParams.pageSize = size;
  queryParams.pageNum = 1;
  fetchData();
}

function handleDetail(row: Api.System.OperLogRecord) {
  currentRow.value = row;
  drawerVisible.value = true;
}

async function handleClean() {
  try {
    const { value } = await ElMessageBox.prompt($t('sys.operLog.allOperationLogsBeforeThisNumberOfDaysWillBeDeleted'), $t('sys.operLog.cleanLogs'), {
      confirmButtonText: $t('common.ok'),
      cancelButtonText: $t('common.cancel'),
      inputValue: '90',
      inputPattern: /^\d+$/,
      inputErrorMessage: $t('sys.operLog.pleaseEnterANumber'),
      inputValidator: (input: string) => {
        const days = Number(input);
        if (!Number.isFinite(days) || days < 7) {
          return $t('sys.operLog.retentionDaysCannotBeLessThan7');
        }
        return true;
      }
    });

    const days = Number(value);
    const { error } = await cleanOperLogs(days);

    if (!error) {
      ElMessage.success($t('sys.operLog.cleanedSuccessfully'));
      handleSearch();
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
        <ElFormItem :label="$t('sys.operLog.module')">
          <ElInput v-model="queryParams.module" :placeholder="$t('sys.operLog.pleaseEnterModule')" clearable @keyup.enter="handleSearch" />
        </ElFormItem>
        <ElFormItem :label="$t('sys.operLog.operator')">
          <ElInput v-model="queryParams.username" :placeholder="$t('sys.operLog.pleaseEnterOperator')" clearable @keyup.enter="handleSearch" />
        </ElFormItem>
        <ElFormItem :label="$t('sys.operLog.result')">
          <ElSelect v-model="queryParams.status" :placeholder="$t('common.all')" clearable style="width: 120px">
            <ElOption :label="$t('common.success')" :value="1" />
            <ElOption :label="$t('common.fail')" :value="0" />
          </ElSelect>
        </ElFormItem>
        <ElFormItem :label="$t('sys.operLog.operationTime')">
          <ElDatePicker
            v-model="dateRange"
            type="daterange"
            :range-separator="$t('common.to')"
            :start-placeholder="$t('common.startDate')"
            :end-placeholder="$t('common.endDate')"
            value-format="YYYY-MM-DD"
            clearable
            style="width: 260px"
          />
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
          <span>{{ $t('sys.operLog.operationLogList') }}</span>
          <ElButton v-permission="'system:oper-log:clean'" type="danger" plain @click="handleClean">
            <template #icon><icon-ep-delete /></template>
            {{ $t('sys.operLog.cleanLogs') }}
          </ElButton>
        </div>
      </template>

      <div class="table-wrapper">
        <ElTable v-loading="loading" :data="tableData" border stripe height="100%">
          <ElTableColumn prop="createdTime" :label="$t('common.time')" width="180" />
          <ElTableColumn prop="module" :label="$t('sys.operLog.module')" min-width="120" show-overflow-tooltip />
          <ElTableColumn prop="action" :label="$t('common.action')" min-width="120" show-overflow-tooltip />
          <ElTableColumn prop="username" :label="$t('sys.operLog.operator')" min-width="100" show-overflow-tooltip />
          <ElTableColumn prop="ip" label="IP" width="140" show-overflow-tooltip />
          <ElTableColumn prop="requestUri" :label="$t('sys.operLog.requestPath')" min-width="200" show-overflow-tooltip />
          <ElTableColumn prop="status" :label="$t('sys.operLog.result')" width="80" align="center">
            <template #default="{ row }">
              <ElTag :type="row.status === 1 ? 'success' : 'danger'" size="small">
                {{ row.status === 1 ? $t('common.success') : $t('common.fail') }}
              </ElTag>
            </template>
          </ElTableColumn>
          <ElTableColumn prop="costMs" :label="$t('sys.operLog.duration')" width="100" align="center">
            <template #default="{ row }">
              <span>{{ row.costMs != null ? `${row.costMs}ms` : '-' }}</span>
            </template>
          </ElTableColumn>
          <ElTableColumn :label="$t('common.details')" width="80" fixed="right" align="center">
            <template #default="{ row }">
              <ElButton type="primary" link @click="handleDetail(row)">{{ $t('common.details') }}</ElButton>
            </template>
          </ElTableColumn>
          <template #empty>
            <ElEmpty :description="$t('common.noData')" />
          </template>
        </ElTable>
      </div>

      <div class="mt-16px flex justify-end">
        <ElPagination
          v-model:current-page="queryParams.pageNum"
          v-model:page-size="queryParams.pageSize"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          @current-change="handlePageChange"
          @size-change="handleSizeChange"
        />
      </div>
    </ElCard>

    <ElDrawer v-model="drawerVisible" :title="$t('sys.operLog.logDetails')" size="560px">
      <ElDescriptions v-if="currentRow" :column="1" border>
        <ElDescriptionsItem :label="$t('common.time')">
          {{ currentRow.createdTime || '-' }}
        </ElDescriptionsItem>
        <ElDescriptionsItem :label="$t('sys.operLog.module')">
          {{ currentRow.module || '-' }}
        </ElDescriptionsItem>
        <ElDescriptionsItem :label="$t('common.action')">
          {{ currentRow.action || '-' }}
        </ElDescriptionsItem>
        <ElDescriptionsItem :label="$t('sys.operLog.operator')">
          {{ currentRow.username || '-' }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="IP">
          {{ currentRow.ip || '-' }}
        </ElDescriptionsItem>
        <ElDescriptionsItem :label="$t('sys.operLog.requestPath')">
          {{ currentRow.requestUri || '-' }}
        </ElDescriptionsItem>
        <ElDescriptionsItem :label="$t('sys.operLog.result')">
          <ElTag :type="currentRow.status === 1 ? 'success' : 'danger'" size="small">
            {{ currentRow.status === 1 ? $t('common.success') : $t('common.fail') }}
          </ElTag>
        </ElDescriptionsItem>
        <ElDescriptionsItem :label="$t('sys.operLog.duration')">
          {{ currentRow.costMs != null ? `${currentRow.costMs}ms` : '-' }}
        </ElDescriptionsItem>
        <ElDescriptionsItem :label="$t('sys.operLog.requestParameters')">
          <pre class="log-pre">{{ formatRequestParams(currentRow.requestParams) || '-' }}</pre>
        </ElDescriptionsItem>
        <ElDescriptionsItem :label="$t('sys.operLog.errorMessage')">
          <span :class="{ 'log-error': currentRow.errorMsg }">{{ currentRow.errorMsg || '-' }}</span>
        </ElDescriptionsItem>
      </ElDescriptions>
    </ElDrawer>
  </div>
</template>

<style scoped>
.log-pre {
  margin: 0;
  max-height: 320px;
  overflow: auto;
  white-space: pre-wrap;
  word-break: break-all;
  font-family: 'JetBrains Mono', Consolas, Menlo, monospace;
  font-size: 12px;
  line-height: 1.6;
}

.log-error {
  color: var(--el-color-danger);
}
</style>
