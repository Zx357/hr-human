package com.kadmin.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kadmin.common.Result;
import com.kadmin.entity.HrContract;
import com.kadmin.service.ContractService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 合同管理Controller
 */
@RestController
@RequestMapping("/hr/contract")
@RequiredArgsConstructor
public class ContractController {

    private final ContractService contractService;

    /**
     * 分页查询合同列表
     */
    @GetMapping("/page")
    public Result<Page<HrContract>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String contractNo,
            @RequestParam(required = false) String employeeName,
            @RequestParam(required = false) String employeeNo,
            @RequestParam(required = false) String contractType,
            @RequestParam(required = false) Integer status) {
        Page<HrContract> page = contractService.getContractPage(pageNum, pageSize, contractNo, employeeName, employeeNo, contractType, status);
        return Result.success(page);
    }

    /**
     * 获取合同详情
     */
    @GetMapping("/{id}")
    public Result<HrContract> getDetail(@PathVariable Long id) {
        HrContract contract = contractService.getById(id);
        return Result.success(contract);
    }

    /**
     * 新增合同
     */
    @PostMapping
    public Result<Void> add(@RequestBody HrContract contract) {
        contractService.addContract(contract);
        return Result.success();
    }

    /**
     * 修改合同
     */
    @PutMapping
    public Result<Void> update(@RequestBody HrContract contract) {
        contractService.updateContract(contract);
        return Result.success();
    }

    /**
     * 删除合同
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        contractService.deleteContract(id);
        return Result.success();
    }
}
