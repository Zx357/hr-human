package com.kadmin.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Data
@TableName("att_daily_record")
public class AttDailyRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long employeeId;
    private LocalDate attDate;
    private Long shiftId;
    private Long periodId;
    private String periodName;
    private LocalTime scheduledIn;
    private LocalTime scheduledOut;
    private LocalTime actualIn;
    private LocalTime actualOut;
    private Integer status; // 0-未处理 1-正常 2-迟到 3-早退 4-旷工 5-请假 6-出差 7-迟到+早退
    private Integer lateMinutes;
    private Integer earlyMinutes;
    private BigDecimal workHours;
    private BigDecimal overtimeHours;
    private String remark;
    private Integer locked;
    private Long lockedBy;
    private LocalDateTime lockedTime;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    @TableField(exist = false)
    private String employeeName;
    @TableField(exist = false)
    private String employeeNo;
    @TableField(exist = false)
    private String companyName;
    @TableField(exist = false)
    private String deptName;
    @TableField(exist = false)
    private String shiftName;
    @TableField(exist = false)
    private List<AttDailyRecord> periods; // 各时段考勤明细
    
    // 申请相关时长（从hr_application关联查询）
    @TableField(exist = false)
    private BigDecimal overtimeDuration; // 加班时长
    @TableField(exist = false)
    private BigDecimal businessDuration; // 出差时长
    @TableField(exist = false)
    private BigDecimal annualLeaveDuration; // 年假时长
    @TableField(exist = false)
    private BigDecimal personalLeaveDuration; // 事假时长
    @TableField(exist = false)
    private BigDecimal sickLeaveDuration; // 病假时长
    @TableField(exist = false)
    private BigDecimal marriageLeaveDuration; // 婚假时长
    @TableField(exist = false)
    private BigDecimal maternityLeaveDuration; // 产假时长
    @TableField(exist = false)
    private BigDecimal paternityLeaveDuration; // 陪产假时长
    @TableField(exist = false)
    private BigDecimal bereavementLeaveDuration; // 丧假时长
}
