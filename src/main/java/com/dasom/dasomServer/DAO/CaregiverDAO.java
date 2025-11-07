package com.dasom.dasomServer.DAO;

import com.dasom.dasomServer.DTO.Caregiver;

// 예시: CaregiverDAO.java
public interface CaregiverDAO {
    // 기존에 있는 메소드
    Caregiver findCaregiverByLoginId(String loginId);

    // 새로 추가할 메소드
    Caregiver findCaregiverById(Long id);
}