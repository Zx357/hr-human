package com.kadmin.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kadmin.common.Result;
import com.kadmin.entity.SysNotice;
import com.kadmin.service.SysNoticeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 公告通知控制器
 */
@Tag(name = "公告管理")
@RestController
@RequestMapping("/system/notice")
@RequiredArgsConstructor
public class SysNoticeController {

    private final SysNoticeService noticeService;

    /**
     * 分页查询公告（后台管理用）
     */
    @Operation(summary = "分页查询公告")
    @GetMapping("/page")
    public Result<Map<String, Object>> pageNotices(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String noticeTitle,
            @RequestParam(required = false) Integer noticeType,
            @RequestParam(required = false) Integer status) {

        Page<SysNotice> page = new Page<>(current, size);
        IPage<SysNotice> result = noticeService.pageNotices(page, noticeTitle, noticeType, status);

        Map<String, Object> data = new HashMap<>();
        data.put("records", result.getRecords());
        data.put("total", result.getTotal());
        data.put("current", result.getCurrent());
        data.put("size", result.getSize());

        return Result.success(data);
    }

    /**
     * 获取公告列表（不分页，供小程序使用）
     */
    @Operation(summary = "获取公告列表")
    @GetMapping("/list")
    public Result<List<SysNotice>> listNotices(
            @RequestParam(required = false) Integer noticeType,
            @RequestParam(required = false) Integer status) {
        List<SysNotice> list = noticeService.listNotices(noticeType, status);
        return Result.success(list);
    }

    /**
     * 获取公告详情
     */
    @Operation(summary = "获取公告详情")
    @GetMapping("/{id}")
    public Result<SysNotice> getNotice(@PathVariable Long id) {
        SysNotice notice = noticeService.getById(id);
        if (notice == null) {
            return Result.error("公告不存在");
        }
        return Result.success(notice);
    }

    /**
     * 新增公告
     */
    @Operation(summary = "新增公告")
    @PostMapping
    public Result<Boolean> addNotice(@RequestBody SysNotice notice) {
        boolean success = noticeService.addNotice(notice);
        return success ? Result.success(true) : Result.error("新增失败");
    }

    /**
     * 更新公告
     */
    @Operation(summary = "更新公告")
    @PutMapping
    public Result<Boolean> updateNotice(@RequestBody SysNotice notice) {
        boolean success = noticeService.updateNotice(notice);
        return success ? Result.success(true) : Result.error("更新失败");
    }

    /**
     * 删除公告
     */
    @Operation(summary = "删除公告")
    @DeleteMapping("/{id}")
    public Result<Boolean> deleteNotice(@PathVariable Long id) {
        boolean success = noticeService.deleteNotice(id);
        return success ? Result.success(true) : Result.error("删除失败");
    }
}
