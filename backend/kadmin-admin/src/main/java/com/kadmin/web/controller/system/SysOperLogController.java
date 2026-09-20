package com.kadmin.web.controller.system;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.kadmin.common.annotation.RequiresPermission;
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
     *
     * @param beginTime 操作时间起（yyyy-MM-dd）
     * @param endTime   操作时间止（yyyy-MM-dd，含当天）
     */
    @GetMapping("/page")
    public Result<Page<SysOperLog>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String module,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String beginTime,
            @RequestParam(required = false) String endTime) {
        LambdaQueryWrapper<SysOperLog> wrapper = new LambdaQueryWrapper<SysOperLog>()
                .eq(StringUtils.hasText(module), SysOperLog::getModule, module)
                .like(StringUtils.hasText(username), SysOperLog::getUsername, username)
                .eq(status != null, SysOperLog::getStatus, status);
        try {
            if (StringUtils.hasText(beginTime)) {
                wrapper.ge(SysOperLog::getCreatedTime,
                        java.time.LocalDate.parse(beginTime).atStartOfDay());
            }
            if (StringUtils.hasText(endTime)) {
                wrapper.lt(SysOperLog::getCreatedTime,
                        java.time.LocalDate.parse(endTime).plusDays(1).atStartOfDay());
            }
        } catch (java.time.format.DateTimeParseException e) {
            return Result.error("时间格式错误，应为 yyyy-MM-dd");
        }
        wrapper.orderByDesc(SysOperLog::getId);
        return Result.success(operLogMapper.selectPage(new Page<>(pageNum, pageSize), wrapper));
    }

    /**
     * 清理 N 天前的日志（默认保留90天）
     */
    @RequiresPermission("system:oper-log:clean")
    @DeleteMapping("/clean")
    public Result<Void> clean(@RequestParam(defaultValue = "90") Integer days) {
        operLogMapper.delete(new LambdaQueryWrapper<SysOperLog>()
                .lt(SysOperLog::getCreatedTime, java.time.LocalDateTime.now().minusDays(Math.max(days, 7))));
        return Result.success();
    }
}
