package com.kadmin.mobile.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import com.kadmin.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 聊天会话已读状态
 * 记录每个员工在每个会话中已读到的最后一条消息，用于计算未读数
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("mobile_chat_read_state")
public class MobileChatReadState extends BaseEntity {

    /**
     * 员工ID
     */
    private Long employeeId;

    /**
     * 会话类型：1-群聊 2-单聊
     */
    private Integer chatType;

    /**
     * 会话目标ID（群聊ID或对方员工ID）
     */
    private Long targetId;

    /**
     * 已读到的最后一条消息ID
     */
    private Long lastReadMessageId;

    /**
     * 是否置顶：0-否 1-是（会话列表排序时置顶优先）
     */
    private Integer sticky;

    /**
     * 是否从会话列表隐藏：0-否 1-是（仅隐藏会话入口，不删除消息记录，发新消息自动取消）
     */
    private Integer hidden;
}
