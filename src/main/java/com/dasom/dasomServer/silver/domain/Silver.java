package com.dasom.dasomServer.silver.domain;

import com.dasom.dasomServer.caregiver.domain.Caregiver;
import com.dasom.dasomServer.shared.domain.BaseTimeEntity;
import com.dasom.dasomServer.silver.presentation.dto.SignupRequest;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;

@Entity
@Table(name = "silvers")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Silver extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "login_id", nullable = false, unique = true)
    private String loginId;

    @JsonIgnore
    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private char gender;

    private LocalDateTime birthday;

    @Column(name = "profile_image_url")
    private String profileImageUrl;

    @Column(columnDefinition = "DECIMAL(4,1) DEFAULT 0.0")
    private Double rhr = 0.0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "caregiver_id")
    private Caregiver caregiver;

    @Builder
    private Silver(String loginId, String password, String name, char gender,
                   LocalDateTime birthday, String profileImageUrl) {
        this.loginId = loginId;
        this.password = password;
        this.name = name;
        this.gender = gender;
        this.birthday = birthday;
        this.profileImageUrl = profileImageUrl;
    }

    public static Silver createSilver(SignupRequest request, String encodedPassword, String profileImageUrl) {
        return Silver.builder()
                .loginId(request.getLoginId())
                .password(encodedPassword)
                .name(request.getName())
                .gender(request.getGender())
                .birthday(request.getBirthday() != null ?
                        request.getBirthday().toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime() : null)
                .profileImageUrl(profileImageUrl)
                .build();
    }

    public void updateRhr(Double rhr) {
        this.rhr = rhr;
    }

    public void updateProfileImage(String newProfileImageUrl) {
        this.profileImageUrl = newProfileImageUrl;
    }

    public void checkPassword(PasswordEncoder passwordEncoder, String rawPassword) {
        if (!passwordEncoder.matches(rawPassword, this.password)) {
            throw new IllegalArgumentException("아이디 또는 비밀번호가 일치하지 않습니다.");
        }
    }

    public void assignCaregiver(Caregiver caregiver) {
        this.caregiver = caregiver;
    }
}