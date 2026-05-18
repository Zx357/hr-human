package com.kadmin.hr.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kadmin.hr.domain.HrMobileApprover;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface HrMobileApproverMapper extends BaseMapper<HrMobileApprover> {

    Page<HrMobileApprover> selectPageWithEmployee(Page<HrMobileApprover> page,
            @Param("employeeName") String employeeName,
            @Param("employeeNo") String employeeNo);
}
