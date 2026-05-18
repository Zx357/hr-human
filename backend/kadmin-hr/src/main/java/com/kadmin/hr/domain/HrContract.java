package com.kadmin.hr.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.kadmin.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 合同实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("hr_contract")
public class HrContract extends BaseEntity {

    /**
     * 合同编号
     */
    private String contractNo;

    /**
     * 员工ID
     */
    private Long employeeId;

    /**
     * 合同类型：字典值 contract_type
     */
    private String contractType;

    /**
     * 开始日期
     */
    private LocalDate startDate;

    /**
     * 结束日期
     */
    private LocalDate endDate;

    /**
     * 签订日期
     */
    private LocalDate signDate;

    /**
     * 试用期（月）
     */
    private Integer probationMonths;

    /**
     * 合同薪资
     */
    private BigDecimal salary;

    /**
     * 状态：1-生效中，2-即将到期，3-已到期，4-已终止
     */
    private Integer status;

    /**
     * 备注
     */
    private String remark;

    /**
     * 合同图片（多张，逗号分隔）
     */
    private String contractImages;

    /**
     * 合同次数（第几次合同）
     */
    private Integer contractCount;

    // ========== 非数据库字段 ==========

    /**
     * 员工姓名
     */
    @TableField(exist = false)
    private String employeeName;

    /**
     * 员工工号
     */
    @TableField(exist = false)
    private String employeeNo;

    /**
     * 公司名称
     */
    @TableField(exist = false)
    private String companyName;
}
