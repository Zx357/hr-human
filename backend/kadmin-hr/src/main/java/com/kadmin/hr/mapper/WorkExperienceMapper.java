package com.kadmin.hr.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kadmin.hr.domain.HrWorkExperience;
import org.apache.ibatis.annotations.Mapper;

/**
 * 工作经历Mapper接口
 */
@Mapper
public interface WorkExperienceMapper extends BaseMapper<HrWorkExperience> {
}