package com.kadmin.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.kadmin.system.domain.SysConfig;
import com.kadmin.system.mapper.SysConfigMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 系统参数配置服务
 */
@Slf4j
@Service
public class SysConfigService extends ServiceImpl<SysConfigMapper, SysConfig> {

    /**
     * 预置配置键：高德地图Key（PC端考勤地点选点 + 移动端H5打卡地图）
     */
    public static final String KEY_AMAP = "map.amap.key";

    /**
     * 预置配置键：高德安全密钥（2021-12 后申请的 key 必须配套，旧 key 可空）
     */
    public static final String KEY_AMAP_SECURITY = "map.amap.security-code";

    /**
     * 预置配置键：微信小程序AppID（仅记录，实际为编译期配置）
     */
    public static final String KEY_WECHAT_APPID = "wechat.appid";

    /**
     * 默认分组：基础
     */
    public static final String GROUP_BASIC = "basic";

    /**
     * 分组：地图配置
     */
    public static final String GROUP_MAP_CONFIG = "map_config";

    /**
     * 分组：微信配置
     */
    public static final String GROUP_WECHAT_CONFIG = "wechat_config";

    /**
     * 获取全部配置（管理端，含非公开/停用项），按分组+排序输出
     */
    public List<SysConfig> getAllConfigs() {
        return list(new LambdaQueryWrapper<SysConfig>()
                .orderByAsc(SysConfig::getConfigGroup)
                .orderByAsc(SysConfig::getSortOrder)
                .orderByAsc(SysConfig::getId));
    }

    /**
     * 新增配置（configKey 唯一校验）
     */
    @Transactional
    public boolean addConfig(SysConfig config) {
        String key = trimToNull(config.getConfigKey());
        if (key == null) {
            throw new IllegalArgumentException("配置键不能为空");
        }
        if (trimToNull(config.getConfigName()) == null) {
            throw new IllegalArgumentException("配置名称不能为空");
        }
        Long count = count(new LambdaQueryWrapper<SysConfig>().eq(SysConfig::getConfigKey, key));
        if (count != null && count > 0) {
            throw new IllegalArgumentException("配置键已存在：" + key);
        }
        config.setConfigKey(key);
        config.setConfigName(config.getConfigName().trim());
        // 配置值可空：为空时对应功能降级，不阻止保存
        config.setConfigValue(trimToNull(config.getConfigValue()));
        if (trimToNull(config.getConfigGroup()) == null) {
            config.setConfigGroup(GROUP_BASIC);
        } else {
            config.setConfigGroup(config.getConfigGroup().trim());
        }
        if (config.getIsPublic() == null) {
            config.setIsPublic(1);
        }
        if (config.getStatus() == null) {
            config.setStatus(1);
        }
        if (config.getSortOrder() == null) {
            config.setSortOrder(0);
        }
        boolean success = save(config);
        if (success) {
            log.info("新增系统参数配置: {}", key);
        }
        return success;
    }

    /**
     * 修改配置（按 id，可改 value/name/group/is_public/remark/status/sort，配置键不可改）。
     *
     * 值/说明字段支持清空：configValue/remark 传空白即置 NULL（管理端编辑弹窗清空输入保存即清除），
     * 传 null（字段缺失）表示不修改；其余字段为 null 时同样不修改。
     */
    @Transactional
    public boolean updateConfig(SysConfig config) {
        if (config.getId() == null) {
            throw new IllegalArgumentException("配置ID不能为空");
        }
        SysConfig exist = getById(config.getId());
        if (exist == null) {
            throw new IllegalArgumentException("配置不存在");
        }
        if (config.getConfigKey() != null && !config.getConfigKey().trim().equals(exist.getConfigKey())) {
            throw new IllegalArgumentException("配置键不允许修改");
        }
        if (config.getConfigName() != null && config.getConfigName().trim().isEmpty()) {
            throw new IllegalArgumentException("配置名不能为空");
        }
        LambdaUpdateWrapper<SysConfig> wrapper = new LambdaUpdateWrapper<SysConfig>()
                .eq(SysConfig::getId, config.getId())
                .set(config.getConfigName() != null, SysConfig::getConfigName, trimToNull(config.getConfigName()))
                // 值允许显式清空：传了字段(即使空白)就按传的值落库，trim 后为空即置 NULL
                .set(config.getConfigValue() != null, SysConfig::getConfigValue, trimToNull(config.getConfigValue()))
                .set(config.getConfigGroup() != null && !config.getConfigGroup().trim().isEmpty(),
                        SysConfig::getConfigGroup, trimToNull(config.getConfigGroup()))
                .set(config.getRemark() != null, SysConfig::getRemark, trimToNull(config.getRemark()))
                .set(config.getIsPublic() != null, SysConfig::getIsPublic, config.getIsPublic())
                .set(config.getStatus() != null, SysConfig::getStatus, config.getStatus())
                .set(config.getSortOrder() != null, SysConfig::getSortOrder, config.getSortOrder());
        boolean success = update(wrapper);
        if (success) {
            log.info("更新系统参数配置: {} (id={})", exist.getConfigKey(), exist.getId());
        }
        return success;
    }

    /**
     * 删除配置（预置种子键也允许删除，删除后对应功能降级）
     */
    @Transactional
    public boolean deleteConfig(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("配置ID不能为空");
        }
        SysConfig exist = getById(id);
        if (exist == null) {
            throw new IllegalArgumentException("配置不存在");
        }
        boolean success = removeById(id);
        if (success) {
            log.info("删除系统参数配置: {} (id={})", exist.getConfigKey(), id);
        }
        return success;
    }

    /**
     * 公开读取配置（登录即可）：
     * 只返回 存在 + is_public=1 + status=1 + 值非空 的键；
     * keys 为空时返回全部公开配置；不存在的键直接忽略（不报错）
     *
     * @param keys 逗号分隔的配置键，可空
     */
    public Map<String, String> getPublicConfigs(String keys) {
        Set<String> wanted = parseKeys(keys);
        List<SysConfig> configs = list(new LambdaQueryWrapper<SysConfig>()
                .eq(SysConfig::getIsPublic, 1)
                .eq(SysConfig::getStatus, 1)
                .orderByAsc(SysConfig::getConfigGroup)
                .orderByAsc(SysConfig::getSortOrder)
                .orderByAsc(SysConfig::getId));
        Map<String, String> result = new LinkedHashMap<>();
        for (SysConfig config : configs) {
            // 双重保险：查询条件已过滤 is_public/status，内存再校验一次，确保非公开/停用配置绝不外泄
            if (config.getIsPublic() == null || config.getIsPublic() != 1) {
                continue;
            }
            if (config.getStatus() == null || config.getStatus() != 1) {
                continue;
            }
            // keys 非空时做精确匹配，未知键直接忽略
            if (!wanted.isEmpty() && !wanted.contains(config.getConfigKey())) {
                continue;
            }
            String value = config.getConfigValue();
            // 值为空的配置不返回（前端按"未配置"降级处理）
            if (value == null || value.trim().isEmpty()) {
                continue;
            }
            result.put(config.getConfigKey(), value.trim());
        }
        return result;
    }

    /**
     * 解析逗号分隔的配置键（去空白、去重、忽略空段）
     */
    private Set<String> parseKeys(String keys) {
        Set<String> result = new LinkedHashSet<>();
        if (keys == null || keys.trim().isEmpty()) {
            return result;
        }
        for (String key : keys.split(",")) {
            String trimmed = key.trim();
            if (!trimmed.isEmpty()) {
                result.add(trimmed);
            }
        }
        return result;
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
