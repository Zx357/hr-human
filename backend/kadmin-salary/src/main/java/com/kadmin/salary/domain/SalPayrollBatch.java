package com.kadmin.salary.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.kadmin.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 工资核算批次（月份×公司，账本式状态机）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sal_payroll_batch")
public class SalPayrollBatch extends BaseEntity {

    /** 核算月份 yyyy-MM（列名避开 MySQL 保留字 YEAR_MONTH） */
    @com.baomidou.mybatisplus.annotation.TableField("pay_month")
    private String yearMonth;

    private Long companyId;

    /** 状态：0-核算中 1-已核算 2-已确认 3-已发放 */
    private Integer status;

    private Integer employeeCount;

    private BigDecimal totalGross;

    private BigDecimal totalNet;

    /** 备注 */
    private String remark;

    @TableField(exist = false)
    private String companyName;
}
