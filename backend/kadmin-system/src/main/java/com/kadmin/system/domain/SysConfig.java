package com.kadmin.system.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import com.kadmin.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 系统参数配置实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_config")
public class SysConfig extends BaseEntity {

    /**
     * 配置键（唯一），如 map.amap.key
     */
    private String configKey;

    /**
     * 配置名称（显示名），如 高德地图Key
     */
    private String configName;

    /**
     * 配置值（可空，为空时对应功能降级）
     */
    private String configValue;

    /**
     * 配置分组：map_config-地图配置，wechat_config-微信配置，basic-基础
     */
    private String configGroup;

    /**
     * 是否公开：1-登录用户可通过公开接口读取，0-仅管理端可见（防敏感配置泄露）
     */
    private Integer isPublic;

    /**
     * 说明
     */
    private String remark;

    /**
     * 状态：1-启用，0-停用（停用的配置公开接口不返回）
     */
    private Integer status;

    /**
     * 排序
     */
    private Integer sortOrder;
}
