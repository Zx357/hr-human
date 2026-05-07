package com.kadmin.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kadmin.entity.HrEmployee;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 员工Mapper接口
 */
@Mapper
public interface EmployeeMapper extends BaseMapper<HrEmployee> {

    /**
     * 分页查询员工列表（带公司和部门名称）
     */
    Page<HrEmployee> selectPageWithDetails(Page<HrEmployee> page, @Param("ew") Wrapper<HrEmployee> wrapper);

    /**
     * 根据工号查询员工
     */
    @Select("SELECT * FROM hr_employee WHERE employee_no = #{employeeNo}")
    HrEmployee selectByEmployeeNo(@Param("employeeNo") String employeeNo);

    /**
     * 查询指定前缀的最大员工编号
     */
    @Select("SELECT MAX(employee_no) FROM hr_employee WHERE employee_no LIKE CONCAT(#{prefix}, '%')")
    String selectMaxEmployeeNo(@Param("prefix") String prefix);
}