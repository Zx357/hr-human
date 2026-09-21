package com.kadmin.web.controller.hr;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
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
        String timeError = validateTimeRange(entity.getAppType(), entity.getStartTime(), entity.getEndTime());
        if (timeError != null) {
            return Result.error(timeError);
        }
        String overlapError = checkTimeOverlap(entity.getEmployeeId(), entity.getAppType(),
                entity.getStartTime(), entity.getEndTime(), null);
        if (overlapError != null) {
            return Result.error(overlapError);
        }
        service.save(entity);
        // 通知移动端审批人（失败不影响提交）
        service.notifyApproversOnSubmit(entity);
        return Result.success();
    }

    /**
     * 修改申请：仅申请人本人且待审批状态，且不允许改状态/审批字段/归属/类型
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
        // 状态/审批字段不允许通过该接口修改；归属员工与申请类型同样锁定，防止越权篡改
        entity.setStatus(null);
        entity.setEmployeeId(existing.getEmployeeId());
        entity.setAppType(existing.getAppType());
        entity.setApproveBy(null);
        entity.setApproveTime(null);
        entity.setApproveRemark(null);
        // 时间段按"提交值+存量值"合并后校验
        LocalDateTime start = entity.getStartTime() != null ? entity.getStartTime() : existing.getStartTime();
        LocalDateTime end = entity.getEndTime() != null ? entity.getEndTime() : existing.getEndTime();
        String timeError = validateTimeRange(existing.getAppType(), start, end);
        if (timeError != null) {
            return Result.error(timeError);
        }
        String overlapError = checkTimeOverlap(existing.getEmployeeId(), existing.getAppType(), start, end,
                existing.getId());
        if (overlapError != null) {
            return Result.error(overlapError);
        }
        service.updateById(entity);
        return Result.success();
    }

    /**
     * 时间段基础校验：结束不得早于开始（离职等无时间段类型放行）
     */
    private String validateTimeRange(String appType, LocalDateTime start, LocalDateTime end) {
        if (!isTimeRangeType(appType) || start == null || end == null) {
            return null;
        }
        if (end.isBefore(start)) {
            return "结束时间不能早于开始时间";
        }
        return null;
    }

    private boolean isTimeRangeType(String appType) {
        return "leave".equals(appType) || "overtime".equals(appType)
                || "business".equals(appType) || "exchange".equals(appType) || "makeup".equals(appType);
    }

    /**
     * 同员工同类型的在途/已通过申请时间段重叠校验（换休/补卡等点状时间段天然不重叠，无需检查）
     */
    private String checkTimeOverlap(Long employeeId, String appType, LocalDateTime start, LocalDateTime end,
            Long excludeId) {
        if (!isTimeRangeType(appType) || "makeup".equals(appType) || employeeId == null
                || start == null || end == null) {
            return null;
        }
        long count = service.count(new LambdaQueryWrapper<HrApplication>()
                .eq(HrApplication::getEmployeeId, employeeId)
                .eq(HrApplication::getAppType, appType)
                .in(HrApplication::getStatus, 0, 1)
                .ne(excludeId != null, HrApplication::getId, excludeId)
                .le(HrApplication::getStartTime, end)
                .ge(HrApplication::getEndTime, start));
        return count > 0 ? "该时间段已存在同类型申请，请勿重复提交" : null;
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
        boolean approved = service.approve(id, status, remark, loginUser, roleIds);
        if (!approved) {
            // 并发场景下申请已被其他审批人处理，条件更新失败
            return Result.error("审批失败：该申请可能已被其他审批人处理，请刷新后查看");
        }
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
