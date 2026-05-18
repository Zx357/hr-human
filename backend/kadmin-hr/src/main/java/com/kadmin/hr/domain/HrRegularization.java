package com.kadmin.hr.domain;

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
@TableName("hr_regularization")
public class HrRegularization extends BaseEntity {
    private Long employeeId;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate applyDate;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate regularDate;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate probationEndDate;
    private String evaluation;
    /**
     * 转正后员工类别（字典值 employee_type）
     */
    private String newEmployeeType;
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
    private String deptName;
    @TableField(exist = false)
    private String positionName;
    @TableField(exist = false)
    private String companyName;
    @TableField(exist = false)
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate entryDate;
}
