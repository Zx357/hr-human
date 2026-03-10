package com.kadmin.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.kadmin.common.Result;
import com.kadmin.entity.AttClockRecord;
import com.kadmin.entity.AttDailyRecord;
import com.kadmin.entity.AttSchedule;
import com.kadmin.entity.AttShift;
import com.kadmin.entity.HrEmployee;
import com.kadmin.mapper.*;
import com.kadmin.security.LoginUser;
import com.kadmin.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 移动端考勤接口
 */
@RestController
@RequestMapping("/mobile/attendance")
@RequiredArgsConstructor
public class MobileAttendanceController {

    private final AttClockRecordMapper clockRecordMapper;
    private final AttDailyRecordMapper dailyRecordMapper;
    private final AttScheduleMapper scheduleMapper;
    private final AttShiftMapper shiftMapper;
    private final EmployeeMapper employeeMapper;

    /**
     * 获取打卡页面数据（当前时间、今日打卡记录）
     */
    @GetMapping("/clock/info")
    public Result<Map<String, Object>> getClockInfo() {
        LoginUser loginUser = SecurityUtils.getCurrentUser();
        if (loginUser == null) {
            return Result.error("请先登录");
        }

        Long employeeId = loginUser.getEmployeeId();
        if (employeeId == null) {
            // 如果是员工登录，userId就是employeeId
            employeeId = loginUser.getUserId();
        }

        Map<String, Object> result = new HashMap<>();
        
        // 当前服务器时间
        result.put("serverTime", LocalDateTime.now());
        
        // 今日打卡记录
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.atTime(LocalTime.MAX);
        
        List<AttClockRecord> todayRecords = clockRecordMapper.selectList(
            new LambdaQueryWrapper<AttClockRecord>()
                .eq(AttClockRecord::getEmployeeId, employeeId)
                .between(AttClockRecord::getClockTime, startOfDay, endOfDay)
                .orderByAsc(AttClockRecord::getClockTime)
        );
        result.put("todayRecords", todayRecords);
        
        // 判断是否已上班打卡
        boolean hasClockedIn = todayRecords.stream()
            .anyMatch(r -> r.getClockType() == 1);
        result.put("hasClockedIn", hasClockedIn);
        
        // 判断是否已下班打卡
        boolean hasClockedOut = todayRecords.stream()
            .anyMatch(r -> r.getClockType() == 2);
        result.put("hasClockedOut", hasClockedOut);

        // 今日排班与应打卡时间
        AttSchedule todaySchedule = scheduleMapper.selectOne(
            new LambdaQueryWrapper<AttSchedule>()
                .eq(AttSchedule::getEmployeeId, employeeId)
                .eq(AttSchedule::getScheduleDate, today)
        );
        if (todaySchedule != null && todaySchedule.getShiftId() != null) {
            AttShift shift = shiftMapper.selectById(todaySchedule.getShiftId());
            if (shift != null) {
                result.put("scheduledIn", shift.getWorkStartTime() != null ? shift.getWorkStartTime().toString().substring(0, 5) : null);
                result.put("scheduledOut", shift.getWorkEndTime() != null ? shift.getWorkEndTime().toString().substring(0, 5) : null);
            }
        }

        // 今日日考勤（用于显示迟到/早退状态）
        AttDailyRecord todayDaily = dailyRecordMapper.selectOne(
            new LambdaQueryWrapper<AttDailyRecord>()
                .eq(AttDailyRecord::getEmployeeId, employeeId)
                .eq(AttDailyRecord::getAttDate, today)
        );
        if (todayDaily != null) {
            Map<String, Object> daily = new HashMap<>();
            daily.put("status", todayDaily.getStatus());
            daily.put("lateMinutes", todayDaily.getLateMinutes() != null ? todayDaily.getLateMinutes() : 0);
            daily.put("earlyMinutes", todayDaily.getEarlyMinutes() != null ? todayDaily.getEarlyMinutes() : 0);
            result.put("todayDaily", daily);
        }

        return Result.success(result);
    }

