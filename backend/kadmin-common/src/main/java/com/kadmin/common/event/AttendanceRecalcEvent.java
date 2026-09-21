package com.kadmin.common.event;

import java.time.LocalDate;

/**
 * 考勤重算事件
 * 审批联动（补卡通过补写打卡、加班通过回写工时等）发布，由考勤模块监听并重算指定员工指定日期范围的日考勤
 */
public class AttendanceRecalcEvent {

    /**
     * 员工ID
     */
    private final Long employeeId;

    /**
     * 重算开始日期（含）
     */
    private final LocalDate startDate;

    /**
     * 重算结束日期（含）
     */
    private final LocalDate endDate;

    public AttendanceRecalcEvent(Long employeeId, LocalDate startDate, LocalDate endDate) {
        this.employeeId = employeeId;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public Long getEmployeeId() {
        return employeeId;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }
}
