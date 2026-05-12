package com.kadmin.annotation;

import java.lang.annotation.*;

/**
 * 权限校验注解
 * 用于标注需要特定权限才能访问的方法
 */
@Target({ ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequiresPermission {

    /**
     * 需要的权限标识
     */
    String[] value() default {};

    /**
     * 验证模式：AND-需要所有权限，OR-需要任一权限
     */
    Logical logical() default Logical.OR;

    /**
     * 逻辑运算符
     */
    enum Logical {
        AND, OR
    }
}




