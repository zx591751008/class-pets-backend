package com.classpets.backend.controller;

import com.classpets.backend.common.ApiResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/upload")
public class FileUploadController {

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    @Value("${file.max-image-size-bytes:524288}")
    private long maxImageSizeBytes;

    private static final Map<String, String> ALLOWED_MIME_TO_EXT = new HashMap<>();

    static {
        ALLOWED_MIME_TO_EXT.put("image/jpeg", ".jpg");
        ALLOWED_MIME_TO_EXT.put("image/png", ".png");
        ALLOWED_MIME_TO_EXT.put("image/webp", ".webp");
    }

    @PostMapping("/image")
    public ApiResponse<String> uploadImage(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ApiResponse.fail(400, "文件不能为空");
        }
        if (file.getSize() > maxImageSizeBytes) {
            return ApiResponse.fail(400, "图片大小超过限制，请上传512KB以内图片");
        }

        String contentType = file.getContentType();
        if (contentType == null) {
            return ApiResponse.fail(400, "文件类型无效，仅支持 JPG/PNG/WEBP");
        }
        String normalizedContentType = contentType.toLowerCase(Locale.ROOT).trim();
        String extension = ALLOWED_MIME_TO_EXT.get(normalizedContentType);
        if (extension == null) {
            return ApiResponse.fail(400, "文件类型不支持，仅支持 JPG/PNG/WEBP");
        }

        if (!isValidImageContent(file)) {
            return ApiResponse.fail(400, "文件内容无效，请上传正确的图片文件");
        }

        try {
            // Ensure upload directory exists (avatars subfolder)
            File directory = new File(uploadDir, "avatars");
            if (!directory.exists() && !directory.mkdirs()) {
                return ApiResponse.fail(500, "创建上传目录失败");
            }

            // Generate unique filename
            String newFilename = UUID.randomUUID().toString() + extension;

            // Save file locally
            Path filePath = new File(directory, newFilename).toPath().normalize();
            if (!filePath.startsWith(directory.toPath().normalize())) {
                return ApiResponse.fail(400, "非法文件路径");
            }
            try (InputStream inputStream = file.getInputStream()) {
                java.nio.file.Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
            }

            // Return accessible URL path
            String fileUrl = "/uploads/avatars/" + newFilename;
            return ApiResponse.ok(fileUrl);

        } catch (IOException e) {
            return ApiResponse.fail(500, "文件上传失败，请稍后重试");
        }
    }

    private boolean isValidImageContent(MultipartFile file) {
        try (InputStream input = file.getInputStream()) {
            BufferedImage image = ImageIO.read(input);
            return image != null && image.getWidth() > 0 && image.getHeight() > 0;
        } catch (IOException e) {
            return false;
        }
    }
}
