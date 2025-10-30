package com.dasom.dasomServer.Controller;


import com.dasom.dasomServer.DTO.ApiResponse;
import com.dasom.dasomServer.DTO.HealthDataRequest;
import com.dasom.dasomServer.Service.LstmInferenceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/health")
@RequiredArgsConstructor
public class HealthController {

    private final LstmInferenceService inferenceService;

    @PostMapping("/data")
    public ResponseEntity<?> receiveAndAnalyzeHealthData(@RequestBody HealthDataRequest healthDataRequest) {
        log.info("유저 데이터 {}: {}", healthDataRequest.getSilverId(), healthDataRequest);

        ApiResponse<?> response = inferenceService.processAndAnalyze(healthDataRequest);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }
}
