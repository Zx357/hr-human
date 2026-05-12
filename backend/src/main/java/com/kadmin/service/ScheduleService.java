package com.kadmin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kadmin.entity.AttSchedule;
import com.kadmin.entity.HrEmployee;
import com.kadmin.mapper.AttScheduleMapper;
import com.kadmin.mapper.EmployeeMapper;
import com.kadmin.mapper.OrgUnitMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ScheduleService extends ServiceImpl<AttScheduleMapper, AttSchedule> {

    private final EmployeeMapper employeeMapper;
    private final OrgUnitMapper orgUnitMapper;

    /**
     * 获取排班列表
     */
    public List<AttSchedule> getScheduleList(List<Long> orgIds, String employeeName, LocalDate startDate,
            LocalDate endDate) {
        return baseMapper.selectScheduleList(orgIds, null, employeeName, startDate, endDate);
    }

    /**
     * 获取员工周排班数据（按员工分组）
     */
    public List<Map<String, Object>> getWeekSchedule(List<Long> orgIds, String employeeNo, String employeeName,
            LocalDate startDate, LocalDate endDate) {
        // 获取组织及子组织ID
        List<Long> allOrgIds = new ArrayList<>();
        if (orgIds != null && !orgIds.isEmpty()) {
            for (Long orgId : orgIds) {
                List<Long> childIds = orgUnitMapper.selectOrgAndChildIds(orgId);
                if (childIds != null) {
                    allOrgIds.addAll(childIds);
                }
            }
        }

        // 获取员工列表
        LambdaQueryWrapper<HrEmployee> empWrapper = new LambdaQueryWrapper<>();
        empWrapper.eq(HrEmployee::getStatus, 1);
        if (!allOrgIds.isEmpty())
            empWrapper.in(HrEmployee::getDeptId, allOrgIds);
        if (employeeNo != null && !employeeNo.isEmpty())
            empWrapper.like(HrEmployee::getEmployeeNo, employeeNo);
        if (employeeName != null && !employeeName.isEmpty())
            empWrapper.like(HrEmployee::getName, employeeName);
        empWrapper.orderByAsc(HrEmployee::getId);
        List<HrEmployee> employees = employeeMapper.selectList(empWrapper);

        // 获取排班数据，必须使用展开后的组织范围，否则按公司筛选时员工有了但排班为空
        List<AttSchedule> schedules = baseMapper.selectScheduleList(allOrgIds, employeeNo, employeeName, startDate,
                endDate);

        // 按员工ID分组排班
        Map<Long, Map<LocalDate, AttSchedule>> scheduleMap = new HashMap<>();
        for (AttSchedule s : schedules) {
            scheduleMap.computeIfAbsent(s.getEmployeeId(), k -> new HashMap<>()).put(s.getScheduleDate(), s);
        }

        // 组装结果
        List<Map<String, Object>> result = new ArrayList<>();
        for (HrEmployee emp : employees) {
            Map<String, Object> row = new HashMap<>();
            row.put("employeeId", emp.getId());
            row.put("employeeName", emp.getName());
            row.put("employeeNo", emp.getEmployeeNo());
            row.put("deptId", emp.getDeptId());

            Map<String, Object> schedule = new HashMap<>();
            Map<LocalDate, AttSchedule> empSchedule = scheduleMap.getOrDefault(emp.getId(), new HashMap<>());
            LocalDate date = startDate;
            while (!date.isAfter(endDate)) {
                AttSchedule s = empSchedule.get(date);
                if (s != null) {
                    Map<String, Object> dayInfo = new HashMap<>();
                    dayInfo.put("shiftId", s.getShiftId());
                    dayInfo.put("shiftName", s.getShiftName());
                    dayInfo.put("shiftCode", s.getShiftCode());
                    schedule.put(date.toString(), dayInfo);
                }
                date = date.plusDays(1);
            }
            row.put("schedule", schedule);
            result.add(row);
        }
        return result;
    }

    /**
     * 保存单个排班
     */
    @Transactional
    public boolean saveSchedule(Long employeeId, Long shiftId, LocalDate scheduleDate) {
        // 删除原有排班
        remove(new LambdaQueryWrapper<AttSchedule>()
                .eq(AttSchedule::getEmployeeId, employeeId)
                .eq(AttSchedule::getScheduleDate, scheduleDate));

        // 如果shiftId为空或0，表示清除排班
        if (shiftId == null || shiftId == 0)
            return true;

        AttSchedule schedule = new AttSchedule();
        schedule.setEmployeeId(employeeId);
        schedule.setShiftId(shiftId);
        schedule.setScheduleDate(scheduleDate);
        return save(schedule);
    }

    /**
     * 批量排班
     */
    @Transactional
    public boolean batchSchedule(List<Long> employeeIds, Long shiftId, LocalDate startDate, LocalDate endDate) {
        for (Long empId : employeeIds) {
            LocalDate date = startDate;
            while (!date.isAfter(endDate)) {
                saveSchedule(empId, shiftId, date);
                date = date.plusDays(1);
            }
        }
        return true;
    }
}
