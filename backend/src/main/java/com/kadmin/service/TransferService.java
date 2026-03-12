package com.kadmin.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kadmin.entity.HrEmployee;
import com.kadmin.entity.HrTransfer;
import com.kadmin.mapper.EmployeeMapper;
import com.kadmin.mapper.HrTransferMapper;
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
            return false;
        }

        // 更新调动记录状态
        HrTransfer entity = new HrTransfer();
        entity.setId(id);
        entity.setStatus(status);
        entity.setApproveRemark(remark);
        entity.setApproveBy(approveBy);
        entity.setApproveTime(LocalDateTime.now());
        boolean result = updateById(entity);

        // 审批通过时，更新员工信息
        if (result && status == 1) {
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

        return result;
    }
}
