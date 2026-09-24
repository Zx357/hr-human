package com.kadmin.web.controller.salary;

import com.kadmin.common.Result;
import com.kadmin.common.annotation.RequiresPermission;
import com.kadmin.salary.domain.SalSalaryScheme;
import com.kadmin.salary.domain.SalSchemeItem;
import com.kadmin.salary.service.SalarySchemeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 薪资方案Controller：方案=薪资项的有序编排，员工挂方案实现灵活结构
 */
@RestController
@RequestMapping("/salary/scheme")
@RequiredArgsConstructor
public class SalarySchemeController {

    private final SalarySchemeService schemeService;

    @GetMapping("/page")
    public Result<com.baomidou.mybatisplus.extension.plugins.pagination.Page<SalSalaryScheme>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword) {
        return Result.success(schemeService.page(pageNum, pageSize, keyword));
    }

    @GetMapping("/list-enabled")
    public Result<List<SalSalaryScheme>> listEnabled() {
        return Result.success(schemeService.listEnabled());
    }

    @GetMapping("/{id}")
    public Result<SalSalaryScheme> detail(@PathVariable Long id) {
        return Result.success(schemeService.getDetail(id));
    }

    /**
     * 新增/更新方案：body = {scheme字段..., items: [{itemId, defaultAmount}]}，数组顺序即计算顺序
     */
    @RequiresPermission("sal:scheme:manage")
    @PostMapping("/save")
    public Result<SalSalaryScheme> save(@RequestBody Map<String, Object> body) {
        SalSalaryScheme scheme = new SalSalaryScheme();
        Object id = body.get("id");
        if (id instanceof Number number) {
            scheme.setId(number.longValue());
        }
        scheme.setSchemeName((String) body.get("schemeName"));
        scheme.setSchemeCode((String) body.get("schemeCode"));
        scheme.setRemark((String) body.get("remark"));
        Object enabled = body.get("enabled");
        scheme.setEnabled(enabled instanceof Number number ? number.intValue() : 1);
        Object sortOrder = body.get("sortOrder");
        scheme.setSortOrder(sortOrder instanceof Number number ? number.intValue() : 0);

        List<SalSchemeItem> items = parseItems(body.get("items"));
        return Result.success(schemeService.saveScheme(scheme, items));
    }

    @RequiresPermission("sal:scheme:manage")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        schemeService.deleteScheme(id);
        return Result.success();
    }

    /** items 数组 → 实体列表（数组顺序即 sortOrder） */
    private List<SalSchemeItem> parseItems(Object raw) {
        if (!(raw instanceof List<?> list)) {
            return List.of();
        }
        List<SalSchemeItem> items = new ArrayList<>();
        for (Object entry : list) {
            if (!(entry instanceof Map<?, ?> map)) {
                continue;
            }
            SalSchemeItem item = new SalSchemeItem();
            Object itemId = map.get("itemId");
            if (itemId instanceof Number number) {
                item.setItemId(number.longValue());
            }
            Object defaultAmount = map.get("defaultAmount");
            if (defaultAmount instanceof Number number) {
                item.setDefaultAmount(BigDecimal.valueOf(number.doubleValue()));
            }
            items.add(item);
        }
        return items;
    }
}
