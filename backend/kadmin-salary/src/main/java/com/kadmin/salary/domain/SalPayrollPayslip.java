package com.kadmin.salary.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.kadmin.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 工资条（批次内员工月度汇总，核算时快照）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sal_payroll_payslip")
public class SalPayrollPayslip extends BaseEntity {

    private Long batchId;

    private Long employeeId;

    /** 冗余快照 */
    private String employeeNo;

    private String employeeName;

    /** 应发合计 */
    private BigDecimal grossPay;

    /** 扣款合计 */
    private BigDecimal totalDeduction;

    /** 实发 */
    private BigDecimal netPay;

    /** 员工已读：0-未读 1-已读 */
    private Integer readFlag;

    private java.time.LocalDateTime readTime;

    /** 员工已确认：0-未确认 1-已确认 */
    private Integer confirmFlag;

    private java.time.LocalDateTime confirmTime;

    @TableField(exist = false)
    private String yearMonth;
}
