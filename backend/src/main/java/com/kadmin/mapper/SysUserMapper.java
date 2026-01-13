package com.kadmin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kadmin.entity.SysUser;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Set;

/**
 * 用户Mapper
 */
@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {

        /**
         * 根据用户名查询用户
         */
        @Select("SELECT * FROM sys_user WHERE username = #{username} AND deleted = 0")
        SysUser selectByUsername(@Param("username") String username);

        /**
         * 根据工号查询用户（通过员工表关联）
         */
        @Select("SELECT u.* FROM sys_user u " +
                        "INNER JOIN hr_employee e ON u.employee_id = e.id " +
                        "WHERE e.employee_no = #{employeeNo} AND u.deleted = 0 AND e.deleted = 0")
        SysUser selectByEmployeeNo(@Param("employeeNo") String employeeNo);

        /**
         * 查询用户角色编码列表
         */
        @Select("SELECT r.role_code FROM sys_role r " +
                        "INNER JOIN sys_user_role ur ON r.id = ur.role_id " +
                        "WHERE ur.user_id = #{userId} AND r.deleted = 0 AND r.status = 1")
        Set<String> selectRoleCodesByUserId(@Param("userId") Long userId);

        /**
         * 查询用户权限列表
         */
        @Select("SELECT DISTINCT m.permission FROM sys_menu m " +
                        "INNER JOIN sys_role_menu rm ON m.id = rm.menu_id " +
                        "INNER JOIN sys_user_role ur ON rm.role_id = ur.role_id " +
                        "WHERE ur.user_id = #{userId} AND m.deleted = 0 AND m.status = 1 " +
                        "AND m.permission IS NOT NULL AND m.permission != ''")
        Set<String> selectPermissionsByUserId(@Param("userId") Long userId);

        /**
         * 查询用户角色ID列表
         */
        @Select("SELECT role_id FROM sys_user_role WHERE user_id = #{userId}")
        List<Long> selectRoleIdsByUserId(@Param("userId") Long userId);

        /**
         * 删除用户角色关联
         */
        @Delete("DELETE FROM sys_user_role WHERE user_id = #{userId}")
        int deleteUserRoleByUserId(@Param("userId") Long userId);

        /**
         * 批量插入用户角色关联
         */
        @Insert("<script>" +
                        "INSERT INTO sys_user_role (user_id, role_id) VALUES " +
                        "<foreach collection='roleIds' item='roleId' separator=','>" +
                        "(#{userId}, #{roleId})" +
                        "</foreach>" +
                        "</script>")
        int insertUserRoleBatch(@Param("userId") Long userId, @Param("roleIds") List<Long> roleIds);
}