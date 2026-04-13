package com.kadmin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kadmin.entity.*;
import com.kadmin.mapper.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AttendanceService {
    private final AttClockRecordMapper clockRecordMapper;
    private final AttDailyRecordMapper dailyRecordMapper;
    private final AttScheduleMapper scheduleMapper;
    private final AttShiftMapper shiftMapper;
    private final AttShiftPeriodMapper shiftPeriodMapper;
    private final AttCalendarRuleMapper calendarRuleMapper;
    private final EmployeeMapper employeeMapper;

    // 打卡记录分页查询
    public IPage<AttClockRecord> getClockRecordPage(int page, int size, List<Long> orgIds, String employeeName,
            String startDate, String endDate) {
        return clockRecordMapper.selectPageWithEmployee(new Page<>(page, size), orgIds, employeeName, startDate,
                endDate);
    }

    // 保存打卡记录
    public void saveClockRecord(AttClockRecord record) {
        if (record.getId() != null) {
            clockRecordMapper.updateById(record);
        } else {
            clockRecordMapper.insert(record);
        }
    }

    // 删除打卡记录
    public void deleteClockRecord(Long id) {
        clockRecordMapper.deleteById(id);
    }

    // 日考勤分页查询（按员工+日期聚合，数据库层面分页）
    public IPage<AttDailyRecord> getDailyRecordPage(int page, int size, String startDate, String endDate,
            List<Long> orgIds, String employeeNo, String employeeName, Integer status) {
        IPage<AttDailyRecord> resultPage = dailyRecordMapper.selectGroupedPage(
                new Page<>(page, size), startDate, endDate, orgIds, employeeNo, employeeName, status);

        // 为每条聚合记录加载各时段明细
        for (AttDailyRecord grouped : resultPage.getRecords()) {
            List<AttDailyRecord> periods = dailyRecordMapper.selectListWithEmployee(
                    grouped.getAttDate().toString(), grouped.getAttDate().toString(),
                    null, null, null, null);
            List<AttDailyRecord> empPeriods = periods.stream()
                    .filter(r -> r.getEmployeeId().equals(grouped.getEmployeeId()))
                    .toList();
            grouped.setPeriods(empPeriods);
        }

        return resultPage;
    }

    // 保存日考勤记录
    public void saveDailyRecord(AttDailyRecord record) {
        if (record.getId() != null) {
            dailyRecordMapper.updateById(record);
        } else {
            dailyRecordMapper.insert(record);
        }
    }

    // 锁定/解锁日考勤记录
    public void lockDailyRecords(List<Long> ids, boolean lock, Long userId) {
        if (ids == null || ids.isEmpty())
            return;
        for (Long id : ids) {
            AttDailyRecord record = dailyRecordMapper.selectById(id);
            if (record != null) {
                record.setLocked(lock ? 1 : 0);
                record.setLockedBy(lock ? userId : null);
                record.setLockedTime(lock ? java.time.LocalDateTime.now() : null);
                dailyRecordMapper.updateById(record);
            }
        }
    }

    // 月考勤汇总
    public List<java.util.Map<String, Object>> getMonthlyAttendance(String month, List<Long> orgIds, String employeeNo,
            String employeeName) {
        // 计算月份的开始和结束日期
        String startDate = month + "-01";
        java.time.YearMonth ym = java.time.YearMonth.parse(month);
        String endDate = month + "-" + String.format("%02d", ym.lengthOfMonth());

        // 查询该月所有日考勤记录
        List<AttDailyRecord> allRecords = dailyRecordMapper.selectListWithEmployee(startDate, endDate, orgIds,
                employeeNo, employeeName, null);

        // 先按 员工ID -> 日期 -> 时段列表 分组
        java.util.Map<Long, java.util.Map<LocalDate, List<AttDailyRecord>>> empDateMap = new java.util.LinkedHashMap<>();
        java.util.Map<Long, AttDailyRecord> empInfoMap = new java.util.LinkedHashMap<>(); // 保存员工基本信息

        for (AttDailyRecord record : allRecords) {
            Long empId = record.getEmployeeId();
            empInfoMap.putIfAbsent(empId, record);
            empDateMap.computeIfAbsent(empId, k -> new java.util.LinkedHashMap<>())
                    .computeIfAbsent(record.getAttDate(), k -> new java.util.ArrayList<>())
                    .add(record);
        }

        // 按员工汇总
        List<java.util.Map<String, Object>> result = new java.util.ArrayList<>();

        for (java.util.Map.Entry<Long, java.util.Map<LocalDate, List<AttDailyRecord>>> empEntry : empDateMap
                .entrySet()) {
            Long empId = empEntry.getKey();
            java.util.Map<LocalDate, List<AttDailyRecord>> dateRecords = empEntry.getValue();
            AttDailyRecord empInfo = empInfoMap.get(empId);

            java.util.Map<String, Object> summary = new java.util.HashMap<>();
            summary.put("employeeId", empId);
            summary.put("employeeName", empInfo.getEmployeeName());
            summary.put("employeeNo", empInfo.getEmployeeNo());
            summary.put("companyName", empInfo.getCompanyName());
            summary.put("deptName", empInfo.getDeptName());
            summary.put("month", month);

            int workDays = 0;
            int actualDays = 0;
            int lateTimes = 0;
            int earlyTimes = 0;
            int absentDays = 0;
            int leaveDays = 0;
            java.math.BigDecimal totalWorkHours = java.math.BigDecimal.ZERO;
            int totalLateMinutes = 0;
            int totalEarlyMinutes = 0;

            for (java.util.Map.Entry<LocalDate, List<AttDailyRecord>> dateEntry : dateRecords.entrySet()) {
                List<AttDailyRecord> dayRecords = dateEntry.getValue();
                workDays++;

                // 按天判定状态
                boolean hasAbsent = dayRecords.stream().anyMatch(r -> r.getStatus() != null && r.getStatus() == 4);
                boolean hasLeave = dayRecords.stream().anyMatch(r -> r.getStatus() != null && r.getStatus() == 5);
                boolean dayHasLate = false;
                boolean dayHasEarly = false;

                for (AttDailyRecord r : dayRecords) {
                    // 累计工时
                    if (r.getWorkHours() != null) {
                        totalWorkHours = totalWorkHours.add(r.getWorkHours());
                    }
                    // 累计迟到/早退分钟
                    if (r.getLateMinutes() != null && r.getLateMinutes() > 0) {
                        totalLateMinutes += r.getLateMinutes();
                        dayHasLate = true;
                    }
                    if (r.getEarlyMinutes() != null && r.getEarlyMinutes() > 0) {
                        totalEarlyMinutes += r.getEarlyMinutes();
                        dayHasEarly = true;
                    }
                }

                // 迟到/早退次数按天统计（一天算一次）
                if (dayHasLate)
                    lateTimes++;
                if (dayHasEarly)
                    earlyTimes++;

                // 出勤/旷工/请假按天统计
                if (hasAbsent) {
                    absentDays++;
                } else if (hasLeave) {
                    leaveDays++;
                } else if (dayRecords.stream().anyMatch(r -> r.getActualIn() != null || r.getActualOut() != null)) {
                    actualDays++;
                }
            }

            summary.put("workDays", workDays);
            summary.put("actualDays", actualDays);
            summary.put("lateTimes", lateTimes);
            summary.put("earlyTimes", earlyTimes);
            summary.put("absentDays", absentDays);
            summary.put("leaveDays", leaveDays);
            summary.put("totalWorkHours", totalWorkHours);
            summary.put("totalLateMinutes", totalLateMinutes);
            summary.put("totalEarlyMinutes", totalEarlyMinutes);
            result.add(summary);
        }

        return result;
    }

    /**
     * 计算指定日期范围的日考勤
     *
     * @param startDate    开始日期
     * @param endDate      结束日期
     * @param orgIds       组织ID列表筛选
     * @param employeeNo   工号筛选
     * @param employeeName 姓名筛选
     * @param employeeIds  指定员工ID列表（优先级最高）
     */
    public void calculateDailyAttendance(LocalDate startDate, LocalDate endDate, List<Long> orgIds, String employeeNo,
            String employeeName, List<Long> employeeIds) {
        // 获取员工列表
        LambdaQueryWrapper<HrEmployee> wrapper = new LambdaQueryWrapper<HrEmployee>()
                .eq(HrEmployee::getStatus, 1);

        // 如果指定了员工ID列表，优先使用
        if (employeeIds != null && !employeeIds.isEmpty()) {
            wrapper.in(HrEmployee::getId, employeeIds);
        } else {
            // 否则按搜索条件筛选
            if (orgIds != null && !orgIds.isEmpty()) {
                wrapper.in(HrEmployee::getDeptId, orgIds);
            }
            if (employeeNo != null && !employeeNo.isEmpty()) {
                wrapper.like(HrEmployee::getEmployeeNo, employeeNo);
            }
            if (employeeName != null && !employeeName.isEmpty()) {
                wrapper.like(HrEmployee::getName, employeeName);
            }
        }

        List<HrEmployee> employees = employeeMapper.selectList(wrapper);

        // 遍历日期范围
        LocalDate currentDate = startDate;
        while (!currentDate.isAfter(endDate)) {
            for (HrEmployee emp : employees) {
                calculateEmployeeDailyAttendance(emp, currentDate);
            }
            currentDate = currentDate.plusDays(1);
        }
    }

    /**
     * 计算单个员工某天的考勤（按时段）
     */
    private void calculateEmployeeDailyAttendance(HrEmployee emp, LocalDate date) {
        // 1. 检查是否是休息日（使用员工部门ID）
        if (isRestDay(emp.getDeptId(), date)) {
            return; // 休息日不生成考勤记录
        }

        // 2. 获取员工排班
        AttSchedule schedule = scheduleMapper.selectOne(
                new LambdaQueryWrapper<AttSchedule>()
                        .eq(AttSchedule::getEmployeeId, emp.getId())
                        .eq(AttSchedule::getScheduleDate, date));

        if (schedule == null || schedule.getShiftId() == null) {
            return; // 没有排班不生成考勤
        }

        // 3. 获取班次信息
        AttShift shift = shiftMapper.selectById(schedule.getShiftId());
        if (shift == null) {
            return;
        }

        // 4. 获取班次时段
        List<AttShiftPeriod> periods = shiftPeriodMapper.selectList(
                new LambdaQueryWrapper<AttShiftPeriod>()
                        .eq(AttShiftPeriod::getShiftId, shift.getId())
                        .orderByAsc(AttShiftPeriod::getSortOrder));

        // 5. 获取当天打卡记录
        List<AttClockRecord> clockRecords = clockRecordMapper.selectList(
                new LambdaQueryWrapper<AttClockRecord>()
                        .eq(AttClockRecord::getEmployeeId, emp.getId())
                        .apply("DATE(clock_time) = {0}", date)
                        .orderByAsc(AttClockRecord::getClockTime));

        // 6. 如果没有时段，使用班次的整体时间作为一个时段
        if (periods == null || periods.isEmpty()) {
            calculateSinglePeriod(emp, date, shift, null, shift.getWorkStartTime(), shift.getWorkEndTime(), "全天",
                    clockRecords);
        } else {
            // 按每个时段计算
            for (AttShiftPeriod period : periods) {
                LocalTime startTime = LocalTime.parse(period.getStartTime());
                LocalTime endTime = LocalTime.parse(period.getEndTime());
                calculateSinglePeriod(emp, date, shift, period.getId(), startTime, endTime, period.getPeriodName(),
                        clockRecords);
            }
        }
    }

    /**
     * 计算单个时段的考勤
     */
    private void calculateSinglePeriod(HrEmployee emp, LocalDate date, AttShift shift, Long periodId,
            LocalTime scheduledIn, LocalTime scheduledOut, String periodName, List<AttClockRecord> clockRecords) {

        // 查找或创建该时段的考勤记录
        LambdaQueryWrapper<AttDailyRecord> wrapper = new LambdaQueryWrapper<AttDailyRecord>()
                .eq(AttDailyRecord::getEmployeeId, emp.getId())
                .eq(AttDailyRecord::getAttDate, date);
        if (periodId != null) {
            wrapper.eq(AttDailyRecord::getPeriodId, periodId);
        } else {
            wrapper.isNull(AttDailyRecord::getPeriodId);
        }

        AttDailyRecord dailyRecord = dailyRecordMapper.selectOne(wrapper);

        // 如果已锁定，跳过计算
        if (dailyRecord != null && dailyRecord.getLocked() != null && dailyRecord.getLocked() == 1) {
            return;
        }

        if (dailyRecord == null) {
            dailyRecord = new AttDailyRecord();
            dailyRecord.setEmployeeId(emp.getId());
            dailyRecord.setAttDate(date);
        }

        // 设置排班信息
        dailyRecord.setShiftId(shift.getId());
        dailyRecord.setPeriodId(periodId);
        dailyRecord.setPeriodName(periodName);
        dailyRecord.setScheduledIn(scheduledIn);
        dailyRecord.setScheduledOut(scheduledOut);

        // 匹配该时段的打卡记录
        LocalTime actualIn = null;
        LocalTime actualOut = null;

        // 时段时间范围（前后各扩展1小时用于匹配打卡）
        LocalTime matchStart = scheduledIn.minusHours(1);
        LocalTime matchEnd = scheduledOut.plusHours(1);

        for (AttClockRecord record : clockRecords) {
            LocalTime clockTime = record.getClockTime().toLocalTime();

            // 判断打卡时间是否在该时段范围内
            if (clockTime.isAfter(matchStart) && clockTime.isBefore(matchEnd)) {
                if (record.getClockType() == 1) { // 上班打卡
                    // 取最接近应打卡时间的上班打卡
                    if (actualIn == null || Math.abs(ChronoUnit.MINUTES.between(clockTime, scheduledIn)) < Math
                            .abs(ChronoUnit.MINUTES.between(actualIn, scheduledIn))) {
                        actualIn = clockTime;
                    }
                } else { // 下班打卡
                    // 取最接近应打卡时间的下班打卡
                    if (actualOut == null || Math.abs(ChronoUnit.MINUTES.between(clockTime, scheduledOut)) < Math
                            .abs(ChronoUnit.MINUTES.between(actualOut, scheduledOut))) {
                        actualOut = clockTime;
                    }
                }
            }
        }

        dailyRecord.setActualIn(actualIn);
        dailyRecord.setActualOut(actualOut);

        // 计算考勤状态
        int status = calculatePeriodStatus(scheduledIn, scheduledOut, actualIn, actualOut);
        dailyRecord.setStatus(status);

        // 计算迟到分钟数
        int lateMinutes = 0;
        if (actualIn != null && scheduledIn != null && actualIn.isAfter(scheduledIn)) {
            lateMinutes = (int) ChronoUnit.MINUTES.between(scheduledIn, actualIn);
        }
        dailyRecord.setLateMinutes(lateMinutes);

        // 计算早退分钟数
        int earlyMinutes = 0;
        if (actualOut != null && scheduledOut != null && actualOut.isBefore(scheduledOut)) {
            earlyMinutes = (int) ChronoUnit.MINUTES.between(actualOut, scheduledOut);
        }
        dailyRecord.setEarlyMinutes(earlyMinutes);

        // 计算工作时长（基于实际打卡时间，但不超过排班时长）
        BigDecimal workHours = BigDecimal.ZERO;
        if (actualIn != null && actualOut != null && scheduledIn != null && scheduledOut != null) {
            // 实际有效开始时间 = max(actualIn, scheduledIn)（早到不多算）
            LocalTime effectiveIn = actualIn.isBefore(scheduledIn) ? scheduledIn : actualIn;
            // 实际有效结束时间 = min(actualOut, scheduledOut)（晚走不多算）
            LocalTime effectiveOut = actualOut.isAfter(scheduledOut) ? scheduledOut : actualOut;
            long actualMinutes = ChronoUnit.MINUTES.between(effectiveIn, effectiveOut);
            if (actualMinutes > 0) {
                workHours = BigDecimal.valueOf(actualMinutes).divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);
            }
        }
        dailyRecord.setWorkHours(workHours);

        // 保存
        if (dailyRecord.getId() != null) {
            dailyRecordMapper.updateById(dailyRecord);
        } else {
            dailyRecordMapper.insert(dailyRecord);
        }
    }

    /**
     * 计算时段考勤状态
     */
    private int calculatePeriodStatus(LocalTime scheduledIn, LocalTime scheduledOut, LocalTime actualIn,
            LocalTime actualOut) {
        // 0-未处理 1-正常 2-迟到 3-早退 4-旷工 5-请假 6-出差 7-迟到+早退
        if (actualIn == null && actualOut == null) {
            return 4; // 旷工
        }

        boolean isLate = actualIn != null && scheduledIn != null && actualIn.isAfter(scheduledIn);
        boolean isEarly = actualOut != null && scheduledOut != null && actualOut.isBefore(scheduledOut);

        if (isLate && isEarly) {
            return 7; // 迟到+早退
        } else if (isLate) {
            return 2; // 迟到
        } else if (isEarly) {
            return 3; // 早退
        } else if (actualIn == null || actualOut == null) {
            return 0; // 未处理（缺少打卡）
        }

        return 1; // 正常
    }

    /**
     * 判断是否是休息日
     */
    private boolean isRestDay(Long companyId, LocalDate date) {
        // 1. 检查是否是调休上班日（优先级最高）
        AttCalendarRule workDay = calendarRuleMapper.selectOne(
                new LambdaQueryWrapper<AttCalendarRule>()
                        .eq(AttCalendarRule::getCompanyId, companyId)
                        .eq(AttCalendarRule::getRuleType, 5) // 调休上班
                        .le(AttCalendarRule::getStartDate, date)
                        .ge(AttCalendarRule::getEndDate, date));
        if (workDay != null) {
            return false; // 调休上班，不是休息日
        }

        // 2. 检查是否是法定假日
        AttCalendarRule holiday = calendarRuleMapper.selectOne(
                new LambdaQueryWrapper<AttCalendarRule>()
                        .eq(AttCalendarRule::getCompanyId, companyId)
                        .eq(AttCalendarRule::getRuleType, 4) // 法定假日
                        .le(AttCalendarRule::getStartDate, date)
                        .ge(AttCalendarRule::getEndDate, date));
        if (holiday != null) {
            return true; // 法定假日是休息日
        }

        // 3. 检查公司休息规则
        AttCalendarRule restRule = calendarRuleMapper.selectOne(
                new LambdaQueryWrapper<AttCalendarRule>()
                        .eq(AttCalendarRule::getCompanyId, companyId)
                        .in(AttCalendarRule::getRuleType, 1, 2, 3) // 单休周日、双休、单休周六
        );

        if (restRule != null) {
            DayOfWeek dayOfWeek = date.getDayOfWeek();
            int ruleType = restRule.getRuleType();
            if (ruleType == 2) { // 双休
                return dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY;
            } else if (ruleType == 1) { // 单休周日
                return dayOfWeek == DayOfWeek.SUNDAY;
            } else if (ruleType == 3) { // 单休周六
                return dayOfWeek == DayOfWeek.SATURDAY;
            }
        }

        return false;
    }
}
