package com.kadmin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 教育经历实体
 */
@Data
@TableName("hr_education")
public class HrEducation {

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
    private String startDate;

    /**
     * 毕业时间
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