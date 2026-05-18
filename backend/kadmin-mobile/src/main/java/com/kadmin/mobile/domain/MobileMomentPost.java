package com.kadmin.mobile.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.kadmin.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Mobile moment post.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("mobile_moment_post")
public class MobileMomentPost extends BaseEntity {
    private Long employeeId;
    private String content;
    private String labels;
    private String images;
    private Integer visibility;
    private Integer status;
    private Integer commentCount;
    private Integer likeCount;

    @TableField(exist = false)
    private String authorName;

    @TableField(exist = false)
    private String authorAvatar;

    @TableField(exist = false)
    private String post;

    @TableField(exist = false)
    private String deptName;

    @TableField(exist = false)
    private Boolean liked;
}
