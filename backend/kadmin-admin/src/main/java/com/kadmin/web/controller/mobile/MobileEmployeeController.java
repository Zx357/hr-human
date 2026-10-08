package com.kadmin.web.controller.mobile;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.kadmin.common.Result;
import com.kadmin.common.security.LoginUser;
import com.kadmin.common.utils.SecurityUtils;
import com.kadmin.hr.domain.HrEmployee;
import com.kadmin.hr.mapper.EmployeeMapper;
import com.kadmin.system.domain.SysUser;
import com.kadmin.system.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.regex.Pattern;

/**
 * 移动端个人信息Controller（员工自助维护）
 */
@RestController
@RequestMapping("/mobile/employee")
@RequiredArgsConstructor
public class MobileEmployeeController {

    private final EmployeeMapper employeeMapper;
    private final SysUserMapper sysUserMapper;
    private final PasswordEncoder passwordEncoder;

    /** 大陆手机号格式（宽松：1开头11位数字） */
    private static final Pattern PHONE_PATTERN = Pattern.compile("^1\\d{10}$");

    /**
     * 修改本人手机号，body = {phone, password}
     * 校验当前登录员工对应账号密码（BCrypt，兼容存量明文/空密码默认123456），
     * 通过后更新员工手机号，并同步冗余的 sys_user.phone（有关联账号时）
     */
    @PutMapping("/phone")
    @Transactional
    public Result<Void> updatePhone(@RequestBody Map<String, Object> body) {
        LoginUser loginUser = SecurityUtils.getCurrentUser();
        if (loginUser == null) {
            return Result.error("请先登录");
        }
        Long employeeId = loginUser.getEmployeeId() != null ? loginUser.getEmployeeId() : loginUser.getUserId();
        String phone = body.get("phone") == null ? null : String.valueOf(body.get("phone")).trim();
        String password = body.get("password") == null ? null : String.valueOf(body.get("password"));

        if (phone == null || phone.isEmpty()) {
            return Result.error("手机号不能为空");
        }
        if (!PHONE_PATTERN.matcher(phone).matches()) {
            return Result.error("手机号格式不正确");
        }
        if (password == null || password.isEmpty()) {
            return Result.error("请输入登录密码");
        }

        HrEmployee employee = employeeMapper.selectById(employeeId);
        if (employee == null) {
            return Result.error("员工信息不存在");
        }
        if (!matchesEmployeePassword(employee, password)) {
            return Result.error("密码错误");
        }

        // 手机号唯一性校验（其他员工已占用则拒绝）
        Long duplicated = employeeMapper.selectCount(new LambdaQueryWrapper<HrEmployee>()
                .eq(HrEmployee::getPhone, phone)
                .ne(employee.getId() != null, HrEmployee::getId, employee.getId()));
        if (duplicated != null && duplicated > 0) {
            return Result.error("该手机号已被使用");
        }

        employee.setPhone(phone);
        employeeMapper.updateById(employee);

        // 同步冗余的 PC 账号手机号（有关联账号时）
        if (employee.getId() != null) {
            SysUser linkedUser = sysUserMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                    .eq(SysUser::getEmployeeId, employee.getId())
                    .last("LIMIT 1"));
            if (linkedUser != null) {
                linkedUser.setPhone(phone);
                sysUserMapper.updateById(linkedUser);
            }
        }
        return Result.success();
    }

    /**
     * 员工密码校验：BCrypt 为准，兼容存量明文与空密码默认123456
     * （与 AuthService.matchesEmployeePassword 口径一致，但校验通过后不落库升级密码）
     */
    private boolean matchesEmployeePassword(HrEmployee employee, String rawPassword) {
        String stored = employee.getPassword();
        if (stored == null || stored.isEmpty()) {
            return "123456".equals(rawPassword);
        }
        if (stored.startsWith("$2")) {
            return passwordEncoder.matches(rawPassword, stored);
        }
        return stored.equals(rawPassword);
    }
}
