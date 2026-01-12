package com.kadmin.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.kadmin.common.Result;
import com.kadmin.entity.OrgCompany;
import com.kadmin.service.CompanyService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 公司管理控制器
 */
@RestController
@RequestMapping("/organization/company")
@RequiredArgsConstructor
public class CompanyController {

    private final CompanyService companyService;

    /**
     * 分页查询公司列表
     */
    @GetMapping("/page")
    public Result<Map<String, Object>> getCompanyPage(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String companyName,
            @RequestParam(required = false) Integer status) {

        IPage<OrgCompany> page = companyService.getCompanyPage(pageNum, pageSize, companyName, status);

        Map<String, Object> result = new HashMap<>();
        result.put("records", page.getRecords());
        result.put("total", page.getTotal());
        result.put("current", page.getCurrent());
        result.put("size", page.getSize());

        return Result.success(result);
    }

    /**
     * 获取所有公司列表
     */
    @GetMapping("/list")
    public Result<List<OrgCompany>> getAllCompanies() {
        return Result.success(companyService.getAllCompanies());
    }

    /**
     * 根据ID获取公司详情
     */
    @GetMapping("/{id}")
    public Result<OrgCompany> getCompanyById(@PathVariable Long id) {
        return Result.success(companyService.getCompanyById(id));
    }

    /**
     * 创建公司
     */
    @PostMapping
    public Result<Boolean> createCompany(@RequestBody OrgCompany company) {
        return Result.success(companyService.createCompany(company));
    }

    /**
     * 更新公司
     */
    @PutMapping("/{id}")
    public Result<Boolean> updateCompany(@PathVariable Long id, @RequestBody OrgCompany company) {
        company.setId(id);
        return Result.success(companyService.updateCompany(company));
    }

    /**
     * 删除公司
     */
    @DeleteMapping("/{id}")
    public Result<Boolean> deleteCompany(@PathVariable Long id) {
        return Result.success(companyService.deleteCompany(id));
    }
}