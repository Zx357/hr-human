package com.kadmin.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("att_calendar_rule")
public class AttCalendarRule {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long companyId;
    private Integer ruleType; // 1-单休(周日) 2-双休 3-单休(周六) 4-法定假日 5-调休上班
    private LocalDate startDate;
    private LocalDate endDate;
    private String ruleName;
    private String remark;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(exist = false)
    private String companyName;
}
