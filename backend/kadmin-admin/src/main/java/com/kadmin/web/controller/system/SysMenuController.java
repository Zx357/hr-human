package com.kadmin.web.controller.system;

import com.kadmin.common.Result;
import com.kadmin.common.annotation.RequiresPermission;
import com.kadmin.system.domain.SysMenu;
import com.kadmin.system.service.SysMenuService;
import com.kadmin.common.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 菜单管理控制器
 */
@RestController
@RequestMapping("/system/menu")
@RequiredArgsConstructor
public class SysMenuController {

    private final SysMenuService menuService;

    /**
     * 获取菜单树形列表（管理页面使用）
     */
    @GetMapping("/tree")
    public Result<List<SysMenu>> getMenuTree() {
        List<SysMenu> tree = menuService.getMenuTree();
        return Result.success(tree);
    }

    /**
     * 获取用户路由（动态路由使用）
     */
    @GetMapping("/routes")
    public Result<Map<String, Object>> getUserRoutes() {
        Long userId = SecurityUtils.getCurrentUserId();
        List<Map<String, Object>> routes = menuService.getUserRoutes(userId);

        Map<String, Object> data = new HashMap<>();
        data.put("routes", routes);
        data.put("home", "home");

        return Result.success(data);
    }

    /**
     * 获取菜单详情
     */
    @GetMapping("/{id}")
    public Result<SysMenu> getMenu(@PathVariable Long id) {
        SysMenu menu = menuService.getById(id);
        return Result.success(menu);
    }

    /**
     * 新增菜单
     */
    @RequiresPermission("system:menu:add")
    @PostMapping
    public Result<Boolean> addMenu(@RequestBody SysMenu menu) {
        boolean success = menuService.addMenu(menu);
        return success ? Result.success(true) : Result.error("新增失败");
    }

    /**
     * 更新菜单
     */
    @RequiresPermission("system:menu:edit")
    @PutMapping
    public Result<Boolean> updateMenu(@RequestBody SysMenu menu) {
        boolean success = menuService.updateMenu(menu);
        return success ? Result.success(true) : Result.error("更新失败");
    }

    /**
     * 删除菜单
     */
    @RequiresPermission("system:menu:delete")
    @DeleteMapping("/{id}")
    public Result<Boolean> deleteMenu(@PathVariable Long id) {
        // 检查是否有子菜单
        if (menuService.hasChildren(id)) {
            return Result.error("存在子菜单，无法删除");
        }
        boolean success = menuService.deleteMenu(id);
        return success ? Result.success(true) : Result.error("删除失败");
    }

    /**
     * 检查路由是否存在
     */
    @GetMapping("/isRouteExist")
    public Result<Boolean> isRouteExist(@RequestParam String routeName) {
        // 简单实现：检查菜单编码是否存在
        List<SysMenu> menus = menuService.getMenuTree();
        boolean exists = checkRouteExists(menus, routeName);
        return Result.success(exists);
    }

    private boolean checkRouteExists(List<SysMenu> menus, String routeName) {
        for (SysMenu menu : menus) {
            if (routeName.equals(menu.getMenuCode())) {
                return true;
            }
            if (menu.getChildren() != null && !menu.getChildren().isEmpty()) {
                if (checkRouteExists(menu.getChildren(), routeName)) {
                    return true;
                }
            }
        }
        return false;
    }
}
