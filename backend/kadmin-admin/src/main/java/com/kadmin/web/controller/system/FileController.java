package com.kadmin.web.controller.system;

import com.kadmin.common.Result;
import com.kadmin.common.annotation.RequiresPermission;
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
    private final com.kadmin.hr.mapper.EmployeeMapper employeeMapper;

    @Value("${file.upload.allowed-types:jpg,jpeg,png,gif,pdf,doc,docx,xls,xlsx}")
    private String allowedTypes;

    private static final String[] IMAGE_TYPES = { "jpg", "jpeg", "png", "gif", "webp" };

    private String getUploadPath() {
        return fileConfigService.getAbsolutePath(FileConfigService.KEY_UPLOAD_BASE);
    }

    /**
     * 纯移动端员工（仅 ROLE_EMPLOYEE）判定：与 PermissionAspect 的约定一致。
     * 通用文档上传（pdf/doc/xls）属于管理侧操作，移动端仅需图片上传（动态/聊天）。
     */
    private boolean isPureMobileEmployee() {
        com.kadmin.common.security.LoginUser loginUser = com.kadmin.common.utils.SecurityUtils.getCurrentUser();
        return loginUser != null && loginUser.getRoles() != null
                && loginUser.getRoles().size() == 1
                && loginUser.getRoles().contains("ROLE_EMPLOYEE");
    }

    /**
     * 员工头像归属校验：仅允许为自己上传；管理员或持有 hr:employee:edit 权限的管理侧用户可为任意员工上传
     */
    private String checkAvatarOwnership(String employeeNo) {
        com.kadmin.common.security.LoginUser loginUser = com.kadmin.common.utils.SecurityUtils.getCurrentUser();
        if (loginUser == null) {
            return "请先登录";
        }
        boolean admin = com.kadmin.common.utils.SecurityUtils.isAdmin();
        boolean hrManager = loginUser.getPermissions() != null && (loginUser.getPermissions().contains("hr:employee:edit")
                || loginUser.getPermissions().contains("hr:employee:*")
                || loginUser.getPermissions().contains("*:*:*"));
        if (admin || hrManager) {
            return null;
        }
        if (loginUser.getEmployeeId() == null) {
            return "无权为其他员工上传头像";
        }
        com.kadmin.hr.domain.HrEmployee self = employeeMapper.selectById(loginUser.getEmployeeId());
        if (self == null || !employeeNo.equals(self.getEmployeeNo())) {
            return "只能上传自己的头像";
        }
        return null;
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

    /**
     * 通用文档魔数校验：按扩展名校验文件内容（pdf/doc/docx/xls/xlsx，图片类型见 hasImageMagicBytes）
     */
    private boolean matchesMagicBytes(MultipartFile file, String extension) {
        if (extension == null) {
            return false;
        }
        switch (extension.toLowerCase()) {
            case "jpg":
            case "jpeg":
            case "png":
            case "gif":
            case "webp":
                return hasImageMagicBytes(file);
            case "pdf":
                return hasExactHeader(file, new byte[] {'%', 'P', 'D', 'F'});
            case "doc":
            case "xls":
                // 旧版 Office 为 OLE2 复合文档：D0 CF 11 E0
                return hasExactHeader(file, new byte[] {(byte) 0xD0, (byte) 0xCF, 0x11, (byte) 0xE0});
            case "docx":
            case "xlsx":
                // 新版 Office 为 ZIP 容器：PK\x03\x04
                return hasExactHeader(file, new byte[] {'P', 'K', 0x03, 0x04});
            default:
                return false;
        }
    }

    private boolean hasExactHeader(MultipartFile file, byte[] expected) {
        try (InputStream is = file.getInputStream()) {
            byte[] header = new byte[expected.length];
            int read = is.read(header);
            if (read < expected.length) {
                return false;
            }
            for (int i = 0; i < expected.length; i++) {
                if (header[i] != expected[i]) {
                    return false;
                }
            }
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    @Operation(summary = "上传文件")
    @PostMapping("/upload")
    public Result<String> upload(@RequestParam("file") MultipartFile file) {
        // 文档上传属于管理侧操作：纯移动端员工不允许上传任意文档（图片走 /upload/image）
        if (isPureMobileEmployee()) {
            return Result.error("无权上传该类型文件");
        }
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
        if (!matchesMagicBytes(file, extension))
            return Result.error("文件内容与扩展名不符或已损坏，请上传真实的 " + extension + " 文件");

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
        if (!hasImageMagicBytes(file))
            return Result.error("图片内容不合法或已损坏");

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
        String ownershipError = checkAvatarOwnership(employeeNo);
        if (ownershipError != null)
            return Result.error(ownershipError);

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
    @RequiresPermission("hr:employee:edit")
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
    @RequiresPermission("hr:employee:edit")
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
    @RequiresPermission("hr:employee:edit")
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
    @RequiresPermission("hr:employee:edit")
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
    @RequiresPermission("hr:employee:edit")
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
