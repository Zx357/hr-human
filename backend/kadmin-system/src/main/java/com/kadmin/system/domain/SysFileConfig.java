package com.kadmin.system.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import com.kadmin.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 文件路径配置实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_file_config")
public class SysFileConfig extends BaseEntity {

    /**
     * 配置键
     */
    private String configKey;

    /**
     * 配置名称
     */
    private String configName;

    /**
     * 配置值(路径)
     */
    private String configValue;

    /**
     * 备注
     */
    private String remark;
}
