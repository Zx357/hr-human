package com.kadmin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kadmin.entity.AttCalendarRule;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface AttCalendarRuleMapper extends BaseMapper<AttCalendarRule> {
    @Select("SELECT r.*, c.unit_name as company_name FROM att_calendar_rule r LEFT JOIN org_unit c ON r.company_id = c.id AND c.deleted = 0 ORDER BY r.company_id, r.rule_type, r.start_date")
    List<AttCalendarRule> selectAllWithCompany();
}
