package com.kadmin.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

/**
 * Web MVC 配置
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private static final Logger log = LoggerFactory.getLogger(WebMvcConfig.class);

    @Value("${file.upload.path:}")
    private String configuredUploadPath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 确定上传路径
        String uploadPath;
        if (configuredUploadPath != null && !configuredUploadPath.isEmpty()) {
            // 如果配置的路径是相对路径，转换为绝对路径
            File configFile = new File(configuredUploadPath);
            if (configFile.isAbsolute()) {
                uploadPath = configuredUploadPath;
            } else {
                uploadPath = System.getProperty("user.dir") + File.separator + configuredUploadPath;
            }
        } else {
            // 使用项目根目录下的uploads文件夹
            uploadPath = System.getProperty("user.dir") + File.separator + "uploads";
        }

        // 确保路径以分隔符结尾
        if (!uploadPath.endsWith(File.separator)) {
            uploadPath += File.separator;
        }

        // 确保目录存在
        File uploadDir = new File(uploadPath);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }

        log.info("静态资源映射: /uploads/** -> file:{}", uploadPath);

        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadPath);
    }
}