package com.kadmin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kadmin.entity.HrContract;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 合同Mapper
 */
@Mapper
public interface HrContractMapper extends BaseMapper<HrContract> {

    /**
     * 分页查询合同列表（带员工信息）
     */
    Page<HrContract> selectPageWithEmployee(
            Page<HrContract> page,
            @Param("contractNo") String contractNo,
            @Param("employeeName") String employeeName,
            @Param("employeeNo") String employeeNo,
            @Param("contractType") String contractType,
            @Param("status") Integer status);
}
