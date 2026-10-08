package com.kadmin.hr.constant;

import java.util.Map;

/**
 * 假期类型公共常量（与字典 leave_type 的 value 对应）
 *
 * 请假申请单（hr_application.title）与假期额度（hr_leave_quota.leave_type）
 * 均以该字典值编码，薪资核算（kadmin-salary）与额度扣减（LeaveQuotaService）
 * 统一引用本常量，避免各处散落的魔法值。
 */
public final class LeaveTypeConstants {

    /** 年假 */
    public static final String ANNUAL_LEAVE = "1";
    /** 事假 */
    public static final String PERSONAL_LEAVE = "2";
    /** 病假 */
    public static final String SICK_LEAVE = "3";
    /** 婚假 */
    public static final String MARRIAGE_LEAVE = "4";
    /** 产假 */
    public static final String MATERNITY_LEAVE = "5";
    /** 陪产假 */
    public static final String PATERNITY_LEAVE = "6";
    /** 丧假 */
    public static final String FUNERAL_LEAVE = "7";

    /** 假期类型展示名（仅用于后端提示消息） */
    public static final Map<String, String> LEAVE_TYPE_LABELS = Map.of(
            ANNUAL_LEAVE, "年假",
            PERSONAL_LEAVE, "事假",
            SICK_LEAVE, "病假",
            MARRIAGE_LEAVE, "婚假",
            MATERNITY_LEAVE, "产假",
            PATERNITY_LEAVE, "陪产假",
            FUNERAL_LEAVE, "丧假");

    private LeaveTypeConstants() {
    }
}
