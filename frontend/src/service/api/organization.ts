import { request } from '../request';

// ==================== 统一组织架构 ====================

/** 组织节点类型 */
export const OrgUnitType = {
  GROUP: 1, // 集团
  COMPANY: 2, // 公司
  DEPT: 3 // 部门
} as const;

/** 获取完整组织架构树 */
export function fetchOrgTree() {
  return request<Api.Organization.OrgUnit[]>({
    url: '/org-unit/tree',
    method: 'get'
  });
}

/** 获取带员工数量的组织架构树 */
export function fetchOrgTreeWithCount() {
  return request<Api.Organization.OrgUnit[]>({
    url: '/org-unit/tree-with-count',
    method: 'get'
  });
}

/** 获取组织统计数据 */
export function fetchOrgStatistics(id: number) {
  return request<Api.Organization.OrgStatistics>({
    url: `/org-unit/${id}/statistics`,
    method: 'get'
  });
}

/** 获取全局统计数据 */
export function fetchAllOrgStatistics() {
  return request<Api.Organization.OrgStatistics>({
    url: '/org-unit/statistics',
    method: 'get'
  });
}

/** 获取指定类型的组织列表 */
export function fetchOrgList(unitType?: number) {
  return request<Api.Organization.OrgUnit[]>({
    url: '/org-unit/list',
    method: 'get',
    params: { unitType }
  });
}

/** 获取组织详情 */
export function fetchOrgUnitById(id: number) {
  return request<Api.Organization.OrgUnit>({
    url: `/org-unit/${id}`,
    method: 'get'
  });
}

/** 创建组织节点 */
export function createOrgUnit(data: Api.Organization.OrgUnitForm) {
  return request<boolean>({
    url: '/org-unit',
    method: 'post',
    data
  });
}

/** 更新组织节点 */
export function updateOrgUnit(id: number, data: Api.Organization.OrgUnitForm) {
  return request<boolean>({
    url: `/org-unit/${id}`,
    method: 'put',
    data
  });
}

/** 删除组织节点 */
export function deleteOrgUnit(id: number) {
  return request<boolean>({
    url: `/org-unit/${id}`,
    method: 'delete'
  });
}

/** 检查编码是否存在 */
export function checkOrgUnitCode(unitCode: string, excludeId?: number) {
  return request<boolean>({
    url: '/org-unit/check-code',
    method: 'get',
    params: { unitCode, excludeId }
  });
}

// ==================== 兼容旧接口 ====================

/** 获取所有公司列表（org_unit 中 unitType=公司 的节点） */
export function fetchCompanyList() {
  return request<Api.Organization.OrgUnit[]>({
    url: '/org-unit/companies',
    method: 'get'
  });
}

/** 获取部门树形结构（org_unit 中某公司下的部门节点） */
export function fetchDepartmentTree(companyId?: number) {
  return request<Api.Organization.OrgUnit[]>({
    url: '/org-unit/depts',
    method: 'get',
    params: { parentId: companyId }
  });
}
