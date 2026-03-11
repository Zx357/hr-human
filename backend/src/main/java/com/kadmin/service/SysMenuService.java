package com.kadmin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kadmin.entity.SysMenu;
import com.kadmin.mapper.SysMenuMapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 菜单服务
 */
@Service
public class SysMenuService extends ServiceImpl<SysMenuMapper, SysMenu> {

    /**
     * 获取菜单树形结构（包含按钮，用于菜单管理）
     */
    public List<SysMenu> getMenuTree() {
        List<SysMenu> allMenus = baseMapper.selectAllMenusWithButtons();
        return buildTree(allMenus, 0L);
    }

    /**
     * 获取用户菜单树形结构（只返回菜单，不包含按钮）
     */
    public List<SysMenu> getUserMenuTree(Long userId) {
        List<SysMenu> userMenus = baseMapper.selectMenusByUserId(userId);
        if (userMenus.isEmpty()) {
            // 如果用户没有分配角色，返回所有菜单（超级管理员）
            userMenus = baseMapper.selectAllMenus();
        }
        return buildTree(userMenus, 0L);
    }

    /**
     * 获取用户按钮权限列表
     */
    public List<String> getUserPermissions(Long userId) {
        return baseMapper.selectPermissionsByUserId(userId);
    }

    /**
     * 获取用户路由（用于前端动态路由）
     */
    public List<Map<String, Object>> getUserRoutes(Long userId) {
        List<SysMenu> menuTree = getUserMenuTree(userId);
        return convertToRoutes(menuTree);
    }

    /**
     * 构建树形结构
     */
    private List<SysMenu> buildTree(List<SysMenu> menus, Long parentId) {
        List<SysMenu> tree = new ArrayList<>();
        for (SysMenu menu : menus) {
            if (parentId.equals(menu.getParentId())) {
                menu.setChildren(buildTree(menus, menu.getId()));
                tree.add(menu);
            }
        }
        return tree;
    }

    /**
     * 转换为前端路由格式
     */
    private List<Map<String, Object>> convertToRoutes(List<SysMenu> menus) {
        List<Map<String, Object>> routes = new ArrayList<>();
        for (SysMenu menu : menus) {
            if (menu.getMenuType() == 3) {
                // 按钮类型不生成路由
                continue;
            }

            Map<String, Object> route = new java.util.HashMap<>();
            route.put("name", menu.getMenuCode());
            route.put("path", menu.getPath());

            // 设置组件
            if (menu.getComponent() != null && !menu.getComponent().isEmpty()) {
                route.put("component", menu.getComponent());
            } else if (menu.getMenuType() == 1) {
                // 目录类型使用layout
                route.put("component", "layout.base");
            }

            // 设置meta
            Map<String, Object> meta = new java.util.HashMap<>();
            // 使用数据库中的菜单名称作为title
            meta.put("title", menu.getMenuName());
            // 添加英文标题
            if (menu.getMenuNameEn() != null && !menu.getMenuNameEn().isEmpty()) {
                meta.put("titleEn", menu.getMenuNameEn());
            }
            // 保留i18nKey作为备用
            meta.put("i18nKey", "route." + menu.getMenuCode());
            if (menu.getIcon() != null && !menu.getIcon().isEmpty()) {
                meta.put("icon", menu.getIcon());
            }
            meta.put("order", menu.getSortOrder());
            if (menu.getVisible() == 0) {
                meta.put("hideInMenu", true);
            }
            route.put("meta", meta);

            // 处理子菜单
            if (menu.getChildren() != null && !menu.getChildren().isEmpty()) {
                route.put("children", convertToRoutes(menu.getChildren()));
            }

            routes.add(route);
        }
        return routes;
    }

    /**
     * 新增菜单
     */
    public boolean addMenu(SysMenu menu) {
        return save(menu);
    }

    /**
     * 更新菜单
     */
    public boolean updateMenu(SysMenu menu) {
        return updateById(menu);
    }

    /**
     * 删除菜单（真删除）
     */
    public boolean deleteMenu(Long id) {
        return removeById(id);
    }

    /**
     * 检查是否有子菜单
     */
    public boolean hasChildren(Long id) {
        return count(new LambdaQueryWrapper<SysMenu>()
                .eq(SysMenu::getParentId, id)) > 0;
    }
}