package com.kadmin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kadmin.entity.SysRole;
import com.kadmin.mapper.SysRoleMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 角色服务
 */
@Slf4j
@Service
public class SysRoleService extends ServiceImpl<SysRoleMapper, SysRole> {

    /**
     * 分页查询角色
     */
    public IPage<SysRole> pageRoles(Page<SysRole> page, String roleName, String roleCode, Integer status) {
        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(roleName)) {
            wrapper.like(SysRole::getRoleName, roleName);
        }
        if (StringUtils.hasText(roleCode)) {
            wrapper.like(SysRole::getRoleCode, roleCode);
        }
        if (status != null) {
            wrapper.eq(SysRole::getStatus, status);
        }
        wrapper.orderByAsc(SysRole::getSortOrder);
        return page(page, wrapper);
    }

    /**
     * 获取所有启用的角色
     */
    public List<SysRole> getAllEnabledRoles() {
        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysRole::getStatus, 1);
        wrapper.orderByAsc(SysRole::getSortOrder);
        return list(wrapper);
    }

    /**
     * 根据角色编码查询角色
     */
    public SysRole getByRoleCode(String roleCode) {
        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysRole::getRoleCode, roleCode);
        return getOne(wrapper);
    }

    /**
     * 获取角色菜单ID列表
     */
    public List<Long> getRoleMenuIds(Long roleId) {
        return baseMapper.selectMenuIdsByRoleId(roleId);
    }

    /**
     * 新增角色
     */
    @Transactional
    public boolean addRole(SysRole role, List<Long> menuIds) {
        // 检查角色编码是否已存在
        SysRole existRole = getByRoleCode(role.getRoleCode());
        if (existRole != null) {
            throw new RuntimeException("角色编码已存在");
        }

        boolean success = save(role);

        if (success && menuIds != null && !menuIds.isEmpty()) {
            // 分配菜单权限
            baseMapper.insertRoleMenuBatch(role.getId(), menuIds);
        }

        return success;
    }

    /**
     * 更新角色
     */
    @Transactional
    public boolean updateRole(SysRole role, List<Long> menuIds) {
        log.info("更新角色: roleId={}, menuIds数量={}", role.getId(), menuIds != null ? menuIds.size() : 0);

        boolean success = updateById(role);
        log.info("更新角色基本信息结果: success={}", success);

        if (success && menuIds != null) {
            // 重新分配菜单权限
            int deleted = baseMapper.deleteRoleMenuByRoleId(role.getId());
            log.info("删除旧的角色菜单关联: deleted={}", deleted);

            if (!menuIds.isEmpty()) {
                int inserted = baseMapper.insertRoleMenuBatch(role.getId(), menuIds);
                log.info("插入新的角色菜单关联: inserted={}, menuIds={}", inserted, menuIds);
            }
        }

        return success;
    }

    /**
     * 删除角色（逻辑删除）
     */
    @Transactional
    public boolean deleteRole(Long id) {
        // 保护管理员角色不被删除
        SysRole role = getById(id);
        if (role != null && "ROLE_ADMIN".equals(role.getRoleCode())) {
            throw new RuntimeException("管理员角色不能删除");
        }

        // 删除角色菜单关联
        baseMapper.deleteRoleMenuByRoleId(id);

        return removeById(id);
    }

    /**
     * 修改状态
     */
    public boolean changeStatus(Long id, Integer status) {
        SysRole role = new SysRole();
        role.setId(id);
        role.setStatus(status);
        return updateById(role);
    }
}