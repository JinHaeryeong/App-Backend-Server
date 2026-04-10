package com.dasom.dasomServer.shared.file;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Slf4j
@Service
public class FileStorageService {

    @Value("${file.upload_dir}")
    private String uploadDir;

    @Value("${file.access_url}")
    private String accessUrl;

    public String save(MultipartFile file) throws IOException {
        ensureUploadDirectoryExists();

        String storedFilename = generateUniqueFilename(file.getOriginalFilename());

        Path rootPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        Path targetPath = rootPath.resolve(storedFilename).normalize();

        if (!targetPath.startsWith(rootPath)) {
            throw new IOException("유효하지 않은 파일 경로입니다: " + storedFilename);
        }

        file.transferTo(targetPath.toFile());

        log.info("파일 저장 완료: {}", targetPath);
        return storedFilename;
    }

    public String toUrl(String storedFilename) {
        if (storedFilename == null || storedFilename.isBlank()) {
            return null;
        }
        String base = accessUrl.endsWith("/") ? accessUrl : accessUrl + "/";
        return base + storedFilename;
    }

    private void ensureUploadDirectoryExists() throws IOException {
        File directory = new File(uploadDir);

        if (directory.exists() && !directory.isDirectory()) {
            throw new IOException("업로드 경로가 디렉토리가 아닙니다: " + uploadDir);
        }

        if (!directory.exists() && !directory.mkdirs()) {
            log.error("업로드 디렉토리 생성 실패: {}", uploadDir);
            throw new IOException("파일 저장 경로를 생성할 수 없습니다: " + uploadDir);
        }
    }

    private String generateUniqueFilename(String originalFilename) {
        String extension = extractExtension(
                originalFilename != null ? originalFilename : ""
        );
        return UUID.randomUUID() + extension;
    }

    private String extractExtension(String filename) {
        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex == -1) return ".dat";

        String ext = filename.substring(dotIndex);
        return ext.replaceAll("[^.a-zA-Z0-9]", "");
    }
}