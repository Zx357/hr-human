package com.kadmin.web.controller.mobile;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kadmin.common.Result;
import com.kadmin.common.security.LoginUser;
import com.kadmin.common.utils.SecurityUtils;
import com.kadmin.salary.domain.SalPayrollPayslip;
import com.kadmin.salary.service.PayrollService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 移动端工资条Controller
 *
 * 注意：纯移动端员工不经过按钮级权限校验（PermissionAspect 约定），
 * 员工只能看自己的工资条，本人归属校验在服务层强制完成。
 */
@RestController
@RequestMapping("/mobile/salary")
@RequiredArgsConstructor
public class MobileSalaryController {

    private final PayrollService payrollService;

    /** 我的工资条分页（仅已发放批次） */
    @GetMapping("/payslips")
    public Result<Page<SalPayrollPayslip>> myPayslips(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "12") Integer pageSize) {
        Long employeeId = currentEmployeeId();
        if (employeeId == null) {
            return Result.error("未绑定员工档案，无法查看工资条");
        }
        return Result.success(payrollService.pageMyPayslips(employeeId, pageNum, pageSize));
    }

    /** 工资条详情（本人+已发放），首次查看自动标记已读 */
    @GetMapping("/payslips/{id}")
    public Result<Map<String, Object>> myPayslipDetail(@PathVariable Long id) {
        Long employeeId = currentEmployeeId();
        if (employeeId == null) {
            return Result.error("未绑定员工档案，无法查看工资条");
        }
        return Result.success(payrollService.myPayslipDetail(employeeId, id));
    }

    /** 确认工资条 */
    @PostMapping("/payslips/{id}/confirm")
    public Result<Void> confirm(@PathVariable Long id) {
        Long employeeId = currentEmployeeId();
        if (employeeId == null) {
            return Result.error("未绑定员工档案，无法查看工资条");
        }
        payrollService.confirmMyPayslip(employeeId, id);
        return Result.success();
    }

    private Long currentEmployeeId() {
        LoginUser loginUser = SecurityUtils.getCurrentUser();
        return loginUser != null ? loginUser.getEmployeeId() : null;
    }
}
