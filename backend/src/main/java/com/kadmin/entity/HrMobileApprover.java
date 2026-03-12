package com.kadmin.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.kadmin.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 移动端审批权限
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("hr_mobile_approver")
public class HrMobileApprover extends BaseEntity {

    /**
     * 员工ID
     */
    private Long employeeId;

    /**
     * 可审批的申请类型，逗号分隔，为空表示全部类型
     */
    private String appTypes;

    // === 非数据库字段 ===

    @TableField(exist = false)
    private String employeeName;

    @TableField(exist = false)
    private String employeeNo;

    @TableField(exist = false)
    private String deptName;
}
