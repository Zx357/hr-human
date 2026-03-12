package com.kadmin.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kadmin.common.Result;
import com.kadmin.entity.SysRole;
import com.kadmin.service.SysRoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 角色管理控制器
 */
@Slf4j
@Tag(name = "角色管理")
@RestController
@RequestMapping("/system/role")
@RequiredArgsConstructor
public class SysRoleController {

    private final SysRoleService roleService;

    /**
     * 分页查询角色列表
     */
    @Operation(summary = "分页查询角色列表")
    @GetMapping("/page")
    public Result<Map<String, Object>> pageRoles(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String roleName,
            @RequestParam(required = false) String roleCode,
            @RequestParam(required = false) Integer status) {

        Page<SysRole> page = new Page<>(current, size);
        IPage<SysRole> result = roleService.pageRoles(page, roleName, roleCode, status);

        Map<String, Object> data = new HashMap<>();
        data.put("records", result.getRecords());
        data.put("total", result.getTotal());
        data.put("current", result.getCurrent());
        data.put("size", result.getSize());

        return Result.success(data);
    }

    /**
     * 获取所有启用的角色（用于下拉选择）
     */
    @Operation(summary = "获取所有启用的角色")
    @GetMapping("/list")
    public Result<List<SysRole>> listRoles() {
        List<SysRole> roles = roleService.getAllEnabledRoles();
        return Result.success(roles);
    }

    /**
     * 获取角色详情
     */
    @Operation(summary = "获取角色详情")
    @GetMapping("/{id}")
    public Result<Map<String, Object>> getRole(@PathVariable Long id) {
        SysRole role = roleService.getById(id);
        if (role == null) {
            return Result.error("角色不存在");
        }

        // 获取角色菜单权限
        List<Long> menuIds = roleService.getRoleMenuIds(id);

        Map<String, Object> data = new HashMap<>();
        data.put("role", role);
        data.put("menuIds", menuIds);

        return Result.success(data);
    }

    /**
     * 新增角色
     */
    @Operation(summary = "新增角色")
    @PostMapping
    public Result<Boolean> addRole(@RequestBody RoleRequest request) {
        try {
            SysRole role = new SysRole();
            role.setRoleCode(request.getRoleCode());
            role.setRoleName(request.getRoleName());
            role.setDescription(request.getDescription());
            role.setStatus(request.getStatus() != null ? request.getStatus() : 1);
            role.setSortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0);
            role.setDataScope(request.getDataScope() != null ? request.getDataScope() : 1);

            boolean success = roleService.addRole(role, request.getMenuIds());
            return success ? Result.success(true) : Result.error("新增失败");
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 更新角色
     */
    @Operation(summary = "更新角色")
    @PutMapping
    public Result<Boolean> updateRole(@RequestBody RoleRequest request) {
        try {
            log.info("更新角色请求: id={}, roleCode={}, roleName={}, menuIds={}",
                    request.getId(), request.getRoleCode(), request.getRoleName(), request.getMenuIds());

            SysRole role = new SysRole();
            role.setId(request.getId());
            role.setRoleCode(request.getRoleCode());
            role.setRoleName(request.getRoleName());
            role.setDescription(request.getDescription());
            role.setStatus(request.getStatus());
            role.setSortOrder(request.getSortOrder());
            role.setDataScope(request.getDataScope());

            boolean success = roleService.updateRole(role, request.getMenuIds());
            log.info("更新角色结果: success={}", success);
            return success ? Result.success(true) : Result.error("更新失败");
        } catch (RuntimeException e) {
            log.error("更新角色失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 删除角色
     */
    @Operation(summary = "删除角色")
    @DeleteMapping("/{id}")
    public Result<Boolean> deleteRole(@PathVariable Long id) {
        boolean success = roleService.deleteRole(id);
        return success ? Result.success(true) : Result.error("删除失败");
    }

    /**
     * 修改状态
     */
    @Operation(summary = "修改角色状态")
    @PutMapping("/{id}/status")
    public Result<Boolean> changeStatus(@PathVariable Long id, @RequestBody StatusRequest request) {
        boolean success = roleService.changeStatus(id, request.getStatus());
        return success ? Result.success(true) : Result.error("修改状态失败");
    }

    /**
     * 角色请求DTO
     */
    @Data
    public static class RoleRequest {
        private Long id;
        private String roleCode;
        private String roleName;
        private String description;
        private Integer status;
        private Integer sortOrder;
        private Integer dataScope;
        private List<Long> menuIds;
    }

    /**
     * 状态请求DTO
     */
    @Data
    public static class StatusRequest {
        private Integer status;
    }
}
