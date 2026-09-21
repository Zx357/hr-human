package com.kadmin.framework.service;

import com.kadmin.common.Result;
import com.kadmin.common.ResultCode;
import com.kadmin.common.exception.BusinessException;
import com.kadmin.hr.domain.HrEmployee;
import com.kadmin.system.domain.SysUser;
import com.kadmin.hr.mapper.EmployeeMapper;
import com.kadmin.system.mapper.SysUserMapper;
import com.kadmin.common.security.LoginUser;
import com.kadmin.framework.security.TokenService;
import com.kadmin.system.domain.SysOperLog;
import com.kadmin.system.mapper.SysOperLogMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

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
    private final SysOperLogMapper operLogMapper;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;
    private final org.springframework.data.redis.core.RedisTemplate<String, Object> redisTemplate;

    /**
     * 登录失败锁定：同一账号 15 分钟内失败 5 次锁定
     */
    private static final String LOGIN_FAIL_PREFIX = "login_fail:";
    private static final int LOGIN_FAIL_MAX = 5;
    private static final long LOGIN_FAIL_TTL_MINUTES = 15;

    /**
     * 登录 - 适配soybean-admin前端
     * 返回格式: { token: string, refreshToken: string }
     */
    public Result<Map<String, String>> login(String username, String password) {
        assertNotLocked("PC", username);

        // 查询用户
        SysUser user = userMapper.selectByUsername(username);
        // 统一提示，防止用户名枚举
        if (user == null || !passwordEncoder.matches(password, user.getPassword())) {
            recordLoginFail("PC", username);
            recordLoginLog(username, false, "用户名或密码错误");
            throw new BusinessException("用户名或密码错误");
        }

        // 检查用户状态
        if (user.getStatus() != 1) {
            recordLoginLog(username, false, "账号已禁用");
            throw new BusinessException(ResultCode.USER_DISABLED);
        }

        clearLoginFail("PC", username);

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
        recordLoginLog(username, true, null);

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
        if (password == null || password.isEmpty()) {
            throw new BusinessException("请输入密码");
        }
        assertNotLocked("M", employeeNo);

        // 直接通过工号查询员工（统一提示，防止工号枚举）
        HrEmployee employee = employeeMapper.selectByEmployeeNo(employeeNo);
        if (employee == null) {
            recordLoginFail("M", employeeNo);
            recordLoginLog(employeeNo, false, "工号或密码错误");
            throw new BusinessException("工号或密码错误");
        }

        // 检查员工状态（1-在职）
        Integer employeeStatus = employee.getStatus();
        if (employeeStatus != null && employeeStatus != 1) {
            recordLoginLog(employeeNo, false, "该员工已离职，无法登录");
            throw new BusinessException("该员工已离职，无法登录");
        }

        if (!matchesEmployeePassword(employee, password)) {
            recordLoginFail("M", employeeNo);
            recordLoginLog(employeeNo, false, "工号或密码错误");
            throw new BusinessException("工号或密码错误");
        }

        clearLoginFail("M", employeeNo);

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

        recordLoginLog(employeeNo, true, null);

        // 返回结果
        Map<String, String> result = new HashMap<>();
        result.put("token", token);
        result.put("refreshToken", refreshToken);

        return Result.success(result);
    }

    /**
     * 校验员工密码：兼容存量明文（命中后自动升级为BCrypt），新密码一律BCrypt
     * 空密码账号仅以默认密码 123456 比对（不落库），首个成功登录后才会写入加密密码
     */
    private boolean matchesEmployeePassword(HrEmployee employee, String rawPassword) {
        String stored = employee.getPassword();
        if (stored == null || stored.isEmpty()) {
            // 未设置过密码的存量账号：与默认密码比对，成功即初始化为加密存储
            if (!"123456".equals(rawPassword)) {
                return false;
            }
            employee.setPassword(passwordEncoder.encode(rawPassword));
            employeeMapper.updateById(employee);
            return true;
        }
        if (stored.startsWith("$2")) {
            return passwordEncoder.matches(rawPassword, stored);
        }
        // 存量明文密码：校验通过后立即升级为BCrypt
        if (stored.equals(rawPassword)) {
            employee.setPassword(passwordEncoder.encode(rawPassword));
            employeeMapper.updateById(employee);
            return true;
        }
        return false;
    }

    // ==================== 登录日志（复用操作日志表，PC"系统管理-操作日志"可查） ====================

    /**
     * 记录登录日志：成功与失败均记录，写入 sys_oper_log（模块=系统登录），
     * 失败不抛出，不影响登录主流程
     */
    private void recordLoginLog(String account, boolean success, String message) {
        try {
            SysOperLog operLog = new SysOperLog();
            operLog.setModule("系统登录");
            operLog.setAction(success ? "登录成功" : "登录失败");
            operLog.setUsername(account);
            operLog.setStatus(success ? 1 : 0);
            if (!success && message != null) {
                operLog.setErrorMsg(message);
            }
            ServletRequestAttributes attributes =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                operLog.setRequestUri(request.getMethod() + " " + request.getRequestURI());
                operLog.setIp(resolveClientIp(request));
            }
            operLog.setCreatedTime(java.time.LocalDateTime.now());
            operLogMapper.insert(operLog);
        } catch (Exception e) {
            log.warn("记录登录日志失败: {}", e.getMessage());
        }
    }

    private String resolveClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
            int index = ip.indexOf(',');
            return index > 0 ? ip.substring(0, index) : ip;
        }
        ip = request.getHeader("X-Real-IP");
        if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
            return ip;
        }
        return request.getRemoteAddr();
    }

    // ==================== 登录防爆破 ====================

    private void assertNotLocked(String scene, String account) {
        Object fails = redisTemplate.opsForValue().get(LOGIN_FAIL_PREFIX + scene + ":" + account);
        if (fails instanceof Number count && count.intValue() >= LOGIN_FAIL_MAX) {
            throw new BusinessException("失败次数过多，账号已临时锁定，请" + LOGIN_FAIL_TTL_MINUTES + "分钟后重试");
        }
    }

    private void recordLoginFail(String scene, String account) {
        try {
            String key = LOGIN_FAIL_PREFIX + scene + ":" + account;
            // 原子自增，避免并发 get-then-set 丢计数；仅首次写入时设置过期时间
            Long next = redisTemplate.opsForValue().increment(key);
            if (next != null && next == 1L) {
                redisTemplate.expire(key, LOGIN_FAIL_TTL_MINUTES, java.util.concurrent.TimeUnit.MINUTES);
            }
        } catch (Exception e) {
            log.warn("记录登录失败次数异常: {}", e.getMessage());
        }
    }

    private void clearLoginFail(String scene, String account) {
        try {
            redisTemplate.delete(LOGIN_FAIL_PREFIX + scene + ":" + account);
        } catch (Exception e) {
            log.warn("清理登录失败次数异常: {}", e.getMessage());
        }
    }

    /**
     * 获取当前用户信息 - 适配soybean-admin前端
     * 返回格式: { userId: string, userName: string, roles: string[], buttons: string[]
     * }
     * 角色权限取自登录会话；后台修改角色/权限时会删除该用户会话强制重新登录，保证生效
     */
    public Result<Map<String, Object>> getUserInfo(LoginUser loginUser) {
        Set<String> roles = loginUser.getRoles() != null ? loginUser.getRoles() : new HashSet<>();
        // 超级管理员（ROLE_ADMIN）追加通配权限：即使角色未绑定任何菜单，
        // PC 端按钮权限（hasAuth/v-permission）也全部放行，避免管理员被锁在按钮之外
        Set<String> permissions = new HashSet<>(
                loginUser.getPermissions() != null ? loginUser.getPermissions() : new HashSet<>());
        if (roles.contains("ROLE_ADMIN")) {
            permissions.add("*:*:*");
        }

        Map<String, Object> result = new HashMap<>();
        result.put("userId", String.valueOf(loginUser.getUserId()));
        result.put("userName", loginUser.getNickname() != null ? loginUser.getNickname() : loginUser.getUsername());
        result.put("employeeId", loginUser.getEmployeeId());
        result.put("roles", new ArrayList<>(roles));
        result.put("buttons", new ArrayList<>(permissions));
        // 员工头像（公开目录，直接以 URL 引用）
        if (loginUser.getEmployeeId() != null) {
            HrEmployee employee = employeeMapper.selectById(loginUser.getEmployeeId());
            if (employee != null && employee.getAvatar() != null && !employee.getAvatar().isBlank()) {
                result.put("avatar", employee.getAvatar());
            }
        }
        return Result.success(result);
    }

    /**
     * 登出（同时吊销访问Token与其配对的RefreshToken会话）
     */
    public Result<Void> logout(LoginUser loginUser) {
        if (loginUser != null) {
            tokenService.deleteTokenWithRefreshToken(loginUser.getToken());
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
        String policyError = com.kadmin.common.utils.PasswordPolicy.check(newPassword);
        if (policyError != null) {
            throw new BusinessException(policyError);
        }
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
        String policyError = com.kadmin.common.utils.PasswordPolicy.check(newPassword);
        if (policyError != null) {
            throw new BusinessException(policyError);
        }
        HrEmployee employee = employeeMapper.selectById(employeeId);
        if (employee == null) {
            throw new BusinessException("员工不存在");
        }

        String stored = employee.getPassword();
        boolean oldMatches;
        if (stored == null || stored.isEmpty()) {
            oldMatches = "123456".equals(oldPassword);
        } else if (stored.startsWith("$2")) {
            oldMatches = passwordEncoder.matches(oldPassword, stored);
        } else {
            oldMatches = stored.equals(oldPassword);
        }
        if (!oldMatches) {
            throw new BusinessException("原密码错误");
        }

        // 更新密码（BCrypt加密存储）
        employee.setPassword(passwordEncoder.encode(newPassword));
        employeeMapper.updateById(employee);

        // 修改密码后强制重新登录（员工命名空间）
        tokenService.deleteUserTokenByEmployee(employeeId);

        return Result.success();
    }
}
