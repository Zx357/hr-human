package com.kadmin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kadmin.entity.*;
import com.kadmin.mapper.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class ApplicationService extends ServiceImpl<HrApplicationMapper, HrApplication> {

    @Autowired
    private EmployeeMapper employeeMapper;
    
    @Autowired
    private SysUserMapper sysUserMapper;
    
    @Autowired
    private AttScheduleMapper scheduleMapper;
    
    @Autowired
    private AttShiftMapper shiftMapper;
    
    @Autowired
    private AttShiftPeriodMapper shiftPeriodMapper;

    /**
     * 计算加班小时数
     * 根据员工排班和班次时段信息计算加班工时
     * - 有排班：计算选择时间与班次时段的交集
     * - 无排班（休息日）：整段时间都算加班
     */
    public BigDecimal calculateOvertimeHours(Long employeeId, LocalDateTime startTime, LocalDateTime endTime) {
        if (employeeId == null || startTime == null || endTime == null) {
            return BigDecimal.ZERO;
        }
        
        BigDecimal totalHours = BigDecimal.ZERO;
        LocalDate startDate = startTime.toLocalDate();
        LocalDate endDate = endTime.toLocalDate();
        
        // 遍历每一天
        LocalDate currentDate = startDate;
        while (!currentDate.isAfter(endDate)) {
            // 获取当天的排班
            AttSchedule schedule = scheduleMapper.selectOne(
                new LambdaQueryWrapper<AttSchedule>()
                    .eq(AttSchedule::getEmployeeId, employeeId)
                    .eq(AttSchedule::getScheduleDate, currentDate)
            );
            
            // 计算当天的加班时间范围
            LocalTime dayStart, dayEnd;
            if (currentDate.equals(startDate)) {
                dayStart = startTime.toLocalTime();
            } else {
                dayStart = LocalTime.of(0, 0);
            }
            if (currentDate.equals(endDate)) {
                dayEnd = endTime.toLocalTime();
            } else {
                dayEnd = LocalTime.of(23, 59);
            }
            
            if (schedule != null && schedule.getShiftId() != null) {
                // 有排班：计算选择时间与班次时段的交集
                List<AttShiftPeriod> periods = shiftPeriodMapper.selectList(
                    new LambdaQueryWrapper<AttShiftPeriod>()
                        .eq(AttShiftPeriod::getShiftId, schedule.getShiftId())
                        .orderByAsc(AttShiftPeriod::getSortOrder)
                );
                
                if (periods != null && !periods.isEmpty()) {
                    for (AttShiftPeriod period : periods) {
                        LocalTime periodStart = LocalTime.parse(period.getStartTime());
                        LocalTime periodEnd = LocalTime.parse(period.getEndTime());
                        
                        // 计算加班时间与班次时段的交集
                        LocalTime overlapStart = dayStart.isAfter(periodStart) ? dayStart : periodStart;
                        LocalTime overlapEnd = dayEnd.isBefore(periodEnd) ? dayEnd : periodEnd;
                        
                        if (overlapStart.isBefore(overlapEnd)) {
                            long minutes = ChronoUnit.MINUTES.between(overlapStart, overlapEnd);
                            if (minutes > 0) {
                                BigDecimal hours = BigDecimal.valueOf(minutes).divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);
                                totalHours = totalHours.add(hours);
                            }
                        }
                    }
                } else {
                    // 有排班但没有时段配置，整段时间都算加班
                    long minutes = ChronoUnit.MINUTES.between(dayStart, dayEnd);
                    if (minutes > 0) {
                        BigDecimal hours = BigDecimal.valueOf(minutes).divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);
                        totalHours = totalHours.add(hours);
                    }
                }
            } else {
                // 没有排班（休息日），整段时间都算加班
                long minutes = ChronoUnit.MINUTES.between(dayStart, dayEnd);
                if (minutes > 0) {
                    BigDecimal hours = BigDecimal.valueOf(minutes).divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);
                    totalHours = totalHours.add(hours);
                }
            }
            
            currentDate = currentDate.plusDays(1);
        }
        
        return totalHours;
    }

    /**
     * 计算请假小时数
     * 根据员工排班和班次时段信息计算实际请假工时
     */
    public BigDecimal calculateLeaveHours(Long employeeId, LocalDateTime startTime, LocalDateTime endTime) {
        if (employeeId == null || startTime == null || endTime == null) {
            return BigDecimal.ZERO;
        }
        
        BigDecimal totalHours = BigDecimal.ZERO;
        LocalDate startDate = startTime.toLocalDate();
        LocalDate endDate = endTime.toLocalDate();
        
        // 遍历每一天
        LocalDate currentDate = startDate;
        while (!currentDate.isAfter(endDate)) {
            // 获取当天的排班
            AttSchedule schedule = scheduleMapper.selectOne(
                new LambdaQueryWrapper<AttSchedule>()
                    .eq(AttSchedule::getEmployeeId, employeeId)
                    .eq(AttSchedule::getScheduleDate, currentDate)
            );
            
            if (schedule != null && schedule.getShiftId() != null) {
                // 获取班次时段列表
                List<AttShiftPeriod> periods = shiftPeriodMapper.selectList(
                    new LambdaQueryWrapper<AttShiftPeriod>()
                        .eq(AttShiftPeriod::getShiftId, schedule.getShiftId())
                        .orderByAsc(AttShiftPeriod::getSortOrder)
                );
                
                if (periods != null && !periods.isEmpty()) {
                    // 计算当天的请假时间范围
                    LocalTime dayStart, dayEnd;
                    
                    if (currentDate.equals(startDate)) {
                        dayStart = startTime.toLocalTime();
                    } else {
                        // 非第一天：从第一个时段开始
                        dayStart = LocalTime.parse(periods.get(0).getStartTime());
                    }
                    
                    if (currentDate.equals(endDate)) {
                        dayEnd = endTime.toLocalTime();
                    } else {
                        // 非最后一天：到最后一个时段结束
                        dayEnd = LocalTime.parse(periods.get(periods.size() - 1).getEndTime());
                    }
                    
                    // 遍历每个时段，计算与请假时间的交集
                    for (AttShiftPeriod period : periods) {
                        LocalTime periodStart = LocalTime.parse(period.getStartTime());
                        LocalTime periodEnd = LocalTime.parse(period.getEndTime());
                        
                        // 计算请假时间与时段的交集
                        LocalTime overlapStart = dayStart.isAfter(periodStart) ? dayStart : periodStart;
                        LocalTime overlapEnd = dayEnd.isBefore(periodEnd) ? dayEnd : periodEnd;
                        
                        if (overlapStart.isBefore(overlapEnd)) {
                            long minutes = ChronoUnit.MINUTES.between(overlapStart, overlapEnd);
                            if (minutes > 0) {
                                BigDecimal hours = BigDecimal.valueOf(minutes).divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);
                                totalHours = totalHours.add(hours);
                            }
                        }
                    }
                }
            }
            
            currentDate = currentDate.plusDays(1);
        }
        
        return totalHours;
    }

    public Page<HrApplication> getPage(int pageNum, int pageSize, String employeeName, String employeeNo, String appType, Integer status, Long employeeId) {
        return baseMapper.selectPageWithEmployee(new Page<>(pageNum, pageSize), employeeName, employeeNo, appType, status, employeeId);
    }
    
    public Page<HrApplication> getPendingPage(int pageNum, int pageSize, String employeeName, String employeeNo, String appType, Long userId) {
        // 获取当前用户的角色ID列表
        List<Long> roleIds = null;
        if (userId != null) {
            roleIds = sysUserMapper.selectRoleIdsByUserId(userId);
        }
        return baseMapper.selectPendingPage(new Page<>(pageNum, pageSize), employeeName, employeeNo, appType, roleIds);
    }

    @Transactional
    public boolean approve(Long id, Integer status, String remark, Long approveBy) {
        HrApplication application = getById(id);
        if (application == null) {
            return false;
        }
        
        // 更新申请状态
        HrApplication entity = new HrApplication();
        entity.setId(id);
        entity.setStatus(status);
        entity.setApproveRemark(remark);
        entity.setApproveBy(approveBy);
        entity.setApproveTime(LocalDateTime.now());
        boolean result = updateById(entity);
        
        // 审批通过时，执行相应的业务逻辑
        if (result && status == 1) {
            String appType = application.getAppType();
            
            if ("regularization".equals(appType)) {
                // 转正：更新员工转正日期和员工类别
                HrEmployee employee = new HrEmployee();
                employee.setId(application.getEmployeeId());
                employee.setRegularDate(application.getRegularDate());
                if (application.getNewEmployeeType() != null && !application.getNewEmployeeType().isEmpty()) {
                    employee.setEmployeeType(application.getNewEmployeeType());
                }
                employeeMapper.updateById(employee);
            } else if ("transfer".equals(appType)) {
                // 调动：更新员工部门、职位（公司通过dept_id向上查找获取）
                HrEmployee employee = new HrEmployee();
                employee.setId(application.getEmployeeId());
                if (application.getToDeptId() != null) {
                    employee.setDeptId(application.getToDeptId());
                }
                if (application.getToPosition() != null && !application.getToPosition().isEmpty()) {
                    employee.setPosition(application.getToPosition());
                }
                employeeMapper.updateById(employee);
            } else if ("resignation".equals(appType)) {
                // 离职：更新员工状态为离职
                HrEmployee employee = new HrEmployee();
                employee.setId(application.getEmployeeId());
                employee.setStatus(2); // 2-离职
                employee.setLeaveDate(application.getLastWorkDate());
                employeeMapper.updateById(employee);
            }
            // 奖励和惩罚暂不需要更新员工信息
        }
        
        return result;
    }
    
    public boolean cancel(Long id) {
        HrApplication entity = new HrApplication();
        entity.setId(id);
        entity.setStatus(3); // 已撤销
        return updateById(entity);
    }
}
