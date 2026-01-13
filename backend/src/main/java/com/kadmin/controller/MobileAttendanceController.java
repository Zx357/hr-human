package com.kadmin.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.kadmin.common.Result;
import com.kadmin.entity.AttClockRecord;
import com.kadmin.entity.HrEmployee;
import com.kadmin.mapper.AttClockRecordMapper;
import com.kadmin.mapper.EmployeeMapper;
import com.kadmin.security.LoginUser;
import com.kadmin.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 移动端考勤接口
 */
@RestController
@RequestMapping("/mobile/attendance")
@RequiredArgsConstructor
public class MobileAttendanceController {

    private final AttClockRecordMapper clockRecordMapper;
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
}
