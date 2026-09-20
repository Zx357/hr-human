<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { type Application, cancelApplication, fetchApplicationPage } from '@/service/api/application';
import { fetchGetUserById } from '@/service/api/system';
import { useAuthStore } from '@/store/modules/auth';
import ApplicationDetailDrawer from '@/components/business/application-detail-drawer.vue';

defineOptions({ name: 'ApprovalMine' });

const authStore = useAuthStore();

const loading = ref(false);
const data = ref<Application[]>([]);
const total = ref(0);
const currentPage = ref(1);
const pageSize = ref(10);
const currentEmployeeId = ref<number | undefined>(undefined);
/** 当前账号未关联员工：置空列表且不发起请求 */
const noEmployee = ref(false);

const searchParams = ref({ appType: undefined as string | undefined, status: undefined as number | undefined });

// 详情抽屉
const detailVisible = ref(false);
const currentApplication = ref<Application | null>(null);

function handleViewDetail(row: Application) {
  currentApplication.value = row;
  detailVisible.value = true;
}

/** 撤销待审批的申请 */
async function handleCancel(row: Application) {
  if (!row.id) return;
  try {
    await ElMessageBox.confirm('确定撤销该申请吗？撤销后不可恢复', '撤销确认', {
      type: 'warning',
      confirmButtonText: '确认撤销',
      cancelButtonText: '取消'
    });
  } catch {
    return;
  }
  try {
    await cancelApplication(row.id);
    ElMessage.success('已撤销');
    loadData();
  } catch {
    ElMessage.error('撤销失败');
  }
}

/** 获取当前登录用户关联的员工ID：优先用登录信息自带的 employeeId，否则查用户详情 */
async function loadCurrentEmployeeId() {
  const fromAuth = Number((authStore.userInfo as unknown as { employeeId?: number | string } | null)?.employeeId);
  if (Number.isFinite(fromAuth) && fromAuth > 0) {
    currentEmployeeId.value = fromAuth;
    return;
  }
  const userId = Number(authStore.userInfo?.userId);
  if (!Number.isFinite(userId) || userId <= 0) return;
  try {
    const res = await fetchGetUserById(userId);
    currentEmployeeId.value = res.data?.user?.employeeId;
  } catch {
    currentEmployeeId.value = undefined;
  }
}

async function loadData() {
  // 拿不到 employeeId 时不回退查全部，直接置空列表
  if (!currentEmployeeId.value) {
    noEmployee.value = true;
    data.value = [];
    total.value = 0;
    return;
  }
  loading.value = true;
  try {
    const res = await fetchApplicationPage({
      pageNum: currentPage.value,
      pageSize: pageSize.value,
      employeeId: currentEmployeeId.value,
      ...searchParams.value
    });
    data.value = res.data?.records || [];
    total.value = res.data?.total || 0;
  } finally {
    loading.value = false;
  }
}

onMounted(async () => {
  await loadCurrentEmployeeId();
  loadData();
});

function handleSearch() {
  currentPage.value = 1;
  loadData();
}
function handleReset() {
  searchParams.value = { appType: undefined, status: undefined };
  currentPage.value = 1;
  loadData();
}
function handlePageChange(page: number) {
  currentPage.value = page;
  loadData();
}
function handleSizeChange(size: number) {
  pageSize.value = size;
  currentPage.value = 1;
  loadData();
}

const appTypeMap: Record<string, string> = {
  leave: '请假申请',
  overtime: '加班申请',
  business: '出差申请',
  makeup: '补卡申请',
  exchange: '换休申请',
  regularization: '转正申请',
  transfer: '调动申请',
  reward: '奖励申请',
  punish: '惩罚申请',
  resignation: '离职申请'
};

const statusMap: Record<number, { label: string; type: string }> = {
  0: { label: '待审批', type: 'warning' },
  1: { label: '已通过', type: 'success' },
  2: { label: '已拒绝', type: 'danger' },
  3: { label: '已撤销', type: 'info' }
};
</script>

<template>
  <div class="list-page">
    <ElCard class="search-card">
      <ElForm inline :model="searchParams">
        <ElFormItem label="申请类型">
          <ElSelect v-model="searchParams.appType" placeholder="请选择类型" clearable>
            <ElOption label="请假申请" value="leave" />
            <ElOption label="加班申请" value="overtime" />
            <ElOption label="出差申请" value="business" />
            <ElOption label="补卡申请" value="makeup" />
            <ElOption label="换休申请" value="exchange" />
            <ElOption label="转正申请" value="regularization" />
            <ElOption label="调动申请" value="transfer" />
            <ElOption label="奖励申请" value="reward" />
            <ElOption label="惩罚申请" value="punish" />
            <ElOption label="离职申请" value="resignation" />
          </ElSelect>
        </ElFormItem>
        <ElFormItem label="状态">
          <ElSelect v-model="searchParams.status" placeholder="请选择状态" clearable>
            <ElOption label="待审批" :value="0" />
            <ElOption label="已通过" :value="1" />
            <ElOption label="已拒绝" :value="2" />
            <ElOption label="已撤销" :value="3" />
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
      <template #header><span>我的申请</span></template>

      <div v-if="noEmployee" class="table-wrapper flex items-center justify-center">
        <ElEmpty description="当前账号未关联员工" />
      </div>
      <template v-else>
        <div class="table-wrapper">
          <ElTable v-loading="loading" :data="data" border stripe height="100%">
            <ElTableColumn type="index" label="序号" width="60" align="center" />
            <ElTableColumn prop="appType" label="申请类型" width="120">
              <template #default="{ row }">
                <ElTag>{{ appTypeMap[row.appType] || row.appType }}</ElTag>
              </template>
            </ElTableColumn>
            <ElTableColumn prop="startTime" label="开始时间" width="160" />
            <ElTableColumn prop="endTime" label="结束时间" width="160" />
            <ElTableColumn prop="reason" label="申请原因" min-width="200" show-overflow-tooltip />
            <ElTableColumn prop="status" label="状态" width="100" align="center">
              <template #default="{ row }">
                <ElTag :type="statusMap[row.status]?.type as any">{{ statusMap[row.status]?.label }}</ElTag>
              </template>
            </ElTableColumn>
            <ElTableColumn prop="approveRemark" label="审批意见" width="150" show-overflow-tooltip />
            <ElTableColumn prop="createdTime" label="申请时间" width="170" />
            <ElTableColumn label="操作" width="130" align="center" fixed="right">
              <template #default="{ row }">
                <ElButton type="primary" link size="small" @click="handleViewDetail(row)">详情</ElButton>
                <ElButton v-if="row.status === 0" type="warning" link size="small" @click="handleCancel(row)">
                  撤销
                </ElButton>
              </template>
            </ElTableColumn>
          </ElTable>
        </div>

        <div class="mt-16px flex justify-end">
          <ElPagination
            v-model:current-page="currentPage"
            v-model:page-size="pageSize"
            :total="total"
            :page-sizes="[10, 20, 50, 100]"
            layout="total, sizes, prev, pager, next, jumper"
            @current-change="handlePageChange"
            @size-change="handleSizeChange"
          />
        </div>
      </template>
    </ElCard>

    <ApplicationDetailDrawer v-model="detailVisible" :application="currentApplication" />
  </div>
</template>

<style scoped>
.list-page {
  display: flex;
  flex-direction: column;
  height: 100%;
}

.table-card {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.table-card :deep(.el-card__body) {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.table-wrapper {
  flex: 1;
  overflow: hidden;
}
</style>
