package com.kadmin.entity.dto;

import lombok.Data;
import java.time.LocalDate;

/**
 * 教育经历数据传输对象
 */
@Data
public class EducationDTO {

    private Long id;

    /**
     * 员工ID
     */
    private Long employeeId;

    /**
     * 学校名称
     */
    private String schoolName;

    /**
     * 毕业证照片
     */
    private String diplomaPhoto;

    /**
     * 是否全日制（字典值）
     */
    private String isFullTime;

    /**
     * 学历（字典值）
     */
    private String education;

    /**
     * 专业
     */
    private String major;

    /**
     * 开学时间
     */
    private LocalDate startDate;

    /**
     * 毕业时间
     */
    private LocalDate endDate;
}