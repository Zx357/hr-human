package com.kadmin.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.kadmin.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Employee assignment for attendance clock location.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("att_location_employee")
public class AttLocationEmployee extends BaseEntity {

    private Long locationId;

    private Long employeeId;
}

