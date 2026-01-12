package com.kadmin.controller;

import com.kadmin.common.Result;
import com.kadmin.dto.OrgStatisticsDTO;
import com.kadmin.entity.OrgUnit;
import com.kadmin.service.OrgUnitService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 组织架构控制器
 */
@RestController
@RequestMapping("/org-unit")
@RequiredArgsConstructor
public class OrgUnitController {

    private final OrgUnitService orgUnitService;

    /**
     * 获取完整组织架构树
     */
    @GetMapping("/tree")
    public Result<List<OrgUnit>> getTree() {
        return Result.success(orgUnitService.getOrgTree());
    }

    /**
     * 获取带员工数量的组织架构树
     */
    @GetMapping("/tree-with-count")
    public Result<List<OrgUnit>> getTreeWithCount() {
        return Result.success(orgUnitService.getOrgTreeWithCount());
    }

    /**
     * 获取组织统计数据
     */
    @GetMapping("/{id}/statistics")
    public Result<OrgStatisticsDTO> getStatistics(@PathVariable Long id) {
        return Result.success(orgUnitService.getStatistics(id));
    }

    /**
     * 获取全局统计数据
     */
    @GetMapping("/statistics")
    public Result<OrgStatisticsDTO> getAllStatistics() {
        return Result.success(orgUnitService.getStatistics(0L));
    }

    /**
     * 获取指定类型的组织列表
     */
    @GetMapping("/list")
    public Result<List<OrgUnit>> getList(@RequestParam(required = false) Integer unitType) {
        if (unitType != null) {
            return Result.success(orgUnitService.getListByType(unitType));
        }
        return Result.success(orgUnitService.list());
    }

    /**
     * 获取公司列表（兼容旧接口）
     */
    @GetMapping("/companies")
    public Result<List<OrgUnit>> getCompanies() {
        return Result.success(orgUnitService.getCompanyList());
    }

    /**
     * 获取部门树（兼容旧接口）
     */
    @GetMapping("/depts")
    public Result<List<OrgUnit>> getDepts(@RequestParam(required = false) Long parentId) {
        return Result.success(orgUnitService.getDeptTree(parentId != null ? parentId : 0L));
    }

    /**
     * 获取单个组织详情
     */
    @GetMapping("/{id}")
    public Result<OrgUnit> getById(@PathVariable Long id) {
        return Result.success(orgUnitService.getById(id));
    }

    /**
     * 创建组织节点
     */
    @PostMapping
    public Result<Void> create(@RequestBody OrgUnit unit) {
        // 检查编码是否重复
        if (orgUnitService.checkCodeExists(unit.getUnitCode(), null)) {
            return Result.error("编码已存在");
        }
        orgUnitService.createUnit(unit);
        return Result.success();
    }

    /**
     * 更新组织节点
     */
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody OrgUnit unit) {
        // 检查编码是否重复
        if (orgUnitService.checkCodeExists(unit.getUnitCode(), id)) {
            return Result.error("编码已存在");
        }
        unit.setId(id);
        orgUnitService.updateUnit(unit);
        return Result.success();
    }

    /**
     * 删除组织节点
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        try {
            orgUnitService.deleteUnit(id);
            return Result.success();
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 检查编码是否存在
     */
    @GetMapping("/check-code")
    public Result<Boolean> checkCode(@RequestParam String unitCode, @RequestParam(required = false) Long excludeId) {
        return Result.success(orgUnitService.checkCodeExists(unitCode, excludeId));
    }

    /**
     * 获取节点所属公司ID
     */
    @GetMapping("/{id}/company")
    public Result<Long> getCompanyId(@PathVariable Long id) {
        return Result.success(orgUnitService.getCompanyId(id));
    }
}
