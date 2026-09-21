<script setup lang="ts">
import { ref, watch } from 'vue';
import { getDurationUnit } from '@/constants/business';
import { appTypeLabel, statusLabel, statusMap } from '@/constants/application';
import { type Application, type ApprovalRecord, fetchApprovalRecords } from '@/service/api/application';
import { useDictOptions } from '@/composables/use-dict-options';
import { formatDateTime } from '@/utils/format';
import { $t } from '@/locales';

defineOptions({ name: 'ApplicationDetailDrawer' });

const props = defineProps<{
  /** 申请记录（需含 id），为空时抽屉不渲染内容 */
  application: Application | null;
}>();

const visible = defineModel<boolean>({ required: true });

const { getDictLabel: getLeaveTypeLabel } = useDictOptions('leave_type');
const { getDictLabel: getTransferTypeLabel } = useDictOptions('transfer_type');
const { getDictLabel: getPositionLabel } = useDictOptions('position');
const { getDictLabel: getResignTypeLabel } = useDictOptions('resign_type');
const { getDictLabel: getRewardCategoryLabel } = useDictOptions('reward_category');
const { getDictLabel: getPunishCategoryLabel } = useDictOptions('punish_category');
const { getDictLabel: getEmployeeTypeLabel } = useDictOptions('employee_type');

const approvalRecords = ref<ApprovalRecord[]>([]);
const recordsLoading = ref(false);

/** 打开抽屉时加载审批进度记录（空/失败时隐藏区块） */
watch(
  [visible, () => props.application?.id],
  ([isVisible, id]) => {
    approvalRecords.value = [];
    if (!isVisible || !id) return;
    recordsLoading.value = true;
    fetchApprovalRecords(id)
      .then(res => {
        approvalRecords.value = res.data || [];
      })
      .catch(() => {
        approvalRecords.value = [];
      })
      .finally(() => {
        recordsLoading.value = false;
      });
  },
  { immediate: true }
);

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
</script>

