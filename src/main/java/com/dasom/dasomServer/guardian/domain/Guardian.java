package com.dasom.dasomServer.guardian.domain;

import com.dasom.dasomServer.shared.domain.BaseTimeEntity;
import com.dasom.dasomServer.silver.domain.Silver;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "guardians")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Guardian extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "silver_id", referencedColumnName = "login_id")
    private Silver silver;

    @Column(nullable = false)
    private String name;

    private String tel;
    private String relationship;
    private String address;

    @Column(name = "profile_image_url") // 이미지 테이블 대신 컬럼으로 통합
    private String profileImageUrl;
}