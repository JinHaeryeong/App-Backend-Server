package com.dasom.dasomServer.Controller;


import com.dasom.dasomServer.DTO.ApiResponse;
import com.dasom.dasomServer.DTO.DailyHealthLogRequest;
import com.dasom.dasomServer.DTO.HealthRequest;
import com.dasom.dasomServer.Service.HealthService;
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
    private final HealthService healthService;

    @PostMapping("/data")
    public ResponseEntity<?> receiveAndAnalyzeHealthData(@RequestBody HealthRequest healthDataRequest) {
        log.info("유저 헬스 데이터 {}: {}", healthDataRequest.getSilverId(), healthDataRequest);

        ApiResponse<?> response = inferenceService.processAndAnalyze(healthDataRequest);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @PostMapping("/daily-log")
    public ResponseEntity<?> receiveDailyHealthLog(@RequestBody DailyHealthLogRequest summaryRequest) {
        log.info("유저 일일 혈압 요약 {}: {}", summaryRequest.getSilverId(), summaryRequest);

        // HealthService를 통해 일일 혈압 요약 정보를 DB에 저장하거나 처리합니다.
        ApiResponse<?> response = healthService.upsertDailyHealthLog(summaryRequest);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }
}