<template>
  <ElDrawer v-model="visible" :title="$t('application.detailDrawer.applicationDetails')" size="640px">
    <template v-if="application">
      <ElDescriptions :column="2" border>
        <ElDescriptionsItem :label="$t('application.common.applicationType')">
          {{ appTypeLabel(application.appType) }}
        </ElDescriptionsItem>
        <ElDescriptionsItem :label="$t('common.status')">
          <ElTag :type="statusMap[application.status ?? -1]?.type as any" size="small">
            {{ statusLabel(application.status) }}
          </ElTag>
        </ElDescriptionsItem>
        <ElDescriptionsItem :label="$t('application.common.applicant')">{{ application.employeeName || '-' }}</ElDescriptionsItem>
        <ElDescriptionsItem :label="$t('common.employeeNo')">{{ application.employeeNo || '-' }}</ElDescriptionsItem>
        <ElDescriptionsItem :label="$t('common.company')">{{ application.companyName || '-' }}</ElDescriptionsItem>
        <ElDescriptionsItem :label="$t('common.department')">{{ application.deptName || '-' }}</ElDescriptionsItem>
        <ElDescriptionsItem :label="$t('application.common.applicationTime')" :span="2">
          {{ formatDateTime(application.createdTime) }}
        </ElDescriptionsItem>

        <!-- 请假/加班/出差/补卡/换休 -->
        <template v-if="['leave', 'overtime', 'business', 'makeup', 'exchange'].includes(application.appType)">
          <ElDescriptionsItem :label="$t('common.startTime')">{{ application.startTime || '-' }}</ElDescriptionsItem>
          <ElDescriptionsItem :label="$t('common.endTime')">{{ application.endTime || '-' }}</ElDescriptionsItem>
          <ElDescriptionsItem v-if="application.appType === 'leave'" :label="$t('common.leaveType')">
            {{ getLeaveTypeLabel(application.title) || '-' }}
          </ElDescriptionsItem>
          <ElDescriptionsItem v-if="application.duration != null" :label="$t('approval.common.duration')">
            {{ application.duration }} {{ getDurationUnit(application.appType) }}
          </ElDescriptionsItem>
        </template>

        <!-- 转正申请 -->
        <template v-if="application.appType === 'regularization'">
          <ElDescriptionsItem :label="$t('common.entryDate')">{{ application.entryDate || '-' }}</ElDescriptionsItem>
          <ElDescriptionsItem :label="$t('approval.common.probationEnd')">{{ application.probationEndDate || '-' }}</ElDescriptionsItem>
          <ElDescriptionsItem :label="$t('application.regularization.regularizationDate')">{{ application.regularDate || '-' }}</ElDescriptionsItem>
          <ElDescriptionsItem :label="$t('approval.common.categoryAfterRegularization')">
            {{ getEmployeeTypeLabel(application.newEmployeeType) || '-' }}
          </ElDescriptionsItem>
          <ElDescriptionsItem :label="$t('application.regularization.probationEvaluation')" :span="2">{{ application.evaluation || '-' }}</ElDescriptionsItem>
        </template>

        <!-- 调动申请 -->
        <template v-if="application.appType === 'transfer'">
          <ElDescriptionsItem :label="$t('application.transfer.changeType')" :span="2">
            {{ getTransferTypeLabel(application.transferType) || '-' }}
          </ElDescriptionsItem>
          <ElDescriptionsItem :label="$t('application.transfer.originalCompany')">{{ application.fromCompanyName || '-' }}</ElDescriptionsItem>
          <ElDescriptionsItem :label="$t('application.transfer.newCompany')">{{ application.toCompanyName || '-' }}</ElDescriptionsItem>
          <ElDescriptionsItem :label="$t('application.transfer.originalDepartment')">{{ application.fromDeptName || '-' }}</ElDescriptionsItem>
          <ElDescriptionsItem :label="$t('application.transfer.newDepartment')">{{ application.toDeptName || '-' }}</ElDescriptionsItem>
          <ElDescriptionsItem :label="$t('application.transfer.originalPosition')">
            {{ getPositionLabel(application.fromPosition) || '-' }}
          </ElDescriptionsItem>
          <ElDescriptionsItem :label="$t('application.transfer.newPosition')">
            {{ getPositionLabel(application.toPosition) || '-' }}
          </ElDescriptionsItem>
          <ElDescriptionsItem :label="$t('application.reward.effectiveDate')" :span="2">{{ application.effectDate || '-' }}</ElDescriptionsItem>
        </template>

        <!-- 奖惩申请 -->
        <template v-if="application.appType === 'reward' || application.appType === 'punish'">
          <ElDescriptionsItem :label="$t('common.type')">
            {{ application.appType === 'reward' ? $t('common.reward') : $t('common.punishment') }}
          </ElDescriptionsItem>
          <ElDescriptionsItem :label="$t('common.category')">
            {{
              application.appType === 'reward'
                ? getRewardCategoryLabel(application.category) || '-'
                : getPunishCategoryLabel(application.category) || '-'
            }}
          </ElDescriptionsItem>
          <ElDescriptionsItem :label="$t('common.amount')">
            {{ application.amount ? `¥${application.amount}` : '-' }}
          </ElDescriptionsItem>
          <ElDescriptionsItem :label="$t('application.reward.effectiveDate')">{{ application.effectDate || '-' }}</ElDescriptionsItem>
        </template>

        <!-- 离职申请 -->
        <template v-if="application.appType === 'resignation'">
          <ElDescriptionsItem :label="$t('common.entryDate')">{{ application.entryDate || '-' }}</ElDescriptionsItem>
          <ElDescriptionsItem :label="$t('common.resignationType')">
            {{ getResignTypeLabel(application.resignType) || '-' }}
          </ElDescriptionsItem>
          <ElDescriptionsItem :label="$t('application.resignation.lastWorkingDay')">{{ application.lastWorkDate || '-' }}</ElDescriptionsItem>
          <ElDescriptionsItem :label="$t('common.handoverPerson')">{{ application.handoverToName || '-' }}</ElDescriptionsItem>
        </template>

        <ElDescriptionsItem :label="$t('approval.common.applicationReasonRemark')" :span="2">{{ application.reason || '-' }}</ElDescriptionsItem>
        <ElDescriptionsItem :label="$t('application.common.approvalComment')" :span="2">{{ application.approveRemark || '-' }}</ElDescriptionsItem>
      </ElDescriptions>

      <!-- 审批进度 -->
      <div v-loading="recordsLoading" class="mt-20px">
        <div class="mb-10px font-bold">{{ $t('approval.common.approvalProgress') }}</div>
        <ElTimeline v-if="approvalRecords.length > 0">
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
        <ElEmpty v-else-if="!recordsLoading" :description="$t('application.detailDrawer.noApprovalProgressRecordsYet')" :image-size="60" />
      </div>
    </template>
  </ElDrawer>
</template>
