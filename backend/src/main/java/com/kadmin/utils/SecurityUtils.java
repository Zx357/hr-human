package com.kadmin.utils;

import com.kadmin.security.LoginUser;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 安全工具类
 */
public class SecurityUtils {

    /**
     * 获取当前登录用户
     */
    public static LoginUser getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof LoginUser) {
            return (LoginUser) authentication.getPrincipal();
        }
        return null;
    }

    /**
     * 获取当前登录用户ID
     */
    public static Long getCurrentUserId() {
        LoginUser loginUser = getCurrentUser();
        return loginUser != null ? loginUser.getUserId() : null;
    }

    /**
     * 获取当前登录用户名
     */
    public static String getCurrentUsername() {
        LoginUser loginUser = getCurrentUser();
        return loginUser != null ? loginUser.getUsername() : null;
    }

    /**
     * 判断是否已登录
     */
    public static boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.isAuthenticated()
                && authentication.getPrincipal() instanceof LoginUser;
    }

    /**
     * 判断是否是管理员
     */
    public static boolean isAdmin() {
        LoginUser loginUser = getCurrentUser();
        if (loginUser != null && loginUser.getRoles() != null) {
            return loginUser.getRoles().stream()
                    .anyMatch(role -> "ROLE_ADMIN".equals(role));
        }
        return false;
    }
}