package com.dasom.dasomServer.health.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum HealthStatus {
    DANGER("위험"),
    NORMAL("정상"),
    WARNING("주의");

    private final String koreanLabel;
}
