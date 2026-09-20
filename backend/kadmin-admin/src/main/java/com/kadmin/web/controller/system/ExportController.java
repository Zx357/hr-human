package com.kadmin.web.controller.system;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kadmin.common.annotation.RequiresPermission;
import com.kadmin.attendance.domain.AttDailyRecord;
import com.kadmin.attendance.service.AttendanceService;
import com.kadmin.common.Result;
import com.kadmin.hr.domain.HrContract;
import com.kadmin.hr.domain.HrEmployee;
import com.kadmin.hr.service.ContractService;
import com.kadmin.hr.service.EmployeeService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 数据导出（Excel）
 * 员工花名册 / 日考勤 / 月考勤 / 合同台账
 */
@RestController
@RequiredArgsConstructor
public class ExportController {

    private final EmployeeService employeeService;
    private final ContractService contractService;
    private final AttendanceService attendanceService;

    /**
     * 员工花名册导出
     */
    @RequiresPermission("hr:employee:export")
    @GetMapping("/employee/export")
    public void exportEmployees(
            HttpServletResponse response,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String employeeNo,
            @RequestParam(required = false) Long deptId,
            @RequestParam(required = false) String orgIds,
            @RequestParam(required = false) Integer status) {
        List<List<Object>> rows = new ArrayList<>();
        int page = 1;
        Page<HrEmployee> result;
        do {
            result = employeeService.getEmployeePage(page, 500, name, employeeNo, deptId, orgIds, status);
            result.getRecords().forEach(e -> rows.add(employeeRow(e)));
            page++;
        } while (result.getRecords().size() == 500);

        List<String> headers = List.of("工号", "姓名", "性别", "手机号", "邮箱", "职位", "员工类别",
                "部门", "公司", "入职日期", "状态");
        ExcelExportUtil.write(response, "员工花名册.xlsx", "员工", headers, rows);
    }

    private List<Object> employeeRow(HrEmployee e) {
        return List.of(
                e.getEmployeeNo(),
                e.getName(),
                e.getGender(),
                e.getPhone(),
                e.getEmail(),
                e.getPosition(),
                e.getEmployeeType(),
                e.getDeptName(),
                e.getCompanyName(),
                e.getEntryDate(),
                statusText(e.getStatus()));
    }

    /**
     * 日考勤导出
     */
    @RequiresPermission("attendance:daily:export")
    @GetMapping("/attendance/daily/export")
    public void exportDailyAttendance(
            HttpServletResponse response,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String attDate,
            @RequestParam(required = false) List<Long> orgIds,
            @RequestParam(required = false) String employeeNo,
            @RequestParam(required = false) String employeeName,
            @RequestParam(required = false) Integer status) {
        // 前端可能只传单日 attDate
        if (!org.springframework.util.StringUtils.hasText(startDate) && org.springframework.util.StringUtils.hasText(attDate)) {
            startDate = attDate;
            endDate = attDate;
        }
        List<List<Object>> rows = new ArrayList<>();
        int page = 1;
        com.baomidou.mybatisplus.core.metadata.IPage<AttDailyRecord> result;
        do {
            result = attendanceService.getDailyRecordPage(page, 500, startDate, endDate, orgIds, employeeNo,
                    employeeName, status);
            for (AttDailyRecord r : result.getRecords()) {
                rows.add(List.of(
                        r.getEmployeeNo(),
                        r.getEmployeeName(),
                        r.getCompanyName(),
                        r.getDeptName(),
                        r.getAttDate(),
                        r.getPeriodName(),
                        r.getScheduledIn(),
                        r.getScheduledOut(),
                        r.getActualIn(),
                        r.getActualOut(),
                        attendanceStatusText(r.getStatus()),
                        r.getLateMinutes(),
                        r.getEarlyMinutes(),
                        r.getWorkHours(),
                        r.getOvertimeHours(),
                        r.getRemark()));
            }
            page++;
        } while (result.getRecords().size() == 500);

        List<String> headers = List.of("工号", "姓名", "公司", "部门", "考勤日期", "时段",
                "应上班时间", "应下班时间", "实际上班", "实际下班", "状态", "迟到(分钟)", "早退(分钟)",
                "工时", "加班工时", "备注");
        ExcelExportUtil.write(response, "日考勤明细.xlsx", "日考勤", headers, rows);
    }

