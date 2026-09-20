package com.kadmin.web.controller.system;

import com.kadmin.common.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 报表统计（服务端聚合，替代前端全量拉取计算）
 */
@RestController
@RequestMapping("/report")
@RequiredArgsConstructor
public class ReportController {

    private final JdbcTemplate jdbcTemplate;

    /**
     * 人事统计汇总
     *
     * @param companyId 公司ID（可选，按 dept 向上归属过滤）
     * @param deptId    部门ID（可选）
     */
    @GetMapping("/employee/summary")
    public Result<Map<String, Object>> employeeSummary(
            @RequestParam(required = false) Long companyId,
            @RequestParam(required = false) Long deptId) {
        StringBuilder filter = new StringBuilder(" WHERE 1=1");
        if (deptId != null) {
            filter.append(" AND (dept_id = ").append(safe(deptId))
                    .append(" OR dept_id IN (SELECT id FROM org_unit WHERE parent_id = ").append(safe(deptId)).append("))");
        } else if (companyId != null) {
            filter.append(" AND (company_id = ").append(safe(companyId))
                    .append(" OR dept_id IN (SELECT id FROM org_unit WHERE company_id = ").append(safe(companyId)).append("))");
        }

        Map<String, Object> result = new LinkedHashMap<>();

        // 总览
        Map<String, Object> totals = new LinkedHashMap<>();
        totals.put("total", count("SELECT COUNT(*) FROM hr_employee" + filter));
        totals.put("active", count("SELECT COUNT(*) FROM hr_employee" + filter + " AND status = 1"));
        totals.put("probation", count(
                "SELECT COUNT(*) FROM hr_employee" + filter + " AND status = 1 AND employee_type = 'probation'"));
        totals.put("resigned", count("SELECT COUNT(*) FROM hr_employee" + filter + " AND status = 2"));
        result.put("totals", totals);

        // 性别分布
        result.put("gender", distribution(
                "SELECT IFNULL(NULLIF(gender, ''), '未知') AS k, COUNT(*) AS c FROM hr_employee" + filter
                        + " GROUP BY k ORDER BY c DESC"));

        // 学历分布
        result.put("education", distribution(
                "SELECT IFNULL(NULLIF(highest_education, ''), '未知') AS k, COUNT(*) AS c FROM hr_employee" + filter
                        + " GROUP BY k ORDER BY c DESC"));

        // 年龄分布
        result.put("ageBuckets", ageBuckets(filter.toString()));

        // 部门分布（Top 15）：外层 JOIN 取部门名，过滤条件在内层原样套用
        result.put("deptDistribution", distribution(
                "SELECT IFNULL(u.unit_name, '未分配') AS k, COUNT(*) AS c FROM hr_employee e"
                        + " LEFT JOIN org_unit u ON e.dept_id = u.id"
                        + " WHERE e.id IN (SELECT id FROM hr_employee" + filter + ")"
                        + " GROUP BY k ORDER BY c DESC LIMIT 15"));

        // 近12个月入离职趋势
        result.put("monthlyTrend", monthlyTrend(filter.toString()));

        return Result.success(result);
    }

