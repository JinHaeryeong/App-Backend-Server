package com.dasom.dasomServer.caregiver.presentation.dto;

import com.dasom.dasomServer.caregiver.domain.Caregiver;

public record CaregiverResponse(
        String name,
        String tel,
        String gender,
        String affiliation,
        String profileImageUrl
) {
    public static CaregiverResponse of(Caregiver caregiver, String profileImageUrl) {
        return new CaregiverResponse(
                caregiver.getName(),
                caregiver.getTel(),
                caregiver.getGender(),
                caregiver.getAffiliation(),
                profileImageUrl
        );
    }
}
