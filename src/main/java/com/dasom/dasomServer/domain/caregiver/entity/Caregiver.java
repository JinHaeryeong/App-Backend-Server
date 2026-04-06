package com.dasom.dasomServer.domain.caregiver.entity;

import com.dasom.dasomServer.silver.domain.Silver;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "caregivers")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Caregiver {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "login_id", nullable = false, unique = true)
    private String loginId;

    private String password;
    private String name;
    private String affiliation;
    private String tel;
    private String gender;

    @Column(name = "refresh_token")
    private String refreshToken;

    @Builder.Default
    private String role = "caregiver";

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Timestamp createdAt;

    // 1:N 관계 (생활지원사 1 : 어르신 N)
    @OneToMany(mappedBy = "caregiver")
    @Builder.Default
    private List<Silver> silvers = new ArrayList<>();

    // 1:N 관계 (생활지원사 1 : 이미지 N)
    @OneToMany(mappedBy = "caregiver", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<CaregiverImage> images = new ArrayList<>();
}