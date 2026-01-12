package com.kadmin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kadmin.entity.OrgDepartment;
import com.kadmin.mapper.DepartmentMapper;
import com.kadmin.service.DepartmentService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 部门服务实现类
 */
@Service
public class DepartmentServiceImpl extends ServiceImpl<DepartmentMapper, OrgDepartment> implements DepartmentService {

    @Override
    public IPage<OrgDepartment> getDepartmentPage(int pageNum, int pageSize, Long companyId, String deptName,
            Integer status) {
        Page<OrgDepartment> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<OrgDepartment> wrapper = new LambdaQueryWrapper<>();

        if (companyId != null) {
            wrapper.eq(OrgDepartment::getCompanyId, companyId);
        }
        if (StringUtils.hasText(deptName)) {
            wrapper.like(OrgDepartment::getDeptName, deptName);
        }
        if (status != null) {
            wrapper.eq(OrgDepartment::getStatus, status);
        }
        wrapper.orderByAsc(OrgDepartment::getSortOrder);

        return this.page(page, wrapper);
    }

    @Override
    public List<OrgDepartment> getDepartmentTree(Long companyId) {
        LambdaQueryWrapper<OrgDepartment> wrapper = new LambdaQueryWrapper<>();
        if (companyId != null) {
            wrapper.eq(OrgDepartment::getCompanyId, companyId);
        }
        wrapper.eq(OrgDepartment::getStatus, 1);
        wrapper.orderByAsc(OrgDepartment::getSortOrder);
        return this.list(wrapper);
    }

    @Override
    public List<OrgDepartment> getDepartmentsByCompanyId(Long companyId) {
        LambdaQueryWrapper<OrgDepartment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OrgDepartment::getCompanyId, companyId);
        wrapper.eq(OrgDepartment::getStatus, 1);
        wrapper.orderByAsc(OrgDepartment::getSortOrder);
        return this.list(wrapper);
    }

    @Override
    public OrgDepartment getDepartmentById(Long id) {
        return this.getById(id);
    }

    @Override
    public boolean createDepartment(OrgDepartment department) {
        return this.save(department);
    }

    @Override
    public boolean updateDepartment(OrgDepartment department) {
        return this.updateById(department);
    }

    @Override
    public boolean deleteDepartment(Long id) {
        return this.removeById(id);
    }
}