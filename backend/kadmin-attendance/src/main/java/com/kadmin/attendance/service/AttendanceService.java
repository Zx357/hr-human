package com.kadmin.attendance.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kadmin.attendance.domain.AttCalendarRule;
import com.kadmin.attendance.domain.AttClockRecord;
import com.kadmin.attendance.domain.AttDailyRecord;
import com.kadmin.attendance.domain.AttSchedule;
import com.kadmin.attendance.domain.AttShift;
import com.kadmin.attendance.domain.AttShiftPeriod;
import com.kadmin.attendance.mapper.AttCalendarRuleMapper;
import com.kadmin.attendance.mapper.AttClockRecordMapper;
import com.kadmin.attendance.mapper.AttDailyRecordMapper;
import com.kadmin.attendance.mapper.AttScheduleMapper;
import com.kadmin.attendance.mapper.AttShiftMapper;
import com.kadmin.attendance.mapper.AttShiftPeriodMapper;
import com.kadmin.hr.domain.HrApplication;
import com.kadmin.hr.domain.HrEmployee;
import com.kadmin.hr.mapper.EmployeeMapper;
import com.kadmin.hr.mapper.HrApplicationMapper;
import com.kadmin.organization.domain.OrgUnit;
import com.kadmin.organization.mapper.OrgUnitMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

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
    private final OrgUnitMapper orgUnitMapper;
    private final HrApplicationMapper applicationMapper;

    // 打卡记录分页查询
    public IPage<AttClockRecord> getClockRecordPage(int page, int size, List<Long> orgIds, String employeeName,
            String startDate, String endDate) {
        return clockRecordMapper.selectPageWithEmployee(new Page<>(page, size), expandOrgIds(orgIds), employeeName,
                startDate,
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
                new Page<>(page, size), startDate, endDate, expandOrgIds(orgIds), employeeNo, employeeName, status);

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
        List<AttDailyRecord> allRecords = dailyRecordMapper.selectListWithEmployee(startDate, endDate,
                expandOrgIds(orgIds),
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
                wrapper.in(HrEmployee::getDeptId, expandOrgIds(orgIds));
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
        // 1. 检查是否是休息日（考勤日历规则挂在公司上）
        Long companyId = resolveCompanyId(emp.getDeptId());
        if (isRestDay(companyId, date)) {
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

        List<ShiftPeriodPlan> periodPlans = buildPeriodPlans(date, shift, periods);
        if (periodPlans.isEmpty()) {
            return;
        }

        LocalDateTime queryStart = periodPlans.stream()
                .map(ShiftPeriodPlan::scheduledIn)
                .min(Comparator.naturalOrder())
                .orElse(date.atStartOfDay())
                .minusHours(1);
        LocalDateTime queryEnd = periodPlans.stream()
                .map(ShiftPeriodPlan::scheduledOut)
                .max(Comparator.naturalOrder())
                .orElse(date.plusDays(1).atStartOfDay())
                .plusHours(1);

        // 5. 获取班次窗口内的打卡记录，支持跨天班次
        List<AttClockRecord> clockRecords = clockRecordMapper.selectList(
                new LambdaQueryWrapper<AttClockRecord>()
                        .eq(AttClockRecord::getEmployeeId, emp.getId())
                        .between(AttClockRecord::getClockTime, queryStart, queryEnd)
                        .orderByAsc(AttClockRecord::getClockTime));

        LocalDateTime shiftActualIn = clockRecords.stream()
                .filter(record -> record.getClockType() != null && record.getClockType() == 1)
                .map(AttClockRecord::getClockTime)
                .min(Comparator.naturalOrder())
                .orElse(null);
        LocalDateTime shiftActualOut = clockRecords.stream()
                .filter(record -> record.getClockType() != null && record.getClockType() == 2)
                .map(AttClockRecord::getClockTime)
                .max(Comparator.naturalOrder())
                .orElse(null);
        Integer approvedAbsenceStatus = resolveApprovedAbsenceStatus(emp.getId(), date);

        for (ShiftPeriodPlan plan : periodPlans) {
            calculateSinglePeriod(emp, date, shift, plan, clockRecords, shiftActualIn, shiftActualOut,
                    approvedAbsenceStatus);
        }
    }

    /**
     * 计算单个时段的考勤
     */
    private void calculateSinglePeriod(HrEmployee emp, LocalDate date, AttShift shift, ShiftPeriodPlan plan,
            List<AttClockRecord> clockRecords, LocalDateTime shiftActualIn, LocalDateTime shiftActualOut,
            Integer approvedAbsenceStatus) {

        // 查找或创建该时段的考勤记录
        LambdaQueryWrapper<AttDailyRecord> wrapper = new LambdaQueryWrapper<AttDailyRecord>()
                .eq(AttDailyRecord::getEmployeeId, emp.getId())
                .eq(AttDailyRecord::getAttDate, date);
        if (plan.periodId() != null) {
            wrapper.eq(AttDailyRecord::getPeriodId, plan.periodId());
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
        dailyRecord.setPeriodId(plan.periodId());
        dailyRecord.setPeriodName(plan.periodName());
        dailyRecord.setScheduledIn(plan.scheduledInTime());
        dailyRecord.setScheduledOut(plan.scheduledOutTime());

        // 匹配该时段的打卡记录
        LocalDateTime actualIn = findClosestClock(clockRecords, 1, plan.scheduledIn());
        LocalDateTime actualOut = findClosestClock(clockRecords, 2, plan.scheduledOut());
        if (actualIn == null && plan.firstPeriod()) {
            actualIn = shiftActualIn;
        }
        if (actualOut == null && plan.lastPeriod()) {
            actualOut = shiftActualOut;
        }

        dailyRecord.setActualIn(actualIn == null ? null : actualIn.toLocalTime());
        dailyRecord.setActualOut(actualOut == null ? null : actualOut.toLocalTime());

        BigDecimal workHours = calculateWorkHours(plan.scheduledIn(), plan.scheduledOut(), shiftActualIn,
                shiftActualOut);

        int lateTolerance = shift.getLateMinutes() == null ? 0 : Math.max(shift.getLateMinutes(), 0);
        int earlyTolerance = shift.getEarlyMinutes() == null ? 0 : Math.max(shift.getEarlyMinutes(), 0);

        // 计算考勤状态
        int status = calculatePeriodStatus(plan, actualIn, actualOut, workHours, approvedAbsenceStatus,
                lateTolerance, earlyTolerance);
        dailyRecord.setStatus(status);

        // 计算迟到分钟数
        int lateMinutes = 0;
        if (plan.firstPeriod() && actualIn != null && actualIn.isAfter(plan.scheduledIn())) {
            lateMinutes = (int) ChronoUnit.MINUTES.between(plan.scheduledIn(), actualIn);
        }
        dailyRecord.setLateMinutes(lateMinutes);

        // 计算早退分钟数
        int earlyMinutes = 0;
        if (plan.lastPeriod() && actualOut != null && actualOut.isBefore(plan.scheduledOut())) {
            earlyMinutes = (int) ChronoUnit.MINUTES.between(actualOut, plan.scheduledOut());
        }
        dailyRecord.setEarlyMinutes(earlyMinutes);

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
    private int calculatePeriodStatus(ShiftPeriodPlan plan, LocalDateTime actualIn, LocalDateTime actualOut,
            BigDecimal workHours, Integer approvedAbsenceStatus, int lateTolerance, int earlyTolerance) {
        // 0-未处理 1-正常 2-迟到 3-早退 4-旷工 5-请假 6-出差 7-迟到+早退
        boolean hasWorkHours = workHours != null && workHours.compareTo(BigDecimal.ZERO) > 0;
        boolean missingIn = plan.firstPeriod() && plan.needClockIn() && actualIn == null;
        boolean missingOut = plan.lastPeriod() && plan.needClockOut() && actualOut == null;

        if ((missingIn || missingOut) && approvedAbsenceStatus != null) {
            return approvedAbsenceStatus;
        }
        if (actualIn == null && actualOut == null && !hasWorkHours) {
            return approvedAbsenceStatus != null ? approvedAbsenceStatus : 4; // 旷工
        }
        if (missingIn || missingOut) {
            return 0; // 未处理（缺少打卡）
        }

        boolean isLate = plan.firstPeriod() && plan.needClockIn() && actualIn != null
                && actualIn.isAfter(plan.scheduledIn().plusMinutes(lateTolerance));
        boolean isEarly = plan.lastPeriod() && plan.needClockOut() && actualOut != null
                && actualOut.isBefore(plan.scheduledOut().minusMinutes(earlyTolerance));

        if (isLate && isEarly) {
            return 7; // 迟到+早退
        } else if (isLate) {
            return 2; // 迟到
        } else if (isEarly) {
            return 3; // 早退
        }

        return 1; // 正常
    }

    private List<ShiftPeriodPlan> buildPeriodPlans(LocalDate date, AttShift shift, List<AttShiftPeriod> periods) {
        List<ShiftPeriodPlan> plans = new ArrayList<>();
        if (periods == null || periods.isEmpty()) {
            if (shift.getWorkStartTime() == null || shift.getWorkEndTime() == null) {
                return plans;
            }
            plans.add(createPeriodPlan(date, null, "全天", shift.getWorkStartTime(), shift.getWorkEndTime(),
                    shift.getIsNextDay() != null && shift.getIsNextDay() == 1, true, true, true, true));
            return plans;
        }

        LocalDateTime previousOut = null;
        for (int i = 0; i < periods.size(); i++) {
            AttShiftPeriod period = periods.get(i);
            if (period.getStartTime() == null || period.getEndTime() == null) {
                continue;
            }
            LocalTime startTime = LocalTime.parse(period.getStartTime());
            LocalTime endTime = LocalTime.parse(period.getEndTime());
            boolean crossDay = period.getCrossDay() != null && period.getCrossDay() == 1;
            boolean needClockIn = period.getNeedClockIn() == null || period.getNeedClockIn() == 1;
            boolean needClockOut = period.getNeedClockOut() == null || period.getNeedClockOut() == 1;

            LocalDateTime scheduledIn = date.atTime(startTime);
            while (previousOut != null && scheduledIn.isBefore(previousOut)) {
                scheduledIn = scheduledIn.plusDays(1);
            }
            LocalDateTime scheduledOut = scheduledIn.toLocalDate().atTime(endTime);
            if (crossDay || !scheduledOut.isAfter(scheduledIn)) {
                scheduledOut = scheduledOut.plusDays(1);
            }
            plans.add(new ShiftPeriodPlan(period.getId(), period.getPeriodName(), startTime, endTime, scheduledIn,
                    scheduledOut, needClockIn, needClockOut, i == 0, i == periods.size() - 1));
            previousOut = scheduledOut;
        }
        return plans;
    }

    private ShiftPeriodPlan createPeriodPlan(LocalDate date, Long periodId, String periodName, LocalTime startTime,
            LocalTime endTime, boolean configuredCrossDay, boolean needClockIn, boolean needClockOut,
            boolean firstPeriod, boolean lastPeriod) {
        LocalDateTime scheduledIn = date.atTime(startTime);
        LocalDateTime scheduledOut = date.atTime(endTime);
        if (configuredCrossDay || !scheduledOut.isAfter(scheduledIn)) {
            scheduledOut = scheduledOut.plusDays(1);
        }
        return new ShiftPeriodPlan(periodId, periodName, startTime, endTime, scheduledIn, scheduledOut, needClockIn,
                needClockOut, firstPeriod, lastPeriod);
    }

    private LocalDateTime findClosestClock(List<AttClockRecord> clockRecords, int clockType,
            LocalDateTime scheduledTime) {
        LocalDateTime matchStart = scheduledTime.minusHours(1);
        LocalDateTime matchEnd = scheduledTime.plusHours(1);
        LocalDateTime closest = null;

        for (AttClockRecord record : clockRecords) {
            if (record.getClockType() == null || record.getClockType() != clockType || record.getClockTime() == null) {
                continue;
            }
            LocalDateTime clockTime = record.getClockTime();
            if (clockTime.isBefore(matchStart) || clockTime.isAfter(matchEnd)) {
                continue;
            }
            if (closest == null || Math.abs(ChronoUnit.MINUTES.between(clockTime, scheduledTime)) < Math
                    .abs(ChronoUnit.MINUTES.between(closest, scheduledTime))) {
                closest = clockTime;
            }
        }

        return closest;
    }

    private BigDecimal calculateWorkHours(LocalDateTime scheduledIn, LocalDateTime scheduledOut,
            LocalDateTime actualIn, LocalDateTime actualOut) {
        if (scheduledIn == null || scheduledOut == null || actualIn == null || actualOut == null) {
            return BigDecimal.ZERO;
        }
        LocalDateTime effectiveIn = actualIn.isBefore(scheduledIn) ? scheduledIn : actualIn;
        LocalDateTime effectiveOut = actualOut.isAfter(scheduledOut) ? scheduledOut : actualOut;
        long actualMinutes = ChronoUnit.MINUTES.between(effectiveIn, effectiveOut);
        if (actualMinutes <= 0) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(actualMinutes).divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);
    }

    private Integer resolveApprovedAbsenceStatus(Long employeeId, LocalDate date) {
        LocalDateTime dayStart = date.atStartOfDay();
        LocalDateTime dayEnd = date.atTime(LocalTime.MAX);
        List<HrApplication> applications = applicationMapper.selectList(
                new LambdaQueryWrapper<HrApplication>()
                        .eq(HrApplication::getEmployeeId, employeeId)
                        .eq(HrApplication::getStatus, 1)
                        .in(HrApplication::getAppType, "leave", "business")
                        .le(HrApplication::getStartTime, dayEnd)
                        .ge(HrApplication::getEndTime, dayStart));

        boolean hasBusiness = applications.stream().anyMatch(app -> "business".equals(app.getAppType()));
        if (hasBusiness) {
            return 6;
        }
        boolean hasLeave = applications.stream().anyMatch(app -> "leave".equals(app.getAppType()));
        return hasLeave ? 5 : null;
    }

    private List<Long> expandOrgIds(List<Long> orgIds) {
        if (orgIds == null || orgIds.isEmpty()) {
            return orgIds;
        }
        Set<Long> expanded = new LinkedHashSet<>();
        for (Long orgId : orgIds) {
            if (orgId == null) {
                continue;
            }
            List<Long> childIds = orgUnitMapper.selectOrgAndChildIds(orgId);
            if (childIds == null || childIds.isEmpty()) {
                expanded.add(orgId);
            } else {
                expanded.addAll(childIds);
            }
        }
        return new ArrayList<>(expanded);
    }

    private Long resolveCompanyId(Long unitId) {
        OrgUnit current = unitId == null ? null : orgUnitMapper.selectById(unitId);
        while (current != null) {
            if (current.getUnitType() != null && current.getUnitType() == OrgUnit.TYPE_COMPANY) {
                return current.getId();
            }
            if (current.getParentId() == null || current.getParentId() == 0) {
                break;
            }
            current = orgUnitMapper.selectById(current.getParentId());
        }
        return null;
    }

    private record ShiftPeriodPlan(Long periodId, String periodName, LocalTime scheduledInTime,
            LocalTime scheduledOutTime, LocalDateTime scheduledIn, LocalDateTime scheduledOut, boolean needClockIn,
            boolean needClockOut, boolean firstPeriod, boolean lastPeriod) {
    }

    /**
     * 判断是否是休息日
     */
    private boolean isRestDay(Long companyId, LocalDate date) {
        if (companyId == null) {
            return false;
        }

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
