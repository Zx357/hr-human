package com.kadmin.web.controller.system;

import com.kadmin.common.Result;
import com.kadmin.common.utils.SqlFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.sql.Date;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 报表统计（服务端聚合，替代前端全量拉取计算；全部使用参数化查询）
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
        SqlFilter filter = SqlFilter.create();
        if (deptId != null) {
            filter.append(" AND (dept_id = ? OR dept_id IN (SELECT id FROM org_unit WHERE parent_id = ?))",
                    deptId, deptId);
        } else if (companyId != null) {
            filter.append(" AND (company_id = ? OR dept_id IN (SELECT id FROM org_unit WHERE company_id = ?))",
                    companyId, companyId);
        }

        Map<String, Object> result = new LinkedHashMap<>();

        // 总览
        Map<String, Object> totals = new LinkedHashMap<>();
        totals.put("total", count("SELECT COUNT(*) FROM hr_employee" + filter.getSql(), filter.getParams()));
        totals.put("active", count("SELECT COUNT(*) FROM hr_employee" + filter.getSql() + " AND status = 1",
                filter.getParams()));
        totals.put("probation", count(
                "SELECT COUNT(*) FROM hr_employee" + filter.getSql()
                        + " AND status = 1 AND employee_type = 'probation'",
                filter.getParams()));
        totals.put("resigned", count("SELECT COUNT(*) FROM hr_employee" + filter.getSql() + " AND status = 2",
                filter.getParams()));
        result.put("totals", totals);

        // 性别分布
        result.put("gender", distribution(
                "SELECT IFNULL(NULLIF(gender, ''), '未知') AS k, COUNT(*) AS c FROM hr_employee" + filter.getSql()
                        + " GROUP BY k ORDER BY c DESC",
                filter.getParams()));

        // 学历分布
        result.put("education", distribution(
                "SELECT IFNULL(NULLIF(highest_education, ''), '未知') AS k, COUNT(*) AS c FROM hr_employee"
                        + filter.getSql() + " GROUP BY k ORDER BY c DESC",
                filter.getParams()));

        // 年龄分布
        result.put("ageBuckets", ageBuckets(filter));

        // 部门分布（Top 15）：外层 JOIN 取部门名，过滤条件在内层原样套用
        result.put("deptDistribution", distribution(
                "SELECT IFNULL(u.unit_name, '未分配') AS k, COUNT(*) AS c FROM hr_employee e"
                        + " LEFT JOIN org_unit u ON e.dept_id = u.id"
                        + " WHERE e.id IN (SELECT id FROM hr_employee" + filter.getSql() + ")"
                        + " GROUP BY k ORDER BY c DESC LIMIT 15",
                filter.getParams()));

        // 近12个月入离职趋势
        result.put("monthlyTrend", monthlyTrend(filter));

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
        LocalDate startDate = ym.atDay(1);
        LocalDate endDate = ym.atEndOfMonth();

        SqlFilter filter = SqlFilter.create()
                .append(" AND r.att_date BETWEEN ? AND ?", Date.valueOf(startDate), Date.valueOf(endDate));
        if (deptId != null) {
            filter.append(" AND e.dept_id IN (SELECT id FROM org_unit WHERE id = ? OR parent_id = ?)",
                    deptId, deptId);
        } else if (companyId != null) {
            filter.append(" AND e.dept_id IN (SELECT id FROM org_unit WHERE company_id = ?)", companyId);
        }

        Map<String, Object> result = new LinkedHashMap<>();

        // 总览
        Map<String, Object> overview = new LinkedHashMap<>();
        overview.put("workDays", count(
                "SELECT COUNT(DISTINCT CONCAT(r.employee_id, '_', r.att_date)) FROM att_daily_record r"
                        + " JOIN hr_employee e ON r.employee_id = e.id" + filter.getSql(),
                filter.getParams()));
        overview.put("actualDays", count(
                "SELECT COUNT(DISTINCT CONCAT(r.employee_id, '_', r.att_date)) FROM att_daily_record r"
                        + " JOIN hr_employee e ON r.employee_id = e.id" + filter.getSql()
                        + " AND r.status IN (1, 2, 3, 7)",
                filter.getParams()));
        overview.put("lateTimes", count(
                "SELECT COUNT(*) FROM att_daily_record r JOIN hr_employee e ON r.employee_id = e.id"
                        + filter.getSql() + " AND r.status IN (2, 7)",
                filter.getParams()));
        overview.put("earlyTimes", count(
                "SELECT COUNT(*) FROM att_daily_record r JOIN hr_employee e ON r.employee_id = e.id"
                        + filter.getSql() + " AND r.status IN (3, 7)",
                filter.getParams()));
        overview.put("absentDays", count(
                "SELECT COUNT(*) FROM att_daily_record r JOIN hr_employee e ON r.employee_id = e.id"
                        + filter.getSql() + " AND r.status = 4",
                filter.getParams()));
        overview.put("leaveDays", count(
                "SELECT COUNT(*) FROM att_daily_record r JOIN hr_employee e ON r.employee_id = e.id"
                        + filter.getSql() + " AND r.status = 5",
                filter.getParams()));
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
                """ + filter.getSql() + """
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
        }, filter.getParams().toArray());
        result.put("deptRanking", deptRanking);

        return Result.success(result);
    }

    /**
     * 考勤按日趋势（服务端聚合，供仪表盘折线图；最多31天）
     *
     * @param startDate 起始日期 yyyy-MM-dd
     * @param endDate   结束日期 yyyy-MM-dd
     */
    @GetMapping("/attendance/daily-trend")
    public Result<List<Map<String, Object>>> attendanceDailyTrend(
            @RequestParam String startDate,
            @RequestParam String endDate) {
        LocalDate start = LocalDate.parse(startDate);
        LocalDate end = LocalDate.parse(endDate);
        if (end.isBefore(start)) {
            return Result.error("结束日期不能早于开始日期");
        }
        if (start.plusDays(31).isBefore(end)) {
            return Result.error("时间跨度不能超过31天");
        }
        List<Map<String, Object>> list = new ArrayList<>();
        jdbcTemplate.query("""
                SELECT r.att_date AS att_date,
                       SUM(CASE WHEN r.status = 1 THEN 1 ELSE 0 END) AS normal_cnt,
                       SUM(CASE WHEN r.status IS NOT NULL AND r.status <> 1 THEN 1 ELSE 0 END) AS abnormal_cnt
                FROM att_daily_record r
                WHERE r.att_date BETWEEN ? AND ?
                GROUP BY r.att_date
                ORDER BY r.att_date
                """, rs -> {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("date", rs.getDate("att_date").toLocalDate().toString());
            item.put("normal", rs.getLong("normal_cnt"));
            item.put("abnormal", rs.getLong("abnormal_cnt"));
            list.add(item);
        }, Date.valueOf(start), Date.valueOf(end));
        return Result.success(list);
    }

    // ==================== 内部工具 ====================

    private long count(String sql, List<Object> params) {
        Long value = jdbcTemplate.queryForObject(sql, Long.class, params.toArray());
        return value != null ? value : 0;
    }

    private long toLong(Object value) {
        if (value instanceof Number number) {
            return number.longValue();
        }
        return 0;
    }

    private List<Map<String, Object>> distribution(String sql, List<Object> params) {
        List<Map<String, Object>> list = new ArrayList<>();
        jdbcTemplate.query(sql, rs -> {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("name", rs.getString("k"));
            item.put("value", rs.getLong("c"));
            list.add(item);
        }, params.toArray());
        return list;
    }

    private List<Map<String, Object>> ageBuckets(SqlFilter filter) {
        List<Map<String, Object>> list = new ArrayList<>();
        int nowYear = LocalDate.now().getYear();
        String sql = "SELECT COUNT(*) FROM hr_employee" + filter.getSql()
                + " AND birth_date IS NOT NULL AND YEAR(birth_date) ";
        // 18-25 / 26-35 / 36-45 / 46-55 / 55以上
        list.add(bucketItem("18-25", count(sql + "BETWEEN ? AND ?",
                withParams(filter, nowYear - 25, nowYear - 18))));
        list.add(bucketItem("26-35", count(sql + "BETWEEN ? AND ?",
                withParams(filter, nowYear - 35, nowYear - 26))));
        list.add(bucketItem("36-45", count(sql + "BETWEEN ? AND ?",
                withParams(filter, nowYear - 45, nowYear - 36))));
        list.add(bucketItem("46-55", count(sql + "BETWEEN ? AND ?",
                withParams(filter, nowYear - 55, nowYear - 46))));
        list.add(bucketItem("55以上", count(sql + "<= ?", withParams(filter, nowYear - 56))));
        return list;
    }

    private List<Object> withParams(SqlFilter filter, Object... extra) {
        List<Object> all = new ArrayList<>(filter.getParams());
        for (Object param : extra) {
            all.add(param);
        }
        return all;
    }

    private Map<String, Object> bucketItem(String name, long value) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("name", name);
        item.put("value", value);
        return item;
    }

    private List<Map<String, Object>> monthlyTrend(SqlFilter filter) {
        List<Map<String, Object>> list = new ArrayList<>();
        LocalDate today = LocalDate.now();
        for (int i = 11; i >= 0; i--) {
            YearMonth ym = YearMonth.from(today.minusMonths(i));
            String monthStr = ym.format(DateTimeFormatter.ofPattern("yyyy-MM"));
            Date monthStart = Date.valueOf(ym.atDay(1));
            Date monthEnd = Date.valueOf(ym.atEndOfMonth());
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("month", monthStr);
            item.put("entry", count("SELECT COUNT(*) FROM hr_employee" + filter.getSql()
                    + " AND entry_date BETWEEN ? AND ?", withParams(filter, monthStart, monthEnd)));
            item.put("exit", count("SELECT COUNT(*) FROM hr_employee" + filter.getSql()
                    + " AND leave_date BETWEEN ? AND ?", withParams(filter, monthStart, monthEnd)));
            list.add(item);
        }
        return list;
    }
}
