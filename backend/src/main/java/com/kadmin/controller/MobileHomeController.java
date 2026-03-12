package com.kadmin.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.kadmin.common.Result;
import com.kadmin.entity.AttClockRecord;
import com.kadmin.entity.HrApplication;
import com.kadmin.mapper.AttClockRecordMapper;
import com.kadmin.mapper.HrApplicationMapper;
import com.kadmin.security.LoginUser;
import com.kadmin.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.Map;

/**
 * 移动端首页接口
 */
@RestController
@RequestMapping("/mobile/home")
@RequiredArgsConstructor
public class MobileHomeController {

    private final AttClockRecordMapper clockRecordMapper;
    private final HrApplicationMapper applicationMapper;

    @GetMapping("/stats")
    public Result<Map<String, Object>> getStats() {
        LoginUser loginUser = SecurityUtils.getCurrentUser();
        if (loginUser == null) {
            return Result.error("请先登录");
        }

        Long employeeId = loginUser.getEmployeeId();
        if (employeeId == null) {
            employeeId = loginUser.getUserId();
        }

        Map<String, Object> stats = new HashMap<>();

        // 本月出勤天数：统计本月有上班打卡(clockType=1)的不同日期数
        YearMonth ym = YearMonth.now();
        LocalDateTime monthStart = ym.atDay(1).atStartOfDay();
        LocalDateTime monthEnd = ym.atEndOfMonth().atTime(LocalTime.MAX);
        Long attendDays = clockRecordMapper.selectCount(
                new LambdaQueryWrapper<AttClockRecord>()
                        .eq(AttClockRecord::getEmployeeId, employeeId)
                        .eq(AttClockRecord::getClockType, 1)
                        .between(AttClockRecord::getClockTime, monthStart, monthEnd));
        stats.put("monthAttendDays", attendDays != null ? attendDays : 0);

        // 待处理（我的待审批申请）
        Long pendingCount = applicationMapper.selectCount(
                new LambdaQueryWrapper<HrApplication>()
                        .eq(HrApplication::getEmployeeId, employeeId)
                        .eq(HrApplication::getStatus, 0));
        stats.put("pendingCount", pendingCount != null ? pendingCount : 0);

        // 已通过
        Long approvedCount = applicationMapper.selectCount(
                new LambdaQueryWrapper<HrApplication>()
                        .eq(HrApplication::getEmployeeId, employeeId)
                        .eq(HrApplication::getStatus, 1));
        stats.put("approvedCount", approvedCount != null ? approvedCount : 0);

        // 待我审批（作为审批人需要处理的）
        Long approvalCount = applicationMapper.selectMobilePendingCount(employeeId);
        stats.put("approvalCount", approvalCount != null ? approvalCount : 0);

        return Result.success(stats);
    }
}
