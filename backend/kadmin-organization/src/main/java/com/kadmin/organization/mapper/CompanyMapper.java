package com.kadmin.organization.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kadmin.organization.domain.OrgCompany;
import org.apache.ibatis.annotations.Mapper;

/**
 * 公司Mapper
 */
@Mapper
public interface CompanyMapper extends BaseMapper<OrgCompany> {
}