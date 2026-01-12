<script setup lang="ts">
import { onMounted, ref, computed } from 'vue';
import { ElMessage } from 'element-plus';
import { fetchApprovalFlowList, createApprovalFlow, updateApprovalFlow, deleteApprovalFlow, updateApprovalFlowStatus, type ApprovalFlow, type ApprovalNode } from '@/service/api/approvalFlow';
import { fetchRoleList } from '@/service/api/system';

defineOptions({ name: 'ApprovalFlow' });

const loading = ref(false);
const data = ref<ApprovalFlow[]>([]);
const roleOptions = ref<Api.SystemManage.Role[]>([]);

const dialogVisible = ref(false);
const operateType = ref<'add' | 'edit'>('add');
const submitLoading = ref(false);
const formData = ref<ApprovalFlow>({ flowCode: '', flowName: '', flowType: '', autoPass: 0, nodes: [] });

// 是否免审批
const isAutoPass = computed(() => formData.value.autoPass === 1);

async function loadData() {
  loading.value = true;
  try {
    const res = await fetchApprovalFlowList();
    data.value = res.data || [];
  } finally { loading.value = false; }
}

async function loadRoles() {
  try {
    const res = await fetchRoleList();
    roleOptions.value = res.data || [];
  } catch (e) { console.error(e); }
}

onMounted(() => { loadData(); loadRoles(); });

function handleAdd() {
  operateType.value = 'add';
  formData.value = { flowCode: '', flowName: '', flowType: '', description: '', status: 1, autoPass: 0, nodes: [{ nodeName: '', nodeType: 1, approverType: 1 }] };
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
    ElMessage.success('删除成功');
    loadData();
  } catch { ElMessage.error('删除失败'); }
}

async function handleToggleStatus(row: ApprovalFlow) {
  const newStatus = row.status === 1 ? 0 : 1;
  try {
    await updateApprovalFlowStatus(row.id!, newStatus);
    ElMessage.success(newStatus === 1 ? '已启用' : '已停用');
    loadData();
  } catch { ElMessage.error('操作失败'); }
}

function addNode() {
  formData.value.nodes?.push({ nodeName: '', nodeType: 1, approverType: 1 });
}

function removeNode(index: number) {
  formData.value.nodes?.splice(index, 1);
}

async function handleSubmit() {
  if (!formData.value.flowCode || !formData.value.flowName || !formData.value.flowType) {
    ElMessage.warning('请填写必填项');
    return;
  }
  // 非免审批时需要审批节点
  if (!isAutoPass.value) {
    if (!formData.value.nodes?.length) {
      ElMessage.warning('请至少添加一个审批节点');
      return;
    }
    for (const node of formData.value.nodes) {
      if (!node.nodeName) {
        ElMessage.warning('请填写节点名称');
        return;
      }
      if (!node.roleId) {
        ElMessage.warning('请选择审批角色');
        return;
      }
    }
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
      ElMessage.success('新增成功');
    } else {
      await updateApprovalFlow(submitData);
      ElMessage.success('更新成功');
    }
    dialogVisible.value = false;
    loadData();
  } catch { ElMessage.error('保存失败'); }
  finally { submitLoading.value = false; }
}

function getRoleName(roleId?: number) {
  if (!roleId) return '';
  const role = roleOptions.value.find(r => r.id === roleId);
  return role?.roleName || '';
}

const flowTypeMap: Record<string, string> = {
  leave: '请假申请', overtime: '加班申请', business: '出差申请', makeup: '补卡申请', exchange: '换休申请',
  regularization: '转正申请', transfer: '调动申请', reward: '奖励申请', punish: '惩罚申请', resignation: '离职申请'
};

const statusMap: Record<number, { label: string; type: string }> = { 0: { label: '停用', type: 'info' }, 1: { label: '启用', type: 'success' } };
</script>

