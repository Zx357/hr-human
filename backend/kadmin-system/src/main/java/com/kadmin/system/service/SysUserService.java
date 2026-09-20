package com.kadmin.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kadmin.common.event.UserSessionEvictEvent;
import com.kadmin.system.domain.SysRole;
import com.kadmin.system.domain.SysUser;
import com.kadmin.system.mapper.SysRoleMapper;
import com.kadmin.system.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Set;

/**
 * 用户服务
 */
@Service
@RequiredArgsConstructor
public class SysUserService extends ServiceImpl<SysUserMapper, SysUser> {

    private final PasswordEncoder passwordEncoder;
    private final SysRoleMapper roleMapper;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * 获取用户列表（不分页）
     */
    public List<SysUser> listUsers(String username, String realName, Integer status) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(username)) {
            wrapper.like(SysUser::getUsername, username);
        }
        if (StringUtils.hasText(realName)) {
            wrapper.like(SysUser::getNickname, realName);
        }
        if (status != null) {
            wrapper.eq(SysUser::getStatus, status);
        }
        wrapper.orderByAsc(SysUser::getId);
        return list(wrapper);
    }

    /**
     * 分页查询用户
     */
    public IPage<SysUser> pageUsers(Page<SysUser> page, String username, String nickname, Integer status) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(username)) {
            wrapper.like(SysUser::getUsername, username);
        }
        if (StringUtils.hasText(nickname)) {
            wrapper.like(SysUser::getNickname, nickname);
        }
        if (status != null) {
            wrapper.eq(SysUser::getStatus, status);
        }
        wrapper.orderByDesc(SysUser::getCreatedTime);
        return page(page, wrapper);
    }

    /**
     * 根据用户名查询用户
     */
    public SysUser getByUsername(String username) {
        return baseMapper.selectByUsername(username);
    }

    /**
     * 获取用户角色编码列表
     */
    public Set<String> getUserRoleCodes(Long userId) {
        return baseMapper.selectRoleCodesByUserId(userId);
    }

    /**
     * 获取用户权限列表
     */
    public Set<String> getUserPermissions(Long userId) {
        return baseMapper.selectPermissionsByUserId(userId);
    }

    /**
     * 获取用户角色列表
     */
    public List<SysRole> getUserRoles(Long userId) {
        return roleMapper.selectRolesByUserId(userId);
    }

    /**
     * 获取用户角色ID列表
     */
    public List<Long> getUserRoleIds(Long userId) {
        return baseMapper.selectRoleIdsByUserId(userId);
    }

    /**
     * 新增用户
     */
    @Transactional
    public boolean addUser(SysUser user, List<Long> roleIds) {
        // 检查用户名是否已存在
        SysUser existUser = getByUsername(user.getUsername());
        if (existUser != null) {
            throw new RuntimeException("用户名已存在");
        }

        // 加密密码
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        boolean success = save(user);

        if (success && roleIds != null && !roleIds.isEmpty()) {
            // 分配角色
            baseMapper.insertUserRoleBatch(user.getId(), roleIds);
        }

        return success;
    }

    /**
     * 更新用户
     */
    @Transactional
    public boolean updateUser(SysUser user, List<Long> roleIds) {
        // 如果密码不为空，则加密
        boolean passwordChanged = StringUtils.hasText(user.getPassword());
        if (passwordChanged) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        } else {
            user.setPassword(null); // 不更新密码
        }

        boolean success = updateById(user);

        if (success && roleIds != null) {
            // 重新分配角色
            baseMapper.deleteUserRoleByUserId(user.getId());
            if (!roleIds.isEmpty()) {
                baseMapper.insertUserRoleBatch(user.getId(), roleIds);
            }
        }

        // 密码或角色变更后吊销会话，强制重新登录（角色权限随会话缓存）
        if (success && (passwordChanged || roleIds != null)) {
            eventPublisher.publishEvent(new UserSessionEvictEvent(user.getId()));
        }

        return success;
    }

    /**
     * 删除用户（逻辑删除）
     */
    @Transactional
    public boolean deleteUser(Long id) {
        // 保护管理员用户不被删除
        SysUser user = getById(id);
        if (user != null && "admin".equals(user.getUsername())) {
            throw new RuntimeException("管理员用户不能删除");
        }

        // 删除用户角色关联
        baseMapper.deleteUserRoleByUserId(id);

        // 使用 MyBatis Plus 的 removeById，会自动处理逻辑删除
        boolean success = removeById(id);
        if (success) {
            eventPublisher.publishEvent(new UserSessionEvictEvent(id));
        }
        return success;
    }

    /**
     * 重置密码
     */
    public boolean resetPassword(Long id, String newPassword) {
        SysUser user = new SysUser();
        user.setId(id);
        user.setPassword(passwordEncoder.encode(newPassword));
        boolean success = updateById(user);
        if (success) {
            eventPublisher.publishEvent(new UserSessionEvictEvent(id));
        }
        return success;
    }

    /**
     * 修改状态
     */
    public boolean changeStatus(Long id, Integer status) {
        SysUser user = new SysUser();
        user.setId(id);
        user.setStatus(status);
        boolean success = updateById(user);
        // 禁用用户立即失去访问权限
        if (success && status != null && status != 1) {
            eventPublisher.publishEvent(new UserSessionEvictEvent(id));
        }
        return success;
    }
}
