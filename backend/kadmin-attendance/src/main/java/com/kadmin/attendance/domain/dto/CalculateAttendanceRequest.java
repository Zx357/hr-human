package com.kadmin.attendance.domain.dto;

import lombok.Data;

import java.util.List;

/**
 * 考勤计算请求参数
 */
@Data
public class CalculateAttendanceRequest {
    private String startDate;
    private String endDate;
    private List<Long> orgIds;
    private String employeeNo;
    private String employeeName;
    private List<Long> employeeIds;
}