<template>
  <div class="min-h-500px flex-col-stretch gap-16px overflow-hidden lt-sm:overflow-auto">
    <ElCard>
      <template #header>
        <div class="flex items-center justify-between">
          <span>审批流程配置</span>
          <ElButton type="primary" @click="handleAdd"><template #icon><icon-ep-plus /></template>新增流程</ElButton>
        </div>
      </template>

      <ElTable v-loading="loading" :data="data" border stripe>
        <ElTableColumn type="index" label="序号" width="60" align="center" />
        <ElTableColumn prop="flowCode" label="流程编码" width="120" />
        <ElTableColumn prop="flowName" label="流程名称" width="150" />
        <ElTableColumn prop="flowType" label="流程类型" width="120">
          <template #default="{ row }">{{ flowTypeMap[row.flowType] || row.flowType }}</template>
        </ElTableColumn>
        <ElTableColumn prop="autoPass" label="免审批" width="80" align="center">
          <template #default="{ row }">
            <ElTag :type="row.autoPass === 1 ? 'success' : 'info'" size="small">
              {{ row.autoPass === 1 ? '是' : '否' }}
            </ElTag>
          </template>
        </ElTableColumn>
        <ElTableColumn label="审批节点" min-width="300">
          <template #default="{ row }">
            <template v-if="row.autoPass === 1">
              <ElTag type="success" size="small">免审批直接通过</ElTag>
            </template>
            <div v-else class="flex items-center gap-8px flex-wrap">
              <template v-for="(node, index) in row.nodes" :key="index">
                <ElTag size="small" :type="node.nodeType === 1 ? 'primary' : 'info'">
                  {{ node.nodeName }}
                  <span class="text-xs opacity-70">({{ node.roleName || '未指定角色' }})</span>
                </ElTag>
                <span v-if="index < row.nodes.length - 1" class="text-gray-400">→</span>
              </template>
            </div>
          </template>
        </ElTableColumn>
        <ElTableColumn prop="status" label="状态" width="80" align="center">
          <template #default="{ row }"><ElTag :type="statusMap[row.status]?.type as any">{{ statusMap[row.status]?.label }}</ElTag></template>
        </ElTableColumn>
        <ElTableColumn label="操作" width="180" align="center" fixed="right">
          <template #default="{ row }">
            <ElButton type="primary" link size="small" @click="handleEdit(row)">编辑</ElButton>
            <ElButton :type="row.status === 1 ? 'warning' : 'success'" link size="small" @click="handleToggleStatus(row)">
              {{ row.status === 1 ? '停用' : '启用' }}
            </ElButton>
            <ElPopconfirm title="确定删除该流程吗？" @confirm="handleDelete(row.id)">
              <template #reference><ElButton type="danger" link size="small">删除</ElButton></template>
            </ElPopconfirm>
          </template>
        </ElTableColumn>
      </ElTable>
    </ElCard>

    <ElDialog v-model="dialogVisible" :title="operateType === 'add' ? '新增审批流程' : '编辑审批流程'" width="700px">
      <ElForm label-width="100px" :model="formData">
        <ElRow :gutter="20">
          <ElCol :span="12">
            <ElFormItem label="流程编码" required>
              <ElInput v-model="formData.flowCode" placeholder="请输入流程编码" :disabled="operateType === 'edit'" />
            </ElFormItem>
          </ElCol>
          <ElCol :span="12">
            <ElFormItem label="流程名称" required>
              <ElInput v-model="formData.flowName" placeholder="请输入流程名称" />
            </ElFormItem>
          </ElCol>
        </ElRow>
        <ElRow :gutter="20">
          <ElCol :span="12">
            <ElFormItem label="流程类型" required>
              <ElSelect v-model="formData.flowType" placeholder="请选择流程类型" style="width: 100%">
                <ElOption v-for="(label, key) in flowTypeMap" :key="key" :label="label" :value="key" />
              </ElSelect>
            </ElFormItem>
          </ElCol>
          <ElCol :span="12">
            <ElFormItem label="状态">
              <ElSwitch v-model="formData.status" :active-value="1" :inactive-value="0" active-text="启用" inactive-text="停用" />
            </ElFormItem>
          </ElCol>
        </ElRow>
        <ElFormItem label="免审批">
          <ElSwitch v-model="formData.autoPass" :active-value="1" :inactive-value="0" active-text="直接通过" inactive-text="需要审批" />
          <span class="ml-12px text-gray-400 text-sm">开启后申请将自动通过，无需审批</span>
        </ElFormItem>
        <ElFormItem label="描述">
          <ElInput v-model="formData.description" type="textarea" :rows="2" placeholder="请输入描述" />
        </ElFormItem>
        
        <template v-if="!isAutoPass">
          <ElDivider content-position="left">审批节点</ElDivider>
          <div v-for="(node, index) in formData.nodes" :key="index" class="mb-16px p-16px bg-gray-50 rounded">
            <div class="flex items-center justify-between mb-8px">
              <span class="font-bold">节点 {{ index + 1 }}</span>
              <ElButton v-if="formData.nodes && formData.nodes.length > 1" type="danger" link size="small" @click="removeNode(index)">删除</ElButton>
            </div>
            <ElRow :gutter="16">
              <ElCol :span="8">
                <ElFormItem label="节点名称" label-width="80px" required>
                  <ElInput v-model="node.nodeName" placeholder="如：部门经理审批" />
                </ElFormItem>
              </ElCol>
              <ElCol :span="8">
                <ElFormItem label="节点类型" label-width="80px">
                  <ElSelect v-model="node.nodeType" style="width: 100%">
                    <ElOption label="审批" :value="1" />
                    <ElOption label="抄送" :value="2" />
                  </ElSelect>
                </ElFormItem>
              </ElCol>
              <ElCol :span="8">
                <ElFormItem label="审批角色" label-width="80px" required>
                  <ElSelect v-model="node.roleId" placeholder="请选择角色" style="width: 100%" filterable clearable>
                    <ElOption v-for="role in roleOptions" :key="role.id" :label="role.roleName" :value="role.id" />
                  </ElSelect>
                </ElFormItem>
              </ElCol>
            </ElRow>
          </div>
          <ElButton type="primary" plain @click="addNode"><template #icon><icon-ep-plus /></template>添加节点</ElButton>
        </template>
      </ElForm>
      <template #footer>
        <ElButton @click="dialogVisible = false">取消</ElButton>
        <ElButton type="primary" :loading="submitLoading" @click="handleSubmit">确定</ElButton>
      </template>
    </ElDialog>
  </div>
</template>
