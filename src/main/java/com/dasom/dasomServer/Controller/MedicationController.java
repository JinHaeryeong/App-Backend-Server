package com.dasom.dasomServer.Controller;

import com.dasom.dasomServer.DTO.SilverMedication;
import com.dasom.dasomServer.Service.MedicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/medications")
public class MedicationController {

    @Autowired
    private MedicationService medicationService;

    @GetMapping("/{silverId}")
    public ResponseEntity<List<SilverMedication>> getMedications(@PathVariable String silverId) {
        List<SilverMedication> medications = medicationService.getMedicationsBySilverId(silverId);

        // HTTP 200 OK와 JSON 데이터를 Android에 응답합니다.
        return ResponseEntity.ok(medications);
    }
}