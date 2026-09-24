<script setup lang="tsx">
import { onMounted, ref } from 'vue';
import { ElMessage, ElPopconfirm } from 'element-plus';
import type { FormInstance, FormRules } from 'element-plus';
import type { TagType } from '@/constants/common';
import { type SalaryItemDef, deleteSalaryItem, fetchSalaryItems, saveSalaryItem } from '@/service/api/salary';
import { $t } from '@/locales';

defineOptions({ name: 'SalaryItem' });

const loading = ref(false);
const data = ref<SalaryItemDef[]>([]);

const directionOptions = [
  { value: 1, label: () => $t('salary.common.income') },
  { value: 2, label: () => $t('salary.common.deduction') }
];
const valueTypeOptions = [
  { value: 1, label: () => $t('salary.common.valueTypeFixed') },
  { value: 2, label: () => $t('salary.common.valueTypeRatio') },
  { value: 3, label: () => $t('salary.common.valueTypeAttendance') },
  { value: 4, label: () => $t('salary.common.valueTypeManual') }
];
const attRuleOptions = [
  { value: 'LATE_TIMES', label: () => $t('salary.item.ruleLateTimes') },
  { value: 'LATE_MINUTES', label: () => $t('salary.item.ruleLateMinutes') },
  { value: 'ABSENT_DAYS', label: () => $t('salary.item.ruleAbsentDays') },
  { value: 'PERSONAL_LEAVE_HOURS', label: () => $t('salary.item.rulePersonalLeaveHours') },
  { value: 'OVERTIME_HOURS', label: () => $t('salary.item.ruleOvertimeHours') },
  { value: 'FULL_ATTENDANCE', label: () => $t('salary.item.ruleFullAttendance') },
  { value: 'ATTEND_DAYS', label: () => $t('salary.item.ruleAttendDays') }
];

function directionTag(direction?: number): TagType {
  return direction === 2 ? 'danger' : 'success';
}

function valueTypeLabel(valueType?: number) {
  return valueTypeOptions.find(option => option.value === valueType)?.label() || '';
}

function attRuleLabel(rule?: string) {
  return attRuleOptions.find(option => option.value === rule)?.label() || rule || '';
}

async function loadData() {
  loading.value = true;
  try {
    const res = await fetchSalaryItems();
    data.value = res.data || [];
  } catch {
    // 请求层已统一弹错
  } finally {
    loading.value = false;
  }
}

onMounted(() => {
  loadData();
});

// ===== 新增/编辑 =====
const dialogVisible = ref(false);
const submitLoading = ref(false);
const formRef = ref<FormInstance>();
const formData = ref<SalaryItemDef>(emptyForm());

function emptyForm(): SalaryItemDef {
  return {
    itemCode: '',
    itemName: '',
    direction: 1,
    valueType: 1,
    enabled: 1,
    sortOrder: 0
  };
}

const formRules: FormRules = {
  itemName: [{ required: true, message: $t('salary.item.pleaseInputName'), trigger: 'blur' }],
  itemCode: [
    { required: true, message: $t('salary.item.pleaseInputCode'), trigger: 'blur' },
    {
      pattern: /^[a-z][a-z0-9_]{1,49}$/,
      message: $t('salary.item.codePattern'),
      trigger: 'blur'
    }
  ]
};

function handleAdd() {
  formData.value = emptyForm();
  dialogVisible.value = true;
}

function handleEdit(row: SalaryItemDef) {
  formData.value = { ...row };
  dialogVisible.value = true;
}

async function handleSubmit() {
  await formRef.value?.validate();
  submitLoading.value = true;
  try {
    await saveSalaryItem(formData.value);
    ElMessage.success($t('common.updateSuccess'));
    dialogVisible.value = false;
    loadData();
  } catch {
    // 请求层已统一弹错
  } finally {
    submitLoading.value = false;
  }
}

async function handleDelete(id?: number) {
  if (!id) return;
  try {
    await deleteSalaryItem(id);
    ElMessage.success($t('common.deleteSuccess'));
    loadData();
  } catch {
    // 请求层已统一弹错
  }
}
</script>

