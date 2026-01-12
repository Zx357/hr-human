package com.kadmin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kadmin.entity.HrCertificate;
import org.apache.ibatis.annotations.Mapper;

/**
 * 证书Mapper接口
 */
@Mapper
public interface CertificateMapper extends BaseMapper<HrCertificate> {
}