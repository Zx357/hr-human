package com.kadmin.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.kadmin.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("hr_transfer")
public class HrTransfer extends BaseEntity {
    private Long employeeId;
    private String transferType;
    private Long fromCompanyId;
    private Long toCompanyId;
    private Long fromDeptId;
    private Long toDeptId;
    private String fromPosition;
    private String toPosition;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate effectDate;
    private String reason;
    private Integer status;
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
    private String fromCompanyName;
    @TableField(exist = false)
    private String toCompanyName;
    @TableField(exist = false)
    private String fromDeptName;
    @TableField(exist = false)
    private String toDeptName;
}
