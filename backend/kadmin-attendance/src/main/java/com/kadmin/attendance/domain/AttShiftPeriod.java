package com.kadmin.attendance.domain;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

/**
 * 班次时段实体
 */
@Data
@TableName("att_shift_period")
public class AttShiftPeriod {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /** 班次ID */
    private Long shiftId;
    
    /** 时段名称 */
    private String periodName;
    
    /** 上班时间 */
    private String startTime;
    
    /** 下班时间 */
    private String endTime;
    
    /** 是否跨天 0-否 1-是 */
    private Integer crossDay;
    
    /** 上班是否打卡 0-否 1-是 */
    private Integer needClockIn;
    
    /** 下班是否打卡 0-否 1-是 */
    private Integer needClockOut;
    
    /** 排序 */
    private Integer sortOrder;
}
