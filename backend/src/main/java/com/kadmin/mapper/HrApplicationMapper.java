package com.kadmin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kadmin.entity.HrApplication;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface HrApplicationMapper extends BaseMapper<HrApplication> {
    Page<HrApplication> selectPageWithEmployee(Page<HrApplication> page,
            @Param("employeeName") String employeeName, @Param("employeeNo") String employeeNo,
            @Param("appType") String appType, @Param("status") Integer status, @Param("employeeId") Long employeeId);
    
    Page<HrApplication> selectPendingPage(Page<HrApplication> page,
            @Param("employeeName") String employeeName, @Param("employeeNo") String employeeNo,
            @Param("appType") String appType, @Param("roleIds") List<Long> roleIds);
}
