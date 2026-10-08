package com.kadmin.salary.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.kadmin.hr.domain.HrEmployee;
import com.kadmin.hr.mapper.EmployeeMapper;
import com.kadmin.organization.domain.OrgUnit;
import com.kadmin.organization.mapper.OrgUnitMapper;
import com.kadmin.salary.domain.SalSalaryArchive;
import com.kadmin.salary.domain.SalSalaryArchiveItem;
import com.kadmin.salary.domain.SalSalaryItemDef;
import com.kadmin.salary.domain.SalSalaryScheme;
import com.kadmin.salary.domain.SalSchemeItem;
import com.kadmin.salary.mapper.SalSalaryArchiveItemMapper;
import com.kadmin.salary.mapper.SalSalaryArchiveMapper;
import com.kadmin.salary.mapper.SalSalaryItemDefMapper;
import com.kadmin.salary.mapper.SalSchemeItemMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 薪资档案服务：员工 ↔ 方案绑定，定薪时把方案的固定项展开成档案明细（个人可在明细上覆盖金额）
 */
@Service
@RequiredArgsConstructor
public class SalaryArchiveService extends ServiceImpl<SalSalaryArchiveMapper, SalSalaryArchive> {

    private final EmployeeMapper employeeMapper;
    private final OrgUnitMapper orgUnitMapper;
    private final SalSchemeItemMapper schemeItemMapper;
    private final SalSalaryItemDefMapper itemDefMapper;
    private final SalSalaryArchiveItemMapper archiveItemMapper;
    private final SalarySchemeService schemeService;

    /**
     * 员工薪资档案分页：以在职员工为主表左联档案（未绑定的员工也要能看到并绑定）
     */
    public Page<Map<String, Object>> pageEmployees(int pageNum, int pageSize, Long companyId, Long deptId,
            String employeeName, String employeeNo, Boolean archivedOnly) {
        LambdaQueryWrapper<HrEmployee> wrapper = new LambdaQueryWrapper<HrEmployee>()
                .eq(HrEmployee::getStatus, 1)
                .like(employeeName != null && !employeeName.isBlank(), HrEmployee::getName, employeeName)
                .like(employeeNo != null && !employeeNo.isBlank(), HrEmployee::getEmployeeNo, employeeNo)
                .eq(deptId != null, HrEmployee::getDeptId, deptId);
        if (companyId != null) {
            List<Long> orgIds = expandOrgIds(companyId);
            wrapper.in(HrEmployee::getDeptId, orgIds);
        }
        Page<HrEmployee> employeePage = employeeMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);

        List<Long> employeeIds = employeePage.getRecords().stream().map(HrEmployee::getId).toList();
        Map<Long, SalSalaryArchive> archiveMap = employeeIds.isEmpty() ? Map.of()
                : list(new LambdaQueryWrapper<SalSalaryArchive>()
                        .in(SalSalaryArchive::getEmployeeId, employeeIds)).stream()
                        .collect(Collectors.toMap(SalSalaryArchive::getEmployeeId, Function.identity(), (a, b) -> a));
        // 批量预载部门与上级公司（单次/少量 in 查询，替代逐员工查部门名的 N+1）
        Map<Long, OrgUnit> orgCache = preloadOrgChain(employeePage.getRecords());
        // 过滤"仅看已建档"
        List<Map<String, Object>> rows = new ArrayList<>();
        Map<Long, String> schemeNames = loadSchemeNames(archiveMap.values());
        for (HrEmployee employee : employeePage.getRecords()) {
            SalSalaryArchive archive = archiveMap.get(employee.getId());
            if (Boolean.TRUE.equals(archivedOnly) && archive == null) {
                continue;
            }
            Map<String, Object> row = new HashMap<>();
            row.put("employeeId", employee.getId());
            row.put("employeeNo", employee.getEmployeeNo());
            row.put("employeeName", employee.getName());
            OrgUnit deptUnit = employee.getDeptId() == null ? null : orgCache.get(employee.getDeptId());
            row.put("deptName", deptUnit != null ? deptUnit.getUnitName() : null);
            row.put("companyName", resolveCompanyName(deptUnit, orgCache));
            row.put("archiveId", archive != null ? archive.getId() : null);
            row.put("schemeId", archive != null ? archive.getSchemeId() : null);
            row.put("schemeName", archive != null ? schemeNames.get(archive.getSchemeId()) : null);
            row.put("effectiveDate", archive != null ? archive.getEffectiveDate() : null);
            row.put("remark", archive != null ? archive.getRemark() : null);
            rows.add(row);
        }

