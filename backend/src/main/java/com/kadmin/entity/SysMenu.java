package com.kadmin.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.kadmin.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 菜单实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_menu")
public class SysMenu extends BaseEntity {

    /**
     * 父菜单ID
     */
    private Long parentId;

    /**
     * 菜单类型：1-目录，2-菜单，3-按钮
     */
    private Integer menuType;

    /**
     * 菜单编码
     */
    private String menuCode;

    /**
     * 菜单名称（中文）
     */
    private String menuName;

    /**
     * 菜单名称（英文）
     */
    private String menuNameEn;

    /**
     * 路由路径
     */
    private String path;

    /**
     * 组件路径
     */
    private String component;

    /**
     * 权限标识
     */
    private String permission;

    /**
     * 图标
     */
    private String icon;

    /**
     * 排序
     */
    private Integer sortOrder;

    /**
     * 是否可见：0-否，1-是
     */
    private Integer visible;

    /**
     * 状态：0-禁用，1-启用
     */
    private Integer status;

    /**
     * 子菜单列表（非数据库字段）
     */
    @TableField(exist = false)
    private List<SysMenu> children;
}