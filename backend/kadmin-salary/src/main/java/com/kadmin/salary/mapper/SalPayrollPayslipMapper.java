package com.kadmin.salary.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kadmin.salary.domain.SalPayrollPayslip;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface SalPayrollPayslipMapper extends BaseMapper<SalPayrollPayslip> {

    /**
     * 批次工资条统计（单条 GROUP BY，避免批次分页逐行 count 的 N+1）：
     * 按 batch_id 返回 total(工资条总数)、readCount(已读数)、confirmCount(已确认数)
     */
    @Select("""
            <script>
            SELECT batch_id AS batchId,
                   COUNT(*) AS total,
                   SUM(CASE WHEN read_flag = 1 THEN 1 ELSE 0 END) AS readCount,
                   SUM(CASE WHEN confirm_flag = 1 THEN 1 ELSE 0 END) AS confirmCount
            FROM sal_payroll_payslip
            WHERE batch_id IN
            <foreach collection='batchIds' item='id' open='(' separator=',' close=')'>#{id}</foreach>
            GROUP BY batch_id
            </script>
            """)
    List<Map<String, Object>> countStatsGroupByBatch(@Param("batchIds") List<Long> batchIds);
}
