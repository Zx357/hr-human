package com.kadmin.hr.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.kadmin.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 申请审批流转记录
 * 每个流程节点的审批动作落一条记录，全部节点通过后申请才置为已通过
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("hr_approval_record")
public class HrApprovalRecord extends BaseEntity {

    /**
     * 申请单ID
     */
    private Long applicationId;

    /**
     * 审批节点ID（sys_approval_node.id）
     */
    private Long nodeId;

    /**
     * 节点名称（冗余，防节点后续改名）
     */
    private String nodeName;

    /**
     * 节点顺序
     */
    private Integer sortOrder;

    /**
     * 审批人ID（员工ID）
     */
    private Long approverId;

    /**
     * 审批人姓名
     */
    private String approverName;

    /**
     * 审批动作：1-通过，2-拒绝
     */
    private Integer status;

    /**
     * 审批意见
     */
    private String comment;

    /**
     * 审批时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime approveTime;
}
