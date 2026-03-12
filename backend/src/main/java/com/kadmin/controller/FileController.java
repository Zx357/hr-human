package com.kadmin.controller;

import com.kadmin.common.Result;
import com.kadmin.service.FileConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
@Slf4j
@Tag(name = "文件管理")
@RestController
@RequestMapping("/file")
@RequiredArgsConstructor
public class FileController {

    private final FileConfigService fileConfigService;

    @Value("${file.upload.allowed-types:jpg,jpeg,png,gif,pdf,doc,docx,xls,xlsx}")
    private String allowedTypes;

    private static final String[] IMAGE_TYPES = { "jpg", "jpeg", "png", "gif", "webp" };

    private String getUploadPath() {
        return fileConfigService.getAbsolutePath(FileConfigService.KEY_UPLOAD_BASE);
    }

    private String getExtension(String filename) {
        if (filename == null)
            return "";
        int dotIndex = filename.lastIndexOf(".");
        return dotIndex > 0 ? filename.substring(dotIndex + 1).toLowerCase() : "";
    }

    private boolean isImageType(String extension) {
        for (String type : IMAGE_TYPES) {
            if (type.equalsIgnoreCase(extension))
                return true;
        }
        return false;
    }

    @Operation(summary = "上传文件")
    @PostMapping("/upload")
    public Result<String> upload(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty())
            return Result.error("请选择要上传的文件");

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null)
            return Result.error("文件名不能为空");

        String extension = getExtension(originalFilename);

        String[] allowedTypeArray = allowedTypes.split(",");
        boolean isAllowed = false;
        for (String type : allowedTypeArray) {
            if (type.trim().equalsIgnoreCase(extension)) {
                isAllowed = true;
                break;
            }
        }
        if (!isAllowed)
            return Result.error("不支持的文件类型: " + extension);

        try {
            String dateDir = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
            Path uploadDir = Paths.get(getUploadPath(), dateDir);
            if (!Files.exists(uploadDir))
                Files.createDirectories(uploadDir);

            String newFilename = UUID.randomUUID().toString().replace("-", "") + "." + extension;
            Path filePath = uploadDir.resolve(newFilename);

            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
            }

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
        if (file.isEmpty())
            return Result.error("请选择要上传的图片");

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null)
            return Result.error("文件名不能为空");

        String extension = getExtension(originalFilename);
        if (!isImageType(extension))
            return Result.error("只支持上传图片文件(jpg, jpeg, png, gif, webp)");

        try {
            String dateDir = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
            Path uploadDir = Paths.get(getUploadPath(), "images", dateDir);
            if (!Files.exists(uploadDir))
                Files.createDirectories(uploadDir);

            String newFilename = UUID.randomUUID().toString().replace("-", "") + "." + extension;
            Path filePath = uploadDir.resolve(newFilename);

            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
            }

            String relativePath = "/uploads/images/" + dateDir + "/" + newFilename;
            log.info("图片上传成功: {} -> {}", originalFilename, filePath.toAbsolutePath());
            return Result.success(relativePath);
        } catch (IOException e) {
            log.error("图片上传失败", e);
            return Result.error("图片上传失败: " + e.getMessage());
        }
    }

    @Operation(summary = "上传员工头像")
    @PostMapping("/upload/employee/avatar")
    public Result<String> uploadEmployeeAvatar(
            @RequestParam("file") MultipartFile file,
            @RequestParam("employeeNo") String employeeNo) {
        if (file.isEmpty())
            return Result.error("请选择要上传的图片");
        if (employeeNo == null || employeeNo.trim().isEmpty())
            return Result.error("员工工号不能为空");

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null)
            return Result.error("文件名不能为空");

        String extension = getExtension(originalFilename);
        if (!isImageType(extension))
            return Result.error("只支持上传图片文件(jpg, jpeg, png, gif, webp)");

        try {
            String avatarPath = fileConfigService.getAbsolutePath(FileConfigService.KEY_AVATAR);
            Path employeePhotoDir = Paths.get(avatarPath);
            if (!Files.exists(employeePhotoDir))
                Files.createDirectories(employeePhotoDir);

            String newFilename = employeeNo + "." + extension;
            Path filePath = employeePhotoDir.resolve(newFilename);

            if (Files.exists(filePath)) {
                Files.delete(filePath);
                log.info("删除旧头像: {}", filePath.toAbsolutePath());
            }

            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
            }

            String relativePath = "/employee_photo/" + newFilename;
            log.info("员工头像上传成功: 工号={}, 文件={}", employeeNo, filePath.toAbsolutePath());
            return Result.success(relativePath);
        } catch (IOException e) {
            log.error("员工头像上传失败", e);
            return Result.error("员工头像上传失败: " + e.getMessage());
        }
    }

    @Operation(summary = "上传员工身份证照片")
    @PostMapping("/upload/employee/idcard")
    public Result<String> uploadEmployeeIdCard(
            @RequestParam("file") MultipartFile file,
            @RequestParam("employeeNo") String employeeNo,
            @RequestParam("type") String type) {
        if (file.isEmpty())
            return Result.error("请选择要上传的图片");
        if (employeeNo == null || employeeNo.trim().isEmpty())
            return Result.error("员工工号不能为空");
        if (type == null || (!type.equals("front") && !type.equals("back")))
            return Result.error("身份证类型参数错误，必须是 front 或 back");

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null)
            return Result.error("文件名不能为空");

        String extension = getExtension(originalFilename);
        if (!isImageType(extension))
            return Result.error("只支持上传图片文件(jpg, jpeg, png, gif, webp)");

        try {
            String configKey = type.equals("front") ? FileConfigService.KEY_ID_FRONT : FileConfigService.KEY_ID_BACK;
            String idCardPath = fileConfigService.getAbsolutePath(configKey);
            Path idCardDir = Paths.get(idCardPath);
            if (!Files.exists(idCardDir))
                Files.createDirectories(idCardDir);

            String folderName = type.equals("front") ? "id_card_front" : "id_card_back";
            String newFilename = employeeNo + "." + extension;
            Path filePath = idCardDir.resolve(newFilename);

            if (Files.exists(filePath)) {
                Files.delete(filePath);
                log.info("删除旧身份证照片: {}", filePath.toAbsolutePath());
            }

            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
            }

            String relativePath = "/" + folderName + "/" + newFilename;
            log.info("员工身份证照片上传成功: 工号={}, 类型={}, 文件={}", employeeNo, type, filePath.toAbsolutePath());
            return Result.success(relativePath);
        } catch (IOException e) {
            log.error("员工身份证照片上传失败", e);
            return Result.error("员工身份证照片上传失败: " + e.getMessage());
        }
    }

    @Operation(summary = "上传合同照片")
    @PostMapping("/upload/contract")
    public Result<String> uploadContractPhoto(
            @RequestParam("file") MultipartFile file,
            @RequestParam("employeeNo") String employeeNo) {
        if (file.isEmpty())
            return Result.error("请选择要上传的图片");
        if (employeeNo == null || employeeNo.trim().isEmpty())
            return Result.error("员工工号不能为空");

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null)
            return Result.error("文件名不能为空");

        String extension = getExtension(originalFilename);
        if (!isImageType(extension))
            return Result.error("只支持上传图片文件(jpg, jpeg, png, gif, webp)");

        try {
            String contractPath = fileConfigService.getAbsolutePath(FileConfigService.KEY_CONTRACT_PHOTO);
            Path contractDir = Paths.get(contractPath);
            if (!Files.exists(contractDir))
                Files.createDirectories(contractDir);

            String newFilename = employeeNo + "_" + System.currentTimeMillis() + "." + extension;
            Path filePath = contractDir.resolve(newFilename);

            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
            }

            String relativePath = "/contract_photo/" + newFilename;
            log.info("合同照片上传成功: 工号={}, 文件={}", employeeNo, filePath.toAbsolutePath());
            return Result.success(relativePath);
        } catch (IOException e) {
            log.error("合同照片上传失败", e);
            return Result.error("合同照片上传失败: " + e.getMessage());
        }
    }

    @Operation(summary = "上传毕业证照片")
    @PostMapping("/upload/diploma")
    public Result<String> uploadDiplomaPhoto(
            @RequestParam("file") MultipartFile file,
            @RequestParam("employeeNo") String employeeNo) {
        if (file.isEmpty())
            return Result.error("请选择要上传的图片");
        if (employeeNo == null || employeeNo.trim().isEmpty())
            return Result.error("员工工号不能为空");

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null)
            return Result.error("文件名不能为空");

        String extension = getExtension(originalFilename);
        if (!isImageType(extension))
            return Result.error("只支持上传图片文件(jpg, jpeg, png, gif, webp)");

        try {
            String diplomaPath = fileConfigService.getAbsolutePath(FileConfigService.KEY_DIPLOMA_PHOTO);
            Path diplomaDir = Paths.get(diplomaPath);
            if (!Files.exists(diplomaDir))
                Files.createDirectories(diplomaDir);

            String newFilename = employeeNo + "_" + System.currentTimeMillis() + "." + extension;
            Path filePath = diplomaDir.resolve(newFilename);

            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
            }

            String relativePath = "/diploma_photo/" + newFilename;
            log.info("毕业证照片上传成功: 工号={}, 文件={}", employeeNo, filePath.toAbsolutePath());
            return Result.success(relativePath);
        } catch (IOException e) {
            log.error("毕业证照片上传失败", e);
            return Result.error("毕业证照片上传失败: " + e.getMessage());
        }
    }

    @Operation(summary = "上传证书照片")
    @PostMapping("/upload/certificate")
    public Result<String> uploadCertPhoto(
            @RequestParam("file") MultipartFile file,
            @RequestParam("employeeNo") String employeeNo) {
        if (file.isEmpty())
            return Result.error("请选择要上传的图片");
        if (employeeNo == null || employeeNo.trim().isEmpty())
            return Result.error("员工工号不能为空");

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null)
            return Result.error("文件名不能为空");

        String extension = getExtension(originalFilename);
        if (!isImageType(extension))
            return Result.error("只支持上传图片文件(jpg, jpeg, png, gif, webp)");

        try {
            String certPath = fileConfigService.getAbsolutePath(FileConfigService.KEY_CERT_PHOTO);
            Path certDir = Paths.get(certPath);
            if (!Files.exists(certDir))
                Files.createDirectories(certDir);

            String newFilename = employeeNo + "_" + System.currentTimeMillis() + "." + extension;
            Path filePath = certDir.resolve(newFilename);

            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
            }

            String relativePath = "/cert_photo/" + newFilename;
            log.info("证书照片上传成功: 工号={}, 文件={}", employeeNo, filePath.toAbsolutePath());
            return Result.success(relativePath);
        } catch (IOException e) {
            log.error("证书照片上传失败", e);
            return Result.error("证书照片上传失败: " + e.getMessage());
        }
    }

    @Operation(summary = "删除文件")
    @DeleteMapping("/delete")
    public Result<Boolean> delete(@RequestParam("path") String path) {
        if (path == null || path.isEmpty())
            return Result.error("文件路径不能为空");
        if (path.contains(".."))
            return Result.error("非法的文件路径");

        try {
            String relativePath = path;
            if (relativePath.startsWith("/uploads/")) {
                relativePath = relativePath.substring(9);
            }

            Path filePath = Paths.get(getUploadPath(), relativePath);
            if (Files.exists(filePath)) {
                Files.delete(filePath);
                return Result.success(true);
            } else {
                return Result.error("文件不存在");
            }
        } catch (IOException e) {
            log.error("文件删除失败", e);
            return Result.error("文件删除失败: " + e.getMessage());
        }
    }
}
