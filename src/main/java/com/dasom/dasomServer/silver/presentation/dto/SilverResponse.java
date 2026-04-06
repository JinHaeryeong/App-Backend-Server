package com.dasom.dasomServer.silver.presentation.dto;

import com.dasom.dasomServer.silver.domain.Silver;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class SilverResponse {
    private Long id;
    private String loginId;
    private String name;
    private char gender;
    private LocalDateTime birthday;
    private Long caregiverId;
    private String profileImageUrl;

    public static SilverResponse from(Silver silver) {
        return SilverResponse.builder()
                .id(silver.getId())
                .loginId(silver.getLoginId())
                .name(silver.getName())
                .gender(silver.getGender())
                .birthday(silver.getBirthday())
                .caregiverId(silver.getCaregiver() != null ? silver.getCaregiver().getId() : null)
                .profileImageUrl(silver.getProfileImageUrl())
                .build();
    }
}