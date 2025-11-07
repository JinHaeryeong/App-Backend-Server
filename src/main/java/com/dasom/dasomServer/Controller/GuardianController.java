package com.dasom.dasomServer.Controller;

import com.dasom.dasomServer.DTO.GuardianResponseDTO;
import com.dasom.dasomServer.Service.GuardianService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/guardians") // API 기본 경로
public class GuardianController {

    private final GuardianService guardianService;

    @Autowired
    public GuardianController(GuardianService guardianService) {
        this.guardianService = guardianService;
    }

    @GetMapping("/{silverId}")
    public ResponseEntity<List<GuardianResponseDTO>> getGuardiansBySilverId(@PathVariable String silverId) {

        List<GuardianResponseDTO> guardians = guardianService.getGuardiansForApp(silverId);

        return ResponseEntity.ok(guardians); // HTTP 200 OK와 함께 리스트 반환
    }
}