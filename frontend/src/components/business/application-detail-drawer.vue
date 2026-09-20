<script setup lang="ts">
import { ref, watch } from 'vue';
import { getDurationUnit } from '@/constants/business';
import { useDictOptions } from '@/composables/use-dict-options';
import { type Application, type ApprovalRecord, fetchApprovalRecords } from '@/service/api/application';

defineOptions({ name: 'ApplicationDetailDrawer' });

const props = defineProps<{
  /** 申请记录（需含 id），为空时抽屉不渲染内容 */
  application: Application | null;
}>();

const visible = defineModel<boolean>({ required: true });

const { options: leaveTypeOptions, getDictLabel: getLeaveTypeLabel } = useDictOptions('leave_type');
const { options: transferTypeOptions, getDictLabel: getTransferTypeLabel } = useDictOptions('transfer_type');
const { options: positionOptions, getDictLabel: getPositionLabel } = useDictOptions('position');
const { options: resignTypeOptions, getDictLabel: getResignTypeLabel } = useDictOptions('resign_type');
const { options: rewardCategoryOptions, getDictLabel: getRewardCategoryLabel } = useDictOptions('reward_category');
const { options: punishCategoryOptions, getDictLabel: getPunishCategoryLabel } = useDictOptions('punish_category');
const { options: employeeTypeOptions, getDictLabel: getEmployeeTypeLabel } = useDictOptions('employee_type');

const approvalRecords = ref<ApprovalRecord[]>([]);
const recordsLoading = ref(false);

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
  if (status === 1) return '通过';
  if (status === 2) return '拒绝';
  return '待审批';
}
</script>

