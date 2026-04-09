package com.dasom.dasomServer.health.presentation.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class HealthDataRequest {

    @NotBlank(message = "silverId는 필수입니다")
    private String silverId;

    private int walkingSteps;
    private double totalCaloriesBurned;
    private int spo2;
    private Long heartRateAvg;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime logDate;

    private Long sleepDurationMin;
    private Long sleepStageWakeMin;
    private Long sleepStageDeepMin;
    private Long sleepStageRemMin;
    private Long sleepStageLightMin;

    private String currentSleepStage;
}
