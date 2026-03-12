package com.kadmin.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kadmin.entity.HrMobileApprover;
import com.kadmin.mapper.HrMobileApproverMapper;
import org.springframework.stereotype.Service;

@Service
public class MobileApproverService extends ServiceImpl<HrMobileApproverMapper, HrMobileApprover> {

    public Page<HrMobileApprover> getPage(int pageNum, int pageSize, String employeeName, String employeeNo) {
        return baseMapper.selectPageWithEmployee(new Page<>(pageNum, pageSize), employeeName, employeeNo);
    }
}
