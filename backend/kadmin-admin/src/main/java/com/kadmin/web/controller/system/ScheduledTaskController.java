package com.kadmin.web.controller.system;

import com.kadmin.attendance.service.AttendanceService;
import com.kadmin.common.Result;
import com.kadmin.hr.domain.HrCertificate;
import com.kadmin.hr.domain.HrContract;
import com.kadmin.hr.domain.HrEmployee;
import com.kadmin.hr.mapper.HrCertificateMapper;
import com.kadmin.hr.mapper.HrContractMapper;
import com.kadmin.hr.mapper.EmployeeMapper;
import com.kadmin.system.service.NotificationService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 定时任务 + 到期提醒
 * - 每日凌晨自动核算前一日考勤（手动触发入口保留）
 * - 合同到期 / 试用期到期 / 证书到期 提醒查询 API
 */
@Slf4j
@RestController
@RequestMapping("/reminder")
@RequiredArgsConstructor
public class ScheduledTaskController {

    private final AttendanceService attendanceService;
    private final HrContractMapper contractMapper;
    private final HrCertificateMapper certificateMapper;
    private final EmployeeMapper employeeMapper;
    private final NotificationService notificationService;
    private final org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

    /**
     * 补偿扫描窗口（天）与单次最大补算日期数：限制失败/宕机积压时的单次负载
     */
    private static final int COMPENSATE_LOOKBACK_DAYS = 7;
    private static final int COMPENSATE_MAX_DATES_PER_RUN = 10;

    /**
     * 每日凌晨2点自动核算前一日的日考勤
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void autoCalculateDailyAttendance() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        try {
            log.info("定时任务开始：自动核算 {} 日考勤", yesterday);
            attendanceService.calculateDailyAttendance(yesterday, yesterday, null, null, null, null);
            log.info("定时任务完成：{} 日考勤核算成功", yesterday);
        } catch (Exception e) {
            log.error("定时任务失败：{} 日考勤核算异常", yesterday, e);
        }
    }

    /**
     * 每日凌晨2点40分补偿扫描：找出最近N天"有排班但没有任何日考勤记录"的日期并补算
     * 覆盖两类缺口：每日核算任务失败（核算整体事务回滚，无残留记录）、服务宕机错过任务；
     * calculateDailyAttendance 幂等（已锁定的记录跳过、已有记录复用更新），重复补算无副作用
     */
    @Scheduled(cron = "0 40 2 * * ?")
    public void compensateMissingAttendance() {
        LocalDate startDate = LocalDate.now().minusDays(COMPENSATE_LOOKBACK_DAYS);
        LocalDate endDate = LocalDate.now().minusDays(1);
        try {
            List<LocalDate> missingDates = jdbcTemplate.query(
                    "SELECT DISTINCT s.schedule_date FROM att_schedule s " +
                            "WHERE s.schedule_date BETWEEN ? AND ? AND s.shift_id IS NOT NULL " +
                            "AND NOT EXISTS (SELECT 1 FROM att_daily_record r " +
                            "WHERE r.employee_id = s.employee_id AND r.att_date = s.schedule_date) " +
                            "ORDER BY s.schedule_date",
                    (rs, rowNum) -> rs.getObject("schedule_date", LocalDate.class),
                    java.sql.Date.valueOf(startDate), java.sql.Date.valueOf(endDate));
            if (missingDates.isEmpty()) {
                return;
            }
            log.warn("发现缺失日考勤的日期 {}（最近{}天），开始补算", missingDates, COMPENSATE_LOOKBACK_DAYS);
            int limit = Math.min(missingDates.size(), COMPENSATE_MAX_DATES_PER_RUN);
            for (int i = 0; i < limit; i++) {
                LocalDate date = missingDates.get(i);
                try {
                    attendanceService.calculateDailyAttendance(date, date, null, null, null, null);
                    log.info("补算 {} 日考勤完成", date);
                } catch (Exception e) {
                    log.error("补算 {} 日考勤失败，等待下轮扫描重试", date, e);
                }
            }
            if (missingDates.size() > limit) {
                log.warn("本轮补算 {} 个日期，剩余 {} 个日期积压待下轮处理",
                        limit, missingDates.size() - limit);
            }
        } catch (Exception e) {
            log.error("考勤补偿扫描异常", e);
        }
    }

