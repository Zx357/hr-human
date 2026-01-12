package com.kadmin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kadmin.entity.OrgUnit;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface OrgUnitMapper extends BaseMapper<OrgUnit> {

    @Select("SELECT u.*, e.name as leader_name FROM org_unit u " +
            "LEFT JOIN hr_employee e ON u.leader_id = e.id " +
            "WHERE u.deleted = 0 ORDER BY u.sort_order, u.id")
    List<OrgUnit> selectAllWithLeader();

    @Select("SELECT u.*, e.name as leader_name FROM org_unit u " +
            "LEFT JOIN hr_employee e ON u.leader_id = e.id " +
            "WHERE u.deleted = 0 AND u.unit_type = #{unitType} ORDER BY u.sort_order, u.id")
    List<OrgUnit> selectByType(@Param("unitType") Integer unitType);

    /**
     * 统计指定部门的员工数量
     */
    @Select("SELECT COUNT(*) FROM hr_employee WHERE dept_id = #{deptId} AND status = 1")
    Integer countEmployeesByDeptId(@Param("deptId") Long deptId);

    /**
     * 统计指定部门列表的员工数量
     */
    @Select("<script>" +
            "SELECT COUNT(*) FROM hr_employee WHERE status = 1 " +
            "<if test='deptIds != null and deptIds.size() > 0'>" +
            "AND dept_id IN <foreach collection='deptIds' item='id' open='(' separator=',' close=')'>#{id}</foreach>" +
            "</if>" +
            "<if test='deptIds == null or deptIds.size() == 0'>" +
            "AND 1=0" +
            "</if>" +
            "</script>")
    Integer countEmployeesByDeptIds(@Param("deptIds") List<Long> deptIds);

    /**
     * 统计所有在职员工数量
     */
    @Select("SELECT COUNT(*) FROM hr_employee WHERE status = 1")
    Integer countAllEmployees();

    /**
     * 按学历统计员工分布
     */
    @Select("<script>" +
            "SELECT COALESCE(d.dict_label, '未知') as name, COUNT(*) as value " +
            "FROM hr_employee e " +
            "LEFT JOIN sys_dict_data d ON e.highest_education = d.dict_value " +
            "LEFT JOIN sys_dict_type t ON d.dict_type_id = t.id AND t.dict_code = 'education' " +
            "WHERE e.status = 1 " +
            "<if test='deptIds != null and deptIds.size() > 0'>" +
            "AND e.dept_id IN <foreach collection='deptIds' item='id' open='(' separator=',' close=')'>#{id}</foreach>" +
            "</if>" +
            "GROUP BY e.highest_education, d.dict_label" +
            "</script>")
    List<Map<String, Object>> countByEducation(@Param("deptIds") List<Long> deptIds);

    /**
     * 按性别统计员工分布
     */
    @Select("<script>" +
            "SELECT CASE e.gender WHEN '1' THEN '男' WHEN '2' THEN '女' ELSE '未知' END as name, COUNT(*) as value " +
            "FROM hr_employee e " +
            "WHERE e.status = 1 " +
            "<if test='deptIds != null and deptIds.size() > 0'>" +
            "AND e.dept_id IN <foreach collection='deptIds' item='id' open='(' separator=',' close=')'>#{id}</foreach>" +
            "</if>" +
            "GROUP BY e.gender" +
            "</script>")
    List<Map<String, Object>> countByGender(@Param("deptIds") List<Long> deptIds);

    /**
     * 按年龄段统计员工分布
     */
    @Select("<script>" +
            "SELECT " +
            "CASE " +
            "  WHEN TIMESTAMPDIFF(YEAR, e.birth_date, CURDATE()) &lt; 25 THEN '25岁以下' " +
            "  WHEN TIMESTAMPDIFF(YEAR, e.birth_date, CURDATE()) BETWEEN 25 AND 30 THEN '25-30岁' " +
            "  WHEN TIMESTAMPDIFF(YEAR, e.birth_date, CURDATE()) BETWEEN 31 AND 35 THEN '31-35岁' " +
            "  WHEN TIMESTAMPDIFF(YEAR, e.birth_date, CURDATE()) BETWEEN 36 AND 40 THEN '36-40岁' " +
            "  WHEN TIMESTAMPDIFF(YEAR, e.birth_date, CURDATE()) BETWEEN 41 AND 45 THEN '41-45岁' " +
            "  WHEN TIMESTAMPDIFF(YEAR, e.birth_date, CURDATE()) BETWEEN 46 AND 50 THEN '46-50岁' " +
            "  WHEN TIMESTAMPDIFF(YEAR, e.birth_date, CURDATE()) > 50 THEN '50岁以上' " +
            "  ELSE '未知' " +
            "END as age_range, COUNT(*) as count " +
            "FROM hr_employee e " +
            "WHERE e.status = 1 " +
            "<if test='deptIds != null and deptIds.size() > 0'>" +
            "AND e.dept_id IN <foreach collection='deptIds' item='id' open='(' separator=',' close=')'>#{id}</foreach>" +
            "</if>" +
            "GROUP BY age_range ORDER BY age_range" +
            "</script>")
    List<Map<String, Object>> countByAgeRange(@Param("deptIds") List<Long> deptIds);

    /**
     * 按在职状态统计员工分布
     */
    @Select("<script>" +
            "SELECT CASE e.status WHEN 1 THEN '在职' WHEN 2 THEN '离职' ELSE '未知' END as name, COUNT(*) as value " +
            "FROM hr_employee e " +
            "<if test='deptIds != null and deptIds.size() > 0'>" +
            "WHERE e.dept_id IN <foreach collection='deptIds' item='id' open='(' separator=',' close=')'>#{id}</foreach>" +
            "</if>" +
            "GROUP BY e.status" +
            "</script>")
    List<Map<String, Object>> countByStatus(@Param("deptIds") List<Long> deptIds);

    /**
     * 按员工类型统计分布
     */
    @Select("<script>" +
            "SELECT COALESCE(d.dict_label, '未知') as name, COUNT(*) as value " +
            "FROM hr_employee e " +
            "LEFT JOIN sys_dict_data d ON e.employee_type = d.dict_value " +
            "LEFT JOIN sys_dict_type t ON d.dict_type_id = t.id AND t.dict_code = 'employee_type' " +
            "WHERE e.status = 1 " +
            "<if test='deptIds != null and deptIds.size() > 0'>" +
            "AND e.dept_id IN <foreach collection='deptIds' item='id' open='(' separator=',' close=')'>#{id}</foreach>" +
            "</if>" +
            "GROUP BY e.employee_type, d.dict_label" +
            "</script>")
    List<Map<String, Object>> countByEmployeeType(@Param("deptIds") List<Long> deptIds);

    /**
     * 获取所有组织ID
     */
    @Select("SELECT id FROM org_unit WHERE deleted = 0")
    List<Long> selectAllOrgIds();

    /**
     * 获取指定组织及其所有子组织ID
     */
    @Select("WITH RECURSIVE org_tree AS (" +
            "  SELECT id FROM org_unit WHERE id = #{orgId} AND deleted = 0 " +
            "  UNION ALL " +
            "  SELECT o.id FROM org_unit o INNER JOIN org_tree t ON o.parent_id = t.id WHERE o.deleted = 0" +
            ") SELECT id FROM org_tree")
    List<Long> selectOrgAndChildIds(@Param("orgId") Long orgId);
}
