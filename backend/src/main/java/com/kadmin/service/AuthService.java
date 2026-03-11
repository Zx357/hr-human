package com.kadmin.service;

import com.kadmin.common.Result;
import com.kadmin.common.ResultCode;
import com.kadmin.common.exception.BusinessException;
import com.kadmin.entity.HrEmployee;
import com.kadmin.entity.SysUser;
import com.kadmin.mapper.EmployeeMapper;
import com.kadmin.mapper.SysUserMapper;
import com.kadmin.security.LoginUser;
import com.kadmin.security.TokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 认证服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final SysUserMapper userMapper;
    private final EmployeeMapper employeeMapper;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;

    /**
     * 登录 - 适配soybean-admin前端
     * 返回格式: { token: string, refreshToken: string }
     */
    public Result<Map<String, String>> login(String username, String password) {
        // 查询用户
        SysUser user = userMapper.selectByUsername(username);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        // 验证密码
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BusinessException(ResultCode.USER_PASSWORD_ERROR);
        }

        // 检查用户状态
        if (user.getStatus() != 1) {
            throw new BusinessException(ResultCode.USER_DISABLED);
        }

        // 查询用户角色和权限
        Set<String> roles = userMapper.selectRoleCodesByUserId(user.getId());
        Set<String> permissions = userMapper.selectPermissionsByUserId(user.getId());

        // 创建登录用户
        LoginUser loginUser = new LoginUser(
                user.getId(),
                user.getUsername(),
                user.getPassword(),
                user.getNickname(),
                user.getAvatar(),
                user.getEmployeeId(),
                roles,
                permissions);

        // 生成Token
        String token = tokenService.createToken(loginUser);
        String refreshToken = tokenService.createRefreshToken(loginUser);

        // 返回结果 - 适配soybean-admin格式
        Map<String, String> result = new HashMap<>();
        result.put("token", token);
        result.put("refreshToken", refreshToken);

        return Result.success(result);
    }

    /**
     * 移动端登录 - 通过工号登录（不需要系统用户）
     * 返回格式: { token: string, refreshToken: string }
     */
    public Result<Map<String, String>> mobileLogin(String employeeNo, String password) {
        // 直接通过工号查询员工
        HrEmployee employee = employeeMapper.selectByEmployeeNo(employeeNo);
        if (employee == null) {
            throw new BusinessException("工号不存在");
        }

        // 检查员工状态（1-在职）
        if (employee.getStatus() != 1) {
            throw new BusinessException("该员工已离职，无法登录");
        }

        // 默认密码为123456
        String defaultPassword = "123456";
        String employeePassword = (employee.getPassword() == null || employee.getPassword().isEmpty())
                ? defaultPassword
                : employee.getPassword();
        String checkPassword = (password == null || password.isEmpty()) ? defaultPassword : password;

        if (!employeePassword.equals(checkPassword)) {
            throw new BusinessException("密码错误");
        }

        // 创建登录用户（员工身份）
        Set<String> roles = new HashSet<>();
        roles.add("ROLE_EMPLOYEE");
        Set<String> permissions = new HashSet<>();

        LoginUser loginUser = new LoginUser(
                employee.getId(),
                employee.getEmployeeNo(),
                "",
                employee.getName(),
                employee.getAvatar(),
                employee.getId(),
                roles,
                permissions);

        // 生成Token
        String token = tokenService.createToken(loginUser);
        String refreshToken = tokenService.createRefreshToken(loginUser);

        // 返回结果
        Map<String, String> result = new HashMap<>();
        result.put("token", token);
        result.put("refreshToken", refreshToken);

        return Result.success(result);
    }

    /**
     * 获取当前用户信息 - 适配soybean-admin前端
     * 返回格式: { userId: string, userName: string, roles: string[], buttons: string[]
     * }
     * 每次都从数据库重新查询权限，确保权限修改后立即生效
     */
    public Result<Map<String, Object>> getUserInfo(LoginUser loginUser) {
        // 重新从数据库查询用户角色和权限，确保权限修改后立即生效
        Set<String> roles = userMapper.selectRoleCodesByUserId(loginUser.getUserId());
        Set<String> permissions = userMapper.selectPermissionsByUserId(loginUser.getUserId());

        Map<String, Object> result = new HashMap<>();
        result.put("userId", String.valueOf(loginUser.getUserId()));
        result.put("userName", loginUser.getNickname() != null ? loginUser.getNickname() : loginUser.getUsername());
        result.put("roles", new ArrayList<>(roles));
        result.put("buttons", new ArrayList<>(permissions));
        return Result.success(result);
    }

    /**
     * 登出
     */
    public Result<Void> logout(LoginUser loginUser) {
        if (loginUser != null) {
            tokenService.deleteToken(loginUser.getToken());
        }
        return Result.success();
    }

    /**
     * 刷新Token
     */
    public Result<Map<String, String>> refreshToken(String refreshToken) {
        LoginUser loginUser = tokenService.validateRefreshToken(refreshToken);
        if (loginUser == null) {
            throw new BusinessException(ResultCode.TOKEN_INVALID);
        }

        // 生成新Token
        String newToken = tokenService.createToken(loginUser);
        String newRefreshToken = tokenService.createRefreshToken(loginUser);

        Map<String, String> result = new HashMap<>();
        result.put("token", newToken);
        result.put("refreshToken", newRefreshToken);

        return Result.success(result);
    }

    /**
     * 修改密码
     */
    public Result<Void> changePassword(Long userId, String oldPassword, String newPassword) {
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        // 验证旧密码
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BusinessException("原密码错误");
        }

        // 更新密码
        user.setPassword(passwordEncoder.encode(newPassword));
        userMapper.updateById(user);

        // 删除用户Token，强制重新登录
        tokenService.deleteUserToken(userId);

        return Result.success();
    }

    /**
     * 移动端修改密码（员工）
     */
    public Result<Void> mobileChangePassword(Long employeeId, String oldPassword, String newPassword) {
        HrEmployee employee = employeeMapper.selectById(employeeId);
        if (employee == null) {
            throw new BusinessException("员工不存在");
        }

        // 默认密码为123456
        String defaultPassword = "123456";
        String currentPassword = (employee.getPassword() == null || employee.getPassword().isEmpty())
                ? defaultPassword
                : employee.getPassword();

        if (!currentPassword.equals(oldPassword)) {
            throw new BusinessException("原密码错误");
        }

        // 更新密码
        employee.setPassword(newPassword);
        employeeMapper.updateById(employee);

        return Result.success();
    }
}