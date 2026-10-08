package com.kadmin.attendance.service;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.kadmin.attendance.domain.AttCalendarRule;
import com.kadmin.attendance.domain.AttClockRecord;
import com.kadmin.attendance.domain.AttDailyRecord;
import com.kadmin.attendance.domain.AttSchedule;
import com.kadmin.attendance.domain.AttShift;
import com.kadmin.attendance.domain.AttShiftPeriod;
import com.kadmin.attendance.mapper.AttCalendarRuleMapper;
import com.kadmin.attendance.mapper.AttClockRecordMapper;
import com.kadmin.attendance.mapper.AttDailyRecordMapper;
import com.kadmin.attendance.mapper.AttScheduleMapper;
import com.kadmin.attendance.mapper.AttShiftMapper;
import com.kadmin.attendance.mapper.AttShiftPeriodMapper;
import com.kadmin.hr.domain.HrApplication;
import com.kadmin.hr.domain.HrEmployee;
import com.kadmin.hr.mapper.EmployeeMapper;
import com.kadmin.hr.mapper.HrApplicationMapper;
import com.kadmin.organization.domain.OrgUnit;
import com.kadmin.organization.mapper.OrgUnitMapper;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collection;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * 考勤核算单元测试：AttendanceService.calculateDailyAttendance 的状态分支
 * （正常/迟到/早退/迟到+早退/缺卡未处理/旷工/请假/出差/休息日/锁定跳过）。
 *
 * 班次为"全天单时段"（无 shift_period 配置），09:00-18:00，无迟到/早退容忍。
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AttendanceServiceTest {

    private static final Long EMP_ID = 100L;
    private static final Long DEPT_ID = 10L;
    private static final Long COMPANY_ID = 5L;
    private static final Long SHIFT_ID = 7L;

    /** 2026-03-04 为周三（工作日）；2026-03-07 为周六 */
    private static final LocalDate WORKDAY = LocalDate.of(2026, 3, 4);
    private static final LocalDate SATURDAY = LocalDate.of(2026, 3, 7);

    @Mock
    private AttClockRecordMapper clockRecordMapper;
    @Mock
    private AttDailyRecordMapper dailyRecordMapper;
    @Mock
    private AttScheduleMapper scheduleMapper;
    @Mock
    private AttShiftMapper shiftMapper;
    @Mock
    private AttShiftPeriodMapper shiftPeriodMapper;
    @Mock
    private AttCalendarRuleMapper calendarRuleMapper;
    @Mock
    private EmployeeMapper employeeMapper;
    @Mock
    private OrgUnitMapper orgUnitMapper;
    @Mock
    private HrApplicationMapper applicationMapper;

    private AttendanceService service;

    @BeforeAll
    static void initMybatisPlusLambdaCache() {
        // 纯单元测试无 MyBatis 启动流程，手动注册实体元数据，否则 LambdaQueryWrapper 解析列名缺少缓存
        MapperBuilderAssistant assistant = new MapperBuilderAssistant(new MybatisConfiguration(), "");
        java.util.stream.Stream.of(HrEmployee.class, AttSchedule.class, AttShift.class, AttShiftPeriod.class,
                AttClockRecord.class, AttDailyRecord.class, HrApplication.class, AttCalendarRule.class)
                .forEach(clazz -> TableInfoHelper.initTableInfo(assistant, clazz));
    }

    @BeforeEach
    void setUp() {
        service = new AttendanceService(clockRecordMapper, dailyRecordMapper, scheduleMapper, shiftMapper,
                shiftPeriodMapper, calendarRuleMapper, employeeMapper, orgUnitMapper, applicationMapper);
        stubCommonData(List.of(), List.of(), List.of());
    }

    /**
     * 公共桩：在职员工 + 排班 + 全天班次（09:00-18:00） + 组织链（部门10→公司5）
     */
    private void stubCommonData(List<AttClockRecord> clocks, List<HrApplication> absenceApps,
            List<AttDailyRecord> existingRecords) {
        HrEmployee employee = new HrEmployee();
        employee.setId(EMP_ID);
        employee.setName("测试员工");
        employee.setDeptId(DEPT_ID);
        employee.setStatus(1);
        lenient().when(employeeMapper.selectList(any())).thenReturn(List.of(employee));

        AttSchedule schedule = new AttSchedule();
        schedule.setId(1L);
        schedule.setEmployeeId(EMP_ID);
        schedule.setScheduleDate(WORKDAY);
        schedule.setShiftId(SHIFT_ID);
        lenient().when(scheduleMapper.selectList(any())).thenReturn(List.of(schedule));

        AttShift shift = new AttShift();
        shift.setId(SHIFT_ID);
        shift.setShiftName("标准班");
        shift.setWorkStartTime(LocalTime.of(9, 0));
        shift.setWorkEndTime(LocalTime.of(18, 0));
        shift.setLateMinutes(0);
        shift.setEarlyMinutes(0);
        lenient().when(shiftMapper.selectBatchIds(anyCollection())).thenReturn(List.of(shift));

        lenient().when(shiftPeriodMapper.selectList(any())).thenReturn(List.of());

        OrgUnit dept = new OrgUnit();
        dept.setId(DEPT_ID);
        dept.setUnitType(OrgUnit.TYPE_DEPT);
        dept.setParentId(COMPANY_ID);
        dept.setUnitName("研发部");
        OrgUnit company = new OrgUnit();
        company.setId(COMPANY_ID);
        company.setUnitType(OrgUnit.TYPE_COMPANY);
        company.setParentId(0L);
        company.setUnitName("测试公司");
        lenient().when(orgUnitMapper.selectBatchIds(anyCollection())).thenAnswer(invocation -> {
            Collection<Long> ids = invocation.getArgument(0);
            return java.util.stream.Stream.of(dept, company).filter(u -> ids.contains(u.getId())).toList();
        });

        // 无假日规则（工作日口径）；休息日场景单独覆盖
        lenient().when(calendarRuleMapper.selectList(any())).thenReturn(List.of());

        lenient().when(clockRecordMapper.selectList(any())).thenReturn(clocks);
        // 第一次调用为请假/出差单预载，第二次为加班单预载
        lenient().when(applicationMapper.selectList(any()))
                .thenReturn(absenceApps)
                .thenReturn(List.of());
        lenient().when(dailyRecordMapper.selectList(any())).thenReturn(existingRecords);
        lenient().when(dailyRecordMapper.insert(any(com.kadmin.attendance.domain.AttDailyRecord.class))).thenAnswer(invocation -> {
            AttDailyRecord record = invocation.getArgument(0);
            record.setId(System.identityHashCode(record) + 0L);
            return 1;
        });
    }

    private AttClockRecord clock(int type, String time) {
        AttClockRecord record = new AttClockRecord();
        record.setEmployeeId(EMP_ID);
        record.setClockType(type);
        record.setClockTime(WORKDAY.atTime(LocalTime.parse(time)));
        return record;
    }

    private HrApplication absence(String appType) {
        HrApplication app = new HrApplication();
        app.setEmployeeId(EMP_ID);
        app.setAppType(appType);
        app.setStatus(1);
        app.setStartTime(WORKDAY.atStartOfDay());
        app.setEndTime(WORKDAY.atTime(LocalTime.MAX));
        return app;
    }

    private AttDailyRecord calculateAndCapture(List<AttClockRecord> clocks, List<HrApplication> absenceApps) {
        return calculateAndCapture(clocks, absenceApps, List.of());
    }

    private AttDailyRecord calculateAndCapture(List<AttClockRecord> clocks, List<HrApplication> absenceApps,
            List<AttDailyRecord> existingRecords) {
        stubCommonData(clocks, absenceApps, existingRecords);
        service.calculateDailyAttendance(WORKDAY, WORKDAY, null, null, null, List.of(EMP_ID));
        ArgumentCaptor<AttDailyRecord> captor = ArgumentCaptor.forClass(AttDailyRecord.class);
        verify(dailyRecordMapper).insert(captor.capture());
        return captor.getValue();
    }

    @Test
    @DisplayName("正常：09:00 前上班卡 + 18:00 后下班卡 → 状态1正常")
    void calculate_normal() {
        AttDailyRecord record = calculateAndCapture(
                List.of(clock(1, "08:55"), clock(2, "18:05")), List.of());
        assertThat(record.getStatus()).isEqualTo(1);
        assertThat(record.getLateMinutes()).isZero();
        assertThat(record.getEarlyMinutes()).isZero();
        assertThat(record.getWorkHours()).isEqualByComparingTo("9.00");
    }

    @Test
    @DisplayName("迟到：09:10 上班卡 → 状态2迟到，迟到10分钟")
    void calculate_late() {
        AttDailyRecord record = calculateAndCapture(
                List.of(clock(1, "09:10"), clock(2, "18:05")), List.of());
        assertThat(record.getStatus()).isEqualTo(2);
        assertThat(record.getLateMinutes()).isEqualTo(10);
    }

    @Test
    @DisplayName("早退：17:00 下班卡 → 状态3早退，早退60分钟")
    void calculate_earlyLeave() {
        AttDailyRecord record = calculateAndCapture(
                List.of(clock(1, "08:55"), clock(2, "17:00")), List.of());
        assertThat(record.getStatus()).isEqualTo(3);
        assertThat(record.getEarlyMinutes()).isEqualTo(60);
    }

    @Test
    @DisplayName("迟到+早退：09:10 上班 + 17:00 下班 → 状态7")
    void calculate_lateAndEarly() {
        AttDailyRecord record = calculateAndCapture(
                List.of(clock(1, "09:10"), clock(2, "17:00")), List.of());
        assertThat(record.getStatus()).isEqualTo(7);
    }

    @Test
    @DisplayName("缺卡：只有上班卡 → 状态0未处理（不算旷工）")
    void calculate_missingClock() {
        AttDailyRecord record = calculateAndCapture(List.of(clock(1, "08:55")), List.of());
        assertThat(record.getStatus()).isEqualTo(0);
    }

    @Test
    @DisplayName("旷工：全天无打卡且无请假/出差单 → 状态4旷工")
    void calculate_absent() {
        AttDailyRecord record = calculateAndCapture(List.of(), List.of());
        assertThat(record.getStatus()).isEqualTo(4);
    }

    @Test
    @DisplayName("请假：全天无打卡但有已审批请假单 → 状态5请假")
    void calculate_leave() {
        AttDailyRecord record = calculateAndCapture(List.of(), List.of(absence("leave")));
        assertThat(record.getStatus()).isEqualTo(5);
    }

    @Test
    @DisplayName("出差：全天无打卡但有已审批出差单 → 状态6出差（优先于请假）")
    void calculate_business() {
        AttDailyRecord record = calculateAndCapture(List.of(),
                List.of(absence("business"), absence("leave")));
        assertThat(record.getStatus()).isEqualTo(6);
    }

    @Test
    @DisplayName("休息日：公司双休规则下的周六不生成考勤记录")
    void calculate_restDay_skipped() {
        AttCalendarRule rule = new AttCalendarRule();
        rule.setCompanyId(COMPANY_ID);
        rule.setRuleType(2); // 双休
        rule.setStartDate(SATURDAY.minusDays(7));
        rule.setEndDate(SATURDAY.plusDays(7));
        lenient().when(calendarRuleMapper.selectList(any())).thenReturn(List.of(rule));

        AttSchedule schedule = new AttSchedule();
        schedule.setEmployeeId(EMP_ID);
        schedule.setScheduleDate(SATURDAY);
        schedule.setShiftId(SHIFT_ID);
        lenient().when(scheduleMapper.selectList(any())).thenReturn(List.of(schedule));

        service.calculateDailyAttendance(SATURDAY, SATURDAY, null, null, null, List.of(EMP_ID));
        verify(dailyRecordMapper, never()).insert(any(com.kadmin.attendance.domain.AttDailyRecord.class));
    }

    @Test
    @DisplayName("已锁定记录跳过核算：既有记录 locked=1 时不更新")
    void calculate_lockedRecord_skipped() {
        AttDailyRecord lockedRecord = new AttDailyRecord();
        lockedRecord.setId(999L);
        lockedRecord.setEmployeeId(EMP_ID);
        lockedRecord.setAttDate(WORKDAY);
        lockedRecord.setLocked(1);

        stubCommonData(List.of(clock(1, "08:55"), clock(2, "18:05")), List.of(), List.of(lockedRecord));
        service.calculateDailyAttendance(WORKDAY, WORKDAY, null, null, null, List.of(EMP_ID));
        verify(dailyRecordMapper, never()).insert(any(com.kadmin.attendance.domain.AttDailyRecord.class));
        verify(dailyRecordMapper, never()).updateById(any(com.kadmin.attendance.domain.AttDailyRecord.class));
    }

    @Test
    @DisplayName("无排班不生成考勤记录")
    void calculate_noSchedule_skipped() {
        lenient().when(scheduleMapper.selectList(any())).thenReturn(List.of());
        service.calculateDailyAttendance(WORKDAY, WORKDAY, null, null, null, List.of(EMP_ID));
        verify(dailyRecordMapper, never()).insert(any(com.kadmin.attendance.domain.AttDailyRecord.class));
    }
}
