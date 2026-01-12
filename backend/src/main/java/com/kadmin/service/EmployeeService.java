package com.kadmin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kadmin.entity.*;
import com.kadmin.mapper.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 员工服务
 */
@Service
public class EmployeeService extends ServiceImpl<EmployeeMapper, HrEmployee> {

    @Autowired
    private HrEducationMapper educationMapper;

    @Autowired
    private HrFamilyMemberMapper familyMemberMapper;

    @Autowired
    private HrWorkExperienceMapper workExperienceMapper;

    @Autowired
    private HrCertificateMapper certificateMapper;

    @Autowired
    private HrEmployeeExtraMapper employeeExtraMapper;

    /**
     * 分页查询员工
     */
    public Page<HrEmployee> getEmployeePage(int pageNum, int pageSize, String name, String employeeNo,
            Long deptId, String orgIds, Integer status) {
        Page<HrEmployee> page = new Page<>(pageNum, pageSize);
        // 使用QueryWrapper并为字段添加表别名e，避免JOIN时列名歧义
        QueryWrapper<HrEmployee> wrapper = new QueryWrapper<>();

        wrapper.eq("e.deleted", 0);

        if (name != null && !name.isEmpty()) {
            wrapper.like("e.name", name);
        }
        if (employeeNo != null && !employeeNo.isEmpty()) {
            wrapper.like("e.employee_no", employeeNo);
        }
        // 支持多组织ID查询（统一使用dept_id，因为company_id已移除）
        if (orgIds != null && !orgIds.isEmpty()) {
            String[] ids = orgIds.split(",");
            wrapper.and(w -> {
                for (int i = 0; i < ids.length; i++) {
                    Long orgId = Long.parseLong(ids[i].trim());
                    if (i == 0) {
                        w.eq("e.dept_id", orgId);
                    } else {
                        w.or().eq("e.dept_id", orgId);
                    }
                }
            });
        } else if (deptId != null) {
            wrapper.eq("e.dept_id", deptId);
        }
        if (status != null) {
            wrapper.eq("e.status", status);
        }

        wrapper.orderByDesc("e.created_time");

        return baseMapper.selectPageWithDetails(page, wrapper);
    }

    /**
     * 获取员工列表（不分页）
     */
    public List<HrEmployee> getEmployeeList(Long deptId, Integer status) {
        LambdaQueryWrapper<HrEmployee> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(HrEmployee::getDeleted, 0);
        if (deptId != null) {
            wrapper.eq(HrEmployee::getDeptId, deptId);
        }
        if (status != null) {
            wrapper.eq(HrEmployee::getStatus, status);
        }
        wrapper.orderByAsc(HrEmployee::getName);
        return list(wrapper);
    }

    /**
     * 获取员工详情（包含教育经历、家庭成员、工作经历、证书）
     */
    public HrEmployee getEmployeeDetail(Long id) {
        HrEmployee employee = getById(id);
        if (employee != null) {
            // 加载教育经历
            employee.setEducationList(educationMapper.selectList(
                    new LambdaQueryWrapper<HrEducation>()
                            .eq(HrEducation::getEmployeeId, id)
                            .orderByAsc(HrEducation::getStartDate)));

            // 加载家庭成员
            employee.setFamilyMemberList(familyMemberMapper.selectList(
                    new LambdaQueryWrapper<HrFamilyMember>()
                            .eq(HrFamilyMember::getEmployeeId, id)));

            // 加载工作经历
            employee.setWorkExperienceList(workExperienceMapper.selectList(
                    new LambdaQueryWrapper<HrWorkExperience>()
                            .eq(HrWorkExperience::getEmployeeId, id)
                            .orderByDesc(HrWorkExperience::getStartDate)));

            // 加载证书
            employee.setCertificateList(certificateMapper.selectList(
                    new LambdaQueryWrapper<HrCertificate>()
                            .eq(HrCertificate::getEmployeeId, id)
                            .orderByDesc(HrCertificate::getIssueDate)));

            // 加载扩展字段
            employee.setExtraFieldList(employeeExtraMapper.selectList(
                    new LambdaQueryWrapper<HrEmployeeExtra>()
                            .eq(HrEmployeeExtra::getEmployeeId, id)));
        }
        return employee;
    }

