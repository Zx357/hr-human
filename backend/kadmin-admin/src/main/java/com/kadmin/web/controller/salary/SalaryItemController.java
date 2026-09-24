package com.kadmin.web.controller.salary;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.kadmin.common.Result;
import com.kadmin.common.annotation.RequiresPermission;
import com.kadmin.salary.domain.SalSalaryItemDef;
import com.kadmin.salary.domain.SalSchemeItem;
import com.kadmin.salary.mapper.SalSalaryItemDefMapper;
import com.kadmin.salary.mapper.SalSchemeItemMapper;
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
 */
@RestController
@RequestMapping("/salary/item")
@RequiredArgsConstructor
public class SalaryItemController {

    private final SalSalaryItemDefMapper itemDefMapper;
    private final SalSchemeItemMapper schemeItemMapper;

    /** 全量项列表（配置池场景数据量小，直接全量返回） */
    @GetMapping("/list")
    public Result<List<SalSalaryItemDef>> list(@RequestParam(required = false) Integer enabled) {
        return Result.success(itemDefMapper.selectList(new LambdaQueryWrapper<SalSalaryItemDef>()
                .eq(enabled != null, SalSalaryItemDef::getEnabled, enabled)
                .orderByAsc(SalSalaryItemDef::getSortOrder)
                .orderByAsc(SalSalaryItemDef::getId)));
    }

    /** 新增/更新薪资项 */
    @RequiresPermission("sal:item:manage")
    @PostMapping("/save")
    public Result<Void> save(@RequestBody SalSalaryItemDef itemDef) {
        if (itemDef.getItemCode() == null || itemDef.getItemCode().isBlank()) {
            return Result.error("薪资项编码不能为空");
        }
        if (!itemDef.getItemCode().matches("[a-z][a-z0-9_]{1,49}")) {
            return Result.error("编码仅允许小写字母/数字/下划线，且以字母开头");
        }
        if (itemDef.getItemName() == null || itemDef.getItemName().isBlank()) {
            return Result.error("薪资项名称不能为空");
        }
        if (itemDef.getValueType() == null) {
            itemDef.setValueType(1);
        }
        if (itemDef.getDirection() == null) {
            itemDef.setDirection(1);
        }
        if (itemDef.getValueType() == 2
                && (itemDef.getRatioBaseCode() == null || itemDef.getRatioValue() == null)) {
            return Result.error("比例项必须配置基项编码与比例值");
        }
        if (itemDef.getValueType() == 3 && (itemDef.getAttRule() == null || itemDef.getUnitPrice() == null)) {
            return Result.error("考勤联动项必须配置联动规则与单价");
        }
        if (itemDef.getEnabled() == null) {
            itemDef.setEnabled(1);
        }
        if (itemDef.getSortOrder() == null) {
            itemDef.setSortOrder(0);
        }
        // 编码唯一
        Long dup = itemDefMapper.selectCount(new LambdaQueryWrapper<SalSalaryItemDef>()
                .eq(SalSalaryItemDef::getItemCode, itemDef.getItemCode())
                .ne(itemDef.getId() != null, SalSalaryItemDef::getId, itemDef.getId()));
        if (dup != null && dup > 0) {
            return Result.error("薪资项编码已存在: " + itemDef.getItemCode());
        }
        if (itemDef.getId() == null) {
            itemDefMapper.insert(itemDef);
        } else if (itemDefMapper.selectById(itemDef.getId()) == null) {
            return Result.error("薪资项不存在");
        } else {
            itemDefMapper.updateById(itemDef);
        }
        return Result.success();
    }

    /** 删除薪资项（被方案引用时拒绝） */
    @RequiresPermission("sal:item:manage")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        Long used = schemeItemMapper.selectCount(new LambdaQueryWrapper<SalSchemeItem>()
                .eq(SalSchemeItem::getItemId, id));
        if (used != null && used > 0) {
            return Result.error("该项已被 " + used + " 个薪资方案引用，请先从方案中移除");
        }
        itemDefMapper.deleteById(id);
        return Result.success();
    }
}
