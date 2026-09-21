<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import type { FormInstance, FormRules } from 'element-plus';
import { type AttCalendarRule, deleteCalendarRule, fetchCalendarRules, saveCalendarRule } from '@/service/api/calendar';
import { fetchCompanyList } from '@/service/api/organization';
import { $t } from '@/locales';

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
  return Array.from(years)
    .filter(y => Number.isFinite(y))
    .sort((a, b) => b - a);
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

// 规则接口（/calendar/rules）不支持服务端分页，这里做前端分页，避免一次渲染过多行
const pagination = ref({ current: 1, pageSize: 20 });
const pagedRules = computed(() => {
  const start = (pagination.value.current - 1) * pagination.value.pageSize;
  return filteredRules.value.slice(start, start + pagination.value.pageSize);
});

// 过滤/删除导致总页数变少时，回收当前页码
watch(
  () => filteredRules.value.length,
  len => {
    const maxPage = Math.max(1, Math.ceil(len / pagination.value.pageSize));
    if (pagination.value.current > maxPage) {
      pagination.value.current = maxPage;
    }
  }
);

function handleFilterChange() {
  pagination.value.current = 1;
}

const dialogVisible = ref(false);
const operateType = ref<'add' | 'edit'>('add');
const dialogForm = ref<AttCalendarRule>({ ruleType: 2 });
const formRef = ref<FormInstance>();
const submitLoading = ref(false);

const ruleTypeOptions = [
  { value: 1, label: $t('attendance.holiday.singleRestDaySunday'), tag: 'info' },
  { value: 2, label: $t('attendance.holiday.twoDayWeekend'), tag: 'success' },
  { value: 3, label: $t('attendance.holiday.singleRestDaySaturday'), tag: 'info' },
  { value: 4, label: $t('attendance.holiday.statutoryHoliday'), tag: 'danger' },
  { value: 5, label: $t('attendance.holiday.adjustedWorkday'), tag: 'warning' }
];

/** 特殊日期（法定假日/调休上班）需要日期范围与名称 */
function needDateRange(type: number) {
  return type >= 4;
}

/** 校验规则：特殊日期时日期范围与名称必填 */
function requiredWhenSpecial(_: unknown, value: unknown, callback: (error?: Error) => void) {
  if (needDateRange(dialogForm.value.ruleType) && !value) {
    callback(new Error($t('common.required')));
  } else {
    callback();
  }
}

const formRules: FormRules = {
  companyId: [{ required: true, message: $t('common.pleaseSelectCompany'), trigger: 'change' }],
  ruleType: [{ required: true, message: $t('attendance.holiday.pleaseSelectRuleType'), trigger: 'change' }],
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
    ElMessage.success($t('common.saveSuccess'));
    dialogVisible.value = false;
    loadRules();
  } catch {
    // 请求层已统一弹错
  } finally {
    submitLoading.value = false;
  }
}

