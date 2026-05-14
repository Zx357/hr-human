package com.kadmin.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.kadmin.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Mobile chat group member.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("mobile_chat_group_member")
public class MobileChatGroupMember extends BaseEntity {
    private Long groupId;
    private Long employeeId;
    private Integer roleType;
    private Integer status;
}
