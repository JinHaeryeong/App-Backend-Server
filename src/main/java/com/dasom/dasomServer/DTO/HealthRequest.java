package com.dasom.dasomServer.DTO;


import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
//@AllArgsConstructor
public class HealthRequest {
    private String silverId;
    private int walkingSteps;
    private double totalCaloriesBurned;
    private int spo2;
    private Long heartRateAvg;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime logDate;


//    private int totalSleepDurationMin;
    private Long sleepDurationMin;
    private Long sleepStageWakeMin;
    private Long sleepStageDeepMin;
    private Long sleepStageRemMin;
    private Long sleepStageLightMin;

    // LSTM Boolean 입력 필드 (서버에서 Minute을 변환)
    private boolean isDeepSleep;
    private boolean isRemSleep;
    private boolean isLightSleep;
    private boolean isAwakeSleep;

    // 현재 데이터 상태 필드 (미착용/수면 등)
    private String currentSleepStage;
}
