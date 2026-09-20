<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { appTypeMap, statusMap } from '@/constants/application';
import { type Application, cancelApplication, fetchApplicationPage } from '@/service/api/application';
import { fetchGetUserById } from '@/service/api/system';
import { useAuthStore } from '@/store/modules/auth';
import { formatDateTime } from '@/utils/format';
import ApplicationDetailDrawer from '@/components/business/application-detail-drawer.vue';
import { $t } from '@/locales';

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
    await ElMessageBox.confirm($t('application.business.areYouSureYouWantToWithdrawThisApplicationThisCannotBeUndone'), $t('application.common.withdrawalConfirmation'), {
      type: 'warning',
      confirmButtonText: $t('application.common.confirmWithdrawal'),
      cancelButtonText: $t('common.cancel')
    });
  } catch {
    return;
  }
  try {
    await cancelApplication(row.id);
    ElMessage.success($t('common.withdrawn'));
    loadData();
  } catch {
    // 请求层已统一弹错
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
</script>

<template>
  <div class="list-page">
    <ElCard class="search-card">
      <ElForm inline :model="searchParams">
        <ElFormItem :label="$t('application.common.applicationType')">
          <ElSelect v-model="searchParams.appType" :placeholder="$t('common.pleaseSelectType')" clearable>
            <ElOption :label="$t('common.leaveApplication')" value="leave" />
            <ElOption :label="$t('common.overtimeApplication')" value="overtime" />
            <ElOption :label="$t('common.businessTripApplication')" value="business" />
            <ElOption :label="$t('common.makeupClockApplication')" value="makeup" />
            <ElOption :label="$t('common.exchangeLeaveApplication')" value="exchange" />
            <ElOption :label="$t('common.regularizationApplication')" value="regularization" />
            <ElOption :label="$t('common.transferApplication')" value="transfer" />
            <ElOption :label="$t('approval.common.rewardApplication')" value="reward" />
            <ElOption :label="$t('approval.common.punishmentApplication')" value="punish" />
            <ElOption :label="$t('common.resignationApplication')" value="resignation" />
          </ElSelect>
        </ElFormItem>
        <ElFormItem :label="$t('common.status')">
          <ElSelect v-model="searchParams.status" :placeholder="$t('common.pleaseSelectStatus')" clearable>
            <ElOption :label="$t('common.pendingApproval')" :value="0" />
            <ElOption :label="$t('common.approved')" :value="1" />
            <ElOption :label="$t('common.rejected')" :value="2" />
            <ElOption :label="$t('common.withdrawn')" :value="3" />
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
      <template #header><span>{{ $t('approval.mine.myApplications') }}</span></template>

      <div v-if="noEmployee" class="table-wrapper flex items-center justify-center">
        <ElEmpty :description="$t('application.common.currentAccountIsNotLinkedToAnEmployee')" />
      </div>
      <template v-else>
        <div class="table-wrapper">
          <ElTable v-loading="loading" :data="data" border stripe height="100%">
            <ElTableColumn type="index" :label="$t('common.index2')" width="60" align="center" />
            <ElTableColumn prop="appType" :label="$t('application.common.applicationType')" width="120">
              <template #default="{ row }">
                <ElTag>{{ appTypeMap[row.appType] || row.appType }}</ElTag>
              </template>
            </ElTableColumn>
            <ElTableColumn prop="startTime" :label="$t('common.startTime')" width="160" />
            <ElTableColumn prop="endTime" :label="$t('common.endTime')" width="160" />
            <ElTableColumn prop="reason" :label="$t('application.common.applicationReason2')" min-width="200" show-overflow-tooltip />
            <ElTableColumn prop="status" :label="$t('common.status')" width="100" align="center">
              <template #default="{ row }">
                <ElTag :type="statusMap[row.status]?.type as any">{{ statusMap[row.status]?.label }}</ElTag>
              </template>
            </ElTableColumn>
            <ElTableColumn prop="approveRemark" :label="$t('application.common.approvalComment')" width="150" show-overflow-tooltip />
            <ElTableColumn prop="createdTime" :label="$t('application.common.applicationTime')" width="170">
              <template #default="{ row }">{{ formatDateTime(row.createdTime) }}</template>
            </ElTableColumn>
            <ElTableColumn :label="$t('common.action')" width="130" align="center" fixed="right">
              <template #default="{ row }">
                <ElButton type="primary" link size="small" @click="handleViewDetail(row)">{{ $t('common.details') }}</ElButton>
                <ElButton v-if="row.status === 0" type="warning" link size="small" @click="handleCancel(row)">
                  {{ $t('common.withdraw') }}
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
