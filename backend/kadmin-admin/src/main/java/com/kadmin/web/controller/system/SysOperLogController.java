package com.kadmin.web.controller.system;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kadmin.common.Result;
import com.kadmin.system.domain.SysOperLog;
import com.kadmin.system.mapper.SysOperLogMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 操作日志查询
 */
@RestController
@RequestMapping("/system/oper-log")
@RequiredArgsConstructor
public class SysOperLogController {

    private final SysOperLogMapper operLogMapper;

    /**
     * 分页查询操作日志
     */
    @GetMapping("/page")
    public Result<Page<SysOperLog>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String module,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) Integer status) {
        LambdaQueryWrapper<SysOperLog> wrapper = new LambdaQueryWrapper<SysOperLog>()
                .eq(StringUtils.hasText(module), SysOperLog::getModule, module)
                .like(StringUtils.hasText(username), SysOperLog::getUsername, username)
                .eq(status != null, SysOperLog::getStatus, status)
                .orderByDesc(SysOperLog::getId);
        return Result.success(operLogMapper.selectPage(new Page<>(pageNum, pageSize), wrapper));
    }

    /**
     * 清理 N 天前的日志（默认保留90天）
     */
    @DeleteMapping("/clean")
    public Result<Void> clean(@RequestParam(defaultValue = "90") Integer days) {
        operLogMapper.delete(new LambdaQueryWrapper<SysOperLog>()
                .lt(SysOperLog::getCreatedTime, java.time.LocalDateTime.now().minusDays(Math.max(days, 7))));
        return Result.success();
    }
}
