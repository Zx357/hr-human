package com.kadmin.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.kadmin.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 统一组织架构实体（集团、公司、部门）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("org_unit")
public class OrgUnit extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 父级ID，0表示顶级 */
    private Long parentId;

    /** 类型：1-集团，2-公司，3-部门 */
    private Integer unitType;

    /** 编码 */
    private String unitCode;

    /** 名称 */
    private String unitName;

    /** 简称 */
    private String shortName;

    /** 负责人ID */
    private Long leaderId;

    /** 联系电话 */
    private String phone;

    /** 邮箱 */
    private String email;

    /** 地址 */
    private String address;

    /** 描述 */
    private String description;

    /** 排序 */
    private Integer sortOrder;

    /** 是否删除 */
    @TableLogic
    private Integer deleted;

    /** 子节点（非数据库字段） */
    @TableField(exist = false)
    private List<OrgUnit> children;

    /** 负责人姓名（非数据库字段） */
    @TableField(exist = false)
    private String leaderName;

    /** 类型名称（非数据库字段） */
    @TableField(exist = false)
    private String unitTypeName;

    /** 员工数量（非数据库字段） */
    @TableField(exist = false)
    private Integer employeeCount;

    // 类型常量
    public static final int TYPE_GROUP = 1;    // 集团
    public static final int TYPE_COMPANY = 2;  // 公司
    public static final int TYPE_DEPT = 3;     // 部门
}
