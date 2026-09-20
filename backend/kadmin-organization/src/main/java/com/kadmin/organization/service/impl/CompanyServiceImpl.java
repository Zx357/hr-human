package com.kadmin.organization.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.kadmin.organization.domain.OrgCompany;
import com.kadmin.organization.domain.OrgDepartment;
import com.kadmin.organization.domain.OrgUnit;
import com.kadmin.organization.mapper.CompanyMapper;
import com.kadmin.organization.mapper.DepartmentMapper;
import com.kadmin.organization.mapper.OrgUnitMapper;
import com.kadmin.organization.service.CompanyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 公司服务实现类
 */
@Service
@RequiredArgsConstructor
public class CompanyServiceImpl extends ServiceImpl<CompanyMapper, OrgCompany> implements CompanyService {

    private final DepartmentMapper departmentMapper;
    private final OrgUnitMapper orgUnitMapper;

    @Override
    public IPage<OrgCompany> getCompanyPage(int pageNum, int pageSize, String companyName, Integer status) {
        Page<OrgCompany> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<OrgCompany> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(companyName)) {
            wrapper.like(OrgCompany::getCompanyName, companyName);
        }
        if (status != null) {
            wrapper.eq(OrgCompany::getStatus, status);
        }
        wrapper.orderByAsc(OrgCompany::getSortOrder);

        return this.page(page, wrapper);
    }

    @Override
    public List<OrgCompany> getAllCompanies() {
        LambdaQueryWrapper<OrgCompany> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OrgCompany::getStatus, 1);
        wrapper.orderByAsc(OrgCompany::getSortOrder);
        return this.list(wrapper);
    }

    @Override
    public OrgCompany getCompanyById(Long id) {
        return this.getById(id);
    }

    @Override
    public boolean createCompany(OrgCompany company) {
        return this.save(company);
    }

    @Override
    public boolean updateCompany(OrgCompany company) {
        return this.updateById(company);
    }

    @Override
    public boolean deleteCompany(Long id) {
        OrgCompany company = this.getById(id);
        if (company == null) {
            return true;
        }
        // 存在子公司时禁止删除
        Long childCompanyCount = count(new LambdaQueryWrapper<OrgCompany>()
                .eq(OrgCompany::getParentId, id));
        if (childCompanyCount != null && childCompanyCount > 0) {
            throw new IllegalArgumentException("该公司下存在子公司，无法删除");
        }
        // 存在下属部门时禁止删除
        Long deptCount = departmentMapper.selectCount(new LambdaQueryWrapper<OrgDepartment>()
                .eq(OrgDepartment::getCompanyId, id));
        if (deptCount != null && deptCount > 0) {
            throw new IllegalArgumentException("该公司下存在部门，无法删除");
        }
        // 统一组织架构树中对应公司节点及子节点下存在在职员工时禁止删除
        OrgUnit unit = findCompanyUnit(company);
        if (unit != null) {
            Integer employeeCount = orgUnitMapper.countEmployeesByDeptIds(orgUnitMapper.selectOrgAndChildIds(unit.getId()));
            if (employeeCount != null && employeeCount > 0) {
                throw new IllegalArgumentException("该公司下存在在职员工，无法删除");
            }
        }
        return this.removeById(id);
    }

    /**
     * 在统一组织架构树中查找公司对应的节点（先按编码，再按名称）
     */
    private OrgUnit findCompanyUnit(OrgCompany company) {
        if (StringUtils.hasText(company.getCompanyCode())) {
            OrgUnit unit = orgUnitMapper.selectOne(new LambdaQueryWrapper<OrgUnit>()
                    .eq(OrgUnit::getUnitType, OrgUnit.TYPE_COMPANY)
                    .eq(OrgUnit::getUnitCode, company.getCompanyCode())
                    .last("LIMIT 1"));
            if (unit != null) {
                return unit;
            }
        }
        if (StringUtils.hasText(company.getCompanyName())) {
            return orgUnitMapper.selectOne(new LambdaQueryWrapper<OrgUnit>()
                    .eq(OrgUnit::getUnitType, OrgUnit.TYPE_COMPANY)
                    .eq(OrgUnit::getUnitName, company.getCompanyName())
                    .last("LIMIT 1"));
        }
        return null;
    }
}