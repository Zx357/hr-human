package com.kadmin.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.kadmin.entity.SysUser;
import com.kadmin.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * 数据初始化器
 * 确保管理员账户密码正确
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final SysUserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // 检查admin用户是否存在
        SysUser admin = userMapper.selectByUsername("admin");

        if (admin == null) {
            // 创建admin用户
            log.info("Creating admin user...");
            admin = new SysUser();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setNickname("系统管理员");
            admin.setStatus(1);
            userMapper.insert(admin);
            log.info("Admin user created successfully. Username: admin, Password: admin123");
        } else {
            // 检查密码是否正确，如果不正确则更新
            if (!passwordEncoder.matches("admin123", admin.getPassword())) {
                log.info("Updating admin password...");
                admin.setPassword(passwordEncoder.encode("admin123"));
                userMapper.updateById(admin);
                log.info("Admin password updated successfully. Password: admin123");
            } else {
                log.info("Admin user already exists with correct password.");
            }
        }

        // 打印登录信息
        log.info("===========================================");
        log.info("Default admin credentials:");
        log.info("Username: admin");
        log.info("Password: admin123");
        log.info("===========================================");
    }
}