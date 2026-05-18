package com.kadmin.organization.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.kadmin.organization.domain.OrgDepartment;

import java.util.List;

/**
 * 部门服务接口
 */
public interface DepartmentService extends IService<OrgDepartment> {

    /**
     * 分页查询部门列表
     */
    IPage<OrgDepartment> getDepartmentPage(int pageNum, int pageSize, Long companyId, String deptName, Integer status);

    /**
     * 获取公司下所有部门（树形结构）
     */
    List<OrgDepartment> getDepartmentTree(Long companyId);

    /**
     * 获取公司下所有部门列表
     */
    List<OrgDepartment> getDepartmentsByCompanyId(Long companyId);

    /**
     * 根据ID获取部门
     */
    OrgDepartment getDepartmentById(Long id);

    /**
     * 创建部门
     */
    boolean createDepartment(OrgDepartment department);

    /**
     * 更新部门
     */
    boolean updateDepartment(OrgDepartment department);

    /**
     * 删除部门
     */
    boolean deleteDepartment(Long id);
}