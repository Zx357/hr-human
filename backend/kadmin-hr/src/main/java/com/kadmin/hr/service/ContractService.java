package com.kadmin.hr.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kadmin.hr.domain.HrContract;
import com.kadmin.hr.mapper.HrContractMapper;
import org.springframework.stereotype.Service;

/**
 * 合同服务
 */
@Service
public class ContractService extends ServiceImpl<HrContractMapper, HrContract> {

    /**
     * 分页查询合同
     */
    public Page<HrContract> getContractPage(int pageNum, int pageSize, String contractNo,
            String employeeName, String employeeNo, String contractType, Integer status, Long employeeId) {
        Page<HrContract> page = new Page<>(pageNum, pageSize);
        return baseMapper.selectPageWithEmployee(page, contractNo, employeeName, employeeNo, contractType, status, employeeId);
    }

    /**
     * 新增合同
     */
    public boolean addContract(HrContract contract) {
        if (contract.getStatus() == null) {
            contract.setStatus(1); // 默认生效中
        }
        return save(contract);
    }

    /**
     * 更新合同
     */
    public boolean updateContract(HrContract contract) {
        return updateById(contract);
    }

    /**
     * 删除合同（逻辑删除）
     */
    public boolean deleteContract(Long id) {
        return removeById(id);
    }
}
