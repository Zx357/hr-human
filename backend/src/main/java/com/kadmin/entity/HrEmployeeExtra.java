package com.kadmin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 员工扩展信息实体（自定义字段）
 */
@Data
@TableName("hr_employee_extra")
public class HrEmployeeExtra {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 员工ID
     */
    private Long employeeId;

    /**
     * 字段编码（对应字典值的dictValue）
     */
    private String fieldCode;

    /**
     * 字段值
     */
    private String fieldValue;

    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;
}
