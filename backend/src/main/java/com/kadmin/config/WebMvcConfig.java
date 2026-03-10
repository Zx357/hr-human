package com.kadmin.config;

import com.kadmin.service.FileConfigService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

/**
 * Web MVC 配置
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private static final Logger log = LoggerFactory.getLogger(WebMvcConfig.class);

    @Autowired
    private FileConfigService fileConfigService;

    @Override
    public void addResourceHandlers(@NonNull ResourceHandlerRegistry registry) {
        // 通用上传路径
        String uploadPath = fileConfigService.getAbsolutePath(FileConfigService.KEY_UPLOAD_BASE);
        registerResourceHandler(registry, "/uploads/**", uploadPath);

        // 员工头像路径
        String avatarPath = fileConfigService.getAbsolutePath(FileConfigService.KEY_AVATAR);
        registerResourceHandler(registry, "/employee_photo/**", avatarPath);

        // 身份证正面路径
        String idFrontPath = fileConfigService.getAbsolutePath(FileConfigService.KEY_ID_FRONT);
        registerResourceHandler(registry, "/id_card_front/**", idFrontPath);

        // 身份证反面路径
        String idBackPath = fileConfigService.getAbsolutePath(FileConfigService.KEY_ID_BACK);
        registerResourceHandler(registry, "/id_card_back/**", idBackPath);

        // 合同照片路径
        String contractPhotoPath = fileConfigService.getAbsolutePath(FileConfigService.KEY_CONTRACT_PHOTO);
        registerResourceHandler(registry, "/contract_photo/**", contractPhotoPath);

        // 毕业证照片路径
        String diplomaPhotoPath = fileConfigService.getAbsolutePath(FileConfigService.KEY_DIPLOMA_PHOTO);
        registerResourceHandler(registry, "/diploma_photo/**", diplomaPhotoPath);

        // 证书照片路径
        String certPhotoPath = fileConfigService.getAbsolutePath(FileConfigService.KEY_CERT_PHOTO);
        registerResourceHandler(registry, "/cert_photo/**", certPhotoPath);
    }

    private void registerResourceHandler(ResourceHandlerRegistry registry, String pattern, String path) {
        if (!path.endsWith(File.separator)) {
            path += File.separator;
        }

        File dir = new File(path);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        log.info("静态资源映射: {} -> file:{}", pattern, path);
        registry.addResourceHandler(pattern)
                .addResourceLocations("file:" + path);
    }
}
