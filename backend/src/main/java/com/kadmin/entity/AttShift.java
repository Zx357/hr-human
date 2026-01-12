package com.kadmin.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * 班次实体
 */
@Data
@TableName("att_shift")
public class AttShift {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /** 公司ID */
    private Long companyId;
    
    /** 班次编码 */
    private String shiftCode;
    
    /** 班次名称 */
    private String shiftName;
    
    /** 上班时间 */
    private LocalTime workStartTime;
    
    /** 下班时间 */
    private LocalTime workEndTime;
    
    /** 迟到分钟数 */
    private Integer lateMinutes;
    
    /** 早退分钟数 */
    private Integer earlyMinutes;
    
    /** 工作时长 */
    private java.math.BigDecimal workHours;
    
    /** 是否跨天 */
    private Integer isNextDay;
    
    /** 休息开始时间 */
    private LocalTime restStartTime;
    
    /** 休息结束时间 */
    private LocalTime restEndTime;
    
    /** 状态 1-启用 0-禁用 */
    private Integer status;
    
    /** 备注 */
    private String remark;
    
    @TableLogic
    private Integer deleted;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedTime;
    
    /** 班次时段列表 */
    @TableField(exist = false)
    private List<AttShiftPeriod> periods;
}
