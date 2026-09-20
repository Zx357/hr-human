package com.kadmin.common.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 操作日志注解
 * 标注在需要审计的接口方法上，切面自动记录操作人、动作、结果
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface OperLog {

    /**
     * 操作模块（如：员工管理）
     */
    String module() default "";

    /**
     * 操作类型（如：新增/修改/删除/审批）
     */
    String action() default "";
}
