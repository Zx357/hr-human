package com.kadmin.web.controller.mobile;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kadmin.common.Result;
import com.kadmin.mobile.domain.MobileMomentComment;
import com.kadmin.mobile.domain.MobileMomentPost;
import com.kadmin.common.security.LoginUser;
import com.kadmin.mobile.service.MobileMomentService;
import com.kadmin.common.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/mobile/moments")
@RequiredArgsConstructor
public class MobileMomentController {

    private final MobileMomentService momentService;

    @GetMapping("/posts")
    public Result<Page<MobileMomentPost>> pagePosts(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(defaultValue = "latest") String tab) {
        Long employeeId = currentEmployeeId();
        if (employeeId == null) {
            return Result.error("请先登录");
        }
        return Result.success(momentService.pagePosts(pageNum, pageSize, tab, employeeId));
    }

    @GetMapping("/posts/{id}")
    public Result<MobileMomentPost> getPost(@PathVariable Long id) {
        Long employeeId = currentEmployeeId();
        if (employeeId == null) {
            return Result.error("请先登录");
        }
        MobileMomentPost post = momentService.getPostDetail(id, employeeId);
        if (post == null) {
            return Result.error("动态不存在");
        }
        return Result.success(post);
    }

    @PostMapping("/posts")
    public Result<MobileMomentPost> createPost(@RequestBody MobileMomentPost post) {
        Long employeeId = currentEmployeeId();
        if (employeeId == null) {
            return Result.error("请先登录");
        }
        try {
            return Result.success(momentService.createPost(post, employeeId));
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/posts/{id}/like")
    public Result<Map<String, Object>> toggleLike(@PathVariable Long id) {
        Long employeeId = currentEmployeeId();
        if (employeeId == null) {
            return Result.error("请先登录");
        }
        try {
            return Result.success(momentService.toggleLike(id, employeeId));
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/messages")
    public Result<Map<String, Object>> messages() {
        Long employeeId = currentEmployeeId();
        if (employeeId == null) {
            return Result.error("请先登录");
        }
        return Result.success(momentService.getMessageSummary(employeeId));
    }

    /**
     * 消息列表：我的动态收到的点赞与评论
     */
    @GetMapping("/messages/list")
    public Result<List<Map<String, Object>>> messageList(@RequestParam(defaultValue = "50") Integer limit) {
        Long employeeId = currentEmployeeId();
        if (employeeId == null) {
            return Result.error("请先登录");
        }
        int safeLimit = Math.min(Math.max(limit, 1), 100);
        return Result.success(momentService.listMessages(employeeId, safeLimit));
    }

    /**
     * 标记消息中心已读
     */
    @PostMapping("/messages/read")
    public Result<Void> markMessagesRead() {
        Long employeeId = currentEmployeeId();
        if (employeeId == null) {
            return Result.error("请先登录");
        }
        momentService.markMessagesRead(employeeId);
        return Result.success();
    }

    /**
     * 动态评论列表
     */
    @GetMapping("/posts/{id}/comments")
    public Result<List<MobileMomentComment>> comments(@PathVariable Long id) {
        Long employeeId = currentEmployeeId();
        if (employeeId == null) {
            return Result.error("请先登录");
        }
        return Result.success(momentService.listComments(id));
    }

    /**
     * 发表评论
     */
    @PostMapping("/posts/{id}/comments")
    public Result<MobileMomentComment> addComment(@PathVariable Long id, @RequestBody Map<String, String> body) {
        Long employeeId = currentEmployeeId();
        if (employeeId == null) {
            return Result.error("请先登录");
        }
        try {
            return Result.success(momentService.addComment(id, employeeId, body.get("content")));
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 删除自己的动态
     */
    @DeleteMapping("/posts/{id}")
    public Result<Void> deletePost(@PathVariable Long id) {
        Long employeeId = currentEmployeeId();
        if (employeeId == null) {
            return Result.error("请先登录");
        }
        try {
            momentService.deletePost(id, employeeId);
            return Result.success();
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 删除自己的评论
     */
    @DeleteMapping("/comments/{id}")
    public Result<Void> deleteComment(@PathVariable Long id) {
        Long employeeId = currentEmployeeId();
        if (employeeId == null) {
            return Result.error("请先登录");
        }
        try {
            momentService.deleteComment(id, employeeId);
            return Result.success();
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        }
    }

    private Long currentEmployeeId() {
        LoginUser loginUser = SecurityUtils.getCurrentUser();
        if (loginUser == null) {
            return null;
        }
        return loginUser.getEmployeeId() != null ? loginUser.getEmployeeId() : loginUser.getUserId();
    }
}
