package com.kadmin.entity.dto;

import lombok.Data;

/**
 * 员工扩展字段DTO
 */
@Data
public class EmployeeExtraDTO {
    /**
     * 字段编码
     */
    private String fieldCode;

    /**
     * 字段值
     */
    private String fieldValue;
}
