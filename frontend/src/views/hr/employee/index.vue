<script setup lang="tsx">
import { onMounted, ref } from 'vue';
import { ElMessage, type FormInstance, type FormRules, type UploadProps } from 'element-plus';
import dayjs from 'dayjs';
import { Plus } from '@element-plus/icons-vue';
import { REG_EMAIL, REG_ID_CARD, REG_PHONE } from '@/constants/reg';
import {
  checkEmployeeNo,
  createEmployee,
  deleteEmployee,
  fetchEmployeeById,
  fetchEmployeePage,
  generateEmployeeNo,
  importEmployees,
  updateEmployee
} from '@/service/api/hr';
import { fetchCompanyList, fetchOrgTree } from '@/service/api/organization';
import {
  getFileUrl,
  uploadCertPhoto,
  uploadDiplomaPhoto,
  uploadEmployeeAvatar,
  uploadEmployeeIdCard
} from '@/service/api/file';
import { useDictOptions } from '@/composables/use-dict-options';
import { downloadFile } from '@/utils/download';
import AuthImage from '@/components/business/auth-image.vue';
import { hasPermission } from '@/directives/permission';
import { $t } from '@/locales';

defineOptions({ name: 'EmployeeManage' });

const loading = ref(false);
const data = ref<Api.Hr.Employee[]>([]);
const total = ref(0);
const currentPage = ref(1);
const pageSize = ref(10);
const companyOptions = ref<Api.Organization.OrgUnit[]>([]);
// 14 个字典走 useDictOptions 模块级缓存，多页面共享，避免重复请求
const { options: genderOptions } = useDictOptions('gender');
const { options: educationOptions } = useDictOptions('education');
const { options: nationOptions } = useDictOptions('nation');
const { options: employeeTypeOptions } = useDictOptions('employee_type');
const { options: maritalStatusOptions } = useDictOptions('marital_status');
const { options: politicalStatusOptions } = useDictOptions('political_status');
const { options: familyRelationOptions } = useDictOptions('family_relation');
const { options: isFullTimeOptions } = useDictOptions('full_time');
const { options: certTypeOptions } = useDictOptions('cert_type');
const { options: certLevelOptions } = useDictOptions('cert_level');
const { options: dutyOptions } = useDictOptions('duty');
const { options: jobLevelOptions } = useDictOptions('job_level');
const { options: positionOptions } = useDictOptions('position');
const { options: extraFieldOptions } = useDictOptions('employee_extra_field');
const drawerVisible = ref(false);
const operateType = ref<'add' | 'edit'>('add');
const editingData = ref<Api.Hr.EmployeeForm>({
  employeeNo: '',
  name: '',
  companyId: undefined,
  deptId: undefined,
  status: 1
});
const activeTab = ref('basic');
const submitLoading = ref(false);
const detailLoading = ref(false); // 编辑抽屉详情加载（与表格 loading 拆分）
const orgTreeOptions = ref<Api.Organization.OrgUnit[]>([]);
const cascadeSelect = ref(false); // 是否联动选择子节点
const searchParams = ref({ name: '', employeeNo: '', orgIds: [] as number[], status: 1 as number | undefined });
const educationList = ref<Api.Hr.Education[]>([
  { schoolName: '', major: '', education: '', isFullTime: '', startDate: '', endDate: '', diplomaPhoto: '' }
]);
const familyMemberList = ref<Api.Hr.FamilyMember[]>([
  { name: '', relation: '', birthDate: '', politicalStatus: '', workUnit: '', occupation: '', phone: '' }
]);
const workList = ref<Api.Hr.WorkExperience[]>([
  {
    companyName: '',
    companyAddress: '',
    department: '',
    position: '',
    witness: '',
    witnessPhone: '',
    startDate: '',
    endDate: ''
  }
]);
const certificateList = ref<Api.Hr.Certificate[]>([
  { certName: '', certPhoto: '', certType: '', certLevel: '', issueDate: '', expireDate: '' }
]);
const extraFieldValues = ref<Record<string, string>>({});
const employeeNoError = ref(''); // 工号重复错误提示
const basicFormRef = ref<FormInstance>();
const exporting = ref(false); // 导出 loading

/** 基本信息表单校验规则（非必填字段，填写时校验格式） */
const basicFormRules: FormRules = {
  phone: [{ pattern: REG_PHONE, message: $t('hr.employee.pleaseEnterAValidPhoneNumber'), trigger: 'blur' }],
  email: [{ pattern: REG_EMAIL, message: $t('hr.employee.pleaseEnterAValidEmailAddress'), trigger: 'blur' }],
  idCard: [{ pattern: REG_ID_CARD, message: $t('hr.employee.pleaseEnterAValidIdNumber'), trigger: 'blur' }],
  emergencyPhone: [{ pattern: REG_PHONE, message: $t('hr.employee.pleaseEnterAValidEmergencyContactPhone'), trigger: 'blur' }]
};

/** 生成随机初始密码（8 位，去除易混淆字符） */
function generateInitialPassword(length = 8): string {
  const chars = 'ABCDEFGHJKLMNPQRSTUVWXYZabcdefghjkmnpqrstuvwxyz23456789';
  let result = '';
  for (let i = 0; i < length; i += 1) {
    result += chars[Math.floor(Math.random() * chars.length)];
  }
  return result;
}

// 检查工号是否重复
async function handleCheckEmployeeNo() {
  const employeeNo = editingData.value.employeeNo;
  if (!employeeNo) {
    employeeNoError.value = '';
    return;
  }
  try {
    const excludeId = operateType.value === 'edit' ? editingData.value.id : undefined;
    const res = await checkEmployeeNo(employeeNo, excludeId);
    if (res.data) {
      employeeNoError.value = $t('hr.employee.thisEmployeeNoAlreadyExists');
      ElMessage.warning($t('hr.employee.thisEmployeeNoAlreadyExists'));
    } else {
      employeeNoError.value = '';
    }
  } catch {
    // 请求层已统一弹错
  }
}

async function loadData() {
  loading.value = true;
  try {
    const res = await fetchEmployeePage({
      pageNum: currentPage.value,
      pageSize: pageSize.value,
      name: searchParams.value.name || undefined,
      employeeNo: searchParams.value.employeeNo || undefined,
      orgIds: searchParams.value.orgIds.length > 0 ? searchParams.value.orgIds.join(',') : undefined,
      status: searchParams.value.status
    });
    if (res.data) {
      data.value = res.data.records || [];
      total.value = res.data.total || 0;
    }
  } catch {
    // 请求层已统一弹错
  } finally {
    loading.value = false;
  }
}

async function loadCompanyList() {
  try {
    const res = await fetchCompanyList();
    if (res.data) {
      companyOptions.value = res.data;
    }
  } catch {
    // 请求层已统一弹错
  }
}

async function loadOrgTree() {
  try {
    const res = await fetchOrgTree();
    if (res.data) {
      orgTreeOptions.value = res.data;
    }
  } catch {
    // 请求层已统一弹错
  }
}

function getOrgName(id: number): string {
  const find = (nodes: any[]): string => {
    for (const node of nodes) {
      if (node.id === id) return node.unitName;
      if (node.children?.length) {
        const found = find(node.children);
        if (found) return found;
      }
    }
    return '';
  };
  return find(orgTreeOptions.value);
}

onMounted(async () => {
  // 公司/组织两个请求无依赖关系，并行加载；字典由 useDictOptions 自行加载
  await Promise.all([loadCompanyList(), loadOrgTree()]);
  loadData();
});

