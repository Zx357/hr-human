package com.kadmin.hr.service;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.kadmin.hr.domain.HrEmployee;
import com.kadmin.hr.domain.HrResignation;
import com.kadmin.hr.mapper.EmployeeMapper;
import com.kadmin.hr.mapper.HrResignationMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ResignationService extends ServiceImpl<HrResignationMapper, HrResignation> {

    private final EmployeeMapper employeeMapper;

    public Page<HrResignation> getPage(int pageNum, int pageSize, String employeeName, String resignType,
            Integer status) {
        return baseMapper.selectPageWithEmployee(new Page<>(pageNum, pageSize), employeeName, resignType, status);
    }

    @Transactional
    public boolean approve(Long id, Integer status, String remark, Long approveBy) {
        HrResignation resignation = getById(id);
        if (resignation == null) {
            throw new IllegalArgumentException("离职记录不存在");
        }
        if (status == null || (status != 1 && status != 2)) {
            throw new IllegalArgumentException("无效的审批操作");
        }

        // 条件更新（仅待审批状态可流转），防止并发重复审批
        LambdaUpdateWrapper<HrResignation> wrapper = new LambdaUpdateWrapper<HrResignation>()
                .eq(HrResignation::getId, id)
                .set(HrResignation::getStatus, status)
                .set(HrResignation::getApproveRemark, remark)
                .set(HrResignation::getApproveBy, approveBy)
                .set(HrResignation::getApproveTime, LocalDateTime.now());
        if (resignation.getStatus() != null) {
            wrapper.eq(HrResignation::getStatus, 0);
        }
        boolean result = update(wrapper);
        if (!result) {
            throw new IllegalArgumentException("该记录已被处理，请刷新后查看");
        }

        // 审批通过时，更新员工状态为离职
        if (status == 1) {
            HrEmployee employee = new HrEmployee();
            employee.setId(resignation.getEmployeeId());
            employee.setStatus(2); // 2-离职
            employee.setLeaveDate(resignation.getLastWorkDate()); // 设置离职日期
            employeeMapper.updateById(employee);
        }

        return true;
    }
}
