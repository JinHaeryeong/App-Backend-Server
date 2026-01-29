package com.dasom.dasomServer.domain.health.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "daily_health_logs")
@Getter
@Builder
@NoArgsConstructor
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

}
