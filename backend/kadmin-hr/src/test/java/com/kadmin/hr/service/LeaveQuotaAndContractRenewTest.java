package com.kadmin.hr.service;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.kadmin.hr.domain.HrContract;
import com.kadmin.hr.domain.HrLeaveQuota;
import com.kadmin.hr.mapper.EmployeeMapper;
import com.kadmin.hr.mapper.HrContractMapper;
import com.kadmin.hr.mapper.HrLeaveQuotaMapper;
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
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 假期额度扣减与合同续签单元测试：
 * 扣减的"未配置放行/余额不足回滚"分支与续签的"次数递增/重复续签拦截"。
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class LeaveQuotaAndContractRenewTest {

    private static final Long EMPLOYEE_ID = 200L;

    @Mock
    private EmployeeMapper employeeMapper;
    @Mock
    private HrLeaveQuotaMapper leaveQuotaMapper;
    @Mock
    private HrContractMapper contractMapper;

    private LeaveQuotaService leaveQuotaService;
    private ContractService contractService;

    @BeforeAll
    static void initMybatisPlusLambdaCache() {
        MapperBuilderAssistant assistant = new MapperBuilderAssistant(new MybatisConfiguration(), "");
        java.util.stream.Stream.of(HrLeaveQuota.class, HrContract.class)
                .forEach(clazz -> TableInfoHelper.initTableInfo(assistant, clazz));
    }

    @BeforeEach
    void setUp() {
        leaveQuotaService = new LeaveQuotaService(employeeMapper);
        ReflectionTestUtils.setField(leaveQuotaService, "baseMapper", leaveQuotaMapper);
        contractService = new ContractService();
        ReflectionTestUtils.setField(contractService, "baseMapper", contractMapper);
    }

    private HrLeaveQuota quota(String type, String total, String used) {
        HrLeaveQuota quota = new HrLeaveQuota();
        quota.setId(1L);
        quota.setEmployeeId(EMPLOYEE_ID);
        quota.setYear(2026);
        quota.setLeaveType(type);
        quota.setTotalHours(new BigDecimal(total));
        quota.setUsedHours(new BigDecimal(used));
        return quota;
    }

    // ==================== 假期额度扣减 ====================

    @Test
    @DisplayName("扣减：未配置额度的员工/类型直接放行，不做任何更新")
    void deduct_withoutQuotaPassesThrough() {
        when(leaveQuotaMapper.selectOne(any(), anyBoolean())).thenReturn(null);
        leaveQuotaService.deductForApprovedLeave(EMPLOYEE_ID, 2026, "1", new BigDecimal("8"), 100L);
        verify(leaveQuotaMapper, org.mockito.Mockito.never()).update(isNull(), any());
    }

    @Test
    @DisplayName("扣减：余额充足时条件更新生效")
    void deduct_withSufficientBalanceUpdates() {
        when(leaveQuotaMapper.selectOne(any(), anyBoolean())).thenReturn(quota("1", "40", "32"));
        when(leaveQuotaMapper.update(isNull(), any())).thenReturn(1);

        leaveQuotaService.deductForApprovedLeave(EMPLOYEE_ID, 2026, "1", new BigDecimal("8"), 100L);

        verify(leaveQuotaMapper).update(isNull(), any());
    }

    @Test
    @DisplayName("扣减：余额不足抛出异常（审批事务回滚），提示剩余额度")
    void deduct_withInsufficientBalanceThrows() {
        when(leaveQuotaMapper.selectOne(any(), anyBoolean())).thenReturn(quota("1", "40", "36"));
        when(leaveQuotaMapper.update(isNull(), any())).thenReturn(0);

        assertThatThrownBy(() -> leaveQuotaService.deductForApprovedLeave(EMPLOYEE_ID, 2026, "1",
                new BigDecimal("8"), 100L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("额度不足")
                .hasMessageContaining("4");
        verify(leaveQuotaMapper).update(isNull(), any());
    }

    @Test
    @DisplayName("扣减：非法参数（空类型/非正时长）静默跳过")
    void deduct_skipsInvalidArguments() {
        leaveQuotaService.deductForApprovedLeave(EMPLOYEE_ID, 2026, null, new BigDecimal("8"), 100L);
        leaveQuotaService.deductForApprovedLeave(EMPLOYEE_ID, 2026, "1", BigDecimal.ZERO, 100L);
        verify(leaveQuotaMapper, org.mockito.Mockito.never()).selectOne(any());
    }

    // ==================== 合同续签 ====================

    @Test
    @DisplayName("续签：生成次数+1的新合同并将旧合同置为已续签")
    void renew_createsNewContractAndMarksOldRenewed() {
        HrContract old = new HrContract();
        old.setId(10L);
        old.setEmployeeId(EMPLOYEE_ID);
        old.setContractNo("HT2026-001");
        old.setContractType("1");
        old.setStartDate(LocalDate.of(2024, 1, 1));
        old.setEndDate(LocalDate.of(2026, 12, 31));
        old.setSalary(new BigDecimal("10000"));
        old.setContractCount(1);
        old.setStatus(1);
        when(contractMapper.selectById(10L)).thenReturn(old);
        when(contractMapper.update(isNull(), any())).thenReturn(1);

        HrContract input = new HrContract();
        input.setId(10L);
        input.setStartDate(LocalDate.of(2027, 1, 1));
        input.setEndDate(LocalDate.of(2029, 12, 31));

        HrContract renewed = contractService.renewContract(input);

        assertThat(renewed.getContractNo()).isEqualTo("HT2026-001-2");
        assertThat(renewed.getContractCount()).isEqualTo(2);
        assertThat(renewed.getStatus()).isEqualTo(1);
        assertThat(renewed.getStartDate()).isEqualTo(LocalDate.of(2027, 1, 1));
        assertThat(renewed.getSalary()).isEqualTo(new BigDecimal("10000"));
        ArgumentCaptor<HrContract> captor = ArgumentCaptor.forClass(HrContract.class);
        verify(contractMapper).insert(captor.capture());
        assertThat(captor.getValue().getEmployeeId()).isEqualTo(EMPLOYEE_ID);
        verify(contractMapper).update(isNull(), any());
    }

    @Test
    @DisplayName("续签：已续签的合同拒绝重复续签")
    void renew_rejectsAlreadyRenewedContract() {
        HrContract old = new HrContract();
        old.setId(10L);
        old.setStatus(4);
        when(contractMapper.selectById(10L)).thenReturn(old);

        HrContract input = new HrContract();
        input.setId(10L);
        assertThatThrownBy(() -> contractService.renewContract(input))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("已续签");
    }

    @Test
    @DisplayName("续签：结束日期早于开始日期拒绝")
    void renew_rejectsInvalidDateRange() {
        HrContract old = new HrContract();
        old.setId(10L);
        old.setStatus(1);
        when(contractMapper.selectById(10L)).thenReturn(old);

        HrContract input = new HrContract();
        input.setId(10L);
        input.setStartDate(LocalDate.of(2027, 1, 1));
        input.setEndDate(LocalDate.of(2026, 1, 1));
        assertThatThrownBy(() -> contractService.renewContract(input))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("结束日期");
    }

    @Test
    @DisplayName("续签：旧合同状态条件更新失败（他人已并发续签）抛异常")
    void renew_concurrentRenewalFailureThrows() {
        HrContract old = new HrContract();
        old.setId(10L);
        old.setContractNo("HT-1");
        old.setStatus(1);
        when(contractMapper.selectById(10L)).thenReturn(old);
        when(contractMapper.update(isNull(), any())).thenReturn(0);

        HrContract input = new HrContract();
        input.setId(10L);
        assertThatThrownBy(() -> contractService.renewContract(input))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("已被他人续签");
    }
}
