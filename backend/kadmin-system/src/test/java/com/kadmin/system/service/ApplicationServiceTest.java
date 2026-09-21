package com.kadmin.system.service;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.kadmin.attendance.domain.AttSchedule;
import com.kadmin.attendance.domain.AttShiftPeriod;
import com.kadmin.attendance.mapper.AttClockRecordMapper;
import com.kadmin.attendance.mapper.AttScheduleMapper;
import com.kadmin.attendance.mapper.AttShiftMapper;
import com.kadmin.attendance.mapper.AttShiftPeriodMapper;
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
import com.kadmin.system.domain.SysApprovalFlow;
import com.kadmin.system.domain.SysApprovalNode;
import com.kadmin.system.mapper.SysApprovalFlowMapper;
import com.kadmin.system.mapper.SysApprovalNodeMapper;
import com.kadmin.system.mapper.SysUserMapper;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 统一审批流单元测试：ApplicationService.approve 多级流转 / 并发防护 / 资格校验 / 通知与业务联动，
 * 以及 calculateLeaveHours / calculateOvertimeHours 工时计算。
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ApplicationServiceTest {

    private static final Long APP_ID = 100L;
    private static final Long APPLICANT_ID = 200L;
    private static final Long APPROVER_ID = 300L;

    @Mock
    private EmployeeMapper employeeMapper;
    @Mock
    private SysUserMapper sysUserMapper;
    @Mock
    private AttScheduleMapper scheduleMapper;
    @Mock
    private AttShiftMapper shiftMapper;
    @Mock
    private AttShiftPeriodMapper shiftPeriodMapper;
    @Mock
    private AttClockRecordMapper clockRecordMapper;
    @Mock
    private SysApprovalFlowMapper flowMapper;
    @Mock
    private SysApprovalNodeMapper flowNodeMapper;
    @Mock
    private HrApprovalRecordMapper approvalRecordMapper;
    @Mock
    private HrMobileApproverMapper mobileApproverMapper;
    @Mock
    private ApplicationEventPublisher eventPublisher;
    @Mock
    private NotificationService notificationService;
    @Mock
    private com.kadmin.hr.service.LeaveQuotaService leaveQuotaService;
    @Mock
    private HrApplicationMapper applicationMapper;

    private ApplicationService service;

    @BeforeAll
    static void initMybatisPlusLambdaCache() {
        // 纯单元测试无 MyBatis 启动流程，手动注册实体元数据，
        // 否则 LambdaQueryWrapper/LambdaUpdateWrapper 解析列名时缺少 lambda cache
        MapperBuilderAssistant assistant = new MapperBuilderAssistant(new MybatisConfiguration(), "");
        java.util.stream.Stream.of(HrApplication.class, HrApprovalRecord.class, SysApprovalFlow.class,
                SysApprovalNode.class, HrMobileApprover.class, AttSchedule.class, AttShiftPeriod.class)
                .forEach(clazz -> TableInfoHelper.initTableInfo(assistant, clazz));
    }

    @BeforeEach
    void setUp() {
        service = new ApplicationService(employeeMapper, sysUserMapper, scheduleMapper, shiftMapper,
                shiftPeriodMapper, clockRecordMapper, flowMapper, flowNodeMapper, approvalRecordMapper,
                mobileApproverMapper, eventPublisher, notificationService, leaveQuotaService);
        // ServiceImpl 基类的 baseMapper 字段注入 mock（getById/update/updateById 均走该 mapper）
        ReflectionTestUtils.setField(service, "baseMapper", applicationMapper);
    }

    // ==================== 构造测试数据 ====================

    private HrApplication pendingLeaveApplication() {
        HrApplication app = new HrApplication();
        app.setId(APP_ID);
        app.setEmployeeId(APPLICANT_ID);
        app.setAppType("leave");
        app.setStatus(0);
        return app;
    }

    private LoginUser login(Long userId, Long employeeId, String... roles) {
        return new LoginUser(userId, "user" + userId, null, null, null, employeeId,
                roles.length == 0 ? Set.of("ROLE_EMPLOYEE") : Set.of(roles), null);
    }

    /** 两级审批流：节点1(角色10) -> 节点2(角色11) */
    private List<SysApprovalNode> twoLevelNodes() {
        SysApprovalNode n1 = new SysApprovalNode();
        n1.setId(11L);
        n1.setFlowId(9L);
        n1.setNodeName("部门审批");
        n1.setRoleId(10L);
        n1.setSortOrder(1);
        SysApprovalNode n2 = new SysApprovalNode();
        n2.setId(12L);
        n2.setFlowId(9L);
        n2.setNodeName("人事审批");
        n2.setRoleId(11L);
        n2.setSortOrder(2);
        return List.of(n1, n2);
    }

    private void stubFlow(List<SysApprovalNode> nodes) {
        if (nodes.isEmpty()) {
            when(flowMapper.selectOne(any())).thenReturn(null);
            return;
        }
        SysApprovalFlow flow = new SysApprovalFlow();
        flow.setId(9L);
        flow.setFlowType("leave");
        flow.setStatus(1);
        when(flowMapper.selectOne(any())).thenReturn(flow);
        when(flowNodeMapper.selectList(any())).thenReturn(nodes);
    }

    private HrApprovalRecord passedRecord(Long nodeId, int sortOrder) {
        HrApprovalRecord record = new HrApprovalRecord();
        record.setApplicationId(APP_ID);
        record.setNodeId(nodeId);
        record.setStatus(1);
        record.setSortOrder(sortOrder);
        return record;
    }

    private AttShiftPeriod period(String start, String end) {
        AttShiftPeriod p = new AttShiftPeriod();
        // 批量预载按时段实体的 shiftId 建立映射，与排班使用的班次(5L)保持一致
        p.setShiftId(5L);
        p.setStartTime(start);
        p.setEndTime(end);
        return p;
    }

    private AttSchedule scheduleFor(LocalDate date, Long shiftId) {
        AttSchedule schedule = new AttSchedule();
        schedule.setEmployeeId(APPLICANT_ID);
        schedule.setScheduleDate(date);
        schedule.setShiftId(shiftId);
        return schedule;
    }

    // ==================== approve：基础校验 ====================

    @Test
    @DisplayName("审批：申请不存在时抛出异常")
    void approve_applicationNotFound() {
        when(applicationMapper.selectById(APP_ID)).thenReturn(null);
        assertThatThrownBy(() -> service.approve(APP_ID, 1, "ok", login(1L, APPROVER_ID), null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("申请不存在");
    }

    @Test
    @DisplayName("审批：非待审批状态（已通过）拒绝重复审批，防并发重放")
    void approve_rejectsAlreadyProcessedApplication() {
        HrApplication app = pendingLeaveApplication();
        app.setStatus(1);
        when(applicationMapper.selectById(APP_ID)).thenReturn(app);
        assertThatThrownBy(() -> service.approve(APP_ID, 1, "ok", login(1L, APPROVER_ID), null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("该申请已处理");
        verify(approvalRecordMapper, never()).insert(any(HrApprovalRecord.class));
    }

    @Test
    @DisplayName("审批：操作参数非通过/拒绝时视为无效操作")
    void approve_rejectsInvalidStatusArgument() {
        when(applicationMapper.selectById(APP_ID)).thenReturn(pendingLeaveApplication());
        assertThatThrownBy(() -> service.approve(APP_ID, 5, "ok", login(1L, APPROVER_ID), null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("无效的审批操作");
        assertThatThrownBy(() -> service.approve(APP_ID, null, "ok", login(1L, APPROVER_ID), null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    // ==================== approve：审批资格 ====================

    @Test
    @DisplayName("审批：不能审批自己提交的申请")
    void approve_cannotApproveOwnApplication() {
        stubFlow(List.of());
        when(applicationMapper.selectById(APP_ID)).thenReturn(pendingLeaveApplication());
        // 审批人 employeeId 与申请人相同
        assertThatThrownBy(() -> service.approve(APP_ID, 1, "ok", login(1L, APPLICANT_ID), null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("不能审批自己提交的申请");
        verify(approvalRecordMapper, never()).insert(any(HrApprovalRecord.class));
    }

    @Test
    @DisplayName("审批：既非当前节点角色也非移动端审批人时拒绝（无审批流场景）")
    void approve_rejectsUserWithoutApprovalRole() {
        stubFlow(List.of());
        when(applicationMapper.selectById(APP_ID)).thenReturn(pendingLeaveApplication());
        when(mobileApproverMapper.selectOne(any())).thenReturn(null);
        assertThatThrownBy(() -> service.approve(APP_ID, 1, "ok", login(1L, APPROVER_ID), null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("您没有审批该申请的权限");
    }

    @Test
    @DisplayName("审批：PC端审批人角色与当前节点角色不匹配时拒绝")
    void approve_rejectsMismatchedNodeRole() {
        stubFlow(twoLevelNodes());
        when(applicationMapper.selectById(APP_ID)).thenReturn(pendingLeaveApplication());
        // 第一轮 findCurrentNode：无通过记录，当前节点为节点1（角色10），审批人只有角色99
        when(approvalRecordMapper.selectList(any())).thenReturn(List.of());
        assertThatThrownBy(
                () -> service.approve(APP_ID, 1, "ok", login(1L, APPROVER_ID), List.of(99L)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("您没有审批该申请的权限");
    }

    @Test
    @DisplayName("审批：移动端审批人 app_types 不包含当前类型时拒绝")
    void approve_rejectsMobileApproverWithTypeMismatch() {
        stubFlow(List.of());
        when(applicationMapper.selectById(APP_ID)).thenReturn(pendingLeaveApplication());
        HrMobileApprover approver = new HrMobileApprover();
        approver.setEmployeeId(APPROVER_ID);
        approver.setAppTypes("overtime,business");
        when(mobileApproverMapper.selectOne(any())).thenReturn(approver);
        assertThatThrownBy(() -> service.approve(APP_ID, 1, "ok", login(1L, APPROVER_ID), null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("您没有审批该申请的权限");
    }

    @Test
    @DisplayName("审批：管理员跳过资格校验可直接审批他人申请")
    void approve_adminBypassesEligibilityCheck() {
        stubFlow(List.of());
        when(applicationMapper.selectById(APP_ID)).thenReturn(pendingLeaveApplication());
        when(approvalRecordMapper.selectList(any()))
                .thenReturn(List.of())          // 第一次 findCurrentNode：无通过记录
                .thenReturn(List.of());         // 第二次 findCurrentNode：仍无节点
        when(applicationMapper.update(isNull(), any())).thenReturn(1);
        // 管理员审批他人申请：不需要节点角色/移动端审批人资格
        boolean result = service.approve(APP_ID, 1, "同意", login(1L, APPROVER_ID, "ROLE_ADMIN"), null);
        assertThat(result).isTrue();
        verify(approvalRecordMapper).insert(any(HrApprovalRecord.class));
        verify(applicationMapper).update(isNull(), any());
        verify(notificationService).notify(eq(APPLICANT_ID), eq("approval_result"), eq("审批通过"),
                any(), eq(APP_ID), eq("/homePages/application"));
    }

    @Test
    @DisplayName("审批：管理员也不能审批自己提交的申请（回避不因角色豁免）")
    void approve_adminCannotApproveOwnApplication() {
        stubFlow(List.of());
        when(applicationMapper.selectById(APP_ID)).thenReturn(pendingLeaveApplication());
        assertThatThrownBy(() -> service.approve(APP_ID, 1, "同意", login(1L, APPLICANT_ID, "ROLE_ADMIN"), null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("不能审批自己提交的申请");
        verify(approvalRecordMapper, never()).insert(any(HrApprovalRecord.class));
        verify(applicationMapper, never()).update(any(), any());
    }

    // ==================== approve：多级流转 ====================

    @Test
    @DisplayName("审批：多级节点按顺序流转，中间节点通过后申请仍保持待审批且不发通知")
    void approve_intermediateNodePassKeepsPendingStatus() {
        stubFlow(twoLevelNodes());
        when(applicationMapper.selectById(APP_ID)).thenReturn(pendingLeaveApplication());
        // 第一次 findCurrentNode：无通过记录 -> 当前节点1；第二次：节点1已通过 -> 当前节点2
        when(approvalRecordMapper.selectList(any()))
                .thenReturn(List.of())
                .thenReturn(List.of(passedRecord(11L, 1)));
        when(applicationMapper.updateById(any(HrApplication.class))).thenReturn(1);

        boolean result = service.approve(APP_ID, 1, "同意", login(1L, APPROVER_ID), List.of(10L));

        assertThat(result).isTrue();
        // 记录了节点1的审批动作
        ArgumentCaptor<HrApprovalRecord> recordCaptor = ArgumentCaptor.forClass(HrApprovalRecord.class);
        verify(approvalRecordMapper).insert(recordCaptor.capture());
        assertThat(recordCaptor.getValue().getNodeId()).isEqualTo(11L);
        assertThat(recordCaptor.getValue().getNodeName()).isEqualTo("部门审批");
        assertThat(recordCaptor.getValue().getStatus()).isEqualTo(1);
        // 仅更新审批痕迹，未做终态流转（update(id,status) 不应被调用）
        ArgumentCaptor<HrApplication> entityCaptor = ArgumentCaptor.forClass(HrApplication.class);
        verify(applicationMapper).updateById(entityCaptor.capture());
        assertThat(entityCaptor.getValue().getApproveRemark()).isEqualTo("同意");
        verify(applicationMapper, never()).update(any(), any());
        // 中间节点不发结果通知
        verify(notificationService, never()).notify(anyLong(), any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("审批：最后一个节点通过才算整体通过并触发通知")
    void approve_finalNodePassCompletesApplication() {
        stubFlow(twoLevelNodes());
        when(applicationMapper.selectById(APP_ID)).thenReturn(pendingLeaveApplication());
        // 第一次 findCurrentNode：节点1已通过 -> 当前节点2；第二次：节点1、2均通过 -> 无待办节点
        when(approvalRecordMapper.selectList(any()))
                .thenReturn(List.of(passedRecord(11L, 1)))
                .thenReturn(List.of(passedRecord(11L, 1), passedRecord(12L, 2)));
        when(applicationMapper.update(isNull(), any())).thenReturn(1);

        boolean result = service.approve(APP_ID, 1, "同意", login(1L, APPROVER_ID), List.of(11L));

        assertThat(result).isTrue();
        verify(applicationMapper).update(isNull(), any());
        verify(notificationService).notify(eq(APPLICANT_ID), eq("approval_result"), eq("审批通过"),
                any(), eq(APP_ID), eq("/homePages/application"));
    }

    // ==================== approve：拒绝 ====================

    @Test
    @DisplayName("审批：任一节点拒绝即整体拒绝并发送驳回通知")
    void approve_anyRejectTerminatesApplication() {
        stubFlow(twoLevelNodes());
        when(applicationMapper.selectById(APP_ID)).thenReturn(pendingLeaveApplication());
        when(approvalRecordMapper.selectList(any())).thenReturn(List.of());
        when(applicationMapper.update(isNull(), any())).thenReturn(1);

        boolean result = service.approve(APP_ID, 2, "事由不符", login(1L, APPROVER_ID), List.of(10L));

        assertThat(result).isTrue();
        ArgumentCaptor<HrApprovalRecord> recordCaptor = ArgumentCaptor.forClass(HrApprovalRecord.class);
        verify(approvalRecordMapper).insert(recordCaptor.capture());
        assertThat(recordCaptor.getValue().getStatus()).isEqualTo(2);
        verify(applicationMapper).update(isNull(), any());
        verify(notificationService).notify(eq(APPLICANT_ID), eq("approval_result"), eq("审批驳回"),
                eq("你的请假申请已被驳回：事由不符"), eq(APP_ID), eq("/homePages/application"));
    }

    @Test
    @DisplayName("审批：拒绝时条件更新失败（已被他人处理）则抛异常且不发通知")
    void approve_rejectConcurrencyFailureThrows() {
        stubFlow(twoLevelNodes());
        when(applicationMapper.selectById(APP_ID)).thenReturn(pendingLeaveApplication());
        when(approvalRecordMapper.selectList(any())).thenReturn(List.of());
        when(applicationMapper.update(isNull(), any())).thenReturn(0);

        assertThatThrownBy(() -> service.approve(APP_ID, 2, "拒绝", login(1L, APPROVER_ID), List.of(10L)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("该申请已被处理");
        verify(notificationService, never()).notify(anyLong(), any(), any(), any(), any(), any());
    }

    // ==================== approve：并发防护 ====================

    @Test
    @DisplayName("审批：节点记录唯一键冲突（他人已并发审批）转为友好提示")
    void approve_duplicateRecordInsertBecomesFriendlyError() {
        stubFlow(List.of());
        when(applicationMapper.selectById(APP_ID)).thenReturn(pendingLeaveApplication());
        when(approvalRecordMapper.selectList(any())).thenReturn(List.of());
        HrMobileApprover approver = new HrMobileApprover();
        approver.setEmployeeId(APPROVER_ID);
        approver.setAppTypes(null); // 全类型可审
        when(mobileApproverMapper.selectOne(any())).thenReturn(approver);
        when(approvalRecordMapper.insert(any(HrApprovalRecord.class)))
                .thenThrow(new DuplicateKeyException("uk duplicate"));

        assertThatThrownBy(() -> service.approve(APP_ID, 1, "ok", login(1L, APPROVER_ID), null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("该申请刚被他人审批");
    }

    // ==================== approve：无审批流场景 ====================

    @Test
    @DisplayName("审批：未配置审批流时移动端指定审批人（app_types 为空表示全类型）可批并通过")
    void approve_mobileApproverWithoutFlowCanApprove() {
        stubFlow(List.of());
        when(applicationMapper.selectById(APP_ID)).thenReturn(pendingLeaveApplication());
        HrMobileApprover approver = new HrMobileApprover();
        approver.setEmployeeId(APPROVER_ID);
        approver.setAppTypes(null);
        when(mobileApproverMapper.selectOne(any())).thenReturn(approver);
        when(approvalRecordMapper.selectList(any())).thenReturn(List.of());
        when(applicationMapper.update(isNull(), any())).thenReturn(1);

        boolean result = service.approve(APP_ID, 1, "ok", login(1L, APPROVER_ID), null);

        assertThat(result).isTrue();
        ArgumentCaptor<HrApprovalRecord> recordCaptor = ArgumentCaptor.forClass(HrApprovalRecord.class);
        verify(approvalRecordMapper).insert(recordCaptor.capture());
        // 无流程节点时使用默认节点名"审批"与顺序1
        assertThat(recordCaptor.getValue().getNodeName()).isEqualTo("审批");
        assertThat(recordCaptor.getValue().getSortOrder()).isEqualTo(1);
        verify(notificationService).notify(eq(APPLICANT_ID), eq("approval_result"), eq("审批通过"),
                any(), eq(APP_ID), any());
    }

    // ==================== approve：通过后的业务联动 ====================

    @Test
    @DisplayName("审批：转正申请通过后联动更新员工转正日期与员工类别")
    void approve_regularizationApplicationUpdatesEmployee() {
        HrApplication app = pendingLeaveApplication();
        app.setAppType("regularization");
        app.setRegularDate(LocalDate.of(2026, 10, 1));
        app.setNewEmployeeType("正式员工");
        when(applicationMapper.selectById(APP_ID)).thenReturn(app);
        stubFlow(List.of());
        when(approvalRecordMapper.selectList(any())).thenReturn(List.of());
        when(applicationMapper.update(isNull(), any())).thenReturn(1);

        boolean result = service.approve(APP_ID, 1, "同意", login(1L, APPROVER_ID, "ROLE_ADMIN"), null);

        assertThat(result).isTrue();
        ArgumentCaptor<HrEmployee> employeeCaptor = ArgumentCaptor.forClass(HrEmployee.class);
        verify(employeeMapper).updateById(employeeCaptor.capture());
        assertThat(employeeCaptor.getValue().getId()).isEqualTo(APPLICANT_ID);
        assertThat(employeeCaptor.getValue().getRegularDate()).isEqualTo(LocalDate.of(2026, 10, 1));
        assertThat(employeeCaptor.getValue().getEmployeeType()).isEqualTo("正式员工");
    }

    @Test
    @DisplayName("审批：离职申请通过后联动更新员工状态并吊销移动端会话")
    void approve_resignationApplicationUpdatesEmployeeAndEvictsSession() {
        HrApplication app = pendingLeaveApplication();
        app.setAppType("resignation");
        app.setLastWorkDate(LocalDate.of(2026, 9, 30));
        when(applicationMapper.selectById(APP_ID)).thenReturn(app);
        stubFlow(List.of());
        when(approvalRecordMapper.selectList(any())).thenReturn(List.of());
        when(applicationMapper.update(isNull(), any())).thenReturn(1);

        boolean result = service.approve(APP_ID, 1, "同意", login(1L, APPROVER_ID, "ROLE_ADMIN"), null);

        assertThat(result).isTrue();
        ArgumentCaptor<HrEmployee> employeeCaptor = ArgumentCaptor.forClass(HrEmployee.class);
        verify(employeeMapper).updateById(employeeCaptor.capture());
        assertThat(employeeCaptor.getValue().getStatus()).isEqualTo(2);
        assertThat(employeeCaptor.getValue().getLeaveDate()).isEqualTo(LocalDate.of(2026, 9, 30));
        ArgumentCaptor<Object> eventCaptor = ArgumentCaptor.forClass(Object.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());
        assertThat(eventCaptor.getValue()).isInstanceOf(UserSessionEvictEvent.class);
        assertThat(((UserSessionEvictEvent) eventCaptor.getValue()).getUserId()).isEqualTo(APPLICANT_ID);
        assertThat(((UserSessionEvictEvent) eventCaptor.getValue()).isEmployee()).isTrue();
    }

    @Test
    @DisplayName("审批：换休申请通过后联动交换两天的排班")
    void approve_exchangeApplicationSwapsSchedules() {
        HrApplication app = pendingLeaveApplication();
        app.setAppType("exchange");
        app.setStartTime(LocalDateTime.of(2026, 9, 21, 0, 0));
        app.setEndTime(LocalDateTime.of(2026, 9, 25, 0, 0));
        when(applicationMapper.selectById(APP_ID)).thenReturn(app);
        stubFlow(List.of());
        when(approvalRecordMapper.selectList(any())).thenReturn(List.of());
        when(applicationMapper.update(isNull(), any())).thenReturn(1);

        AttSchedule original = scheduleFor(LocalDate.of(2026, 9, 21), 5L);
        original.setId(101L);
        AttSchedule swap = scheduleFor(LocalDate.of(2026, 9, 25), 7L);
        swap.setId(102L);
        when(scheduleMapper.selectOne(any())).thenReturn(original, swap);

        boolean result = service.approve(APP_ID, 1, "同意", login(1L, APPROVER_ID, "ROLE_ADMIN"), null);

        assertThat(result).isTrue();
        // 两个排班都存在：两次交换更新（原日换入 swap 的班次、互换日换入 original 的班次）
        verify(scheduleMapper, times(2)).update(isNull(), any());
    }

    // ==================== calculateOvertimeHours ====================

    @Test
    @DisplayName("加班计算：参数缺失返回0且不查库")
    void overtimeCalculation_nullArgumentsReturnsZero() {
        assertThat(service.calculateOvertimeHours(null, LocalDateTime.now(), LocalDateTime.now()))
                .isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(service.calculateOvertimeHours(1L, null, LocalDateTime.now()))
                .isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(service.calculateOvertimeHours(1L, LocalDateTime.now(), null))
                .isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("加班计算：时间跨度超过92天直接抛出异常")
    void overtimeCalculation_rejectsRangeBeyond92Days() {
        assertThatThrownBy(() -> service.calculateOvertimeHours(1L,
                LocalDateTime.of(2026, 1, 1, 0, 0), LocalDateTime.of(2026, 5, 1, 0, 0)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("92天");
    }

    @Test
    @DisplayName("加班计算：休息日（无排班）整段计入加班")
    void overtimeCalculation_restDayCountsFullDuration() {
        LocalDate restDay = LocalDate.of(2026, 9, 19); // 周六
        when(scheduleMapper.selectList(any())).thenReturn(List.of());

        BigDecimal hours = service.calculateOvertimeHours(APPLICANT_ID,
                LocalDateTime.of(restDay, LocalTime.of(9, 0)), LocalDateTime.of(restDay, LocalTime.of(18, 0)));

        assertThat(hours).isEqualByComparingTo(new BigDecimal("9.00"));
    }

    @Test
    @DisplayName("加班计算：有排班时只计与班次时段的交集（自动扣除午休）")
    void overtimeCalculation_shiftDayCountsOnlyPeriodOverlap() {
        LocalDate workDay = LocalDate.of(2026, 9, 21);
        when(scheduleMapper.selectList(any())).thenReturn(List.of(scheduleFor(workDay, 5L)));
        when(shiftPeriodMapper.selectList(any())).thenReturn(List.of(
                period("09:00", "12:00"), period("13:00", "18:00")));

        // 加班 10:00-20:00：与上午段交集 10:00-12:00=2h，与下午段交集 13:00-18:00=5h
        BigDecimal hours = service.calculateOvertimeHours(APPLICANT_ID,
                LocalDateTime.of(workDay, LocalTime.of(10, 0)), LocalDateTime.of(workDay, LocalTime.of(20, 0)));

        assertThat(hours).isEqualByComparingTo(new BigDecimal("7.00"));
    }

    @Test
    @DisplayName("加班计算：有排班但未配置时段时整段计入加班")
    void overtimeCalculation_shiftWithoutPeriodsCountsFullDuration() {
        LocalDate workDay = LocalDate.of(2026, 9, 21);
        when(scheduleMapper.selectList(any())).thenReturn(List.of(scheduleFor(workDay, 5L)));
        when(shiftPeriodMapper.selectList(any())).thenReturn(List.of());

        BigDecimal hours = service.calculateOvertimeHours(APPLICANT_ID,
                LocalDateTime.of(workDay, LocalTime.of(18, 0)), LocalDateTime.of(workDay, LocalTime.of(20, 0)));

        assertThat(hours).isEqualByComparingTo(new BigDecimal("2.00"));
    }

    @Test
    @DisplayName("加班计算：跨天加班按天切分计算（首日到23:59、次日从0点起）")
    void overtimeCalculation_crossDaySplitsByDay() {
        // 两天均无排班
        when(scheduleMapper.selectList(any())).thenReturn(List.of());

        BigDecimal hours = service.calculateOvertimeHours(APPLICANT_ID,
                LocalDateTime.of(2026, 9, 19, 22, 0), LocalDateTime.of(2026, 9, 20, 2, 0));

        // 第一天 22:00-23:59 = 119 分钟 = 1.98h，第二天 00:00-02:00 = 2h
        assertThat(hours).isEqualByComparingTo(new BigDecimal("3.98"));
    }

    @Test
    @DisplayName("加班计算：分钟数按半小时上取整为小时（91分钟=1.52h）")
    void overtimeCalculation_minutesRoundedHalfUp() {
        LocalDate restDay = LocalDate.of(2026, 9, 19);
        when(scheduleMapper.selectList(any())).thenReturn(List.of());

        BigDecimal hours = service.calculateOvertimeHours(APPLICANT_ID,
                LocalDateTime.of(restDay, LocalTime.of(9, 0)), LocalDateTime.of(restDay, LocalTime.of(10, 31)));

        assertThat(hours).isEqualByComparingTo(new BigDecimal("1.52"));
    }

    // ==================== calculateLeaveHours ====================

    @Test
    @DisplayName("请假计算：休息日（无排班）不计请假工时")
    void leaveCalculation_restDayCountsZero() {
        LocalDate restDay = LocalDate.of(2026, 9, 19);
        when(scheduleMapper.selectList(any())).thenReturn(List.of());

        BigDecimal hours = service.calculateLeaveHours(APPLICANT_ID,
                LocalDateTime.of(restDay, LocalTime.of(9, 0)), LocalDateTime.of(restDay, LocalTime.of(18, 0)));

        assertThat(hours).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("请假计算：按班次时段交集计算（跨午休请假自动扣除午休）")
    void leaveCalculation_countsOnlyPeriodOverlap() {
        LocalDate workDay = LocalDate.of(2026, 9, 21);
        when(scheduleMapper.selectList(any())).thenReturn(List.of(scheduleFor(workDay, 5L)));
        when(shiftPeriodMapper.selectList(any())).thenReturn(List.of(
                period("09:00", "12:00"), period("13:00", "18:00")));

        // 请假 10:00-14:00：上午 10:00-12:00=2h，下午 13:00-14:00=1h
        BigDecimal hours = service.calculateLeaveHours(APPLICANT_ID,
                LocalDateTime.of(workDay, LocalTime.of(10, 0)), LocalDateTime.of(workDay, LocalTime.of(14, 0)));

        assertThat(hours).isEqualByComparingTo(new BigDecimal("3.00"));
    }

    @Test
    @DisplayName("请假计算：跨天时首日从请假时间起、非末日计至当日最后时段结束")
    void leaveCalculation_crossDayClampsToShiftPeriods() {
        // 两天同一班次
        when(scheduleMapper.selectList(any())).thenReturn(List.of(
                scheduleFor(LocalDate.of(2026, 9, 21), 5L),
                scheduleFor(LocalDate.of(2026, 9, 22), 5L)));
        when(shiftPeriodMapper.selectList(any())).thenReturn(List.of(
                period("09:00", "12:00"), period("13:00", "18:00")));

        // 第一天 16:00 起（下午段 16:00-18:00=2h），第二天至 11:00 止（上午段 09:00-11:00=2h）
        BigDecimal hours = service.calculateLeaveHours(APPLICANT_ID,
                LocalDateTime.of(2026, 9, 21, 16, 0), LocalDateTime.of(2026, 9, 22, 11, 0));

        assertThat(hours).isEqualByComparingTo(new BigDecimal("4.00"));
    }

    @Test
    @DisplayName("请假计算：参数缺失返回0")
    void leaveCalculation_nullArgumentsReturnsZero() {
        assertThat(service.calculateLeaveHours(null, LocalDateTime.now(), LocalDateTime.now()))
                .isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(service.calculateLeaveHours(1L, null, LocalDateTime.now()))
                .isEqualByComparingTo(BigDecimal.ZERO);
    }
}
