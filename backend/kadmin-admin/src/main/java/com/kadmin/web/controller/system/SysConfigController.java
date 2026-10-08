package com.kadmin.web.controller.system;

import com.kadmin.common.Result;
import com.kadmin.common.annotation.RequiresPermission;
import com.kadmin.system.domain.SysConfig;
import com.kadmin.system.service.SysConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 系统参数配置控制器（仅管理员）
 */
@Tag(name = "系统参数配置")
@RestController
@RequestMapping("/system/config")
@RequiredArgsConstructor
public class SysConfigController {

    private final SysConfigService sysConfigService;

    @Operation(summary = "获取所有参数配置（含非公开项）")
    @GetMapping("/list")
    public Result<List<SysConfig>> list() {
        List<SysConfig> list = sysConfigService.getAllConfigs();
        return Result.success(list);
    }

    @Operation(summary = "新增参数配置")
    @RequiresPermission("system:config:add")
    @PostMapping
    public Result<Boolean> add(@RequestBody SysConfig config) {
        try {
            boolean success = sysConfigService.addConfig(config);
            return success ? Result.success(true) : Result.error("新增失败");
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "修改参数配置")
    @RequiresPermission("system:config:edit")
    @PutMapping
    public Result<Boolean> update(@RequestBody SysConfig config) {
        try {
            boolean success = sysConfigService.updateConfig(config);
            return success ? Result.success(true) : Result.error("更新失败");
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "删除参数配置")
    @RequiresPermission("system:config:delete")
    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        try {
            boolean success = sysConfigService.deleteConfig(id);
            return success ? Result.success(true) : Result.error("删除失败");
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        }
    }
}
