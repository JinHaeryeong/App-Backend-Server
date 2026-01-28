package com.dasom.dasomServer.domain.silver.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class SilverResponse {
    private Long id;
    private String loginId;
    private String name;
    private char gender;
    private LocalDateTime birthday;
    private Long caregiverId; // 생활지원사 객체 대신 ID만 깔끔하게 전달
    private List<String> imageUrls;
}