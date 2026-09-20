package com.kadmin.framework.aspectj;

import com.kadmin.common.annotation.RequiresPermission;
import com.kadmin.common.Result;
import com.kadmin.common.ResultCode;
import com.kadmin.common.security.LoginUser;
import com.kadmin.common.utils.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Set;

/**
 * 权限校验切面
 *
 * 渐进式启用：仅拦截标注了 {@link RequiresPermission} 的接口，无注解接口零影响。
 * - ROLE_ADMIN 一律放行（与 SecurityUtils/SecurityConfig 的 ROLE_ADMIN 约定一致）；
 * - 纯移动端员工（仅 ROLE_EMPLOYEE）放行：/hr/application/** 等共享接口的员工侧
 *   鉴权由接口内部的本人/审批人校验完成，此处不做按钮级权限拦截；
 * - 其他用户按会话中的权限集合判断（支持 hr:employee:* / hr:*:* 通配符），
 *   无权限返回统一错误码 ResultCode.FORBIDDEN。
 */
@Slf4j
@Aspect
@Component
@Order(1)
public class PermissionAspect {

    /**
     * 权限校验
     */
    @Around("@annotation(com.kadmin.common.annotation.RequiresPermission)")
    public Object checkPermission(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        RequiresPermission annotation = method.getAnnotation(RequiresPermission.class);
        if (annotation == null) {
            // 兼容类级标注（注解 @Target 含 TYPE）
            annotation = joinPoint.getTarget().getClass().getAnnotation(RequiresPermission.class);
        }

        if (annotation == null) {
            return joinPoint.proceed();
        }

        String[] requiredPermissions = annotation.value();
        RequiresPermission.Logical logical = annotation.logical();

        // 获取当前用户
        LoginUser loginUser = SecurityUtils.getCurrentUser();
        if (loginUser == null) {
            return Result.error(ResultCode.UNAUTHORIZED);
        }

        // 超级管理员拥有所有权限（与 SecurityUtils/SecurityConfig 的 ROLE_ADMIN 约定一致）
        if (SecurityUtils.isAdmin()) {
            return joinPoint.proceed();
        }

        // 纯移动端员工（仅 ROLE_EMPLOYEE，无任何系统角色）不做按钮级权限校验：
        // 其可访问的共享接口（如申请提交/审批）由接口内部的本人/审批人校验兜底
        if (isPureMobileEmployee(loginUser)) {
            return joinPoint.proceed();
        }

        // 获取用户权限
        Set<String> userPermissions = loginUser.getPermissions();

        // 检查权限
        boolean hasPermission = checkPermissions(userPermissions, requiredPermissions, logical);

        if (!hasPermission) {
            log.warn("用户 {} 没有权限 {} 访问 {}",
                    loginUser.getUsername(),
                    Arrays.toString(requiredPermissions),
                    method.getName());
            return Result.error(ResultCode.FORBIDDEN);
        }

        return joinPoint.proceed();
    }

    /**
     * 是否为纯移动端员工（仅 ROLE_EMPLOYEE 角色，与 ApplicationController 的判定约定一致）
     */
    private boolean isPureMobileEmployee(LoginUser loginUser) {
        return loginUser.getRoles() != null && loginUser.getRoles().size() == 1
                && loginUser.getRoles().contains("ROLE_EMPLOYEE");
    }

    /**
     * 检查权限
     */
    private boolean checkPermissions(Set<String> userPermissions, String[] requiredPermissions,
            RequiresPermission.Logical logical) {
        if (userPermissions == null || userPermissions.isEmpty()) {
            return false;
        }

        if (requiredPermissions == null || requiredPermissions.length == 0) {
            return true;
        }

        // 检查是否有通配符权限
        if (userPermissions.contains("*:*:*")) {
            return true;
        }

        if (logical == RequiresPermission.Logical.AND) {
            // 需要所有权限
            for (String permission : requiredPermissions) {
                if (!hasPermission(userPermissions, permission)) {
                    return false;
                }
            }
            return true;
        } else {
            // 需要任一权限
            for (String permission : requiredPermissions) {
                if (hasPermission(userPermissions, permission)) {
                    return true;
                }
            }
            return false;
        }
    }

    /**
     * 检查单个权限（支持通配符）
     */
    private boolean hasPermission(Set<String> userPermissions, String permission) {
        if (userPermissions.contains(permission)) {
            return true;
        }

        // 检查通配符权限
        // 例如：hr:employee:* 可以匹配 hr:employee:add
        String[] parts = permission.split(":");
        if (parts.length >= 2) {
            // 检查模块级通配符 hr:*:*
            String moduleWildcard = parts[0] + ":*:*";
            if (userPermissions.contains(moduleWildcard)) {
                return true;
            }

            // 检查功能级通配符 hr:employee:*
            if (parts.length >= 3) {
                String functionWildcard = parts[0] + ":" + parts[1] + ":*";
                if (userPermissions.contains(functionWildcard)) {
                    return true;
                }
            }
        }

        return false;
    }
}
