package com.kadmin.salary.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.kadmin.salary.domain.SalSalaryItemDef;
import com.kadmin.salary.domain.SalSchemeItem;
import com.kadmin.salary.mapper.SalSalaryItemDefMapper;
import com.kadmin.salary.mapper.SalSchemeItemMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 薪资项定义服务（公司级薪资项池：固定/比例/考勤联动/手工）
 * 增删改统一下沉到 Service 层，保证事务边界与校验集中
 */
@Service
@RequiredArgsConstructor
public class SalaryItemDefService extends ServiceImpl<SalSalaryItemDefMapper, SalSalaryItemDef> {

    private final SalSchemeItemMapper schemeItemMapper;

    /** 全量项列表（配置池场景数据量小，直接全量返回） */
    public List<SalSalaryItemDef> listItems(Integer enabled) {
        return list(new LambdaQueryWrapper<SalSalaryItemDef>()
                .eq(enabled != null, SalSalaryItemDef::getEnabled, enabled)
                .orderByAsc(SalSalaryItemDef::getSortOrder)
                .orderByAsc(SalSalaryItemDef::getId));
    }

    /**
     * 新增/更新薪资项（编码唯一、比例/考勤联动项配置完整性校验）
     */
    @Transactional
    public void saveItemDef(SalSalaryItemDef itemDef) {
        if (itemDef.getItemCode() == null || itemDef.getItemCode().isBlank()) {
            throw new IllegalArgumentException("薪资项编码不能为空");
        }
        if (!itemDef.getItemCode().matches("[a-z][a-z0-9_]{1,49}")) {
            throw new IllegalArgumentException("编码仅允许小写字母/数字/下划线，且以字母开头");
        }
        if (itemDef.getItemName() == null || itemDef.getItemName().isBlank()) {
            throw new IllegalArgumentException("薪资项名称不能为空");
        }
        if (itemDef.getValueType() == null) {
            itemDef.setValueType(SalaryCalcEngine.TYPE_FIXED);
        }
        if (itemDef.getDirection() == null) {
            itemDef.setDirection(SalaryCalcEngine.DIRECTION_INCOME);
        }
        if (itemDef.getValueType() == SalaryCalcEngine.TYPE_RATIO
                && (itemDef.getRatioBaseCode() == null || itemDef.getRatioValue() == null)) {
            throw new IllegalArgumentException("比例项必须配置基项编码与比例值");
        }
        if (itemDef.getValueType() == SalaryCalcEngine.TYPE_ATTENDANCE
                && (itemDef.getAttRule() == null || itemDef.getUnitPrice() == null)) {
            throw new IllegalArgumentException("考勤联动项必须配置联动规则与单价");
        }
        if (itemDef.getEnabled() == null) {
            itemDef.setEnabled(1);
        }
        if (itemDef.getSortOrder() == null) {
            itemDef.setSortOrder(0);
        }
        // 编码唯一
        Long dup = count(new LambdaQueryWrapper<SalSalaryItemDef>()
                .eq(SalSalaryItemDef::getItemCode, itemDef.getItemCode())
                .ne(itemDef.getId() != null, SalSalaryItemDef::getId, itemDef.getId()));
        if (dup != null && dup > 0) {
            throw new IllegalArgumentException("薪资项编码已存在: " + itemDef.getItemCode());
        }
        if (itemDef.getId() == null) {
            save(itemDef);
        } else {
            SalSalaryItemDef existing = getById(itemDef.getId());
            if (existing == null) {
                throw new IllegalArgumentException("薪资项不存在");
            }
            updateById(itemDef);
        }
    }

    /**
     * 删除薪资项（被方案引用时拒绝，删除与引用校验同事务）
     */
    @Transactional
    public void deleteItemDef(Long id) {
        Long used = schemeItemMapper.selectCount(new LambdaQueryWrapper<SalSchemeItem>()
                .eq(SalSchemeItem::getItemId, id));
        if (used != null && used > 0) {
            throw new IllegalArgumentException("该项已被 " + used + " 个薪资方案引用，请先从方案中移除");
        }
        removeById(id);
    }
}
