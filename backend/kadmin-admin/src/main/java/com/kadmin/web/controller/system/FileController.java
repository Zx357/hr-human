package com.kadmin.web.controller.system;

import com.kadmin.common.Result;
import com.kadmin.system.service.FileConfigService;
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

    /**
     * 工号净化：仅允许字母/数字/汉字/下划线/连字符，防止拼接文件名产生路径遍历
     */
    private boolean isValidEmployeeNo(String employeeNo) {
        return employeeNo != null && employeeNo.matches("[\\w\\-\u4e00-\u9fa5]{1,64}");
    }

    /**
     * 图片魔数校验：防止伪装扩展名上传任意内容（jpg/png/gif/webp）
     */
    private boolean hasImageMagicBytes(MultipartFile file) {
        try (InputStream is = file.getInputStream()) {
            byte[] header = new byte[12];
            int read = is.read(header);
            if (read < 4) {
                return false;
            }
            // JPEG: FF D8 FF
            if ((header[0] & 0xFF) == 0xFF && (header[1] & 0xFF) == 0xD8 && (header[2] & 0xFF) == 0xFF) {
                return true;
            }
            // PNG: 89 50 4E 47
            if ((header[0] & 0xFF) == 0x89 && header[1] == 0x50 && header[2] == 0x4E && header[3] == 0x47) {
                return true;
            }
            // GIF: GIF8
            if (header[0] == 'G' && header[1] == 'I' && header[2] == 'F' && header[3] == '8') {
                return true;
            }
            // WEBP: RIFF....WEBP
            if (header[0] == 'R' && header[1] == 'I' && header[2] == 'F' && header[3] == 'F'
                    && header[8] == 'W' && header[9] == 'E' && header[10] == 'B' && header[11] == 'P') {
                return true;
            }
            return false;
        } catch (IOException e) {
            return false;
        }
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
        if (!isValidEmployeeNo(employeeNo))
            return Result.error("员工工号不合法");

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null)
            return Result.error("文件名不能为空");

        String extension = getExtension(originalFilename);
        if (!isImageType(extension))
            return Result.error("只支持上传图片文件(jpg, jpeg, png, gif, webp)");
        if (!hasImageMagicBytes(file))
            return Result.error("图片内容不合法或已损坏");

        try {
            String avatarPath = fileConfigService.getAbsolutePath(FileConfigService.KEY_AVATAR);
            Path employeePhotoDir = Paths.get(avatarPath);
            if (!Files.exists(employeePhotoDir))
                Files.createDirectories(employeePhotoDir);

            // UUID命名防止通过工号枚举下载他人证件照
            String newFilename = java.util.UUID.randomUUID().toString().replace("-", "") + "." + extension;
            Path filePath = employeePhotoDir.resolve(newFilename);

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
        if (!isValidEmployeeNo(employeeNo))
            return Result.error("员工工号不合法");
        if (type == null || (!type.equals("front") && !type.equals("back")))
            return Result.error("身份证类型参数错误，必须是 front 或 back");

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null)
            return Result.error("文件名不能为空");

        String extension = getExtension(originalFilename);
        if (!isImageType(extension))
            return Result.error("只支持上传图片文件(jpg, jpeg, png, gif, webp)");
        if (!hasImageMagicBytes(file))
            return Result.error("图片内容不合法或已损坏");

        try {
            String configKey = type.equals("front") ? FileConfigService.KEY_ID_FRONT : FileConfigService.KEY_ID_BACK;
            String idCardPath = fileConfigService.getAbsolutePath(configKey);
            Path idCardDir = Paths.get(idCardPath);
            if (!Files.exists(idCardDir))
                Files.createDirectories(idCardDir);

            String folderName = type.equals("front") ? "id_card_front" : "id_card_back";
            // UUID命名防止通过工号枚举下载他人证件照
            String newFilename = java.util.UUID.randomUUID().toString().replace("-", "") + "." + extension;
            Path filePath = idCardDir.resolve(newFilename);

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
        if (!isValidEmployeeNo(employeeNo))
            return Result.error("员工工号不合法");

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null)
            return Result.error("文件名不能为空");

        String extension = getExtension(originalFilename);
        if (!isImageType(extension))
            return Result.error("只支持上传图片文件(jpg, jpeg, png, gif, webp)");
        if (!hasImageMagicBytes(file))
            return Result.error("图片内容不合法或已损坏");

        try {
            String contractPath = fileConfigService.getAbsolutePath(FileConfigService.KEY_CONTRACT_PHOTO);
            Path contractDir = Paths.get(contractPath);
            if (!Files.exists(contractDir))
                Files.createDirectories(contractDir);

            String newFilename = java.util.UUID.randomUUID().toString().replace("-", "") + "_" + System.currentTimeMillis() + "." + extension;
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
        if (!isValidEmployeeNo(employeeNo))
            return Result.error("员工工号不合法");

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null)
            return Result.error("文件名不能为空");

        String extension = getExtension(originalFilename);
        if (!isImageType(extension))
            return Result.error("只支持上传图片文件(jpg, jpeg, png, gif, webp)");
        if (!hasImageMagicBytes(file))
            return Result.error("图片内容不合法或已损坏");

        try {
            String diplomaPath = fileConfigService.getAbsolutePath(FileConfigService.KEY_DIPLOMA_PHOTO);
            Path diplomaDir = Paths.get(diplomaPath);
            if (!Files.exists(diplomaDir))
                Files.createDirectories(diplomaDir);

            String newFilename = java.util.UUID.randomUUID().toString().replace("-", "") + "_" + System.currentTimeMillis() + "." + extension;
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
        if (!isValidEmployeeNo(employeeNo))
            return Result.error("员工工号不合法");

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null)
            return Result.error("文件名不能为空");

        String extension = getExtension(originalFilename);
        if (!isImageType(extension))
            return Result.error("只支持上传图片文件(jpg, jpeg, png, gif, webp)");
        if (!hasImageMagicBytes(file))
            return Result.error("图片内容不合法或已损坏");

        try {
            String certPath = fileConfigService.getAbsolutePath(FileConfigService.KEY_CERT_PHOTO);
            Path certDir = Paths.get(certPath);
            if (!Files.exists(certDir))
                Files.createDirectories(certDir);

            String newFilename = java.util.UUID.randomUUID().toString().replace("-", "") + "_" + System.currentTimeMillis() + "." + extension;
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

            // 归一化后必须仍位于上传根目录内，防止绝对路径/符号链接逃逸
            Path baseDir = Paths.get(getUploadPath()).toAbsolutePath().normalize();
            Path filePath = baseDir.resolve(relativePath).normalize();
            if (!filePath.startsWith(baseDir)) {
                return Result.error("非法的文件路径");
            }
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