    /**
     * 打卡
     */
    @PostMapping("/clock")
    public Result<AttClockRecord> clock(@RequestBody Map<String, Object> params) {
        LoginUser loginUser = SecurityUtils.getCurrentUser();
        if (loginUser == null) {
            return Result.error("请先登录");
        }

        Long employeeId = loginUser.getEmployeeId();
        if (employeeId == null) {
            employeeId = loginUser.getUserId();
        }

        // 获取打卡类型：1-上班 2-下班
        Integer clockType = (Integer) params.get("clockType");
        if (clockType == null) {
            // 自动判断：如果今天没有上班打卡则为上班，否则为下班
            LocalDate today = LocalDate.now();
            LocalDateTime startOfDay = today.atStartOfDay();
            LocalDateTime endOfDay = today.atTime(LocalTime.MAX);
            
            long inCount = clockRecordMapper.selectCount(
                new LambdaQueryWrapper<AttClockRecord>()
                    .eq(AttClockRecord::getEmployeeId, employeeId)
                    .eq(AttClockRecord::getClockType, 1)
                    .between(AttClockRecord::getClockTime, startOfDay, endOfDay)
            );
            clockType = inCount == 0 ? 1 : 2;
        }

        // 创建打卡记录
        AttClockRecord record = new AttClockRecord();
        record.setEmployeeId(employeeId);
        record.setClockTime(LocalDateTime.now());
        record.setClockType(clockType);
        record.setClockMethod(1); // 1-APP打卡
        record.setLocation((String) params.get("location"));
        record.setDeviceInfo((String) params.get("deviceInfo"));
        record.setRemark(clockType == 1 ? "移动端上班打卡" : "移动端下班打卡");

        clockRecordMapper.insert(record);

        return Result.success(record);
    }

    /**
     * 获取我的打卡记录（分页）
     */
    @GetMapping("/clock/records")
    public Result<Map<String, Object>> getMyClockRecords(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        
        LoginUser loginUser = SecurityUtils.getCurrentUser();
        if (loginUser == null) {
            return Result.error("请先登录");
        }

        Long employeeId = loginUser.getEmployeeId();
        if (employeeId == null) {
            employeeId = loginUser.getUserId();
        }

        LambdaQueryWrapper<AttClockRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AttClockRecord::getEmployeeId, employeeId);
        
        if (startDate != null && !startDate.isEmpty()) {
            wrapper.ge(AttClockRecord::getClockTime, LocalDate.parse(startDate).atStartOfDay());
        }
        if (endDate != null && !endDate.isEmpty()) {
            wrapper.le(AttClockRecord::getClockTime, LocalDate.parse(endDate).atTime(LocalTime.MAX));
        }
        
        wrapper.orderByDesc(AttClockRecord::getClockTime);
        
        // 简单分页
        long total = clockRecordMapper.selectCount(wrapper);
        wrapper.last("LIMIT " + (pageNum - 1) * pageSize + ", " + pageSize);
        List<AttClockRecord> records = clockRecordMapper.selectList(wrapper);

        Map<String, Object> result = new HashMap<>();
        result.put("records", records);
        result.put("total", total);
        
