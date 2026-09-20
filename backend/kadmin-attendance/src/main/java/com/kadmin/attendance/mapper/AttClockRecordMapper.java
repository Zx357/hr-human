package com.kadmin.attendance.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kadmin.attendance.domain.AttClockRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface AttClockRecordMapper extends BaseMapper<AttClockRecord> {
    IPage<AttClockRecord> selectPageWithEmployee(Page<AttClockRecord> page, 
        @Param("orgIds") List<Long> orgIds, @Param("employeeName") String employeeName, 
        @Param("startDate") String startDate, @Param("endDate") String endDate);

    /**
     * 统计时间段内有上班打卡的不同日期数（按日期去重，避免一天多次打卡虚增出勤天数）
     */
    @Select("SELECT COUNT(DISTINCT COALESCE(clock_date, DATE(clock_time))) FROM att_clock_record "
            + "WHERE employee_id = #{employeeId} AND clock_type = 1 "
            + "AND clock_time BETWEEN #{startTime} AND #{endTime}")
    Long countDistinctAttendDays(@Param("employeeId") Long employeeId,
            @Param("startTime") java.time.LocalDateTime startTime,
            @Param("endTime") java.time.LocalDateTime endTime);
}
