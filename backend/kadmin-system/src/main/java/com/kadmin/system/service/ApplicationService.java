package com.kadmin.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.kadmin.attendance.domain.AttClockRecord;
import com.kadmin.attendance.domain.AttSchedule;
import com.kadmin.attendance.domain.AttShiftPeriod;
import com.kadmin.attendance.mapper.AttClockRecordMapper;
import com.kadmin.attendance.mapper.AttScheduleMapper;
import com.kadmin.attendance.mapper.AttShiftMapper;
import com.kadmin.attendance.mapper.AttShiftPeriodMapper;
import com.kadmin.common.event.AttendanceRecalcEvent;
import com.kadmin.common.event.UserSessionEvictEvent;
import com.kadmin.common.security.LoginUser;
import com.kadmin.hr.domain.HrApplication;
import com.kadmin.hr.domain.HrApprovalRecord;
import com.kadmin.hr.domain.HrEmployee;
import com.kadmin.hr.domain.HrMobileApprover;
import com.kadmin.hr.mapper.EmployeeMapper;
import com.kadmin.hr.mapper.HrApplicationMapper;
import com.kadmin.hr.mapper.HrApprovalRecordMapper;
import com.kadmin.hr.mapper.HrMobileApproverMapper;
import com.kadmin.hr.service.LeaveQuotaService;
import com.kadmin.system.domain.SysApprovalFlow;
import com.kadmin.system.domain.SysApprovalNode;
import com.kadmin.system.mapper.SysApprovalFlowMapper;
import com.kadmin.system.mapper.SysApprovalNodeMapper;
import com.kadmin.system.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ApplicationService extends ServiceImpl<HrApplicationMapper, HrApplication> {

    private final EmployeeMapper employeeMapper;
    private final SysUserMapper sysUserMapper;
    private final AttScheduleMapper scheduleMapper;
    private final AttShiftMapper shiftMapper;
    private final AttShiftPeriodMapper shiftPeriodMapper;
    private final AttClockRecordMapper clockRecordMapper;
    private final SysApprovalFlowMapper flowMapper;
    private final SysApprovalNodeMapper flowNodeMapper;
    private final HrApprovalRecordMapper approvalRecordMapper;
    private final HrMobileApproverMapper mobileApproverMapper;
    private final ApplicationEventPublisher eventPublisher;
    private final NotificationService notificationService;
    private final LeaveQuotaService leaveQuotaService;

    /**
     * 计算加班小时数
     * 根据员工排班和班次时段信息计算加班工时
     * - 有排班：计算选择时间与班次时段的交集
     * - 无排班（休息日）：整段时间都算加班
     */
    public BigDecimal calculateOvertimeHours(Long employeeId, LocalDateTime startTime, LocalDateTime endTime) {
        if (employeeId == null || startTime == null || endTime == null) {
            return BigDecimal.ZERO;
        }
        // 限制跨度，防止超大范围拖垮数据库
        if (ChronoUnit.DAYS.between(startTime, endTime) > 92) {
            throw new IllegalArgumentException("时间跨度不能超过92天");
        }

        BigDecimal totalHours = BigDecimal.ZERO;
        LocalDate startDate = startTime.toLocalDate();
        LocalDate endDate = endTime.toLocalDate();

        // 批量预载排班与时段，避免逐天查库（长假单最多92天=184条SQL的N+1）
        Map<LocalDate, AttSchedule> scheduleMap = loadScheduleMap(employeeId, startDate, endDate);
        Map<Long, List<AttShiftPeriod>> periodsMap = loadPeriodsMap(scheduleMap.values());

        // 遍历每一天
        LocalDate currentDate = startDate;
        while (!currentDate.isAfter(endDate)) {
            AttSchedule schedule = scheduleMap.get(currentDate);

            // 计算当天的加班时间范围
            LocalTime dayStart, dayEnd;
            if (currentDate.equals(startDate)) {
                dayStart = startTime.toLocalTime();
            } else {
                dayStart = LocalTime.of(0, 0);
            }
            if (currentDate.equals(endDate)) {
                dayEnd = endTime.toLocalTime();
            } else {
                dayEnd = LocalTime.of(23, 59);
            }

            if (schedule != null && schedule.getShiftId() != null) {
                // 有排班：计算选择时间与班次时段的交集
                List<AttShiftPeriod> periods = periodsMap.getOrDefault(schedule.getShiftId(), List.of());

                if (!periods.isEmpty()) {
                    for (AttShiftPeriod period : periods) {
                        totalHours = totalHours.add(overlapHours(dayStart, dayEnd,
                                LocalTime.parse(period.getStartTime()), LocalTime.parse(period.getEndTime())));
                    }
                } else {
                    // 有排班但没有时段配置，整段时间都算加班
                    totalHours = totalHours.add(hoursBetween(dayStart, dayEnd));
                }
            } else {
                // 没有排班（休息日），整段时间都算加班
                totalHours = totalHours.add(hoursBetween(dayStart, dayEnd));
            }

            currentDate = currentDate.plusDays(1);
        }

        return totalHours;
    }

    /**
     * 计算请假小时数
     * 根据员工排班和班次时段信息计算实际请假工时
     */
    public BigDecimal calculateLeaveHours(Long employeeId, LocalDateTime startTime, LocalDateTime endTime) {
        if (employeeId == null || startTime == null || endTime == null) {
            return BigDecimal.ZERO;
        }
        // 限制跨度，防止超大范围拖垮数据库
        if (ChronoUnit.DAYS.between(startTime, endTime) > 92) {
            throw new IllegalArgumentException("时间跨度不能超过92天");
        }

        BigDecimal totalHours = BigDecimal.ZERO;
        LocalDate startDate = startTime.toLocalDate();
        LocalDate endDate = endTime.toLocalDate();

        // 批量预载排班与时段，避免逐天查库（N+1）
        Map<LocalDate, AttSchedule> scheduleMap = loadScheduleMap(employeeId, startDate, endDate);
        Map<Long, List<AttShiftPeriod>> periodsMap = loadPeriodsMap(scheduleMap.values());

        // 遍历每一天
        LocalDate currentDate = startDate;
        while (!currentDate.isAfter(endDate)) {
            AttSchedule schedule = scheduleMap.get(currentDate);

            if (schedule != null && schedule.getShiftId() != null) {
                // 获取班次时段列表
                List<AttShiftPeriod> periods = periodsMap.getOrDefault(schedule.getShiftId(), List.of());

                if (!periods.isEmpty()) {
                    // 计算当天的请假时间范围
                    LocalTime dayStart, dayEnd;

                    if (currentDate.equals(startDate)) {
                        dayStart = startTime.toLocalTime();
                    } else {
                        // 非第一天：从第一个时段开始
                        dayStart = LocalTime.parse(periods.get(0).getStartTime());
                    }

                    if (currentDate.equals(endDate)) {
                        dayEnd = endTime.toLocalTime();
                    } else {
                        // 非最后一天：到最后一个时段结束
                        dayEnd = LocalTime.parse(periods.get(periods.size() - 1).getEndTime());
                    }

                    // 遍历每个时段，计算与请假时间的交集
                    for (AttShiftPeriod period : periods) {
                        totalHours = totalHours.add(overlapHours(dayStart, dayEnd,
                                LocalTime.parse(period.getStartTime()), LocalTime.parse(period.getEndTime())));
                    }
                }
            }

            currentDate = currentDate.plusDays(1);
        }

        return totalHours;
    }

    /**
     * 批量预载员工在日期区间内的排班（日期 -> 排班）
     */
    private Map<LocalDate, AttSchedule> loadScheduleMap(Long employeeId, LocalDate startDate, LocalDate endDate) {
        List<AttSchedule> schedules = scheduleMapper.selectList(new LambdaQueryWrapper<AttSchedule>()
                .eq(AttSchedule::getEmployeeId, employeeId)
                .between(AttSchedule::getScheduleDate, startDate, endDate));
        Map<LocalDate, AttSchedule> map = new HashMap<>();
        for (AttSchedule schedule : schedules) {
            map.put(schedule.getScheduleDate(), schedule);
        }
        return map;
    }

    /**
     * 批量预载排班涉及班次的时段列表（班次ID -> 时段列表，按时段顺序）
     */
    private Map<Long, List<AttShiftPeriod>> loadPeriodsMap(java.util.Collection<AttSchedule> schedules) {
        java.util.Set<Long> shiftIds = new java.util.HashSet<>();
        for (AttSchedule schedule : schedules) {
            if (schedule.getShiftId() != null) {
                shiftIds.add(schedule.getShiftId());
            }
        }
        if (shiftIds.isEmpty()) {
            return Map.of();
        }
        Map<Long, List<AttShiftPeriod>> map = new HashMap<>();
        for (AttShiftPeriod period : shiftPeriodMapper.selectList(new LambdaQueryWrapper<AttShiftPeriod>()
                .in(AttShiftPeriod::getShiftId, shiftIds)
                .orderByAsc(AttShiftPeriod::getSortOrder))) {
            map.computeIfAbsent(period.getShiftId(), k -> new ArrayList<>()).add(period);
        }
        return map;
    }

    /**
     * 两时间段的交集小时数（2位小数，半单位进位），无交集返回0
     */
    private BigDecimal overlapHours(LocalTime aStart, LocalTime aEnd, LocalTime bStart, LocalTime bEnd) {
        LocalTime overlapStart = aStart.isAfter(bStart) ? aStart : bStart;
        LocalTime overlapEnd = aEnd.isBefore(bEnd) ? aEnd : bEnd;
        if (!overlapStart.isBefore(overlapEnd)) {
            return BigDecimal.ZERO;
        }
        return hoursBetween(overlapStart, overlapEnd);
    }

    private BigDecimal hoursBetween(LocalTime start, LocalTime end) {
        long minutes = ChronoUnit.MINUTES.between(start, end);
        if (minutes <= 0) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(minutes).divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);
    }

    public Page<HrApplication> getPage(int pageNum, int pageSize, String employeeName, String employeeNo,
            String appType, Integer status, Long employeeId, LocalDate beginTime, LocalDate endTime) {
        return baseMapper.selectPageWithEmployee(new Page<>(pageNum, pageSize), employeeName, employeeNo, appType,
                status, employeeId, beginTime, endTime);
    }

    public Page<HrApplication> getPendingPage(int pageNum, int pageSize, String employeeName, String employeeNo,
            String appType, Long userId) {
        // 获取当前用户的角色ID列表
        List<Long> roleIds = null;
        if (userId != null) {
            roleIds = sysUserMapper.selectRoleIdsByUserId(userId);
        }
        return baseMapper.selectPendingPage(new Page<>(pageNum, pageSize), employeeName, employeeNo, appType, roleIds);
    }

    public Page<HrApplication> getMobilePendingPage(int pageNum, int pageSize, String employeeName, String employeeNo,
            String appType, Long approverEmployeeId) {
        return baseMapper.selectMobilePendingPage(new Page<>(pageNum, pageSize), employeeName, employeeNo, appType,
                approverEmployeeId);
    }

    /**
     * 审批（多级流转，并发安全）
     * 按审批流节点顺序逐级推进：当前节点通过后流转到下一节点，
     * 全部节点通过申请才置为已通过；任一节点拒绝即整体拒绝。
     * 防并发：节点记录有 (application_id, node_id) 唯一键，终态流转用条件更新（仅 status=0 时生效）。
     * 兼容未配置审批流的场景：仅管理员或移动端指定审批人可审批。
     */
    @Transactional
    public boolean approve(Long id, Integer status, String remark, LoginUser loginUser, List<Long> approverRoleIds) {
        HrApplication application = getById(id);
        if (application == null) {
            throw new IllegalArgumentException("申请不存在");
        }
        if (application.getStatus() == null || application.getStatus() != 0) {
            throw new IllegalArgumentException("该申请已处理，无需重复审批");
        }
        if (status == null || (status != 1 && status != 2)) {
            throw new IllegalArgumentException("无效的审批操作");
        }

        Long approveBy = loginUser.getEmployeeId() != null ? loginUser.getEmployeeId() : loginUser.getUserId();
        boolean admin = loginUser.getRoles() != null && loginUser.getRoles().contains("ROLE_ADMIN");

        // 任何人不能审批自己提交的申请（管理员同样回避，防止自批离职/调动等敏感单）
        if (approveBy.equals(application.getEmployeeId())) {
            throw new IllegalArgumentException("不能审批自己提交的申请");
        }

        List<SysApprovalNode> nodes = getActiveFlowNodes(application.getAppType());
        SysApprovalNode currentNode = findCurrentNode(id, nodes);

        // 审批资格校验：管理员 / PC当前节点角色 / 移动端指定审批人（含类型校验）
        if (!admin) {
            boolean eligible;
            if (currentNode != null && approverRoleIds != null && !approverRoleIds.isEmpty()) {
                eligible = currentNode.getRoleId() != null && approverRoleIds.contains(currentNode.getRoleId());
            } else {
                eligible = isMobileApproverForType(approveBy, application.getAppType());
            }
            if (!eligible) {
                throw new IllegalArgumentException("您没有审批该申请的权限");
            }
        }

        // 记录当前节点审批动作（唯一键防并发重复插入）
        HrApprovalRecord record = new HrApprovalRecord();
        record.setApplicationId(id);
        record.setNodeId(currentNode != null ? currentNode.getId() : null);
        record.setNodeName(currentNode != null ? currentNode.getNodeName() : "审批");
        record.setSortOrder(currentNode != null ? currentNode.getSortOrder() : 1);
        record.setApproverId(approveBy);
        record.setApproverName(resolveEmployeeName(approveBy));
        record.setStatus(status);
        record.setComment(remark);
        record.setApproveTime(LocalDateTime.now());
        try {
            approvalRecordMapper.insert(record);
        } catch (org.springframework.dao.DuplicateKeyException e) {
            throw new IllegalArgumentException("该申请刚被他人审批，请刷新后查看");
        }

        // 拒绝：条件更新终结（仅待审批状态可流转，防并发重放）
        if (status == 2) {
            boolean rejected = rejectApplication(id, remark, approveBy);
            if (!rejected) {
                throw new IllegalArgumentException("该申请已被处理");
            }
            notificationService.notify(application.getEmployeeId(), "approval_result", "审批驳回",
                    "你的" + appTypeLabel(application.getAppType()) + "申请已被驳回"
                            + (remark != null && !remark.isBlank() ? "：" + remark : ""),
                    id, "/homePages/application");
            return true;
        }

        // 通过：重算当前节点，无未通过节点即终审通过
        SysApprovalNode nextCurrent = findCurrentNode(id, nodes);
        if (nextCurrent != null) {
            // 还有后续节点：保持待审批，流转到下一节点
            HrApplication entity = new HrApplication();
            entity.setId(id);
            entity.setApproveRemark(remark);
            entity.setApproveBy(approveBy);
            entity.setApproveTime(LocalDateTime.now());
            return updateById(entity);
        }

        // 最后一个节点通过：条件更新置为已通过并执行业务联动
        boolean result = completeApplication(id, remark, approveBy);
        if (result) {
            applyApprovedEffects(application);
            notificationService.notify(application.getEmployeeId(), "approval_result", "审批通过",
                    "你的" + appTypeLabel(application.getAppType()) + "申请已通过审批",
                    id, "/homePages/application");
        }
        return result;
    }

    /**
     * 条件更新为已拒绝（仅 status=0 时生效）
     */
    private boolean rejectApplication(Long id, String remark, Long approveBy) {
        return update(new LambdaUpdateWrapper<HrApplication>()
                .eq(HrApplication::getId, id)
                .eq(HrApplication::getStatus, 0)
                .set(HrApplication::getStatus, 2)
                .set(HrApplication::getApproveRemark, remark)
                .set(HrApplication::getApproveBy, approveBy)
                .set(HrApplication::getApproveTime, LocalDateTime.now()));
    }

    /**
     * 条件更新为已通过（仅 status=0 时生效，防并发重复执行业务联动）
     */
    private boolean completeApplication(Long id, String remark, Long approveBy) {
        return update(new LambdaUpdateWrapper<HrApplication>()
                .eq(HrApplication::getId, id)
                .eq(HrApplication::getStatus, 0)
                .set(HrApplication::getStatus, 1)
                .set(HrApplication::getApproveRemark, remark)
                .set(HrApplication::getApproveBy, approveBy)
                .set(HrApplication::getApproveTime, LocalDateTime.now()));
    }

    /**
     * 撤销申请（仅申请人本人且待审批状态，条件更新防与审批并发冲突）
     */
    public boolean cancel(Long id, LoginUser loginUser) {
        HrApplication application = getById(id);
        if (application == null) {
            throw new IllegalArgumentException("申请不存在");
        }
        if (application.getStatus() == null || application.getStatus() != 0) {
            throw new IllegalArgumentException("仅待审批的申请可以撤销");
        }
        boolean admin = loginUser.getRoles() != null && loginUser.getRoles().contains("ROLE_ADMIN");
        Long selfEmployeeId = loginUser.getEmployeeId() != null ? loginUser.getEmployeeId() : loginUser.getUserId();
        if (!admin && !selfEmployeeId.equals(application.getEmployeeId())) {
            throw new IllegalArgumentException("只能撤销自己的申请");
        }
        boolean updated = update(new LambdaUpdateWrapper<HrApplication>()
                .eq(HrApplication::getId, id)
                .eq(HrApplication::getStatus, 0)
                .set(HrApplication::getStatus, 3));
        if (!updated) {
            throw new IllegalArgumentException("申请状态已变化，请刷新后重试");
        }
        return true;
    }

    /**
     * 审批进度记录（含已处理节点与待处理节点）
     */
    public List<Map<String, Object>> getApprovalRecords(Long applicationId) {
        HrApplication application = getById(applicationId);
        List<Map<String, Object>> result = new ArrayList<>();

        List<HrApprovalRecord> records = approvalRecordMapper.selectList(
                new LambdaQueryWrapper<HrApprovalRecord>()
                        .eq(HrApprovalRecord::getApplicationId, applicationId)
                        .orderByAsc(HrApprovalRecord::getSortOrder)
                        .orderByAsc(HrApprovalRecord::getId));
        for (HrApprovalRecord record : records) {
            Map<String, Object> item = new HashMap<>();
            item.put("nodeName", record.getNodeName());
            item.put("approverName", record.getApproverName());
            item.put("status", record.getStatus());
            item.put("comment", record.getComment());
            item.put("createTime", record.getApproveTime());
            result.add(item);
        }

        // 申请仍待审批时，补充展示未处理节点
        if (application != null && application.getStatus() == 0) {
            List<SysApprovalNode> nodes = getActiveFlowNodes(application.getAppType());
            int processedMaxSort = records.stream()
                    .mapToInt(r -> r.getSortOrder() != null ? r.getSortOrder() : 0)
                    .max().orElse(0);
            List<SysApprovalNode> pendingNodes = nodes.stream()
                    .filter(n -> n.getSortOrder() != null && n.getSortOrder() > processedMaxSort)
                    .collect(Collectors.toList());
            for (SysApprovalNode node : pendingNodes) {
                Map<String, Object> item = new HashMap<>();
                item.put("nodeName", node.getNodeName());
                item.put("approverName", null);
                item.put("status", 0);
                item.put("comment", null);
                item.put("createTime", null);
                result.add(item);
            }
        }
        return result;
    }

    /**
     * 获取申请类型对应启用审批流的审批节点（按顺序）
     */
    private List<SysApprovalNode> getActiveFlowNodes(String appType) {
        if (appType == null) {
            return List.of();
        }
        SysApprovalFlow flow = flowMapper.selectOne(new LambdaQueryWrapper<SysApprovalFlow>()
                .eq(SysApprovalFlow::getFlowType, appType)
                .eq(SysApprovalFlow::getStatus, 1)
                .last("LIMIT 1"));
        if (flow == null) {
            return List.of();
        }
        return flowNodeMapper.selectList(new LambdaQueryWrapper<SysApprovalNode>()
                .eq(SysApprovalNode::getFlowId, flow.getId())
                .orderByAsc(SysApprovalNode::getSortOrder));
    }

    /**
     * 当前待审批节点：第一个没有"通过"记录的节点
     */
    private SysApprovalNode findCurrentNode(Long applicationId, List<SysApprovalNode> nodes) {
        if (nodes == null || nodes.isEmpty()) {
            return null;
        }
        List<HrApprovalRecord> passed = approvalRecordMapper.selectList(
                new LambdaQueryWrapper<HrApprovalRecord>()
                        .eq(HrApprovalRecord::getApplicationId, applicationId)
                        .eq(HrApprovalRecord::getStatus, 1));
        List<Long> passedNodeIds = passed.stream()
                .map(HrApprovalRecord::getNodeId)
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toList());
        return nodes.stream()
                .filter(n -> !passedNodeIds.contains(n.getId()))
                .findFirst()
                .orElse(null);
    }

    /**
     * 申请提交后通知移动端审批人（不含申请人本人；通知失败不影响提交）
     */
    public void notifyApproversOnSubmit(HrApplication application) {
        try {
            List<HrMobileApprover> approvers = mobileApproverMapper.selectList(null);
            String applicantName = resolveEmployeeName(application.getEmployeeId());
            String label = appTypeLabel(application.getAppType());
            for (HrMobileApprover approver : approvers) {
                Long approverEmployeeId = approver.getEmployeeId();
                if (approverEmployeeId == null || approverEmployeeId.equals(application.getEmployeeId())) {
                    continue;
                }
                if (!appTypesMatch(approver.getAppTypes(), application.getAppType())) {
                    continue;
                }
                notificationService.notifyOnce(approverEmployeeId, "approval_todo", "待审批提醒",
                        (applicantName != null ? applicantName : "同事") + "提交了" + label + "申请，请及时处理",
                        application.getId(), "/homePages/pending");
            }
        } catch (Exception e) {
            log.warn("提交申请通知审批人失败: applicationId={}", application != null ? application.getId() : null, e);
        }
    }

    /**
     * 申请类型展示名
     */
    public static String appTypeLabel(String appType) {
        if (appType == null) {
            return "";
        }
        return switch (appType) {
            case "leave" -> "请假";
            case "overtime" -> "加班";
            case "business" -> "出差";
            case "makeup" -> "补卡";
            case "exchange" -> "换休";
            case "regularization" -> "转正";
            case "transfer" -> "调动";
            case "reward" -> "奖励";
            case "punish" -> "惩罚";
            case "resignation" -> "离职";
            case "cost" -> "费用报销";
            case "device" -> "设备申请";
            default -> "";
        };
    }

    /**
     * 审批人类型匹配：app_types 为空表示可审全部类型
     */
    private boolean appTypesMatch(String appTypes, String appType) {
        if (appTypes == null || appTypes.isBlank() || appType == null) {
            return true;
        }
        return java.util.Arrays.stream(appTypes.split(","))
                .map(String::trim)
                .anyMatch(t -> t.equalsIgnoreCase(appType));
    }

    private boolean isMobileApprover(Long employeeId) {
        if (employeeId == null) {
            return false;
        }
        Long count = mobileApproverMapper.selectCount(new LambdaQueryWrapper<HrMobileApprover>()
                .eq(HrMobileApprover::getEmployeeId, employeeId));
        return count != null && count > 0;
    }

    /**
     * 移动端审批人校验（含申请类型）：app_types 为空表示可审全部类型，否则需包含当前类型
     */
    private boolean isMobileApproverForType(Long employeeId, String appType) {
        if (employeeId == null) {
            return false;
        }
        HrMobileApprover approver = mobileApproverMapper.selectOne(new LambdaQueryWrapper<HrMobileApprover>()
                .eq(HrMobileApprover::getEmployeeId, employeeId)
                .last("LIMIT 1"));
        if (approver == null) {
            return false;
        }
        // 与待办列表的 FIND_IN_SET 过滤口径一致
        return appTypesMatch(approver.getAppTypes(), appType);
    }

    private String resolveEmployeeName(Long employeeId) {
        if (employeeId == null) {
            return null;
        }
        HrEmployee employee = employeeMapper.selectById(employeeId);
        return employee != null ? employee.getName() : null;
    }

    /**
     * 审批最终通过后的业务联动（转正/调动/换休/离职等）
     */
    private void applyApprovedEffects(HrApplication application) {
        String appType = application.getAppType();

        if ("regularization".equals(appType)) {
            // 转正：更新员工转正日期和员工类别
            HrEmployee employee = new HrEmployee();
            employee.setId(application.getEmployeeId());
            employee.setRegularDate(application.getRegularDate());
            if (application.getNewEmployeeType() != null && !application.getNewEmployeeType().isEmpty()) {
                employee.setEmployeeType(application.getNewEmployeeType());
            }
            employeeMapper.updateById(employee);
        } else if ("transfer".equals(appType)) {
            // 调动：更新员工部门、职位（公司通过dept_id向上查找获取）
            HrEmployee employee = new HrEmployee();
            employee.setId(application.getEmployeeId());
            if (application.getToDeptId() != null) {
                employee.setDeptId(application.getToDeptId());
            }
            if (application.getToPosition() != null && !application.getToPosition().isEmpty()) {
                employee.setPosition(application.getToPosition());
            }
            employeeMapper.updateById(employee);
        } else if ("exchange".equals(appType)) {
            // 换休：交换两天的排班
            LocalDate originalDay = application.getStartTime().toLocalDate();
            LocalDate swapDay = application.getEndTime().toLocalDate();
            Long empId = application.getEmployeeId();

            AttSchedule originalSchedule = scheduleMapper.selectOne(
                    new LambdaQueryWrapper<AttSchedule>()
                            .eq(AttSchedule::getEmployeeId, empId)
                            .eq(AttSchedule::getScheduleDate, originalDay));
            AttSchedule swapSchedule = scheduleMapper.selectOne(
                    new LambdaQueryWrapper<AttSchedule>()
                            .eq(AttSchedule::getEmployeeId, empId)
                            .eq(AttSchedule::getScheduleDate, swapDay));

            Long originalShiftId = originalSchedule != null ? originalSchedule.getShiftId() : null;
            Long swapShiftId = swapSchedule != null ? swapSchedule.getShiftId() : null;

            if (originalSchedule != null && swapSchedule != null) {
                scheduleMapper.update(null, new LambdaUpdateWrapper<AttSchedule>()
                        .eq(AttSchedule::getId, originalSchedule.getId())
                        .set(AttSchedule::getShiftId, swapShiftId));
                scheduleMapper.update(null, new LambdaUpdateWrapper<AttSchedule>()
                        .eq(AttSchedule::getId, swapSchedule.getId())
                        .set(AttSchedule::getShiftId, originalShiftId));
            } else if (originalSchedule != null) {
                scheduleMapper.update(null, new LambdaUpdateWrapper<AttSchedule>()
                        .eq(AttSchedule::getId, originalSchedule.getId())
                        .set(AttSchedule::getShiftId, null));
                AttSchedule newSchedule = new AttSchedule();
                newSchedule.setEmployeeId(empId);
                newSchedule.setScheduleDate(swapDay);
                newSchedule.setShiftId(originalShiftId);
                scheduleMapper.insert(newSchedule);
            } else if (swapSchedule != null) {
                scheduleMapper.update(null, new LambdaUpdateWrapper<AttSchedule>()
                        .eq(AttSchedule::getId, swapSchedule.getId())
                        .set(AttSchedule::getShiftId, null));
                AttSchedule newSchedule = new AttSchedule();
                newSchedule.setEmployeeId(empId);
                newSchedule.setScheduleDate(originalDay);
                newSchedule.setShiftId(swapShiftId);
                scheduleMapper.insert(newSchedule);
            }
        } else if ("resignation".equals(appType)) {
            // 离职：更新员工状态为离职，并吊销其移动端会话
            HrEmployee employee = new HrEmployee();
            employee.setId(application.getEmployeeId());
            employee.setStatus(2); // 2-离职
            employee.setLeaveDate(application.getLastWorkDate());
            employeeMapper.updateById(employee);
            eventPublisher.publishEvent(new UserSessionEvictEvent(application.getEmployeeId(), true));
        } else if ("makeup".equals(appType)) {
            // 补卡：补写打卡记录（方式=手动补卡），并重算当日考勤
            applyMakeupApproval(application);
        } else if ("leave".equals(appType)) {
            // 请假：扣减假期额度（仅该员工该假期类型配置了额度时生效）
            applyLeaveQuotaDeduction(application);
        } else if ("overtime".equals(appType)) {
            // 加班：重算受影响日期的考勤，加班工时在核算时按已审批加班单回写
            if (application.getStartTime() != null && application.getEndTime() != null) {
                eventPublisher.publishEvent(new AttendanceRecalcEvent(application.getEmployeeId(),
                        application.getStartTime().toLocalDate(), application.getEndTime().toLocalDate()));
            }
        }
        // 奖励和惩罚暂不需要更新员工信息
    }

    /**
     * 补卡审批通过：按申请单补写缺失的打卡记录（已有同类型打卡时保留原记录），并触发当日考勤重算
     */
    private void applyMakeupApproval(HrApplication application) {
        LocalDateTime makeupTime = application.getStartTime();
        if (application.getEmployeeId() == null || makeupTime == null) {
            return;
        }
        LocalDate makeupDate = makeupTime.toLocalDate();
        int clockType = resolveMakeupClockType(application);

        Long existing = clockRecordMapper.selectCount(new LambdaQueryWrapper<AttClockRecord>()
                .eq(AttClockRecord::getEmployeeId, application.getEmployeeId())
                .eq(AttClockRecord::getClockDate, makeupDate)
                .eq(AttClockRecord::getClockType, clockType));
        if (existing != null && existing > 0) {
            return; // 已有真实打卡，不覆盖
        }

        AttClockRecord record = new AttClockRecord();
        record.setEmployeeId(application.getEmployeeId());
        record.setClockDate(makeupDate);
        record.setClockTime(makeupTime);
        record.setClockType(clockType);
        record.setClockMethod(3); // 3-手动补卡
        record.setRemark("补卡申请单 #" + application.getId() + " 审批通过自动补写");
        record.setCreateTime(LocalDateTime.now());
        clockRecordMapper.insert(record);

        eventPublisher.publishEvent(new AttendanceRecalcEvent(application.getEmployeeId(), makeupDate, makeupDate));
    }

    /**
     * 推断补卡类型：优先按申请类别/标题中的"上班/下班"字样；
     * 否则按当日已有打卡推断缺失的卡（只有上班卡→补下班，只有下班卡→补上班）；
     * 都无法判定时按时间上午/下午划分
     */
    private int resolveMakeupClockType(HrApplication application) {
        String marker = (application.getCategory() == null ? "" : application.getCategory())
                + (application.getTitle() == null ? "" : application.getTitle());
        if (marker.contains("下班")) {
            return 2;
        }
        if (marker.contains("上班")) {
            return 1;
        }
        LocalDate makeupDate = application.getStartTime().toLocalDate();
        boolean hasClockIn = countClockType(application.getEmployeeId(), makeupDate, 1) > 0;
        boolean hasClockOut = countClockType(application.getEmployeeId(), makeupDate, 2) > 0;
        if (hasClockIn && !hasClockOut) {
            return 2;
        }
        if (!hasClockIn && hasClockOut) {
            return 1;
        }
        return application.getStartTime().getHour() < 12 ? 1 : 2;
    }

    private long countClockType(Long employeeId, LocalDate date, int clockType) {
        Long count = clockRecordMapper.selectCount(new LambdaQueryWrapper<AttClockRecord>()
                .eq(AttClockRecord::getEmployeeId, employeeId)
                .eq(AttClockRecord::getClockDate, date)
                .eq(AttClockRecord::getClockType, clockType));
        return count != null ? count : 0;
    }

    /**
     * 请假审批通过后扣减假期额度：
     * 仅当该员工该假期类型（申请单 title 存字典值）配置了年度额度时校验并扣减，未配置额度不拦截；
     * 余额不足抛出异常使审批整体回滚，提示管理员先调整额度
     */
    private void applyLeaveQuotaDeduction(HrApplication application) {
        if (application.getEmployeeId() == null || application.getStartTime() == null) {
            return;
        }
        BigDecimal hours = application.getDuration();
        if (hours == null || hours.compareTo(BigDecimal.ZERO) <= 0) {
            hours = calculateLeaveHours(application.getEmployeeId(), application.getStartTime(),
                    application.getEndTime());
        }
        if (hours == null || hours.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }
        int year = application.getStartTime().toLocalDate().getYear();
        leaveQuotaService.deductForApprovedLeave(application.getEmployeeId(), year, application.getTitle(), hours,
                application.getId());
    }
}
