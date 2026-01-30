package com.dasom.dasomServer.domain.health.entity;

public enum HealthStatus {
    DANGER("위험"),
    NORMAL("정상"),
    WARNING("주의");

    private final String koreanLabel;

    HealthStatus(String koreanLabel) {
        this.koreanLabel = koreanLabel;
    }

    public String getKoreanLabel() {
        return koreanLabel;
    }
}