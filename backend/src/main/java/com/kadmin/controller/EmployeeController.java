package com.kadmin.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kadmin.common.Result;
import com.kadmin.entity.HrEmployee;
import com.kadmin.entity.HrEducation;
import com.kadmin.entity.HrFamilyMember;
import com.kadmin.entity.HrWorkExperience;
import com.kadmin.entity.HrCertificate;
import com.kadmin.entity.HrEmployeeExtra;
import com.kadmin.entity.dto.EmployeeDTO;
import com.kadmin.entity.dto.EducationDTO;
import com.kadmin.entity.dto.FamilyMemberDTO;
import com.kadmin.entity.dto.WorkExperienceDTO;
import com.kadmin.entity.dto.CertificateDTO;
import com.kadmin.entity.dto.EmployeeExtraDTO;
import com.kadmin.security.LoginUser;
import com.kadmin.service.EmployeeService;
import com.kadmin.utils.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * 员工管理Controller
 */
@RestController
@RequestMapping("/employee")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;

    /**
     * 获取当前登录员工信息（移动端使用）
     */
    @GetMapping("/current")
    public Result<HrEmployee> getCurrentEmployee() {
        LoginUser loginUser = SecurityUtils.getCurrentUser();
        if (loginUser == null || loginUser.getEmployeeId() == null) {
            return Result.error("未登录或无员工信息");
        }
        HrEmployee employee = employeeService.getEmployeeDetail(loginUser.getEmployeeId());
        return Result.success(employee);
    }

    /**
     * 分页查询员工列表
     */
    @GetMapping("/page")
    public Result<Page<HrEmployee>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String employeeNo,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Long deptId,
            @RequestParam(required = false) String orgIds,
            @RequestParam(required = false) Integer status) {
        Page<HrEmployee> page = employeeService.getEmployeePage(pageNum, pageSize, name, employeeNo, deptId, orgIds,
                status);
        return Result.success(page);
    }

    /**
     * 获取员工列表（不分页）
     */
    @GetMapping("/list")
    public Result<List<HrEmployee>> list(
            @RequestParam(required = false) Long deptId,
            @RequestParam(required = false) Integer status) {
        List<HrEmployee> list = employeeService.getEmployeeList(deptId, status);
        return Result.success(list);
    }

    /**
     * 获取员工详情
     */
    @GetMapping("/{id}")
    public Result<HrEmployee> getDetail(@PathVariable Long id) {
        HrEmployee employee = employeeService.getEmployeeDetail(id);
        return Result.success(employee);
    }

    /**
     * 新增员工
     */
    @PostMapping
    public Result<Void> add(@Valid @RequestBody EmployeeDTO dto) {
        // 检查工号是否重复
        if (dto.getEmployeeNo() != null && !dto.getEmployeeNo().isEmpty()) {
            if (employeeService.checkEmployeeNoExists(dto.getEmployeeNo(), null)) {
                return Result.error("工号已存在");
            }
        }
        HrEmployee employee = convertDtoToEntity(dto);
        employeeService.addEmployee(employee);
        return Result.success();
    }

    /**
     * 修改员工
     */
    @PutMapping
    public Result<Void> update(@Valid @RequestBody EmployeeDTO dto) {
        // 检查工号是否重复（排除自己）
        if (dto.getEmployeeNo() != null && !dto.getEmployeeNo().isEmpty()) {
            if (employeeService.checkEmployeeNoExists(dto.getEmployeeNo(), dto.getId())) {
                return Result.error("工号已存在");
            }
        }
        HrEmployee employee = convertDtoToEntity(dto);
        employeeService.updateEmployee(employee);
        return Result.success();
    }

    /**
     * 删除员工
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        employeeService.deleteEmployee(id);
        return Result.success();
    }

    /**
     * 批量删除员工
     */
    @DeleteMapping("/batch")
    public Result<Void> deleteBatch(@RequestBody List<Long> ids) {
        for (Long id : ids) {
            employeeService.deleteEmployee(id);
        }
        return Result.success();
    }

    /**
     * 检查工号是否存在
     */
    @GetMapping("/check-no")
    public Result<Boolean> checkEmployeeNo(
            @RequestParam String employeeNo,
            @RequestParam(required = false) Long excludeId) {
        boolean exists = employeeService.checkEmployeeNoExists(employeeNo, excludeId);
        return Result.success(exists);
    }

    /**
     * 将DTO转换为实体
     */
    private HrEmployee convertDtoToEntity(EmployeeDTO dto) {
        HrEmployee employee = new HrEmployee();
        BeanUtils.copyProperties(dto, employee);

        // 转换教育经历
        if (dto.getEducationList() != null) {
            List<HrEducation> educationList = new ArrayList<>();
            for (EducationDTO eduDto : dto.getEducationList()) {
                HrEducation education = new HrEducation();
                BeanUtils.copyProperties(eduDto, education);
                educationList.add(education);
            }
            employee.setEducationList(educationList);
        }

        // 转换家庭成员
        if (dto.getFamilyMemberList() != null) {
            List<HrFamilyMember> familyMemberList = new ArrayList<>();
            for (FamilyMemberDTO famDto : dto.getFamilyMemberList()) {
                HrFamilyMember familyMember = new HrFamilyMember();
                BeanUtils.copyProperties(famDto, familyMember);
                familyMemberList.add(familyMember);
            }
            employee.setFamilyMemberList(familyMemberList);
        }

        // 转换工作经历
        if (dto.getWorkExperienceList() != null) {
            List<HrWorkExperience> workExperienceList = new ArrayList<>();
            for (WorkExperienceDTO workDto : dto.getWorkExperienceList()) {
                HrWorkExperience workExperience = new HrWorkExperience();
                BeanUtils.copyProperties(workDto, workExperience);
                workExperienceList.add(workExperience);
            }
            employee.setWorkExperienceList(workExperienceList);
        }

        // 转换证书
        if (dto.getCertificateList() != null) {
            List<HrCertificate> certificateList = new ArrayList<>();
            for (CertificateDTO certDto : dto.getCertificateList()) {
                HrCertificate certificate = new HrCertificate();
                BeanUtils.copyProperties(certDto, certificate);
                certificateList.add(certificate);
            }
            employee.setCertificateList(certificateList);
        }

        // 转换扩展字段
        if (dto.getExtraFieldList() != null) {
            List<HrEmployeeExtra> extraFieldList = new ArrayList<>();
            for (EmployeeExtraDTO extraDto : dto.getExtraFieldList()) {
                HrEmployeeExtra extra = new HrEmployeeExtra();
                extra.setFieldCode(extraDto.getFieldCode());
                extra.setFieldValue(extraDto.getFieldValue());
                extraFieldList.add(extra);
            }
            employee.setExtraFieldList(extraFieldList);
        }

        return employee;
    }
}
