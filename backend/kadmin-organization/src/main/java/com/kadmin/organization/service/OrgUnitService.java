package com.kadmin.organization.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kadmin.organization.domain.dto.OrgStatisticsDTO;
import com.kadmin.organization.domain.OrgUnit;
import com.kadmin.organization.mapper.OrgUnitMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrgUnitService extends ServiceImpl<OrgUnitMapper, OrgUnit> {

    /**
     * 获取完整的组织架构树
     */
    public List<OrgUnit> getOrgTree() {
        List<OrgUnit> allUnits = baseMapper.selectAllWithLeader();
        return buildTree(allUnits, 0L);
    }

    /**
     * 获取带员工数量的组织架构树
     */
    public List<OrgUnit> getOrgTreeWithCount() {
        List<OrgUnit> allUnits = baseMapper.selectAllWithLeader();
        List<OrgUnit> tree = buildTree(allUnits, 0L);
        // 计算每个节点的员工数量
        calculateEmployeeCount(tree);
        return tree;
    }

    /**
     * 计算员工数量：一次 GROUP BY 统计各部门直接人数，再自底向上累加（替代每节点2条SQL的N+1）
     */
    private int calculateEmployeeCount(List<OrgUnit> units) {
        Map<Long, Integer> directCount = new java.util.HashMap<>();
        for (Map<String, Object> row : baseMapper.countEmployeesGroupByDept()) {
            Object deptId = row.get("dept_id");
            Object cnt = row.get("cnt");
            if (deptId instanceof Number id && cnt instanceof Number c) {
                directCount.put(id.longValue(), c.intValue());
            }
        }
        return calculateEmployeeCount(units, directCount);
    }

    private int calculateEmployeeCount(List<OrgUnit> units, Map<Long, Integer> directCount) {
        int total = 0;
        for (OrgUnit unit : units) {
            int self = directCount.getOrDefault(unit.getId(), 0);
            int children = unit.getChildren() != null && !unit.getChildren().isEmpty()
                    ? calculateEmployeeCount(unit.getChildren(), directCount)
                    : 0;
            unit.setEmployeeCount(self + children);
            total += self + children;
        }
        return total;
    }

    /**
     * 获取组织统计数据
     */
    public OrgStatisticsDTO getStatistics(Long orgId) {
        OrgStatisticsDTO dto = new OrgStatisticsDTO();

        // 获取要统计的部门ID列表
        List<Long> deptIds;
        if (orgId == null || orgId == 0) {
            // 统计所有
            deptIds = baseMapper.selectAllOrgIds();
        } else {
            // 统计指定组织及其子组织
            deptIds = baseMapper.selectOrgAndChildIds(orgId);
        }

        // 员工总数
        Integer totalCount = baseMapper.countEmployeesByDeptIds(deptIds);
        dto.setTotalCount(totalCount != null ? totalCount : 0);

        // 学历分布
        List<Map<String, Object>> educationData = baseMapper.countByEducation(deptIds);
        dto.setEducationDistribution(convertToDistributionItems(educationData));

        // 性别分布
        List<Map<String, Object>> genderData = baseMapper.countByGender(deptIds);
        dto.setGenderDistribution(convertToDistributionItems(genderData));

        // 年龄分布
        List<Map<String, Object>> ageData = baseMapper.countByAgeRange(deptIds);
        dto.setAgeDistribution(convertToAgeDistributionItems(ageData));

        // 在职状态分布
        List<Map<String, Object>> statusData = baseMapper.countByStatus(deptIds);
        dto.setStatusDistribution(convertToDistributionItems(statusData));

        // 员工类型分布
        List<Map<String, Object>> typeData = baseMapper.countByEmployeeType(deptIds);
        dto.setEmployeeTypeDistribution(convertToDistributionItems(typeData));

        return dto;
    }

    private List<OrgStatisticsDTO.DistributionItem> convertToDistributionItems(List<Map<String, Object>> data) {
        if (data == null)
            return new ArrayList<>();
        return data.stream()
                .map(m -> new OrgStatisticsDTO.DistributionItem(
                        String.valueOf(m.get("name")),
                        ((Number) m.get("value")).intValue()))
                .collect(Collectors.toList());
    }

    private List<OrgStatisticsDTO.AgeDistributionItem> convertToAgeDistributionItems(List<Map<String, Object>> data) {
        if (data == null)
            return new ArrayList<>();
        return data.stream()
                .map(m -> new OrgStatisticsDTO.AgeDistributionItem(
                        String.valueOf(m.get("age_range")),
                        ((Number) m.get("count")).intValue()))
                .collect(Collectors.toList());
    }

    /**
     * 获取指定类型的组织列表
     */
    public List<OrgUnit> getListByType(Integer unitType) {
        return baseMapper.selectByType(unitType);
    }

    /**
     * 获取所有公司列表（兼容旧接口）
     */
    public List<OrgUnit> getCompanyList() {
        return getListByType(OrgUnit.TYPE_COMPANY);
    }

    /**
     * 获取指定父级下的部门树（兼容旧接口）
     */
    public List<OrgUnit> getDeptTree(Long parentId) {
        List<OrgUnit> allUnits = baseMapper.selectAllWithLeader();
        // 找到指定父级下的所有部门
        return buildTree(allUnits, parentId);
    }

    /**
     * 构建树形结构
     */
    private List<OrgUnit> buildTree(List<OrgUnit> allUnits, Long parentId) {
        Map<Long, List<OrgUnit>> groupByParent = allUnits.stream()
                .collect(Collectors.groupingBy(u -> u.getParentId() == null ? 0L : u.getParentId()));

        return buildTreeRecursive(groupByParent, parentId);
    }

    private List<OrgUnit> buildTreeRecursive(Map<Long, List<OrgUnit>> groupByParent, Long parentId) {
        List<OrgUnit> children = groupByParent.get(parentId);
        if (children == null) {
            return new ArrayList<>();
        }

        for (OrgUnit unit : children) {
            unit.setUnitTypeName(getTypeName(unit.getUnitType()));
            unit.setChildren(buildTreeRecursive(groupByParent, unit.getId()));
        }

        return children;
    }

    private String getTypeName(Integer type) {
        if (type == null)
            return "";
        return switch (type) {
            case 1 -> "集团";
            case 2 -> "公司";
            case 3 -> "部门";
            default -> "";
        };
    }

    /**
     * 创建组织节点
     */
    public boolean createUnit(OrgUnit unit) {
        if (unit.getSortOrder() == null) {
            unit.setSortOrder(0);
        }
        return save(unit);
    }

    /**
     * 更新组织节点
     */
    public boolean updateUnit(OrgUnit unit) {
        return updateById(unit);
    }

    /**
     * 删除组织节点（检查是否有子节点）
     */
    public boolean deleteUnit(Long id) {
        // 检查是否有子节点
        long childCount = count(new LambdaQueryWrapper<OrgUnit>()
                .eq(OrgUnit::getParentId, id));
        if (childCount > 0) {
            throw new RuntimeException("该节点下存在子节点，无法删除");
        }
        return removeById(id);
    }

    /**
     * 检查编码是否存在
     */
    public boolean checkCodeExists(String unitCode, Long excludeId) {
        LambdaQueryWrapper<OrgUnit> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OrgUnit::getUnitCode, unitCode);
        if (excludeId != null) {
            wrapper.ne(OrgUnit::getId, excludeId);
        }
        return count(wrapper) > 0;
    }

    /**
     * 获取指定节点的所有上级ID（用于获取完整路径）
     */
    public List<Long> getParentIds(Long id) {
        List<Long> parentIds = new ArrayList<>();
        OrgUnit current = getById(id);
        while (current != null && current.getParentId() != null && current.getParentId() > 0) {
            parentIds.add(0, current.getParentId());
            current = getById(current.getParentId());
        }
        return parentIds;
    }

    /**
     * 获取指定节点所属的公司ID
     */
    public Long getCompanyId(Long unitId) {
        OrgUnit current = getById(unitId);
        while (current != null) {
            if (current.getUnitType() == OrgUnit.TYPE_COMPANY) {
                return current.getId();
            }
            if (current.getParentId() == null || current.getParentId() == 0) {
                break;
            }
            current = getById(current.getParentId());
        }
        return null;
    }
}
