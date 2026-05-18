package com.kadmin.mobile.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import com.kadmin.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Mobile moment like record.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("mobile_moment_like")
public class MobileMomentLike extends BaseEntity {
    private Long postId;
    private Long employeeId;
}
