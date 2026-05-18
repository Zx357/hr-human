package com.kadmin.attendance.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kadmin.attendance.domain.AttClockRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface AttClockRecordMapper extends BaseMapper<AttClockRecord> {
    IPage<AttClockRecord> selectPageWithEmployee(Page<AttClockRecord> page, 
        @Param("orgIds") List<Long> orgIds, @Param("employeeName") String employeeName, 
        @Param("startDate") String startDate, @Param("endDate") String endDate);
}
