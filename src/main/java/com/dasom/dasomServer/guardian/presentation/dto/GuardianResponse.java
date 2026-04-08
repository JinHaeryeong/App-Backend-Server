package com.dasom.dasomServer.guardian.presentation.dto;

import com.dasom.dasomServer.guardian.domain.Guardian;

public record GuardianResponse(
        String name,
        String tel,
        String relationship,
        String address,
        String profileImageUrl
) {
    public static GuardianResponse of(Guardian guardian, String fullUrl) {
        return new GuardianResponse(
                guardian.getName(),
                guardian.getTel(),
                guardian.getRelationship(),
                guardian.getAddress(),
                fullUrl
        );
    }
}