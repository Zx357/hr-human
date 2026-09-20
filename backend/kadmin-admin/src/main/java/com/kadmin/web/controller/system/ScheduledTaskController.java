package com.kadmin.framework.task;

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
     * 每日凌晨2点半扫描未来30天内到期的合同/试用期/证书，写入站内通知
     * （按业务ID去重，同一事项只提醒一次）
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
                notificationService.notifyOnce(contract.getEmployeeId(), "reminder", "合同到期提醒",
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
                notificationService.notifyOnce(employee.getId(), "reminder", "试用期到期提醒",
                        "你的试用期将于 " + employee.getRegularDate() + " 到期，请关注转正安排",
                        employee.getId(), "/homePages/application");
                count++;
            }
            for (HrCertificate certificate : certificateMapper.selectList(new LambdaQueryWrapper<HrCertificate>()
                    .isNotNull(HrCertificate::getExpireDate)
                    .between(HrCertificate::getExpireDate, now, deadline))) {
                notificationService.notifyOnce(certificate.getEmployeeId(), "reminder", "证书到期提醒",
                        "你的证书「" + certificate.getCertName() + "」将于 " + certificate.getExpireDate()
                                + " 到期，请及时复审",
                        certificate.getId(), null);
                count++;
            }
            log.info("到期提醒扫描完成，共处理 {} 条", count);
        } catch (Exception e) {
            log.error("到期提醒扫描异常", e);
        }
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

        // 3. 证书到期（有到期日的证书在窗口内）
        List<Map<String, Object>> certificates = new ArrayList<>();
        for (HrCertificate certificate : certificateMapper.selectList(new LambdaQueryWrapper<HrCertificate>()
                .isNotNull(HrCertificate::getExpireDate)
                .between(HrCertificate::getExpireDate, now, deadline)
                .orderByAsc(HrCertificate::getExpireDate))) {
            HrEmployee employee = employeeMapper.selectById(certificate.getEmployeeId());
            Map<String, Object> item = new HashMap<>();
            item.put("type", "certificate");
            item.put("typeName", "证书到期");
            item.put("employeeId", certificate.getEmployeeId());
            item.put("employeeName", employee != null ? employee.getName() : null);
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
