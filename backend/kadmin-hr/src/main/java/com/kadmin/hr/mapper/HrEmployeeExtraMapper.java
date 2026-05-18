package com.kadmin.hr.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kadmin.hr.domain.HrEmployeeExtra;
import org.apache.ibatis.annotations.Mapper;

/**
 * 员工扩展信息Mapper
 */
@Mapper
public interface HrEmployeeExtraMapper extends BaseMapper<HrEmployeeExtra> {
}
