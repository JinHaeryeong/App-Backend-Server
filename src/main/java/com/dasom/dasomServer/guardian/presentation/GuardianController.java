package com.dasom.dasomServer.guardian.presentation;

import com.dasom.dasomServer.guardian.presentation.dto.GuardianResponse;
import com.dasom.dasomServer.guardian.application.GuardianService;
import com.dasom.dasomServer.shared.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/guardians")
public class GuardianController {

    private final GuardianService guardianService;

    @GetMapping("/{silverId}")
    public ResponseEntity<ApiResponse<List<GuardianResponse>>> getGuardiansBySilverId(@PathVariable String silverId) {
        List<GuardianResponse> guardians = guardianService.getGuardiansForApp(silverId);
        return ResponseEntity.ok(ApiResponse.success("보호자 목록 조회 성공", guardians));
    }
}