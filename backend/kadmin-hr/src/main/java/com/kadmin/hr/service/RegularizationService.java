package com.kadmin.hr.service;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.kadmin.hr.domain.HrEmployee;
import com.kadmin.hr.domain.HrRegularization;
import com.kadmin.hr.mapper.EmployeeMapper;
import com.kadmin.hr.mapper.HrRegularizationMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class RegularizationService extends ServiceImpl<HrRegularizationMapper, HrRegularization> {

    private final EmployeeMapper employeeMapper;

    public Page<HrRegularization> getPage(int pageNum, int pageSize, String employeeName, Integer status) {
        return baseMapper.selectPageWithEmployee(new Page<>(pageNum, pageSize), employeeName, status);
    }

    @Transactional
    public boolean approve(Long id, Integer status, String remark, Long approveBy) {
        HrRegularization regularization = getById(id);
        if (regularization == null) {
            throw new IllegalArgumentException("转正记录不存在");
        }
        if (status == null || (status != 1 && status != 2)) {
            throw new IllegalArgumentException("无效的审批操作");
        }

        // 条件更新（仅待审批状态可流转），防止并发重复审批
        LambdaUpdateWrapper<HrRegularization> wrapper = new LambdaUpdateWrapper<HrRegularization>()
                .eq(HrRegularization::getId, id)
                .set(HrRegularization::getStatus, status)
                .set(HrRegularization::getApproveRemark, remark)
                .set(HrRegularization::getApproveBy, approveBy)
                .set(HrRegularization::getApproveTime, LocalDateTime.now());
        if (regularization.getStatus() != null) {
            wrapper.eq(HrRegularization::getStatus, 0);
        }
        boolean result = update(wrapper);
        if (!result) {
            throw new IllegalArgumentException("该记录已被处理，请刷新后查看");
        }

        // 审批通过时，更新员工的类别和转正日期
        if (status == 1) {
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

        return true;
    }
}
