<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue';
import { ElMessage } from 'element-plus';
import type { FormInstance, FormRules } from 'element-plus';
import { appTypeMap } from '@/constants/application';
import {
  type ApprovalFlow,
  createApprovalFlow,
  deleteApprovalFlow,
  fetchApprovalFlowList,
  updateApprovalFlow,
  updateApprovalFlowStatus
} from '@/service/api/approvalFlow';
import { fetchRoleList } from '@/service/api/system';
import { $t } from '@/locales';

defineOptions({ name: 'ApprovalFlow' });

const loading = ref(false);
const data = ref<ApprovalFlow[]>([]);
const roleOptions = ref<Api.System.Role[]>([]);

// 搜索条件（列表接口不支持服务端筛选，前端对全量列表过滤）
const searchFlowType = ref<string | undefined>(undefined);
const filteredData = computed(() =>
  searchFlowType.value ? data.value.filter(item => item.flowType === searchFlowType.value) : data.value
);

// 列表接口不支持服务端分页，这里做前端分页，避免一次渲染过多行
const pagination = ref({ current: 1, pageSize: 20 });
const pagedData = computed(() => {
  const start = (pagination.value.current - 1) * pagination.value.pageSize;
  return filteredData.value.slice(start, start + pagination.value.pageSize);
});

watch(
  () => [filteredData.value.length, pagination.value.pageSize] as const,
  ([len, size]) => {
    const maxPage = Math.max(1, Math.ceil(len / size));
    if (pagination.value.current > maxPage) {
      pagination.value.current = maxPage;
    }
  }
);

const dialogVisible = ref(false);
const operateType = ref<'add' | 'edit'>('add');
const submitLoading = ref(false);
const formData = ref<ApprovalFlow>({ flowCode: '', flowName: '', flowType: '', autoPass: 0, nodes: [] });
const formRef = ref<FormInstance>();

// 是否免审批
const isAutoPass = computed(() => formData.value.autoPass === 1);

const baseFormRules: FormRules = {
  flowCode: [{ required: true, message: $t('approval.flow.pleaseEnterFlowCode'), trigger: 'blur' }],
  flowName: [{ required: true, message: $t('approval.flow.pleaseEnterFlowName'), trigger: 'blur' }],
  flowType: [{ required: true, message: $t('approval.flow.pleaseSelectFlowType'), trigger: 'change' }]
};

/** 审批节点字段校验规则（非免审批时必填） */
function nodeRequired(message: string): FormRules[string] {
  return [
    {
      validator: (_rule, value, callback) => {
        if (!isAutoPass.value && !value) {
          callback(new Error(message));
        } else {
          callback();
        }
      },
      trigger: ['blur', 'change']
    }
  ];
}

async function loadData() {
  loading.value = true;
  try {
    const { data: resData, error } = await fetchApprovalFlowList();
    if (!error && resData) {
      data.value = resData;
    } else {
      data.value = [];
    }
  } finally {
    loading.value = false;
  }
}

async function loadRoles() {
  try {
    const { data: resData, error } = await fetchRoleList();
    if (!error && resData) {
      roleOptions.value = resData;
    }
  } catch {
    // 请求层已统一弹错
  }
}

onMounted(() => {
  loadData();
  loadRoles();
});

function handleAdd() {
  operateType.value = 'add';
  formData.value = {
    flowCode: '',
    flowName: '',
    flowType: '',
    description: '',
    status: 1,
    autoPass: 0,
    nodes: [{ nodeName: '', nodeType: 1, approverType: 1 }]
  };
  dialogVisible.value = true;
}

function handleEdit(row: ApprovalFlow) {
  operateType.value = 'edit';
  formData.value = { ...row, nodes: row.nodes?.map(n => ({ ...n })) || [] };
  dialogVisible.value = true;
}

async function handleDelete(id: number) {
  try {
    await deleteApprovalFlow(id);
    ElMessage.success($t('common.deleteSuccess'));
    loadData();
  } catch {
    // 请求层已统一弹错
  }
}

