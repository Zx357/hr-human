package com.kadmin.salary.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.kadmin.attendance.service.AttendanceService;
import com.kadmin.hr.domain.HrApplication;
import com.kadmin.hr.domain.HrEmployee;
import com.kadmin.hr.mapper.EmployeeMapper;
import com.kadmin.hr.mapper.HrApplicationMapper;
import com.kadmin.organization.domain.OrgUnit;
import com.kadmin.organization.mapper.OrgUnitMapper;
import com.kadmin.salary.domain.SalPayrollBatch;
import com.kadmin.salary.domain.SalPayrollItem;
import com.kadmin.salary.domain.SalPayrollPayslip;
import com.kadmin.salary.domain.SalSalaryArchive;
import com.kadmin.salary.domain.SalSalaryArchiveItem;
import com.kadmin.salary.domain.SalSchemeItem;
import com.kadmin.salary.domain.SalSalaryItemDef;
import com.kadmin.salary.mapper.SalPayrollBatchMapper;
import com.kadmin.salary.mapper.SalPayrollItemMapper;
import com.kadmin.salary.mapper.SalPayrollPayslipMapper;
import com.kadmin.salary.mapper.SalSalaryArchiveItemMapper;
import com.kadmin.salary.mapper.SalSalaryArchiveMapper;
import com.kadmin.salary.mapper.SalSalaryItemDefMapper;
import com.kadmin.system.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 工资核算服务：批次状态机（0核算中→1已核算→2已确认→3已发放）
 *
 * 批次是账本：核算时快照档案金额/项名称到 payslip_item，之后档案或方案的任何变更
 * 都不影响已核算批次；重算仅允许在确认前，删除重建（batch_id+employee_id 唯一键幂等）。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PayrollService extends ServiceImpl<SalPayrollBatchMapper, SalPayrollBatch> {

    public static final int STATUS_COMPUTING = 0;
    public static final int STATUS_COMPUTED = 1;
    public static final int STATUS_CONFIRMED = 2;
    public static final int STATUS_PUBLISHED = 3;

    private final EmployeeMapper employeeMapper;
    private final OrgUnitMapper orgUnitMapper;
    private final HrApplicationMapper applicationMapper;
    private final AttendanceService attendanceService;
    private final SalSalaryArchiveMapper archiveMapper;
    private final SalSalaryArchiveItemMapper archiveItemMapper;
    private final SalSalaryItemDefMapper itemDefMapper;
    private final SalarySchemeService schemeService;
    private final SalPayrollPayslipMapper payslipMapper;
    private final SalPayrollItemMapper payrollItemMapper;
    private final NotificationService notificationService;

    /**
     * 创建批次（同月同公司唯一），创建即执行首次核算
     */
    @Transactional
    public SalPayrollBatch createBatch(String yearMonth, Long companyId, String remark) {
        validateYearMonth(yearMonth);
        Long exists = baseMapper.selectCount(new LambdaQueryWrapper<SalPayrollBatch>()
                .eq(SalPayrollBatch::getYearMonth, yearMonth)
                .eq(SalPayrollBatch::getCompanyId, companyId));
        if (exists != null && exists > 0) {
            throw new IllegalArgumentException("该月该公司已存在工资批次，请直接重算");
        }
        SalPayrollBatch batch = new SalPayrollBatch();
        batch.setYearMonth(yearMonth);
        batch.setCompanyId(companyId);
        batch.setStatus(STATUS_COMPUTING);
        batch.setRemark(remark);
        batch.setTotalGross(BigDecimal.ZERO);
        batch.setTotalNet(BigDecimal.ZERO);
        batch.setEmployeeCount(0);
        save(batch);
        computeBatch(batch.getId());
        return getById(batch.getId());
    }

    /**
     * 核算/重算：仅状态<已确认允许；先清空既有工资条再全量重建（单事务，失败整体回滚）
     */
    @Transactional
    public SalPayrollBatch computeBatch(Long batchId) {
        SalPayrollBatch batch = getById(batchId);
        if (batch == null) {
            throw new IllegalArgumentException("批次不存在");
        }
        if (batch.getStatus() >= STATUS_CONFIRMED) {
            throw new IllegalArgumentException("批次已确认，不能重算；请先解锁");
        }
        // 0/1 → 0（核算中），条件更新防并发重算
        boolean locked = update(new LambdaUpdateWrapper<SalPayrollBatch>()
                .eq(SalPayrollBatch::getId, batchId)
                .lt(SalPayrollBatch::getStatus, STATUS_CONFIRMED)
                .set(SalPayrollBatch::getStatus, STATUS_COMPUTING));
        if (!locked) {
            throw new IllegalArgumentException("批次状态已变化，请刷新后重试");
        }

        List<SalPayrollPayslip> old = payslipMapper.selectList(new LambdaQueryWrapper<SalPayrollPayslip>()
                .eq(SalPayrollPayslip::getBatchId, batchId));
        if (!old.isEmpty()) {
            payrollItemMapper.delete(new LambdaQueryWrapper<SalPayrollItem>()
                    .in(SalPayrollItem::getPayslipId, old.stream().map(SalPayrollPayslip::getId).toList()));
            payslipMapper.delete(new LambdaQueryWrapper<SalPayrollPayslip>()
                    .eq(SalPayrollPayslip::getBatchId, batchId));
        }

        List<PayslipWithItems> computed = computeAllPayslips(batch);
        for (PayslipWithItems p : computed) {
            payslipMapper.insert(p.payslip());
            for (SalPayrollItem item : p.items()) {
                item.setPayslipId(p.payslip().getId());
                payrollItemMapper.insert(item);
            }
        }

        BigDecimal totalGross = computed.stream()
                .map(p -> p.payslip().getGrossPay()).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalNet = computed.stream()
                .map(p -> p.payslip().getNetPay()).reduce(BigDecimal.ZERO, BigDecimal::add);
        update(new LambdaUpdateWrapper<SalPayrollBatch>()
                .eq(SalPayrollBatch::getId, batchId)
                .set(SalPayrollBatch::getStatus, STATUS_COMPUTED)
                .set(SalPayrollBatch::getEmployeeCount, computed.size())
                .set(SalPayrollBatch::getTotalGross, totalGross)
                .set(SalPayrollBatch::getTotalNet, totalNet));
        log.info("工资批次核算完成: batchId={}, month={}, company={}, employees={}, totalGross={}, totalNet={}",
                batchId, batch.getYearMonth(), batch.getCompanyId(), computed.size(), totalGross, totalNet);
        return getById(batchId);
    }

    /**
     * 确认批次（1已核算→2已确认），确认后锁定
     */
    public void confirmBatch(Long batchId) {
        boolean updated = update(new LambdaUpdateWrapper<SalPayrollBatch>()
                .eq(SalPayrollBatch::getId, batchId)
                .eq(SalPayrollBatch::getStatus, STATUS_COMPUTED)
                .set(SalPayrollBatch::getStatus, STATUS_CONFIRMED));
        if (!updated) {
            throw new IllegalArgumentException("仅已核算状态的批次可以确认");
        }
    }

    /**
     * 发放批次（2已确认→3已发放）：员工端可见并发送站内通知
     */
    @Transactional
    public void publishBatch(Long batchId) {
        SalPayrollBatch batch = getById(batchId);
        if (batch == null) {
            throw new IllegalArgumentException("批次不存在");
        }
        boolean updated = update(new LambdaUpdateWrapper<SalPayrollBatch>()
                .eq(SalPayrollBatch::getId, batchId)
                .eq(SalPayrollBatch::getStatus, STATUS_CONFIRMED)
                .set(SalPayrollBatch::getStatus, STATUS_PUBLISHED));
        if (!updated) {
            throw new IllegalArgumentException("仅已确认状态的批次可以发放");
        }
        List<SalPayrollPayslip> payslips = payslipMapper.selectList(new LambdaQueryWrapper<SalPayrollPayslip>()
                .eq(SalPayrollPayslip::getBatchId, batchId));
        for (SalPayrollPayslip payslip : payslips) {
            try {
                String content = batch.getYearMonth() + " 月工资条已发放，实发 "
                        + payslip.getNetPay() + " 元，请查看详情";
                // 落库 + WebSocket 实时推送（NotificationService 内部发布推送事件）
                notificationService.notify(payslip.getEmployeeId(), "salary", "工资条发放通知",
                        content, payslip.getId(), "/minePages/payslip");
            } catch (Exception e) {
                log.warn("工资条发放通知失败: payslipId={}", payslip.getId(), e);
            }
        }
    }

    /**
     * 解锁批次（2已确认→1已核算），允许重算；调用方应记录操作日志
     */
    public void unlockBatch(Long batchId) {
        boolean updated = update(new LambdaUpdateWrapper<SalPayrollBatch>()
                .eq(SalPayrollBatch::getId, batchId)
                .eq(SalPayrollBatch::getStatus, STATUS_CONFIRMED)
                .set(SalPayrollBatch::getStatus, STATUS_COMPUTED));
        if (!updated) {
            throw new IllegalArgumentException("仅已确认状态的批次可以解锁");
        }
    }

    /**
     * 编辑手工项金额（仅未确认批次），并重算该工资条汇总
     */
    @Transactional
    public void updateManualItem(Long payrollItemId, BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("金额不合法");
        }
        SalPayrollItem item = payrollItemMapper.selectById(payrollItemId);
        if (item == null) {
            throw new IllegalArgumentException("明细项不存在");
        }
        if (item.getValueType() == null || item.getValueType() != SalaryCalcEngine.TYPE_MANUAL) {
            throw new IllegalArgumentException("仅手工录入项可以修改金额");
        }
        SalPayrollPayslip payslip = payslipMapper.selectById(item.getPayslipId());
        SalPayrollBatch batch = payslip != null ? getById(payslip.getBatchId()) : null;
        if (batch == null || batch.getStatus() >= STATUS_CONFIRMED) {
            throw new IllegalArgumentException("批次已确认，不能修改；请先解锁");
        }
        item.setAmount(amount);
        item.setSource("手工录入");
        payrollItemMapper.updateById(item);
        recomputePayslipTotals(item.getPayslipId());
    }

    /**
     * 删除批次（仅未确认状态：核算中/已核算），连级联删除工资条与明细
     */
    @Transactional
    public void deleteBatch(Long batchId) {
        SalPayrollBatch batch = getById(batchId);
        if (batch == null) {
            throw new IllegalArgumentException("批次不存在");
        }
        if (batch.getStatus() >= STATUS_CONFIRMED) {
            throw new IllegalArgumentException("已确认/已发放的批次不能删除，账本数据须留痕");
        }
        List<SalPayrollPayslip> payslips = payslipMapper.selectList(new LambdaQueryWrapper<SalPayrollPayslip>()
                .eq(SalPayrollPayslip::getBatchId, batchId));
        if (!payslips.isEmpty()) {
            payrollItemMapper.delete(new LambdaQueryWrapper<SalPayrollItem>()
                    .in(SalPayrollItem::getPayslipId, payslips.stream().map(SalPayrollPayslip::getId).toList()));
            payslipMapper.delete(new LambdaQueryWrapper<SalPayrollPayslip>()
                    .eq(SalPayrollPayslip::getBatchId, batchId));
        }
        removeById(batchId);
    }

    /**
     * 批次分页
     */
    public Page<SalPayrollBatch> pageBatches(int pageNum, int pageSize, String yearMonth, Long companyId) {
        Page<SalPayrollBatch> page = page(new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<SalPayrollBatch>()
                        .eq(yearMonth != null && !yearMonth.isBlank(), SalPayrollBatch::getYearMonth, yearMonth)
                        .eq(companyId != null, SalPayrollBatch::getCompanyId, companyId)
                        .orderByDesc(SalPayrollBatch::getYearMonth)
                        .orderByDesc(SalPayrollBatch::getId));
        for (SalPayrollBatch batch : page.getRecords()) {
            OrgUnit company = batch.getCompanyId() == null ? null : orgUnitMapper.selectById(batch.getCompanyId());
            batch.setCompanyName(company != null ? company.getUnitName() : null);
        }
        return page;
    }

    /**
     * 批次内工资条分页
     */
    public Page<SalPayrollPayslip> pagePayslips(Long batchId, int pageNum, int pageSize,
            String employeeName, String employeeNo) {
        Page<SalPayrollPayslip> page = payslipMapper.selectPage(new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<SalPayrollPayslip>()
                        .eq(SalPayrollPayslip::getBatchId, batchId)
                        .like(employeeName != null && !employeeName.isBlank(),
                                SalPayrollPayslip::getEmployeeName, employeeName)
                        .like(employeeNo != null && !employeeNo.isBlank(),
                                SalPayrollPayslip::getEmployeeNo, employeeNo)
                        .orderByDesc(SalPayrollPayslip::getNetPay));
        SalPayrollBatch batch = getById(batchId);
        String yearMonth = batch != null ? batch.getYearMonth() : null;
        for (SalPayrollPayslip payslip : page.getRecords()) {
            payslip.setYearMonth(yearMonth);
        }
        return page;
    }

    /**
     * 工资条详情（明细行按方向与顺序分组返回）
     */
    public Map<String, Object> payslipDetail(Long payslipId) {
        SalPayrollPayslip payslip = payslipMapper.selectById(payslipId);
        if (payslip == null) {
            throw new IllegalArgumentException("工资条不存在");
        }
        SalPayrollBatch batch = getById(payslip.getBatchId());
        List<SalPayrollItem> items = payrollItemMapper.selectList(new LambdaQueryWrapper<SalPayrollItem>()
                .eq(SalPayrollItem::getPayslipId, payslipId)
                .orderByAsc(SalPayrollItem::getDirection)
                .orderByAsc(SalPayrollItem::getSortOrder)
                .orderByAsc(SalPayrollItem::getId));
        Map<String, Object> result = new HashMap<>();
        result.put("payslip", payslip);
        result.put("yearMonth", batch != null ? batch.getYearMonth() : null);
        result.put("batchStatus", batch != null ? batch.getStatus() : null);
        result.put("items", items);
        return result;
    }

    /**
     * 批次导出：固定列（工号/姓名）+ 动态薪资项列（按方案内出现顺序）+ 应发/扣款/实发
     */
    public Map<String, Object> buildExportData(Long batchId) {
        SalPayrollBatch batch = getById(batchId);
        if (batch == null) {
            throw new IllegalArgumentException("批次不存在");
        }
        List<SalPayrollPayslip> payslips = payslipMapper.selectList(new LambdaQueryWrapper<SalPayrollPayslip>()
                .eq(SalPayrollPayslip::getBatchId, batchId)
                .orderByAsc(SalPayrollPayslip::getEmployeeNo));
        List<SalPayrollItem> allItems = payslips.isEmpty() ? List.of()
                : payrollItemMapper.selectList(new LambdaQueryWrapper<SalPayrollItem>()
                        .in(SalPayrollItem::getPayslipId, payslips.stream().map(SalPayrollPayslip::getId).toList()));

        // 动态列：按 (方向, 项ID) 去重排序，保证所有员工同一项对齐同一列
        Map<Long, SalPayrollItem> columnDefs = new LinkedHashMap<>();
        allItems.stream()
                .sorted(Comparator.comparing(SalPayrollItem::getDirection)
                        .thenComparing(SalPayrollItem::getSortOrder).thenComparing(SalPayrollItem::getId))
                .forEach(item -> columnDefs.putIfAbsent(item.getItemId(), item));

        Map<Long, Map<Long, BigDecimal>> amountByPayslip = new HashMap<>();
        for (SalPayrollItem item : allItems) {
            amountByPayslip.computeIfAbsent(item.getPayslipId(), k -> new HashMap<>())
                    .put(item.getItemId(), item.getAmount());
        }

        List<String> headers = new ArrayList<>();
        headers.add("工号");
        headers.add("姓名");
        for (SalPayrollItem def : columnDefs.values()) {
            headers.add((def.getDirection() == SalaryCalcEngine.DIRECTION_DEDUCTION ? "[扣]" : "")
                    + def.getItemName());
        }
        headers.add("应发合计");
        headers.add("扣款合计");
        headers.add("实发");

        List<List<Object>> rows = new ArrayList<>();
        for (SalPayrollPayslip payslip : payslips) {
            List<Object> row = new ArrayList<>();
            row.add(payslip.getEmployeeNo());
            row.add(payslip.getEmployeeName());
            Map<Long, BigDecimal> amounts = amountByPayslip.getOrDefault(payslip.getId(), Map.of());
            for (Long itemId : columnDefs.keySet()) {
                row.add(amounts.getOrDefault(itemId, BigDecimal.ZERO));
            }
            row.add(payslip.getGrossPay());
            row.add(payslip.getTotalDeduction());
            row.add(payslip.getNetPay());
            rows.add(row);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("fileName", "工资表-" + batch.getYearMonth() + ".xlsx");
        result.put("headers", headers);
        result.put("rows", rows);
        return result;
    }

    /**
     * 员工本人工资条分页（仅已发放批次；接口层必须以当前登录员工过滤）
     */
    public Page<SalPayrollPayslip> pageMyPayslips(Long employeeId, int pageNum, int pageSize) {
        List<Long> publishedBatchIds = publishedBatchIds();
        if (publishedBatchIds.isEmpty()) {
            return new Page<>(pageNum, pageSize);
        }
        return payslipMapper.selectPage(new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<SalPayrollPayslip>()
                        .eq(SalPayrollPayslip::getEmployeeId, employeeId)
                        .in(SalPayrollPayslip::getBatchId, publishedBatchIds)
                        .orderByDesc(SalPayrollPayslip::getId));
    }

    /**
     * 员工查看本人工资条详情（归属校验），首次查看自动标记已读
     */
    public Map<String, Object> myPayslipDetail(Long employeeId, Long payslipId) {
        SalPayrollPayslip payslip = payslipMapper.selectById(payslipId);
        if (payslip == null || !employeeId.equals(payslip.getEmployeeId())) {
            throw new IllegalArgumentException("工资条不存在");
        }
        SalPayrollBatch batch = getById(payslip.getBatchId());
        if (batch == null || batch.getStatus() < STATUS_PUBLISHED) {
            throw new IllegalArgumentException("工资条尚未发放");
        }
        if (payslip.getReadFlag() == null || payslip.getReadFlag() != 1) {
            payslipMapper.update(new LambdaUpdateWrapper<SalPayrollPayslip>()
                    .eq(SalPayrollPayslip::getId, payslipId)
                    .set(SalPayrollPayslip::getReadFlag, 1)
                    .set(SalPayrollPayslip::getReadTime, LocalDateTime.now()));
        }
        return payslipDetail(payslipId);
    }

    /**
     * 员工确认工资条（本人）
     */
    public void confirmMyPayslip(Long employeeId, Long payslipId) {
        int updated = payslipMapper.update(null, new LambdaUpdateWrapper<SalPayrollPayslip>()
                .eq(SalPayrollPayslip::getId, payslipId)
                .eq(SalPayrollPayslip::getEmployeeId, employeeId)
                .eq(SalPayrollPayslip::getConfirmFlag, 0)
                .set(SalPayrollPayslip::getConfirmFlag, 1)
                .set(SalPayrollPayslip::getConfirmTime, LocalDateTime.now()));
        if (updated == 0) {
            throw new IllegalArgumentException("工资条不存在或已确认");
        }
    }

    // ==================== 核算 ====================

    private record PayslipWithItems(SalPayrollPayslip payslip, List<SalPayrollItem> items) {
    }

    /**
     * 批量预载（档案/明细/方案/考勤/请假单）后逐员工内存核算
     */
    private List<PayslipWithItems> computeAllPayslips(SalPayrollBatch batch) {
        String yearMonth = batch.getYearMonth();
        List<Long> orgIds = expandOrgIds(batch.getCompanyId());

        // 1. 公司内在职员工
        List<HrEmployee> employees = orgIds.isEmpty() ? List.of()
                : employeeMapper.selectList(new LambdaQueryWrapper<HrEmployee>()
                        .eq(HrEmployee::getStatus, 1)
                        .in(HrEmployee::getDeptId, orgIds));
        if (employees.isEmpty()) {
            return List.of();
        }
        List<Long> employeeIds = employees.stream().map(HrEmployee::getId).toList();
        Map<Long, HrEmployee> employeeMap = employees.stream()
                .collect(Collectors.toMap(HrEmployee::getId, Function.identity(), (a, b) -> a));

        // 2. 有档案的员工才参与核算
        List<SalSalaryArchive> archives = archiveMapper.selectList(new LambdaQueryWrapper<SalSalaryArchive>()
                .in(SalSalaryArchive::getEmployeeId, employeeIds));
        if (archives.isEmpty()) {
            return List.of();
        }

        // 3. 档案固定项明细
        Map<Long, List<SalSalaryArchiveItem>> archiveItemsByArchive = archiveItemMapper.selectList(
                        new LambdaQueryWrapper<SalSalaryArchiveItem>()
                                .in(SalSalaryArchiveItem::getArchiveId,
                                        archives.stream().map(SalSalaryArchive::getId).toList()))
                .stream()
                .collect(Collectors.groupingBy(SalSalaryArchiveItem::getArchiveId));

        // 4. 方案明细 + 项定义（按方案分组、排好序的求值行）
        List<Long> schemeIds = archives.stream().map(SalSalaryArchive::getSchemeId).distinct().toList();
        Map<Long, List<SalaryCalcEngine.SchemeItemLine>> schemeLines = new HashMap<>();
        for (Long schemeId : schemeIds) {
            List<SalaryCalcEngine.SchemeItemLine> lines = new ArrayList<>();
            for (SalSchemeItem schemeItem : schemeService.listSchemeItems(schemeId)) {
                SalSalaryItemDef def = itemDefMapper.selectById(schemeItem.getItemId());
                if (def == null || def.getEnabled() == null || def.getEnabled() != 1) {
                    continue;
                }
                lines.add(new SalaryCalcEngine.SchemeItemLine(def.getId(), def.getItemCode(), def.getItemName(),
                        def.getDirection() != null ? def.getDirection() : SalaryCalcEngine.DIRECTION_INCOME,
                        def.getValueType() != null ? def.getValueType() : SalaryCalcEngine.TYPE_FIXED,
                        def.getRatioBaseCode(), def.getRatioValue(), def.getAttRule(), def.getUnitPrice(),
                        def.getTolerance(), schemeItem.getSortOrder() != null ? schemeItem.getSortOrder() : 0));
            }
            schemeLines.put(schemeId, lines);
        }

        // 5. 月考勤汇总（迟到/旷工/出勤/加班）
        Map<Long, Map<String, Object>> attendanceByEmployee = attendanceService
                .getMonthlyAttendance(yearMonth, List.of(batch.getCompanyId()), null, null).stream()
                .filter(item -> item.get("employeeId") != null)
                .collect(Collectors.toMap(item -> (Long) item.get("employeeId"), Function.identity(),
                        (a, b) -> a));

        // 6. 已审批事假小时（按申请单时长对窗口日历天比例折算，与对账导出口径一致）
        Map<Long, BigDecimal> personalLeaveHours = loadPersonalLeaveHours(employeeIds, yearMonth);

        List<PayslipWithItems> results = new ArrayList<>();
        for (SalSalaryArchive archive : archives) {
            HrEmployee employee = employeeMap.get(archive.getEmployeeId());
            if (employee == null) {
                continue;
            }
            List<SalaryCalcEngine.SchemeItemLine> lines = schemeLines.get(archive.getSchemeId());
            if (lines == null || lines.isEmpty()) {
                continue;
            }
            Map<String, Object> att = attendanceByEmployee.getOrDefault(archive.getEmployeeId(), Map.of());
            BigDecimal personalHours = personalLeaveHours.getOrDefault(archive.getEmployeeId(), BigDecimal.ZERO);
            boolean fullAttendance = intOf(att.get("lateTimes")) == 0
                    && intOf(att.get("absentDays")) == 0
                    && intOf(att.get("leaveDays")) == 0;

            Map<Long, BigDecimal> fixedAmounts = archiveItemsByArchive
                    .getOrDefault(archive.getId(), List.of()).stream()
                    .collect(Collectors.toMap(SalSalaryArchiveItem::getItemId,
                            item -> item.getAmount() != null ? item.getAmount() : BigDecimal.ZERO,
                            (a, b) -> a));

            SalaryCalcEngine.EmployeeCalcInput input = new SalaryCalcEngine.EmployeeCalcInput(
                    archive.getEmployeeId(),
                    BigDecimal.valueOf(intOf(att.get("actualDays"))),
                    intOf(att.get("lateTimes")),
                    intOf(att.get("totalLateMinutes")),
                    intOf(att.get("absentDays")),
                    personalHours,
                    decimalOf(att.get("totalOvertimeHours")),
                    decimalOf(att.get("totalWorkHours")),
                    fullAttendance,
                    fixedAmounts,
                    Map.of());

            SalaryCalcEngine.CalcResult calc = SalaryCalcEngine.calculate(lines, input);

            SalPayrollPayslip payslip = new SalPayrollPayslip();
            payslip.setBatchId(batch.getId());
            payslip.setEmployeeId(archive.getEmployeeId());
            payslip.setEmployeeNo(employee.getEmployeeNo());
            payslip.setEmployeeName(employee.getName());
            payslip.setGrossPay(calc.grossPay());
            payslip.setTotalDeduction(calc.totalDeduction());
            payslip.setNetPay(calc.netPay());
            payslip.setReadFlag(0);
            payslip.setConfirmFlag(0);

            List<SalPayrollItem> items = calc.items().stream().map(ci -> {
                SalPayrollItem entity = new SalPayrollItem();
                entity.setItemId(ci.itemId());
                entity.setItemCode(ci.itemCode());
                entity.setItemName(ci.itemName());
                entity.setDirection(ci.direction());
                entity.setValueType(ci.valueType());
                entity.setAmount(ci.amount());
                entity.setSource(ci.source());
                entity.setSortOrder(ci.sortOrder());
                return entity;
            }).collect(Collectors.toList());

            results.add(new PayslipWithItems(payslip, items));
        }
        return results;
    }

    /**
     * 已审批事假单（title='2' 事假）在月份窗口内的折算小时数，按员工分组
     */
    private Map<Long, BigDecimal> loadPersonalLeaveHours(List<Long> employeeIds, String yearMonth) {
        YearMonth ym = YearMonth.parse(yearMonth);
        LocalDateTime windowStart = ym.atDay(1).atStartOfDay();
        LocalDateTime windowEnd = ym.atEndOfMonth().atTime(java.time.LocalTime.MAX);
        List<HrApplication> apps = applicationMapper.selectList(new LambdaQueryWrapper<HrApplication>()
                .in(HrApplication::getEmployeeId, employeeIds)
                .eq(HrApplication::getStatus, 1)
                .eq(HrApplication::getAppType, "leave")
                .eq(HrApplication::getTitle, "2")
                .le(HrApplication::getStartTime, windowEnd)
                .ge(HrApplication::getEndTime, windowStart));
        Map<Long, BigDecimal> result = new HashMap<>();
        for (HrApplication app : apps) {
            BigDecimal hours = proratedHoursInWindow(app, windowStart, windowEnd);
            if (hours.compareTo(BigDecimal.ZERO) > 0 && app.getEmployeeId() != null) {
                result.merge(app.getEmployeeId(), hours, BigDecimal::add);
            }
        }
        return result;
    }

    /**
     * 申请单时长按与窗口的重叠比例折算（跨月分摊，整体不重复计）
     */
    private BigDecimal proratedHoursInWindow(HrApplication app, LocalDateTime windowStart, LocalDateTime windowEnd) {
        if (app.getStartTime() == null || app.getEndTime() == null || app.getDuration() == null
                || app.getDuration().compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        LocalDateTime overlapStart = app.getStartTime().isAfter(windowStart) ? app.getStartTime() : windowStart;
        LocalDateTime overlapEnd = app.getEndTime().isBefore(windowEnd) ? app.getEndTime() : windowEnd;
        if (!overlapStart.isBefore(overlapEnd)) {
            return BigDecimal.ZERO;
        }
        long totalMinutes = ChronoUnit.MINUTES.between(app.getStartTime(), app.getEndTime());
        long overlapMinutes = ChronoUnit.MINUTES.between(overlapStart, overlapEnd);
        if (totalMinutes <= 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal ratio = BigDecimal.valueOf(overlapMinutes).divide(BigDecimal.valueOf(totalMinutes), 6,
                RoundingMode.HALF_UP);
        return app.getDuration().multiply(ratio).setScale(2, RoundingMode.HALF_UP).min(app.getDuration());
    }

    /**
     * 重算单条工资条汇总（明细行求和）
     */
    private void recomputePayslipTotals(Long payslipId) {
        List<SalPayrollItem> items = payrollItemMapper.selectList(new LambdaQueryWrapper<SalPayrollItem>()
                .eq(SalPayrollItem::getPayslipId, payslipId));
        BigDecimal gross = BigDecimal.ZERO;
        BigDecimal deduction = BigDecimal.ZERO;
        for (SalPayrollItem item : items) {
            BigDecimal amount = item.getAmount() != null ? item.getAmount() : BigDecimal.ZERO;
            if (item.getDirection() != null && item.getDirection() == SalaryCalcEngine.DIRECTION_DEDUCTION) {
                deduction = deduction.add(amount);
            } else {
                gross = gross.add(amount);
            }
        }
        payslipMapper.update(null, new LambdaUpdateWrapper<SalPayrollPayslip>()
                .eq(SalPayrollPayslip::getId, payslipId)
                .set(SalPayrollPayslip::getGrossPay, gross.setScale(2, RoundingMode.HALF_UP))
                .set(SalPayrollPayslip::getTotalDeduction, deduction.setScale(2, RoundingMode.HALF_UP))
                .set(SalPayrollPayslip::getNetPay, gross.subtract(deduction).setScale(2, RoundingMode.HALF_UP)));
    }

    private List<Long> publishedBatchIds() {
        List<SalPayrollBatch> batches = list(new LambdaQueryWrapper<SalPayrollBatch>()
                .eq(SalPayrollBatch::getStatus, STATUS_PUBLISHED)
                .select(SalPayrollBatch::getId));
        return batches.stream().map(SalPayrollBatch::getId).toList();
    }

    private List<Long> expandOrgIds(Long orgId) {
        if (orgId == null) {
            return List.of();
        }
        List<Long> childIds = orgUnitMapper.selectOrgAndChildIds(orgId);
        return childIds != null && !childIds.isEmpty() ? childIds : List.of(orgId);
    }

    private void validateYearMonth(String yearMonth) {
        try {
            YearMonth.parse(yearMonth);
        } catch (Exception e) {
            throw new IllegalArgumentException("月份格式不合法，应为 yyyy-MM");
        }
    }

    private int intOf(Object value) {
        return value instanceof Number number ? number.intValue() : 0;
    }

    private BigDecimal decimalOf(Object value) {
        return value instanceof BigDecimal decimal ? decimal : BigDecimal.ZERO;
    }
}
