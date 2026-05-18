package com.kadmin.common;

import lombok.Getter;
import lombok.AllArgsConstructor;

/**
 * 响应状态码枚举
 * 适配soybean-admin前端框架，code为字符串格式
 */
@Getter
@AllArgsConstructor
public enum ResultCode {

    // 成功 - soybean-admin默认成功码为"0000"
    SUCCESS("0000", "操作成功"),

    // 客户端错误 4xx
    BAD_REQUEST("0400", "请求参数错误"),
    UNAUTHORIZED("0401", "未授权，请先登录"),
    FORBIDDEN("0403", "没有权限访问"),
    NOT_FOUND("0404", "资源不存在"),
    METHOD_NOT_ALLOWED("0405", "请求方法不允许"),
    CONFLICT("0409", "数据冲突"),
    UNPROCESSABLE_ENTITY("0422", "请求参数验证失败"),

    // 服务端错误 5xx
    ERROR("0500", "服务器内部错误"),
    SERVICE_UNAVAILABLE("0503", "服务暂不可用"),

    // 业务错误 1xxx
    USER_NOT_FOUND("1001", "用户不存在"),
    USER_PASSWORD_ERROR("1002", "用户名或密码错误"),
    USER_DISABLED("1003", "用户已被禁用"),
    USER_EXISTS("1004", "用户已存在"),

    // Token相关 - 使用soybean-admin的登出码和过期码
    TOKEN_INVALID("8888", "Token无效"),
    TOKEN_EXPIRED("9999", "Token已过期"),

    PARAM_ERROR("1201", "参数错误"),
    PARAM_MISSING("1202", "参数缺失"),

    DATA_NOT_FOUND("1301", "数据不存在"),
    DATA_EXISTS("1302", "数据已存在"),
    DATA_ERROR("1303", "数据错误"),

    FILE_UPLOAD_ERROR("1401", "文件上传失败"),
    FILE_TYPE_ERROR("1402", "文件类型不支持"),
    FILE_SIZE_ERROR("1403", "文件大小超出限制"),

    APPROVAL_ERROR("1501", "审批流程错误"),
    APPROVAL_NOT_FOUND("1502", "审批流程不存在");

    /**
     * 状态码
     */
    private final String code;

    /**
     * 消息
     */
    private final String message;
}