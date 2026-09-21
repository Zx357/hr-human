package com.kadmin.attendance.service;

import com.kadmin.common.event.AttendanceRecalcEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;

/**
 * 考勤重算监听：审批联动后重算受影响员工与日期范围的日考勤
 * AFTER_COMMIT：仅在实际事务提交后执行，确保重算能读到已审批的申请数据；
 * 失败仅记录日志，不影响已提交的审批事务
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AttendanceRecalcListener {

    private final AttendanceService attendanceService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onAttendanceRecalc(AttendanceRecalcEvent event) {
        if (event.getEmployeeId() == null || event.getStartDate() == null || event.getEndDate() == null) {
            return;
        }
        try {
            attendanceService.calculateDailyAttendance(event.getStartDate(), event.getEndDate(),
                    null, null, null, List.of(event.getEmployeeId()));
            log.info("审批联动重算考勤完成：employee={} {} ~ {}", event.getEmployeeId(),
                    event.getStartDate(), event.getEndDate());
        } catch (Exception e) {
            log.error("审批联动重算考勤失败：employee={} {} ~ {}", event.getEmployeeId(),
                    event.getStartDate(), event.getEndDate(), e);
        }
    }
}
