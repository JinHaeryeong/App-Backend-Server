package com.dasom.dasomServer.health.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "daily_health_logs")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class DailyHealthLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "silver_id", length = 50, nullable = false)
    private String silverId;

    private Double weight;

    @Column(name = "blood_sugar")
    private Integer bloodSugar;

    @Column(name = "body_temperature")
    private Double bodyTemperature;

    @Column(name = "sleep_score")
    private Integer sleepScore;

    @Column(name = "systolic_blood_pressure")
    private Integer systolicBloodPressure;

    @Column(name = "diastolic_blood_pressure")
    private Integer diastolicBloodPressure;

    @Column(name = "log_date")
    private LocalDate logDate;

    /**
     * DailyHealthLogRequest로부터 신규 DailyHealthLog 생성
     */
    public static DailyHealthLog from(
            String silverId,
            Double weight,
            Integer bloodSugar,
            Double bodyTemperature,
            Integer sleepScore,
            Integer systolicBloodPressure,
            Integer diastolicBloodPressure,
            LocalDate logDate
    ) {
        return DailyHealthLog.builder()
                .silverId(silverId)
                .weight(weight)
                .bloodSugar(bloodSugar)
                .bodyTemperature(bodyTemperature)
                .sleepScore(sleepScore)
                .systolicBloodPressure(systolicBloodPressure)
                .diastolicBloodPressure(diastolicBloodPressure)
                .logDate(logDate)
                .build();
    }

    /**
     * 기존 일일 건강 기록을 새 요청 값으로 갱신
     */
    public void update(Double weight, Integer bloodSugar, Double bodyTemperature,
                       Integer sleepScore, Integer systolicBloodPressure, Integer diastolicBloodPressure) {
        this.weight                 = weight;
        this.bloodSugar             = bloodSugar;
        this.bodyTemperature        = bodyTemperature;
        this.sleepScore             = sleepScore;
        this.systolicBloodPressure  = systolicBloodPressure;
        this.diastolicBloodPressure = diastolicBloodPressure;
    }
}
