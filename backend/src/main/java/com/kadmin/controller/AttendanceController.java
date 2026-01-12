package com.kadmin.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.kadmin.common.Result;
import com.kadmin.entity.AttClockRecord;
import com.kadmin.entity.AttDailyRecord;
import com.kadmin.service.AttendanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/attendance")
@RequiredArgsConstructor
public class AttendanceController {
    private final AttendanceService attendanceService;

    // 打卡记录分页
    @GetMapping("/clock/page")
    public Result<IPage<AttClockRecord>> getClockRecordPage(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) List<Long> orgIds,
            @RequestParam(required = false) String employeeName,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        return Result.success(attendanceService.getClockRecordPage(page, size, orgIds, employeeName, startDate, endDate));
    }

    // 保存打卡记录
    @PostMapping("/clock")
    public Result<Void> saveClockRecord(@RequestBody AttClockRecord record) {
        attendanceService.saveClockRecord(record);
        return Result.success();
    }

    // 删除打卡记录
    @DeleteMapping("/clock/{id}")
    public Result<Void> deleteClockRecord(@PathVariable Long id) {
        attendanceService.deleteClockRecord(id);
        return Result.success();
    }

    // 日考勤分页
    @GetMapping("/daily/page")
    public Result<IPage<AttDailyRecord>> getDailyRecordPage(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) List<Long> orgIds,
            @RequestParam(required = false) String employeeNo,
            @RequestParam(required = false) String employeeName,
            @RequestParam(required = false) Integer status) {
        return Result.success(attendanceService.getDailyRecordPage(page, size, startDate, endDate, orgIds, employeeNo, employeeName, status));
    }

    // 保存日考勤记录
    @PostMapping("/daily")
    public Result<Void> saveDailyRecord(@RequestBody AttDailyRecord record) {
        attendanceService.saveDailyRecord(record);
        return Result.success();
    }

    // 计算指定日期范围的日考勤
    @PostMapping("/daily/calculate")
    public Result<Void> calculateDailyAttendance(@RequestBody java.util.Map<String, Object> params) {
        String startDate = (String) params.get("startDate");
        String endDate = (String) params.get("endDate");
        @SuppressWarnings("unchecked")
        List<Long> orgIds = params.get("orgIds") != null ? 
            ((List<Integer>) params.get("orgIds")).stream().map(Integer::longValue).toList() : null;
        String employeeNo = (String) params.get("employeeNo");
        String employeeName = (String) params.get("employeeName");
        @SuppressWarnings("unchecked")
        java.util.List<Integer> employeeIds = (java.util.List<Integer>) params.get("employeeIds");
        
        LocalDate start = LocalDate.parse(startDate);
        LocalDate end = LocalDate.parse(endDate);
        java.util.List<Long> empIds = employeeIds != null && !employeeIds.isEmpty() 
            ? employeeIds.stream().map(Integer::longValue).toList() : null;
        
        attendanceService.calculateDailyAttendance(start, end, orgIds, employeeNo, employeeName, empIds);
        return Result.success();
    }

    // 锁定日考勤记录
    @PostMapping("/daily/lock")
    public Result<Void> lockDailyRecords(@RequestBody java.util.Map<String, Object> params) {
        @SuppressWarnings("unchecked")
        java.util.List<Integer> ids = (java.util.List<Integer>) params.get("ids");
        Boolean lock = (Boolean) params.get("lock");
        // TODO: 从SecurityContext获取当前用户ID
        Long userId = 1L;
        
        java.util.List<Long> recordIds = ids.stream().map(Integer::longValue).toList();
        attendanceService.lockDailyRecords(recordIds, lock != null && lock, userId);
        return Result.success();
    }

    // 月考勤汇总
    @GetMapping("/monthly")
    public Result<java.util.List<java.util.Map<String, Object>>> getMonthlyAttendance(
            @RequestParam String month,
            @RequestParam(required = false) List<Long> orgIds,
            @RequestParam(required = false) String employeeNo,
            @RequestParam(required = false) String employeeName) {
        return Result.success(attendanceService.getMonthlyAttendance(month, orgIds, employeeNo, employeeName));
    }
}
