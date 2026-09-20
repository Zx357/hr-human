package com.kadmin.web.controller.organization;

import com.kadmin.common.Result;
import com.kadmin.common.annotation.RequiresPermission;
import com.kadmin.organization.domain.dto.OrgStatisticsDTO;
import com.kadmin.organization.domain.OrgUnit;
import com.kadmin.organization.service.OrgUnitService;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Organization structure APIs.
 */
@RestController
@RequestMapping("/org-unit")
@RequiredArgsConstructor
public class OrgUnitController {

    private final OrgUnitService orgUnitService;

    @GetMapping("/tree")
    public Result<List<OrgUnit>> getTree() {
        return Result.success(orgUnitService.getOrgTree());
    }

    @GetMapping("/tree-with-count")
    public Result<List<OrgUnit>> getTreeWithCount() {
        return Result.success(orgUnitService.getOrgTreeWithCount());
    }

    @GetMapping("/{id}/statistics")
    public Result<OrgStatisticsDTO> getStatistics(@PathVariable Long id) {
        return Result.success(orgUnitService.getStatistics(id));
    }

    @GetMapping("/statistics")
    public Result<OrgStatisticsDTO> getAllStatistics() {
        return Result.success(orgUnitService.getStatistics(0L));
    }

    @GetMapping("/list")
    public Result<List<OrgUnit>> getList(@RequestParam(required = false) Integer unitType) {
        if (unitType != null) {
            return Result.success(orgUnitService.getListByType(unitType));
        }
        return Result.success(orgUnitService.list());
    }

    @GetMapping("/companies")
    public Result<List<OrgUnit>> getCompanies() {
        return Result.success(orgUnitService.getCompanyList());
    }

    @GetMapping("/depts")
    public Result<List<OrgUnit>> getDepts(@RequestParam(required = false) Long parentId) {
        return Result.success(orgUnitService.getDeptTree(parentId != null ? parentId : 0L));
    }

    @GetMapping("/{id}")
    public Result<OrgUnit> getById(@PathVariable Long id) {
        return Result.success(orgUnitService.getById(id));
    }

    @RequiresPermission("org:unit:add")
    @PostMapping
    public Result<Void> create(@RequestBody OrgUnit unit) {
        if (orgUnitService.checkCodeExists(unit.getUnitCode(), null)) {
            return Result.error("组织编码已存在");
        }

        String attendanceError = validateAttendanceConfig(unit);
        if (attendanceError != null) {
            return Result.error(attendanceError);
        }

        orgUnitService.createUnit(unit);
        return Result.success();
    }

    @RequiresPermission("org:unit:edit")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody OrgUnit unit) {
        if (orgUnitService.checkCodeExists(unit.getUnitCode(), id)) {
            return Result.error("组织编码已存在");
        }

        String attendanceError = validateAttendanceConfig(unit);
        if (attendanceError != null) {
            return Result.error(attendanceError);
        }

        unit.setId(id);
        orgUnitService.updateUnit(unit);
        return Result.success();
    }

    @RequiresPermission("org:unit:delete")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        try {
            orgUnitService.deleteUnit(id);
            return Result.success();
        } catch (RuntimeException exception) {
            return Result.error(exception.getMessage());
        }
    }

    @GetMapping("/check-code")
    public Result<Boolean> checkCode(@RequestParam String unitCode, @RequestParam(required = false) Long excludeId) {
        return Result.success(orgUnitService.checkCodeExists(unitCode, excludeId));
    }

    @GetMapping("/{id}/company")
    public Result<Long> getCompanyId(@PathVariable Long id) {
        return Result.success(orgUnitService.getCompanyId(id));
    }

    private String validateAttendanceConfig(OrgUnit unit) {
        if (unit.getUnitType() == null || unit.getUnitType() != OrgUnit.TYPE_COMPANY) {
            unit.setAttendanceAddress(null);
            unit.setAttendanceLatitude(null);
            unit.setAttendanceLongitude(null);
            unit.setAttendanceRange(null);
            return null;
        }

        boolean hasLatitude = unit.getAttendanceLatitude() != null;
        boolean hasLongitude = unit.getAttendanceLongitude() != null;
        boolean hasAddress = StringUtils.hasText(unit.getAttendanceAddress());
        boolean hasRange = unit.getAttendanceRange() != null;

        if (hasLatitude != hasLongitude) {
            return "请同时填写打卡纬度和经度";
        }

        if (hasLatitude) {
            double latitude = unit.getAttendanceLatitude().doubleValue();
            double longitude = unit.getAttendanceLongitude().doubleValue();
            if (latitude < -90 || latitude > 90) {
                return "打卡纬度必须在 -90 到 90 之间";
            }
            if (longitude < -180 || longitude > 180) {
                return "打卡经度必须在 -180 到 180 之间";
            }

            if (unit.getAttendanceRange() == null) {
                unit.setAttendanceRange(300);
            }
            if (unit.getAttendanceRange() <= 0) {
                return "打卡范围必须大于 0";
            }
        } else {
            if (hasAddress || hasRange) {
                return "配置打卡地址或范围时，请先填写打卡经纬度";
            }
            unit.setAttendanceRange(null);
        }

        if (!StringUtils.hasText(unit.getAttendanceAddress())) {
            unit.setAttendanceAddress(null);
        }
        return null;
    }
}
