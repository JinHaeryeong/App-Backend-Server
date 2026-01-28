package com.dasom.dasomServer.domain.health.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "health_result_logs")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class HealthResultLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "silver_id", length = 50, nullable = false)
    private String silverId;

    @Enumerated(EnumType.STRING) // Enum을 문자열로 저장
    @Column(columnDefinition = "ENUM('위험', '정상', '주의')")
    private HealthStatus label;

    @Column(name = "log_date")
    private LocalDateTime logDate;
}

// 상태 관리를 위한 Enum
