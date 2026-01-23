package com.dasom.dasomServer.domain.health.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

//@Entity
@Table(name = "ten_minutes_health_logs")
@Getter
@NoArgsConstructor
public class HealthLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "silver_id", nullable = false)
    private String silverId;

    @Column(name = "heart_rate")
    private Integer heartRate;

    @Column(name = "step_count")
    private Integer stepCount;

    @Column(name = "calories_burned")
    private Double caloriesBurned;

    private Double oxygen;

    @Column(name = "log_date")
    private LocalDateTime logDate;

    // 수면 관련 필드들
    private Integer totalSleepMin;
    private Integer deepMin;
    private Integer lightMin;
    private Integer remMin;
    private Integer wakeMin;
}