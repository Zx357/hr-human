package com.kadmin.web.controller.mobile;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kadmin.common.Result;
import com.kadmin.mobile.domain.MobileMomentPost;
import com.kadmin.common.security.LoginUser;
import com.kadmin.mobile.service.MobileMomentService;
import com.kadmin.common.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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

    private Long currentEmployeeId() {
        LoginUser loginUser = SecurityUtils.getCurrentUser();
        if (loginUser == null) {
            return null;
        }
        return loginUser.getEmployeeId() != null ? loginUser.getEmployeeId() : loginUser.getUserId();
    }
}
