package com.dasom.dasomServer.domain.user.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "silvers")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Silver {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // PK

    @Column(name = "login_id", nullable = false, unique = true)
    private String loginId;

    private String password;
    private String name;
    private char gender;
    private java.sql.Date birthday;

    @Column(columnDefinition = "DECIMAL(4,1) DEFAULT 0.0")
    private Double rhr;

    // 실시간 건강 데이터와의 관계 (1:N)
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<UserImage> images = new ArrayList<>();
}