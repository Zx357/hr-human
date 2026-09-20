import { useAuthStore } from '@/store/modules/auth';

/**
 * 判断当前用户是否为超级管理员（后端 ROLE_ADMIN 角色）。
 * 超管未配置任何按钮权限时也全部放行，避免管理员看不到操作按钮。
 */
export function isAdminRole() {
  const authStore = useAuthStore();
  return authStore.isLogin && authStore.userInfo.roles.includes('ROLE_ADMIN');
}

export function useAuth() {
  const authStore = useAuthStore();

  function hasAuth(codes: string | string[]) {
    if (!authStore.isLogin) {
      return false;
    }

    // 超级管理员放行所有按钮
    if (isAdminRole()) {
      return true;
    }

    const { buttons } = authStore.userInfo;

    if (typeof codes === 'string') {
      return matchPermission(buttons, codes);
    }

    return codes.some(code => matchPermission(buttons, code));
  }

  return {
    hasAuth
  };
}

/**
 * 单个权限匹配（支持通配符：*:*:* / hr:*:* / hr:employee:*）
 */
function matchPermission(buttons: string[], permission: string) {
  if (buttons.includes('*:*:*') || buttons.includes(permission)) {
    return true;
  }

  const parts = permission.split(':');
  if (parts.length >= 2) {
    if (buttons.includes(`${parts[0]}:*:*`)) {
      return true;
    }
    if (parts.length >= 3 && buttons.includes(`${parts[0]}:${parts[1]}:*`)) {
      return true;
    }
  }

  return false;
}
