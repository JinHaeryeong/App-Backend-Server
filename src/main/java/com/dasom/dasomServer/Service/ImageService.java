package com.dasom.dasomServer.Service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Service // 💡 UserServiceImpl에 주입되는 파일 처리 클래스
public class ImageService {

    // 💡 application.yml에 정의된 파일 저장 실제 물리 경로 (예: /home/ubuntu/images/)
    @Value("${file.upload-dir}")
    private String uploadDir;

    // 💡 [추가] application.yml에 정의된 클라이언트 접근 URL (예: /images/)
    //    (WebConfig에서 /images/** 요청을 uploadDir로 매핑해야 함)
    @Value("${file.access-url}")
    private String accessUrl;


    /**
     * 💡 [수정됨] 1. UserServiceImpl에서 호출하도록 saveProfileImage -> saveFile로 이름 변경
     */
    public String saveFile(MultipartFile file) throws IOException {

        // 1. 저장 디렉토리가 없으면 생성
        File directory = new File(uploadDir);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        // 2. 고유한 파일 이름 생성 (중복 방지)
        String originalFilename = file.getOriginalFilename();
        // 💡 확장자만 추출 (예: .jpg)
        String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        // 💡 UUID로 고유한 파일명 생성 (예: a1b2c3d4-uuid.jpg)
        String storedFilename = UUID.randomUUID().toString() + extension;

        // 💡 저장될 전체 물리적 경로 (예: /home/ubuntu/images/a1b2c3d4-uuid.jpg)
        String fullPath = uploadDir + storedFilename;

        // 3. 파일 시스템에 실제 파일 저장
        File dest = new File(fullPath);
        file.transferTo(dest);

        // 4. 💡 DB에 저장할 '고유 파일명' (storedFilename) 반환
        return storedFilename;
    }

    /**
     * 💡 [추가됨] 2. UserServiceImpl(authenticateUser)에서 호출할 getFileUrl 추가
     * DB에 저장된 파일명을 실제 접근 URL로 변환
     */
    public String getFileUrl(String storedFilename) {
        if (storedFilename == null) {
            return null;
        }

        // 💡 'accessUrl' (예: /images/)과 'storedFilename' (예: uuid.jpg)을 조합
        //    (최종 반환 예: /images/uuid.jpg)
        return accessUrl + storedFilename;
    }
}