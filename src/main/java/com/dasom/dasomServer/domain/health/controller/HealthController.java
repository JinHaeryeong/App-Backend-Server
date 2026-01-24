package com.dasom.dasomServer.domain.health.controller;


import com.dasom.dasomServer.domain.health.entity.HealthLog;
import com.dasom.dasomServer.global.common.ApiResponse;
import com.dasom.dasomServer.DTO.DailyHealthLogRequest;
import com.dasom.dasomServer.DTO.HealthRequest;
import com.dasom.dasomServer.domain.health.service.HealthService;
import com.dasom.dasomServer.infra.ai.LstmInferenceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

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

    // 테스트용 api
    @GetMapping("/sequence-test")
    public ResponseEntity<?> getSequenceTestData(@RequestParam String silverId) {
        log.info("JPA 기반 최적화 쿼리 실행: silverId = {}", silverId);

        // 시간을 계산해서 넘길 필요 없이, JPA가 최신 6개를 알아서 가져옴
        List<HealthLog> response = healthService.findRecentLogs(silverId);

        return ResponseEntity.ok(ApiResponse.success("조회 성공", response));
    }

    @PostMapping("/daily-log")
    public ResponseEntity<?> receiveDailyHealthLog(@RequestBody DailyHealthLogRequest summaryRequest) {
        log.info("유저 일일 요약 {}: {}", summaryRequest.getSilverId(), summaryRequest);

        // HealthService를 통해 일일 혈압 요약 정보를 DB에 저장하거나 처리합니다.
        ApiResponse<?> response = healthService.upsertDailyHealthLog(summaryRequest);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }
}
