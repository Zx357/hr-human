package com.kadmin.hr.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kadmin.hr.domain.HrCertificate;
import com.kadmin.hr.domain.HrEducation;
import com.kadmin.hr.domain.HrEmployee;
import com.kadmin.hr.domain.HrEmployeeExtra;
import com.kadmin.hr.domain.HrFamilyMember;
import com.kadmin.hr.domain.HrWorkExperience;
import com.kadmin.hr.mapper.EmployeeMapper;
import com.kadmin.hr.mapper.HrCertificateMapper;
import com.kadmin.hr.mapper.HrEducationMapper;
import com.kadmin.hr.mapper.HrEmployeeExtraMapper;
import com.kadmin.hr.mapper.HrFamilyMemberMapper;
import com.kadmin.hr.mapper.HrWorkExperienceMapper;
import com.kadmin.organization.domain.OrgUnit;
import com.kadmin.organization.mapper.OrgUnitMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * 员工服务
 */
@Service
@RequiredArgsConstructor
public class EmployeeService extends ServiceImpl<EmployeeMapper, HrEmployee> {

    private final HrEducationMapper educationMapper;
    private final HrFamilyMemberMapper familyMemberMapper;
    private final HrWorkExperienceMapper workExperienceMapper;
    private final HrCertificateMapper certificateMapper;
    private final HrEmployeeExtraMapper employeeExtraMapper;
    private final OrgUnitMapper orgUnitMapper;

    /**
     * 分页查询员工
     */
    public Page<HrEmployee> getEmployeePage(int pageNum, int pageSize, String name, String employeeNo,
            Long deptId, String orgIds, Integer status) {
        Page<HrEmployee> page = new Page<>(pageNum, pageSize);
        // 使用QueryWrapper并为字段添加表别名e，避免JOIN时列名歧义
        QueryWrapper<HrEmployee> wrapper = new QueryWrapper<>();

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
        return getEmployeeDetail(id, false);
    }

    public HrEmployee getEmployeeDetail(Long id, boolean includeMiniAppPassword) {
        HrEmployee employee = getById(id);
        if (employee != null) {
            if (includeMiniAppPassword) {
                employee.setMiniAppPassword(employee.getPassword());
            }

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

            // 回显部门和公司名称（组织架构统一使用 org_unit：1-集团，2-公司，3-部门）
            if (employee.getDeptId() != null) {
                OrgUnit deptUnit = orgUnitMapper.selectById(employee.getDeptId());
                if (deptUnit != null) {
                    employee.setDeptName(deptUnit.getUnitName());

                    // 向上查找到公司节点
                    OrgUnit cursor = deptUnit;
                    int guard = 0;
                    while (cursor != null && cursor.getParentId() != null && cursor.getParentId() != 0L
                            && guard++ < 20) {
                        if (cursor.getUnitType() != null && cursor.getUnitType() == OrgUnit.TYPE_COMPANY) {
                            employee.setCompanyName(cursor.getUnitName());
                            break;
                        }
                        cursor = orgUnitMapper.selectById(cursor.getParentId());
                    }

                    // 如果部门本身就是公司节点
                    if (employee.getCompanyName() == null
                            && deptUnit.getUnitType() != null
                            && deptUnit.getUnitType() == OrgUnit.TYPE_COMPANY) {
                        employee.setCompanyName(deptUnit.getUnitName());
                    }
                }
            }
        }
        return employee;
    }

    /**
     * 新增员工（包含教育经历、家庭成员、工作经历、证书）
     */
    @Transactional
    public boolean addEmployee(HrEmployee employee) {
        if (employee.getStatus() == null) {
            employee.setStatus(1); // 默认在职
        }

        normalizePassword(employee);

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
        normalizePassword(employee);

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
     * 删除员工（真删除）
     */
    @Transactional
    public boolean deleteEmployee(Long id) {
        return removeById(id);
    }

    /**
     * 检查员工编号是否存在
     */
    public boolean checkEmployeeNoExists(String employeeNo, Long excludeId) {
        LambdaQueryWrapper<HrEmployee> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(HrEmployee::getEmployeeNo, employeeNo);
        if (excludeId != null) {
            wrapper.ne(HrEmployee::getId, excludeId);
        }
        return count(wrapper) > 0;
    }

    /**
     * 生成下一个员工编号
     * 格式: K + yyyyMMdd + 3位序号
     */
    public String generateNextEmployeeNo() {
        String today = LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"));
        String prefix = "K" + today;

        // 查询当天最大的工号
        String maxNo = baseMapper.selectMaxEmployeeNo(prefix);

        if (maxNo != null && maxNo.length() > prefix.length()) {
            int seq = Integer.parseInt(maxNo.substring(prefix.length())) + 1;
            return prefix + String.format("%03d", seq);
        }
        return prefix + "001";
    }

    private void normalizePassword(HrEmployee employee) {
        if (employee.getPassword() != null && employee.getPassword().trim().isEmpty()) {
            employee.setPassword(null);
        }
    }
}
