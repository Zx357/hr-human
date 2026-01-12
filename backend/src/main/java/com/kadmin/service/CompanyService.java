package com.kadmin.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.kadmin.entity.OrgCompany;

import java.util.List;

/**
 * 公司服务接口
 */
public interface CompanyService extends IService<OrgCompany> {

    /**
     * 分页查询公司列表
     */
    IPage<OrgCompany> getCompanyPage(int pageNum, int pageSize, String companyName, Integer status);

    /**
     * 获取所有公司列表
     */
    List<OrgCompany> getAllCompanies();

    /**
     * 根据ID获取公司
     */
    OrgCompany getCompanyById(Long id);

    /**
     * 创建公司
     */
    boolean createCompany(OrgCompany company);

    /**
     * 更新公司
     */
    boolean updateCompany(OrgCompany company);

    /**
     * 删除公司
     */
    boolean deleteCompany(Long id);
}