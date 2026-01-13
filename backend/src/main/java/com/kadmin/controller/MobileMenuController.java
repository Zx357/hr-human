package com.kadmin.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kadmin.common.Result;
import com.kadmin.entity.MobileMenu;
import com.kadmin.service.MobileMenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 移动端菜单管理
 */
@RestController
@RequestMapping("/system/mobile-menu")
@RequiredArgsConstructor
public class MobileMenuController {

    private final MobileMenuService mobileMenuService;

    /**
     * 分页查询（管理端）
     */
    @GetMapping("/page")
    public Result<Map<String, Object>> page(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String menuName,
            @RequestParam(required = false) Integer status) {
        Page<MobileMenu> page = mobileMenuService.page(pageNum, pageSize, menuName, status);
        Map<String, Object> result = new HashMap<>();
        result.put("records", page.getRecords());
        result.put("total", page.getTotal());
        return Result.success(result);
    }

    /**
     * 查询所有菜单（管理端）
     */
    @GetMapping("/list")
    public Result<List<MobileMenu>> list() {
        return Result.success(mobileMenuService.listEnabled());
    }

    /**
     * 获取详情
     */
    @GetMapping("/{id}")
    public Result<MobileMenu> getById(@PathVariable Long id) {
        return Result.success(mobileMenuService.getById(id));
    }

    /**
     * 新增
     */
    @PostMapping
    @PreAuthorize("hasAuthority('system:mobile-menu:add')")
    public Result<Void> create(@RequestBody MobileMenu menu) {
        mobileMenuService.create(menu);
        return Result.success();
    }

    /**
     * 更新
     */
    @PutMapping
    @PreAuthorize("hasAuthority('system:mobile-menu:edit')")
    public Result<Void> update(@RequestBody MobileMenu menu) {
        mobileMenuService.update(menu);
        return Result.success();
    }

    /**
     * 删除
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('system:mobile-menu:delete')")
    public Result<Void> delete(@PathVariable Long id) {
        mobileMenuService.delete(id);
        return Result.success();
    }

    /**
     * 切换状态
     */
    @PutMapping("/toggle-status/{id}")
    @PreAuthorize("hasAuthority('system:mobile-menu:edit')")
    public Result<Void> toggleStatus(@PathVariable Long id) {
        mobileMenuService.toggleStatus(id);
        return Result.success();
    }

    /**
     * 获取移动端菜单（移动端调用，无需权限）
     */
    @GetMapping("/mobile/list")
    public Result<Map<String, List<MobileMenu>>> getMobileMenus() {
        List<MobileMenu> menus = mobileMenuService.listEnabled();
        // 按分组返回
        Map<String, List<MobileMenu>> grouped = menus.stream()
                .collect(Collectors.groupingBy(MobileMenu::getMenuGroup));
        return Result.success(grouped);
    }
}
