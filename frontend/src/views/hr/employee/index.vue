<script setup lang="tsx">
import { onMounted, ref, watch } from 'vue';
import { ElMessage, type UploadProps } from 'element-plus';
import { Plus } from '@element-plus/icons-vue';
import {
  createEmployee,
  deleteEmployee,
  fetchEmployeeById,
  fetchEmployeePage,
  updateEmployee,
  checkEmployeeNo
} from '@/service/api/hr';
import { fetchCompanyList, fetchDepartmentTree, fetchOrgTree } from '@/service/api/organization';
import { fetchDictDataByCode } from '@/service/api/system';
import { getFileUrl, uploadEmployeeAvatar, uploadEmployeeIdCard, uploadDiplomaPhoto, uploadCertPhoto } from '@/service/api/file';
import { hasPermission } from '@/directives/permission';

defineOptions({ name: 'EmployeeManage' });

const loading = ref(false);
const data = ref<Api.Hr.Employee[]>([]);
const total = ref(0);
const currentPage = ref(1);
const pageSize = ref(10);
const companyOptions = ref<Api.Organization.OrgUnit[]>([]);
const genderOptions = ref<Api.System.DictData[]>([]);
const educationOptions = ref<Api.System.DictData[]>([]);
const nationOptions = ref<Api.System.DictData[]>([]);
const employeeTypeOptions = ref<Api.System.DictData[]>([]);
const maritalStatusOptions = ref<Api.System.DictData[]>([]);
const politicalStatusOptions = ref<Api.System.DictData[]>([]);
const familyRelationOptions = ref<Api.System.DictData[]>([]);
const isFullTimeOptions = ref<Api.System.DictData[]>([]);
const certTypeOptions = ref<Api.System.DictData[]>([]);
const certLevelOptions = ref<Api.System.DictData[]>([]);
const dutyOptions = ref<Api.System.DictData[]>([]);
const jobLevelOptions = ref<Api.System.DictData[]>([]);
const positionOptions = ref<Api.System.DictData[]>([]);
const drawerVisible = ref(false);
const operateType = ref<'add' | 'edit'>('add');
const editingData = ref<Api.Hr.EmployeeForm>({ employeeNo: '', name: '', companyId: undefined, deptId: undefined, status: 1 });
const activeTab = ref('basic');
const submitLoading = ref(false);
const orgTreeOptions = ref<Api.Organization.OrgUnit[]>([]);
const cascadeSelect = ref(false); // 是否联动选择子节点
const searchParams = ref({ name: '', employeeNo: '', orgIds: [] as number[], status: 1 as number | undefined });
const educationList = ref<Api.Hr.Education[]>([{ schoolName: '', major: '', education: '', isFullTime: '', startDate: '', endDate: '', diplomaPhoto: '' }]);
const familyMemberList = ref<Api.Hr.FamilyMember[]>([{ name: '', relation: '', birthDate: '', politicalStatus: '', workUnit: '', occupation: '', phone: '' }]);
const workList = ref<Api.Hr.WorkExperience[]>([{ companyName: '', companyAddress: '', department: '', position: '', witness: '', witnessPhone: '', startDate: '', endDate: '' }]);
const certificateList = ref<Api.Hr.Certificate[]>([{ certName: '', certPhoto: '', certType: '', certLevel: '', issueDate: '', expireDate: '' }]);
const extraFieldOptions = ref<Api.System.DictData[]>([]);
const extraFieldValues = ref<Record<string, string>>({});
const employeeNoError = ref(''); // 工号重复错误提示

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
      employeeNoError.value = '该工号已存在';
      ElMessage.warning('该工号已存在');
    } else {
      employeeNoError.value = '';
    }
  } catch (error) {
    console.error('检查工号失败:', error);
  }
}

async function loadData() {
  loading.value = true;
  try {
    const res = await fetchEmployeePage({ pageNum: currentPage.value, pageSize: pageSize.value, name: searchParams.value.name || undefined, employeeNo: searchParams.value.employeeNo || undefined, orgIds: searchParams.value.orgIds.length > 0 ? searchParams.value.orgIds.join(',') : undefined, status: searchParams.value.status });
    if (res.data) { data.value = res.data.records || []; total.value = res.data.total || 0; }
  } catch (error) { console.error('加载员工列表失败:', error); } finally { loading.value = false; }
}

async function loadCompanyList() {
  try { const res = await fetchCompanyList(); if (res.data) { companyOptions.value = res.data; } } catch (error) { console.error('加载公司列表失败:', error); }
}

