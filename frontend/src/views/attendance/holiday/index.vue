<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { fetchCalendarRules, saveCalendarRule, deleteCalendarRule, type AttCalendarRule } from '@/service/api/calendar';
import { fetchCompanyList } from '@/service/api/organization';

defineOptions({ name: 'HolidayManage' });

const loading = ref(false);
const rules = ref<AttCalendarRule[]>([]);
const companies = ref<any[]>([]);

const dialogVisible = ref(false);
const dialogForm = ref<AttCalendarRule>({ ruleType: 2 });

const ruleTypeOptions = [
  { value: 1, label: '单休（周日）', tag: 'info' },
  { value: 2, label: '双休', tag: 'success' },
  { value: 3, label: '单休（周六）', tag: 'info' },
  { value: 4, label: '法定假日', tag: 'danger' },
  { value: 5, label: '调休上班', tag: 'warning' }
];

async function loadCompanies() {
  const res = await fetchCompanyList();
  companies.value = res.data || [];
}

async function loadRules() {
  loading.value = true;
  try {
    const res = await fetchCalendarRules();
    rules.value = res.data || [];
  } finally {
    loading.value = false;
  }
}

onMounted(() => {
  loadCompanies();
  loadRules();
});

function handleAdd() {
  dialogForm.value = { ruleType: 2 };
  dialogVisible.value = true;
}

async function handleSubmit() {
  if (!dialogForm.value.companyId) {
    ElMessage.warning('请选择公司');
    return;
  }
  // 特殊日期需要日期范围
  if (dialogForm.value.ruleType >= 4) {
    if (!dialogForm.value.startDate || !dialogForm.value.endDate) {
      ElMessage.warning('请选择日期范围');
      return;
    }
    if (!dialogForm.value.ruleName) {
      ElMessage.warning('请输入名称');
      return;
    }
  }
  await saveCalendarRule(dialogForm.value);
  ElMessage.success('保存成功');
  dialogVisible.value = false;
  loadRules();
}

async function handleDelete(row: AttCalendarRule) {
  await ElMessageBox.confirm(`确定删除该规则吗？`, '提示');
  await deleteCalendarRule(row.id!);
  ElMessage.success('删除成功');
  loadRules();
}

function getRuleTypeLabel(type: number) {
  return ruleTypeOptions.find(o => o.value === type)?.label || '';
}

function getRuleTypeTag(type: number) {
  return ruleTypeOptions.find(o => o.value === type)?.tag || 'info';
}

function needDateRange(type: number) {
  return type >= 4;
}
</script>

<template>
  <div class="min-h-500px flex-col-stretch gap-16px overflow-hidden lt-sm:overflow-auto">
    <ElCard class="flex-1">
      <template #header>
        <div class="flex items-center justify-between">
          <span>考勤日历规则</span>
          <ElButton type="primary" @click="handleAdd">
            <template #icon><icon-ep-plus /></template>
            添加规则
          </ElButton>
        </div>
      </template>

      <ElTable v-loading="loading" :data="rules" border stripe>
        <ElTableColumn prop="companyName" label="公司" min-width="150" />
        <ElTableColumn prop="ruleType" label="规则类型" width="130" align="center">
          <template #default="{ row }">
            <ElTag :type="getRuleTypeTag(row.ruleType) as any">{{ getRuleTypeLabel(row.ruleType) }}</ElTag>
          </template>
        </ElTableColumn>
        <ElTableColumn prop="ruleName" label="名称" width="120" />
        <ElTableColumn label="日期范围" width="200">
          <template #default="{ row }">
            <span v-if="row.startDate">{{ row.startDate }} ~ {{ row.endDate }}</span>
            <span v-else class="text-gray-400">-</span>
          </template>
        </ElTableColumn>
        <ElTableColumn prop="remark" label="备注" min-width="150" />
        <ElTableColumn label="操作" width="80" align="center">
          <template #default="{ row }">
            <ElButton type="danger" link size="small" @click="handleDelete(row)">删除</ElButton>
          </template>
        </ElTableColumn>
      </ElTable>

      <div class="mt-12px text-gray-400 text-sm">
        说明：休息规则（单休/双休）每个公司只能设置一条；法定假日和调休上班可设置多条。
      </div>
    </ElCard>

    <!-- 添加弹窗 -->
    <ElDialog v-model="dialogVisible" title="添加规则" width="500px">
      <ElForm label-width="80px" :model="dialogForm">
        <ElFormItem label="公司" required>
          <ElSelect v-model="dialogForm.companyId" placeholder="请选择公司" style="width: 100%">
            <ElOption v-for="c in companies" :key="c.id" :label="c.companyName" :value="c.id" />
          </ElSelect>
        </ElFormItem>
        <ElFormItem label="规则类型" required>
          <ElSelect v-model="dialogForm.ruleType" placeholder="请选择类型" style="width: 100%">
            <ElOption v-for="opt in ruleTypeOptions" :key="opt.value" :label="opt.label" :value="opt.value" />
          </ElSelect>
        </ElFormItem>
        <ElFormItem v-if="needDateRange(dialogForm.ruleType)" label="日期范围" required>
          <ElDatePicker
            v-model="dialogForm.startDate"
            type="date"
            placeholder="开始日期"
            value-format="YYYY-MM-DD"
            style="width: 45%"
          />
          <span class="mx-8px">~</span>
          <ElDatePicker
            v-model="dialogForm.endDate"
            type="date"
            placeholder="结束日期"
            value-format="YYYY-MM-DD"
            style="width: 45%"
          />
        </ElFormItem>
        <ElFormItem v-if="needDateRange(dialogForm.ruleType)" label="名称" required>
          <ElInput v-model="dialogForm.ruleName" placeholder="如：国庆节" />
        </ElFormItem>
        <ElFormItem label="备注">
          <ElInput v-model="dialogForm.remark" placeholder="备注" />
        </ElFormItem>
      </ElForm>
      <template #footer>
        <ElButton @click="dialogVisible = false">取消</ElButton>
        <ElButton type="primary" @click="handleSubmit">确定</ElButton>
      </template>
    </ElDialog>
  </div>
</template>
