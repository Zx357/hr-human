package com.kadmin.mobile.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kadmin.mobile.domain.MobileMenu;
import com.kadmin.mobile.mapper.MobileMenuMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 移动端菜单服务
 */
@Service
@RequiredArgsConstructor
public class MobileMenuService {

    private final MobileMenuMapper mobileMenuMapper;

    /**
     * 分页查询
     */
    public Page<MobileMenu> page(int pageNum, int pageSize, String menuName, Integer status) {
        LambdaQueryWrapper<MobileMenu> wrapper = new LambdaQueryWrapper<>();
        if (menuName != null && !menuName.isEmpty()) {
            wrapper.like(MobileMenu::getMenuName, menuName);
        }
        if (status != null) {
            wrapper.eq(MobileMenu::getStatus, status);
        }
        wrapper.orderByAsc(MobileMenu::getMenuGroup, MobileMenu::getSortOrder);
        return mobileMenuMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
    }

    /**
     * 查询所有启用的菜单（移动端使用）
     */
    public List<MobileMenu> listEnabled() {
        LambdaQueryWrapper<MobileMenu> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MobileMenu::getStatus, 1);
        wrapper.orderByAsc(MobileMenu::getMenuGroup, MobileMenu::getSortOrder);
        return mobileMenuMapper.selectList(wrapper);
    }

    /**
     * 根据ID查询
     */
    public MobileMenu getById(Long id) {
        return mobileMenuMapper.selectById(id);
    }

    /**
     * 新增
     */
    public void create(MobileMenu menu) {
        mobileMenuMapper.insert(menu);
    }

    /**
     * 更新
     */
    public void update(MobileMenu menu) {
        mobileMenuMapper.updateById(menu);
    }

    /**
     * 删除
     */
    public void delete(Long id) {
        mobileMenuMapper.deleteById(id);
    }

    /**
     * 切换状态
     */
    public void toggleStatus(Long id) {
        MobileMenu menu = mobileMenuMapper.selectById(id);
        if (menu != null) {
            menu.setStatus(menu.getStatus() == 1 ? 0 : 1);
            mobileMenuMapper.updateById(menu);
        }
    }
}
