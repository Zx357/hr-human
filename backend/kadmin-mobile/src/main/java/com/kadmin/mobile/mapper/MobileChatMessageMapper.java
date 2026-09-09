package com.kadmin.mobile.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kadmin.mobile.domain.MobileChatMessage;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MobileChatMessageMapper extends BaseMapper<MobileChatMessage> {
}
