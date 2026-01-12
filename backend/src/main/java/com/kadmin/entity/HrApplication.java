package com.kadmin.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.kadmin.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("hr_application")
public class HrApplication extends BaseEntity {
    private Long employeeId;
    private String appType; // leave-请假, overtime-加班, business-出差, makeup-补卡, exchange-换休, regularization-转正, transfer-调动, reward-奖励, punish-惩罚, resignation-离职
    private String title;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;
    private BigDecimal duration; // 时长（小时或天）
    private String reason;
    
    // 转正申请字段
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate regularDate;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate probationEndDate;
    private String evaluation;
    private String newEmployeeType;
    
    // 调动申请字段
    private String transferType;
    private Long fromCompanyId;
    private Long toCompanyId;
    private Long fromDeptId;
    private Long toDeptId;
    private String fromPosition;
    private String toPosition;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate effectDate;
    
    // 奖惩申请字段
    private Integer rewardType; // 1-奖励，2-惩罚
    private String category;
    private BigDecimal amount;
    
    // 离职申请字段
    private String resignType;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate lastWorkDate;
    private Long handoverTo;
    
    private Integer status; // 0-待审批, 1-已通过, 2-已拒绝, 3-已撤销
    private Long approveBy;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime approveTime;
    private String approveRemark;
    private String remark;

    @TableField(exist = false)
    private String employeeName;
    @TableField(exist = false)
    private String employeeNo;
    @TableField(exist = false)
    private String deptName;
    @TableField(exist = false)
    private String companyName;
    @TableField(exist = false)
    private String fromCompanyName;
    @TableField(exist = false)
    private String toCompanyName;
    @TableField(exist = false)
    private String fromDeptName;
    @TableField(exist = false)
    private String toDeptName;
    @TableField(exist = false)
    private String handoverToName;
    @TableField(exist = false)
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate entryDate;
}
