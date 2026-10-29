package com.tamdan.dto.response;

import java.time.Instant;

public record DeviceHealthResponse(
    Long deviceId,
    Long childId,
    String schemaVersion,
    Instant reportedAt,
    String monitoringState,
    String syncState,
    String policyVersion,
    Integer queuedEventCount,
    Instant observedAt
) {}