<template>
  <div class="min-h-500px flex-col-stretch gap-16px overflow-hidden lt-sm:overflow-auto">
    <ElCard class="flex-1">
      <template #header>
        <div class="flex flex-wrap items-center justify-between gap-12px">
          <span>{{ $t('salary.item.title') }}</span>
          <div class="flex flex-wrap items-center gap-8px">
            <ElButton @click="loadData">
              <template #icon><icon-ep-refresh /></template>
              {{ $t('common.refresh') }}
            </ElButton>
            <ElButton v-permission="'sal:item:manage'" type="primary" @click="handleAdd">
              <template #icon><icon-ep-plus /></template>
              {{ $t('salary.item.addItem') }}
            </ElButton>
          </div>
        </div>
      </template>

      <ElTable v-loading="loading" :data="data" size="small" border>
        <ElTableColumn prop="sortOrder" :label="$t('salary.common.sortOrder')" width="70" align="center" />
        <ElTableColumn prop="itemName" :label="$t('salary.item.itemName')" min-width="120" />
        <ElTableColumn prop="itemCode" :label="$t('salary.item.itemCode')" min-width="140" />
        <ElTableColumn :label="$t('salary.common.direction')" width="90" align="center">
          <template #default="{ row }">
            <ElTag :type="directionTag(row.direction)">
              {{ directionOptions.find(o => o.value === row.direction)?.label() }}
            </ElTag>
          </template>
        </ElTableColumn>
        <ElTableColumn :label="$t('salary.common.valueType')" width="100" align="center">
          <template #default="{ row }">{{ valueTypeLabel(row.valueType) }}</template>
        </ElTableColumn>
        <ElTableColumn :label="$t('salary.item.config')" min-width="220">
          <template #default="{ row }">
            <span v-if="row.valueType === 2">{{ row.ratioBaseCode }} × {{ row.ratioValue }}</span>
            <span v-else-if="row.valueType === 3">
              {{ attRuleLabel(row.attRule) }} × {{ row.unitPrice
              }}<template v-if="row.tolerance">（{{ $t('salary.item.tolerance') }}{{ row.tolerance }}）</template>
            </span>
            <span v-else-if="row.valueType === 4">{{ $t('salary.item.manualTip') }}</span>
            <span v-else class="text-gray-400">-</span>
          </template>
        </ElTableColumn>
        <ElTableColumn :label="$t('common.status')" width="90" align="center">
          <template #default="{ row }">
            <ElTag :type="row.enabled === 1 ? 'success' : 'info'">
              {{ row.enabled === 1 ? $t('salary.common.enabled') : $t('salary.common.disabled') }}
            </ElTag>
          </template>
        </ElTableColumn>
        <ElTableColumn prop="remark" :label="$t('common.remark')" min-width="120" show-overflow-tooltip />
        <ElTableColumn :label="$t('common.action')" width="130" align="center" fixed="right">
          <template #default="{ row }">
            <ElButton v-permission="'sal:item:manage'" type="primary" link size="small" @click="handleEdit(row)">
              {{ $t('common.edit') }}
            </ElButton>
            <ElPopconfirm :title="$t('salary.item.confirmDelete')" @confirm="handleDelete(row.id)">
              <template #reference>
                <ElButton v-permission="'sal:item:manage'" type="danger" link size="small">
                  {{ $t('common.delete') }}
                </ElButton>
              </template>
            </ElPopconfirm>
          </template>
        </ElTableColumn>
      </ElTable>
    </ElCard>

    <!-- 新增/编辑薪资项 -->
    <ElDialog
      v-model="dialogVisible"
      :title="formData.id ? $t('salary.item.editItem') : $t('salary.item.addItem')"
      width="520px"
      destroy-on-close
      :close-on-click-modal="false"
    >
      <ElForm ref="formRef" label-width="110px" :model="formData" :rules="formRules">
        <ElFormItem :label="$t('salary.item.itemName')" prop="itemName">
          <ElInput v-model="formData.itemName" :placeholder="$t('salary.item.pleaseInputName')" />
        </ElFormItem>
        <ElFormItem :label="$t('salary.item.itemCode')" prop="itemCode">
          <ElInput
            v-model="formData.itemCode"
            :disabled="!!formData.id"
            :placeholder="$t('salary.item.codePattern')"
          />
        </ElFormItem>
        <ElFormItem :label="$t('salary.common.direction')">
          <ElRadioGroup v-model="formData.direction">
            <ElRadio v-for="option in directionOptions" :key="option.value" :value="option.value">
              {{ option.label() }}
            </ElRadio>
          </ElRadioGroup>
        </ElFormItem>
        <ElFormItem :label="$t('salary.common.valueType')">
          <ElSelect
            v-model="formData.valueType"
            :disabled="!!formData.id"
            :placeholder="$t('common.pleaseSelect')"
            style="width: 100%"
          >
            <ElOption
              v-for="option in valueTypeOptions"
              :key="option.value"
              :label="option.label()"
              :value="option.value"
            />
          </ElSelect>
        </ElFormItem>
        <template v-if="formData.valueType === 2">
          <ElFormItem :label="$t('salary.item.ratioBaseCode')" required>
            <ElInput
              v-model="formData.ratioBaseCode"
              :placeholder="$t('salary.item.ratioBaseCodeTip')"
            />
          </ElFormItem>
          <ElFormItem :label="$t('salary.item.ratioValue')" required>
            <ElInputNumber v-model="formData.ratioValue" :min="0" :max="10" :step="0.01" :precision="4" style="width: 100%" />
          </ElFormItem>
        </template>
        <template v-if="formData.valueType === 3">
          <ElFormItem :label="$t('salary.item.attRule')" required>
            <ElSelect v-model="formData.attRule" :placeholder="$t('common.pleaseSelect')" style="width: 100%">
              <ElOption
                v-for="option in attRuleOptions"
                :key="option.value"
                :label="option.label()"
                :value="option.value"
              />
            </ElSelect>
          </ElFormItem>
          <ElFormItem :label="$t('salary.item.unitPrice')" required>
            <ElInputNumber v-model="formData.unitPrice" :min="0" :precision="2" :controls="false" style="width: 100%" />
          </ElFormItem>
          <ElFormItem :label="$t('salary.item.tolerance')">
            <ElInputNumber v-model="formData.tolerance" :min="0" :precision="0" :controls="false" style="width: 100%" />
          </ElFormItem>
        </template>
        <ElFormItem :label="$t('salary.common.sortOrder')">
          <ElInputNumber v-model="formData.sortOrder" :min="0" :precision="0" :controls="false" />
        </ElFormItem>
        <ElFormItem :label="$t('common.status')">
          <ElSwitch v-model="formData.enabled" :active-value="1" :inactive-value="0" />
        </ElFormItem>
        <ElFormItem :label="$t('common.remark')">
          <ElInput v-model="formData.remark" type="textarea" :rows="2" />
        </ElFormItem>
      </ElForm>
      <template #footer>
        <ElButton @click="dialogVisible = false">{{ $t('common.cancel') }}</ElButton>
        <ElButton type="primary" :loading="submitLoading" @click="handleSubmit">{{ $t('common.ok') }}</ElButton>
      </template>
    </ElDialog>
  </div>
</template>
