package com.kadmin.web.controller.mobile;

import com.kadmin.attendance.domain.AttClockRecord;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.kadmin.attendance.mapper.AttClockRecordMapper;
import com.kadmin.attendance.mapper.AttDailyRecordMapper;
import com.kadmin.attendance.mapper.AttScheduleMapper;
import com.kadmin.attendance.mapper.AttShiftMapper;
import com.kadmin.attendance.service.AttLocationService;
import com.kadmin.common.Result;
import com.kadmin.common.security.LoginUser;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 移动端打卡接口单元测试：MobileAttendanceController.clock 的类型校验、重复打卡拦截、
 * 并发唯一键冲突转友好提示、正常打卡落库。
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class MobileAttendanceControllerTest {

    @Mock
    private AttClockRecordMapper clockRecordMapper;
    @Mock
    private AttDailyRecordMapper dailyRecordMapper;
    @Mock
    private AttScheduleMapper scheduleMapper;
    @Mock
    private AttShiftMapper shiftMapper;
    @Mock
    private AttLocationService attLocationService;

    private MobileAttendanceController controller;

    @BeforeAll
    static void initMybatisPlusLambdaCache() {
        // 打卡记录的 LambdaQueryWrapper 构建需要实体元数据缓存
        MapperBuilderAssistant assistant = new MapperBuilderAssistant(new MybatisConfiguration(), "");
        TableInfoHelper.initTableInfo(assistant, AttClockRecord.class);
    }

    @BeforeEach
    void setUp() {
        controller = new MobileAttendanceController(clockRecordMapper, dailyRecordMapper, scheduleMapper,
                shiftMapper, attLocationService);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void login() {
        LoginUser loginUser = new LoginUser(1L, "user1", null, null, null, 77L, null, null);
        SecurityContextHolder.getContext()
                .setAuthentication(new UsernamePasswordAuthenticationToken(loginUser, "N/A"));
    }

    private Map<String, Object> clockParams(Object clockType) {
        Map<String, Object> params = new HashMap<>();
        params.put("clockType", clockType);
        return params;
    }

    @Test
    @DisplayName("打卡：未登录时返回请先登录")
    void clock_requiresLogin() {
        Result<AttClockRecord> result = controller.clock(clockParams(1));
        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getMsg()).contains("请先登录");
    }

    @Test
    @DisplayName("打卡：类型参数非法时返回打卡类型不正确")
    void clock_rejectsInvalidClockType() {
        login();
        Result<AttClockRecord> result = controller.clock(clockParams(5));
        assertThat(result.getMsg()).contains("打卡类型不正确");
        verify(clockRecordMapper, never()).insert(any(AttClockRecord.class));
    }

    @Test
    @DisplayName("打卡：当日已签到时拦截重复打卡")
    void clock_rejectsDuplicateClockIn() {
        login();
        when(clockRecordMapper.selectCount(any())).thenReturn(2L);

        Result<AttClockRecord> result = controller.clock(clockParams(1));

        assertThat(result.getMsg()).contains("今日已签到，请勿重复打卡");
        verify(clockRecordMapper, never()).insert(any(AttClockRecord.class));
    }

    @Test
    @DisplayName("打卡：并发场景下唯一键冲突转为友好提示")
    void clock_duplicateKeyBecomesFriendlyMessage() {
        login();
        // 两次防重检查都通过（无当日记录），插入时被唯一键拦截
        when(clockRecordMapper.selectCount(any())).thenReturn(0L);
        when(attLocationService.getAssignedActiveLocations(77L)).thenReturn(List.of());
        when(clockRecordMapper.insert(any(AttClockRecord.class)))
                .thenThrow(new DuplicateKeyException("uk_employee_date_type duplicate"));

        assertThatThrownBy(() -> controller.clock(clockParams(2)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("今日已打过该卡，请勿重复打卡");
    }

    @Test
    @DisplayName("打卡：正常打卡成功落库并携带员工/类型/备注")
    void clock_successPersistsRecord() {
        login();
        when(clockRecordMapper.selectCount(any())).thenReturn(0L);
        when(attLocationService.getAssignedActiveLocations(77L)).thenReturn(List.of());

        Result<AttClockRecord> result = controller.clock(clockParams(1));

        assertThat(result.isSuccess()).isTrue();
        org.mockito.ArgumentCaptor<AttClockRecord> captor =
                org.mockito.ArgumentCaptor.forClass(AttClockRecord.class);
        verify(clockRecordMapper).insert(captor.capture());
        AttClockRecord saved = captor.getValue();
        assertThat(saved.getEmployeeId()).isEqualTo(77L);
        assertThat(saved.getClockType()).isEqualTo(1);
        assertThat(saved.getRemark()).isEqualTo("移动端上班打卡");
    }

    @Test
    @DisplayName("打卡：未配置考勤地点时不做距离校验直接放行落库")
    void clock_skipsLocationCheckWhenUnconfigured() {
        login();
        when(clockRecordMapper.selectCount(any())).thenReturn(0L);
        when(attLocationService.getAssignedActiveLocations(77L)).thenReturn(List.of());

        Result<AttClockRecord> result = controller.clock(clockParams(2));

        assertThat(result.isSuccess()).isTrue();
        verify(clockRecordMapper).insert(any(AttClockRecord.class));
    }
}
