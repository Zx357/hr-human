package com.kadmin.web.controller.system;

import com.kadmin.organization.mapper.OrgUnitMapper;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.Date;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.RETURNS_DEFAULTS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ReportControllerTest {

    @Test
    void attendanceSummaryWithoutOrganizationHasOnlyDateParameters() {
        assertAttendanceQueries(null, null, List.of(), List.of(
                Date.valueOf("2026-02-01"), Date.valueOf("2026-02-28")));
    }

    @Test
    void attendanceSummaryExpandsCompanyAndBindsEachOrganizationSeparately() {
        assertAttendanceQueries(1L, null, List.of(1L, 2L, 3L), List.of(
                1L, 2L, 3L, Date.valueOf("2026-02-01"), Date.valueOf("2026-02-28")));
    }

    @Test
    void attendanceSummaryUsesDepartmentBeforeCompany() {
        assertAttendanceQueries(1L, 2L, List.of(2L, 3L), List.of(
                2L, 3L, Date.valueOf("2026-02-01"), Date.valueOf("2026-02-28")));
    }

    @Test
    void attendanceSummaryWithUnknownOrganizationHasNoResults() {
        assertAttendanceQueries(99L, null, List.of(), List.of(
                Date.valueOf("2026-02-01"), Date.valueOf("2026-02-28")));
    }

    private void assertAttendanceQueries(Long companyId, Long deptId, List<Long> orgIds, List<Object> expectedParams) {
        List<String> queries = new ArrayList<>();
        JdbcTemplate jdbc = mock(JdbcTemplate.class, invocation -> {
            String method = invocation.getMethod().getName();
            if (method.equals("queryForObject") || method.equals("query")) {
                String sql = invocation.getArgument(0);
                Object[] params = (Object[]) invocation.getRawArguments()[2];
                queries.add(sql);
                assertThat(sql).containsOnlyOnce(" WHERE ");
                assertThat(sql.chars().filter(c -> c == '?').count()).isEqualTo(params.length);
                assertThat(Arrays.asList(params)).containsExactlyElementsOf(expectedParams);
                return method.equals("queryForObject") ? 0L : null;
            }
            return RETURNS_DEFAULTS.answer(invocation);
        });
        OrgUnitMapper orgMapper = mock(OrgUnitMapper.class);
        Long orgId = deptId != null ? deptId : companyId;
        if (orgId != null) {
            when(orgMapper.selectOrgAndChildIds(orgId)).thenReturn(orgIds);
        }

        assertThat(new ReportController(jdbc, orgMapper).attendanceSummary("2026-02", companyId, deptId).isSuccess())
                .isTrue();
        assertThat(queries).hasSize(7);
        if (orgId != null && orgIds.isEmpty()) {
            assertThat(queries).allSatisfy(sql -> assertThat(sql).contains("AND 1=0"));
        }
    }
}
