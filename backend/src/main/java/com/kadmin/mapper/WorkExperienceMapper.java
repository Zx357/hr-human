package com.kadmin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kadmin.entity.HrWorkExperience;
import org.apache.ibatis.annotations.Mapper;

/**
 * 工作经历Mapper接口
 */
@Mapper
public interface WorkExperienceMapper extends BaseMapper<HrWorkExperience> {
}