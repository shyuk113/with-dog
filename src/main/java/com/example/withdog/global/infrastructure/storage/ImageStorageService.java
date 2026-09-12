package com.example.withdog.global.infrastructure.storage;

import com.example.withdog.global.exception.BusinessException;
import com.example.withdog.global.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

@Slf4j
@Component
public class ImageStorageService {

    @Value("${file.upload-dir}")
    private String uploadDir;

    public String store(MultipartFile file, String subDirectory) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_IMAGE);
        }

        String ext = StringUtils.getFilenameExtension(file.getOriginalFilename());
        String storedFilename = UUID.randomUUID() + (ext != null ? "." + ext : "");

        try {
            Path dir = Path.of(uploadDir, subDirectory);
            Files.createDirectories(dir);
            Path target = dir.resolve(storedFilename);
            file.transferTo(target);
            return "/uploads/" + subDirectory + "/" + storedFilename;
        } catch (IOException e) {
            log.error("이미지 저장 실패: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.IMAGE_STORAGE_FAILED);
        }
    }
}