async function handleToggleStatus(row: ApprovalFlow) {
  const newStatus = row.status === 1 ? 0 : 1;
  try {
    await updateApprovalFlowStatus(row.id!, newStatus);
    ElMessage.success(newStatus === 1 ? $t('approval.flow.enabled') : $t('approval.flow.disabled'));
    loadData();
  } catch {
    // 请求层已统一弹错
  }
}

function addNode() {
  formData.value.nodes?.push({ nodeName: '', nodeType: 1, approverType: 1 });
}

function removeNode(index: number) {
  formData.value.nodes?.splice(index, 1);
}

async function handleSubmit() {
  if (!formRef.value) return;
  const valid = await formRef.value.validate().catch(() => false);
  if (!valid) return;
  // 非免审批时需要至少一个审批节点（列表级校验，表单 rules 无法覆盖）
  if (!isAutoPass.value && !formData.value.nodes?.length) {
    ElMessage.warning($t('approval.flow.pleaseAddAtLeastOneApprovalNode'));
    return;
  }
  submitLoading.value = true;
  try {
    // 免审批时清空节点
    const submitData = { ...formData.value };
    if (isAutoPass.value) {
      submitData.nodes = [];
    }
    if (operateType.value === 'add') {
      await createApprovalFlow(submitData);
      ElMessage.success($t('common.addSuccess'));
    } else {
      await updateApprovalFlow(submitData);
      ElMessage.success($t('common.updateSuccess'));
    }
    dialogVisible.value = false;
    loadData();
  } catch {
    // 请求层已统一弹错
  } finally {
    submitLoading.value = false;
  }
}

const flowTypeMap = appTypeMap;

const statusMap: Record<number, { label: string; type: string }> = {
  0: { label: $t('common.deactivate'), type: 'info' },
  1: { label: $t('common.enable'), type: 'success' }
};
</script>