    /**
     * 每日凌晨2点半扫描未来30天内到期的合同/试用期/证书，写入站内通知
     * （通知类型区分业务线（contract/probation/cert），与业务ID共同构成去重键，同一事项只提醒一次）
     */
    @Scheduled(cron = "0 30 2 * * ?")
    public void scanExpiryReminders() {
        LocalDate now = LocalDate.now();
        LocalDate deadline = now.plusDays(30);
        int count = 0;
        try {
            for (HrContract contract : contractMapper.selectList(new LambdaQueryWrapper<HrContract>()
                    .eq(HrContract::getStatus, 1)
                    .isNotNull(HrContract::getEndDate)
                    .between(HrContract::getEndDate, now, deadline))) {
                notificationService.notifyOnce(contract.getEmployeeId(), "contract", "合同到期提醒",
                        "你的合同 " + contract.getContractNo() + " 将于 " + contract.getEndDate()
                                + " 到期，请联系人事办理续签",
                        contract.getId(), null);
                count++;
            }
            for (HrEmployee employee : employeeMapper.selectList(new LambdaQueryWrapper<HrEmployee>()
                    .eq(HrEmployee::getStatus, 1)
                    .eq(HrEmployee::getEmployeeType, "probation")
                    .isNotNull(HrEmployee::getRegularDate)
                    .between(HrEmployee::getRegularDate, now, deadline))) {
                notificationService.notifyOnce(employee.getId(), "probation", "试用期到期提醒",
                        "你的试用期将于 " + employee.getRegularDate() + " 到期，请关注转正安排",
                        employee.getId(), "/homePages/application");
                count++;
            }
            for (HrCertificate certificate : certificateMapper.selectList(new LambdaQueryWrapper<HrCertificate>()
                    .isNotNull(HrCertificate::getExpireDate)
                    .between(HrCertificate::getExpireDate, now, deadline))) {
                notificationService.notifyOnce(certificate.getEmployeeId(), "cert", "证书到期提醒",
                        "你的证书「" + certificate.getCertName() + "」将于 " + certificate.getExpireDate()
                                + " 到期，请及时复审",
                        certificate.getId(), null);
                count++;
            }
            // 生日提醒：未来 7 天内过生日的在职员工（去重键含年份，每年提醒一次）
            for (HrEmployee employee : employeeMapper.selectList(new LambdaQueryWrapper<HrEmployee>()
                    .eq(HrEmployee::getStatus, 1)
                    .isNotNull(HrEmployee::getBirthDate))) {
                LocalDate birthday = nextAnnualDate(employee.getBirthDate(), now);
                if (birthday != null && !birthday.isAfter(now.plusDays(7))) {
                    notificationService.notifyOnce(employee.getId(), "birthday", "生日提醒",
                            "你的生日 " + birthday + " 将至，提前祝你生日快乐！",
                            (long) birthday.getYear(), null);
                    count++;
                }
            }
            // 入职周年提醒：未来 7 天内到周年日的在职员工
            for (HrEmployee employee : employeeMapper.selectList(new LambdaQueryWrapper<HrEmployee>()
                    .eq(HrEmployee::getStatus, 1)
                    .isNotNull(HrEmployee::getEntryDate))) {
                LocalDate anniversary = nextAnnualDate(employee.getEntryDate(), now);
                if (anniversary != null && !anniversary.isAfter(now.plusDays(7))) {
                    int years = anniversary.getYear() - employee.getEntryDate().getYear();
                    notificationService.notifyOnce(employee.getId(), "anniversary", "入职周年提醒",
                            "你将于 " + anniversary + " 迎来入职 " + years + " 周年，感谢你的付出！",
                            (long) anniversary.getYear(), null);
                    count++;
                }
            }
            log.info("到期提醒扫描完成，共处理 {} 条", count);
        } catch (Exception e) {
            log.error("到期提醒扫描异常", e);
        }
    }

    /**
     * 计算年度日期（生日/周年）的下一次发生日期
     * 2月29日在平年顺延为2月28日（LocalDate.withYear 行为）
     */
    private LocalDate nextAnnualDate(LocalDate annualDate, LocalDate now) {
        if (annualDate == null) {
            return null;
        }
        LocalDate thisYear = annualDate.withYear(now.getYear());
        return thisYear.isAfter(now) ? thisYear : annualDate.withYear(now.getYear() + 1);
    }

