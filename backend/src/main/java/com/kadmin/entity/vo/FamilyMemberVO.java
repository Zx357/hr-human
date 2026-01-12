package com.kadmin.entity.vo;

import lombok.Data;
import java.time.LocalDate;

/**
 * 家庭成员视图对象
 */
@Data
public class FamilyMemberVO {

    private Long id;

    /**
     * 员工ID
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
}