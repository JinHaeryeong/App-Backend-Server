package com.dasom.dasomServer.Service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.IOException;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service // 💡 UserServiceImpl에 주입되는 파일 처리 클래스
public class ImageService {

    private static final Logger log = LoggerFactory.getLogger(ImageService.class);

    // 💡 application.yml에 정의된 파일 저장 실제 물리 경로 (예: /home/ubuntu/images/)
    @Value("${file.upload-dir}")
    private String uploadDir;

    // 💡 [추가] application.yml에 정의된 클라이언트 접근 URL (예: /images/)
    //    (WebConfig에서 /images/** 요청을 uploadDir로 매핑해야 함)
    @Value("${file.access-path}")
    private String accessUrl;


    /**
     * 💡 [수정됨] 1. UserServiceImpl에서 호출하도록 saveProfileImage -> saveFile로 이름 변경
     */
    public String saveFile(MultipartFile file) throws IOException {

        // 1. 저장 디렉토리가 없으면 생성 (경로 안정화 및 권한 확보)
        File directory = new File(uploadDir);
        if (!directory.exists()) {
            if (!directory.mkdirs()) {
                log.error("파일 업로드 디렉토리 생성 실패: {}", uploadDir);
                throw new IOException("파일 저장 경로를 생성할 수 없습니다. 권한 및 경로를 확인하세요.");
            }
        }

        // 2. 고유한 파일 이름 생성 (중복 방지)
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isEmpty()) {
            originalFilename = "default_image_name";
        }

        // 💡 확장자만 추출 (없으면 기본 확장자 사용)
        String extension = "";
        int dotIndex = originalFilename.lastIndexOf(".");
        if (dotIndex != -1) {
            extension = originalFilename.substring(dotIndex);
        } else {
            // 확장자가 없는 파일에 대한 처리
            extension = ".dat";
        }

        String storedFilename = UUID.randomUUID().toString() + extension;

        // 💡 저장될 전체 물리적 경로 (예: C:/uploads/a1b2c3d4-uuid.jpg)
        // [경로 보안 강화] 파일 이름에 "../" 또는 유효하지 않은 문자가 포함되지 않도록 검사하는 것이 좋습니다.
        String fullPath = uploadDir + File.separator + storedFilename;

        // 3. 파일 시스템에 실제 파일 저장
        File dest = new File(fullPath);
        file.transferTo(dest);
        log.info("파일 저장 성공. 경로: {}", fullPath);

        // 4. 💡 DB에 저장할 '고유 파일명' (storedFilename) 반환
        return storedFilename;
    }

    /**
     * 💡 [추가됨] 2. UserServiceImpl(authenticateUser)에서 호출할 getFileUrl 추가
     * DB에 저장된 파일명을 실제 접근 URL로 변환
     */
    public String getFileUrl(String storedFilename) {
        if (storedFilename == null || storedFilename.isEmpty()) {
            return null;
        }

        // 💡 [수정] accessUrl이 "/"로 끝나지 않을 경우 보정하여 URL을 구성
        String sanitizedAccessUrl = accessUrl;
        if (!accessUrl.endsWith("/") && !accessUrl.endsWith("\\")) {
            sanitizedAccessUrl += "/";
        }

        // 💡 'accessUrl' (예: /images/)과 'storedFilename' (예: uuid.jpg)을 조합
        //    (최종 반환 예: /images/uuid.jpg)
        return sanitizedAccessUrl + storedFilename;
    }
}