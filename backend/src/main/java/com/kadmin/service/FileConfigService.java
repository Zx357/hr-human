package com.kadmin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kadmin.entity.SysFileConfig;
import com.kadmin.mapper.SysFileConfigMapper;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 文件路径配置服务
 */
@Service
public class FileConfigService extends ServiceImpl<SysFileConfigMapper, SysFileConfig> {

    private static final Logger log = LoggerFactory.getLogger(FileConfigService.class);

    public static final String KEY_UPLOAD_BASE = "upload_base_path";
    public static final String KEY_AVATAR = "employee_avatar_path";
    public static final String KEY_ID_FRONT = "id_card_front_path";
    public static final String KEY_ID_BACK = "id_card_back_path";
    public static final String KEY_CONTRACT_PHOTO = "contract_photo_path";
    public static final String KEY_DIPLOMA_PHOTO = "diploma_photo_path";
    public static final String KEY_CERT_PHOTO = "cert_photo_path";

    private final Map<String, String> configCache = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        refreshCache();
    }

    /**
     * 刷新缓存
     */
    public void refreshCache() {
        try {
            List<SysFileConfig> configs = list(
                    new LambdaQueryWrapper<SysFileConfig>().eq(SysFileConfig::getDeleted, 0));
            configCache.clear();
            for (SysFileConfig config : configs) {
                configCache.put(config.getConfigKey(), config.getConfigValue());
                log.info("加载文件路径配置: {} = {}", config.getConfigKey(), config.getConfigValue());
            }
        } catch (Exception e) {
            log.warn("加载文件路径配置失败，使用默认值: {}", e.getMessage());
        }
    }

    /**
     * 获取配置值
     */
    public String getConfigValue(String key) {
        return configCache.getOrDefault(key, getDefaultValue(key));
    }

    /**
     * 获取解析后的绝对路径
     */
    public String getAbsolutePath(String key) {
        String value = getConfigValue(key);
        if (value == null || value.isEmpty()) {
            value = getDefaultValue(key);
        }
        File file = new File(value);
        if (file.isAbsolute()) {
            return value;
        }
        return System.getProperty("user.dir") + File.separator + value;
    }

    /**
     * 获取所有配置
     */
    public List<SysFileConfig> getAllConfigs() {
        return list(new LambdaQueryWrapper<SysFileConfig>().eq(SysFileConfig::getDeleted, 0));
    }

    /**
     * 更新配置
     */
    @Transactional
    public boolean updateConfig(SysFileConfig config) {
        boolean result = updateById(config);
        if (result) {
            ensureDirectoryExists(config.getConfigValue());
            refreshCache();
        }
        return result;
    }

    /**
     * 确保目录存在
     */
    public void ensureDirectoryExists(String pathStr) {
        try {
            Path path = Paths.get(pathStr);
            if (!path.isAbsolute()) {
                path = Paths.get(System.getProperty("user.dir"), pathStr);
            }
            File dir = path.toFile();
            if (!dir.exists()) {
                boolean created = dir.mkdirs();
                log.info("创建目录: {} - {}", dir.getAbsolutePath(), created ? "成功" : "失败");
            }
        } catch (Exception e) {
            log.error("创建目录失败: {}", pathStr, e);
        }
    }

    private String getDefaultValue(String key) {
        return switch (key) {
            case KEY_UPLOAD_BASE -> "./uploads";
            case KEY_AVATAR -> "D:/rzphoto/employee_photo";
            case KEY_ID_FRONT -> "D:/rzphoto/id_card_front";
            case KEY_ID_BACK -> "D:/rzphoto/id_card_back";
            case KEY_CONTRACT_PHOTO -> "D:/rzphoto/Pic_Contract";
            case KEY_DIPLOMA_PHOTO -> "D:/rzphoto/Pic_Diploma";
            case KEY_CERT_PHOTO -> "D:/rzphoto/Pic_Certificate";
            default -> "";
        };
    }
}
