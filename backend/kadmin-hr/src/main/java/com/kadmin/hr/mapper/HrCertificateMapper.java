package com.kadmin.hr.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kadmin.hr.domain.HrCertificate;
import org.apache.ibatis.annotations.Mapper;

/**
 * 证书信息Mapper
 */
@Mapper
public interface HrCertificateMapper extends BaseMapper<HrCertificate> {
}