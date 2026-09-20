package com.kadmin.web.controller.attendance;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.kadmin.common.Result;
import com.kadmin.attendance.domain.dto.CalculateAttendanceRequest;
import com.kadmin.attendance.domain.dto.LockDailyRecordRequest;
import com.kadmin.attendance.domain.AttClockRecord;
import com.kadmin.attendance.domain.AttDailyRecord;
import com.kadmin.attendance.service.AttendanceService;
import com.kadmin.common.annotation.OperLog;
import com.kadmin.common.utils.SecurityUtils;
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
        return Result
                .success(attendanceService.getClockRecordPage(page, size, orgIds, employeeName, startDate, endDate));
    }

    // 保存打卡记录（PC补录）
    @OperLog(module = "考勤管理", action = "补录打卡")
    @PostMapping("/clock")
    public Result<Void> saveClockRecord(@RequestBody AttClockRecord record) {
        attendanceService.saveClockRecord(record);
        return Result.success();
    }

    // 删除打卡记录
    @OperLog(module = "考勤管理", action = "删除打卡")
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
        return Result.success(attendanceService.getDailyRecordPage(page, size, startDate, endDate, orgIds, employeeNo,
                employeeName, status));
    }

    // 保存日考勤记录
    @PostMapping("/daily")
    public Result<Void> saveDailyRecord(@RequestBody AttDailyRecord record) {
        attendanceService.saveDailyRecord(record);
        return Result.success();
    }

    // 计算指定日期范围的日考勤
    @PostMapping("/daily/calculate")
    public Result<Void> calculateDailyAttendance(@RequestBody CalculateAttendanceRequest request) {
        LocalDate start = LocalDate.parse(request.getStartDate());
        LocalDate end = LocalDate.parse(request.getEndDate());
        attendanceService.calculateDailyAttendance(start, end, request.getOrgIds(),
                request.getEmployeeNo(), request.getEmployeeName(), request.getEmployeeIds());
        return Result.success();
    }

    // 锁定日考勤记录
    @OperLog(module = "考勤管理", action = "锁定解锁")
    @PostMapping("/daily/lock")
    public Result<Void> lockDailyRecords(@RequestBody LockDailyRecordRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        attendanceService.lockDailyRecords(request.getIds(),
                request.getLock() != null && request.getLock(), userId);
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
