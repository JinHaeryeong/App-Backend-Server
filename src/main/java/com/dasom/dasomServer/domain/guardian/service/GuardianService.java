package com.dasom.dasomServer.domain.guardian.service;

import com.dasom.dasomServer.domain.guardian.mapper.GuardianMapper;
import com.dasom.dasomServer.domain.user.mapper.UserMapper;
import com.dasom.dasomServer.domain.guardian.dto.Guardian;
import com.dasom.dasomServer.domain.guardian.dto.GuardianResponseDTO;
import com.dasom.dasomServer.infra.storage.ImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GuardianService {

    private final GuardianMapper guardianMapper;
    private final ImageService imageService;

    @Value("${file.access_url}")
    private String serverBaseUrl;


    public List<GuardianResponseDTO> getGuardiansForApp(String silverId) {

        List<Guardian> guardians = guardianMapper.findGuardiansBySilverId(silverId);

        return guardians.stream()
                .map(guardian -> {

                    Long guardianId = guardian.getId();
                    String storedFilename = guardianMapper.findGuardianStoredFilenameByGuardianId(guardianId);

                    String profileImageUrl = null;
                    if (storedFilename != null && !storedFilename.isEmpty()) {

                        // c) ImageService에서 깨끗한 상대 경로를 얻습니다. (예: /uploads/uuid.jpg)
                        String relativePath = imageService.getFileUrl(storedFilename);

                        // d) [!! URL 구성 로직 수정: 중복 슬래시 및 경로 방지 !!]

                        // 1. serverBaseUrl의 끝 슬래시 제거 (예: http://ip:port)
                        String cleanBaseUrl = serverBaseUrl.endsWith("/")
                                ? serverBaseUrl.substring(0, serverBaseUrl.length() - 1)
                                : serverBaseUrl;

                        // 2. relativePath의 시작 슬래시가 두 개 이상일 경우 하나만 남깁니다.
                        //    (예: //uploads/ -> /uploads/)
                        String cleanRelativePath = relativePath.replaceAll("/+", "/");

                        // 3. 최종 절대 URL 구성: http://ip:port + /uploads/uuid.jpg
                        //    만약 ImageService가 'uploads/uuid.jpg'를 반환하도록 수정되었다면 이 로직이 맞습니다.
                        profileImageUrl = cleanBaseUrl + cleanRelativePath;

                        // 🚨 디버깅을 위해 최종 URL 로그 출력
                        System.out.println("FINAL Guardian Image URL: " + profileImageUrl);
                    }

                    return new GuardianResponseDTO(
                            guardian.getName(),
                            guardian.getTel(),
                            guardian.getRelationship(),
                            guardian.getAddress(),
                            profileImageUrl
                    );
                })
                .collect(Collectors.toList());
    }
}