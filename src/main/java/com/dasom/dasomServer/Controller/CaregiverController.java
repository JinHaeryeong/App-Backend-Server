package com.dasom.dasomServer.Controller;

import com.dasom.dasomServer.Service.CaregiverService;
import com.dasom.dasomServer.DTO.CaregiverlResponseDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/caregivers")
public class CaregiverController {

    private final CaregiverService caregiverService;

    @Autowired
    public CaregiverController(CaregiverService caregiverService) {
        this.caregiverService = caregiverService;
    }

    // ------------------------- ID 기반 조회 엔드포인트 -------------------------
    // 요청 예시: GET /api/caregivers/details/id/1
    @GetMapping("/details/id/{id}")
    public ResponseEntity<CaregiverlResponseDTO> getCaregiverDetailsById(@PathVariable Long id) {
        CaregiverlResponseDTO response = caregiverService.getCaregiverDetails(id);

        if (response == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(response);
    }

    // ------------------------- Login ID 기반 조회 엔드포인트 -------------------------
    // 요청 예시: GET /api/caregivers/details/login/yys1234
    @GetMapping("/details/login/{loginId}")
    public ResponseEntity<CaregiverlResponseDTO> getCaregiverDetailsByLoginId(@PathVariable String loginId) {

        CaregiverlResponseDTO response = caregiverService.getCaregiverDetailsForApp(loginId);

        if (response == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(response);
    }

    // ------------------- 보호대상자(Silver)의 로그인 ID를 기반으로 담당 요양보호사(Caregiver) 정보를 조회 -------------------
    //(예: /api/caregivers/by-silver/ppp1234)
    @GetMapping("/by-silver/{silverLoginId}")
    public ResponseEntity<CaregiverlResponseDTO> getCaregiverBySilverId(@PathVariable String silverLoginId) {

        CaregiverlResponseDTO caregiverDTO = caregiverService.getCaregiverBySilverLoginId(silverLoginId);

        if (caregiverDTO != null) {
            return ResponseEntity.ok(caregiverDTO);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}