package com.kadmin.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kadmin.common.Result;
import com.kadmin.entity.HrRewardPunish;
import com.kadmin.service.RewardPunishService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/hr/reward")
@RequiredArgsConstructor
public class RewardPunishController {
    private final RewardPunishService service;

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

    @PostMapping
    public Result<Void> add(@RequestBody HrRewardPunish entity) {
        if (entity.getStatus() == null) entity.setStatus(0);
        service.save(entity);
        return Result.success();
    }

    @PutMapping
    public Result<Void> update(@RequestBody HrRewardPunish entity) {
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
