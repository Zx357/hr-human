import type { App, Directive, DirectiveBinding } from 'vue';
import { useAuthStore } from '@/store/modules/auth';

/**
 * 权限指令
 * 用法：
 * v-permission="'system:user:add'" - 单个权限
 * v-permission="['system:user:add', 'system:user:edit']" - 多个权限（满足任一即可）
 * v-permission.all="['system:user:add', 'system:user:edit']" - 多个权限（需要全部满足）
 */
const permissionDirective: Directive = {
  mounted(el: HTMLElement, binding: DirectiveBinding) {
    checkAndUpdatePermission(el, binding);
  },
  updated(el: HTMLElement, binding: DirectiveBinding) {
    checkAndUpdatePermission(el, binding);
  }
};

function checkAndUpdatePermission(el: HTMLElement, binding: DirectiveBinding) {
  const { value, modifiers } = binding;
  const authStore = useAuthStore();
  const buttons = authStore.userInfo?.buttons || [];

  // 调试日志

  // 检查是否有通配符权限
  if (buttons.includes('*:*:*')) {
    el.style.display = '';
    return;
  }

  if (value) {
    const requiredPermissions = Array.isArray(value) ? value : [value];
    const permitted = modifiers.all
      ? requiredPermissions.every(p => checkPermission(buttons, p))
      : requiredPermissions.some(p => checkPermission(buttons, p));

    if (!permitted) {
      // 隐藏元素而不是移除，这样可以在权限变化时重新显示
      el.style.display = 'none';
    } else {
      el.style.display = '';
    }
  }
}

/**
 * 检查单个权限（支持通配符）
 */
function checkPermission(userPermissions: string[], permission: string): boolean {
  if (userPermissions.includes(permission)) {
    return true;
  }

  // 检查通配符权限
  const parts = permission.split(':');
  if (parts.length >= 2) {
    // 检查模块级通配符 hr:*:*
    const moduleWildcard = `${parts[0]}:*:*`;
    if (userPermissions.includes(moduleWildcard)) {
      return true;
    }

    // 检查功能级通配符 hr:employee:*
    if (parts.length >= 3) {
      const functionWildcard = `${parts[0]}:${parts[1]}:*`;
      if (userPermissions.includes(functionWildcard)) {
        return true;
      }
    }
  }

  return false;
}

/**
 * 权限检查函数（可在组件中使用）
 */
export function hasPermission(permission: string | string[], requireAll = false): boolean {
  const authStore = useAuthStore();
  const buttons = authStore.userInfo?.buttons || [];

  // 检查是否有通配符权限
  if (buttons.includes('*:*:*')) {
    return true;
  }

  const requiredPermissions = Array.isArray(permission) ? permission : [permission];

  if (requireAll) {
    return requiredPermissions.every(p => checkPermission(buttons, p));
  }
  return requiredPermissions.some(p => checkPermission(buttons, p));
}

/**
 * 角色检查函数
 */
export function hasRole(role: string | string[], requireAll = false): boolean {
  const authStore = useAuthStore();
  const roles = authStore.userInfo?.roles || [];

  const requiredRoles = Array.isArray(role) ? role : [role];

  if (requireAll) {
    return requiredRoles.every(r => roles.includes(r));
  }
  return requiredRoles.some(r => roles.includes(r));
}

/**
 * 注册权限指令
 */
export function setupPermissionDirective(app: App) {
  app.directive('permission', permissionDirective);
}

export default permissionDirective;
