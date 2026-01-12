package com.kadmin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kadmin.entity.OrgCompany;
import org.apache.ibatis.annotations.Mapper;

/**
 * 公司Mapper
 */
@Mapper
public interface CompanyMapper extends BaseMapper<OrgCompany> {
}