<template>
  <ElDrawer v-model="visible" title="申请详情" size="640px">
    <template v-if="application">
      <ElDescriptions :column="2" border>
        <ElDescriptionsItem label="申请类型">
          {{ appTypeMap[application.appType] || application.appType }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="状态">
          <ElTag :type="statusMap[application.status ?? -1]?.type as any" size="small">
            {{ statusMap[application.status ?? -1]?.label || '-' }}
          </ElTag>
        </ElDescriptionsItem>
        <ElDescriptionsItem label="申请人">{{ application.employeeName || '-' }}</ElDescriptionsItem>
        <ElDescriptionsItem label="工号">{{ application.employeeNo || '-' }}</ElDescriptionsItem>
        <ElDescriptionsItem label="公司">{{ application.companyName || '-' }}</ElDescriptionsItem>
        <ElDescriptionsItem label="部门">{{ application.deptName || '-' }}</ElDescriptionsItem>
        <ElDescriptionsItem label="申请时间" :span="2">{{ application.createdTime || '-' }}</ElDescriptionsItem>

        <!-- 请假/加班/出差/补卡/换休 -->
        <template v-if="['leave', 'overtime', 'business', 'makeup', 'exchange'].includes(application.appType)">
          <ElDescriptionsItem label="开始时间">{{ application.startTime || '-' }}</ElDescriptionsItem>
          <ElDescriptionsItem label="结束时间">{{ application.endTime || '-' }}</ElDescriptionsItem>
          <ElDescriptionsItem v-if="application.appType === 'leave'" label="请假类型">
            {{ getLeaveTypeLabel(application.title) || '-' }}
          </ElDescriptionsItem>
          <ElDescriptionsItem v-if="application.duration != null" label="时长">
            {{ application.duration }} {{ getDurationUnit(application.appType) }}
          </ElDescriptionsItem>
        </template>

        <!-- 转正申请 -->
        <template v-if="application.appType === 'regularization'">
          <ElDescriptionsItem label="入职日期">{{ application.entryDate || '-' }}</ElDescriptionsItem>
          <ElDescriptionsItem label="试用期结束">{{ application.probationEndDate || '-' }}</ElDescriptionsItem>
          <ElDescriptionsItem label="转正日期">{{ application.regularDate || '-' }}</ElDescriptionsItem>
          <ElDescriptionsItem label="转正后类别">
            {{ getEmployeeTypeLabel(application.newEmployeeType) || '-' }}
          </ElDescriptionsItem>
          <ElDescriptionsItem label="试用期评价" :span="2">{{ application.evaluation || '-' }}</ElDescriptionsItem>
        </template>

        <!-- 调动申请 -->
        <template v-if="application.appType === 'transfer'">
          <ElDescriptionsItem label="变更类型" :span="2">
            {{ getTransferTypeLabel(application.transferType) || '-' }}
          </ElDescriptionsItem>
          <ElDescriptionsItem label="原公司">{{ application.fromCompanyName || '-' }}</ElDescriptionsItem>
          <ElDescriptionsItem label="新公司">{{ application.toCompanyName || '-' }}</ElDescriptionsItem>
          <ElDescriptionsItem label="原部门">{{ application.fromDeptName || '-' }}</ElDescriptionsItem>
          <ElDescriptionsItem label="新部门">{{ application.toDeptName || '-' }}</ElDescriptionsItem>
          <ElDescriptionsItem label="原职位">
            {{ getPositionLabel(application.fromPosition) || '-' }}
          </ElDescriptionsItem>
          <ElDescriptionsItem label="新职位">
            {{ getPositionLabel(application.toPosition) || '-' }}
          </ElDescriptionsItem>
          <ElDescriptionsItem label="生效日期" :span="2">{{ application.effectDate || '-' }}</ElDescriptionsItem>
        </template>

        <!-- 奖惩申请 -->
        <template v-if="application.appType === 'reward' || application.appType === 'punish'">
          <ElDescriptionsItem label="类型">
            {{ application.appType === 'reward' ? '奖励' : '惩罚' }}
          </ElDescriptionsItem>
          <ElDescriptionsItem label="类别">
            {{
              application.appType === 'reward'
                ? getRewardCategoryLabel(application.category) || '-'
                : getPunishCategoryLabel(application.category) || '-'
            }}
          </ElDescriptionsItem>
          <ElDescriptionsItem label="金额">
            {{ application.amount ? `¥${application.amount}` : '-' }}
          </ElDescriptionsItem>
          <ElDescriptionsItem label="生效日期">{{ application.effectDate || '-' }}</ElDescriptionsItem>
        </template>

        <!-- 离职申请 -->
        <template v-if="application.appType === 'resignation'">
          <ElDescriptionsItem label="入职日期">{{ application.entryDate || '-' }}</ElDescriptionsItem>
          <ElDescriptionsItem label="离职类型">
            {{ getResignTypeLabel(application.resignType) || '-' }}
          </ElDescriptionsItem>
          <ElDescriptionsItem label="最后工作日">{{ application.lastWorkDate || '-' }}</ElDescriptionsItem>
          <ElDescriptionsItem label="工作交接人">{{ application.handoverToName || '-' }}</ElDescriptionsItem>
        </template>

        <ElDescriptionsItem label="申请原因/备注" :span="2">{{ application.reason || '-' }}</ElDescriptionsItem>
        <ElDescriptionsItem label="审批意见" :span="2">{{ application.approveRemark || '-' }}</ElDescriptionsItem>
      </ElDescriptions>

      <!-- 审批进度 -->
      <div v-loading="recordsLoading" class="mt-20px">
        <div class="mb-10px font-bold">审批进度</div>
        <ElTimeline v-if="approvalRecords.length > 0">
          <ElTimelineItem
            v-for="(record, index) in approvalRecords"
            :key="`${record.createTime || ''}-${record.nodeName || ''}-${record.approverName || ''}-${index}`"
            :type="getTimelineType(record.status)"
            :timestamp="record.createTime"
          >
            <div class="flex items-center gap-8px">
              <span class="font-medium">{{ record.nodeName || '审批节点' }}</span>
              <ElTag :type="record.status === 1 ? 'success' : record.status === 2 ? 'danger' : 'info'" size="small">
                {{ getRecordStatusLabel(record.status) }}
              </ElTag>
              <span v-if="record.approverName" class="text-xs text-gray-500">审批人: {{ record.approverName }}</span>
            </div>
            <div v-if="record.comment" class="mt-2px text-xs text-gray-500">{{ record.comment }}</div>
          </ElTimelineItem>
        </ElTimeline>
        <ElEmpty v-else-if="!recordsLoading" description="暂无审批进度记录" :image-size="60" />
      </div>
    </template>
  </ElDrawer>
</template>
