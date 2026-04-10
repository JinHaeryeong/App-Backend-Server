package com.dasom.dasomServer.health.presentation;

import com.dasom.dasomServer.health.application.HealthService;
import com.dasom.dasomServer.health.domain.DailyHealthLog;
import com.dasom.dasomServer.health.presentation.dto.DailyHealthLogRequest;
import com.dasom.dasomServer.health.presentation.dto.HealthDataRequest;
import com.dasom.dasomServer.health.presentation.dto.HealthLogResponse;
import com.dasom.dasomServer.infra.ai.HealthAnalysisPipeline;
import com.dasom.dasomServer.shared.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/health")
@RequiredArgsConstructor
public class HealthController {

    private final HealthService healthService;
    private final HealthAnalysisPipeline healthAnalysisPipeline;
    
    // 리팩토링하면서 API 엔드포인트의 이름들을 좀 더 명확하게 바꿨음

    /**
     * POST /api/health/logs
     * 10분 단위 생체 신호 수집 및 AI 분석 트리거
     */
    @PostMapping("/logs")
    public ResponseEntity<ApiResponse<?>> createHealthLog(
            @Valid @RequestBody HealthDataRequest request) {
        log.info("건강 데이터 수신: silverId={}", request.getSilverId());

        ApiResponse<?> response = healthAnalysisPipeline.collectAndPublish(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * GET /api/health/logs?silverId={silverId}
     * 최근 건강 로그 6개 조회
     */
    @GetMapping("/logs")
    public ResponseEntity<ApiResponse<List<HealthLogResponse>>> getRecentHealthLogs(
            @RequestParam String silverId) {
        log.info("최근 건강 로그 조회: silverId={}", silverId);

        List<HealthLogResponse> logs = healthService.findRecentLogs(silverId)
                .stream()
                .map(HealthLogResponse::from)
                .toList();

        return ResponseEntity.ok(ApiResponse.success("조회 성공", logs));
    }

    /**
     * PUT /api/health/daily-logs
     * 일일 건강 요약 데이터 저장 또는 갱신 (upsert)
     */
    @PutMapping("/daily-logs")
    public ResponseEntity<ApiResponse<DailyHealthLog>> upsertDailyHealthLog(
            @Valid @RequestBody DailyHealthLogRequest request) {
        log.info("일일 건강 로그 upsert: silverId={}", request.getSilverId());

        DailyHealthLog saved = healthService.upsertDailyHealthLog(request);

        return ResponseEntity.ok(ApiResponse.success("일일 건강 데이터 저장 완료", saved));
    }
}