    /**
     * 考勤统计汇总
     *
     * @param month 月份 YYYY-MM
     */
    @GetMapping("/attendance/summary")
    public Result<Map<String, Object>> attendanceSummary(
            @RequestParam String month,
            @RequestParam(required = false) Long companyId,
            @RequestParam(required = false) Long deptId) {
        YearMonth ym = YearMonth.parse(month);
        String startDate = month + "-01";
        String endDate = month + "-" + String.format("%02d", ym.lengthOfMonth());

        String filter = " WHERE r.att_date BETWEEN '" + startDate + "' AND '" + endDate + "'";
        if (deptId != null) {
            filter += " AND e.dept_id IN (SELECT id FROM org_unit WHERE id = " + safe(deptId)
                    + " OR parent_id = " + safe(deptId) + ")";
        } else if (companyId != null) {
            filter += " AND e.dept_id IN (SELECT id FROM org_unit WHERE company_id = " + safe(companyId) + ")";
        }

        Map<String, Object> result = new LinkedHashMap<>();

        // 总览
        Map<String, Object> overview = new LinkedHashMap<>();
        overview.put("workDays", count(
                "SELECT COUNT(DISTINCT CONCAT(r.employee_id, '_', r.att_date)) FROM att_daily_record r"
                        + " JOIN hr_employee e ON r.employee_id = e.id" + filter));
        overview.put("actualDays", count(
                "SELECT COUNT(DISTINCT CONCAT(r.employee_id, '_', r.att_date)) FROM att_daily_record r"
                        + " JOIN hr_employee e ON r.employee_id = e.id" + filter
                        + " AND r.status IN (1, 2, 3, 7)"));
        overview.put("lateTimes", count(
                "SELECT COUNT(*) FROM att_daily_record r JOIN hr_employee e ON r.employee_id = e.id" + filter
                        + " AND r.status IN (2, 7)"));
        overview.put("earlyTimes", count(
                "SELECT COUNT(*) FROM att_daily_record r JOIN hr_employee e ON r.employee_id = e.id" + filter
                        + " AND r.status IN (3, 7)"));
        overview.put("absentDays", count(
                "SELECT COUNT(*) FROM att_daily_record r JOIN hr_employee e ON r.employee_id = e.id" + filter
                        + " AND r.status = 4"));
        overview.put("leaveDays", count(
                "SELECT COUNT(*) FROM att_daily_record r JOIN hr_employee e ON r.employee_id = e.id" + filter
                        + " AND r.status = 5"));
        long actual = toLong(overview.get("actualDays"));
        long work = toLong(overview.get("workDays"));
        overview.put("attendanceRate", work > 0
                ? java.math.BigDecimal.valueOf(actual * 100).divide(java.math.BigDecimal.valueOf(work), 1,
                        java.math.RoundingMode.HALF_UP)
                : java.math.BigDecimal.ZERO);
        result.put("overview", overview);

        // 部门排名（Top 10，按出勤率倒序）
        String deptRankingSql = """
                SELECT IFNULL(u.unit_name, '未分配') AS dept_name,
                       COUNT(*) AS total_cnt,
                       SUM(CASE WHEN r.status IN (1, 2, 3, 7) THEN 1 ELSE 0 END) AS actual_cnt,
                       SUM(CASE WHEN r.status IN (2, 7) THEN 1 ELSE 0 END) AS late_cnt,
                       SUM(CASE WHEN r.status = 4 THEN 1 ELSE 0 END) AS absent_cnt
                FROM att_daily_record r
                JOIN hr_employee e ON r.employee_id = e.id
                LEFT JOIN org_unit u ON e.dept_id = u.id
                """ + filter + """
                 GROUP BY u.unit_name
                HAVING total_cnt > 0
                ORDER BY actual_cnt * 1.0 / total_cnt DESC
                LIMIT 10
                """;
        List<Map<String, Object>> deptRanking = new ArrayList<>();
        jdbcTemplate.query(deptRankingSql, rs -> {
            long totalCnt = rs.getLong("total_cnt");
            long actualCnt = rs.getLong("actual_cnt");
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("deptName", rs.getString("dept_name"));
            item.put("attendanceRate", totalCnt > 0
                    ? java.math.BigDecimal.valueOf(actualCnt * 100).divide(java.math.BigDecimal.valueOf(totalCnt), 1,
                            java.math.RoundingMode.HALF_UP)
                    : java.math.BigDecimal.ZERO);
            item.put("lateTimes", rs.getLong("late_cnt"));
            item.put("absentDays", rs.getLong("absent_cnt"));
            deptRanking.add(item);
        });
        result.put("deptRanking", deptRanking);

        return Result.success(result);
    }

    // ==================== 内部工具 ====================

    private long count(String sql) {
        Long value = jdbcTemplate.queryForObject(sql, Long.class);
        return value != null ? value : 0;
    }

    private long toLong(Object value) {
        if (value instanceof Number number) {
            return number.longValue();
        }
        return 0;
    }

    /**
     * ID 参数只允许数字，防注入
     */
    private String safe(Long id) {
        return id == null ? "0" : String.valueOf(id);
    }

    private List<Map<String, Object>> distribution(String sql) {
        List<Map<String, Object>> list = new ArrayList<>();
        jdbcTemplate.query(sql, rs -> {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("name", rs.getString("k"));
            item.put("value", rs.getLong("c"));
            list.add(item);
        });
        return list;
    }

    private List<Map<String, Object>> ageBuckets(String filter) {
        List<Map<String, Object>> list = new ArrayList<>();
        int nowYear = LocalDate.now().getYear();
        String sql = "SELECT COUNT(*) FROM hr_employee" + filter
                + " AND birth_date IS NOT NULL AND YEAR(birth_date) ";
        // 18-25 / 26-35 / 36-45 / 46-55 / 55以上
        list.add(bucketItem("18-25", count(sql + "BETWEEN " + (nowYear - 25) + " AND " + (nowYear - 18))));
        list.add(bucketItem("26-35", count(sql + "BETWEEN " + (nowYear - 35) + " AND " + (nowYear - 26))));
        list.add(bucketItem("36-45", count(sql + "BETWEEN " + (nowYear - 45) + " AND " + (nowYear - 36))));
        list.add(bucketItem("46-55", count(sql + "BETWEEN " + (nowYear - 55) + " AND " + (nowYear - 46))));
        list.add(bucketItem("55以上", count(sql + "<= " + (nowYear - 56))));
        return list;
    }

    private Map<String, Object> bucketItem(String name, long value) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("name", name);
        item.put("value", value);
        return item;
    }

    private List<Map<String, Object>> monthlyTrend(String filter) {
        List<Map<String, Object>> list = new ArrayList<>();
        LocalDate today = LocalDate.now();
        for (int i = 11; i >= 0; i--) {
            YearMonth ym = YearMonth.from(today.minusMonths(i));
            String monthStr = ym.format(DateTimeFormatter.ofPattern("yyyy-MM"));
            String monthStart = monthStr + "-01";
            String monthEnd = monthStr + "-" + String.format("%02d", ym.lengthOfMonth());
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("month", monthStr);
            item.put("entry", count("SELECT COUNT(*) FROM hr_employee" + filter
                    + " AND entry_date BETWEEN '" + monthStart + "' AND '" + monthEnd + "'"));
            item.put("exit", count("SELECT COUNT(*) FROM hr_employee" + filter
                    + " AND leave_date BETWEEN '" + monthStart + "' AND '" + monthEnd + "'"));
            list.add(item);
        }
        return list;
    }
}
