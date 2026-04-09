package com.dasom.dasomServer.health.domain;

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

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "ENUM('DANGER', 'NORMAL', 'WARNING')")
    private HealthStatus label;

    @Column(name = "log_date")
    private LocalDateTime logDate;
}
