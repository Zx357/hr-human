package com.kadmin.hr.service;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.kadmin.hr.domain.HrEmployee;
import com.kadmin.hr.domain.HrTransfer;
import com.kadmin.hr.mapper.EmployeeMapper;
import com.kadmin.hr.mapper.HrTransferMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class TransferService extends ServiceImpl<HrTransferMapper, HrTransfer> {

    private final EmployeeMapper employeeMapper;

    public Page<HrTransfer> getPage(int pageNum, int pageSize, String employeeName, String transferType,
            Integer status) {
        return baseMapper.selectPageWithEmployee(new Page<>(pageNum, pageSize), employeeName, transferType, status);
    }

    @Transactional
    public boolean approve(Long id, Integer status, String remark, Long approveBy) {
        HrTransfer transfer = getById(id);
        if (transfer == null) {
            throw new IllegalArgumentException("调动记录不存在");
        }
        if (status == null || (status != 1 && status != 2)) {
            throw new IllegalArgumentException("无效的审批操作");
        }

        // 条件更新（仅待审批状态可流转），防止并发重复审批
        LambdaUpdateWrapper<HrTransfer> wrapper = new LambdaUpdateWrapper<HrTransfer>()
                .eq(HrTransfer::getId, id)
                .set(HrTransfer::getStatus, status)
                .set(HrTransfer::getApproveRemark, remark)
                .set(HrTransfer::getApproveBy, approveBy)
                .set(HrTransfer::getApproveTime, LocalDateTime.now());
        if (transfer.getStatus() != null) {
            wrapper.eq(HrTransfer::getStatus, 0);
        }
        boolean result = update(wrapper);
        if (!result) {
            throw new IllegalArgumentException("该记录已被处理，请刷新后查看");
        }

        // 审批通过时，更新员工信息
        if (status == 1) {
            HrEmployee employee = new HrEmployee();
            employee.setId(transfer.getEmployeeId());
            // 更新部门（公司通过dept_id向上查找获取）
            if (transfer.getToDeptId() != null) {
                employee.setDeptId(transfer.getToDeptId());
            }
            // 更新职位
            if (transfer.getToPosition() != null && !transfer.getToPosition().isEmpty()) {
                employee.setPosition(transfer.getToPosition());
            }
            employeeMapper.updateById(employee);
        }

        return true;
    }
}
