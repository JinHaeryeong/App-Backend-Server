package com.dasom.dasomServer.caregiver.domain;

import com.dasom.dasomServer.shared.domain.BaseTimeEntity;
import com.dasom.dasomServer.silver.domain.Silver;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "caregivers")
public class Caregiver extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "login_id", nullable = false, unique = true)
    private String loginId;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String name;

    private String affiliation;
    private String tel;
    private String gender;

    @Column(name = "refresh_token")
    private String refreshToken;

    @Builder.Default
    private String role = "caregiver";

    @Column(name = "profile_image_url")
    private String profileImageUrl;

    @OneToMany(mappedBy = "caregiver")
    @Builder.Default
    private List<Silver> silvers = new ArrayList<>();

    public void updateRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}