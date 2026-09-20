import { ref } from 'vue';
import { fetchCompanyList, fetchDepartmentTree, fetchOrgTree } from '@/service/api/organization';

/**
 * 组织架构 composable：封装 公司→部门 级联加载与完整组织树的加载逻辑
 *
 * - orgTreeOptions + loadOrgTree：完整组织树（用于统一组织选择器）
 * - companies + loadCompanies：公司列表
 * - departments + loadDepartments：某公司下的部门树
 */
export function useOrgTree() {
  const orgTreeOptions = ref<Api.Organization.OrgUnit[]>([]);
  const companies = ref<Api.Organization.OrgUnit[]>([]);
  const departments = ref<Api.Organization.OrgUnit[]>([]);

  /** 加载完整组织树 */
  async function loadOrgTree() {
    try {
      const res = await fetchOrgTree();
      orgTreeOptions.value = res.data || [];
    } catch {
      // 请求层已统一弹错
    }
  }

  /** 加载公司列表 */
  async function loadCompanies() {
    try {
      const res = await fetchCompanyList();
      companies.value = res.data || [];
    } catch {
      // 请求层已统一弹错
    }
  }

  /** 按公司加载部门树，companyId 为空时清空 */
  async function loadDepartments(companyId?: number) {
    if (!companyId) {
      departments.value = [];
      return;
    }
    try {
      const res = await fetchDepartmentTree(companyId);
      departments.value = res.data || [];
    } catch {
      departments.value = [];
    }
  }

  return { orgTreeOptions, loadOrgTree, companies, loadCompanies, departments, loadDepartments };
}
