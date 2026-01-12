package com.kadmin.entity;

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
