package com.kadmin.mobile.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.kadmin.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * Mobile chat group.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("mobile_chat_group")
public class MobileChatGroup extends BaseEntity {
    private String groupName;
    private Long ownerId;
    private String avatar;
    private Integer memberCount;
    private String lastMessage;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastMessageTime;

    private Integer status;

    @TableField(exist = false)
    private String ownerName;
}
