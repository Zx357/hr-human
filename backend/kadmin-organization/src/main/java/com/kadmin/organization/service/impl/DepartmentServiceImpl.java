package com.kadmin.organization.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.kadmin.organization.domain.OrgDepartment;
import com.kadmin.organization.domain.OrgUnit;
import com.kadmin.organization.mapper.DepartmentMapper;
import com.kadmin.organization.mapper.OrgUnitMapper;
import com.kadmin.organization.service.DepartmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 部门服务实现类
 */
@Service
@RequiredArgsConstructor
public class DepartmentServiceImpl extends ServiceImpl<DepartmentMapper, OrgDepartment> implements DepartmentService {

    private final OrgUnitMapper orgUnitMapper;

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
        OrgDepartment department = this.getById(id);
        if (department == null) {
            return true;
        }
        // 存在子部门时禁止删除
        Long childCount = count(new LambdaQueryWrapper<OrgDepartment>()
                .eq(OrgDepartment::getParentId, id));
        if (childCount != null && childCount > 0) {
            throw new IllegalArgumentException("该部门下存在子部门，无法删除");
        }
        // 统一组织架构树中对应部门节点及子节点下存在在职员工时禁止删除
        OrgUnit unit = findDepartmentUnit(department);
        if (unit != null) {
            Integer employeeCount = orgUnitMapper.countEmployeesByDeptIds(orgUnitMapper.selectOrgAndChildIds(unit.getId()));
            if (employeeCount != null && employeeCount > 0) {
                throw new IllegalArgumentException("该部门下存在在职员工，无法删除");
            }
        }
        return this.removeById(id);
    }

    /**
     * 在统一组织架构树中查找部门对应的节点（先按编码，再按名称）
     */
    private OrgUnit findDepartmentUnit(OrgDepartment department) {
        if (StringUtils.hasText(department.getDeptCode())) {
            OrgUnit unit = orgUnitMapper.selectOne(new LambdaQueryWrapper<OrgUnit>()
                    .eq(OrgUnit::getUnitType, OrgUnit.TYPE_DEPT)
                    .eq(OrgUnit::getUnitCode, department.getDeptCode())
                    .last("LIMIT 1"));
            if (unit != null) {
                return unit;
            }
        }
        if (StringUtils.hasText(department.getDeptName())) {
            return orgUnitMapper.selectOne(new LambdaQueryWrapper<OrgUnit>()
                    .eq(OrgUnit::getUnitType, OrgUnit.TYPE_DEPT)
                    .eq(OrgUnit::getUnitName, department.getDeptName())
                    .last("LIMIT 1"));
        }
        return null;
    }
}