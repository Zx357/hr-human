package com.kadmin.mobile.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.kadmin.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 移动端时光动态评论
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("mobile_moment_comment")
public class MobileMomentComment extends BaseEntity {

    /**
     * 动态ID
     */
    private Long postId;

    /**
     * 评论人员工ID
     */
    private Long employeeId;

    /**
     * 评论内容
     */
    private String content;

    /**
     * 评论人姓名（联表冗余）
     */
    @TableField(exist = false)
    private String authorName;

    /**
     * 评论人头像（联表冗余）
     */
    @TableField(exist = false)
    private String authorAvatar;
}
