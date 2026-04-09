package com.dasom.dasomServer.health.presentation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
public class DailyHealthLogRequest {

    @NotBlank(message = "silverId는 필수입니다")
    private String silverId;

    private Integer systolicBloodPressure;
    private Integer diastolicBloodPressure;
    private Integer bloodSugar;
    private Double weight;
    private Double bodyTemperature;
    private Integer sleepScore;

    @NotNull(message = "logDate는 필수입니다")
    private LocalDate logDate;
}
