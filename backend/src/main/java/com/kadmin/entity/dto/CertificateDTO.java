package com.kadmin.entity.dto;

import lombok.Data;
import java.time.LocalDate;

/**
 * 证书数据传输对象
 */
@Data
public class CertificateDTO {

    private Long id;

    /**
     * 员工ID
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
}