<template>
  <div class="approval-flow-page">
    <ElCard shadow="never" class="table-card">
      <template #header>
        <div class="flex flex-wrap items-center justify-between gap-12px">
          <span>{{ $t('approval.flow.approvalFlowConfig') }}</span>
          <div class="flex items-center gap-12px">
            <ElSelect
              v-model="searchFlowType"
              :placeholder="$t('approval.flow.filterByFlowType')"
              clearable
              filterable
              style="width: 180px"
              @change="pagination.current = 1"
            >
              <ElOption v-for="(label, key) in flowTypeMap" :key="key" :label="label" :value="key" />
            </ElSelect>
            <ElButton v-permission="'approval:flow:add'" type="primary" @click="handleAdd">
              <template #icon><icon-ep-plus /></template>
              {{ $t('approval.flow.newFlow') }}
            </ElButton>
          </div>
        </div>
      </template>

      <div class="table-wrapper">
        <ElTable v-loading="loading" :data="pagedData" border stripe height="100%">
          <ElTableColumn type="index" :label="$t('common.index2')" width="60" align="center" />
          <ElTableColumn prop="flowCode" :label="$t('approval.flow.flowCode')" width="120" />
          <ElTableColumn prop="flowName" :label="$t('approval.flow.flowName')" width="150" />
          <ElTableColumn prop="flowType" :label="$t('approval.flow.flowType')" width="120">
            <template #default="{ row }">{{ flowTypeMap[row.flowType] || row.flowType }}</template>
          </ElTableColumn>
          <ElTableColumn prop="autoPass" :label="$t('approval.flow.noApproval')" width="80" align="center">
            <template #default="{ row }">
              <ElTag :type="row.autoPass === 1 ? 'success' : 'info'" size="small">
                {{ row.autoPass === 1 ? $t('common.yesOrNo.yes') : $t('common.yesOrNo.no') }}
              </ElTag>
            </template>
          </ElTableColumn>
          <ElTableColumn :label="$t('approval.flow.approvalNode')" min-width="300">
            <template #default="{ row }">
              <template v-if="row.autoPass === 1">
                <ElTag type="success" size="small">{{ $t('approval.flow.autoApproveWithoutReview') }}</ElTag>
              </template>
              <div v-else class="flex flex-wrap items-center gap-8px">
                <template v-for="(node, index) in row.nodes" :key="index">
                  <ElTag size="small" :type="node.nodeType === 1 ? 'primary' : 'info'">
                    {{ node.nodeName }}
                    <span class="text-xs opacity-70">({{ node.roleName || $t('approval.flow.noRoleSpecified') }})</span>
                  </ElTag>
                  <span v-if="index < row.nodes.length - 1" class="text-gray-400">→</span>
                </template>
              </div>
            </template>
          </ElTableColumn>
          <ElTableColumn prop="status" :label="$t('common.status')" width="80" align="center">
            <template #default="{ row }">
              <ElTag :type="statusMap[row.status]?.type as any">{{ statusMap[row.status]?.label }}</ElTag>
            </template>
          </ElTableColumn>
          <ElTableColumn :label="$t('common.action')" width="180" align="center" fixed="right">
            <template #default="{ row }">
              <ElButton v-permission="'approval:flow:edit'" type="primary" link size="small" @click="handleEdit(row)">{{ $t('common.edit') }}</ElButton>
              <ElButton
                v-permission="'approval:flow:edit'"
                :type="row.status === 1 ? 'warning' : 'success'"
                link
                size="small"
                @click="handleToggleStatus(row)"
              >
                {{ row.status === 1 ? $t('common.deactivate') : $t('common.enable') }}
              </ElButton>
              <ElPopconfirm :title="$t('approval.flow.areYouSureYouWantToDeleteThisFlow')" @confirm="handleDelete(row.id)">
                <template #reference><ElButton v-permission="'approval:flow:delete'" type="danger" link size="small">{{ $t('common.delete') }}</ElButton></template>
              </ElPopconfirm>
            </template>
          </ElTableColumn>
        </ElTable>
      </div>

      <div class="mt-12px flex justify-end">
        <ElPagination
          v-model:current-page="pagination.current"
          v-model:page-size="pagination.pageSize"
          :total="filteredData.length"
          :page-sizes="[20, 50, 100]"
          layout="total, sizes, prev, pager, next"
        />
      </div>
    </ElCard>

    <ElDialog v-model="dialogVisible" :title="operateType === 'add' ? $t('approval.flow.newApprovalFlow') : $t('approval.flow.editApprovalFlow')" width="700px">
      <ElForm ref="formRef" label-width="100px" :model="formData" :rules="baseFormRules">
        <ElRow :gutter="20">
          <ElCol :span="12">
            <ElFormItem :label="$t('approval.flow.flowCode')" prop="flowCode">
              <ElInput v-model="formData.flowCode" :placeholder="$t('approval.flow.pleaseEnterFlowCode')" :disabled="operateType === 'edit'" />
            </ElFormItem>
          </ElCol>
          <ElCol :span="12">
            <ElFormItem :label="$t('approval.flow.flowName')" prop="flowName">
              <ElInput v-model="formData.flowName" :placeholder="$t('approval.flow.pleaseEnterFlowName')" />
            </ElFormItem>
          </ElCol>
        </ElRow>
        <ElRow :gutter="20">
          <ElCol :span="12">
            <ElFormItem :label="$t('approval.flow.flowType')" prop="flowType">
              <ElSelect v-model="formData.flowType" :placeholder="$t('approval.flow.pleaseSelectFlowType')" style="width: 100%">
                <ElOption v-for="(label, key) in flowTypeMap" :key="key" :label="label" :value="key" />
              </ElSelect>
            </ElFormItem>
          </ElCol>
          <ElCol :span="12">
            <ElFormItem :label="$t('common.status')">
              <ElSwitch
                v-model="formData.status"
                :active-value="1"
                :inactive-value="0"
                :active-text="$t('common.enable')"
                :inactive-text="$t('common.deactivate')"
              />
            </ElFormItem>
          </ElCol>
        </ElRow>
        <ElFormItem :label="$t('approval.flow.noApproval')">
          <ElSwitch
            v-model="formData.autoPass"
            :active-value="1"
            :inactive-value="0"
            :active-text="$t('approval.flow.directApproval')"
            :inactive-text="$t('approval.flow.approvalRequired')"
          />
          <span class="ml-12px text-sm text-gray-400">{{ $t('approval.flow.whenEnabledApplicationsWillBeAutoApprovedWithoutReview') }}</span>
        </ElFormItem>
        <ElFormItem :label="$t('common.description')">
          <ElInput v-model="formData.description" type="textarea" :rows="2" :placeholder="$t('approval.flow.pleaseEnterDescription')" />
        </ElFormItem>

        <template v-if="!isAutoPass">
          <ElDivider content-position="left">{{ $t('approval.flow.approvalNode') }}</ElDivider>
          <div v-for="(node, index) in formData.nodes" :key="index" class="mb-16px rounded bg-gray-50 p-16px">
            <div class="mb-8px flex items-center justify-between">
              <span class="font-bold">{{ $t('approval.flow.node') }} {{ index + 1 }}</span>
              <ElButton
                v-if="formData.nodes && formData.nodes.length > 1"
                type="danger"
                link
                size="small"
                @click="removeNode(index)"
              >
                {{ $t('common.delete') }}
              </ElButton>
            </div>
            <ElRow :gutter="16">
              <ElCol :span="8">
                <ElFormItem
                  :label="$t('approval.flow.nodeName')"
                  label-width="80px"
                  :prop="`nodes.${index}.nodeName`"
                  :rules="nodeRequired($t('approval.flow.pleaseEnterNodeName'))"
                >
                  <ElInput v-model="node.nodeName" :placeholder="$t('approval.flow.eGDepartmentManagerApproval')" />
                </ElFormItem>
              </ElCol>
              <ElCol :span="8">
                <ElFormItem :label="$t('approval.flow.nodeType')" label-width="80px">
                  <ElSelect v-model="node.nodeType" style="width: 100%">
                    <ElOption :label="$t('common.approval')" :value="1" />
                    <ElOption :label="$t('approval.flow.cc')" :value="2" />
                  </ElSelect>
                </ElFormItem>
              </ElCol>
              <ElCol :span="8">
                <ElFormItem
                  :label="$t('approval.flow.approvalRole')"
                  label-width="80px"
                  :prop="`nodes.${index}.roleId`"
                  :rules="nodeRequired($t('approval.flow.pleaseSelectApprovalRole'))"
                >
                  <ElSelect v-model="node.roleId" :placeholder="$t('approval.flow.pleaseSelectARole')" style="width: 100%" filterable clearable>
                    <ElOption v-for="role in roleOptions" :key="role.id" :label="role.roleName" :value="role.id" />
                  </ElSelect>
                </ElFormItem>
              </ElCol>
            </ElRow>
          </div>
          <ElButton type="primary" plain @click="addNode">
            <template #icon><icon-ep-plus /></template>
            {{ $t('approval.flow.addNode') }}
          </ElButton>
        </template>
      </ElForm>
      <template #footer>
        <ElButton @click="dialogVisible = false">{{ $t('common.cancel') }}</ElButton>
        <ElButton type="primary" :loading="submitLoading" @click="handleSubmit">{{ $t('common.ok') }}</ElButton>
      </template>
    </ElDialog>
  </div>
</template>

<style scoped>
.approval-flow-page {
  display: flex;
  flex-direction: column;
  height: calc(100vh - 120px);
  overflow: hidden;
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
  min-height: 0;
  overflow: auto;
}
</style>
