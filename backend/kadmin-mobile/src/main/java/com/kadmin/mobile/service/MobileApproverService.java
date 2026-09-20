package com.kadmin.mobile.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.kadmin.hr.domain.HrMobileApprover;
import com.kadmin.hr.mapper.HrMobileApproverMapper;
import org.springframework.stereotype.Service;

@Service
public class MobileApproverService extends ServiceImpl<HrMobileApproverMapper, HrMobileApprover> {

    public Page<HrMobileApprover> getPage(int pageNum, int pageSize, String employeeName, String employeeNo) {
        return baseMapper.selectPageWithEmployee(new Page<>(pageNum, pageSize), employeeName, employeeNo);
    }
}
