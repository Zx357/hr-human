package com.kadmin.web.controller.mobile;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kadmin.common.Result;
import com.kadmin.common.security.LoginUser;
import com.kadmin.common.utils.SecurityUtils;
import com.kadmin.system.domain.SysFeedback;
import com.kadmin.system.service.SysFeedbackService;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 移动端意见反馈Controller：本人反馈历史（含管理员回复）与提交反馈
 */
@RestController
@RequestMapping("/mobile/feedback")
@RequiredArgsConstructor
public class MobileFeedbackController {

    private final SysFeedbackService feedbackService;

    /**
     * 我的反馈分页列表（含管理员回复内容 replyContent 与回复时间 replyTime）
     */
    @GetMapping("/my")
    public Result<Page<SysFeedback>> myFeedbacks(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        LoginUser loginUser = SecurityUtils.getCurrentUser();
        if (loginUser == null) {
            return Result.error("请先登录");
        }
        return Result.success((Page<SysFeedback>) feedbackService.pageMine(
                new Page<>(Math.max(pageNum, 1), Math.min(Math.max(pageSize, 1), 100))));
    }

    /**
     * 提交反馈，body = {feedbackType?, feedbackContent, contactName?, contactPhone?}
     * 联系人/联系电话缺省时由服务端按当前登录人自动补全
     */
    @PostMapping
    public Result<Void> submit(@RequestBody Map<String, Object> body) {
        LoginUser loginUser = SecurityUtils.getCurrentUser();
        if (loginUser == null) {
            return Result.error("请先登录");
        }
        String content = body.get("feedbackContent") == null ? null : String.valueOf(body.get("feedbackContent"));
        if (!StringUtils.hasText(content)) {
            return Result.error("反馈内容不能为空");
        }
        SysFeedback feedback = new SysFeedback();
        feedback.setFeedbackType(body.get("feedbackType") == null ? null
                : Integer.valueOf(String.valueOf(body.get("feedbackType"))));
        feedback.setFeedbackContent(content.trim());
        feedback.setContactName(body.get("contactName") == null ? null : String.valueOf(body.get("contactName")));
        feedback.setContactPhone(body.get("contactPhone") == null ? null : String.valueOf(body.get("contactPhone")));
        boolean success = feedbackService.submit(feedback);
        return success ? Result.success() : Result.error("提交失败，请稍后重试");
    }
}
