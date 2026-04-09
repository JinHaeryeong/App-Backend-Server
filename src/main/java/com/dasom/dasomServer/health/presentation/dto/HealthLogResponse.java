package com.dasom.dasomServer.health.presentation.dto;

import com.dasom.dasomServer.health.domain.HealthLog;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

public record HealthLogResponse(
        String silverId,
        Integer heartRate,
        Integer stepCount,
        Double caloriesBurned,
        Double oxygen,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime logDate,
        Integer totalSleepMin,
        Integer deepMin,
        Integer lightMin,
        Integer remMin,
        Integer wakeMin
) {
    public static HealthLogResponse from(HealthLog log) {
        return new HealthLogResponse(
                log.getSilverId(),
                log.getHeartRate(),
                log.getStepCount(),
                log.getCaloriesBurned(),
                log.getOxygen(),
                log.getLogDate(),
                log.getTotalSleepMin(),
                log.getDeepMin(),
                log.getLightMin(),
                log.getRemMin(),
                log.getWakeMin()
        );
    }
}
