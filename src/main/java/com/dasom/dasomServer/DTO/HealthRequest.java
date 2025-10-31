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
    private double heartRateAvg;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    private LocalDateTime logDate;


//    private int totalSleepDurationMin;
    private int sleepDurationMin;
    private int sleepStageWakeMin;
    private int sleepStageDeepMin;
    private int sleepStageRemMin;
    private int sleepStageLightMin;
}
