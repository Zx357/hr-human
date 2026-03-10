package com.kadmin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 工作经历实体
 */
@Data
@TableName("hr_work_experience")
public class HrWorkExperience {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 人员ID
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
    private String startDate;

    /**
     * 结束日期
     */
    private String endDate;

    /**
     * 创建时间
     */
    private LocalDateTime createdTime;

    /**
     * 更新时间
     */
    private LocalDateTime updatedTime;
}