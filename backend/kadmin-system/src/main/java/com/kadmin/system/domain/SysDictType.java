package com.kadmin.system.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.kadmin.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 字典类型实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_dict_type")
public class SysDictType extends BaseEntity {

    /**
     * 字典编码
     */
    private String dictCode;

    /**
     * 字典名称（中文）
     */
    private String dictName;

    /**
     * 字典名称（英文）
     */
    private String dictNameEn;

    /**
     * 描述
     */
    private String description;

    /**
     * 状态：0-禁用，1-启用
     */
    private Integer status;

    /**
     * 字典数据列表（非数据库字段）
     */
    @TableField(exist = false)
    private List<SysDictData> dictDataList;

    /**
     * 多语言名称（非数据库字段）
     */
    @TableField(exist = false)
    private String i18nName;
}