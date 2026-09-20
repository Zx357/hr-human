package com.kadmin.web.controller.hr;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kadmin.common.annotation.RequiresPermission;
import com.kadmin.common.Result;
import com.kadmin.hr.domain.HrApplication;
import com.kadmin.common.security.LoginUser;
import com.kadmin.common.annotation.OperLog;
import com.kadmin.system.mapper.SysUserMapper;
import com.kadmin.system.service.ApplicationService;
import com.kadmin.common.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/hr/application")
@RequiredArgsConstructor
public class ApplicationController {
    /**
     * 允许提交的申请类型白名单
     */
    private static final Set<String> SUPPORTED_APP_TYPES = Set.of(
            "leave", "overtime", "business", "makeup", "exchange",
            "regularization", "transfer", "reward", "punish", "resignation");

    private final ApplicationService service;
    private final SysUserMapper sysUserMapper;

    @GetMapping("/page")
    public Result<Page<HrApplication>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String employeeName,
            @RequestParam(required = false) String employeeNo,
            @RequestParam(required = false) String appType,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Long employeeId,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate beginTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endTime) {
        // 非管理员强制只看自己的申请
        LoginUser loginUser = SecurityUtils.getCurrentUser();
        if (loginUser != null && !SecurityUtils.isAdmin()) {
            Long selfId = loginUser.getEmployeeId() != null ? loginUser.getEmployeeId() : loginUser.getUserId();
            employeeId = selfId;
        }
        return Result.success(
                service.getPage(pageNum, pageSize, employeeName, employeeNo, appType, status, employeeId, beginTime,
                        endTime));
    }

    @GetMapping("/pending")
    public Result<Page<HrApplication>> pending(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String employeeName,
            @RequestParam(required = false) String employeeNo,
            @RequestParam(required = false) String appType) {
        Long userId = SecurityUtils.getCurrentUserId();
        return Result.success(service.getPendingPage(pageNum, pageSize, employeeName, employeeNo, appType, userId));
    }

    @GetMapping("/mobile-pending")
    public Result<Page<HrApplication>> mobilePending(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String employeeName,
            @RequestParam(required = false) String employeeNo,
            @RequestParam(required = false) String appType) {
        LoginUser loginUser = SecurityUtils.getCurrentUser();
        Long employeeId = loginUser.getEmployeeId();
        if (employeeId == null) {
            employeeId = loginUser.getUserId();
        }
        return Result.success(
                service.getMobilePendingPage(pageNum, pageSize, employeeName, employeeNo, appType, employeeId));
    }

    /**
     * 计算请假小时数（仅本人或管理员可调用）
     */
    @GetMapping("/calculate-leave-hours")
    public Result<BigDecimal> calculateLeaveHours(
            @RequestParam Long employeeId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {
        String denied = checkNotSelfOrAdmin(employeeId);
        if (denied != null) {
            return Result.error(denied);
        }
        BigDecimal hours = service.calculateLeaveHours(employeeId, startTime, endTime);
        return Result.success(hours);
    }

    /**
     * 计算加班小时数（仅本人或管理员可调用）
     */
    @GetMapping("/calculate-overtime-hours")
    public Result<BigDecimal> calculateOvertimeHours(
            @RequestParam Long employeeId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {
        String denied = checkNotSelfOrAdmin(employeeId);
        if (denied != null) {
            return Result.error(denied);
        }
        BigDecimal hours = service.calculateOvertimeHours(employeeId, startTime, endTime);
        return Result.success(hours);
    }

    /**
     * employeeId 与当前登录人不一致且非管理员时返回错误信息；允许时返回 null
     */
    private String checkNotSelfOrAdmin(Long employeeId) {
        LoginUser loginUser = SecurityUtils.getCurrentUser();
        if (loginUser == null) {
            return "请先登录";
        }
        if (SecurityUtils.isAdmin()) {
            return null;
        }
        Long selfId = loginUser.getEmployeeId() != null ? loginUser.getEmployeeId() : loginUser.getUserId();
        if (!java.util.Objects.equals(selfId, employeeId)) {
            return "只能查询自己的排班工时";
        }
        return null;
    }

    @GetMapping("/{id}")
    public Result<HrApplication> getById(@PathVariable Long id) {
        HrApplication application = service.getById(id);
        // 非管理员仅可查看自己的申请
        LoginUser loginUser = SecurityUtils.getCurrentUser();
        if (application != null && loginUser != null) {
            Long selfId = loginUser.getEmployeeId() != null ? loginUser.getEmployeeId() : loginUser.getUserId();
            boolean admin = loginUser.getRoles() != null && loginUser.getRoles().contains("ROLE_ADMIN");
            if (!admin && !selfId.equals(application.getEmployeeId())) {
                return Result.error("无权查看该申请");
            }
        }
        return Result.success(application);
    }

    @PostMapping
    public Result<Void> add(@RequestBody HrApplication entity) {
        // 申请类型白名单校验
        if (entity.getAppType() == null || !SUPPORTED_APP_TYPES.contains(entity.getAppType())) {
            return Result.error("不支持的申请类型: " + entity.getAppType());
        }
        // 非管理员只能为自己提交申请；状态一律服务端控制，禁止客户端直传"已通过"
        LoginUser loginUser = SecurityUtils.getCurrentUser();
        if (loginUser != null) {
            boolean admin = loginUser.getRoles() != null && loginUser.getRoles().contains("ROLE_ADMIN");
            Long selfId = loginUser.getEmployeeId() != null ? loginUser.getEmployeeId() : loginUser.getUserId();
            if (!admin && entity.getEmployeeId() != null && !selfId.equals(entity.getEmployeeId())) {
                return Result.error("只能为自己提交申请");
            }
            if (entity.getEmployeeId() == null) {
                entity.setEmployeeId(selfId);
            }
        }
        entity.setStatus(0);
        service.save(entity);
        // 通知移动端审批人（失败不影响提交）
        service.notifyApproversOnSubmit(entity);
        return Result.success();
    }

    /**
     * 修改申请：仅申请人本人且待审批状态，且不允许改状态/审批字段
     */
    @PutMapping
    public Result<Void> update(@RequestBody HrApplication entity) {
        if (entity.getId() == null) {
            return Result.error("参数错误");
        }
        LoginUser loginUser = SecurityUtils.getCurrentUser();
        HrApplication existing = service.getById(entity.getId());
        if (existing == null) {
            return Result.error("申请不存在");
        }
        boolean admin = loginUser != null && loginUser.getRoles() != null
                && loginUser.getRoles().contains("ROLE_ADMIN");
        if (!admin) {
            Long selfId = loginUser.getEmployeeId() != null ? loginUser.getEmployeeId() : loginUser.getUserId();
            if (!selfId.equals(existing.getEmployeeId())) {
                return Result.error("只能修改自己的申请");
            }
            if (existing.getStatus() == null || existing.getStatus() != 0) {
                return Result.error("仅待审批的申请可以修改");
            }
        }
        // 状态/审批字段不允许通过该接口修改
        entity.setStatus(null);
        entity.setApproveBy(null);
        entity.setApproveTime(null);
        entity.setApproveRemark(null);
        service.updateById(entity);
        return Result.success();
    }

    /**
     * 删除申请：仅管理员
     */
    @OperLog(module = "申请审批", action = "删除申请")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        if (!SecurityUtils.isAdmin()) {
            return Result.error("仅管理员可以删除申请");
        }
        service.removeById(id);
        return Result.success();
    }

    @RequiresPermission({"approval:pending:approve", "approval:pending:reject"})
    @PostMapping("/approve/{id}")
    @OperLog(module = "申请审批", action = "审批")
    public Result<Void> approve(@PathVariable Long id, @RequestParam Integer status,
            @RequestParam(required = false) String remark) {
        LoginUser loginUser = SecurityUtils.getCurrentUser();
        if (loginUser == null) {
            return Result.error("请先登录");
        }
        // 移动端员工（仅 ROLE_EMPLOYEE）走 hr_mobile_approver 校验，不查系统角色避免ID撞号
        boolean mobileEmployee = loginUser.getRoles() != null && loginUser.getRoles().size() == 1
                && loginUser.getRoles().contains("ROLE_EMPLOYEE");
        List<Long> roleIds = mobileEmployee ? null : sysUserMapper.selectRoleIdsByUserId(loginUser.getUserId());
        service.approve(id, status, remark, loginUser, roleIds);
        return Result.success();
    }

    @PostMapping("/cancel/{id}")
    public Result<Void> cancel(@PathVariable Long id) {
        LoginUser loginUser = SecurityUtils.getCurrentUser();
        if (loginUser == null) {
            return Result.error("请先登录");
        }
        service.cancel(id, loginUser);
        return Result.success();
    }

    /**
     * 审批进度记录（按节点顺序，含待处理节点）
     * 非管理员仅可查看自己的申请进度
     */
    @GetMapping("/approval-records/{id}")
    public Result<List<Map<String, Object>>> approvalRecords(@PathVariable Long id) {
        LoginUser loginUser = SecurityUtils.getCurrentUser();
        if (loginUser != null && !SecurityUtils.isAdmin()) {
            HrApplication application = service.getById(id);
            Long selfId = loginUser.getEmployeeId() != null ? loginUser.getEmployeeId() : loginUser.getUserId();
            if (application == null || !selfId.equals(application.getEmployeeId())) {
                return Result.error("无权查看该申请");
            }
        }
        return Result.success(service.getApprovalRecords(id));
    }
}
