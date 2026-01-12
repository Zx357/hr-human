package com.kadmin.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kadmin.common.Result;
import com.kadmin.entity.HrApplication;
import com.kadmin.service.ApplicationService;
import com.kadmin.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/hr/application")
@RequiredArgsConstructor
public class ApplicationController {
    private final ApplicationService service;

    @GetMapping("/page")
    public Result<Page<HrApplication>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String employeeName,
            @RequestParam(required = false) String employeeNo,
            @RequestParam(required = false) String appType,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Long employeeId) {
        return Result.success(service.getPage(pageNum, pageSize, employeeName, employeeNo, appType, status, employeeId));
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

    @GetMapping("/{id}")
    public Result<HrApplication> getById(@PathVariable Long id) {
        return Result.success(service.getById(id));
    }

    @PostMapping
    public Result<Void> add(@RequestBody HrApplication entity) {
        if (entity.getStatus() == null) entity.setStatus(0);
        service.save(entity);
        return Result.success();
    }

    @PutMapping
    public Result<Void> update(@RequestBody HrApplication entity) {
        service.updateById(entity);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        service.removeById(id);
        return Result.success();
    }

    @PostMapping("/approve/{id}")
    public Result<Void> approve(@PathVariable Long id, @RequestParam Integer status,
            @RequestParam(required = false) String remark) {
        service.approve(id, status, remark, 1L);
        return Result.success();
    }
    
    @PostMapping("/cancel/{id}")
    public Result<Void> cancel(@PathVariable Long id) {
        service.cancel(id);
        return Result.success();
    }
}
