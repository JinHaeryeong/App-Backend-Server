package com.dasom.dasomServer.domain.health.entity;

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
    private LocalDateTime logDate;

    // 수면 관련 필드들
    private Integer totalSleepMin;
    private Integer deepMin;
    private Integer lightMin;
    private Integer remMin;
    private Integer wakeMin;


    @Transient // DB 컬럼은 안 만들지만 자바 코드에선 쓰겠다는 의미
    private boolean isDeepSleep;
    @Transient
    private boolean isRemSleep;
    @Transient
    private boolean isLightSleep;
    @Transient
    private boolean isAwakeSleep;

    /**
     * AI 모델에 데이터를 넣기 전, 숫자를 True/False로 바꿔주는 메서드
     */
    public void prepareForAiInference() {
        this.isDeepSleep = (this.deepMin != null && this.deepMin > 0);
        this.isRemSleep = (this.remMin != null && this.remMin > 0);
        this.isLightSleep = (this.lightMin != null && this.lightMin > 0);
        this.isAwakeSleep = (this.wakeMin != null && this.wakeMin > 0);
    }
}