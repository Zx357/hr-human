package com.kadmin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kadmin.entity.AttLocation;
import com.kadmin.entity.AttLocationEmployee;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface AttLocationEmployeeMapper extends BaseMapper<AttLocationEmployee> {

    @Select("""
            SELECT l.*
            FROM att_location l
            INNER JOIN att_location_employee le ON le.location_id = l.id
            WHERE le.employee_id = #{employeeId}
              AND l.status = 1
            ORDER BY l.updated_time DESC, l.id DESC
            """)
    List<AttLocation> selectActiveLocationsByEmployeeId(@Param("employeeId") Long employeeId);
}