async function handleAdd() {
  operateType.value = 'add';
  // 打开弹窗时生成随机 8 位初始密码，避免使用统一默认密码
  editingData.value = {
    employeeNo: '',
    name: '',
    password: generateInitialPassword(8),
    avatar: '',
    idCardFront: '',
    idCardBack: '',
    deptId: undefined,
    gender: '',
    phone: '',
    email: '',
    entryDate: '',
    status: 1
  };
  educationList.value = [
    { schoolName: '', major: '', education: '', isFullTime: '', startDate: '', endDate: '', diplomaPhoto: '' }
  ];
  familyMemberList.value = [
    { name: '', relation: '', birthDate: '', politicalStatus: '', workUnit: '', occupation: '', phone: '' }
  ];
  workList.value = [
    {
      companyName: '',
      companyAddress: '',
      department: '',
      position: '',
      witness: '',
      witnessPhone: '',
      startDate: '',
      endDate: ''
    }
  ];
  certificateList.value = [{ certName: '', certPhoto: '', certType: '', certLevel: '', issueDate: '', expireDate: '' }];
  extraFieldValues.value = {};
  employeeNoError.value = '';
  activeTab.value = 'basic';
  drawerVisible.value = true;

  // 自动获取下一个工号
  try {
    const res = await generateEmployeeNo();
    if (res.data) {
      editingData.value.employeeNo = res.data;
    }
  } catch {
    // 请求层已统一弹错
  }
}

async function handleEdit(row: Api.Hr.Employee) {
  operateType.value = 'edit';
  // 编辑抽屉使用独立的 detailLoading，与表格 loading 拆分
  detailLoading.value = true;
  drawerVisible.value = true;
  try {
    // 调用API获取完整的员工详情（包含教育经历、家庭成员、工作经历、证书）
    const res = await fetchEmployeeById(row.id!);
    if (res.data) {
      const employee = res.data;
      // 编辑时不回显密码，留空表示不修改
      editingData.value = { ...employee, password: '' };
      educationList.value =
        employee.educationList && employee.educationList.length > 0
          ? employee.educationList
          : [
              { schoolName: '', major: '', education: '', isFullTime: '', startDate: '', endDate: '', diplomaPhoto: '' }
            ];
      familyMemberList.value =
        employee.familyMemberList && employee.familyMemberList.length > 0
          ? employee.familyMemberList
          : [{ name: '', relation: '', birthDate: '', politicalStatus: '', workUnit: '', occupation: '', phone: '' }];
      workList.value =
        employee.workExperienceList && employee.workExperienceList.length > 0
          ? employee.workExperienceList
          : [
              {
                companyName: '',
                companyAddress: '',
                department: '',
                position: '',
                witness: '',
                witnessPhone: '',
                startDate: '',
                endDate: ''
              }
            ];
      certificateList.value =
        employee.certificateList && employee.certificateList.length > 0
          ? employee.certificateList
          : [{ certName: '', certPhoto: '', certType: '', certLevel: '', issueDate: '', expireDate: '' }];
      // 加载扩展字段值
      extraFieldValues.value = {};
      if (employee.extraFieldList && employee.extraFieldList.length > 0) {
        for (const extra of employee.extraFieldList) {
          extraFieldValues.value[extra.fieldCode] = extra.fieldValue || '';
        }
      }
      activeTab.value = 'basic';
      employeeNoError.value = '';
    }
  } catch {
    // 请求层已统一弹错
  } finally {
    detailLoading.value = false;
  }
}

