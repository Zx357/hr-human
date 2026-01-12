package com.kadmin.entity.vo;

import lombok.Data;
import java.time.LocalDate;

/**
 * 工作经历视图对象
 */
@Data
public class WorkExperienceVO {

    private Long id;

    /**
     * 员工ID
     */
    private Long employeeId;

    /**
     * 公司名称
     */
    private String companyName;

    /**
     * 公司地址
     */
    private String companyAddress;

    /**
     * 所在部门
     */
    private String department;

    /**
     * 职位
     */
    private String position;

    /**
     * 证明人
     */
    private String witness;

    /**
     * 证明电话
     */
    private String witnessPhone;

    /**
     * 开始日期
     */
    private LocalDate startDate;

    /**
     * 结束日期
     */
    private LocalDate endDate;
}