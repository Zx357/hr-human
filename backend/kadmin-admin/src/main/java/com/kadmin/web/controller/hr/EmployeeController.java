package com.kadmin.web.controller.hr;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kadmin.common.Result;
import com.kadmin.hr.domain.HrEmployee;
import com.kadmin.hr.domain.HrEducation;
import com.kadmin.hr.domain.HrFamilyMember;
import com.kadmin.hr.domain.HrWorkExperience;
import com.kadmin.hr.domain.HrCertificate;
import com.kadmin.hr.domain.HrEmployeeExtra;
import com.kadmin.hr.domain.dto.EmployeeDTO;
import com.kadmin.hr.domain.dto.EducationDTO;
import com.kadmin.hr.domain.dto.FamilyMemberDTO;
import com.kadmin.hr.domain.dto.WorkExperienceDTO;
import com.kadmin.hr.domain.dto.CertificateDTO;
import com.kadmin.hr.domain.dto.EmployeeExtraDTO;
import com.kadmin.common.security.LoginUser;
import com.kadmin.common.annotation.OperLog;
import com.kadmin.hr.service.EmployeeService;
import com.kadmin.common.utils.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
        // 密码哈希不回显
        page.getRecords().forEach(employee -> {
            employee.setPassword(null);
            employee.setMiniAppPassword(null);
        });
        return Result.success(page);
    }

    /**
     * 获取员工列表（不分页，通讯录使用）
     * 非管理员仅返回基础联系字段，敏感信息脱敏
     */
    @GetMapping("/list")
    public Result<List<HrEmployee>> list(
            @RequestParam(required = false) Long deptId,
            @RequestParam(required = false) Integer status) {
        List<HrEmployee> list = employeeService.getEmployeeList(deptId, status);
        for (HrEmployee employee : list) {
            // 密码哈希任何场景都不回显
            employee.setPassword(null);
            employee.setMiniAppPassword(null);
        }
        if (!SecurityUtils.isAdmin()) {
            for (HrEmployee employee : list) {
                employee.setIdCard(null);
                employee.setIdCardFront(null);
                employee.setIdCardBack(null);
            }
        }
        return Result.success(list);
    }

    /**
     * 获取员工详情
     * 管理员可看完整档案；普通员工仅能看自己完整档案或他人的基础信息（敏感字段脱敏）
     */
    @GetMapping("/{id}")
    public Result<HrEmployee> getDetail(@PathVariable Long id) {
        HrEmployee employee = employeeService.getEmployeeDetail(id, true);
        LoginUser loginUser = SecurityUtils.getCurrentUser();
        boolean self = loginUser != null && id != null && id.equals(loginUser.getEmployeeId());
        if (employee != null && !SecurityUtils.isAdmin() && !self) {
            employee.setIdCard(null);
            employee.setIdCardFront(null);
            employee.setIdCardBack(null);
            employee.setFamilyMemberList(null);
            employee.setEducationList(null);
            employee.setWorkExperienceList(null);
            employee.setCertificateList(null);
            employee.setExtraFieldList(null);
        }
        return Result.success(employee);
    }

    /**
     * 新增员工
     */
    @OperLog(module = "员工管理", action = "新增")
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
    @OperLog(module = "员工管理", action = "修改")
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
    @OperLog(module = "员工管理", action = "删除")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        employeeService.deleteEmployee(id);
        return Result.success();
    }

    /**
     * 批量删除员工（事务内执行，失败整体回滚）
     */
    @OperLog(module = "员工管理", action = "批量删除")
    @DeleteMapping("/batch")
    public Result<Void> deleteBatch(@RequestBody List<Long> ids) {
        employeeService.deleteEmployees(ids);
        return Result.success();
    }

    /**
     * 生成下一个员工编号
     */
    @GetMapping("/next-no")
    public Result<String> generateNextEmployeeNo() {
        String nextNo = employeeService.generateNextEmployeeNo();
        return Result.success(nextNo);
    }

    /**
     * 员工批量导入模板下载
     */
    @GetMapping("/import-template")
    public void importTemplate(jakarta.servlet.http.HttpServletResponse response) throws java.io.IOException {
        List<String> headers = List.of("工号(留空自动生成)", "姓名*", "性别(男/女)", "手机号", "邮箱",
                "身份证号", "出生日期(yyyy-MM-dd)", "学历", "员工类别", "入职日期(yyyy-MM-dd)",
                "部门名称", "岗位", "职务", "紧急联系人", "与紧急联系人关系", "紧急联系电话", "初始密码(留空默认123456)");
        List<List<Object>> rows = List.of(List.of("", "张三", "男", "13800000000", "zhangsan@example.com",
                "110101199001011234", "1990-01-01", "本科", "probation", java.time.LocalDate.now().toString(),
                "人事部", "Java开发工程师", "开发工程师", "李四", "父子", "13900000000", ""));
        com.kadmin.web.controller.system.ExcelExportUtil.write(response, "员工导入模板.xlsx", "员工导入",
                headers, rows);
    }

    /**
     * 员工批量导入（xlsx，最多1000行）
     */
    @OperLog(module = "员工管理", action = "导入")
    @PostMapping("/import")
    public Result<Map<String, Object>> importEmployees(@RequestParam("file") org.springframework.web.multipart.MultipartFile file)
            throws java.io.IOException {
        if (file == null || file.isEmpty()) {
            return Result.error("请选择文件");
        }
        String fileName = file.getOriginalFilename();
        if (fileName == null || !fileName.toLowerCase().endsWith(".xlsx")) {
            return Result.error("仅支持 .xlsx 文件");
        }

        List<String> errors = new ArrayList<>();
        int successCount = 0;
        try (org.apache.poi.ss.usermodel.Workbook workbook =
                org.apache.poi.ss.usermodel.WorkbookFactory.create(file.getInputStream())) {
            org.apache.poi.ss.usermodel.Sheet sheet = workbook.getSheetAt(0);
            int lastRow = sheet.getLastRowNum();
            if (lastRow < 1) {
                return Result.error("文件中没有数据行");
            }
            if (lastRow > 1000) {
                return Result.error("单次最多导入1000行");
            }
            for (int r = 1; r <= lastRow; r++) {
                org.apache.poi.ss.usermodel.Row row = sheet.getRow(r);
                if (row == null || isBlankRow(row)) {
                    continue;
                }
                int lineNo = r + 1;
                try {
                    HrEmployee employee = parseImportRow(row);
                    if (employee.getName() == null || employee.getName().isBlank()) {
                        errors.add("第" + lineNo + "行：姓名不能为空");
                        continue;
                    }
                    if (employee.getEmployeeNo() == null || employee.getEmployeeNo().isBlank()) {
                        employee.setEmployeeNo(employeeService.generateNextEmployeeNo());
                    }
                    if (employeeService.checkEmployeeNoExists(employee.getEmployeeNo(), null)) {
                        errors.add("第" + lineNo + "行：工号 " + employee.getEmployeeNo() + " 已存在");
                        continue;
                    }
                    if (employee.getPassword() != null && !employee.getPassword().isBlank()) {
                        String policyError = com.kadmin.common.utils.PasswordPolicy.check(employee.getPassword());
                        if (policyError != null) {
                            errors.add("第" + lineNo + "行：" + policyError);
                            continue;
                        }
                    }
                    employeeService.addEmployee(employee);
                    successCount++;
                } catch (Exception e) {
                    errors.add("第" + lineNo + "行：" + e.getMessage());
                }
            }
        } catch (Exception e) {
            return Result.error("文件解析失败，请使用下载的模板填写");
        }

        Map<String, Object> data = new java.util.HashMap<>();
        data.put("successCount", successCount);
        data.put("failCount", errors.size());
        data.put("errors", errors);
        return Result.success(data);
    }

    private HrEmployee parseImportRow(org.apache.poi.ss.usermodel.Row row) {
        HrEmployee employee = new HrEmployee();
        employee.setEmployeeNo(readCellString(row, 0));
        employee.setName(readCellString(row, 1));
        employee.setGender(readCellString(row, 2));
        employee.setPhone(readCellString(row, 3));
        employee.setEmail(readCellString(row, 4));
        employee.setIdCard(readCellString(row, 5));
        employee.setBirthDate(readCellDate(row, 6));
        employee.setHighestEducation(readCellString(row, 7));
        employee.setEmployeeType(readCellString(row, 8));
        employee.setEntryDate(readCellDate(row, 9));
        String deptName = readCellString(row, 10);
        if (deptName != null && !deptName.isBlank()) {
            Long orgId = employeeService.findOrgIdByName(deptName);
            if (orgId == null) {
                throw new IllegalArgumentException("部门「" + deptName + "」不存在");
            }
            employee.setDeptId(orgId);
        }
        employee.setPosition(readCellString(row, 11));
        employee.setDuty(readCellString(row, 12));
        employee.setEmergencyContact(readCellString(row, 13));
        employee.setEmergencyRelation(readCellString(row, 14));
        employee.setEmergencyPhone(readCellString(row, 15));
        employee.setPassword(readCellString(row, 16));
        return employee;
    }

    private String readCellString(org.apache.poi.ss.usermodel.Row row, int index) {
        org.apache.poi.ss.usermodel.Cell cell = row.getCell(index);
        if (cell == null) {
            return null;
        }
        org.apache.poi.ss.usermodel.CellType type = cell.getCellType();
        if (type == org.apache.poi.ss.usermodel.CellType.STRING) {
            return cell.getStringCellValue().trim();
        }
        if (type == org.apache.poi.ss.usermodel.CellType.NUMERIC) {
            double value = cell.getNumericCellValue();
            if (value == Math.floor(value)) {
                return String.valueOf((long) value);
            }
            return String.valueOf(value);
        }
        if (type == org.apache.poi.ss.usermodel.CellType.BOOLEAN) {
            return String.valueOf(cell.getBooleanCellValue());
        }
        if (type == org.apache.poi.ss.usermodel.CellType.FORMULA) {
            try {
                return cell.getStringCellValue().trim();
            } catch (IllegalStateException e) {
                return String.valueOf(cell.getNumericCellValue());
            }
        }
        return null;
    }

    private java.time.LocalDate readCellDate(org.apache.poi.ss.usermodel.Row row, int index) {
        org.apache.poi.ss.usermodel.Cell cell = row.getCell(index);
        if (cell == null) {
            return null;
        }
        try {
            if (cell.getCellType() == org.apache.poi.ss.usermodel.CellType.NUMERIC
                    && org.apache.poi.ss.usermodel.DateUtil.isCellDateFormatted(cell)) {
                return cell.getLocalDateTimeCellValue().toLocalDate();
            }
        } catch (IllegalStateException ignored) {
            // 落到文本解析
        }
        String text = readCellString(row, index);
        if (text == null || text.isBlank()) {
            return null;
        }
        return java.time.LocalDate.parse(text.trim());
    }

    private boolean isBlankRow(org.apache.poi.ss.usermodel.Row row) {
        for (int c = 0; c < 17; c++) {
            String value = readCellString(row, c);
            if (value != null && !value.isBlank()) {
                return false;
            }
        }
        return true;
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
