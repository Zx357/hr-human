package com.kadmin.system.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.kadmin.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 意见反馈实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_feedback")
public class SysFeedback extends BaseEntity {

    /**
     * 反馈类型：1-功能建议，2-问题反馈，3-其他
     */
    private Integer feedbackType;

    /**
     * 反馈内容
     */
    private String feedbackContent;

    /**
     * 联系人
     */
    private String contactName;

    /**
     * 联系电话
     */
    private String contactPhone;

    /**
     * 员工ID
     */
    private Long employeeId;

    /**
     * 员工工号
     */
    private String employeeNo;

    /**
     * 处理状态：0-待处理，1-处理中，2-已处理
     */
    private Integer status;

    /**
     * 回复内容
     */
    private String replyContent;

    /**
     * 回复时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime replyTime;
}
