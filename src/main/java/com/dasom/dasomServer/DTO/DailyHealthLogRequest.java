package com.dasom.dasomServer.DTO;

import lombok.Data;

import java.time.LocalDate;

@Data
public class DailyHealthLogRequest {
    private String silverId;

    // 혈압 (INT)
    private Integer systolicBloodPressure;
    private Integer diastolicBloodPressure;

    // 혈당 (INT)
    private Integer bloodSugar;

    // 체중 (DECIMAL)
    private Double weight;

    // 체온 (DECIMAL)
    private Double bodyTemperature;

    // 수면 점수 (INT)
    private Integer sleepScore;

    private LocalDate logDate;
}