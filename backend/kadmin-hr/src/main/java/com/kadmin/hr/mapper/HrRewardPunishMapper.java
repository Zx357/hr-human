package com.kadmin.hr.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kadmin.hr.domain.HrRewardPunish;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface HrRewardPunishMapper extends BaseMapper<HrRewardPunish> {
    Page<HrRewardPunish> selectPageWithEmployee(Page<HrRewardPunish> page,
            @Param("employeeName") String employeeName, @Param("type") Integer type,
            @Param("status") Integer status);
}
