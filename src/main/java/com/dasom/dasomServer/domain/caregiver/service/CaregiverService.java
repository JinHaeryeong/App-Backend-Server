package com.dasom.dasomServer.domain.caregiver.service;

import com.dasom.dasomServer.domain.caregiver.dto.CaregiverResponse;
import com.dasom.dasomServer.domain.caregiver.entity.Caregiver; // 1. 엔티티 패키지로 변경 확인!
import com.dasom.dasomServer.domain.caregiver.repository.CaregiverImageRepository;
import com.dasom.dasomServer.domain.caregiver.repository.CaregiverRepository;
import com.dasom.dasomServer.infra.storage.ImageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CaregiverService {

    private final CaregiverRepository caregiverRepository;
    private final CaregiverImageRepository caregiverImageRepository;
    private final ImageService imageService;

    @Value("${file.access_url}")
    private String serverBaseUrl;

    /**
     * ID 기반 조회
     */
    public CaregiverResponse getCaregiverDetails(Long caregiverId) {
        // caregiverMapper.findCaregiverById 대신 findById(JPA 기본 메서드) 사용
        return caregiverRepository.findById(caregiverId)
                .map(this::mapToCaregiverResponseDTO)
                .orElse(null);
    }

    /**
     * Login ID 기반 조회
     */
    public CaregiverResponse getCaregiverDetailsForApp(String loginId) {
        // caregiverMapper.findCaregiverByLoginId 대신 리포지토리 메서드 사용
        return caregiverRepository.findByLoginId(loginId)
                .map(this::mapToCaregiverResponseDTO)
                .orElse(null);
    }

    /**
     * 어르신(Silver) 로그인 ID 기반 담당 지원사 조회
     */
    public CaregiverResponse getCaregiverBySilverLoginId(String silverLoginId) {
        // caregiverMapper.findCaregiverBySilverLoginId 대신 리포지토리의 @Query 메서드 사용
        return caregiverRepository.findBySilverLoginId(silverLoginId)
                .map(this::mapToCaregiverResponseDTO)
                .orElse(null);
    }

    /**
     * DTO 매핑 및 URL 구성
     */
    private CaregiverResponse mapToCaregiverResponseDTO(Caregiver caregiver) {

        // 이미지 리포지토리에서 첫 번째 이미지를 가져와 URL 빌드
        String profileImageUrl = caregiverImageRepository.findFirstByCaregiverIdOrderByIdAsc(caregiver.getId())
                .map(image -> buildFullUrl(image.getStoredFileName())) // storedFileName 철자 주의!
                .orElse(null);

        return new CaregiverResponse(
                caregiver.getName(),
                caregiver.getTel(),
                caregiver.getGender(),
                caregiver.getAffiliation(),
                profileImageUrl
        );
    }

    /**
     * URL 결합 헬퍼 메서드 (중복 제거용)
     */
    private String buildFullUrl(String storedFilename) {
        if (storedFilename == null || storedFilename.isEmpty()) return null;

        String relativePath = imageService.getFileUrl(storedFilename);

        String cleanBaseUrl = serverBaseUrl.replaceAll("/+$", "");
        String cleanRelativePath = relativePath.startsWith("/") ? relativePath : "/" + relativePath;
        cleanRelativePath = cleanRelativePath.replaceAll("/+", "/");

        return cleanBaseUrl + cleanRelativePath;
    }
}