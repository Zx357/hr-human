package com.kadmin.system.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 操作日志
 */
@Data
@TableName("sys_oper_log")
public class SysOperLog implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 操作模块
     */
    private String module;

    /**
     * 操作类型
     */
    private String action;

    /**
     * 请求方式+路径
     */
    private String requestUri;

    /**
     * 操作人用户ID
     */
    private Long userId;

    /**
     * 操作人用户名
     */
    private String username;

    /**
     * 操作人IP
     */
    private String ip;

    /**
     * 请求参数（脱敏截断）
     */
    private String requestParams;

    /**
     * 操作结果：0-失败，1-成功
     */
    private Integer status;

    /**
     * 错误信息（失败时记录，截断）
     */
    private String errorMsg;

    /**
     * 耗时(毫秒)
     */
    private Long costMs;

    /**
     * 操作时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdTime;
}
