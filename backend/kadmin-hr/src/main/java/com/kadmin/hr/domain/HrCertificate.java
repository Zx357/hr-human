package com.kadmin.hr.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 证书信息实体
 */
@Data
@TableName("hr_certificate")
public class HrCertificate {

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
     * 证书名称
     */
    private String certName;

    /**
     * 证书照片
     */
    private String certPhoto;

    /**
     * 证书类型（字典值）
     */
    private String certType;

    /**
     * 证书等级（字典值）
     */
    private String certLevel;

    /**
     * 颁发日期
     */
    private LocalDate issueDate;

    /**
     * 过期日期
     */
    private LocalDate expireDate;

    /**
     * 创建时间
     */
    private LocalDateTime createdTime;

    /**
     * 更新时间
     */
    private LocalDateTime updatedTime;
}