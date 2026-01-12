package com.kadmin.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kadmin.entity.HrEmployee;
import com.kadmin.entity.HrRegularization;
import com.kadmin.mapper.EmployeeMapper;
import com.kadmin.mapper.HrRegularizationMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class RegularizationService extends ServiceImpl<HrRegularizationMapper, HrRegularization> {

    @Autowired
    private EmployeeMapper employeeMapper;

    public Page<HrRegularization> getPage(int pageNum, int pageSize, String employeeName, Integer status) {
        return baseMapper.selectPageWithEmployee(new Page<>(pageNum, pageSize), employeeName, status);
    }

    @Transactional
    public boolean approve(Long id, Integer status, String remark, Long approveBy) {
        HrRegularization regularization = getById(id);
        if (regularization == null) {
            return false;
        }

        // 更新转正记录状态
        HrRegularization entity = new HrRegularization();
        entity.setId(id);
        entity.setStatus(status);
        entity.setApproveRemark(remark);
        entity.setApproveBy(approveBy);
        entity.setApproveTime(LocalDateTime.now());
        boolean result = updateById(entity);

        // 审批通过时，更新员工的类别和转正日期
        if (result && status == 1) {
            HrEmployee employee = new HrEmployee();
            employee.setId(regularization.getEmployeeId());
            // 更新员工类别
            if (regularization.getNewEmployeeType() != null && !regularization.getNewEmployeeType().isEmpty()) {
                employee.setEmployeeType(regularization.getNewEmployeeType());
            }
            // 更新转正日期
            if (regularization.getRegularDate() != null) {
                employee.setRegularDate(regularization.getRegularDate());
            }
            employeeMapper.updateById(employee);
        }

        return result;
    }
}
