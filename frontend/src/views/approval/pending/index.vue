<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { getDurationUnit } from '@/constants/business';
import { appTypeMap } from '@/constants/application';
import {
  type Application,
  type ApprovalRecord,
  approveApplication,
  fetchApprovalRecords,
  fetchPendingPage
} from '@/service/api/application';
import { useDictOptions } from '@/composables/use-dict-options';
import { formatDateTime } from '@/utils/format';
import { $t } from '@/locales';

defineOptions({ name: 'ApprovalPending' });

const loading = ref(false);
const data = ref<Application[]>([]);
const total = ref(0);
const currentPage = ref(1);
const pageSize = ref(10);

const detailVisible = ref(false);
const currentRecord = ref<Application | null>(null);
const approvalComment = ref('');
const submitLoading = ref(false);
const approvalRecords = ref<ApprovalRecord[]>([]);

const searchParams = ref({ employeeName: '', appType: undefined as string | undefined });

// 字典走 useDictOptions 模块级缓存，多页面共享
const { options: leaveTypeOptions } = useDictOptions('leave_type');
const { options: transferTypeOptions } = useDictOptions('transfer_type');
const { options: positionOptions } = useDictOptions('position');
const { options: resignTypeOptions } = useDictOptions('resign_type');
const { options: rewardCategoryOptions } = useDictOptions('reward_category');
const { options: punishCategoryOptions } = useDictOptions('punish_category');
const { options: employeeTypeOptions } = useDictOptions('employee_type');

async function loadData() {
  loading.value = true;
  try {
    const res = await fetchPendingPage({ pageNum: currentPage.value, pageSize: pageSize.value, ...searchParams.value });
    data.value = res.data?.records || [];
    total.value = res.data?.total || 0;
  } finally {
    loading.value = false;
  }
}

onMounted(() => {
  loadData();
});

function handleView(row: Application) {
  currentRecord.value = row;
  approvalComment.value = '';
  approvalRecords.value = [];
  detailVisible.value = true;
  loadApprovalRecords(row);
}

/** 加载审批进度记录（空/失败时隐藏区块） */
async function loadApprovalRecords(row: Application) {
  if (!row.id) return;
  try {
    const res = await fetchApprovalRecords(row.id);
    approvalRecords.value = res.data || [];
  } catch {
    approvalRecords.value = [];
  }
}

/** 审批节点状态对应的 timeline 类型 */
function getTimelineType(status?: number): 'primary' | 'success' | 'danger' {
  if (status === 1) return 'success';
  if (status === 2) return 'danger';
  return 'primary';
}

/** 审批节点状态文案 */
function getRecordStatusLabel(status?: number): string {
  if (status === 1) return $t('common.approve');
  if (status === 2) return $t('common.reject');
  return $t('common.pendingApproval');
}

async function handleApprove() {
  if (!currentRecord.value) return;
  try {
    await ElMessageBox.confirm($t('approval.common.areYouSureYouWantToApproveThisApplication'), $t('approval.common.approvalConfirmation'), {
      type: 'warning',
      confirmButtonText: $t('approval.common.confirmApproval'),
      cancelButtonText: $t('approval.common.notNow')
    });
  } catch {
    return;
  }
  submitLoading.value = true;
  try {
    await approveApplication(currentRecord.value.id!, 1, approvalComment.value);
    ElMessage.success($t('approval.common.approved'));
    detailVisible.value = false;
    loadData();
  } catch {
    // 请求层已统一弹错
  } finally {
    submitLoading.value = false;
  }
}

async function handleReject() {
  if (!approvalComment.value) {
    ElMessage.warning($t('approval.common.pleaseEnterRejectionReason'));
    return;
  }
  if (!currentRecord.value) return;
  try {
    await ElMessageBox.confirm($t('approval.common.areYouSureYouWantToRejectThisApplication'), $t('approval.common.approvalConfirmation'), {
      type: 'warning',
      confirmButtonText: $t('approval.common.confirmRejection'),
      cancelButtonText: $t('approval.common.notNow')
    });
  } catch {
    return;
  }
  submitLoading.value = true;
  try {
    await approveApplication(currentRecord.value.id!, 2, approvalComment.value);
    ElMessage.success($t('common.rejected'));
    detailVisible.value = false;
    loadData();
  } catch {
    // 请求层已统一弹错
  } finally {
    submitLoading.value = false;
  }
}

