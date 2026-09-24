package com.kadmin.salary.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 薪资核算引擎（纯函数，不依赖 Spring 与数据库，便于单测）
 *
 * 按方案明细顺序逐项求值：
 * - FIXED 固定：取档案明细金额
 * - RATIO 比例：按指定基项(编码) × 比例值（基项必须排在当前项之前，保存方案时已校验）
 * - ATTENDANCE 考勤联动：按月考勤数据 × 参数（单价/容忍值）
 * - MANUAL 手工：批次内录入（核算时默认 0，随后可编辑）
 * - FORMULA 公式：一期预留，恒为 0
 */
public final class SalaryCalcEngine {

    /** 取值类型：1-固定 2-比例 3-考勤联动 4-手工 5-公式(预留) */
    public static final int TYPE_FIXED = 1;
    public static final int TYPE_RATIO = 2;
    public static final int TYPE_ATTENDANCE = 3;
    public static final int TYPE_MANUAL = 4;
    public static final int TYPE_FORMULA = 5;

    /** 方向：1-收入 2-扣款 */
    public static final int DIRECTION_INCOME = 1;
    public static final int DIRECTION_DEDUCTION = 2;

    /**
     * 方案项求值入参（项定义已联表展开到行上）
     */
    public record SchemeItemLine(Long itemId, String itemCode, String itemName, int direction, int valueType,
            String ratioBaseCode, BigDecimal ratioValue, String attRule, BigDecimal unitPrice, Integer tolerance,
            int sortOrder) {
    }

    /**
     * 员工月度核算输入（考勤数据 + 档案固定项 + 手工项）
     */
    public record EmployeeCalcInput(Long employeeId, BigDecimal attendDays, int lateTimes, int lateMinutes,
            int absentDays, BigDecimal personalLeaveHours, BigDecimal overtimeHours, BigDecimal workHours,
            boolean fullAttendance, Map<Long, BigDecimal> fixedAmounts, Map<Long, BigDecimal> manualAmounts) {

        public static EmployeeCalcInput empty(Long employeeId) {
            return new EmployeeCalcInput(employeeId, BigDecimal.ZERO, 0, 0, 0, BigDecimal.ZERO, BigDecimal.ZERO,
                    BigDecimal.ZERO, false, Map.of(), Map.of());
        }
    }

    /**
     * 求值结果：明细行 + 三个汇总
     */
    public record CalcResult(List<CalculatedItem> items, BigDecimal grossPay, BigDecimal totalDeduction,
            BigDecimal netPay) {
    }

    /**
     * 求值后的明细行
     */
    public record CalculatedItem(Long itemId, String itemCode, String itemName, int direction, int valueType,
            BigDecimal amount, String source, int sortOrder) {
    }

    private SalaryCalcEngine() {
    }

