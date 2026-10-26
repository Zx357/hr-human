package com.kadmin.web.controller.hr;

import com.kadmin.hr.domain.HrEmployee;
import com.kadmin.hr.service.EmployeeService;
import com.kadmin.system.domain.SysDictData;
import com.kadmin.system.service.SysDictService;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

class EmployeeControllerTest {

    @Test
    void contactListsResolvePositionLabelsWithOneDictionaryQuery() {
        EmployeeService employeeService = mock(EmployeeService.class);
        SysDictService dictService = mock(SysDictService.class);
        HrEmployee employee = new HrEmployee();
        employee.setPosition("gm");
        SysDictData position = new SysDictData();
        position.setDictValue("gm");
        position.setDictLabel("总经理");
        when(employeeService.getEmployeeList(12L, 1)).thenReturn(List.of(employee));
        when(dictService.getDictDataByCode("position")).thenReturn(List.of(position));

        List<HrEmployee> result = new EmployeeController(employeeService, dictService).list(12L, 1).getData();

        assertThat(result.get(0).getPositionName()).isEqualTo("总经理");
        verify(dictService).getDictDataByCode("position");
    }

    @Test
    void employeeDetailsIncludeTheConfiguredPositionLabelWithoutChangingTheCode() {
        EmployeeService employeeService = mock(EmployeeService.class);
        SysDictService dictService = mock(SysDictService.class);
        HrEmployee employee = new HrEmployee();
        employee.setId(1L);
        employee.setPosition("gm");
        SysDictData position = new SysDictData();
        position.setDictValue("gm");
        position.setDictLabel("总经理");
        when(employeeService.getEmployeeDetail(1L, true)).thenReturn(employee);
        when(dictService.getDictDataByCode("position")).thenReturn(List.of(position));

        HrEmployee result = new EmployeeController(employeeService, dictService).getDetail(1L).getData();

        assertThat(result.getPosition()).isEqualTo("gm");
        assertThat(result.getPositionName()).isEqualTo("总经理");
    }

    @Test
    void legacyPositionTextIsPreservedWhenNoDictionaryEntryMatches() {
        EmployeeService employeeService = mock(EmployeeService.class);
        SysDictService dictService = mock(SysDictService.class);
        HrEmployee employee = new HrEmployee();
        employee.setPosition("项目经理");
        when(employeeService.getEmployeeDetail(1L, true)).thenReturn(employee);
        when(dictService.getDictDataByCode("position")).thenReturn(List.of());

        HrEmployee result = new EmployeeController(employeeService, dictService).getDetail(1L).getData();

        assertThat(result.getPosition()).isEqualTo("项目经理");
        assertThat(result.getPositionName()).isNull();
    }
}
