package com.dasom.dasomServer.Service;

import com.dasom.dasomServer.DAO.UserDAO;
import com.dasom.dasomServer.DTO.Caregiver; // 요양보호사 엔티티/DTO
import com.dasom.dasomServer.DTO.CaregiverlResponseDTO; // 최종 응답 DTO
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CaregiverService {

    private final UserDAO userDAO;
    private final ImageService imageService;

    // 서버 기본 URL (예: http://ip:port)을 application.properties에서 주입받음
    @Value("${file.access_url}")
    private String serverBaseUrl;

    @Autowired // 의존성 주입 (Dependency Injection)
    public CaregiverService(UserDAO userDAO, ImageService imageService) {
        this.userDAO = userDAO;
        this.imageService = imageService;
    }

    // ------------------------- ID 기반 조회 (Controller 오류 해결용) -------------------------

    /**
     * Primary Key ID를 기반으로 요양보호사 정보를 조회하고 DTO를 반환합니다.
     */
    public CaregiverlResponseDTO getCaregiverDetails(Long caregiverId) {

        // 1. DB에서 요양보호사 정보를 ID로 조회 (UserDAO.findCaregiverById 호출)
        Caregiver caregiver = userDAO.findCaregiverById(caregiverId);

        if (caregiver == null) {
            return null; // 데이터가 없으면 null 반환 (Controller에서 404 처리 유도)
        }

        // 2. DTO 변환 및 URL 구성 로직을 내부 메서드에 위임
        return mapToCaregiverResponseDTO(caregiver);
    }

    // ------------------------- Login ID 기반 조회 -------------------------

    /**
     * 특정 로그인 ID를 가진 요양보호사(Caregiver)의 상세 정보를 조회하고 이미지 URL을 구성합니다.
     */
    public CaregiverlResponseDTO getCaregiverDetailsForApp(String loginId) {

        // 1. DB에서 요양보호사 정보를 로그인 ID로 조회 (UserDAO.findCaregiverByLoginId 호출)
        Caregiver caregiver = userDAO.findCaregiverByLoginId(loginId);

        if (caregiver == null) {
            return null;
        }

        // 2. DTO 변환 및 URL 구성 로직을 내부 메서드에 위임
        return mapToCaregiverResponseDTO(caregiver);
    }

    // ------------------- 🚨 [새로 추가된 메서드] -------------------

    /**
     * 보호대상자(Silver)의 로그인 ID를 기반으로
     * 담당 요양보호사(Caregiver)의 상세 정보를 조회합니다.
     * (안드로이드 YoyangsaActivity에서 호출)
     */
    public CaregiverlResponseDTO getCaregiverBySilverLoginId(String silverLoginId) {

        // 1. DB에서 보호대상자 ID를 이용해 요양보호사 정보를 조회
        //    (UserDAO.findCaregiverBySilverLoginId 호출)
        Caregiver caregiver = userDAO.findCaregiverBySilverLoginId(silverLoginId);

        if (caregiver == null) {
            return null; // 배정된 요양사가 없거나, silverLoginId가 잘못된 경우
        }

        // 2. DTO 변환 및 URL 구성 로직 재사용
        return mapToCaregiverResponseDTO(caregiver);
    }

    // ------------------------- 내부 DTO 매핑 및 URL 구성 로직 -------------------------

    /**
     * Caregiver 엔티티를 받아 이미지 URL을 구성하고 최종 응답 DTO로 매핑합니다.
     */
    private CaregiverlResponseDTO mapToCaregiverResponseDTO(Caregiver caregiver) {

        // 1. 이미지 리스트에서 첫 번째 이미지의 저장된 파일 이름을 안전하게 추출
        String storedFilename = Optional.ofNullable(caregiver.getImages())
                .filter(list -> !list.isEmpty())
                .map(list -> list.get(0).getStoredFilename())
                .orElse(null);

        String profileImageUrl = null;

        if (storedFilename != null && !storedFilename.isEmpty()) {

            // ImageService를 통해 이미지 파일의 상대 경로 획득
            String relativePath = imageService.getFileUrl(storedFilename);

            // 서버 기본 URL과 상대 경로를 안전하게 결합하여 최종 접근 URL 생성
            String cleanBaseUrl = Optional.ofNullable(serverBaseUrl)
                    .map(url -> url.replaceAll("/+$", "")) // 기본 URL의 끝 슬래시 제거
                    .orElse("");

            String cleanRelativePath = Optional.ofNullable(relativePath)
                    .map(path -> path.startsWith("/") ? path : "/" + path) // 상대 경로 시작 슬래시 확인
                    .orElse("");

            profileImageUrl = cleanBaseUrl + cleanRelativePath;
        }

        // 2. DTO 생성 및 반환 (Controller로 전달)
        return new CaregiverlResponseDTO(
                caregiver.getName(),
                caregiver.getTel(),
                caregiver.getGender(),
                caregiver.getAffiliation(),
                profileImageUrl // 이 값이 최종 응답 DTO의 storedFilename 필드에 들어감
        );
    }
}