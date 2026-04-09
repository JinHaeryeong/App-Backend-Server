package com.dasom.dasomServer.health.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "ten_minutes_health_logs",
        indexes = {
                @Index(name = "idx_silver_log_date", columnList = "silver_id, log_date")
        }
)
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
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
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime logDate;

    private Integer totalSleepMin;
    private Integer deepMin;
    private Integer lightMin;
    private Integer remMin;
    private Integer wakeMin;

    @Transient
    private boolean isDeepSleep;
    @Transient
    private boolean isRemSleep;
    @Transient
    private boolean isLightSleep;
    @Transient
    private boolean isAwakeSleep;

    /**
     * HealthDataRequest로부터 HealthLog 생성
     * 기본값 처리 및 null 방어 로직을 도메인 안으로 캡슐화
     */
    public static HealthLog from(
            String silverId,
            Integer heartRate,
            int walkingSteps,
            double totalCaloriesBurned,
            int spo2,
            LocalDateTime logDate,
            Long sleepDurationMin,
            Long sleepStageDeepMin,
            Long sleepStageLightMin,
            Long sleepStageRemMin,
            Long sleepStageWakeMin
    ) {
        return HealthLog.builder()
                .silverId(silverId)
                .heartRate(heartRate)
                .stepCount(walkingSteps)
                .caloriesBurned(totalCaloriesBurned)
                .oxygen(spo2 > 0 ? (double) spo2 : 98.0)
                .logDate(logDate)
                .totalSleepMin(sleepDurationMin  != null ? sleepDurationMin.intValue()  : 0)
                .deepMin(sleepStageDeepMin        != null ? sleepStageDeepMin.intValue()  : 0)
                .lightMin(sleepStageLightMin      != null ? sleepStageLightMin.intValue() : 0)
                .remMin(sleepStageRemMin          != null ? sleepStageRemMin.intValue()   : 0)
                .wakeMin(sleepStageWakeMin        != null ? sleepStageWakeMin.intValue()  : 0)
                .build();
    }

    /**
     * 결측 구간 보간용 HealthLog 생성 (이전 로그 기반)
     */
    public static HealthLog filledFrom(HealthLog lastLog, LocalDateTime fillTime) {
        return HealthLog.builder()
                .silverId(lastLog.getSilverId())
                .heartRate(lastLog.getHeartRate())
                .oxygen(lastLog.getOxygen())
                .stepCount(0)
                .caloriesBurned(0.0)
                .logDate(fillTime)
                .wakeMin(10)
                .build();
    }

    /**
     * AI 모델 추론 전, 수면 단계 분 데이터를 Boolean 플래그로 변환
     */
    public void prepareForAiInference() {
        this.isDeepSleep  = deepMin  != null && deepMin  > 0;
        this.isRemSleep   = remMin   != null && remMin   > 0;
        this.isLightSleep = lightMin != null && lightMin > 0;
        this.isAwakeSleep = wakeMin  != null && wakeMin  > 0;
    }
}
