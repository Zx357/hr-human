package com.kadmin.web.controller.hr;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kadmin.common.Result;
import com.kadmin.common.annotation.RequiresPermission;
import com.kadmin.common.security.LoginUser;
import com.kadmin.common.utils.SecurityUtils;
import com.kadmin.hr.domain.HrLeaveQuota;
import com.kadmin.hr.service.LeaveQuotaService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 假期额度管理Controller
 */
@RestController
@RequestMapping("/hr/leave-quota")
@RequiredArgsConstructor
public class LeaveQuotaController {

    private final LeaveQuotaService leaveQuotaService;

    /**
     * 分页查询额度列表
     */
    @RequiresPermission("hr:leavequota:list")
    @GetMapping("/page")
    public Result<Page<HrLeaveQuota>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) String leaveType,
            @RequestParam(required = false) String employeeName,
            @RequestParam(required = false) String employeeNo) {
        return Result.success(leaveQuotaService.pageQuotas(pageNum, pageSize, year, leaveType,
                employeeName, employeeNo));
    }

    /**
     * 指定员工某年度的额度列表（申请请假时展示余额用）
     */
    @RequiresPermission("hr:leavequota:list")
    @GetMapping("/list")
    public Result<List<HrLeaveQuota>> list(
            @RequestParam Long employeeId,
            @RequestParam(required = false) Integer year) {
        return Result.success(leaveQuotaService.listByEmployee(employeeId, year));
    }

    /**
     * 当前登录用户某年度的额度（PC 个人视角）
     */
    @GetMapping("/my")
    public Result<List<HrLeaveQuota>> my(@RequestParam(required = false) Integer year) {
        LoginUser loginUser = SecurityUtils.getCurrentUser();
        if (loginUser == null || loginUser.getEmployeeId() == null) {
            return Result.success(List.of());
        }
        return Result.success(leaveQuotaService.listByEmployee(loginUser.getEmployeeId(), year));
    }

    /**
     * 新增/更新额度（按 员工+年度+类型 幂等）
     */
    @RequiresPermission("hr:leavequota:manage")
    @PostMapping("/save")
    public Result<HrLeaveQuota> save(@RequestBody HrLeaveQuota quota) {
        return Result.success(leaveQuotaService.saveQuota(quota));
    }

    /**
     * 删除额度
     */
    @RequiresPermission("hr:leavequota:manage")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        leaveQuotaService.deleteQuota(id);
        return Result.success();
    }
}
