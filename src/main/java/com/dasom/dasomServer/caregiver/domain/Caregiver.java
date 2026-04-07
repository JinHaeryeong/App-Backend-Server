package com.dasom.dasomServer.caregiver.domain;

import com.dasom.dasomServer.silver.domain.Silver;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Entity
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

    @OneToMany(mappedBy = "caregiver")
    @Builder.Default
    private List<Silver> silvers = new ArrayList<>();

    @OneToMany(mappedBy = "caregiver", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<CaregiverImage> images = new ArrayList<>();
}
