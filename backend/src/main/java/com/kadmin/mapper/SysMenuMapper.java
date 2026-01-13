package com.kadmin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kadmin.entity.SysMenu;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 菜单Mapper
 */
@Mapper
public interface SysMenuMapper extends BaseMapper<SysMenu> {

    /**
     * 根据用户ID获取菜单列表（只返回目录和菜单类型，不包含按钮）
     */
    @Select("SELECT DISTINCT m.* FROM sys_menu m " +
            "LEFT JOIN sys_role_menu rm ON m.id = rm.menu_id " +
            "LEFT JOIN sys_user_role ur ON rm.role_id = ur.role_id " +
            "WHERE ur.user_id = #{userId} AND m.status = 1 AND m.deleted = 0 AND m.menu_type IN (1, 2) " +
            "ORDER BY m.sort_order")
    List<SysMenu> selectMenusByUserId(@Param("userId") Long userId);

    /**
     * 根据用户ID获取按钮权限列表
     */
    @Select("SELECT DISTINCT m.permission FROM sys_menu m " +
            "LEFT JOIN sys_role_menu rm ON m.id = rm.menu_id " +
            "LEFT JOIN sys_user_role ur ON rm.role_id = ur.role_id " +
            "WHERE ur.user_id = #{userId} AND m.status = 1 AND m.deleted = 0 AND m.menu_type = 3 " +
            "AND m.permission IS NOT NULL AND m.permission != ''")
    List<String> selectPermissionsByUserId(@Param("userId") Long userId);

    /**
     * 获取所有启用的菜单（只返回目录和菜单类型，不包含按钮）
     */
    @Select("SELECT * FROM sys_menu WHERE status = 1 AND deleted = 0 AND menu_type IN (1, 2) ORDER BY sort_order")
    List<SysMenu> selectAllMenus();

    /**
     * 获取所有菜单（包含按钮，用于菜单管理）
     */
    @Select("SELECT * FROM sys_menu WHERE deleted = 0 ORDER BY parent_id, sort_order")
    List<SysMenu> selectAllMenusWithButtons();
}