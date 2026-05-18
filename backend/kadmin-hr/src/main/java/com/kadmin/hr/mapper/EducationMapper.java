package com.kadmin.hr.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kadmin.hr.domain.HrEducation;
import org.apache.ibatis.annotations.Mapper;

/**
 * 教育经历Mapper接口
 */
@Mapper
public interface EducationMapper extends BaseMapper<HrEducation> {
}