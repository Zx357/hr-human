package com.kadmin.web.controller.salary;

import com.kadmin.common.Result;
import com.kadmin.common.annotation.RequiresPermission;
import com.kadmin.salary.domain.SalSalaryItemDef;
import com.kadmin.salary.service.SalaryItemDefService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 薪资项定义Controller（公司级薪资项池：固定/比例/考勤联动/手工）
 * 增删改下沉到 SalaryItemDefService（事务与校验集中在服务层），Controller 只做参数转发
 */
@RestController
@RequestMapping("/salary/item")
@RequiredArgsConstructor
public class SalaryItemController {

    private final SalaryItemDefService itemDefService;

    /** 全量项列表（配置池场景数据量小，直接全量返回） */
    @GetMapping("/list")
    public Result<List<SalSalaryItemDef>> list(@RequestParam(required = false) Integer enabled) {
        return Result.success(itemDefService.listItems(enabled));
    }

    /** 新增/更新薪资项 */
    @RequiresPermission("sal:item:manage")
    @PostMapping("/save")
    public Result<Void> save(@RequestBody SalSalaryItemDef itemDef) {
        itemDefService.saveItemDef(itemDef);
        return Result.success();
    }

    /** 删除薪资项（被方案引用时拒绝） */
    @RequiresPermission("sal:item:manage")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        itemDefService.deleteItemDef(id);
        return Result.success();
    }
}
