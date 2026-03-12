package com.kadmin.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.kadmin.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/**
 * 公告通知实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_notice")
public class SysNotice extends BaseEntity {

    /**
     * 公告标题
     */
    private String noticeTitle;

    /**
     * 公告类型：1-公告，2-通知
     */
    private Integer noticeType;

    /**
     * 公告内容
     */
    private String noticeContent;

    /**
     * 状态：0-关闭，1-正常
     */
    private Integer status;

    /**
     * 发布日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate publishTime;
}
