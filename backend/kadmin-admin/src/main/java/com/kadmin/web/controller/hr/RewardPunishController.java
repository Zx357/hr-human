package com.kadmin.web.controller.hr;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kadmin.common.annotation.OperLog;
import com.kadmin.common.annotation.RequiresPermission;
import com.kadmin.common.Result;
import com.kadmin.hr.domain.HrRewardPunish;
import com.kadmin.hr.service.RewardPunishService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/hr/reward")
@RequiredArgsConstructor
public class RewardPunishController {
    private final RewardPunishService service;
    private final com.kadmin.system.service.NotificationService notificationService;

    @GetMapping("/page")
    public Result<Page<HrRewardPunish>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String employeeName,
            @RequestParam(required = false) Integer type,
            @RequestParam(required = false) Integer status) {
        return Result.success(service.getPage(pageNum, pageSize, employeeName, type, status));
    }

    @GetMapping("/{id}")
    public Result<HrRewardPunish> getById(@PathVariable Long id) {
        return Result.success(service.getById(id));
    }

    @RequiresPermission("application:reward:add")
    @PostMapping
    public Result<Void> add(@RequestBody HrRewardPunish entity) {
        // 初始状态由服务端强制，禁止客户端直传
        entity.setStatus(0);
        service.save(entity);
        return Result.success();
    }

    @RequiresPermission("application:reward:edit")
    @PutMapping
    public Result<Void> update(@RequestBody HrRewardPunish entity) {
        // 状态/审批字段不允许通过该接口修改
        entity.setStatus(null);
        entity.setApproveBy(null);
        entity.setApproveTime(null);
        entity.setApproveRemark(null);
        service.updateById(entity);
        return Result.success();
    }

    @RequiresPermission("application:reward:delete")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        service.removeById(id);
        return Result.success();
    }

    @RequiresPermission("application:reward:approve")
    @PostMapping("/approve/{id}")
    @OperLog(module = "申请审批", action = "奖惩审批")
    public Result<Void> approve(@PathVariable Long id, @RequestParam Integer status,
            @RequestParam(required = false) String remark) {
        Long approverId = com.kadmin.common.utils.SecurityUtils.getCurrentUserId();
        if (approverId == null) {
            return Result.error("无法识别当前审批人，请重新登录");
        }
        HrRewardPunish record = service.getById(id);
        boolean success = service.approve(id, status, remark, approverId);
        if (success) {
            notifyApplicant(record, status, remark, id, "奖惩");
        }
        return Result.success();
    }

    /**
     * 审批结果站内通知申请人（通知失败不影响审批）
     */
    private void notifyApplicant(HrRewardPunish record, Integer status, String remark, Long refId, String label) {
        if (record == null || record.getEmployeeId() == null) {
            return;
        }
        boolean approved = status != null && status == 1;
        notificationService.notify(record.getEmployeeId(), "approval_result",
                approved ? "审批通过" : "审批驳回",
                "你的" + label + "申请已" + (approved ? "通过审批" : "被驳回")
                        + (remark != null && !remark.isBlank() ? "：" + remark : ""),
                refId, "/homePages/application");
    }
}
