package com.kadmin.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kadmin.common.Result;
import com.kadmin.entity.HrMobileApprover;
import com.kadmin.service.MobileApproverService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/hr/mobile-approver")
@RequiredArgsConstructor
public class MobileApproverController {

    private final MobileApproverService service;

    @GetMapping("/page")
    public Result<Page<HrMobileApprover>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String employeeName,
            @RequestParam(required = false) String employeeNo) {
        return Result.success(service.getPage(pageNum, pageSize, employeeName, employeeNo));
    }

    @GetMapping("/list")
    public Result<List<HrMobileApprover>> list() {
        return Result.success(service.list());
    }

    @GetMapping("/{id}")
    public Result<HrMobileApprover> getById(@PathVariable Long id) {
        return Result.success(service.getById(id));
    }

    @PostMapping
    public Result<Void> add(@RequestBody HrMobileApprover entity) {
        // 检查同一员工是否已配置
        long count = service.count(new LambdaQueryWrapper<HrMobileApprover>()
                .eq(HrMobileApprover::getEmployeeId, entity.getEmployeeId()));
        if (count > 0) {
            return Result.error("该员工已配置审批权限，请勿重复添加");
        }
        service.save(entity);
        return Result.success();
    }

    @PutMapping
    public Result<Void> update(@RequestBody HrMobileApprover entity) {
        service.updateById(entity);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        service.removeById(id);
        return Result.success();
    }
}
