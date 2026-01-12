package com.kadmin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kadmin.entity.HrEducation;
import org.apache.ibatis.annotations.Mapper;

/**
 * 教育经历Mapper接口
 */
@Mapper
public interface EducationMapper extends BaseMapper<HrEducation> {
}