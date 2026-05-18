package com.kadmin.mobile.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.kadmin.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * Mobile contact friend request.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("mobile_contact_request")
public class MobileContactRequest extends BaseEntity {
    private Long requesterId;
    private Long targetId;
    private Integer status;
    private String remark;
    private LocalDateTime handledTime;

    @TableField(exist = false)
    private String requesterName;

    @TableField(exist = false)
    private String requesterAvatar;

    @TableField(exist = false)
    private String targetName;

    @TableField(exist = false)
    private String targetAvatar;
}
