import { request } from '../request';

/** 菜单管理API */

// 获取菜单树形列表
export function fetchMenuTree() {
  return request<Api.System.Menu[]>({ url: '/system/menu/tree' });
}

// 获取菜单详情
export function fetchMenuDetail(id: number) {
  return request<Api.System.Menu>({ url: `/system/menu/${id}` });
}

// 新增菜单
export function createMenu(data: Api.System.MenuForm) {
  return request<boolean>({
    url: '/system/menu',
    method: 'post',
    data
  });
}

// 更新菜单
export function updateMenu(data: Api.System.MenuForm) {
  return request<boolean>({
    url: '/system/menu',
    method: 'put',
    data
  });
}

// 删除菜单
export function deleteMenu(id: number) {
  return request<boolean>({
    url: `/system/menu/${id}`,
    method: 'delete'
  });
}

/** 字典管理API */

// 获取字典类型列表
export function fetchDictTypeList(params?: Api.System.DictTypeSearchParams) {
  return request<Api.System.DictTypeList>({
    url: '/system/dict/type/list',
    params
  });
}

// 获取字典类型详情
export function fetchDictTypeDetail(id: number) {
  return request<Api.System.DictType>({ url: `/system/dict/type/${id}` });
}

// 新增字典类型
export function createDictType(data: Api.System.DictTypeForm) {
  return request<boolean>({
    url: '/system/dict/type',
    method: 'post',
    data
  });
}

// 更新字典类型
export function updateDictType(data: Api.System.DictTypeForm) {
  return request<boolean>({
    url: '/system/dict/type',
    method: 'put',
    data
  });
}

// 删除字典类型
export function deleteDictType(id: number) {
  return request<boolean>({
    url: `/system/dict/type/${id}`,
    method: 'delete'
  });
}

// 获取字典数据列表
export function fetchDictDataList(dictTypeId: number) {
  return request<Api.System.DictData[]>({
    url: '/system/dict/data/list',
    params: { dictTypeId }
  });
}

// 新增字典数据
export function createDictData(data: Api.System.DictDataForm) {
  return request<boolean>({
    url: '/system/dict/data',
    method: 'post',
    data
  });
}

// 更新字典数据
export function updateDictData(data: Api.System.DictDataForm) {
  return request<boolean>({
    url: '/system/dict/data',
    method: 'put',
    data
  });
}

// 删除字典数据
export function deleteDictData(id: number) {
  return request<boolean>({
    url: `/system/dict/data/${id}`,
    method: 'delete'
  });
}

// 根据字典编码获取字典数据
export function fetchDictDataByCode(dictCode: string) {
  return request<Api.System.DictData[]>({
    url: '/system/dict/data/byCode',
    params: { dictCode }
  });
}
/** 用户管理API */

// 获取用户列表
export function fetchUserList(params?: { username?: string; realName?: string; status?: number }) {
  return request<Api.System.User[]>({
    url: '/system/user/list',
    params
  });
}

/** 角色管理API */

// 获取所有启用的角色列表
export function fetchRoleList() {
  return request<Api.System.Role[]>({
    url: '/system/role/list'
  });
}