function handleSearch() {
  currentPage.value = 1;
  loadData();
}
function handleReset() {
  searchParams.value = { employeeName: '', appType: undefined };
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

function getDictLabel(options: Api.System.DictData[], value?: string) {
  if (!value) return '';
  return options.find(o => o.dictValue === value)?.dictLabel || value;
}
</script>

<template>
  <div class="list-page">
    <ElCard class="search-card">
      <ElForm inline :model="searchParams">
        <ElFormItem :label="$t('application.common.applicant')">
          <ElInput
            v-model="searchParams.employeeName"
            :placeholder="$t('approval.common.pleaseEnterApplicant')"
            clearable
            @keyup.enter="handleSearch"
          />
        </ElFormItem>
        <ElFormItem :label="$t('application.common.applicationType')">
          <ElSelect v-model="searchParams.appType" :placeholder="$t('common.pleaseSelectType')" clearable style="width: 150px">
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
          <span>{{ $t('approval.pending.pendingApprovals') }}</span>
          <ElTag type="danger">{{ total }} {{ $t('approval.common.pending') }}</ElTag>
        </div>
      </template>

      <div class="table-wrapper">
        <ElTable v-loading="loading" :data="data" border stripe height="100%">
          <ElTableColumn type="index" :label="$t('common.index2')" width="60" align="center" />
          <ElTableColumn prop="appType" :label="$t('application.common.applicationType')" width="120">
            <template #default="{ row }">
              <ElTag>{{ appTypeMap[row.appType] || row.appType }}</ElTag>
            </template>
          </ElTableColumn>
          <ElTableColumn prop="employeeName" :label="$t('application.common.applicant')" width="100" />
          <ElTableColumn prop="companyName" :label="$t('common.company')" width="120" show-overflow-tooltip />
          <ElTableColumn prop="deptName" :label="$t('common.department')" width="120" />
          <ElTableColumn prop="reason" :label="$t('approval.common.applicationReasonNotes')" min-width="200" show-overflow-tooltip>
            <template #default="{ row }">
              <template v-if="row.appType === 'leave'">
                {{ getDictLabel(leaveTypeOptions, row.title) }} {{ row.duration }}{{ getDurationUnit(row.appType) }}
              </template>
              <template v-else-if="row.appType === 'regularization'">{{ $t('approval.common.regularizationDate') }} {{ row.regularDate }}</template>
              <template v-else-if="row.appType === 'transfer'">
                {{ getDictLabel(transferTypeOptions, row.transferType) }}: {{ row.fromDeptName }} → {{ row.toDeptName }}
              </template>
              <template v-else-if="row.appType === 'reward' || row.appType === 'punish'">
                {{
                  getDictLabel(row.appType === 'reward' ? rewardCategoryOptions : punishCategoryOptions, row.category)
                }}
                {{ row.amount ? `¥${row.amount}` : '' }}
              </template>
              <template v-else-if="row.appType === 'resignation'">
                {{ getDictLabel(resignTypeOptions, row.resignType) }} {{ $t('approval.common.lastWorkingDay') }} {{ row.lastWorkDate }}
              </template>
              <template v-else>{{ row.reason }}</template>
            </template>
          </ElTableColumn>
          <ElTableColumn prop="createdTime" :label="$t('application.common.applicationTime')" width="170">
            <template #default="{ row }">{{ formatDateTime(row.createdTime) }}</template>
          </ElTableColumn>
          <ElTableColumn :label="$t('common.action')" width="100" align="center" fixed="right">
            <template #default="{ row }">
              <ElButton
                v-permission="['approval:pending:approve', 'approval:pending:reject']"
                type="success"
                size="small"
                @click="handleView(row)"
              >{{ $t('common.approval') }}</ElButton>
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
    </ElCard>

    <ElDialog v-model="detailVisible" :title="$t('approval.common.approvalDetails')" width="700px">
      <template v-if="currentRecord">
        <ElDescriptions :column="2" border>
          <ElDescriptionsItem :label="$t('application.common.applicationType')">
            {{ appTypeMap[currentRecord.appType] || currentRecord.appType }}
          </ElDescriptionsItem>
          <ElDescriptionsItem :label="$t('application.common.applicant')">{{ currentRecord.employeeName }}</ElDescriptionsItem>
          <ElDescriptionsItem :label="$t('common.company')">{{ currentRecord.companyName }}</ElDescriptionsItem>
          <ElDescriptionsItem :label="$t('common.department')">{{ currentRecord.deptName }}</ElDescriptionsItem>
          <ElDescriptionsItem :label="$t('application.common.applicationTime')" :span="2">
            {{ formatDateTime(currentRecord.createdTime) }}
          </ElDescriptionsItem>

          <!-- 请假/加班/出差/补卡/换休 -->
          <template v-if="['leave', 'overtime', 'business', 'makeup', 'exchange'].includes(currentRecord.appType)">
            <ElDescriptionsItem :label="$t('common.startTime')">{{ currentRecord.startTime }}</ElDescriptionsItem>
            <ElDescriptionsItem :label="$t('common.endTime')">{{ currentRecord.endTime }}</ElDescriptionsItem>
            <ElDescriptionsItem v-if="currentRecord.appType === 'leave'" :label="$t('common.leaveType')">
              {{ getDictLabel(leaveTypeOptions, currentRecord.title) }}
            </ElDescriptionsItem>
            <ElDescriptionsItem v-if="currentRecord.duration" :label="$t('approval.common.duration')">
              {{ currentRecord.duration }} {{ getDurationUnit(currentRecord.appType) }}
            </ElDescriptionsItem>
          </template>

          <!-- 转正申请 -->
          <template v-if="currentRecord.appType === 'regularization'">
            <ElDescriptionsItem :label="$t('common.entryDate')">{{ currentRecord.entryDate }}</ElDescriptionsItem>
            <ElDescriptionsItem :label="$t('approval.common.probationEnd')">{{ currentRecord.probationEndDate }}</ElDescriptionsItem>
            <ElDescriptionsItem :label="$t('application.regularization.regularizationDate')">{{ currentRecord.regularDate }}</ElDescriptionsItem>
            <ElDescriptionsItem :label="$t('approval.common.categoryAfterRegularization')">
              {{ getDictLabel(employeeTypeOptions, currentRecord.newEmployeeType) }}
            </ElDescriptionsItem>
            <ElDescriptionsItem :label="$t('application.regularization.probationEvaluation')" :span="2">{{ currentRecord.evaluation }}</ElDescriptionsItem>
          </template>

          <!-- 调动申请 -->
          <template v-if="currentRecord.appType === 'transfer'">
            <ElDescriptionsItem :label="$t('application.transfer.changeType')" :span="2">
              {{ getDictLabel(transferTypeOptions, currentRecord.transferType) }}
            </ElDescriptionsItem>
            <ElDescriptionsItem :label="$t('application.transfer.originalCompany')">{{ currentRecord.fromCompanyName }}</ElDescriptionsItem>
            <ElDescriptionsItem :label="$t('application.transfer.newCompany')">{{ currentRecord.toCompanyName }}</ElDescriptionsItem>
            <ElDescriptionsItem :label="$t('application.transfer.originalDepartment')">{{ currentRecord.fromDeptName }}</ElDescriptionsItem>
            <ElDescriptionsItem :label="$t('application.transfer.newDepartment')">{{ currentRecord.toDeptName }}</ElDescriptionsItem>
            <ElDescriptionsItem :label="$t('application.transfer.originalPosition')">
              {{ getDictLabel(positionOptions, currentRecord.fromPosition) }}
            </ElDescriptionsItem>
            <ElDescriptionsItem :label="$t('application.transfer.newPosition')">
              {{ getDictLabel(positionOptions, currentRecord.toPosition) }}
            </ElDescriptionsItem>
            <ElDescriptionsItem :label="$t('application.reward.effectiveDate')" :span="2">{{ currentRecord.effectDate }}</ElDescriptionsItem>
          </template>

          <!-- 奖惩申请 -->
          <template v-if="currentRecord.appType === 'reward' || currentRecord.appType === 'punish'">
            <ElDescriptionsItem :label="$t('common.type')">
              {{ currentRecord.appType === 'reward' ? $t('common.reward') : $t('common.punishment') }}
            </ElDescriptionsItem>
            <ElDescriptionsItem :label="$t('common.category')">
              {{
                getDictLabel(
                  currentRecord.appType === 'reward' ? rewardCategoryOptions : punishCategoryOptions,
                  currentRecord.category
                )
              }}
            </ElDescriptionsItem>
            <ElDescriptionsItem :label="$t('common.amount')">
              {{ currentRecord.amount ? `¥${currentRecord.amount}` : '-' }}
            </ElDescriptionsItem>
            <ElDescriptionsItem :label="$t('application.reward.effectiveDate')">{{ currentRecord.effectDate }}</ElDescriptionsItem>
          </template>

          <!-- 离职申请 -->
          <template v-if="currentRecord.appType === 'resignation'">
            <ElDescriptionsItem :label="$t('common.entryDate')">{{ currentRecord.entryDate }}</ElDescriptionsItem>
            <ElDescriptionsItem :label="$t('common.resignationType')">
              {{ getDictLabel(resignTypeOptions, currentRecord.resignType) }}
            </ElDescriptionsItem>
            <ElDescriptionsItem :label="$t('application.resignation.lastWorkingDay')">{{ currentRecord.lastWorkDate }}</ElDescriptionsItem>
            <ElDescriptionsItem :label="$t('common.handoverPerson')">{{ currentRecord.handoverToName }}</ElDescriptionsItem>
          </template>

          <ElDescriptionsItem :label="$t('approval.common.applicationReasonRemark')" :span="2">{{ currentRecord.reason }}</ElDescriptionsItem>
        </ElDescriptions>

        <!-- 审批进度 -->
        <div v-if="approvalRecords.length > 0" class="mt-20px">
          <div class="mb-10px font-bold">{{ $t('approval.common.approvalProgress') }}</div>
          <ElTimeline>
            <ElTimelineItem
              v-for="(record, index) in approvalRecords"
              :key="`${record.createTime || ''}-${record.nodeName || ''}-${record.approverName || ''}-${index}`"
              :type="getTimelineType(record.status)"
              :timestamp="record.createTime"
            >
              <div class="flex items-center gap-8px">
                <span class="font-medium">{{ record.nodeName || $t('approval.flow.approvalNode') }}</span>
                <ElTag :type="record.status === 1 ? 'success' : record.status === 2 ? 'danger' : 'info'" size="small">
                  {{ getRecordStatusLabel(record.status) }}
                </ElTag>
                <span v-if="record.approverName" class="text-xs text-gray-500">{{ $t('approval.common.approver') }} {{ record.approverName }}</span>
              </div>
              <div v-if="record.comment" class="mt-2px text-xs text-gray-500">{{ record.comment }}</div>
            </ElTimelineItem>
          </ElTimeline>
        </div>

        <div class="mt-20px">
          <div class="mb-10px font-bold">{{ $t('application.common.approvalComment') }}</div>
          <ElInput v-model="approvalComment" type="textarea" :rows="3" :placeholder="$t('approval.common.pleaseEnterApprovalCommentRequiredWhenRejecting')" />
        </div>
      </template>
      <template #footer>
        <ElButton @click="detailVisible = false">{{ $t('common.cancel') }}</ElButton>
        <ElButton v-permission="'approval:pending:reject'" type="danger" :loading="submitLoading" @click="handleReject">{{ $t('common.reject') }}</ElButton>
        <ElButton v-permission="'approval:pending:approve'" type="success" :loading="submitLoading" @click="handleApprove">{{ $t('common.approve') }}</ElButton>
      </template>
    </ElDialog>
  </div>
</template>