        return Result.success(result);
    }

    /**
     * 获取月度考勤数据（日历视图 + 统计）
     */
    @GetMapping("/month")
    public Result<Map<String, Object>> getMonthAttendance(@RequestParam String month) {
        LoginUser loginUser = SecurityUtils.getCurrentUser();
        if (loginUser == null) {
            return Result.error("请先登录");
        }

        Long employeeId = loginUser.getEmployeeId();
        if (employeeId == null) {
            employeeId = loginUser.getUserId();
        }

        YearMonth ym = YearMonth.parse(month);
        LocalDate startDate = ym.atDay(1);
        LocalDate endDate = ym.atEndOfMonth();

        // 获取该月所有打卡记录
        List<AttClockRecord> clockRecords = clockRecordMapper.selectList(
            new LambdaQueryWrapper<AttClockRecord>()
                .eq(AttClockRecord::getEmployeeId, employeeId)
                .between(AttClockRecord::getClockTime, startDate.atStartOfDay(), endDate.atTime(LocalTime.MAX))
                .orderByAsc(AttClockRecord::getClockTime)
        );

        // 获取该月排班信息以确定应出勤天数
        List<AttSchedule> schedules = scheduleMapper.selectList(
            new LambdaQueryWrapper<AttSchedule>()
                .eq(AttSchedule::getEmployeeId, employeeId)
                .between(AttSchedule::getScheduleDate, startDate, endDate)
        );

        // 获取该月日考勤记录
        List<AttDailyRecord> dailyRecords = dailyRecordMapper.selectList(
            new LambdaQueryWrapper<AttDailyRecord>()
                .eq(AttDailyRecord::getEmployeeId, employeeId)
                .between(AttDailyRecord::getAttDate, startDate, endDate)
        );

        // 按日期分组打卡记录
        Map<String, List<AttClockRecord>> clockByDate = new LinkedHashMap<>();
        DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("HH:mm");

        for (AttClockRecord r : clockRecords) {
            String dateKey = r.getClockTime().toLocalDate().format(dateFmt);
            clockByDate.computeIfAbsent(dateKey, k -> new ArrayList<>()).add(r);
        }

        // 按日期分组日考勤
        Map<LocalDate, AttDailyRecord> dailyByDate = new LinkedHashMap<>();
        for (AttDailyRecord dr : dailyRecords) {
            dailyByDate.putIfAbsent(dr.getAttDate(), dr);
        }

        // 构建排班日期集合
        Set<LocalDate> scheduledDates = new HashSet<>();
        for (AttSchedule s : schedules) {
            if (s.getShiftId() != null) {
                scheduledDates.add(s.getScheduleDate());
            }
        }

        // 构建每天的记录
        List<Map<String, Object>> records = new ArrayList<>();
        int normalIn = 0, normalOut = 0, lateDays = 0, earlyDays = 0, absentDays = 0;

        LocalDate current = startDate;
        LocalDate today = LocalDate.now();

        while (!current.isAfter(endDate)) {
            String dateStr = current.format(dateFmt);
            List<AttClockRecord> dayClock = clockByDate.getOrDefault(dateStr, Collections.emptyList());
            AttDailyRecord dailyRecord = dailyByDate.get(current);

            Map<String, Object> dayData = new HashMap<>();
            dayData.put("attDate", dateStr);

            AttClockRecord clockIn = dayClock.stream().filter(c -> c.getClockType() == 1).findFirst().orElse(null);
            AttClockRecord clockOut = dayClock.stream().filter(c -> c.getClockType() == 2).findFirst().orElse(null);

            if (clockIn != null) {
                dayData.put("clockIn", clockIn.getClockTime().toLocalTime().format(timeFmt));
            }
            if (clockOut != null) {
                dayData.put("clockOut", clockOut.getClockTime().toLocalTime().format(timeFmt));
            }

            // 从日考勤记录判断状态
            boolean isLate = false, isEarly = false, isNormalIn = false, isNormalOut = false;

            if (dailyRecord != null) {
                Integer status = dailyRecord.getStatus();
                if (status != null) {
                    isLate = (status == 2 || status == 7);
                    isEarly = (status == 3 || status == 7);
                    isNormalIn = clockIn != null && !isLate;
                    isNormalOut = clockOut != null && !isEarly;
                }
            } else if (clockIn != null || clockOut != null) {
                // 没有日考勤记录但有打卡，根据排班简单判断
                isNormalIn = clockIn != null;
                isNormalOut = clockOut != null;
            }

            dayData.put("lateIn", isLate);
            dayData.put("earlyOut", isEarly);
            dayData.put("normalIn", isNormalIn);
            dayData.put("normalOut", isNormalOut);

            // 统计 (只统计已过的日期，且是排班日或有打卡记录的日期)
            boolean isPast = !current.isAfter(today);
            boolean isScheduled = scheduledDates.contains(current) || !dayClock.isEmpty();

            if (isPast && isScheduled) {
                if (isNormalIn) normalIn++;
                if (isNormalOut) normalOut++;
                if (isLate) lateDays++;
                if (isEarly) earlyDays++;
                if (clockIn == null || clockOut == null) absentDays++;
            }

            if (!dayClock.isEmpty() || dailyRecord != null) {
                records.add(dayData);
            }

            current = current.plusDays(1);
        }

        Map<String, Object> stats = new HashMap<>();
        stats.put("normalIn", normalIn);
        stats.put("normalOut", normalOut);
        stats.put("lateDays", lateDays);
        stats.put("earlyDays", earlyDays);
        stats.put("absentDays", absentDays);

        Map<String, Object> result = new HashMap<>();
        result.put("records", records);
        result.put("stats", stats);

        return Result.success(result);
    }
}
