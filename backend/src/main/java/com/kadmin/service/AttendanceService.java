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
    public IPage<AttClockRecord> getClockRecordPage(int page, int size, List<Long> orgIds, String employeeName, String startDate, String endDate) {
        return clockRecordMapper.selectPageWithEmployee(new Page<>(page, size), orgIds, employeeName, startDate, endDate);
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

    // 日考勤分页查询（按员工+日期聚合，包含各时段明细）
    public IPage<AttDailyRecord> getDailyRecordPage(int page, int size, String startDate, String endDate, List<Long> orgIds, String employeeNo, String employeeName, Integer status) {
        // 查询所有时段记录（不分页，因为需要聚合）
        List<AttDailyRecord> allRecords = dailyRecordMapper.selectListWithEmployee(startDate, endDate, orgIds, employeeNo, employeeName, status);
        
        // 按员工+日期聚合
        java.util.Map<String, AttDailyRecord> groupedMap = new java.util.LinkedHashMap<>();
        for (AttDailyRecord record : allRecords) {
            String key = record.getEmployeeId() + "_" + record.getAttDate();
            if (!groupedMap.containsKey(key)) {
                // 创建聚合记录
                AttDailyRecord grouped = new AttDailyRecord();
                grouped.setId(record.getId());
                grouped.setEmployeeId(record.getEmployeeId());
                grouped.setAttDate(record.getAttDate());
                grouped.setShiftId(record.getShiftId());
                grouped.setEmployeeName(record.getEmployeeName());
                grouped.setEmployeeNo(record.getEmployeeNo());
                grouped.setCompanyName(record.getCompanyName());
                grouped.setDeptName(record.getDeptName());
                grouped.setShiftName(record.getShiftName());
                grouped.setPeriods(new java.util.ArrayList<>());
                grouped.setLateMinutes(0);
                grouped.setEarlyMinutes(0);
                grouped.setWorkHours(java.math.BigDecimal.ZERO);
                grouped.setStatus(1); // 默认正常
                grouped.setLocked(0); // 默认未锁定
                groupedMap.put(key, grouped);
            }
            
            AttDailyRecord grouped = groupedMap.get(key);
            grouped.getPeriods().add(record);
            
            // 累计迟到、早退、工时
            grouped.setLateMinutes(grouped.getLateMinutes() + (record.getLateMinutes() != null ? record.getLateMinutes() : 0));
            grouped.setEarlyMinutes(grouped.getEarlyMinutes() + (record.getEarlyMinutes() != null ? record.getEarlyMinutes() : 0));
            grouped.setWorkHours(grouped.getWorkHours().add(record.getWorkHours() != null ? record.getWorkHours() : java.math.BigDecimal.ZERO));
            
            // 累计申请相关时长（只取第一条记录的值，因为同一天同一员工的值相同）
            if (grouped.getOvertimeDuration() == null || grouped.getOvertimeDuration().compareTo(java.math.BigDecimal.ZERO) == 0) {
                grouped.setOvertimeDuration(record.getOvertimeDuration());
            }
            if (grouped.getBusinessDuration() == null || grouped.getBusinessDuration().compareTo(java.math.BigDecimal.ZERO) == 0) {
                grouped.setBusinessDuration(record.getBusinessDuration());
            }
            if (grouped.getAnnualLeaveDuration() == null || grouped.getAnnualLeaveDuration().compareTo(java.math.BigDecimal.ZERO) == 0) {
                grouped.setAnnualLeaveDuration(record.getAnnualLeaveDuration());
            }
            if (grouped.getPersonalLeaveDuration() == null || grouped.getPersonalLeaveDuration().compareTo(java.math.BigDecimal.ZERO) == 0) {
                grouped.setPersonalLeaveDuration(record.getPersonalLeaveDuration());
            }
            if (grouped.getSickLeaveDuration() == null || grouped.getSickLeaveDuration().compareTo(java.math.BigDecimal.ZERO) == 0) {
                grouped.setSickLeaveDuration(record.getSickLeaveDuration());
            }
            if (grouped.getMarriageLeaveDuration() == null || grouped.getMarriageLeaveDuration().compareTo(java.math.BigDecimal.ZERO) == 0) {
                grouped.setMarriageLeaveDuration(record.getMarriageLeaveDuration());
            }
            if (grouped.getMaternityLeaveDuration() == null || grouped.getMaternityLeaveDuration().compareTo(java.math.BigDecimal.ZERO) == 0) {
                grouped.setMaternityLeaveDuration(record.getMaternityLeaveDuration());
            }
            if (grouped.getPaternityLeaveDuration() == null || grouped.getPaternityLeaveDuration().compareTo(java.math.BigDecimal.ZERO) == 0) {
                grouped.setPaternityLeaveDuration(record.getPaternityLeaveDuration());
            }
            if (grouped.getBereavementLeaveDuration() == null || grouped.getBereavementLeaveDuration().compareTo(java.math.BigDecimal.ZERO) == 0) {
                grouped.setBereavementLeaveDuration(record.getBereavementLeaveDuration());
            }
            
            // 状态取最差的
            if (record.getStatus() != null && record.getStatus() > grouped.getStatus()) {
                grouped.setStatus(record.getStatus());
            }
            
            // 锁定状态：只要有一个时段锁定，整条记录就显示锁定
            if (record.getLocked() != null && record.getLocked() == 1) {
                grouped.setLocked(1);
            }
        }
        
        // 手动分页
        List<AttDailyRecord> groupedList = new java.util.ArrayList<>(groupedMap.values());
        int total = groupedList.size();
        int fromIndex = (page - 1) * size;
        int toIndex = Math.min(fromIndex + size, total);
        List<AttDailyRecord> pageRecords = fromIndex < total ? groupedList.subList(fromIndex, toIndex) : new java.util.ArrayList<>();
        
        // 构建返回结果
        Page<AttDailyRecord> resultPage = new Page<>(page, size);
        resultPage.setRecords(pageRecords);
        resultPage.setTotal(total);
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
        if (ids == null || ids.isEmpty()) return;
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
    public List<java.util.Map<String, Object>> getMonthlyAttendance(String month, List<Long> orgIds, String employeeNo, String employeeName) {
        // 计算月份的开始和结束日期
        String startDate = month + "-01";
        java.time.YearMonth ym = java.time.YearMonth.parse(month);
        String endDate = month + "-" + String.format("%02d", ym.lengthOfMonth());
        
        // 查询该月所有日考勤记录
        List<AttDailyRecord> allRecords = dailyRecordMapper.selectListWithEmployee(startDate, endDate, orgIds, employeeNo, employeeName, null);
        
        // 按员工汇总
        java.util.Map<Long, java.util.Map<String, Object>> summaryMap = new java.util.LinkedHashMap<>();
        
        for (AttDailyRecord record : allRecords) {
            Long empId = record.getEmployeeId();
            java.util.Map<String, Object> summary = summaryMap.get(empId);
            
            if (summary == null) {
                summary = new java.util.HashMap<>();
                summary.put("employeeId", empId);
                summary.put("employeeName", record.getEmployeeName());
                summary.put("employeeNo", record.getEmployeeNo());
                summary.put("companyName", record.getCompanyName());
                summary.put("deptName", record.getDeptName());
                summary.put("month", month);
                summary.put("workDays", 0);       // 应出勤（有排班的天数）
                summary.put("actualDays", 0);     // 实出勤（正常打卡的天数）
                summary.put("lateTimes", 0);      // 迟到次数
                summary.put("earlyTimes", 0);     // 早退次数
                summary.put("absentDays", 0);     // 旷工天数
                summary.put("leaveDays", 0);      // 请假天数
                summary.put("totalWorkHours", java.math.BigDecimal.ZERO);
                summary.put("totalLateMinutes", 0);
                summary.put("totalEarlyMinutes", 0);
                summary.put("dates", new java.util.HashSet<LocalDate>()); // 用于去重统计天数
                summaryMap.put(empId, summary);
            }
            
            @SuppressWarnings("unchecked")
            java.util.Set<LocalDate> dates = (java.util.Set<LocalDate>) summary.get("dates");
            LocalDate attDate = record.getAttDate();
            
            // 每天只统计一次（多时段只算一天）
            if (!dates.contains(attDate)) {
                dates.add(attDate);
                summary.put("workDays", (int) summary.get("workDays") + 1);
            }
            
            // 统计各项数据
            Integer status = record.getStatus();
            if (status != null) {
                if (status == 1) { // 正常
                    // 实出勤按天统计，在下面处理
                } else if (status == 2 || status == 7) { // 迟到或迟到+早退
                    summary.put("lateTimes", (int) summary.get("lateTimes") + 1);
                }
                if (status == 3 || status == 7) { // 早退或迟到+早退
                    summary.put("earlyTimes", (int) summary.get("earlyTimes") + 1);
                }
                if (status == 4) { // 旷工
                    // 旷工按时段统计
                }
                if (status == 5) { // 请假
                    // 请假按时段统计
                }
            }
            
            // 累计工时
            if (record.getWorkHours() != null) {
                java.math.BigDecimal total = (java.math.BigDecimal) summary.get("totalWorkHours");
                summary.put("totalWorkHours", total.add(record.getWorkHours()));
            }
            
            // 累计迟到分钟
            if (record.getLateMinutes() != null && record.getLateMinutes() > 0) {
                summary.put("totalLateMinutes", (int) summary.get("totalLateMinutes") + record.getLateMinutes());
            }
            
            // 累计早退分钟
            if (record.getEarlyMinutes() != null && record.getEarlyMinutes() > 0) {
                summary.put("totalEarlyMinutes", (int) summary.get("totalEarlyMinutes") + record.getEarlyMinutes());
            }
        }
        
        // 计算实出勤天数和旷工天数
        for (java.util.Map<String, Object> summary : summaryMap.values()) {
            Long empId = (Long) summary.get("employeeId");
            @SuppressWarnings("unchecked")
            java.util.Set<LocalDate> dates = (java.util.Set<LocalDate>) summary.get("dates");
            
            int actualDays = 0;
            int absentDays = 0;
            int leaveDays = 0;
            
            for (LocalDate date : dates) {
                // 查询该员工该天的所有时段状态
                List<AttDailyRecord> dayRecords = allRecords.stream()
                    .filter(r -> r.getEmployeeId().equals(empId) && r.getAttDate().equals(date))
                    .toList();
                
                boolean hasAbsent = dayRecords.stream().anyMatch(r -> r.getStatus() != null && r.getStatus() == 4);
                boolean hasLeave = dayRecords.stream().anyMatch(r -> r.getStatus() != null && r.getStatus() == 5);
                boolean allNormal = dayRecords.stream().allMatch(r -> r.getStatus() != null && (r.getStatus() == 1 || r.getStatus() == 2 || r.getStatus() == 3 || r.getStatus() == 7));
                
                if (hasAbsent) {
                    absentDays++;
                } else if (hasLeave) {
                    leaveDays++;
                } else if (allNormal || dayRecords.stream().anyMatch(r -> r.getActualIn() != null && r.getActualOut() != null)) {
                    actualDays++;
                }
            }
            
            summary.put("actualDays", actualDays);
            summary.put("absentDays", absentDays);
            summary.put("leaveDays", leaveDays);
            summary.remove("dates"); // 移除临时字段
        }
        
        return new java.util.ArrayList<>(summaryMap.values());
    }

    /**
     * 计算指定日期范围的日考勤
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param orgIds 组织ID列表筛选
     * @param employeeNo 工号筛选
     * @param employeeName 姓名筛选
     * @param employeeIds 指定员工ID列表（优先级最高）
     */
    public void calculateDailyAttendance(LocalDate startDate, LocalDate endDate, List<Long> orgIds, String employeeNo, String employeeName, List<Long> employeeIds) {
        // 获取员工列表
        LambdaQueryWrapper<HrEmployee> wrapper = new LambdaQueryWrapper<HrEmployee>()
            .eq(HrEmployee::getDeleted, 0)
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
                .eq(AttSchedule::getScheduleDate, date)
        );

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
                .orderByAsc(AttShiftPeriod::getSortOrder)
        );

        // 5. 获取当天打卡记录
        List<AttClockRecord> clockRecords = clockRecordMapper.selectList(
            new LambdaQueryWrapper<AttClockRecord>()
                .eq(AttClockRecord::getEmployeeId, emp.getId())
                .apply("DATE(clock_time) = {0}", date)
                .orderByAsc(AttClockRecord::getClockTime)
        );

        // 6. 如果没有时段，使用班次的整体时间作为一个时段
        if (periods == null || periods.isEmpty()) {
            calculateSinglePeriod(emp, date, shift, null, shift.getWorkStartTime(), shift.getWorkEndTime(), "全天", clockRecords);
        } else {
            // 按每个时段计算
            for (AttShiftPeriod period : periods) {
                LocalTime startTime = LocalTime.parse(period.getStartTime());
                LocalTime endTime = LocalTime.parse(period.getEndTime());
                calculateSinglePeriod(emp, date, shift, period.getId(), startTime, endTime, period.getPeriodName(), clockRecords);
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
        
        // 时段时间范围（前后各扩展2小时用于匹配打卡）
        LocalTime matchStart = scheduledIn.minusHours(2);
        LocalTime matchEnd = scheduledOut.plusHours(2);
        
        for (AttClockRecord record : clockRecords) {
            LocalTime clockTime = record.getClockTime().toLocalTime();
            
            // 判断打卡时间是否在该时段范围内
            if (clockTime.isAfter(matchStart) && clockTime.isBefore(matchEnd)) {
                if (record.getClockType() == 1) { // 上班打卡
                    // 取最接近应打卡时间的上班打卡
                    if (actualIn == null || Math.abs(ChronoUnit.MINUTES.between(clockTime, scheduledIn)) < Math.abs(ChronoUnit.MINUTES.between(actualIn, scheduledIn))) {
                        actualIn = clockTime;
                    }
                } else { // 下班打卡
                    // 取最接近应打卡时间的下班打卡
                    if (actualOut == null || Math.abs(ChronoUnit.MINUTES.between(clockTime, scheduledOut)) < Math.abs(ChronoUnit.MINUTES.between(actualOut, scheduledOut))) {
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

        // 计算工作时长（按排班时段计算，正常打卡则算满时段工时）
        BigDecimal workHours = BigDecimal.ZERO;
        if (actualIn != null && actualOut != null && scheduledIn != null && scheduledOut != null) {
            // 按排班时段计算工时
            long scheduledMinutes = ChronoUnit.MINUTES.between(scheduledIn, scheduledOut);
            workHours = BigDecimal.valueOf(scheduledMinutes).divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);
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
    private int calculatePeriodStatus(LocalTime scheduledIn, LocalTime scheduledOut, LocalTime actualIn, LocalTime actualOut) {
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
                .ge(AttCalendarRule::getEndDate, date)
        );
        if (workDay != null) {
            return false; // 调休上班，不是休息日
        }

        // 2. 检查是否是法定假日
        AttCalendarRule holiday = calendarRuleMapper.selectOne(
            new LambdaQueryWrapper<AttCalendarRule>()
                .eq(AttCalendarRule::getCompanyId, companyId)
                .eq(AttCalendarRule::getRuleType, 4) // 法定假日
                .le(AttCalendarRule::getStartDate, date)
                .ge(AttCalendarRule::getEndDate, date)
        );
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
