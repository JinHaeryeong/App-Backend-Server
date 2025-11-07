package com.dasom.dasomServer.DTO;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.sql.Timestamp;
import java.util.List;

@Data // Lombok 사용 가정
@NoArgsConstructor
@AllArgsConstructor
public class Caregiver {

    private Long id; // Primary Key
    private String loginId;
    private String password;
    private String name;
    private String affiliation;
    private String tel;
    private String gender;
    private Timestamp createdAt;
    private String refreshToken;
    private String role; // 기본값 'caregiver'
    private List<CaregiverImage> images;
}