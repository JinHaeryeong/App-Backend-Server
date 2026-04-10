package com.dasom.dasomServer.shared.file;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
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
        File dest = new File(uploadDir + File.separator + storedFilename);
        file.transferTo(dest);

        log.info("파일 저장 완료: {}", dest.getAbsolutePath());
        return storedFilename;
    }

    /**
     * DB에 저장된 파일명을 클라이언트 접근 URL로 변환
     *
     * @return 접근 URL, 파일명이 없으면 null
     */
    public String toUrl(String storedFilename) {
        if (storedFilename == null || storedFilename.isBlank()) {
            return null;
        }
        String base = accessUrl.endsWith("/") ? accessUrl : accessUrl + "/";
        return base + storedFilename;
    }

    private void ensureUploadDirectoryExists() throws IOException {
        File directory = new File(uploadDir);
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
        return dotIndex != -1 ? filename.substring(dotIndex) : ".dat";
    }
}