async function loadOrgTree() {
  try { const res = await fetchOrgTree(); if (res.data) { orgTreeOptions.value = res.data; } } catch (error) { console.error('加载组织架构失败:', error); }
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

async function loadDepartmentList(companyId?: number) {
  try { const res = await fetchDepartmentTree(companyId); if (res.data) { return res.data; } return []; } catch (error) { console.error('加载部门列表失败:', error); return []; }
}

async function loadDictData() {
  try {
    const dictTypes = ['gender', 'education', 'nation', 'employee_type', 'marital_status', 'political_status', 'family_relation', 'full_time', 'cert_type', 'cert_level', 'duty', 'job_level', 'position', 'employee_extra_field'];
    const results = await Promise.all(dictTypes.map(type => fetchDictDataByCode(type)));
    genderOptions.value = results[0].data || []; educationOptions.value = results[1].data || []; nationOptions.value = results[2].data || [];
    employeeTypeOptions.value = results[3].data || []; maritalStatusOptions.value = results[4].data || []; politicalStatusOptions.value = results[5].data || [];
    familyRelationOptions.value = results[6].data || []; isFullTimeOptions.value = results[7].data || []; certTypeOptions.value = results[8].data || [];
    certLevelOptions.value = results[9].data || []; dutyOptions.value = results[10].data || []; jobLevelOptions.value = results[11].data || [];
    positionOptions.value = results[12].data || []; extraFieldOptions.value = results[13].data || [];
  } catch (error) { console.error('加载字典数据失败:', error); }
}

watch(() => searchParams.value.orgIds, () => { /* 组织变化时重新加载数据 */ });
// 编辑表单组织变化时的处理（如果需要）

onMounted(async () => { await loadCompanyList(); await loadOrgTree(); await loadDictData(); loadData(); });

function handleAdd() {
  operateType.value = 'add';
  editingData.value = { employeeNo: '', name: '', avatar: '', idCardFront: '', idCardBack: '', deptId: undefined, gender: '', phone: '', email: '', entryDate: '', status: 1 };
  educationList.value = [{ schoolName: '', major: '', education: '', isFullTime: '', startDate: '', endDate: '', diplomaPhoto: '' }];
  familyMemberList.value = [{ name: '', relation: '', birthDate: '', politicalStatus: '', workUnit: '', occupation: '', phone: '' }];
  workList.value = [{ companyName: '', companyAddress: '', department: '', position: '', witness: '', witnessPhone: '', startDate: '', endDate: '' }];
  certificateList.value = [{ certName: '', certPhoto: '', certType: '', certLevel: '', issueDate: '', expireDate: '' }];
  extraFieldValues.value = {};
  employeeNoError.value = '';
  activeTab.value = 'basic';
  drawerVisible.value = true;
}

async function handleEdit(row: Api.Hr.Employee) {
  operateType.value = 'edit';
  loading.value = true;
  try {
    // 调用API获取完整的员工详情（包含教育经历、家庭成员、工作经历、证书）
    const res = await fetchEmployeeById(row.id!);
    if (res.data) {
      const employee = res.data;
      editingData.value = { ...employee };
      educationList.value = employee.educationList && employee.educationList.length > 0 ? employee.educationList : [{ schoolName: '', major: '', education: '', isFullTime: '', startDate: '', endDate: '', diplomaPhoto: '' }];
      familyMemberList.value = employee.familyMemberList && employee.familyMemberList.length > 0 ? employee.familyMemberList : [{ name: '', relation: '', birthDate: '', politicalStatus: '', workUnit: '', occupation: '', phone: '' }];
      workList.value = employee.workExperienceList && employee.workExperienceList.length > 0 ? employee.workExperienceList : [{ companyName: '', companyAddress: '', department: '', position: '', witness: '', witnessPhone: '', startDate: '', endDate: '' }];
      certificateList.value = employee.certificateList && employee.certificateList.length > 0 ? employee.certificateList : [{ certName: '', certPhoto: '', certType: '', certLevel: '', issueDate: '', expireDate: '' }];
      // 加载扩展字段值
      extraFieldValues.value = {};
      if (employee.extraFieldList && employee.extraFieldList.length > 0) {
        for (const extra of employee.extraFieldList) {
          extraFieldValues.value[extra.fieldCode] = extra.fieldValue || '';
        }
      }
      activeTab.value = 'basic';
      employeeNoError.value = '';
      drawerVisible.value = true;
    }
  } catch (error) {
    console.error('获取员工详情失败:', error);
    ElMessage.error('获取员工详情失败');
  } finally {
    loading.value = false;
  }
}

async function handleDelete(id: number) { try { await deleteEmployee(id); ElMessage.success('删除成功'); loadData(); } catch (error) { console.error('删除失败:', error); ElMessage.error('删除失败'); } }
function handleSearch() { currentPage.value = 1; loadData(); }
function handleReset() { searchParams.value = { name: '', employeeNo: '', orgIds: [], status: undefined }; currentPage.value = 1; loadData(); }
function handlePageChange(page: number) { currentPage.value = page; loadData(); }
function handleSizeChange(size: number) { pageSize.value = size; currentPage.value = 1; loadData(); }
function addEducation() { educationList.value.push({ schoolName: '', major: '', education: '', isFullTime: '', startDate: '', endDate: '', diplomaPhoto: '' }); }
function removeEducation(index: number) { if (educationList.value.length > 1) { educationList.value.splice(index, 1); } }
function addFamilyMember() { familyMemberList.value.push({ name: '', relation: '', birthDate: '', politicalStatus: '', workUnit: '', occupation: '', phone: '' }); }
function removeFamilyMember(index: number) { if (familyMemberList.value.length > 1) { familyMemberList.value.splice(index, 1); } }
function addWork() { workList.value.push({ companyName: '', companyAddress: '', department: '', position: '', witness: '', witnessPhone: '', startDate: '', endDate: '' }); }
function removeWork(index: number) { if (workList.value.length > 1) { workList.value.splice(index, 1); } }
function addCertificate() { certificateList.value.push({ certName: '', certPhoto: '', certType: '', certLevel: '', issueDate: '', expireDate: '' }); }
function removeCertificate(index: number) { if (certificateList.value.length > 1) { certificateList.value.splice(index, 1); } }

async function handleImageUpload(file: File, type: 'avatar' | 'idCardFront' | 'idCardBack'): Promise<boolean> {
  try {
    // 检查是否有工号
    if (!editingData.value.employeeNo) {
      ElMessage.warning('请先填写员工工号');
      return false;
    }

    // 头像使用专门的员工头像上传接口
    if (type === 'avatar') {
      const res = await uploadEmployeeAvatar(file, editingData.value.employeeNo);
      if (res.data) {
        editingData.value[type] = res.data;
        ElMessage.success('头像上传成功');
        return true;
      }
    } else if (type === 'idCardFront') {
      // 身份证正面使用专门的接口
      const res = await uploadEmployeeIdCard(file, editingData.value.employeeNo, 'front');
      if (res.data) {
        editingData.value[type] = res.data;
        ElMessage.success('身份证正面上传成功');
        return true;
      }
    } else if (type === 'idCardBack') {
      // 身份证反面使用专门的接口
      const res = await uploadEmployeeIdCard(file, editingData.value.employeeNo, 'back');
      if (res.data) {
        editingData.value[type] = res.data;
        ElMessage.success('身份证反面上传成功');
        return true;
      }
    }
    return false;
  } catch (error) {
    console.error('上传失败:', error);
    ElMessage.error('上传失败');
    return false;
  }
}
async function handleDiplomaUpload(file: File, index: number): Promise<boolean> {
  try {
    if (!editingData.value.employeeNo) { ElMessage.warning('请先填写员工工号'); return false; }
    const res = await uploadDiplomaPhoto(file, editingData.value.employeeNo);
    if (res.data) { educationList.value[index].diplomaPhoto = res.data; ElMessage.success('上传成功'); return true; }
    return false;
  } catch (error) { console.error('上传失败:', error); ElMessage.error('上传失败'); return false; }
}
async function handleCertPhotoUpload(file: File, index: number): Promise<boolean> {
  try {
    if (!editingData.value.employeeNo) { ElMessage.warning('请先填写员工工号'); return false; }
    const res = await uploadCertPhoto(file, editingData.value.employeeNo);
    if (res.data) { certificateList.value[index].certPhoto = res.data; ElMessage.success('上传成功'); return true; }
    return false;
  } catch (error) { console.error('上传失败:', error); ElMessage.error('上传失败'); return false; }
}
const beforeUpload: UploadProps['beforeUpload'] = (rawFile) => {
  const allowedTypes = ['image/jpeg', 'image/png', 'image/gif', 'image/webp'];
  if (!allowedTypes.includes(rawFile.type)) { ElMessage.error('只支持上传 JPG/PNG/GIF/WEBP 格式的图片'); return false; }
  if (rawFile.size / 1024 / 1024 > 5) { ElMessage.error('图片大小不能超过 5MB'); return false; }
  return true;
};

async function handleSubmit() {
  if (!editingData.value.employeeNo || !editingData.value.name || !editingData.value.deptId) { ElMessage.warning('请填写必填项（人员编号、姓名、所属组织）'); return; }
  if (employeeNoError.value) { ElMessage.warning('工号已存在，请修改'); return; }
  // 构建扩展字段列表
  const extraFieldList = Object.entries(extraFieldValues.value)
    .filter(([_, value]) => value && value.trim())
    .map(([fieldCode, fieldValue]) => ({ fieldCode, fieldValue }));
  const submitData: Api.Hr.EmployeeForm = { ...editingData.value, educationList: educationList.value.filter(e => e.schoolName), familyMemberList: familyMemberList.value.filter(f => f.name), workExperienceList: workList.value.filter(w => w.companyName), certificateList: certificateList.value.filter(c => c.certName), extraFieldList };
  submitLoading.value = true;
  try { if (operateType.value === 'add') { await createEmployee(submitData); ElMessage.success('新增成功'); } else { await updateEmployee(editingData.value.id!, submitData); ElMessage.success('更新成功'); } drawerVisible.value = false; loadData(); } catch (error) { console.error('保存失败:', error); ElMessage.error('保存失败'); } finally { submitLoading.value = false; }
}

function getDictLabel(options: Api.System.DictData[], value?: string): string { if (!value) return ''; const item = options.find(o => o.dictValue === value); return item?.dictLabel || value; }
const statusMap: Record<number, { label: string; type: string }> = { 1: { label: '在职', type: 'success' }, 2: { label: '离职', type: 'info' } };
</script>

<template>
  <div class="list-page">
    <ElCard class="search-card">
      <ElForm inline :model="searchParams">
        <ElFormItem label="员工姓名"><ElInput v-model="searchParams.name" placeholder="请输入员工姓名" clearable style="width: 150px" /></ElFormItem>
        <ElFormItem label="人员编号"><ElInput v-model="searchParams.employeeNo" placeholder="请输入人员编号" clearable style="width: 150px" /></ElFormItem>
        <ElFormItem label="组织"><ElTreeSelect v-model="searchParams.orgIds" :data="orgTreeOptions" :props="{ children: 'children', label: 'unitName', value: 'id' }" node-key="id" placeholder="请选择组织" clearable multiple :check-strictly="!cascadeSelect" show-checkbox collapse-tags :max-collapse-tags="2" style="width: 150px" :render-after-expand="false" filterable><template #default="{ node, data }"><div class="tree-node-content"><span>{{ data.unitName }}</span><ElCheckbox v-if="node.level === 1" v-model="cascadeSelect" @click.stop>联动</ElCheckbox></div></template><template #label="{ value }"><span>{{ getOrgName(value) }}</span></template></ElTreeSelect></ElFormItem>
        <ElFormItem label="状态"><ElSelect v-model="searchParams.status" placeholder="请选择状态" clearable style="width: 120px"><ElOption label="在职" :value="1" /><ElOption label="离职" :value="2" /></ElSelect></ElFormItem>
        <ElFormItem><ElButton type="primary" @click="handleSearch"><template #icon><icon-ep-search /></template>搜索</ElButton><ElButton @click="handleReset"><template #icon><icon-ep-refresh /></template>重置</ElButton></ElFormItem>
      </ElForm>
    </ElCard>
    <ElCard class="table-card">
      <template #header><div class="flex items-center justify-between"><span>员工列表</span><ElButton v-permission="'hr:employee:add'" type="primary" @click="handleAdd"><template #icon><icon-ep-plus /></template>新增员工</ElButton></div></template>
      <div class="table-wrapper">
        <ElTable v-loading="loading" :data="data" border stripe height="100%">
        <ElTableColumn type="index" label="序号" width="60" align="center" fixed="left" />
        <ElTableColumn prop="employeeNo" label="人员编号" width="100" fixed="left" />
        <ElTableColumn prop="name" label="姓名" width="80" fixed="left" />
        <ElTableColumn prop="gender" label="性别" width="60" align="center"><template #default="{ row }">{{ getDictLabel(genderOptions, row.gender) }}</template></ElTableColumn>
        <ElTableColumn prop="idCard" label="身份证号" width="170" />
        <ElTableColumn prop="birthDate" label="出生日期" width="100" />
        <ElTableColumn prop="nation" label="民族" width="70"><template #default="{ row }">{{ getDictLabel(nationOptions, row.nation) }}</template></ElTableColumn>
        <ElTableColumn prop="highestEducation" label="学历" width="80"><template #default="{ row }">{{ getDictLabel(educationOptions, row.highestEducation) }}</template></ElTableColumn>
        <ElTableColumn prop="phone" label="电话" width="120" />
        <ElTableColumn prop="email" label="邮箱" width="150" show-overflow-tooltip />
        <ElTableColumn prop="employeeType" label="员工类别" width="90"><template #default="{ row }">{{ getDictLabel(employeeTypeOptions, row.employeeType) }}</template></ElTableColumn>
        <ElTableColumn prop="maritalStatus" label="婚姻状况" width="80"><template #default="{ row }">{{ getDictLabel(maritalStatusOptions, row.maritalStatus) }}</template></ElTableColumn>
        <ElTableColumn prop="politicalStatus" label="政治面貌" width="90"><template #default="{ row }">{{ getDictLabel(politicalStatusOptions, row.politicalStatus) }}</template></ElTableColumn>
        <ElTableColumn prop="companyName" label="公司" width="100" show-overflow-tooltip />
        <ElTableColumn prop="deptName" label="部门" width="90" />
        <ElTableColumn prop="entryDate" label="入职日期" width="100" />
        <ElTableColumn prop="regularDate" label="转正日期" width="100" />
        <ElTableColumn prop="duty" label="职务" width="80"><template #default="{ row }">{{ getDictLabel(dutyOptions, row.duty) }}</template></ElTableColumn>
        <ElTableColumn prop="position" label="职位" width="100"><template #default="{ row }">{{ getDictLabel(positionOptions, row.position) }}</template></ElTableColumn>
        <ElTableColumn prop="jobLevel" label="职级" width="70"><template #default="{ row }">{{ getDictLabel(jobLevelOptions, row.jobLevel) }}</template></ElTableColumn>
        <ElTableColumn prop="nativePlace" label="籍贯" width="100" show-overflow-tooltip />
        <ElTableColumn prop="registeredAddress" label="户籍地址" width="150" show-overflow-tooltip />
        <ElTableColumn prop="currentAddress" label="现居住地" width="150" show-overflow-tooltip />
        <ElTableColumn prop="emergencyContact" label="紧急联系人" width="90" />
        <ElTableColumn prop="emergencyRelation" label="关系" width="70"><template #default="{ row }">{{ getDictLabel(familyRelationOptions, row.emergencyRelation) }}</template></ElTableColumn>
        <ElTableColumn prop="emergencyPhone" label="紧急电话" width="120" />
        <ElTableColumn prop="status" label="状态" width="70" align="center"><template #default="{ row }"><ElTag :type="statusMap[row.status]?.type as any">{{ statusMap[row.status]?.label }}</ElTag></template></ElTableColumn>
        <ElTableColumn label="操作" width="120" align="center" fixed="right"><template #default="{ row }"><ElButton v-permission="'hr:employee:edit'" type="primary" link size="small" @click="handleEdit(row)">编辑</ElButton><ElPopconfirm v-if="hasPermission('hr:employee:delete')" title="确定删除该员工吗？" @confirm="handleDelete(row.id)"><template #reference><ElButton type="danger" link size="small">删除</ElButton></template></ElPopconfirm></template></ElTableColumn>
      </ElTable>
      </div>
      <div class="mt-16px flex justify-end"><ElPagination v-model:current-page="currentPage" v-model:page-size="pageSize" :total="total" :page-sizes="[10, 20, 50, 100]" layout="total, sizes, prev, pager, next, jumper" @current-change="handlePageChange" @size-change="handleSizeChange" /></div>
    </ElCard>
    <ElDrawer v-model="drawerVisible" :title="operateType === 'add' ? '新增员工' : '编辑员工'" size="900px" class="employee-drawer">
      <div class="drawer-content">
      <ElTabs v-model="activeTab">
        <ElTabPane label="基本信息" name="basic">
          <ElForm label-width="100px" :model="editingData">
            <ElDivider content-position="left">人员基本信息</ElDivider>
            <ElRow :gutter="20" class="mb-20px">
              <ElCol :span="8"><div class="text-center"><div class="mb-8px font-bold">头像</div><ElUpload class="avatar-uploader" :show-file-list="false" :before-upload="beforeUpload" :http-request="({ file }) => handleImageUpload(file as File, 'avatar')"><ElImage v-if="editingData.avatar" :src="getFileUrl(editingData.avatar)" fit="cover" class="w-100px h-100px rounded-full" /><div v-else class="w-100px h-100px rounded-full bg-gray-100 flex items-center justify-center border border-dashed border-gray-300 cursor-pointer hover:border-primary"><ElIcon :size="28" class="text-gray-400"><Plus /></ElIcon></div></ElUpload></div></ElCol>
              <ElCol :span="8"><div class="text-center"><div class="mb-8px font-bold">身份证正面</div><ElUpload class="id-card-uploader" :show-file-list="false" :before-upload="beforeUpload" :http-request="({ file }) => handleImageUpload(file as File, 'idCardFront')"><ElImage v-if="editingData.idCardFront" :src="getFileUrl(editingData.idCardFront)" fit="cover" class="w-160px h-100px rounded" /><div v-else class="w-160px h-100px rounded bg-gray-100 flex items-center justify-center border border-dashed border-gray-300 cursor-pointer hover:border-primary"><div class="text-center"><ElIcon :size="28" class="text-gray-400"><Plus /></ElIcon><div class="text-12px text-gray-400 mt-4px">身份证正面</div></div></div></ElUpload></div></ElCol>
              <ElCol :span="8"><div class="text-center"><div class="mb-8px font-bold">身份证反面</div><ElUpload class="id-card-uploader" :show-file-list="false" :before-upload="beforeUpload" :http-request="({ file }) => handleImageUpload(file as File, 'idCardBack')"><ElImage v-if="editingData.idCardBack" :src="getFileUrl(editingData.idCardBack)" fit="cover" class="w-160px h-100px rounded" /><div v-else class="w-160px h-100px rounded bg-gray-100 flex items-center justify-center border border-dashed border-gray-300 cursor-pointer hover:border-primary"><div class="text-center"><ElIcon :size="28" class="text-gray-400"><Plus /></ElIcon><div class="text-12px text-gray-400 mt-4px">身份证反面</div></div></div></ElUpload></div></ElCol>
            </ElRow>
            <ElRow :gutter="20"><ElCol :span="8"><ElFormItem label="人员编号" required :error="employeeNoError"><ElInput v-model="editingData.employeeNo" placeholder="请输入人员编号" @blur="handleCheckEmployeeNo" /></ElFormItem></ElCol><ElCol :span="8"><ElFormItem label="姓名" required><ElInput v-model="editingData.name" placeholder="请输入姓名" /></ElFormItem></ElCol><ElCol :span="8"><ElFormItem label="性别"><ElSelect v-model="editingData.gender" placeholder="请选择性别" style="width: 100%"><ElOption v-for="item in genderOptions" :key="item.dictValue" :label="item.dictLabel" :value="item.dictValue" /></ElSelect></ElFormItem></ElCol></ElRow>
            <ElRow :gutter="20"><ElCol :span="8"><ElFormItem label="身份证号"><ElInput v-model="editingData.idCard" placeholder="请输入身份证号" /></ElFormItem></ElCol><ElCol :span="8"><ElFormItem label="出生日期"><ElDatePicker v-model="editingData.birthDate" type="date" placeholder="选择日期" style="width: 100%" value-format="YYYY-MM-DD" /></ElFormItem></ElCol><ElCol :span="8"><ElFormItem label="民族"><ElSelect v-model="editingData.nation" placeholder="请选择民族" style="width: 100%"><ElOption v-for="item in nationOptions" :key="item.dictValue" :label="item.dictLabel" :value="item.dictValue" /></ElSelect></ElFormItem></ElCol></ElRow>
            <ElRow :gutter="20"><ElCol :span="8"><ElFormItem label="最高学历"><ElSelect v-model="editingData.highestEducation" placeholder="请选择学历" style="width: 100%"><ElOption v-for="item in educationOptions" :key="item.dictValue" :label="item.dictLabel" :value="item.dictValue" /></ElSelect></ElFormItem></ElCol><ElCol :span="8"><ElFormItem label="电话"><ElInput v-model="editingData.phone" placeholder="请输入电话" /></ElFormItem></ElCol><ElCol :span="8"><ElFormItem label="邮箱"><ElInput v-model="editingData.email" placeholder="请输入邮箱" /></ElFormItem></ElCol></ElRow>
            <ElRow :gutter="20"><ElCol :span="8"><ElFormItem label="员工类别"><ElSelect v-model="editingData.employeeType" placeholder="请选择员工类别" style="width: 100%"><ElOption v-for="item in employeeTypeOptions" :key="item.dictValue" :label="item.dictLabel" :value="item.dictValue" /></ElSelect></ElFormItem></ElCol><ElCol :span="8"><ElFormItem label="婚姻状况"><ElSelect v-model="editingData.maritalStatus" placeholder="请选择婚姻状况" style="width: 100%"><ElOption v-for="item in maritalStatusOptions" :key="item.dictValue" :label="item.dictLabel" :value="item.dictValue" /></ElSelect></ElFormItem></ElCol><ElCol :span="8"><ElFormItem label="政治面貌"><ElSelect v-model="editingData.politicalStatus" placeholder="请选择政治面貌" style="width: 100%"><ElOption v-for="item in politicalStatusOptions" :key="item.dictValue" :label="item.dictLabel" :value="item.dictValue" /></ElSelect></ElFormItem></ElCol></ElRow>
            <ElDivider content-position="left">工作信息</ElDivider>
            <ElRow :gutter="20"><ElCol :span="8"><ElFormItem label="所属组织" required><ElTreeSelect v-model="editingData.deptId" :data="orgTreeOptions" :props="{ children: 'children', label: 'unitName', value: 'id' }" node-key="id" placeholder="请选择组织" check-strictly style="width: 100%" :render-after-expand="false" filterable /></ElFormItem></ElCol><ElCol :span="8"><ElFormItem label="入职日期"><ElDatePicker v-model="editingData.entryDate" type="date" placeholder="选择日期" style="width: 100%" value-format="YYYY-MM-DD" /></ElFormItem></ElCol><ElCol :span="8"><ElFormItem label="转正日期"><ElDatePicker v-model="editingData.regularDate" type="date" placeholder="选择日期" style="width: 100%" value-format="YYYY-MM-DD" /></ElFormItem></ElCol></ElRow>
            <ElRow :gutter="20"><ElCol :span="8"><ElFormItem label="职务"><ElSelect v-model="editingData.duty" placeholder="请选择职务" style="width: 100%"><ElOption v-for="item in dutyOptions" :key="item.dictValue" :label="item.dictLabel" :value="item.dictValue" /></ElSelect></ElFormItem></ElCol><ElCol :span="8"><ElFormItem label="职位"><ElSelect v-model="editingData.position" placeholder="请选择职位" style="width: 100%"><ElOption v-for="item in positionOptions" :key="item.dictValue" :label="item.dictLabel" :value="item.dictValue" /></ElSelect></ElFormItem></ElCol><ElCol :span="8"><ElFormItem label="职级"><ElSelect v-model="editingData.jobLevel" placeholder="请选择职级" style="width: 100%"><ElOption v-for="item in jobLevelOptions" :key="item.dictValue" :label="item.dictLabel" :value="item.dictValue" /></ElSelect></ElFormItem></ElCol></ElRow>
            <ElRow :gutter="20"><ElCol :span="8"><ElFormItem label="状态"><ElSelect v-model="editingData.status" placeholder="请选择状态" style="width: 100%"><ElOption label="在职" :value="1" /><ElOption label="离职" :value="2" /></ElSelect></ElFormItem></ElCol></ElRow>
            <ElDivider content-position="left">联系信息</ElDivider>
            <ElRow :gutter="20"><ElCol :span="8"><ElFormItem label="籍贯"><ElInput v-model="editingData.nativePlace" placeholder="请输入籍贯" /></ElFormItem></ElCol><ElCol :span="8"><ElFormItem label="户籍地址"><ElInput v-model="editingData.registeredAddress" placeholder="请输入户籍地址" /></ElFormItem></ElCol><ElCol :span="8"><ElFormItem label="现居住地"><ElInput v-model="editingData.currentAddress" placeholder="请输入现居住地" /></ElFormItem></ElCol></ElRow>
            <ElDivider content-position="left">紧急联系人</ElDivider>
            <ElRow :gutter="20"><ElCol :span="8"><ElFormItem label="联系人"><ElInput v-model="editingData.emergencyContact" placeholder="请输入紧急联系人" /></ElFormItem></ElCol><ElCol :span="8"><ElFormItem label="关系"><ElSelect v-model="editingData.emergencyRelation" placeholder="请选择关系" style="width: 100%"><ElOption v-for="item in familyRelationOptions" :key="item.dictValue" :label="item.dictLabel" :value="item.dictValue" /></ElSelect></ElFormItem></ElCol><ElCol :span="8"><ElFormItem label="联系电话"><ElInput v-model="editingData.emergencyPhone" placeholder="请输入联系电话" /></ElFormItem></ElCol></ElRow>
          </ElForm>
        </ElTabPane>
        <ElTabPane label="教育经历" name="education">

          <div class="mb-10px"><ElButton type="primary" size="small" @click="addEducation"><template #icon><icon-ep-plus /></template>添加教育经历</ElButton></div>
          <div v-for="(edu, index) in educationList" :key="index" class="mb-20px p-15px bg-gray-50 rounded">
            <div class="mb-10px flex justify-between"><span class="font-bold">教育经历 {{ index + 1 }}</span><ElButton v-if="educationList.length > 1" type="danger" link size="small" @click="removeEducation(index)">删除</ElButton></div>
            <ElForm label-width="80px">
              <ElRow :gutter="20">
                <ElCol :span="8"><ElFormItem label="学校"><ElInput v-model="edu.schoolName" placeholder="请输入学校名称" /></ElFormItem></ElCol>
                <ElCol :span="8"><ElFormItem label="专业"><ElInput v-model="edu.major" placeholder="请输入专业" /></ElFormItem></ElCol>
                <ElCol :span="8"><ElFormItem label="学历"><ElSelect v-model="edu.education" placeholder="请选择学历" style="width: 100%"><ElOption v-for="item in educationOptions" :key="item.dictValue" :label="item.dictLabel" :value="item.dictValue" /></ElSelect></ElFormItem></ElCol>
              </ElRow>
              <ElRow :gutter="20">
                <ElCol :span="8"><ElFormItem label="是否全日制"><ElSelect v-model="edu.isFullTime" placeholder="请选择" style="width: 100%"><ElOption v-for="item in isFullTimeOptions" :key="item.dictValue" :label="item.dictLabel" :value="item.dictValue" /></ElSelect></ElFormItem></ElCol>
                <ElCol :span="8"><ElFormItem label="开学时间"><ElDatePicker v-model="edu.startDate" type="month" placeholder="选择时间" style="width: 100%" value-format="YYYY-MM" /></ElFormItem></ElCol>
                <ElCol :span="8"><ElFormItem label="毕业时间"><ElDatePicker v-model="edu.endDate" type="month" placeholder="选择时间" style="width: 100%" value-format="YYYY-MM" /></ElFormItem></ElCol>
              </ElRow>
              <ElRow :gutter="20">
                <ElCol :span="8">
                  <ElFormItem label="毕业证">
                    <ElUpload class="diploma-uploader" :show-file-list="false" :before-upload="beforeUpload" :http-request="({ file }) => handleDiplomaUpload(file as File, index)">
                      <ElImage v-if="edu.diplomaPhoto" :src="getFileUrl(edu.diplomaPhoto)" fit="cover" class="w-120px h-80px rounded" />
                      <div v-else class="w-120px h-80px rounded bg-gray-100 flex items-center justify-center border border-dashed border-gray-300 cursor-pointer hover:border-primary"><ElIcon :size="24" class="text-gray-400"><Plus /></ElIcon></div>
                    </ElUpload>
                  </ElFormItem>
                </ElCol>
              </ElRow>
            </ElForm>
          </div>
        </ElTabPane>
        <ElTabPane label="家庭成员" name="family">
          <div class="mb-10px"><ElButton type="primary" size="small" @click="addFamilyMember"><template #icon><icon-ep-plus /></template>添加家庭成员</ElButton></div>
          <div v-for="(member, index) in familyMemberList" :key="index" class="mb-20px p-15px bg-gray-50 rounded">
            <div class="mb-10px flex justify-between"><span class="font-bold">家庭成员 {{ index + 1 }}</span><ElButton v-if="familyMemberList.length > 1" type="danger" link size="small" @click="removeFamilyMember(index)">删除</ElButton></div>
            <ElForm label-width="80px">
              <ElRow :gutter="20">
                <ElCol :span="8"><ElFormItem label="姓名"><ElInput v-model="member.name" placeholder="请输入姓名" /></ElFormItem></ElCol>
                <ElCol :span="8"><ElFormItem label="关系"><ElSelect v-model="member.relation" placeholder="请选择关系" style="width: 100%"><ElOption v-for="item in familyRelationOptions" :key="item.dictValue" :label="item.dictLabel" :value="item.dictValue" /></ElSelect></ElFormItem></ElCol>
                <ElCol :span="8"><ElFormItem label="出生日期"><ElDatePicker v-model="member.birthDate" type="date" placeholder="选择日期" style="width: 100%" value-format="YYYY-MM-DD" /></ElFormItem></ElCol>
              </ElRow>
              <ElRow :gutter="20">
                <ElCol :span="8"><ElFormItem label="政治面貌"><ElSelect v-model="member.politicalStatus" placeholder="请选择" style="width: 100%"><ElOption v-for="item in politicalStatusOptions" :key="item.dictValue" :label="item.dictLabel" :value="item.dictValue" /></ElSelect></ElFormItem></ElCol>
                <ElCol :span="8"><ElFormItem label="工作单位"><ElInput v-model="member.workUnit" placeholder="请输入工作单位" /></ElFormItem></ElCol>
                <ElCol :span="8"><ElFormItem label="电话"><ElInput v-model="member.phone" placeholder="请输入电话" /></ElFormItem></ElCol>
              </ElRow>
            </ElForm>
          </div>
        </ElTabPane>
        <ElTabPane label="工作经历" name="work">
          <div class="mb-10px"><ElButton type="primary" size="small" @click="addWork"><template #icon><icon-ep-plus /></template>添加工作经历</ElButton></div>
          <div v-for="(work, index) in workList" :key="index" class="mb-20px p-15px bg-gray-50 rounded">
            <div class="mb-10px flex justify-between"><span class="font-bold">工作经历 {{ index + 1 }}</span><ElButton v-if="workList.length > 1" type="danger" link size="small" @click="removeWork(index)">删除</ElButton></div>
            <ElForm label-width="80px">
              <ElRow :gutter="20">
                <ElCol :span="8"><ElFormItem label="公司名称"><ElInput v-model="work.companyName" placeholder="请输入公司名称" /></ElFormItem></ElCol>
                <ElCol :span="8"><ElFormItem label="所在部门"><ElInput v-model="work.department" placeholder="请输入所在部门" /></ElFormItem></ElCol>
                <ElCol :span="8"><ElFormItem label="职位"><ElInput v-model="work.position" placeholder="请输入职位" /></ElFormItem></ElCol>
              </ElRow>
              <ElRow :gutter="20">
                <ElCol :span="8"><ElFormItem label="证明人"><ElInput v-model="work.witness" placeholder="请输入证明人" /></ElFormItem></ElCol>
                <ElCol :span="8"><ElFormItem label="证明电话"><ElInput v-model="work.witnessPhone" placeholder="请输入证明电话" /></ElFormItem></ElCol>
                <ElCol :span="8"><ElFormItem label="时间"><ElDatePicker v-model="work.startDate" type="month" placeholder="开始" style="width: 45%" value-format="YYYY-MM" /><span class="mx-5px">-</span><ElDatePicker v-model="work.endDate" type="month" placeholder="结束" style="width: 45%" value-format="YYYY-MM" /></ElFormItem></ElCol>
              </ElRow>
            </ElForm>
          </div>
        </ElTabPane>
        <ElTabPane label="证书" name="certificate">
          <div class="mb-10px"><ElButton type="primary" size="small" @click="addCertificate"><template #icon><icon-ep-plus /></template>添加证书</ElButton></div>
          <div v-for="(cert, index) in certificateList" :key="index" class="mb-20px p-15px bg-gray-50 rounded">
            <div class="mb-10px flex justify-between"><span class="font-bold">证书 {{ index + 1 }}</span><ElButton v-if="certificateList.length > 1" type="danger" link size="small" @click="removeCertificate(index)">删除</ElButton></div>
            <ElForm label-width="80px">
              <ElRow :gutter="20">
                <ElCol :span="8"><ElFormItem label="证书名称"><ElInput v-model="cert.certName" placeholder="请输入证书名称" /></ElFormItem></ElCol>
                <ElCol :span="8"><ElFormItem label="证书类型"><ElSelect v-model="cert.certType" placeholder="请选择证书类型" style="width: 100%"><ElOption v-for="item in certTypeOptions" :key="item.dictValue" :label="item.dictLabel" :value="item.dictValue" /></ElSelect></ElFormItem></ElCol>
                <ElCol :span="8"><ElFormItem label="证书等级"><ElSelect v-model="cert.certLevel" placeholder="请选择证书等级" style="width: 100%"><ElOption v-for="item in certLevelOptions" :key="item.dictValue" :label="item.dictLabel" :value="item.dictValue" /></ElSelect></ElFormItem></ElCol>
              </ElRow>
              <ElRow :gutter="20">
                <ElCol :span="8"><ElFormItem label="颁发日期"><ElDatePicker v-model="cert.issueDate" type="date" placeholder="选择日期" style="width: 100%" value-format="YYYY-MM-DD" /></ElFormItem></ElCol>
                <ElCol :span="8"><ElFormItem label="过期日期"><ElDatePicker v-model="cert.expireDate" type="date" placeholder="选择日期" style="width: 100%" value-format="YYYY-MM-DD" /></ElFormItem></ElCol>
                <ElCol :span="8">
                  <ElFormItem label="证书照片">
                    <ElUpload class="cert-uploader" :show-file-list="false" :before-upload="beforeUpload" :http-request="({ file }) => handleCertPhotoUpload(file as File, index)">
                      <ElImage v-if="cert.certPhoto" :src="getFileUrl(cert.certPhoto)" fit="cover" class="w-120px h-80px rounded" />
                      <div v-else class="w-120px h-80px rounded bg-gray-100 flex items-center justify-center border border-dashed border-gray-300 cursor-pointer hover:border-primary"><ElIcon :size="24" class="text-gray-400"><Plus /></ElIcon></div>
                    </ElUpload>
                  </ElFormItem>
                </ElCol>
              </ElRow>
            </ElForm>
          </div>
        </ElTabPane>
        <ElTabPane label="其他信息" name="extra" v-if="extraFieldOptions.length > 0">
          <ElForm label-width="100px">
            <ElRow :gutter="20">
              <ElCol :span="8" v-for="field in extraFieldOptions" :key="field.dictValue">
                <ElFormItem :label="field.dictLabel">
                  <ElInput v-model="extraFieldValues[field.dictValue]" :placeholder="`请输入${field.dictLabel}`" />
                </ElFormItem>
              </ElCol>
            </ElRow>
            <div v-if="extraFieldOptions.length === 0" class="text-center text-gray-400 py-20px">
              暂无自定义字段，请在字典管理中添加"员工扩展字段"(employee_extra_field)的字典数据
            </div>
          </ElForm>
        </ElTabPane>
      </ElTabs>
      </div>
      <template #footer><ElButton @click="drawerVisible = false">取消</ElButton><ElButton type="primary" :loading="submitLoading" @click="handleSubmit">保存</ElButton></template>
    </ElDrawer>
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
  background-color: #c0c4cc;
  border-radius: 3px;
}

.drawer-content::-webkit-scrollbar-track {
  background-color: #f5f7fa;
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