    /**
     * 到期提醒汇总（合同/试用期/证书）
     *
     * @param days 未来多少天内到期视为"即将到期"，默认30天
     */
    @GetMapping("/list")
    public Result<Map<String, Object>> reminders(
            @RequestParam(defaultValue = "30") Integer days) {
        LocalDate now = LocalDate.now();
        LocalDate deadline = now.plusDays(Math.max(days, 1));

        // 1. 合同到期（履行中，结束日期在窗口内）
        List<Map<String, Object>> contracts = new ArrayList<>();
        for (HrContract contract : contractMapper.selectList(new LambdaQueryWrapper<HrContract>()
                .eq(HrContract::getStatus, 1)
                .isNotNull(HrContract::getEndDate)
                .between(HrContract::getEndDate, now, deadline)
                .orderByAsc(HrContract::getEndDate))) {
            Map<String, Object> item = new HashMap<>();
            item.put("type", "contract");
            item.put("typeName", "合同到期");
            item.put("employeeId", contract.getEmployeeId());
            item.put("employeeName", contract.getEmployeeName());
            item.put("date", contract.getEndDate());
            item.put("remainDays", contract.getEndDate().toEpochDay() - now.toEpochDay());
            item.put("detail", "合同 " + contract.getContractNo());
            contracts.add(item);
        }

        // 2. 试用期到期（在职试用期员工，转正日期在窗口内）
        List<Map<String, Object>> probations = new ArrayList<>();
        for (HrEmployee employee : employeeMapper.selectList(new LambdaQueryWrapper<HrEmployee>()
                .eq(HrEmployee::getStatus, 1)
                .eq(HrEmployee::getEmployeeType, "probation")
                .isNotNull(HrEmployee::getRegularDate)
                .between(HrEmployee::getRegularDate, now, deadline)
                .orderByAsc(HrEmployee::getRegularDate))) {
            Map<String, Object> item = new HashMap<>();
            item.put("type", "probation");
            item.put("typeName", "试用期到期");
            item.put("employeeId", employee.getId());
            item.put("employeeName", employee.getName());
            item.put("date", employee.getRegularDate());
            item.put("remainDays", employee.getRegularDate().toEpochDay() - now.toEpochDay());
            item.put("detail", "应转正日期");
            probations.add(item);
        }

        // 3. 证书到期（有到期日的证书在窗口内），批量查询员工姓名避免 N+1
        List<HrCertificate> expiringCertificates = certificateMapper.selectList(new LambdaQueryWrapper<HrCertificate>()
                .isNotNull(HrCertificate::getExpireDate)
                .between(HrCertificate::getExpireDate, now, deadline)
                .orderByAsc(HrCertificate::getExpireDate));
        List<Long> certEmployeeIds = expiringCertificates.stream()
                .map(HrCertificate::getEmployeeId)
                .filter(java.util.Objects::nonNull)
                .distinct()
                .toList();
        Map<Long, String> employeeNames = certEmployeeIds.isEmpty() ? Map.of()
                : employeeMapper.selectBatchIds(certEmployeeIds).stream()
                        .collect(java.util.stream.Collectors.toMap(HrEmployee::getId, HrEmployee::getName,
                                (a, b) -> a));
        List<Map<String, Object>> certificates = new ArrayList<>();
        for (HrCertificate certificate : expiringCertificates) {
            Map<String, Object> item = new HashMap<>();
            item.put("type", "certificate");
            item.put("typeName", "证书到期");
            item.put("employeeId", certificate.getEmployeeId());
            item.put("employeeName", employeeNames.get(certificate.getEmployeeId()));
            item.put("date", certificate.getExpireDate());
            item.put("remainDays", certificate.getExpireDate().toEpochDay() - now.toEpochDay());
            item.put("detail", certificate.getCertName());
            certificates.add(item);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("contracts", contracts);
        result.put("probations", probations);
        result.put("certificates", certificates);
        result.put("total", contracts.size() + probations.size() + certificates.size());
        return Result.success(result);
    }
}
