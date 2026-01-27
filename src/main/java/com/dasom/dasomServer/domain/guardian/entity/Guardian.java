package com.dasom.dasomServer.domain.guardian.entity;

import com.dasom.dasomServer.domain.silver.entity.Silver;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "guardians") // 실제 DB 테이블명
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED) // JPA 기본 생성자
public class Guardian {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // id

    // String 대신 Silver 객체로 연관관계 매핑 (JOIN을 쓰기 위해)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "silver_id", referencedColumnName = "login_id") // DB 컬럼명에 맞춰 매핑
    private Silver silver;

    @Column(nullable = false)
    private String name; // g_name

    private String tel; // g_tel

    private String relationship; // g_relationship

    private String address; // g_address

    @CreationTimestamp // 생성 시각 자동 저장
    @Column(name = "created_at", updatable = false)
    private Timestamp createdAt;

    // GuardianImage와의 1:N 양방향 관계 설정
    // mappedBy: 자식 엔티티(GuardianImage)에 있는 필드명
    @OneToMany(mappedBy = "guardian", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<GuardianImage> images = new ArrayList<>();
}