package com.kadmin.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kadmin.common.Result;
import com.kadmin.entity.SysRole;
import com.kadmin.entity.SysUser;
import com.kadmin.service.SysUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户管理控制器
 */
@Tag(name = "用户管理")
@RestController
@RequestMapping("/system/user")
public class SysUserController {

    @Autowired
    private SysUserService userService;

    /**
     * 获取用户列表（不分页）
     */
    @Operation(summary = "获取用户列表")
    @GetMapping("/list")
    public Result<List<SysUser>> listUsers(
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String realName,
            @RequestParam(required = false) Integer status) {
        List<SysUser> users = userService.listUsers(username, realName, status);
        users.forEach(user -> user.setPassword(null));
        return Result.success(users);
    }

    /**
     * 分页查询用户列表
     */
    @Operation(summary = "分页查询用户列表")
    @GetMapping("/page")
    public Result<Map<String, Object>> pageUsers(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String nickname,
            @RequestParam(required = false) Integer status) {

        Page<SysUser> page = new Page<>(current, size);
        IPage<SysUser> result = userService.pageUsers(page, username, nickname, status);

        // 隐藏密码
        result.getRecords().forEach(user -> user.setPassword(null));

        Map<String, Object> data = new HashMap<>();
        data.put("records", result.getRecords());
        data.put("total", result.getTotal());
        data.put("current", result.getCurrent());
        data.put("size", result.getSize());

        return Result.success(data);
    }

    /**
     * 获取用户详情
     */
    @Operation(summary = "获取用户详情")
    @GetMapping("/{id}")
    public Result<Map<String, Object>> getUser(@PathVariable Long id) {
        SysUser user = userService.getById(id);
        if (user == null) {
            return Result.error("用户不存在");
        }
        user.setPassword(null);

        // 获取用户角色
        List<Long> roleIds = userService.getUserRoleIds(id);

        Map<String, Object> data = new HashMap<>();
        data.put("user", user);
        data.put("roleIds", roleIds);

        return Result.success(data);
    }

    /**
     * 新增用户
     */
    @Operation(summary = "新增用户")
    @PostMapping
    public Result<Boolean> addUser(@RequestBody UserRequest request) {
        try {
            SysUser user = new SysUser();
            user.setUsername(request.getUsername());
            user.setPassword(request.getPassword());
            user.setNickname(request.getNickname());
            user.setEmail(request.getEmail());
            user.setPhone(request.getPhone());
            user.setGender(request.getGender());
            user.setStatus(request.getStatus() != null ? request.getStatus() : 1);
            user.setEmployeeId(request.getEmployeeId());

            boolean success = userService.addUser(user, request.getRoleIds());
            return success ? Result.success(true) : Result.error("新增失败");
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 更新用户
     */
    @Operation(summary = "更新用户")
    @PutMapping
    public Result<Boolean> updateUser(@RequestBody UserRequest request) {
        try {
            SysUser user = new SysUser();
            user.setId(request.getId());
            user.setUsername(request.getUsername());
            user.setPassword(request.getPassword());
            user.setNickname(request.getNickname());
            user.setEmail(request.getEmail());
            user.setPhone(request.getPhone());
            user.setGender(request.getGender());
            user.setStatus(request.getStatus());
            user.setEmployeeId(request.getEmployeeId());

            boolean success = userService.updateUser(user, request.getRoleIds());
            return success ? Result.success(true) : Result.error("更新失败");
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 删除用户
     */
    @Operation(summary = "删除用户")
    @DeleteMapping("/{id}")
    public Result<Boolean> deleteUser(@PathVariable Long id) {
        boolean success = userService.deleteUser(id);
        return success ? Result.success(true) : Result.error("删除失败");
    }

    /**
     * 重置密码
     */
    @Operation(summary = "重置密码")
    @PostMapping("/{id}/resetPassword")
    public Result<Boolean> resetPassword(@PathVariable Long id, @RequestBody ResetPasswordRequest request) {
        boolean success = userService.resetPassword(id, request.getNewPassword());
        return success ? Result.success(true) : Result.error("重置密码失败");
    }

    /**
     * 修改状态
     */
    @Operation(summary = "修改用户状态")
    @PutMapping("/{id}/status")
    public Result<Boolean> changeStatus(@PathVariable Long id, @RequestBody StatusRequest request) {
        boolean success = userService.changeStatus(id, request.getStatus());
        return success ? Result.success(true) : Result.error("修改状态失败");
    }

    /**
     * 用户请求DTO
     */
    @Data
    public static class UserRequest {
        private Long id;
        private String username;
        private String password;
        private String nickname;
        private String email;
        private String phone;
        private Integer gender;
        private Integer status;
        private Long employeeId;
        private List<Long> roleIds;
    }

    /**
     * 重置密码请求DTO
     */
    @Data
    public static class ResetPasswordRequest {
        private String newPassword;
    }

    /**
     * 状态请求DTO
     */
    @Data
    public static class StatusRequest {
        private Integer status;
    }
}