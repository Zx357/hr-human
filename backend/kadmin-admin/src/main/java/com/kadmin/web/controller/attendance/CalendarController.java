package com.kadmin.web.controller.attendance;

import com.kadmin.common.Result;
import com.kadmin.attendance.domain.AttCalendarRule;
import com.kadmin.attendance.service.CalendarService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/calendar")
@RequiredArgsConstructor
public class CalendarController {
    private final CalendarService calendarService;

    // 获取所有规则
    @GetMapping("/rules")
    public Result<List<AttCalendarRule>> getAllRules() {
        return Result.success(calendarService.getAllRules());
    }

    // 保存规则
    @PostMapping("/rule")
    public Result<Void> saveRule(@RequestBody AttCalendarRule rule) {
        calendarService.saveRule(rule);
        return Result.success();
    }

    // 删除规则
    @DeleteMapping("/rule/{id}")
    public Result<Void> deleteRule(@PathVariable Long id) {
        calendarService.deleteRule(id);
        return Result.success();
    }
}
