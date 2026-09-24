package com.kadmin.salary.service;

import com.kadmin.salary.service.SalaryCalcEngine.CalcResult;
import com.kadmin.salary.service.SalaryCalcEngine.EmployeeCalcInput;
import com.kadmin.salary.service.SalaryCalcEngine.SchemeItemLine;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 薪资核算引擎单元测试：四种取值类型 + 比例项顺序引用 + 汇总计算
 */
class SalaryCalcEngineTest {

    private static final BigDecimal HUNDRED = new BigDecimal("100");

    private SchemeItemLine fixed(long id, String code, String name, BigDecimal unitPrice) {
        return new SchemeItemLine(id, code, name, 1, SalaryCalcEngine.TYPE_FIXED,
                null, null, null, null, null, (int) id);
    }

    private SchemeItemLine line(long id, String code, String name, int direction, int valueType,
            String ratioBase, BigDecimal ratio, String attRule, BigDecimal price, Integer tolerance) {
        return new SchemeItemLine(id, code, name, direction, valueType,
                ratioBase, ratio, attRule, price, tolerance, (int) id);
    }

    private EmployeeCalcInput input() {
        // 基础输入：出勤20天/迟到2次15分钟/旷工0/事假4小时/加班8小时/全勤false
        return new EmployeeCalcInput(1L, BigDecimal.valueOf(20), 2, 15, 0,
                BigDecimal.valueOf(4), BigDecimal.valueOf(8), BigDecimal.valueOf(160), false,
                Map.of(1L, new BigDecimal("8000"), 2L, new BigDecimal("2000")),
                Map.of());
    }

    @Test
    @DisplayName("固定项取档案金额，比例项按前面项求值，汇总正确")
    void calculate_fixedAndRatio() {
        List<SchemeItemLine> scheme = List.of(
                fixed(1L, "basic_salary", "基本工资", null),
                fixed(2L, "post_salary", "岗位工资", null),
                line(3L, "pension", "公积金个人", 2, SalaryCalcEngine.TYPE_RATIO,
                        "basic_salary", new BigDecimal("0.12"), null, null, null),
                line(4L, "subsidy", "交通补贴", 1, SalaryCalcEngine.TYPE_RATIO,
                        "post_salary", new BigDecimal("0.05"), null, null, null));

        CalcResult result = SalaryCalcEngine.calculate(scheme, input());

        assertThat(result.items()).hasSize(4);
        assertThat(result.items().get(0).amount()).isEqualByComparingTo("8000.00");
        assertThat(result.items().get(2).amount()).isEqualByComparingTo("960.00"); // 8000×12%
        assertThat(result.items().get(3).amount()).isEqualByComparingTo("100.00"); // 2000×5%
        // 应发 8000+2000+100 = 10100；扣款 960；实发 9140
        assertThat(result.grossPay()).isEqualByComparingTo("10100.00");
        assertThat(result.totalDeduction()).isEqualByComparingTo("960.00");
        assertThat(result.netPay()).isEqualByComparingTo("9140.00");
    }

    @Test
    @DisplayName("考勤联动：迟到扣款带豁免次数，事假按小时，旷工按天")
    void calculate_attendanceDeductions() {
        List<SchemeItemLine> scheme = List.of(
                line(10L, "late_cut", "迟到扣款", 2, SalaryCalcEngine.TYPE_ATTENDANCE,
                        null, null, "LATE_TIMES", new BigDecimal("50"), 1),
                line(11L, "leave_cut", "事假扣款", 2, SalaryCalcEngine.TYPE_ATTENDANCE,
                        null, null, "PERSONAL_LEAVE_HOURS", new BigDecimal("50"), null),
                line(12L, "absent_cut", "旷工扣款", 2, SalaryCalcEngine.TYPE_ATTENDANCE,
                        null, null, "ABSENT_DAYS", new BigDecimal("500"), null));

        CalcResult result = SalaryCalcEngine.calculate(scheme, input());

        // 迟到2次豁免1次=1×50；事假4h×50=200；旷工0天=0
        assertThat(result.items().get(0).amount()).isEqualByComparingTo("50.00");
        assertThat(result.items().get(0).source()).contains("迟到2次");
        assertThat(result.items().get(1).amount()).isEqualByComparingTo("200.00");
        assertThat(result.items().get(2).amount()).isEqualByComparingTo("0.00");
        assertThat(result.netPay()).isEqualByComparingTo("-250.00"); // 无收入项时为负（仅扣款）
    }

