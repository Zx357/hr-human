package com.kadmin.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kadmin.entity.HrEmployee;
import com.kadmin.entity.HrResignation;
import com.kadmin.mapper.EmployeeMapper;
import com.kadmin.mapper.HrResignationMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Service
public class ResignationService extends ServiceImpl<HrResignationMapper, HrResignation> {

    @Autowired
    private EmployeeMapper employeeMapper;

    public Page<HrResignation> getPage(int pageNum, int pageSize, String employeeName, String resignType, Integer status) {
        return baseMapper.selectPageWithEmployee(new Page<>(pageNum, pageSize), employeeName, resignType, status);
    }

    @Transactional
    public boolean approve(Long id, Integer status, String remark, Long approveBy) {
        HrResignation resignation = getById(id);
        if (resignation == null) {
            return false;
        }
        
        // 更新离职记录状态
        HrResignation entity = new HrResignation();
        entity.setId(id);
        entity.setStatus(status);
        entity.setApproveRemark(remark);
        entity.setApproveBy(approveBy);
        entity.setApproveTime(LocalDateTime.now());
        boolean result = updateById(entity);
        
        // 审批通过时，更新员工状态为离职
        if (result && status == 1) {
            HrEmployee employee = new HrEmployee();
            employee.setId(resignation.getEmployeeId());
            employee.setStatus(2); // 2-离职
            employee.setLeaveDate(resignation.getLastWorkDate()); // 设置离职日期
            employeeMapper.updateById(employee);
        }
        
        return result;
    }
}
