package com.kadmin.hr.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.kadmin.hr.domain.HrEmployee;
import com.kadmin.hr.domain.HrLeaveQuota;
import com.kadmin.hr.mapper.EmployeeMapper;
import com.kadmin.hr.mapper.HrLeaveQuotaMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 假期额度服务：按员工+年度+假期类型管理额度，请假审批通过后扣减
 *
 * 扣减采用"条件更新"（used_hours + 扣减值 <= total_hours 才生效），
 * 并发审批同一员工时不会超额；仅对配置了额度的员工/类型生效，未配置不拦截。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LeaveQuotaService extends ServiceImpl<HrLeaveQuotaMapper, HrLeaveQuota> {

    private final EmployeeMapper employeeMapper;

    /**
     * 假期类型展示名（与字典 leave_type 的 value 对应，仅用于后端提示消息）
     */
    private static final Map<String, String> LEAVE_TYPE_LABELS = Map.of(
            "1", "年假", "2", "事假", "3", "病假",
            "4", "婚假", "5", "产假", "6", "陪产假", "7", "丧假");

    /**
     * 分页查询额度（支持按员工姓名/工号、年度、假期类型过滤）
     */
    public Page<HrLeaveQuota> pageQuotas(int pageNum, int pageSize, Integer year, String leaveType,
            String employeeName, String employeeNo) {
        LambdaQueryWrapper<HrLeaveQuota> wrapper = new LambdaQueryWrapper<>();
        if (year != null) {
            wrapper.eq(HrLeaveQuota::getYear, year);
        }
        if (leaveType != null && !leaveType.isBlank()) {
            wrapper.eq(HrLeaveQuota::getLeaveType, leaveType);
        }
        List<Long> employeeIds = resolveEmployeeIds(employeeName, employeeNo);
        // 搜索条件命中不到任何员工时直接返回空页，避免退化为全量查询
        if (employeeIds != null && employeeIds.isEmpty()) {
            return new Page<>(pageNum, pageSize);
        }
        wrapper.in(employeeIds != null, HrLeaveQuota::getEmployeeId, employeeIds);
        wrapper.orderByDesc(HrLeaveQuota::getYear).orderByAsc(HrLeaveQuota::getEmployeeId)
                .orderByAsc(HrLeaveQuota::getLeaveType);

        Page<HrLeaveQuota> page = page(new Page<>(pageNum, pageSize), wrapper);
        fillEmployeeInfo(page.getRecords());
        return page;
    }

    /**
     * 某员工某年度的额度列表
     */
    public List<HrLeaveQuota> listByEmployee(Long employeeId, Integer year) {
        if (employeeId == null) {
            return List.of();
        }
        List<HrLeaveQuota> quotas = list(new LambdaQueryWrapper<HrLeaveQuota>()
                .eq(HrLeaveQuota::getEmployeeId, employeeId)
                .eq(year != null, HrLeaveQuota::getYear, year)
                .orderByAsc(HrLeaveQuota::getLeaveType));
        fillEmployeeInfo(quotas);
        return quotas;
    }

    /**
     * 新增/更新额度（按 员工+年度+类型 幂等；used_hours 不允许通过该接口篡改）
     */
    @Transactional
    public HrLeaveQuota saveQuota(HrLeaveQuota input) {
        validateQuota(input);
        HrLeaveQuota existing = getQuota(input.getEmployeeId(), input.getYear(), input.getLeaveType());
        if (existing == null) {
            input.setUsedHours(BigDecimal.ZERO);
            save(input);
            return input;
        }
        existing.setTotalHours(input.getTotalHours());
        updateById(existing);
        return existing;
    }

    /**
     * 删除额度（已使用数据一并删除，仅管理操作）
     */
    public boolean deleteQuota(Long id) {
        return removeById(id);
    }

    /**
     * 请假审批通过后的额度扣减：
     * 未配置额度直接放行；配置了额度则条件更新扣减，余额不足抛出异常（审批事务回滚）
     */
    @Transactional
    public void deductForApprovedLeave(Long employeeId, int year, String leaveType, BigDecimal hours,
            Long applicationId) {
        if (employeeId == null || leaveType == null || leaveType.isBlank()
                || hours == null || hours.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }
        HrLeaveQuota quota = getQuota(employeeId, year, leaveType);
        if (quota == null) {
            // 该员工该假期类型未配置额度：不做限制
            return;
        }
        // 条件更新防并发超额：只有余额足够时才生效
        boolean updated = baseMapper.update(null, new LambdaUpdateWrapper<HrLeaveQuota>()
                .eq(HrLeaveQuota::getId, quota.getId())
                .apply("used_hours + {0} <= total_hours", hours)
                .setSql("used_hours = used_hours + " + hours.stripTrailingZeros().toPlainString())) > 0;
        if (!updated) {
            BigDecimal remain = quota.getTotalHours()
                    .subtract(quota.getUsedHours() != null ? quota.getUsedHours() : BigDecimal.ZERO);
            throw new IllegalArgumentException(
                    quotaLabel(leaveType) + "额度不足：剩余 " + remain.stripTrailingZeros().toPlainString()
                            + " 小时，本次申请 " + hours.stripTrailingZeros().toPlainString()
                            + " 小时，请先调整额度（申请单 #" + applicationId + "）");
        }
        log.info("假期额度扣减成功: employeeId={}, year={}, type={}, hours={}, applicationId={}",
                employeeId, year, leaveType, hours, applicationId);
    }

    private HrLeaveQuota getQuota(Long employeeId, int year, String leaveType) {
        return getOne(new LambdaQueryWrapper<HrLeaveQuota>()
                .eq(HrLeaveQuota::getEmployeeId, employeeId)
                .eq(HrLeaveQuota::getYear, year)
                .eq(HrLeaveQuota::getLeaveType, leaveType)
                .last("LIMIT 1"));
    }

    private void validateQuota(HrLeaveQuota quota) {
        if (quota.getEmployeeId() == null) {
            throw new IllegalArgumentException("员工不能为空");
        }
        if (quota.getYear() == null || quota.getYear() < 2000 || quota.getYear() > 2100) {
            throw new IllegalArgumentException("年度不合法");
        }
        if (quota.getLeaveType() == null || quota.getLeaveType().isBlank()) {
            throw new IllegalArgumentException("假期类型不能为空");
        }
        if (quota.getTotalHours() == null || quota.getTotalHours().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("额度时长必须大于等于0");
        }
    }

    private void fillEmployeeInfo(List<HrLeaveQuota> quotas) {
        if (quotas == null || quotas.isEmpty()) {
            return;
        }
        Set<Long> employeeIds = new LinkedHashSet<>();
        for (HrLeaveQuota quota : quotas) {
            if (quota.getEmployeeId() != null) {
                employeeIds.add(quota.getEmployeeId());
            }
        }
        if (employeeIds.isEmpty()) {
            return;
        }
        Map<Long, HrEmployee> employees = employeeMapper.selectBatchIds(employeeIds).stream()
                .collect(Collectors.toMap(HrEmployee::getId, Function.identity(), (a, b) -> a));
        for (HrLeaveQuota quota : quotas) {
            HrEmployee employee = employees.get(quota.getEmployeeId());
            if (employee != null) {
                quota.setEmployeeName(employee.getName());
                quota.setEmployeeNo(employee.getEmployeeNo());
            }
        }
    }

    /**
     * 按姓名/工号模糊搜索员工ID列表；无搜索条件返回 null 表示不过滤
     */
    private List<Long> resolveEmployeeIds(String employeeName, String employeeNo) {
        boolean hasName = employeeName != null && !employeeName.isBlank();
        boolean hasNo = employeeNo != null && !employeeNo.isBlank();
        if (!hasName && !hasNo) {
            return null;
        }
        return new ArrayList<>(employeeMapper.selectList(new LambdaQueryWrapper<HrEmployee>()
                .like(hasName, HrEmployee::getName, employeeName)
                .like(hasNo, HrEmployee::getEmployeeNo, employeeNo))
                .stream().map(HrEmployee::getId).collect(Collectors.toSet()));
    }

    private String quotaLabel(String leaveType) {
        return LEAVE_TYPE_LABELS.getOrDefault(leaveType, "假期(" + leaveType + ")");
    }
}