        Page<Map<String, Object>> result = new Page<>(pageNum, pageSize, employeePage.getTotal());
        result.setRecords(rows);
        return result;
    }

    /**
     * 绑定/换绑方案：换绑时按新方案重新展开档案明细（固定项取方案默认值）
     */
    @Transactional
    public SalSalaryArchive bindArchive(Long employeeId, Long schemeId, LocalDate effectiveDate, String remark) {
        HrEmployee employee = employeeMapper.selectById(employeeId);
        if (employee == null || employee.getStatus() == null || employee.getStatus() != 1) {
            throw new IllegalArgumentException("员工不存在或非在职状态");
        }
        SalSalaryScheme scheme = schemeService.getById(schemeId);
        if (scheme == null || scheme.getEnabled() == null || scheme.getEnabled() != 1) {
            throw new IllegalArgumentException("薪资方案不存在或未启用");
        }

        SalSalaryArchive archive = getOne(new LambdaQueryWrapper<SalSalaryArchive>()
                .eq(SalSalaryArchive::getEmployeeId, employeeId).last("LIMIT 1"));
        boolean isNew = archive == null;
        if (isNew) {
            archive = new SalSalaryArchive();
            archive.setEmployeeId(employeeId);
        }
        archive.setSchemeId(schemeId);
        archive.setEffectiveDate(effectiveDate != null ? effectiveDate : LocalDate.now());
        archive.setRemark(remark);
        if (isNew) {
            save(archive);
        } else {
            updateById(archive);
        }
        expandArchiveItems(archive.getId(), schemeId);
        return archive;
    }

    /**
     * 批量绑定/换绑方案：给多个员工换绑同一方案（复用单员工换绑逻辑，单事务内整体成功或回滚）
     *
     * @return 成功换绑的档案列表
     */
    @Transactional
    public List<SalSalaryArchive> batchBindArchives(List<Long> employeeIds, Long schemeId,
            LocalDate effectiveDate, String remark) {
        if (employeeIds == null || employeeIds.isEmpty()) {
            throw new IllegalArgumentException("请选择要绑定的员工");
        }
        if (schemeId == null) {
            throw new IllegalArgumentException("请选择薪资方案");
        }
        // 先校验方案有效性，避免逐员工重复查询
        SalSalaryScheme scheme = schemeService.getById(schemeId);
        if (scheme == null || scheme.getEnabled() == null || scheme.getEnabled() != 1) {
            throw new IllegalArgumentException("薪资方案不存在或未启用");
        }
        List<SalSalaryArchive> result = new ArrayList<>();
        for (Long employeeId : employeeIds.stream().filter(Objects::nonNull).distinct().toList()) {
            result.add(bindArchive(employeeId, schemeId, effectiveDate, remark));
        }
        return result;
    }

    /**
     * 按方案展开固定项明细（先删后插覆盖既有明细）：
     * 换绑方案时保留旧档案中带个人覆盖标记（custom_flag=1）且新方案仍包含的薪资项金额，
     * 其余项取新方案默认值，避免换绑清空个人定薪
     */
    @Transactional
    public void expandArchiveItems(Long archiveId, Long schemeId) {
        // 旧明细中的个人覆盖值（custom_flag=1），换绑后同薪资项保留
        Map<Long, SalSalaryArchiveItem> overriddenItems = archiveItemMapper.selectList(
                        new LambdaQueryWrapper<SalSalaryArchiveItem>()
                                .eq(SalSalaryArchiveItem::getArchiveId, archiveId))
                .stream()
                .filter(item -> item.getCustomFlag() != null && item.getCustomFlag() == 1)
                .collect(Collectors.toMap(SalSalaryArchiveItem::getItemId, Function.identity(), (a, b) -> a));

        archiveItemMapper.delete(new LambdaQueryWrapper<SalSalaryArchiveItem>()
                .eq(SalSalaryArchiveItem::getArchiveId, archiveId));
        List<SalSchemeItem> schemeItems = schemeItemMapper.selectList(new LambdaQueryWrapper<SalSchemeItem>()
                .eq(SalSchemeItem::getSchemeId, schemeId)
                .orderByAsc(SalSchemeItem::getSortOrder));
        if (schemeItems.isEmpty()) {
            return;
        }
        Map<Long, SalSalaryItemDef> defs = itemDefMapper.selectBatchIds(
                        schemeItems.stream().map(SalSchemeItem::getItemId).toList()).stream()
                .collect(Collectors.toMap(SalSalaryItemDef::getId, Function.identity(), (a, b) -> a));
        for (SalSchemeItem schemeItem : schemeItems) {
            SalSalaryItemDef def = defs.get(schemeItem.getItemId());
            // 只物化固定项（比例/考勤联动/手工在核算时动态求值）
            if (def == null || def.getValueType() == null || def.getValueType() != SalaryCalcEngine.TYPE_FIXED) {
                continue;
            }
            SalSalaryArchiveItem archiveItem = new SalSalaryArchiveItem();
            archiveItem.setArchiveId(archiveId);
            archiveItem.setItemId(schemeItem.getItemId());
            SalSalaryArchiveItem overridden = overriddenItems.get(schemeItem.getItemId());
            if (overridden != null) {
                // 个人覆盖值优先于新方案默认值，覆盖标记一并保留
                archiveItem.setAmount(overridden.getAmount() != null ? overridden.getAmount() : BigDecimal.ZERO);
                archiveItem.setCustomFlag(1);
            } else {
                archiveItem.setAmount(schemeItem.getDefaultAmount() != null ? schemeItem.getDefaultAmount()
                        : BigDecimal.ZERO);
                archiveItem.setCustomFlag(0);
            }
            archiveItemMapper.insert(archiveItem);
        }
    }

    /**
     * 档案明细（固定项，含项信息）
     */
    public List<SalSalaryArchiveItem> listArchiveItems(Long archiveId) {
        List<SalSalaryArchiveItem> items = archiveItemMapper.selectList(new LambdaQueryWrapper<SalSalaryArchiveItem>()
                .eq(SalSalaryArchiveItem::getArchiveId, archiveId));
        if (!items.isEmpty()) {
            Map<Long, SalSalaryItemDef> defs = itemDefMapper.selectBatchIds(
                            items.stream().map(SalSalaryArchiveItem::getItemId).toList()).stream()
                    .collect(Collectors.toMap(SalSalaryItemDef::getId, Function.identity(), (a, b) -> a));
            for (SalSalaryArchiveItem item : items) {
                SalSalaryItemDef def = defs.get(item.getItemId());
                if (def != null) {
                    item.setItemCode(def.getItemCode());
                    item.setItemName(def.getItemName());
                }
            }
        }
        return items;
    }

    /**
     * 更新固定项金额（个人覆盖方案默认值，更新后打上个人覆盖标记）
     */
    @Transactional
    public void updateArchiveItems(Long archiveId, List<SalSalaryArchiveItem> inputs) {
        SalSalaryArchive archive = getById(archiveId);
        if (archive == null) {
            throw new IllegalArgumentException("薪资档案不存在");
        }
        Map<Long, SalSalaryArchiveItem> existing = archiveItemMapper.selectList(
                        new LambdaQueryWrapper<SalSalaryArchiveItem>()
                                .eq(SalSalaryArchiveItem::getArchiveId, archiveId)).stream()
                .collect(Collectors.toMap(SalSalaryArchiveItem::getItemId, Function.identity(), (a, b) -> a));
        for (SalSalaryArchiveItem input : inputs) {
            if (input.getItemId() == null || input.getAmount() == null || input.getAmount().compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException("明细项金额不合法");
            }
            SalSalaryArchiveItem current = existing.get(input.getItemId());
            if (current == null) {
                throw new IllegalArgumentException("明细项不属于该档案，请重新按方案展开");
            }
            current.setAmount(input.getAmount());
            // 个人覆盖标记：换绑方案时保留该金额
            current.setCustomFlag(1);
            archiveItemMapper.updateById(current);
        }
    }

    /**
     * 解除绑定并删除档案明细
     */
    @Transactional
    public void unbindArchive(Long archiveId) {
        archiveItemMapper.delete(new LambdaQueryWrapper<SalSalaryArchiveItem>()
                .eq(SalSalaryArchiveItem::getArchiveId, archiveId));
        removeById(archiveId);
    }

    private Map<Long, String> loadSchemeNames(Iterable<SalSalaryArchive> archives) {
        Set<Long> ids = new LinkedHashSet<>();
        for (SalSalaryArchive archive : archives) {
            ids.add(archive.getSchemeId());
        }
        return ids.isEmpty() ? Map.of()
                : schemeService.listByIds(ids).stream()
                        .collect(Collectors.toMap(SalSalaryScheme::getId, SalSalaryScheme::getSchemeName));
    }

    private List<Long> expandOrgIds(Long orgId) {
        List<Long> childIds = orgUnitMapper.selectOrgAndChildIds(orgId);
        return childIds != null && !childIds.isEmpty() ? childIds : List.of(orgId);
    }

    /**
     * 批量预载员工部门及其上级组织链（公司归属逐级向上）：
     * 先一次 in 查所有部门，再把缺失的父节点循环补齐，循环内纯内存
     */
    private Map<Long, OrgUnit> preloadOrgChain(List<HrEmployee> employees) {
        Map<Long, OrgUnit> orgCache = new HashMap<>();
        Set<Long> pendingOrgIds = new LinkedHashSet<>();
        for (HrEmployee employee : employees) {
            if (employee.getDeptId() != null) {
                pendingOrgIds.add(employee.getDeptId());
            }
        }
        while (!pendingOrgIds.isEmpty()) {
            List<Long> toLoad = pendingOrgIds.stream().filter(id -> !orgCache.containsKey(id)).toList();
            pendingOrgIds.clear();
            if (toLoad.isEmpty()) {
                break;
            }
            for (OrgUnit unit : orgUnitMapper.selectBatchIds(toLoad)) {
                orgCache.put(unit.getId(), unit);
                if (unit.getParentId() != null && unit.getParentId() != 0) {
                    pendingOrgIds.add(unit.getParentId());
                }
            }
        }
        return orgCache;
    }

    /**
     * 从预载的组织缓存向上解析公司名称（部门本身是公司节点时直接返回）
     */
    private String resolveCompanyName(OrgUnit deptUnit, Map<Long, OrgUnit> orgCache) {
        if (deptUnit == null) {
            return null;
        }
        OrgUnit cursor = deptUnit;
        int guard = 0;
        while (cursor != null && guard++ < 20) {
            if (cursor.getUnitType() != null && cursor.getUnitType() == OrgUnit.TYPE_COMPANY) {
                return cursor.getUnitName();
            }
            cursor = cursor.getParentId() == null || cursor.getParentId() == 0 ? null
                    : orgCache.get(cursor.getParentId());
        }
        return null;
    }
}
