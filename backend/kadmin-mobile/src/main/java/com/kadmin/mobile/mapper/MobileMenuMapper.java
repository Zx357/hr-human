package com.kadmin.mobile.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kadmin.mobile.domain.MobileMenu;
import org.apache.ibatis.annotations.Mapper;

/**
 * 移动端菜单Mapper
 */
@Mapper
public interface MobileMenuMapper extends BaseMapper<MobileMenu> {
}
