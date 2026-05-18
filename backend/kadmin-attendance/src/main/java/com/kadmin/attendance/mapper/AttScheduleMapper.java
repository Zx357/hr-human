package com.kadmin.attendance.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kadmin.attendance.domain.AttSchedule;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.time.LocalDate;
import java.util.List;

@Mapper
public interface AttScheduleMapper extends BaseMapper<AttSchedule> {
    
    List<AttSchedule> selectScheduleList(@Param("orgIds") List<Long> orgIds,
                                          @Param("employeeNo") String employeeNo,
                                          @Param("employeeName") String employeeName,
                                          @Param("startDate") LocalDate startDate, 
                                          @Param("endDate") LocalDate endDate);
}
