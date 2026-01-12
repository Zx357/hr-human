package com.kadmin.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.kadmin.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 公司实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("org_company")
public class OrgCompany extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 父公司ID
     */
    private Long parentId;

    /**
     * 公司编码
     */
    private String companyCode;

    /**
     * 公司名称
     */
    private String companyName;

    /**
     * 简称
     */
    private String shortName;

    /**
     * 法人代表
     */
    private String legalPerson;

    /**
     * 税号
     */
    private String taxNumber;

    /**
     * 地址
     */
    private String address;

    /**
     * 联系电话
     */
    private String phone;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 网站
     */
    private String website;

    /**
     * 描述
     */
    private String description;

    /**
     * 排序
     */
    private Integer sortOrder;

    /**
     * 状态：0-禁用，1-启用
     */
    private Integer status;

    /**
     * 是否删除
     */
    @TableLogic
    private Integer deleted;
}