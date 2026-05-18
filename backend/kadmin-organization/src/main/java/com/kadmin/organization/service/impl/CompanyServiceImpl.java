package com.kadmin.organization.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kadmin.organization.domain.OrgCompany;
import com.kadmin.organization.mapper.CompanyMapper;
import com.kadmin.organization.service.CompanyService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 公司服务实现类
 */
@Service
public class CompanyServiceImpl extends ServiceImpl<CompanyMapper, OrgCompany> implements CompanyService {

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
        return this.removeById(id);
    }
}