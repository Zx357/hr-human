package com.kadmin.hr.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kadmin.hr.domain.HrFamilyMember;
import org.apache.ibatis.annotations.Mapper;

/**
 * 家庭成员Mapper
 */
@Mapper
public interface HrFamilyMemberMapper extends BaseMapper<HrFamilyMember> {
}