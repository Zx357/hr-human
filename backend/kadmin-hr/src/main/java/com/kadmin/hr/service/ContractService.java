package com.kadmin.hr.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.kadmin.hr.domain.HrContract;
import com.kadmin.hr.mapper.HrContractMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * 合同服务
 */
@Service
public class ContractService extends ServiceImpl<HrContractMapper, HrContract> {

    /**
     * 合同状态：已续签
     */
    private static final int STATUS_RENEWED = 4;

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
     * 删除合同（HrContract 未启用 @TableLogic，此处为物理删除，历史考勤/申请不受影响）
     */
    public boolean deleteContract(Long id) {
        return removeById(id);
    }

    /**
     * 合同续签：旧合同置为"已续签"，生成关联的新合同（次数+1），形成"到期提醒→办理"闭环。
     * 入参 entity 以 id 标识旧合同，可携带新合同的字段（日期/薪资/类型等，缺省沿用旧合同）。
     *
     * @return 新建的合同
     */
    @Transactional
    public HrContract renewContract(HrContract entity) {
        if (entity == null || entity.getId() == null) {
            throw new IllegalArgumentException("缺少要续签的合同");
        }
        HrContract old = getById(entity.getId());
        if (old == null) {
            throw new IllegalArgumentException("合同不存在");
        }
        if (old.getStatus() != null && old.getStatus() == STATUS_RENEWED) {
            throw new IllegalArgumentException("该合同已续签，请勿重复操作");
        }
        if (entity.getEndDate() != null && entity.getStartDate() != null
                && entity.getEndDate().isBefore(entity.getStartDate())) {
            throw new IllegalArgumentException("结束日期不能早于开始日期");
        }

        int nextCount = (old.getContractCount() != null && old.getContractCount() > 0)
                ? old.getContractCount() + 1
                : 2;

        HrContract renewed = new HrContract();
        renewed.setEmployeeId(old.getEmployeeId());
        renewed.setContractNo(StringUtils.hasText(entity.getContractNo())
                ? entity.getContractNo()
                : old.getContractNo() + "-" + nextCount);
        renewed.setContractType(StringUtils.hasText(entity.getContractType())
                ? entity.getContractType() : old.getContractType());
        renewed.setStartDate(entity.getStartDate() != null ? entity.getStartDate() : old.getStartDate());
        renewed.setEndDate(entity.getEndDate());
        renewed.setSignDate(entity.getSignDate());
        renewed.setProbationMonths(entity.getProbationMonths());
        renewed.setSalary(entity.getSalary() != null ? entity.getSalary() : old.getSalary());
        renewed.setStatus(1);
        renewed.setRemark(StringUtils.hasText(entity.getRemark())
                ? entity.getRemark()
                : "由合同 " + old.getContractNo() + " 续签");
        renewed.setContractImages(old.getContractImages());
        renewed.setContractCount(nextCount);
        save(renewed);

        // 旧合同置为已续签（条件更新防并发重复续签）
        boolean updated = update(new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<HrContract>()
                .eq(HrContract::getId, old.getId())
                .ne(HrContract::getStatus, STATUS_RENEWED)
                .set(HrContract::getStatus, STATUS_RENEWED));
        if (!updated) {
            throw new IllegalArgumentException("该合同已被他人续签，请刷新后查看");
        }
        return renewed;
    }
}
