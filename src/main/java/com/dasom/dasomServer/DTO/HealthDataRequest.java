package com.dasom.dasomServer.DTO;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Data
//@AllArgsConstructor
public class HealthDataRequest {
    private String silverId;
    private int age;
    private Character gender;
    private double rhr;
    private int walkingSteps;
    private double totalCaloriesBurned;
    private int spo2;
    private double heartRateAvg;
    private LocalDateTime logDate;

    private int totalSleepDurationMin;
    private int sleepDurationMin;
    private int sleepStageWakeMin;
    private int sleepStageDeepMin;
    private int sleepStageRemMin;
    private int sleepStageLightMin;
}
