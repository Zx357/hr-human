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

// 获取用户详情（含关联员工ID）
export function fetchGetUserById(id: number) {
  return request<{ user: Api.System.User; roleIds: number[] }>({
    url: `/system/user/${id}`,
    method: 'get'
  });
}

/** 角色管理API */

// 获取所有启用的角色列表
export function fetchRoleList() {
  return request<Api.System.Role[]>({
    url: '/system/role/list'
  });
}

/** 操作日志API */

// 分页查询操作日志
export function fetchOperLogPage(params?: Api.System.OperLogSearchParams) {
  return request<Api.Common.PageResult<Api.System.OperLogRecord>>({
    url: '/system/oper-log/page',
    method: 'get',
    params
  });
}

// 清理N天前的操作日志
export function cleanOperLogs(days: number) {
  return request<boolean>({
    url: '/system/oper-log/clean',
    method: 'delete',
    params: { days }
  });
}

// ==================== 通知公告 ====================

export interface SysNoticeItem {
  id: number;
  noticeTitle: string;
  noticeType: number;
  noticeContent: string;
  status: number;
  publishTime?: string;
  createdTime?: string;
}

export function fetchNoticePage(params: {
  current: number;
  size: number;
  noticeTitle?: string;
  noticeType?: number;
  status?: number;
}) {
  return request<Api.Common.PageResult<SysNoticeItem>>({
    url: '/system/notice/page',
    method: 'get',
    params
  });
}

export function saveNotice(data: Partial<SysNoticeItem>) {
  return request<boolean>({
    url: '/system/notice',
    method: data.id ? 'put' : 'post',
    data
  });
}

export function deleteNotice(id: number) {
  return request<boolean>({ url: `/system/notice/${id}`, method: 'delete' });
}

// ==================== 意见反馈 ====================

export function fetchFeedbackPage(params: Record<string, unknown>) {
  return request<Api.Common.PageResult<Record<string, unknown>>>({
    url: '/system/feedback/page',
    method: 'get',
    params
  });
}

export function replyFeedback(data: { id: number; replyContent: string; status?: number }) {
  return request<boolean>({ url: '/system/feedback/reply', method: 'put', data });
}

// ==================== 移动端菜单 ====================

export function fetchMobileMenuPage(params: Record<string, unknown>) {
  return request<Api.Common.PageResult<Record<string, unknown>>>({
    url: '/system/mobile-menu/page',
    method: 'get',
    params
  });
}

export function deleteMobileMenu(id: number) {
  return request<boolean>({ url: `/system/mobile-menu/${id}`, method: 'delete' });
}

export function toggleMobileMenuStatus(id: number) {
  return request<boolean>({ url: `/system/mobile-menu/toggle-status/${id}`, method: 'put' });
}

export function saveMobileMenu(data: Record<string, unknown>) {
  return request<boolean>({
    url: '/system/mobile-menu',
    method: data.id ? 'put' : 'post',
    data
  });
}

export function handleFeedbackReply(id: number, data: { replyContent: string; status?: number }) {
  return request<boolean>({ url: `/system/feedback/${id}/reply`, method: 'put', data });
}

export function changeFeedbackStatus(id: number, status: number) {
  return request<boolean>({ url: `/system/feedback/${id}/status`, method: 'put', data: { status } });
}

export function deleteFeedback(id: number) {
  return request<boolean>({ url: `/system/feedback/${id}`, method: 'delete' });
}
