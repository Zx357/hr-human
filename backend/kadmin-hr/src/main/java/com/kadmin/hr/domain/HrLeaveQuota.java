package com.kadmin.hr.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.kadmin.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 假期额度实体（按员工+年度+假期类型）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("hr_leave_quota")
public class HrLeaveQuota extends BaseEntity {

    /**
     * 员工ID
     */
    private Long employeeId;

    /**
     * 年度
     */
    private Integer year;

    /**
     * 假期类型（字典值 leave_type：1-年假 2-事假 3-病假 4-婚假 5-产假 6-陪产假 7-丧假）
     */
    private String leaveType;

    /**
     * 额度总时长（小时）
     */
    private BigDecimal totalHours;

    /**
     * 已使用时长（小时）
     */
    private BigDecimal usedHours;

    // ========== 非数据库字段 ==========

    @TableField(exist = false)
    private String employeeName;

    @TableField(exist = false)
    private String employeeNo;
}
