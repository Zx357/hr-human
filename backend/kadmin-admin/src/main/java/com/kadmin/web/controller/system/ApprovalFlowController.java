package com.kadmin.web.controller.system;

import com.kadmin.common.Result;
import com.kadmin.common.annotation.RequiresPermission;
import com.kadmin.system.domain.SysApprovalFlow;
import com.kadmin.system.service.ApprovalFlowService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/system/approval-flow")
@RequiredArgsConstructor
public class ApprovalFlowController {
    private final ApprovalFlowService service;

    @GetMapping("/list")
    public Result<List<SysApprovalFlow>> list() {
        return Result.success(service.listAll());
    }

    @GetMapping("/{id}")
    public Result<SysApprovalFlow> getById(@PathVariable Long id) {
        return Result.success(service.getDetailById(id));
    }

    @RequiresPermission("approval:flow:add")
    @PostMapping
    public Result<Void> add(@RequestBody SysApprovalFlow flow) {
        if (flow.getStatus() == null) flow.setStatus(1);
        service.saveFlow(flow);
        return Result.success();
    }

    @RequiresPermission("approval:flow:edit")
    @PutMapping
    public Result<Void> update(@RequestBody SysApprovalFlow flow) {
        service.updateFlow(flow);
        return Result.success();
    }

    @RequiresPermission("approval:flow:delete")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        service.removeById(id);
        return Result.success();
    }

    @RequiresPermission("approval:flow:edit")
    @PostMapping("/status/{id}")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        service.updateStatus(id, status);
        return Result.success();
    }
}
