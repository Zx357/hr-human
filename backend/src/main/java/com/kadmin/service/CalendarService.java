package com.kadmin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.kadmin.entity.AttCalendarRule;
import com.kadmin.mapper.AttCalendarRuleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CalendarService {
    private final AttCalendarRuleMapper calendarRuleMapper;

    // 获取所有规则
    public List<AttCalendarRule> getAllRules() {
        return calendarRuleMapper.selectAllWithCompany();
    }

    // 保存规则
    @Transactional
    public void saveRule(AttCalendarRule rule) {
        // 休息规则（1-3）每个公司只能有一条
        if (rule.getRuleType() <= 3) {
            LambdaQueryWrapper<AttCalendarRule> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(AttCalendarRule::getCompanyId, rule.getCompanyId());
            wrapper.le(AttCalendarRule::getRuleType, 3);
            if (rule.getId() != null) {
                wrapper.ne(AttCalendarRule::getId, rule.getId());
            }
            AttCalendarRule existing = calendarRuleMapper.selectOne(wrapper);
            if (existing != null) {
                // 更新已有的休息规则
                existing.setRuleType(rule.getRuleType());
                existing.setRemark(rule.getRemark());
                calendarRuleMapper.updateById(existing);
                return;
            }
        }
        
        if (rule.getId() != null) {
            calendarRuleMapper.updateById(rule);
        } else {
            calendarRuleMapper.insert(rule);
        }
    }

    // 删除规则
    public void deleteRule(Long id) {
        calendarRuleMapper.deleteById(id);
    }
}
