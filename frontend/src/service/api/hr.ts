import { request } from '../request';

// ==================== 员工管理 ====================

/** 分页查询员工列表 */
export function fetchEmployeePage(params: {
  pageNum?: number;
  pageSize?: number;
  name?: string;
  employeeNo?: string;
  companyId?: number;
  deptId?: number;
  orgIds?: string;
  status?: number;
}) {
  return request<Api.Common.PageResult<Api.Hr.Employee>>({
    url: '/employee/page',
    method: 'get',
    params
  });
}

/** 员工批量导入（xlsx） */
export function importEmployees(file: File) {
  const formData = new FormData();
  formData.append('file', file);
  return request<Api.Hr.ImportResult>({
    url: '/employee/import',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  });
}

/** 获取员工列表 */
export function fetchEmployeeList(params?: { deptId?: number; status?: number }) {
  return request<Api.Hr.Employee[]>({
    url: '/employee/list',
    method: 'get',
    params
  });
}

/** 获取部门员工列表 */
export function fetchEmployeesByDept(deptId: number) {
  return request<Api.Hr.Employee[]>({
    url: `/employee/dept/${deptId}`,
    method: 'get'
  });
}

/** 获取员工详情 */
export function fetchEmployeeById(id: number) {
  return request<Api.Hr.Employee>({
    url: `/employee/${id}`,
    method: 'get'
  });
}

/** 创建员工 */
export function createEmployee(data: Api.Hr.EmployeeForm) {
  return request<boolean>({
    url: '/employee',
    method: 'post',
    data
  });
}

/** 更新员工 */
export function updateEmployee(id: number, data: Api.Hr.EmployeeForm) {
  return request<boolean>({
    url: '/employee',
    method: 'put',
    data: { ...data, id }
  });
}

/** 删除员工 */
export function deleteEmployee(id: number) {
  return request<boolean>({
    url: `/employee/${id}`,
    method: 'delete'
  });
}

/** 检查工号是否存在 */
export function checkEmployeeNo(employeeNo: string, excludeId?: number) {
  return request<boolean>({
    url: '/employee/check-no',
    method: 'get',
    params: { employeeNo, excludeId }
  });
}

/** 生成下一个员工编号 */
export function generateEmployeeNo() {
  return request<string>({
    url: '/employee/next-no',
    method: 'get'
  });
}
