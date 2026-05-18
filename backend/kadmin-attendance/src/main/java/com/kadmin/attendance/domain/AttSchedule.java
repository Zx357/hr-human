package com.kadmin.attendance.domain;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 员工排班实体
 */
@Data
@TableName("att_schedule")
public class AttSchedule {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /** 员工ID */
    private Long employeeId;
    
    /** 班次ID */
    private Long shiftId;
    
    /** 排班日期 */
    private LocalDate scheduleDate;
    
    /** 备注 */
    private String remark;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedTime;
    
    /** 员工姓名 */
    @TableField(exist = false)
    private String employeeName;
    
    /** 员工工号 */
    @TableField(exist = false)
    private String employeeNo;
    
    /** 部门名称 */
    @TableField(exist = false)
    private String deptName;
    
    /** 班次名称 */
    @TableField(exist = false)
    private String shiftName;
    
    /** 班次编码 */
    @TableField(exist = false)
    private String shiftCode;
}
