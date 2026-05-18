package com.kadmin.hr.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 家庭成员实体
 */
@Data
@TableName("hr_family_member")
public class HrFamilyMember {

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
     * 姓名
     */
    private String name;

    /**
     * 关系（字典值）
     */
    private String relation;

    /**
     * 出生日期
     */
    private LocalDate birthDate;

    /**
     * 政治面貌（字典值）
     */
    private String politicalStatus;

    /**
     * 工作单位
     */
    private String workUnit;

    /**
     * 职业
     */
    private String occupation;

    /**
     * 电话
     */
    private String phone;

    /**
     * 创建时间
     */
    private LocalDateTime createdTime;

    /**
     * 更新时间
     */
    private LocalDateTime updatedTime;
}