    /**
     * 月考勤汇总导出
     */
    @RequiresPermission("attendance:monthly:export")
    @GetMapping("/attendance/monthly/export")
    public void exportMonthlyAttendance(
            HttpServletResponse response,
            @RequestParam String month,
            @RequestParam(required = false) List<Long> orgIds,
            @RequestParam(required = false) String employeeNo,
            @RequestParam(required = false) String employeeName) {
        List<Map<String, Object>> data = attendanceService.getMonthlyAttendance(month, orgIds, employeeNo,
                employeeName);
        List<List<Object>> rows = new ArrayList<>();
        for (Map<String, Object> item : data) {
            rows.add(List.of(
                    item.get("employeeNo"),
                    item.get("employeeName"),
                    item.get("companyName"),
                    item.get("deptName"),
                    item.get("workDays"),
                    item.get("actualDays"),
                    item.get("lateTimes"),
                    item.get("earlyTimes"),
                    item.get("absentDays"),
                    item.get("leaveDays"),
                    item.get("totalWorkHours"),
                    item.get("totalOvertimeHours"),
                    item.get("totalLateMinutes"),
                    item.get("totalEarlyMinutes")));
        }
        List<String> headers = List.of("工号", "姓名", "公司", "部门", "应出勤天数", "实际出勤天数",
                "迟到次数", "早退次数", "旷工天数", "请假天数", "总工时", "总加班工时", "累计迟到(分钟)", "累计早退(分钟)");
        ExcelExportUtil.write(response, "月考勤汇总-" + month + ".xlsx", "月考勤", headers, rows);
    }

    /**
     * 合同台账导出（支持按当前筛选条件导出）
     */
    @RequiresPermission("hr:contract:export")
    @GetMapping("/hr/contract/export")
    public void exportContracts(
            HttpServletResponse response,
            @RequestParam(required = false) String contractNo,
            @RequestParam(required = false) String employeeName,
            @RequestParam(required = false) String employeeNo,
            @RequestParam(required = false) String contractType,
            @RequestParam(required = false) Integer status) {
        List<List<Object>> rows = new ArrayList<>();
        int page = 1;
        Page<HrContract> result;
        do {
            result = contractService.getContractPage(page, 500, contractNo, employeeName, employeeNo, contractType,
                    status, null);
            for (HrContract c : result.getRecords()) {
                rows.add(List.of(
                        c.getContractNo(),
                        c.getEmployeeName(),
                        c.getEmployeeNo(),
                        c.getCompanyName(),
                        c.getContractType(),
                        c.getStartDate(),
                        c.getEndDate(),
                        c.getSignDate(),
                        c.getProbationMonths(),
                        c.getSalary(),
                        contractStatusText(c.getStatus()),
                        c.getRemark()));
            }
            page++;
        } while (result.getRecords().size() == 500);

        List<String> headers = List.of("合同编号", "员工姓名", "工号", "公司", "合同类型",
                "开始日期", "结束日期", "签订日期", "试用期(月)", "薪资", "状态", "备注");
        ExcelExportUtil.write(response, "合同台账.xlsx", "合同", headers, rows);
    }

    private String statusText(Integer status) {
        if (status == null) {
            return "";
        }
        return switch (status) {
            case 1 -> "在职";
            case 2 -> "离职";
            case 3 -> "待入职";
            default -> "未知";
        };
    }

    private String attendanceStatusText(Integer status) {
        if (status == null) {
            return "";
        }
        return switch (status) {
            case 0 -> "未处理";
            case 1 -> "正常";
            case 2 -> "迟到";
            case 3 -> "早退";
            case 4 -> "旷工";
            case 5 -> "请假";
            case 6 -> "出差";
            case 7 -> "迟到+早退";
            default -> "未知";
        };
    }

    private String contractStatusText(Integer status) {
        if (status == null) {
            return "";
        }
        return switch (status) {
            case 1 -> "履行中";
            case 2 -> "已到期";
            case 3 -> "已解除";
            case 4 -> "已续签";
            default -> "未知";
        };
    }
}
