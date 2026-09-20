package com.kadmin.common.utils;

/**
 * 密码强度策略：6-20 位，须同时包含字母和数字。
 * 仅在设置/修改密码时校验，登录与存量默认密码（123456）不受影响。
 */
public final class PasswordPolicy {

    private PasswordPolicy() {
    }

    /**
     * 校验密码，合法返回 null，否则返回错误提示
     */
    public static String check(String password) {
        if (password == null || password.length() < 6 || password.length() > 20) {
            return "密码长度须为6-20位";
        }
        if (!password.matches(".*[A-Za-z].*") || !password.matches(".*\\d.*")) {
            return "密码须同时包含字母和数字";
        }
        return null;
    }
}
