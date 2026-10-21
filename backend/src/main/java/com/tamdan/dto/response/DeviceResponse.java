package com.tamdan.dto.response;

import java.time.Instant;

public record DeviceResponse(Long id, String displayName, String platform, String appVersion, Boolean active, Instant lastSeenAt) {}
