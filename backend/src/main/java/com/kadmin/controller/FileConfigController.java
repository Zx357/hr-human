package com.kadmin.controller;

import com.kadmin.common.Result;
import com.kadmin.entity.SysFileConfig;
import com.kadmin.service.FileConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 文件路径配置控制器
 */
@Tag(name = "文件路径配置")
@RestController
@RequestMapping("/system/file-config")
@RequiredArgsConstructor
public class FileConfigController {

    private final FileConfigService fileConfigService;

    @Operation(summary = "获取所有路径配置")
    @GetMapping("/list")
    public Result<List<SysFileConfig>> list() {
        List<SysFileConfig> list = fileConfigService.getAllConfigs();
        return Result.success(list);
    }

    @Operation(summary = "获取配置详情")
    @GetMapping("/{id}")
    public Result<SysFileConfig> getById(@PathVariable Long id) {
        SysFileConfig config = fileConfigService.getById(id);
        return Result.success(config);
    }

    @Operation(summary = "更新路径配置")
    @PutMapping
    public Result<Boolean> update(@RequestBody SysFileConfig config) {
        if (config.getId() == null) {
            return Result.error("配置ID不能为空");
        }
        if (config.getConfigValue() == null || config.getConfigValue().trim().isEmpty()) {
            return Result.error("路径不能为空");
        }
        boolean success = fileConfigService.updateConfig(config);
        return success ? Result.success(true) : Result.error("更新失败");
    }

    @Operation(summary = "刷新路径配置缓存")
    @PostMapping("/refresh")
    public Result<Boolean> refresh() {
        fileConfigService.refreshCache();
        return Result.success(true);
    }
}
