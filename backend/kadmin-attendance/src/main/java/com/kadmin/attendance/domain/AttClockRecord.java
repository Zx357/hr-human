package com.kadmin.attendance.domain;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("att_clock_record")
public class AttClockRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long employeeId;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime clockTime;
    /** 打卡日期（clock_time 的日期部分），与 (employee_id, clock_date, clock_type) 唯一键配合防重复打卡 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private java.time.LocalDate clockDate;
    private Integer clockType; // 1-上班 2-下班
    private Integer clockMethod; // 1-APP 2-考勤机 3-手动补卡
    private String location;
    private String deviceInfo;
    private String remark;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(exist = false)
    private String employeeName;
    @TableField(exist = false)
    private String employeeNo;
    @TableField(exist = false)
    private String companyName;
    @TableField(exist = false)
    private String deptName;
}
