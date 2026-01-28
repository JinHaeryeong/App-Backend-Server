package com.dasom.dasomServer.domain.guardian.service;

import com.dasom.dasomServer.domain.guardian.dto.GuardianResponse;
import com.dasom.dasomServer.domain.guardian.entity.Guardian;
import com.dasom.dasomServer.domain.guardian.repository.GuardianImageRepository;
import com.dasom.dasomServer.domain.guardian.repository.GuardianRepository;
import com.dasom.dasomServer.infra.storage.ImageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true) // 읽기 전용 성능 최적화
public class GuardianService {

    private final GuardianRepository guardianRepository;
    private final GuardianImageRepository guardianImageRepository; // 새로 만든 리포지토리
    private final ImageService imageService;

    @Value("${file.access_url}")
    private String serverBaseUrl;

    public List<GuardianResponse> getGuardiansForApp(String silverId) {
        // MyBatis 매퍼 대신 JPA 리포지토리 사용
        List<Guardian> guardians = guardianRepository.findBySilverLoginId(silverId);

        return guardians.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private GuardianResponse convertToDTO(Guardian guardian) {
        // 이미지를 찾아서 => 있으면 URL로 변환하고 => 없으면 null 반환
        String profileImageUrl = guardianImageRepository.findFirstByGuardianIdOrderByIdAsc(guardian.getId())
                .map(image -> buildFullUrl(image.getStoredFileName()))
                .orElse(null); // 이미지가 없으면 null

        return new GuardianResponse(
                guardian.getName(),
                guardian.getTel(),
                guardian.getRelationship(),
                guardian.getAddress(),
                profileImageUrl
        );
    }

    private String buildFullUrl(String storedFilename) {
        String relativePath = imageService.getFileUrl(storedFilename);

        // URL 정리 로직 (기존 로직 유지하되 가독성 조아짐)
        String cleanBaseUrl = serverBaseUrl.endsWith("/")
                ? serverBaseUrl.substring(0, serverBaseUrl.length() - 1)
                : serverBaseUrl;

        String cleanRelativePath = relativePath.startsWith("/") ? relativePath : "/" + relativePath;
        cleanRelativePath = cleanRelativePath.replaceAll("/+", "/");

        return cleanBaseUrl + cleanRelativePath;
    }
}