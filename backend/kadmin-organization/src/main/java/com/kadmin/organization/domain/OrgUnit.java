package com.kadmin.organization.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.kadmin.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.util.List;

/**
 * Unified organization node (group / company / department).
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("org_unit")
public class OrgUnit extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** Parent ID, 0 means root. */
    private Long parentId;

    /** 1-group, 2-company, 3-department. */
    private Integer unitType;

    /** Code. */
    private String unitCode;

    /** Name. */
    private String unitName;

    /** Short name. */
    private String shortName;

    /** Leader employee ID. */
    private Long leaderId;

    /** Contact phone. */
    private String phone;

    /** Contact email. */
    private String email;

    /** Organization address. */
    private String address;

    /** Attendance location address shown to mobile users. */
    private String attendanceAddress;

    /** Attendance location latitude (GCJ-02). */
    private BigDecimal attendanceLatitude;

    /** Attendance location longitude (GCJ-02). */
    private BigDecimal attendanceLongitude;

    /** Allowed clock-in radius in meters. */
    private Integer attendanceRange;

    /** Description. */
    private String description;

    /** Sort order. */
    private Integer sortOrder;

    /** Child nodes. */
    @TableField(exist = false)
    private List<OrgUnit> children;

    /** Leader name. */
    @TableField(exist = false)
    private String leaderName;

    /** Type label. */
    @TableField(exist = false)
    private String unitTypeName;

    /** Employee count under this node. */
    @TableField(exist = false)
    private Integer employeeCount;

    public static final int TYPE_GROUP = 1;
    public static final int TYPE_COMPANY = 2;
    public static final int TYPE_DEPT = 3;
}
