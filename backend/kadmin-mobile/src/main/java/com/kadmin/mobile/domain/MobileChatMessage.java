package com.kadmin.mobile.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.kadmin.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Mobile chat message.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("mobile_chat_message")
public class MobileChatMessage extends BaseEntity {
    /** 会话类型：1-群聊 2-单聊 */
    private Integer chatType;
    /** 群聊ID（群聊时有效） */
    private Long groupId;
    /** 对方员工ID（单聊时有效，存接收方） */
    private Long peerEmployeeId;
    /** 发送人员工ID */
    private Long fromEmployeeId;
    private String content;

    @TableField(exist = false)
    private String fromName;

    @TableField(exist = false)
    private String fromAvatar;
}
