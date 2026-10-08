package com.kadmin.web.controller.salary;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kadmin.common.Result;
import com.kadmin.common.annotation.RequiresPermission;
import com.kadmin.salary.domain.SalSalaryArchive;
import com.kadmin.salary.domain.SalSalaryArchiveItem;
import com.kadmin.salary.service.SalaryArchiveService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 薪资档案Controller：员工挂接薪资方案，定薪时展开固定项明细
 */
@RestController
@RequestMapping("/salary/archive")
@RequiredArgsConstructor
public class SalaryArchiveController {

    private final SalaryArchiveService archiveService;

    /** 员工档案分页（以在职员工为主表左联档案） */
    @GetMapping("/employee-page")
    public Result<Page<Map<String, Object>>> employeePage(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long companyId,
            @RequestParam(required = false) Long deptId,
            @RequestParam(required = false) String employeeName,
            @RequestParam(required = false) String employeeNo,
            @RequestParam(required = false) Boolean archivedOnly) {
        return Result.success(archiveService.pageEmployees(pageNum, pageSize, companyId, deptId,
                employeeName, employeeNo, archivedOnly));
    }

    /** 档案明细（固定项） */
    @GetMapping("/{archiveId}/items")
    public Result<List<SalSalaryArchiveItem>> items(@PathVariable Long archiveId) {
        return Result.success(archiveService.listArchiveItems(archiveId));
    }

    /**
     * 绑定/换绑方案（换绑按新方案重新展开），body = {employeeId, schemeId, effectiveDate, remark}
     */
    @RequiresPermission("sal:archive:manage")
    @PostMapping("/bind")
    public Result<SalSalaryArchive> bind(@RequestBody Map<String, Object> body) {
        Long employeeId = longOf(body.get("employeeId"));
        Long schemeId = longOf(body.get("schemeId"));
        if (employeeId == null || schemeId == null) {
            return Result.error("参数错误：员工与方案必填");
        }
        LocalDate effectiveDate = body.get("effectiveDate") != null
                ? LocalDate.parse(String.valueOf(body.get("effectiveDate"))) : null;
        return Result.success(archiveService.bindArchive(employeeId, schemeId, effectiveDate,
                (String) body.get("remark")));
    }

    /**
     * 批量绑定/换绑方案，body = {employeeIds:[Long], schemeId:Long, effectiveDate?, remark?}
     * 单事务内整体成功或回滚，返回成功换绑的档案数
     */
    @RequiresPermission("sal:archive:manage")
    @PostMapping("/batch-bind")
    public Result<Integer> batchBind(@RequestBody Map<String, Object> body) {
        Long schemeId = longOf(body.get("schemeId"));
        if (schemeId == null) {
            return Result.error("参数错误：方案必填");
        }
        List<Long> employeeIds = longListOf(body.get("employeeIds"));
        if (employeeIds.isEmpty()) {
            return Result.error("参数错误：请选择员工");
        }
        LocalDate effectiveDate = body.get("effectiveDate") != null
                ? LocalDate.parse(String.valueOf(body.get("effectiveDate"))) : null;
        return Result.success(archiveService.batchBindArchives(employeeIds, schemeId, effectiveDate,
                (String) body.get("remark")).size());
    }

    /** 更新固定项金额，body = [{itemId, amount}] */
    @RequiresPermission("sal:archive:manage")
    @PostMapping("/{archiveId}/items")
    public Result<Void> updateItems(@PathVariable Long archiveId,
            @RequestBody List<SalSalaryArchiveItem> items) {
        archiveService.updateArchiveItems(archiveId, items);
        return Result.success();
    }

    /** 解除绑定 */
    @RequiresPermission("sal:archive:manage")
    @DeleteMapping("/{archiveId}")
    public Result<Void> unbind(@PathVariable Long archiveId) {
        archiveService.unbindArchive(archiveId);
        return Result.success();
    }

    private Long longOf(Object value) {
        return value instanceof Number number ? number.longValue() : null;
    }

    private List<Long> longListOf(Object value) {
        if (!(value instanceof List<?> list)) {
            return List.of();
        }
        return list.stream()
                .filter(item -> item instanceof Number)
                .map(item -> ((Number) item).longValue())
                .toList();
    }
}
