package com.kadmin.salary.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.kadmin.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 方案明细：方案包含哪些薪资项及固定项默认值
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sal_scheme_item")
public class SalSchemeItem extends BaseEntity {

    private Long schemeId;

    private Long itemId;

    /** FIXED 项默认金额 */
    private BigDecimal defaultAmount;

    /** 排序（核算与展示顺序，比例/公式项只能引用排在前面的项） */
    private Integer sortOrder;

    @TableField(exist = false)
    private String itemCode;

    @TableField(exist = false)
    private String itemName;
}
