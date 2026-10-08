package com.kadmin.salary.service;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.kadmin.attendance.service.AttendanceService;
import com.kadmin.hr.mapper.EmployeeMapper;
import com.kadmin.hr.mapper.HrApplicationMapper;
import com.kadmin.organization.mapper.OrgUnitMapper;
import com.kadmin.salary.domain.SalPayrollBatch;
import com.kadmin.salary.domain.SalPayrollPayslip;
import com.kadmin.salary.mapper.SalPayrollBatchMapper;
import com.kadmin.salary.mapper.SalPayrollItemMapper;
import com.kadmin.salary.mapper.SalPayrollPayslipMapper;
import com.kadmin.salary.mapper.SalSalaryArchiveItemMapper;
import com.kadmin.salary.mapper.SalSalaryArchiveMapper;
import com.kadmin.salary.mapper.SalSalaryItemDefMapper;
import com.kadmin.system.service.NotificationService;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.AfterEach;
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
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 工资批次状态机单元测试：0核算中→1已核算→2已确认→3已发放 的合法流转与非法流转拒绝，
 * 以及发放通知"事务提交后推送"（C20）的时序。
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PayrollServiceBatchTest {

    private static final Long BATCH_ID = 50L;

    @Mock
    private EmployeeMapper employeeMapper;
    @Mock
    private OrgUnitMapper orgUnitMapper;
    @Mock
    private HrApplicationMapper applicationMapper;
    @Mock
    private AttendanceService attendanceService;
    @Mock
    private SalSalaryArchiveMapper archiveMapper;
    @Mock
    private SalSalaryArchiveItemMapper archiveItemMapper;
    @Mock
    private SalSalaryItemDefMapper itemDefMapper;
    @Mock
    private SalarySchemeService schemeService;
    @Mock
    private SalPayrollPayslipMapper payslipMapper;
    @Mock
    private SalPayrollItemMapper payrollItemMapper;
    @Mock
    private NotificationService notificationService;
    @Mock
    private SalPayrollBatchMapper batchMapper;

    private PayrollService service;

    @BeforeAll
    static void initMybatisPlusLambdaCache() {
        MapperBuilderAssistant assistant = new MapperBuilderAssistant(new MybatisConfiguration(), "");
        java.util.stream.Stream.of(SalPayrollBatch.class, SalPayrollPayslip.class,
                com.kadmin.hr.domain.HrEmployee.class,
                com.kadmin.salary.domain.SalSalaryArchive.class)
                .forEach(clazz -> TableInfoHelper.initTableInfo(assistant, clazz));
    }

    @BeforeEach
    void setUp() {
        service = new PayrollService(employeeMapper, orgUnitMapper, applicationMapper, attendanceService,
                archiveMapper, archiveItemMapper, itemDefMapper, schemeService, payslipMapper,
                payrollItemMapper, notificationService);
        // ServiceImpl 基类的 baseMapper 字段注入 mock（getById/update/save 均走该 mapper）
        ReflectionTestUtils.setField(service, "baseMapper", batchMapper);
    }

    @AfterEach
    void clearTransactionSynchronization() {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    private SalPayrollBatch batch(int status) {
        SalPayrollBatch batch = new SalPayrollBatch();
        batch.setId(BATCH_ID);
        batch.setYearMonth("2026-09");
        batch.setCompanyId(5L);
        batch.setStatus(status);
        batch.setEmployeeCount(0);
        return batch;
    }

    private SalPayrollPayslip payslip(Long id, Long employeeId) {
        SalPayrollPayslip payslip = new SalPayrollPayslip();
        payslip.setId(id);
        payslip.setBatchId(BATCH_ID);
        payslip.setEmployeeId(employeeId);
        payslip.setNetPay(BigDecimal.valueOf(8000));
        return payslip;
    }

    @Test
    @DisplayName("确认批次：已核算(1)→已确认(2) 条件更新生效")
    void confirmBatch_fromComputed() {
        when(batchMapper.update(any(), any())).thenReturn(1);
        service.confirmBatch(BATCH_ID);
        verify(batchMapper).update(any(), any());
    }

    @Test
    @DisplayName("确认批次：核算中(0)不允许确认（条件更新不生效即拒绝）")
    void confirmBatch_fromComputing_rejected() {
        when(batchMapper.update(any(), any())).thenReturn(0);
        assertThatThrownBy(() -> service.confirmBatch(BATCH_ID))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("仅已核算状态的批次可以确认");
    }

    @Test
    @DisplayName("发放批次：已确认(2)→已发放(3)，通知在事务提交后发送（提交前不发送）")
    void publishBatch_notifiesAfterCommit() {
        when(batchMapper.selectById(BATCH_ID)).thenReturn(batch(PayrollService.STATUS_CONFIRMED));
        when(batchMapper.update(any(), any())).thenReturn(1);
        when(payslipMapper.selectList(any()))
                .thenReturn(List.of(payslip(1L, 100L), payslip(2L, 101L)));

        // 模拟事务上下文：注册 afterCommit 同步
        TransactionSynchronizationManager.initSynchronization();
        service.publishBatch(BATCH_ID);

        // 提交前：通知一条都不能发（避免发放回滚后员工已收到通知）
        verify(notificationService, never()).notify(any(), anyString(), anyString(), anyString(), any(), any());

        // 模拟事务提交：触发 afterCommit 后逐条发送
        for (TransactionSynchronization synchronization : TransactionSynchronizationManager.getSynchronizations()) {
            synchronization.afterCommit();
        }
        verify(notificationService, times(2)).notify(any(), eq("salary"), eq("工资条发放通知"),
                anyString(), any(), anyString());
    }

    @Test
    @DisplayName("发放批次：无事务上下文时直接发送通知（兜底）")
    void publishBatch_withoutTransaction_sendsDirectly() {
        when(batchMapper.selectById(BATCH_ID)).thenReturn(batch(PayrollService.STATUS_CONFIRMED));
        when(batchMapper.update(any(), any())).thenReturn(1);
        when(payslipMapper.selectList(any())).thenReturn(List.of(payslip(1L, 100L)));

        service.publishBatch(BATCH_ID);
        verify(notificationService, times(1)).notify(eq(100L), eq("salary"), eq("工资条发放通知"),
                anyString(), eq(1L), eq("/minePages/payslip"));
    }

    @Test
    @DisplayName("发放批次：已核算(1)不允许发放")
    void publishBatch_fromComputed_rejected() {
        when(batchMapper.selectById(BATCH_ID)).thenReturn(batch(PayrollService.STATUS_COMPUTED));
        when(batchMapper.update(any(), any())).thenReturn(0);
        assertThatThrownBy(() -> service.publishBatch(BATCH_ID))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("仅已确认状态的批次可以发放");
    }

    @Test
    @DisplayName("解锁批次：已发放(3)不允许解锁回已核算")
    void unlockBatch_fromPublished_rejected() {
        when(batchMapper.update(any(), any())).thenReturn(0);
        assertThatThrownBy(() -> service.unlockBatch(BATCH_ID))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("仅已确认状态的批次可以解锁");
    }

    @Test
    @DisplayName("重算批次：已确认(2)及之后状态不允许重算")
    void computeBatch_fromConfirmed_rejected() {
        when(batchMapper.selectById(BATCH_ID)).thenReturn(batch(PayrollService.STATUS_CONFIRMED));
        assertThatThrownBy(() -> service.computeBatch(BATCH_ID))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("批次已确认，不能重算");
        verify(batchMapper, never()).update(any(), any());
    }

    @Test
    @DisplayName("删除批次：已发放(3)不允许删除（账本留痕）")
    void deleteBatch_fromPublished_rejected() {
        when(batchMapper.selectById(BATCH_ID)).thenReturn(batch(PayrollService.STATUS_PUBLISHED));
        assertThatThrownBy(() -> service.deleteBatch(BATCH_ID))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("不能删除");
    }

    @Test
    @DisplayName("重算批次：已核算(1)重算成功，回写状态与人数汇总")
    void computeBatch_fromComputed_recomputes() {
        when(batchMapper.selectById(BATCH_ID)).thenReturn(batch(PayrollService.STATUS_COMPUTED));
        when(batchMapper.update(any(), any())).thenReturn(1);
        // 公司范围内无在职员工：核算结果为空批次
        when(orgUnitMapper.selectOrgAndChildIds(5L)).thenReturn(List.of(5L));
        when(employeeMapper.selectList(any())).thenReturn(List.of());
        lenient().when(payslipMapper.selectList(any())).thenReturn(List.of());

        service.computeBatch(BATCH_ID);

        // 第二次 update 为核算结果回填：状态置回已核算、人数0、合计0
        ArgumentCaptor<com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<SalPayrollBatch>>
                captor = ArgumentCaptor.forClass(
                        (Class) com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper.class);
        verify(batchMapper, times(2)).update(any(), captor.capture());
        String sql = captor.getAllValues().get(1).getSqlSet().toLowerCase();
        assertThat(sql).contains("status");
        assertThat(sql).contains("employee_count");
    }

    @Test
    @DisplayName("员工确认工资条：非本人或已确认时条件更新不生效即拒绝")
    void confirmMyPayslip_alreadyConfirmed_rejected() {
        when(payslipMapper.update(any(), any())).thenReturn(0);
        assertThatThrownBy(() -> service.confirmMyPayslip(100L, 1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("工资条不存在或已确认");
    }
}
