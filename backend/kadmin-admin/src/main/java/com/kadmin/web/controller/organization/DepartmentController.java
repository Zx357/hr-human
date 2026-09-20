package com.kadmin.web.controller.organization;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.kadmin.common.annotation.RequiresPermission;
import com.kadmin.common.Result;
import com.kadmin.organization.domain.OrgDepartment;
import com.kadmin.organization.service.DepartmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 部门管理控制器
 */
@RestController
@RequestMapping("/organization/department")
@RequiredArgsConstructor
public class DepartmentController {

    private final DepartmentService departmentService;

    /**
     * 分页查询部门列表
     */
    @GetMapping("/page")
    public Result<Map<String, Object>> getDepartmentPage(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) Long companyId,
            @RequestParam(required = false) String deptName,
            @RequestParam(required = false) Integer status) {

        IPage<OrgDepartment> page = departmentService.getDepartmentPage(pageNum, pageSize, companyId, deptName, status);

        Map<String, Object> result = new HashMap<>();
        result.put("records", page.getRecords());
        result.put("total", page.getTotal());
        result.put("current", page.getCurrent());
        result.put("size", page.getSize());

        return Result.success(result);
    }

    /**
     * 获取部门树形结构
     */
    @GetMapping("/tree")
    public Result<List<OrgDepartment>> getDepartmentTree(@RequestParam(required = false) Long companyId) {
        return Result.success(departmentService.getDepartmentTree(companyId));
    }

    /**
     * 获取公司下所有部门列表
     */
    @GetMapping("/list")
    public Result<List<OrgDepartment>> getDepartmentsByCompanyId(@RequestParam(required = false) Long companyId) {
        if (companyId != null) {
            return Result.success(departmentService.getDepartmentsByCompanyId(companyId));
        }
        return Result.success(departmentService.getDepartmentTree(null));
    }

    /**
     * 根据ID获取部门详情
     */
    @GetMapping("/{id}")
    public Result<OrgDepartment> getDepartmentById(@PathVariable Long id) {
        return Result.success(departmentService.getDepartmentById(id));
    }

    /**
     * 创建部门
     */
    @RequiresPermission("org:department:add")
    @PostMapping
    public Result<Boolean> createDepartment(@RequestBody OrgDepartment department) {
        return Result.success(departmentService.createDepartment(department));
    }

    /**
     * 更新部门
     */
    @RequiresPermission("org:department:edit")
    @PutMapping("/{id}")
    public Result<Boolean> updateDepartment(@PathVariable Long id, @RequestBody OrgDepartment department) {
        department.setId(id);
        return Result.success(departmentService.updateDepartment(department));
    }

    /**
     * 删除部门
     */
    @RequiresPermission("org:department:delete")
    @DeleteMapping("/{id}")
    public Result<Boolean> deleteDepartment(@PathVariable Long id) {
        return Result.success(departmentService.deleteDepartment(id));
    }
}