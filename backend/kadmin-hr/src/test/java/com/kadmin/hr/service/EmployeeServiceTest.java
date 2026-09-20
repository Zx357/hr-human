package com.kadmin.hr.service;

import com.kadmin.hr.mapper.EmployeeMapper;
import com.kadmin.hr.mapper.HrCertificateMapper;
import com.kadmin.hr.mapper.HrEducationMapper;
import com.kadmin.hr.mapper.HrEmployeeExtraMapper;
import com.kadmin.hr.mapper.HrFamilyMemberMapper;
import com.kadmin.hr.mapper.HrMobileApproverMapper;
import com.kadmin.hr.mapper.HrWorkExperienceMapper;
import com.kadmin.organization.mapper.OrgUnitMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.lenient;

/**
 * 工号生成单元测试：EmployeeService.generateNextEmployeeNo 的核心分支。
 */
@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {

    @Mock
    private HrEducationMapper educationMapper;
    @Mock
    private HrFamilyMemberMapper familyMemberMapper;
    @Mock
    private HrWorkExperienceMapper workExperienceMapper;
    @Mock
    private HrCertificateMapper certificateMapper;
    @Mock
    private HrEmployeeExtraMapper employeeExtraMapper;
    @Mock
    private HrMobileApproverMapper mobileApproverMapper;
    @Mock
    private OrgUnitMapper orgUnitMapper;
    @Mock
    private ApplicationEventPublisher eventPublisher;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private EmployeeMapper employeeMapper;

    private EmployeeService service;

    @BeforeEach
    void setUp() {
        service = new EmployeeService(educationMapper, familyMemberMapper, workExperienceMapper,
                certificateMapper, employeeExtraMapper, mobileApproverMapper, orgUnitMapper,
                eventPublisher, passwordEncoder);
        // ServiceImpl 基类的 baseMapper 字段注入 mock
        ReflectionTestUtils.setField(service, "baseMapper", employeeMapper);
    }

    private String todayPrefix() {
        return "K" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
    }

    @Test
    @DisplayName("空表（无当日工号）从 001 起号")
    void generateNextEmployeeNo_emptyTableStartsFrom001() {
        lenient().when(employeeMapper.selectMaxEmployeeNo(todayPrefix())).thenReturn(null);
        assertThat(service.generateNextEmployeeNo()).isEqualTo(todayPrefix() + "001");
    }

    @Test
    @DisplayName("工号格式为 K + yyyyMMdd + 3位序号")
    void generateNextEmployeeNo_prefixFormat() {
        lenient().when(employeeMapper.selectMaxEmployeeNo(todayPrefix())).thenReturn(null);
        String no = service.generateNextEmployeeNo();
        assertThat(no).startsWith("K");
        assertThat(no).matches("K\\d{8}\\d{3}");
        assertThat(no).hasSize(12);
    }

    @Test
    @DisplayName("当日已有 005 号则生成 006（序号自增）")
    void generateNextEmployeeNo_incrementsExistingSequence() {
        lenient().when(employeeMapper.selectMaxEmployeeNo(todayPrefix())).thenReturn(todayPrefix() + "005");
        assertThat(service.generateNextEmployeeNo()).isEqualTo(todayPrefix() + "006");
    }

    @Test
    @DisplayName("序号 999 进位为 4 位 1000")
    void generateNextEmployeeNo_sequenceOverflowToFourDigits() {
        lenient().when(employeeMapper.selectMaxEmployeeNo(todayPrefix())).thenReturn(todayPrefix() + "999");
        assertThat(service.generateNextEmployeeNo()).isEqualTo(todayPrefix() + "1000");
    }

    @Test
    @DisplayName("最大工号与前缀等长（无序号后缀）视为空表，从 001 起")
    void generateNextEmployeeNo_maxNoWithoutSequenceFallsBackTo001() {
        lenient().when(employeeMapper.selectMaxEmployeeNo(todayPrefix())).thenReturn(todayPrefix());
        assertThat(service.generateNextEmployeeNo()).isEqualTo(todayPrefix() + "001");
    }
}