async function handleDelete(id: number) {
  try {
    await deleteEmployee(id);
    ElMessage.success($t('common.deleteSuccess'));
    loadData();
  } catch {
    // 请求层已统一弹错
  }
}
function handleSearch() {
  currentPage.value = 1;
  loadData();
}
function handleReset() {
  searchParams.value = { name: '', employeeNo: '', orgIds: [], status: 1 };
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
function addEducation() {
  educationList.value.push({
    schoolName: '',
    major: '',
    education: '',
    isFullTime: '',
    startDate: '',
    endDate: '',
    diplomaPhoto: ''
  });
}
function removeEducation(index: number) {
  if (educationList.value.length > 1) {
    educationList.value.splice(index, 1);
  }
}
function addFamilyMember() {
  familyMemberList.value.push({
    name: '',
    relation: '',
    birthDate: '',
    politicalStatus: '',
    workUnit: '',
    occupation: '',
    phone: ''
  });
}
function removeFamilyMember(index: number) {
  if (familyMemberList.value.length > 1) {
    familyMemberList.value.splice(index, 1);
  }
}
function addWork() {
  workList.value.push({
    companyName: '',
    companyAddress: '',
    department: '',
    position: '',
    witness: '',
    witnessPhone: '',
    startDate: '',
    endDate: ''
  });
}
function removeWork(index: number) {
  if (workList.value.length > 1) {
    workList.value.splice(index, 1);
  }
}
function addCertificate() {
  certificateList.value.push({
    certName: '',
    certPhoto: '',
    certType: '',
    certLevel: '',
    issueDate: '',
    expireDate: ''
  });
}
function removeCertificate(index: number) {
  if (certificateList.value.length > 1) {
    certificateList.value.splice(index, 1);
  }
}

async function handleImageUpload(file: File, type: 'avatar' | 'idCardFront' | 'idCardBack'): Promise<boolean> {
  try {
    // 检查是否有工号
    if (!editingData.value.employeeNo) {
      ElMessage.warning($t('hr.employee.pleaseEnterEmployeeNoFirst'));
      return false;
    }

    // 头像使用专门的员工头像上传接口
    if (type === 'avatar') {
      const res = await uploadEmployeeAvatar(file, editingData.value.employeeNo);
      if (res.data) {
        editingData.value[type] = res.data;
        ElMessage.success($t('hr.employee.avatarUploadedSuccessfully'));
        return true;
      }
    } else if (type === 'idCardFront') {
      // 身份证正面使用专门的接口
      const res = await uploadEmployeeIdCard(file, editingData.value.employeeNo, 'front');
      if (res.data) {
        editingData.value[type] = res.data;
        ElMessage.success($t('hr.employee.idCardFrontUploadedSuccessfully'));
        return true;
      }
    } else if (type === 'idCardBack') {
      // 身份证反面使用专门的接口
      const res = await uploadEmployeeIdCard(file, editingData.value.employeeNo, 'back');
      if (res.data) {
        editingData.value[type] = res.data;
        ElMessage.success($t('hr.employee.idCardBackUploadedSuccessfully'));
        return true;
      }
    }
    return false;
  } catch {
    // 请求层已统一弹错
    return false;
  }
}
async function handleDiplomaUpload(file: File, index: number): Promise<boolean> {
  try {
    if (!editingData.value.employeeNo) {
      ElMessage.warning($t('hr.employee.pleaseEnterEmployeeNoFirst'));
      return false;
    }
    const res = await uploadDiplomaPhoto(file, editingData.value.employeeNo);
    if (res.data) {
      educationList.value[index].diplomaPhoto = res.data;
      ElMessage.success($t('hr.contract.uploadedSuccessfully'));
      return true;
    }
    return false;
  } catch {
    // 请求层已统一弹错
    return false;
  }
}
async function handleCertPhotoUpload(file: File, index: number): Promise<boolean> {
  try {
    if (!editingData.value.employeeNo) {
      ElMessage.warning($t('hr.employee.pleaseEnterEmployeeNoFirst'));
      return false;
    }
    const res = await uploadCertPhoto(file, editingData.value.employeeNo);
    if (res.data) {
      certificateList.value[index].certPhoto = res.data;
      ElMessage.success($t('hr.contract.uploadedSuccessfully'));
      return true;
    }
    return false;
  } catch {
    // 请求层已统一弹错
    return false;
  }
}
const beforeUpload: UploadProps['beforeUpload'] = rawFile => {
  const allowedTypes = ['image/jpeg', 'image/png', 'image/gif', 'image/webp'];
  if (!allowedTypes.includes(rawFile.type)) {
    ElMessage.error($t('hr.employee.onlyJpgPngGifWebpImagesAreSupported'));
    return false;
  }
  if (rawFile.size / 1024 / 1024 > 5) {
    ElMessage.error($t('hr.employee.imageSizeCannotExceed5mb'));
    return false;
  }
  return true;
};

// ==================== 员工导入 ====================
const importDialogVisible = ref(false);
const importing = ref(false);
const importFileList = ref<UploadProps['fileList']>([]);
const importResult = ref<Api.Hr.ImportResult | null>(null);

function openImportDialog() {
  importDialogVisible.value = true;
  importFileList.value = [];
  importResult.value = null;
}

function handleDownloadTemplate() {
  downloadFile('/employee/import-template', $t('hr.employee.employeeImportTemplateXlsx'));
}

async function handleImportSubmit() {
  const files = importFileList.value || [];
  const fileItem = files[0];
  if (!fileItem) {
    ElMessage.warning($t('hr.employee.pleaseSelectAnXlsxFile'));
    return;
  }
  // el-upload on-change 包装过的 File 在 raw 字段
  const raw = (fileItem as unknown as { raw?: File }).raw || (fileItem as unknown as File);
  if (!(raw instanceof File)) {
    ElMessage.warning($t('hr.employee.failedToReadTheFilePleaseSelectAgain'));
    return;
  }
  importing.value = true;
  try {
    const res = await importEmployees(raw);
    importResult.value = res.data || null;
    if (res.data && res.data.successCount > 0) {
      ElMessage.success($t('hr.employee.successfullyImportedEmployees', { count: res.data.successCount }));
      loadData();
    }
  } catch {
    // 请求层已统一弹错
  } finally {
    importing.value = false;
  }
}

/** 导出员工列表 */
async function handleExport() {
  exporting.value = true;
  try {
    await downloadFile('/employee/export', $t('hr.employee.employeeListXlsx', { date: dayjs().format('YYYYMMDD') }), {
      name: searchParams.value.name || undefined,
      employeeNo: searchParams.value.employeeNo || undefined,
      orgIds: searchParams.value.orgIds.length > 0 ? searchParams.value.orgIds.join(',') : undefined,
      status: searchParams.value.status
    });
    ElMessage.success($t('attendance.common.exportSuccessful'));
  } catch {
    // downloadFile 内部已提示具体错误
  } finally {
    exporting.value = false;
  }
}

async function handleSubmit() {
  if (!editingData.value.employeeNo || !editingData.value.name || !editingData.value.deptId) {
    ElMessage.warning($t('hr.employee.pleaseFillInRequiredFieldsPersonnelNoNameOrganization'));
    return;
  }
  if (employeeNoError.value) {
    ElMessage.warning($t('hr.employee.employeeNoAlreadyExistsPleaseModifyIt'));
    return;
  }
  // 校验手机/邮箱/身份证/紧急电话格式
  if (basicFormRef.value) {
    const valid = await basicFormRef.value.validate().catch(() => false);
    if (!valid) {
      activeTab.value = 'basic';
      ElMessage.warning($t('hr.employee.pleaseCheckTheFormatsOfPhoneEmailIdNoInBasicInfo'));
      return;
    }
  }
  // 构建扩展字段列表
  const extraFieldList = Object.entries(extraFieldValues.value)
    .filter(([_, value]) => value && value.trim())
    .map(([fieldCode, fieldValue]) => ({ fieldCode, fieldValue }));
  const submitData: Api.Hr.EmployeeForm = {
    ...editingData.value,
    educationList: educationList.value.filter(e => e.schoolName),
    familyMemberList: familyMemberList.value.filter(f => f.name),
    workExperienceList: workList.value.filter(w => w.companyName),
    certificateList: certificateList.value.filter(c => c.certName),
    extraFieldList
  };
  submitLoading.value = true;
  try {
    if (operateType.value === 'add') {
      await createEmployee(submitData);
      ElMessage.success($t('common.addSuccess'));
    } else {
      await updateEmployee(editingData.value.id!, submitData);
      ElMessage.success($t('common.updateSuccess'));
    }
    drawerVisible.value = false;
    loadData();
  } catch {
    // 请求层已统一弹错
  } finally {
    submitLoading.value = false;
  }
}

function getDictLabel(options: Api.System.DictData[], value?: string): string {
  if (!value) return '';
  const item = options.find(o => o.dictValue === value);
  return item?.dictLabel || value;
}
const statusMap: Record<number, { label: string; type: string }> = {
  1: { label: $t('common.active'), type: 'success' },
  2: { label: $t('common.resigned'), type: 'info' },
  3: { label: $t('common.pendingEntry'), type: 'warning' }
};
</script>

<template>
  <div class="list-page">
    <ElCard class="search-card">
      <ElForm inline :model="searchParams">
        <ElFormItem :label="$t('common.employeeName')">
          <ElInput
            v-model="searchParams.name"
            :placeholder="$t('common.pleaseInputEmployeeName')"
            clearable
            style="width: 150px"
            @keyup.enter="handleSearch"
          />
        </ElFormItem>
        <ElFormItem :label="$t('hr.employee.personnelNo')">
          <ElInput
            v-model="searchParams.employeeNo"
            :placeholder="$t('hr.employee.pleaseEnterPersonnelNo')"
            clearable
            style="width: 150px"
            @keyup.enter="handleSearch"
          />
        </ElFormItem>
        <ElFormItem :label="$t('common.organization')">
          <ElTreeSelect
            v-model="searchParams.orgIds"
            :data="orgTreeOptions"
            :props="{ children: 'children', label: 'unitName', value: 'id' }"
            node-key="id"
            :placeholder="$t('common.pleaseSelectOrganization')"
            clearable
            multiple
            :check-strictly="!cascadeSelect"
            show-checkbox
            collapse-tags
            :max-collapse-tags="2"
            style="width: 150px"
            :render-after-expand="false"
            filterable
          >
            <template #default="{ node, data }">
              <div class="tree-node-content">
                <span>{{ data.unitName }}</span>
                <ElCheckbox v-if="node.level === 1" v-model="cascadeSelect" @click.stop>{{ $t('common.cascade') }}</ElCheckbox>
              </div>
            </template>
            <template #label="{ value }">
              <span>{{ getOrgName(value) }}</span>
            </template>
          </ElTreeSelect>
        </ElFormItem>
        <ElFormItem :label="$t('common.status')">
          <ElSelect v-model="searchParams.status" :placeholder="$t('common.pleaseSelectStatus')" clearable style="width: 120px">
            <ElOption :label="$t('common.active')" :value="1" />
            <ElOption :label="$t('common.resigned')" :value="2" />
            <ElOption :label="$t('common.pendingEntry')" :value="3" />
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
          <span>{{ $t('common.employeeList') }}</span>
          <div class="flex items-center gap-8px">
            <ElButton v-permission="'hr:employee:export'" :loading="exporting" @click="handleExport">
              <template #icon><icon-ep-download /></template>
              {{ $t('common.export') }}
            </ElButton>
            <ElButton v-permission="'hr:employee:import'" @click="openImportDialog">
              <template #icon><icon-ep-upload /></template>
              {{ $t('hr.employee.importEmployees') }}
            </ElButton>
            <ElButton v-permission="'hr:employee:add'" type="primary" @click="handleAdd">
              <template #icon><icon-ep-plus /></template>
              {{ $t('hr.employee.addEmployee') }}
            </ElButton>
          </div>
        </div>
      </template>
      <div class="table-wrapper">
        <ElTable v-loading="loading" :data="data" border stripe height="100%">
          <ElTableColumn type="index" :label="$t('common.index2')" width="60" align="center" fixed="left" />
          <ElTableColumn prop="employeeNo" :label="$t('hr.employee.personnelNo')" width="100" fixed="left" />
          <ElTableColumn prop="name" :label="$t('common.name')" width="80" fixed="left" />
          <ElTableColumn prop="gender" :label="$t('common.gender')" width="60" align="center">
            <template #default="{ row }">{{ getDictLabel(genderOptions, row.gender) }}</template>
          </ElTableColumn>
          <ElTableColumn prop="idCard" :label="$t('common.idNumber')" width="170" />
          <ElTableColumn prop="birthDate" :label="$t('hr.employee.dateOfBirth')" width="100" />
          <ElTableColumn prop="nation" :label="$t('common.ethnicity')" width="70">
            <template #default="{ row }">{{ getDictLabel(nationOptions, row.nation) }}</template>
          </ElTableColumn>
          <ElTableColumn prop="highestEducation" :label="$t('common.education')" width="80">
            <template #default="{ row }">{{ getDictLabel(educationOptions, row.highestEducation) }}</template>
          </ElTableColumn>
          <ElTableColumn prop="phone" :label="$t('hr.employee.telephone')" width="120" />
          <ElTableColumn prop="email" :label="$t('common.email')" width="150" show-overflow-tooltip />
          <ElTableColumn prop="employeeType" :label="$t('hr.employee.employeeCategory')" width="90">
            <template #default="{ row }">{{ getDictLabel(employeeTypeOptions, row.employeeType) }}</template>
          </ElTableColumn>
          <ElTableColumn prop="maritalStatus" :label="$t('common.maritalStatus')" width="80">
            <template #default="{ row }">{{ getDictLabel(maritalStatusOptions, row.maritalStatus) }}</template>
          </ElTableColumn>
          <ElTableColumn prop="politicalStatus" :label="$t('common.politicalStatus')" width="90">
            <template #default="{ row }">{{ getDictLabel(politicalStatusOptions, row.politicalStatus) }}</template>
          </ElTableColumn>
          <ElTableColumn prop="companyName" :label="$t('common.company')" width="100" show-overflow-tooltip />
          <ElTableColumn prop="deptName" :label="$t('common.department')" width="90" />
          <ElTableColumn prop="entryDate" :label="$t('common.entryDate')" width="100" />
          <ElTableColumn prop="regularDate" :label="$t('application.regularization.regularizationDate')" width="100" />
          <ElTableColumn prop="duty" :label="$t('common.jobTitle')" width="80">
            <template #default="{ row }">{{ getDictLabel(dutyOptions, row.duty) }}</template>
          </ElTableColumn>
          <ElTableColumn prop="position" :label="$t('common.position')" width="100">
            <template #default="{ row }">{{ getDictLabel(positionOptions, row.position) }}</template>
          </ElTableColumn>
          <ElTableColumn prop="jobLevel" :label="$t('hr.employee.jobGrade')" width="70">
            <template #default="{ row }">{{ getDictLabel(jobLevelOptions, row.jobLevel) }}</template>
          </ElTableColumn>
          <ElTableColumn prop="nativePlace" :label="$t('common.nativePlace')" width="100" show-overflow-tooltip />
          <ElTableColumn prop="registeredAddress" :label="$t('hr.employee.registeredAddress')" width="150" show-overflow-tooltip />
          <ElTableColumn prop="currentAddress" :label="$t('hr.employee.currentResidence')" width="150" show-overflow-tooltip />
          <ElTableColumn prop="emergencyContact" :label="$t('common.emergencyContact')" width="90" />
          <ElTableColumn prop="emergencyRelation" :label="$t('hr.employee.relationship')" width="70">
            <template #default="{ row }">{{ getDictLabel(familyRelationOptions, row.emergencyRelation) }}</template>
          </ElTableColumn>
          <ElTableColumn prop="emergencyPhone" :label="$t('hr.employee.emergencyPhone')" width="120" />
          <ElTableColumn prop="status" :label="$t('common.status')" width="70" align="center">
            <template #default="{ row }">
              <ElTag :type="statusMap[row.status]?.type as any">{{ statusMap[row.status]?.label }}</ElTag>
            </template>
          </ElTableColumn>
          <ElTableColumn :label="$t('common.action')" width="120" align="center" fixed="right">
            <template #default="{ row }">
              <ElButton v-permission="'hr:employee:edit'" type="primary" link size="small" @click="handleEdit(row)">
                {{ $t('common.edit') }}
              </ElButton>
              <ElPopconfirm
                v-if="hasPermission('hr:employee:delete')"
                :title="$t('hr.employee.areYouSureYouWantToDeleteThisEmployee')"
                @confirm="handleDelete(row.id)"
              >
                <template #reference><ElButton type="danger" link size="small">{{ $t('common.delete') }}</ElButton></template>
              </ElPopconfirm>
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
    <ElDrawer
      v-model="drawerVisible"
      :title="operateType === 'add' ? $t('hr.employee.addEmployee') : $t('hr.employee.editEmployee')"
      size="900px"
      class="employee-drawer"
    >
      <div v-loading="detailLoading" class="drawer-content">
        <ElTabs v-model="activeTab">
          <ElTabPane :label="$t('hr.employee.basicInfo')" name="basic">
            <ElForm ref="basicFormRef" label-width="100px" :model="editingData" :rules="basicFormRules">
              <ElDivider content-position="left">{{ $t('hr.employee.personnelBasicInfo') }}</ElDivider>
              <ElRow :gutter="20">
                <ElCol :span="8">
                  <ElFormItem :label="$t('hr.employee.miniProgramPassword')">
                    <ElInput
                      v-model="editingData.password"
                      type="password"
                      show-password
                      :placeholder="operateType === 'edit' ? $t('hr.employee.leaveBlankToKeepUnchanged') : $t('hr.employee.leaveBlankToUseARandomInitialPassword')"
                    />
                  </ElFormItem>
                </ElCol>
              </ElRow>
              <ElRow :gutter="20" class="mb-20px">
                <ElCol :span="8">
                  <div class="text-center">
                    <div class="mb-8px font-bold">{{ $t('hr.employee.avatar') }}</div>
                    <ElUpload
                      class="avatar-uploader"
                      :show-file-list="false"
                      :before-upload="beforeUpload"
                      :http-request="({ file }) => handleImageUpload(file as File, 'avatar')"
                    >
                      <ElImage
                        v-if="editingData.avatar"
                        :src="getFileUrl(editingData.avatar)"
                        :preview-src-list="[getFileUrl(editingData.avatar)]"
                        preview-teleported
                        teleported
                        hide-on-click-modal
                        fit="cover"
                        class="h-100px w-100px cursor-pointer rounded-full"
                      />
                      <div
                        v-else
                        class="h-100px w-100px flex cursor-pointer items-center justify-center border border-[var(--el-border-color)] rounded-full border-dashed bg-[var(--el-fill-color-light)] hover:border-primary"
                      >
                        <ElIcon :size="28" class="text-gray-400"><Plus /></ElIcon>
                      </div>
                    </ElUpload>
                  </div>
                </ElCol>
                <ElCol :span="8">
                  <div class="text-center">
                    <div class="mb-8px font-bold">{{ $t('hr.employee.idCardFront') }}</div>
                    <ElUpload
                      class="id-card-uploader"
                      :show-file-list="false"
                      :before-upload="beforeUpload"
                      :http-request="({ file }) => handleImageUpload(file as File, 'idCardFront')"
                    >
                      <AuthImage
                        v-if="editingData.idCardFront"
                        :url="editingData.idCardFront"
                        fit="cover"
                        preview
                        class="h-100px w-160px rounded"
                      />
                      <div
                        v-else
                        class="h-100px w-160px flex cursor-pointer items-center justify-center border border-[var(--el-border-color)] rounded border-dashed bg-[var(--el-fill-color-light)] hover:border-primary"
                      >
                        <div class="text-center">
                          <ElIcon :size="28" class="text-gray-400"><Plus /></ElIcon>
                          <div class="mt-4px text-12px text-gray-400">{{ $t('hr.employee.idCardFront') }}</div>
                        </div>
                      </div>
                    </ElUpload>
                  </div>
                </ElCol>
                <ElCol :span="8">
                  <div class="text-center">
                    <div class="mb-8px font-bold">{{ $t('hr.employee.idCardBack') }}</div>
                    <ElUpload
                      class="id-card-uploader"
                      :show-file-list="false"
                      :before-upload="beforeUpload"
                      :http-request="({ file }) => handleImageUpload(file as File, 'idCardBack')"
                    >
                      <AuthImage
                        v-if="editingData.idCardBack"
                        :url="editingData.idCardBack"
                        fit="cover"
                        preview
                        class="h-100px w-160px rounded"
                      />
                      <div
                        v-else
                        class="h-100px w-160px flex cursor-pointer items-center justify-center border border-[var(--el-border-color)] rounded border-dashed bg-[var(--el-fill-color-light)] hover:border-primary"
                      >
                        <div class="text-center">
                          <ElIcon :size="28" class="text-gray-400"><Plus /></ElIcon>
                          <div class="mt-4px text-12px text-gray-400">{{ $t('hr.employee.idCardBack') }}</div>
                        </div>
                      </div>
                    </ElUpload>
                  </div>
                </ElCol>
              </ElRow>
              <ElRow :gutter="20">
                <ElCol :span="8">
                  <ElFormItem :label="$t('hr.employee.personnelNo')" required :error="employeeNoError">
                    <ElInput
                      v-model="editingData.employeeNo"
                      :placeholder="$t('hr.employee.pleaseEnterPersonnelNo')"
                      @blur="handleCheckEmployeeNo"
                    />
                  </ElFormItem>
                </ElCol>
                <ElCol :span="8">
                  <ElFormItem :label="$t('common.name')" required>
                    <ElInput v-model="editingData.name" :placeholder="$t('common.pleaseInputName')" />
                  </ElFormItem>
                </ElCol>
                <ElCol :span="8">
                  <ElFormItem :label="$t('common.gender')">
                    <ElSelect v-model="editingData.gender" :placeholder="$t('hr.employee.pleaseSelectGender')" style="width: 100%">
                      <ElOption
                        v-for="item in genderOptions"
                        :key="item.dictValue"
                        :label="item.dictLabel"
                        :value="item.dictValue"
                      />
                    </ElSelect>
                  </ElFormItem>
                </ElCol>
              </ElRow>
              <ElRow :gutter="20">
                <ElCol :span="8">
                  <ElFormItem :label="$t('common.idNumber')" prop="idCard">
                    <ElInput v-model="editingData.idCard" :placeholder="$t('hr.employee.pleaseEnterIdNumber')" />
                  </ElFormItem>
                </ElCol>
                <ElCol :span="8">
                  <ElFormItem :label="$t('hr.employee.dateOfBirth')">
                    <ElDatePicker
                      v-model="editingData.birthDate"
                      type="date"
                      :placeholder="$t('common.selectDate')"
                      style="width: 100%"
                      value-format="YYYY-MM-DD"
                    />
                  </ElFormItem>
                </ElCol>
                <ElCol :span="8">
                  <ElFormItem :label="$t('common.ethnicity')">
                    <ElSelect v-model="editingData.nation" :placeholder="$t('hr.employee.pleaseSelectEthnicity')" style="width: 100%">
                      <ElOption
                        v-for="item in nationOptions"
                        :key="item.dictValue"
                        :label="item.dictLabel"
                        :value="item.dictValue"
                      />
                    </ElSelect>
                  </ElFormItem>
                </ElCol>
              </ElRow>
              <ElRow :gutter="20">
                <ElCol :span="8">
                  <ElFormItem :label="$t('hr.employee.highestEducation')">
                    <ElSelect v-model="editingData.highestEducation" :placeholder="$t('hr.employee.pleaseSelectEducation')" style="width: 100%">
                      <ElOption
                        v-for="item in educationOptions"
                        :key="item.dictValue"
                        :label="item.dictLabel"
                        :value="item.dictValue"
                      />
                    </ElSelect>
                  </ElFormItem>
                </ElCol>
                <ElCol :span="8">
                  <ElFormItem :label="$t('hr.employee.telephone')" prop="phone">
                    <ElInput v-model="editingData.phone" :placeholder="$t('hr.employee.pleaseEnterPhoneNumber')" />
                  </ElFormItem>
                </ElCol>
                <ElCol :span="8">
                  <ElFormItem :label="$t('common.email')" prop="email">
                    <ElInput v-model="editingData.email" :placeholder="$t('common.pleaseEnterEmail')" />
                  </ElFormItem>
                </ElCol>
              </ElRow>
              <ElRow :gutter="20">
                <ElCol :span="8">
                  <ElFormItem :label="$t('hr.employee.employeeCategory')">
                    <ElSelect v-model="editingData.employeeType" :placeholder="$t('hr.employee.pleaseSelectEmployeeCategory')" style="width: 100%">
                      <ElOption
                        v-for="item in employeeTypeOptions"
                        :key="item.dictValue"
                        :label="item.dictLabel"
                        :value="item.dictValue"
                      />
                    </ElSelect>
                  </ElFormItem>
                </ElCol>
                <ElCol :span="8">
                  <ElFormItem :label="$t('common.maritalStatus')">
                    <ElSelect v-model="editingData.maritalStatus" :placeholder="$t('hr.employee.pleaseSelectMaritalStatus')" style="width: 100%">
                      <ElOption
                        v-for="item in maritalStatusOptions"
                        :key="item.dictValue"
                        :label="item.dictLabel"
                        :value="item.dictValue"
                      />
                    </ElSelect>
                  </ElFormItem>
                </ElCol>
                <ElCol :span="8">
                  <ElFormItem :label="$t('common.politicalStatus')">
                    <ElSelect v-model="editingData.politicalStatus" :placeholder="$t('hr.employee.pleaseSelectPoliticalStatus')" style="width: 100%">
                      <ElOption
                        v-for="item in politicalStatusOptions"
                        :key="item.dictValue"
                        :label="item.dictLabel"
                        :value="item.dictValue"
                      />
                    </ElSelect>
                  </ElFormItem>
                </ElCol>
              </ElRow>
              <ElDivider content-position="left">{{ $t('hr.employee.workInfo') }}</ElDivider>
              <ElRow :gutter="20">
                <ElCol :span="8">
                  <ElFormItem :label="$t('hr.employee.organization')" required>
                    <ElTreeSelect
                      v-model="editingData.deptId"
                      :data="orgTreeOptions"
                      :props="{ children: 'children', label: 'unitName', value: 'id' }"
                      node-key="id"
                      :placeholder="$t('common.pleaseSelectOrganization')"
                      check-strictly
                      style="width: 100%"
                      :render-after-expand="false"
                      filterable
                    />
                  </ElFormItem>
                </ElCol>
                <ElCol :span="8">
                  <ElFormItem :label="$t('common.entryDate')">
                    <ElDatePicker
                      v-model="editingData.entryDate"
                      type="date"
                      :placeholder="$t('common.selectDate')"
                      style="width: 100%"
                      value-format="YYYY-MM-DD"
                    />
                  </ElFormItem>
                </ElCol>
                <ElCol :span="8">
                  <ElFormItem :label="$t('application.regularization.regularizationDate')">
                    <ElDatePicker
                      v-model="editingData.regularDate"
                      type="date"
                      :placeholder="$t('common.selectDate')"
                      style="width: 100%"
                      value-format="YYYY-MM-DD"
                    />
                  </ElFormItem>
                </ElCol>
              </ElRow>
              <ElRow :gutter="20">
                <ElCol :span="8">
                  <ElFormItem :label="$t('common.jobTitle')">
                    <ElSelect v-model="editingData.duty" :placeholder="$t('hr.employee.pleaseSelectJobTitle')" style="width: 100%">
                      <ElOption
                        v-for="item in dutyOptions"
                        :key="item.dictValue"
                        :label="item.dictLabel"
                        :value="item.dictValue"
                      />
                    </ElSelect>
                  </ElFormItem>
                </ElCol>
                <ElCol :span="8">
                  <ElFormItem :label="$t('common.position')">
                    <ElSelect v-model="editingData.position" :placeholder="$t('hr.employee.pleaseSelectPosition')" style="width: 100%">
                      <ElOption
                        v-for="item in positionOptions"
                        :key="item.dictValue"
                        :label="item.dictLabel"
                        :value="item.dictValue"
                      />
                    </ElSelect>
                  </ElFormItem>
                </ElCol>
                <ElCol :span="8">
                  <ElFormItem :label="$t('hr.employee.jobGrade')">
                    <ElSelect v-model="editingData.jobLevel" :placeholder="$t('hr.employee.pleaseSelectJobGrade')" style="width: 100%">
                      <ElOption
                        v-for="item in jobLevelOptions"
                        :key="item.dictValue"
                        :label="item.dictLabel"
                        :value="item.dictValue"
                      />
                    </ElSelect>
                  </ElFormItem>
                </ElCol>
              </ElRow>
              <ElRow :gutter="20">
                <ElCol :span="8">
                  <ElFormItem :label="$t('common.status')">
                    <ElSelect v-model="editingData.status" :placeholder="$t('common.pleaseSelectStatus')" style="width: 100%">
                      <ElOption :label="$t('common.active')" :value="1" />
                      <ElOption :label="$t('common.resigned')" :value="2" />
                      <ElOption :label="$t('common.pendingEntry')" :value="3" />
                    </ElSelect>
                  </ElFormItem>
                </ElCol>
              </ElRow>
              <ElDivider content-position="left">{{ $t('hr.employee.contactInfo') }}</ElDivider>
              <ElRow :gutter="20">
                <ElCol :span="8">
                  <ElFormItem :label="$t('common.nativePlace')">
                    <ElInput v-model="editingData.nativePlace" :placeholder="$t('hr.employee.pleaseEnterNativePlace')" />
                  </ElFormItem>
                </ElCol>
                <ElCol :span="8">
                  <ElFormItem :label="$t('hr.employee.registeredAddress')">
                    <ElInput v-model="editingData.registeredAddress" :placeholder="$t('hr.employee.pleaseEnterRegisteredAddress')" />
                  </ElFormItem>
                </ElCol>
                <ElCol :span="8">
                  <ElFormItem :label="$t('hr.employee.currentResidence')">
                    <ElInput v-model="editingData.currentAddress" :placeholder="$t('hr.employee.pleaseEnterCurrentResidence')" />
                  </ElFormItem>
                </ElCol>
              </ElRow>
              <ElDivider content-position="left">{{ $t('common.emergencyContact') }}</ElDivider>
              <ElRow :gutter="20">
                <ElCol :span="8">
                  <ElFormItem :label="$t('common.contact')">
                    <ElInput v-model="editingData.emergencyContact" :placeholder="$t('hr.employee.pleaseEnterEmergencyContact')" />
                  </ElFormItem>
                </ElCol>
                <ElCol :span="8">
                  <ElFormItem :label="$t('hr.employee.relationship')">
                    <ElSelect v-model="editingData.emergencyRelation" :placeholder="$t('hr.employee.pleaseSelectRelationship')" style="width: 100%">
                      <ElOption
                        v-for="item in familyRelationOptions"
                        :key="item.dictValue"
                        :label="item.dictLabel"
                        :value="item.dictValue"
                      />
                    </ElSelect>
                  </ElFormItem>
                </ElCol>
                <ElCol :span="8">
                  <ElFormItem :label="$t('common.contactPhone')" prop="emergencyPhone">
                    <ElInput v-model="editingData.emergencyPhone" :placeholder="$t('hr.employee.pleaseEnterContactPhone')" />
                  </ElFormItem>
                </ElCol>
              </ElRow>
            </ElForm>
          </ElTabPane>
          <ElTabPane :label="$t('hr.employee.education')" name="education">
            <div class="mb-10px">
              <ElButton type="primary" size="small" @click="addEducation">
                <template #icon><icon-ep-plus /></template>
                {{ $t('hr.employee.addEducation') }}
              </ElButton>
            </div>
            <div
              v-for="(edu, index) in educationList"
              :key="index"
              class="mb-20px rounded bg-[var(--el-fill-color-light)] p-15px"
            >
              <div class="mb-10px flex justify-between">
                <span class="font-bold">{{ $t('hr.employee.education') }} {{ index + 1 }}</span>
                <ElButton
                  v-if="educationList.length > 1"
                  type="danger"
                  link
                  size="small"
                  @click="removeEducation(index)"
                >
                  {{ $t('common.delete') }}
                </ElButton>
              </div>
              <ElForm label-width="80px">
                <ElRow :gutter="20">
                  <ElCol :span="8">
                    <ElFormItem :label="$t('hr.employee.school')">
                      <ElInput v-model="edu.schoolName" :placeholder="$t('hr.employee.pleaseEnterSchoolName')" />
                    </ElFormItem>
                  </ElCol>
                  <ElCol :span="8">
                    <ElFormItem :label="$t('hr.employee.major')"><ElInput v-model="edu.major" :placeholder="$t('hr.employee.pleaseEnterMajor')" /></ElFormItem>
                  </ElCol>
                  <ElCol :span="8">
                    <ElFormItem :label="$t('common.education')">
                      <ElSelect v-model="edu.education" :placeholder="$t('hr.employee.pleaseSelectEducation')" style="width: 100%">
                        <ElOption
                          v-for="item in educationOptions"
                          :key="item.dictValue"
                          :label="item.dictLabel"
                          :value="item.dictValue"
                        />
                      </ElSelect>
                    </ElFormItem>
                  </ElCol>
                </ElRow>
                <ElRow :gutter="20">
                  <ElCol :span="8">
                    <ElFormItem :label="$t('hr.employee.fullTime')">
                      <ElSelect v-model="edu.isFullTime" :placeholder="$t('common.pleaseSelect')" style="width: 100%">
                        <ElOption
                          v-for="item in isFullTimeOptions"
                          :key="item.dictValue"
                          :label="item.dictLabel"
                          :value="item.dictValue"
                        />
                      </ElSelect>
                    </ElFormItem>
                  </ElCol>
                  <ElCol :span="8">
                    <ElFormItem :label="$t('hr.employee.enrollmentDate')">
                      <ElDatePicker
                        v-model="edu.startDate"
                        type="month"
                        :placeholder="$t('common.selectTime')"
                        style="width: 100%"
                        value-format="YYYY-MM"
                      />
                    </ElFormItem>
                  </ElCol>
                  <ElCol :span="8">
                    <ElFormItem :label="$t('hr.employee.graduationDate')">
                      <ElDatePicker
                        v-model="edu.endDate"
                        type="month"
                        :placeholder="$t('common.selectTime')"
                        style="width: 100%"
                        value-format="YYYY-MM"
                      />
                    </ElFormItem>
                  </ElCol>
                </ElRow>
                <ElRow :gutter="20">
                  <ElCol :span="8">
                    <ElFormItem :label="$t('hr.employee.diploma')">
                      <ElUpload
                        class="diploma-uploader"
                        :show-file-list="false"
                        :before-upload="beforeUpload"
                        :http-request="({ file }) => handleDiplomaUpload(file as File, index)"
                      >
                        <AuthImage
                          v-if="edu.diplomaPhoto"
                          :url="edu.diplomaPhoto"
                          fit="cover"
                          preview
                          class="h-80px w-120px rounded"
                        />
                        <div
                          v-else
                          class="h-80px w-120px flex cursor-pointer items-center justify-center border border-[var(--el-border-color)] rounded border-dashed bg-[var(--el-fill-color-light)] hover:border-primary"
                        >
                          <ElIcon :size="24" class="text-gray-400"><Plus /></ElIcon>
                        </div>
                      </ElUpload>
                    </ElFormItem>
                  </ElCol>
                </ElRow>
              </ElForm>
            </div>
          </ElTabPane>
          <ElTabPane :label="$t('hr.employee.familyMember')" name="family">
            <div class="mb-10px">
              <ElButton type="primary" size="small" @click="addFamilyMember">
                <template #icon><icon-ep-plus /></template>
                {{ $t('hr.employee.addFamilyMember') }}
              </ElButton>
            </div>
            <div
              v-for="(member, index) in familyMemberList"
              :key="index"
              class="mb-20px rounded bg-[var(--el-fill-color-light)] p-15px"
            >
              <div class="mb-10px flex justify-between">
                <span class="font-bold">{{ $t('hr.employee.familyMember') }} {{ index + 1 }}</span>
                <ElButton
                  v-if="familyMemberList.length > 1"
                  type="danger"
                  link
                  size="small"
                  @click="removeFamilyMember(index)"
                >
                  {{ $t('common.delete') }}
                </ElButton>
              </div>
              <ElForm label-width="80px">
                <ElRow :gutter="20">
                  <ElCol :span="8">
                    <ElFormItem :label="$t('common.name')"><ElInput v-model="member.name" :placeholder="$t('common.pleaseInputName')" /></ElFormItem>
                  </ElCol>
                  <ElCol :span="8">
                    <ElFormItem :label="$t('hr.employee.relationship')">
                      <ElSelect v-model="member.relation" :placeholder="$t('hr.employee.pleaseSelectRelationship')" style="width: 100%">
                        <ElOption
                          v-for="item in familyRelationOptions"
                          :key="item.dictValue"
                          :label="item.dictLabel"
                          :value="item.dictValue"
                        />
                      </ElSelect>
                    </ElFormItem>
                  </ElCol>
                  <ElCol :span="8">
                    <ElFormItem :label="$t('hr.employee.dateOfBirth')">
                      <ElDatePicker
                        v-model="member.birthDate"
                        type="date"
                        :placeholder="$t('common.selectDate')"
                        style="width: 100%"
                        value-format="YYYY-MM-DD"
                      />
                    </ElFormItem>
                  </ElCol>
                </ElRow>
                <ElRow :gutter="20">
                  <ElCol :span="8">
                    <ElFormItem :label="$t('common.politicalStatus')">
                      <ElSelect v-model="member.politicalStatus" :placeholder="$t('common.pleaseSelect')" style="width: 100%">
                        <ElOption
                          v-for="item in politicalStatusOptions"
                          :key="item.dictValue"
                          :label="item.dictLabel"
                          :value="item.dictValue"
                        />
                      </ElSelect>
                    </ElFormItem>
                  </ElCol>
                  <ElCol :span="8">
                    <ElFormItem :label="$t('hr.employee.workUnit')">
                      <ElInput v-model="member.workUnit" :placeholder="$t('hr.employee.pleaseEnterWorkUnit')" />
                    </ElFormItem>
                  </ElCol>
                  <ElCol :span="8">
                    <ElFormItem :label="$t('hr.employee.telephone')"><ElInput v-model="member.phone" :placeholder="$t('hr.employee.pleaseEnterPhoneNumber')" /></ElFormItem>
                  </ElCol>
                </ElRow>
              </ElForm>
            </div>
          </ElTabPane>
          <ElTabPane :label="$t('hr.employee.workExperience')" name="work">
            <div class="mb-10px">
              <ElButton type="primary" size="small" @click="addWork">
                <template #icon><icon-ep-plus /></template>
                {{ $t('hr.employee.addWorkExperience') }}
              </ElButton>
            </div>
            <div
              v-for="(work, index) in workList"
              :key="index"
              class="mb-20px rounded bg-[var(--el-fill-color-light)] p-15px"
            >
              <div class="mb-10px flex justify-between">
                <span class="font-bold">{{ $t('hr.employee.workExperience') }} {{ index + 1 }}</span>
                <ElButton v-if="workList.length > 1" type="danger" link size="small" @click="removeWork(index)">
                  {{ $t('common.delete') }}
                </ElButton>
              </div>
              <ElForm label-width="80px">
                <ElRow :gutter="20">
                  <ElCol :span="8">
                    <ElFormItem :label="$t('hr.employee.companyName')">
                      <ElInput v-model="work.companyName" :placeholder="$t('hr.employee.pleaseEnterCompanyName')" />
                    </ElFormItem>
                  </ElCol>
                  <ElCol :span="8">
                    <ElFormItem :label="$t('hr.employee.department')">
                      <ElInput v-model="work.department" :placeholder="$t('hr.employee.pleaseEnterDepartment')" />
                    </ElFormItem>
                  </ElCol>
                  <ElCol :span="8">
                    <ElFormItem :label="$t('common.position')"><ElInput v-model="work.position" :placeholder="$t('hr.employee.pleaseEnterPosition')" /></ElFormItem>
                  </ElCol>
                </ElRow>
                <ElRow :gutter="20">
                  <ElCol :span="8">
                    <ElFormItem :label="$t('hr.employee.referencePerson')">
                      <ElInput v-model="work.witness" :placeholder="$t('hr.employee.pleaseEnterReferencePerson')" />
                    </ElFormItem>
                  </ElCol>
                  <ElCol :span="8">
                    <ElFormItem :label="$t('hr.employee.referencePhone')">
                      <ElInput v-model="work.witnessPhone" :placeholder="$t('hr.employee.pleaseEnterReferencePhone')" />
                    </ElFormItem>
                  </ElCol>
                  <ElCol :span="8">
                    <ElFormItem :label="$t('common.time')">
                      <ElDatePicker
                        v-model="work.startDate"
                        type="month"
                        :placeholder="$t('attendance.clock.start')"
                        style="width: 45%"
                        value-format="YYYY-MM"
                      />
                      <span class="mx-5px">-</span>
                      <ElDatePicker
                        v-model="work.endDate"
                        type="month"
                        :placeholder="$t('attendance.clock.end')"
                        style="width: 45%"
                        value-format="YYYY-MM"
                      />
                    </ElFormItem>
                  </ElCol>
                </ElRow>
              </ElForm>
            </div>
          </ElTabPane>
          <ElTabPane :label="$t('hr.employee.certificate')" name="certificate">
            <div class="mb-10px">
              <ElButton type="primary" size="small" @click="addCertificate">
                <template #icon><icon-ep-plus /></template>
                {{ $t('hr.employee.addCertificate') }}
              </ElButton>
            </div>
            <div
              v-for="(cert, index) in certificateList"
              :key="index"
              class="mb-20px rounded bg-[var(--el-fill-color-light)] p-15px"
            >
              <div class="mb-10px flex justify-between">
                <span class="font-bold">{{ $t('hr.employee.certificate') }} {{ index + 1 }}</span>
                <ElButton
                  v-if="certificateList.length > 1"
                  type="danger"
                  link
                  size="small"
                  @click="removeCertificate(index)"
                >
                  {{ $t('common.delete') }}
                </ElButton>
              </div>
              <ElForm label-width="80px">
                <ElRow :gutter="20">
                  <ElCol :span="8">
                    <ElFormItem :label="$t('hr.employee.certificateName')">
                      <ElInput v-model="cert.certName" :placeholder="$t('hr.employee.pleaseEnterCertificateName')" />
                    </ElFormItem>
                  </ElCol>
                  <ElCol :span="8">
                    <ElFormItem :label="$t('hr.employee.certificateType')">
                      <ElSelect v-model="cert.certType" :placeholder="$t('hr.employee.pleaseSelectCertificateType')" style="width: 100%">
                        <ElOption
                          v-for="item in certTypeOptions"
                          :key="item.dictValue"
                          :label="item.dictLabel"
                          :value="item.dictValue"
                        />
                      </ElSelect>
                    </ElFormItem>
                  </ElCol>
                  <ElCol :span="8">
                    <ElFormItem :label="$t('hr.employee.certificateLevel')">
                      <ElSelect v-model="cert.certLevel" :placeholder="$t('hr.employee.pleaseSelectCertificateLevel')" style="width: 100%">
                        <ElOption
                          v-for="item in certLevelOptions"
                          :key="item.dictValue"
                          :label="item.dictLabel"
                          :value="item.dictValue"
                        />
                      </ElSelect>
                    </ElFormItem>
                  </ElCol>
                </ElRow>
                <ElRow :gutter="20">
                  <ElCol :span="8">
                    <ElFormItem :label="$t('hr.employee.issueDate')">
                      <ElDatePicker
                        v-model="cert.issueDate"
                        type="date"
                        :placeholder="$t('common.selectDate')"
                        style="width: 100%"
                        value-format="YYYY-MM-DD"
                      />
                    </ElFormItem>
                  </ElCol>
                  <ElCol :span="8">
                    <ElFormItem :label="$t('hr.employee.expireDate')">
                      <ElDatePicker
                        v-model="cert.expireDate"
                        type="date"
                        :placeholder="$t('common.selectDate')"
                        style="width: 100%"
                        value-format="YYYY-MM-DD"
                      />
                    </ElFormItem>
                  </ElCol>
                  <ElCol :span="8">
                    <ElFormItem :label="$t('hr.employee.certificatePhoto')">
                      <ElUpload
                        class="cert-uploader"
                        :show-file-list="false"
                        :before-upload="beforeUpload"
                        :http-request="({ file }) => handleCertPhotoUpload(file as File, index)"
                      >
                        <AuthImage
                          v-if="cert.certPhoto"
                          :url="cert.certPhoto"
                          fit="cover"
                          preview
                          class="h-80px w-120px rounded"
                        />
                        <div
                          v-else
                          class="h-80px w-120px flex cursor-pointer items-center justify-center border border-[var(--el-border-color)] rounded border-dashed bg-[var(--el-fill-color-light)] hover:border-primary"
                        >
                          <ElIcon :size="24" class="text-gray-400"><Plus /></ElIcon>
                        </div>
                      </ElUpload>
                    </ElFormItem>
                  </ElCol>
                </ElRow>
              </ElForm>
            </div>
          </ElTabPane>
          <ElTabPane v-if="extraFieldOptions.length > 0" :label="$t('hr.employee.otherInfo')" name="extra">
            <ElForm label-width="100px">
              <ElRow :gutter="20">
                <ElCol v-for="field in extraFieldOptions" :key="field.dictValue" :span="8">
                  <ElFormItem :label="field.dictLabel">
                    <ElInput v-model="extraFieldValues[field.dictValue]" :placeholder="$t('hr.employee.pleaseEnter', { field: field.dictLabel })" />
                  </ElFormItem>
                </ElCol>
              </ElRow>
            </ElForm>
          </ElTabPane>
        </ElTabs>
      </div>
      <template #footer>
        <ElButton @click="drawerVisible = false">{{ $t('common.cancel') }}</ElButton>
        <ElButton type="primary" :loading="submitLoading" @click="handleSubmit">{{ $t('common.save') }}</ElButton>
      </template>
    </ElDrawer>

    <!-- 员工导入 -->
    <ElDialog v-model="importDialogVisible" :title="$t('hr.employee.importEmployees')" width="560px" :close-on-click-modal="false">
      <div class="mb-12px">
        <ElButton link type="primary" @click="handleDownloadTemplate">
          <template #icon><icon-ep-download /></template>
          {{ $t('hr.employee.downloadImportTemplate') }}
        </ElButton>
      </div>
      <ElUpload
        v-model:file-list="importFileList"
        drag
        accept=".xlsx"
        :auto-upload="false"
        :limit="1"
        :on-exceed="() => ElMessage.warning($t('hr.employee.onlyOneFileCanBeSelected'))"
      >
        <div class="py-16px">
          <ElIcon :size="36" class="mb-8px text-gray-400"><icon-ep-upload-filled /></ElIcon>
          <div>{{ $t('hr.employee.dragTheXlsxFileHereOrClickToSelect') }}</div>
        </div>
      </ElUpload>
      <div v-if="importResult" class="mt-12px">
        <div class="mb-8px">
          <ElTag type="success">{{ $t('common.success') }} {{ importResult.successCount }} {{ $t('hr.employee.items') }}</ElTag>
          <ElTag v-if="importResult.failCount > 0" type="danger" class="ml-8px">
            {{ $t('common.fail') }} {{ importResult.failCount }} {{ $t('hr.employee.items') }}
          </ElTag>
        </div>
        <div
          v-if="importResult.errors.length > 0"
          class="max-h-160px overflow-y-auto rounded bg-gray-50 p-8px text-xs text-red-500"
        >
          <div v-for="(err, index) in importResult.errors" :key="index">{{ err }}</div>
        </div>
      </div>
      <template #footer>
        <ElButton @click="importDialogVisible = false">{{ $t('common.close') }}</ElButton>
        <ElButton type="primary" :loading="importing" @click="handleImportSubmit">{{ $t('hr.employee.startImport') }}</ElButton>
      </template>
    </ElDialog>
  </div>
</template>

<style scoped>
.employee-drawer :deep(.el-drawer__body) {
  padding: 0;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.drawer-content {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  padding-right: 30px;
}

.drawer-content::-webkit-scrollbar {
  width: 6px;
}

.drawer-content::-webkit-scrollbar-thumb {
  background-color: var(--el-border-color-darker);
  border-radius: 3px;
}

.drawer-content::-webkit-scrollbar-track {
  background-color: var(--el-fill-color-lighter);
}

:deep(.el-tree-node__content) {
  width: 100%;
}

.tree-node-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex: 1;
  padding-right: 8px;
}
</style>
