<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import type { FormInstance, FormRules } from 'element-plus';
import { type AttCalendarRule, deleteCalendarRule, fetchCalendarRules, saveCalendarRule } from '@/service/api/calendar';
import { fetchCompanyList } from '@/service/api/organization';

defineOptions({ name: 'HolidayManage' });

const loading = ref(false);
const rules = ref<AttCalendarRule[]>([]);
const companies = ref<any[]>([]);

// 筛选条件
const filterYear = ref<number | undefined>(undefined);
const filterRuleType = ref<number | undefined>(undefined);

// 年份选项：规则日期中出现过的年份 + 今年（倒序）
const yearOptions = computed(() => {
  const years = new Set<number>();
  rules.value.forEach(r => {
    if (r.startDate) years.add(Number(r.startDate.slice(0, 4)));
  });
  years.add(new Date().getFullYear());
  return Array.from(years).filter(y => Number.isFinite(y)).sort((a, b) => b - a);
});

const filteredRules = computed(() =>
  rules.value.filter(rule => {
    if (filterRuleType.value !== undefined && rule.ruleType !== filterRuleType.value) return false;
    if (filterYear.value !== undefined) {
      // 休息规则无日期范围，不参与年份筛选（仅在未选择年份时显示）
      if (!rule.startDate) return false;
      const startYear = Number(rule.startDate.slice(0, 4));
      const endYear = rule.endDate ? Number(rule.endDate.slice(0, 4)) : startYear;
      if (!(filterYear.value >= startYear && filterYear.value <= endYear)) return false;
    }
    return true;
  })
);

const dialogVisible = ref(false);
const operateType = ref<'add' | 'edit'>('add');
const dialogForm = ref<AttCalendarRule>({ ruleType: 2 });
const formRef = ref<FormInstance>();
const submitLoading = ref(false);

const ruleTypeOptions = [
  { value: 1, label: '单休（周日）', tag: 'info' },
  { value: 2, label: '双休', tag: 'success' },
  { value: 3, label: '单休（周六）', tag: 'info' },
  { value: 4, label: '法定假日', tag: 'danger' },
  { value: 5, label: '调休上班', tag: 'warning' }
];

/** 特殊日期（法定假日/调休上班）需要日期范围与名称 */
function needDateRange(type: number) {
  return type >= 4;
}

/** 校验规则：特殊日期时日期范围与名称必填 */
function requiredWhenSpecial(_: unknown, value: unknown, callback: (error?: Error) => void) {
  if (needDateRange(dialogForm.value.ruleType) && !value) {
    callback(new Error('必填'));
  } else {
    callback();
  }
}

const formRules: FormRules = {
  companyId: [{ required: true, message: '请选择公司', trigger: 'change' }],
  ruleType: [{ required: true, message: '请选择规则类型', trigger: 'change' }],
  startDate: [{ validator: requiredWhenSpecial, trigger: 'change' }],
  endDate: [{ validator: requiredWhenSpecial, trigger: 'change' }],
  ruleName: [{ validator: requiredWhenSpecial, trigger: 'blur' }]
};

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

function handleReset() {
  filterYear.value = undefined;
  filterRuleType.value = undefined;
}

function handleAdd() {
  operateType.value = 'add';
  dialogForm.value = { ruleType: 2 };
  dialogVisible.value = true;
}

function handleEdit(row: AttCalendarRule) {
  operateType.value = 'edit';
  dialogForm.value = { ...row };
  dialogVisible.value = true;
}

async function handleSubmit() {
  if (!formRef.value) return;
  const valid = await formRef.value.validate().catch(() => false);
  if (!valid) return;
  submitLoading.value = true;
  try {
    // saveCalendarRule 为 upsert：带 id 更新，不带 id 新增
    await saveCalendarRule(dialogForm.value);
    ElMessage.success('保存成功');
    dialogVisible.value = false;
    loadRules();
  } catch {
    ElMessage.error('保存失败');
  } finally {
    submitLoading.value = false;
  }
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
</script>

<template>
  <div class="min-h-500px flex-col-stretch gap-16px overflow-hidden lt-sm:overflow-auto">
    <ElCard class="flex-1">
      <template #header>
        <div class="flex flex-wrap items-center justify-between gap-12px">
          <div class="flex items-center gap-12px">
            <span>考勤日历规则</span>
            <ElSelect v-model="filterYear" placeholder="年份" clearable style="width: 110px">
              <ElOption v-for="y in yearOptions" :key="y" :label="`${y}年`" :value="y" />
            </ElSelect>
            <ElSelect v-model="filterRuleType" placeholder="规则类型" clearable style="width: 150px">
              <ElOption v-for="opt in ruleTypeOptions" :key="opt.value" :label="opt.label" :value="opt.value" />
            </ElSelect>
            <ElButton v-if="filterYear !== undefined || filterRuleType !== undefined" link type="primary" @click="handleReset">
              清除筛选
            </ElButton>
          </div>
          <ElButton type="primary" @click="handleAdd">
            <template #icon><icon-ep-plus /></template>
            添加规则
          </ElButton>
        </div>
      </template>

      <ElTable v-loading="loading" :data="filteredRules" border stripe>
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
        <ElTableColumn label="操作" width="120" align="center">
          <template #default="{ row }">
            <ElButton type="primary" link size="small" @click="handleEdit(row)">编辑</ElButton>
            <ElButton type="danger" link size="small" @click="handleDelete(row)">删除</ElButton>
          </template>
        </ElTableColumn>
      </ElTable>

      <div class="mt-12px text-sm text-gray-400">
        说明：休息规则（单休/双休）每个公司只能设置一条；法定假日和调休上班可设置多条。
      </div>
    </ElCard>

    <!-- 添加/编辑弹窗 -->
    <ElDialog v-model="dialogVisible" :title="operateType === 'add' ? '添加规则' : '编辑规则'" width="500px">
      <ElForm ref="formRef" label-width="80px" :model="dialogForm" :rules="formRules">
        <ElFormItem label="公司" prop="companyId">
          <ElSelect v-model="dialogForm.companyId" placeholder="请选择公司" style="width: 100%">
            <ElOption v-for="c in companies" :key="c.id" :label="c.unitName" :value="c.id" />
          </ElSelect>
        </ElFormItem>
        <ElFormItem label="规则类型" prop="ruleType">
          <ElSelect
            v-model="dialogForm.ruleType"
            placeholder="请选择类型"
            style="width: 100%"
            :disabled="operateType === 'edit'"
          >
            <ElOption v-for="opt in ruleTypeOptions" :key="opt.value" :label="opt.label" :value="opt.value" />
          </ElSelect>
        </ElFormItem>
        <ElFormItem v-if="needDateRange(dialogForm.ruleType)" label="日期范围" prop="startDate">
          <div class="flex w-full items-center">
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
          </div>
        </ElFormItem>
        <ElFormItem v-if="needDateRange(dialogForm.ruleType)" label="名称" prop="ruleName">
          <ElInput v-model="dialogForm.ruleName" placeholder="如：国庆节" />
        </ElFormItem>
        <ElFormItem label="备注">
          <ElInput v-model="dialogForm.remark" placeholder="备注" />
        </ElFormItem>
      </ElForm>
      <template #footer>
        <ElButton @click="dialogVisible = false">取消</ElButton>
        <ElButton type="primary" :loading="submitLoading" @click="handleSubmit">确定</ElButton>
      </template>
    </ElDialog>
  </div>
</template>
