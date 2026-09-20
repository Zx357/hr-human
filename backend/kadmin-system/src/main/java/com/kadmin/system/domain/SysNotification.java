package com.kadmin.system.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.kadmin.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 站内通知实体（审批结果、待办提醒、到期提醒等面向员工的通知）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_notification")
public class SysNotification extends BaseEntity {

    /**
     * 接收员工ID
     */
    private Long employeeId;

    /**
     * 通知类型：approval_result-审批结果，approval_todo-待审批，reminder-到期提醒
     */
    private String type;

    /**
     * 标题
     */
    private String title;

    /**
     * 内容
     */
    private String content;

    /**
     * 关联业务ID（申请ID/合同ID/证书ID等，用于去重与跳转）
     */
    private Long refId;

    /**
     * 移动端跳转路径
     */
    private String url;

    /**
     * 是否已读：0-未读，1-已读
     */
    private Integer readFlag;

    /**
     * 已读时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime readTime;
}
