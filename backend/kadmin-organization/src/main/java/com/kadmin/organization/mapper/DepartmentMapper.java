package com.kadmin.organization.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kadmin.organization.domain.OrgDepartment;
import org.apache.ibatis.annotations.Mapper;

/**
 * 部门Mapper
 */
@Mapper
public interface DepartmentMapper extends BaseMapper<OrgDepartment> {
}