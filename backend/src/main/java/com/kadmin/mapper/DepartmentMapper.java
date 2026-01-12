package com.kadmin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kadmin.entity.OrgDepartment;
import org.apache.ibatis.annotations.Mapper;

/**
 * 部门Mapper
 */
@Mapper
public interface DepartmentMapper extends BaseMapper<OrgDepartment> {
}