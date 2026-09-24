package com.kadmin.salary.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.kadmin.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 员工档案明细：定薪时按方案展开成行（FIXED 项金额，可覆盖方案默认值）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sal_salary_archive_item")
public class SalSalaryArchiveItem extends BaseEntity {

    private Long archiveId;

    private Long itemId;

    /** 金额 */
    private BigDecimal amount;

    @TableField(exist = false)
    private String itemCode;

    @TableField(exist = false)
    private String itemName;
}
