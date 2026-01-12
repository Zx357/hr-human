package com.kadmin.controller;

import com.kadmin.common.Result;
import com.kadmin.service.ScheduleService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/attendance/schedule")
@RequiredArgsConstructor
public class ScheduleController {
    
    private final ScheduleService service;

    /**
     * 获取周排班数据
     */
    @GetMapping("/week")
    public Result<List<Map<String, Object>>> getWeekSchedule(
            @RequestParam(required = false) List<Long> orgIds,
            @RequestParam(required = false) String employeeName,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        return Result.success(service.getWeekSchedule(orgIds, employeeName, startDate, endDate));
    }

    /**
     * 保存单个排班
     */
    @PostMapping("/save")
    public Result<Void> saveSchedule(@RequestBody ScheduleRequest request) {
        service.saveSchedule(request.getEmployeeId(), request.getShiftId(), request.getScheduleDate());
        return Result.success();
    }

    /**
     * 批量排班
     */
    @PostMapping("/batch")
    public Result<Void> batchSchedule(@RequestBody BatchScheduleRequest request) {
        service.batchSchedule(request.getEmployeeIds(), request.getShiftId(), request.getStartDate(), request.getEndDate());
        return Result.success();
    }

    @Data
    public static class ScheduleRequest {
        private Long employeeId;
        private Long shiftId;
        @DateTimeFormat(pattern = "yyyy-MM-dd")
        private LocalDate scheduleDate;
    }

    @Data
    public static class BatchScheduleRequest {
        private List<Long> employeeIds;
        private Long shiftId;
        @DateTimeFormat(pattern = "yyyy-MM-dd")
        private LocalDate startDate;
        @DateTimeFormat(pattern = "yyyy-MM-dd")
        private LocalDate endDate;
    }
}
