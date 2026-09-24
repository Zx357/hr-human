package com.kadmin.salary.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.kadmin.salary.domain.SalSalaryArchive;
import com.kadmin.salary.domain.SalSalaryItemDef;
import com.kadmin.salary.domain.SalSalaryScheme;
import com.kadmin.salary.domain.SalSchemeItem;
import com.kadmin.salary.mapper.SalSalaryArchiveMapper;
import com.kadmin.salary.mapper.SalSalaryItemDefMapper;
import com.kadmin.salary.mapper.SalSalarySchemeMapper;
import com.kadmin.salary.mapper.SalSchemeItemMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 薪资方案服务：方案 CRUD + 方案明细编排（含比例项引用顺序校验）
 */
@Service
@RequiredArgsConstructor
public class SalarySchemeService extends ServiceImpl<SalSalarySchemeMapper, SalSalaryScheme> {

    private final SalSchemeItemMapper schemeItemMapper;
    private final SalSalaryItemDefMapper itemDefMapper;
    private final SalSalaryArchiveMapper archiveMapper;

    public Page<SalSalaryScheme> page(int pageNum, int pageSize, String keyword) {
        Page<SalSalaryScheme> page = page(new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<SalSalaryScheme>()
                        .and(keyword != null && !keyword.isBlank(), w -> w
                                .like(SalSalaryScheme::getSchemeName, keyword)
                                .or().like(SalSalaryScheme::getSchemeCode, keyword))
                        .orderByAsc(SalSalaryScheme::getSortOrder)
                        .orderByDesc(SalSalaryScheme::getId));
        fillItemCount(page.getRecords());
        return page;
    }

    /**
     * 启用中的方案简表（档案绑定下拉用）
     */
    public List<SalSalaryScheme> listEnabled() {
        return list(new LambdaQueryWrapper<SalSalaryScheme>()
                .eq(SalSalaryScheme::getEnabled, 1)
                .orderByAsc(SalSalaryScheme::getSortOrder));
    }

    /**
     * 方案详情（含明细行，按顺序）
     */
    public SalSalaryScheme getDetail(Long id) {
        SalSalaryScheme scheme = getById(id);
        if (scheme == null) {
            throw new IllegalArgumentException("方案不存在");
        }
        scheme.setItems(listSchemeItems(id));
        return scheme;
    }

    public List<SalSchemeItem> listSchemeItems(Long schemeId) {
        List<SalSchemeItem> items = schemeItemMapper.selectList(new LambdaQueryWrapper<SalSchemeItem>()
                .eq(SalSchemeItem::getSchemeId, schemeId)
                .orderByAsc(SalSchemeItem::getSortOrder));
        fillItemInfo(items);
        return items;
    }

    /**
     * 新增/更新方案并整体替换明细（items 数组顺序即 sortOrder）
     *
     * @return 校验后的比例项引用顺序问题会直接抛异常，整体回滚
     */
    @Transactional
    public SalSalaryScheme saveScheme(SalSalaryScheme scheme, List<SalSchemeItem> items) {
        if (scheme.getSchemeName() == null || scheme.getSchemeName().isBlank()) {
            throw new IllegalArgumentException("方案名称不能为空");
        }
        if (items != null && !items.isEmpty()) {
            // 明细项不能重复
            Set<Long> itemIds = new HashSet<>();
            for (SalSchemeItem item : items) {
                if (item.getItemId() == null || !itemIds.add(item.getItemId())) {
                    throw new IllegalArgumentException("方案明细存在重复或缺失的薪资项");
                }
            }
        }
        if (scheme.getId() == null) {
            save(scheme);
        } else if (getById(scheme.getId()) == null) {
            throw new IllegalArgumentException("方案不存在");
        } else {
            updateById(scheme);
        }

        // 整体替换明细
        schemeItemMapper.delete(new LambdaQueryWrapper<SalSchemeItem>()
                .eq(SalSchemeItem::getSchemeId, scheme.getId()));
        if (items != null && !items.isEmpty()) {
            int order = 1;
            for (SalSchemeItem item : items) {
                item.setId(null);
                item.setSchemeId(scheme.getId());
                item.setSortOrder(order++);
                schemeItemMapper.insert(item);
            }
            validateRatioReferenceOrder(scheme.getId(), items);
        }
        return scheme;
    }

    /**
     * 删除方案（有员工档案引用时拒绝）
     */
    @Transactional
    public void deleteScheme(Long id) {
        Long used = archiveMapper.selectCount(new LambdaQueryWrapper<SalSalaryArchive>()
                .eq(SalSalaryArchive::getSchemeId, id));
        if (used != null && used > 0) {
            throw new IllegalArgumentException("方案已被 " + used + " 名员工的薪资档案引用，请先解除绑定");
        }
        schemeItemMapper.delete(new LambdaQueryWrapper<SalSchemeItem>()
                .eq(SalSchemeItem::getSchemeId, id));
        removeById(id);
    }

    /**
     * 比例项校验：ratioBaseCode 必须是同方案中排在它前面的项的编码（天然无环）
     */
    private void validateRatioReferenceOrder(Long schemeId, List<SalSchemeItem> items) {
        Map<Long, SalSalaryItemDef> defs = itemDefMapper.selectBatchIds(
                        items.stream().map(SalSchemeItem::getItemId).toList()).stream()
                .collect(Collectors.toMap(SalSalaryItemDef::getId, Function.identity(), (a, b) -> a));
        Set<String> seenCodes = new HashSet<>();
        for (SalSchemeItem item : items) {
            SalSalaryItemDef def = defs.get(item.getItemId());
            if (def == null) {
                throw new IllegalArgumentException("薪资项不存在或已删除: " + item.getItemId());
            }
            if (def.getValueType() != null && def.getValueType() == SalaryCalcEngine.TYPE_RATIO) {
                String base = def.getRatioBaseCode();
                if (base == null || base.isBlank()) {
                    throw new IllegalArgumentException("比例项[" + def.getItemName() + "]未配置基项编码");
                }
                if (!seenCodes.contains(base)) {
                    throw new IllegalArgumentException("比例项[" + def.getItemName() + "]的基项["
                            + base + "]必须在该项之前加入方案");
                }
            }
            seenCodes.add(def.getItemCode());
        }
    }

    private void fillItemInfo(List<SalSchemeItem> items) {
        if (items == null || items.isEmpty()) {
            return;
        }
        Map<Long, SalSalaryItemDef> defs = itemDefMapper.selectBatchIds(
                        items.stream().map(SalSchemeItem::getItemId).distinct().toList()).stream()
                .collect(Collectors.toMap(SalSalaryItemDef::getId, Function.identity(), (a, b) -> a));
        for (SalSchemeItem item : items) {
            SalSalaryItemDef def = defs.get(item.getItemId());
            if (def != null) {
                item.setItemCode(def.getItemCode());
                item.setItemName(def.getItemName());
            }
        }
    }

    private void fillItemCount(List<SalSalaryScheme> schemes) {
        if (schemes == null || schemes.isEmpty()) {
            return;
        }
        for (SalSalaryScheme scheme : schemes) {
            Long count = schemeItemMapper.selectCount(new LambdaQueryWrapper<SalSchemeItem>()
                    .eq(SalSchemeItem::getSchemeId, scheme.getId()));
            scheme.setItemCount(count != null ? count.intValue() : 0);
        }
    }
}
