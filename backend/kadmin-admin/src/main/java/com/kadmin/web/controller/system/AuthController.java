package com.kadmin.web.controller.system;

import com.kadmin.common.Result;
import com.kadmin.common.security.LoginUser;
import com.kadmin.framework.service.AuthService;
import com.kadmin.common.utils.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 认证控制器
 */
@Tag(name = "认证管理")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * 登录
     * 返回格式: { token: string, refreshToken: string }
     */
    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public Result<Map<String, String>> login(@RequestBody LoginRequest request) {
        return authService.login(request.getUsername(), request.getPassword());
    }

    /**
     * 移动端登录（工号登录）
     * 返回格式: { token: string, refreshToken: string }
     */
    @Operation(summary = "移动端工号登录")
    @PostMapping("/mobile/login")
    public Result<Map<String, String>> mobileLogin(@RequestBody MobileLoginRequest request) {
        return authService.mobileLogin(request.getEmployeeNo(), request.getPassword());
    }

    /**
     * 登出
     */
    @Operation(summary = "用户登出")
    @PostMapping("/logout")
    public Result<Void> logout() {
        LoginUser loginUser = SecurityUtils.getCurrentUser();
        return authService.logout(loginUser);
    }

    /**
     * 获取当前用户信息
     * 返回格式: { userId: string, userName: string, roles: string[], buttons: string[]
     * }
     */
    @Operation(summary = "获取当前用户信息")
    @GetMapping("/info")
    public Result<Map<String, Object>> getUserInfo() {
        LoginUser loginUser = SecurityUtils.getCurrentUser();
        return authService.getUserInfo(loginUser);
    }

    /**
     * 刷新Token
     */
    @Operation(summary = "刷新Token")
    @PostMapping("/refreshToken")
    public Result<Map<String, String>> refreshToken(@RequestBody @Validated RefreshTokenRequest request) {
        return authService.refreshToken(request.getRefreshToken());
    }

    /**
     * 修改密码
     */
    @Operation(summary = "修改密码")
    @PostMapping("/change-password")
    public Result<Void> changePassword(@RequestBody @Validated ChangePasswordRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        return authService.changePassword(userId, request.getOldPassword(), request.getNewPassword());
    }

    /**
     * 移动端修改密码（员工）
     */
    @Operation(summary = "移动端修改密码")
    @PostMapping("/mobile/change-password")
    public Result<Void> mobileChangePassword(@RequestBody @Validated ChangePasswordRequest request) {
        LoginUser loginUser = SecurityUtils.getCurrentUser();
        if (loginUser == null) {
            return Result.error("请先登录");
        }
        if (loginUser.getEmployeeId() == null) {
            return Result.error("当前账号未关联员工信息，无法修改密码");
        }
        return authService.mobileChangePassword(loginUser.getEmployeeId(), request.getOldPassword(),
                request.getNewPassword());
    }

    /**
     * 登录请求
     */
    @Data
    public static class LoginRequest {
        private String username;
        private String password;
    }

    /**
     * 刷新Token请求
     */
    @Data
    public static class RefreshTokenRequest {
        @NotBlank(message = "RefreshToken不能为空")
        private String refreshToken;
    }

    /**
     * 修改密码请求
     */
    @Data
    public static class ChangePasswordRequest {
        @NotBlank(message = "原密码不能为空")
        private String oldPassword;

        @NotBlank(message = "新密码不能为空")
        private String newPassword;
    }

    /**
     * 移动端登录请求
     */
    @Data
    public static class MobileLoginRequest {
        @NotBlank(message = "工号不能为空")
        private String employeeNo;

        private String password;
    }
}