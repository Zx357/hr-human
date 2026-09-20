package com.kadmin.system.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import com.kadmin.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 公告已读记录实体（员工阅读公告后落一条记录，用于未读角标）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_notice_read")
public class SysNoticeRead extends BaseEntity {

    /**
     * 公告ID
     */
    private Long noticeId;

    /**
     * 员工ID
     */
    private Long employeeId;

    /**
     * 阅读时间
     */
    private LocalDateTime readTime;
}
