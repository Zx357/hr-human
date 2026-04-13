package com.kadmin.dto;

import lombok.Data;

import java.util.List;

/**
 * 日考勤锁定请求参数
 */
@Data
public class LockDailyRecordRequest {
    private List<Long> ids;
    private Boolean lock;
}
