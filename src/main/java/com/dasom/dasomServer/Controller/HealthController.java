package com.dasom.dasomServer.Controller;


import com.dasom.dasomServer.DTO.ApiResponse;
import com.dasom.dasomServer.DTO.HealthRequest;
import com.dasom.dasomServer.Service.LstmInferenceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/health")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:8080")
public class HealthController {

    private final LstmInferenceService inferenceService;
//    @PostMapping("/save")
//    public ResponseEntity<?> receiveHealthData(@RequestBody HealthRequest healthRequest) {
//
//        log.info(healthRequest.toString());
//
//
//
//        ApiResponse<?> response = healthService.saveHealthData(healthRequest);;
//
//        return ResponseEntity.ok(response);
//    }

    @PostMapping("/data")
    public ResponseEntity<?> receiveAndAnalyzeHealthData(@RequestBody HealthRequest healthDataRequest) {
        log.info("유저 헬스 데이터 {}: {}", healthDataRequest.getSilverId(), healthDataRequest);

        ApiResponse<?> response = inferenceService.processAndAnalyze(healthDataRequest);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }
}
