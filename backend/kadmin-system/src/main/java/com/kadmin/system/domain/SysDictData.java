package com.kadmin.system.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.kadmin.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 字典数据实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_dict_data")
public class SysDictData extends BaseEntity {

    /**
     * 字典类型ID
     */
    private Long dictTypeId;

    /**
     * 字典标签（中文）
     */
    private String dictLabel;

    /**
     * 字典标签（英文）
     */
    private String dictLabelEn;

    /**
     * 字典值
     */
    private String dictValue;

    /**
     * CSS样式
     */
    private String cssClass;

    /**
     * 列表样式
     */
    private String listClass;

    /**
     * 排序
     */
    private Integer sortOrder;

    /**
     * 状态：0-禁用，1-启用
     */
    private Integer status;

    /**
     * 是否默认：0-否，1-是
     */
    private Integer isDefault;

    /**
     * 多语言标签（非数据库字段）
     */
    @TableField(exist = false)
    private String i18nLabel;
}