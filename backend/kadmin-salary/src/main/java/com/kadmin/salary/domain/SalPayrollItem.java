package com.kadmin.salary.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.kadmin.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 工资条明细行：每个薪资项一行（核算时快照项编码/名称，报表与工资条渲染的数据源）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sal_payroll_item")
public class SalPayrollItem extends BaseEntity {

    private Long payslipId;

    private Long itemId;

    /** 快照 */
    private String itemCode;

    private String itemName;

    /** 方向：1-收入 2-扣款 */
    private Integer direction;

    /** 取值类型（快照） */
    private Integer valueType;

    /** 金额 */
    private BigDecimal amount;

    /** 取值来源说明（如 "档案金额"、"迟到2次x50"、"手工录入"） */
    private String source;

    private Integer sortOrder;
}
