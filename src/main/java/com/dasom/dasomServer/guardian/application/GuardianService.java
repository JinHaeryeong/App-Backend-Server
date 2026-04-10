package com.dasom.dasomServer.guardian.application;

import com.dasom.dasomServer.guardian.presentation.dto.GuardianResponse;
import com.dasom.dasomServer.guardian.domain.Guardian;
import com.dasom.dasomServer.guardian.domain.GuardianRepository;
import com.dasom.dasomServer.shared.file.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GuardianService {

    private final GuardianRepository guardianRepository;
    private final FileStorageService fileStorageService;

    public List<GuardianResponse> getGuardiansForApp(String silverId) {
        return guardianRepository.findBySilverLoginId(silverId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private GuardianResponse toResponse(Guardian guardian) {
        String fileName = guardian.getProfileImageUrl();
        String fullUrl = (fileName != null) ? fileStorageService.toUrl(fileName) : null;

        return GuardianResponse.of(guardian, fullUrl);
    }
}