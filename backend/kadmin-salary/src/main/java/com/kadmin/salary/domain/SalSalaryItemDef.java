package com.kadmin.salary.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.kadmin.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 薪资项定义（公司级全量可选池）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sal_salary_item_def")
public class SalSalaryItemDef extends BaseEntity {

    /** 薪资项编码 */
    private String itemCode;

    /** 名称 */
    private String itemName;

    /** 方向：1-收入 2-扣款 */
    private Integer direction;

    /** 取值类型：1-固定 2-比例 3-考勤联动 4-手工 5-公式(预留) */
    private Integer valueType;

    /** 比例基项编码（value_type=2 时生效，如 basic_salary） */
    private String ratioBaseCode;

    /** 比例值（value_type=2 时生效，如 0.12） */
    private BigDecimal ratioValue;

    /** 考勤联动规则（value_type=3 时生效）：LATE_TIMES/LATE_MINUTES/ABSENT_DAYS/PERSONAL_LEAVE_HOURS/OVERTIME_HOURS/FULL_ATTENDANCE/ATTEND_DAYS */
    private String attRule;

    /** 单价/固定给付（value_type=3 时生效） */
    private BigDecimal unitPrice;

    /** 容忍值：迟到次数/分钟豁免（value_type=3 时生效） */
    private Integer tolerance;

    /** 公式（value_type=5 预留） */
    private String formula;

    /** 是否启用：1-是 0-否 */
    private Integer enabled;

    /** 排序 */
    private Integer sortOrder;

    /** 备注 */
    private String remark;
}
