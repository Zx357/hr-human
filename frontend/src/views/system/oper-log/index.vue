<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { cleanOperLogs, fetchOperLogPage } from '@/service/api';

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
    const { value } = await ElMessageBox.prompt('将删除该天数之前的所有操作日志', '清理日志', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputValue: '90',
      inputPattern: /^\d+$/,
      inputErrorMessage: '请输入数字',
      inputValidator: (input: string) => {
        const days = Number(input);
        if (!Number.isFinite(days) || days < 7) {
          return '保留天数不能小于7天';
        }
        return true;
      }
    });

    const days = Number(value);
    const { error } = await cleanOperLogs(days);

    if (!error) {
      ElMessage.success('清理成功');
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
        <ElFormItem label="模块">
          <ElInput v-model="queryParams.module" placeholder="请输入模块" clearable />
        </ElFormItem>
        <ElFormItem label="操作人">
          <ElInput v-model="queryParams.username" placeholder="请输入操作人" clearable />
        </ElFormItem>
        <ElFormItem label="结果">
          <ElSelect v-model="queryParams.status" placeholder="全部" clearable style="width: 120px">
            <ElOption label="成功" :value="1" />
            <ElOption label="失败" :value="0" />
          </ElSelect>
        </ElFormItem>
        <ElFormItem label="操作时间">
          <ElDatePicker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
            clearable
            style="width: 260px"
          />
        </ElFormItem>
        <ElFormItem>
          <ElButton type="primary" @click="handleSearch">
            <template #icon><icon-ep-search /></template>
            查询
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
          <span>操作日志列表</span>
          <ElButton v-permission="'system:oper-log:clean'" type="danger" plain @click="handleClean">
            <template #icon><icon-ep-delete /></template>
            清理日志
          </ElButton>
        </div>
      </template>

      <div class="table-wrapper">
        <ElTable v-loading="loading" :data="tableData" border stripe height="100%">
          <ElTableColumn prop="createdTime" label="时间" width="180" />
          <ElTableColumn prop="module" label="模块" min-width="120" show-overflow-tooltip />
          <ElTableColumn prop="action" label="操作" min-width="120" show-overflow-tooltip />
          <ElTableColumn prop="username" label="操作人" min-width="100" show-overflow-tooltip />
          <ElTableColumn prop="ip" label="IP" width="140" show-overflow-tooltip />
          <ElTableColumn prop="requestUri" label="请求路径" min-width="200" show-overflow-tooltip />
          <ElTableColumn prop="status" label="结果" width="80" align="center">
            <template #default="{ row }">
              <ElTag :type="row.status === 1 ? 'success' : 'danger'" size="small">
                {{ row.status === 1 ? '成功' : '失败' }}
              </ElTag>
            </template>
          </ElTableColumn>
          <ElTableColumn prop="costMs" label="耗时" width="100" align="center">
            <template #default="{ row }">
              <span>{{ row.costMs != null ? `${row.costMs}ms` : '-' }}</span>
            </template>
          </ElTableColumn>
          <ElTableColumn label="详情" width="80" fixed="right" align="center">
            <template #default="{ row }">
              <ElButton type="primary" link @click="handleDetail(row)">详情</ElButton>
            </template>
          </ElTableColumn>
          <template #empty>
            <ElEmpty description="暂无数据" />
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

    <ElDrawer v-model="drawerVisible" title="日志详情" size="560px">
      <ElDescriptions v-if="currentRow" :column="1" border>
        <ElDescriptionsItem label="时间">
          {{ currentRow.createdTime || '-' }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="模块">
          {{ currentRow.module || '-' }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="操作">
          {{ currentRow.action || '-' }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="操作人">
          {{ currentRow.username || '-' }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="IP">
          {{ currentRow.ip || '-' }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="请求路径">
          {{ currentRow.requestUri || '-' }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="结果">
          <ElTag :type="currentRow.status === 1 ? 'success' : 'danger'" size="small">
            {{ currentRow.status === 1 ? '成功' : '失败' }}
          </ElTag>
        </ElDescriptionsItem>
        <ElDescriptionsItem label="耗时">
          {{ currentRow.costMs != null ? `${currentRow.costMs}ms` : '-' }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="请求参数">
          <pre class="log-pre">{{ formatRequestParams(currentRow.requestParams) || '-' }}</pre>
        </ElDescriptionsItem>
        <ElDescriptionsItem label="错误信息">
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
