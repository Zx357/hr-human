package com.kadmin.salary.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.kadmin.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;


/**
 * 员工薪资档案（员工与方案的绑定）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sal_salary_archive")
public class SalSalaryArchive extends BaseEntity {

    private Long employeeId;

    private Long schemeId;

    /** 生效日期 */
    private java.time.LocalDate effectiveDate;

    /** 备注 */
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
    private String schemeName;
}
