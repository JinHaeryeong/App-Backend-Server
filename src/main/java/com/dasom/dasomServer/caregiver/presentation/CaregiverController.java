package com.dasom.dasomServer.caregiver.presentation;

import com.dasom.dasomServer.caregiver.application.CaregiverService;
import com.dasom.dasomServer.caregiver.presentation.dto.CaregiverResponse;
import com.dasom.dasomServer.shared.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/caregivers")
@RequiredArgsConstructor
public class CaregiverController {

    private final CaregiverService caregiverService;

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CaregiverResponse>> getCaregiverById(
            @PathVariable Long id) {
        CaregiverResponse response = caregiverService.getCaregiverById(id);
        return ResponseEntity.ok(ApiResponse.success("생활지원사 조회 성공", response));
    }

    @GetMapping("/login/{loginId}")
    public ResponseEntity<ApiResponse<CaregiverResponse>> getCaregiverByLoginId(
            @PathVariable String loginId) {
        CaregiverResponse response = caregiverService.getCaregiverByLoginId(loginId);
        return ResponseEntity.ok(ApiResponse.success("생활지원사 조회 성공", response));
    }

    @GetMapping("/by-silver/{silverLoginId}")
    public ResponseEntity<ApiResponse<CaregiverResponse>> getCaregiverBySilverLoginId(
            @PathVariable String silverLoginId) {
        CaregiverResponse response = caregiverService.getCaregiverBySilverLoginId(silverLoginId);
        return ResponseEntity.ok(ApiResponse.success("담당 생활지원사 조회 성공", response));
    }
}
