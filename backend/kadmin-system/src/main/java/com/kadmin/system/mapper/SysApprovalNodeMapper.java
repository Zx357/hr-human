package com.kadmin.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kadmin.system.domain.SysApprovalNode;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface SysApprovalNodeMapper extends BaseMapper<SysApprovalNode> {
    List<SysApprovalNode> selectByFlowIdWithApprover(@Param("flowId") Long flowId);
}