    @Test
    @DisplayName("考勤联动：加班费按倍率、兼职按出勤天数、全勤奖按判定")
    void calculate_attendanceIncome() {
        List<SchemeItemLine> scheme = List.of(
                line(20L, "overtime_pay", "加班费", 1, SalaryCalcEngine.TYPE_ATTENDANCE,
                        null, null, "OVERTIME_HOURS", new BigDecimal("50"), null),
                line(21L, "day_pay", "日结工资", 1, SalaryCalcEngine.TYPE_ATTENDANCE,
                        null, null, "ATTEND_DAYS", new BigDecimal("150"), null),
                line(22L, "full_att", "全勤奖", 1, SalaryCalcEngine.TYPE_ATTENDANCE,
                        null, null, "FULL_ATTENDANCE", new BigDecimal("300"), null));

        CalcResult result = SalaryCalcEngine.calculate(scheme, input());

        assertThat(result.items().get(0).amount()).isEqualByComparingTo("400.00"); // 8h×50
        assertThat(result.items().get(1).amount()).isEqualByComparingTo("3000.00"); // 20天×150
        assertThat(result.items().get(2).amount()).isEqualByComparingTo("0.00"); // 非全勤
        assertThat(result.grossPay()).isEqualByComparingTo("3400.00");
    }

    @Test
    @DisplayName("全勤判定：无迟到无旷工无请假时给全勤奖")
    void calculate_fullAttendanceBonus() {
        List<SchemeItemLine> scheme = List.of(
                line(30L, "full_att", "全勤奖", 1, SalaryCalcEngine.TYPE_ATTENDANCE,
                        null, null, "FULL_ATTENDANCE", new BigDecimal("300"), null));
        EmployeeCalcInput clean = new EmployeeCalcInput(1L, BigDecimal.valueOf(22), 0, 0, 0,
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, true, Map.of(), Map.of());

        CalcResult result = SalaryCalcEngine.calculate(scheme, clean);
        assertThat(result.items().get(0).amount()).isEqualByComparingTo("300.00");
        assertThat(result.items().get(0).source()).isEqualTo("全勤奖励");
    }

    @Test
    @DisplayName("手工项默认0、可录入；比例基项缺失时为0并注明原因")
    void calculate_manualAndMissingRatioBase() {
        List<SchemeItemLine> scheme = List.of(
                line(40L, "commission", "销售提成", 1, SalaryCalcEngine.TYPE_MANUAL,
                        null, null, null, null, null),
                line(41L, "pension", "公积金个人", 2, SalaryCalcEngine.TYPE_RATIO,
                        "basic_salary", new BigDecimal("0.10"), null, null, null));

        // 未录提成、无档案固定项（basic_salary 缺失）
        CalcResult result = SalaryCalcEngine.calculate(scheme,
                new EmployeeCalcInput(1L, BigDecimal.ZERO, 0, 0, 0, BigDecimal.ZERO, BigDecimal.ZERO,
                        BigDecimal.ZERO, false, Map.of(), Map.of(40L, new BigDecimal("5000"))));

        assertThat(result.items().get(0).amount()).isEqualByComparingTo("5000.00");
        assertThat(result.items().get(1).amount()).isEqualByComparingTo("0.00");
        assertThat(result.items().get(1).source()).contains("缺失");
        assertThat(result.netPay()).isEqualByComparingTo("5000.00");
    }

    @Test
    @DisplayName("求值顺序按 sortOrder 而非列表传入顺序，比例项引用看编码")
    void calculate_orderFollowsSortOrder() {
        // 比例项 sortOrder=1 排在前、其基项 sortOrder=2 排在后 → 比例取不到基项为 0
        // （基项 itemId=1L 与 input() 档案金额的键一致）
        List<SchemeItemLine> scheme = List.of(
                new SchemeItemLine(50L, "pension", "公积金个人", 2,
                        SalaryCalcEngine.TYPE_RATIO, "basic_salary", new BigDecimal("0.10"),
                        null, null, null, 1),
                new SchemeItemLine(1L, "basic_salary", "基本工资", 1,
                        SalaryCalcEngine.TYPE_FIXED, null, null, null, null, null, 2));

        CalcResult result = SalaryCalcEngine.calculate(scheme, input());
        assertThat(result.items().get(0).amount()).isEqualByComparingTo("0.00");
        assertThat(result.items().get(1).amount()).isEqualByComparingTo("8000.00");
    }
}
