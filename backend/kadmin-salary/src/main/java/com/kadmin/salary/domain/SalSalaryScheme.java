package com.kadmin.salary.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.kadmin.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;


/**
 * 薪资方案（把薪资项编排成一套结构）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sal_salary_scheme")
public class SalSalaryScheme extends BaseEntity {

    /** 方案编码 */
    private String schemeCode;

    /** 方案名称 */
    private String schemeName;

    /** 是否启用：1-是 0-否 */
    private Integer enabled;

    /** 排序 */
    private Integer sortOrder;

    /** 备注 */
    private String remark;

    @TableField(exist = false)
    private java.util.List<SalSchemeItem> items;

    @TableField(exist = false)
    private Integer itemCount;
}
