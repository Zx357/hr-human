package com.kadmin.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.kadmin.common.Result;
import com.kadmin.entity.AttClockRecord;
import com.kadmin.entity.AttDailyRecord;
import com.kadmin.entity.AttLocation;
import com.kadmin.entity.AttSchedule;
import com.kadmin.entity.AttShift;
import com.kadmin.entity.HrEmployee;
import com.kadmin.entity.OrgUnit;
import com.kadmin.mapper.AttClockRecordMapper;
import com.kadmin.mapper.AttDailyRecordMapper;
import com.kadmin.mapper.AttScheduleMapper;
import com.kadmin.mapper.AttShiftMapper;
import com.kadmin.mapper.EmployeeMapper;
import com.kadmin.security.LoginUser;
import com.kadmin.service.OrgUnitService;
import com.kadmin.service.AttLocationService;
import com.kadmin.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Mobile attendance APIs.
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
    private final OrgUnitService orgUnitService;
    private final AttLocationService attLocationService;

    @GetMapping("/clock/info")
    public Result<Map<String, Object>> getClockInfo() {
        LoginUser loginUser = SecurityUtils.getCurrentUser();
        if (loginUser == null) {
            return Result.error("请先登录");
        }

        Long employeeId = getCurrentEmployeeId(loginUser);
        Map<String, Object> result = new HashMap<>();
        result.put("serverTime", LocalDateTime.now());

        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.atTime(LocalTime.MAX);

        List<AttClockRecord> todayRecords = clockRecordMapper.selectList(
                new LambdaQueryWrapper<AttClockRecord>()
                        .eq(AttClockRecord::getEmployeeId, employeeId)
                        .between(AttClockRecord::getClockTime, startOfDay, endOfDay)
                        .orderByAsc(AttClockRecord::getClockTime));
        result.put("todayRecords", todayRecords);
        result.put("hasClockedIn", todayRecords.stream().anyMatch(record -> Objects.equals(record.getClockType(), 1)));
        result.put("hasClockedOut", todayRecords.stream().anyMatch(record -> Objects.equals(record.getClockType(), 2)));

        List<AttSchedule> todaySchedules = scheduleMapper.selectList(
                new LambdaQueryWrapper<AttSchedule>()
                        .eq(AttSchedule::getEmployeeId, employeeId)
                        .eq(AttSchedule::getScheduleDate, today)
                        .orderByDesc(AttSchedule::getId));
        AttSchedule todaySchedule = todaySchedules.isEmpty() ? null : todaySchedules.get(0);
        if (todaySchedule != null && todaySchedule.getShiftId() != null) {
            AttShift shift = shiftMapper.selectById(todaySchedule.getShiftId());
            if (shift != null) {
                result.put(
                        "scheduledIn",
                        shift.getWorkStartTime() != null ? shift.getWorkStartTime().toString().substring(0, 5) : null);
                result.put(
                        "scheduledOut",
                        shift.getWorkEndTime() != null ? shift.getWorkEndTime().toString().substring(0, 5) : null);
            }
        }

        List<AttDailyRecord> todayDailyRecords = dailyRecordMapper.selectList(
                new LambdaQueryWrapper<AttDailyRecord>()
                        .eq(AttDailyRecord::getEmployeeId, employeeId)
                        .eq(AttDailyRecord::getAttDate, today)
                        .orderByAsc(AttDailyRecord::getPeriodId));
        if (!todayDailyRecords.isEmpty()) {
            Map<String, Object> todayDaily = new HashMap<>();
            todayDaily.put("status", mergeDailyStatus(todayDailyRecords));
            todayDaily.put("lateMinutes", sumLateMinutes(todayDailyRecords));
            todayDaily.put("earlyMinutes", sumEarlyMinutes(todayDailyRecords));
            result.put("todayDaily", todayDaily);
        }

        List<AttLocation> locations = attLocationService.getAssignedActiveLocations(employeeId);
        AttLocation location = locations.stream().filter(this::hasAttendanceConfig).findFirst().orElse(null);
        result.put("clockLocations", locations);
        if (location != null) {
            result.put("companyId", location.getId());
            result.put("companyName", location.getLocationName());
            result.put(
                    "companyAddress",
                    StringUtils.hasText(location.getAddress()) ? location.getAddress() : location.getLocationName());
            result.put("companyLat", location.getLatitude());
            result.put("companyLng", location.getLongitude());
            result.put("clockRange", location.getClockRange());
            result.put("attendanceConfigured", hasAttendanceConfig(location));
        } else {
            result.put("attendanceConfigured", false);
        }

        return Result.success(result);
    }

    @PostMapping("/clock")
    public Result<AttClockRecord> clock(@RequestBody Map<String, Object> params) {
        LoginUser loginUser = SecurityUtils.getCurrentUser();
        if (loginUser == null) {
            return Result.error("请先登录");
        }

        Long employeeId = getCurrentEmployeeId(loginUser);
        Integer clockType = resolveClockType(employeeId, params.get("clockType"));

        List<AttLocation> assignedLocations = attLocationService.getAssignedActiveLocations(employeeId);
        List<AttLocation> configuredLocations = assignedLocations.stream().filter(this::hasAttendanceConfig).toList();
        if (!configuredLocations.isEmpty()) {
            String location = params.get("location") instanceof String ? (String) params.get("location") : null;
            double[] userPoint = parseLocation(location);
            if (userPoint == null) {
                return Result.error("未获取到当前位置，请开启定位后重试");
            }

            double tolerance = resolveAccuracyToleranceMeters(params.get("accuracy"));
            double minExceeded = Double.MAX_VALUE;
            for (AttLocation assignedLocation : configuredLocations) {
                double distance = calculateDistanceMeters(
                        userPoint[0],
                        userPoint[1],
                        assignedLocation.getLatitude(),
                        assignedLocation.getLongitude());
                double allowedRange = assignedLocation.getClockRange().doubleValue() + tolerance;
                if (distance <= allowedRange) {
                    minExceeded = 0D;
                    break;
                }
                minExceeded = Math.min(minExceeded, distance - allowedRange);
            }
            if (minExceeded > 0D) {
                long exceeded = Math.round(minExceeded);
                return Result.error("当前位置不在打卡范围内，超出约 " + exceeded + " 米");
            }
        }

        AttClockRecord record = new AttClockRecord();
        record.setEmployeeId(employeeId);
        record.setClockTime(LocalDateTime.now());
        record.setClockType(clockType);
        record.setClockMethod(1);
        record.setLocation(params.get("location") instanceof String ? (String) params.get("location") : null);
        record.setDeviceInfo(params.get("deviceInfo") instanceof String ? (String) params.get("deviceInfo") : null);
        record.setRemark(clockType == 1 ? "移动端上班打卡" : "移动端下班打卡");

        clockRecordMapper.insert(record);
        return Result.success(record);
    }

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

        Long employeeId = getCurrentEmployeeId(loginUser);
        LambdaQueryWrapper<AttClockRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AttClockRecord::getEmployeeId, employeeId);

        if (StringUtils.hasText(startDate)) {
            wrapper.ge(AttClockRecord::getClockTime, LocalDate.parse(startDate).atStartOfDay());
        }
        if (StringUtils.hasText(endDate)) {
            wrapper.le(AttClockRecord::getClockTime, LocalDate.parse(endDate).atTime(LocalTime.MAX));
        }

        wrapper.orderByDesc(AttClockRecord::getClockTime);

        long total = clockRecordMapper.selectCount(wrapper);
        wrapper.last("LIMIT " + (pageNum - 1) * pageSize + ", " + pageSize);
        List<AttClockRecord> records = clockRecordMapper.selectList(wrapper);

        Map<String, Object> result = new HashMap<>();
        result.put("records", records);
        result.put("total", total);
        return Result.success(result);
    }

    @GetMapping("/month")
    public Result<Map<String, Object>> getMonthAttendance(@RequestParam String month) {
        LoginUser loginUser = SecurityUtils.getCurrentUser();
        if (loginUser == null) {
            return Result.error("请先登录");
        }

        Long employeeId = getCurrentEmployeeId(loginUser);
        YearMonth yearMonth = YearMonth.parse(month);
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        List<AttClockRecord> clockRecords = clockRecordMapper.selectList(
                new LambdaQueryWrapper<AttClockRecord>()
                        .eq(AttClockRecord::getEmployeeId, employeeId)
                        .between(AttClockRecord::getClockTime, startDate.atStartOfDay(), endDate.atTime(LocalTime.MAX))
                        .orderByAsc(AttClockRecord::getClockTime));

        List<AttSchedule> schedules = scheduleMapper.selectList(
                new LambdaQueryWrapper<AttSchedule>()
                        .eq(AttSchedule::getEmployeeId, employeeId)
                        .between(AttSchedule::getScheduleDate, startDate, endDate));

        List<AttDailyRecord> dailyRecords = dailyRecordMapper.selectList(
                new LambdaQueryWrapper<AttDailyRecord>()
                        .eq(AttDailyRecord::getEmployeeId, employeeId)
                        .between(AttDailyRecord::getAttDate, startDate, endDate));

        Map<String, List<AttClockRecord>> clockByDate = new LinkedHashMap<>();
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

        for (AttClockRecord record : clockRecords) {
            String dateKey = record.getClockTime().toLocalDate().format(dateFormatter);
            clockByDate.computeIfAbsent(dateKey, key -> new ArrayList<>()).add(record);
        }

        Map<LocalDate, AttDailyRecord> dailyByDate = new LinkedHashMap<>();
        for (AttDailyRecord record : dailyRecords) {
            dailyByDate.putIfAbsent(record.getAttDate(), record);
        }

        Set<LocalDate> scheduledDates = new HashSet<>();
        for (AttSchedule schedule : schedules) {
            if (schedule.getShiftId() != null) {
                scheduledDates.add(schedule.getScheduleDate());
            }
        }

        List<Map<String, Object>> records = new ArrayList<>();
        int normalIn = 0;
        int normalOut = 0;
        int lateDays = 0;
        int earlyDays = 0;
        int absentDays = 0;

        LocalDate current = startDate;
        LocalDate today = LocalDate.now();

        while (!current.isAfter(endDate)) {
            String dateString = current.format(dateFormatter);
            List<AttClockRecord> dayClockRecords = clockByDate.getOrDefault(dateString, Collections.emptyList());
            AttDailyRecord dailyRecord = dailyByDate.get(current);

            Map<String, Object> dayData = new HashMap<>();
            dayData.put("attDate", dateString);

            AttClockRecord clockInRecord = dayClockRecords.stream()
                    .filter(record -> Objects.equals(record.getClockType(), 1))
                    .findFirst()
                    .orElse(null);
            AttClockRecord clockOutRecord = dayClockRecords.stream()
                    .filter(record -> Objects.equals(record.getClockType(), 2))
                    .findFirst()
                    .orElse(null);

            if (clockInRecord != null) {
                dayData.put("clockIn", clockInRecord.getClockTime().toLocalTime().format(timeFormatter));
            }
            if (clockOutRecord != null) {
                dayData.put("clockOut", clockOutRecord.getClockTime().toLocalTime().format(timeFormatter));
            }

            boolean isLate = false;
            boolean isEarly = false;
            boolean isNormalIn = false;
            boolean isNormalOut = false;

            if (dailyRecord != null) {
                Integer status = dailyRecord.getStatus();
                if (status != null) {
                    isLate = status == 2 || status == 7;
                    isEarly = status == 3 || status == 7;
                    isNormalIn = clockInRecord != null && !isLate;
                    isNormalOut = clockOutRecord != null && !isEarly;
                }
            } else if (clockInRecord != null || clockOutRecord != null) {
                isNormalIn = clockInRecord != null;
                isNormalOut = clockOutRecord != null;
            }

            dayData.put("lateIn", isLate);
            dayData.put("earlyOut", isEarly);
            dayData.put("normalIn", isNormalIn);
            dayData.put("normalOut", isNormalOut);

            boolean isPast = !current.isAfter(today);
            boolean isScheduled = scheduledDates.contains(current) || !dayClockRecords.isEmpty();
            if (isPast && isScheduled) {
                if (isNormalIn) {
                    normalIn++;
                }
                if (isNormalOut) {
                    normalOut++;
                }
                if (isLate) {
                    lateDays++;
                }
                if (isEarly) {
                    earlyDays++;
                }
                if (clockInRecord == null || clockOutRecord == null) {
                    absentDays++;
                }
            }

            if (!dayClockRecords.isEmpty() || dailyRecord != null) {
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

    private Long getCurrentEmployeeId(LoginUser loginUser) {
        return loginUser.getEmployeeId() != null ? loginUser.getEmployeeId() : loginUser.getUserId();
    }

    private Integer resolveClockType(Long employeeId, Object clockTypeValue) {
        if (clockTypeValue instanceof Number number) {
            return number.intValue();
        }

        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.atTime(LocalTime.MAX);
        long inCount = clockRecordMapper.selectCount(
                new LambdaQueryWrapper<AttClockRecord>()
                        .eq(AttClockRecord::getEmployeeId, employeeId)
                        .eq(AttClockRecord::getClockType, 1)
                        .between(AttClockRecord::getClockTime, startOfDay, endOfDay));
        return inCount == 0 ? 1 : 2;
    }

    private OrgUnit getEmployeeCompany(Long employeeId) {
        HrEmployee employee = employeeMapper.selectById(employeeId);
        if (employee == null || employee.getDeptId() == null) {
            return null;
        }

        Long companyId = orgUnitService.getCompanyId(employee.getDeptId());
        return companyId == null ? null : orgUnitService.getById(companyId);
    }

    private boolean hasAttendanceConfig(OrgUnit company) {
        return company != null
                && company.getAttendanceLatitude() != null
                && company.getAttendanceLongitude() != null
                && company.getAttendanceRange() != null
                && company.getAttendanceRange() > 0;
    }

    private boolean hasAttendanceConfig(AttLocation location) {
        return location != null
                && location.getLatitude() != null
                && location.getLongitude() != null
                && location.getClockRange() != null
                && location.getClockRange() > 0;
    }

    private double[] parseLocation(String location) {
        if (!StringUtils.hasText(location)) {
            return null;
        }

        String[] parts = location.split(",");
        if (parts.length != 2) {
            return null;
        }

        try {
            double latitude = Double.parseDouble(parts[0].trim());
            double longitude = Double.parseDouble(parts[1].trim());
            return new double[] { latitude, longitude };
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private double resolveAccuracyToleranceMeters(Object accuracyValue) {
        if (accuracyValue == null) {
            return 0D;
        }

        try {
            double accuracy = Double.parseDouble(String.valueOf(accuracyValue));
            if (accuracy <= 0D || accuracy > 200D) {
                return 0D;
            }
            return Math.min(Math.round(accuracy), 80D);
        } catch (NumberFormatException exception) {
            return 0D;
        }
    }

    private double calculateDistanceMeters(double lat1, double lng1, double lat2, double lng2) {
        double radLat1 = Math.toRadians(lat1);
        double radLat2 = Math.toRadians(lat2);
        double deltaLat = radLat1 - radLat2;
        double deltaLng = Math.toRadians(lng1 - lng2);

        double distance = 2 * Math.asin(Math.sqrt(
                Math.pow(Math.sin(deltaLat / 2), 2)
                        + Math.cos(radLat1) * Math.cos(radLat2) * Math.pow(Math.sin(deltaLng / 2), 2)));
        return distance * 6378137D;
    }

    private int sumLateMinutes(List<AttDailyRecord> dailyRecords) {
        return dailyRecords.stream()
                .map(AttDailyRecord::getLateMinutes)
                .filter(Objects::nonNull)
                .mapToInt(Integer::intValue)
                .sum();
    }

    private int sumEarlyMinutes(List<AttDailyRecord> dailyRecords) {
        return dailyRecords.stream()
                .map(AttDailyRecord::getEarlyMinutes)
                .filter(Objects::nonNull)
                .mapToInt(Integer::intValue)
                .sum();
    }

    private int mergeDailyStatus(List<AttDailyRecord> dailyRecords) {
        boolean hasNormal = false;
        boolean hasLate = false;
        boolean hasEarly = false;
        boolean hasAbsent = false;
        boolean hasLeave = false;
        boolean hasBusiness = false;

        for (AttDailyRecord dailyRecord : dailyRecords) {
            Integer status = dailyRecord.getStatus();
            if (status == null) {
                continue;
            }

            switch (status) {
                case 1 -> hasNormal = true;
                case 2 -> hasLate = true;
                case 3 -> hasEarly = true;
                case 4 -> hasAbsent = true;
                case 5 -> hasLeave = true;
                case 6 -> hasBusiness = true;
                case 7 -> {
                    hasLate = true;
                    hasEarly = true;
                }
                default -> {
                }
            }
        }

        if (hasLate && hasEarly) {
            return 7;
        }
        if (hasAbsent) {
            return 4;
        }
        if (hasLeave) {
            return 5;
        }
        if (hasBusiness) {
            return 6;
        }
        if (hasLate) {
            return 2;
        }
        if (hasEarly) {
            return 3;
        }
        return hasNormal ? 1 : 0;
    }
}
