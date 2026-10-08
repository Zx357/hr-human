package com.kadmin.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kadmin.system.domain.SysConfig;
import org.apache.ibatis.annotations.Mapper;

/**
 * 系统参数配置 Mapper
 */
@Mapper
public interface SysConfigMapper extends BaseMapper<SysConfig> {
}
