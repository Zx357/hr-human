package com.kadmin.hr.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kadmin.hr.domain.HrRegularization;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface HrRegularizationMapper extends BaseMapper<HrRegularization> {
    Page<HrRegularization> selectPageWithEmployee(Page<HrRegularization> page,
            @Param("employeeName") String employeeName, @Param("status") Integer status);
}
