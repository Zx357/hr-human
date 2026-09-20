package com.kadmin.web.controller.mobile;

import com.kadmin.attendance.mapper.AttClockRecordMapper;
import com.kadmin.common.Result;
import com.kadmin.common.security.LoginUser;
import com.kadmin.hr.mapper.HrApplicationMapper;
import com.kadmin.system.mapper.SysNoticeMapper;
import com.kadmin.system.service.NotificationService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

/**
 * 移动端首页统计单元测试：MobileHomeController.getStats 的出勤天数去重口径与兜底。
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class MobileHomeControllerTest {

    @Mock
    private AttClockRecordMapper clockRecordMapper;
    @Mock
    private HrApplicationMapper applicationMapper;
    @Mock
    private SysNoticeMapper noticeMapper;
    @Mock
    private NotificationService notificationService;

    private MobileHomeController controller;

    @BeforeEach
    void setUp() {
        controller = new MobileHomeController(clockRecordMapper, applicationMapper, noticeMapper,
                notificationService);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void login(Long userId, Long employeeId) {
        LoginUser loginUser = new LoginUser(userId, "user" + userId, null, null, null, employeeId, null, null);
        SecurityContextHolder.getContext()
                .setAuthentication(new UsernamePasswordAuthenticationToken(loginUser, "N/A"));
    }

    @Test
    @DisplayName("未登录时返回请先登录错误")
    void getStats_requiresLogin() {
        Result<Map<String, Object>> result = controller.getStats();
        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getMsg()).contains("请先登录");
    }

    @Test
    @DisplayName("月度出勤天数取 mapper 的按日期去重统计值，其余指标正确汇总")
    void getStats_returnsDistinctAttendDaysAndCounts() {
        login(1L, 77L);
        lenient().when(clockRecordMapper.countDistinctAttendDays(org.mockito.ArgumentMatchers.eq(77L),
                org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any())).thenReturn(15L);
        // 依次为：待处理、已通过
        lenient().when(applicationMapper.selectCount(org.mockito.ArgumentMatchers.any())).thenReturn(3L, 5L);
        lenient().when(applicationMapper.selectMobilePendingCount(77L)).thenReturn(2L);
        lenient().when(noticeMapper.selectCount(org.mockito.ArgumentMatchers.any())).thenReturn(4L);
        lenient().when(notificationService.unreadCount(77L)).thenReturn(6L);

        Result<Map<String, Object>> result = controller.getStats();

        assertThat(result.isSuccess()).isTrue();
        Map<String, Object> stats = result.getData();
        assertThat(stats.get("monthAttendDays")).isEqualTo(15L);
        assertThat(stats.get("pendingCount")).isEqualTo(3L);
        assertThat(stats.get("approvedCount")).isEqualTo(5L);
        assertThat(stats.get("approvalCount")).isEqualTo(2L);
        assertThat(stats.get("noticeCount")).isEqualTo(4L);
        assertThat(stats.get("unreadNotifications")).isEqualTo(6L);
    }

    @Test
    @DisplayName("各 mapper 返回 null 时统计兜底为0，不抛异常")
    void getStats_nullMapperResultsFallBackToZero() {
        login(1L, 77L);
        lenient().when(clockRecordMapper.countDistinctAttendDays(org.mockito.ArgumentMatchers.eq(77L),
                org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any())).thenReturn(null);
        lenient().when(applicationMapper.selectCount(org.mockito.ArgumentMatchers.any())).thenReturn(null, null);
        lenient().when(applicationMapper.selectMobilePendingCount(77L)).thenReturn(null);
        lenient().when(noticeMapper.selectCount(org.mockito.ArgumentMatchers.any())).thenReturn(null);
        lenient().when(notificationService.unreadCount(77L)).thenReturn(0L);

        Result<Map<String, Object>> result = controller.getStats();

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getData().get("monthAttendDays")).isEqualTo(0L);
        assertThat(result.getData().get("pendingCount")).isEqualTo(0L);
        assertThat(result.getData().get("approvedCount")).isEqualTo(0L);
        assertThat(result.getData().get("approvalCount")).isEqualTo(0L);
    }
}
