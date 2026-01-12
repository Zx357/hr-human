package com.kadmin.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kadmin.common.Result;
import com.kadmin.entity.HrTransfer;
import com.kadmin.service.TransferService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/hr/transfer")
@RequiredArgsConstructor
public class TransferController {
    private final TransferService service;

    @GetMapping("/page")
    public Result<Page<HrTransfer>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String employeeName,
            @RequestParam(required = false) String transferType,
            @RequestParam(required = false) Integer status) {
        return Result.success(service.getPage(pageNum, pageSize, employeeName, transferType, status));
    }

    @GetMapping("/{id}")
    public Result<HrTransfer> getById(@PathVariable Long id) {
        return Result.success(service.getById(id));
    }

    @PostMapping
    public Result<Void> add(@RequestBody HrTransfer entity) {
        if (entity.getStatus() == null) entity.setStatus(0);
        service.save(entity);
        return Result.success();
    }

    @PutMapping
    public Result<Void> update(@RequestBody HrTransfer entity) {
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
}
