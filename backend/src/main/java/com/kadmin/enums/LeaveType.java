package com.kadmin.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 请假类型枚举，对应 hr_application 表中 app_type='leave' 时的 title 字段值
 */
@Getter
@AllArgsConstructor
public enum LeaveType {
    ANNUAL("1", "年假"),
    PERSONAL("2", "事假"),
    SICK("3", "病假"),
    MARRIAGE("4", "婚假"),
    MATERNITY("5", "产假"),
    PATERNITY("6", "陪产假"),
    BEREAVEMENT("7", "丧假");

    private final String code;
    private final String label;
}
