package com.kadmin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kadmin.entity.OrgUnit;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Mapper
public interface OrgUnitMapper extends BaseMapper<OrgUnit> {

    @Select("""
            SELECT u.*, e.name AS leader_name
            FROM org_unit u
            LEFT JOIN hr_employee e ON u.leader_id = e.id
            ORDER BY u.sort_order, u.id
            """)
    List<OrgUnit> selectAllWithLeader();

    @Select("""
            SELECT u.*, e.name AS leader_name
            FROM org_unit u
            LEFT JOIN hr_employee e ON u.leader_id = e.id
            WHERE u.unit_type = #{unitType}
            ORDER BY u.sort_order, u.id
            """)
    List<OrgUnit> selectByType(@Param("unitType") Integer unitType);

    @Select("SELECT COUNT(*) FROM hr_employee WHERE dept_id = #{deptId} AND status = 1")
    Integer countEmployeesByDeptId(@Param("deptId") Long deptId);

    @Select({
            "<script>",
            "SELECT COUNT(*)",
            "FROM hr_employee",
            "WHERE status = 1",
            "<if test='deptIds != null and deptIds.size() > 0'>",
            "AND dept_id IN",
            "<foreach collection='deptIds' item='id' open='(' separator=',' close=')'>#{id}</foreach>",
            "</if>",
            "<if test='deptIds == null or deptIds.size() == 0'>",
            "AND 1 = 0",
            "</if>",
            "</script>"
    })
    Integer countEmployeesByDeptIds(@Param("deptIds") List<Long> deptIds);

    @Select("SELECT COUNT(*) FROM hr_employee WHERE status = 1")
    Integer countAllEmployees();

    @Select({
            "<script>",
            "SELECT COALESCE(d.dict_label, '未知') AS name, COUNT(*) AS value",
            "FROM hr_employee e",
            "LEFT JOIN sys_dict_data d ON e.highest_education = d.dict_value",
            "LEFT JOIN sys_dict_type t ON d.dict_type_id = t.id AND t.dict_code = 'education'",
            "WHERE e.status = 1",
            "<if test='deptIds != null and deptIds.size() > 0'>",
            "AND e.dept_id IN",
            "<foreach collection='deptIds' item='id' open='(' separator=',' close=')'>#{id}</foreach>",
            "</if>",
            "GROUP BY e.highest_education, d.dict_label",
            "</script>"
    })
    List<Map<String, Object>> countByEducation(@Param("deptIds") List<Long> deptIds);

    @Select({
            "<script>",
            "SELECT CASE e.gender",
            "WHEN '1' THEN '男'",
            "WHEN '2' THEN '女'",
            "ELSE '未知' END AS name, COUNT(*) AS value",
            "FROM hr_employee e",
            "WHERE e.status = 1",
            "<if test='deptIds != null and deptIds.size() > 0'>",
            "AND e.dept_id IN",
            "<foreach collection='deptIds' item='id' open='(' separator=',' close=')'>#{id}</foreach>",
            "</if>",
            "GROUP BY e.gender",
            "</script>"
    })
    List<Map<String, Object>> countByGender(@Param("deptIds") List<Long> deptIds);

    @Select({
            "<script>",
            "SELECT",
            "CASE",
            "WHEN TIMESTAMPDIFF(YEAR, e.birth_date, CURDATE()) &lt; 25 THEN '25岁以下'",
            "WHEN TIMESTAMPDIFF(YEAR, e.birth_date, CURDATE()) BETWEEN 25 AND 30 THEN '25-30岁'",
            "WHEN TIMESTAMPDIFF(YEAR, e.birth_date, CURDATE()) BETWEEN 31 AND 35 THEN '31-35岁'",
            "WHEN TIMESTAMPDIFF(YEAR, e.birth_date, CURDATE()) BETWEEN 36 AND 40 THEN '36-40岁'",
            "WHEN TIMESTAMPDIFF(YEAR, e.birth_date, CURDATE()) BETWEEN 41 AND 45 THEN '41-45岁'",
            "WHEN TIMESTAMPDIFF(YEAR, e.birth_date, CURDATE()) BETWEEN 46 AND 50 THEN '46-50岁'",
            "WHEN TIMESTAMPDIFF(YEAR, e.birth_date, CURDATE()) &gt; 50 THEN '50岁以上'",
            "ELSE '未知'",
            "END AS age_range, COUNT(*) AS count",
            "FROM hr_employee e",
            "WHERE e.status = 1",
            "<if test='deptIds != null and deptIds.size() > 0'>",
            "AND e.dept_id IN",
            "<foreach collection='deptIds' item='id' open='(' separator=',' close=')'>#{id}</foreach>",
            "</if>",
            "GROUP BY age_range",
            "ORDER BY age_range",
            "</script>"
    })
    List<Map<String, Object>> countByAgeRange(@Param("deptIds") List<Long> deptIds);

    @Select({
            "<script>",
            "SELECT CASE e.status WHEN 1 THEN '在职' WHEN 2 THEN '离职' ELSE '未知' END AS name, COUNT(*) AS value",
            "FROM hr_employee e",
            "WHERE 1 = 1",
            "<if test='deptIds != null and deptIds.size() > 0'>",
            "AND e.dept_id IN",
            "<foreach collection='deptIds' item='id' open='(' separator=',' close=')'>#{id}</foreach>",
            "</if>",
            "GROUP BY e.status",
            "</script>"
    })
    List<Map<String, Object>> countByStatus(@Param("deptIds") List<Long> deptIds);

    @Select({
            "<script>",
            "SELECT COALESCE(d.dict_label, '未知') AS name, COUNT(*) AS value",
            "FROM hr_employee e",
            "LEFT JOIN sys_dict_data d ON e.employee_type = d.dict_value",
            "LEFT JOIN sys_dict_type t ON d.dict_type_id = t.id AND t.dict_code = 'employee_type'",
            "WHERE e.status = 1",
            "<if test='deptIds != null and deptIds.size() > 0'>",
            "AND e.dept_id IN",
            "<foreach collection='deptIds' item='id' open='(' separator=',' close=')'>#{id}</foreach>",
            "</if>",
            "GROUP BY e.employee_type, d.dict_label",
            "</script>"
    })
    List<Map<String, Object>> countByEmployeeType(@Param("deptIds") List<Long> deptIds);

    @Select("SELECT id FROM org_unit")
    List<Long> selectAllOrgIds();

    @Select("SELECT id FROM org_unit WHERE parent_id = #{parentId}")
    List<Long> selectChildIds(@Param("parentId") Long parentId);

    default List<Long> selectOrgAndChildIds(Long orgId) {
        Set<Long> visited = new LinkedHashSet<>();
        if (orgId == null) {
            return new ArrayList<>(visited);
        }

        Deque<Long> pending = new ArrayDeque<>();
        visited.add(orgId);
        pending.add(orgId);

        while (!pending.isEmpty()) {
            Long parentId = pending.removeFirst();
            List<Long> childIds = selectChildIds(parentId);
            for (Long childId : childIds) {
                if (childId != null && visited.add(childId)) {
                    pending.add(childId);
                }
            }
        }

        return new ArrayList<>(visited);
    }
}
