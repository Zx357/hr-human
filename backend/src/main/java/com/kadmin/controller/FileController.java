package com.kadmin.controller;

import com.kadmin.common.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * 文件上传控制器
 */
@Tag(name = "文件管理")
@RestController
@RequestMapping("/file")
public class FileController {

    private static final Logger log = LoggerFactory.getLogger(FileController.class);

    @Value("${file.upload.path:}")
    private String configuredUploadPath;

    private String uploadPath;

    @PostConstruct
    public void init() {
        // 如果配置了路径，使用配置的路径；否则使用项目根目录下的uploads
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

        // 确保上传目录存在
        File uploadDir = new File(uploadPath);
        if (!uploadDir.exists()) {
            boolean created = uploadDir.mkdirs();
            log.info("创建上传目录: {} - {}", uploadPath, created ? "成功" : "失败");
        }
        log.info("文件上传路径(绝对路径): {}", uploadPath);
    }

    @Value("${file.upload.max-size:10MB}")
    private String maxSize;

    @Value("${file.upload.allowed-types:jpg,jpeg,png,gif,pdf,doc,docx,xls,xlsx}")
    private String allowedTypes;

    @Operation(summary = "上传文件")
    @PostMapping("/upload")
    public Result<String> upload(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return Result.error("请选择要上传的文件");
        }

        // 获取原始文件名
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            return Result.error("文件名不能为空");
        }

        // 获取文件扩展名
        String extension = "";
        int dotIndex = originalFilename.lastIndexOf(".");
        if (dotIndex > 0) {
            extension = originalFilename.substring(dotIndex + 1).toLowerCase();
        }

        // 验证文件类型
        String[] allowedTypeArray = allowedTypes.split(",");
        boolean isAllowed = false;
        for (String type : allowedTypeArray) {
            if (type.trim().equalsIgnoreCase(extension)) {
                isAllowed = true;
                break;
            }
        }
        if (!isAllowed) {
            return Result.error("不支持的文件类型: " + extension);
        }

        try {
            // 按日期创建目录
            String dateDir = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
            Path uploadDir = Paths.get(uploadPath, dateDir);
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }

            // 生成新文件名
            String newFilename = UUID.randomUUID().toString().replace("-", "") + "." + extension;
            Path filePath = uploadDir.resolve(newFilename);

            // 使用Files.copy保存文件，避免transferTo的相对路径问题
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
            }

            // 返回相对路径
            String relativePath = "/uploads/" + dateDir + "/" + newFilename;
            log.info("文件上传成功: {} -> {}", originalFilename, filePath.toAbsolutePath());
            return Result.success(relativePath);
        } catch (IOException e) {
            log.error("文件上传失败", e);
            return Result.error("文件上传失败: " + e.getMessage());
        }
    }

    @Operation(summary = "上传图片")
    @PostMapping("/upload/image")
    public Result<String> uploadImage(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return Result.error("请选择要上传的图片");
        }

        // 获取原始文件名
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            return Result.error("文件名不能为空");
        }

        // 获取文件扩展名
        String extension = "";
        int dotIndex = originalFilename.lastIndexOf(".");
        if (dotIndex > 0) {
            extension = originalFilename.substring(dotIndex + 1).toLowerCase();
        }

        // 只允许图片类型
        String[] imageTypes = { "jpg", "jpeg", "png", "gif", "webp" };
        boolean isImage = false;
        for (String type : imageTypes) {
            if (type.equalsIgnoreCase(extension)) {
                isImage = true;
                break;
            }
        }
        if (!isImage) {
            return Result.error("只支持上传图片文件(jpg, jpeg, png, gif, webp)");
        }

        try {
            // 按日期创建目录
            String dateDir = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
            Path uploadDir = Paths.get(uploadPath, "images", dateDir);
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }

            // 生成新文件名
            String newFilename = UUID.randomUUID().toString().replace("-", "") + "." + extension;
            Path filePath = uploadDir.resolve(newFilename);

            // 使用Files.copy保存文件，避免transferTo的相对路径问题
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
            }

            // 返回相对路径
            String relativePath = "/uploads/images/" + dateDir + "/" + newFilename;
            log.info("图片上传成功: {} -> {}", originalFilename, filePath.toAbsolutePath());
            return Result.success(relativePath);
        } catch (IOException e) {
            log.error("图片上传失败", e);
            return Result.error("图片上传失败: " + e.getMessage());
        }
    }

    @Operation(summary = "删除文件")
    @DeleteMapping("/delete")
    public Result<Boolean> delete(@RequestParam("path") String path) {
        if (path == null || path.isEmpty()) {
            return Result.error("文件路径不能为空");
        }

        // 安全检查：防止路径遍历攻击
        if (path.contains("..")) {
            return Result.error("非法的文件路径");
        }

        try {
            // 移除前缀 /uploads/
            String relativePath = path;
            if (relativePath.startsWith("/uploads/")) {
                relativePath = relativePath.substring(9);
            }

            Path filePath = Paths.get(uploadPath, relativePath);
            if (Files.exists(filePath)) {
                Files.delete(filePath);
                return Result.success(true);
            } else {
                return Result.error("文件不存在");
            }
        } catch (IOException e) {
            e.printStackTrace();
            return Result.error("文件删除失败: " + e.getMessage());
        }
    }
}