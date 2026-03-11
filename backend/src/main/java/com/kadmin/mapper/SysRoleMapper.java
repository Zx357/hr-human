package com.kadmin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kadmin.entity.SysRole;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 角色Mapper
 */
@Mapper
public interface SysRoleMapper extends BaseMapper<SysRole> {

        /**
         * 根据用户ID查询角色列表
         */
        @Select("SELECT r.* FROM sys_role r " +
                        "INNER JOIN sys_user_role ur ON r.id = ur.role_id " +
                        "WHERE ur.user_id = #{userId}")
        List<SysRole> selectRolesByUserId(@Param("userId") Long userId);

        /**
         * 根据角色ID查询菜单ID列表
         */
        @Select("SELECT menu_id FROM sys_role_menu WHERE role_id = #{roleId}")
        List<Long> selectMenuIdsByRoleId(@Param("roleId") Long roleId);

        /**
         * 删除角色菜单关联
         */
        @Delete("DELETE FROM sys_role_menu WHERE role_id = #{roleId}")
        int deleteRoleMenuByRoleId(@Param("roleId") Long roleId);

        /**
         * 批量插入角色菜单关联
         */
        @Insert("<script>" +
                        "INSERT INTO sys_role_menu (role_id, menu_id) VALUES " +
                        "<foreach collection='menuIds' item='menuId' separator=','>" +
                        "(#{roleId}, #{menuId})" +
                        "</foreach>" +
                        "</script>")
        int insertRoleMenuBatch(@Param("roleId") Long roleId, @Param("menuIds") List<Long> menuIds);
}