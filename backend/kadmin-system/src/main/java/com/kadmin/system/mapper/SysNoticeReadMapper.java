package com.kadmin.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kadmin.system.domain.SysNoticeRead;
import org.apache.ibatis.annotations.Mapper;

/**
 * 公告已读记录Mapper
 */
@Mapper
public interface SysNoticeReadMapper extends BaseMapper<SysNoticeRead> {
}
