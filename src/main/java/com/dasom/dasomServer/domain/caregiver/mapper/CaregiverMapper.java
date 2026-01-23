package com.dasom.dasomServer.domain.caregiver.mapper;

import com.dasom.dasomServer.domain.caregiver.dto.Caregiver;
import org.apache.ibatis.annotations.Mapper;

@Mapper
// 예시: CaregiverMapper.java
public interface CaregiverMapper {
    Caregiver findCaregiverById(Long id);
    Caregiver findCaregiverByLoginId(String loginId);
    Caregiver findCaregiverBySilverLoginId(String silverLoginId);
}