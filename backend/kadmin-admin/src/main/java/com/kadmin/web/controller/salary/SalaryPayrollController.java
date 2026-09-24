package com.kadmin.web.controller.salary;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kadmin.common.Result;
import com.kadmin.common.annotation.OperLog;
import com.kadmin.common.annotation.RequiresPermission;
import com.kadmin.salary.domain.SalPayrollBatch;
import com.kadmin.salary.domain.SalPayrollPayslip;
import com.kadmin.salary.service.PayrollService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 工资核算Controller：批次状态机 0核算中→1已核算→2已确认→3已发放
 */
@RestController
@RequestMapping("/salary/payroll")
@RequiredArgsConstructor
public class SalaryPayrollController {

    private final PayrollService payrollService;

    @GetMapping("/batch-page")
    public Result<Page<SalPayrollBatch>> batchPage(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String yearMonth,
            @RequestParam(required = false) Long companyId) {
        return Result.success(payrollService.pageBatches(pageNum, pageSize, yearMonth, companyId));
    }

    /** 创建批次并首次核算，body = {yearMonth, companyId, remark} */
    @RequiresPermission("sal:payroll:manage")
    @OperLog(module = "工资核算", action = "创建批次")
    @PostMapping("/create")
    public Result<SalPayrollBatch> create(@RequestBody Map<String, Object> body) {
        Object yearMonth = body.get("yearMonth");
        Object companyId = body.get("companyId");
        if (yearMonth == null || String.valueOf(yearMonth).isBlank()) {
            return Result.error("请选择核算月份");
        }
        if (!(companyId instanceof Number)) {
            return Result.error("请选择核算公司");
        }
        return Result.success(payrollService.createBatch(String.valueOf(yearMonth),
                ((Number) companyId).longValue(), (String) body.get("remark")));
    }

    /** 删除批次（仅未确认状态），级联清掉工资条 */
    @RequiresPermission("sal:payroll:manage")
    @OperLog(module = "工资核算", action = "删除批次")
    @org.springframework.web.bind.annotation.DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        payrollService.deleteBatch(id);
        return Result.success();
    }

    /** 重算（仅未确认） */
    @RequiresPermission("sal:payroll:manage")
    @OperLog(module = "工资核算", action = "重算批次")
    @PostMapping("/{id}/compute")
    public Result<SalPayrollBatch> compute(@PathVariable Long id) {
        return Result.success(payrollService.computeBatch(id));
    }

    @RequiresPermission("sal:payroll:manage")
    @OperLog(module = "工资核算", action = "确认批次")
    @PostMapping("/{id}/confirm")
    public Result<Void> confirm(@PathVariable Long id) {
        payrollService.confirmBatch(id);
        return Result.success();
    }

    /** 发放（员工端可见+站内通知） */
    @RequiresPermission("sal:payroll:manage")
    @OperLog(module = "工资核算", action = "发放工资")
    @PostMapping("/{id}/publish")
    public Result<Void> publish(@PathVariable Long id) {
        payrollService.publishBatch(id);
        return Result.success();
    }

    /** 解锁（已确认→已核算），高危操作留审计 */
    @RequiresPermission("sal:payroll:manage")
    @OperLog(module = "工资核算", action = "解锁批次")
    @PostMapping("/{id}/unlock")
    public Result<Void> unlock(@PathVariable Long id) {
        payrollService.unlockBatch(id);
        return Result.success();
    }

    @GetMapping("/{id}/payslip-page")
    public Result<Page<SalPayrollPayslip>> payslipPage(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String employeeName,
            @RequestParam(required = false) String employeeNo) {
        return Result.success(payrollService.pagePayslips(id, pageNum, pageSize, employeeName, employeeNo));
    }

    /** 工资条详情（含明细行） */
    @GetMapping("/payslip/{payslipId}")
    public Result<Map<String, Object>> payslipDetail(@PathVariable Long payslipId) {
        return Result.success(payrollService.payslipDetail(payslipId));
    }

    /** 修改手工项金额（仅未确认批次），body = {amount} */
    @RequiresPermission("sal:payroll:manage")
    @OperLog(module = "工资核算", action = "修改手工项")
    @PostMapping("/item/{payrollItemId}/amount")
    public Result<Void> updateManualItem(@PathVariable Long payrollItemId,
            @RequestBody Map<String, Object> body) {
        Object amount = body.get("amount");
        if (!(amount instanceof Number)) {
            return Result.error("金额不合法");
        }
        payrollService.updateManualItem(payrollItemId, BigDecimal.valueOf(((Number) amount).doubleValue()));
        return Result.success();
    }

    /** 导出工资表 Excel（动态列：工号/姓名 + 各薪资项 + 应发/扣款/实发） */
    @RequiresPermission("sal:payroll:export")
    @OperLog(module = "工资核算", action = "导出工资表")
    @GetMapping("/{id}/export")
    public void export(@PathVariable Long id, jakarta.servlet.http.HttpServletResponse response) {
        Map<String, Object> data = payrollService.buildExportData(id);
        @SuppressWarnings("unchecked")
        java.util.List<String> headers = (java.util.List<String>) data.get("headers");
        @SuppressWarnings("unchecked")
        java.util.List<java.util.List<Object>> rows = (java.util.List<java.util.List<Object>>) data.get("rows");
        com.kadmin.web.controller.system.ExcelExportUtil.write(response, String.valueOf(data.get("fileName")),
                "工资表", headers, rows);
    }
}
