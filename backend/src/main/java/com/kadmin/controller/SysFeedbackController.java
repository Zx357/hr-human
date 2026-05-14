package com.kadmin.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kadmin.common.Result;
import com.kadmin.entity.SysFeedback;
import com.kadmin.service.SysFeedbackService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 意见反馈控制器
 */
@Tag(name = "意见反馈")
@RestController
@RequestMapping("/system/feedback")
@RequiredArgsConstructor
public class SysFeedbackController {

    private final SysFeedbackService feedbackService;

    @Operation(summary = "分页查询意见反馈")
    @GetMapping("/page")
    public Result<Map<String, Object>> pageFeedbacks(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Integer feedbackType,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String employeeNo) {
        Page<SysFeedback> page = new Page<>(current, size);
        IPage<SysFeedback> result = feedbackService.pageFeedbacks(page, feedbackType, status, keyword, employeeNo);

        Map<String, Object> data = new HashMap<>();
        data.put("records", result.getRecords());
        data.put("total", result.getTotal());
        data.put("current", result.getCurrent());
        data.put("size", result.getSize());
        return Result.success(data);
    }

    @Operation(summary = "查询我的意见反馈")
    @GetMapping("/my")
    public Result<Map<String, Object>> myFeedbacks(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size) {
        IPage<SysFeedback> result = feedbackService.pageMine(new Page<>(current, size));

        Map<String, Object> data = new HashMap<>();
        data.put("records", result.getRecords());
        data.put("total", result.getTotal());
        data.put("current", result.getCurrent());
        data.put("size", result.getSize());
        return Result.success(data);
    }

    @Operation(summary = "提交意见反馈")
    @PostMapping
    public Result<Boolean> submit(@Valid @RequestBody SubmitFeedbackRequest request) {
        SysFeedback feedback = new SysFeedback();
        feedback.setFeedbackType(request.getFeedbackType());
        feedback.setFeedbackContent(request.getFeedbackContent());
        feedback.setContactName(request.getContactName());
        feedback.setContactPhone(request.getContactPhone());
        boolean success = feedbackService.submit(feedback);
        return success ? Result.success(true) : Result.error("提交失败");
    }

    @Operation(summary = "获取意见反馈详情")
    @GetMapping("/{id}")
    public Result<SysFeedback> detail(@PathVariable Long id) {
        SysFeedback feedback = feedbackService.getById(id);
        if (feedback == null) {
            return Result.error("反馈不存在");
        }
        return Result.success(feedback);
    }

    @Operation(summary = "回复意见反馈")
    @PutMapping("/{id}/reply")
    public Result<Boolean> reply(@PathVariable Long id, @RequestBody ReplyFeedbackRequest request) {
        boolean success = feedbackService.reply(id, request.getReplyContent());
        return success ? Result.success(true) : Result.error("回复失败");
    }

    @Operation(summary = "更新意见反馈状态")
    @PutMapping("/{id}/status")
    public Result<Boolean> updateStatus(@PathVariable Long id, @RequestBody UpdateStatusRequest request) {
        boolean success = feedbackService.updateStatus(id, request.getStatus());
        return success ? Result.success(true) : Result.error("更新失败");
    }

    @Operation(summary = "删除意见反馈")
    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        boolean success = feedbackService.removeById(id);
        return success ? Result.success(true) : Result.error("删除失败");
    }

    @Data
    public static class SubmitFeedbackRequest {
        private Integer feedbackType;

        @NotBlank(message = "反馈内容不能为空")
        private String feedbackContent;

        private String contactName;
        private String contactPhone;
    }

    @Data
    public static class ReplyFeedbackRequest {
        private String replyContent;
    }

    @Data
    public static class UpdateStatusRequest {
        private Integer status;
    }
}
