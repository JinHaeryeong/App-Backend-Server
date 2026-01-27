package com.dasom.dasomServer.domain.silver.entity;

import com.dasom.dasomServer.domain.caregiver.entity.Caregiver;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "silvers")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Silver {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // PK

    @Column(name = "login_id", nullable = false, unique = true)
    private String loginId;

    @JsonIgnore
    private String password;
    private String name;
    private char gender;

    private LocalDateTime birthday;

    @Column(columnDefinition = "DECIMAL(4,1) DEFAULT 0.0")
    private Double rhr;

    // 이미지와의 관계 (1:N)
    @OneToMany(mappedBy = "silver", cascade = CascadeType.ALL)
    @Builder.Default
    private List<SilverImage> images = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "caregiver_id") // DB 컬럼명에 맞춰주세요
    private Caregiver caregiver;
}