async function handleDelete(row: AttCalendarRule) {
  try {
    await ElMessageBox.confirm($t('attendance.holiday.areYouSureYouWantToDeleteThisRule'), $t('common.tip'));
  } catch {
    // 用户取消删除
    return;
  }
  try {
    await deleteCalendarRule(row.id!);
    ElMessage.success($t('common.deleteSuccess'));
    loadRules();
  } catch {
    // 请求层已统一弹错
  }
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
            <span>{{ $t('attendance.holiday.attendanceCalendarRules') }}</span>
            <ElSelect
              v-model="filterYear"
              :placeholder="$t('common.year')"
              clearable
              style="width: 110px"
              @change="handleFilterChange"
            >
              <ElOption
                v-for="y in yearOptions"
                :key="y"
                :label="$t('attendance.holiday.year', { year: y })"
                :value="y"
              />
            </ElSelect>
            <ElSelect
              v-model="filterRuleType"
              :placeholder="$t('attendance.holiday.ruleType')"
              clearable
              style="width: 150px"
              @change="handleFilterChange"
            >
              <ElOption v-for="opt in ruleTypeOptions" :key="opt.value" :label="opt.label" :value="opt.value" />
            </ElSelect>
            <ElButton
              v-if="filterYear !== undefined || filterRuleType !== undefined"
              link
              type="primary"
              @click="handleReset"
            >
              {{ $t('attendance.holiday.clearFilters') }}
            </ElButton>
          </div>
          <ElButton v-permission="'attendance:holiday:add'" type="primary" @click="handleAdd">
            <template #icon><icon-ep-plus /></template>
            {{ $t('attendance.holiday.addRule') }}
          </ElButton>
        </div>
      </template>

      <ElTable v-loading="loading" :data="pagedRules" border stripe>
        <ElTableColumn prop="companyName" :label="$t('common.company')" min-width="150" />
        <ElTableColumn prop="ruleType" :label="$t('attendance.holiday.ruleType')" width="130" align="center">
          <template #default="{ row }">
            <ElTag :type="getRuleTypeTag(row.ruleType) as any">{{ getRuleTypeLabel(row.ruleType) }}</ElTag>
          </template>
        </ElTableColumn>
        <ElTableColumn prop="ruleName" :label="$t('common.name')" width="120" show-overflow-tooltip />
        <ElTableColumn :label="$t('common.dateRange')" width="200">
          <template #default="{ row }">
            <span v-if="row.startDate">{{ row.startDate }} ~ {{ row.endDate }}</span>
            <span v-else class="text-gray-400">-</span>
          </template>
        </ElTableColumn>
        <ElTableColumn prop="remark" :label="$t('common.remark')" min-width="150" show-overflow-tooltip />
        <ElTableColumn :label="$t('common.action')" width="120" align="center">
          <template #default="{ row }">
            <ElButton
              v-permission="'attendance:holiday:edit'"
              type="primary"
              link
              size="small"
              @click="handleEdit(row)"
            >
              {{ $t('common.edit') }}
            </ElButton>
            <ElButton
              v-permission="'attendance:holiday:delete'"
              type="danger"
              link
              size="small"
              @click="handleDelete(row)"
            >
              {{ $t('common.delete') }}
            </ElButton>
          </template>
        </ElTableColumn>
      </ElTable>

      <div class="mt-12px flex items-center justify-between">
        <span class="text-sm text-gray-400">
          {{
            $t(
              'attendance.holiday.noteEachCompanyCanHaveOnlyOneRestRuleSingleTwoDayWeekendStatutoryHolidaysAndAdjustedWorkdaysCanHaveMultipleEntries'
            )
          }}
        </span>
        <ElPagination
          v-model:current-page="pagination.current"
          v-model:page-size="pagination.pageSize"
          :total="filteredRules.length"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next"
        />
      </div>
    </ElCard>

    <!-- 添加/编辑弹窗 -->
    <ElDialog
      v-model="dialogVisible"
      :title="operateType === 'add' ? $t('attendance.holiday.addRule') : $t('attendance.holiday.editRule')"
      width="500px"
    >
      <ElForm ref="formRef" label-width="80px" :model="dialogForm" :rules="formRules">
        <ElFormItem :label="$t('common.company')" prop="companyId">
          <ElSelect v-model="dialogForm.companyId" :placeholder="$t('common.pleaseSelectCompany')" style="width: 100%">
            <ElOption v-for="c in companies" :key="c.id" :label="c.unitName" :value="c.id" />
          </ElSelect>
        </ElFormItem>
        <ElFormItem :label="$t('attendance.holiday.ruleType')" prop="ruleType">
          <ElSelect
            v-model="dialogForm.ruleType"
            :placeholder="$t('common.pleaseSelectType')"
            style="width: 100%"
            :disabled="operateType === 'edit'"
          >
            <ElOption v-for="opt in ruleTypeOptions" :key="opt.value" :label="opt.label" :value="opt.value" />
          </ElSelect>
        </ElFormItem>
        <ElFormItem v-if="needDateRange(dialogForm.ruleType)" :label="$t('common.dateRange')" prop="startDate">
          <div class="w-full flex items-center">
            <ElDatePicker
              v-model="dialogForm.startDate"
              type="date"
              :placeholder="$t('common.startDate')"
              value-format="YYYY-MM-DD"
              style="width: 45%"
            />
            <span class="mx-8px">~</span>
            <ElDatePicker
              v-model="dialogForm.endDate"
              type="date"
              :placeholder="$t('common.endDate')"
              value-format="YYYY-MM-DD"
              style="width: 45%"
            />
          </div>
        </ElFormItem>
        <ElFormItem v-if="needDateRange(dialogForm.ruleType)" :label="$t('common.name')" prop="ruleName">
          <ElInput v-model="dialogForm.ruleName" :placeholder="$t('attendance.holiday.eGNationalDay')" />
        </ElFormItem>
        <ElFormItem :label="$t('common.remark')">
          <ElInput v-model="dialogForm.remark" :placeholder="$t('common.remark')" />
        </ElFormItem>
      </ElForm>
      <template #footer>
        <ElButton @click="dialogVisible = false">{{ $t('common.cancel') }}</ElButton>
        <ElButton type="primary" :loading="submitLoading" @click="handleSubmit">{{ $t('common.ok') }}</ElButton>
      </template>
    </ElDialog>
  </div>
</template>
