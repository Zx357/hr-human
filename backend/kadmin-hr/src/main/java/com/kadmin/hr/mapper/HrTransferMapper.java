package com.kadmin.hr.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kadmin.hr.domain.HrTransfer;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface HrTransferMapper extends BaseMapper<HrTransfer> {
    Page<HrTransfer> selectPageWithEmployee(Page<HrTransfer> page,
            @Param("employeeName") String employeeName, @Param("transferType") String transferType,
            @Param("status") Integer status);
}