    /**
     * 新增员工（包含教育经历、家庭成员、工作经历、证书）
     */
    @Transactional
    public boolean addEmployee(HrEmployee employee) {
        employee.setDeleted(0);
        if (employee.getStatus() == null) {
            employee.setStatus(1); // 默认在职
        }

        boolean result = save(employee);

        if (result) {
            Long employeeId = employee.getId();

            // 保存教育经历
            if (employee.getEducationList() != null) {
                for (HrEducation education : employee.getEducationList()) {
                    education.setEmployeeId(employeeId);
                    educationMapper.insert(education);
                }
            }

            // 保存家庭成员
            if (employee.getFamilyMemberList() != null) {
                for (HrFamilyMember familyMember : employee.getFamilyMemberList()) {
                    familyMember.setEmployeeId(employeeId);
                    familyMemberMapper.insert(familyMember);
                }
            }

            // 保存工作经历
            if (employee.getWorkExperienceList() != null) {
                for (HrWorkExperience workExperience : employee.getWorkExperienceList()) {
                    workExperience.setEmployeeId(employeeId);
                    workExperienceMapper.insert(workExperience);
                }
            }

            // 保存证书
            if (employee.getCertificateList() != null) {
                for (HrCertificate certificate : employee.getCertificateList()) {
                    certificate.setEmployeeId(employeeId);
                    certificateMapper.insert(certificate);
                }
            }

            // 保存扩展字段
            if (employee.getExtraFieldList() != null) {
                for (HrEmployeeExtra extra : employee.getExtraFieldList()) {
                    extra.setEmployeeId(employeeId);
                    employeeExtraMapper.insert(extra);
                }
            }
        }

        return result;
    }

    /**
     * 更新员工（包含教育经历、家庭成员、工作经历、证书）
     */
    @Transactional
    public boolean updateEmployee(HrEmployee employee) {
        boolean result = updateById(employee);

        if (result) {
            Long employeeId = employee.getId();

            // 更新教育经历（先删后增）
            if (employee.getEducationList() != null) {
                educationMapper.delete(new LambdaQueryWrapper<HrEducation>()
                        .eq(HrEducation::getEmployeeId, employeeId));
                for (HrEducation education : employee.getEducationList()) {
                    education.setId(null);
                    education.setEmployeeId(employeeId);
                    educationMapper.insert(education);
                }
            }

            // 更新家庭成员（先删后增）
            if (employee.getFamilyMemberList() != null) {
                familyMemberMapper.delete(new LambdaQueryWrapper<HrFamilyMember>()
                        .eq(HrFamilyMember::getEmployeeId, employeeId));
                for (HrFamilyMember familyMember : employee.getFamilyMemberList()) {
                    familyMember.setId(null);
                    familyMember.setEmployeeId(employeeId);
                    familyMemberMapper.insert(familyMember);
                }
            }

            // 更新工作经历（先删后增）
            if (employee.getWorkExperienceList() != null) {
                workExperienceMapper.delete(new LambdaQueryWrapper<HrWorkExperience>()
                        .eq(HrWorkExperience::getEmployeeId, employeeId));
                for (HrWorkExperience workExperience : employee.getWorkExperienceList()) {
                    workExperience.setId(null);
                    workExperience.setEmployeeId(employeeId);
                    workExperienceMapper.insert(workExperience);
                }
            }

            // 更新证书（先删后增）
            if (employee.getCertificateList() != null) {
                certificateMapper.delete(new LambdaQueryWrapper<HrCertificate>()
                        .eq(HrCertificate::getEmployeeId, employeeId));
                for (HrCertificate certificate : employee.getCertificateList()) {
                    certificate.setId(null);
                    certificate.setEmployeeId(employeeId);
                    certificateMapper.insert(certificate);
                }
            }

            // 更新扩展字段（先删后增）
            if (employee.getExtraFieldList() != null) {
                employeeExtraMapper.delete(new LambdaQueryWrapper<HrEmployeeExtra>()
                        .eq(HrEmployeeExtra::getEmployeeId, employeeId));
                for (HrEmployeeExtra extra : employee.getExtraFieldList()) {
                    extra.setId(null);
                    extra.setEmployeeId(employeeId);
                    employeeExtraMapper.insert(extra);
                }
            }
        }

        return result;
    }

    /**
     * 删除员工（逻辑删除）
     */
    @Transactional
    public boolean deleteEmployee(Long id) {
        HrEmployee employee = new HrEmployee();
        employee.setId(id);
        employee.setDeleted(1);
        return updateById(employee);
    }

    /**
     * 检查员工编号是否存在
     */
    public boolean checkEmployeeNoExists(String employeeNo, Long excludeId) {
        LambdaQueryWrapper<HrEmployee> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(HrEmployee::getEmployeeNo, employeeNo)
                .eq(HrEmployee::getDeleted, 0);
        if (excludeId != null) {
            wrapper.ne(HrEmployee::getId, excludeId);
        }
        return count(wrapper) > 0;
    }
}