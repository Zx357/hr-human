package com.kadmin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kadmin.entity.AttDailyRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface AttDailyRecordMapper extends BaseMapper<AttDailyRecord> {
    IPage<AttDailyRecord> selectPageWithEmployee(Page<AttDailyRecord> page,
            @Param("startDate") String startDate, @Param("endDate") String endDate,
            @Param("orgIds") List<Long> orgIds,
            @Param("employeeNo") String employeeNo, @Param("employeeName") String employeeName,
            @Param("status") Integer status);

    List<AttDailyRecord> selectListWithEmployee(
            @Param("startDate") String startDate, @Param("endDate") String endDate,
            @Param("orgIds") List<Long> orgIds,
            @Param("employeeNo") String employeeNo, @Param("employeeName") String employeeName,
            @Param("status") Integer status);

    IPage<AttDailyRecord> selectGroupedPage(Page<AttDailyRecord> page,
            @Param("startDate") String startDate, @Param("endDate") String endDate,
            @Param("orgIds") List<Long> orgIds,
            @Param("employeeNo") String employeeNo, @Param("employeeName") String employeeName,
            @Param("status") Integer status);
}
