package com.kadmin.attendance.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.kadmin.common.BaseEntity;
import com.kadmin.hr.domain.HrEmployee;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * Attendance clock location.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("att_location")
public class AttLocation extends BaseEntity {

    private String locationName;

    private String address;

    private Double latitude;

    private Double longitude;

    private Integer clockRange;

    private Integer status;

    private String remark;

    @TableField(exist = false)
    private Integer assignedCount;

    @TableField(exist = false)
    private List<Long> employeeIds;

    @TableField(exist = false)
    private List<HrEmployee> employees;
}
