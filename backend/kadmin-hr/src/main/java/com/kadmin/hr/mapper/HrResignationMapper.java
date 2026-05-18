package com.kadmin.hr.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kadmin.hr.domain.HrResignation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface HrResignationMapper extends BaseMapper<HrResignation> {
    Page<HrResignation> selectPageWithEmployee(Page<HrResignation> page,
            @Param("employeeName") String employeeName, @Param("resignType") String resignType,
            @Param("status") Integer status);
}