    /**
     * 按方案顺序核算单个员工
     */
    public static CalcResult calculate(List<SchemeItemLine> orderedItems, EmployeeCalcInput input) {
        List<CalculatedItem> items = new ArrayList<>();
        // 已求值项的金额（按编码索引），供比例项引用（只允许引用排在前面的项）
        Map<String, BigDecimal> computedByCode = new HashMap<>();
        BigDecimal gross = BigDecimal.ZERO;
        BigDecimal deduction = BigDecimal.ZERO;

        for (SchemeItemLine line : orderedItems) {
            BigDecimal amount = BigDecimal.ZERO;
            String source;
            switch (line.valueType()) {
                case TYPE_FIXED -> {
                    amount = round2(input.fixedAmounts().getOrDefault(line.itemId(), BigDecimal.ZERO));
                    source = "档案金额";
                }
                case TYPE_RATIO -> {
                    BigDecimal base = line.ratioBaseCode() == null ? null
                            : computedByCode.get(line.ratioBaseCode());
                    if (base == null) {
                        amount = BigDecimal.ZERO;
                        source = "比例基项[" + line.ratioBaseCode() + "]缺失或不在前面";
                    } else {
                        amount = round2(base.multiply(nvl(line.ratioValue())));
                        source = base.stripTrailingZeros().toPlainString() + "×"
                                + nvl(line.ratioValue()).stripTrailingZeros().toPlainString();
                    }
                }
                case TYPE_ATTENDANCE -> {
                    RuleOutcome outcome = evalAttendanceRule(line, input);
                    amount = outcome.amount();
                    source = outcome.source();
                }
                case TYPE_MANUAL -> {
                    amount = round2(input.manualAmounts().getOrDefault(line.itemId(), BigDecimal.ZERO));
                    source = "手工录入";
                }
                default -> {
                    amount = BigDecimal.ZERO;
                    source = "公式暂未启用";
                }
            }

            items.add(new CalculatedItem(line.itemId(), line.itemCode(), line.itemName(), line.direction(),
                    line.valueType(), amount, source, line.sortOrder()));
            computedByCode.put(line.itemCode(), amount);
            if (line.direction() == DIRECTION_DEDUCTION) {
                deduction = deduction.add(amount);
            } else {
                gross = gross.add(amount);
            }
        }

        BigDecimal net = round2(gross.subtract(deduction));
        return new CalcResult(items, round2(gross), round2(deduction), net);
    }

    /**
     * 考勤联动规则求值结果
     */
    private record RuleOutcome(BigDecimal amount, String source) {
    }

    /**
     * 考勤联动规则求值
     */
    private static RuleOutcome evalAttendanceRule(SchemeItemLine line, EmployeeCalcInput input) {
        BigDecimal price = nvl(line.unitPrice());
        int tolerance = line.tolerance() != null ? Math.max(line.tolerance(), 0) : 0;
        String rule = line.attRule() == null ? "" : line.attRule();
        return switch (rule) {
            case "LATE_TIMES" -> {
                int times = Math.max(0, input.lateTimes() - tolerance);
                yield new RuleOutcome(round2(price.multiply(BigDecimal.valueOf(times))),
                        "迟到" + input.lateTimes() + "次×" + price + "（豁免" + tolerance + "次）");
            }
            case "LATE_MINUTES" -> {
                int minutes = Math.max(0, input.lateMinutes() - tolerance);
                yield new RuleOutcome(round2(price.multiply(BigDecimal.valueOf(minutes))),
                        "迟到" + input.lateMinutes() + "分×" + price + "（豁免" + tolerance + "分）");
            }
            case "ABSENT_DAYS" -> new RuleOutcome(
                    round2(price.multiply(BigDecimal.valueOf(input.absentDays()))),
                    "旷工" + input.absentDays() + "天×" + price);
            case "PERSONAL_LEAVE_HOURS" -> new RuleOutcome(
                    round2(price.multiply(nvl(input.personalLeaveHours()))),
                    "事假" + input.personalLeaveHours() + "小时×" + price);
            case "OVERTIME_HOURS" -> new RuleOutcome(
                    round2(price.multiply(nvl(input.overtimeHours()))),
                    "加班" + input.overtimeHours() + "小时×" + price);
            case "FULL_ATTENDANCE" -> input.fullAttendance()
                    ? new RuleOutcome(round2(price), "全勤奖励")
                    : new RuleOutcome(BigDecimal.ZERO, "非全勤（有迟到/旷工/请假）");
            case "ATTEND_DAYS" -> new RuleOutcome(
                    round2(price.multiply(nvl(input.attendDays()))),
                    "出勤" + input.attendDays() + "天×" + price);
            case "WORK_HOURS" -> new RuleOutcome(
                    round2(price.multiply(nvl(input.workHours()))),
                    "工时" + input.workHours() + "小时×" + price);
            default -> new RuleOutcome(BigDecimal.ZERO, "未知考勤规则[" + rule + "]");
        };
    }

    private static BigDecimal nvl(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

    private static BigDecimal round2(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }
}
