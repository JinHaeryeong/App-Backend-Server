package com.dasom.dasomServer.caregiver.application;

import com.dasom.dasomServer.caregiver.domain.Caregiver;
import com.dasom.dasomServer.caregiver.domain.CaregiverRepository;
import com.dasom.dasomServer.caregiver.presentation.dto.CaregiverResponse;
import com.dasom.dasomServer.shared.file.FileStorageService;
import com.dasom.dasomServer.shared.error.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CaregiverService {

    private final CaregiverRepository caregiverRepository;
    private final FileStorageService fileStorageService;

    public CaregiverResponse getCaregiverById(Long caregiverId) {
        Caregiver caregiver = caregiverRepository.findById(caregiverId)
                .orElseThrow(() -> new UserNotFoundException("생활지원사를 찾을 수 없습니다. id=" + caregiverId));
        return toResponse(caregiver);
    }

    public CaregiverResponse getCaregiverByLoginId(String loginId) {
        Caregiver caregiver = caregiverRepository.findByLoginId(loginId)
                .orElseThrow(() -> new UserNotFoundException("생활지원사를 찾을 수 없습니다. loginId=" + loginId));
        return toResponse(caregiver);
    }

    public CaregiverResponse getCaregiverBySilverLoginId(String silverLoginId) {
        Caregiver caregiver = caregiverRepository.findBySilverLoginId(silverLoginId)
                .orElseThrow(() -> new UserNotFoundException("해당 어르신의 담당 생활지원사를 찾을 수 없습니다. silverLoginId=" + silverLoginId));
        return toResponse(caregiver);
    }

    private CaregiverResponse toResponse(Caregiver caregiver) {
        String fileName = caregiver.getProfileImageUrl();

        String fullUrl = (fileName != null) ? fileStorageService.toUrl(fileName) : null;

        return CaregiverResponse.of(caregiver, fullUrl);
    }
}
